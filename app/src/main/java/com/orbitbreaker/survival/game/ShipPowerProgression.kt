package com.orbitbreaker.survival.game

/**
 * Specifications for the 10 distinct Ship Power levels.
 */
data class ShipPowerLevelSpec(
    val level: Int,
    val title: String,
    val damage: Float,
    val fireInterval: Float,
    val projectileCount: Int,
    val projectileSpeed: Float,
    val spreadAngleDeg: Float, // Total spread angle in degrees
    val projectileRadius: Float,
    val hasHeavyGlow: Boolean,
    val description: String
)

object ShipPowerProgression {
    const val MIN_POWER_LEVEL = 1
    const val MAX_POWER_LEVEL = 10

    // Bonus credits granted if collecting a Power Core at Level 10
    const val MAX_POWER_OVERFLOW_CREDITS = 40
    const val MAX_POWER_OVERFLOW_SCORE = 250

    val powerLevels: List<ShipPowerLevelSpec> = listOf(
        // Level 1: Starter single blaster
        ShipPowerLevelSpec(
            level = 1,
            title = "Base Plasma",
            damage = 1.0f,
            fireInterval = 0.30f,
            projectileCount = 1,
            projectileSpeed = 750f,
            spreadAngleDeg = 0f,
            projectileRadius = 4.5f,
            hasHeavyGlow = false,
            description = "Single concentrated plasma bolt."
        ),
        // Level 2: Twin light plasma
        ShipPowerLevelSpec(
            level = 2,
            title = "Twin Plasma",
            damage = 1.15f,
            fireInterval = 0.28f,
            projectileCount = 2,
            projectileSpeed = 780f,
            spreadAngleDeg = 0f,
            projectileRadius = 4.8f,
            hasHeavyGlow = false,
            description = "Parallel dual plasma cannons with increased velocity."
        ),
        // Level 3: Accelerated twin cannons
        ShipPowerLevelSpec(
            level = 3,
            title = "Overclocked Twin",
            damage = 1.30f,
            fireInterval = 0.25f,
            projectileCount = 2,
            projectileSpeed = 820f,
            spreadAngleDeg = 0f,
            projectileRadius = 5.0f,
            hasHeavyGlow = false,
            description = "Supercharged twin emitters firing higher-density rounds."
        ),
        // Level 4: Tri-stream cannons
        ShipPowerLevelSpec(
            level = 4,
            title = "Tri-Stream",
            damage = 1.45f,
            fireInterval = 0.23f,
            projectileCount = 3,
            projectileSpeed = 840f,
            spreadAngleDeg = 10f,
            projectileRadius = 5.2f,
            hasHeavyGlow = false,
            description = "Triple plasma stream with light angled outward coverage."
        ),
        // Level 5: Rapid tri-stream
        ShipPowerLevelSpec(
            level = 5,
            title = "Rapid Tri-Stream",
            damage = 1.65f,
            fireInterval = 0.21f,
            projectileCount = 3,
            projectileSpeed = 860f,
            spreadAngleDeg = 14f,
            projectileRadius = 5.5f,
            hasHeavyGlow = false,
            description = "Rapid cyclical triple burst covering wide flight vectors."
        ),
        // Level 6: Quad orbital blasters
        ShipPowerLevelSpec(
            level = 6,
            title = "Quad Array",
            damage = 1.85f,
            fireInterval = 0.19f,
            projectileCount = 4,
            projectileSpeed = 880f,
            spreadAngleDeg = 18f,
            projectileRadius = 5.8f,
            hasHeavyGlow = false,
            description = "Four synchronized orbital lances for heavy perimeter control."
        ),
        // Level 7: Supercharged quad array
        ShipPowerLevelSpec(
            level = 7,
            title = "Heavy Quad",
            damage = 2.10f,
            fireInterval = 0.17f,
            projectileCount = 4,
            projectileSpeed = 900f,
            spreadAngleDeg = 20f,
            projectileRadius = 6.0f,
            hasHeavyGlow = true,
            description = "Condensed high-energy quad battery piercing outer armor."
        ),
        // Level 8: Penta spread cannons
        ShipPowerLevelSpec(
            level = 8,
            title = "Penta Cannon",
            damage = 2.35f,
            fireInterval = 0.15f,
            projectileCount = 5,
            projectileSpeed = 920f,
            spreadAngleDeg = 26f,
            projectileRadius = 6.2f,
            hasHeavyGlow = true,
            description = "Five-point orbital fan eliminating incoming swarms with ease."
        ),
        // Level 9: Apex hyper-penta cannons
        ShipPowerLevelSpec(
            level = 9,
            title = "Apex Hyper-Array",
            damage = 2.65f,
            fireInterval = 0.13f,
            projectileCount = 5,
            projectileSpeed = 950f,
            spreadAngleDeg = 30f,
            projectileRadius = 6.6f,
            hasHeavyGlow = true,
            description = "Near-instantaneous hyper-cycling with radiant energy trails."
        ),
        // Level 10: Singularity Overdrive (MAX POWER)
        ShipPowerLevelSpec(
            level = 10,
            title = "Singularity Overdrive",
            damage = 3.20f,
            fireInterval = 0.11f,
            projectileCount = 7, // 7-barrel devastating starburst
            projectileSpeed = 990f,
            spreadAngleDeg = 36f,
            projectileRadius = 7.2f,
            hasHeavyGlow = true,
            description = "MAXIMUM POWER: Radiant emerald singularity aura and 7-stream annihilation grid."
        )
    )

    fun getSpec(level: Int): ShipPowerLevelSpec {
        val clamped = level.coerceIn(MIN_POWER_LEVEL, MAX_POWER_LEVEL)
        return powerLevels[clamped - 1]
    }
}
