package io.github.hypertopctl.ui.component.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import io.github.hypertopctl.ui.LocalMainPagerState
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune

private fun iconFor(dest: BottomBarDestination): ImageVector = when (dest) {
    BottomBarDestination.Home -> MiuixIcons.Home
    BottomBarDestination.TopCtl -> MiuixIcons.Tune
    BottomBarDestination.AppList -> MiuixIcons.ListView
    BottomBarDestination.Settings -> MiuixIcons.Settings
}

@Composable
fun BottomBarMiuix() {
    val pager = LocalMainPagerState.current
    NavigationBar {
        BottomBarDestination.entries.forEachIndexed { index, dest ->
            NavigationBarItem(
                selected = pager.selectedPage == index,
                onClick = { pager.animateToPage(index) },
                icon = iconFor(dest),
                label = stringResource(dest.label),
            )
        }
    }
}
