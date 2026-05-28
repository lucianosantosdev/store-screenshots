import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
    signing
}

android {
    namespace = "dev.lucianosantos.storescreenshots"
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.material3)
    api(libs.compose.foundation)
    api(libs.compose.material.icons.extended)
    api(libs.compose.ui.test.junit4)
    api(libs.compose.ui.tooling.preview)

    api(libs.junit)
    api(libs.robolectric)
    api(libs.roborazzi)
    api(libs.roborazzi.compose)
    api(libs.roborazzi.junit.rule)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            // android.publishing.singleVariant("release") is created after evaluation, so
            // wiring `from(components["release"])` must wait.
            afterEvaluate { from(components["release"]) }
            artifactId = "storescreenshots-library"
            pom {
                name.set("store-screenshots library")
                description.set("Runtime API (annotation, rule, frames) for store-screenshots.")
                url.set("https://github.com/lucianosantosdev/store-screenshots")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("lucianosantosdev")
                        name.set("Luciano Santos")
                        email.set("contact@lucianosantos.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/lucianosantosdev/store-screenshots.git")
                    developerConnection.set("scm:git:ssh://github.com/lucianosantosdev/store-screenshots.git")
                    url.set("https://github.com/lucianosantosdev/store-screenshots")
                }
            }
        }
    }
}
