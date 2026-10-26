package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color

data class CombatFeedback(
    val id: Long,
    val text: String,
    val color: Color,
    var x: Float,
    var y: Float,
    var life: Float = 1.4f,
    val maxLife: Float = 1.4f
) {
    fun update(dt: Float) {
        life -= dt
        y -= 38f * dt // gently drifts upward
    }

    val isExpired: Boolean get() = life <= 0f
    val alpha: Float get() = (life / maxLife).coerceIn(0f, 1f)
}
