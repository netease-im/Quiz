# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the talk_line_layout number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the talk_line_layout number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

### @keep
-keep,allowobfuscation @interface com.netease.nim.quizgame.common.annotation.Keep
-keep @com.netease.nim.quizgame.common.annotation.Keep class *
-keepclassmembers class * {
    @com.netease.nim.quizgame.common.annotation.Keep *;
}

### @KeepMemberNames
-keep,allowobfuscation @interface com.netease.nim.quizgame.common.annotation.KeepMemberNames
-keep @com.netease.nim.quizgame.common.annotation.KeepMemberNames class *
-keepclasseswithmembernames @com.netease.nim.quizgame.common.annotation.KeepMemberNames class * {*;}

### NIM SDK
-dontwarn com.netease.**
-keep class com.netease.nimlib.** {*;}
-keep class com.netease.share.** {*;}
-keep class com.netease.neliveplayer.** {*;}

### tinker lib
-keepattributes *Annotation*
-dontwarn com.tencent.tinker.**
-keep class com.tencent.tinker.** {*;}
-keep public class * extends com.tencent.tinker.loader.TinkerLoader {*;}
-keep public class * implements com.tencent.tinker.loader.app.ApplicationLifeCycle {*;}
-keep @com.tencent.tinker.anno.DefaultLifeCycle public class *

### NIM hotfix SDK
-keep class com.netease.nim.hotfix.** { *; }

### ok http
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.netease.nimlib.** {*;}
-keep class okhttp3.** {*;}
-keep class org.codehaus.* {*;}
-keep class java.nio.* {*;}

### fabric
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**