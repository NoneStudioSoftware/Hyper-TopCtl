package io.github.hypertopctl.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hypertopctl.ui.component.bottombar.BottomBar
import io.github.hypertopctl.ui.component.bottombar.BottomBarDestination
import io.github.hypertopctl.ui.screen.about.AboutScreen
import io.github.hypertopctl.ui.screen.applist.AppListScreen
import io.github.hypertopctl.ui.screen.home.HomeScreen
import io.github.hypertopctl.ui.screen.settings.SettingsScreen
import io.github.hypertopctl.ui.screen.theme.ThemeScreen
import io.github.hypertopctl.ui.screen.topctl.TopCtlScreen
import io.github.hypertopctl.ui.theme.HyperTopCtlTheme
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

private enum class SubScreen { None, Theme, About }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val uiMode = UiMode.fromValue(state.uiFramework)

            val pagerState = rememberPagerState(pageCount = { BottomBarDestination.PAGE_COUNT })
            val mainPagerState = rememberMainPagerState(pagerState)
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { mainPagerState.syncPage() }
            }

            var subScreen by remember { mutableStateOf(SubScreen.None) }

            CompositionLocalProvider(
                LocalUiMode provides uiMode,
                LocalMainPagerState provides mainPagerState,
            ) {
                HyperTopCtlTheme(uiMode = uiMode, settings = state.toThemeSettings()) {
                    BackHandler(enabled = subScreen != SubScreen.None) { subScreen = SubScreen.None }
                    AnimatedContent(
                        targetState = subScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "root_nav",
                    ) { current ->
                        when (current) {
                            SubScreen.Theme -> ThemeScreen(
                                state = state,
                                onBack = { subScreen = SubScreen.None },
                                onUiFrameworkChange = vm::setUiFramework,
                                onThemeModeChange = vm::setThemeMode,
                                onMiuixMonetChange = vm::setMiuixMonet,
                                onDynamicColorChange = vm::setDynamicColor,
                                onKeyColorChange = vm::setKeyColor,
                                onColorStyleChange = vm::setColorStyle,
                                onColorSpecChange = vm::setColorSpec,
                            )

                            SubScreen.About -> AboutScreen(onBack = { subScreen = SubScreen.None })

                            SubScreen.None -> MainScaffold(bottomBar = { BottomBar() }) { innerPadding ->
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = innerPadding.calculateBottomPadding()),
                                ) { page ->
                                    when (BottomBarDestination.entries[page]) {
                                        BottomBarDestination.Home -> HomeScreen(state)
                                        BottomBarDestination.TopCtl -> TopCtlScreen(
                                            state = state,
                                            onGlobalEnabledChange = vm::setGlobalEnabled,
                                            onListModeChange = vm::setListMode,
                                        )

                                        BottomBarDestination.AppList -> AppListScreen(state)
                                        BottomBarDestination.Settings -> SettingsScreen(
                                            state = state,
                                            onOpenTheme = { subScreen = SubScreen.Theme },
                                            onOpenAbout = { subScreen = SubScreen.About },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Provides a framework-appropriate [Scaffold] hosting the shared bottom bar and pager content.
 * Miuix and Material each have their own Scaffold; dispatching here keeps insets/behavior native.
 */
@Composable
private fun MainScaffold(
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> top.yukonga.miuix.kmp.basic.Scaffold(
            bottomBar = bottomBar,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            content = content,
        )

        UiMode.Material -> androidx.compose.material3.Scaffold(
            bottomBar = bottomBar,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            content = content,
        )
    }
}
