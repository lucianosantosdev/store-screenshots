import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    id("io.github.lucianosantosdev.storescreenshots")
}

android {
    namespace = "dev.lucianosantos.storescreenshots.example"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.lucianosantos.storescreenshots.example"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        // Make the test-only device-frame images (src/screenshots/resources/mockups) visible to the
        // debug variant so the DeviceImageMockup @Preview functions can load them. Debug only — the
        // release build never bundles them, keeping them out of the shipped app.
        //
        // As assets (not just classpath resources): the Studio preview renderer (LayoutLib) resolves
        // images through the Context's AssetManager, but does not expose the variant's Java resources,
        // so the previews load the frames from assets/mockups/ via LocalContext.
        getByName("debug") {
            resources.srcDir("src/screenshots/resources")
            assets.srcDir("src/screenshots/resources")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

storeScreenshots {
    destDir = layout.projectDirectory.dir("screenshots")
}

// The full-resolution PNGs the screenshot run produces are git-ignored; the small JPEGs committed
// for the README are produced by the root `compressScreenshots` task (run after `storeScreenshots`).

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
