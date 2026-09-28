package io.github.hypertopctl.data

import androidx.core.content.edit
import io.github.hypertopctl.lsp.Constant
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.lsp.config.ModuleConfig
import io.github.libxposed.service.XposedService

/**
 * App-side read/write access to the module's remote preferences via [XposedService].
 *
 * When the service is null (module inactive), reads return defaults and writes are no-ops. This
 * keeps the UI functional (showing defaults + an inactive banner) even without an active framework.
 */
class SettingsRepository(private val service: XposedService?) {

    private val prefs get() = service?.getRemotePreferences(Constant.SP_GROUP)

    val isActive: Boolean get() = service != null

    fun frameworkName(): String? = service?.frameworkName

    fun readConfig(): ModuleConfig {
        val p = prefs ?: return ModuleConfig(
            globalEnabled = Constant.DEFAULT_GLOBAL_ENABLED,
            listMode = Constant.DEFAULT_LIST_MODE,
            packages = emptySet(),
        )
        return ModuleConfig.from(p)
    }

    fun readUiFramework(): String =
        prefs?.getString(Constant.KEY_UI_FRAMEWORK, UiFrameworkValue.DEFAULT)
            ?: UiFrameworkValue.DEFAULT

    fun setGlobalEnabled(value: Boolean) {
        prefs?.edit { putBoolean(Constant.KEY_GLOBAL_ENABLED, value) }
    }

    fun setListMode(mode: ListMode) {
        prefs?.edit { putString(Constant.KEY_LIST_MODE, mode.name) }
    }

    fun setPackages(packages: Set<String>) {
        prefs?.edit { putStringSet(Constant.KEY_PACKAGE_SET, packages) }
    }

    fun setUiFramework(value: String) {
        prefs?.edit { putString(Constant.KEY_UI_FRAMEWORK, value) }
    }
}
