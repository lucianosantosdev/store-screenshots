import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
    signing
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
        maven {
            name = "MavenCentral"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: providers.gradleProperty("mavenCentral.user").orNull
                password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: providers.gradleProperty("mavenCentral.password").orNull
            }
        }
    }
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "storescreenshots-plugin"
        }
        pom {
            name.set("store-screenshots plugin")
            description.set("Gradle plugin for generating framed Play Store / App Store screenshots from Compose UI.")
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

val signingKey = System.getenv("GPG_SIGNING_KEY")
val signingKeyId = System.getenv("GPG_KEY_ID")
if (signingKey != null) {
    signing {
        useInMemoryPgpKeys(signingKeyId, signingKey, "")
        sign(publishing.publications)
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
