package io.github.hypertopctl.ui.component.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBarMaterial(isBottomBar: Boolean = true) {
    val pager = LocalMainPagerState.current
    val destinations = BottomBarDestination.entries

    if (isBottomBar) {
        FlexibleBottomAppBar(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                ),
        ) {
            destinations.forEachIndexed { index, dest ->
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
                            text = stringResource(dest.label),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                        )
                    },
                    alwaysShowLabel = false,
                )
            }
        }
    } else {
        WideNavigationRail(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                ),
            colors = WideNavigationRailDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            destinations.forEachIndexed { index, dest ->
                val selected = pager.selectedPage == index
                val icons = iconFor(dest)
                WideNavigationRailItem(
                    railExpanded = false,
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
                            text = stringResource(dest.label),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                        )
                    },
                )
            }
        }
    }
}
