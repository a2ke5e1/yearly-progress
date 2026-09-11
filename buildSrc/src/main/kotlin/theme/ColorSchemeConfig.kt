package theme

/**
 * Configuration for the Material 3 color scheme roles emitted by the generator.
 *
 * Roles are the `androidx.compose.material3.ColorScheme` properties (verified against
 * material3-desktop 1.12.0-alpha03). Each role is emitted as a `<color>` resource named
 * `widget_<key>_<snake_role>`, a `widget<Role>` styleable attr and a theme `<item>`.
 */
object ColorSchemeRoles {

    /** All scheme roles in canonical order (this is the [androidx.compose.material3.ColorScheme] shape). */
    val all: List<String> = listOf(
        // Primary family
        "primary", "onPrimary", "primaryContainer", "onPrimaryContainer",
        // Secondary family
        "secondary", "onSecondary", "secondaryContainer", "onSecondaryContainer",
        // Tertiary family
        "tertiary", "onTertiary", "tertiaryContainer", "onTertiaryContainer",
        // Error family
        "error", "onError", "errorContainer", "onErrorContainer",
        // Background / surface
        "background", "onBackground",
        "surface", "onSurface", "surfaceVariant", "onSurfaceVariant",
        // Tonal outline / scrim
        "outline", "outlineVariant", "scrim",
        // Inverse + surface tint
        "surfaceTint", "inverseSurface", "inverseOnSurface", "inversePrimary",
        // Surface containers
        "surfaceDim", "surfaceBright",
        "surfaceContainerLowest", "surfaceContainerLow", "surfaceContainer",
        "surfaceContainerHigh", "surfaceContainerHighest",
        // Material 3 Expressive "fixed" roles
        "primaryFixed", "primaryFixedDim", "onPrimaryFixed", "onPrimaryFixedVariant",
        "secondaryFixed", "secondaryFixedDim", "onSecondaryFixed", "onSecondaryFixedVariant",
        "tertiaryFixed", "tertiaryFixedDim", "onTertiaryFixed", "onTertiaryFixedVariant",
    )
}

/** Maps a `ColorScheme` role name to its style attr (e.g. `onPrimaryContainer` -> `widgetOnPrimaryContainer`). */
fun roleAttr(role: String): String = "widget" + role.replaceFirstChar { it.uppercase() }

/** Maps a role name to a resource suffix (e.g. `onPrimaryContainer` -> `on_primary_container`). */
fun roleSnake(role: String): String =
    role.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()

/** Full color resource name for a seeded theme role, e.g. `widget_ocean_on_primary_container`. */
fun roleColorName(seedKey: String, role: String): String =
    "widget_" + seedKey.lowercase() + "_" + roleSnake(role)

/**
 * Legacy widget colors referenced by layouts, drawables and the theme picker.
 * Each legacy color is emitted as an alias to one of the scheme roles so the two can never drift.
 *
 * The first five double as theme attrs (`widgetBackgroundColor`, ...). The progress bar
 * background color is only referenced by the `WidgetProgressTheme` styles
 * (`android:progressBackgroundTint`) and is therefore a color alias alone.
 *
 * Order: background -> surfaceContainerHighest, backgroundLow -> surfaceContainerHigh,
 * primary -> primary, secondary -> onSurfaceVariant, accent -> tertiary,
 * progressBarBackground -> outlineVariant.
 */
object LegacyAliases {

    data class Alias(val resourceName: String, val role: String)

    /** Theme-attr aliases (background..accent). */
    val all: List<Alias> = listOf(
        Alias("background_color", "surfaceContainer"),
        Alias("background_low_color", "surfaceContainerLow"),
        Alias("primary_color", "primary"),
        Alias("secondary_color", "onSurfaceVariant"),
        Alias("accent_color", "tertiary"),
    )

    /** Pure color aliases, including the progress bar track color. */
    val allColors: List<Alias> = all + Alias("progress_bar_background", "outlineVariant")
}

/**
 * DYNAMIC (Material You) theme: full role set mapped to the Android 12+ system palette
 * (`@android:color/system_*`), so it always follows the user's wallpaper.
 *
 * Tones are selected to approximate the material-color-utilities dynamic scheme roles
 * from the coarse system palette (tones 0,10,50,100,200..900,1000). Tunable per taste.
 */
object DynamicSystemPalette {

    data class SystemColor(val palette: String, val tone: Int) {
        val resource: String get() = "@android:color/system_${palette}_$tone"
    }

    /** role -> (light, dark) system color. */
    val byRole: Map<String, Pair<SystemColor, SystemColor>> = mapOf(
        // Primary
        "primary" to (SystemColor("accent1", 600) to SystemColor("accent1", 200)),
        "onPrimary" to (SystemColor("accent1", 100) to SystemColor("accent1", 900)),
        "primaryContainer" to (SystemColor("accent1", 100) to SystemColor("accent1", 700)),
        "onPrimaryContainer" to (SystemColor("accent1", 900) to SystemColor("accent1", 100)),
        // Secondary
        "secondary" to (SystemColor("accent2", 600) to SystemColor("accent2", 200)),
        "onSecondary" to (SystemColor("accent2", 100) to SystemColor("accent2", 900)),
        "secondaryContainer" to (SystemColor("accent2", 100) to SystemColor("accent2", 700)),
        "onSecondaryContainer" to (SystemColor("accent2", 900) to SystemColor("accent2", 100)),
        // Tertiary
        "tertiary" to (SystemColor("accent3", 600) to SystemColor("accent3", 200)),
        "onTertiary" to (SystemColor("accent3", 100) to SystemColor("accent3", 900)),
        "tertiaryContainer" to (SystemColor("accent3", 100) to SystemColor("accent3", 700)),
        "onTertiaryContainer" to (SystemColor("accent3", 900) to SystemColor("accent3", 100)),
        // Error (system_error_* tones only ship with API 35+, so the generator substitutes
        // these fixed Material 3 colors; palette name stays "error" to trigger that branch).
        "error" to (SystemColor("error", 700) to SystemColor("error", 700)),
        "onError" to (SystemColor("error", 100) to SystemColor("error", 900)),
        "errorContainer" to (SystemColor("error", 100) to SystemColor("error", 700)),
        "onErrorContainer" to (SystemColor("error", 900) to SystemColor("error", 100)),
        // Background / surface
        "background" to (SystemColor("neutral1", 50) to SystemColor("neutral1", 800)),
        "onBackground" to (SystemColor("neutral1", 900) to SystemColor("neutral1", 50)),
        "surface" to (SystemColor("neutral2", 50) to SystemColor("neutral2", 800)),
        "onSurface" to (SystemColor("neutral1", 900) to SystemColor("neutral1", 50)),
        "surfaceVariant" to (SystemColor("neutral1", 200) to SystemColor("neutral1", 700)),
        "onSurfaceVariant" to (SystemColor("neutral1", 600) to SystemColor("neutral1", 200)),
        // Tonal outline / scrim
        "outline" to (SystemColor("neutral1", 500) to SystemColor("neutral1", 500)),
        "outlineVariant" to (SystemColor("neutral1", 300) to SystemColor("neutral1", 400)),
        "scrim" to (SystemColor("neutral1", 1000) to SystemColor("neutral1", 1000)),
        // Inverse + surface tint
        "surfaceTint" to (SystemColor("accent1", 500) to SystemColor("accent1", 200)),
        "inverseSurface" to (SystemColor("neutral1", 800) to SystemColor("neutral1", 100)),
        "inverseOnSurface" to (SystemColor("neutral1", 50) to SystemColor("neutral1", 900)),
        "inversePrimary" to (SystemColor("accent1", 200) to SystemColor("accent1", 600)),
        // Surface containers + dim/bright
        "surfaceDim" to (SystemColor("neutral2", 400) to SystemColor("neutral2", 700)),
        "surfaceBright" to (SystemColor("neutral2", 0) to SystemColor("neutral2", 500)),
        "surfaceContainerLowest" to (SystemColor("neutral2", 0) to SystemColor("neutral2", 1000)),
        "surfaceContainerLow" to (SystemColor("neutral2", 50) to SystemColor("neutral2", 900)),
        "surfaceContainer" to (SystemColor("neutral2", 100) to SystemColor("neutral2", 800)),
        "surfaceContainerHigh" to (SystemColor("neutral2", 200) to SystemColor("neutral2", 600)),
        "surfaceContainerHighest" to (SystemColor("neutral2", 300) to SystemColor("neutral2", 500)),
        // Fixed roles
        "primaryFixed" to (SystemColor("accent1", 100) to SystemColor("accent1", 100)),
        "primaryFixedDim" to (SystemColor("accent1", 200) to SystemColor("accent1", 300)),
        "onPrimaryFixed" to (SystemColor("accent1", 900) to SystemColor("accent1", 900)),
        "onPrimaryFixedVariant" to (SystemColor("accent1", 700) to SystemColor("accent1", 700)),
        "secondaryFixed" to (SystemColor("accent2", 100) to SystemColor("accent2", 100)),
        "secondaryFixedDim" to (SystemColor("accent2", 200) to SystemColor("accent2", 300)),
        "onSecondaryFixed" to (SystemColor("accent2", 900) to SystemColor("accent2", 900)),
        "onSecondaryFixedVariant" to (SystemColor("accent2", 700) to SystemColor("accent2", 700)),
        "tertiaryFixed" to (SystemColor("accent3", 100) to SystemColor("accent3", 100)),
        "tertiaryFixedDim" to (SystemColor("accent3", 200) to SystemColor("accent3", 300)),
        "onTertiaryFixed" to (SystemColor("accent3", 900) to SystemColor("accent3", 900)),
        "onTertiaryFixedVariant" to (SystemColor("accent3", 700) to SystemColor("accent3", 700)),
    ).mapValues { (_, v) -> v }

    /** Fixed Material 3 error colors (light, dark) substituted for the unavailable system_error_* palette. */
    val errorHex: Map<String, Pair<String, String>> = mapOf(
        "error" to ("#B3261E" to "#F2B8B5"),
        "onError" to ("#FFFFFF" to "#601410"),
        "errorContainer" to ("#F9DEDC" to "#8C1D18"),
        "onErrorContainer" to ("#410E0B" to "#F9DEDC"),
    )
}