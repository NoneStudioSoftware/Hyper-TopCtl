package io.github.hypertopctl.ui.screen.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Animation
import androidx.compose.material.icons.twotone.ColorLens
import androidx.compose.material.icons.twotone.DarkMode
import androidx.compose.material.icons.twotone.DesignServices
import androidx.compose.material.icons.twotone.Palette
import androidx.compose.material.icons.twotone.Style
import androidx.compose.material.icons.twotone.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackAnimation
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackExitDirection
import io.github.hypertopctl.ui.component.settings.AppBackButton
import io.github.hypertopctl.ui.component.settings.SegmentedColumn
import io.github.hypertopctl.ui.component.settings.SettingsBaseWidget
import io.github.hypertopctl.ui.component.settings.SettingsChooseWidget
import io.github.hypertopctl.ui.component.settings.SettingsSwitchWidget
import io.github.hypertopctl.ui.theme.displayName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeMaterial(
    state: MainUiState,
    onBack: () -> Unit,
    onUiFrameworkChange: (String) -> Unit,
    onThemeModeChange: (Int) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onKeyColorChange: (Int) -> Unit,
    onColorStyleChange: (String) -> Unit,
    onColorSpecChange: (String) -> Unit,
    onPredictiveBackAnimationChange: (PredictiveBackAnimation) -> Unit = {},
    onPredictiveBackExitDirectionChange: (PredictiveBackExitDirection) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val themeModeIndex = (if (state.colorMode >= 3) state.colorMode - 3 else state.colorMode).coerceIn(0, 2)
    val paletteStyles = PaletteStyle.entries.toList()
    val colorSpecs = ColorSpec.SpecVersion.entries.toList()
    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.theme_settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                navigationIcon = {
                    AppBackButton(onClick = onBack)
                },
                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }
            item {
                SegmentedColumn(
                    title = stringResource(R.string.settings_category_appearance),
                ) {
                    // UI framework: Miuix / Material
                    item {
                        SettingsChooseWidget(
                            icon = Icons.TwoTone.Palette,
                            title = stringResource(R.string.ui_framework),
                            items = listOf(
                                stringResource(R.string.ui_framework_miuix),
                                stringResource(R.string.ui_framework_material),
                            ),
                            selectedIndex = if (state.uiFramework == UiFrameworkValue.MATERIAL) 1 else 0,
                            onSelectedIndexChange = {
                                onUiFrameworkChange(
                                    if (it == 1) UiFrameworkValue.MATERIAL else UiFrameworkValue.MIUIX,
                                )
                            },
                        )
                    }

                    // Theme mode
                    item {
                        SettingsChooseWidget(
                            icon = Icons.TwoTone.DarkMode,
                            title = stringResource(R.string.theme_mode),
                            items = listOf(
                                stringResource(R.string.theme_follow_system),
                                stringResource(R.string.theme_light),
                                stringResource(R.string.theme_dark),
                            ),
                            selectedIndex = themeModeIndex,
                            onSelectedIndexChange = onThemeModeChange,
                        )
                    }

                    // Dynamic color (expandable → theme color picker when OFF)
                    expandableItem(
                        expanded = !state.dynamicColor,
                        topContent = {
                            SettingsSwitchWidget(
                                icon = Icons.TwoTone.ColorLens,
                                title = stringResource(R.string.dynamic_color_title),
                                description = stringResource(R.string.dynamic_color_summary),
                                checked = state.dynamicColor,
                                onCheckedChange = onDynamicColorChange,
                            )
                        },
                        bottomContent = {
                            item {
                                SettingsBaseWidget(
                                    icon = Icons.TwoTone.Palette,
                                    title = stringResource(R.string.theme_color),
                                    description = "#%06X".format(0xFFFFFF and state.keyColor),
                                    onClick = { showColorDialog = true },
                                    trailingContent = { _ ->
                                        Box(
                                            Modifier
                                                .size(24.dp)
                                                .then(Modifier)
                                        ) {
                                            Icon(
                                                imageVector = Icons.TwoTone.Palette,
                                                contentDescription = null,
                                                tint = if (state.keyColor == 0) MaterialTheme.colorScheme.primary
                                                else Color(state.keyColor),
                                            )
                                        }
                                    },
                                )
                            }
                        },
                    )

                    // Palette style
                    item {
                        SettingsChooseWidget(
                            icon = Icons.TwoTone.Style,
                            title = stringResource(R.string.dynamic_palette_style),
                            items = paletteStyles.map { it.displayName() },
                            selectedIndex = paletteStyles.indexOfFirst { it.name == state.colorStyle }
                                .coerceAtLeast(0),
                            onSelectedIndexChange = { onColorStyleChange(paletteStyles[it].name) },
                        )
                    }

                    // Color spec
                    item {
                        SettingsChooseWidget(
                            icon = Icons.TwoTone.DesignServices,
                            title = stringResource(R.string.dynamic_color_spec),
                            items = colorSpecs.map { it.displayName() },
                            selectedIndex = colorSpecs.indexOfFirst { it.name == state.colorSpec }
                                .coerceAtLeast(0),
                            onSelectedIndexChange = { onColorSpecChange(colorSpecs[it].name) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Predictive back & secondary screen transitions
            item {
                SegmentedColumn(
                    title = stringResource(R.string.predictive_back_settings),
                ) {
                    item {
                        val anims = PredictiveBackAnimation.entries
                        SettingsChooseWidget(
                            icon = Icons.TwoTone.Animation,
                            title = stringResource(R.string.settings_transition_animation),
                            items = anims.map { anim ->
                                when (anim) {
                                    PredictiveBackAnimation.None -> stringResource(R.string.predictive_back_animation_none)
                                    PredictiveBackAnimation.AOSP -> stringResource(R.string.predictive_back_animation_aosp)
                                    PredictiveBackAnimation.Scale -> stringResource(R.string.predictive_back_animation_scale)
                                    PredictiveBackAnimation.KernelSUClassic -> stringResource(R.string.predictive_back_animation_ksu_classic)
                                }
                            },
                            selectedIndex = anims.indexOf(state.predictiveBackAnimation).coerceAtLeast(0),
                            onSelectedIndexChange = { onPredictiveBackAnimationChange(anims[it]) },
                        )
                    }

                    item(visible = state.predictiveBackAnimation == PredictiveBackAnimation.Scale) {
                        val directions = PredictiveBackExitDirection.entries
                        SettingsChooseWidget(
                            icon = Icons.TwoTone.SwapHoriz,
                            title = stringResource(R.string.predictive_back_exit_direction),
                            items = directions.map { dir ->
                                when (dir) {
                                    PredictiveBackExitDirection.FOLLOW_GESTURE -> stringResource(R.string.predictive_back_exit_direction_follow_gesture)
                                    PredictiveBackExitDirection.ALWAYS_RIGHT -> stringResource(R.string.predictive_back_exit_direction_always_right)
                                    PredictiveBackExitDirection.ALWAYS_LEFT -> stringResource(R.string.predictive_back_exit_direction_always_left)
                                }
                            },
                            selectedIndex = directions.indexOf(state.predictiveBackExitDirection).coerceAtLeast(0),
                            onSelectedIndexChange = { onPredictiveBackExitDirectionChange(directions[it]) },
                        )
                    }
                }
            }
        }
    }

    if (showColorDialog) {
        ThemeColorDialog(
            initialColor = if (state.keyColor == 0) MaterialTheme.colorScheme.primary.let { it }
            else Color(state.keyColor),
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                onKeyColorChange(it)
                showColorDialog = false
            },
        )
    }
}
