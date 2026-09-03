-dontoptimize
-keepattributes *
-keep class android.** {
    *;
}
-keep class com.kangrio.** {
    *;
}
-repackageclasses 'com.kangrio'