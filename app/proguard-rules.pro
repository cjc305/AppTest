# AppTest ProGuard / R8 rules
# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, Signature, Exception
-keep class kotlin.Metadata { *; }

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ── Kotlinx Serialization ─────────────────────────────────────────────────────
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# ── Retrofit ─────────────────────────────────────────────────────────────────
-keep interface com.apptest.core.network.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── OkHttp ───────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Data models (keep for JSON deserialization) ───────────────────────────────
-keep class com.apptest.core.network.** { *; }
-keep class com.apptest.core.common.** { *; }
-keep class com.apptest.core.domain.** { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ── Compose ──────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Firebase ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── Coil ─────────────────────────────────────────────────────────────────────
-dontwarn coil.**
