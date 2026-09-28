package io.github.hypertopctl.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavKey

class Navigator(
    val backStack: NavBackStack
) {
    constructor(vararg initial: NavKey) : this(mutableStateListOf(*initial))

    fun push(key: NavKey) {
        if (backStack.lastOrNull() == key) return
        backStack.add(key)
    }

    fun pop() {
        if (backStack.size <= 1) return
        backStack.removeLastOrNull()
    }

    fun current(): NavKey? = backStack.lastOrNull()
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator not provided")
}
