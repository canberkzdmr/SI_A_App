package com.cbo.user.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cbo.user.presentation.viewmodel.EditProfileViewModel
import com.cbo.user.presentation.viewmodel.EditUserProfileUiState
import com.example.ui.components.AppBody
import com.example.ui.components.AppIconButton
import com.example.ui.components.AppOutlinedTextField
import com.example.ui.components.AppTitle
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.MyApplicationTheme

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
        updateAddress = viewModel::updateAddress,
        updateBio = viewModel::updateBio,
        updatePhoneNumber = viewModel::updatePhoneNumber,
        updateAvatarUrl = viewModel::updateAvatarUrl,
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
    updateAddress: (String) -> Unit,
    updateBio: (String) -> Unit,
    updatePhoneNumber: (String) -> Unit,
    updateAvatarUrl: (String) -> Unit,
    save: () -> Unit,
) {
    // Launcher for gallery picker
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            updateAvatarUrl(uri?.toString().orEmpty())
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
                    Box(
                        modifier =
                            Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isLoading) {
                            ShimmerBox(modifier = Modifier.fillMaxSize())
                        } else if (uiState.avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = uiState.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            AppTitle(
                                text =
                                    uiState.username
                                        .firstOrNull()
                                        ?.uppercaseChar()
                                        ?.toString() ?: "?",
                            )
                        }

                        // Change Avatar Button
                        IconButton(
                            onClick = { launcher.launch("image/*") },
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
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
                    error = null,
                    isSaved = false,
                ),
            onCancel = {},
            updateFullName = {},
            updateBio = {},
            updateAddress = {},
            updateAvatarUrl = {},
            updatePhoneNumber = {},
            save = {},
        )
    }
}
