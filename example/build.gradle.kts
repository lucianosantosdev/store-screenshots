import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.composeCompiler)
    id("dev.lucianosantos.storescreenshots")
}

android {
    namespace = "dev.lucianosantos.storescreenshots.example"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

storeScreenshots {
    // Keep example PNGs inside this module so README links stay stable.
    destDir = layout.projectDirectory.dir("screenshots")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)

    // Robolectric host activity for Compose tests. ScreenshotRule's createComposeRule()
    // needs ComponentActivity in the merged debug manifest.
    debugImplementation(libs.compose.ui.test.manifest)
}
