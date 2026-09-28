package io.github.hypertopctl.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import io.github.hypertopctl.App
import io.github.hypertopctl.data.AppSettingsRepository
import io.github.hypertopctl.data.SettingsRepository
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackAnimation
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackExitDirection
import io.github.hypertopctl.ui.theme.AppThemeSettings
import io.github.hypertopctl.ui.theme.ColorMode
import io.github.libxposed.service.XposedService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class DeviceInfo(
    val model: String,
    val androidRelease: String,
    val sdk: Int,
    val hyperOsVersion: String?,
)

@Immutable
data class MainUiState(
    val active: Boolean = false,
    val frameworkName: String? = null,
    val globalEnabled: Boolean = true,
    val listMode: ListMode = ListMode.WHITELIST,
    val packages: Set<String> = emptySet(),
    val uiFramework: String = UiFrameworkValue.DEFAULT,
    val device: DeviceInfo = readDeviceInfo(),
    // Theme configuration (local, app-only)
    val colorMode: Int = 0,
    val keyColor: Int = 0,
    val colorStyle: String = PaletteStyle.TonalSpot.name,
    val colorSpec: String = ColorSpec.SpecVersion.SPEC_2025.name,
    val dynamicColor: Boolean = true,
    val miuixTransitionAnimation: Boolean = true,
    val predictiveBackAnimation: PredictiveBackAnimation = PredictiveBackAnimation.Scale,
    val predictiveBackExitDirection: PredictiveBackExitDirection = PredictiveBackExitDirection.FOLLOW_GESTURE,
) {
    /** Build the immutable snapshot consumed by the theme layer. */
    fun toThemeSettings(): AppThemeSettings = AppThemeSettings(
        uiFramework = uiFramework,
        colorMode = ColorMode.fromValue(colorMode),
        keyColor = keyColor,
        paletteStyle = runCatching { PaletteStyle.valueOf(colorStyle) }
            .getOrDefault(PaletteStyle.TonalSpot),
        colorSpec = runCatching { ColorSpec.SpecVersion.valueOf(colorSpec) }
            .getOrDefault(ColorSpec.SpecVersion.SPEC_2025),
        dynamicColor = dynamicColor,
    )
}

private fun readDeviceInfo(): DeviceInfo = DeviceInfo(
    model = android.os.Build.MODEL,
    androidRelease = android.os.Build.VERSION.RELEASE,
    sdk = android.os.Build.VERSION.SDK_INT,
    hyperOsVersion = runCatching {
        @Suppress("PrivateApi")
        val get = Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java)
        (get.invoke(null, "ro.mi.os.version.name") as? String)?.takeIf { it.isNotBlank() }
    }.getOrNull(),
)

class MainViewModel : ViewModel(), App.ServiceStateListener {

    private val _state = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    /** Hook config (globalEnabled / listMode / packages) lives in the libxposed remote prefs. */
    private var repo: SettingsRepository = SettingsRepository(App.service)

    /** UI framework + theme are app-only, persisted locally. */
    private val appSettings = AppSettingsRepository(App.instance)

    init {
        App.addServiceStateListener(this, notifyImmediately = true)
        loadLocalSettings()
    }

    override fun onServiceStateChanged(service: XposedService?) {
        repo = SettingsRepository(service)
        reload()
    }

    private fun reload() {
        val config = repo.readConfig()
        _state.update {
            it.copy(
                active = repo.isActive,
                frameworkName = repo.frameworkName(),
                globalEnabled = config.globalEnabled,
                listMode = config.listMode,
                packages = config.packages,
            )
        }
    }

    private fun loadLocalSettings() {
        _state.update {
            it.copy(
                uiFramework = appSettings.uiFramework,
                colorMode = appSettings.colorMode,
                keyColor = appSettings.keyColor,
                colorStyle = appSettings.colorStyle,
                colorSpec = appSettings.colorSpec,
                dynamicColor = appSettings.dynamicColor,
                miuixTransitionAnimation = appSettings.miuixTransitionAnimation,
                predictiveBackAnimation = PredictiveBackAnimation.fromValueOrDefault(appSettings.predictiveBackAnimation),
                predictiveBackExitDirection = PredictiveBackExitDirection.fromValueOrDefault(appSettings.predictiveBackExitDirection),
            )
        }
    }

    // ---- Hook config setters (remote prefs) ----

    fun setGlobalEnabled(value: Boolean) {
        repo.setGlobalEnabled(value)
        _state.update { it.copy(globalEnabled = value) }
    }

    fun setListMode(mode: ListMode) {
        repo.setListMode(mode)
        _state.update { it.copy(listMode = mode) }
    }

    fun setPackages(packages: Set<String>) {
        repo.setPackages(packages)
        _state.update { it.copy(packages = packages) }
    }

    fun togglePackage(pkg: String) {
        val next = _state.value.packages.toMutableSet().apply {
            if (!add(pkg)) remove(pkg)
        }
        setPackages(next)
    }

    // ---- UI framework + theme setters (local prefs) ----

    fun setUiFramework(value: String) {
        appSettings.uiFramework = value
        _state.update { it.copy(uiFramework = value) }
    }

    /** Set light/dark preference (0=system,1=light,2=dark), preserving the Monet flag. */
    fun setThemeMode(mode: Int) {
        val current = ColorMode.fromValue(_state.value.colorMode)
        val newValue = if (current.isMonet) {
            ColorMode.fromValue(mode).toMonetMode()
        } else {
            mode
        }
        appSettings.colorMode = newValue
        _state.update { it.copy(colorMode = newValue) }
    }

    /** Miuix side: toggle Monet (dynamic) on/off, preserving light/dark preference. */
    fun setMiuixMonet(enabled: Boolean) {
        val current = ColorMode.fromValue(_state.value.colorMode)
        val newValue = if (enabled) current.toMonetMode() else current.toNonMonetMode()
        appSettings.colorMode = newValue
        _state.update { it.copy(colorMode = newValue) }
    }

    fun setColorMode(value: Int) {
        appSettings.colorMode = value
        _state.update { it.copy(colorMode = value) }
    }

    fun setKeyColor(argb: Int) {
        appSettings.keyColor = argb
        _state.update { it.copy(keyColor = argb) }
    }

    fun setColorStyle(name: String) {
        appSettings.colorStyle = name
        _state.update { it.copy(colorStyle = name) }
    }

    fun setColorSpec(name: String) {
        appSettings.colorSpec = name
        _state.update { it.copy(colorSpec = name) }
    }

    fun setDynamicColor(enabled: Boolean) {
        appSettings.dynamicColor = enabled
        _state.update { it.copy(dynamicColor = enabled) }
    }

    fun setMiuixTransitionAnimation(enabled: Boolean) {
        appSettings.miuixTransitionAnimation = enabled
        _state.update { it.copy(miuixTransitionAnimation = enabled) }
    }

    fun setPredictiveBackAnimation(anim: PredictiveBackAnimation) {
        appSettings.predictiveBackAnimation = anim.value
        _state.update { it.copy(predictiveBackAnimation = anim) }
    }

    fun setPredictiveBackExitDirection(direction: PredictiveBackExitDirection) {
        appSettings.predictiveBackExitDirection = direction.value
        _state.update { it.copy(predictiveBackExitDirection = direction) }
    }

    override fun onCleared() {
        App.removeServiceStateListener(this)
        super.onCleared()
    }
}
