import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
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
            id = "io.github.lucianosantosdev.storescreenshots"
            displayName = "Store Screenshots"
            description = "Generates framed Play Store / App Store screenshots from Compose UI."
            implementationClass = "dev.lucianosantos.storescreenshots.gradle.StoreScreenshotsPlugin"
        }
    }
}

mavenPublishing {
    configure(GradlePlugin(javadocJar = JavadocJar.Empty(), sourcesJar = true))
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), "storescreenshots-plugin", project.version.toString())
    pom {
        name.set("store-screenshots plugin")
        description.set("Gradle plugin for generating framed Play Store / App Store screenshots from Compose UI.")
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
