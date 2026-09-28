package io.github.hypertopctl.ui.screen.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.ColorMode
import io.github.hypertopctl.ui.theme.keyColorOptions
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun ThemeMiuix(
    state: MainUiState,
    onBack: () -> Unit,
    onUiFrameworkChange: (String) -> Unit,
    onThemeModeChange: (Int) -> Unit,
    onMiuixMonetChange: (Boolean) -> Unit,
    onKeyColorChange: (Int) -> Unit,
    onColorStyleChange: (String) -> Unit,
    onColorSpecChange: (String) -> Unit,
    onMiuixTransitionAnimationChange: (Boolean) -> Unit = {},
) {
    val scrollBehavior = MiuixScrollBehavior()
    val colorMode = ColorMode.fromValue(state.colorMode)

    // Named color labels: index 0 = default, then the 15 concrete colors.
    val colorLabels = listOf(
        stringResource(R.string.settings_key_color_default),
        stringResource(R.string.color_red), stringResource(R.string.color_pink),
        stringResource(R.string.color_purple), stringResource(R.string.color_deep_purple),
        stringResource(R.string.color_indigo), stringResource(R.string.color_blue),
        stringResource(R.string.color_cyan), stringResource(R.string.color_teal),
        stringResource(R.string.color_green), stringResource(R.string.color_yellow),
        stringResource(R.string.color_amber), stringResource(R.string.color_orange),
        stringResource(R.string.color_brown), stringResource(R.string.color_blue_grey),
        stringResource(R.string.color_sakura),
    )
    val colorValues = listOf(0) + keyColorOptions

    val paletteStyles = PaletteStyle.entries
    val colorSpecs = ColorSpec.SpecVersion.entries

    Scaffold(
        topBar = {
            TopAppBar(
                color = colorScheme.surface,
                title = stringResource(R.string.settings_theme),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null,
                            tint = colorScheme.onBackground,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            item {
                Spacer(Modifier.height(12.dp))

                // UI framework: Miuix / Material
                Text(
                    text = stringResource(R.string.ui_framework),
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
                )
                TabRow(
                    tabs = listOf(
                        stringResource(R.string.ui_framework_miuix),
                        stringResource(R.string.ui_framework_material),
                    ),
                    selectedTabIndex = if (state.uiFramework == UiFrameworkValue.MATERIAL) 1 else 0,
                    onTabSelected = {
                        onUiFrameworkChange(if (it == 1) UiFrameworkValue.MATERIAL else UiFrameworkValue.MIUIX)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // Theme mode: system / light / dark
                Text(
                    text = stringResource(R.string.theme_mode),
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
                )
                TabRow(
                    tabs = listOf(
                        stringResource(R.string.settings_theme_mode_system),
                        stringResource(R.string.settings_theme_mode_light),
                        stringResource(R.string.settings_theme_mode_dark),
                    ),
                    selectedTabIndex = (if (state.colorMode >= 3) state.colorMode - 3 else state.colorMode)
                        .coerceIn(0, 2),
                    onTabSelected = onThemeModeChange,
                    modifier = Modifier.fillMaxWidth(),
                )

                Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                    SwitchPreference(
                        title = stringResource(R.string.settings_monet),
                        checked = colorMode.isMonet,
                        onCheckedChange = onMiuixMonetChange,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp),
                                tint = colorScheme.onBackground,
                            )
                        },
                    )

                    AnimatedVisibility(visible = colorMode.isMonet) {
                        Column {
                            OverlayDropdownPreference(
                                title = stringResource(R.string.settings_key_color),
                                items = colorLabels,
                                selectedIndex = colorValues.indexOf(state.keyColor).takeIf { it >= 0 } ?: 0,
                                onSelectedIndexChange = { onKeyColorChange(colorValues[it]) },
                                startAction = {
                                    Icon(
                                        imageVector = Icons.Rounded.Palette,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 6.dp),
                                        tint = colorScheme.onBackground,
                                    )
                                },
                            )

                            AnimatedVisibility(visible = state.keyColor != 0) {
                                Column {
                                    OverlayDropdownPreference(
                                        title = stringResource(R.string.settings_color_style),
                                        items = paletteStyles.map { it.name },
                                        selectedIndex = paletteStyles.indexOfFirst { it.name == state.colorStyle }
                                            .coerceAtLeast(0),
                                        onSelectedIndexChange = { onColorStyleChange(paletteStyles[it].name) },
                                        startAction = {
                                            Icon(
                                                imageVector = Icons.Rounded.Style,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 6.dp),
                                                tint = colorScheme.onBackground,
                                            )
                                        },
                                    )
                                    OverlayDropdownPreference(
                                        title = stringResource(R.string.settings_color_spec),
                                        items = colorSpecs.map { it.name },
                                        selectedIndex = colorSpecs.indexOfFirst { it.name == state.colorSpec }
                                            .coerceAtLeast(0),
                                        onSelectedIndexChange = { onColorSpecChange(colorSpecs[it].name) },
                                        startAction = {
                                            Icon(
                                                imageVector = Icons.Rounded.DesignServices,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 6.dp),
                                                tint = colorScheme.onBackground,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transition animation (Miuix side: simple toggle)
            item {
                SmallTitle(
                    text = stringResource(R.string.predictive_back_settings),
                    modifier = Modifier.padding(top = 12.dp),
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = stringResource(R.string.settings_transition_animation),
                        summary = stringResource(R.string.settings_transition_animation_desc),
                        checked = state.miuixTransitionAnimation,
                        onCheckedChange = onMiuixTransitionAnimationChange,
                    )
                }
            }
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
