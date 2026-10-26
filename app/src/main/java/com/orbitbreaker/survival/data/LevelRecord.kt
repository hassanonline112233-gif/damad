package com.orbitbreaker.survival.data

data class LevelRewardBreakdown(
    val baseCredits: Int,
    val firstTimeBonus: Int,
    val objectiveBonus: Int,
    val excellentBonus: Int,
    val totalCreditsEarned: Int,
    val isFirstTime: Boolean,
    val earnedStars: Int
)

data class LevelRecord(
    val levelId: Int,
    val completed: Boolean = false,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val bestSurvivalTime: Float = 0f
)
