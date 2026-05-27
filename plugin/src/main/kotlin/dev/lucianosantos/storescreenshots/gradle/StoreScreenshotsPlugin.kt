package dev.lucianosantos.storescreenshots.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import java.util.Properties

/**
 * Apply with:
 *
 * ```kotlin
 * plugins { id("dev.lucianosantos.storescreenshots") }
 * ```
 *
 * What this does to the host module:
 * 1. Applies the Roborazzi Gradle plugin (so `captureRoboImage` works at test time).
 * 2. Adds `src/screenshots/{kotlin,res}` to the Android `test` source set so screenshot code
 *    lives separately from regular unit tests without needing a custom build variant.
 * 3. Wires the store-screenshots library in as a `testImplementation` dependency.
 * 4. Enables Android resource & return-default-values in unit tests (Robolectric needs both).
 * 5. Sets `storeScreenshots.outputRoot` to the root project dir on all `Test` tasks,
 *    so screenshots land at `<repoRoot>/fastlane/metadata/...` regardless of which module
 *    produced them.
 * 6. Registers a `storeScreenshots` task in the host module that runs `testDebugUnitTest`
 *    and is the recommended entry point.
 */
class StoreScreenshotsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("storeScreenshots", StoreScreenshotsExtension::class.java)
        extension.destDir.convention(target.layout.buildDirectory.dir("outputs/store-screenshots"))

        // Apply Roborazzi by class to avoid id-based plugin lookup, which doesn't resolve
        // when this plugin is consumed via composite build.
        target.pluginManager.apply(io.github.takahirom.roborazzi.RoborazziPlugin::class.java)

        target.pluginManager.withPlugin("com.android.application") { configureAndroid(target) }
        target.pluginManager.withPlugin("com.android.library") { configureAndroid(target) }

        target.dependencies.add("testImplementation", libraryNotation())
        // Also add to debug so Android Studio can render @Preview functions that
        // reference library classes (ScreenshotPreview, FormFactor, frames, etc.).
        target.dependencies.add("debugImplementation", libraryNotation())

        // If the consuming project lives in the same build as a `:library` project that
        // matches our group/name (i.e. the example module inside this very repo), wire it
        // up by project reference instead of a maven coord. Composite-build consumers don't
        // need this because Gradle substitutes group:name across included builds automatically.
        target.afterEvaluate {
            val siblingLibrary = target.rootProject.allprojects.firstOrNull { sibling ->
                sibling.path != target.path &&
                    sibling.name == "library" &&
                    sibling.group.toString() == "dev.lucianosantos.storescreenshots"
            }
            if (siblingLibrary != null) {
                target.configurations.configureEach { config ->
                    config.resolutionStrategy.dependencySubstitution { rules ->
                        rules.substitute(rules.module("dev.lucianosantos.storescreenshots:library"))
                            .using(rules.project(siblingLibrary.path))
                            .because("same-build sibling project takes precedence over Maven lookup")
                    }
                }
            }
        }

        target.tasks.withType(Test::class.java).configureEach { task ->
            task.systemProperty(
                "storeScreenshots.outputRoot",
                extension.destDir.map { it.asFile.absolutePath }.get()
            )
            // Roborazzi defaults to compare mode; we always want to write images.
            task.systemProperty("roborazzi.test.record", "true")
        }

        target.tasks.register("storeScreenshots") { task ->
            task.group = "store-screenshots"
            task.description = "Generates framed Play Store / App Store screenshots."
            task.dependsOn("testDebugUnitTest")
        }
    }

    private fun configureAndroid(target: Project) {
        val androidExt = target.extensions.findByType(BaseExtension::class.java)
            ?: error("Could not find Android extension on project ${target.path}")

        @Suppress("UNCHECKED_CAST")
        val common = androidExt as CommonExtension<*, *, *, *, *, *>

        val testSourceSet = androidExt.sourceSets.getByName("test")
        testSourceSet.java.srcDir("src/screenshots/kotlin")
        testSourceSet.java.srcDir("src/screenshots/java")
        testSourceSet.resources.srcDir("src/screenshots/resources")
        testSourceSet.res.srcDir("src/screenshots/res")


        common.testOptions.unitTests.apply {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    private fun libraryNotation(): Any {
        // When the plugin is consumed via composite build (includeBuild), Gradle substitutes the
        // group:name pair with the included build's project. When consumed from GitHub Packages,
        // this resolves to the matching library version published alongside the plugin.
        return mapOf(
            "group" to "dev.lucianosantos.storescreenshots",
            "name" to "library",
            "version" to pluginVersion,
        )
    }

    companion object {
        // Embedded at build time by `generateVersionResource` in plugin/build.gradle.kts.
        internal val pluginVersion: String by lazy {
            StoreScreenshotsPlugin::class.java.getResourceAsStream("/store-screenshots-version.properties")
                ?.bufferedReader()
                ?.use { reader -> Properties().apply { load(reader) }.getProperty("version") }
                ?: error("store-screenshots-version.properties is missing from the plugin jar")
        }
    }
}
