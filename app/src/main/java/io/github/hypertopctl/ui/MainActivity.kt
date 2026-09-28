package io.github.hypertopctl.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hypertopctl.ui.animation.predictiveback.NoPredictiveBackTransition
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackAnimation
import io.github.hypertopctl.ui.animation.predictiveback.installerNavTransition
import io.github.hypertopctl.ui.component.bottombar.BottomBar
import io.github.hypertopctl.ui.component.bottombar.BottomBarDestination
import io.github.hypertopctl.ui.navigation.LocalNavigator
import io.github.hypertopctl.ui.overscroll.StretchOverscrollCompensationState
import io.github.hypertopctl.ui.overscroll.rememberCustomOverscrollFactory
import io.github.hypertopctl.ui.navigation.Navigator
import io.github.hypertopctl.ui.navigation.Route
import io.github.hypertopctl.ui.screen.about.AboutScreen
import io.github.hypertopctl.ui.screen.applist.AppListScreen
import io.github.hypertopctl.ui.screen.home.HomeScreen
import io.github.hypertopctl.ui.screen.settings.SettingsScreen
import io.github.hypertopctl.ui.screen.theme.ThemeScreen
import io.github.hypertopctl.ui.screen.topctl.TopCtlScreen
import io.github.hypertopctl.ui.theme.HyperTopCtlTheme
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode
import io.github.hypertopctl.ui.util.rememberDeviceCornerRadius
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

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
            val overscrollCompensationState = remember { StretchOverscrollCompensationState() }
            val overscrollFactory = rememberCustomOverscrollFactory(
                compensationState = overscrollCompensationState,
            )
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { mainPagerState.syncPage() }
            }

            val backStack = rememberNavBackStack<Route>(Route.Main)
            val navigator = remember(backStack) { Navigator(backStack) }
            val onBack = remember(navigator) { { navigator.pop() } }
            val transition = remember(
                uiMode,
                state.miuixTransitionAnimation,
                state.predictiveBackAnimation,
                state.predictiveBackExitDirection,
            ) {
                when (uiMode) {
                    UiMode.Miuix -> if (state.miuixTransitionAnimation) {
                        NavTransitions.MiuixDefault
                    } else {
                        NoPredictiveBackTransition
                    }

                    UiMode.Material -> installerNavTransition(
                        state.predictiveBackAnimation,
                        state.predictiveBackExitDirection,
                    )
                }
            }
            val navCornerRadius = rememberDeviceCornerRadius(defaultRadius = 0.dp)
            val roundAllCorners =
                uiMode == UiMode.Material && (
                    state.predictiveBackAnimation == PredictiveBackAnimation.AOSP ||
                        state.predictiveBackAnimation == PredictiveBackAnimation.Scale ||
                        state.predictiveBackAnimation == PredictiveBackAnimation.KernelSUClassic
                )
            val enablePredictiveBack = when (uiMode) {
                UiMode.Miuix -> state.miuixTransitionAnimation
                UiMode.Material -> state.predictiveBackAnimation != PredictiveBackAnimation.None
            }

            val swipeBackDirection = if (enablePredictiveBack) {
                when (LocalLayoutDirection.current) {
                    LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
                    LayoutDirection.Ltr -> NavSwipeDirection.LeftToRight
                }
            } else {
                NavSwipeDirection.None
            }

            CompositionLocalProvider(
                LocalOverscrollFactory provides if (uiMode == UiMode.Material) overscrollFactory else null,
                LocalUiMode provides uiMode,
                LocalMainPagerState provides mainPagerState,
                LocalNavigator provides navigator,
            ) {
                HyperTopCtlTheme(uiMode = uiMode, settings = state.toThemeSettings()) {
                    val navBackdropColor = when (uiMode) {
                        UiMode.Miuix -> colorScheme.surface
                        UiMode.Material -> MaterialTheme.colorScheme.surfaceContainer
                    }
                    val effects = remember(navCornerRadius, roundAllCorners, navBackdropColor) {
                        NavDisplayEffects(
                            enableCornerClip = true,
                            cornerClipRadius = if (roundAllCorners && navCornerRadius <= 0.dp) {
                                32.dp
                            } else if (navCornerRadius > 0.dp) {
                                navCornerRadius
                            } else {
                                28.dp
                            },
                            cornerClipMode = if (roundAllCorners) {
                                NavCornerClipMode.All
                            } else {
                                NavCornerClipMode.Leading
                            },
                            dimAmount = 0.5f,
                            backdropColor = navBackdropColor,
                            blockInputDuringTransition = false,
                        )
                    }
                    NavDisplay(
                        backStack = backStack,
                        onBack = onBack,
                        transition = transition,
                        effects = effects,
                    ) {
                        entry<Route.Main>(swipeDismiss = NavSwipeDirection.None) {
                            MainScaffold(
                                bottomBar = { BottomBar() },
                                sideBar = { BottomBar(isBottomBar = false) },
                            ) { innerPadding ->
                                HorizontalPager(
                                    state = pagerState,
                                    beyondViewportPageCount = 3,
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
                                            onOpenTheme = { navigator.push(Route.Theme) },
                                            onOpenAbout = { navigator.push(Route.About) },
                                        )
                                    }
                                }
                            }
                        }

                        entry<Route.Theme>(swipeDismiss = swipeBackDirection) {
                            ThemeScreen(
                                state = state,
                                onBack = onBack,
                                onUiFrameworkChange = vm::setUiFramework,
                                onThemeModeChange = vm::setThemeMode,
                                onMiuixMonetChange = vm::setMiuixMonet,
                                onDynamicColorChange = vm::setDynamicColor,
                                onKeyColorChange = vm::setKeyColor,
                                onColorStyleChange = vm::setColorStyle,
                                onColorSpecChange = vm::setColorSpec,
                                onMiuixTransitionAnimationChange = vm::setMiuixTransitionAnimation,
                                onPredictiveBackAnimationChange = vm::setPredictiveBackAnimation,
                                onPredictiveBackExitDirectionChange = vm::setPredictiveBackExitDirection,
                            )
                        }

                        entry<Route.About>(swipeDismiss = swipeBackDirection) {
                            AboutScreen(onBack = onBack)
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
    sideBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> top.yukonga.miuix.kmp.basic.Scaffold(
            bottomBar = bottomBar,
            containerColor = colorScheme.surface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            content = content,
        )

        UiMode.Material -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useBottomBar = maxWidth < maxHeight || maxHeight / maxWidth > 1.4f
            if (useBottomBar) {
                androidx.compose.material3.Scaffold(
                    bottomBar = bottomBar,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    content = content,
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    sideBar()
                    androidx.compose.material3.Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        content = content,
                    )
                }
            }
        }
    }
}
