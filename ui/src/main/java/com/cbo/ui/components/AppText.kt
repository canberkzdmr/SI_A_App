package com.cbo.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun AppHeadline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun AppTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun AppTitleMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = fontWeight,
        color = color,
        overflow = overflow,
        textAlign = textAlign,
        modifier = modifier,
    )
}

@Composable
fun AppBody(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun AppRegular(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        color = color,
        modifier = modifier,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun AppCaption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun AppLabel(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    overflow: TextOverflow = TextOverflow.Visible,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = Color.Unspecified,
) {
    Text(
        text = text,
        style = style,
        modifier = modifier,
        textAlign = textAlign,
        color = color,
        overflow = overflow,
        maxLines = maxLines
    )
}

@Composable
fun AppLabelLarge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Text Components Preview",
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun AppTextPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppHeadline("This is a Headline")
            AppTitle("This is a Title")
            AppTitleMedium("This is a Title Medium")
            AppBody("This is body text used for regular content.")
            AppCaption("This is a caption for subtle info.")
            AppLabelLarge("This is a large label for UI elements.")
            AppLabel("This is a label for UI elements.")
            SectionHeader("Section Header")
        }
    }
}
