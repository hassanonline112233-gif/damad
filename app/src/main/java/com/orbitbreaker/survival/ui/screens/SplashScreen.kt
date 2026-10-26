package com.orbitbreaker.survival.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.ShipCatalog
import com.orbitbreaker.survival.game.GameRenderer
import com.orbitbreaker.survival.ui.theme.DeepForest
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OrbitGreen
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    audioManager: com.orbitbreaker.survival.game.AudioManager? = null,
    settings: com.orbitbreaker.survival.data.PlayerSettings? = null,
    onSplashFinished: () -> Unit
) {
    val spaceAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.4f) }
    val ringRotation = remember { Animatable(0f) }
    val shipOffset = remember { Animatable(200f) }
    val shipAlpha = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        settings?.let { s ->
            audioManager?.playSound(com.orbitbreaker.survival.game.SoundEvent.SPLASH, s, priority = 3)
        }

        // 1. Fade in space environment
        spaceAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))

        // 2. Expand and rotate orbital rings
        ringScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        ringRotation.animateTo(180f, animationSpec = tween(1200, easing = FastOutSlowInEasing))

        // 3. Reveal spacecraft with smooth ascension entrance
        shipAlpha.animateTo(1f, animationSpec = tween(400))
        shipOffset.animateTo(0f, animationSpec = tween(800, easing = FastOutSlowInEasing))

        // 4. Reveal game logo
        logoAlpha.animateTo(1f, animationSpec = tween(500))

        // 5. Brief authentic pause then transition smoothly to main menu (total ~2.4s)
        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ObsidianBlack, DeepForest, ObsidianBlack)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Orbital Rings & Ship Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cx = size.width / 2f
            val cy = size.height * 0.42f
            val s = ringScale.value

            // Outer orbital ring
            rotate(ringRotation.value, pivot = Offset(cx, cy)) {
                drawOval(
                    color = PlasmaTurquoise.copy(alpha = 0.45f * spaceAlpha.value),
                    topLeft = Offset(cx - size.width * 0.40f * s, cy - size.width * 0.18f * s),
                    size = Size(size.width * 0.80f * s, size.width * 0.36f * s),
                    style = Stroke(width = 2.8f)
                )
            }

            // Inner counter ring
            rotate(-ringRotation.value * 1.5f, pivot = Offset(cx, cy)) {
                drawOval(
                    color = OrbitGreen.copy(alpha = 0.6f * spaceAlpha.value),
                    topLeft = Offset(cx - size.width * 0.30f * s, cy - size.width * 0.14f * s),
                    size = Size(size.width * 0.60f * s, size.width * 0.28f * s),
                    style = Stroke(width = 2.0f)
                )
            }

            // Glowing core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PlasmaTurquoise.copy(alpha = 0.25f * spaceAlpha.value), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = size.width * 0.35f * s
                ),
                radius = size.width * 0.35f * s,
                center = Offset(cx, cy)
            )

            // Spaceship ascender
            if (shipAlpha.value > 0.05f) {
                val ship = ShipCatalog.ships.first()
                GameRenderer.drawSpacecraft(
                    drawScope = this,
                    x = cx,
                    y = cy + shipOffset.value,
                    width = 56f,
                    height = 84f,
                    tiltAngle = 0f,
                    ship = ship
                )
            }
        }

        // Animated Logo & Subtitle
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ORBIT BREAKER",
                color = StarlightMint.copy(alpha = logoAlpha.value),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ORBITAL SURVIVAL ARCADE",
                color = PlasmaTurquoise.copy(alpha = logoAlpha.value),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Minimal non-fake genuine initialization indicator
            if (logoAlpha.value > 0.5f) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PlasmaTurquoise,
                    strokeWidth = 2.dp
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
