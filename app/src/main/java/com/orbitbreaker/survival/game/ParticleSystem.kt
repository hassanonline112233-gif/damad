package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 down to 0.0
    val maxLife: Float,
    val size: Float,
    val color: Color
)

data class Star(
    val xRatio: Float,
    var yRatio: Float,
    val size: Float,
    val speedFactor: Float,
    val alpha: Float
)

data class OrbitalRing(
    var rotationAngle: Float,
    val rotationSpeed: Float,
    val radiusX: Float,
    val radiusY: Float,
    val tiltAngle: Float,
    val strokeWidth: Float,
    val color: Color
)

class ParticleSystem {
    private val maxParticles = 90
    private val particles = mutableListOf<Particle>()
    private val random = Random(42)

    val activeParticles: List<Particle> get() = particles

    fun clear() {
        particles.clear()
    }

    /**
     * Spawns rocket engine flame/trail particles
     */
    fun spawnEngineTrail(rocketX: Float, rocketY: Float, reducedEffects: Boolean) {
        if (reducedEffects) return
        if (particles.size >= maxParticles) return

        // Emerald / mint engine plume
        val count = 2
        for (i in 0 until count) {
            val spreadX = (random.nextFloat() - 0.5f) * 12f
            val vx = spreadX * 4f
            val vy = 120f + random.nextFloat() * 80f
            val life = 0.35f + random.nextFloat() * 0.25f

            val color = if (random.nextBoolean()) {
                Color(0xFF2DE2C5) // Plasma Turquoise
            } else {
                Color(0xFFD7FFF4) // Starlight Mint
            }

            particles.add(
                Particle(
                    x = rocketX + spreadX,
                    y = rocketY + 28f, // Engine nozzle position
                    vx = vx,
                    vy = vy,
                    life = life,
                    maxLife = life,
                    size = 2.5f + random.nextFloat() * 2.5f,
                    color = color
                )
            )
        }
    }

    /**
     * Spawns impact burst when rocket collides with meteor
     */
    fun spawnImpactBurst(x: Float, y: Float, reducedEffects: Boolean) {
        val count = if (reducedEffects) 14 else 35
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 100f + random.nextFloat() * 260f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val life = 0.5f + random.nextFloat() * 0.4f

            val color = when (random.nextInt(3)) {
                0 -> Color(0xFFFF647C) // Impact Coral
                1 -> Color(0xFFFFB86B) // Aurora Warning
                else -> Color(0xFF2DE2C5) // Turquoise sparks
            }

            if (particles.size < maxParticles) {
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = vx,
                        vy = vy,
                        life = life,
                        maxLife = life,
                        size = 3.5f + random.nextFloat() * 3.5f,
                        color = color
                    )
                )
            }
        }
    }

    /**
     * Spawns shield hit/absorption barrier pulse particles
     */
    fun spawnShieldAbsorbBurst(x: Float, y: Float, reducedEffects: Boolean) {
        val count = if (reducedEffects) 10 else 24
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 80f + random.nextFloat() * 160f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val life = 0.4f + random.nextFloat() * 0.3f
            val col = if (random.nextBoolean()) Color(0xFF38BDF8) else Color(0xFFBAE6FD)

            if (particles.size < maxParticles) {
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = vx,
                        vy = vy,
                        life = life,
                        maxLife = life,
                        size = 3.0f + random.nextFloat() * 2.5f,
                        color = col
                    )
                )
            }
        }
    }

    /**
     * Spawns collectible pickup sparkle effect
     */
    fun spawnPickupSparkle(x: Float, y: Float, color: Color, reducedEffects: Boolean) {
        val count = if (reducedEffects) 8 else 18
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 60f + random.nextFloat() * 140f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val life = 0.45f + random.nextFloat() * 0.35f

            if (particles.size < maxParticles) {
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = vx,
                        vy = vy,
                        life = life,
                        maxLife = life,
                        size = 3.2f + random.nextFloat() * 2.0f,
                        color = color
                    )
                )
            }
        }
    }

    /**
     * Spawns radiant screen clear nova shockwave particles
     */
    fun spawnNovaBombEffect(screenWidth: Float, screenHeight: Float, reducedEffects: Boolean) {
        val count = if (reducedEffects) 25 else 60
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 250f + random.nextFloat() * 450f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val life = 0.7f + random.nextFloat() * 0.4f
            val color = when (random.nextInt(3)) {
                0 -> Color(0xFFFFE600)
                1 -> Color(0xFF2DE2C5)
                else -> Color(0xFFFFFFFF)
            }

            if (particles.size < maxParticles) {
                particles.add(
                    Particle(
                        x = centerX,
                        y = centerY,
                        vx = vx,
                        vy = vy,
                        life = life,
                        maxLife = life,
                        size = 5.0f + random.nextFloat() * 4.0f,
                        color = color
                    )
                )
            }
        }
    }

    fun update(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life -= dt
            if (p.life <= 0f) {
                iterator.remove()
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
            }
        }
    }
}
