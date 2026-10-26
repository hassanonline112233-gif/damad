package com.orbitbreaker.survival.game

import com.orbitbreaker.survival.data.GameDifficulty
import kotlin.math.abs
import kotlin.random.Random

class MeteorSpawner(
    private val difficultyController: DifficultyController = DifficultyController()
) {
    private var spawnTimer = 0f
    private var nextMeteorId = 1L
    private val random = Random(System.currentTimeMillis())

    private var lastSpawnX = -1f

    fun reset() {
        spawnTimer = 0f
        nextMeteorId = 1L
        lastSpawnX = -1f
    }

    fun update(
        dt: Float,
        survivalTimeSeconds: Float,
        screenWidth: Float,
        activeMeteors: List<Meteor>,
        rocketX: Float,
        rocketWidth: Float,
        levelSpeedModifier: Float = 1.0f,
        levelSpawnModifier: Float = 1.0f,
        difficulty: GameDifficulty = GameDifficulty.NORMAL
    ): Meteor? {
        spawnTimer += dt
        val targetInterval = difficultyController.getSpawnInterval(
            survivalTimeSeconds = survivalTimeSeconds,
            levelSpawnModifier = levelSpawnModifier,
            difficulty = difficulty
        )
        val maxActive = difficultyController.getMaxActiveMeteors(
            survivalTimeSeconds = survivalTimeSeconds,
            difficulty = difficulty
        )

        if (spawnTimer >= targetInterval) {
            spawnTimer = 0f
            if (activeMeteors.size >= maxActive) {
                return null
            }
            return spawnMeteor(
                survivalTimeSeconds = survivalTimeSeconds,
                screenWidth = screenWidth,
                activeMeteors = activeMeteors,
                rocketX = rocketX,
                rocketWidth = rocketWidth,
                levelSpeedModifier = levelSpeedModifier,
                difficulty = difficulty
            )
        }
        return null
    }

    private fun spawnMeteor(
        survivalTimeSeconds: Float,
        screenWidth: Float,
        activeMeteors: List<Meteor>,
        rocketX: Float,
        rocketWidth: Float,
        levelSpeedModifier: Float,
        difficulty: GameDifficulty
    ): Meteor {
        // Size distribution shifts as survival time increases
        val size = when {
            survivalTimeSeconds < 15f -> {
                if (random.nextFloat() < 0.70f) MeteorSize.SMALL else MeteorSize.MEDIUM
            }
            survivalTimeSeconds < 45f -> {
                val p = random.nextFloat()
                if (p < 0.45f) MeteorSize.SMALL else if (p < 0.85f) MeteorSize.MEDIUM else MeteorSize.LARGE
            }
            else -> {
                val p = random.nextFloat()
                if (p < 0.35f) MeteorSize.SMALL else if (p < 0.70f) MeteorSize.MEDIUM else MeteorSize.LARGE
            }
        }

        val padding = size.radius + 16f
        val availableWidth = (screenWidth - 2 * padding).coerceAtLeast(100f)

        // Guaranteed minimum escape corridor: scaled by difficulty factor
        val minEscapeCorridor = rocketWidth * difficulty.corridorFactor

        var bestX = padding + random.nextFloat() * availableWidth
        var attempts = 0
        while (attempts < 6) {
            val candidateX = padding + random.nextFloat() * availableWidth
            var isOverlappingTop = false

            for (m in activeMeteors) {
                if (m.y < 120f) {
                    val gap = abs(candidateX - m.x) - (size.radius + m.radius)
                    if (gap < minEscapeCorridor && gap > 0f) {
                        isOverlappingTop = true
                        break
                    }
                    if (gap <= 0f) {
                        isOverlappingTop = true
                        break
                    }
                }
            }

            if (!isOverlappingTop) {
                bestX = candidateX
                break
            }
            attempts++
        }

        lastSpawnX = bestX

        val speedMultiplier = difficultyController.getSpeedMultiplier(
            survivalTimeSeconds = survivalTimeSeconds,
            levelSpeedModifier = levelSpeedModifier,
            difficulty = difficulty
        )
        val speedVariation = 0.90f + random.nextFloat() * 0.20f
        val vy = size.baseSpeed * speedMultiplier * speedVariation

        val vx = (random.nextFloat() - 0.5f) * 45f
        val rotationSpeed = (if (random.nextBoolean()) 1 else -1) * (30f + random.nextFloat() * 60f)
        val seed = random.nextInt(1000)

        return Meteor(
            id = nextMeteorId++,
            x = bestX,
            y = -size.radius - 10f,
            vy = vy,
            vx = vx,
            size = size,
            rotationAngle = random.nextFloat() * 360f,
            rotationSpeed = rotationSpeed,
            seed = seed
        )
    }
}
