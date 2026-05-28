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
                maven {
                    name = "MavenCentral"
                    url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    credentials {
                        username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: providers.gradleProperty("mavenCentral.user").orNull
                        password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: providers.gradleProperty("mavenCentral.password").orNull
                    }
                }
            }
        }
    }

    plugins.withId("signing") {
        extensions.configure<SigningExtension> {
            val signingKey = System.getenv("GPG_SIGNING_KEY")
            val signingKeyId = System.getenv("GPG_KEY_ID")
            if (signingKey != null) {
                useInMemoryPgpKeys(signingKeyId, signingKey, "")
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
    }
}
