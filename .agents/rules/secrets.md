# Secrets & API Key Management

## Temel Kural
- **Hiçbir API key, secret, token veya credential Kotlin/XML/Gradle dosyalarında hardcoded olarak yazılmamalıdır**
- Tüm secret'lar `local.properties` dosyasında saklanmalı ve build sistemi üzerinden erişilmelidir

## Yapı
```
local.properties          ← Secret'lar burada (Git'e dahil EDİLMEZ)
├── MAPS_API_KEY=...
├── SIGNING_KEY_PASSWORD=...
└── ...
```

## Secret Okuma Yöntemi
`local.properties`'den okunan değerler `build.gradle.kts` içinde `manifestPlaceholders` veya `BuildConfig` aracılığıyla koda aktarılır:

```kotlin
// build.gradle.kts
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val apiKey = localProperties.getProperty("MY_API_KEY", "")
manifestPlaceholders["MY_API_KEY"] = apiKey
```

```xml
<!-- AndroidManifest.xml -->
<meta-data android:name="com.example.API_KEY"
           android:value="${MY_API_KEY}" />
```

## Mevcut Secret'lar
| Key | Kullanım |
|---|---|
| `MAPS_API_KEY` | Google Maps API key (`AndroidManifest.xml` manifest placeholder) |

## Secrets Gradle Plugin
Proje `com.google.android.libraries.mapsplatform.secrets-gradle-plugin` kullanır. Bu plugin `local.properties` değerlerini otomatik olarak `BuildConfig`'e ve manifest placeholder'lara aktarır.

## .gitignore Zorunlulukları
Aşağıdaki dosya/pattern'ler `.gitignore`'da **mutlaka** bulunmalıdır:
- `local.properties` — SDK path ve API key'ler
- `google-services.json` — Firebase konfigürasyonu
- `*.jks`, `*.keystore`, `*.pepk`, `*.p12`, `*.pem` — Signing key'leri

## AI İçin Kurallar
- Secret gerektiren yeni bir servis entegre edilirken, key'i doğrudan koda yazmak yerine `local.properties` → `manifestPlaceholders` veya `BuildConfig` akışını kullanın
- PR/commit'lerde secret içeren dosyaların yer almadığından emin olun
- Örnek yapılandırma dosyası gerekiyorsa `local.properties.example` oluşturun (gerçek değerler olmadan)
