package dev.lucianosantos.storescreenshots

/**
 * Where the device mockup sits within the canvas. Title/description fill the remaining space.
 *
 * - [Top]: device at the top, title + description stacked below it.
 * - [Middle]: title at top, device centered vertically, description at bottom.
 * - [Bottom]: title + description at the top, device pushed to the bottom (default; matches
 *   the most common Play Store screenshot layout).
 */
enum class MockupPosition { Top, Middle, Bottom }
