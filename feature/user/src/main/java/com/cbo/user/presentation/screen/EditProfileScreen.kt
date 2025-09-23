package com.cbo.user.presentation.screen

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppIconButton
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.ShimmerBox
import com.cbo.ui.theme.MyApplicationTheme
import com.cbo.user.presentation.viewmodel.EditProfileViewModel
import com.cbo.user.presentation.viewmodel.EditUserProfileUiState
import java.util.Calendar
import kotlin.math.exp

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit,
    onCancelEditProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    EditProfileScreenContent(
        uiState,
        onCancel = { onCancelEditProfile() },
        updateFullName = viewModel::updateFullName,
        updateGender = viewModel::updateGender,
        updateDateOfBirth = viewModel::updateDateOfBirth,
        updateAddress = viewModel::updateAddress,
        updateBio = viewModel::updateBio,
        updatePhoneNumber = viewModel::updatePhoneNumber,
        onImageSelected = viewModel::onImageSelected,
        save = { viewModel.save() },
    )

    // Show error
    uiState.error?.let { errorMsg ->
        Text(
            text = errorMsg,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // Trigger success callback
    if (uiState.isSaved) {
        LaunchedEffect(Unit) {
            onSaveSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenContent(
    uiState: EditUserProfileUiState,
    onCancel: () -> Unit,
    updateFullName: (String) -> Unit,
    updateGender: (String) -> Unit,
    updateDateOfBirth: (String) -> Unit,
    updateBio: (String) -> Unit,
    updatePhoneNumber: (String) -> Unit,
    updateAddress: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    save: () -> Unit,
) {
    // Launcher for gallery picker
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                Log.i("EditProfileScreen", "Image selected: $selectedUri")
                onImageSelected(selectedUri)
            }
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                title = { AppTitle("Edit Profile", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    AppIconButton(
                        onClick = { onCancel() },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
        ) {
            // ------------------------
            // Profile Header with Avatar + Picker
            // ------------------------
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Container Box for avatar and edit button - no clipping here
                    Box(
                        modifier = Modifier.size(140.dp), // Larger to accommodate edit button
                        contentAlignment = Alignment.Center,
                    ) {
                        // Avatar container with clipping
                        Box(
                            modifier =
                                Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                uiState.isLoading -> {
                                    // Overall loading state
                                    ShimmerBox(
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 60.dp, // Circular shape
                                    )
                                }

                                uiState.isImageLoading -> {
                                    // Image loading state with shimmer
                                    ShimmerBox(
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 60.dp, // Circular shape
                                    )
                                }

                                uiState.avatarUrl.isNotEmpty() -> {
                                    SubcomposeAsyncImage(
                                        model = uiState.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            ShimmerBox(
                                                modifier = Modifier.fillMaxSize(),
                                                cornerRadius = 60.dp, // Circular shape
                                            )
                                        },
                                    )
                                }

                                else -> {
                                    AppTitle(
                                        text =
                                            uiState.username
                                                .firstOrNull()
                                                ?.uppercaseChar()
                                                ?.toString() ?: "?",
                                    )
                                }
                            }
                        }

                        // Change Avatar Button - positioned outside the clipped avatar
                        IconButton(
                            onClick = {
                                if (!uiState.isImageLoading) {
                                    launcher.launch("image/*")
                                }
                            },
                            enabled = !uiState.isImageLoading,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .background(
                                        if (uiState.isImageLoading) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        CircleShape,
                                    ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Avatar",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTitle(uiState.username)
                    Spacer(Modifier.height(4.dp))
                    AppBody(
                        text = uiState.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(top = 16.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    )
                }
            }

            // ------------------------
            // Editable Fields Section (no Avatar URL anymore)
            // ------------------------
            item {
                AppOutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = updateFullName,
                    label = "Full Name",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )

                // Gender
                GenderSelector(
                    selectedGender = uiState.gender,
                    onGenderSelected = updateGender,
                )

                // Date of birth
                DateOfBirthPicker(
                    dob = uiState.dateOfBirth,
                    onDobSelected = updateDateOfBirth,
                )

                // Bio
                AppOutlinedTextField(
                    value = uiState.bio,
                    onValueChange = updateBio,
                    label = "Bio",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
                AppOutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = updatePhoneNumber,
                    label = "Phone Number",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
                AppOutlinedTextField(
                    value = uiState.address,
                    onValueChange = updateAddress,
                    label = "Address",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
            }

            // ------------------------
            // Save Button
            // ------------------------
            item {
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "Save",
                    onClick = save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
) {
    val options = listOf("Female", "Male")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        AppOutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = "Gender",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { gender ->
                DropdownMenuItem(
                    text = { AppBody(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun DateOfBirthPicker(
    dob: String,
    onDobSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog =
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = "%04d-%02d-%02d".format(year, month + 1, day)
                onDobSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )

    AppOutlinedTextField(
        value = dob,
        onValueChange = {},
        label = "Date of birth",
        readOnly = true,
        onClick = { datePickerDialog.show() },
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun EditProfileScreenPreview() {
    MyApplicationTheme {
        EditProfileScreenContent(
            uiState =
                EditUserProfileUiState(
                    username = "canberk",
                    email = "canberk@example.com",
                    fullName = "Can Berk",
                    avatarUrl = "https://example.com/avatar.png",
                    bio = "Android Developer, coffee lover ☕",
                    phoneNumber = "+90 555 555 55 55",
                    address = "Istanbul, Türkiye",
                    isLoading = false,
                    isImageLoading = false,
                    error = null,
                    isSaved = false,
                ),
            onCancel = {},
            updateFullName = {},
            updateGender = {},
            updateDateOfBirth = {},
            updateBio = {},
            updateAddress = {},
            onImageSelected = {},
            updatePhoneNumber = {},
            save = {},
        )
    }
}
