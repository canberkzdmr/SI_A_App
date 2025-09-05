package com.cbo.user.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cbo.user.R
import com.example.ui.components.AppCaption
import com.example.ui.components.AppLabel
import com.example.ui.components.AppRegular
import com.example.ui.components.AppTitle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.outlineLight
import com.example.ui.theme.primaryLight
import com.example.ui.theme.surfaceDimLight
import java.time.LocalDate

@Composable
fun ProfileScreen() {
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
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = MaterialTheme.shapes.medium,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                ) {
                    var imageHeightPx by remember { mutableIntStateOf(0) }
                    val imageHeightDp = with(LocalDensity.current) { imageHeightPx.toDp() }

                    Row(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Image(
                            painter =
                                rememberAsyncImagePainter(
                                    model =
                                        ImageRequest
                                            .Builder(LocalContext.current)
                                            .data(
                                                "https://media.licdn.com/dms/image/v2/D4D03AQEJBacyfXSHhw/profile-displayphoto-shrink_200_200/profile-displayphoto-shrink_200_200/0/1664964495340?e=2147483647&v=beta&t=A1kDbRKOFaif32iL2x385e08RIdm7DzLgTyad00ccKo",
                                            ) // https://media.licdn.com/dms/image/v2/C4E03AQHtv96d31B3Ng/profile-displayphoto-shrink_200_200/profile-displayphoto-shrink_200_200/0/1646760923279?e=2147483647&v=beta&t=uPjTJv65VDayii8xjWNFseYzqVjKkxFz15tgZaiMcyU
                                            .crossfade(true)
                                            .placeholder(R.drawable.person_profile)
                                            .build(),
                                ),
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(48.dp))
                                    .border(2.dp, color = outlineLight, RoundedCornerShape(48.dp))
                                    .onGloballyPositioned { coordinates ->
                                        imageHeightPx = coordinates.size.height
                                    },
                            contentScale = ContentScale.Crop,
                        )
                        Column(
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(imageHeightDp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            AppTitle("Yasin Maden")
                            AppCaption("yasinmaden1783@gmail.com")
                            AppLabel("Last online: ${LocalDate.now()}")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    MyApplicationTheme {
        ProfileScreen()
    }
}
