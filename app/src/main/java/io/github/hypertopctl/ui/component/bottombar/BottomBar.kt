package io.github.hypertopctl.ui.component.bottombar

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import io.github.hypertopctl.R
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

/**
 * The four top-level destinations, in pager order.
 * Home / TopCtl / AppList / Settings — mirrors KernelSU Manager's Home / SuperUser / Module / Setting.
 */
enum class BottomBarDestination(@get:StringRes val label: Int) {
    Home(R.string.nav_home),
    TopCtl(R.string.nav_topctl),
    AppList(R.string.nav_applist),
    Settings(R.string.nav_settings);

    companion object {
        val PAGE_COUNT = entries.size
    }
}

@Composable
fun BottomBar(isBottomBar: Boolean = true) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> BottomBarMiuix()
        UiMode.Material -> BottomBarMaterial(isBottomBar = isBottomBar)
    }
}
