package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color

enum class PickupType(
    val title: String,
    val notificationText: String,
    val primaryColor: Color,
    val accentColor: Color,
    val durationSeconds: Float = 0f,
    val weight: Float // Higher weight = more frequent drop
) {
    POWER_CORE(
        title = "Power Core",
        notificationText = "POWER UP +1",
        primaryColor = Color(0xFF2DE2C5), // Plasma Turquoise
        accentColor = Color(0xFFD7FFF4), // Starlight Mint
        weight = 22f
    ),
    SHIELD(
        title = "Orbital Shield",
        notificationText = "SHIELD ACTIVATED",
        primaryColor = Color(0xFF38BDF8), // Sky Blue / Cyan
        accentColor = Color(0xFFBAE6FD),
        durationSeconds = 8.0f,
        weight = 12f
    ),
    HEALTH_REPAIR(
        title = "Hull Repair",
        notificationText = "HEALTH RESTORED",
        primaryColor = Color(0xFF4ADE80), // Emerald Green
        accentColor = Color(0xFFDCFCE7),
        weight = 14f
    ),
    DOUBLE_COINS(
        title = "Double Coins",
        notificationText = "DOUBLE COINS ACTIVE",
        primaryColor = Color(0xFFFACC15), // Gold / Amber
        accentColor = Color(0xFFFEF08A),
        durationSeconds = 12.0f,
        weight = 12f
    ),
    RAPID_FIRE(
        title = "Rapid Fire",
        notificationText = "RAPID FIRE ACTIVE",
        primaryColor = Color(0xFFFF7A00), // Plasma Orange
        accentColor = Color(0xFFFFEDD5),
        durationSeconds = 10.0f,
        weight = 12f
    ),
    SPREAD_SHOT(
        title = "Spread Shot",
        notificationText = "SPREAD SHOT ACTIVE",
        primaryColor = Color(0xFFA855F7), // Hyper Violet
        accentColor = Color(0xFFF3E8FF),
        durationSeconds = 10.0f,
        weight = 10f
    ),
    HEAVY_SHOT(
        title = "Heavy Shot",
        notificationText = "HEAVY SHOT ACTIVE",
        primaryColor = Color(0xFFFF4D4D), // High Energy Red
        accentColor = Color(0xFFFFE4E6),
        durationSeconds = 10.0f,
        weight = 8f
    ),
    SCREEN_CLEAR_BOMB(
        title = "Nova Bomb",
        notificationText = "NOVA BOMB DETONATED",
        primaryColor = Color(0xFFFFE600), // Radiant Nova Yellow
        accentColor = Color(0xFFFFFFFF),
        weight = 5f
    ),
    COIN_CACHE(
        title = "Coin Cache",
        notificationText = "+50 CREDITS CACHE",
        primaryColor = Color(0xFF22C55E), // Credit Green
        accentColor = Color(0xFF86EFAC),
        weight = 16f
    )
}

data class CollectiblePickup(
    val id: Long,
    val type: PickupType,
    var x: Float,
    var y: Float,
    var vy: Float = 140f,
    val radius: Float = 16f,
    var animTimer: Float = 0f,
    var isCollected: Boolean = false
) {
    fun update(dt: Float) {
        y += vy * dt
        animTimer += dt
    }

    fun isOffScreen(screenHeight: Float): Boolean {
        return y > screenHeight + 40f
    }
}
