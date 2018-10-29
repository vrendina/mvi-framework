# ------------------------
#
#  Mvi Proguard Config
#
# -----------------------

-keep class ** implements com.victorrendina.mvi.MviState {
    public <init>(...);
    *;
}
-keep class com.victorrendina.mvi.MviState { *; }

-keep class kotlin.Metadata { *; }
-keep,includedescriptorclasses public class kotlin.reflect.jvm.internal.** { public *; }
-dontnote kotlin.reflect.jvm.internal.**