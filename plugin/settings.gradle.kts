// Single-project Gradle build for the storescreenshots plugin.
// It exists as a separate build so :example (in the root build) can apply the plugin via
// its id — sibling-module plugin application within one build isn't supported.

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "plugin"
