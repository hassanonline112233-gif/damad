package com.orbitbreaker.survival.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.CampaignCatalog
import com.orbitbreaker.survival.data.CampaignLevel
import com.orbitbreaker.survival.game.GameEngine
import com.orbitbreaker.survival.game.GameRenderer
import com.orbitbreaker.survival.game.GameState
import com.orbitbreaker.survival.game.PickupType
import com.orbitbreaker.survival.ui.components.OrbitButton
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.ImpactCoral
import com.orbitbreaker.survival.ui.theme.MutedText
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OverlayScrim
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import com.orbitbreaker.survival.ui.theme.SurfacePanel
import com.orbitbreaker.survival.ui.theme.SurfacePanelBorder

@Composable
fun SurvivalFlightScreen(
    levelId: Int?,
    engine: GameEngine,
    onReturnToStation: () -> Unit,
    onLevelSuccess: (level: CampaignLevel, score: Int, survivalTime: Float, totalCredits: Int, stars: Int) -> Unit,
    onGameOver: (level: CampaignLevel?, score: Int, survivalTime: Float, isNewRecord: Boolean) -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var scoreDisplay by remember { mutableIntStateOf(0) }
    var creditsDisplay by remember { mutableIntStateOf(0) }
    var timeDisplay by remember { mutableFloatStateOf(0f) }
    var healthDisplay by remember { mutableFloatStateOf(100f) }
    var maxHealthDisplay by remember { mutableFloatStateOf(100f) }
    var powerLevelDisplay by remember { mutableIntStateOf(1) }
    var shieldTimerDisplay by remember { mutableFloatStateOf(0f) }
    var rapidFireTimerDisplay by remember { mutableFloatStateOf(0f) }
    var spreadShotTimerDisplay by remember { mutableFloatStateOf(0f) }
    var heavyShotTimerDisplay by remember { mutableFloatStateOf(0f) }
    var doubleCoinsTimerDisplay by remember { mutableFloatStateOf(0f) }
    var bossHealthRatio by remember { mutableStateOf<Float?>(null) }
    var bossName by remember { mutableStateOf<String?>(null) }

    val activeLevel = remember(levelId) {
        if (levelId != null) CampaignCatalog.getLevel(levelId) else null
    }

    LaunchedEffect(levelId) {
        engine.onLevelSuccess = { lvl, finalScore, finalTime, totalCredits, stars ->
            onLevelSuccess(lvl, finalScore, finalTime, totalCredits, stars)
        }
        engine.onGameOver = { lvl, finalScore, survivalTime, isNewRecord ->
            onGameOver(lvl, finalScore, survivalTime, isNewRecord)
        }

        if (levelId != null) {
            engine.startLevel(levelId)
        } else {
            engine.startEndlessRun()
        }
    }

    BackHandler {
        if (!isPaused && engine.gameState == GameState.PLAYING) {
            engine.pauseGame()
            isPaused = true
        } else if (isPaused) {
            engine.resumeGame()
            isPaused = false
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                if (engine.gameState == GameState.PLAYING) {
                    engine.pauseGame()
                    isPaused = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var renderTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var previousFrameTime = 0L
        var stateSyncTimer = 0f
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (previousFrameTime != 0L) {
                    val dt = (frameTimeNanos - previousFrameTime) / 1_000_000_000f
                    engine.update(dt)
                    renderTrigger = (renderTrigger + 1) and 0x7FFFFFFF

                    // Throttle Compose state writes to ~15-20Hz to eliminate GC micro-stutters
                    stateSyncTimer += dt
                    if (stateSyncTimer >= 0.05f) {
                        stateSyncTimer = 0f
                        scoreDisplay = (engine.scoreManager.currentScore * engine.currentDifficulty.scoreMultiplier).toInt()
                        creditsDisplay = engine.storage.getCredits()
                        timeDisplay = engine.scoreManager.survivalTimeSeconds
                        healthDisplay = engine.rocket.health
                        maxHealthDisplay = engine.rocket.maxHealth
                        powerLevelDisplay = engine.rocket.powerLevel

                        shieldTimerDisplay = engine.powerupManager.shieldTimer
                        rapidFireTimerDisplay = engine.powerupManager.rapidFireTimer
                        spreadShotTimerDisplay = engine.powerupManager.spreadShotTimer
                        heavyShotTimerDisplay = engine.powerupManager.heavyShotTimer
                        doubleCoinsTimerDisplay = engine.powerupManager.doubleCoinsTimer

                        engine.activeBoss?.let { boss ->
                            bossHealthRatio = boss.healthRatio
                            bossName = boss.bossId.title
                        } ?: run {
                            if (bossHealthRatio != null) {
                                bossHealthRatio = null
                                bossName = null
                            }
                        }
                    }
                }
                previousFrameTime = frameTimeNanos
            }
        }
    }

    val bgRes = remember(levelId) { engine.getSectorBackgroundRes() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                engine.setScreenDimensions(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        // High-resolution sector background backdrop with luminous cosmic presence
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.85f
        )

        // Radial celestial lens flare over the sector
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            PlasmaTurquoise.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset.Zero,
                        radius = 900f
                    )
                )
        )

        // Playable Canvas with smooth, zero-lag 2D hold-and-drag flight control
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isPaused) {
                    if (isPaused) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        engine.onTouchDown(down.position.x, down.position.y)

                        // Track continuous finger drag in all directions (up, down, left, right)
                        var pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }
                                ?: event.changes.firstOrNull()

                            if (change == null || !change.pressed) {
                                engine.onTouchUp()
                                break
                            }

                            pointerId = change.id
                            engine.onTouchMove(change.position.x, change.position.y)
                            change.consume()
                        }
                    }
                }
        ) {
            // Read renderTrigger to guarantee DrawScope redraws on every animation tick
            @Suppress("UNUSED_VARIABLE")
            val frame = renderTrigger
            GameRenderer.render(this, engine)
        }

        // Animated In-Combat Top Feedback Alert Banner
        val latestFeedback = engine.combatFeedbacks.lastOrNull()
        if (latestFeedback != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 115.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfacePanel.copy(alpha = 0.92f * latestFeedback.alpha))
                        .border(1.dp, latestFeedback.color.copy(alpha = latestFeedback.alpha), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = latestFeedback.text,
                        color = latestFeedback.color.copy(alpha = latestFeedback.alpha),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Top Status Bar Boundary & HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Professional Cybernetic Status Bar Separator Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                PlasmaTurquoise.copy(alpha = 0.4f),
                                StarlightMint.copy(alpha = 0.95f),
                                PlasmaTurquoise.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top HUD Overlay Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
            // Row 1: Health Bar, Power Level, Coins & Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Health Bar Container
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel.copy(alpha = 0.90f))
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val hpRatio = (healthDisplay / maxHealthDisplay).coerceIn(0f, 1f)
                    val isLowHealth = hpRatio <= 0.30f
                    val hpColor = when {
                        isLowHealth -> ImpactCoral
                        hpRatio <= 0.60f -> AuroraWarning
                        else -> PlasmaTurquoise
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(110.dp)
                    ) {
                        Text(
                            text = if (isLowHealth) "HULL CRITICAL" else "HULL INTEGRITY",
                            color = if (isLowHealth) ImpactCoral else MutedText,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${healthDisplay.toInt()}%",
                            color = hpColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .width(110.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = hpColor,
                        trackColor = ObsidianBlack
                    )
                }

                // Power Level Indicator: POWER x/10
                val powerBgBrush = if (powerLevelDisplay == 10) {
                    Brush.horizontalGradient(listOf(Color(0xFF0F4234), Color(0xFF1E6B55)))
                } else {
                    androidx.compose.ui.graphics.SolidColor(SurfacePanel.copy(alpha = 0.90f))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(powerBgBrush)
                        .border(
                            1.dp,
                            if (powerLevelDisplay == 10) PlasmaTurquoise else SurfacePanelBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (powerLevelDisplay == 10) "★ MAX " else "⚡ ",
                            color = if (powerLevelDisplay == 10) StarlightMint else PlasmaTurquoise,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "POWER $powerLevelDisplay/10",
                            color = if (powerLevelDisplay == 10) StarlightMint else PlasmaTurquoise,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Live Coins Counter
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel.copy(alpha = 0.90f))
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙 ", fontSize = 11.sp)
                    Text(
                        text = "$creditsDisplay",
                        color = Color(0xFFFACC15),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pause Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel.copy(alpha = 0.90f))
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            engine.pauseGame()
                            isPaused = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⏸", color = StarlightMint, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Score, Time / Tier, and Active Powerup Buff Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Time Display
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfacePanel.copy(alpha = 0.85f))
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (activeLevel == null) {
                            val stage = com.orbitbreaker.survival.data.ThreatDirector.getCurrentStage(timeDisplay)
                            Text(
                                text = "TIER ${stage.tier}: ${stage.name.take(14)}",
                                color = AuroraWarning,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "LVL ${activeLevel.id}: ${activeLevel.name.take(14)}",
                                color = MutedText,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "SCORE $scoreDisplay",
                            color = PlasmaTurquoise,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = String.format("%.1fs", timeDisplay),
                        color = StarlightMint,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active Temporary Powerup Indicators with countdown timers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shieldTimerDisplay > 0f) {
                        BuffBadge("SHIELD", String.format("%.1fs", shieldTimerDisplay), Color(0xFF38BDF8))
                    }
                    if (rapidFireTimerDisplay > 0f) {
                        BuffBadge("RAPID", String.format("%.1fs", rapidFireTimerDisplay), Color(0xFFFF7A00))
                    }
                    if (spreadShotTimerDisplay > 0f) {
                        BuffBadge("SPREAD", String.format("%.1fs", spreadShotTimerDisplay), Color(0xFFA855F7))
                    }
                    if (heavyShotTimerDisplay > 0f) {
                        BuffBadge("HEAVY", String.format("%.1fs", heavyShotTimerDisplay), Color(0xFFFF4D4D))
                    }
                    if (doubleCoinsTimerDisplay > 0f) {
                        BuffBadge("2X COINS", String.format("%.1fs", doubleCoinsTimerDisplay), Color(0xFFFACC15))
                    }
                }
            }

            // Boss Health Bar (if active boss)
            if (bossHealthRatio != null && bossName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfacePanel.copy(alpha = 0.94f))
                        .border(1.dp, ImpactCoral.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        engine.activeBoss?.let { b ->
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = b.bossId.artRes),
                                contentDescription = b.bossId.title,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, ImpactCoral, RoundedCornerShape(6.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BOSS: $bossName",
                                    color = AuroraWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${(bossHealthRatio!! * 100).toInt()}%",
                                    color = StarlightMint,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { bossHealthRatio!! },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ImpactCoral,
                                trackColor = ObsidianBlack
                            )
                        }
                    }
                }
            }
        }
    }

        // Steering Guidance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "◄ STEER LEFT",
                color = MutedText.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "STEER RIGHT ►",
                color = MutedText.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Pause Overlay
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfacePanel)
                        .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FLIGHT PAUSED",
                        color = StarlightMint,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeLevel != null) "SECTOR ${activeLevel.id}: ${activeLevel.name.uppercase()}" else "ORBITAL HOLD",
                        color = PlasmaTurquoise,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OrbitButton(
                        text = "RESUME FLIGHT",
                        onClick = {
                            engine.resumeGame()
                            isPaused = false
                        },
                        isPrimary = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OrbitButton(
                        text = "RESTART FLIGHT",
                        onClick = {
                            isPaused = false
                            if (levelId != null) engine.startLevel(levelId) else engine.startEndlessRun()
                        },
                        isPrimary = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OrbitButton(
                        text = "RETURN TO STATION",
                        onClick = {
                            isPaused = false
                            onReturnToStation()
                        },
                        isPrimary = false
                    )
                }
            }
        }
    }
}

@Composable
private fun BuffBadge(name: String, remaining: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.20f))
            .border(1.dp, color.copy(alpha = 0.70f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$name $remaining",
            color = color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
