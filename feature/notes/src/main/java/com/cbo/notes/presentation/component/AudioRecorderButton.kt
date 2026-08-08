package com.cbo.notes.presentation.component

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.os.Build
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import com.cbo.ui.theme.Dimens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * A composable that handles audio recording with permission management.
 * Records to a file in the app's cache directory and returns the file path via [onRecordingComplete].
 *
 * @param isRecording Whether currently recording.
 * @param onStartRecording Called when recording starts (permission granted).
 * @param onStopRecording Called when recording stops.
 * @param onRecordingComplete Called with the file path when a recording is saved.
 * @param modifier Modifier for this component.
 */
@Composable
fun AudioRecorderButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onRecordingComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var recordingDuration by remember { mutableLongStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartRecording()
        } else {
            coroutineScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Warning("Microphone permission required for audio notes"))
            }
        }
    }

    // Timer for recording duration
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0L
            while (true) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    // Start/stop MediaRecorder
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val file = File(context.cacheDir, "audio_note_${System.currentTimeMillis()}.m4a")
            outputFile = file
            try {
                val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                mediaRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000)
                    setAudioSamplingRate(44100)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                recorder = mediaRecorder
            } catch (e: Exception) {
                SnackbarManager.showMessage(SnackbarMessage.Error("Failed to start recording: ${e.message}"))
                onStopRecording()
            }
        } else {
            try {
                recorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                // Ignore stop errors
            }
            recorder = null
            // If we had an active recording, notify completion
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    onRecordingComplete(file.absolutePath)
                }
            }
            outputFile = null
        }
    }

    // Clean up on disposal
    DisposableEffect(Unit) {
        onDispose {
            try {
                recorder?.apply {
                    stop()
                    release()
                }
            } catch (_: Exception) { }
            recorder = null
        }
    }

    // Recording pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (isRecording) {
        // Recording indicator bar
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Padding.default, vertical = Dimens.Padding.tiny),
            shape = RoundedCornerShape(Dimens.CornerRadius.default),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Padding.default, vertical = Dimens.Padding.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.medium)
                ) {
                    // Pulsing red dot
                    Box(
                        modifier = Modifier
                            .size(Dimens.Spacing.medium)
                            .scale(pulse)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = "Recording",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = formatDuration(recordingDuration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }

                IconButton(onClick = onStopRecording) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop recording",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * A mini audio player for playing back recorded audio attached to a note.
 * Supports seeking via progress bar, playback speed options (1x, 1.25x, 1.5x, 1.75x, 2x), and inline renaming.
 *
 * @param audioUri The URI or file path of the audio file (may contain "|displayName").
 * @param onRemove Callback to remove this audio attachment.
 * @param onRename Callback to rename this audio attachment.
 * @param modifier Modifier for this component.
 */
@Composable
fun AudioPlayerMini(
    audioUri: String,
    onRemove: (() -> Unit)? = null,
    onRename: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }

    val speeds = remember { listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f) }
    var speedIndex by remember { mutableIntStateOf(0) }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPositionMs by remember { mutableFloatStateOf(0f) }

    var isEditingName by remember { mutableStateOf(false) }
    val currentDisplayName = remember(audioUri) { getAudioDisplayName(audioUri) }
    var editedName by remember(currentDisplayName) { mutableStateOf(currentDisplayName) }
    val audioPath = remember(audioUri) { getAudioPath(audioUri) }

    // Retrieve initial duration without starting playback
    LaunchedEffect(audioPath) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioPath)
            val timeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            timeStr?.toLongOrNull()?.let { totalDurationMs = it }
            retriever.release()
        } catch (_: Exception) { }
    }

    // Timer loop to update position while playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                player?.let { p ->
                    if (p.isPlaying) {
                        if (!isDraggingSlider) {
                            currentPositionMs = p.currentPosition.toLong()
                        }
                        if (p.duration > 0) {
                            totalDurationMs = p.duration.toLong()
                        }
                    }
                }
                delay(16)
            }
        }
    }

    DisposableEffect(audioPath) {
        onDispose {
            try {
                player?.release()
            } catch (_: Exception) {}
            player = null
        }
    }

    // Helper to apply speed
    val applyPlaybackSpeed = { p: MediaPlayer ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = p.playbackParams ?: PlaybackParams()
                p.playbackParams = params.setSpeed(speeds[speedIndex])
            } catch (_: Exception) {}
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Padding.default, vertical = Dimens.Padding.tiny),
        shape = RoundedCornerShape(Dimens.CornerRadius.default),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Padding.medium, vertical = Dimens.Padding.small)
        ) {
            // Top Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing.small)
            ) {
                // Play / Pause button
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            player?.pause()
                            isPlaying = false
                        } else {
                            try {
                                if (player == null) {
                                    val newPlayer = MediaPlayer().apply {
                                        setDataSource(audioPath)
                                        prepare()
                                        setOnCompletionListener {
                                            isPlaying = false
                                            currentPositionMs = 0L
                                        }
                                    }
                                    player = newPlayer
                                    applyPlaybackSpeed(newPlayer)
                                }
                                player?.let { p ->
                                    applyPlaybackSpeed(p)
                                    p.start()
                                }
                                isPlaying = true
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    SnackbarManager.showMessage(SnackbarMessage.Error("Cannot play audio"))
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(Dimens.Spacing.xxxLarge)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Audio Mic Icon
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(Dimens.Icon.extraSmall)
                )

                if (isEditingName) {
                    BasicTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(Dimens.CornerRadius.small)
                            )
                            .padding(horizontal = Dimens.Padding.small, vertical = Dimens.Padding.tiny),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (editedName.isNotBlank()) {
                                    onRename?.invoke(editedName.trim())
                                }
                                isEditingName = false
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (editedName.isNotBlank()) {
                                onRename?.invoke(editedName.trim())
                            }
                            isEditingName = false
                        },
                        modifier = Modifier.size(Dimens.Icon.extraLarge)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save name",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.Icon.extraSmall)
                        )
                    }

                    IconButton(
                        onClick = {
                            editedName = currentDisplayName
                            isEditingName = false
                        },
                        modifier = Modifier.size(Dimens.Icon.extraLarge)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel edit",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(Dimens.Icon.extraSmall)
                        )
                    }
                } else {
                    Text(
                        text = currentDisplayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (onRename != null) {
                                    isEditingName = true
                                }
                            }
                    )

                    // Playback speed selector chip (1x, 1.25x, 1.5x, 1.75x, 2x)
                    Surface(
                        onClick = {
                            speedIndex = (speedIndex + 1) % speeds.size
                            player?.let { p -> applyPlaybackSpeed(p) }
                        },
                        shape = RoundedCornerShape(Dimens.CornerRadius.medium),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = Dimens.Padding.extraTiny)
                    ) {
                        val speedText = "${speeds[speedIndex]}x".replace(".0x", "x")
                        Text(
                            text = speedText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = Dimens.Padding.small, vertical = Dimens.Padding.tiny)
                        )
                    }

                    if (onRename != null) {
                        IconButton(
                            onClick = { isEditingName = true },
                            modifier = Modifier.size(Dimens.Icon.extraLarge)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename audio",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(Dimens.Icon.extraSmall)
                            )
                        }
                    }

                    if (onRemove != null) {
                        IconButton(
                            onClick = {
                                try {
                                    player?.release()
                                } catch (_: Exception) {}
                                player = null
                                isPlaying = false
                                onRemove()
                            },
                            modifier = Modifier.size(Dimens.Icon.extraLarge)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove audio",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(Dimens.Icon.extraSmall)
                            )
                        }
                    }
                }
            }

            // Progress Slider & Timers (shown if totalDurationMs > 0 or when playing)
            if (totalDurationMs > 0 || isPlaying) {
                val maxRange = totalDurationMs.toFloat().coerceAtLeast(1f)
                val currentSliderValue = (if (isDraggingSlider) sliderPositionMs else currentPositionMs.toFloat()).coerceIn(0f, maxRange)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.Padding.tiny)
                ) {
                    Slider(
                        value = currentSliderValue,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderPositionMs = it
                        },
                        onValueChangeFinished = {
                            player?.seekTo(sliderPositionMs.toInt())
                            currentPositionMs = sliderPositionMs.toLong()
                            isDraggingSlider = false
                        },
                        valueRange = 0f..maxRange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.Icon.large)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.Padding.tiny),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(
                                (if (isDraggingSlider) sliderPositionMs.toLong() else currentPositionMs) / 1000
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatDuration(totalDurationMs / 1000),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats seconds into MM:SS format for the recording timer.
 */
private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

/**
 * Extracts the file path from an audio attachment string (formatted as "path|displayName" or "path").
 */
fun getAudioPath(uri: String): String {
    return uri.substringBefore("|")
}

/**
 * Extracts the display name from an audio attachment string (formatted as "path|displayName" or "path").
 */
fun getAudioDisplayName(uri: String): String {
    return if (uri.contains("|")) {
        val name = uri.substringAfter("|").trim()
        if (name.isNotBlank()) name else "Audio Note"
    } else {
        "Audio Note"
    }
}

/**
 * Checks if a file path or URI points to an audio file based on extension.
 */
fun isAudioAttachment(uri: String): Boolean {
    val cleanPath = getAudioPath(uri)
    val lower = cleanPath.lowercase()
    return lower.endsWith(".m4a") || lower.endsWith(".mp3") || 
           lower.endsWith(".wav") || lower.endsWith(".ogg") ||
           lower.endsWith(".aac") || lower.endsWith(".3gp")
}

@Preview(showBackground = true, name = "Audio Recorder Button - Recording")
@Composable
private fun AudioRecorderButtonRecordingPreview() {
    MaterialTheme {
        AudioRecorderButton(
            isRecording = true,
            onStartRecording = {},
            onStopRecording = {},
            onRecordingComplete = {}
        )
    }
}

@Preview(showBackground = true, name = "Audio Player Mini")
@Composable
private fun AudioPlayerMiniPreview() {
    MaterialTheme {
        AudioPlayerMini(
            audioUri = "sample.m4a|Örnek Ses Kaydı 1",
            onRemove = {},
            onRename = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Audio Player Mini - Dark Mode")
@Composable
private fun AudioPlayerMiniDarkModePreview() {
    MaterialTheme {
        AudioPlayerMini(
            audioUri = "sample.m4a|Örnek Ses Kaydı 1",
            onRemove = {},
            onRename = {}
        )
    }
}


