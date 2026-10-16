# ---- kotlinx.serialization (rules models) ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.bharatsuraksha.saathi.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.bharatsuraksha.saathi.domain.model.**$$serializer { *; }

# ---- ML Kit on-device text recognition ----
# The bundled OCR models and their entry points must survive shrinking.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# ---- Hilt / Dagger generate their own rules; nothing extra needed. ----

# Keep our services (they are started by the system by name).
-keep class com.bharatsuraksha.saathi.service.** { *; }
