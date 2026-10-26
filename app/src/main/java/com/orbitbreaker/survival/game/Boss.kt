package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

enum class BossId(val levelId: Int, val title: String, val subtitle: String, val maxHealth: Float, val artRes: Int) {
    IRON_COLOSSUS(5, "THE IRON COLOSSUS", "Heavy Armored Planetary Dreadnought", 28f, com.orbitbreaker.survival.R.drawable.boss_art_iron_colossus),
    VOID_HARVESTER(10, "THE VOID HARVESTER", "Singularity Energy Siphon", 45f, com.orbitbreaker.survival.R.drawable.boss_art_void_harvester),
    ECLIPSE_WARDEN(15, "THE ECLIPSE WARDEN", "Multi-Phase Guardian Array", 65f, com.orbitbreaker.survival.R.drawable.boss_art_eclipse_warden),
    FINAL_SINGULARITY(20, "THE FINAL SINGULARITY", "Zenith Orbital Apex Leviathan", 95f, com.orbitbreaker.survival.R.drawable.boss_art_final_singularity)
}

data class Boss(
    val bossId: BossId,
    var x: Float,
    var y: Float,
    var health: Float,
    val maxHealth: Float,
    var phase: Int = 1,
    val width: Float = 140f,
    val height: Float = 88f,
    var attackTimer: Float = 0f,
    var stateTime: Float = 0f,
    var isDefeated: Boolean = false
) {
    val healthRatio: Float get() = (health / maxHealth).coerceIn(0f, 1f)

    fun takeDamage(damage: Float): Boolean {
        health = (health - damage).coerceAtLeast(0f)
        if (health <= maxHealth * 0.5f && phase == 1) {
            phase = 2
        }
        if (bossId == BossId.FINAL_SINGULARITY && health <= maxHealth * 0.25f && phase == 2) {
            phase = 3
        }
        if (health <= 0f) {
            isDefeated = true
            return true
        }
        return false
    }

    fun update(dt: Float, screenWidth: Float, difficultyRateMultiplier: Float = 1.0f): List<Projectile> {
        stateTime += dt
        attackTimer += dt

        // Hover smoothly across upper arena
        val targetY = 140f
        if (y < targetY) {
            y += (targetY - y) * 2.0f * dt
        }

        // Horizontal sway
        val swaySpeed = if (phase >= 2) 2.2f else 1.4f
        val swayWidth = (screenWidth * 0.30f).coerceAtLeast(60f)
        x = (screenWidth / 2f) + sin(stateTime * swaySpeed) * swayWidth

        val firedProjectiles = mutableListOf<Projectile>()
        val baseInterval = when (bossId) {
            BossId.IRON_COLOSSUS -> if (phase == 1) 1.8f else 1.2f
            BossId.VOID_HARVESTER -> if (phase == 1) 1.5f else 1.0f
            BossId.ECLIPSE_WARDEN -> if (phase == 1) 1.3f else 0.85f
            BossId.FINAL_SINGULARITY -> if (phase == 1) 1.1f else if (phase == 2) 0.8f else 0.55f
        }
        val interval = baseInterval / difficultyRateMultiplier

        if (attackTimer >= interval) {
            attackTimer = 0f
            when (bossId) {
                BossId.IRON_COLOSSUS -> {
                    // Spread 3 heavy plasma cannon shells
                    firedProjectiles.add(Projectile(x = x - 35f, y = y + height / 2f, vx = -45f, vy = 280f, isPlayer = false, radius = 6.5f, color = Color(0xFFFFB86B)))
                    firedProjectiles.add(Projectile(x = x, y = y + height / 2f, vx = 0f, vy = 310f, isPlayer = false, radius = 7.5f, color = Color(0xFFFF647C)))
                    firedProjectiles.add(Projectile(x = x + 35f, y = y + height / 2f, vx = 45f, vy = 280f, isPlayer = false, radius = 6.5f, color = Color(0xFFFFB86B)))
                }
                BossId.VOID_HARVESTER -> {
                    // Radial 5-shot circular sweep
                    val angles = listOf(-40f, -20f, 0f, 20f, 40f)
                    for (ang in angles) {
                        val rad = Math.toRadians(ang.toDouble())
                        val vx = sin(rad).toFloat() * 260f
                        val vy = cos(rad).toFloat() * 260f
                        firedProjectiles.add(Projectile(x = x, y = y + height / 2f, vx = vx, vy = vy, isPlayer = false, radius = 5.5f, color = Color(0xFFD7FFF4)))
                    }
                }
                BossId.ECLIPSE_WARDEN -> {
                    // Twin barrage + targeted bolt
                    firedProjectiles.add(Projectile(x = x - 45f, y = y + height / 2f, vx = -20f, vy = 340f, isPlayer = false, color = Color(0xFF2DE2C5)))
                    firedProjectiles.add(Projectile(x = x + 45f, y = y + height / 2f, vx = 20f, vy = 340f, isPlayer = false, color = Color(0xFF2DE2C5)))
                    if (phase == 2) {
                        firedProjectiles.add(Projectile(x = x, y = y + height / 2f, vx = 0f, vy = 400f, isPlayer = false, radius = 7f, color = Color(0xFFFF647C)))
                    }
                }
                BossId.FINAL_SINGULARITY -> {
                    // Zenith apocalypse barrage
                    val count = if (phase == 3) 7 else 5
                    val step = 140f / (count - 1)
                    for (i in 0 until count) {
                        val ang = -70f + i * step
                        val rad = Math.toRadians(ang.toDouble())
                        val spd = if (phase == 3) 320f else 270f
                        firedProjectiles.add(Projectile(
                            x = x,
                            y = y + height / 2f,
                            vx = sin(rad).toFloat() * spd,
                            vy = cos(rad).toFloat() * spd,
                            isPlayer = false,
                            radius = 6.0f,
                            color = if (i % 2 == 0) Color(0xFF2DE2C5) else Color(0xFFFF647C)
                        ))
                    }
                }
            }
        }

        return firedProjectiles
    }
}
