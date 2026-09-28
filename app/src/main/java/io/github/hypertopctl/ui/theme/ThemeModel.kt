package io.github.hypertopctl.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

/**
 * Color mode, mirroring KernelSU Manager's ColorMode. Stored as an Int in prefs (`color_mode`).
 * The MONET_* variants pair "dynamic color" with a light/dark preference for the Miuix side.
 */
enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    MONET_SYSTEM(3),
    MONET_LIGHT(4),
    MONET_DARK(5),
    DARK_AMOLED(6);

    val isSystem: Boolean get() = value == 0 || value == 3
    val isDark: Boolean get() = value == 2 || value == 5 || value == 6
    val isAmoled: Boolean get() = value == 6
    val isMonet: Boolean get() = value >= 3

    fun toNonMonetMode(): Int = when (this) {
        MONET_SYSTEM -> 0
        MONET_LIGHT -> 1
        MONET_DARK, DARK_AMOLED -> 2
        else -> value
    }

    fun toMonetMode(): Int = when (this) {
        SYSTEM -> 3
        LIGHT -> 4
        DARK -> 5
        else -> value
    }

    companion object {
        fun fromValue(value: Int): ColorMode = entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}

/** Immutable snapshot of the app's theme configuration, consumed by the theme layer. */
@Immutable
data class AppThemeSettings(
    val uiFramework: String,
    val colorMode: ColorMode,
    /** ARGB seed color; 0 means "default" (use the dynamic/system primary as seed). */
    val keyColor: Int,
    val paletteStyle: PaletteStyle,
    val colorSpec: ColorSpec.SpecVersion,
    /** MD3 side: whether dynamic (system) color is used instead of the custom seed. */
    val dynamicColor: Boolean,
)

/** Palette styles usable under a given spec. SPEC_2025 only supports the first four. */
val PaletteStyle.supportsSpec2025: Boolean
    get() = this == PaletteStyle.TonalSpot ||
        this == PaletteStyle.Neutral ||
        this == PaletteStyle.Vibrant ||
        this == PaletteStyle.Expressive

/** Downgrade a spec to 2021 when the chosen style is not 2025-compatible. */
fun ColorSpec.SpecVersion.effectiveFor(style: PaletteStyle): ColorSpec.SpecVersion =
    if (this == ColorSpec.SpecVersion.SPEC_2025 && !style.supportsSpec2025) {
        ColorSpec.SpecVersion.SPEC_2021
    } else {
        this
    }

/** Human-readable label for a palette style (MD3 side uses these; Miuix uses raw enum names). */
fun PaletteStyle.displayName(): String = when (this) {
    PaletteStyle.TonalSpot -> "Tonal Spot"
    PaletteStyle.Neutral -> "Neutral"
    PaletteStyle.Vibrant -> "Vibrant"
    PaletteStyle.Expressive -> "Expressive"
    PaletteStyle.Rainbow -> "Rainbow"
    PaletteStyle.FruitSalad -> "Fruit Salad"
    PaletteStyle.Monochrome -> "Monochrome"
    PaletteStyle.Fidelity -> "Fidelity"
    PaletteStyle.Content -> "Content"
}

fun ColorSpec.SpecVersion.displayName(): String = when (this) {
    ColorSpec.SpecVersion.SPEC_2021 -> "Spec 2021"
    ColorSpec.SpecVersion.SPEC_2025 -> "Spec 2025"
}

/**
 * Named seed colors, matching KernelSU's palette. Index 0 is the sentinel "Default" (value 0,
 * meaning "use the dynamic/system primary"); the rest are concrete ARGB seeds.
 */
data class NamedColor(val labelRes: Int, val argb: Int)

/** ARGB values for the 15 concrete named colors, in KernelSU order. */
val keyColorOptions: List<Int> = listOf(
    Color(0xFFF44336).toArgb(), // red
    Color(0xFFE91E63).toArgb(), // pink
    Color(0xFF9C27B0).toArgb(), // purple
    Color(0xFF673AB7).toArgb(), // deep purple
    Color(0xFF3F51B5).toArgb(), // indigo
    Color(0xFF2196F3).toArgb(), // blue
    Color(0xFF00BCD4).toArgb(), // cyan
    Color(0xFF009688).toArgb(), // teal
    Color(0xFF4FAF50).toArgb(), // green
    Color(0xFFFFEB3B).toArgb(), // yellow
    Color(0xFFFFC107).toArgb(), // amber
    Color(0xFFFF9800).toArgb(), // orange
    Color(0xFF795548).toArgb(), // brown
    Color(0xFF607D8F).toArgb(), // blue grey
    Color(0xFFFF9CA8).toArgb(), // sakura
)
