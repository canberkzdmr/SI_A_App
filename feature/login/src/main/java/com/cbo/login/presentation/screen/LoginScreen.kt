package com.cbo.login.presentation.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.Dimens
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.login.presentation.viewmodel.LoginUiState
import com.cbo.login.presentation.viewmodel.LoginViewModel
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppRegular
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.dialogs.AppConfirmationDialog
import com.cbo.ui.components.dialogs.DialogType
import com.cbo.ui.components.forms.AppFormActions
import com.cbo.ui.components.forms.AppFormFieldGroup
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    prefillUsername: String? = null,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val context = LocalContext.current as FragmentActivity
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedIn) {
        Log.d("LoginScreen", "state isLoggedIn: ${state.isLoggedIn}")
        if (state.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        state = state,
        prefillUsername = prefillUsername,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick,
        onUserNameChange = viewModel::onUsernameChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onBiometricLoginEnabled = viewModel::enableBiometricLogin,
        onShowBiometricDialog = viewModel::setShowBiometricDialog,
    )
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    state: LoginUiState,
    prefillUsername: String? = null,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBiometricLoginEnabled: (Boolean) -> Unit,
    onShowBiometricDialog: (Boolean) -> Unit,
) {
    val focusRequesterPassword = remember { FocusRequester() }

    // Prefill username if provided
    LaunchedEffect(prefillUsername) {
        prefillUsername?.let(onUserNameChange)
    }

    MemCloudApplicationTheme {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.primary),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(1f))

                WelcomeMessage(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(topStart = Dimens.CornerRadius.extraLarge, topEnd = Dimens.CornerRadius.extraLarge),
                            ).padding(vertical = Dimens.Padding.extraLarge),
                ) {
                    AppHeadline(
                        stringResource(id = com.cbo.login.R.string.your_style_of_notes),
                        modifier = Modifier.padding(horizontal = Dimens.Padding.extraLarge),
                    )
                    Spacer(modifier = Modifier.height(Dimens.Spacing.small))
                    AppRegular(
                        stringResource(id = com.cbo.login.R.string.capture_thoughts),
                        modifier = Modifier.padding(horizontal = Dimens.Padding.extraLarge),
                    )

                    Spacer(modifier = Modifier.height(Dimens.Spacing.default))

                    AppOutlinedTextField(
                        value = state.username,
                        onValueChange = onUserNameChange,
                        label = stringResource(id = com.cbo.login.R.string.username_label),
                        placeholder = stringResource(id = com.cbo.login.R.string.username_placeholder_login),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.Padding.extraLarge),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusRequesterPassword.requestFocus() }),
                    )

                    Spacer(modifier = Modifier.height(Dimens.Spacing.small))

                    val keyboardController = LocalSoftwareKeyboardController.current
                    AppOutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = stringResource(id = com.cbo.login.R.string.password_label),
                        placeholder = stringResource(id = com.cbo.login.R.string.password_placeholder_login),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.Padding.extraLarge)
                                .focusRequester(remember { focusRequesterPassword }),
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Send,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onSend = {
                                    keyboardController?.hide()
                                    onLoginClick()
                                },
                            ),
                    )

                    Spacer(modifier = Modifier.height(Dimens.Spacing.default))

                    PrimaryButton(
                        text = stringResource(id = com.cbo.login.R.string.login_cta),
                        onClick = {
                            onLoginClick()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.Padding.extraLarge),
                    )

                    Spacer(modifier = Modifier.height(Dimens.Spacing.small))

                    TextButton(
                        onClick = onRegisterClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        AppLabel(stringResource(id = com.cbo.login.R.string.dont_have_account))
                    }
                }
            }
        }

        if (state.showBiometricDialog) {
            AppConfirmationDialog(
                type = DialogType.INFO,
                title = stringResource(id = com.cbo.login.R.string.enable_biometric_title),
                message = stringResource(id = com.cbo.login.R.string.enable_biometric_message),
                onConfirm = {
                    onBiometricLoginEnabled(true)
                    Log.d("LoginScreen", "Biometric Login Enabled")
                    onShowBiometricDialog(false)
                    onLoginClick()
                },
                onDismiss = {
                    onShowBiometricDialog(false)
                    Log.d("LoginScreen", "Biometric Login is Not Enabled")
                    onLoginClick()
                },
                confirmText = stringResource(id = com.cbo.login.R.string.enable),
                dismissText = stringResource(id = com.cbo.login.R.string.not_now)
            )
        }
    }
}

@Composable
fun WelcomeMessage(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(animationSpec = tween(800)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                ),
    ) {
        AppHeadline(
            stringResource(id = com.cbo.login.R.string.welcome),
            modifier = modifier,
            color = MaterialTheme.colorScheme.primaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MemCloudApplicationTheme {
        LoginScreenContent(
            state = LoginUiState(username = "canberk", password = "1234"),
            prefillUsername = "",
            onLoginClick = {},
            onRegisterClick = {},
            onUserNameChange = {},
            onPasswordChange = {},
            onBiometricLoginEnabled = {},
            onShowBiometricDialog = {},
        )
    }
}
