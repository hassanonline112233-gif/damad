package com.orbitbreaker.survival.data

enum class SteeringSensitivity(val label: String, val acceleration: Float, val maxSpeed: Float) {
    LOW("Low", 1100f, 380f),
    NORMAL("Normal", 1500f, 460f),
    HIGH("High", 1950f, 540f)
}

data class PlayerSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 0.9f,
    val vibrationEnabled: Boolean = true,
    val reducedVisualEffects: Boolean = false,
    val steeringSensitivity: SteeringSensitivity = SteeringSensitivity.NORMAL
)
