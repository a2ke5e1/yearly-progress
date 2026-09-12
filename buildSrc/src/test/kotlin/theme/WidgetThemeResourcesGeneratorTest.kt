package theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetThemeResourcesGeneratorTest {

    @Test
    fun `all 57 themes are seeded`() {
        assertEquals(54, ThemeSeeds.all.size)
        assertEquals("DEFAULT", ThemeSeeds.all.first().key)
        assertEquals("HONEYDEW", ThemeSeeds.all.last().key)
        ThemeSeeds.all.forEach { seed ->
            assertTrue("seed rgba", seed.seedArgb != 0u)
        }
    }

    @Test
    fun `48 scheme roles are emitted`() {
        assertEquals(48, ColorSchemeRoles.all.size)
        assertEquals("primary", ColorSchemeRoles.all.first())
        assertEquals("onTertiaryFixedVariant", ColorSchemeRoles.all.last())
        assertTrue(ColorSchemeRoles.all.toSet().size == ColorSchemeRoles.all.size)
    }

    @Test
    fun `naming helpers`() {
        assertEquals("widgetOnPrimaryContainer", roleAttr("onPrimaryContainer"))
        assertEquals("on_primary_container", roleSnake("onPrimaryContainer"))
        assertEquals("widget_ocean_on_primary_container", roleColorName("OCEAN", "onPrimaryContainer"))
    }

    @Test
    fun `legacy aliases - five theme attrs and six colors`() {
        assertEquals(5, LegacyAliases.all.size)
        assertEquals(6, LegacyAliases.allColors.size)
        assertEquals("surfaceContainer", LegacyAliases.all.first().role)
        assertEquals("outlineVariant", LegacyAliases.allColors.last().role)
        assertFalse(LegacyAliases.all.map { it.resourceName }.contains("progress_bar_background"))
        assertTrue(LegacyAliases.allColors.map { it.resourceName }.contains("progress_bar_background"))
    }

    @Test
    fun `widget theme ids preserve renderer ordering quirks`() {
        val ids = WidgetThemeResourcesGenerator.widgetThemeIds()

        // Linear: signature themes then Dynamic LAST.
        val linear = ids.substringAfter("linearProgressIds", "missing").substringBefore(")")
        assertTrue(linear.contains("widgetProgressBarDefault"))
        assertTrue(linear.contains("widgetProgressBarHoneydew"))
        assertTrue(linear.contains("widgetProgressBarDynamic"))
        assertTrue(linear.indexOf("widgetProgressBarDynamic") > linear.indexOf("widgetProgressBarHoneydew"))

        // Circular: Default, Green, Dynamic, then the rest.
        val circular = ids.substringAfter("val circularProgressIds", "missing").substringBefore("    )")
        assertTrue(circular.contains("circularProgressBarDefault"))
        assertTrue(circular.contains("circularProgressBarGreen"))
        assertTrue(circular.contains("circularProgressBarDynamic"))
        assertTrue(circular.indexOf("circularProgressBarDynamic") < circular.indexOf("circularProgressBarOcean"))

        // DYNAMIC quirks: linear -> *BarDynamic, shape container -> widgetContainerDefault, circular -> circularBarDynamic.
        assertTrue(ids.contains("WidgetTheme.DYNAMIC -> R.id.widgetProgressBarDynamic"))
        assertTrue(ids.contains("WidgetTheme.DYNAMIC -> R.id.widgetContainerDefault"))
        assertTrue(ids.contains("WidgetTheme.DYNAMIC -> R.id.circularProgressBarDynamic"))
    }

    @Test
    fun `widget theme enum has 57 entries with seedArgb and title-case style refs`() {
        val kt = WidgetThemeResourcesGenerator.widgetThemeEnum()
        assertEquals(55, kt.lineSequence().count { it.matches(Regex("^    [A-Z_]+\\(.*")) })
        assertTrue(kt.contains("""DEFAULT(R.style.WidgetTheme_Default,"""))
        assertTrue(kt.contains("""DYNAMIC(R.style.WidgetTheme_Dynamic,"""))
        assertTrue(kt.contains("0xFF276688L.toInt()"))
        assertFalse(kt.contains("WidgetTheme_DEFAULT"))
    }

    @Test
    fun `themes xml has one style per theme with all role attrs`() {
        val xml = WidgetThemeResourcesGenerator.widgetThemesXml()
        assertEquals(54, xml.lineSequence().count { it.contains("<style name=\"WidgetTheme.") })
        assertTrue(xml.contains("""<style name="WidgetTheme.Default""""))
        assertTrue(xml.contains("""<item name="widgetPrimaryFixedDim">@color/widget_default_primary_fixed_dim</item>"""))
        assertFalse(xml.contains("WidgetTheme.DEFAULT"))
    }

    @Test
    fun `colors xml emits roles and legacy aliases for every theme`() {
        val light = WidgetThemeResourcesGenerator.colorsXml(isDark = false)
        val dark = WidgetThemeResourcesGenerator.colorsXml(isDark = true)
        assertEquals(54 * 54, light.lineSequence().count { it.contains("<color name=\"widget_") })
        assertTrue(light.contains("""<color name="widget_ocean_primary_color">@color/widget_ocean_primary</color>"""))
        assertTrue(light.contains("""<color name="widget_ocean_progress_bar_background">@color/widget_ocean_outline_variant</color>"""))
        assertTrue(dark.contains("<color name=\"widget_"))
        assertEquals(54 * 54, dark.lineSequence().count { it.contains("<color name=\"widget_") })
    }

    @Test
    fun `dynamic palette maps every role to a system color for light and dark`() {
        assertEquals(48, DynamicSystemPalette.byRole.size)
        assertEquals(setOf("error", "onError", "errorContainer", "onErrorContainer"), DynamicSystemPalette.errorHex.keys)
        val systemColorRegex = Regex(
            "@android:color/system_(neutral[12]|accent[123])_(0|10|50|100|200|300|400|500|600|700|800|900|1000)"
        )
        DynamicSystemPalette.byRole.forEach { (role, scheme) ->
            if (role in DynamicSystemPalette.errorHex) return@forEach
            val light = scheme.first.resource
            val dark = scheme.second.resource
            assertTrue("light $role", light.startsWith("@android:color/system_"))
            assertTrue("dark $role", dark.startsWith("@android:color/system_"))
            assertTrue(light, light.matches(systemColorRegex))
            assertTrue(dark, dark.matches(systemColorRegex))
        }
        assertEquals("@android:color/system_accent1_600", DynamicSystemPalette.byRole["primary"]!!.first.resource)
        assertEquals("@android:color/system_accent1_900", DynamicSystemPalette.byRole["onPrimary"]!!.second.resource)
        // Error roles fall back to fixed M3 hex because system_error_* requires API 35+.
        val lightXml = WidgetThemeResourcesGenerator.dynamicColorsXml(isDark = false)
        val darkXml = WidgetThemeResourcesGenerator.dynamicColorsXml(isDark = true)
        assertFalse(lightXml.contains("system_error"))
        assertFalse(darkXml.contains("system_error"))
        assertEquals("#B3261E", Regex("<color name=\"widget_dynamic_error\">([^<]+)</color>").find(lightXml)!!.groupValues[1])
        assertEquals("#F2B8B5", Regex("<color name=\"widget_dynamic_error\">([^<]+)</color>").find(darkXml)!!.groupValues[1])
    }

    @Test
    fun `widget colors kt exposes legacy plus every scheme role`() {
        val kt = WidgetThemeResourcesGenerator.widgetColorsKt()
        listOf("backgroundColor", "backgroundLowColor", "primaryColor", "secondaryColor", "accentColor")
            .forEach { field -> assertTrue(kt.contains("    val ${field}: Int,")) }
        ColorSchemeRoles.all.forEach { role ->
            val prop = role.replaceFirstChar { it.lowercase() }
            assertTrue("missing $prop field", kt.contains("    val $prop: Int,"))
            assertTrue(
                "missing $prop read",
                kt.contains("R.styleable.WidgetTheme_${roleAttr(role)}") &&
                    kt.contains("R.color.${roleColorName("DEFAULT", role)}")
            )
        }
        assertTrue(kt.contains("fun fromTheme(context: Context, theme: WidgetTheme): WidgetColors"))
        assertEquals(
            "legacy defaults only (no stale scheme-role defaults on legacy attrs)",
            kt.lines().count { it.contains("widget_default_") },
            53,
        )
        assertTrue(kt.count { it == ',' } > 0)
    }

    @Test
    fun `per-theme layouts mirror the view-id maps`() {
        val linear = WidgetThemeResourcesGenerator.standaloneLinearLayout()
        val clover = WidgetThemeResourcesGenerator.standaloneCloverLayout()
        val pill = WidgetThemeResourcesGenerator.standalonePillLayout()
        val circular = WidgetThemeResourcesGenerator.circularContainerLayout()

        assertEquals(55, linear.lineSequence().count { it.contains("@+id/widgetProgressBar") })
        assertEquals(55, clover.lineSequence().count { it.contains("@+id/widgetContainer") })
        assertEquals(55, pill.lineSequence().count { it.contains("@+id/widgetContainer") })
        assertEquals(55, circular.lineSequence().count { it.contains("@+id/circularProgressBar") })

        // Linear/container end with Dynamic; circular starts Default, Green, Dynamic.
        assertTrue(linear.indexOf("widgetProgressBarDynamic") > linear.indexOf("widgetProgressBarHoneydew"))
        assertTrue(clover.indexOf("widgetContainerDynamic") > clover.indexOf("widgetContainerHoneydew"))
        assertTrue(circular.indexOf("circularProgressBarDefault") < circular.indexOf("circularProgressBarDynamic"))
        assertTrue(circular.indexOf("circularProgressBarGreen") < circular.indexOf("circularProgressBarDynamic"))
        assertTrue(circular.indexOf("circularProgressBarDynamic") < circular.indexOf("circularProgressBarOcean"))
    }

    @Test
    fun `base-adjacent files are generated separately and hand files stay untouched`() {
        val output = WidgetThemeResourcesGenerator.Output(
            resDir = java.io.File("/tmp/opencode/gen-test/res"),
            modelDir = java.io.File("/tmp/opencode/gen-test/model"),
            utilDir = java.io.File("/tmp/opencode/gen-test/util"),
        )
        val names = WidgetThemeResourcesGenerator.generate(output).map { it.path.path }
        val baseOnly = setOf(
            "values/colors.xml", "values/themes.xml", "values/styles.xml", "values/attrs.xml",
            "values-v31/themes.xml", "values-v31/styles.xml", "values-night-v31/themes.xml",
        )
        baseOnly.forEach { base ->
            assertFalse("$base must stay manual", names.any { it.endsWith(base) })
        }
        listOf(
            "values/widget_colors.xml",
            "values-night/widget_colors.xml",
            "values/widget_themes.xml",
            "values/widget_styles.xml",
            "values/widget_attrs.xml",
            "values-v31/widget_colors.xml",
            "values-night-v31/widget_colors.xml",
            "values-v31/widget_themes.xml",
            "values-v31/widget_styles.xml",
            "layout/standalone_progress_bar_containers.xml",
            "layout/standalone_clover_progress_bar_containers.xml",
            "layout/standalone_pill_progress_bar_containers.xml",
            "layout/circular_progress_bars_container.xml",
        ).forEach { generated ->
            assertTrue("$generated must be generated", names.any { it.endsWith(generated) })
        }
    }

    @Test
    fun `generation is deterministic`() {
        val output = WidgetThemeResourcesGenerator.Output(
            resDir = java.io.File("/tmp/opencode/gen-test/res"),
            modelDir = java.io.File("/tmp/opencode/gen-test/model"),
            utilDir = java.io.File("/tmp/opencode/gen-test/util"),
        )
        val first = WidgetThemeResourcesGenerator.generate(output)
        val second = WidgetThemeResourcesGenerator.generate(output)
        assertEquals(first.size, second.size)
        first.zip(second).forEach { (a, b) ->
            assertEquals(a.path, b.path)
            assertEquals(a.content, b.content)
        }
    }
}