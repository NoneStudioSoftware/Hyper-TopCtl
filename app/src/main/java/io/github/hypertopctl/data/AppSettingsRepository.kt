package io.github.hypertopctl.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import io.github.hypertopctl.lsp.UiFrameworkValue

/**
 * Local (in-app) settings persisted via plain [SharedPreferences]. These are app-only preferences
 * (UI framework choice + theme configuration) that do NOT need to cross into the hooked process,
 * so they live locally rather than in the libxposed remote preferences used by [SettingsRepository].
 *
 * Storage layout mirrors KernelSU Manager (file "settings", same keys) for familiarity.
 */
class AppSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var uiFramework: String
        get() = prefs.getString(KEY_UI_MODE, UiFrameworkValue.DEFAULT) ?: UiFrameworkValue.DEFAULT
        set(value) = prefs.edit { putString(KEY_UI_MODE, value) }

    /** ColorMode ordinal-value (see ColorMode.value). Default 0 = SYSTEM. */
    var colorMode: Int
        get() = prefs.getInt(KEY_COLOR_MODE, 0)
        set(value) = prefs.edit { putInt(KEY_COLOR_MODE, value) }

    /** ARGB seed color; 0 = default (use dynamic/system primary). */
    var keyColor: Int
        get() = prefs.getInt(KEY_KEY_COLOR, 0)
        set(value) = prefs.edit { putInt(KEY_KEY_COLOR, value) }

    var colorStyle: String
        get() = prefs.getString(KEY_COLOR_STYLE, PaletteStyle.TonalSpot.name)
            ?: PaletteStyle.TonalSpot.name
        set(value) = prefs.edit { putString(KEY_COLOR_STYLE, value) }

    var colorSpec: String
        get() = prefs.getString(KEY_COLOR_SPEC, ColorSpec.SpecVersion.SPEC_2025.name)
            ?: ColorSpec.SpecVersion.SPEC_2025.name
        set(value) = prefs.edit { putString(KEY_COLOR_SPEC, value) }

    /** MD3 side: use system dynamic color instead of the custom seed. Default true. */
    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) = prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }

    /** Register a listener so Compose can recompose when any theme pref changes. */
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_UI_MODE = "ui_mode"
        private const val KEY_COLOR_MODE = "color_mode"
        private const val KEY_KEY_COLOR = "key_color"
        private const val KEY_COLOR_STYLE = "color_style"
        private const val KEY_COLOR_SPEC = "color_spec"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }
}
