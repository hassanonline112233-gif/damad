package com.orbitbreaker.survival.data

import android.content.Context
import android.content.SharedPreferences

sealed class PurchaseResult {
    data class Success(val ship: Spacecraft, val remainingBalance: Int) : PurchaseResult()
    data class InsufficientFunds(val ship: Spacecraft, val required: Int, val current: Int, val shortfall: Int) : PurchaseResult()
    data class AlreadyOwned(val ship: Spacecraft) : PurchaseResult()
    object InvalidShip : PurchaseResult()
}

class LocalGameStorage(
    context: Context? = null,
    injectedPrefs: SharedPreferences? = null
) {
    private val prefs: SharedPreferences =
        injectedPrefs ?: context?.getSharedPreferences("orbit_breaker_prefs_v2", Context.MODE_PRIVATE)
        ?: error("Either context or injectedPrefs must be provided")

    companion object {
        private const val KEY_CREDITS = "key_credits"
        private const val KEY_HIGHEST_UNLOCKED_LEVEL = "key_highest_unlocked_level"
        private const val KEY_EQUIPPED_SHIP_ID = "key_equipped_ship_id"
        private const val KEY_OWNED_SHIPS = "key_owned_ships"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
        private const val KEY_REDUCED_EFFECTS = "key_reduced_effects"
        private const val KEY_SENSITIVITY = "key_sensitivity"
        private const val KEY_SELECTED_DIFFICULTY = "key_selected_difficulty"

        // Overall best record
        private const val KEY_GLOBAL_BEST_SCORE = "key_global_best_score"
        private const val KEY_GLOBAL_BEST_TIME = "key_global_best_time"
    }

    // --- CREDITS ECONOMY ---
    fun getCredits(): Int = prefs.getInt(KEY_CREDITS, 150) // 150 starting credits

    fun addCredits(amount: Int): Int {
        if (amount <= 0) return getCredits()
        val current = getCredits()
        val updated = (current.toLong() + amount).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        prefs.edit().putInt(KEY_CREDITS, updated).apply()
        return updated
    }

    fun spendCredits(amount: Int): Boolean {
        if (amount <= 0) return true
        val current = getCredits()
        if (current >= amount) {
            prefs.edit().putInt(KEY_CREDITS, current - amount).apply()
            return true
        }
        return false
    }

    // --- CAMPAIGN LEVEL PROGRESSION (DIFFICULTY-SCOPED) ---
    private fun getUnlockedLevelKey(difficulty: GameDifficulty): String =
        "key_highest_unlocked_level_${difficulty.name}"

    fun getHighestUnlockedLevel(difficulty: GameDifficulty = getSelectedDifficulty()): Int {
        val key = getUnlockedLevelKey(difficulty)
        if (!prefs.contains(key) && difficulty == GameDifficulty.NORMAL) {
            return prefs.getInt(KEY_HIGHEST_UNLOCKED_LEVEL, 1)
        }
        return prefs.getInt(key, 1)
    }

    fun unlockNextLevel(completedLevelId: Int, difficulty: GameDifficulty = getSelectedDifficulty()) {
        val nextLevel = (completedLevelId + 1).coerceAtMost(CampaignCatalog.MAX_PLAYABLE_LEVEL)
        val currentUnlocked = getHighestUnlockedLevel(difficulty)
        if (nextLevel > currentUnlocked) {
            val editor = prefs.edit()
            editor.putInt(getUnlockedLevelKey(difficulty), nextLevel)
            if (difficulty == GameDifficulty.NORMAL) {
                editor.putInt(KEY_HIGHEST_UNLOCKED_LEVEL, nextLevel)
            }
            editor.apply()
        }
    }

    fun isLevelCompleted(levelId: Int, difficulty: GameDifficulty = getSelectedDifficulty()): Boolean {
        val key = "level_${levelId}_${difficulty.name}_completed"
        if (!prefs.contains(key) && difficulty == GameDifficulty.NORMAL) {
            return prefs.getBoolean("level_${levelId}_completed", false)
        }
        return prefs.getBoolean(key, false)
    }

    fun getLevelStars(levelId: Int, difficulty: GameDifficulty = getSelectedDifficulty()): Int {
        val key = "level_${levelId}_${difficulty.name}_stars"
        if (!prefs.contains(key) && difficulty == GameDifficulty.NORMAL) {
            return prefs.getInt("level_${levelId}_stars", 0)
        }
        return prefs.getInt(key, 0)
    }

    fun getLevelBestScore(levelId: Int, difficulty: GameDifficulty = getSelectedDifficulty()): Int =
        prefs.getInt("level_${levelId}_${difficulty.name}_score", 0)

    fun getCompletedLevelCount(difficulty: GameDifficulty = getSelectedDifficulty()): Int {
        var count = 0
        for (i in 1..CampaignCatalog.MAX_PLAYABLE_LEVEL) {
            if (isLevelCompleted(i, difficulty)) count++
        }
        return count
    }

    fun getTotalStarsEarned(difficulty: GameDifficulty = getSelectedDifficulty()): Int {
        var stars = 0
        for (i in 1..CampaignCatalog.MAX_PLAYABLE_LEVEL) {
            stars += getLevelStars(i, difficulty)
        }
        return stars
    }

    fun saveLevelCompletion(
        levelId: Int,
        score: Int,
        survivalTime: Float,
        stars: Int,
        difficulty: GameDifficulty
    ): Boolean {
        val editor = prefs.edit()
        val isFirstTime = !isLevelCompleted(levelId, difficulty)

        editor.putBoolean("level_${levelId}_${difficulty.name}_completed", true)

        val currentStars = getLevelStars(levelId, difficulty)
        if (stars > currentStars) {
            editor.putInt("level_${levelId}_${difficulty.name}_stars", stars)
        }

        val currentScore = getLevelBestScore(levelId, difficulty)
        if (score > currentScore) {
            editor.putInt("level_${levelId}_${difficulty.name}_score", score)
        }

        if (difficulty == GameDifficulty.NORMAL) {
            editor.putBoolean("level_${levelId}_completed", true)
            if (stars > prefs.getInt("level_${levelId}_stars", 0)) {
                editor.putInt("level_${levelId}_stars", stars)
            }
        }

        editor.apply()
        unlockNextLevel(levelId, difficulty)
        saveBestScore(score)
        saveBestSurvivalTime(survivalTime)

        return isFirstTime
    }

    fun resetCampaignProgress(difficulty: GameDifficulty? = null) {
        val editor = prefs.edit()
        val difficultiesToReset = if (difficulty != null) listOf(difficulty) else GameDifficulty.values().toList()
        for (d in difficultiesToReset) {
            editor.putInt(getUnlockedLevelKey(d), 1)
            for (i in 1..CampaignCatalog.MAX_PLAYABLE_LEVEL) {
                editor.remove("level_${i}_${d.name}_completed")
                editor.remove("level_${i}_${d.name}_stars")
                editor.remove("level_${i}_${d.name}_score")
            }
        }
        if (difficulty == null || difficulty == GameDifficulty.NORMAL) {
            editor.putInt(KEY_HIGHEST_UNLOCKED_LEVEL, 1)
            for (i in 1..CampaignCatalog.MAX_PLAYABLE_LEVEL) {
                editor.remove("level_${i}_completed")
                editor.remove("level_${i}_stars")
            }
        }
        editor.apply()
    }

    // --- SPACESHIP HANGAR & OWNERSHIP ---
    fun getEquippedShipId(): String {
        val defaultId = ShipCatalog.ships.first().id
        val saved = prefs.getString(KEY_EQUIPPED_SHIP_ID, defaultId) ?: defaultId
        if (saved != defaultId && !isShipOwned(saved)) {
            setEquippedShipId(defaultId)
            return defaultId
        }
        return saved
    }

    fun setEquippedShipId(shipId: String) {
        prefs.edit().putString(KEY_EQUIPPED_SHIP_ID, shipId).apply()
    }

    fun getOwnedShipIds(): Set<String> {
        val defaultId = ShipCatalog.ships.first().id
        val set = prefs.getStringSet(KEY_OWNED_SHIPS, null)
        return if (set.isNullOrEmpty()) {
            setOf(defaultId) // Default starter ship is always owned
        } else {
            set + defaultId
        }
    }

    fun unlockShip(shipId: String) {
        val current = getOwnedShipIds().toMutableSet()
        current.add(shipId)
        prefs.edit().putStringSet(KEY_OWNED_SHIPS, current).apply()
    }

    fun isShipOwned(shipId: String): Boolean {
        if (shipId == ShipCatalog.ships.first().id) return true
        return getOwnedShipIds().contains(shipId)
    }

    @Synchronized
    fun purchaseShip(shipId: String): PurchaseResult {
        val ship = ShipCatalog.findShipById(shipId) ?: return PurchaseResult.InvalidShip
        if (isShipOwned(shipId)) {
            return PurchaseResult.AlreadyOwned(ship)
        }
        val current = getCredits()
        if (current < ship.price) {
            return PurchaseResult.InsufficientFunds(
                ship = ship,
                required = ship.price,
                current = current,
                shortfall = ship.price - current
            )
        }

        val newBalance = current - ship.price
        val owned = getOwnedShipIds().toMutableSet()
        owned.add(shipId)

        val editor = prefs.edit()
        editor.putInt(KEY_CREDITS, newBalance)
        editor.putStringSet(KEY_OWNED_SHIPS, owned)
        editor.apply()

        return PurchaseResult.Success(ship, newBalance)
    }

    @Synchronized
    fun equipShip(shipId: String): Boolean {
        val ship = ShipCatalog.findShipById(shipId) ?: return false
        if (!isShipOwned(shipId)) return false
        setEquippedShipId(shipId)
        return true
    }

    // --- UNLIMITED / INFINITE SURVIVAL RECORDS ---
    fun getUnlimitedBestScore(difficulty: GameDifficulty): Int =
        prefs.getInt("unlimited_${difficulty.name}_best_score", 0)

    fun getUnlimitedBestTime(difficulty: GameDifficulty): Float =
        prefs.getFloat("unlimited_${difficulty.name}_best_time", 0f)

    fun saveUnlimitedRecord(score: Int, survivalTime: Float, difficulty: GameDifficulty): Boolean {
        var isNewRecord = false
        val currentScore = getUnlimitedBestScore(difficulty)
        val currentTime = getUnlimitedBestTime(difficulty)
        val editor = prefs.edit()

        if (score > currentScore) {
            editor.putInt("unlimited_${difficulty.name}_best_score", score)
            isNewRecord = true
        }
        if (survivalTime > currentTime) {
            editor.putFloat("unlimited_${difficulty.name}_best_time", survivalTime)
            isNewRecord = true
        }
        editor.apply()
        saveBestScore(score)
        saveBestSurvivalTime(survivalTime)
        return isNewRecord
    }

    fun resetUnlimitedRecords() {
        val editor = prefs.edit()
        for (d in GameDifficulty.values()) {
            editor.remove("unlimited_${d.name}_best_score")
            editor.remove("unlimited_${d.name}_best_time")
        }
        editor.apply()
    }

    // --- GLOBAL BEST RECORDS ---
    fun getBestScore(): Int = prefs.getInt(KEY_GLOBAL_BEST_SCORE, 0)

    fun saveBestScore(score: Int) {
        val current = getBestScore()
        if (score > current) {
            prefs.edit().putInt(KEY_GLOBAL_BEST_SCORE, score).apply()
        }
    }

    fun getBestSurvivalTime(): Float = prefs.getFloat(KEY_GLOBAL_BEST_TIME, 0f)

    fun saveBestSurvivalTime(seconds: Float) {
        val current = getBestSurvivalTime()
        if (seconds > current) {
            prefs.edit().putFloat(KEY_GLOBAL_BEST_TIME, seconds).apply()
        }
    }

    fun resetBestScore() {
        prefs.edit()
            .putInt(KEY_GLOBAL_BEST_SCORE, 0)
            .putFloat(KEY_GLOBAL_BEST_TIME, 0f)
            .apply()
    }

    // --- SETTINGS ---
    fun getSelectedDifficulty(): GameDifficulty {
        val name = prefs.getString(KEY_SELECTED_DIFFICULTY, GameDifficulty.NORMAL.name)
        return try {
            GameDifficulty.valueOf(name ?: GameDifficulty.NORMAL.name)
        } catch (_: Exception) {
            GameDifficulty.NORMAL
        }
    }

    fun saveSelectedDifficulty(difficulty: GameDifficulty) {
        prefs.edit().putString(KEY_SELECTED_DIFFICULTY, difficulty.name).apply()
    }

    fun getSettings(): PlayerSettings {
        val sound = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val music = prefs.getBoolean("key_music_enabled", true)
        val musicVol = prefs.getFloat("key_music_volume", 0.8f)
        val sfxVol = prefs.getFloat("key_sfx_volume", 0.9f)
        val vibration = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        val reduced = prefs.getBoolean(KEY_REDUCED_EFFECTS, false)
        val sensitivityStr = prefs.getString(KEY_SENSITIVITY, SteeringSensitivity.NORMAL.name)
        val sensitivity = try {
            SteeringSensitivity.valueOf(sensitivityStr ?: SteeringSensitivity.NORMAL.name)
        } catch (_: Exception) {
            SteeringSensitivity.NORMAL
        }

        return PlayerSettings(
            soundEnabled = sound,
            musicEnabled = music,
            musicVolume = musicVol,
            sfxVolume = sfxVol,
            vibrationEnabled = vibration,
            reducedVisualEffects = reduced,
            steeringSensitivity = sensitivity
        )
    }

    fun saveSettings(settings: PlayerSettings) {
        prefs.edit()
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            .putBoolean("key_music_enabled", settings.musicEnabled)
            .putFloat("key_music_volume", settings.musicVolume)
            .putFloat("key_sfx_volume", settings.sfxVolume)
            .putBoolean(KEY_VIBRATION_ENABLED, settings.vibrationEnabled)
            .putBoolean(KEY_REDUCED_EFFECTS, settings.reducedVisualEffects)
            .putString(KEY_SENSITIVITY, settings.steeringSensitivity.name)
            .apply()
    }
}
