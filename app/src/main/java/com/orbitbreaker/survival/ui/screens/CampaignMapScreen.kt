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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.CampaignCatalog
import com.orbitbreaker.survival.data.CampaignLevel
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LevelObjectiveType
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.ui.components.OrbitButton
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.DeepForest
import com.orbitbreaker.survival.ui.theme.MutedText
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OverlayScrim
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import com.orbitbreaker.survival.ui.theme.SurfacePanel
import com.orbitbreaker.survival.ui.theme.SurfacePanelBorder

@Composable
fun CampaignMapScreen(
    storage: LocalGameStorage,
    onSelectLevel: (levelId: Int) -> Unit,
    onBackToStation: () -> Unit
) {
    var activeDifficulty by remember { mutableStateOf(storage.getSelectedDifficulty()) }
    var highestUnlocked by remember(activeDifficulty) { mutableStateOf(storage.getHighestUnlockedLevel(activeDifficulty)) }
    var completedCount by remember(activeDifficulty) { mutableStateOf(storage.getCompletedLevelCount(activeDifficulty)) }
    var totalStars by remember(activeDifficulty) { mutableStateOf(storage.getTotalStarsEarned(activeDifficulty)) }

    var selectedLevelForModal by remember { mutableStateOf<CampaignLevel?>(null) }
    var showComingSoonModal by remember { mutableStateOf(false) }

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
            painter = androidx.compose.ui.res.painterResource(id = com.orbitbreaker.survival.R.drawable.bg_sector_rings),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.55f
        )

        // Radial celestial illumination
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PlasmaTurquoise.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset.Zero,
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CAMPAIGN MAP",
                        color = StarlightMint,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "20 ORBITAL SECTORS · STRICT ISOLATION",
                        color = PlasmaTurquoise,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
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

            Spacer(modifier = Modifier.height(14.dp))

            // Difficulty Switcher Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameDifficulty.values().forEach { diff ->
                    val isSelected = activeDifficulty == diff
                    val diffCleared = storage.getCompletedLevelCount(diff)
                    val activeColor = when (diff) {
                        GameDifficulty.EASY -> StarlightMint
                        GameDifficulty.NORMAL -> PlasmaTurquoise
                        GameDifficulty.HARD -> AuroraWarning
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SurfacePanel else SurfacePanel.copy(alpha = 0.5f))
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) activeColor else SurfacePanelBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                activeDifficulty = diff
                                storage.saveSelectedDifficulty(diff)
                                highestUnlocked = storage.getHighestUnlockedLevel(diff)
                                completedCount = storage.getCompletedLevelCount(diff)
                                totalStars = storage.getTotalStarsEarned(diff)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = diff.label.uppercase(),
                                color = if (isSelected) activeColor else MutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$diffCleared/20",
                                color = if (isSelected) StarlightMint else MutedText.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sector Progress Summary Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfacePanel)
                    .border(1.dp, SurfacePanelBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECTOR PROGRESS (${activeDifficulty.label.uppercase()})",
                        color = MutedText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedCount / 20 CLEARED · $totalStars/60 ★",
                        color = StarlightMint,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (completedCount / 20f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (activeDifficulty) {
                        GameDifficulty.EASY -> StarlightMint
                        GameDifficulty.NORMAL -> PlasmaTurquoise
                        GameDifficulty.HARD -> AuroraWarning
                    },
                    trackColor = ObsidianBlack
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 20 Level Grid + 2 Coming Soon nodes
            val allDisplayItems = (1..22).toList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allDisplayItems) { levelNum ->
                    val isPlayableLevel = levelNum <= CampaignCatalog.MAX_PLAYABLE_LEVEL
                    val isUnlocked = isPlayableLevel && levelNum <= highestUnlocked
                    val isCompleted = isPlayableLevel && storage.isLevelCompleted(levelNum, activeDifficulty)
                    val stars = if (isPlayableLevel) storage.getLevelStars(levelNum, activeDifficulty) else 0
                    val isBossLevel = isPlayableLevel && (levelNum % 5 == 0)

                    val levelData = if (isPlayableLevel) CampaignCatalog.getLevel(levelNum) else null

                    Box(
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    !isPlayableLevel -> SurfacePanel.copy(alpha = 0.35f)
                                    isCompleted -> SurfacePanel
                                    isUnlocked -> SurfacePanel
                                    else -> ObsidianBlack.copy(alpha = 0.8f)
                                }
                            )
                            .border(
                                1.5.dp,
                                when {
                                    !isPlayableLevel -> SurfacePanelBorder.copy(alpha = 0.4f)
                                    isBossLevel && isCompleted -> PlasmaTurquoise
                                    isBossLevel && isUnlocked -> com.orbitbreaker.survival.ui.theme.ImpactCoral
                                    isCompleted -> PlasmaTurquoise
                                    isUnlocked -> AuroraWarning
                                    else -> SurfacePanelBorder.copy(alpha = 0.5f)
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (!isPlayableLevel) {
                                    showComingSoonModal = true
                                } else if (isUnlocked) {
                                    selectedLevelForModal = levelData
                                }
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isBossLevel) {
                                Text(
                                    text = "BOSS",
                                    color = com.orbitbreaker.survival.ui.theme.ImpactCoral,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = if (isPlayableLevel) "$levelNum" else "✦",
                                color = when {
                                    !isPlayableLevel -> MutedText.copy(alpha = 0.5f)
                                    isBossLevel && isUnlocked -> com.orbitbreaker.survival.ui.theme.ImpactCoral
                                    isUnlocked -> StarlightMint
                                    else -> MutedText.copy(alpha = 0.4f)
                                },
                                fontSize = if (isBossLevel) 16.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = when {
                                    !isPlayableLevel -> "SOON"
                                    isCompleted -> "★".repeat(stars.coerceAtLeast(1))
                                    isUnlocked -> "READY"
                                    else -> "LOCKED"
                                },
                                color = when {
                                    !isPlayableLevel -> MutedText.copy(alpha = 0.5f)
                                    isCompleted -> PlasmaTurquoise
                                    isUnlocked -> AuroraWarning
                                    else -> MutedText.copy(alpha = 0.4f)
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Level Details Briefing Modal
        selectedLevelForModal?.let { lvl ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfacePanel)
                        .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "SECTOR BRIEFING — LVL ${lvl.id} [${activeDifficulty.label.uppercase()}]",
                        color = AuroraWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lvl.name.uppercase(),
                        color = StarlightMint,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = lvl.description,
                        color = MutedText,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Objective Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "MISSION OBJECTIVE",
                            color = PlasmaTurquoise,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (lvl.objectiveType) {
                                LevelObjectiveType.SURVIVE_TIME -> "Survive for ${lvl.targetValue.toInt()} seconds"
                                LevelObjectiveType.REACH_SCORE -> "Reach ${lvl.targetValue.toInt()} flight telemetry points"
                            },
                            color = StarlightMint,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val isClearedOnDiff = storage.isLevelCompleted(lvl.id, activeDifficulty)
                        val bestScoreOnDiff = storage.getLevelBestScore(lvl.id, activeDifficulty)
                        val starsOnDiff = storage.getLevelStars(lvl.id, activeDifficulty)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isClearedOnDiff) "RECORD: $bestScoreOnDiff PTS" else "RECORD: UNRECORDED",
                                color = AuroraWarning,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isClearedOnDiff) "STATUS: " + "★".repeat(starsOnDiff.coerceAtLeast(1)) else "STATUS: READY",
                                color = PlasmaTurquoise,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OrbitButton(
                        text = "ENGAGE SECTOR (${activeDifficulty.label.uppercase()})",
                        onClick = {
                            val id = lvl.id
                            selectedLevelForModal = null
                            storage.saveSelectedDifficulty(activeDifficulty)
                            onSelectLevel(id)
                        },
                        isPrimary = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OrbitButton(
                        text = "CANCEL",
                        onClick = { selectedLevelForModal = null },
                        isPrimary = false
                    )
                }
            }
        }

        // Level 21+ Coming Soon Modal
        if (showComingSoonModal) {
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
                        text = "SECTOR CLASSIFIED",
                        color = AuroraWarning,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Levels 21 and beyond are currently locked under deep space anomaly quarantine.\n\nComplete all 20 campaign sectors to prepare for future orbital expansions.",
                        color = StarlightMint,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OrbitButton(
                        text = "ACKNOWLEDGED",
                        onClick = { showComingSoonModal = false },
                        isPrimary = true
                    )
                }
            }
        }
    }
}
