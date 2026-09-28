package io.github.hypertopctl.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.hypertopctl.lsp.UiFrameworkValue

/**
 * The selectable UI design system. Pattern borrowed from KernelSU Manager's dual-framework setup.
 */
enum class UiMode(val value: String) {
    Miuix(UiFrameworkValue.MIUIX),
    Material(UiFrameworkValue.MATERIAL);

    companion object {
        fun fromValue(value: String?): UiMode = when (value) {
            Material.value -> Material
            else -> Miuix
        }
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.Miuix }
