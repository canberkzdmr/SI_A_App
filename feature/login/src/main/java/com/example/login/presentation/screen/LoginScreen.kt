package com.example.login.presentation.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.login.presentation.viewmodel.LoginUiState
import com.example.login.presentation.viewmodel.LoginViewModel
import com.example.ui.components.AppHeadline
import com.example.ui.components.AppLabel
import com.example.ui.components.AppRegular
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.backgroundLight
import com.example.ui.theme.primaryContainerLight
import com.example.ui.theme.primaryLight

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    prefillUsername: String?,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoggedIn) {
        LaunchedEffect(Unit) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        state = state,
        prefillUsername = prefillUsername,
        onLoginClick = viewModel::login,
        onRegisterClick = { onRegisterClick() },
        onUserNameChange = viewModel::onUsernameChanged,
        onPasswordChange = viewModel::onPasswordChanged,
    )
}

@Composable
fun LoginScreenContent(
    state: LoginUiState,
    prefillUsername: String?,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    prefillUsername?.let {
        onUserNameChange(it)
    }

    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = primaryLight)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                WelcomeMessage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = backgroundLight,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                ) {
                    AppHeadline(
                        "Your style of notes!",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppRegular(
                        "Capture your thoughts and any ideas.",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                    )
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = {
                            onUserNameChange(it)
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        placeholder = { Text("Your username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = {
                            onPasswordChange(it)
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        placeholder = { Text("Enter your password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                    PrimaryButton(
                        "Login",
                        onClick = { onLoginClick() },
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    )
                    TextButton(
                        onClick = { onRegisterClick() },
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        AppLabel("Don't have an account? Register here!")
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeMessage(modifier: Modifier) {
    var visible by remember { mutableStateOf(false) }

    // Trigger animation when the composable enters composition
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 800)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
    ) {
        AppHeadline(
            "Welcome!",
            modifier = modifier,
            color = primaryContainerLight,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(
    showBackground = true, showSystemUi = true,
    device = "spec:parent=pixel_9"
)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreenContent(
            state = LoginUiState(username = "canberk", "1234"),
            prefillUsername = "",
            onLoginClick = { Log.d("LoginPreview", "Login Clicked") },
            onRegisterClick = {},
            onUserNameChange = {},
            onPasswordChange = {},
        )
    }
}
