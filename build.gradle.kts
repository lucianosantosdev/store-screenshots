// AGP 9 ships built-in Kotlin backed by KGP 2.2.10. This project pins a newer Kotlin
// (see gradle/libs.versions.toml), so force the matching KGP onto the build classpath — the
// compose compiler plugin is versioned to the same Kotlin and must line up with the compiler.
buildscript {
    dependencies {
        // Keep in sync with `kotlin` in gradle/libs.versions.toml.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
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
