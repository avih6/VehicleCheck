# Optimization & Code Shrinking
-repackageclasses ''
-allowaccessmodification

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Retrofit & Kotlinx Serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
-keep class kotlinx.serialization.** { *; }

# Coil Image Loader
-dontwarn coil.**
-keep class coil.** { *; }

# Keep Ads
-keep class com.google.android.gms.ads.** { *; }

# Security: Strip sensitive debugging and system logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}