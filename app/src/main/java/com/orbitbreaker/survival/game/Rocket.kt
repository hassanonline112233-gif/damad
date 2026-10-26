package com.orbitbreaker.survival.game

enum class GameState {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class Rocket(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 44f, // dp
    val height: Float = 66f, // dp
    var tiltAngle: Float = 0f, // degrees
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    var powerLevel: Int = 1,
    var invincibilityTimer: Float = 0f,
    var fireTimer: Float = 0f,
    var auraPulseTimer: Float = 0f,
    var hitFlashTimer: Float = 0f
) {
    /**
     * Smoothly moves the rocket towards target coordinate (hold-and-drag).
     * Uses fluid exponential smoothing for zero-lag 60fps tracking.
     */
    fun moveToTarget(
        targetX: Float,
        targetY: Float,
        dt: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        val halfW = width / 2f
        val halfH = height / 2f

        // Clamp target position inside screen boundaries
        val clampedTargetX = targetX.coerceIn(halfW + 4f, screenWidth - halfW - 4f)
        val clampedTargetY = targetY.coerceIn(halfH + 20f, screenHeight - halfH - 24f)

        val prevX = x
        val prevY = y

        // Responsive, buttery-smooth lerp (blend factor scales with frame dt)
        val blend = (24f * dt).coerceIn(0f, 1f)
        x += (clampedTargetX - x) * blend
        y += (clampedTargetY - y) * blend

        // Calculate apparent velocity for tilt angle and visual effects
        if (dt > 0.0001f) {
            vx = (x - prevX) / dt
            vy = (y - prevY) / dt
        }

        // Tilt smoothly in the direction of horizontal movement
        val maxTiltSpd = 800f
        val targetTilt = (vx / maxTiltSpd).coerceIn(-1f, 1f) * 24f
        tiltAngle += (targetTilt - tiltAngle) * (14f * dt).coerceIn(0f, 1f)
    }

    /**
     * Inertial update when the player is not actively holding/dragging the ship.
     * Dampens horizontal and vertical velocity cleanly.
     */
    fun update(
        dt: Float,
        targetDir: Float, // -1 for left, +1 for right, 0 for release
        acceleration: Float,
        maxSpeed: Float,
        screenWidth: Float,
        screenHeight: Float = 1920f
    ) {
        val halfW = width / 2f
        val halfH = height / 2f

        if (targetDir != 0f) {
            vx += targetDir * acceleration * dt
            if (vx > maxSpeed) vx = maxSpeed
            if (vx < -maxSpeed) vx = -maxSpeed
        } else {
            val damping = 6.5f
            if (vx > 0f) {
                vx -= vx * damping * dt
                if (vx < 5f) vx = 0f
            } else if (vx < 0f) {
                vx -= vx * damping * dt
                if (vx > -5f) vx = 0f
            }
        }

        // Dampen vertical velocity if any remains from a drag release
        val vDamping = 6.5f
        if (vy > 0f) {
            vy -= vy * vDamping * dt
            if (vy < 5f) vy = 0f
        } else if (vy < 0f) {
            vy -= vy * vDamping * dt
            if (vy > -5f) vy = 0f
        }

        x += vx * dt
        y += vy * dt

        // Horizontal boundary clamp
        if (x - halfW < 0f) {
            x = halfW
            if (vx < 0f) vx = 0f
        } else if (x + halfW > screenWidth) {
            x = screenWidth - halfW
            if (vx > 0f) vx = 0f
        }

        // Vertical boundary clamp
        if (y - halfH < 20f) {
            y = halfH + 20f
            if (vy < 0f) vy = 0f
        } else if (y + halfH > screenHeight - 24f) {
            y = screenHeight - halfH - 24f
            if (vy > 0f) vy = 0f
        }

        // Visual tilt calculation: proportional to velocity
        val targetTilt = (vx / maxSpeed.coerceAtLeast(1f)) * 22f
        tiltAngle += (targetTilt - tiltAngle) * (12f * dt).coerceIn(0f, 1f)
    }

    fun reset(startX: Float, startY: Float) {
        x = startX
        y = startY
        vx = 0f
        vy = 0f
        tiltAngle = 0f
        health = maxHealth
        powerLevel = 1
        invincibilityTimer = 0f
        fireTimer = 0f
        auraPulseTimer = 0f
        hitFlashTimer = 0f
    }
}
