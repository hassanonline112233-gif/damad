package com.orbitbreaker.survival.data

import androidx.compose.ui.graphics.Color

enum class ShipSilhouette {
    DEFAULT_INTERCEPTOR, // Classic needle & delta wing
    ORBITAL_CORVETTE,    // Double-winged broad hull
    PLASMA_VANGUARD,     // Sleek forward-swept wing strike craft
    TITAN_FRIGATE,       // Heavy faceted armored orbital hull
    VOID_ECLIPSE,        // Dual-nacelle stealth ring ship
    STELLAR_VALKYRIE     // Diamond apex high-agility flyer
}

data class Spacecraft(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Int,
    val silhouette: ShipSilhouette,
    val primaryColor: Color,
    val accentColor: Color,
    val trailColor: Color,
    val accelerationBonus: Float, // Multiplier applied to handling, e.g. 1.0f = default
    val maxSpeedBonus: Float,     // Multiplier applied to max speed
    val description: String,
    val artRes: Int = com.orbitbreaker.survival.R.drawable.ship_art_scout
)

object ShipCatalog {
    val ships: List<Spacecraft> = listOf(
        Spacecraft(
            id = "ship_orbit_scout",
            name = "Aero-1 Scout",
            subtitle = "Standard Orbital Interceptor",
            price = 0, // Free starter ship
            silhouette = ShipSilhouette.DEFAULT_INTERCEPTOR,
            primaryColor = Color(0xFF13362A),
            accentColor = Color(0xFF2DE2C5),
            trailColor = Color(0xFF2DE2C5),
            accelerationBonus = 1.0f,
            maxSpeedBonus = 1.0f,
            description = "Lightweight fleet patrol craft with balanced thrusters and dependable handling.",
            artRes = com.orbitbreaker.survival.R.drawable.ship_art_scout
        ),
        Spacecraft(
            id = "ship_mint_corvette",
            name = "Vanguard Mint",
            subtitle = "Tactical Escort Vessel",
            price = 250,
            silhouette = ShipSilhouette.ORBITAL_CORVETTE,
            primaryColor = Color(0xFF104235),
            accentColor = Color(0xFF42F2D1),
            trailColor = Color(0xFF42F2D1),
            accelerationBonus = 1.08f,
            maxSpeedBonus = 1.05f,
            description = "Equipped with reinforced wing baffles for enhanced stability inside dense planetary rings.",
            artRes = com.orbitbreaker.survival.R.drawable.ship_art_corvette
        ),
        Spacecraft(
            id = "ship_plasma_striker",
            name = "Plasma Striker",
            subtitle = "High-Agility Swept Wing",
            price = 600,
            silhouette = ShipSilhouette.PLASMA_VANGUARD,
            primaryColor = Color(0xFF0C2B22),
            accentColor = Color(0xFF2DE2C5),
            trailColor = Color(0xFFD7FFF4),
            accelerationBonus = 1.15f,
            maxSpeedBonus = 1.10f,
            description = "Forward-swept aerofoils produce ultra-sharp directional response during evasive maneuvers.",
            artRes = com.orbitbreaker.survival.R.drawable.ship_art_striker
        ),
        Spacecraft(
            id = "ship_titan_breaker",
            name = "Titan Aegis",
            subtitle = "Heavy Armor Ring Breaker",
            price = 1100,
            silhouette = ShipSilhouette.TITAN_FRIGATE,
            primaryColor = Color(0xFF152923),
            accentColor = Color(0xFFFFB86B),
            trailColor = Color(0xFFFFB86B),
            accelerationBonus = 1.02f,
            maxSpeedBonus = 1.18f,
            description = "Heavy reinforced alloy hull tailored for deep-space asteroid belt penetrations.",
            artRes = com.orbitbreaker.survival.R.drawable.ship_art_titan
        ),
        Spacecraft(
            id = "ship_void_eclipse",
            name = "Eclipse Twin",
            subtitle = "Dual-Nacelle Stealth Vessel",
            price = 1800,
            silhouette = ShipSilhouette.VOID_ECLIPSE,
            primaryColor = Color(0xFF0A1F18),
            accentColor = Color(0xFF4EECD3),
            trailColor = Color(0xFF2DE2C5),
            accelerationBonus = 1.22f,
            maxSpeedBonus = 1.15f,
            description = "Twin ion propulsion nacelles offering rapid micro-adjustments and top-tier lateral velocity.",
            artRes = com.orbitbreaker.survival.R.drawable.ship_art_eclipse
        ),
        Spacecraft(
            id = "ship_stellar_valkyrie",
            name = "Stellar Valkyrie",
            subtitle = "Apex Orbital Flagship",
            price = 3000,
            silhouette = ShipSilhouette.STELLAR_VALKYRIE,
            primaryColor = Color(0xFF183B32),
            accentColor = Color(0xFFD7FFF4),
            trailColor = Color(0xFFD7FFF4),
            accelerationBonus = 1.28f,
            maxSpeedBonus = 1.22f,
            description = "The pinnacle of orbital engineering: radiant starlight thrusters and unmatched responsiveness.",
            artRes = com.orbitbreaker.survival.R.drawable.ship_art_valkyrie
        )
    )

    fun getShipById(id: String): Spacecraft {
        return ships.find { it.id == id } ?: ships.first()
    }

    fun findShipById(id: String): Spacecraft? {
        return ships.find { it.id == id }
    }
}
