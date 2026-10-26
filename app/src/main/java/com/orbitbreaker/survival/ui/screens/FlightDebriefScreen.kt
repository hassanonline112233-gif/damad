package com.orbitbreaker.survival.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.CampaignCatalog
import com.orbitbreaker.survival.data.CampaignLevel
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.ui.components.OrbitButton
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.DeepForest
import com.orbitbreaker.survival.ui.theme.ImpactCoral
import com.orbitbreaker.survival.ui.theme.MutedText
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import com.orbitbreaker.survival.ui.theme.SurfacePanel
import com.orbitbreaker.survival.ui.theme.SurfacePanelBorder

@Composable
fun FlightDebriefScreen(
    level: CampaignLevel?,
    isSuccess: Boolean,
    score: Int,
    survivalTime: Float,
    earnedCredits: Int,
    stars: Int,
    isNewRecord: Boolean,
    difficulty: GameDifficulty,
    storage: LocalGameStorage,
    onFlyAgain: () -> Unit,
    onNextLevel: ((nextLevelId: Int) -> Unit)?,
    onCampaignMap: (() -> Unit)? = null,
    onReturnToStation: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    val difficultyBest = remember(difficulty, level) {
        if (level != null) {
            storage.getLevelBestScore(level.id, difficulty)
        } else {
            storage.getUnlimitedBestScore(difficulty)
        }
    }

    val completedSectorCount = remember(difficulty) {
        storage.getCompletedLevelCount(difficulty)
    }

    val totalStarsOnDifficulty = remember(difficulty) {
        storage.getTotalStarsEarned(difficulty)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    BackHandler {
        onReturnToStation()
    }

    // Victory animation transition
    val infiniteTransition = rememberInfiniteTransition(label = "debrief_rings")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "debrief_ring_rot"
    )
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "debrief_ring_pulse"
    )

    val diffColor = when (difficulty) {
        GameDifficulty.EASY -> StarlightMint
        GameDifficulty.NORMAL -> PlasmaTurquoise
        GameDifficulty.HARD -> ImpactCoral
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ObsidianBlack, DeepForest, ObsidianBlack)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { it / 3 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Cinematic Victory / Defeat Emblem
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val primaryColor = if (isSuccess) PlasmaTurquoise else ImpactCoral
                        val accentColor = if (isSuccess) StarlightMint else AuroraWarning

                        // Pulsing outer halo
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.15f * ringPulse),
                            radius = size.width * 0.48f * ringPulse
                        )

                        // Outer orbital ring
                        rotate(ringRotation) {
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.5f),
                                radius = size.width * 0.42f,
                                style = Stroke(width = 2.5f)
                            )
                        }

                        // Inner counter-rotating ring
                        rotate(-ringRotation * 1.5f) {
                            drawCircle(
                                color = accentColor.copy(alpha = 0.6f),
                                radius = size.width * 0.30f,
                                style = Stroke(width = 1.8f)
                            )
                        }

                        // Center victory core
                        drawCircle(
                            color = primaryColor,
                            radius = size.width * 0.14f
                        )
                    }

                    Text(
                        text = if (isSuccess) "✦" else "✕",
                        color = ObsidianBlack,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Header Title: Context-Aware Congratulation
                val headerTitle = when {
                    !isSuccess -> "MISSION FAILED"
                    level?.id == 20 -> "CAMPAIGN CONQUERED!"
                    level != null && level.id % 5 == 0 -> "BOSS DEFEATED!"
                    isNewRecord -> "NEW RECORD ACHIEVED!"
                    else -> "LEVEL COMPLETE!"
                }

                Text(
                    text = headerTitle,
                    color = if (isSuccess) StarlightMint else ImpactCoral,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Context Subtitle
                val subtitle = when {
                    !isSuccess -> "SHIP INTEGRITY COMPROMISED · RETRY READY"
                    level?.id == 20 -> "ALL 20 ORBITAL SECTORS SECURED ON ${difficulty.label.uppercase()}!"
                    level != null && level.id % 5 == 0 -> "SECTOR ${level.id} TITAN DESTROYED · SECTOR LIBERATED"
                    level != null -> "LEVEL ${level.id}: ${level.name.uppercase()}"
                    else -> "UNLIMITED ORBITAL RECON COMPLETED"
                }

                Text(
                    text = subtitle,
                    color = if (isSuccess) PlasmaTurquoise else MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Difficulty Mode Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfacePanel)
                        .border(1.dp, diffColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DIFFICULTY: ${difficulty.label.uppercase()}",
                        color = diffColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // If Boss Level, display Tactical Boss Dossier Artwork
                if (level != null && level.id % 5 == 0) {
                    val bossType = when (level.id) {
                        5 -> com.orbitbreaker.survival.game.BossId.IRON_COLOSSUS
                        10 -> com.orbitbreaker.survival.game.BossId.VOID_HARVESTER
                        15 -> com.orbitbreaker.survival.game.BossId.ECLIPSE_WARDEN
                        20 -> com.orbitbreaker.survival.game.BossId.FINAL_SINGULARITY
                        else -> null
                    }
                    if (bossType != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfacePanel)
                                .border(1.2.dp, if (isSuccess) PlasmaTurquoise else ImpactCoral, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = bossType.artRes),
                                contentDescription = bossType.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                alpha = 0.90f
                            )
                        }
                    }
                }

                // Star Rating (if campaign success)
                if (isSuccess && level != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..3) {
                            Text(
                                text = if (i <= stars) "★" else "☆",
                                color = if (i <= stars) AuroraWarning else MutedText.copy(alpha = 0.4f),
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Telemetry Breakdown Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfacePanel)
                        .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    // Final Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FINAL SCORE",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isNewRecord) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AuroraWarning.copy(alpha = 0.2f))
                                        .border(1.dp, AuroraWarning, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NEW RECORD",
                                        color = AuroraWarning,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "$score PTS",
                                color = PlasmaTurquoise,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mission / Survival Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSuccess) "CLEAR TIME" else "SURVIVAL TIME",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = String.format("%.1fs", survivalTime),
                            color = StarlightMint,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Credits Earned Breakdown
                    if (isSuccess && earnedCredits > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CREDITS EARNED",
                                color = MutedText,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "+$earnedCredits CR",
                                color = AuroraWarning,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sector / Difficulty Campaign Progress
                    if (level != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CAMPAIGN PROGRESS",
                                color = MutedText,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$completedSectorCount / 20 CLEARED (${totalStarsOnDifficulty}★)",
                                color = StarlightMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Difficulty Best Record
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BEST (${difficulty.label.uppercase()})",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$difficultyBest PTS",
                            color = PlasmaTurquoise,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Next Level Unlock Banner
                val nextLvlId = level?.id?.plus(1)
                val hasNextLevel = isSuccess && nextLvlId != null && nextLvlId <= CampaignCatalog.MAX_PLAYABLE_LEVEL

                if (hasNextLevel) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfacePanel)
                            .border(1.dp, PlasmaTurquoise, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SECTOR $nextLvlId UNLOCKED ON ${difficulty.label.uppercase()}!",
                            color = PlasmaTurquoise,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasNextLevel && onNextLevel != null) {
                        OrbitButton(
                            text = "NEXT LEVEL (LVL $nextLvlId)",
                            onClick = { onNextLevel(nextLvlId!!) },
                            isPrimary = true
                        )
                    }

                    OrbitButton(
                        text = if (isSuccess) "REPLAY MISSION" else "RETRY FLIGHT",
                        onClick = onFlyAgain,
                        isPrimary = !hasNextLevel
                    )

                    if (onCampaignMap != null) {
                        OrbitButton(
                            text = "CAMPAIGN MAP",
                            onClick = onCampaignMap,
                            isPrimary = false
                        )
                    }

                    OrbitButton(
                        text = "RETURN TO ORBIT STATION",
                        onClick = onReturnToStation,
                        isPrimary = false
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
