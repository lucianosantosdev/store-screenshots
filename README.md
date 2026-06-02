# store-screenshots

[![Maven Central](https://img.shields.io/maven-central/v/io.github.lucianosantosdev/storescreenshots-plugin?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.lucianosantosdev/storescreenshots-plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Build your store screenshots with Compose. Automate the process. Keep them always in sync with your app's real design.**

| | | | |
| :---: | :---: | :---: | :---: |
| <img src="example/screenshots/en-US/images/phoneScreenshots/counter.png" width="200" /> | <img src="example/screenshots/en-US/images/phoneScreenshots/custom_layout.png" width="200" /> | <img src="example/screenshots/en-US/images/phoneScreenshots/counter_styled.png" width="200" /> | <img src="example/screenshots/en-US/images/phoneScreenshots/custom_frame.png" width="200" /> |
| Phone | Custom layout | Styled | Custom frame |

Gradle plugin + Compose library for generating framed Play Store / App Store screenshots from Compose UI under Robolectric.

## What it gives you

- A `screenshot()` function that renders your composable inside a device frame at the exact pixel size each store expects
- Localized titles via `R.string.*` resource IDs — one PNG per locale, automatic
- A `screenshots` source set (auto-created by the plugin) so screenshot code lives separately from regular tests
- Per-form-factor `@Preview` annotations for pixel-identical IDE previews
- A `ScreenshotPreview` composable so previews match generated PNGs

## Quick start

### Option A — Maven Central (released versions)

The plugin and library are published to Maven Central, so no authentication is required. In your `settings.gradle.kts`, make sure `mavenCentral()` is listed for both plugins and dependencies:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("io.github.lucianosantosdev.storescreenshots") version "1.0.0"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

The plugin resolves its runtime library (`io.github.lucianosantosdev:storescreenshots-library`) automatically — you only declare the plugin id.

### Option B — Composite build (local development)

```kotlin
pluginManagement {
    includeBuild("path/to/store-screenshots/plugin")
}
includeBuild("path/to/store-screenshots")
```

`mobile/build.gradle.kts`:

```kotlin
plugins {
    id("io.github.lucianosantosdev.storescreenshots")
}
```

`mobile/src/screenshots/kotlin/.../MyScreenshots.kt`:

```kotlin
class MyScreenshots : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun settings() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_settings_title,
        descriptionRes = R.string.screenshot_settings_desc,
    ) { MySettingsScreen() }
}
```

`StoreScreenshotsTest` bundles `@RunWith(RobolectricTestRunner)`, `@GraphicsMode(NATIVE)`, `@Config(sdk = [35], application = StoreScreenshotsStubApplication)`, and a `@get:Rule ScreenshotRule`.

Run with:

```
./gradlew :mobile:storeScreenshots
```

## screenshot() parameters

Everything is driven by the `screenshot()` function — no annotations needed beyond `@Test`:

```kotlin
@Test fun home() = screenshot(
    locales = listOf("en-US", "pt-BR"),   // one PNG per locale (default: en-US)
    titleRes = R.string.screenshot_title,  // resolved per locale automatically
    descriptionRes = R.string.screenshot_desc,
    backgroundColor = Color(0xFF1F2937),  // banner background
    contentColor = Color.White,           // banner text color
    fileName = "01_home",                 // PNG name (default: test method name)
    style = ScreenshotStyle(...),         // advanced styling (optional)
) { HomeScreen() }
```

| Parameter | Purpose |
| --- | --- |
| `locales` | BCP-47 tags — one PNG per entry. Default `listOf("en-US")`. |
| `title` / `description` | Raw string headline/sub-headline. |
| `titleRes` / `descriptionRes` | `R.string.*` resource ID, resolved per locale. Takes precedence over raw strings. |
| `backgroundColor` | Banner background color. Default dark gray. |
| `contentColor` | Banner text color. Default white. |
| `fileName` | Output PNG name (without the locale path). Defaults to the test method name. `.png` is appended if omitted. Useful to control ordering in the store (e.g. `"01_home"`). |
| `style` | `ScreenshotStyle` for advanced customization. |

## Custom output directory

By default, screenshots land in `build/outputs/store-screenshots/{locale}/images/{subdir}/`. To write into Fastlane's layout:

```kotlin
storeScreenshots {
    destDir = rootProject.layout.projectDirectory.dir("fastlane/metadata/android")
    // → fastlane/metadata/android/{locale}/images/phoneScreenshots/*.png
}
```

## Supported form factors

| FormFactor | Output size | Subdir |
| --- | --- | --- |
| `Phone` | 1080 x 1920 | `phoneScreenshots` |
| `Wear` | 384 x 384 | `wearScreenshots` |
| `Tablet7` | 1200 x 1920 | `sevenInchScreenshots` |
| `Tablet10` | 1600 x 2560 | `tenInchScreenshots` |
| `AppleIPhone67` | 1290 x 2796 | `iphone67` |
| `GooglePlayFeatureGraphic` | 1024 x 500 | `featureGraphic` |

### Feature graphic

`GooglePlayFeatureGraphic` is the landscape 1024 x 500 banner shown at the top of a Play Store
listing. Unlike the other form factors it has no built-in title/description frame — a feature
graphic is promotional art, so you compose it yourself with `customScreenshot { … }` and drop a
`DeviceMockup(formFactor = …)` for each device your app supports. Each mockup is laid out at its
native size and uniformly scaled to fit, so the bezel, status bar, and content keep real-device
proportions no matter how small you draw it. Size it with the `Modifier` you pass — bound the
height to line several devices up in a `Row`. Calling `screenshot(…)` on this form factor
throws — use `customScreenshot`.

For watches, `DeviceMockup(FormFactor.Wear)` draws a round case; use `WatchMockup(shape = …)`
directly to choose `WatchShape.Round` or `WatchShape.Square`.

```kotlin
class FeatureGraphic : StoreScreenshotsTest(FormFactor.GooglePlayFeatureGraphic) {
    @Test fun banner() = customScreenshot {
        Row(verticalAlignment = Alignment.Bottom) {
            DeviceMockup(FormFactor.Tablet10, Modifier.fillMaxHeight(0.85f)) { HomeScreen() }
            DeviceMockup(FormFactor.Phone, Modifier.fillMaxHeight(0.7f)) { HomeScreen() }
            WatchMockup(WatchShape.Round, Modifier.fillMaxHeight(0.35f)) { WatchScreen() }
            WatchMockup(WatchShape.Square, Modifier.fillMaxHeight(0.35f)) { WatchScreen() }
        }
    }
}
```

## Styling

Pass a `ScreenshotStyle` to `StoreScreenshotsTest` (class-level default) or to `screenshot(style = …)` (per-method override):

```kotlin
@Test fun home() = screenshot(
    titleRes = R.string.screenshot_title,
    style = ScreenshotStyle(
        mockupPosition = MockupPosition.Middle,
        mockupOffset = DpOffset(x = 24.dp, y = 32.dp),
        showStatusBar = true,
        statusBarClock = "9:41",
        titleFontFamily = FontFamily.Serif,
        descriptionFontFamily = FontFamily.Monospace,
        background = { MyGradientBackground() },
        title = { text -> MyStyledTitle(text) },
        description = { text -> MyStyledDescription(text) },
    ),
) { HomeScreen() }
```

| Option | Purpose |
| --- | --- |
| `mockupPosition` | Device frame at `Top`, `Middle`, or `Bottom` (default). |
| `mockupOffset` | `DpOffset(x, y)` — X crops off the canvas edge, Y reserves layout space so text doesn't overlap. |
| `showStatusBar` | Show/hide the status bar on phone, tablet, and Apple mockups. Default `true`. |
| `statusBarClock` | Clock text in the status bar. Default `"12:00"`. |
| `titleFontFamily` / `descriptionFontFamily` | Font for the default title/description Text composables. |
| `background` | Composable rendered behind everything. Overrides `backgroundColor`. |
| `mockupFrame` | Composable that replaces the built-in device bezel entirely. Receives app content as a parameter. |
| `title` / `description` | Full composable control over banner text rendering. |

## Custom device frame

Replace the built-in device bezel with your own composable via `mockupFrame`. Title/description/positioning still work — only the device shape changes:

<img src="example/screenshots/en-US/images/phoneScreenshots/custom_frame.png" width="280" />

```kotlin
@Test fun home() = screenshot(
    title = "My app",
    style = ScreenshotStyle(
        mockupFrame = { content ->
            // Your custom bezel — draw anything, put content() inside
            Box(
                Modifier.fillMaxWidth().aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(32.dp))
                    .border(4.dp, Color.Cyan, RoundedCornerShape(32.dp))
                    .background(Color.Black)
            ) { content() }
        },
    ),
) { HomeScreen() }
```

## Fully custom layout

For complete control, use `customScreenshot {}`. You get a `ScreenshotScope` with a `Mockup {}` composable that renders just the device bezel — everything else is yours:

<img src="example/screenshots/en-US/images/phoneScreenshots/custom_layout.png" width="280" />

```kotlin
// Shared layout in src/main/ (used by both test and preview)
@Composable
fun CustomScreenshotLayout(mockup: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {
        // Layer 1: diagonal purple stripe
        Box(Modifier.fillMaxWidth().height(300.dp).offset(x = (-40).dp, y = 520.dp)
            .rotate(-12f).background(Brush.horizontalGradient(
                listOf(Color(0xFF6D28D9), Color(0xFF7C3AED), Color(0xFF8B5CF6)))))
        // Layer 2: rotated device mockup
        Box(Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.85f)
            .offset(x = 60.dp, y = 80.dp).rotate(12f)) { mockup() }
        // Layer 3: text on top
        Column(Modifier.fillMaxSize().padding(28.dp).padding(top = 36.dp, bottom = 12.dp)) {
            Text("STORE\nSCREENSHOTS", color = Color.White, fontSize = 42.sp,
                fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
            Text("TEMPLATE", Modifier.background(Color.White).padding(horizontal = 12.dp),
                color = Color(0xFF8B5CF6), fontSize = 42.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("BY STORE-SCREENSHOTS", Modifier.align(Alignment.CenterHorizontally),
                color = Color.White, fontSize = 13.sp, letterSpacing = 3.sp)
        }
    }
}

// Test — src/screenshots/
@Test fun custom_layout() = customScreenshot {
    CustomScreenshotLayout { Mockup { CounterScreen(count = 42) } }
}

// Preview — src/debug/
@PhoneScreenshotPreview
@Composable
fun CustomLayoutPreview() = CustomScreenshotLayout {
    DeviceMockup(FormFactor.Phone) { CounterScreen(count = 42) }
}
```

## Previews

Per-form-factor `@Preview` annotations bundle the right `widthDp`/`heightDp`:

```kotlin
// src/debug/ — Android Studio renders these
@PhoneScreenshotPreview
@Composable
fun HomePreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Welcome home",
    description = "Sign in to get started",
) { HomeScreen() }
```

| Annotation | Dimensions |
| --- | --- |
| `@PhoneScreenshotPreview` | 411 x 914 dp |
| `@WearScreenshotPreview` | 227 x 227 dp |
| `@Tablet7ScreenshotPreview` | 600 x 960 dp |
| `@Tablet10ScreenshotPreview` | 800 x 1280 dp |
| `@AppleIPhone67ScreenshotPreview` | 430 x 932 dp |
| `@AllScreenshotPreviews` | All five at once |

Previews go in `src/debug/` (Studio only renders debug variant). Tests go in `src/screenshots/`. Shared composables go in `src/main/`.

## Examples

The [`example/`](example) module generates screenshots from the same `CounterScreen` composable. Source: `example/src/screenshots/kotlin/`.

### Mockup positions

`MockupPosition.Top` / `Middle` / `Bottom` — controls where the device sits relative to the title and description.

#### Phone

| Top | Middle | Bottom (default) |
| :---: | :---: | :---: |
| <img src="example/screenshots/en-US/images/phoneScreenshots/phone_top.png" width="200" /> | <img src="example/screenshots/en-US/images/phoneScreenshots/phone_middle.png" width="200" /> | <img src="example/screenshots/en-US/images/phoneScreenshots/phone_bottom.png" width="200" /> |

#### 7-inch tablet

| Top | Middle | Bottom (default) |
| :---: | :---: | :---: |
| <img src="example/screenshots/en-US/images/sevenInchScreenshots/tablet7_top.png" width="220" /> | <img src="example/screenshots/en-US/images/sevenInchScreenshots/tablet7_middle.png" width="220" /> | <img src="example/screenshots/en-US/images/sevenInchScreenshots/tablet7_bottom.png" width="220" /> |

#### 10-inch tablet

| Top | Middle | Bottom (default) |
| :---: | :---: | :---: |
| <img src="example/screenshots/en-US/images/tenInchScreenshots/tablet10_top.png" width="240" /> | <img src="example/screenshots/en-US/images/tenInchScreenshots/tablet10_middle.png" width="240" /> | <img src="example/screenshots/en-US/images/tenInchScreenshots/tablet10_bottom.png" width="240" /> |

#### Apple iPhone 6.7"

| Top | Middle | Bottom (default) |
| :---: | :---: | :---: |
| <img src="example/screenshots/en-US/iphone67/apple_top.png" width="180" /> | <img src="example/screenshots/en-US/iphone67/apple_middle.png" width="180" /> | <img src="example/screenshots/en-US/iphone67/apple_bottom.png" width="180" /> |

```kotlin
// One line to change the position:
@Test fun home_top() = screenshot(
    title = "Mockup at top",
    description = "Title and description below the device",
    style = ScreenshotStyle(mockupPosition = MockupPosition.Top),
) { HomeScreen() }
```

### Wear OS

<img src="example/screenshots/en-US/images/wearScreenshots/counter.png" width="240" />

Wear screenshots have no title/description banner, so `mockupPosition` doesn't apply.

### Custom style (composable background + title + description)

<img src="example/screenshots/en-US/images/phoneScreenshots/counter_styled.png" width="280" />

```kotlin
@Test fun counter_styled() = screenshot(
    titleRes = R.string.screenshot_styled_title,
    descriptionRes = R.string.screenshot_styled_desc,
    style = ScreenshotStyle(
        mockupPosition = MockupPosition.Middle,
        mockupOffset = DpOffset(x = 100.dp, y = 32.dp),
        mockupRotation = -5f,
        background = { MarketingBackground() },
        title = { text -> StyledTitle(text) },
        description = { text -> StyledDescription(text) },
    ),
) { CounterScreen(count = 42) }
```

## Releasing

Push a tag matching `v[0-9]+.[0-9]+.[0-9]+` (e.g. `v0.2.0`). The release workflow publishes to Maven Central and GitHub Packages.

## License

MIT — see [LICENSE](LICENSE).
