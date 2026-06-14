# ─── RootEncoder ─────────────────────────────────────────────────────────────
-keep class com.pedro.** { *; }
-keep interface com.pedro.** { *; }
-keep enum com.pedro.** { *; }
-keep class net.ossrs.** { *; }
-dontwarn com.pedro.**
-dontwarn net.ossrs.**

# ─── Kotlin ──────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { <methods>; }

# ─── Kotlin Coroutines ───────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ─── kotlinx.serialization ───────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }

# ─── Hilt / Dagger ───────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# ─── AndroidX / Compose ──────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.lifecycle.** { *; }

# ─── App data models (DataStore / serialization) ─────────────────────────────
-keep class com.castIRL.data.** { *; }
-keep class com.castIRL.streaming.** { *; }

# ─── General Android ─────────────────────────────────────────────────────────
-keepattributes SourceFile, LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-renamesourcefileattribute SourceFile
