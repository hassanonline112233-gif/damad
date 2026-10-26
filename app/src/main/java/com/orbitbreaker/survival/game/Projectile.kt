package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color

data class Projectile(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float,
    val isPlayer: Boolean,
    val damage: Float = 1.0f,
    val radius: Float = 4.5f,
    val color: Color = if (isPlayer) Color(0xFF2DE2C5) else Color(0xFFFF647C),
    val angleDeg: Float = 0f,
    val isSpecial: Boolean = false
) {
    fun update(dt: Float) {
        x += vx * dt
        y += vy * dt
    }

    fun isOffScreen(screenHeight: Float, screenWidth: Float): Boolean {
        return y < -40f || y > screenHeight + 40f || x < -40f || x > screenWidth + 40f
    }
}
