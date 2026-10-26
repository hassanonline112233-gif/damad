package com.orbitbreaker.survival.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.orbitbreaker.survival.R
import com.orbitbreaker.survival.data.PlayerSettings

enum class SoundEvent {
    SPLASH,
    BUTTON_CLICK,
    LAUNCH,
    LASER_STANDARD,
    LASER_HEAVY,
    LASER_MAX,
    ENEMY_LASER,
    IMPACT_HULL,
    SHIELD_ABSORB,
    SHIELD_BREAK,
    ENEMY_EXPLODE,
    BOSS_ROAR,
    BOSS_DEFEAT,
    PICKUP_CORE,
    PICKUP_BUFF,
    PICKUP_COIN,
    BOMB_NOVA,
    GAME_OVER,
    VICTORY,
    WARNING_LOW_HP
}

enum class MusicTrack {
    NONE,
    LOBBY,
    FLIGHT,
    BOSS,
    VICTORY
}

class AudioManager(private val context: Context) {
    private var vibrator: Vibrator? = null
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundEvent, Int>()
    private val loadedSounds = mutableSetOf<Int>()

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: MusicTrack = MusicTrack.NONE

    // Firing sound throttling to prevent audio clipping during rapid fire
    private var lastPlayerFireSoundTime = 0L
    private var lastEnemyFireSoundTime = 0L

    init {
        initVibrator()
        initSoundPool()
    }

    private fun initVibrator() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {}
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val sp = SoundPool.Builder()
                .setMaxStreams(14)
                .setAudioAttributes(audioAttributes)
                .build()

            sp.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSounds.add(sampleId)
                }
            }

            // Register all 20 discrete sound events to raw resources
            loadSound(sp, SoundEvent.SPLASH, R.raw.orbit_snd_splash)
            loadSound(sp, SoundEvent.BUTTON_CLICK, R.raw.orbit_snd_btn_click)
            loadSound(sp, SoundEvent.LAUNCH, R.raw.orbit_snd_launch)
            loadSound(sp, SoundEvent.LASER_STANDARD, R.raw.orbit_snd_laser_standard)
            loadSound(sp, SoundEvent.LASER_HEAVY, R.raw.orbit_snd_laser_heavy)
            loadSound(sp, SoundEvent.LASER_MAX, R.raw.orbit_snd_laser_max)
            loadSound(sp, SoundEvent.ENEMY_LASER, R.raw.orbit_snd_enemy_laser)
            loadSound(sp, SoundEvent.IMPACT_HULL, R.raw.orbit_snd_impact_hull)
            loadSound(sp, SoundEvent.SHIELD_ABSORB, R.raw.orbit_snd_shield_absorb)
            loadSound(sp, SoundEvent.SHIELD_BREAK, R.raw.orbit_snd_shield_break)
            loadSound(sp, SoundEvent.ENEMY_EXPLODE, R.raw.orbit_snd_enemy_explode)
            loadSound(sp, SoundEvent.BOSS_ROAR, R.raw.orbit_snd_boss_roar)
            loadSound(sp, SoundEvent.BOSS_DEFEAT, R.raw.orbit_snd_boss_defeat)
            loadSound(sp, SoundEvent.PICKUP_CORE, R.raw.orbit_snd_pickup_core)
            loadSound(sp, SoundEvent.PICKUP_BUFF, R.raw.orbit_snd_pickup_buff)
            loadSound(sp, SoundEvent.PICKUP_COIN, R.raw.orbit_snd_pickup_coin)
            loadSound(sp, SoundEvent.BOMB_NOVA, R.raw.orbit_snd_bomb_nova)
            loadSound(sp, SoundEvent.GAME_OVER, R.raw.orbit_snd_game_over)
            loadSound(sp, SoundEvent.VICTORY, R.raw.orbit_snd_victory)
            loadSound(sp, SoundEvent.WARNING_LOW_HP, R.raw.orbit_snd_warning_lowhp)

            soundPool = sp
        } catch (_: Exception) {}
    }

    private fun loadSound(sp: SoundPool, event: SoundEvent, resId: Int) {
        try {
            val id = sp.load(context, resId, 1)
            soundMap[event] = id
        } catch (_: Exception) {}
    }

    fun playSound(event: SoundEvent, settings: PlayerSettings, priority: Int = 1) {
        if (!settings.soundEnabled) return

        val soundId = soundMap[event] ?: return
        if (!loadedSounds.contains(soundId)) return

        val now = System.currentTimeMillis()
        // Rate-limit high-frequency repetitive sounds
        if (event == SoundEvent.LASER_STANDARD || event == SoundEvent.LASER_HEAVY || event == SoundEvent.LASER_MAX) {
            if (now - lastPlayerFireSoundTime < 65) return
            lastPlayerFireSoundTime = now
        } else if (event == SoundEvent.ENEMY_LASER) {
            if (now - lastEnemyFireSoundTime < 75) return
            lastEnemyFireSoundTime = now
        }

        val vol = settings.sfxVolume.coerceIn(0f, 1f)
        try {
            soundPool?.play(soundId, vol, vol, priority, 0, 1.0f)
        } catch (_: Exception) {}
    }

    // --- CINEMATIC BACKGROUND MUSIC ---
    fun playMusic(track: MusicTrack, settings: PlayerSettings) {
        if (!settings.musicEnabled) {
            stopMusic()
            return
        }

        if (currentTrack == track && mediaPlayer?.isPlaying == true) {
            updateMusicVolume(settings)
            return
        }

        stopMusic()

        val resId = when (track) {
            MusicTrack.LOBBY -> R.raw.orbit_bgm_lobby
            MusicTrack.FLIGHT -> R.raw.orbit_bgm_flight
            MusicTrack.BOSS -> R.raw.orbit_bgm_boss
            MusicTrack.VICTORY -> R.raw.orbit_bgm_victory
            MusicTrack.NONE -> return
        }

        try {
            val mp = MediaPlayer.create(context, resId) ?: return
            mp.isLooping = (track != MusicTrack.VICTORY)
            val vol = settings.musicVolume.coerceIn(0f, 1f) * 0.45f // Normalized master music scale
            mp.setVolume(vol, vol)
            mp.start()
            mediaPlayer = mp
            currentTrack = track
        } catch (_: Exception) {
            currentTrack = MusicTrack.NONE
        }
    }

    fun updateMusicVolume(settings: PlayerSettings) {
        if (!settings.musicEnabled) {
            stopMusic()
            return
        }
        val vol = settings.musicVolume.coerceIn(0f, 1f) * 0.45f
        try {
            mediaPlayer?.setVolume(vol, vol)
        } catch (_: Exception) {}
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeMusic(settings: PlayerSettings) {
        if (!settings.musicEnabled) {
            stopMusic()
            return
        }
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                updateMusicVolume(settings)
                mediaPlayer?.start()
            } else if (mediaPlayer == null && currentTrack != MusicTrack.NONE) {
                playMusic(currentTrack, settings)
            }
        } catch (_: Exception) {}
    }

    fun stopAllSounds() {
        try {
            soundPool?.autoPause()
        } catch (_: Exception) {}
    }

    fun onSettingsChanged(settings: PlayerSettings, defaultLobbyMusic: Boolean = false) {
        if (!settings.soundEnabled) {
            stopAllSounds()
        }
        if (!settings.musicEnabled) {
            stopMusic()
        } else {
            if (mediaPlayer == null && defaultLobbyMusic) {
                playMusic(MusicTrack.LOBBY, settings)
            } else {
                updateMusicVolume(settings)
            }
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        currentTrack = MusicTrack.NONE
    }

    fun release() {
        stopMusic()
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
        loadedSounds.clear()
        soundMap.clear()
    }

    // --- HAPTIC FEEDBACK ---
    fun triggerVibration(durationMs: Long, settings: PlayerSettings) {
        if (!settings.vibrationEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun triggerCollisionFeedback(settings: PlayerSettings) {
        triggerVibration(120, settings)
    }

    fun triggerPickupFeedback(settings: PlayerSettings) {
        triggerVibration(45, settings)
    }

    fun triggerShieldAbsorbFeedback(settings: PlayerSettings) {
        triggerVibration(70, settings)
    }

    fun triggerBombFeedback(settings: PlayerSettings) {
        triggerVibration(160, settings)
    }

    fun triggerButtonFeedback(settings: PlayerSettings) {
        triggerVibration(25, settings)
    }
}
