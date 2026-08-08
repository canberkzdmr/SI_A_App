package com.cbo.notes.presentation.component

import android.Manifest
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * A bottom toolbar for the note editor that provides quick access to:
 * - Color picker toggle
 * - Add Todo item
 * - Audio recording
 *
 * @param showColorPicker Whether the color picker bar is currently visible.
 * @param isRecording Whether currently recording audio.
 * @param onToggleColorPicker Toggle color picker visibility.
 * @param onAddTodo Add a new todo item.
 * @param onStartRecording Start audio recording (after permission check).
 * @param onStopRecording Stop audio recording.
 * @param modifier Modifier for this component.
 */
@Composable
fun NoteBottomToolbar(
    showColorPicker: Boolean,
    isRecording: Boolean,
    onToggleColorPicker: () -> Unit,
    onAddImage: () -> Unit,
    onAddReminder: () -> Unit,
    onAddTodo: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartRecording()
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_LONG).show()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color picker toggle
            IconButton(onClick = onToggleColorPicker) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Note color",
                    tint = if (showColorPicker)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Add To-do button
            IconButton(onClick = onAddTodo) {
                Icon(
                    imageVector = Icons.Default.CheckBox,
                    contentDescription = "Add Todo",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Audio recording
            IconButton(
                onClick = {
                    if (isRecording) {
                        onStopRecording()
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            onStartRecording()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop recording" else "Record audio",
                    tint = if (isRecording)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Add image toggle
            IconButton(onClick = onAddImage) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Note color",
                    tint = if (showColorPicker)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Add Reminder toggle
            IconButton(onClick = onAddReminder) {
                Icon(
                    imageVector = Icons.Default.NotificationAdd,
                    contentDescription = "Note color",
                    tint = if (showColorPicker)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Note Bottom Toolbar - Default")
@Composable
private fun NoteBottomToolbarPreview() {
    MaterialTheme {
        NoteBottomToolbar(
            showColorPicker = false,
            isRecording = false,
            onToggleColorPicker = {},
            onAddTodo = {},
            onStartRecording = {},
            onAddImage = {},
            onAddReminder = {},
            onStopRecording = {}
        )
    }
}

@Preview(showBackground = true, name = "Note Bottom Toolbar - Color Picker Active")
@Composable
private fun NoteBottomToolbarColorPickerActivePreview() {
    MaterialTheme {
        NoteBottomToolbar(
            showColorPicker = true,
            isRecording = false,
            onToggleColorPicker = {},
            onAddTodo = {},
            onStartRecording = {},
            onAddImage = {},
            onAddReminder = {},
            onStopRecording = {}
        )
    }
}

@Preview(showBackground = true, name = "Note Bottom Toolbar - Recording Active")
@Composable
private fun NoteBottomToolbarRecordingPreview() {
    MaterialTheme {
        NoteBottomToolbar(
            showColorPicker = false,
            isRecording = true,
            onToggleColorPicker = {},
            onAddTodo = {},
            onAddImage = {},
            onStartRecording = {},
            onAddReminder = {},
            onStopRecording = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Note Bottom Toolbar - Dark Mode")
@Composable
private fun NoteBottomToolbarDarkModePreview() {
    MaterialTheme {
        NoteBottomToolbar(
            showColorPicker = false,
            isRecording = false,
            onToggleColorPicker = {},
            onAddTodo = {},
            onStartRecording = {},
            onAddImage = {},
            onAddReminder = {},
            onStopRecording = {}
        )
    }
}

