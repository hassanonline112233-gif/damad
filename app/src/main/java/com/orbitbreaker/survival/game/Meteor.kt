package com.orbitbreaker.survival.game

import kotlin.math.cos
import kotlin.math.sin

enum class MeteorSize(val radius: Float, val baseSpeed: Float, val scorePoints: Int) {
    SMALL(16f, 275f, 15),
    MEDIUM(28f, 215f, 25),
    LARGE(44f, 165f, 40)
}

data class Meteor(
    val id: Long,
    var x: Float,
    var y: Float,
    var vy: Float,
    var vx: Float = 0f,
    val size: MeteorSize,
    var rotationAngle: Float = 0f,
    val rotationSpeed: Float = 45f, // degrees per second
    val seed: Int = 0, // for procedural crater / faceted outline rendering
    var health: Float = when (size) {
        MeteorSize.SMALL -> 2.0f
        MeteorSize.MEDIUM -> 4.5f
        MeteorSize.LARGE -> 8.0f
    },
    val maxHealth: Float = when (size) {
        MeteorSize.SMALL -> 2.0f
        MeteorSize.MEDIUM -> 4.5f
        MeteorSize.LARGE -> 8.0f
    }
) {
    val radius: Float get() = size.radius

    fun update(dt: Float) {
        x += vx * dt
        y += vy * dt
        rotationAngle = (rotationAngle + rotationSpeed * dt) % 360f
    }

    fun isOffScreen(screenHeight: Float): Boolean {
        return y - radius > screenHeight + 50f
    }

    /**
     * Approximate polygon outline vertices for rendering a faceted orbital asteroid
     */
    fun getPolygonPoints(): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val count = 8
        val angleStep = (2.0 * Math.PI / count).toFloat()
        val radOffset = (rotationAngle * Math.PI / 180.0).toFloat()

        for (i in 0 until count) {
            val angle = radOffset + i * angleStep
            // Deterministic jitter based on index and seed
            val jitter = 0.82f + (((seed * 37 + i * 19) % 25) / 100f)
            val r = radius * jitter
            val px = x + r * cos(angle.toDouble()).toFloat()
            val py = y + r * sin(angle.toDouble()).toFloat()
            points.add(px to py)
        }
        return points
    }
}
