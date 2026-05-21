# store-screenshots

Gradle plugin + Compose library for generating framed Play Store / App Store screenshots from Compose UI under Robolectric.

## What it gives you

- An `@Screenshot(locales = [...], title = "...", description = "...")` annotation
- A `ScreenshotRule` JUnit rule that renders your composable inside a device frame at the exact pixel size each store expects
- A `screenshots` source set (auto-created by the plugin) so screenshot code lives separately from regular tests
- Output written directly into the Fastlane metadata layout

## Quick start

`settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("path/to/store-screenshots")
}
```

`mobile/build.gradle.kts`:

```kotlin
plugins {
    id("dev.lucianosantos.storescreenshots")
}
```

`mobile/src/screenshots/kotlin/.../MyScreenshots.kt`:

```kotlin
@RunWith(RobolectricTestRunner::class)
class MyScreenshots {
    @get:Rule val screenshot = ScreenshotRule(FormFactor.Phone)

    @Test
    @Screenshot(
        locales = ["en-US", "pt-BR"],
        title = "Set up your workout",
        description = "Pick sections, train and rest times",
        backgroundColor = 0xFF1F2937,
    )
    fun settings() = screenshot.capture {
        MySettingsScreen()
    }
}
```

Run with:

```
./gradlew :mobile:storeScreenshots
```

Output lands at `fastlane/metadata/android/{locale}/images/{phone|wear|sevenInch|tenInch}Screenshots/`.

## Supported form factors

| FormFactor | Output size | Fastlane dir |
| --- | --- | --- |
| `Phone` | 1080 x 1920 | `phoneScreenshots` |
| `Wear` | 384 x 384 | `wearScreenshots` |
| `Tablet7` | 1200 x 1920 | `sevenInchScreenshots` |
| `Tablet10` | 1600 x 2560 | `tenInchScreenshots` |
| `AppleIPhone67` | 1290 x 2796 | `fastlane/screenshots/{locale}/iphone67` |

## Status

Pre-1.0. Used by the [Interval Timer](https://github.com/lucianosantosdev/IntervalTimer) app.
