package com.orbitbreaker.survival.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.CampaignCatalog
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.data.ShipCatalog
import com.orbitbreaker.survival.game.GameRenderer
import com.orbitbreaker.survival.ui.components.OrbitButton
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.DeepForest
import com.orbitbreaker.survival.ui.theme.MutedText
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OrbitGreen
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import com.orbitbreaker.survival.ui.theme.SurfacePanel
import com.orbitbreaker.survival.ui.theme.SurfacePanelBorder

@Composable
fun OrbitStationScreen(
    storage: LocalGameStorage,
    onLaunchFlight: () -> Unit,
    onOpenCampaign: () -> Unit,
    onOpenUnlimited: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSystemsBay: () -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(storage.getSelectedDifficulty()) }
    var bestScore by remember { mutableStateOf(storage.getBestScore()) }
    var credits by remember { mutableStateOf(storage.getCredits()) }
    var highestLevel by remember(selectedDifficulty) { mutableStateOf(storage.getHighestUnlockedLevel(selectedDifficulty)) }
    var equippedShipId by remember { mutableStateOf(storage.getEquippedShipId()) }
    val settings = remember { storage.getSettings() }

    LaunchedEffect(Unit) {
        bestScore = storage.getBestScore()
        credits = storage.getCredits()
        selectedDifficulty = storage.getSelectedDifficulty()
        highestLevel = storage.getHighestUnlockedLevel(selectedDifficulty)
        equippedShipId = storage.getEquippedShipId()
    }

    val equippedShip = remember(equippedShipId) {
        ShipCatalog.getShipById(equippedShipId)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "station_anim")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    val shipHover by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ship_hover"
    )

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
            painter = androidx.compose.ui.res.painterResource(id = com.orbitbreaker.survival.R.drawable.bg_station_orbit),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.78f
        )

        // Hero Spaceship & Orbital Rings Canvas (Centered in visual sweet-spot)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cx = size.width / 2f
            val cy = size.height * 0.29f

            // Outer pulsing cyan ring
            rotate(ringRotation, pivot = Offset(cx, cy)) {
                drawOval(
                    color = PlasmaTurquoise.copy(alpha = 0.35f),
                    topLeft = Offset(cx - size.width * 0.40f, cy - size.width * 0.18f),
                    size = Size(size.width * 0.80f, size.width * 0.36f),
                    style = Stroke(width = 2.0f)
                )
            }

            // Inner counter-rotating ring
            rotate(-ringRotation * 1.5f, pivot = Offset(cx, cy)) {
                drawOval(
                    color = OrbitGreen.copy(alpha = 0.50f),
                    topLeft = Offset(cx - size.width * 0.30f, cy - size.width * 0.13f),
                    size = Size(size.width * 0.60f, size.width * 0.26f),
                    style = Stroke(width = 1.5f)
                )
            }

            // Radial energy core glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PlasmaTurquoise.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = size.width * 0.32f
                ),
                radius = size.width * 0.32f,
                center = Offset(cx, cy)
            )

            // Render Hero Equipped Spacecraft with hovering animation
            GameRenderer.drawSpacecraft(
                drawScope = this,
                x = cx,
                y = cy + shipHover,
                width = 62f,
                height = 92f,
                tiltAngle = 0f,
                ship = equippedShip
            )
        }

        // Main Menu Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar: Credits Badge, Hangar shortcut, and Systems shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Credits Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel)
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❖",
                        color = AuroraWarning,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$credits CR",
                        color = AuroraWarning,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top Right Quick Access: Systems & Records
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfacePanel)
                            .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                            .clickable { onOpenRecords() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ARCHIVE",
                            color = StarlightMint,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfacePanel)
                            .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                            .clickable { onOpenSystemsBay() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "⚙ SETTINGS",
                            color = PlasmaTurquoise,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Top Status Bar Cybernetic Boundary Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                PlasmaTurquoise.copy(alpha = 0.6f),
                                StarlightMint,
                                PlasmaTurquoise.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.weight(1f))

            // 2. Center Branding & Equipped Ship Spec Badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfacePanel.copy(alpha = 0.75f))
                    .border(1.dp, SurfacePanelBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ORBIT BREAKER",
                    color = StarlightMint,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PlasmaTurquoise.copy(alpha = 0.20f))
                            .border(1.dp, PlasmaTurquoise.copy(alpha = 0.60f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = equippedShip.name.uppercase(),
                            color = PlasmaTurquoise,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  SPEED +${((equippedShip.maxSpeedBonus - 1f) * 100).toInt()}%  •  AGILITY +${((equippedShip.accelerationBonus - 1f) * 100).toInt()}%",
                        color = MutedText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Campaign & Record Overview Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfacePanel)
                    .border(1.dp, SurfacePanelBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CAMPAIGN PROGRESS",
                        color = MutedText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SECTOR $highestLevel / 20",
                        color = StarlightMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BEST PILOT SCORE",
                        color = MutedText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$bestScore PTS",
                        color = PlasmaTurquoise,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Difficulty Selector (Easy / Normal / Hard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameDifficulty.values().forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PlasmaTurquoise else SurfacePanel)
                            .border(1.2.dp, if (isSelected) PlasmaTurquoise else SurfacePanelBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                selectedDifficulty = diff
                                storage.saveSelectedDifficulty(diff)
                                highestLevel = storage.getHighestUnlockedLevel(diff)
                            }
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

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Primary Action Deck
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Continue Campaign Button
                OrbitButton(
                    text = "CONTINUE CAMPAIGN (LVL $highestLevel)",
                    onClick = onLaunchFlight,
                    isPrimary = true
                )

                // Dedicated High-End UNLIMITED SURVIVAL CARD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    SurfacePanel,
                                    PlasmaTurquoise.copy(alpha = 0.18f),
                                    SurfacePanel
                                )
                            )
                        )
                        .border(1.5.dp, PlasmaTurquoise.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .clickable { onOpenUnlimited() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✦ UNLIMITED SURVIVAL",
                                color = PlasmaTurquoise,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.1.sp
                            )
                        }
                        Text(
                            text = "Infinite enemy waves • Scaling threats",
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "ENGAGE ►",
                        color = StarlightMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Dual Tactical Shortcuts: Campaign Map & Ship Hangar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OrbitButton(
                            text = "SECTOR MAP",
                            onClick = onOpenCampaign,
                            isPrimary = false
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OrbitButton(
                            text = "SHIP HANGAR",
                            onClick = onOpenHangar,
                            isPrimary = false
                        )
                    }
                }
            }
        }
    }
}
