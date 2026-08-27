# Localization Rules

## Hardcoded String Yasağı
- Kullanıcıya görünen **hiçbir string** Kotlin kodunda hardcoded olarak yazılmamalıdır
- Tüm UI string'leri `strings.xml` dosyalarında tanımlanmalı ve Compose içinde `stringResource(R.string.xxx)` ile kullanılmalıdır
- Sadece log mesajları, hata debug mesajları ve `contentDescription = null` gibi durumlar istisnadır

## Çoklu Dil Desteği (Zorunlu)
- Her yeni string eklenirken **hem Türkçe hem İngilizce** çevirisi birlikte oluşturulmalıdır
- Dizin yapısı:
  ```
  src/main/res/
  ├── values/strings.xml          ← İngilizce (default)
  └── values-tr/strings.xml       ← Türkçe
  ```
- İngilizce her zaman default dil olarak `values/strings.xml`'de yer alır
- Türkçe çeviri `values-tr/strings.xml`'de yer alır

## String Naming Convention
- `snake_case` kullanılmalıdır
- Ekran/özellik prefix'i ile gruplandırılmalıdır:
  ```xml
  <!-- Notes ekranı -->
  <string name="notes_title">Notes</string>
  <string name="notes_empty_state">No notes yet</string>
  <string name="notes_search_hint">Search notes…</string>

  <!-- Statistics ekranı -->
  <string name="statistics_title">Statistics</string>
  <string name="statistics_overview">Overview</string>
  ```

## Plurals ve Biçimlendirme
- Sayıya bağlı string'ler için `<plurals>` kullanılmalıdır:
  ```xml
  <plurals name="notes_count">
      <item quantity="one">%d note</item>
      <item quantity="other">%d notes</item>
  </plurals>
  ```
- Değişken içeren string'ler için format argument kullanılmalıdır:
  ```xml
  <string name="statistics_completed_tasks">%1$d / %2$d tasks completed (%3$d%%)</string>
  ```

## Compose Kullanımı
```kotlin
// ✅ Doğru
AppTitle(text = stringResource(R.string.statistics_title))
AppBody(text = stringResource(R.string.notes_count, noteCount))

// ❌ Yanlış
AppTitle(text = "İstatistikler")
AppBody(text = "$noteCount not bulundu")
```
