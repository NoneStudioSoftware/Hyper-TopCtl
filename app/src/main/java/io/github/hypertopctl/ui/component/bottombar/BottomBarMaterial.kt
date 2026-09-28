package io.github.hypertopctl.ui.component.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.github.hypertopctl.ui.LocalMainPagerState

private data class MdNavIcon(val selected: ImageVector, val unselected: ImageVector)

private fun iconFor(dest: BottomBarDestination): MdNavIcon = when (dest) {
    BottomBarDestination.Home -> MdNavIcon(Icons.Filled.Home, Icons.Outlined.Home)
    BottomBarDestination.TopCtl -> MdNavIcon(Icons.Filled.Tune, Icons.Outlined.Tune)
    BottomBarDestination.AppList -> MdNavIcon(Icons.Filled.Apps, Icons.Outlined.Apps)
    BottomBarDestination.Settings -> MdNavIcon(Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun BottomBarMaterial() {
    val pager = LocalMainPagerState.current
    NavigationBar {
        BottomBarDestination.entries.forEachIndexed { index, dest ->
            val selected = pager.selectedPage == index
            val icons = iconFor(dest)
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) pager.animateToPage(index) },
                icon = {
                    Icon(
                        imageVector = if (selected) icons.selected else icons.unselected,
                        contentDescription = stringResource(dest.label),
                    )
                },
                label = {
                    Text(
                        stringResource(dest.label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}
