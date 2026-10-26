package com.orbitbreaker.survival.game

import kotlin.math.floor

class ScoreManager {
    var currentScore: Int = 0
        private set

    var survivalTimeSeconds: Float = 0f
        private set

    private var accumulatedSeconds: Float = 0f
    private var lastWholeSecond: Int = 0

    fun reset() {
        currentScore = 0
        survivalTimeSeconds = 0f
        accumulatedSeconds = 0f
        lastWholeSecond = 0
    }

    /**
     * Updates active survival time and score: 10 points per elapsed survival second.
     * Guaranteed no points added during pause because dt is 0f when paused.
     */
    fun update(dt: Float) {
        survivalTimeSeconds += dt
        accumulatedSeconds += dt

        val currentWholeSecond = floor(accumulatedSeconds).toInt()
        if (currentWholeSecond > lastWholeSecond) {
            val deltaSec = currentWholeSecond - lastWholeSecond
            currentScore += deltaSec * 10
            lastWholeSecond = currentWholeSecond
        }
    }

    fun addBonusPoints(points: Int) {
        currentScore += points
    }
}
