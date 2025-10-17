-dontwarn org.slf4j.impl.StaticLoggerBinder
-keep class com.google.errorprone.annotations.CanIgnoreReturnValue {
    *;
}

-keep class hnau.echospeak.projector.Res** { *; }
-keep class org.jetbrains.compose.resources.** { *; }
-keep interface org.jetbrains.compose.resources.** { *; }
-keep class androidx.compose.runtime.internal.** { *; }
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidResourcesKt { *; }
-keep class androidx.compose.ui.res.** { *; }
-keep class android.content.res.** { *; }
-keep class android.util.** { *; }
#-keep class hnau.common.app.projector.utils.BuildPrettyColorSchemeKt
-keep class hnau.common.android.AndroidDynamicColorsGenerator

-dontwarn androidx.test.platform.app.InstrumentationRegistry
-dontwarn org.jetbrains.compose.resources.AndroidContextProviderKt