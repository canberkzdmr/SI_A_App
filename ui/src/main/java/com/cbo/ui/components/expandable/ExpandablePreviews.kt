package com.cbo.ui.components.expandable

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
private fun ExpandableComponentsDemo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTitle(text = "Expandable UI Components")

        // 1. Expandable Cards
        AppExpandableCard(
            title = "Proje Detayları",
            subtitle = "Gelişmiş ayarlar ve yapılandırmalar",
            leadingIcon = Icons.Default.Info,
            initialExpanded = true,
            variant = CardVariant.OUTLINED
        ) {
            AppBody(
                text = "Bu kart genişletilebilir yapısıyla içeriklerinizi düzenli tutar. " +
                        "Aydınlık ve Karanlık tema desteği tam olarak entegre edilmiştir."
            )
        }

        AppExpandableCard(
            title = "Favori Notlar",
            leadingIcon = Icons.Default.Star,
            initialExpanded = false,
            variant = CardVariant.ELEVATED
        ) {
            AppBody(
                text = "Sık kullanılan ve favorilere eklenen notlar burada listelenir."
            )
        }

        // 2. Expandable Text
        AppExpandableCard(
            title = "Uzun Metin (Expandable Text)",
            variant = CardVariant.SURFACE
        ) {
            AppExpandableText(
                text = "Memcloud uygulaması; notlarınızı, fikirlerinizi ve yapılacaklar listelerinizi " +
                        "güvenli bir şekilde bulut üzerinde saklamanızı sağlayan modern bir mimariye sahiptir. " +
                        "Gelişmiş Jetpack Compose UI kütüphanesi sayesinde kullanıcı deneyimi en üst düzeye çıkarılmıştır. " +
                        "Bu bileşen uzun açıklamaların ekranı kaplamasını önleyerek temiz bir görünüm sunar.",
                collapsedMaxLines = 2
            )
        }

        // 3. Expandable List Items
        Column {
            AppExpandableListItem(
                title = "Genel Ayarlar",
                subtitle = "Tema, dil ve bildirim tercihleri",
                leadingIcon = Icons.Default.Settings,
                badgeText = "Yeni",
                initialExpanded = false
            ) {
                AppBody("Ayarlar içeriği buraya gelir...")
            }

            AppExpandableListItem(
                title = "Sıkça Sorulan Sorular",
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                initialExpanded = false
            ) {
                AppBody("Hesabınızı ve verilerinizi profil ayarları bölümünden kolayca yönetebilirsiniz.")
            }
        }

        // 4. Accordion Demo
        AppTitle(
            text = "Accordion (Single Select Mode)",
            style = MaterialTheme.typography.titleMedium
        )

        val accordionItems = listOf(
            AccordionItem(
                id = 1,
                title = "1. Adım: Kayıt Olun",
                leadingIcon = Icons.Default.Info,
                badgeText = "1",
                content = { AppBody("Hesap oluşturmak için e-posta adresinizi doğrulayın.") }
            ),
            AccordionItem(
                id = 2,
                title = "2. Adım: Not Oluşturun",
                leadingIcon = Icons.Default.Star,
                badgeText = "2",
                content = { AppBody("Zengin metin editörü ile ilk notunuzu yazmaya başlayın.") }
            ),
            AccordionItem(
                id = 3,
                title = "3. Adım: Senkronize Edin",
                leadingIcon = Icons.Default.Settings,
                badgeText = "5",
                content = { AppBody("Cihazlarınız arasında anlık senkronizasyonun tadını çıkarın.") }
            )
        )

        AppAccordion(
            items = accordionItems,
            mode = AccordionMode.SINGLE,
            style = AccordionStyle.CARD,
            initialExpandedId = 1
        )

        // 5. Borderless Accordion Demo
        AppTitle(
            text = "Accordion (Borderless Variant)",
            style = MaterialTheme.typography.titleMedium
        )

        AppAccordion(
            items = accordionItems,
            mode = AccordionMode.SINGLE,
            style = AccordionStyle.BORDERLESS,
            initialExpandedId = 2
        )
    }
}

@Preview(showBackground = true, name = "Expandable Components Light Mode")
@Composable
private fun ExpandableComponentsLightPreview() {
    MemCloudApplicationTheme(darkTheme = false) {
        Surface {
            ExpandableComponentsDemo()
        }
    }
}

@Preview(
    showBackground = true,
    name = "Expandable Components Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ExpandableComponentsDarkPreview() {
    MemCloudApplicationTheme(darkTheme = true) {
        Surface {
            ExpandableComponentsDemo()
        }
    }
}
