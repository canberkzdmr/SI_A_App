# Proguard rules for :core-sync (JNI bindings).
# If we obfuscate, keep JNI entry points stable by keeping the native wrapper classes.
-keep class com.cbo.core.sync.** { *; }



