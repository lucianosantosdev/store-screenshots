import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
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
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "storescreenshots-plugin"
        }
    }
}

// Embed the plugin's resolved version as a resource so StoreScreenshotsPlugin can declare a
// matching testImplementation coordinate on :library without hardcoding the version.
val generateVersionResource = tasks.register("generateVersionResource") {
    val outputDir = layout.buildDirectory.dir("generated/version")
    val versionString = project.version.toString()
    inputs.property("version", versionString)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile.apply { mkdirs() }
        dir.resolve("store-screenshots-version.properties")
            .writeText("version=$versionString\n")
    }
}
sourceSets.named("main") {
    resources.srcDir(generateVersionResource)
}
