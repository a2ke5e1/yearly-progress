package theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import java.io.File
import theme.ColorSchemeRoles.all as roles
import theme.DynamicSystemPalette.byRole as dynamicByRole
import theme.LegacyAliases.all as legacyAliases
import theme.LegacyAliases.allColors as legacyColorAliases

data class GeneratedFile(val path: File, val content: String)

/**
 * Emits every file that is derived from [ThemeSeeds] + [ColorSchemeRoles] on every build.
 *
 * Generated files live in dedicated `widget_*` files so the hand-authored base resources
 * (`values/colors.xml`, `values/themes.xml`, `values/styles.xml`, `values/attrs.xml`,
 * the `values-v31` base files and `values-night-v31/themes.xml`)
 * stay fully editable and survive regeneration:
 *  - `values/widget_colors.xml`, `values-night/widget_colors.xml` (light/dark seeded palettes + legacy aliases)
 *  - `values/widget_themes.xml`, `values/widget_styles.xml` (seeded widget themes + progress styles)
 *  - `values/widget_attrs.xml` (WidgetTheme styleable incl. one attr per scheme role)
 *  - `values-v31/widget_colors.xml` + `values-night-v31/widget_colors.xml` (DYNAMIC Material You, 31-33 coarse palette)
 *  - `values-v34/widget_colors.xml` + `values-night-v34/widget_colors.xml` (DYNAMIC Material You, 34+ refined palette)
 *  - `values-v31/widget_themes.xml`, `values-v31/widget_styles.xml` (DYNAMIC theme + progress style)
 *  - `layout/standalone_progress_bar_containers.xml`, `standalone_clover_*`, `standalone_pill_*`,
 *    `circular_progress_bars_container.xml` (one view per theme; ids tracked by `WidgetThemeIds`)
 *  - `WidgetTheme.kt` (enum with seedArgb), `WidgetThemeIds.kt` (view-id maps), `WidgetColors.kt`
 *    (the full Material 3 role set + legacy colors read from the `WidgetTheme` styleable)
 */
object WidgetThemeResourcesGenerator {

    data class Output(
        val resDir: File,
        val modelDir: File,
        val utilDir: File,
    )

    private const val GENERATED_HEADER =
        "<!-- GENERATED FILE - DO NOT EDIT. Regenerate with the app `generateWidgetThemeResources` task. -->"
    private const val KOTLIN_HEADER =
        "// GENERATED FILE - DO NOT EDIT. Regenerate with the app `generateWidgetThemeResources` task."

    fun generate(output: Output): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()
        files += GeneratedFile(File(output.resDir, "values/widget_colors.xml"), colorsXml(isDark = false))
        files += GeneratedFile(File(output.resDir, "values-night/widget_colors.xml"), colorsXml(isDark = true))
        files += GeneratedFile(File(output.resDir, "values/widget_themes.xml"), widgetThemesXml())
        files += GeneratedFile(File(output.resDir, "values/widget_styles.xml"), widgetStylesXml())
        files += GeneratedFile(File(output.resDir, "values/widget_attrs.xml"), widgetAttrsXml())
        files += GeneratedFile(File(output.resDir, "values-v31/widget_colors.xml"), dynamicColorsXml(isDark = false))
        files += GeneratedFile(File(output.resDir, "values-night-v31/widget_colors.xml"), dynamicColorsXml(isDark = true))
        files += GeneratedFile(File(output.resDir, "values-v34/widget_colors.xml"), dynamicColorsXml(isDark = false, api = DynamicApi.V34))
        files += GeneratedFile(File(output.resDir, "values-night-v34/widget_colors.xml"), dynamicColorsXml(isDark = true, api = DynamicApi.V34))
        files += GeneratedFile(File(output.resDir, "values-v31/widget_themes.xml"), dynamicThemesXml())
        files += GeneratedFile(File(output.resDir, "values-v31/widget_styles.xml"), dynamicStylesXml())
        files += GeneratedFile(File(output.resDir, "layout/standalone_progress_bar_containers.xml"), standaloneLinearLayout())
        files += GeneratedFile(File(output.resDir, "layout/standalone_clover_progress_bar_containers.xml"), standaloneCloverLayout())
        files += GeneratedFile(File(output.resDir, "layout/standalone_pill_progress_bar_containers.xml"), standalonePillLayout())
        files += GeneratedFile(File(output.resDir, "layout/circular_progress_bars_container.xml"), circularContainerLayout())
        files += GeneratedFile(File(output.modelDir, "WidgetTheme.kt"), widgetThemeEnum())
        files += GeneratedFile(File(output.utilDir, "WidgetThemeIds.kt"), widgetThemeIds())
        files += GeneratedFile(File(output.modelDir, "WidgetColors.kt"), widgetColorsKt())
        return files
    }

    // region scheme computation

    fun schemeFor(seed: UInt, isDark: Boolean): ColorScheme =
        dynamicColorScheme(
            primary = Color(seed.toInt()),
            isDark = isDark,
        )

    internal fun roleColor(scheme: ColorScheme, role: String): Color = when (role) {
        "primary" -> scheme.primary
        "onPrimary" -> scheme.onPrimary
        "primaryContainer" -> scheme.primaryContainer
        "onPrimaryContainer" -> scheme.onPrimaryContainer
        "secondary" -> scheme.secondary
        "onSecondary" -> scheme.onSecondary
        "secondaryContainer" -> scheme.secondaryContainer
        "onSecondaryContainer" -> scheme.onSecondaryContainer
        "tertiary" -> scheme.tertiary
        "onTertiary" -> scheme.onTertiary
        "tertiaryContainer" -> scheme.tertiaryContainer
        "onTertiaryContainer" -> scheme.onTertiaryContainer
        "error" -> scheme.error
        "onError" -> scheme.onError
        "errorContainer" -> scheme.errorContainer
        "onErrorContainer" -> scheme.onErrorContainer
        "background" -> scheme.background
        "onBackground" -> scheme.onBackground
        "surface" -> scheme.surface
        "onSurface" -> scheme.onSurface
        "surfaceVariant" -> scheme.surfaceVariant
        "onSurfaceVariant" -> scheme.onSurfaceVariant
        "outline" -> scheme.outline
        "outlineVariant" -> scheme.outlineVariant
        "scrim" -> scheme.scrim
        "surfaceTint" -> scheme.surfaceTint
        "inverseSurface" -> scheme.inverseSurface
        "inverseOnSurface" -> scheme.inverseOnSurface
        "inversePrimary" -> scheme.inversePrimary
        "surfaceDim" -> scheme.surfaceDim
        "surfaceBright" -> scheme.surfaceBright
        "surfaceContainerLowest" -> scheme.surfaceContainerLowest
        "surfaceContainerLow" -> scheme.surfaceContainerLow
        "surfaceContainer" -> scheme.surfaceContainer
        "surfaceContainerHigh" -> scheme.surfaceContainerHigh
        "surfaceContainerHighest" -> scheme.surfaceContainerHighest
        "primaryFixed" -> scheme.primaryFixed
        "primaryFixedDim" -> scheme.primaryFixedDim
        "onPrimaryFixed" -> scheme.onPrimaryFixed
        "onPrimaryFixedVariant" -> scheme.onPrimaryFixedVariant
        "secondaryFixed" -> scheme.secondaryFixed
        "secondaryFixedDim" -> scheme.secondaryFixedDim
        "onSecondaryFixed" -> scheme.onSecondaryFixed
        "onSecondaryFixedVariant" -> scheme.onSecondaryFixedVariant
        "tertiaryFixed" -> scheme.tertiaryFixed
        "tertiaryFixedDim" -> scheme.tertiaryFixedDim
        "onTertiaryFixed" -> scheme.onTertiaryFixed
        "onTertiaryFixedVariant" -> scheme.onTertiaryFixedVariant
        else -> error("Unknown scheme role: $role")
    }

    internal fun colorHex(color: Color): String {
        val a = (color.alpha * 255 + 0.5).toInt().coerceIn(0, 255)
        val r = (color.red * 255 + 0.5).toInt().coerceIn(0, 255)
        val g = (color.green * 255 + 0.5).toInt().coerceIn(0, 255)
        val b = (color.blue * 255 + 0.5).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X%02X", a, r, g, b)
    }

    // endregion

    // region colors.xml (seeded light/dark)

    fun colorsXml(isDark: Boolean): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine("<resources>")
        sb.appendLine("    <!-- ${if (isDark) "Dark" else "Light"} mode palettes, one seed per theme (Material 3 Expressive) -->")
        ThemeSeeds.all.forEach { seed ->
            sb.appendLine("    <!-- ${seed.displayName} -->")
            val scheme = schemeFor(seed.seedArgb, isDark)
            roles.forEach { role ->
                sb.appendLine(
                    "    <color name=\"${roleColorName(seed.key, role)}\">${colorHex(roleColor(scheme, role))}</color>"
                )
            }
            legacyColorAliases.forEach { alias ->
                sb.appendLine(
                    "    <color name=\"widget_${seed.key.lowercase()}_${alias.resourceName}\">@color/${roleColorName(seed.key, alias.role)}</color>"
                )
            }
        }
        sb.appendLine("</resources>")
        return sb.toString()
    }

    // endregion

    // region themes.xml

    fun widgetThemesXml(): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine("<resources>")
        ThemeSeeds.all.forEach { seed ->
            val key = seed.key.lowercase()
            sb.appendLine("    <style name=\"WidgetTheme.${idToken(seed.key)}\" parent=\"@style/Theme.YearlyProgress\">")
            legacyAliases.forEach { alias ->
                sb.appendLine(
                    "        <item name=\"${legacyAttr(alias)}\">@color/widget_${key}_${alias.resourceName}</item>"
                )
            }
            roles.forEach { role ->
                sb.appendLine(
                    "        <item name=\"${roleAttr(role)}\">@color/${roleColorName(seed.key, role)}</item>"
                )
            }
            sb.appendLine("    </style>")
        }
        sb.appendLine("</resources>")
        return sb.toString()
    }

    // endregion

    // region styles.xml

    fun widgetStylesXml(): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine("<resources>")
        sb.appendLine()
        sb.appendLine("    <!-- Progress Bar Themes (one per theme, mirror the WidgetProgressTheme.* styles) -->")
        ThemeSeeds.all.forEach { seed ->
            val key = seed.key.lowercase()
            sb.appendLine("    <style name=\"WidgetProgressTheme.${idToken(seed.key)}\" parent=\"WidgetProgressBarBase\">")
            sb.appendLine("        <item name=\"android:progressTint\">@color/widget_${key}_primary_color</item>")
            sb.appendLine("        <item name=\"android:progressBackgroundTint\">@color/widget_${key}_progress_bar_background</item>")
            sb.appendLine("    </style>")
            sb.appendLine()
        }
        sb.appendLine("</resources>")
        return sb.toString()
    }

    // endregion

    // region attrs.xml

    fun widgetAttrsXml(): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine("<resources>")
        sb.appendLine("    <declare-styleable name=\"WidgetTheme\">")
        legacyAliases.forEach { alias ->
            sb.appendLine("        <attr name=\"${legacyAttr(alias)}\" format=\"color\" />")
        }
        roles.forEach { role ->
            sb.appendLine("        <attr name=\"${roleAttr(role)}\" format=\"color\" />")
        }
        sb.appendLine("    </declare-styleable>")
        sb.appendLine("</resources>")
        return sb.toString()
    }

    // endregion

    // region values-v31/values-v34 (DYNAMIC / Material You)

    /** Which Android dynamic color generation to emit: 31-33 coarse `system_*` or 34+ refined `system_*_light/_dark`. */
    enum class DynamicApi { V31, V34 }

    fun dynamicColorsXml(isDark: Boolean, api: DynamicApi = DynamicApi.V31): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        val resourcesTag = if (api == DynamicApi.V34) {
            "<resources xmlns:tools=\"http://schemas.android.com/tools\" tools:targetApi=\"34\">"
        } else {
            "<resources>"
        }
        sb.appendLine(resourcesTag)
        // Existing hand-tuned DYNAMIC legacy colors (kept verbatim for 31-33).
        val legacyLight = if (api == DynamicApi.V34) {
            listOf(
                "widget_dynamic_background_color" to "@android:color/system_surface_container_light",
                "widget_dynamic_background_low_color" to "@android:color/system_surface_container_low_light",
                "widget_dynamic_primary_color" to "@android:color/system_primary_light",
                "widget_dynamic_secondary_color" to "@android:color/system_on_surface_variant_light",
                "widget_dynamic_accent_color" to "@android:color/system_tertiary_light",
                "widget_dynamic_progress_bar_background" to "@android:color/system_outline_variant_light",
            )
        } else {
            listOf(
                "widget_dynamic_background_color" to "@android:color/system_neutral2_50",
                "widget_dynamic_background_low_color" to "@android:color/system_neutral2_100",
                "widget_dynamic_primary_color" to "@android:color/system_accent1_600",
                "widget_dynamic_secondary_color" to "@android:color/system_neutral1_400",
                "widget_dynamic_accent_color" to "@android:color/system_accent3_600",
                "widget_dynamic_progress_bar_background" to "@android:color/system_neutral2_200",
            )
        }
        val legacyDark = if (api == DynamicApi.V34) {
            listOf(
                "widget_dynamic_background_color" to "@android:color/system_surface_container_dark",
                "widget_dynamic_background_low_color" to "@android:color/system_surface_container_low_dark",
                "widget_dynamic_primary_color" to "@android:color/system_primary_dark",
                "widget_dynamic_secondary_color" to "@android:color/system_on_surface_variant_dark",
                "widget_dynamic_accent_color" to "@android:color/system_tertiary_dark",
                "widget_dynamic_progress_bar_background" to "@android:color/system_outline_variant_dark",
            )
        } else {
            listOf(
                "widget_dynamic_background_color" to "@android:color/system_neutral2_800",
                "widget_dynamic_background_low_color" to "@android:color/system_neutral2_700",
                "widget_dynamic_primary_color" to "@android:color/system_accent1_100",
                "widget_dynamic_secondary_color" to "@android:color/system_neutral1_400",
                "widget_dynamic_accent_color" to "@android:color/system_accent3_200",
                "widget_dynamic_progress_bar_background" to "@android:color/system_neutral1_500",
            )
        }
        (if (isDark) legacyDark else legacyLight).forEach { (name, res) ->
            sb.appendLine("    <color name=\"$name\">$res</color>")
        }
        sb.appendLine()
        sb.appendLine("    <!-- ${if (api == DynamicApi.V34) "Refined Material You role set (Android 14+)" else "Full Material 3 role set following the wallpaper (Material You)"} -->")
        roles.forEach { role ->
            if (api == DynamicApi.V34) {
                val (light, dark) = DynamicSystemPalette.api34.getValue(role)
                val res = if (isDark) dark else light
                sb.appendLine("    <color name=\"${roleColorName("DYNAMIC", role)}\">$res</color>")
            } else {
                val (light, dark) = dynamicByRole.getValue(role)
                val res = if (isDark) dark.resource else light.resource
                if (light.palette == "error") {
                    // The system_error_* palette only ships with API 35+; fall back to fixed
                    // Material 3 error colors so DYNAMIC themes resolve on API 31-34 devices.
                    val hex = DynamicSystemPalette.errorHex.getValue(role)
                    sb.appendLine("    <color name=\"${roleColorName("DYNAMIC", role)}\">${if (isDark) hex.second else hex.first}</color>")
                } else {
                    sb.appendLine("    <color name=\"${roleColorName("DYNAMIC", role)}\">$res</color>")
                }
            }
        }
        sb.appendLine()
        sb.appendLine("</resources>")
        return sb.toString()
    }

    fun dynamicThemesXml(): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine("<resources>")
        sb.appendLine("    <style name=\"WidgetTheme.Dynamic\" parent=\"WidgetTheme.Default\">")
        legacyAliases.forEach { alias ->
            sb.appendLine(
                "        <item name=\"${legacyAttr(alias)}\">@color/widget_dynamic_${alias.resourceName}</item>"
            )
        }
        roles.forEach { role ->
            sb.appendLine(
                "        <item name=\"${roleAttr(role)}\">@color/${roleColorName("DYNAMIC", role)}</item>"
            )
        }
        sb.appendLine("    </style>")
        sb.appendLine("</resources>")
        return sb.toString()
    }

    fun dynamicStylesXml(): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine("<resources>")
        sb.appendLine("    <style name=\"WidgetProgressTheme.Dynamic\" parent=\"WidgetProgressBarBase\">")
        sb.appendLine("        <item name=\"android:progressTint\">@color/widget_dynamic_primary_color</item>")
        sb.appendLine("        <item name=\"android:progressBackgroundTint\">@color/widget_dynamic_progress_bar_background</item>")
        sb.appendLine("    </style>")
        sb.appendLine("</resources>")
        return sb.toString()
    }

    // endregion

    // region per-theme layouts

    /** The view order must mirror [WidgetThemeIds]: linear/container lists end with Dynamic. */
    private fun layoutOrder(): List<String> = ThemeSeeds.all.map { it.key } + "DYNAMIC"

    /** circular order: Default, Green, Dynamic, then the remaining seeds (mirrors `circularProgressIds`). */
    private fun circularLayoutOrder(): List<String> = listOf("DEFAULT", "GREEN", "DYNAMIC") +
        ThemeSeeds.all.map { it.key }.filterNot { it == "DEFAULT" || it == "GREEN" }

    private fun layoutHeader(openTag: String): String {
        val sb = StringBuilder()
        sb.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.appendLine(GENERATED_HEADER)
        sb.appendLine(openTag)
        return sb.toString()
    }

    fun standaloneLinearLayout(): String {
        val sb = StringBuilder(layoutHeader(
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"wrap_content\"\n" +
                "    android:orientation=\"vertical\">\n"
        ))
        layoutOrder().forEach { key ->
            sb.appendLine()
            sb.appendLine("    <ProgressBar")
            sb.appendLine("        android:id=\"@+id/widgetProgressBar${idToken(key)}\"")
            sb.appendLine("        style=\"@style/WidgetProgressBarBase\"")
            sb.appendLine("        android:indeterminate=\"false\"")
            sb.appendLine("        android:max=\"100\"")
            sb.appendLine("        android:progress=\"20\"")
            sb.appendLine("        android:theme=\"@style/WidgetProgressTheme.${idToken(key)}\" />")
        }
        sb.appendLine()
        sb.appendLine("</LinearLayout>")
        return sb.toString()
    }

    fun standaloneCloverLayout(): String {
        val sb = StringBuilder(layoutHeader(
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:orientation=\"vertical\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\">\n"
        ))
        layoutOrder().forEach { key ->
            sb.appendLine()
            sb.appendLine("    <ImageView")
            sb.appendLine("        android:id=\"@+id/widgetContainer${idToken(key)}\"")
            sb.appendLine("        android:layout_width=\"match_parent\"")
            sb.appendLine("        android:layout_height=\"match_parent\"")
            sb.appendLine("        android:src=\"@drawable/background_clover_50\"")
            sb.appendLine("        android:theme=\"@style/WidgetTheme.${idToken(key)}\" />")
        }
        sb.appendLine()
        sb.appendLine("</LinearLayout>")
        return sb.toString()
    }

    fun standalonePillLayout(): String {
        val sb = StringBuilder(layoutHeader(
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:orientation=\"vertical\">\n"
        ))
        layoutOrder().forEach { key ->
            sb.appendLine()
            sb.appendLine("    <ImageView")
            sb.appendLine("        android:id=\"@+id/widgetContainer${idToken(key)}\"")
            sb.appendLine("        android:layout_width=\"match_parent\"")
            sb.appendLine("        android:layout_height=\"match_parent\"")
            sb.appendLine("        android:layout_centerInParent=\"true\"")
            sb.appendLine("        android:src=\"@drawable/background_pill_00\"")
            sb.appendLine("        android:theme=\"@style/WidgetTheme.${idToken(key)}\" />")
        }
        sb.appendLine()
        sb.appendLine("</LinearLayout>")
        return sb.toString()
    }

    fun circularContainerLayout(): String {
        val sb = StringBuilder(layoutHeader(
            "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\">\n"
        ))
        sb.appendLine()
        sb.appendLine("    <ImageView")
        sb.appendLine("        android:id=\"@+id/circularProgressBackground\"")
        sb.appendLine("        android:layout_width=\"match_parent\"")
        sb.appendLine("        android:layout_height=\"match_parent\"")
        sb.appendLine("        android:padding=\"5dp\"")
        sb.appendLine("        android:src=\"@drawable/circular_progress_bg\" />")
        circularLayoutOrder().forEach { key ->
            sb.appendLine()
            sb.appendLine("    <ProgressBar")
            sb.appendLine("        android:id=\"@+id/circularProgressBar${idToken(key)}\"")
            sb.appendLine("        style=\"@style/CircularProgressIndicatorBase\"")
            sb.appendLine("        android:layout_width=\"match_parent\"")
            sb.appendLine("        android:layout_height=\"match_parent\"")
            sb.appendLine("        android:indeterminateOnly=\"false\"")
            sb.appendLine("        android:max=\"100\"")
            sb.appendLine("        android:progress=\"50\"")
            sb.appendLine("        android:theme=\"@style/WidgetProgressTheme.${idToken(key)}\"")
            sb.appendLine("        android:visibility=\"gone\" />")
        }
        sb.appendLine()
        sb.appendLine("</RelativeLayout>")
        return sb.toString()
    }

    // endregion

    // region WidgetTheme.kt

    fun widgetThemeEnum(): String {
        val sb = StringBuilder()
        sb.appendLine(KOTLIN_HEADER)
        sb.appendLine("package com.a3.yearlyprogess.feature.widgets.domain.model")
        sb.appendLine()
        sb.appendLine("import androidx.annotation.StyleRes")
        sb.appendLine("import com.a3.yearlyprogess.R")
        sb.appendLine()
        sb.appendLine("enum class WidgetTheme(")
        sb.appendLine("    @param:StyleRes val themeRes: Int,")
        sb.appendLine("    val displayName: String,")
        sb.appendLine("    val seedArgb: Int")
        sb.appendLine(") {")
        ThemeSeeds.all.forEach { seed ->
            val seedHex = "0x" + seed.seedArgb.toString(16).uppercase().padStart(8, '0') + "L.toInt()"
            sb.appendLine(
                "    ${seed.key}(R.style.WidgetTheme_${idToken(seed.key)}, \"${seed.displayName}\", $seedHex),"
            )
        }
        sb.appendLine("    DYNAMIC(R.style.WidgetTheme_Dynamic, \"Dynamic Colors\", 0)")
        sb.appendLine("}")
        return sb.toString()
    }

    // endregion

    // region WidgetColors.kt

    fun widgetColorsKt(): String {
        val sb = StringBuilder()
        sb.appendLine(KOTLIN_HEADER)
        sb.appendLine("package com.a3.yearlyprogess.feature.widgets.domain.model")
        sb.appendLine()
        sb.appendLine("import android.content.Context")
        sb.appendLine("import com.a3.yearlyprogess.R")
        sb.appendLine()
        sb.appendLine("data class WidgetColors(")
        legacyAliases.forEach { alias ->
            sb.appendLine("    val ${legacyField(alias)}: Int,")
        }
        roles.forEach { role ->
            sb.appendLine(
                "    val ${role.replaceFirstChar { it.lowercase() }}: Int,"
            )
        }
        sb.appendLine(") {")
        sb.appendLine("    companion object {")
        sb.appendLine("        fun fromTheme(context: Context, theme: WidgetTheme): WidgetColors {")
        sb.appendLine("            val res = context.resources")
        sb.appendLine("            val ta = context.obtainStyledAttributes(theme.themeRes, R.styleable.WidgetTheme)")
        sb.appendLine("            val colors = WidgetColors(")
        legacyAliases.forEach { alias ->
            val field = legacyField(alias)
            sb.appendLine("                $field = ta.getColor(")
            sb.appendLine(
                "                    R.styleable.WidgetTheme_${legacyAttr(alias)}," +
                    " res.getColor(R.color.widget_default_${alias.resourceName}, null)"
            )
            sb.appendLine("                ),")
        }
        roles.forEach { role ->
            val prop = role.replaceFirstChar { it.lowercase() }
            sb.appendLine("                $prop = ta.getColor(")
            sb.appendLine(
                "                    R.styleable.WidgetTheme_${roleAttr(role)}," +
                    " res.getColor(R.color.${roleColorName("DEFAULT", role)}, null)"
            )
            sb.appendLine("                ),")
        }
        sb.appendLine("            )")
        sb.appendLine("            ta.recycle()")
        sb.appendLine("            return colors")
        sb.appendLine("        }")
        sb.appendLine("    }")
        sb.appendLine("}")
        return sb.toString()
    }

    // endregion

    // region WidgetThemeIds.kt

    private fun id(name: String): String = "R.id.$name"

    /** Enum-style key -> title-case used in view ids, e.g. `"DEFAULT"` -> `"Default"`. */
    private fun idToken(key: String): String = key.lowercase().replaceFirstChar { it.uppercase() }

    fun widgetThemeIds(): String {
        val sb = StringBuilder()
        sb.appendLine(KOTLIN_HEADER)
        sb.appendLine("package com.a3.yearlyprogess.feature.widgets.util")
        sb.appendLine()
        sb.appendLine("import com.a3.yearlyprogess.R")
        sb.appendLine("import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme")
        sb.appendLine()
        sb.appendLine("object WidgetThemeIds {")
        sb.appendLine()
        sb.appendLine("    val linearProgressIds = listOf(")
        ThemeSeeds.all.forEach { seed ->
            sb.appendLine("        ${id("widgetProgressBar${idToken(seed.key)}")},")
        }
        sb.appendLine("        ${id("widgetProgressBarDynamic")},")
        sb.appendLine("    )")
        sb.appendLine()
        sb.appendLine("    val shapeContainerIds = listOf(")
        ThemeSeeds.all.forEach { seed ->
            sb.appendLine("        ${id("widgetContainer${idToken(seed.key)}")},")
        }
        sb.appendLine("        ${id("widgetContainerDynamic")},")
        sb.appendLine("    )")
        sb.appendLine()
        sb.appendLine("    val circularProgressIds = listOf(")
        sb.appendLine("        ${id("circularProgressBarDefault")},")
        sb.appendLine("        ${id("circularProgressBarGreen")},")
        sb.appendLine("        ${id("circularProgressBarDynamic")},")
        ThemeSeeds.all.drop(2).forEach { seed ->
            sb.appendLine("        ${id("circularProgressBar${idToken(seed.key)}")},")
        }
        sb.appendLine("    )")
        sb.appendLine()

        sb.appendLine("    fun linearProgressId(theme: WidgetTheme): Int =")
        sb.appendLine("        when (theme) {")
        ThemeSeeds.all.forEach { seed ->
            sb.appendLine("            WidgetTheme.${seed.key} -> ${id("widgetProgressBar${idToken(seed.key)}")}")
        }
        sb.appendLine("            WidgetTheme.DYNAMIC -> ${id("widgetProgressBarDynamic")}")
        sb.appendLine("        }")
        sb.appendLine()

        sb.appendLine("    fun shapeContainerId(theme: WidgetTheme): Int =")
        sb.appendLine("        when (theme) {")
        ThemeSeeds.all.forEach { seed ->
            sb.appendLine("            WidgetTheme.${seed.key} -> ${id("widgetContainer${idToken(seed.key)}")}")
        }
        sb.appendLine("            WidgetTheme.DYNAMIC -> ${id("widgetContainerDefault")}")
        sb.appendLine("        }")
        sb.appendLine()

        sb.appendLine("    fun circularProgressId(theme: WidgetTheme): Int =")
        sb.appendLine("        when (theme) {")
        sb.appendLine("            WidgetTheme.DEFAULT -> ${id("circularProgressBarDefault")}")
        sb.appendLine("            WidgetTheme.GREEN -> ${id("circularProgressBarGreen")}")
        sb.appendLine("            WidgetTheme.DYNAMIC -> ${id("circularProgressBarDynamic")}")
        ThemeSeeds.all.drop(2).forEach { seed ->
            sb.appendLine("            WidgetTheme.${seed.key} -> ${id("circularProgressBar${idToken(seed.key)}")}")
        }
        sb.appendLine("        }")
        sb.appendLine("}")
        return sb.toString()
    }

    // endregion

    private fun legacyAttr(alias: LegacyAliases.Alias): String = when (alias.resourceName) {
        "background_color" -> "widgetBackgroundColor"
        "background_low_color" -> "widgetBackgroundLowColor"
        "primary_color" -> "widgetPrimaryColor"
        "secondary_color" -> "widgetSecondaryColor"
        "accent_color" -> "widgetAccentColor"
        else -> error("Unknown legacy alias: ${alias.resourceName}")
    }

    /** Alias resource name (snake case) -> Kotlin field name, e.g. `background_low_color` -> `backgroundLowColor`. */
    private fun legacyField(alias: LegacyAliases.Alias): String =
        alias.resourceName
            .split('_')
            .mapIndexed { index, part -> if (index == 0) part else part.replaceFirstChar { it.uppercase() } }
            .joinToString("")
}