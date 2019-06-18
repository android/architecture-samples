# Proguard rules that are applied to your test apk/code.
-ignorewarnings
-dontoptimize

-keepattributes *Annotation*

-keep class androidx.test.espresso.**
# keep the class and specified members from being removed or renamed
-keep class androidx.test.espresso.IdlingRegistry { *; }
-keep class androidx.test.espresso.IdlingResource { *; }

-dontnote junit.framework.**
-dontnote junit.runner.**

-dontwarn androidx.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter
# Uncomment this if you use Mockito
-dontwarn org.mockito.**