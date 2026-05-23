import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    implementation(libs.roborazzi.gradlePlugin)
}

gradlePlugin {
    plugins {
        create("storeScreenshots") {
            id = "dev.lucianosantos.storescreenshots"
            displayName = "Store Screenshots"
            description = "Generates framed Play Store / App Store screenshots from Compose UI."
            implementationClass = "dev.lucianosantos.storescreenshots.gradle.StoreScreenshotsPlugin"
        }
    }
}
