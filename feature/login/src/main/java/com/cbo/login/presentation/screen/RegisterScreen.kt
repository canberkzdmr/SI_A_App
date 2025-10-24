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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.core.common.base.UiState
import com.cbo.core.common.validation.FieldValidation
import com.cbo.login.presentation.viewmodel.RegisterState
import com.cbo.login.presentation.viewmodel.RegisterViewModel
import com.cbo.ui.components.AppCard
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppRegular
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.theme.MemCloudApplicationTheme

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
                }
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
                append("I accept the")
            }
            pushStringAnnotation("TERMS", annotation = "terms_and_conditions")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                append(" terms and conditions ")
            }
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append("as well as the privacy policy of application")
            }
            pop()
        }

    val alreadyHaveAnAccountAnnotatedText =
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append("Already have an account? ")
            }
            pushStringAnnotation("ACCOUNT", annotation = "already_have_an_account")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Login.")
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

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
        ) {
            AppCard(
                onClick={},
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium,
                modifier =
                    Modifier
                        .align(Alignment.Center),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(16.dp),
                ) {
                    AppRegular("Registration")

                    AppTitle("Create an account")

                    // Username Field
                    OutlinedTextField(
                        value = registerState.username,
                        onValueChange = {
                            onUserNameChange(it)
                            usernameValidation = validateUserName()
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        placeholder = { Text("Username") },
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
                        label = { Text("Email") },
                        singleLine = true,
                        placeholder = { Text("Email") },
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
                        label = { Text("Password") },
                        singleLine = true,
                        placeholder = { Text("Password") },
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
                                        contentDescription = "hide password"
                                    )
                                }
                            } else {
                                IconButton(onClick = { showPassword = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.VisibilityOff,
                                        contentDescription = "show password"
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
                        label = { Text("Retype Password") },
                        singleLine = true,
                        placeholder = { Text("Retype Password") },
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
                                        contentDescription = "hide password"
                                    )
                                }
                            } else {
                                IconButton(onClick = { showReTypePassword = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.VisibilityOff,
                                        contentDescription = "show password"
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
                        "Register",
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
                            .padding(vertical = 8.dp),
                )
                AppLabel("■cnbrkzdmr")
            }
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
