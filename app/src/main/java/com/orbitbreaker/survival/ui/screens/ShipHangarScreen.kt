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
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.data.PlayerSettings
import com.orbitbreaker.survival.data.PurchaseResult
import com.orbitbreaker.survival.data.ShipCatalog
import com.orbitbreaker.survival.data.Spacecraft
import com.orbitbreaker.survival.game.AudioManager
import com.orbitbreaker.survival.game.GameRenderer
import com.orbitbreaker.survival.game.SoundEvent
import com.orbitbreaker.survival.ui.components.OrbitButton
import com.orbitbreaker.survival.ui.theme.AuroraWarning
import com.orbitbreaker.survival.ui.theme.DeepForest
import com.orbitbreaker.survival.ui.theme.ImpactCoral
import com.orbitbreaker.survival.ui.theme.MutedText
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OverlayScrim
import com.orbitbreaker.survival.ui.theme.PlasmaTurquoise
import com.orbitbreaker.survival.ui.theme.StarlightMint
import com.orbitbreaker.survival.ui.theme.SurfacePanel
import com.orbitbreaker.survival.ui.theme.SurfacePanelBorder

@Composable
fun ShipHangarScreen(
    storage: LocalGameStorage,
    audioManager: AudioManager? = null,
    settings: PlayerSettings? = null,
    onOpenCampaign: (() -> Unit)? = null,
    onBackToStation: () -> Unit
) {
    var playerCredits by remember { mutableStateOf(storage.getCredits()) }
    var equippedShipId by remember { mutableStateOf(storage.getEquippedShipId()) }
    var ownedShipIds by remember { mutableStateOf(storage.getOwnedShipIds()) }

    var shipToPurchase by remember { mutableStateOf<Spacecraft?>(null) }
    var insufficientFundsShip by remember { mutableStateOf<Spacecraft?>(null) }
    var purchaseSuccessShip by remember { mutableStateOf<Spacecraft?>(null) }
    var isProcessingPurchase by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playerCredits = storage.getCredits()
        equippedShipId = storage.getEquippedShipId()
        ownedShipIds = storage.getOwnedShipIds()
    }

    BackHandler {
        onBackToStation()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hangar_anim")
    val previewRotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hangar_tilt"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hangar_ring"
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
            painter = androidx.compose.ui.res.painterResource(id = com.orbitbreaker.survival.R.drawable.bg_sector_nebula),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.50f
        )

        // Radial celestial lighting
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PlasmaTurquoise.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(200f, 400f),
                        radius = 750f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            // Header: Hangar Title & Authoritative Credit Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHIP HANGAR",
                        color = StarlightMint,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "AVAILABLE BALANCE: $playerCredits CR",
                        color = AuroraWarning,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel)
                        .border(1.dp, SurfacePanelBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            audioManager?.playSound(SoundEvent.BUTTON_CLICK, settings ?: storage.getSettings())
                            onBackToStation()
                        }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Ships Catalog List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(ShipCatalog.ships) { ship ->
                    val isOwned = ownedShipIds.contains(ship.id)
                    val isEquipped = equippedShipId == ship.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfacePanel)
                            .border(
                                width = if (isEquipped) 2.dp else 1.2.dp,
                                color = if (isEquipped) PlasmaTurquoise else SurfacePanelBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Live Ship Canvas & Tactical Blueprint Preview
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ObsidianBlack)
                                .border(1.dp, if (isEquipped) PlasmaTurquoise.copy(alpha = 0.6f) else SurfacePanelBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = ship.artRes),
                                contentDescription = ship.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                alpha = 0.85f
                            )
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                GameRenderer.drawSpacecraft(
                                    drawScope = this,
                                    x = size.width / 2f,
                                    y = size.height / 2f,
                                    width = 32f,
                                    height = 46f,
                                    tiltAngle = if (isEquipped) previewRotation else 0f,
                                    ship = ship
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Ship Specifications & Action
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ship.name.uppercase(),
                                    color = StarlightMint,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                when {
                                    isEquipped -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PlasmaTurquoise.copy(alpha = 0.2f))
                                                .border(1.dp, PlasmaTurquoise, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "EQUIPPED",
                                                color = PlasmaTurquoise,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    isOwned -> {
                                        Text(
                                            text = "OWNED",
                                            color = MutedText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "${ship.price} CR",
                                            color = AuroraWarning,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Handling: +${((ship.accelerationBonus - 1.0f) * 100).toInt()}%  ·  Max Spd: +${((ship.maxSpeedBonus - 1.0f) * 100).toInt()}%",
                                color = PlasmaTurquoise,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ship.description,
                                color = MutedText,
                                fontSize = 11.sp,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action button: Equip vs Purchase
                            when {
                                isEquipped -> {
                                    // Already equipped, active
                                }
                                isOwned -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PlasmaTurquoise)
                                            .clickable {
                                                if (storage.equipShip(ship.id)) {
                                                    audioManager?.playSound(SoundEvent.BUTTON_CLICK, settings ?: storage.getSettings())
                                                    equippedShipId = ship.id
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "EQUIP SHIP",
                                            color = ObsidianBlack,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                else -> {
                                    val canAfford = playerCredits >= ship.price
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (canAfford) AuroraWarning else SurfacePanelBorder)
                                            .clickable {
                                                audioManager?.playSound(SoundEvent.BUTTON_CLICK, settings ?: storage.getSettings())
                                                if (canAfford) {
                                                    shipToPurchase = ship
                                                } else {
                                                    insufficientFundsShip = ship
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "UNLOCK FOR ${ship.price} CR",
                                            color = if (canAfford) ObsidianBlack else MutedText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Purchase Confirmation Dialog (Case 1: player has enough coins)
        shipToPurchase?.let { ship ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfacePanel)
                        .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COMMISSION SPACECRAFT",
                        color = AuroraWarning,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Ship Preview
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBlack)
                            .border(1.dp, PlasmaTurquoise.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            GameRenderer.drawSpacecraft(
                                drawScope = this,
                                x = size.width / 2f,
                                y = size.height / 2f,
                                width = 38f,
                                height = 54f,
                                tiltAngle = 0f,
                                ship = ship
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = ship.name.uppercase(),
                        color = StarlightMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = ship.subtitle,
                        color = PlasmaTurquoise,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transaction Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ACQUISITION COST", color = MutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "${ship.price} CR", color = AuroraWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "CURRENT BALANCE", color = MutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "$playerCredits CR", color = StarlightMint, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "REMAINING AFTER", color = MutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "${(playerCredits - ship.price).coerceAtLeast(0)} CR", color = PlasmaTurquoise, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OrbitButton(
                        text = if (isProcessingPurchase) "PROCESSING..." else "CONFIRM ACQUISITION",
                        onClick = {
                            if (!isProcessingPurchase) {
                                isProcessingPurchase = true
                                val result = storage.purchaseShip(ship.id)
                                isProcessingPurchase = false
                                when (result) {
                                    is PurchaseResult.Success -> {
                                        audioManager?.playSound(SoundEvent.PICKUP_CORE, settings ?: storage.getSettings())
                                        playerCredits = result.remainingBalance
                                        ownedShipIds = storage.getOwnedShipIds()
                                        shipToPurchase = null
                                        purchaseSuccessShip = result.ship
                                    }
                                    is PurchaseResult.InsufficientFunds -> {
                                        shipToPurchase = null
                                        insufficientFundsShip = result.ship
                                    }
                                    is PurchaseResult.AlreadyOwned -> {
                                        ownedShipIds = storage.getOwnedShipIds()
                                        shipToPurchase = null
                                    }
                                    is PurchaseResult.InvalidShip -> {
                                        shipToPurchase = null
                                    }
                                }
                            }
                        },
                        isPrimary = true,
                        enabled = !isProcessingPurchase
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OrbitButton(
                        text = "CANCEL",
                        onClick = { shipToPurchase = null },
                        isPrimary = false
                    )
                }
            }
        }

        // 2. Insufficient Credits Dialog (Case 2: player does not have enough coins)
        insufficientFundsShip?.let { ship ->
            val shortfall = ship.price - playerCredits

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfacePanel)
                        .border(1.5.dp, ImpactCoral.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NOT ENOUGH CREDITS",
                        color = ImpactCoral,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You need ${ship.price} Credits to unlock ${ship.name}.\nYou currently have $playerCredits Credits.",
                        color = StarlightMint,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Shortfall Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBlack)
                            .border(1.dp, ImpactCoral.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SHORTFALL: $shortfall CREDITS",
                            color = ImpactCoral,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Earn more credits by completing campaign sectors, destroying combatants, and gathering coin caches.",
                        color = MutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (onOpenCampaign != null) {
                        OrbitButton(
                            text = "EARN CREDITS (CAMPAIGN)",
                            onClick = {
                                insufficientFundsShip = null
                                onOpenCampaign()
                            },
                            isPrimary = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OrbitButton(
                        text = "GOT IT",
                        onClick = { insufficientFundsShip = null },
                        isPrimary = onOpenCampaign == null
                    )
                }
            }
        }

        // 3. Purchase Success Celebration Modal
        purchaseSuccessShip?.let { ship ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfacePanel)
                        .border(1.8.dp, PlasmaTurquoise, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SPACECRAFT UNLOCKED!",
                        color = StarlightMint,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FLEET COMMISSION APPROVED",
                        color = PlasmaTurquoise,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Glowing Emblem Preview
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            rotate(ringRotation) {
                                drawCircle(
                                    color = PlasmaTurquoise.copy(alpha = 0.5f),
                                    radius = size.width * 0.44f,
                                    style = Stroke(width = 2f)
                                )
                            }
                            GameRenderer.drawSpacecraft(
                                drawScope = this,
                                x = size.width / 2f,
                                y = size.height / 2f,
                                width = 42f,
                                height = 58f,
                                tiltAngle = 0f,
                                ship = ship
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = ship.name.uppercase(),
                        color = StarlightMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Cost: ${ship.price} CR · New Balance: $playerCredits CR",
                        color = AuroraWarning,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OrbitButton(
                        text = "EQUIP NOW",
                        onClick = {
                            if (storage.equipShip(ship.id)) {
                                audioManager?.playSound(SoundEvent.BUTTON_CLICK, settings ?: storage.getSettings())
                                equippedShipId = ship.id
                            }
                            purchaseSuccessShip = null
                        },
                        isPrimary = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OrbitButton(
                        text = "KEEP CURRENT SHIP",
                        onClick = { purchaseSuccessShip = null },
                        isPrimary = false
                    )
                }
            }
        }
    }
}
