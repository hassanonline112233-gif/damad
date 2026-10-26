package com.orbitbreaker.survival.ui

import com.orbitbreaker.survival.data.CampaignLevel
import com.orbitbreaker.survival.data.GameDifficulty

sealed class Screen {
    object Splash : Screen()
    object OrbitStation : Screen()
    data class SurvivalFlight(val levelId: Int?) : Screen() // null = Endless mode, int = Campaign level
    data class FlightDebrief(
        val level: CampaignLevel?,
        val isSuccess: Boolean,
        val score: Int,
        val survivalTime: Float,
        val earnedCredits: Int,
        val stars: Int,
        val isNewRecord: Boolean,
        val difficulty: GameDifficulty = GameDifficulty.NORMAL
    ) : Screen()
    object CampaignMap : Screen()
    object UnlimitedMode : Screen()
    object ShipHangar : Screen()
    object Records : Screen()
    object SystemsBay : Screen()
}
