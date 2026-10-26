package com.orbitbreaker.survival.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.game.GameEngine
import com.orbitbreaker.survival.ui.screens.CampaignMapScreen
import com.orbitbreaker.survival.ui.screens.FlightDebriefScreen
import com.orbitbreaker.survival.ui.screens.OrbitStationScreen
import com.orbitbreaker.survival.ui.screens.ShipHangarScreen
import com.orbitbreaker.survival.ui.screens.SplashScreen
import com.orbitbreaker.survival.ui.screens.SurvivalFlightScreen
import com.orbitbreaker.survival.ui.screens.SystemsBayScreen

@Composable
fun GameNavigation(
    storage: LocalGameStorage,
    engine: GameEngine
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    Crossfade(
        targetState = currentScreen,
        modifier = Modifier.fillMaxSize(),
        label = "navigation_crossfade"
    ) { screen ->
        when (screen) {
            is Screen.Splash -> {
                SplashScreen(
                    audioManager = engine.audioManager,
                    settings = engine.settings,
                    onSplashFinished = {
                        engine.audioManager.playMusic(com.orbitbreaker.survival.game.MusicTrack.LOBBY, engine.settings)
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
            is Screen.OrbitStation -> {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    engine.audioManager.playMusic(com.orbitbreaker.survival.game.MusicTrack.LOBBY, engine.settings)
                }
                OrbitStationScreen(
                    storage = storage,
                    onLaunchFlight = {
                        engine.audioManager.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, engine.settings)
                        val diff = storage.getSelectedDifficulty()
                        val currentLevel = storage.getHighestUnlockedLevel(diff)
                        currentScreen = Screen.SurvivalFlight(currentLevel)
                    },
                    onOpenCampaign = {
                        engine.audioManager.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, engine.settings)
                        currentScreen = Screen.CampaignMap
                    },
                    onOpenUnlimited = {
                        engine.audioManager.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, engine.settings)
                        currentScreen = Screen.UnlimitedMode
                    },
                    onOpenHangar = {
                        engine.audioManager.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, engine.settings)
                        currentScreen = Screen.ShipHangar
                    },
                    onOpenRecords = {
                        engine.audioManager.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, engine.settings)
                        currentScreen = Screen.Records
                    },
                    onOpenSystemsBay = {
                        engine.audioManager.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, engine.settings)
                        currentScreen = Screen.SystemsBay
                    }
                )
            }
            is Screen.Records -> {
                com.orbitbreaker.survival.ui.screens.RecordsScreen(
                    storage = storage,
                    onBackToStation = {
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
            is Screen.UnlimitedMode -> {
                com.orbitbreaker.survival.ui.screens.UnlimitedModeScreen(
                    storage = storage,
                    onStartUnlimitedRun = { difficulty ->
                        currentScreen = Screen.SurvivalFlight(null)
                    },
                    onBackToStation = {
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
            is Screen.CampaignMap -> {
                CampaignMapScreen(
                    storage = storage,
                    onSelectLevel = { levelId ->
                        currentScreen = Screen.SurvivalFlight(levelId)
                    },
                    onBackToStation = {
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
            is Screen.ShipHangar -> {
                ShipHangarScreen(
                    storage = storage,
                    audioManager = engine.audioManager,
                    settings = engine.settings,
                    onOpenCampaign = {
                        engine.refreshSettings()
                        currentScreen = Screen.CampaignMap
                    },
                    onBackToStation = {
                        engine.refreshSettings()
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
            is Screen.SurvivalFlight -> {
                SurvivalFlightScreen(
                    levelId = screen.levelId,
                    engine = engine,
                    onReturnToStation = {
                        currentScreen = Screen.OrbitStation
                    },
                    onLevelSuccess = { lvl, finalScore, finalTime, totalCredits, stars ->
                        currentScreen = Screen.FlightDebrief(
                            level = lvl,
                            isSuccess = true,
                            score = finalScore,
                            survivalTime = finalTime,
                            earnedCredits = totalCredits,
                            stars = stars,
                            isNewRecord = false,
                            difficulty = engine.currentDifficulty
                        )
                    },
                    onGameOver = { lvl, finalScore, survivalTime, isNewRecord ->
                        currentScreen = Screen.FlightDebrief(
                            level = lvl,
                            isSuccess = false,
                            score = finalScore,
                            survivalTime = survivalTime,
                            earnedCredits = 0,
                            stars = 0,
                            isNewRecord = isNewRecord,
                            difficulty = engine.currentDifficulty
                        )
                    }
                )
            }
            is Screen.FlightDebrief -> {
                FlightDebriefScreen(
                    level = screen.level,
                    isSuccess = screen.isSuccess,
                    score = screen.score,
                    survivalTime = screen.survivalTime,
                    earnedCredits = screen.earnedCredits,
                    stars = screen.stars,
                    isNewRecord = screen.isNewRecord,
                    difficulty = screen.difficulty,
                    storage = storage,
                    onFlyAgain = {
                        currentScreen = Screen.SurvivalFlight(screen.level?.id)
                    },
                    onNextLevel = { nextId ->
                        currentScreen = Screen.SurvivalFlight(nextId)
                    },
                    onCampaignMap = {
                        currentScreen = Screen.CampaignMap
                    },
                    onReturnToStation = {
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
            is Screen.SystemsBay -> {
                SystemsBayScreen(
                    storage = storage,
                    audioManager = engine.audioManager,
                    onSettingsChanged = {
                        engine.refreshSettings()
                    },
                    onBackToStation = {
                        engine.refreshSettings()
                        currentScreen = Screen.OrbitStation
                    }
                )
            }
        }
    }
}
