# ============================================================================
# R8 / ProGuard rules for Suraksha Saathi.
#
# The app is offline-first and uses reflection-heavy libraries, so each one gets
# an explicit keep block. If a release build ever misbehaves, check here first.
# ============================================================================

-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# ---- kotlinx.serialization (the scam rule models are parsed from JSON) ----
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.bharatsuraksha.saathi.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.bharatsuraksha.saathi.domain.model.**$$serializer { *; }
-keep class com.bharatsuraksha.saathi.domain.model.** { *; }

# ---- ML Kit on-device text recognition (bundled models, no network) ----
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode** { *; }
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.mlkit.vision.barcode.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.mlkit.vision.text.chinese.**
-dontwarn com.google.mlkit.vision.text.japanese.**
-dontwarn com.google.mlkit.vision.text.korean.**

# ---- CameraX ----
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# ---- Hilt / Dagger generate their own rules. Keep the entry points anyway. ----
-keep,allowobfuscation @interface dagger.hilt.android.AndroidEntryPoint
-keep,allowobfuscation @interface dagger.hilt.android.HiltAndroidApp
-keep,allowobfuscation @interface dagger.hilt.android.lifecycle.HiltViewModel

# ---- WorkManager + the Hilt worker factory (workers are built by name) ----
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class com.bharatsuraksha.saathi.work.** { *; }
-keep class androidx.hilt.work.** { *; }

# ---- Components the system starts by name ----
-keep class com.bharatsuraksha.saathi.service.** { *; }
-keep class com.bharatsuraksha.saathi.MainActivity { *; }
-keep class com.bharatsuraksha.saathi.BharatSurakshaApp { *; }

# ---- Compose keeps what it needs; silence the usual noise ----
-dontwarn org.jetbrains.annotations.**
