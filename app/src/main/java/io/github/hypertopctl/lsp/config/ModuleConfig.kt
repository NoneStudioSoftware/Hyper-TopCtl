package io.github.hypertopctl.lsp.config

import android.content.SharedPreferences
import io.github.hypertopctl.lsp.Constant
import io.github.hypertopctl.lsp.ListMode

/**
 * A pure, immutable snapshot of the module configuration.
 *
 * Both the manager app (via [io.github.libxposed.service.XposedService.getRemotePreferences])
 * and the hook process (via the module's own remote preferences) obtain a
 * [SharedPreferences] and decode it into this object. Keeping the decision logic here means
 * the app and the hook can never disagree about what a config means.
 */
data class ModuleConfig(
    val globalEnabled: Boolean,
    val listMode: ListMode,
    val packages: Set<String>,
) {
    /**
     * Decide whether the "scroll to top" action should be intercepted (i.e. disabled)
     * for the given foreground package.
     *
     * @param packageName the resolved foreground app package, or null when it could not be
     *   determined in the SystemUI process. When null we fall back to the global policy that
     *   does not depend on a package (empty-list cases still resolve correctly).
     */
    fun shouldIntercept(packageName: String?): Boolean {
        if (!globalEnabled) return false
        return when (listMode) {
            // Only listed packages are intercepted. Unknown package => not intercepted.
            ListMode.BLACKLIST -> packageName != null && packageName in packages
            // Listed packages are exempt; everything else is intercepted.
            // Unknown package => cannot prove it is exempt, so intercept (global-first policy).
            ListMode.WHITELIST -> packageName == null || packageName !in packages
        }
    }

    companion object {
        fun from(prefs: SharedPreferences): ModuleConfig = ModuleConfig(
            globalEnabled = prefs.getBoolean(
                Constant.KEY_GLOBAL_ENABLED,
                Constant.DEFAULT_GLOBAL_ENABLED,
            ),
            listMode = ListMode.fromName(prefs.getString(Constant.KEY_LIST_MODE, null)),
            packages = prefs.getStringSet(Constant.KEY_PACKAGE_SET, emptySet())
                ?.toSet()
                ?: emptySet(),
        )
    }
}
