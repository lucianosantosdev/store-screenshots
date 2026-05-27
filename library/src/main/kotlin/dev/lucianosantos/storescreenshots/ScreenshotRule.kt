package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import android.content.Context
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
 * and writes a PNG per locale into the Fastlane metadata layout.
 *
 * Usage:
 *
 * ```kotlin
 * @RunWith(RobolectricTestRunner::class)
 * @Config(sdk = [35], application = StubApplication::class)
 * class MyScreenshots {
 *     @get:Rule val screenshot = ScreenshotRule(FormFactor.Phone)
 *
 *     @Test
 *     @Screenshot(locales = ["en-US", "pt-BR"], title = "Hello", description = "World")
 *     fun home() = screenshot.capture { MyHomeScreen() }
 * }
 * ```
 *
 * Each `@Test` annotated with [Screenshot] is rendered once per locale.
 * The qualifier from [FormFactor] is applied to Robolectric at the start of each iteration,
 * so the underlying device config matches the target screenshot size.
 *
 * @param formFactor Determines the frame, output size, and Fastlane subdirectory.
 * @param outputRootDir Override the output root. Defaults to the system property
 *   `storeScreenshots.outputRoot` set by the Gradle plugin, or the working directory.
 */
class ScreenshotRule(
    val formFactor: FormFactor,
    val style: ScreenshotStyle = ScreenshotStyle(),
    private val outputRootDir: File = defaultOutputRoot(),
) : TestRule {

    private lateinit var composeRule: ComposeContentTestRule
    private lateinit var testMethodName: String
    private var currentLocale: String = "en-US"
    private var currentBackground: Color = Screenshot.DEFAULT_BACKGROUND.toComposeColor()
    private var currentContentColor: Color = Color.White

    // Set by capture() for the current invocation; read by renderFrame().
    private var currentTitle: String = ""
    private var currentDescription: String = ""

    override fun apply(base: Statement, description: Description): Statement {
        val annotation = description.getAnnotation(Screenshot::class.java)
            ?: error(
                "Missing @Screenshot annotation on ${description.className}#${description.methodName}. " +
                    "Either add @Screenshot or remove ScreenshotRule from this class."
            )
        testMethodName = description.methodName ?: error("Test has no method name")

        return object : Statement() {
            override fun evaluate() {
                for (locale in annotation.locales) {
                    currentLocale = locale
                    currentBackground = annotation.backgroundColor.toComposeColor()
                    currentContentColor = annotation.contentColor.toComposeColor()

                    RuntimeEnvironment.setQualifiers(formFactor.qualifiers)
                    RuntimeEnvironment.setQualifiers("+${locale.toAndroidResourceQualifier()}")

                    val freshComposeRule = createComposeRule()
                    composeRule = freshComposeRule
                    freshComposeRule.apply(
                        object : Statement() {
                            override fun evaluate() {
                                base.evaluate()
                            }
                        },
                        description
                    ).evaluate()
                }
            }
        }
    }

    /**
     * Render [content] inside the form-factor frame and capture a PNG for the current locale.
     *
     * @param title Raw string headline. Ignored when [titleRes] is set.
     * @param description Raw string sub-headline. Ignored when [descriptionRes] is set.
     * @param titleRes String resource ID (`R.string.xxx`). Resolved per locale automatically.
     * @param descriptionRes Same as [titleRes] for the description.
     * @param style Override the class-level style for just this screenshot.
     */
    fun capture(
        title: String = "",
        description: String = "",
        titleRes: Int = 0,
        descriptionRes: Int = 0,
        style: ScreenshotStyle = this.style,
        content: @Composable () -> Unit,
    ) {
        require(::composeRule.isInitialized) {
            "capture() called outside a test body. Ensure ScreenshotRule is wired as @get:Rule."
        }
        // Resolve title/description from resource IDs (already locale-qualified by setQualifiers)
        // or fall back to raw strings.
        val context: Context = RuntimeEnvironment.getApplication()
        currentTitle = if (titleRes != 0) context.getString(titleRes) else title
        currentDescription = if (descriptionRes != 0) context.getString(descriptionRes) else description

        composeRule.setContent {
            renderFrame(style, content)
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = outputPath().absolutePath,
            roborazziOptions = RoborazziOptions(),
        )
    }

    @Composable
    private fun renderFrame(style: ScreenshotStyle, content: @Composable () -> Unit) {
        when (formFactor) {
            FormFactor.Phone -> PhoneFrame(
                title = currentTitle,
                description = currentDescription,
                backgroundColor = currentBackground,
                contentColor = currentContentColor,
                style = style,
                content = content
            )
            FormFactor.Wear -> WearFrame(
                backgroundColor = currentBackground,
                content = content
            )
            FormFactor.Tablet7,
            FormFactor.Tablet10 -> TabletFrame(
                title = currentTitle,
                description = currentDescription,
                backgroundColor = currentBackground,
                contentColor = currentContentColor,
                style = style,
                content = content
            )
            FormFactor.AppleIPhone67 -> AppleFrame(
                title = currentTitle,
                description = currentDescription,
                backgroundColor = currentBackground,
                contentColor = currentContentColor,
                style = style,
                content = content
            )
        }
    }

    private fun outputPath(): File {
        val subPath = if (formFactor.useImagesSubdir) {
            "$currentLocale/images/${formFactor.subdir}"
        } else {
            "$currentLocale/${formFactor.subdir}"
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

private fun Long.toComposeColor(): Color {
    val a = ((this shr 24) and 0xFF).toInt()
    val r = ((this shr 16) and 0xFF).toInt()
    val g = ((this shr 8) and 0xFF).toInt()
    val b = (this and 0xFF).toInt()
    return Color(red = r, green = g, blue = b, alpha = a)
}
