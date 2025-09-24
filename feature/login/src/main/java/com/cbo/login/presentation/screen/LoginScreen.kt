package com.cbo.login.presentation.screen

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.login.presentation.viewmodel.LoginUiState
import com.cbo.login.presentation.viewmodel.LoginViewModel
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppRegular
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.snackbar.SnackbarHostProvider
import com.cbo.ui.theme.MyApplicationTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    prefillUsername: String? = null,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    LoginScreenContent(
        state = state,
        prefillUsername = prefillUsername,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick,
        onUserNameChange = viewModel::onUsernameChanged,
        onPasswordChange = viewModel::onPasswordChanged,
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
) {
    val focusRequesterPassword = remember { FocusRequester() }

    // Prefill username if provided
    LaunchedEffect(prefillUsername) {
        prefillUsername?.let(onUserNameChange)
    }

    MyApplicationTheme {
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
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            ).padding(vertical = 24.dp),
                ) {
                    AppHeadline(
                        "Your style of notes!",
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppRegular(
                        "Capture your thoughts and any ideas.",
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.username,
                        onValueChange = onUserNameChange,
                        label = { Text("Username") },
                        placeholder = { Text("Your username") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusRequesterPassword.requestFocus() }),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val keyboardController = LocalSoftwareKeyboardController.current
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Login",
                        onClick = onLoginClick,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onRegisterClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        AppLabel("Don't have an account? Register here!")
                    }
                }
            }
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
            "Welcome!",
            modifier = modifier,
            color = MaterialTheme.colorScheme.primaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreenContent(
            state = LoginUiState(username = "canberk", password = "1234"),
            prefillUsername = "",
            onLoginClick = {},
            onRegisterClick = {},
            onUserNameChange = {},
            onPasswordChange = {},
        )
    }
}
