package com.cbo.login.presentation.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.Dimens
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.cbo.core.common.base.UiState
import com.cbo.core.common.validation.FieldValidation
import com.cbo.login.presentation.viewmodel.RegisterState
import com.cbo.login.presentation.viewmodel.RegisterViewModel
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppRegular
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.cards.AppCard
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.theme.MemCloudApplicationTheme

import com.cbo.ui.components.dialogs.AppConfirmationDialog
import com.cbo.ui.components.dialogs.DialogType

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: (username: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()

    MemCloudApplicationTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            RegisterScreenContent(
                uiState = uiState,
                registerState = registerState,
                onUserNameChange = viewModel::updateUsername,
                onEmailChange = viewModel::updateEmail,
                onPasswordChange = viewModel::updatePassword,
                onRetypePasswordChange = viewModel::updateRetypePassword,
                onTermsAndConditionsChecked = viewModel::updateTermsAndConditionsChecker,
                validateUserName = viewModel::validateUserName,
                validateEmail = viewModel::validateEmail,
                validatePassword = viewModel::validatePassword,
                validateRetypePassword = viewModel::validateReTypePassword,
                onRegister = {
                    viewModel.register {
                        onRegisterSuccess(registerState.username)
                    }
                },
                onBiometricLoginEnabled = { enabled ->
                    viewModel.enableBiometricLogin(enabled) {
                        onRegisterSuccess(registerState.username)
                    }
                },
                onShowBiometricDialog = viewModel::setShowBiometricDialog,
            )
        }
    }
}

@Composable
fun RegisterScreenContent(
    uiState: UiState<Unit>,
    registerState: RegisterState,
    onUserNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRetypePasswordChange: (String) -> Unit,
    onTermsAndConditionsChecked: (Boolean) -> Unit,
    validateUserName: () -> FieldValidation,
    validateEmail: () -> FieldValidation,
    validatePassword: () -> FieldValidation,
    validateRetypePassword: () -> FieldValidation,
    onRegister: () -> Unit,
    onBiometricLoginEnabled: (Boolean) -> Unit = {},
    onShowBiometricDialog: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val focusRequesterEmail = remember { FocusRequester() }
    val focusRequesterPassword = remember { FocusRequester() }
    val focusRequesterReTypePassword = remember { FocusRequester() }

    var usernameValidation by remember { mutableStateOf(FieldValidation(true)) }
    var emailValidation by remember { mutableStateOf(FieldValidation(true)) }
    var passwordValidation by remember { mutableStateOf(FieldValidation(true)) }
    var reTypePasswordValidation by remember { mutableStateOf(FieldValidation(true)) }

    var showPassword by remember { mutableStateOf(false) }
    var showReTypePassword by remember { mutableStateOf(false) }


    val termsAnnotatedText =
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append(stringResource(id = com.cbo.login.R.string.terms_prefix))
            }
            pushStringAnnotation("TERMS", annotation = "terms_and_conditions")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                append(stringResource(id = com.cbo.login.R.string.terms_link_text))
            }
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append(stringResource(id = com.cbo.login.R.string.privacy_suffix))
            }
            pop()
        }

    val alreadyHaveAnAccountAnnotatedText =
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append(stringResource(id = com.cbo.login.R.string.already_have_account_prefix))
            }
            pushStringAnnotation("ACCOUNT", annotation = "already_have_an_account")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(stringResource(id = com.cbo.login.R.string.login_link_text))
            }
            pop()
        }


    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.primary),
            )
            Spacer(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surfaceDim),
            )
        }

        if (!registerState.showBiometricDialog) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(Dimens.Padding.extraLarge),
            ) {
            AppCard(
                variant = CardVariant.DEFAULT,
                modifier =
                    Modifier
                        .align(Alignment.Center),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(Dimens.Padding.default),
                ) {
                    AppRegular(stringResource(id = com.cbo.login.R.string.registration_title))

                    AppTitle(stringResource(id = com.cbo.login.R.string.create_account_title))

                    // Username Field
                    OutlinedTextField(
                        value = registerState.username,
                        onValueChange = {
                            onUserNameChange(it)
                            usernameValidation = validateUserName()
                        },
                        label = { Text(stringResource(id = com.cbo.login.R.string.username_label)) },
                        singleLine = true,
                        placeholder = { Text(stringResource(id = com.cbo.login.R.string.username_placeholder)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            if (!usernameValidation.isValid) {
                                Text(
                                    text = usernameValidation.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(remember { FocusRequester() }),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusRequesterEmail.requestFocus() })
                    )

                    // Email Field
                    OutlinedTextField(
                        value = registerState.email,
                        onValueChange = {
                            onEmailChange(it)
                            emailValidation = validateEmail()
                        },
                        label = { Text(stringResource(id = com.cbo.login.R.string.email_label)) },
                        singleLine = true,
                        placeholder = { Text(stringResource(id = com.cbo.login.R.string.email_placeholder)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            if (!emailValidation.isValid) {
                                Text(
                                    text = emailValidation.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(remember { focusRequesterEmail }),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusRequesterPassword.requestFocus() })
                    )

                    // Password Field
                    OutlinedTextField(
                        value = registerState.password,
                        onValueChange = {
                            onPasswordChange(it)
                            passwordValidation = validatePassword()
                        },
                        label = { Text(stringResource(id = com.cbo.login.R.string.password_label)) },
                        singleLine = true,
                        placeholder = { Text(stringResource(id = com.cbo.login.R.string.password_placeholder)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            if (!passwordValidation.isValid) {
                                Text(
                                    text = passwordValidation.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        visualTransformation = if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            if (showPassword) {
                                IconButton(onClick = { showPassword = false }) {
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = stringResource(id = com.cbo.login.R.string.hide_password_cd)
                                    )
                                }
                            } else {
                                IconButton(onClick = { showPassword = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.VisibilityOff,
                                        contentDescription = stringResource(id = com.cbo.login.R.string.show_password_cd)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequesterPassword),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusRequesterReTypePassword.requestFocus() })
                    )

                    val keyboardController = LocalSoftwareKeyboardController.current
                    // Retype Password Field
                    OutlinedTextField(
                        value = registerState.reTypePassword,
                        onValueChange = {
                            onRetypePasswordChange(it)
                            reTypePasswordValidation = validateRetypePassword()
                        },
                        label = { Text(stringResource(id = com.cbo.login.R.string.retype_password_label)) },
                        singleLine = true,
                        placeholder = { Text(stringResource(id = com.cbo.login.R.string.retype_password_placeholder)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            if (!reTypePasswordValidation.isValid) {
                                Text(
                                    text = reTypePasswordValidation.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        visualTransformation = if (showReTypePassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            if (showReTypePassword) {
                                IconButton(onClick = { showReTypePassword = false }) {
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = stringResource(id = com.cbo.login.R.string.hide_password_cd)
                                    )
                                }
                            } else {
                                IconButton(onClick = { showReTypePassword = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.VisibilityOff,
                                        contentDescription = stringResource(id = com.cbo.login.R.string.show_password_cd)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequesterReTypePassword),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = registerState.termsAndConditionsChecked,
                            onCheckedChange = {
                                onTermsAndConditionsChecked(it)
                                Log.d(
                                    "RegisterScreen",
                                    "Terms of service check box changed to ${registerState.termsAndConditionsChecked}",
                                )
                            },
                        )

                        ClickableText(
                            text = termsAnnotatedText,
                            style = MaterialTheme.typography.labelSmall,
                            onClick = { offset ->
                                termsAnnotatedText
                                    .getStringAnnotations(
                                        tag = "TERMS",
                                        start = offset,
                                        end = offset,
                                    ).firstOrNull()
                                    ?.let {
                                        Log.d(
                                            "RegisterScreen",
                                            "Terms and conditions clicked"
                                        )
                                    }
                            },
                        )
                    }

                    PrimaryButton(
                        stringResource(id = com.cbo.login.R.string.register_cta),
                        onClick = {
                            onRegister()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        isLoading = uiState == UiState.Loading,
                        enabled = registerState.isValid,
                    )
                }
            }
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ClickableText(
                    text = alreadyHaveAnAccountAnnotatedText,
                    style = MaterialTheme.typography.labelLarge,
                    onClick = { offset ->
                        alreadyHaveAnAccountAnnotatedText
                            .getStringAnnotations("ACCOUNT", start = offset, end = offset)
                            .firstOrNull()
                            ?.let {
                                Log.d("Register Screen", "Login clicked.")
                            }
                    },
                    modifier =
                        Modifier
                            .padding(vertical = Dimens.Padding.small),
                )
                AppLabel("■cnbrkzdmr")
            }
        }
    }

        if (registerState.showBiometricDialog) {
            AppConfirmationDialog(
                type = DialogType.INFO,
                title = stringResource(id = com.cbo.login.R.string.enable_biometric_title),
                message = stringResource(id = com.cbo.login.R.string.enable_biometric_message),
                onConfirm = {
                    onBiometricLoginEnabled(true)
                    Log.d("RegisterScreen", "Biometric Login Enabled")
                    onShowBiometricDialog(false)
                },
                onDismiss = {
                    onBiometricLoginEnabled(false)
                    Log.d("RegisterScreen", "Biometric Login is Not Enabled")
                    onShowBiometricDialog(false)
                },
                confirmText = stringResource(id = com.cbo.login.R.string.enable),
                dismissText = stringResource(id = com.cbo.login.R.string.not_now)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun RegisterScreenPreview() {
    MemCloudApplicationTheme {
        RegisterScreenContent(
            uiState = UiState.Idle,
            registerState = RegisterState(),
            onUserNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onRetypePasswordChange = {},
            onTermsAndConditionsChecked = {},
            validateUserName = { FieldValidation(true) },
            validateEmail = { FieldValidation(true) },
            validatePassword = { FieldValidation(true) },
            validateRetypePassword = { FieldValidation(false) },
            onRegister = {},
        )
    }
}
