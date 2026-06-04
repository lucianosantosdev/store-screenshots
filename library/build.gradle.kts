import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
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

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), "storescreenshots-library", project.version.toString())
    pom {
        name.set("store-screenshots library")
        description.set("Runtime API (annotation, rule, frames) for store-screenshots.")
        inceptionYear.set("2026")
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

// Keep GitHub Packages as a secondary target alongside Maven Central.
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/lucianosantosdev/store-screenshots")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
                password = System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.token").orNull
            }
        }
    }
}
