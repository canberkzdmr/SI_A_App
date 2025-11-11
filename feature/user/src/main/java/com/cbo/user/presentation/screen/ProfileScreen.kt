package com.cbo.user.presentation.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.SectionHeader
import com.cbo.ui.components.ShimmerBox
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.user.R
import com.cbo.user.presentation.viewmodel.ProfileEvent
import com.cbo.user.presentation.viewmodel.ProfileUiState
import com.cbo.user.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeLanguage: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNotesClicked: () -> Unit = {},
    onCategoriesClicked: () -> Unit = {},
    onTagsClicked: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsState("")

    // Collect one-shot events
    LaunchedEffect(user) {
        Log.d("ProfileScreen", "is user null -> (${user == null})")
        if (user == null) {
            onLogOut()
        }
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LoggedOut -> {
                    Log.d("ProfileScreen", "User Log Out")
                    onLogOut()
                }
            }
        }
    }

    /*BackHandler {
        Log.i("ProfileScreen", "Back button is disabled for Profile Screen")
    }*/

    ProfileScreenContent(
        uiState = uiState,
        onLogout = viewModel::logout,
        onEditProfile = { onEditProfile() },
        onChangePassword = { onChangePassword() },
        onDeleteAccount = { onDeleteAccount() },
        onNotesClicked = { onNotesClicked() },
        onThemeChange = {},
        onLanguageChange = { onChangeLanguage() },
        onManageCategories = { onCategoriesClicked() },
        onManageTags = { onTagsClicked() },
        onExportNotes = {},
        onEnableBiometrics = viewModel::toggleBiometrics,
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
    onNotesClicked: () -> Unit,
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit,
    onManageCategories: () -> Unit,
    onManageTags: () -> Unit,
    onExportNotes: () -> Unit,
    onEnableBiometrics: () -> Unit,
    onContactSupport: () -> Unit,
) {
    ScreenWithTopBarAndInsets(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                title = {
                    Text(stringResource(id = R.string.profile_title), color = MaterialTheme.colorScheme.onSurface)
                },
            )
        }
    ) { innerPadding ->
        // Build sections and items using string resource IDs (not calling composables in LazyListScope)
        val sections: List<Pair<Int, List<Pair<ImageVector, Int>>>> = listOf(
            R.string.section_account to listOf(
                Icons.Default.Edit to R.string.edit_profile,
                Icons.Default.Lock to R.string.change_password,
                Icons.AutoMirrored.Filled.ExitToApp to R.string.logout,
                Icons.Default.Delete to R.string.delete_account,
            ),
            R.string.section_preferences to listOf(
                Icons.Default.DarkMode to R.string.theme,
                Icons.Default.Language to R.string.language,
            ),
            R.string.section_notes to listOf(
                Icons.AutoMirrored.Filled.Note to R.string.my_notes,
                Icons.Default.Category to R.string.manage_categories,
                Icons.Default.Tag to R.string.manage_tags,
                Icons.Default.UploadFile to R.string.export_notes,
            ),
            R.string.section_security to listOf(
                Icons.Default.Fingerprint to R.string.enable_biometrics,
            ),
            R.string.section_about to listOf(
                Icons.Default.Info to R.string.app_version,
                Icons.Default.SupportAgent to R.string.contact_support,
            ),
        )

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
                    } else if (uiState.avatarUrl.isNotEmpty()) {
                        SubcomposeAsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                            loading = {
                                ShimmerBox(
                                    modifier =
                                        Modifier
                                            .size(96.dp)
                                            .clip(CircleShape),
                                    cornerRadius = 48.dp, // Half of 96dp for circular shape
                                )
                            },
                            error = {
                                // Fallback to default image if avatar fails to load
                                Image(
                                    painter = painterResource(R.drawable.person_profile),
                                    contentDescription = "Default Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            },
                        )
                    } else {
                        // Show default image when no avatar is set
                        Image(
                            painter = painterResource(R.drawable.person_profile),
                            contentDescription = "Default Profile Picture",
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
                            AppTitle(
                                text = uiState.username ?: stringResource(id = R.string.guest),
                            )
                            Spacer(Modifier.height(8.dp))
                            AppLabel(
                                text = uiState.email ?: stringResource(id = R.string.not_available),
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

            // Sections with list items
            sections.forEach { (sectionTitleResId, items) ->
                item {
                    SectionHeader(
                        stringResource(id = sectionTitleResId),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                }

                items(items) { item ->
                    val label = if (item.second == R.string.app_version) {
                        stringResource(id = item.second, "1.0.0")
                    } else {
                        stringResource(id = item.second)
                    }
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
                            text = label,
                            onClick = {
                                when (item.second) {
                                    R.string.edit_profile -> onEditProfile()
                                    R.string.change_password -> onChangePassword()
                                    R.string.logout -> onLogout()
                                    R.string.delete_account -> onDeleteAccount()
                                    R.string.my_notes -> onNotesClicked()
                                    R.string.theme -> onThemeChange()
                                    R.string.language -> onLanguageChange()
                                    R.string.manage_categories -> onManageCategories()
                                    R.string.manage_tags -> onManageTags()
                                    R.string.export_notes -> onExportNotes()
                                    R.string.enable_biometrics -> onEnableBiometrics()
                                    R.string.contact_support -> onContactSupport()
                                    R.string.app_version -> { /* no-op */ }
                                    else -> {}
                                }
                            },
                            isDanger = item.second == R.string.delete_account,
                            isSwitch = item.second == R.string.enable_biometrics,
                            switchState = uiState.isBiometricEnabled,
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
    isSwitch: Boolean = false,
    switchState: Boolean = false,
) {
    val textColor =
        if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

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
            AppBody(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
            if (isSwitch) {
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = switchState,
                    onCheckedChange = {
                        onClick()
                    },
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun ProfileScreenPreview() {
    MemCloudApplicationTheme {
        ProfileScreenContent(
            uiState =
                ProfileUiState(
                    username = "Aslanim Ismail",
                    email = "ismail.aslan@example.com",
                    avatarUrl = "",
                ),
            onLogout = {},
            onEditProfile = {},
            onChangePassword = {},
            onDeleteAccount = {},
            onNotesClicked = {},
            onThemeChange = {},
            onLanguageChange = {},
            onManageCategories = {},
            onManageTags = {},
            onExportNotes = {},
            onEnableBiometrics = {},
            onContactSupport = {},
        )
    }
}
