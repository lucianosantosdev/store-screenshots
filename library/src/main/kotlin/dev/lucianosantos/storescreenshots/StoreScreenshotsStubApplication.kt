package dev.lucianosantos.storescreenshots

import android.app.Application

/**
 * Empty [Application] used by [StoreScreenshotsTest] to bypass the consuming app's real
 * Application class (which typically does DI, Firebase, analytics setup that crashes under
 * Robolectric). Subclasses inherit this via `@Config(application = …)` on [StoreScreenshotsTest]
 * — there's no need to declare your own stub.
 */
class StoreScreenshotsStubApplication : Application()
