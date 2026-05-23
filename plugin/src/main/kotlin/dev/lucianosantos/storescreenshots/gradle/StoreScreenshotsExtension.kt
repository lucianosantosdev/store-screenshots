package dev.lucianosantos.storescreenshots.gradle

import org.gradle.api.file.DirectoryProperty

/**
 * Configures the [StoreScreenshotsPlugin]. Apply with:
 *
 * ```kotlin
 * storeScreenshots {
 *     destDir = layout.projectDirectory.dir("fastlane")
 * }
 * ```
 *
 * If unset, screenshots land at `<rootProject>/fastlane/metadata/android/{locale}/images/...`
 * (Play Store form factors) and `<rootProject>/fastlane/screenshots/{locale}/...` (Apple).
 *
 * When [destDir] is set, the same per-locale subtree is created underneath it instead of the
 * root project. The Fastlane subdirectory layout (phoneScreenshots/wearScreenshots/iphone67/…)
 * is always preserved.
 */
abstract class StoreScreenshotsExtension {
    /**
     * Output root for generated screenshots. Defaults to `rootProject.projectDir`.
     */
    abstract val destDir: DirectoryProperty
}
