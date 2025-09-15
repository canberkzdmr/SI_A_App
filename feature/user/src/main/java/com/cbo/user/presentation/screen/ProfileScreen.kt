package com.cbo.user.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cbo.user.R
import com.cbo.user.presentation.viewmodel.ProfileEvent
import com.cbo.user.presentation.viewmodel.ProfileViewModel
import com.example.ui.components.AppCaption
import com.example.ui.components.AppLabel
import com.example.ui.components.AppTitle
import com.example.ui.components.DestructiveButton
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.outlineLight
import com.example.ui.theme.primaryLight
import com.example.ui.theme.surfaceDimLight
import java.time.LocalDate

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogOut: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LoggedOut -> onLogOut()
            }
        }
    }

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(color = primaryLight),
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(color = surfaceDimLight),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                ) {
                    var imageHeightPx by remember { mutableIntStateOf(0) }
                    val imageHeightDp = with(LocalDensity.current) { imageHeightPx.toDp() }

                    Row(modifier = Modifier.padding(16.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(
                                        "https://media.licdn.com/dms/image/v2/D4D03AQEJBacyfXSHhw/profile-displayphoto-shrink_200_200/0/1664964495340"
                                    )
                                    .crossfade(true)
                                    .placeholder(R.drawable.person_profile)
                                    .build(),
                            ),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(48.dp))
                                .border(2.dp, color = outlineLight, RoundedCornerShape(48.dp))
                                .onGloballyPositioned { coordinates ->
                                    imageHeightPx = coordinates.size.height
                                },
                            contentScale = ContentScale.Crop,
                        )
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .height(imageHeightDp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            AppTitle(state.username.ifEmpty { "Guest User" })
                            AppCaption(state.email.ifEmpty { "No email" })
                            AppLabel("Last online: ${LocalDate.now()}")
                        }
                    }
                }

                DestructiveButton(
                    "Log out",
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    leadingIcon = { Icon(Icons.AutoMirrored.Default.Logout, contentDescription = "Log out") }
                )
            }
        }
    }
}


@Preview
@Composable
fun ProfileScreenPreview() {
    MyApplicationTheme {
        ProfileScreen(
            onLogOut = {}
        )
    }
}
