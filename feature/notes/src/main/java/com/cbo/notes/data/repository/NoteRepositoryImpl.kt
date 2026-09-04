package com.cbo.notes.data.repository

import android.util.Log
import com.cbo.notes.data.mapper.NoteEntityMapper
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.NoteLinkDao
import com.cbo.core.database.dao.TagDao
import com.cbo.core.database.entity.NoteTagCrossRef
import com.cbo.notes.data.mapper.TagEntityMapper
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.NoteStatistics
import com.cbo.notes.domain.model.TodoItem
import com.cbo.notes.domain.repository.NoteRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.cbo.core.common.util.safePutMetric
import com.cbo.core.common.util.traceMetricSuspend
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val noteLinkDao: NoteLinkDao,
    private val tagDao: TagDao,
    private val noteEntityMapper: NoteEntityMapper,
    private val tagEntityMapper: TagEntityMapper,
    private val gson: Gson,
) : NoteRepository {

    override fun getNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getNotesWithDetailsByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getArchivedNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getArchivedNotesByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getDeletedNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getDeletedNotesWithDetailsByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getFavoriteNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getFavoriteNotesByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getNotesByCategory(userId: Int, categoryId: Int): Flow<List<Note>> {
        return noteDao.getNotesByCategory(userId, categoryId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override suspend fun getNoteById(noteId: Int): Note? {
        return try {
            noteDao.getNoteWithDetails(noteId)?.let {
                noteEntityMapper.toDomain(it)
            }
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting note by id: ${e.message}")
            null
        }
    }

    override fun searchNotes(userId: Int, searchQuery: String): Flow<List<Note>> {
        return noteDao.searchNotes(userId, searchQuery).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override suspend fun insertNote(note: Note): Result<Note> = traceMetricSuspend("trace_insert_note") { trace ->
        try {
            trace.safePutMetric("todos_count", note.todos.size.toLong())
            trace.safePutMetric("attachments_count", note.attachments.size.toLong())
            trace.safePutMetric("tags_count", note.tags.size.toLong())

            val entity = noteEntityMapper.toEntity(note)
            val insertedId: Long = noteDao.insert(entity)
            val insertedNote = note.copy(id = insertedId.toInt())
            
            // Add tags if any
            if (note.tags.isNotEmpty()) {
                note.tags.forEach { tag ->
                    noteDao.insertNoteTagCrossRef(NoteTagCrossRef(insertedId.toInt(), tag.id))
                    tagDao.updateTagUsageCount(tag.id)
                }
            }
            
            Result.success(insertedNote)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error inserting note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> = traceMetricSuspend("trace_update_note") { trace ->
        try {
            trace.safePutMetric("todos_count", note.todos.size.toLong())
            trace.safePutMetric("attachments_count", note.attachments.size.toLong())
            trace.safePutMetric("tags_count", note.tags.size.toLong())

            val entity = noteEntityMapper.toEntity(note)
            val tagList = tagDao.getTagsByNote(note.id) // tags in db
            if (tagList.isNotEmpty()) {
                for (tag in tagList) { // tags in db
                    if (!note.tags.contains(tagEntityMapper.toDomain(tag))) { // if not in updated tag list remove from cross ref
                        noteDao.deleteNoteTagCrossRef(NoteTagCrossRef(note.id, tag.id))
                        tagDao.updateTagUsageCount(tag.id)
                    }
                }
            }
            if (note.tags.isNotEmpty()) {
                for (tag in note.tags) { // updated tag list
                    if (!tagList.contains(tagEntityMapper.toEntity(tag))) { // if in updated list but not in db insert it
                        noteDao.insertNoteTagCrossRef(NoteTagCrossRef(note.id, tag.id))
                        tagDao.updateTagUsageCount(tag.id)
                    }
                }
            }

            noteDao.update(entity)
            Result.success(note)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating note: ${e.message}")
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun deleteNote(noteId: Int): Result<Unit> {
        return try {
            // Get existing tags to update their usage count
            val existingTags = tagDao.getTagsByNote(noteId)
            
            noteDao.deleteById(noteId)
            
            // Update tag usage counts
            existingTags.forEach { tag ->
                tagDao.updateTagUsageCount(tag.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error deleting note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun softDeleteNote(noteId: Int): Result<Unit> {
        return try {
            noteDao.softDeleteNote(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error soft deleting note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun restoreDeletedNote(noteId: Int): Result<Unit> {
        return try {
            noteDao.restoreDeletedNote(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error restoring deleted note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun permanentlyDeleteNote(noteId: Int): Result<Unit> {
        return try {
            // Get existing tags to update their usage count
            val existingTags = tagDao.getTagsByNote(noteId)
            
            noteDao.permanentlyDeleteNote(noteId)
            
            // Update tag usage counts
            existingTags.forEach { tag ->
                tagDao.updateTagUsageCount(tag.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error permanently deleting note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun cleanupExpiredDeletedNotes(): Result<Int> {
        return try {
            val expirationTimestamp = Note.getExpirationTimestamp()
            
            // Get expired notes to update tag usage counts
            val expiredNotes = noteDao.getExpiredDeletedNotes(expirationTimestamp)
            
            // Update tag usage counts for each expired note
            expiredNotes.forEach { note ->
                val tags = tagDao.getTagsByNote(note.id)
                tags.forEach { tag ->
                    tagDao.updateTagUsageCount(tag.id)
                }
            }
            
            // Permanently delete expired notes
            val deletedCount = noteDao.permanentlyDeleteExpiredNotes(expirationTimestamp)
            Log.d("NoteRepositoryImpl", "Cleaned up $deletedCount expired deleted notes")
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error cleaning up expired deleted notes: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updatePinnedStatus(noteId: Int, isPinned: Boolean): Result<Unit> {
        return try {
            noteDao.updatePinnedStatus(noteId, isPinned)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating pinned status: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean): Result<Unit> {
        return try {
            noteDao.updateFavoriteStatus(noteId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating favorite status: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateArchivedStatus(noteId: Int, isArchived: Boolean): Result<Unit> {
        return try {
            noteDao.updateArchivedStatus(noteId, isArchived)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating archived status: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNoteCategory(noteId: Int, categoryId: Int?): Result<Unit> {
        return try {
            noteDao.updateNoteCategory(noteId, categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating note category: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun addTagsToNote(noteId: Int, tagIds: List<Int>): Result<Unit> {
        return try {
            tagIds.forEach { tagId ->
                noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
                tagDao.updateTagUsageCount(tagId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error adding tags to note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun removeTagsFromNote(noteId: Int, tagIds: List<Int>): Result<Unit> {
        return try {
            tagIds.forEach { tagId ->
                noteDao.deleteNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
                tagDao.updateTagUsageCount(tagId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error removing tags from note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNoteTags(noteId: Int, tagIds: List<Int>): Result<Unit> {
        return try {
            // Get existing tags
            val existingTags = tagDao.getTagsByNote(noteId)
            
            // Remove all existing tags
            noteDao.deleteAllTagsForNote(noteId)
            
            // Add new tags
            tagIds.forEach { tagId ->
                noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
            }
            
            // Update usage counts for all affected tags
            existingTags.forEach { tag ->
                tagDao.updateTagUsageCount(tag.id)
            }
            tagIds.forEach { tagId ->
                tagDao.updateTagUsageCount(tagId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating note tags: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getNotesCount(userId: Int): Int {
        return try {
            noteDao.getNotesCount(userId)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting notes count: ${e.message}")
            0
        }
    }

    override suspend fun getNotesCountByCategory(userId: Int, categoryId: Int): Int {
        return try {
            noteDao.getNotesCountByCategory(userId, categoryId)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting notes count by category: ${e.message}")
            0
        }
    }

    override suspend fun deleteAllArchivedNotes(userId: Int): Result<Unit> {
        return try {
            noteDao.deleteAllArchivedNotes(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error deleting archived notes: ${e.message}")
            Result.failure(e)
        }
    }

    // Reminder operations
    override suspend fun setReminder(
        noteId: Int, 
        reminderTime: Long, 
        repeat: com.cbo.notes.domain.model.ReminderRepeat, 
        priority: com.cbo.notes.domain.model.ReminderPriority
    ): Result<Unit> {
        return try {
            noteDao.updateReminder(noteId, reminderTime, repeat.name, priority.name)
            Log.d("NoteRepositoryImpl", "Reminder set for note $noteId at $reminderTime")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error setting reminder: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun removeReminder(noteId: Int): Result<Unit> {
        return try {
            noteDao.updateReminder(noteId, null)
            Log.d("NoteRepositoryImpl", "Reminder removed for note $noteId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error removing reminder: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getNotesWithActiveReminders(): Flow<List<Note>> {
        return noteDao.getNotesWithActiveReminders().map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override suspend fun getNotesWithRemindersBetween(startTime: Long, endTime: Long): List<Note> {
        return try {
            noteDao.getNotesWithRemindersBetween(startTime, endTime).map { 
                noteEntityMapper.toDomain(it) 
            }
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting notes with reminders: ${e.message}")
            emptyList()
        }
    }

    override suspend fun setLocationReminderEnabled(noteId: Int, isEnabled: Boolean): Result<Unit> {
        return try {
            noteDao.updateLocationReminderEnabled(noteId, isEnabled)
            Log.d("NoteRepositoryImpl", "Location reminder for note $noteId set to $isEnabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error setting location reminder: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getNotesWithActiveLocationReminders(): List<Note> {
        return try {
            noteDao.getNotesWithActiveLocationReminders().map { 
                noteEntityMapper.toDomain(it) 
            }
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting notes with active location reminders: ${e.message}")
            emptyList()
        }
    }

    // -------------------------------------------------------------------------
    // Statistics
    // -------------------------------------------------------------------------

    override suspend fun getNoteStatistics(userId: Int): NoteStatistics = traceMetricSuspend("trace_calculate_note_statistics") { trace ->
        coroutineScope {
            try {
                val now = System.currentTimeMillis()
                // Haftanın başlangıcı (Pazartesi 00:00)
                val weekStart = LocalDate.now(ZoneId.systemDefault())
                    .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), DayOfWeek.MONDAY.value.toLong())
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                // Eski not eşiği (90 gün önce)
                val staleThreshold = now - (90L * 24 * 60 * 60 * 1000)

                // Tüm sorguları ardışık olarak çalıştırıyoruz (SQLite zaten çok hızlıdır, 23 paralel sorgu thread havuzunu kilitler)
                val totalNotes           = noteDao.getNotesCount(userId)
                val archived             = noteDao.getArchivedNotesCount(userId)
                val favorite             = noteDao.getFavoriteNotesCount(userId)
                val pinned               = noteDao.getPinnedNotesCount(userId)
                val deleted              = noteDao.getDeletedNotesCount(userId)
                val perDay               = noteDao.getNotesCountPerDay(userId)
                val perDow               = noteDao.getNotesCountByDayOfWeek(userId)
                val perHour              = noteDao.getNotesCountByHour(userId)
                val perMonth             = noteDao.getNotesCountPerMonth(userId)
                val categoryStats        = noteDao.getCategoryDistribution(userId)
                val colorStats           = noteDao.getColorDistribution(userId)
                val activeReminder       = noteDao.getActiveReminderCount(userId)
                val expiredReminder      = noteDao.getExpiredReminderCount(userId)
                val locationReminder     = noteDao.getLocationReminderCount(userId)
                val staleNotes           = noteDao.getStaleNotesCount(userId, staleThreshold)
                val weeklyNotes          = noteDao.getWeeklyNoteCount(userId, weekStart)
                val weeklyTodosRaw       = noteDao.getWeeklyUpdatedNoteTodos(userId, weekStart)
                val totalLinks           = noteLinkDao.getTotalLinkCountForUser(userId)
                val mostLinked           = noteLinkDao.getMostLinkedNotes(userId, 5)
                val orphans              = noteLinkDao.getOrphanNoteCount(userId)
                val topTags              = tagDao.getMostUsedTags(userId, 10)
                val weeklyTopTag         = tagDao.getMostUsedTagSince(userId, weekStart)

                // Aktif notların anlık listesini al (word count, attachment, todo için)
                val activeNotes = noteDao.getNotesWithDetailsByUser(userId).first()

                // Word count ve attachment hesaplama
                var wordCount = 0L
                var totalTodos = 0
                var completedTodos = 0
                var totalAttach = 0
                var imageAttach = 0
                var audioAttach = 0

                activeNotes.forEach { noteWithDetails ->
                    val note = noteWithDetails.note
                    // Yaklaşık kelime sayısı: boşlukla ayrılmış token'lar
                    val words = (note.title + " " + note.content).trim().split("\\s+".toRegex())
                    wordCount += words.count { it.isNotBlank() }

                    // Todo istatistikleri
                    note.todos.forEach { todo ->
                        totalTodos++
                        if (todo.isDone) completedTodos++
                    }

                    // Attachment istatistikleri
                    note.attachments.forEach { attachUri ->
                        totalAttach++
                        when {
                            attachUri.endsWith(".mp3", true) ||
                            attachUri.endsWith(".m4a", true) ||
                            attachUri.endsWith(".3gp", true) ||
                            attachUri.contains("audio", ignoreCase = true) -> audioAttach++
                            else -> imageAttach++
                        }
                    }
                }

                // Haftalık todo sayısını JSON'dan hesapla
                val todoType = object : TypeToken<List<Map<String, Any>>>() {}.type
                var weeklyCompleted = 0
                weeklyTodosRaw.forEach { todosJson ->
                    try {
                        val todos: List<Map<String, Any>> = gson.fromJson(todosJson, todoType)
                        weeklyCompleted += todos.count { it["isDone"] == true }
                    } catch (_: Exception) { /* ignore malformed json */ }
                }

                // Streak hesaplama
                val perDayMap = perDay.associate { it.day to it.noteCount }
                val streaks = calculateStreaks(perDayMap)

                trace.safePutMetric("total_notes_count", totalNotes.toLong())
                trace.safePutMetric("total_words_count", wordCount)
                trace.safePutMetric("total_todos_count", totalTodos.toLong())
                trace.safePutMetric("total_attachments_count", totalAttach.toLong())

                NoteStatistics(
                    totalNotes           = totalNotes,
                    archivedNotes        = archived,
                    favoriteNotes        = favorite,
                    pinnedNotes          = pinned,
                    deletedNotes         = deleted,
                    totalWordCount       = wordCount,
                    totalTodoItems       = totalTodos,
                    completedTodoItems   = completedTodos,
                    totalAttachments     = totalAttach,
                    imageAttachments     = imageAttach,
                    audioAttachments     = audioAttach,
                    notesPerDay          = perDayMap,
                    notesPerHour         = perHour.associate { it.hour to it.noteCount },
                    notesPerDayOfWeek    = perDow.associate { it.dayOfWeek to it.noteCount },
                    notesPerMonth        = perMonth.associate { it.month to it.noteCount },
                    currentStreak        = streaks.first,
                    longestStreak        = streaks.second,
                    categoryDistribution = categoryStats.associate { it.categoryName to it.noteCount },
                    topTags              = topTags.map { it.name to it.usageCount },
                    colorDistribution    = colorStats.associate { it.colorName to it.noteCount },
                    totalNoteLinks       = totalLinks,
                    mostLinkedNotes      = mostLinked.map { it.noteId to it.linkCount },
                    orphanNoteCount      = orphans,
                    activeReminderCount  = activeReminder,
                    expiredReminderCount = expiredReminder,
                    locationReminderCount= locationReminder,
                    weeklyNoteCount      = weeklyNotes,
                    weeklyCompletedTodoCount = weeklyCompleted,
                    mostUsedTagThisWeek  = weeklyTopTag?.name,
                    staleNoteCount       = staleNotes,
                    fullyCompletedTodoNoteCount = 0, // Hesaplama aktif notlardan yapılıyor
                )
            } catch (e: Exception) {
                Log.e("NoteRepositoryImpl", "Error computing statistics: ${e.message}", e)
                NoteStatistics()
            }
        }
    }

    /**
     * Günlük not sayısı haritasından (YYYY-MM-DD → count) streak çifti hesaplar.
     * @return Pair(currentStreak, longestStreak)
     */
    private fun calculateStreaks(perDay: Map<String, Int>): Pair<Int, Int> {
        if (perDay.isEmpty()) return Pair(0, 0)

        val sortedDays = perDay.keys.sorted()
        var longestStreak = 1
        var currentRun = 1
        var currentStreak = 0

        // Uzun seri hesaplama
        for (i in 1 until sortedDays.size) {
            val prev = LocalDate.parse(sortedDays[i - 1])
            val curr = LocalDate.parse(sortedDays[i])
            if (curr == prev.plusDays(1)) {
                currentRun++
                if (currentRun > longestStreak) longestStreak = currentRun
            } else {
                currentRun = 1
            }
        }

        // Güncel seri hesaplama (bugünden geriye doğru)
        val today = LocalDate.now(ZoneId.systemDefault())
        val lastDayKey = sortedDays.last()
        val lastDay = LocalDate.parse(lastDayKey)
        if (lastDay == today || lastDay == today.minusDays(1)) {
            currentStreak = 1
            for (i in sortedDays.size - 2 downTo 0) {
                val prev = LocalDate.parse(sortedDays[i])
                val next = LocalDate.parse(sortedDays[i + 1])
                if (next == prev.plusDays(1)) {
                    currentStreak++
                } else {
                    break
                }
            }
        }

        return Pair(currentStreak, longestStreak)
    }
}
