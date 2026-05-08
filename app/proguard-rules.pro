# LiteBox TV — ProGuard rules
-keepattributes Signature
-keepattributes *Annotation*

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }

# Kotlin serialization
-keepattributes EnclosingMethod
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coil
-keep class coil.** { *; }
