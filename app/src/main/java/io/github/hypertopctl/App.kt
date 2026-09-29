package io.github.hypertopctl

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import io.github.hypertopctl.data.AppSettingsRepository
import io.github.libxposed.service.XposedService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lsposed.hiddenapibypass.HiddenApiBypass
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.Volatile

/**
 * Application that binds to the LSPosed [XposedService]. A non-null service means the module is
 * active in the framework; it is also the channel used to read/write the module's remote
 * preferences (shared with the hooked SystemUI process).
 *
 * Pattern adapted from the reference project `fuck-hyperos-scroll-top`.
 */
/**
 * Startup phases gating the splash screen: Loading keeps the system splash on screen until the
 * manager UI has finished its initial configuration load (see [ui.StartupSplashGate]).
 */
enum class StartupState { Loading, Ready }

class App : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        /** Application context, available from onCreate onward, for local settings access. */
        @Volatile
        lateinit var instance: App
            private set

        @Volatile
        var service: XposedService? = null
            private set

        private val startupStateFlow = MutableStateFlow(StartupState.Loading)

        /** Observed by the splash screen keep-on-screen condition. */
        val startupState: StateFlow<StartupState> = startupStateFlow.asStateFlow()

        /** Called once initial configuration (service state + local settings) has been loaded. */
        fun markStartupReady() {
            startupStateFlow.value = StartupState.Ready
        }

        private val listeners = CopyOnWriteArraySet<ServiceStateListener>()

        fun addServiceStateListener(listener: ServiceStateListener, notifyImmediately: Boolean) {
            listeners.add(listener)
            if (notifyImmediately) listener.onServiceStateChanged(service)
        }

        fun removeServiceStateListener(listener: ServiceStateListener) {
            listeners.remove(listener)
        }

        /** Mirrors KernelSU Manager: opt out of Android's platform predictive-back callback when disabled. */
        fun setEnableOnBackInvokedCallback(appInfo: ApplicationInfo, enable: Boolean) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
            runCatching {
                HiddenApiBypass.addHiddenApiExemptions(
                    "Landroid/content/pm/ApplicationInfo;->setEnableOnBackInvokedCallback",
                )
                val method = ApplicationInfo::class.java.getDeclaredMethod(
                    "setEnableOnBackInvokedCallback",
                    Boolean::class.javaPrimitiveType,
                )
                method.isAccessible = true
                method.invoke(appInfo, enable)
            }
        }

        private fun notifyAll(current: XposedService?) {
            for (l in listeners) if (listeners.contains(l)) l.onServiceStateChanged(current)
        }
    }

    fun interface ServiceStateListener {
        fun onServiceStateChanged(service: XposedService?)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setEnableOnBackInvokedCallback(
            applicationInfo,
            AppSettingsRepository(this).enablePredictiveBack,
        )
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        Companion.service = service
        notifyAll(service)
    }

    override fun onServiceDied(service: XposedService) {
        Companion.service = null
        notifyAll(null)
    }
}
