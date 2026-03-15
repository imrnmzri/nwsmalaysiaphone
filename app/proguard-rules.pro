# Data models (Gson serialization)
-keep class my.gov.met.nwsmalaysia.data.model.** { *; }
-keep class my.gov.met.nwsmalaysia.domain.model.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep,allowobfuscation interface * { @retrofit2.http.* <methods>; }
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn dagger.hilt.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
