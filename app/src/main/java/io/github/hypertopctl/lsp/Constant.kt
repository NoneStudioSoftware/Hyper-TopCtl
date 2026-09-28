package io.github.hypertopctl.lsp

/**
 * Shared keys for the remote preferences bridged between the app process (writer)
 * and the hooked SystemUI process (reader) via libxposed [io.github.libxposed.service.XposedService].
 */
object Constant {
    /** Remote preferences group name. */
    const val SP_GROUP = "hyper-topctl"

    /** Master switch. When false, the hook never intercepts (original behavior kept). */
    const val KEY_GLOBAL_ENABLED = "global_enabled"

    /**
     * List mode. See [ListMode]. Stored as the enum name string.
     * - BLACKLIST: only packages in the set are intercepted.
     * - WHITELIST: packages in the set are NOT intercepted; everything else is.
     */
    const val KEY_LIST_MODE = "list_mode"

    /** The package name set, stored as a StringSet. */
    const val KEY_PACKAGE_SET = "package_set"

    /** Selected UI framework for the manager app. Stored as [UiFrameworkValue]. */
    const val KEY_UI_FRAMEWORK = "ui_framework"

    /** Defaults: global enabled, empty list. With an empty set + BLACKLIST, nothing matches,
     *  so the effective default is "intercept for all apps" is achieved by WHITELIST+empty.
     *  We default to WHITELIST + empty set => intercept everywhere. */
    const val DEFAULT_GLOBAL_ENABLED = true

    /** Whitelist + empty set = disable scroll-to-top globally (matches the product default). */
    val DEFAULT_LIST_MODE = ListMode.WHITELIST
}

enum class ListMode {
    /** Only listed packages are intercepted. */
    BLACKLIST,

    /** Listed packages are exempt; everything else is intercepted. */
    WHITELIST;

    companion object {
        fun fromName(name: String?): ListMode =
            entries.firstOrNull { it.name == name } ?: Constant.DEFAULT_LIST_MODE
    }
}

object UiFrameworkValue {
    const val MIUIX = "miuix"
    const val MATERIAL = "material"
    const val DEFAULT = MIUIX
}
