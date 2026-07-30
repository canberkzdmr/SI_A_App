package com.cbo.notes.presentation.screen

import android.R.attr.top
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.notes.R
import com.cbo.ui.components.AppSearchField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.theme.Dimens
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun MainScreen(

) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = modifier,
                title = { AppTitle("MemCloud") },
                actions = {
                    IconButton(onClick = {
                        //TODO show notifications.
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = stringResource(id = R.string.notifications)
                        )
                    }
                    IconButton(onClick = {
                        // TODO show user profile settings or smth
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile)
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
            )
        }
    ) { paddingValues ->
        Column(

        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onClear = onClearSearch,
                placeholder = stringResource(R.string.main_screen_search_placeholder),
                modifier = Modifier
                    .padding(top = Dimens.Padding.small)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            AppTitle(stringResource(R.string.day_summary))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MemCloudApplicationTheme() {
        MainScreenContent(
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
        )
    }
}
