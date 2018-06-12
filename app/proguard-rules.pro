-dontwarn rx.**
-dontwarn retrofit2.**
-dontwarn javax.naming.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp 3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
