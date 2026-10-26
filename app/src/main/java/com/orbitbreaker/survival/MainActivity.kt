package com.orbitbreaker.survival

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.game.AudioManager
import com.orbitbreaker.survival.game.GameEngine
import com.orbitbreaker.survival.ui.GameNavigation
import com.orbitbreaker.survival.ui.theme.ObsidianBlack
import com.orbitbreaker.survival.ui.theme.OrbitBreakerTheme

class MainActivity : ComponentActivity() {

    private lateinit var storage: LocalGameStorage
    private lateinit var audioManager: AudioManager
    private lateinit var engine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storage = LocalGameStorage(applicationContext)
        audioManager = AudioManager(applicationContext)
        engine = GameEngine(storage, audioManager)

        setContent {
            OrbitBreakerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBlack
                ) {
                    GameNavigation(
                        storage = storage,
                        engine = engine
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        engine.pauseGame()
        audioManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        audioManager.resumeMusic(engine.settings)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}