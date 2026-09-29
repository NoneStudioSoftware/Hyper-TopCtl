// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 ReSukiSU contributors
package io.github.hypertopctl.ui.animation.predictiveback

import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitions

fun installerNavTransition(
    animation: PredictiveBackAnimation,
    exitDirection: PredictiveBackExitDirection,
): NavTransition = when (animation) {
    PredictiveBackAnimation.None -> NavTransitions.MiuixDefault
    PredictiveBackAnimation.AOSP -> AospNavTransition
    PredictiveBackAnimation.Scale -> scaleNavTransition(exitDirection)
    PredictiveBackAnimation.KernelSUClassic -> ClassicNavTransition
}
