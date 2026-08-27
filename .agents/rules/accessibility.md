# Accessibility Rules

## Content Descriptions (Zorunlu)
- Tüm interaktif bileşenler (butonlar, ikonlar, switch'ler) anlamlı bir `contentDescription` içermelidir
- Dekoratif ikonlar `contentDescription = null` olarak işaretlenmelidir
- `contentDescription` string'leri de `stringResource()` ile localize edilmelidir

```kotlin
// ✅ İnteraktif ikon — açıklama gerekli
Icon(
    Icons.Default.Delete,
    contentDescription = stringResource(R.string.cd_delete_note)
)

// ✅ Dekoratif ikon — null
Icon(
    Icons.Default.Circle,
    contentDescription = null
)
```

## Semantics
- Karmaşık custom bileşenlerde `Modifier.semantics { }` ile anlamlı bilgi sağlanmalıdır
- Özellikle chart, progress bar ve custom gesture bileşenlerinde erişilebilirlik bilgisi eklenmelidir

## Touch Targets
- Material 3 bileşenleri minimum 48dp touch target'ı otomatik sağlar
- Custom bileşenlerde `Modifier.minimumInteractiveComponentSize()` kullanılmalıdır

## Renk Kontrastı
- Metin ve arka plan arasında WCAG AA standardına uygun kontrast oranı sağlanmalıdır
- `MaterialTheme.colorScheme` renkleri zaten bu standardı karşılar; hardcoded renkler kullanılırken kontrastta dikkatli olunmalıdır
