package com.cbo.user.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cbo.core.domain.model.SupportedLanguage
import com.cbo.ui.components.AppSwitchGroup
import com.cbo.ui.components.AppSwitchOption
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.HeaderCard
import com.cbo.ui.components.states.AppLoadingScreen
import com.cbo.ui.snackbar.SnackbarHostProvider
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.user.R
import com.cbo.user.presentation.viewmodel.ChangeLanguageUiState
import com.cbo.user.presentation.viewmodel.ChangeLanguageViewModel

@Composable
fun ChangeLanguageScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangeLanguageViewModel = hiltViewModel(),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    
    // Listen to recreate activity event
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.recreateActivityEvent.collect {
            activity?.recreate()
        }
    }
    
    val uiState = viewModel.uiState.collectAsState()

    ChangeLanguageScreenContent(
        onNavigateBack = onNavigateBack,
        onSelectAppLanguage = viewModel::setAppLanguage,
        uiState = uiState.value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeLanguageScreenContent(
    onNavigateBack: () -> Unit,
    onSelectAppLanguage: (String?) -> Unit,
    uiState: ChangeLanguageUiState,
) {
    SnackbarHostProvider { paddingValues ->
        ScreenWithTopBarAndInsets(
            topBar = {
                CenterAlignedTopAppBar(
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    title = {
                        AppTitle(stringResource(R.string.change_language_title), color = MaterialTheme.colorScheme.onPrimary)
                    },
                    navigationIcon = {
                        IconButton(onClick = { onNavigateBack() }) {
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
                verticalArrangement = Arrangement.Top,
            ) {
                HeaderCard(
                    modifier = Modifier.padding(paddingValues),
                    variant = CardVariant.DEFAULT,
                    icon = Icons.Default.Language,
                    title = stringResource(id = R.string.about_languages),
                    content = stringResource(id = R.string.language_help),
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.isLoading && uiState.languageOptions.isEmpty()) {
                    AppLoadingScreen(modifier = Modifier.fillMaxSize())
                } else {
                    AppSwitchGroup(
                        options = uiState.languageOptions,
                        selectedOptionId = uiState.selectedLanguageCode,
                        onSelectionChange = onSelectAppLanguage,
                        title = stringResource(R.string.language_options),
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChangeLanguageScreen_Preview() {
    MemCloudApplicationTheme {
        ChangeLanguageScreenContent(
            onNavigateBack = {},
            onSelectAppLanguage = {},
            ChangeLanguageUiState(
                isLoading = false,
                languages = listOf(
                    SupportedLanguage(id = 0, code = "tr", displayName = "Turkish", nativeName = "Türkçe", isEnabled = true, sortOrder = 0),
                    SupportedLanguage(id = 1, code = "en", displayName = "English", nativeName = "English", isEnabled = true, sortOrder = 1),
                    SupportedLanguage(id = 2, code = "de", displayName = "German", nativeName = "Deutsch", isEnabled = false, sortOrder = 2),
                ),
                languageOptions = listOf(
                    AppSwitchOption(id = "tr", label = "Turkish", enabled = true),
                    AppSwitchOption(id = "en", label = "English", enabled = true),
                    AppSwitchOption(id = "de", label = "Deustch", enabled = false),
                ),
                selectedLanguageCode = "tr",
            )
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChangeLanguageScreen_Loading_Preview() {
    MemCloudApplicationTheme {
        ChangeLanguageScreenContent(
            onNavigateBack = {},
            onSelectAppLanguage = {},
            ChangeLanguageUiState(
                isLoading = true,
                languages = listOf(),
                languageOptions = listOf(),
                selectedLanguageCode = "en",
            )
        )
    }
}
