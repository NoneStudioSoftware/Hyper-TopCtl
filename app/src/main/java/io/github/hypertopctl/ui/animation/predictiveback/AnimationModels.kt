package io.github.hypertopctl.ui.animation.predictiveback

enum class PredictiveBackAnimation(val value: String) {
    None("none"),
    AOSP("aosp"),
    Scale("scale"),
    KernelSUClassic("ksu_classic");

    companion object {
        fun fromValueOrDefault(value: String) = entries.find { it.value == value } ?: Scale
    }
}

enum class PredictiveBackExitDirection(val value: String) {
    FOLLOW_GESTURE("follow_gesture"),
    ALWAYS_RIGHT("always_right"),
    ALWAYS_LEFT("always_left");

    companion object {
        fun fromValueOrDefault(value: String) =
            entries.find { it.value == value } ?: FOLLOW_GESTURE
    }
}
