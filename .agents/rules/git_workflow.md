# Git Workflow Rules

## Branch Naming
- `feature/<kisa-aciklama>` — Yeni özellikler (e.g., `feature/statistics-screen`)
- `fix/<kisa-aciklama>` — Hata düzeltmeleri (e.g., `fix/note-delete-crash`)
- `refactor/<kisa-aciklama>` — Refactoring (e.g., `refactor/uikit-cleanup`)
- `chore/<kisa-aciklama>` — Yapılandırma, bağımlılık güncellemeleri

## Commit Mesajları
Conventional Commits formatı kullanılmalıdır:
```
<type>(<scope>): <açıklama>

[opsiyonel body]
```

**Type'lar:**
- `feat` — Yeni özellik
- `fix` — Hata düzeltme
- `refactor` — Davranış değiştirmeyen kod düzenlemesi
- `style` — Biçimlendirme, import düzenleme
- `test` — Test ekleme/düzenleme
- `docs` — Dokümantasyon
- `chore` — Build, bağımlılık, CI değişiklikleri

**Scope:** Değişikliğin yapıldığı modül (e.g., `notes`, `ui`, `core-database`)

**Örnekler:**
```
feat(statistics): add heatmap chart component
fix(notes): resolve crash on empty category filter
refactor(ui): extract AppSectionCard to display components
test(user): add ProfileViewModel unit tests
chore(deps): bump Room to 2.8.4
```

## Genel Kurallar
- `main` branch'ine doğrudan commit yapılmaz
- Her feature kendi branch'inde geliştirilir
- Commit'ler atomik ve tek bir değişikliğe odaklı olmalıdır
