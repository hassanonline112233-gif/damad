package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class EnemyType(
    val displayName: String,
    val relativeHealth: Float,
    val speedMultiplier: Float,
    val baseScore: Int,
    val creditValue: Int,
    val width: Float,
    val height: Float
) {
    SCOUT_DRONE("Scout Drone", 1.8f, 0.90f, 25, 2, 28f, 28f),
    METEOR_SKIMMER("Meteor Skimmer", 1.5f, 1.45f, 45, 3, 24f, 32f),
    ARMORED_INTERCEPTOR("Armored Interceptor", 4.2f, 0.72f, 80, 5, 38f, 42f),
    STRIKER_FIGHTER("Striker Fighter", 2.6f, 1.15f, 100, 6, 32f, 36f),
    HEAVY_DESTROYER("Heavy Destroyer", 8.5f, 0.45f, 220, 15, 54f, 58f),
    SWARM_CARRIER("Swarm Carrier", 4.0f, 0.80f, 120, 8, 44f, 46f),
    ELITE_HUNTER("Elite Hunter", 5.8f, 1.25f, 250, 20, 36f, 44f)
}

data class EnemyShip(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var health: Float,
    val maxHealth: Float,
    var vx: Float = 0f,
    var vy: Float,
    var fireTimer: Float = 0f,
    var fireInterval: Float = 2.0f,
    var stateTime: Float = 0f,
    val seed: Int = 0,
    val tier: Int = 1
) {
    fun update(dt: Float): List<Projectile> {
        stateTime += dt
        y += vy * dt

        // Specific movement behaviors
        when (type) {
            EnemyType.METEOR_SKIMMER -> {
                // High frequency zig-zag evasive sweep
                x += sin(stateTime * 4.2f) * 110f * dt
            }
            EnemyType.STRIKER_FIGHTER -> {
                // Deliberate lateral bank
                x += cos(stateTime * 2.2f) * 70f * dt
            }
            EnemyType.ELITE_HUNTER -> {
                // Complex figure-8 hunt trajectory
                x += sin(stateTime * 2.8f) * 90f * dt
            }
            EnemyType.HEAVY_DESTROYER -> {
                // Slow imposing drift
                x += sin(stateTime * 1.2f) * 40f * dt
            }
            else -> {
                x += vx * dt
            }
        }

        // Firing logic
        val spawnedShots = mutableListOf<Projectile>()
        fireTimer += dt
        if (fireTimer >= fireInterval && y > 20f) {
            fireTimer = 0f
            val halfH = type.height / 2f
            when (type) {
                EnemyType.STRIKER_FIGHTER -> {
                    // Twin precision laser bolts
                    spawnedShots.add(Projectile(x = x - 8f, y = y + halfH, vy = 340f, isPlayer = false, damage = 1.2f))
                    spawnedShots.add(Projectile(x = x + 8f, y = y + halfH, vy = 340f, isPlayer = false, damage = 1.2f))
                    if (tier >= 3) {
                        spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 360f, vx = -35f, isPlayer = false, damage = 1.0f))
                        spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 360f, vx = 35f, isPlayer = false, damage = 1.0f))
                    }
                }
                EnemyType.ARMORED_INTERCEPTOR -> {
                    // Heavy dual cannons + angled flank defense
                    spawnedShots.add(Projectile(x = x - 12f, y = y + halfH, vy = 280f, isPlayer = false, damage = 1.5f))
                    spawnedShots.add(Projectile(x = x + 12f, y = y + halfH, vy = 280f, isPlayer = false, damage = 1.5f))
                    if (tier >= 2) {
                        spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 260f, vx = -50f, isPlayer = false, damage = 1.2f))
                        spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 260f, vx = 50f, isPlayer = false, damage = 1.2f))
                    }
                }
                EnemyType.HEAVY_DESTROYER -> {
                    // 3-way or 5-way destructive barrage
                    spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 260f, vx = -65f, isPlayer = false, damage = 1.8f))
                    spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 290f, vx = 0f, isPlayer = false, damage = 2.0f))
                    spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 260f, vx = 65f, isPlayer = false, damage = 1.8f))
                    if (tier >= 4) {
                        spawnedShots.add(Projectile(x = x - 20f, y = y + halfH, vy = 240f, vx = -110f, isPlayer = false, damage = 1.5f))
                        spawnedShots.add(Projectile(x = x + 20f, y = y + halfH, vy = 240f, vx = 110f, isPlayer = false, damage = 1.5f))
                    }
                }
                EnemyType.ELITE_HUNTER -> {
                    // Rapid hyper-velocity energy burst
                    spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 390f, isPlayer = false, damage = 2.2f))
                    if (tier >= 2) {
                        spawnedShots.add(Projectile(x = x - 10f, y = y + halfH, vy = 370f, vx = -30f, isPlayer = false, damage = 1.5f))
                        spawnedShots.add(Projectile(x = x + 10f, y = y + halfH, vy = 370f, vx = 30f, isPlayer = false, damage = 1.5f))
                    }
                }
                EnemyType.SWARM_CARRIER -> {
                    // Dual spread pulses
                    spawnedShots.add(Projectile(x = x - 14f, y = y + halfH, vy = 270f, vx = -25f, isPlayer = false, damage = 1.2f))
                    spawnedShots.add(Projectile(x = x + 14f, y = y + halfH, vy = 270f, vx = 25f, isPlayer = false, damage = 1.2f))
                }
                else -> {
                    if (type == EnemyType.SCOUT_DRONE && Random.nextFloat() < 0.65f) {
                        spawnedShots.add(Projectile(x = x, y = y + halfH, vy = 260f, isPlayer = false, damage = 1.0f))
                    }
                }
            }
        }

        return spawnedShots
    }
}
