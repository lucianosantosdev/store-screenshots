// AGP 9 ships built-in Kotlin backed by KGP 2.2.10. This project pins a newer Kotlin
// (see gradle/libs.versions.toml), so force the matching KGP onto the build classpath — the
// compose compiler plugin is versioned to the same Kotlin and must line up with the compiler.
buildscript {
    dependencies {
        // Keep in sync with `kotlin` in gradle/libs.versions.toml.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.roborazzi) apply false
}

// group + version come from gradle.properties; overridden by -Pversion=… in CI.
// Maven Central publishing + signing are configured per-module via the
// com.vanniktech.maven.publish plugin (see library/ and plugin/ build files).
subprojects {
    group = rootProject.group
    version = rootProject.version
}

// The example's README screenshots are versioned as small JPEGs, not the heavy PNGs the screenshot
// run produces (those are git-ignored). This re-encodes every generated PNG to JPEG and deletes the
// PNG. Run after `:example:storeScreenshots`. Defined on the root (plain) build so it has the full
// JDK on its classpath — the Android module's build script doesn't expose `java.desktop`.
tasks.register("compressScreenshots") {
    group = "store-screenshots"
    description = "Re-encodes generated example screenshots from PNG to small JPEG for versioning."
    val screenshotsDir = layout.projectDirectory.dir("example/screenshots")
    doLast {
        System.setProperty("java.awt.headless", "true")
        var converted = 0
        screenshotsDir.asFile.walkTopDown().filter { it.isFile && it.extension == "png" }.forEach { png ->
            val src = javax.imageio.ImageIO.read(png) ?: return@forEach
            val rgb = java.awt.image.BufferedImage(src.width, src.height, java.awt.image.BufferedImage.TYPE_INT_RGB)
            rgb.createGraphics().apply { drawImage(src, 0, 0, java.awt.Color.WHITE, null); dispose() }
            val writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next()
            val params = writer.defaultWriteParam.apply {
                compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
                compressionQuality = 0.82f
            }
            File(png.parentFile, "${png.nameWithoutExtension}.jpg").outputStream().use { out ->
                javax.imageio.ImageIO.createImageOutputStream(out).use { ios ->
                    writer.output = ios
                    writer.write(null, javax.imageio.IIOImage(rgb, null, null), params)
                }
            }
            writer.dispose()
            png.delete()
            converted++
        }
        logger.lifecycle("compressScreenshots: converted $converted PNG(s) to JPEG")
    }
}
