# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Ozodrukh/Documents/Android/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

##### REMOVE WARNING FROM CLASSES
-dontwarn rx.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit.appengine.UrlFetchClient
-keepattributes Annotation
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {@retrofit.http.* <methods>; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class sun.misc.Unsafe { *; }


-keep class com.yandex.metrica.** { *; }
-dontwarn com.yandex.metrica.**

-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}