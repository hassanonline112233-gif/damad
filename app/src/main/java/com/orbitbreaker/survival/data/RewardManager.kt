package com.orbitbreaker.survival.data

object RewardManager {

    /**
     * Calculates the exact reward breakdown for completing a level:
     * - Base credits
     * - First-time completion bonus
     * - Objective completion bonus
     * - Excellent performance bonus (e.g. survival > 1.3x target or score > 1.3x target)
     * - Star rating: 1 to 3 stars
     */
    fun calculateRewards(
        level: CampaignLevel,
        score: Int,
        survivalTime: Float,
        isFirstTime: Boolean
    ): LevelRewardBreakdown {
        val baseCredits = level.baseRewardCredits
        val firstTimeBonus = if (isFirstTime) level.firstTimeBonusCredits else 0

        // Determine if performance qualified for bonuses
        var objectiveMet = false
        var excellentPerformance = false
        var stars = 1

        when (level.objectiveType) {
            LevelObjectiveType.SURVIVE_TIME -> {
                if (survivalTime >= level.targetValue) {
                    objectiveMet = true
                    stars = 2
                }
                if (survivalTime >= level.targetValue * 1.25f || score >= level.targetValue * 14) {
                    excellentPerformance = true
                    stars = 3
                }
            }
            LevelObjectiveType.REACH_SCORE -> {
                if (score >= level.targetValue) {
                    objectiveMet = true
                    stars = 2
                }
                if (score >= level.targetValue * 1.3f) {
                    excellentPerformance = true
                    stars = 3
                }
            }
        }

        val objectiveBonus = if (objectiveMet) level.objectiveBonusCredits else 0
        val excellentBonus = if (excellentPerformance) level.excellentBonusCredits else 0

        val totalCredits = baseCredits + firstTimeBonus + objectiveBonus + excellentBonus

        return LevelRewardBreakdown(
            baseCredits = baseCredits,
            firstTimeBonus = firstTimeBonus,
            objectiveBonus = objectiveBonus,
            excellentBonus = excellentBonus,
            totalCreditsEarned = totalCredits,
            isFirstTime = isFirstTime,
            earnedStars = stars
        )
    }
}
