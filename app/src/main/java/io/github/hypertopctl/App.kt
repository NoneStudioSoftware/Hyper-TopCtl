package io.github.hypertopctl

import android.app.Application
import io.github.libxposed.service.XposedService
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
class App : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        /** Application context, available from onCreate onward, for local settings access. */
        @Volatile
        lateinit var instance: App
            private set

        @Volatile
        var service: XposedService? = null
            private set

        private val listeners = CopyOnWriteArraySet<ServiceStateListener>()

        fun addServiceStateListener(listener: ServiceStateListener, notifyImmediately: Boolean) {
            listeners.add(listener)
            if (notifyImmediately) listener.onServiceStateChanged(service)
        }

        fun removeServiceStateListener(listener: ServiceStateListener) {
            listeners.remove(listener)
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
