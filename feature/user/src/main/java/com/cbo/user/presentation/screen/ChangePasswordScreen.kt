package com.cbo.user.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.cards.AppCard
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.HeaderCard
import com.cbo.ui.snackbar.SnackbarHostProvider
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.user.presentation.viewmodel.ChangePasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button
    BackHandler {
        onNavigateBack()
    }

    // Handle password change success
    LaunchedEffect(uiState.isPasswordChanged) {
        if (uiState.isPasswordChanged) {
            onPasswordChanged()
        }
    }

    SnackbarHostProvider { paddingValues ->
        ScreenWithTopBarAndInsets(
            topBar = {
                CenterAlignedTopAppBar(
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    title = {
                        AppTitle(stringResource(id = com.cbo.user.R.string.change_password_title), color = MaterialTheme.colorScheme.onPrimary)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                )
            },
        ) { scaffoldPaddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(scaffoldPaddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // Header text
                HeaderCard(
                    modifier = Modifier
                        .padding(paddingValues),
                    variant = CardVariant.DEFAULT,
                    icon = Icons.Default.Lock,
                    title = stringResource(id = com.cbo.user.R.string.update_your_password),
                    content = stringResource(id = com.cbo.user.R.string.password_help)
                )
                /*AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.TONAL
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        AppTitle(
                            text = "Update Your Password",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppBody(
                            text ="Enter your current password and choose a new secure password",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }*/

                Spacer(modifier = Modifier.height(32.dp))

                // Current Password Field
                AppOutlinedTextField(
                    value = uiState.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChanged,
                    label = stringResource(id = com.cbo.user.R.string.current_password),
                    placeholder = stringResource(id = com.cbo.user.R.string.enter_current_password),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onCurrentPasswordVisibilityToggle) {
                            Icon(
                                imageVector =
                                    if (uiState.isCurrentPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                contentDescription =
                                    if (uiState.isCurrentPasswordVisible) {
                                        stringResource(id = com.cbo.user.R.string.hide_password)
                                    } else {
                                        stringResource(id = com.cbo.user.R.string.show_password)
                                    },
                            )
                        }
                    },
                    visualTransformation =
                        if (uiState.isCurrentPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New Password Field
                AppOutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChanged,
                    label = stringResource(id = com.cbo.user.R.string.new_password),
                    placeholder = stringResource(id = com.cbo.user.R.string.enter_new_password),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onNewPasswordVisibilityToggle) {
                            Icon(
                                imageVector =
                                    if (uiState.isNewPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                contentDescription =
                                    if (uiState.isNewPasswordVisible) {
                                        stringResource(id = com.cbo.user.R.string.hide_password)
                                    } else {
                                        stringResource(id = com.cbo.user.R.string.show_password)
                                    },
                            )
                        }
                    },
                    visualTransformation =
                        if (uiState.isNewPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                AppOutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    label = stringResource(id = com.cbo.user.R.string.confirm_new_password),
                    placeholder = stringResource(id = com.cbo.user.R.string.confirm_new_password_placeholder),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onConfirmPasswordVisibilityToggle) {
                            Icon(
                                imageVector =
                                    if (uiState.isConfirmPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                contentDescription =
                                    if (uiState.isConfirmPasswordVisible) {
                                        stringResource(id = com.cbo.user.R.string.hide_password)
                                    } else {
                                        stringResource(id = com.cbo.user.R.string.show_password)
                                    },
                            )
                        }
                    },
                    visualTransformation =
                        if (uiState.isConfirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Requirements
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.SURFACE
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        AppTitleMedium(
                            text = stringResource(id = com.cbo.user.R.string.password_requirements),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val requirements =
                            listOf(
                                stringResource(id = com.cbo.user.R.string.req_len),
                                stringResource(id = com.cbo.user.R.string.req_upper),
                                stringResource(id = com.cbo.user.R.string.req_lower),
                                stringResource(id = com.cbo.user.R.string.req_number),
                            )

                        requirements.forEach { requirement ->
                            AppBody(
                                text = "• $requirement",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Change Password Button
                PrimaryButton(
                    text = if (uiState.isLoading) stringResource(id = com.cbo.user.R.string.changing_password) else stringResource(id = com.cbo.user.R.string.change_password_action),
                    onClick = viewModel::changePassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading &&
                            uiState.currentPassword.isNotEmpty() &&
                            uiState.newPassword.isNotEmpty() &&
                            uiState.confirmPassword.isNotEmpty(),
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordScreenPreview() {
    MemCloudApplicationTheme {
        ChangePasswordScreen(
            onNavigateBack = {},
            onPasswordChanged = {},
        )
    }
}
