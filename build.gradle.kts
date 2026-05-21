plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.roborazzi) apply false
}

group = "dev.lucianosantos.storescreenshots"
version = "0.1.0"

subprojects {
    group = rootProject.group
    version = rootProject.version
}
