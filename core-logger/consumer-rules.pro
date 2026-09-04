# Retain file names and line numbers in stack traces for accurate auto-tagging in release builds
-keepattributes SourceFile,LineNumberTable

# Room rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
