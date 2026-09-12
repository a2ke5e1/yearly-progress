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
 * Order: background -> surfaceContainer, backgroundLow -> surfaceContainerLow,
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

/** role -> (light, dark) system color. Tones follow the Android dynamic color table for 31-33. */
    val byRole: Map<String, Pair<SystemColor, SystemColor>> = mapOf(
        // Primary
        "primary" to (SystemColor("accent1", 600) to SystemColor("accent1", 200)),
        "onPrimary" to (SystemColor("accent1", 0) to SystemColor("accent1", 800)),
        "primaryContainer" to (SystemColor("accent1", 100) to SystemColor("accent1", 700)),
        "onPrimaryContainer" to (SystemColor("accent1", 900) to SystemColor("accent1", 100)),
        // Secondary
        "secondary" to (SystemColor("accent2", 600) to SystemColor("accent2", 200)),
        "onSecondary" to (SystemColor("accent2", 0) to SystemColor("accent2", 800)),
        "secondaryContainer" to (SystemColor("accent2", 100) to SystemColor("accent2", 700)),
        "onSecondaryContainer" to (SystemColor("accent2", 900) to SystemColor("accent2", 100)),
        // Tertiary
        "tertiary" to (SystemColor("accent3", 600) to SystemColor("accent3", 200)),
        "onTertiary" to (SystemColor("accent3", 0) to SystemColor("accent3", 800)),
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
        "surfaceVariant" to (SystemColor("neutral2", 100) to SystemColor("neutral2", 700)),
        "onSurfaceVariant" to (SystemColor("neutral2", 700) to SystemColor("neutral2", 200)),
        // Tonal outline / scrim
        "outline" to (SystemColor("neutral2", 500) to SystemColor("neutral2", 400)),
        "outlineVariant" to (SystemColor("neutral2", 200) to SystemColor("neutral2", 700)),
        "scrim" to (SystemColor("neutral1", 1000) to SystemColor("neutral1", 1000)),
        // Inverse + surface tint
        "surfaceTint" to (SystemColor("accent1", 500) to SystemColor("accent1", 200)),
        "inverseSurface" to (SystemColor("neutral1", 800) to SystemColor("neutral1", 100)),
        "inverseOnSurface" to (SystemColor("neutral1", 50) to SystemColor("neutral1", 800)),
        "inversePrimary" to (SystemColor("accent1", 200) to SystemColor("accent1", 600)),
        // Surface containers + dim/bright
        "surfaceDim" to (SystemColor("neutral2", 400) to SystemColor("neutral2", 700)),
        "surfaceBright" to (SystemColor("neutral2", 0) to SystemColor("neutral2", 500)),
        "surfaceContainerLowest" to (SystemColor("neutral2", 0) to SystemColor("neutral2", 1000)),
        "surfaceContainerLow" to (SystemColor("neutral2", 50) to SystemColor("neutral2", 900)),
        "surfaceContainer" to (SystemColor("neutral2", 100) to SystemColor("neutral2", 800)),
        "surfaceContainerHigh" to (SystemColor("neutral2", 200) to SystemColor("neutral2", 600)),
        "surfaceContainerHighest" to (SystemColor("neutral2", 300) to SystemColor("neutral2", 500)),
        // Fixed roles (the doc table uses the same tone for light and dark)
        "primaryFixed" to (SystemColor("accent1", 100) to SystemColor("accent1", 100)),
        "primaryFixedDim" to (SystemColor("accent1", 200) to SystemColor("accent1", 200)),
        "onPrimaryFixed" to (SystemColor("accent1", 900) to SystemColor("accent1", 900)),
        "onPrimaryFixedVariant" to (SystemColor("accent1", 700) to SystemColor("accent1", 700)),
        "secondaryFixed" to (SystemColor("accent2", 100) to SystemColor("accent2", 100)),
        "secondaryFixedDim" to (SystemColor("accent2", 200) to SystemColor("accent2", 200)),
        "onSecondaryFixed" to (SystemColor("accent2", 900) to SystemColor("accent2", 900)),
        "onSecondaryFixedVariant" to (SystemColor("accent2", 700) to SystemColor("accent2", 700)),
        "tertiaryFixed" to (SystemColor("accent3", 100) to SystemColor("accent3", 100)),
        "tertiaryFixedDim" to (SystemColor("accent3", 200) to SystemColor("accent3", 200)),
        "onTertiaryFixed" to (SystemColor("accent3", 900) to SystemColor("accent3", 900)),
        "onTertiaryFixedVariant" to (SystemColor("accent3", 700) to SystemColor("accent3", 700)),
    ).mapValues { (_, v) -> v }

    /**
     * API 34+ (Android 14+) dynamic system resources. Android 14 exposed the refined Material You
     * palette as public `system_*_light/_dark` colors; values are the `Light/Dark dynamic 34+` cells
     * of the Android dynamic color table. `scrim` has no table entry (kept coarse) and `surfaceTint`
     * mirrors the primary.
     */
    val api34: Map<String, Pair<String, String>> = mapOf(
    "primary" to ("@android:color/system_primary_light" to "@android:color/system_primary_dark"),
    "onPrimary" to ("@android:color/system_on_primary_light" to "@android:color/system_on_primary_dark"),
    "primaryContainer" to ("@android:color/system_primary_container_light" to "@android:color/system_primary_container_dark"),
    "onPrimaryContainer" to ("@android:color/system_on_primary_container_light" to "@android:color/system_on_primary_container_dark"),
    "secondary" to ("@android:color/system_secondary_light" to "@android:color/system_secondary_dark"),
    "onSecondary" to ("@android:color/system_on_secondary_light" to "@android:color/system_on_secondary_dark"),
    "secondaryContainer" to ("@android:color/system_secondary_container_light" to "@android:color/system_secondary_container_dark"),
    "onSecondaryContainer" to ("@android:color/system_on_secondary_container_light" to "@android:color/system_on_secondary_container_dark"),
    "tertiary" to ("@android:color/system_tertiary_light" to "@android:color/system_tertiary_dark"),
    "onTertiary" to ("@android:color/system_on_tertiary_light" to "@android:color/system_on_tertiary_dark"),
    "tertiaryContainer" to ("@android:color/system_tertiary_container_light" to "@android:color/system_tertiary_container_dark"),
    "onTertiaryContainer" to ("@android:color/system_on_tertiary_container_light" to "@android:color/system_on_tertiary_container_dark"),
    "error" to ("@android:color/system_error_light" to "@android:color/system_error_dark"),
    "onError" to ("@android:color/system_on_error_light" to "@android:color/system_on_error_dark"),
    "errorContainer" to ("@android:color/system_error_container_light" to "@android:color/system_error_container_dark"),
    "onErrorContainer" to ("@android:color/system_on_error_container_light" to "@android:color/system_on_error_container_dark"),
    "background" to ("@android:color/system_background_light" to "@android:color/system_background_dark"),
    "onBackground" to ("@android:color/system_on_background_light" to "@android:color/system_on_background_dark"),
    "surface" to ("@android:color/system_surface_light" to "@android:color/system_surface_dark"),
    "onSurface" to ("@android:color/system_on_surface_light" to "@android:color/system_on_surface_dark"),
    "surfaceVariant" to ("@android:color/system_surface_variant_light" to "@android:color/system_surface_variant_dark"),
    "onSurfaceVariant" to ("@android:color/system_on_surface_variant_light" to "@android:color/system_on_surface_variant_dark"),
    "outline" to ("@android:color/system_outline_light" to "@android:color/system_outline_dark"),
    "outlineVariant" to ("@android:color/system_outline_variant_light" to "@android:color/system_outline_variant_dark"),
    "scrim" to ("@android:color/system_neutral1_1000" to "@android:color/system_neutral1_1000"),
    "surfaceTint" to ("@android:color/system_primary_light" to "@android:color/system_primary_dark"),
    "inverseSurface" to ("@android:color/system_surface_dark" to "@android:color/system_surface_light"),
    "inverseOnSurface" to ("@android:color/system_on_surface_dark" to "@android:color/system_on_surface_light"),
    "inversePrimary" to ("@android:color/system_primary_dark" to "@android:color/system_primary_light"),
    "surfaceDim" to ("@android:color/system_surface_dim_light" to "@android:color/system_surface_dim_dark"),
    "surfaceBright" to ("@android:color/system_surface_bright_light" to "@android:color/system_surface_bright_dark"),
    "surfaceContainerLowest" to ("@android:color/system_surface_container_lowest_light" to "@android:color/system_surface_container_lowest_dark"),
    "surfaceContainerLow" to ("@android:color/system_surface_container_low_light" to "@android:color/system_surface_container_low_dark"),
    "surfaceContainer" to ("@android:color/system_surface_container_light" to "@android:color/system_surface_container_dark"),
    "surfaceContainerHigh" to ("@android:color/system_surface_container_high_light" to "@android:color/system_surface_container_high_dark"),
    "surfaceContainerHighest" to ("@android:color/system_surface_container_highest_light" to "@android:color/system_surface_container_highest_dark"),
    "primaryFixed" to ("@android:color/system_primary_fixed" to "@android:color/system_primary_fixed"),
    "primaryFixedDim" to ("@android:color/system_primary_fixed_dim" to "@android:color/system_primary_fixed_dim"),
    "onPrimaryFixed" to ("@android:color/system_on_primary_fixed" to "@android:color/system_on_primary_fixed"),
    "onPrimaryFixedVariant" to ("@android:color/system_on_primary_fixed_variant" to "@android:color/system_on_primary_fixed_variant"),
    "secondaryFixed" to ("@android:color/system_secondary_fixed" to "@android:color/system_secondary_fixed"),
    "secondaryFixedDim" to ("@android:color/system_secondary_fixed_dim" to "@android:color/system_secondary_fixed_dim"),
    "onSecondaryFixed" to ("@android:color/system_on_secondary_fixed" to "@android:color/system_on_secondary_fixed"),
    "onSecondaryFixedVariant" to ("@android:color/system_on_secondary_fixed_variant" to "@android:color/system_on_secondary_fixed_variant"),
    "tertiaryFixed" to ("@android:color/system_tertiary_fixed" to "@android:color/system_tertiary_fixed"),
    "tertiaryFixedDim" to ("@android:color/system_tertiary_fixed_dim" to "@android:color/system_tertiary_fixed_dim"),
    "onTertiaryFixed" to ("@android:color/system_on_tertiary_fixed" to "@android:color/system_on_tertiary_fixed"),
    "onTertiaryFixedVariant" to ("@android:color/system_on_tertiary_fixed_variant" to "@android:color/system_on_tertiary_fixed_variant"),
    )

    /** Fixed Material 3 error colors (light, dark) substituted for the unavailable system_error_* palette. */
    val errorHex: Map<String, Pair<String, String>> = mapOf(
        "error" to ("#B3261E" to "#F2B8B5"),
        "onError" to ("#FFFFFF" to "#601410"),
        "errorContainer" to ("#F9DEDC" to "#8C1D18"),
        "onErrorContainer" to ("#410E0B" to "#F9DEDC"),
    )
}