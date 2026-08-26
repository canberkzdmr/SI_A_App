package com.cbo.notes.domain.model

/**
 * Kullanıcının tüm not istatistiklerini kapsayan immutable domain modeli.
 * StatisticsScreen'deki tüm bölümler bu modelden beslenir.
 */
data class NoteStatistics(

    // -------------------------------------------------------------------------
    // Bölüm 1: İçerik Metrikleri
    // -------------------------------------------------------------------------

    /** Aktif (arşivlenmemiş ve silinmemiş) not sayısı */
    val totalNotes: Int = 0,
    /** Arşivlenmiş not sayısı */
    val archivedNotes: Int = 0,
    /** Favori not sayısı */
    val favoriteNotes: Int = 0,
    /** Sabitlenmiş not sayısı */
    val pinnedNotes: Int = 0,
    /** Çöp kutusundaki not sayısı */
    val deletedNotes: Int = 0,

    /** Tüm notlardaki yaklaşık toplam kelime sayısı */
    val totalWordCount: Long = 0L,

    /** Tüm notlardaki toplam todo öğesi sayısı */
    val totalTodoItems: Int = 0,
    /** Tamamlanmış todo öğesi sayısı */
    val completedTodoItems: Int = 0,

    /** Notlardaki toplam ek (resim + ses) sayısı */
    val totalAttachments: Int = 0,
    /** Yalnızca görsel ek sayısı */
    val imageAttachments: Int = 0,
    /** Yalnızca ses kaydı ek sayısı */
    val audioAttachments: Int = 0,

    // -------------------------------------------------------------------------
    // Bölüm 2: Üretkenlik & Alışkanlık Analitiği
    // -------------------------------------------------------------------------

    /**
     * Tarih → not sayısı eşlemesi. "YYYY-MM-DD" formatında.
     * Heatmap ve streak hesaplama için kullanılır.
     */
    val notesPerDay: Map<String, Int> = emptyMap(),

    /**
     * Saat → not sayısı eşlemesi (0-23).
     * En verimli saat analizinde kullanılır.
     */
    val notesPerHour: Map<Int, Int> = emptyMap(),

    /**
     * Haftanın günü → not sayısı eşlemesi.
     * SQLite strftime('%w') çıktısına göre: 0=Pazar, 1=Pazartesi, … 6=Cumartesi.
     */
    val notesPerDayOfWeek: Map<Int, Int> = emptyMap(),

    /**
     * "YYYY-MM" → not sayısı. Aylık trend grafiği için kullanılır.
     */
    val notesPerMonth: Map<String, Int> = emptyMap(),

    /** Güncel ardışık aktif gün (seri / streak) sayısı */
    val currentStreak: Int = 0,
    /** Tarihin en uzun ardışık aktif gün serisi */
    val longestStreak: Int = 0,

    // -------------------------------------------------------------------------
    // Bölüm 3: Kategori & Etiket İçgörüleri
    // -------------------------------------------------------------------------

    /** Kategori adı → not sayısı eşlemesi. Donut chart için. */
    val categoryDistribution: Map<String, Int> = emptyMap(),

    /** En çok kullanılan etiketler (ad, kullanım sayısı) listesi */
    val topTags: List<Pair<String, Int>> = emptyList(),

    /** Renk hex kodu (ya da "Varsayılan") → not sayısı eşlemesi */
    val colorDistribution: Map<String, Int> = emptyMap(),

    // -------------------------------------------------------------------------
    // Bölüm 4: Zettelkasten & Bilgi Ağı Analitiği
    // -------------------------------------------------------------------------

    /** Toplam Zettelkasten bağlantı sayısı */
    val totalNoteLinks: Int = 0,
    /** (noteId, bağlantı sayısı) — en çok bağlantı alan notlar */
    val mostLinkedNotes: List<Pair<Int, Int>> = emptyList(),
    /** Hiç bağlantısı olmayan not sayısı */
    val orphanNoteCount: Int = 0,

    // -------------------------------------------------------------------------
    // Bölüm 5: Konum & Hatırlatıcı Analitiği
    // -------------------------------------------------------------------------

    /** Aktif (gelecekte tetiklenecek) hatırlatıcı sayısı */
    val activeReminderCount: Int = 0,
    /** Süresi geçmiş hatırlatıcı sayısı */
    val expiredReminderCount: Int = 0,
    /** Konum hatırlatıcısı etkin not sayısı */
    val locationReminderCount: Int = 0,

    // -------------------------------------------------------------------------
    // Bölüm 6: Haftalık Özet & Not Sağlığı
    // -------------------------------------------------------------------------

    /** Bu hafta oluşturulan not sayısı */
    val weeklyNoteCount: Int = 0,
    /** Bu hafta tamamlanan todo öğesi sayısı */
    val weeklyCompletedTodoCount: Int = 0,
    /** Bu hafta en çok kullanılan etiket adı */
    val mostUsedTagThisWeek: String? = null,

    /**
     * 90+ gün boyunca güncellenmemiş not sayısı.
     * "Not Sağlığı" bölümünde uyarı olarak gösterilir.
     */
    val staleNoteCount: Int = 0,

    /**
     * Tüm todo'ları tamamlanmış ve arşivlemeye hazır not sayısı.
     * "Not Sağlığı" bölümünde uyarı olarak gösterilir.
     */
    val fullyCompletedTodoNoteCount: Int = 0,
) {
    /** Todo tamamlanma yüzdesi (0.0 - 1.0) */
    val todoCompletionRate: Float
        get() = if (totalTodoItems > 0) completedTodoItems.toFloat() / totalTodoItems else 0f

    /** Aktif + arşivlenmiş toplam not sayısı */
    val totalActiveAndArchived: Int
        get() = totalNotes + archivedNotes

    /** Tahmini toplam okuma süresi (dakika) — ortalama okuma hızı: 200 kelime/dk */
    val estimatedReadingMinutes: Int
        get() = (totalWordCount / 200L).toInt()

    /** Zettelkasten bağlantı yoğunluğu — not başına düşen ortalama bağlantı sayısı */
    val linkDensity: Float
        get() = if (totalNotes > 0) totalNoteLinks.toFloat() / totalNotes else 0f

    /** En verimli saat (en fazla not yazılan saat dilimi) */
    val peakHour: Int?
        get() = notesPerHour.maxByOrNull { it.value }?.key

    /** En verimli haftanın günü */
    val peakDayOfWeek: Int?
        get() = notesPerDayOfWeek.maxByOrNull { it.value }?.key
}
