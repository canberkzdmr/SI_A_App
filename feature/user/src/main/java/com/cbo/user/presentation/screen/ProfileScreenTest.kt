package com.cbo.user.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.user.R
import com.cbo.user.presentation.viewmodel.ProfileUiState
import com.example.ui.components.AppLabel
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit,
    onManageCategories: () -> Unit,
    onExportNotes: () -> Unit,
    onEnableBiometrics: () -> Unit,
    onContactSupport: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.person_profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.username ?: "Guest",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = uiState.email ?: "Not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Account Section
            item { SectionHeader("Account") }
            item {
                ProfileListItem(
                    icon = Icons.Default.Edit,
                    text = "Edit Profile",
                    onClick = onEditProfile
                )
            }
            item {
                ProfileListItem(
                    icon = Icons.Default.Lock,
                    text = "Change Password",
                    onClick = onChangePassword
                )
            }
            item {
                ProfileListItem(
                    icon = Icons.Default.ExitToApp,
                    text = "Logout",
                    onClick = onLogout
                )
            }
            item {
                ProfileListItem(
                    icon = Icons.Default.Delete,
                    text = "Delete Account",
                    onClick = onDeleteAccount,
                    isDanger = true
                )
            }

            // Preferences Section
            item { SectionHeader("Preferences") }
            item {
                ProfileListItem(
                    icon = Icons.Default.DarkMode,
                    text = "Theme",
                    onClick = onThemeChange
                )
            }
            item {
                ProfileListItem(
                    icon = Icons.Default.Language,
                    text = "Language",
                    onClick = onLanguageChange
                )
            }

            // Notes Section
            item { SectionHeader("Notes") }
            item {
                ProfileListItem(
                    icon = Icons.Default.Category,
                    text = "Manage Categories",
                    onClick = onManageCategories
                )
            }
            item {
                ProfileListItem(
                    icon = Icons.Default.UploadFile,
                    text = "Export Notes",
                    onClick = onExportNotes
                )
            }

            // Security Section
            item { SectionHeader("Security") }
            item {
                ProfileListItem(
                    icon = Icons.Default.Fingerprint,
                    text = "Enable Biometrics",
                    onClick = onEnableBiometrics
                )
            }

            // About Section
            item { SectionHeader("About") }
            item {
                ProfileListItem(
                    icon = Icons.Default.Info,
                    text = "App Version: 1.0.0",
                    onClick = {}
                )
            }
            item {
                ProfileListItem(
                    icon = Icons.Default.SupportAgent,
                    text = "Contact Support",
                    onClick = onContactSupport
                )
            }
        }
    }
}

@Composable
fun ProfileListItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    ListItem(
        headlineContent = {
            Text(
                text = text,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Profile Screen Preview"
)
@Composable
fun ProfileScreenPreview2() {
    MaterialTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                username = "John Doe",
                email = "johndoe@example.com"
            ),
            onLogout = {},
            onEditProfile = {},
            onChangePassword = {},
            onDeleteAccount = {},
            onThemeChange = {},
            onLanguageChange = {},
            onManageCategories = {},
            onExportNotes = {},
            onEnableBiometrics = {},
            onContactSupport = {}
        )
    }
}

