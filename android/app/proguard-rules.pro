# Proguard rules for SkinSafe

# Keep Retrofit and Gson data models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.skinsafe.app.data.models.** { *; }

# Keep CameraX
-keep class androidx.camera.core.** { *; }
