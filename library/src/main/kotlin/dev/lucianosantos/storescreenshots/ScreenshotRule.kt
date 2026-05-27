package dev.lucianosantos.storescreenshots

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.lucianosantos.storescreenshots.frames.AppleFrame
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
        style: ScreenshotStyle = this.style,
        content: @Composable () -> Unit,
    ) {
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

                        val outputFile = outputPath(locale)
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

    @Composable
    private fun renderFrame(
        title: String,
        description: String,
        backgroundColor: Color,
        contentColor: Color,
        style: ScreenshotStyle,
        content: @Composable () -> Unit,
    ) {
        when (formFactor) {
            FormFactor.Phone -> PhoneFrame(title, description, backgroundColor, contentColor, style, content)
            FormFactor.Wear -> WearFrame(backgroundColor, content)
            FormFactor.Tablet7,
            FormFactor.Tablet10 -> TabletFrame(title, description, backgroundColor, contentColor, style, content = content)
            FormFactor.AppleIPhone67 -> AppleFrame(title, description, backgroundColor, contentColor, style, content)
        }
    }

    private fun outputPath(locale: String): File {
        val subPath = if (formFactor.useImagesSubdir) {
            "$locale/images/${formFactor.subdir}"
        } else {
            "$locale/${formFactor.subdir}"
        }
        val dir = File(outputRootDir, subPath).apply { mkdirs() }
        return File(dir, "$testMethodName.png")
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
