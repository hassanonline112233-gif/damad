package com.orbitbreaker.survival.game

/**
 * Manages active temporary powerups (Shield, Double Coins, Rapid Fire, Spread Shot, Heavy Shot).
 */
class ActivePowerupManager {
    var shieldTimer: Float = 0f
        private set
    var doubleCoinsTimer: Float = 0f
        private set
    var rapidFireTimer: Float = 0f
        private set
    var spreadShotTimer: Float = 0f
        private set
    var heavyShotTimer: Float = 0f
        private set

    val isShieldActive: Boolean get() = shieldTimer > 0f
    val isDoubleCoinsActive: Boolean get() = doubleCoinsTimer > 0f
    val isRapidFireActive: Boolean get() = rapidFireTimer > 0f
    val isSpreadShotActive: Boolean get() = spreadShotTimer > 0f
    val isHeavyShotActive: Boolean get() = heavyShotTimer > 0f

    fun activate(type: PickupType) {
        when (type) {
            PickupType.SHIELD -> shieldTimer = type.durationSeconds
            PickupType.DOUBLE_COINS -> doubleCoinsTimer = type.durationSeconds
            PickupType.RAPID_FIRE -> rapidFireTimer = type.durationSeconds
            PickupType.SPREAD_SHOT -> spreadShotTimer = type.durationSeconds
            PickupType.HEAVY_SHOT -> heavyShotTimer = type.durationSeconds
            else -> {}
        }
    }

    fun consumeShield(): Boolean {
        if (shieldTimer > 0f) {
            shieldTimer = 0f
            return true
        }
        return false
    }

    fun update(dt: Float) {
        if (shieldTimer > 0f) shieldTimer = (shieldTimer - dt).coerceAtLeast(0f)
        if (doubleCoinsTimer > 0f) doubleCoinsTimer = (doubleCoinsTimer - dt).coerceAtLeast(0f)
        if (rapidFireTimer > 0f) rapidFireTimer = (rapidFireTimer - dt).coerceAtLeast(0f)
        if (spreadShotTimer > 0f) spreadShotTimer = (spreadShotTimer - dt).coerceAtLeast(0f)
        if (heavyShotTimer > 0f) heavyShotTimer = (heavyShotTimer - dt).coerceAtLeast(0f)
    }

    fun reset() {
        shieldTimer = 0f
        doubleCoinsTimer = 0f
        rapidFireTimer = 0f
        spreadShotTimer = 0f
        heavyShotTimer = 0f
    }
}
