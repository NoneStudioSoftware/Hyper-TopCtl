package io.github.hypertopctl.lsp.module

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import io.github.hypertopctl.lsp.Constant
import io.github.hypertopctl.lsp.config.ModuleConfig
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * Entry point for the modern libxposed API (level 102).
 *
 * Hooks `miui.hardware.input.MiuiInputManager.scrollToTop()` inside `com.android.systemui`.
 * That method is what HyperOS/MIUI invokes when the user taps the status bar to scroll the
 * foreground app's list back to the top. By intercepting it and NOT calling `chain.proceed()`,
 * the scroll-to-top action is suppressed.
 *
 * The decision is data-driven via [ModuleConfig], read from remote preferences that the manager
 * app writes. Reading happens on every invocation so config changes take effect without a reboot.
 */
@Keep
class ScrollTopModule : XposedModule() {

    private companion object {
        const val TAG = "HyperTopCtl"
        const val TARGET_CLASS = "miui.hardware.input.MiuiInputManager"
        const val TARGET_METHOD = "scrollToTop"
    }

    override fun onPackageReady(param: PackageReadyParam) {
        // Scope is statically limited to com.android.systemui, but guard anyway.
        if (param.packageName != "com.android.systemui") return

        val clazz = runCatching {
            Class.forName(TARGET_CLASS, false, param.classLoader)
        }.getOrNull()

        if (clazz == null) {
            log(Log.WARN, TAG, "$TARGET_CLASS not found; device may not use this hook point")
            return
        }

        val method = runCatching { clazz.getDeclaredMethod(TARGET_METHOD) }.getOrNull()
        if (method == null) {
            log(Log.WARN, TAG, "$TARGET_CLASS.$TARGET_METHOD() not found; structure changed")
            return
        }

        hook(method)
            .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
            .intercept(ScrollTopHooker(this))

        log(Log.INFO, TAG, "Hooked $TARGET_CLASS.$TARGET_METHOD()")
    }

    /**
     * Reads the current config from remote preferences. Kept lightweight; SharedPreferences reads
     * are cheap and the hot path (status-bar tap) is not performance sensitive.
     */
    internal fun currentConfig(): ModuleConfig =
        ModuleConfig.from(getRemotePreferences(Constant.SP_GROUP))

    /**
     * Best-effort resolution of the foreground package from the SystemUI process.
     *
     * The hook process has no injected Context, so [android.app.ActivityThread.currentApplication]
     * (a classic Xposed technique, exempted from hidden-API restrictions by the framework) is used
     * to obtain one, then the running-tasks API returns the top task — SystemUI is a privileged
     * app and can see other apps' tasks. When any step fails (context not yet attached, permission
     * missing on some builds) this returns null and the config falls back to the global policy
     * (see [ModuleConfig.shouldIntercept]).
     */
    internal fun resolveForegroundPackage(chain: XposedInterface.Chain): String? = runCatching {
        val context = currentApplicationContext() ?: return@runCatching null
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return@runCatching null
        am.getRunningTasks(1)
            ?.firstOrNull()
            ?.topActivity
            ?.packageName
    }.onFailure {
        log(Log.WARN, TAG, "resolveForegroundPackage failed: ${it.message}")
    }.getOrNull()

    private fun currentApplicationContext(): Context? = runCatching {
        val activityThread = Class.forName("android.app.ActivityThread")
        activityThread
            .getMethod("currentApplication")
            .invoke(null) as? Application
    }.getOrNull()?.applicationContext

    /**
     * The actual interceptor. Separated into its own [XposedInterface.Hooker] so the logic is clear
     * and testable, and so PROTECTIVE mode can catch any config-read failure without affecting the
     * original scroll behavior.
     */
    private class ScrollTopHooker(
        private val module: ScrollTopModule,
    ) : XposedInterface.Hooker {
        override fun intercept(chain: XposedInterface.Chain): Any? {
            val config = module.currentConfig()
            val pkg = module.resolveForegroundPackage(chain)

            return if (config.shouldIntercept(pkg)) {
                // Suppress scroll-to-top: do NOT call chain.proceed().
                module.log(Log.INFO, TAG, "Intercepted scrollToTop (pkg=$pkg)")
                null
            } else {
                module.log(Log.INFO, TAG, "Allowed scrollToTop (pkg=$pkg)")
                chain.proceed()
            }
        }
    }
}
