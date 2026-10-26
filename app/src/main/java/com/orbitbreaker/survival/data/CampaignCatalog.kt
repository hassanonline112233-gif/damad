package com.orbitbreaker.survival.data

enum class LevelObjectiveType {
    SURVIVE_TIME,
    REACH_SCORE
}

data class CampaignLevel(
    val id: Int,
    val name: String,
    val sector: String,
    val objectiveType: LevelObjectiveType,
    val targetValue: Float, // Target seconds (if SURVIVE_TIME) or target score points (if REACH_SCORE)
    val baseRewardCredits: Int,
    val firstTimeBonusCredits: Int,
    val objectiveBonusCredits: Int,
    val excellentBonusCredits: Int,
    val baseSpeedModifier: Float,
    val spawnRateModifier: Float,
    val description: String
)

object CampaignCatalog {
    val levels: List<CampaignLevel> = listOf(
        // Levels 1-3: Introductory
        CampaignLevel(
            id = 1,
            name = "Orbital Ascent",
            sector = "Sector 1: Low Orbit",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 20f, // 20s
            baseRewardCredits = 100,
            firstTimeBonusCredits = 50,
            objectiveBonusCredits = 25,
            excellentBonusCredits = 25,
            baseSpeedModifier = 0.90f,
            spawnRateModifier = 0.85f,
            description = "Ascend safely through the lower debris layer and calibrate lateral thrusters."
        ),
        CampaignLevel(
            id = 2,
            name = "Station Perimeter",
            sector = "Sector 1: Low Orbit",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 25f,
            baseRewardCredits = 100,
            firstTimeBonusCredits = 50,
            objectiveBonusCredits = 25,
            excellentBonusCredits = 25,
            baseSpeedModifier = 0.95f,
            spawnRateModifier = 0.90f,
            description = "Navigate drifting micrometeorites patrolling the outer docking ring."
        ),
        CampaignLevel(
            id = 3,
            name = "Ion Corridor",
            sector = "Sector 1: Low Orbit",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 250f, // 250 pts
            baseRewardCredits = 110,
            firstTimeBonusCredits = 50,
            objectiveBonusCredits = 25,
            excellentBonusCredits = 25,
            baseSpeedModifier = 1.00f,
            spawnRateModifier = 0.95f,
            description = "Accumulate telemetry data by evading concentrated ion stream debris."
        ),

        // Levels 4-7: Easy to Normal
        CampaignLevel(
            id = 4,
            name = "Ring Gateway",
            sector = "Sector 2: Outer Rings",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 30f,
            baseRewardCredits = 110,
            firstTimeBonusCredits = 50,
            objectiveBonusCredits = 25,
            excellentBonusCredits = 25,
            baseSpeedModifier = 1.00f,
            spawnRateModifier = 1.00f,
            description = "Cross the boundary plane separating the inner halo from the asteroid belt."
        ),
        CampaignLevel(
            id = 5,
            name = "Silicate Drift",
            sector = "Sector 2: Outer Rings",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 320f,
            baseRewardCredits = 120,
            firstTimeBonusCredits = 60,
            objectiveBonusCredits = 30,
            excellentBonusCredits = 25,
            baseSpeedModifier = 1.05f,
            spawnRateModifier = 1.00f,
            description = "Dense silicate boulders appear in oscillating patterns across your corridor."
        ),
        CampaignLevel(
            id = 6,
            name = "Solar Flare Shadow",
            sector = "Sector 2: Outer Rings",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 35f,
            baseRewardCredits = 120,
            firstTimeBonusCredits = 60,
            objectiveBonusCredits = 30,
            excellentBonusCredits = 25,
            baseSpeedModifier = 1.08f,
            spawnRateModifier = 1.05f,
            description = "Maintain flight integrity while taking shelter beneath large planetary shadows."
        ),
        CampaignLevel(
            id = 7,
            name = "Debris Cascade",
            sector = "Sector 2: Outer Rings",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 400f,
            baseRewardCredits = 130,
            firstTimeBonusCredits = 60,
            objectiveBonusCredits = 30,
            excellentBonusCredits = 30,
            baseSpeedModifier = 1.10f,
            spawnRateModifier = 1.08f,
            description = "Chain-reaction fractures cause unpredictable lateral asteroid velocity."
        ),

        // Levels 8-12: Normal Difficulty
        CampaignLevel(
            id = 8,
            name = "Titan Grav-Well",
            sector = "Sector 3: Moon Enclave",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 40f,
            baseRewardCredits = 130,
            firstTimeBonusCredits = 70,
            objectiveBonusCredits = 35,
            excellentBonusCredits = 30,
            baseSpeedModifier = 1.12f,
            spawnRateModifier = 1.10f,
            description = "Lunar gravity draws denser clusters into your flight path."
        ),
        CampaignLevel(
            id = 9,
            name = "Magnetic Turbulence",
            sector = "Sector 3: Moon Enclave",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 480f,
            baseRewardCredits = 140,
            firstTimeBonusCredits = 70,
            objectiveBonusCredits = 35,
            excellentBonusCredits = 30,
            baseSpeedModifier = 1.15f,
            spawnRateModifier = 1.12f,
            description = "High-energy aurora fields accelerate metallic meteors downward."
        ),
        CampaignLevel(
            id = 10,
            name = "Frozen Remnants",
            sector = "Sector 3: Moon Enclave",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 45f,
            baseRewardCredits = 140,
            firstTimeBonusCredits = 70,
            objectiveBonusCredits = 35,
            excellentBonusCredits = 30,
            baseSpeedModifier = 1.18f,
            spawnRateModifier = 1.15f,
            description = "Massive ice chunks fragment into multiple smaller hazards."
        ),
        CampaignLevel(
            id = 11,
            name = "Vanguard Sector",
            sector = "Sector 3: Moon Enclave",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 550f,
            baseRewardCredits = 150,
            firstTimeBonusCredits = 80,
            objectiveBonusCredits = 40,
            excellentBonusCredits = 35,
            baseSpeedModifier = 1.20f,
            spawnRateModifier = 1.18f,
            description = "Navigate through the vanguard outpost graveyard."
        ),
        CampaignLevel(
            id = 12,
            name = "Planetary Horizon",
            sector = "Sector 3: Moon Enclave",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 50f,
            baseRewardCredits = 150,
            firstTimeBonusCredits = 80,
            objectiveBonusCredits = 40,
            excellentBonusCredits = 35,
            baseSpeedModifier = 1.22f,
            spawnRateModifier = 1.20f,
            description = "Transition between planetary orbit and the deep-space void."
        ),

        // Levels 13-16: Hard
        CampaignLevel(
            id = 13,
            name = "Nebula Breach",
            sector = "Sector 4: Deep Void",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 650f,
            baseRewardCredits = 160,
            firstTimeBonusCredits = 90,
            objectiveBonusCredits = 45,
            excellentBonusCredits = 40,
            baseSpeedModifier = 1.26f,
            spawnRateModifier = 1.24f,
            description = "Turbulent ionized clouds obscure distant asteroid trajectories."
        ),
        CampaignLevel(
            id = 14,
            name = "Hyper-Velocity Belt",
            sector = "Sector 4: Deep Void",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 55f,
            baseRewardCredits = 170,
            firstTimeBonusCredits = 90,
            objectiveBonusCredits = 45,
            excellentBonusCredits = 40,
            baseSpeedModifier = 1.30f,
            spawnRateModifier = 1.25f,
            description = "High-speed asteroid streams test extreme reflex timing."
        ),
        CampaignLevel(
            id = 15,
            name = "Dark Matter Rift",
            sector = "Sector 4: Deep Void",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 750f,
            baseRewardCredits = 180,
            firstTimeBonusCredits = 100,
            objectiveBonusCredits = 50,
            excellentBonusCredits = 40,
            baseSpeedModifier = 1.33f,
            spawnRateModifier = 1.28f,
            description = "Gravitational lensing clusters obstacles into tight corridors."
        ),
        CampaignLevel(
            id = 16,
            name = "Eclipse Singularity",
            sector = "Sector 4: Deep Void",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 60f,
            baseRewardCredits = 190,
            firstTimeBonusCredits = 100,
            objectiveBonusCredits = 50,
            excellentBonusCredits = 45,
            baseSpeedModifier = 1.36f,
            spawnRateModifier = 1.30f,
            description = "Near-complete blackout forces reliance on peripheral emerald signatures."
        ),

        // Levels 17-19: Very Hard
        CampaignLevel(
            id = 17,
            name = "Plasma Storm Alpha",
            sector = "Sector 5: Core Orbit",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 850f,
            baseRewardCredits = 200,
            firstTimeBonusCredits = 110,
            objectiveBonusCredits = 55,
            excellentBonusCredits = 45,
            baseSpeedModifier = 1.40f,
            spawnRateModifier = 1.35f,
            description = "Fierce energy torrents unleash relentless multi-size boulder waves."
        ),
        CampaignLevel(
            id = 18,
            name = "Oblivion Belt",
            sector = "Sector 5: Core Orbit",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 65f,
            baseRewardCredits = 210,
            firstTimeBonusCredits = 110,
            objectiveBonusCredits = 55,
            excellentBonusCredits = 50,
            baseSpeedModifier = 1.44f,
            spawnRateModifier = 1.38f,
            description = "The dense outer shell where only master orbital pilots survive."
        ),
        CampaignLevel(
            id = 19,
            name = "Supernova Echo",
            sector = "Sector 5: Core Orbit",
            objectiveType = LevelObjectiveType.REACH_SCORE,
            targetValue = 1000f,
            baseRewardCredits = 220,
            firstTimeBonusCredits = 120,
            objectiveBonusCredits = 60,
            excellentBonusCredits = 50,
            baseSpeedModifier = 1.48f,
            spawnRateModifier = 1.40f,
            description = "Shockwaves accelerate thousands of jagged fragments toward your vector."
        ),

        // Level 20: Memorable Final Challenge
        CampaignLevel(
            id = 20,
            name = "Orbit Breaker Zenith",
            sector = "Sector 5: Core Orbit",
            objectiveType = LevelObjectiveType.SURVIVE_TIME,
            targetValue = 75f, // 75 seconds survival
            baseRewardCredits = 350,
            firstTimeBonusCredits = 200,
            objectiveBonusCredits = 100,
            excellentBonusCredits = 100,
            baseSpeedModifier = 1.55f,
            spawnRateModifier = 1.45f,
            description = "The ultimate trial: transcend the deepest orbital anomaly and shatter the barrier."
        )
    )

    const val MAX_PLAYABLE_LEVEL = 20

    fun getLevel(id: Int): CampaignLevel? {
        return levels.find { it.id == id }
    }
}
