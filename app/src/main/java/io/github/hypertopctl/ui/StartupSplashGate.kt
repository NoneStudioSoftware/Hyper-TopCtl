package io.github.hypertopctl.ui

import io.github.hypertopctl.StartupState

/**
 * Splash gate ported from ReSukiSU's `StartupSplashGate`: the system splash stays on screen
 * while startup is in progress and fades out as soon as the UI is ready.
 */
internal fun shouldKeepStartupSplash(
    startupState: StartupState,
): Boolean = when (startupState) {
    StartupState.Loading -> true
    StartupState.Ready -> false
}
