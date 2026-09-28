package io.github.hypertopctl.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import io.github.hypertopctl.App
import io.github.hypertopctl.data.SettingsRepository
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.libxposed.service.XposedService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class MainUiState(
    val active: Boolean = false,
    val frameworkName: String? = null,
    val globalEnabled: Boolean = true,
    val listMode: ListMode = ListMode.WHITELIST,
    val packages: Set<String> = emptySet(),
    val uiFramework: String = UiFrameworkValue.DEFAULT,
)

class MainViewModel : ViewModel(), App.ServiceStateListener {

    private val _state = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private var repo: SettingsRepository = SettingsRepository(App.service)

    init {
        App.addServiceStateListener(this, notifyImmediately = true)
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
                uiFramework = repo.readUiFramework(),
            )
        }
    }

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

    fun setUiFramework(value: String) {
        repo.setUiFramework(value)
        _state.update { it.copy(uiFramework = value) }
    }

    override fun onCleared() {
        App.removeServiceStateListener(this)
        super.onCleared()
    }
}
