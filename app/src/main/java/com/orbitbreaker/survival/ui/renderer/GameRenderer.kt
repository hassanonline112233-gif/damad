package com.orbitbreaker.survival.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.orbitbreaker.survival.data.ShipCatalog
import com.orbitbreaker.survival.data.ShipSilhouette
import com.orbitbreaker.survival.data.Spacecraft
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.ImpactCoral
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OrbitGreen
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import kotlin.math.sin

object GameRenderer {

    fun render(drawScope: DrawScope, engine: GameEngine, backgroundBitmap: androidx.compose.ui.graphics.ImageBitmap? = null) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Space background
        drawScope.drawRect(
            color = ObsidianBlack,
            size = Size(width, height)
        )

        // 1b. Render high-resolution tactical sector backdrop with smooth vertical scrolling
        backgroundBitmap?.let { bmp ->
            val scrollY = engine.backgroundScrollOffset % height
            // Primary pass
            drawScope.drawImage(
                image = bmp,
                dstOffset = androidx.compose.ui.unit.IntOffset(0, scrollY.toInt()),
                dstSize = androidx.compose.ui.unit.IntSize(width.toInt(), height.toInt()),
                alpha = 0.75f
            )
            // Seamless wrap-around pass
            drawScope.drawImage(
                image = bmp,
                dstOffset = androidx.compose.ui.unit.IntOffset(0, (scrollY - height).toInt()),
                dstSize = androidx.compose.ui.unit.IntSize(width.toInt(), height.toInt()),
                alpha = 0.75f
            )
        }

        // 1c. Radiant celestial aura / horizon lens-flare overlay
        drawScope.drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(
                    PlasmaTurquoise.copy(alpha = 0.22f),
                    OrbitGreen.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.12f),
                radius = width * 0.75f
            ),
            radius = width * 0.75f,
            center = Offset(width * 0.5f, height * 0.12f)
        )

        // 2. Stars with glistening lens flares
        for (star in engine.stars) {
            val sx = star.xRatio * width
            val sy = star.yRatio * height
            drawScope.drawCircle(
                color = StarlightMint.copy(alpha = star.alpha),
                radius = star.size,
                center = Offset(sx, sy)
            )

            // Radiant cross lens flare on prominent stars
            if (star.size > 2.6f && star.alpha > 0.65f) {
                val flareLen = star.size * 2.8f
                val flareColor = StarlightMint.copy(alpha = star.alpha * 0.55f)
                drawScope.drawLine(
                    color = flareColor,
                    start = Offset(sx - flareLen, sy),
                    end = Offset(sx + flareLen, sy),
                    strokeWidth = 1.0f
                )
                drawScope.drawLine(
                    color = flareColor,
                    start = Offset(sx, sy - flareLen),
                    end = Offset(sx, sy + flareLen),
                    strokeWidth = 1.0f
                )
            }
        }

        // 3. Orbital rings
        for (ring in engine.orbitalRings) {
            drawScope.rotate(ring.rotationAngle, pivot = Offset(width / 2f, height * 0.45f)) {
                drawOval(
                    color = ring.color,
                    topLeft = Offset(width / 2f - ring.radiusX, height * 0.45f - ring.radiusY),
                    size = Size(ring.radiusX * 2, ring.radiusY * 2),
                    style = Stroke(width = ring.strokeWidth)
                )
            }
        }

        // 4. Particles (Engine plume, impacts, shield sparks)
        for (p in engine.particleSystem.activeParticles) {
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            drawScope.drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * (0.5f + 0.5f * alpha),
                center = Offset(p.x, p.y)
            )
        }

        // 5. Meteors
        for (meteor in engine.meteors) {
            drawMeteor(drawScope, meteor)
        }

        // 6. Enemies
        for (enemy in engine.activeEnemies) {
            drawEnemy(drawScope, enemy)
        }

        // 7. Boss (if active)
        engine.activeBoss?.let { boss ->
            drawBoss(drawScope, boss)
        }

        // 8. Collectible Pickups
        for (pickup in engine.activePickups) {
            drawPickup(drawScope, pickup)
        }

        // 9. Projectiles
        for (proj in engine.activeProjectiles) {
            drawProjectile(drawScope, proj)
        }

        // 10. Rocket (if playing or ready)
        if (engine.gameState != GameState.GAME_OVER) {
            val isFlashing = engine.rocket.invincibilityTimer > 0f && ((engine.rocket.invincibilityTimer * 10).toInt() % 2 == 0)
            if (!isFlashing) {
                val ship = ShipCatalog.getShipById(engine.equippedShipId)

                // Level 10 Maximum Power Aura: Pulsing radiant orbital ring and energy petals
                if (engine.rocket.powerLevel == 10) {
                    val auraPulse = 0.85f + 0.15f * sin(engine.rocket.auraPulseTimer * 8f)
                    val auraRadius = (engine.rocket.width * 0.95f) * auraPulse

                    drawScope.drawCircle(
                        color = Color(0x332DE2C5),
                        radius = auraRadius + 8f,
                        center = Offset(engine.rocket.x, engine.rocket.y)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFF2DE2C5),
                        radius = auraRadius,
                        center = Offset(engine.rocket.x, engine.rocket.y),
                        style = Stroke(width = 2.2f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFD7FFF4),
                        radius = auraRadius * 0.65f,
                        center = Offset(engine.rocket.x, engine.rocket.y),
                        style = Stroke(width = 1.2f)
                    )
                }

                // Temporary Orbital Shield Barrier
                if (engine.powerupManager.isShieldActive) {
                    val shieldPulse = 0.92f + 0.08f * sin(engine.rocket.auraPulseTimer * 10f)
                    val shieldRadius = (engine.rocket.width * 0.85f) * shieldPulse

                    drawScope.drawCircle(
                        color = Color(0x2238BDF8),
                        radius = shieldRadius,
                        center = Offset(engine.rocket.x, engine.rocket.y)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = shieldRadius,
                        center = Offset(engine.rocket.x, engine.rocket.y),
                        style = Stroke(width = 2.4f)
                    )
                }

                drawSpacecraft(drawScope, engine.rocket.x, engine.rocket.y, engine.rocket.width, engine.rocket.height, engine.rocket.tiltAngle, ship)
            }
        }

        // 11. In-Combat Feedback Floating Particles
        for (feedback in engine.combatFeedbacks) {
            val fAlpha = feedback.alpha
            drawScope.drawCircle(
                color = feedback.color.copy(alpha = fAlpha * 0.4f),
                radius = 18f,
                center = Offset(feedback.x, feedback.y)
            )
            drawScope.drawCircle(
                color = Color.White.copy(alpha = fAlpha * 0.7f),
                radius = 6f,
                center = Offset(feedback.x, feedback.y)
            )
        }
    }

    private fun drawProjectile(drawScope: DrawScope, proj: Projectile) {
        if (proj.isSpecial) {
            // Enhanced radiant laser bolt with trailing halo
            drawScope.drawCircle(
                color = proj.color.copy(alpha = 0.35f),
                radius = proj.radius * 2.2f,
                center = Offset(proj.x, proj.y)
            )
            drawScope.drawCircle(
                color = proj.color,
                radius = proj.radius,
                center = Offset(proj.x, proj.y)
            )
            drawScope.drawCircle(
                color = Color.White,
                radius = proj.radius * 0.45f,
                center = Offset(proj.x, proj.y)
            )
        } else {
            drawScope.drawCircle(
                color = proj.color,
                radius = proj.radius,
                center = Offset(proj.x, proj.y)
            )
        }
    }

    private fun drawPickup(drawScope: DrawScope, pickup: CollectiblePickup) {
        val pulse = 0.88f + 0.12f * sin(pickup.animTimer * 6.5f)
        val r = pickup.radius * pulse

        // Outer soft glow
        drawScope.drawCircle(
            color = pickup.type.primaryColor.copy(alpha = 0.28f),
            radius = r * 1.55f,
            center = Offset(pickup.x, pickup.y)
        )

        // Geometric diamond/capsule pod body
        val p = Path().apply {
            moveTo(pickup.x, pickup.y - r)
            lineTo(pickup.x + r, pickup.y)
            lineTo(pickup.x, pickup.y + r)
            lineTo(pickup.x - r, pickup.y)
            close()
        }

        drawScope.drawPath(
            path = p,
            color = Color(0xFF0F261E),
            style = Fill
        )
        drawScope.drawPath(
            path = p,
            color = pickup.type.primaryColor,
            style = Stroke(width = 2.2f)
        )

        // Inner glowing core
        drawScope.drawCircle(
            color = pickup.type.accentColor,
            radius = r * 0.38f,
            center = Offset(pickup.x, pickup.y)
        )
    }

    private fun drawMeteor(drawScope: DrawScope, meteor: Meteor) {
        val points = meteor.getPolygonPoints()
        if (points.size < 3) return

        val path = Path().apply {
            moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) {
                lineTo(points[i].first, points[i].second)
            }
            close()
        }

        drawScope.drawPath(path = path, color = Color(0xFF192F26), style = Fill)
        drawScope.drawPath(path = path, color = OrbitGreen.copy(alpha = 0.9f), style = Stroke(width = 2.2f))

        val craterRadius = meteor.radius * 0.22f
        val craterOffset = Offset(
            meteor.x + (meteor.seed % 7 - 3) * 2f,
            meteor.y + (meteor.seed % 5 - 2) * 2f
        )
        drawScope.drawCircle(color = Color(0xFF0F1E18), radius = craterRadius, center = craterOffset)
        drawScope.drawCircle(color = OrbitGreen.copy(alpha = 0.5f), radius = craterRadius, center = craterOffset, style = Stroke(width = 1.2f))
    }

    private fun drawEnemy(drawScope: DrawScope, enemy: EnemyShip) {
        val halfW = enemy.type.width / 2f
        val halfH = enemy.type.height / 2f
        val p = Path()

        when (enemy.type) {
            EnemyType.SCOUT_DRONE -> {
                p.moveTo(enemy.x, enemy.y + halfH)
                p.lineTo(enemy.x + halfW, enemy.y - halfH * 0.4f)
                p.lineTo(enemy.x, enemy.y - halfH)
                p.lineTo(enemy.x - halfW, enemy.y - halfH * 0.4f)
                p.close()
            }
            EnemyType.METEOR_SKIMMER -> {
                p.moveTo(enemy.x, enemy.y + halfH)
                p.lineTo(enemy.x + halfW * 0.8f, enemy.y)
                p.lineTo(enemy.x + halfW, enemy.y - halfH)
                p.lineTo(enemy.x - halfW, enemy.y - halfH)
                p.lineTo(enemy.x - halfW * 0.8f, enemy.y)
                p.close()
            }
            EnemyType.ARMORED_INTERCEPTOR, EnemyType.HEAVY_DESTROYER -> {
                p.moveTo(enemy.x - halfW * 0.6f, enemy.y - halfH)
                p.lineTo(enemy.x + halfW * 0.6f, enemy.y - halfH)
                p.lineTo(enemy.x + halfW, enemy.y + halfH * 0.2f)
                p.lineTo(enemy.x, enemy.y + halfH)
                p.lineTo(enemy.x - halfW, enemy.y + halfH * 0.2f)
                p.close()
            }
            else -> {
                p.moveTo(enemy.x, enemy.y + halfH)
                p.lineTo(enemy.x + halfW, enemy.y - halfH)
                p.lineTo(enemy.x, enemy.y - halfH * 0.5f)
                p.lineTo(enemy.x - halfW, enemy.y - halfH)
                p.close()
            }
        }

        drawScope.drawPath(path = p, color = Color(0xFF261919), style = Fill)
        drawScope.drawPath(path = p, color = ImpactCoral, style = Stroke(width = 1.8f))

        // Eye core
        drawScope.drawCircle(color = AuroraWarning, radius = 3.5f, center = Offset(enemy.x, enemy.y))
    }

    private fun drawBoss(drawScope: DrawScope, boss: Boss) {
        val halfW = boss.width / 2f
        val halfH = boss.height / 2f

        val bossPath = Path().apply {
            moveTo(boss.x, boss.y + halfH)
            lineTo(boss.x + halfW * 0.5f, boss.y + halfH * 0.4f)
            lineTo(boss.x + halfW, boss.y - halfH * 0.2f)
            lineTo(boss.x + halfW * 0.8f, boss.y - halfH)
            lineTo(boss.x - halfW * 0.8f, boss.y - halfH)
            lineTo(boss.x - halfW, boss.y - halfH * 0.2f)
            lineTo(boss.x - halfW * 0.5f, boss.y + halfH * 0.4f)
            close()
        }

        val primaryCol = when (boss.bossId) {
            BossId.IRON_COLOSSUS -> Color(0xFF332014)
            BossId.VOID_HARVESTER -> Color(0xFF142433)
            BossId.ECLIPSE_WARDEN -> Color(0xFF163324)
            BossId.FINAL_SINGULARITY -> Color(0xFF2E1236)
        }
        val edgeCol = when (boss.bossId) {
            BossId.IRON_COLOSSUS -> Color(0xFFFFB86B)
            BossId.VOID_HARVESTER -> Color(0xFF2DE2C5)
            BossId.ECLIPSE_WARDEN -> Color(0xFF42F2D1)
            BossId.FINAL_SINGULARITY -> Color(0xFFFF647C)
        }

        drawScope.drawPath(path = bossPath, color = primaryCol, style = Fill)
        drawScope.drawPath(path = bossPath, color = edgeCol, style = Stroke(width = 3.0f))

        // Boss glowing core eye
        drawScope.drawCircle(
            color = if (boss.phase >= 2) ImpactCoral else edgeCol,
            radius = halfH * 0.35f,
            center = Offset(boss.x, boss.y)
        )
    }

    fun drawSpacecraft(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        tiltAngle: Float,
        ship: Spacecraft
    ) {
        val halfW = width / 2f
        val halfH = height / 2f

        drawScope.rotate(tiltAngle, pivot = Offset(x, y)) {
            val bodyPath = Path()

            when (ship.silhouette) {
                ShipSilhouette.DEFAULT_INTERCEPTOR -> {
                    bodyPath.moveTo(x, y - halfH)
                    bodyPath.lineTo(x + halfW * 0.65f, y - halfH * 0.25f)
                    bodyPath.lineTo(x + halfW, y + halfH * 0.75f)
                    bodyPath.lineTo(x + halfW * 0.55f, y + halfH * 0.65f)
                    bodyPath.lineTo(x + halfW * 0.35f, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.35f, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.55f, y + halfH * 0.65f)
                    bodyPath.lineTo(x - halfW, y + halfH * 0.75f)
                    bodyPath.lineTo(x - halfW * 0.65f, y - halfH * 0.25f)
                    bodyPath.close()
                }
                ShipSilhouette.ORBITAL_CORVETTE -> {
                    bodyPath.moveTo(x, y - halfH * 0.9f)
                    bodyPath.lineTo(x + halfW * 0.45f, y - halfH * 0.4f)
                    bodyPath.lineTo(x + halfW, y - halfH * 0.1f)
                    bodyPath.lineTo(x + halfW * 0.85f, y + halfH * 0.85f)
                    bodyPath.lineTo(x + halfW * 0.4f, y + halfH * 0.6f)
                    bodyPath.lineTo(x + halfW * 0.25f, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.25f, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.4f, y + halfH * 0.6f)
                    bodyPath.lineTo(x - halfW * 0.85f, y + halfH * 0.85f)
                    bodyPath.lineTo(x - halfW, y - halfH * 0.1f)
                    bodyPath.lineTo(x - halfW * 0.45f, y - halfH * 0.4f)
                    bodyPath.close()
                }
                ShipSilhouette.PLASMA_VANGUARD -> {
                    bodyPath.moveTo(x, y - halfH)
                    bodyPath.lineTo(x + halfW * 0.35f, y - halfH * 0.1f)
                    bodyPath.lineTo(x + halfW * 0.95f, y - halfH * 0.35f)
                    bodyPath.lineTo(x + halfW * 0.65f, y + halfH * 0.7f)
                    bodyPath.lineTo(x + halfW * 0.2f, y + halfH * 0.9f)
                    bodyPath.lineTo(x - halfW * 0.2f, y + halfH * 0.9f)
                    bodyPath.lineTo(x - halfW * 0.65f, y + halfH * 0.7f)
                    bodyPath.lineTo(x - halfW * 0.95f, y - halfH * 0.35f)
                    bodyPath.lineTo(x - halfW * 0.35f, y - halfH * 0.1f)
                    bodyPath.close()
                }
                ShipSilhouette.TITAN_FRIGATE -> {
                    bodyPath.moveTo(x, y - halfH * 0.8f)
                    bodyPath.lineTo(x + halfW * 0.75f, y - halfH * 0.2f)
                    bodyPath.lineTo(x + halfW * 0.75f, y + halfH * 0.7f)
                    bodyPath.lineTo(x + halfW * 0.45f, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.45f, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.75f, y + halfH * 0.7f)
                    bodyPath.lineTo(x - halfW * 0.75f, y - halfH * 0.2f)
                    bodyPath.close()
                }
                ShipSilhouette.VOID_ECLIPSE -> {
                    bodyPath.moveTo(x, y - halfH * 0.7f)
                    bodyPath.lineTo(x + halfW * 0.3f, y - halfH * 0.1f)
                    bodyPath.lineTo(x + halfW * 0.85f, y - halfH * 0.4f)
                    bodyPath.lineTo(x + halfW, y + halfH * 0.85f)
                    bodyPath.lineTo(x + halfW * 0.65f, y + halfH * 0.65f)
                    bodyPath.lineTo(x, y + halfH * 0.35f)
                    bodyPath.lineTo(x - halfW * 0.65f, y + halfH * 0.65f)
                    bodyPath.lineTo(x - halfW, y + halfH * 0.85f)
                    bodyPath.lineTo(x - halfW * 0.85f, y - halfH * 0.4f)
                    bodyPath.lineTo(x - halfW * 0.3f, y - halfH * 0.1f)
                    bodyPath.close()
                }
                ShipSilhouette.STELLAR_VALKYRIE -> {
                    bodyPath.moveTo(x, y - halfH)
                    bodyPath.lineTo(x + halfW * 0.4f, y - halfH * 0.3f)
                    bodyPath.lineTo(x + halfW, y + halfH * 0.5f)
                    bodyPath.lineTo(x + halfW * 0.3f, y + halfH * 0.7f)
                    bodyPath.lineTo(x, y + halfH)
                    bodyPath.lineTo(x - halfW * 0.3f, y + halfH * 0.7f)
                    bodyPath.lineTo(x - halfW, y + halfH * 0.5f)
                    bodyPath.lineTo(x - halfW * 0.4f, y - halfH * 0.3f)
                    bodyPath.close()
                }
            }

            drawPath(path = bodyPath, color = ship.primaryColor, style = Fill)
            drawPath(path = bodyPath, color = ship.accentColor, style = Stroke(width = 2.4f))

            drawCircle(color = StarlightMint, radius = halfW * 0.22f, center = Offset(x, y - halfH * 0.25f))

            drawLine(
                color = ship.accentColor,
                start = Offset(x - halfW * 0.7f, y + halfH * 0.45f),
                end = Offset(x - halfW * 0.4f, y + halfH * 0.45f),
                strokeWidth = 2f
            )
            drawLine(
                color = ship.accentColor,
                start = Offset(x + halfW * 0.7f, y + halfH * 0.45f),
                end = Offset(x + halfW * 0.4f, y + halfH * 0.45f),
                strokeWidth = 2f
            )

            drawCircle(color = ship.trailColor, radius = halfW * 0.24f, center = Offset(x, y + halfH))
        }
    }
}
