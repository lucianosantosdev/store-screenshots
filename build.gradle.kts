plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.roborazzi) apply false
}

// group + version come from gradle.properties; overridden by -Pversion=… in CI.
// Maven Central publishing + signing are configured per-module via the
// com.vanniktech.maven.publish plugin (see library/ and plugin/ build files).
subprojects {
    group = rootProject.group
    version = rootProject.version
}
