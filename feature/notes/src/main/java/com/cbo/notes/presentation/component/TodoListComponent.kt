package com.cbo.notes.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.TodoItem

/**
 * A composable that renders a list of TodoItems attached to a note.
 * Items can be checked/unchecked, edited inline, and deleted.
 *
 * @param todos The list of [TodoItem]s.
 * @param onAddTodo Callback to add a new empty todo item.
 * @param onUpdateTodo Callback when an item's text or checked state changes.
 * @param onDeleteTodo Callback to delete an item.
 * @param modifier Modifier for this component.
 */
@Composable
fun TodoListComponent(
    todos: List<TodoItem>,
    onAddTodo: () -> Unit,
    onUpdateTodo: (id: String, text: String, isDone: Boolean) -> Unit,
    onDeleteTodo: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var previousTodos by remember { mutableStateOf(todos.map { it.id }) }
    var itemToFocus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(todos) {
        val currentIds = todos.map { it.id }
        if (currentIds.size > previousTodos.size) {
            val newId = currentIds.lastOrNull { it !in previousTodos }
            if (newId != null) {
                itemToFocus = newId
            }
        }
        previousTodos = currentIds
    }

    AnimatedVisibility(
        visible = todos.isNotEmpty(),
        enter = expandVertically(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "To-Do List",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            todos.forEach { item ->
                TodoItemRow(
                    item = item,
                    requestFocus = itemToFocus == item.id,
                    onFocusRequested = {
                        if (itemToFocus == item.id) {
                            itemToFocus = null
                        }
                    },
                    onCheckedChange = { checked ->
                        onUpdateTodo(item.id, item.text, checked)
                    },
                    onTextChange = { newText ->
                        onUpdateTodo(item.id, newText, item.isDone)
                    },
                    onDelete = {
                        onDeleteTodo(item.id)
                    },
                    onEnterPressed = {
                        onAddTodo()
                    }
                )
            }
        }
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    requestFocus: Boolean,
    onFocusRequested: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            onFocusRequested()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.size(40.dp)
        )

        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (item.isDone) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { onEnterPressed() }
            ),
            decorationBox = { innerTextField ->
                if (item.text.isEmpty()) {
                    Text(
                        text = "Task item",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete item",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
