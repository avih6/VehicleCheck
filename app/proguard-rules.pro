# Optimization & Code Shrinking
-allowaccessmodification

# Keep Data Models & Services
-keep class com.avih6.vehiclecheck.data.** { *; }
-dontwarn com.avih6.vehiclecheck.data.**

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
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

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