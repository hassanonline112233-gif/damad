package com.orbitbreaker.survival.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.data.ThreatDirector
import com.orbitbreaker.survival.ui.components.OrbitButton
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.DeepForest
import com.orbitbreaker.survival.ui.theme.MutedText
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import com.orbitbreaker.survival.ui.theme.SurfacePanel
import com.orbitbreaker.survival.ui.theme.SurfacePanelBorder

@Composable
fun UnlimitedModeScreen(
    storage: LocalGameStorage,
    onStartUnlimitedRun: (difficulty: GameDifficulty) -> Unit,
    onBackToStation: () -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(storage.getSelectedDifficulty()) }
    var bestScore by remember { mutableIntStateOf(storage.getUnlimitedBestScore(selectedDifficulty)) }
    var bestTime by remember { mutableFloatStateOf(storage.getUnlimitedBestTime(selectedDifficulty)) }

    fun refreshStats(diff: GameDifficulty) {
        selectedDifficulty = diff
        storage.saveSelectedDifficulty(diff)
        bestScore = storage.getUnlimitedBestScore(diff)
        bestTime = storage.getUnlimitedBestTime(diff)
    }

    LaunchedEffect(selectedDifficulty) {
        refreshStats(selectedDifficulty)
    }

    BackHandler {
        onBackToStation()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ObsidianBlack, DeepForest, ObsidianBlack)
                )
            )
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.orbitbreaker.survival.R.drawable.bg_sector_unlimited),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.55f
        )

        // Radial celestial lighting
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PlasmaTurquoise.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(300f, 300f),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "UNLIMITED MODE",
                        color = StarlightMint,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "INFINITE SURVIVAL SIMULATION",
                        color = PlasmaTurquoise,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel)
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                        .clickable { onBackToStation() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BACK",
                        color = PlasmaTurquoise,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Central Info Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfacePanel)
                    .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "MISSION BRIEFING",
                    color = AuroraWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Endure escalating waves of planetary hazards, enemy strike craft, and recurring dreadnought anomalies. Flight continues until ship destruction.",
                    color = StarlightMint,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Threat Stages Progression Preview
                Text(
                    text = "ESCALATING THREAT TIERS",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThreatDirector.stages.take(4).forEach { stage ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stage.name,
                            color = StarlightMint,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stage.minTimeSeconds.toInt()}s+",
                            color = PlasmaTurquoise,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Stats Card by Selected Difficulty
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfacePanel)
                    .border(1.2.dp, SurfacePanelBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "${selectedDifficulty.label.uppercase()} MODE PERSONAL RECORDS",
                    color = AuroraWarning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MAX SCORE",
                            color = MutedText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (bestScore > 0) "$bestScore PTS" else "NO RECORD",
                            color = PlasmaTurquoise,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SURVIVAL TIME",
                            color = MutedText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (bestTime > 0f) String.format("%.1fs", bestTime) else "0.0s",
                            color = StarlightMint,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Difficulty Switcher Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameDifficulty.values().forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PlasmaTurquoise else SurfacePanel)
                            .border(
                                1.dp,
                                if (isSelected) PlasmaTurquoise else SurfacePanelBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { refreshStats(diff) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diff.label.uppercase(),
                            color = if (isSelected) ObsidianBlack else StarlightMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Launch Action
            OrbitButton(
                text = "START UNLIMITED RUN",
                onClick = { onStartUnlimitedRun(selectedDifficulty) },
                isPrimary = true
            )
        }
    }
}
