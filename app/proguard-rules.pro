-keep class com.myurrrjj.trackabite.local.** {*;}
-keep class * extends androidx.room.RoomDatabase{*;}
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keep class com.google.genai.** { *; }
-keep class com.myurrrjj.trackabite.network.** { *; }
