package com.cbo.notes.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.session.UserSession
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.ReminderPriority
import com.cbo.notes.domain.model.ReminderRepeat
import com.cbo.notes.domain.model.TodoItem
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.usecase.CreateNoteUseCase
import com.cbo.notes.domain.usecase.CreateTagUseCase
import com.cbo.notes.domain.usecase.CreateCategoryUseCase
import com.cbo.notes.domain.usecase.GetCategoriesUseCase
import com.cbo.notes.domain.usecase.GetNoteByIdUseCase
import com.cbo.notes.domain.usecase.GetNotesUseCase
import com.cbo.notes.domain.usecase.GetTagsUseCase
import com.cbo.notes.domain.usecase.AddNoteLinkUseCase
import com.cbo.notes.domain.usecase.DeleteAllLinksForNoteUseCase
import com.cbo.notes.domain.usecase.GetBacklinksForNoteUseCase
import com.cbo.notes.domain.usecase.RemoveReminderUseCase
import com.cbo.notes.domain.usecase.SetReminderUseCase
import com.cbo.notes.domain.usecase.UpdateNoteUseCase
import com.cbo.notes.domain.usecase.GetNoteTemplatesUseCase
import com.cbo.notes.domain.usecase.AddNoteTemplateUseCase
import com.cbo.notes.domain.model.NoteTemplate
import com.cbo.notes.worker.ReminderScheduler
import com.cbo.notes.presentation.component.getAudioPath
import com.cbo.notes.presentation.component.isAudioAttachment
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userSession: UserSession,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val setReminderUseCase: SetReminderUseCase,
    private val removeReminderUseCase: RemoveReminderUseCase,
    private val reminderScheduler: ReminderScheduler,
    private val locationReminderManager: com.cbo.notes.worker.LocationReminderManager,
    private val getNoteTemplatesUseCase: GetNoteTemplatesUseCase,
    private val addNoteTemplateUseCase: AddNoteTemplateUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteLinkUseCase: AddNoteLinkUseCase,
    private val deleteAllLinksForNoteUseCase: DeleteAllLinksForNoteUseCase,
    private val getBacklinksForNoteUseCase: GetBacklinksForNoteUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val noteId: Int = savedStateHandle.get<Int>("noteId") ?: 0
    private val isEditing = noteId != 0

    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val categories = getCategoriesUseCase().first()
                val tags = getTagsUseCase().first()

                // Fetch templates
                val userId = userSession.currentUser.first()?.id ?: -1
                var templates = getNoteTemplatesUseCase(userId).first()
                if (templates.isEmpty() && userId != -1) {
                    val defaultTemplates = listOf(
                        NoteTemplate(userId = userId, name = "Zettelkasten", content = "# \n\n**Links:**\n"),
                        NoteTemplate(userId = userId, name = "Literature Note", content = "# \n\n**Author:** \n**Year:** \n\n**Summary:**\n"),
                        NoteTemplate(userId = userId, name = "Fleeting Note", content = "...")
                    )
                    defaultTemplates.forEach { addNoteTemplateUseCase(it) }
                    templates = getNoteTemplatesUseCase(userId).first()
                }
                
                // Fetch all notes for linking
                val allNotes = getNotesUseCase().first()

                if (isEditing) {
                    val note = getNoteByIdUseCase(noteId)
                    val backlinks = getBacklinksForNoteUseCase(noteId).first()
                    if (note != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                title = note.title,
                                content = note.content,
                                selectedCategory = note.category,
                                selectedTags = note.tags,
                                availableCategories = categories,
                                availableTags = tags,
                                availableTemplates = templates,
                                allNotes = allNotes,
                                backlinks = backlinks,
                                originalNote = note,
                                reminderTime = note.reminderTime,
                                reminderRepeat = note.reminderRepeat ?: ReminderRepeat.NONE,
                                reminderPriority = note.reminderPriority ?: ReminderPriority.DEFAULT,
                                reminderLatitude = note.reminderLatitude,
                                reminderLongitude = note.reminderLongitude,
                                reminderLocationName = note.reminderLocationName,
                                reminderRadius = note.reminderRadius,
                                isLocationReminderEnabled = note.isLocationReminderEnabled,
                                attachments = note.attachments,
                                color = note.color,
                                todos = note.todos
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(isLoading = false, errorMessage = "Note not found")
                        }
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            availableCategories = categories,
                            availableTags = tags,
                            availableTemplates = templates,
                            allNotes = allNotes
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = "Failed to load data: ${e.message}")
                }
            }
        }
    }

    fun updateTitle(title: String) {
        if (_uiState.value.title != title) {
            _uiState.update { it.copy(title = title, hasUnsavedChanges = true) }
        }
    }

    fun updateContent(content: String) {
        if (_uiState.value.content != content) {
            val linkSearchQuery = extractLinkQuery(content)
            _uiState.update { 
                it.copy(
                    content = content, 
                    hasUnsavedChanges = true,
                    linkSearchQuery = linkSearchQuery,
                    showLinkSuggestions = linkSearchQuery != null
                ) 
            }
        }
    }

    private fun extractLinkQuery(content: String): String? {
        // Find if the cursor is currently typing a link `[[...` without a closing `]]`
        val lastOpen = content.lastIndexOf("[[")
        val lastClose = content.lastIndexOf("]]")
        
        if (lastOpen != -1 && lastOpen > lastClose) {
            return content.substring(lastOpen + 2)
        }
        return null
    }

    fun insertLink(note: Note) {
        _uiState.update { state ->
            val lastOpen = state.content.lastIndexOf("[[")
            if (lastOpen != -1) {
                val newContent = state.content.substring(0, lastOpen) + "[[${note.title}]] "
                state.copy(
                    content = newContent,
                    hasUnsavedChanges = true,
                    showLinkSuggestions = false,
                    linkSearchQuery = null
                )
            } else {
                state
            }
        }
    }

    fun selectCategory(category: Category?) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.update { it.copy(selectedCategory = category, hasUnsavedChanges = true, categoryInputText = "") }
        } else {
            _uiState.update { it.copy(categoryInputText = "") }
        }
    }

    fun toggleTag(tag: Tag) {
        val currentTags = _uiState.value.selectedTags.toMutableList()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        Log.d("EditNoteViewModel", "toggleTag current tags: $currentTags")
        _uiState.update { it.copy(selectedTags = currentTags, hasUnsavedChanges = true, tagInputText = "") }
    }

    fun selectTags(tags: List<Tag>) {
        if (_uiState.value.selectedTags != tags) {
            _uiState.update { it.copy(selectedTags = tags, hasUnsavedChanges = true) }
        }
    }

    fun saveNote() {
        val currentState = _uiState.value
        
        // If the note is completely empty and it's a new note, just navigate back without saving
        if (currentState.title.isBlank() && 
            currentState.content.isBlank() && 
            currentState.attachments.isEmpty() &&
            currentState.todos.isEmpty() &&
            !isEditing) {
            _navigationEvents.trySend(NavigationEvent.NavigateBack)
            return
        }

        // Determine title
        val finalTitle = if (currentState.title.isNotBlank()) {
            currentState.title
        } else if (currentState.content.isNotBlank()) {
            val firstLine = currentState.content.lines().firstOrNull { it.isNotBlank() } ?: "İsimsiz Not"
            if (firstLine.length > 40) firstLine.take(40) + "..." else firstLine
        } else {
            "İsimsiz Not"
        }


        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                _uiState.update { it.copy(isSaving = true) }

                val noteToSave = if (isEditing) {
                    currentState.originalNote!!.copy(
                        title = finalTitle,
                        content = currentState.content,
                        category = currentState.selectedCategory,
                        tags = currentState.selectedTags,
                        reminderTime = currentState.reminderTime,
                        reminderRepeat = currentState.reminderRepeat,
                        reminderPriority = currentState.reminderPriority,
                        reminderLatitude = currentState.reminderLatitude,
                        reminderLongitude = currentState.reminderLongitude,
                        reminderLocationName = currentState.reminderLocationName,
                        reminderRadius = currentState.reminderRadius,
                        isLocationReminderEnabled = currentState.isLocationReminderEnabled,
                        attachments = currentState.attachments,
                        color = currentState.color,
                        todos = currentState.todos
                    )
                } else {
                    Note(
                        userId = user.id,
                        title = finalTitle,
                        content = currentState.content,
                        category = currentState.selectedCategory,
                        tags = currentState.selectedTags,
                        reminderTime = currentState.reminderTime,
                        reminderRepeat = currentState.reminderRepeat,
                        reminderPriority = currentState.reminderPriority,
                        reminderLatitude = currentState.reminderLatitude,
                        reminderLongitude = currentState.reminderLongitude,
                        reminderLocationName = currentState.reminderLocationName,
                        reminderRadius = currentState.reminderRadius,
                        isLocationReminderEnabled = currentState.isLocationReminderEnabled,
                        attachments = currentState.attachments,
                        color = currentState.color,
                        todos = currentState.todos
                    )
                }

                val result = if (isEditing) {
                    updateNoteUseCase(noteToSave)
                } else {
                    createNoteUseCase(noteToSave)
                }

                result.fold(
                    onSuccess = { savedNote ->
                        // Extract and save links
                        handleLinksExtraction(savedNote)

                        // Schedule or cancel reminder based on saved note
                        handleReminderScheduling(savedNote)
                        
                        _uiState.update { 
                            it.copy(
                                isSaving = false, 
                                hasUnsavedChanges = false,
                                originalNote = savedNote
                            ) 
                        }
                        snackbarManager.showMessage(
                            SnackbarMessage.Success(if (isEditing) "Note updated" else "Note created")
                        )

                        _navigationEvents.trySend(NavigationEvent.NavigateBack)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isSaving = false) }
                        snackbarManager.showMessage(
                            SnackbarMessage.Error("Failed to save note: ${error.message}")
                        )
                    }
                )
            }
        }
    }

    fun discardChanges() {
        val originalNote = _uiState.value.originalNote
        if (originalNote != null) {
            _uiState.update { 
                it.copy(
                    title = originalNote.title,
                    content = originalNote.content,
                    selectedCategory = originalNote.category,
                    selectedTags = originalNote.tags,
                    attachments = originalNote.attachments,
                    color = originalNote.color,
                    todos = originalNote.todos,
                    reminderTime = originalNote.reminderTime,
                    reminderRepeat = originalNote.reminderRepeat ?: ReminderRepeat.NONE,
                    reminderPriority = originalNote.reminderPriority ?: ReminderPriority.DEFAULT,
                    reminderLatitude = originalNote.reminderLatitude,
                    reminderLongitude = originalNote.reminderLongitude,
                    reminderLocationName = originalNote.reminderLocationName,
                    reminderRadius = originalNote.reminderRadius,
                    isLocationReminderEnabled = originalNote.isLocationReminderEnabled,
                    hasUnsavedChanges = false
                )
            }
        } else {
            _uiState.update { 
                it.copy(
                    title = "",
                    content = "",
                    selectedCategory = null,
                    selectedTags = emptyList(),
                    attachments = emptyList(),
                    color = null,
                    todos = emptyList(),
                    reminderTime = null,
                    reminderRepeat = ReminderRepeat.NONE,
                    reminderPriority = ReminderPriority.DEFAULT,
                    reminderLatitude = null,
                    reminderLongitude = null,
                    reminderLocationName = null,
                    reminderRadius = null,
                    isLocationReminderEnabled = false,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Tag creation methods
    fun showCreateTagDialog() {
        _uiState.update { 
            it.copy(
                showCreateTagDialog = true,
                newTagName = "",
                newTagColor = null
            ) 
        }
    }

    fun hideCreateTagDialog() {
        _uiState.update { 
            it.copy(
                showCreateTagDialog = false,
                newTagName = "",
                newTagColor = null
            ) 
        }
    }

    fun updateNewTagName(name: String) {
        _uiState.update { it.copy(newTagName = name) }
    }

    fun updateNewTagColor(color: String?) {
        _uiState.update { it.copy(newTagColor = color) }
    }

    fun createTag() {
        val currentState = _uiState.value
        if (currentState.newTagName.isBlank()) {
            viewModelScope.launch {
                snackbarManager.showMessage(SnackbarMessage.Warning("Tag name cannot be empty"))
            }
            return
        }

        // Check if tag already exists
        val existingTag = currentState.availableTags.find { 
            it.name.equals(currentState.newTagName.trim(), ignoreCase = true) 
        }
        if (existingTag != null) {
            viewModelScope.launch {
                snackbarManager.showMessage(SnackbarMessage.Warning("Tag '${currentState.newTagName}' already exists"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTag = true) }

            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                val newTag = Tag(
                    userId = user.id,
                    name = currentState.newTagName.trim(),
                    color = currentState.newTagColor
                )

                createTagUseCase(newTag).fold(
                    onSuccess = { createdTag ->
                        // Add the new tag to available tags and select it
                        _uiState.update { state ->
                            state.copy(
                                availableTags = state.availableTags + createdTag,
                                selectedTags = state.selectedTags + createdTag,
                                showCreateTagDialog = false,
                                isCreatingTag = false,
                                newTagName = "",
                                newTagColor = null,
                                hasUnsavedChanges = true
                            )
                        }
                        snackbarManager.showMessage(SnackbarMessage.Success("Tag '${createdTag.name}' created successfully"))
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(isCreatingTag = false) }
                        snackbarManager.showMessage(SnackbarMessage.Error("Failed to create tag: ${throwable.message}"))
                    }
                )
            }
        }
    }

    // Reminder methods
    fun showReminderDialog() {
        _uiState.update { it.copy(showReminderDialog = true) }
    }

    fun hideReminderDialog() {
        _uiState.update { it.copy(showReminderDialog = false) }
    }

    fun setReminder(reminderTime: Long, repeat: ReminderRepeat, priority: ReminderPriority) {
        _uiState.update { 
            it.copy(
                reminderTime = reminderTime, 
                reminderRepeat = repeat,
                reminderPriority = priority,
                showReminderDialog = false,
                hasUnsavedChanges = true
            ) 
        }
    }

    fun setLocationReminder(latitude: Double, longitude: Double, locationName: String, isReminder: Boolean, radius: Float = 100f) {
        _uiState.update {
            it.copy(
                reminderLatitude = latitude,
                reminderLongitude = longitude,
                reminderLocationName = locationName,
                reminderRadius = radius,
                isLocationReminderEnabled = isReminder,
                hasUnsavedChanges = true
            )
        }
    }

    fun removeReminder() {
        _uiState.update { 
            it.copy(
                reminderTime = null, 
                reminderRepeat = ReminderRepeat.NONE,
                reminderPriority = ReminderPriority.DEFAULT,
                hasUnsavedChanges = true,
                showReminderDialog = false
            ) 
        }
    }

    fun removeLocationReminder() {
        _uiState.update { 
            it.copy(
                reminderLatitude = null,
                reminderLongitude = null,
                reminderLocationName = null,
                reminderRadius = null,
                isLocationReminderEnabled = false,
                hasUnsavedChanges = true
            ) 
        }
    }

    private suspend fun handleLinksExtraction(note: Note) {
        // Find all [[Link]] patterns in content
        val regex = Regex("\\[\\[(.*?)\\]\\]")
        val matches = regex.findAll(note.content)
        val linkedTitles = matches.map { it.groupValues[1] }.toList()

        if (linkedTitles.isNotEmpty() || isEditing) {
            // Delete old links first
            deleteAllLinksForNoteUseCase(note.id)
            
            // Find target notes and add links
            val allNotesList = _uiState.value.allNotes
            for (title in linkedTitles) {
                val targetNote = allNotesList.find { it.title.equals(title, ignoreCase = true) }
                if (targetNote != null) {
                    addNoteLinkUseCase(
                        com.cbo.notes.domain.model.NoteLink(
                            sourceNoteId = note.id,
                            targetNoteId = targetNote.id
                        )
                    )
                }
            }
        }
    }

    private fun handleReminderScheduling(savedNote: Note) {
        // Time based reminder
        if (savedNote.reminderTime != null && savedNote.reminderTime > System.currentTimeMillis()) {
            reminderScheduler.scheduleReminder(
                noteId = savedNote.id,
                noteTitle = savedNote.title,
                reminderTime = savedNote.reminderTime
            )
        } else {
            reminderScheduler.cancelReminder(savedNote.id)
        }

        // Location based reminder
        if (savedNote.reminderLatitude != null && savedNote.reminderLongitude != null && savedNote.isLocationReminderEnabled) {
            locationReminderManager.addLocationReminder(
                noteId = savedNote.id,
                latitude = savedNote.reminderLatitude,
                longitude = savedNote.reminderLongitude,
                radiusInMeters = savedNote.reminderRadius ?: 100f
            )
        } else {
            locationReminderManager.removeLocationReminder(savedNote.id)
        }
    }

    // Tag input field methods
    fun updateTagInputText(text: String) {
        _uiState.update { it.copy(tagInputText = text) }
    }

    fun createTagFromInput() {
        val currentState = _uiState.value
        val tagName = currentState.tagInputText.trim()
        
        if (tagName.isBlank()) {
            return // Don't show error for empty input, just ignore
        }

        // Check if tag already exists
        val existingTag = currentState.availableTags.find { 
            it.name.equals(tagName, ignoreCase = true) 
        }
        if (existingTag != null) {
            // If tag exists, just select it instead of creating a new one
            if (!currentState.selectedTags.contains(existingTag)) {
                toggleTag(existingTag)
            }
            _uiState.update { it.copy(tagInputText = "") }
            return
        }

        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                val newTag = Tag(
                    userId = user.id,
                    name = tagName,
                    color = null // Default to no color for quick-created tags
                )

                createTagUseCase(newTag).fold(
                    onSuccess = { createdTag ->
                        // Add the new tag to available tags and select it
                        _uiState.update { state ->
                            state.copy(
                                availableTags = state.availableTags + createdTag,
                                selectedTags = state.selectedTags + createdTag,
                                tagInputText = "",
                                hasUnsavedChanges = true
                            )
                        }
                        snackbarManager.showMessage(SnackbarMessage.Success("Tag '${createdTag.name}' created"))
                    },
                    onFailure = { throwable ->
                        snackbarManager.showMessage(SnackbarMessage.Error("Failed to create tag: ${throwable.message}"))
                    }
                )
            }
        }
    }

    // Category input field methods
    fun updateCategoryInputText(text: String) {
        _uiState.update { it.copy(categoryInputText = text) }
    }

    fun createCategoryFromInput() {
        val currentState = _uiState.value
        val categoryName = currentState.categoryInputText.trim()
        
        if (categoryName.isBlank()) {
            return
        }

        // Check if category already exists
        val existingCategory = currentState.availableCategories.find { 
            it.name.equals(categoryName, ignoreCase = true) 
        }
        if (existingCategory != null) {
            if (currentState.selectedCategory != existingCategory) {
                selectCategory(existingCategory)
            }
            _uiState.update { it.copy(categoryInputText = "") }
            return
        }

        viewModelScope.launch {
            val currentUser = userSession.currentUser.first()
            currentUser?.let { user ->
                val newCategory = Category(
                    userId = user.id,
                    name = categoryName,
                    color = "#FF6B6B", // Provide a default color
                    description = ""
                )

                createCategoryUseCase(newCategory).fold(
                    onSuccess = { createdCategory ->
                        _uiState.update { state ->
                            state.copy(
                                availableCategories = state.availableCategories + createdCategory,
                                selectedCategory = createdCategory,
                                categoryInputText = "",
                                hasUnsavedChanges = true
                            )
                        }
                        snackbarManager.showMessage(SnackbarMessage.Success("Category '${createdCategory.name}' created"))
                    },
                    onFailure = { throwable ->
                        snackbarManager.showMessage(SnackbarMessage.Error("Failed to create category: ${throwable.message}"))
                    }
                )
            }
        }
    }

    fun showTemplateSelector() {
        _uiState.update { it.copy(showTemplateSelector = true) }
    }

    fun hideTemplateSelector() {
        _uiState.update { it.copy(showTemplateSelector = false) }
    }

    fun createTemplate(name: String, content: String) {
        viewModelScope.launch {
            val userId = userSession.currentUser.first()?.id ?: -1
            if (userId != -1) {
                val template = NoteTemplate(userId = userId, name = name, content = content)
                addNoteTemplateUseCase(template)
                // Refresh templates
                val templates = getNoteTemplatesUseCase(userId).first()
                _uiState.update { it.copy(availableTemplates = templates) }
            }
        }
    }

    fun applyTemplate(template: NoteTemplate) {
        val zettelId = System.currentTimeMillis().toString()
        _uiState.update { 
            it.copy(
                content = if (it.content.isBlank()) template.content else "${it.content}\n\n${template.content}",
                showTemplateSelector = false,
                hasUnsavedChanges = true
            )
        }
    }

    fun addAttachments(uris: List<String>) {
        _uiState.update { 
            val newAttachments = (it.attachments + uris).distinct()
            if (it.attachments != newAttachments) {
                it.copy(attachments = newAttachments, hasUnsavedChanges = true)
            } else {
                it
            }
        }
    }

    fun removeAttachment(uri: String) {
        _uiState.update { 
            val newAttachments = it.attachments.filter { attachment -> attachment != uri }
            if (it.attachments != newAttachments) {
                it.copy(attachments = newAttachments, hasUnsavedChanges = true)
            } else {
                it
            }
        }
    }

    // Color methods
    fun updateColor(color: String?) {
        if (_uiState.value.color != color) {
            _uiState.update { it.copy(color = color, hasUnsavedChanges = true) }
        }
    }

    fun toggleColorPicker() {
        _uiState.update { it.copy(showColorPicker = !it.showColorPicker) }
    }

    // Todo list methods
    fun addTodo() {
        _uiState.update { 
            val newTodos = it.todos + TodoItem(text = "")
            it.copy(todos = newTodos, hasUnsavedChanges = true) 
        }
    }

    fun updateTodo(id: String, text: String, isDone: Boolean) {
        _uiState.update {
            val newTodos = it.todos.map { todo -> 
                if (todo.id == id) todo.copy(text = text, isDone = isDone) else todo 
            }
            if (it.todos != newTodos) {
                it.copy(todos = newTodos, hasUnsavedChanges = true)
            } else {
                it
            }
        }
    }

    fun deleteTodo(id: String) {
        _uiState.update {
            val newTodos = it.todos.filter { todo -> todo.id != id }
            if (it.todos != newTodos) {
                it.copy(todos = newTodos, hasUnsavedChanges = true)
            } else {
                it
            }
        }
    }

    // Audio recording methods
    fun startRecording() {
        _uiState.update { it.copy(isRecording = true) }
    }

    fun stopRecording() {
        _uiState.update { it.copy(isRecording = false) }
    }

    fun onAudioRecordingComplete(filePath: String) {
        _uiState.update { currentState ->
            val existingAudioCount = currentState.attachments.count { isAudioAttachment(it) }
            val defaultName = "Ses Kaydı ${existingAudioCount + 1}"
            val formattedAttachment = "$filePath|$defaultName"
            val newAttachments = (currentState.attachments + formattedAttachment).distinct()
            if (currentState.attachments != newAttachments) {
                currentState.copy(
                    attachments = newAttachments,
                    hasUnsavedChanges = true
                )
            } else {
                currentState
            }
        }
    }

    fun onRenameAttachment(oldAttachment: String, newName: String) {
        val path = getAudioPath(oldAttachment)
        val updatedAttachment = if (newName.isBlank()) path else "$path|$newName"
        _uiState.update { currentState ->
            val updatedAttachments = currentState.attachments.map { attachment ->
                if (attachment == oldAttachment) updatedAttachment else attachment
            }
            if (currentState.attachments != updatedAttachments) {
                currentState.copy(
                    attachments = updatedAttachments,
                    hasUnsavedChanges = true
                )
            } else {
                currentState
            }
        }
    }
}

data class EditNoteUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val content: String = "",
    val selectedCategory: Category? = null,
    val selectedTags: List<Tag> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val hasUnsavedChanges: Boolean = false,
    val originalNote: Note? = null,
    val errorMessage: String? = null,
    // Tag creation state
    val showCreateTagDialog: Boolean = false,
    val isCreatingTag: Boolean = false,
    val newTagName: String = "",
    val newTagColor: String? = null,
    // Tag input field state
    val tagInputText: String = "",
    // Category input field state
    val categoryInputText: String = "",
    // Reminder state
    val reminderTime: Long? = null,
    val reminderRepeat: ReminderRepeat = ReminderRepeat.NONE,
    val reminderPriority: ReminderPriority = ReminderPriority.DEFAULT,
    val reminderLatitude: Double? = null,
    val reminderLongitude: Double? = null,
    val reminderLocationName: String? = null,
    val reminderRadius: Float? = null,
    val isLocationReminderEnabled: Boolean = false,
    val showReminderDialog: Boolean = false,
    // Templates state
    val availableTemplates: List<NoteTemplate> = emptyList(),
    val showTemplateSelector: Boolean = false,
    // Linking state
    val allNotes: List<Note> = emptyList(),
    val showLinkSuggestions: Boolean = false,
    val linkSearchQuery: String? = null,
    val backlinks: List<Note> = emptyList(),
    // Attachments state
    val attachments: List<String> = emptyList(),
    // Color state
    val color: String? = null,
    val showColorPicker: Boolean = false,
    // Todo state
    val todos: List<TodoItem> = emptyList(),
    // Audio recording state
    val isRecording: Boolean = false
) {
    val canSave: Boolean
        get() = !isSaving && (
            title.isNotBlank() ||
            content.isNotBlank() ||
            attachments.isNotEmpty() ||
            todos.isNotEmpty() ||
            hasUnsavedChanges ||
            originalNote != null
        )
}

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}
