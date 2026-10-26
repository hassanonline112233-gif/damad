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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.data.PlayerSettings
import com.orbitbreaker.survival.data.SteeringSensitivity
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
fun SystemsBayScreen(
    storage: LocalGameStorage,
    audioManager: com.orbitbreaker.survival.game.AudioManager? = null,
    onSettingsChanged: () -> Unit = {},
    onBackToStation: () -> Unit
) {
    var settings by remember { mutableStateOf(storage.getSettings()) }
    var selectedDifficulty by remember { mutableStateOf(storage.getSelectedDifficulty()) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showResetCampaignConfirmation by remember { mutableStateOf(false) }

    fun updateSettings(newSettings: PlayerSettings) {
        settings = newSettings
        storage.saveSettings(newSettings)
        audioManager?.onSettingsChanged(newSettings, defaultLobbyMusic = true)
        onSettingsChanged()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEMS BAY",
                    color = StarlightMint,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

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

            Spacer(modifier = Modifier.height(22.dp))

            // Settings Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfacePanel)
                    .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                // SFX Toggle & Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SOUND EFFECTS",
                            color = StarlightMint,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (settings.soundEnabled) "Combat SFX: ${(settings.sfxVolume * 100).toInt()}%" else "Combat sound disabled",
                            color = if (settings.soundEnabled) PlasmaTurquoise else MutedText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = { checked ->
                            val updated = settings.copy(soundEnabled = checked)
                            updateSettings(updated)
                            if (checked) {
                                audioManager?.playSound(com.orbitbreaker.survival.game.SoundEvent.BUTTON_CLICK, updated)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = PlasmaTurquoise,
                            uncheckedThumbColor = MutedText,
                            uncheckedTrackColor = SurfacePanel
                        )
                    )
                }

                if (settings.soundEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.sfxVolume,
                        onValueChange = { newVol ->
                            updateSettings(settings.copy(sfxVolume = newVol))
                        },
                        valueRange = 0.05f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = PlasmaTurquoise,
                            activeTrackColor = PlasmaTurquoise,
                            inactiveTrackColor = SurfacePanelBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Music Toggle & Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BACKGROUND MUSIC",
                            color = StarlightMint,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (settings.musicEnabled) "Lobby & combat BGM: ${(settings.musicVolume * 100).toInt()}%" else "Soundtrack disabled",
                            color = if (settings.musicEnabled) PlasmaTurquoise else MutedText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = settings.musicEnabled,
                        onCheckedChange = { checked ->
                            val updated = settings.copy(musicEnabled = checked)
                            updateSettings(updated)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = PlasmaTurquoise,
                            uncheckedThumbColor = MutedText,
                            uncheckedTrackColor = SurfacePanel
                        )
                    )
                }

                if (settings.musicEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.musicVolume,
                        onValueChange = { newVol ->
                            updateSettings(settings.copy(musicVolume = newVol))
                        },
                        valueRange = 0.05f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = PlasmaTurquoise,
                            activeTrackColor = PlasmaTurquoise,
                            inactiveTrackColor = SurfacePanelBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HAPTIC FEEDBACK",
                            color = StarlightMint,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Vibration on impact and events",
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { checked ->
                            updateSettings(settings.copy(vibrationEnabled = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = PlasmaTurquoise,
                            uncheckedThumbColor = MutedText,
                            uncheckedTrackColor = SurfacePanel
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reduced Visual Effects
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REDUCED EFFECTS",
                            color = StarlightMint,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Lower particle load for performance",
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.reducedVisualEffects,
                        onCheckedChange = { checked ->
                            updateSettings(settings.copy(reducedVisualEffects = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = PlasmaTurquoise,
                            uncheckedThumbColor = MutedText,
                            uncheckedTrackColor = SurfacePanel
                        )
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Steering Sensitivity Selector
                Text(
                    text = "STEERING SENSITIVITY",
                    color = StarlightMint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Adjust lateral thruster responsiveness",
                    color = MutedText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SteeringSensitivity.values().forEach { sens ->
                        val isSelected = settings.steeringSensitivity == sens
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
                                .clickable {
                                    updateSettings(settings.copy(steeringSensitivity = sens))
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sens.label.uppercase(),
                                color = if (isSelected) ObsidianBlack else StarlightMint,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Management Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfacePanel)
                    .border(1.5.dp, SurfacePanelBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "DATA MANAGEMENT",
                    color = StarlightMint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Manage campaign progress and high scores",
                    color = MutedText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OrbitButton(
                    text = "RESET BEST SCORE",
                    onClick = { showResetConfirmation = true },
                    isPrimary = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                OrbitButton(
                    text = "RESET CAMPAIGN PROGRESS",
                    onClick = { showResetCampaignConfirmation = true },
                    isPrimary = false
                )
            }
        }

        // Reset Best Score Confirmation Modal
        if (showResetConfirmation) {
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
                        text = "CONFIRM RESET",
                        color = AuroraWarning,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Are you sure you want to reset your orbital record to 0? This action cannot be undone.",
                        color = StarlightMint,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OrbitButton(
                        text = "YES, RESET RECORD",
                        onClick = {
                            storage.resetBestScore()
                            showResetConfirmation = false
                        },
                        isPrimary = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OrbitButton(
                        text = "CANCEL",
                        onClick = { showResetConfirmation = false },
                        isPrimary = false
                    )
                }
            }
        }

        // Reset Campaign Confirmation Modal
        if (showResetCampaignConfirmation) {
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
                        text = "RESET CAMPAIGN",
                        color = ImpactCoral,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reset all 20 campaign sectors back to Level 1? Completed level stars will be cleared.",
                        color = StarlightMint,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OrbitButton(
                        text = "YES, RESET CAMPAIGN",
                        onClick = {
                            storage.resetCampaignProgress()
                            showResetCampaignConfirmation = false
                        },
                        isPrimary = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OrbitButton(
                        text = "CANCEL",
                        onClick = { showResetCampaignConfirmation = false },
                        isPrimary = false
                    )
                }
            }
        }
    }
}
