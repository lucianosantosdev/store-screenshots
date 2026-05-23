pluginManagement {
    // :plugin is its own Gradle build so it can be applied to :example by id.
    includeBuild("plugin")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
includeBuild("plugin")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "store-screenshots"
include(":library")
include(":example")
