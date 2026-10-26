package com.orbitbreaker.survival.game

import com.orbitbreaker.survival.data.GameDifficulty

class DifficultyController {

    fun getSpawnInterval(
        survivalTimeSeconds: Float,
        levelSpawnModifier: Float = 1.0f,
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ): Float {
        val baseInterval = when {
            survivalTimeSeconds < 15f -> 1.55f - (survivalTimeSeconds / 15f) * 0.35f
            survivalTimeSeconds < 45f -> 1.20f - ((survivalTimeSeconds - 15f) / 30f) * 0.35f
            survivalTimeSeconds < 90f -> 0.85f - ((survivalTimeSeconds - 45f) / 45f) * 0.25f
            else -> 0.58f
        }
        // Apply level modifier and difficulty mode
        return ((baseInterval * difficulty.spawnIntervalMultiplier) / levelSpawnModifier).coerceAtLeast(0.38f)
    }

    fun getSpeedMultiplier(
        survivalTimeSeconds: Float,
        levelSpeedModifier: Float = 1.0f,
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ): Float {
        val baseMultiplier = when {
            survivalTimeSeconds < 15f -> 1.0f + (survivalTimeSeconds / 15f) * 0.15f
            survivalTimeSeconds < 45f -> 1.15f + ((survivalTimeSeconds - 15f) / 30f) * 0.25f
            survivalTimeSeconds < 90f -> 1.40f + ((survivalTimeSeconds - 45f) / 45f) * 0.30f
            else -> 1.75f
        }
        return (baseMultiplier * levelSpeedModifier * difficulty.speedMultiplier).coerceAtMost(2.6f)
    }

    fun getMaxActiveMeteors(
        survivalTimeSeconds: Float,
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ): Int {
        val baseMax = when {
            survivalTimeSeconds < 15f -> 4
            survivalTimeSeconds < 45f -> 7
            survivalTimeSeconds < 90f -> 10
            else -> 14
        }
        return when (difficulty) {
            GameDifficulty.EASY -> (baseMax * 0.8f).toInt().coerceAtLeast(3)
            GameDifficulty.NORMAL -> baseMax
            GameDifficulty.HARD -> (baseMax * 1.3f).toInt().coerceAtLeast(5)
        }
    }
}
