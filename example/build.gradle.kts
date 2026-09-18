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

    lint {
        // The library is an `api` consumer of Robolectric and the plugin puts the library on
        // `debugImplementation` so @Preview can render the frames. Lint therefore sees
        // org.robolectric.shadows.ShadowService on the debug classpath, decides this app posts
        // notifications, and demands POST_NOTIFICATIONS in the manifest.
        //
        // It does not. Robolectric never runs outside a unit test, and the permission would be a
        // lie told to satisfy an analysis of code that is not reachable from the app at all — so
        // the check is disabled here rather than the manifest being changed to match it.
        disable += "NotificationPermission"
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
    // The plugin puts the library on `test` and `debug` only, and that is deliberate: an app that
    // applies it should not ship a screenshot-generation dependency, and everything that reaches
    // for the library — the screenshot tests and the @Preview functions — lives in one of those two.
    //
    // This module is the exception, because it is the example rather than a consumer: its banners
    // and mockup layouts sit in `src/main` so the screenshot tests can see them, which means the
    // release variant compiles them too and needs the library to do it. Without this, `./gradlew
    // build` fails on a variant nobody ever ships. Nothing is published from here, so putting the
    // library on release costs only this module's own build.
    releaseImplementation(project(":library"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
