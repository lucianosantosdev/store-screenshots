package dev.lucianosantos.storescreenshots

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.AppleFrame
import dev.lucianosantos.storescreenshots.frames.FramedLayout
import dev.lucianosantos.storescreenshots.frames.PhoneFrame
import dev.lucianosantos.storescreenshots.frames.TabletFrame
import dev.lucianosantos.storescreenshots.frames.WearFrame
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * JUnit rule that renders Compose UI under Robolectric, wraps it in a form-factor frame,
 * and writes a PNG per locale.
 *
 * Everything is driven by `capture()` — no `@Screenshot` annotation needed:
 *
 * ```kotlin
 * class HomeScreenshots : StoreScreenshotsTest(FormFactor.Phone) {
 *     @Test fun home() = screenshot(
 *         locales = listOf("en-US", "pt-BR"),
 *         titleRes = R.string.screenshot_home_title,
 *         descriptionRes = R.string.screenshot_home_desc,
 *     ) { HomeScreen() }
 * }
 * ```
 */
class ScreenshotRule(
    val formFactor: FormFactor,
    val style: ScreenshotStyle = ScreenshotStyle(),
    private val outputRootDir: File = defaultOutputRoot(),
) : TestRule {

    private lateinit var testMethodName: String
    private lateinit var junitDescription: Description

    override fun apply(base: Statement, description: Description): Statement {
        testMethodName = description.methodName ?: error("Test has no method name")
        junitDescription = description
        return base
    }

    /**
     * Render [content] inside the form-factor frame and capture a PNG for each locale.
     *
     * @param locales BCP-47 tags — one PNG per entry. Default is just en-US.
     * @param title Raw string headline. Ignored when [titleRes] is set.
     * @param description Raw string sub-headline. Ignored when [descriptionRes] is set.
     * @param titleRes String resource ID (`R.string.xxx`). Resolved per locale automatically.
     * @param descriptionRes Same as [titleRes] for the description.
     * @param backgroundColor Banner background color.
     * @param contentColor Foreground text color.
     * @param fileName Output PNG name (without locale path). Defaults to the test method name.
     *   A `.png` extension is added automatically if omitted.
     * @param style Override the class-level style for just this screenshot.
     */
    fun screenshot(
        locales: List<String> = listOf("en-US"),
        title: String = "",
        description: String = "",
        titleRes: Int = 0,
        descriptionRes: Int = 0,
        backgroundColor: Color = Color(0xFF1F2937),
        contentColor: Color = Color.White,
        fileName: String? = null,
        style: ScreenshotStyle = this.style,
        content: @Composable () -> Unit,
    ) {
        check(formFactor != FormFactor.GooglePlayFeatureGraphic) {
            "FormFactor.GooglePlayFeatureGraphic has no built-in title/description frame. " +
                "A feature graphic is a custom promotional banner — build it with customScreenshot { } " +
                "and compose DeviceMockup(formFactor = …) for each device you want to show."
        }
        for (locale in locales) {
            RuntimeEnvironment.setQualifiers(formFactor.qualifiers)
            RuntimeEnvironment.setQualifiers("+${locale.toAndroidResourceQualifier()}")

            val context: Context = RuntimeEnvironment.getApplication()
            val resolvedTitle = if (titleRes != 0) context.getString(titleRes) else title
            val resolvedDescription = if (descriptionRes != 0) context.getString(descriptionRes) else description

            val composeRule = createComposeRule()
            composeRule.apply(
                object : Statement() {
                    override fun evaluate() {
                        composeRule.setContent {
                            renderFrame(resolvedTitle, resolvedDescription, backgroundColor, contentColor, style, content)
                        }
                        composeRule.waitForIdle()

                        val outputFile = outputPath(locale, fileName ?: testMethodName)
                        composeRule.onRoot().captureRoboImage(
                            filePath = outputFile.absolutePath,
                            roborazziOptions = RoborazziOptions(),
                        )
                    }
                },
                junitDescription,
            ).evaluate()
        }
    }

    /**
     * Fully custom layout — you control everything. The [ScreenshotScope] provides a `Mockup`
     * composable for the device bezel; you place it wherever you want.
     *
     * ```kotlin
     * @Test fun custom() = screenshot {
     *     Box(Modifier.fillMaxSize().background(Color.Red)) {
     *         Text("Custom title", Modifier.align(Alignment.TopCenter))
     *         Mockup(Modifier.align(Alignment.Center)) { HomeScreen() }
     *     }
     * }
     * ```
     */
    fun customScreenshot(
        locales: List<String> = listOf("en-US"),
        fileName: String? = null,
        content: @Composable ScreenshotScope.() -> Unit,
    ) {
        val scope = ScreenshotScope(formFactor, style)
        for (locale in locales) {
            RuntimeEnvironment.setQualifiers(formFactor.qualifiers)
            RuntimeEnvironment.setQualifiers("+${locale.toAndroidResourceQualifier()}")

            val composeRule = createComposeRule()
            composeRule.apply(
                object : Statement() {
                    override fun evaluate() {
                        composeRule.setContent { scope.content() }
                        composeRule.waitForIdle()
                        composeRule.onRoot().captureRoboImage(
                            filePath = outputPath(locale, fileName ?: testMethodName).absolutePath,
                            roborazziOptions = RoborazziOptions(),
                        )
                    }
                },
                junitDescription,
            ).evaluate()
        }
    }

    /**
     * Renders one continuous layout [panels] screenshots wide, then slices it into [panels]
     * equal-width PNGs that sit side by side in the store and read as one design — text, devices,
     * or a background can flow across the seams between screenshots.
     *
     * The [content] runs in a [ScreenshotScope] (same `Mockup { }` as [customScreenshot]) over a
     * canvas that is `panels ×` the form factor's normal width and its normal height. Lay out a
     * single wide composition; panel 1 is the leftmost slice. Each slice is written next to the
     * others as `{name}_01.png`, `{name}_02.png`, … so they sort into order in the store.
     *
     * ```kotlin
     * @Test fun story() = splitScreenshot(panels = 3) {
     *     Row(Modifier.fillMaxSize().background(brand)) {
     *         repeat(3) { Box(Modifier.weight(1f)) { /* one panel */ } }
     *     }
     *     // …or place Mockup { } / Text freely so they straddle the seams.
     * }
     * ```
     *
     * @param panels How many screenshots to split the canvas into (must be >= 2).
     * @param fileName Base name for the slices (without the `_NN` suffix or `.png`). Defaults to
     *   the test method name.
     */
    fun splitScreenshot(
        panels: Int,
        locales: List<String> = listOf("en-US"),
        fileName: String? = null,
        content: @Composable ScreenshotScope.() -> Unit,
    ) {
        require(panels >= 2) {
            "splitScreenshot needs panels >= 2 (got $panels); use customScreenshot for a single image."
        }
        val scope = ScreenshotScope(formFactor, style)
        val wideQualifiers = widenQualifiers(formFactor.qualifiers, panels)
        for (locale in locales) {
            RuntimeEnvironment.setQualifiers(wideQualifiers)
            RuntimeEnvironment.setQualifiers("+${locale.toAndroidResourceQualifier()}")

            val composeRule = createComposeRule()
            composeRule.apply(
                object : Statement() {
                    override fun evaluate() {
                        composeRule.setContent { scope.content() }
                        composeRule.waitForIdle()
                        // Capture the whole wide canvas with Roborazzi (the path that works under
                        // Robolectric — captureToImage()/PixelCopy does not), then slice it.
                        val temp = File.createTempFile("storescreenshots-split", ".png")
                        try {
                            composeRule.onRoot().captureRoboImage(
                                filePath = temp.absolutePath,
                                roborazziOptions = RoborazziOptions(),
                            )
                            val full = BitmapFactory.decodeFile(temp.absolutePath)
                                ?: error("Could not decode the captured split canvas at ${temp.absolutePath}")
                            writeSlices(full, panels, locale, fileName ?: testMethodName)
                        } finally {
                            temp.delete()
                        }
                    }
                },
                junitDescription,
            ).evaluate()
        }
    }

    /** Cuts [full] into [panels] equal-width PNGs, written as `{base}_01.png`, `{base}_02.png`, … */
    private fun writeSlices(full: Bitmap, panels: Int, locale: String, base: String) {
        val panelWidth = full.width / panels
        for (i in 0 until panels) {
            val x = i * panelWidth
            // The last panel takes any remainder so no column of pixels is dropped.
            val width = if (i == panels - 1) full.width - x else panelWidth
            val slice = Bitmap.createBitmap(full, x, 0, width, full.height)
            val name = "${base}_${(i + 1).toString().padStart(2, '0')}"
            outputPath(locale, name).outputStream().use { slice.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Composable
    private fun renderFrame(
        title: String,
        description: String,
        backgroundColor: Color,
        contentColor: Color,
        style: ScreenshotStyle,
        content: @Composable () -> Unit,
    ) {
        // Apply the style-level glass to the screen drawn inside the bezel, once for every frame.
        val content = glassWrap(style.screenGlass, content)
        val customFrame = style.mockupFrame
        if (customFrame != null) {
            // User-defined frame replaces the built-in bezel but keeps FramedLayout
            // for title/description/positioning.
            FramedLayout(
                title = title,
                description = description,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                style = style,
                horizontalPadding = 28.dp,
                verticalPadding = 48.dp,
                mockup = { externalModifier ->
                    Box(externalModifier) { customFrame(content) }
                }
            )
        } else {
            when (formFactor) {
                FormFactor.Phone -> PhoneFrame(title, description, backgroundColor, contentColor, style, content)
                FormFactor.Wear -> WearFrame(backgroundColor, content)
                FormFactor.Tablet7,
                FormFactor.Tablet10 -> TabletFrame(title, description, backgroundColor, contentColor, style, content = content)
                FormFactor.AppleIPhone67 -> AppleFrame(title, description, backgroundColor, contentColor, style, content)
                // Guarded against in screenshot(); a feature graphic is composed via customScreenshot.
                FormFactor.GooglePlayFeatureGraphic -> error("unreachable")
            }
        }
    }

    private fun outputPath(locale: String, name: String): File {
        val subPath = if (formFactor.useImagesSubdir) {
            "$locale/images/${formFactor.subdir}"
        } else {
            "$locale/${formFactor.subdir}"
        }
        val dir = File(outputRootDir, subPath).apply { mkdirs() }
        val pngName = if (name.endsWith(".png", ignoreCase = true)) name else "$name.png"
        return File(dir, pngName)
    }

    companion object {
        internal fun defaultOutputRoot(): File {
            System.getProperty("storeScreenshots.outputRoot")?.let { return File(it) }
            return File(System.getProperty("user.dir"))
        }
    }
}

private fun String.toAndroidResourceQualifier(): String {
    val parts = split("-")
    return if (parts.size == 2) "${parts[0]}-r${parts[1]}" else this
}

/**
 * Multiplies the `wNNNdp` width in a Robolectric [qualifiers] string by [panels], leaving the
 * height and density buckets untouched — so the canvas is [panels] screenshots wide at the form
 * factor's normal height and pixel density. e.g. `w411dp-h914dp-xxhdpi` × 3 → `w1233dp-h914dp-xxhdpi`.
 */
private fun widenQualifiers(qualifiers: String, panels: Int): String =
    Regex("""w(\d+)dp""").replace(qualifiers) { match ->
        "w${match.groupValues[1].toInt() * panels}dp"
    }
