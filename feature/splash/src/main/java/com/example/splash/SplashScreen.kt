package com.example.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoading, state.isLoggedIn) {
        if (!state.isLoading) {
            if (state.isLoggedIn) {
                onNavigateToMain()
            } else {
                onNavigateToLogin()
            }
        }
    }

    // UI for splash screen (logo, brand animation, etc.)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("My Note App", style = MaterialTheme.typography.headlineLarge)
    }
}


@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onNavigateToLogin = {}, onNavigateToMain = {})
}