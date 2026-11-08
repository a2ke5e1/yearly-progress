package theme

/**
 * Single source of truth for the widget themes used by the code generator.
 *
 * Each theme is defined by exactly one seed color. On every build the generator calls
 * [com.materialkolor.dynamicColorScheme] with [PaletteStyle.Expressive] to derive the full
 * Material 3 color scheme (light + dark) and emits:
 *  - `res/values/colors.xml` + `res/values-night/colors.xml` (all scheme roles + legacy aliases)
 *  - `res/values/themes.xml`, `res/values/styles.xml` (+ `values-v31` variants)
 *  - `WidgetTheme.kt` (enum) and `WidgetThemeIds.kt` (widget view-id mappings)
 *
 * Seeds are the themes' original `widget_<key>_primary_color` values (dropping the opaque
 * alpha channel), kept so every theme keeps its identity hue.
 *
 * @param key canonical enum-style key, e.g. `"OCEAN"`. Must match [WidgetTheme] constants
 * @param displayName user-facing name used in the theme picker
 * @param seedArgb opaque ARGB seed color, e.g. `0xFF0284C7u`
 */
data class ThemeSeed(
    val key: String,
    val displayName: String,
    val seedArgb: UInt,
)

object ThemeSeeds {

    /** All seeded themes, in the exact order used by [WidgetTheme]. DYNAMIC is not a seed. */
    val all: List<ThemeSeed> = listOf(
        ThemeSeed("DEFAULT", "Default", 0xFF276688u),
        ThemeSeed("GREEN", "Green", 0xFF3C6A1Fu),
        ThemeSeed("OCEAN", "Ocean Blue", 0xFF0284C7u),
        ThemeSeed("SUNSET", "Sunset Orange", 0xFFEA580Cu),
        ThemeSeed("PURPLE", "Purple Dream", 0xFF9333EAu),
        ThemeSeed("ROSE", "Rose Pink", 0xFFE11D48u),
        ThemeSeed("MINT", "Mint Fresh", 0xFF059669u),
        ThemeSeed("CORAL", "Coral", 0xFFF43F5Eu),
        ThemeSeed("SKY", "Sky Blue", 0xFF0284C7u),
        ThemeSeed("FOREST", "Forest Green", 0xFF15803Du),
        ThemeSeed("AMBER", "Amber Glow", 0xFFD97706u),
        ThemeSeed("TEAL", "Teal Wave", 0xFF0D9488u),
        ThemeSeed("LAVENDER", "Lavender", 0xFF7C3AEDu),
        ThemeSeed("CRIMSON", "Crimson", 0xFFE11D48u),
        ThemeSeed("INDIGO", "Indigo Night", 0xFF4F46E5u),
        ThemeSeed("PEACH", "Peach", 0xFFEA580Cu),
        ThemeSeed("CYAN", "Cyan Electric", 0xFF0891B2u),
        ThemeSeed("MAGENTA", "Magenta", 0xFFC026D3u),
        ThemeSeed("LIME", "Lime", 0xFF65A30Du),
        ThemeSeed("RUBY", "Ruby Red", 0xFFDC2626u),
        ThemeSeed("SLATE", "Slate Gray", 0xFF475569u),
        ThemeSeed("EMERALD", "Emerald", 0xFF059669u),
        ThemeSeed("FUCHSIA", "Fuchsia", 0xFFC026D3u),
        ThemeSeed("YELLOW", "Yellow Sunshine", 0xFFCA8A04u),
        ThemeSeed("VIOLET", "Violet", 0xFF7C3AEDu),
        ThemeSeed("STEEL", "Steel Blue", 0xFF1D4ED8u),
        ThemeSeed("BRONZE", "Bronze", 0xFFC2782Eu),
        ThemeSeed("AQUA", "Aqua", 0xFF0D9488u),
        ThemeSeed("CHERRY", "Cherry Blossom", 0xFFDB2777u),
        ThemeSeed("NAVY", "Navy", 0xFF1E40AFu),
        ThemeSeed("SAGE", "Sage Green", 0xFF16A34Au),
        ThemeSeed("BURGUNDY", "Burgundy", 0xFFBE123Cu),
        ThemeSeed("CHARCOAL", "Charcoal", 0xFF4B5563u),
        ThemeSeed("TURQUOISE", "Turquoise", 0xFF0F766Eu),
        ThemeSeed("MAUVE", "Mauve", 0xFF7C3AEDu),
        ThemeSeed("GOLD", "Gold", 0xFFD97706u),
        ThemeSeed("SEAFOAM", "Seafoam", 0xFF059669u),
        ThemeSeed("RASPBERRY", "Raspberry", 0xFFBE185Du),
        ThemeSeed("MIDNIGHT", "Midnight", 0xFF4338CAu),
        ThemeSeed("OLIVE", "Olive", 0xFF4D7C0Fu),
        ThemeSeed("SALMON", "Salmon", 0xFFDC2626u),
        ThemeSeed("PERIWINKLE", "Periwinkle", 0xFF6366F1u),
        ThemeSeed("AUTUMN", "Autumn", 0xFFC2410Cu),
        ThemeSeed("GRAPHITE", "Graphite", 0xFF525252u),
        ThemeSeed("SPRING", "Spring", 0xFF16A34Au),
        ThemeSeed("BLUSH", "Blush", 0xFFE11D48u),
        ThemeSeed("COBALT", "Cobalt", 0xFF1E40AFu),
        ThemeSeed("PISTACHIO", "Pistachio", 0xFF4D7C0Fu),
        ThemeSeed("MOCHA", "Mocha", 0xFF92643Au),
        ThemeSeed("LILAC", "Lilac", 0xFF7C3AEDu),
        ThemeSeed("TANGERINE", "Tangerine", 0xFFC2410Cu),
        ThemeSeed("ARCTIC", "Arctic", 0xFF0284C7u),
        ThemeSeed("PLUM", "Plum", 0xFF7E22CEu),
        ThemeSeed("HONEYDEW", "Honeydew", 0xFF16A34Au),
    )
}