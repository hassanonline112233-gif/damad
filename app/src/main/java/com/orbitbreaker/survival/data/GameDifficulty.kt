package com.orbitbreaker.survival.data

enum class GameDifficulty(
    val label: String,
    val description: String,
    val speedMultiplier: Float,
    val spawnIntervalMultiplier: Float,
    val scoreMultiplier: Float,
    val corridorFactor: Float,
    val enemyHealthMultiplier: Float,
    val enemyFireRateMultiplier: Float,
    val bossHealthMultiplier: Float,
    val bossFireRateMultiplier: Float,
    val escalationRateMultiplier: Float
) {
    EASY(
        label = "Easy",
        description = "Relaxed pace, wider flight corridors, forgiving hazard damage, and readable boss attack telegraphs.",
        speedMultiplier = 0.82f,
        spawnIntervalMultiplier = 1.25f,
        scoreMultiplier = 0.8f,
        corridorFactor = 2.0f,
        enemyHealthMultiplier = 0.75f,
        enemyFireRateMultiplier = 0.75f,
        bossHealthMultiplier = 0.80f,
        bossFireRateMultiplier = 0.80f,
        escalationRateMultiplier = 0.75f
    ),
    NORMAL(
        label = "Normal",
        description = "Balanced default challenge. Standard enemy hulls, escalating wave formations, and intended arcade pacing.",
        speedMultiplier = 1.0f,
        spawnIntervalMultiplier = 1.0f,
        scoreMultiplier = 1.0f,
        corridorFactor = 1.6f,
        enemyHealthMultiplier = 1.0f,
        enemyFireRateMultiplier = 1.0f,
        bossHealthMultiplier = 1.0f,
        bossFireRateMultiplier = 1.0f,
        escalationRateMultiplier = 1.0f
    ),
    HARD(
        label = "Hard",
        description = "Maximum survival challenge. Accelerated hazards, aggressive enemy salvos, tighter corridors, and rapid escalation.",
        speedMultiplier = 1.28f,
        spawnIntervalMultiplier = 0.78f,
        scoreMultiplier = 1.5f,
        corridorFactor = 1.35f,
        enemyHealthMultiplier = 1.35f,
        enemyFireRateMultiplier = 1.35f,
        bossHealthMultiplier = 1.35f,
        bossFireRateMultiplier = 1.35f,
        escalationRateMultiplier = 1.35f
    )
}
