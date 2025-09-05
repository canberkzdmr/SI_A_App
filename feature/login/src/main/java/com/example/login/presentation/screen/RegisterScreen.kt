package com.example.login.presentation.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.login.presentation.viewmodel.RegisterUiState
import com.example.login.presentation.viewmodel.RegisterViewModel
import com.example.ui.components.AppLabel
import com.example.ui.components.AppRegular
import com.example.ui.components.AppTitle
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.primaryLight
import com.example.ui.theme.surfaceDimLight

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: (username: String) -> Unit,
    onUserNameChange: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    var termsAndConditionsChecker by remember { mutableStateOf(false) }

    val termsAnnotatedText =
        buildAnnotatedString {
            append("I accept the")
            pushStringAnnotation("TERMS", annotation = "terms_and_conditions")
            withStyle(style = SpanStyle(color = primaryLight)) {
                append(" terms and conditions ")
            }
            pop()
            append("as well as the privacy policy of application")
        }

    val alreadyHaveAnAccountAnnotatedText =
        buildAnnotatedString {
            append("Already have an account? ")
            pushStringAnnotation("ACCOUNT", annotation = "already_have_an_account")
            withStyle(style = SpanStyle(color = primaryLight)) {
                append("Login.")
            }
            pop()
        }

    MyApplicationTheme {
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
                            .background(color = primaryLight),
                )
                Spacer(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(color = surfaceDimLight),
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
            ) {
                Card(
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
                            value = uiState.username,
                            onValueChange = onUserNameChange,
                            label = { Text("Username") },
                            singleLine = true,
                            placeholder = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            placeholder = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            placeholder = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Retype Password Field
                        OutlinedTextField(
                            value = retypePassword,
                            onValueChange = { retypePassword = it },
                            label = { Text("Retype Password") },
                            singleLine = true,
                            placeholder = { Text("Retype Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = termsAndConditionsChecker,
                                onCheckedChange = {
                                    termsAndConditionsChecker = !termsAndConditionsChecker
                                    Log.d(
                                        "RegisterScreen",
                                        "Terms of service check box changed to $termsAndConditionsChecker",
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
                                            Log.d("RegisterScreen", "Terms and conditions clicked")
                                        }
                                },
                            )
                        }

                        PrimaryButton(
                            "Register",
                            onClick = {
                                viewModel.register {
                                    onRegisterSuccess(username)
                                    Log.d("RegisterScreen", "User registered successfully")
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
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
}

fun RegisterScreenContent(
    uiState: RegisterUiState,
    onUserNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRetypePasswordChange: (String) -> Unit,
    onRegisterSuccess: (username: String) -> Unit,
    modifier: Modifier = Modifier,
) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    var termsAndConditionsChecker by remember { mutableStateOf(false) }

    val termsAnnotatedText =
        buildAnnotatedString {
            append("I accept the")
            pushStringAnnotation("TERMS", annotation = "terms_and_conditions")
            withStyle(style = SpanStyle(color = primaryLight)) {
                append(" terms and conditions ")
            }
            pop()
            append("as well as the privacy policy of application")
        }

    val alreadyHaveAnAccountAnnotatedText =
        buildAnnotatedString {
            append("Already have an account? ")
            pushStringAnnotation("ACCOUNT", annotation = "already_have_an_account")
            withStyle(style = SpanStyle(color = primaryLight)) {
                append("Login.")
            }
            pop()
        }

    MyApplicationTheme {
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
                        .background(color = primaryLight),
                )
                Spacer(
                    modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(color = surfaceDimLight),
                )
            }

            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Card(
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
                            value = uiState.username,
                            onValueChange = onUserNameChange,
                            label = { Text("Username") },
                            singleLine = true,
                            placeholder = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            placeholder = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            placeholder = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Retype Password Field
                        OutlinedTextField(
                            value = retypePassword,
                            onValueChange = { retypePassword = it },
                            label = { Text("Retype Password") },
                            singleLine = true,
                            placeholder = { Text("Retype Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = termsAndConditionsChecker,
                                onCheckedChange = {
                                    termsAndConditionsChecker = !termsAndConditionsChecker
                                    Log.d(
                                        "RegisterScreen",
                                        "Terms of service check box changed to $termsAndConditionsChecker",
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
                                            Log.d("RegisterScreen", "Terms and conditions clicked")
                                        }
                                },
                            )
                        }

                        PrimaryButton(
                            "Register",
                            onClick = {
                                viewModel.register {
                                    onRegisterSuccess(username)
                                    Log.d("RegisterScreen", "User registered successfully")
                                }
                            },
                            modifier =
                            Modifier
                                .fillMaxWidth(),
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
}

@Preview
@Composable
fun RegisterScreenPreview() {
    MyApplicationTheme {
        RegisterScreen(
            onRegisterSuccess = {},
            onUserNameChange = {},
        )
    }
}
