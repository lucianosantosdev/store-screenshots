plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.roborazzi) apply false
}

// group + version come from gradle.properties; overridden by -Pversion=… in CI.
subprojects {
    group = rootProject.group
    version = rootProject.version

    // Every publishable subproject targets GitHub Packages. Credentials come from env vars
    // GITHUB_ACTOR / GITHUB_TOKEN so local `publishToMavenLocal` works without secrets,
    // and the release workflow injects the real values.
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
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
    }
}
