package com.cbo.user.presentation.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cbo.user.R
import com.cbo.user.presentation.viewmodel.ProfileEvent
import com.cbo.user.presentation.viewmodel.ProfileUiState
import com.cbo.user.presentation.viewmodel.ProfileViewModel
import com.example.ui.components.AppCaption
import com.example.ui.components.AppLabel
import com.example.ui.components.AppTitle
import com.example.ui.components.DestructiveButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.outlineLight
import com.example.ui.theme.primaryLight
import com.example.ui.theme.surfaceDimLight
import java.time.LocalDate

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsState()

    // Collect one-shot events
    LaunchedEffect(user) {
        if (user == null) {
            Log.i("ProfileScreen", "Could not retrieve user!")
            onLogOut()
        }
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LoggedOut -> onLogOut()
            }
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onLogout = viewModel::logout,
        onEditProfile = { onEditProfile() },
        onChangePassword = {},
        onDeleteAccount = {},
        onThemeChange = {},
        onLanguageChange = {},
        onManageCategories = {},
        onExportNotes = {},
        onEnableBiometrics = {},
        onContactSupport = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
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
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    Text("Profile", color = MaterialTheme.colorScheme.onPrimary)
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
            // Profile Header
            // ------------------------
            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.isLoading) {
                        ShimmerBox(
                            modifier =
                                Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.person_profile),
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Spacer(Modifier.height(8.dp))
                        if (uiState.isLoading) {
                            ShimmerBox(
                                modifier =
                                    Modifier
                                        .height(20.dp)
                                        .fillMaxWidth(0.6f),
                            )
                            Spacer(Modifier.height(8.dp))
                            ShimmerBox(
                                modifier =
                                    Modifier
                                        .height(16.dp)
                                        .fillMaxWidth(0.4f),
                            )
                        } else {
                            Text(
                                text = uiState.username ?: "Guest",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = uiState.email ?: "Not available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                )
            }

            // ------------------------
            // Sections with list items
            // ------------------------
            val sections =
                listOf(
                    "Account" to
                        listOf(
                            Pair(Icons.Default.Edit, "Edit Profile"),
                            Pair(Icons.Default.Lock, "Change Password"),
                            Pair(Icons.Default.ExitToApp, "Logout"),
                            Pair(Icons.Default.Delete, "Delete Account"),
                        ),
                    "Preferences" to
                        listOf(
                            Pair(Icons.Default.DarkMode, "Theme"),
                            Pair(Icons.Default.Language, "Language"),
                        ),
                    "Notes" to
                        listOf(
                            Pair(Icons.Default.Category, "Manage Categories"),
                            Pair(Icons.Default.UploadFile, "Export Notes"),
                        ),
                    "Security" to
                        listOf(
                            Pair(Icons.Default.Fingerprint, "Enable Biometrics"),
                        ),
                    "About" to
                        listOf(
                            Pair(Icons.Default.Info, "App Version: 1.0.0"),
                            Pair(Icons.Default.SupportAgent, "Contact Support"),
                        ),
                )

            sections.forEach { (sectionTitle, items) ->
                item { SectionHeader(sectionTitle, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }

                items(items) { item ->
                    if (uiState.isLoading) {
                        ShimmerBox(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                        )
                    } else {
                        ProfileListItem(
                            icon = item.first,
                            text = item.second,
                            onClick = {
                                when (item.second) {
                                    "Edit Profile" -> onEditProfile()
                                    "Change Password" -> onChangePassword()
                                    "Logout" -> onLogout()
                                    "Delete Account" -> onDeleteAccount()
                                    "Theme" -> onThemeChange()
                                    "Language" -> onLanguageChange()
                                    "Manage Categories" -> onManageCategories()
                                    "Export Notes" -> onExportNotes()
                                    "Enable Biometrics" -> onEnableBiometrics()
                                    "Contact Support" -> onContactSupport()
                                    else -> {}
                                }
                            },
                            isDanger = item.second == "Delete Account",
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileListItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false,
) {
    val textColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun ProfileScreenPreview() {
    MyApplicationTheme {
        ProfileScreenContent(
            uiState =
                ProfileUiState(
                    username = "Aslanim Ismail",
                    email = "ismail.aslan@example.com",
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
            onContactSupport = {},
        )
    }
}
