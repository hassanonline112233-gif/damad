package com.orbitbreaker.survival.game

import androidx.compose.ui.graphics.Color
import com.orbitbreaker.survival.data.CampaignCatalog
import com.orbitbreaker.survival.data.CampaignLevel
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LevelObjectiveType
import com.orbitbreaker.survival.data.LocalGameStorage
import com.orbitbreaker.survival.data.PlayerSettings
import com.orbitbreaker.survival.data.RewardManager
import com.orbitbreaker.survival.data.ShipCatalog
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    val storage: LocalGameStorage,
    val audioManager: AudioManager
) {
    var gameState: GameState = GameState.READY
        private set

    val rocket = Rocket()
    val meteors = mutableListOf<Meteor>()
    val activeEnemies = mutableListOf<EnemyShip>()
    val activeProjectiles = mutableListOf<Projectile>()
    val activePickups = mutableListOf<CollectiblePickup>()
    val combatFeedbacks = mutableListOf<CombatFeedback>()
    val powerupManager = ActivePowerupManager()
    var activeBoss: Boss? = null
        private set

    val particleSystem = ParticleSystem()
    val scoreManager = ScoreManager()
    private val spawner = MeteorSpawner()

    var settings: PlayerSettings = storage.getSettings()
        private set

    var currentLevel: CampaignLevel? = null
        private set
    var currentDifficulty: GameDifficulty = storage.getSelectedDifficulty()
        private set
    var equippedShipId: String = storage.getEquippedShipId()
        private set

    var screenWidth: Float = 1080f
        private set
    var screenHeight: Float = 1920f
        private set

    var steeringInput: Float = 0f
    var backgroundScrollOffset: Float = 0f

    fun getSectorBackgroundRes(): Int {
        val lvlId = currentLevel?.id ?: return com.orbitbreaker.survival.R.drawable.bg_sector_unlimited
        return when {
            lvlId in 1..5 -> com.orbitbreaker.survival.R.drawable.bg_sector_rings
            lvlId in 6..10 -> com.orbitbreaker.survival.R.drawable.bg_sector_nebula
            lvlId in 11..15 -> com.orbitbreaker.survival.R.drawable.bg_sector_asteroids
            lvlId in 16..20 -> com.orbitbreaker.survival.R.drawable.bg_sector_singularity
            else -> com.orbitbreaker.survival.R.drawable.bg_sector_unlimited
        }
    }

    val stars = mutableListOf<Star>()
    private var starsInitialized = false
    val orbitalRings = mutableListOf<OrbitalRing>()

    var onLevelSuccess: ((level: CampaignLevel, score: Int, survivalTime: Float, totalCredits: Int, stars: Int) -> Unit)? = null
    var onGameOver: ((level: CampaignLevel?, score: Int, survivalTime: Float, isNewRecord: Boolean) -> Unit)? = null

    private var runReported = false
    private var enemySpawnTimer = 0f
    private var nextEnemyId = 1L
    private var nextPickupId = 1L
    private var nextFeedbackId = 1L
    private var lowHpAlarmTimer = 0f
    private var bossRoarTriggered = false

    // Configurable drop chances and caps (Halved for balanced combat challenge)
    private val maxActivePickups = 3
    private val baseEnemyDropRate = 0.22f
    private val meteorDropRate = 0.10f

    init {
        refreshSettings()
    }

    fun refreshSettings() {
        settings = storage.getSettings()
        currentDifficulty = storage.getSelectedDifficulty()
        equippedShipId = storage.getEquippedShipId()
    }

    fun setScreenDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val changed = screenWidth != width || screenHeight != height
        screenWidth = width
        screenHeight = height

        if (!starsInitialized || changed) {
            initStars()
            initOrbitalRings()
            starsInitialized = true
        }

        if (gameState == GameState.READY) {
            rocket.reset(screenWidth / 2f, screenHeight * 0.78f)
        }
    }

    private fun initStars() {
        stars.clear()
        val random = Random(1337)
        val starCount = if (settings.reducedVisualEffects) 35 else 75
        for (i in 0 until starCount) {
            val xRatio = random.nextFloat()
            val yRatio = random.nextFloat()
            val speedFactor = 0.35f + random.nextFloat() * 1.35f
            val size = 1.2f + random.nextFloat() * 2.5f
            val alpha = 0.35f + random.nextFloat() * 0.65f
            stars.add(Star(xRatio, yRatio, size, speedFactor, alpha))
        }
    }

    private fun initOrbitalRings() {
        orbitalRings.clear()
        orbitalRings.add(
            OrbitalRing(
                rotationAngle = 15f,
                rotationSpeed = 4f,
                radiusX = screenWidth * 0.55f,
                radiusY = screenWidth * 0.22f,
                tiltAngle = -20f,
                strokeWidth = 2.0f,
                color = Color(0x332DE2C5)
            )
        )
        orbitalRings.add(
            OrbitalRing(
                rotationAngle = 45f,
                rotationSpeed = -6f,
                radiusX = screenWidth * 0.75f,
                radiusY = screenWidth * 0.30f,
                tiltAngle = -25f,
                strokeWidth = 1.5f,
                color = Color(0x221E6B55)
            )
        )
    }

    fun startLevel(levelId: Int, difficulty: GameDifficulty = storage.getSelectedDifficulty()) {
        refreshSettings()
        currentDifficulty = difficulty
        currentLevel = CampaignCatalog.getLevel(levelId) ?: CampaignCatalog.levels.first()
        startRunInternal()
    }

    fun startEndlessRun(difficulty: GameDifficulty = storage.getSelectedDifficulty()) {
        refreshSettings()
        currentDifficulty = difficulty
        currentLevel = null
        startRunInternal()
    }

    private fun startRunInternal() {
        gameState = GameState.PLAYING
        meteors.clear()
        activeEnemies.clear()
        activeProjectiles.clear()
        activePickups.clear()
        combatFeedbacks.clear()
        powerupManager.reset()
        activeBoss = null
        particleSystem.clear()
        scoreManager.reset()
        spawner.reset()
        steeringInput = 0f
        runReported = false
        enemySpawnTimer = 0f
        lowHpAlarmTimer = 0f
        bossRoarTriggered = false

        rocket.reset(screenWidth / 2f, screenHeight * 0.78f)

        // Trigger cinematic orbital launch ignition and stream background flight track
        audioManager.playSound(SoundEvent.LAUNCH, settings, priority = 3)
        audioManager.playMusic(MusicTrack.FLIGHT, settings)

        // Setup Boss if Level is a multiple of 5 (scaled by unified global difficulty)
        currentLevel?.let { lvl ->
            val bossType = when (lvl.id) {
                5 -> BossId.IRON_COLOSSUS
                10 -> BossId.VOID_HARVESTER
                15 -> BossId.ECLIPSE_WARDEN
                20 -> BossId.FINAL_SINGULARITY
                else -> null
            }
            if (bossType != null) {
                val scaledHp = bossType.maxHealth * currentDifficulty.bossHealthMultiplier
                activeBoss = Boss(
                    bossId = bossType,
                    x = screenWidth / 2f,
                    y = -60f,
                    health = scaledHp,
                    maxHealth = scaledHp
                )
                audioManager.playSound(SoundEvent.BOSS_ROAR, settings, priority = 4)
                audioManager.playMusic(MusicTrack.BOSS, settings)
                bossRoarTriggered = true
            }
        }
    }

    var isDragging: Boolean = false
        private set
    var dragTargetX: Float = 0f
        private set
    var dragTargetY: Float = 0f
        private set
    private var dragTouchOffsetX: Float = 0f
    private var dragTouchOffsetY: Float = 0f

    fun pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED
            steeringInput = 0f
            isDragging = false
            audioManager.pauseMusic()
        }
    }

    fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING
            audioManager.resumeMusic(settings)
        }
    }

    /**
     * Called when user touches the screen to grab / steer the ship.
     * If user touches near the ship, it grabs with touch offset so the finger doesn't obscure the ship.
     * If user touches elsewhere, the ship directly centers and follows smoothly.
     */
    fun onTouchDown(touchX: Float, touchY: Float) {
        if (gameState != GameState.PLAYING) return
        isDragging = true

        val dist = kotlin.math.hypot(touchX - rocket.x, touchY - rocket.y)
        if (dist <= 120f) {
            // Grab offset so user finger does not cover ship view
            dragTouchOffsetX = rocket.x - touchX
            dragTouchOffsetY = rocket.y - touchY
        } else {
            // Touch directly pulls ship towards finger with slight upward offset for finger visibility
            dragTouchOffsetX = 0f
            dragTouchOffsetY = -30f
        }
        dragTargetX = touchX + dragTouchOffsetX
        dragTargetY = touchY + dragTouchOffsetY
    }

    /**
     * Called continuously as user drags their finger anywhere across the screen.
     */
    fun onTouchMove(touchX: Float, touchY: Float) {
        if (gameState != GameState.PLAYING) return
        isDragging = true
        dragTargetX = touchX + dragTouchOffsetX
        dragTargetY = touchY + dragTouchOffsetY
    }

    /**
     * Called when user lifts their finger off the screen.
     */
    fun onTouchUp() {
        isDragging = false
        steeringInput = 0f
    }

    fun setSteering(dir: Float) {
        if (gameState == GameState.PLAYING) {
            steeringInput = dir
        }
    }

    fun releaseSteering() {
        steeringInput = 0f
        isDragging = false
    }

    fun update(rawDt: Float) {
        val dt = rawDt.coerceIn(0.001f, 0.05f)
        updateBackground(dt)

        if (gameState != GameState.PLAYING) return

        scoreManager.update(dt)
        powerupManager.update(dt)

        // Update feedback notifications
        val fbIt = combatFeedbacks.iterator()
        while (fbIt.hasNext()) {
            val fb = fbIt.next()
            fb.update(dt)
            if (fb.isExpired) fbIt.remove()
        }

        // Check level objective completion
        currentLevel?.let { level ->
            val isBossLevel = level.id % 5 == 0
            if (isBossLevel) {
                if (activeBoss?.isDefeated == true && !runReported) {
                    handleLevelSuccess(level)
                    return
                }
            } else {
                val scoreMult = currentDifficulty.scoreMultiplier
                val objectiveAchieved = when (level.objectiveType) {
                    LevelObjectiveType.SURVIVE_TIME -> scoreManager.survivalTimeSeconds >= level.targetValue
                    LevelObjectiveType.REACH_SCORE -> (scoreManager.currentScore * scoreMult).toInt() >= level.targetValue.toInt()
                }
                if (objectiveAchieved && !runReported) {
                    handleLevelSuccess(level)
                    return
                }
            }
        }

        // 1. Rocket update & auto-fire
        val sensitivity = settings.steeringSensitivity
        val ship = ShipCatalog.getShipById(equippedShipId)
        val accel = sensitivity.acceleration * ship.accelerationBonus
        val maxSpd = sensitivity.maxSpeed * ship.maxSpeedBonus

        if (isDragging) {
            rocket.moveToTarget(
                targetX = dragTargetX,
                targetY = dragTargetY,
                dt = dt,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        } else {
            rocket.update(
                dt = dt,
                targetDir = steeringInput,
                acceleration = accel,
                maxSpeed = maxSpd,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        }

        if (rocket.invincibilityTimer > 0f) {
            rocket.invincibilityTimer -= dt
        }
        if (rocket.hitFlashTimer > 0f) {
            rocket.hitFlashTimer -= dt
        }

        rocket.auraPulseTimer += dt

        // Low health pulsing warning alarm
        if (rocket.health > 0f && (rocket.health / rocket.maxHealth) <= 0.30f) {
            lowHpAlarmTimer += dt
            if (lowHpAlarmTimer >= 1.6f) {
                lowHpAlarmTimer = 0f
                audioManager.playSound(SoundEvent.WARNING_LOW_HP, settings, priority = 3)
            }
        } else {
            lowHpAlarmTimer = 1.2f
        }

        // Dynamic weapon progression auto-fire
        val powerSpec = ShipPowerProgression.getSpec(rocket.powerLevel)
        val rapidFireMultiplier = if (powerupManager.isRapidFireActive) 0.58f else 1.0f
        val effectiveFireInterval = (powerSpec.fireInterval * rapidFireMultiplier) / ship.maxSpeedBonus

        rocket.fireTimer += dt
        if (rocket.fireTimer >= effectiveFireInterval) {
            rocket.fireTimer = 0f
            emitPlayerProjectiles(powerSpec)
        }

        particleSystem.spawnEngineTrail(rocket.x, rocket.y, settings.reducedVisualEffects)

        // 2. Spawn Meteors
        val threatStage = if (currentLevel == null) com.orbitbreaker.survival.data.ThreatDirector.getCurrentStage(scoreManager.survivalTimeSeconds) else null
        val threatSpeedMod = threatStage?.speedMod ?: 1.0f
        val threatSpawnMod = threatStage?.spawnIntervalMod ?: 1.0f

        val speedMod = (currentLevel?.baseSpeedModifier ?: 1.0f) * threatSpeedMod
        val spawnMod = ((currentLevel?.spawnRateModifier ?: 1.0f) / threatSpawnMod) * (if (activeBoss != null) 0.35f else 1.0f)

        val newMeteor = spawner.update(
            dt = dt,
            survivalTimeSeconds = scoreManager.survivalTimeSeconds,
            screenWidth = screenWidth,
            activeMeteors = meteors,
            rocketX = rocket.x,
            rocketWidth = rocket.width,
            levelSpeedModifier = speedMod,
            levelSpawnModifier = spawnMod,
            difficulty = currentDifficulty
        )
        newMeteor?.let { meteors.add(it) }

        // 3. Spawn Enemy Ships (or maintain waves when battlefield empty)
        val waveCooldownFinished = activeEnemies.isEmpty() || enemySpawnTimer >= (4.5f / (currentDifficulty.speedMultiplier * (currentLevel?.spawnRateModifier ?: 1f))).coerceAtLeast(1.8f)
        if (activeBoss == null || activeBoss!!.healthRatio > 0.6f) {
            enemySpawnTimer += dt
            if (waveCooldownFinished && activeEnemies.size < 6) {
                enemySpawnTimer = 0f
                spawnEnemy()
            }
        }

        // 4. Update Boss
        activeBoss?.let { boss ->
            val bossShots = boss.update(dt, screenWidth, currentDifficulty.bossFireRateMultiplier)
            if (bossShots.isNotEmpty()) {
                audioManager.playSound(SoundEvent.ENEMY_LASER, settings)
            }
            activeProjectiles.addAll(bossShots)
        }

        // 5. Update Enemies
        val enemyIt = activeEnemies.iterator()
        while (enemyIt.hasNext()) {
            val enemy = enemyIt.next()
            val enemyShots = enemy.update(dt)
            if (enemyShots.isNotEmpty()) {
                audioManager.playSound(SoundEvent.ENEMY_LASER, settings)
            }
            activeProjectiles.addAll(enemyShots)

            // Direct hull crash with rocket
            if (rocket.invincibilityTimer <= 0f && hypot(rocket.x - enemy.x, rocket.y - enemy.y) < (rocket.width * 0.45f + enemy.type.width * 0.45f)) {
                particleSystem.spawnImpactBurst(rocket.x, rocket.y, settings.reducedVisualEffects)
                takePlayerDamage(30f)
                rollEnemyDrop(enemy.x, enemy.y, enemy.type.relativeHealth)
                audioManager.playSound(SoundEvent.ENEMY_EXPLODE, settings)
                enemyIt.remove()
                if (gameState == GameState.GAME_OVER) return
                continue
            }

            if (enemy.y > screenHeight + 50f) {
                enemyIt.remove()
            }
        }

        // 6. Update Meteors
        val meteorIterator = meteors.iterator()
        while (meteorIterator.hasNext()) {
            val meteor = meteorIterator.next()
            meteor.update(dt)

            if (rocket.invincibilityTimer <= 0f && CollisionSystem.checkCollision(rocket, meteor)) {
                particleSystem.spawnImpactBurst(rocket.x, rocket.y, settings.reducedVisualEffects)
                val meteorDamage = when (meteor.size) {
                    MeteorSize.SMALL -> 20f
                    MeteorSize.MEDIUM -> 30f
                    MeteorSize.LARGE -> 45f
                }
                takePlayerDamage(meteorDamage)
                rollMeteorDrop(meteor.x, meteor.y)
                audioManager.playSound(SoundEvent.ENEMY_EXPLODE, settings)
                meteorIterator.remove()
                if (gameState == GameState.GAME_OVER) return
                continue
            }

            if (meteor.isOffScreen(screenHeight)) {
                meteorIterator.remove()
            }
        }

        // 7. Update Collectible Pickups
        val pickupIt = activePickups.iterator()
        while (pickupIt.hasNext()) {
            val pickup = pickupIt.next()
            pickup.update(dt)

            // Player collection detection (forgiving magnetic / radius capture)
            val collectDistance = rocket.width * 0.65f + pickup.radius
            if (!pickup.isCollected && hypot(rocket.x - pickup.x, rocket.y - pickup.y) < collectDistance) {
                pickup.isCollected = true
                applyPickupReward(pickup)
                particleSystem.spawnPickupSparkle(pickup.x, pickup.y, pickup.type.primaryColor, settings.reducedVisualEffects)
                pickupIt.remove()
                continue
            }

            if (pickup.isOffScreen(screenHeight)) {
                pickupIt.remove()
            }
        }

        // 8. Update Projectiles & Combat Collisions
        val projIt = activeProjectiles.iterator()
        while (projIt.hasNext()) {
            val proj = projIt.next()
            proj.update(dt)

            if (proj.isPlayer) {
                // Check collision with meteors
                var hitObstacle = false
                val mIt = meteors.iterator()
                while (mIt.hasNext()) {
                    val meteor = mIt.next()
                    if (hypot(proj.x - meteor.x, proj.y - meteor.y) < meteor.radius) {
                        meteor.health -= proj.damage
                        particleSystem.spawnImpactBurst(proj.x, proj.y, settings.reducedVisualEffects)
                        hitObstacle = true

                        if (meteor.health <= 0f) {
                            particleSystem.spawnImpactBurst(meteor.x, meteor.y, settings.reducedVisualEffects)
                            scoreManager.addBonusPoints(meteor.size.scorePoints)
                            rollMeteorDrop(meteor.x, meteor.y)
                            audioManager.playSound(SoundEvent.ENEMY_EXPLODE, settings)
                            mIt.remove()
                        }
                        break
                    }
                }
                if (hitObstacle) {
                    projIt.remove()
                    continue
                }

                // Check collision with enemies
                var hitEnemy = false
                val eIt = activeEnemies.iterator()
                while (eIt.hasNext()) {
                    val enemy = eIt.next()
                    if (hypot(proj.x - enemy.x, proj.y - enemy.y) < enemy.type.width * 0.65f) {
                        enemy.health -= proj.damage
                        particleSystem.spawnImpactBurst(proj.x, proj.y, settings.reducedVisualEffects)
                        hitEnemy = true

                        if (enemy.health <= 0f) {
                            particleSystem.spawnImpactBurst(enemy.x, enemy.y, settings.reducedVisualEffects)
                            val coinBonus = if (powerupManager.isDoubleCoinsActive) enemy.type.creditValue * 2 else enemy.type.creditValue
                            scoreManager.addBonusPoints(enemy.type.baseScore)
                            storage.addCredits(coinBonus)
                            rollEnemyDrop(enemy.x, enemy.y, enemy.type.relativeHealth)
                            audioManager.playSound(SoundEvent.ENEMY_EXPLODE, settings)
                            eIt.remove()
                        }
                        break
                    }
                }
                if (hitEnemy) {
                    projIt.remove()
                    continue
                }

                // Check collision with Boss
                var hitBoss = false
                activeBoss?.let { boss ->
                    if (hypot(proj.x - boss.x, proj.y - boss.y) < boss.width * 0.48f) {
                        particleSystem.spawnImpactBurst(proj.x, proj.y, settings.reducedVisualEffects)
                        scoreManager.addBonusPoints(15)
                        if (boss.takeDamage(proj.damage)) {
                            particleSystem.spawnImpactBurst(boss.x, boss.y, settings.reducedVisualEffects)
                            audioManager.playSound(SoundEvent.BOSS_DEFEAT, settings, priority = 5)
                            // Small chance of dropping power core during boss struggle
                            if (Random.nextFloat() < 0.20f && activePickups.size < maxActivePickups) {
                                spawnPickup(boss.x + (Random.nextFloat() - 0.5f) * 60f, boss.y + 40f, PickupType.POWER_CORE)
                            }
                        }
                        hitBoss = true
                    }
                }
                if (hitBoss) {
                    projIt.remove()
                    continue
                }
            } else {
                // Enemy projectile hitting player rocket
                if (rocket.invincibilityTimer <= 0f && hypot(proj.x - rocket.x, proj.y - rocket.y) < rocket.width * 0.45f) {
                    particleSystem.spawnImpactBurst(rocket.x, rocket.y, settings.reducedVisualEffects)
                    projIt.remove()
                    takePlayerDamage(18f)
                    if (gameState == GameState.GAME_OVER) return
                    continue
                }
            }

            if (proj.isOffScreen(screenHeight, screenWidth)) {
                projIt.remove()
            }
        }

        particleSystem.update(dt)
    }

    private fun emitPlayerProjectiles(powerSpec: ShipPowerLevelSpec) {
        val baseDamage = powerSpec.damage * (if (powerupManager.isHeavyShotActive) 1.8f else 1.0f)
        val speed = powerSpec.projectileSpeed
        val count = if (powerupManager.isSpreadShotActive) (powerSpec.projectileCount + 2).coerceAtMost(9) else powerSpec.projectileCount
        val spreadAngle = if (powerupManager.isSpreadShotActive) (powerSpec.spreadAngleDeg + 16f) else powerSpec.spreadAngleDeg
        val isSpecial = rocket.powerLevel == 10 || powerupManager.isHeavyShotActive

        // Trigger differentiated weapon sound
        when {
            rocket.powerLevel == 10 -> audioManager.playSound(SoundEvent.LASER_MAX, settings)
            powerupManager.isHeavyShotActive || rocket.powerLevel >= 6 -> audioManager.playSound(SoundEvent.LASER_HEAVY, settings)
            else -> audioManager.playSound(SoundEvent.LASER_STANDARD, settings)
        }

        val bulletColor = when {
            powerupManager.isHeavyShotActive -> Color(0xFFFF4D4D)
            rocket.powerLevel == 10 -> Color(0xFFD7FFF4) // Radiant starlight cyan
            powerupManager.isSpreadShotActive -> Color(0xFFA855F7)
            else -> Color(0xFF2DE2C5)
        }

        if (count == 1) {
            activeProjectiles.add(
                Projectile(
                    x = rocket.x,
                    y = rocket.y - 30f,
                    vx = 0f,
                    vy = -speed,
                    isPlayer = true,
                    damage = baseDamage,
                    radius = powerSpec.projectileRadius,
                    color = bulletColor,
                    angleDeg = 0f,
                    isSpecial = isSpecial
                )
            )
        } else {
            val stepAngle = if (count > 1 && spreadAngle > 0f) spreadAngle / (count - 1) else 0f
            val startAngle = -spreadAngle / 2f

            for (i in 0 until count) {
                val angleDeg = startAngle + i * stepAngle
                val rad = Math.toRadians((angleDeg - 90.0)).toFloat()
                val vx = cos(rad.toDouble()).toFloat() * speed
                val vy = sin(rad.toDouble()).toFloat() * speed

                val offsetX = (i - (count - 1) / 2f) * 10f
                activeProjectiles.add(
                    Projectile(
                        x = rocket.x + offsetX,
                        y = rocket.y - 28f,
                        vx = vx,
                        vy = vy,
                        isPlayer = true,
                        damage = baseDamage,
                        radius = powerSpec.projectileRadius,
                        color = bulletColor,
                        angleDeg = angleDeg,
                        isSpecial = isSpecial
                    )
                )
            }
        }
    }

    private fun rollEnemyDrop(x: Float, y: Float, enemyWeight: Float) {
        if (activePickups.size >= maxActivePickups) return
        val dropChance = (baseEnemyDropRate + enemyWeight * 0.04f).coerceAtMost(0.42f)
        if (Random.nextFloat() <= dropChance) {
            val type = selectRandomPickup()
            spawnPickup(x, y, type)
        }
    }

    private fun rollMeteorDrop(x: Float, y: Float) {
        if (activePickups.size >= maxActivePickups) return
        if (Random.nextFloat() <= meteorDropRate) {
            val type = selectRandomPickup()
            spawnPickup(x, y, type)
        }
    }

    private fun selectRandomPickup(): PickupType {
        val all = PickupType.values()
        val totalWeight = all.sumOf { it.weight.toDouble() }.toFloat()
        var r = Random.nextFloat() * totalWeight
        for (item in all) {
            r -= item.weight
            if (r <= 0f) return item
        }
        return PickupType.POWER_CORE
    }

    private fun spawnPickup(x: Float, y: Float, type: PickupType) {
        if (activePickups.size >= maxActivePickups) return
        activePickups.add(
            CollectiblePickup(
                id = nextPickupId++,
                type = type,
                x = x.coerceIn(40f, screenWidth - 40f),
                y = y,
                vy = 125f + Random.nextFloat() * 45f
            )
        )
    }

    private fun applyPickupReward(pickup: CollectiblePickup) {
        audioManager.triggerPickupFeedback(settings)

        when (pickup.type) {
            PickupType.POWER_CORE -> {
                audioManager.playSound(SoundEvent.PICKUP_CORE, settings, priority = 3)
                if (rocket.powerLevel < ShipPowerProgression.MAX_POWER_LEVEL) {
                    rocket.powerLevel++
                    showNotification(pickup.type.notificationText, pickup.type.primaryColor)
                } else {
                    // Level 10 overflow reward: +40 coins & +250 score
                    val coinBonus = if (powerupManager.isDoubleCoinsActive) ShipPowerProgression.MAX_POWER_OVERFLOW_CREDITS * 2 else ShipPowerProgression.MAX_POWER_OVERFLOW_CREDITS
                    storage.addCredits(coinBonus)
                    scoreManager.addBonusPoints(ShipPowerProgression.MAX_POWER_OVERFLOW_SCORE)
                    showNotification("MAX POWER: +${coinBonus} CREDITS", Color(0xFFD7FFF4))
                }
            }
            PickupType.SHIELD -> {
                audioManager.playSound(SoundEvent.PICKUP_BUFF, settings, priority = 3)
                powerupManager.activate(PickupType.SHIELD)
                showNotification(pickup.type.notificationText, pickup.type.primaryColor)
            }
            PickupType.HEALTH_REPAIR -> {
                audioManager.playSound(SoundEvent.PICKUP_BUFF, settings, priority = 3)
                val repairAmount = 35f
                val previousHp = rocket.health
                rocket.health = (rocket.health + repairAmount).coerceAtMost(rocket.maxHealth)
                val actualHealed = rocket.health - previousHp
                if (actualHealed > 0f) {
                    showNotification("HEALTH RESTORED (+${actualHealed.toInt()})", pickup.type.primaryColor)
                } else {
                    showNotification("HULL AT MAXIMUM", Color(0xFFDCFCE7))
                }
            }
            PickupType.DOUBLE_COINS -> {
                audioManager.playSound(SoundEvent.PICKUP_BUFF, settings, priority = 2)
                powerupManager.activate(PickupType.DOUBLE_COINS)
                showNotification(pickup.type.notificationText, pickup.type.primaryColor)
            }
            PickupType.RAPID_FIRE -> {
                audioManager.playSound(SoundEvent.PICKUP_BUFF, settings, priority = 2)
                powerupManager.activate(PickupType.RAPID_FIRE)
                showNotification(pickup.type.notificationText, pickup.type.primaryColor)
            }
            PickupType.SPREAD_SHOT -> {
                audioManager.playSound(SoundEvent.PICKUP_BUFF, settings, priority = 2)
                powerupManager.activate(PickupType.SPREAD_SHOT)
                showNotification(pickup.type.notificationText, pickup.type.primaryColor)
            }
            PickupType.HEAVY_SHOT -> {
                audioManager.playSound(SoundEvent.PICKUP_BUFF, settings, priority = 2)
                powerupManager.activate(PickupType.HEAVY_SHOT)
                showNotification(pickup.type.notificationText, pickup.type.primaryColor)
            }
            PickupType.SCREEN_CLEAR_BOMB -> {
                audioManager.playSound(SoundEvent.BOMB_NOVA, settings, priority = 4)
                triggerScreenClearBomb()
                showNotification(pickup.type.notificationText, pickup.type.primaryColor)
            }
            PickupType.COIN_CACHE -> {
                audioManager.playSound(SoundEvent.PICKUP_COIN, settings, priority = 2)
                val base = 50
                val amount = if (powerupManager.isDoubleCoinsActive) base * 2 else base
                storage.addCredits(amount)
                showNotification("+$amount CREDITS", pickup.type.primaryColor)
            }
        }
    }

    private fun triggerScreenClearBomb() {
        audioManager.triggerBombFeedback(settings)
        particleSystem.spawnNovaBombEffect(screenWidth, screenHeight, settings.reducedVisualEffects)

        // Clear or heavily damage ordinary enemies
        val eIt = activeEnemies.iterator()
        while (eIt.hasNext()) {
            val enemy = eIt.next()
            particleSystem.spawnImpactBurst(enemy.x, enemy.y, settings.reducedVisualEffects)
            val coinBonus = if (powerupManager.isDoubleCoinsActive) enemy.type.creditValue * 2 else enemy.type.creditValue
            scoreManager.addBonusPoints(enemy.type.baseScore)
            storage.addCredits(coinBonus)
            eIt.remove()
        }

        // Obliterate meteors
        val mIt = meteors.iterator()
        while (mIt.hasNext()) {
            val m = mIt.next()
            particleSystem.spawnImpactBurst(m.x, m.y, settings.reducedVisualEffects)
            scoreManager.addBonusPoints(m.size.scorePoints)
            mIt.remove()
        }

        // Apply balanced significant damage to Boss if present
        activeBoss?.let { boss ->
            val bombDamage = 25f * currentDifficulty.enemyHealthMultiplier
            if (boss.takeDamage(bombDamage)) {
                particleSystem.spawnImpactBurst(boss.x, boss.y, settings.reducedVisualEffects)
            }
        }
    }

    private fun showNotification(text: String, color: Color) {
        combatFeedbacks.add(
            CombatFeedback(
                id = nextFeedbackId++,
                text = text,
                color = color,
                x = rocket.x,
                y = rocket.y - 50f
            )
        )
    }

    private fun spawnEnemy() {
        val threatStage = if (currentLevel == null) com.orbitbreaker.survival.data.ThreatDirector.getCurrentStage(scoreManager.survivalTimeSeconds) else null
        val currentTier = threatStage?.tier ?: 1

        val rand = Random.nextFloat()
        val type = when {
            // Higher tiers introduce heavy destroyers and swarm carriers in unlimited mode
            currentTier >= 4 && rand < 0.20f -> EnemyType.HEAVY_DESTROYER
            currentTier >= 3 && rand < 0.35f -> EnemyType.SWARM_CARRIER
            rand < 0.25f -> EnemyType.SCOUT_DRONE
            rand < 0.48f -> EnemyType.METEOR_SKIMMER
            rand < 0.70f -> EnemyType.STRIKER_FIGHTER
            rand < 0.88f -> EnemyType.ARMORED_INTERCEPTOR
            else -> EnemyType.ELITE_HUNTER
        }

        val padding = type.width + 20f
        val startX = padding + Random.nextFloat() * (screenWidth - padding * 2).coerceAtLeast(60f)
        val speedFactor = if (currentLevel == null) (1.0f + (currentTier - 1) * 0.08f) else 1.0f
        val vy = 125f * type.speedMultiplier * currentDifficulty.speedMultiplier * speedFactor

        // Health scales with difficulty and progressive unlimited tiers
        val tierHpMultiplier = if (currentLevel == null) (1.0f + (currentTier - 1) * 0.25f) else 1.0f
        val scaledHp = type.relativeHealth * 2.2f * currentDifficulty.enemyHealthMultiplier * tierHpMultiplier
        val fireRate = (2.0f / (currentDifficulty.speedMultiplier * currentDifficulty.enemyFireRateMultiplier * (1.0f + (currentTier - 1) * 0.12f))).coerceAtLeast(0.6f)

        activeEnemies.add(
            EnemyShip(
                id = nextEnemyId++,
                type = type,
                x = startX,
                y = -type.height - 10f,
                health = scaledHp,
                maxHealth = scaledHp,
                vy = vy,
                fireInterval = fireRate,
                seed = Random.nextInt(1000),
                tier = currentTier
            )
        )
    }

    private fun takePlayerDamage(amount: Float) {
        // If active shield, consume barrier and prevent health loss
        if (powerupManager.isShieldActive) {
            powerupManager.consumeShield()
            audioManager.triggerShieldAbsorbFeedback(settings)
            audioManager.playSound(SoundEvent.SHIELD_ABSORB, settings, priority = 4)
            audioManager.playSound(SoundEvent.SHIELD_BREAK, settings, priority = 3)
            particleSystem.spawnShieldAbsorbBurst(rocket.x, rocket.y, settings.reducedVisualEffects)
            rocket.invincibilityTimer = 0.8f
            showNotification("SHIELD ABSORBED IMPACT", Color(0xFF38BDF8))
            return
        }

        audioManager.triggerCollisionFeedback(settings)
        audioManager.playSound(SoundEvent.IMPACT_HULL, settings, priority = 4)
        rocket.health = (rocket.health - amount).coerceAtLeast(0f)
        rocket.hitFlashTimer = 0.25f

        if (rocket.health > 0f) {
            rocket.invincibilityTimer = 1.4f // 1.4s invulnerability buffer
        } else {
            handleGameOver()
        }
    }

    private fun handleGameOver() {
        if (gameState == GameState.GAME_OVER || runReported) return

        gameState = GameState.GAME_OVER
        runReported = true
        steeringInput = 0f

        audioManager.stopMusic()
        audioManager.playSound(SoundEvent.GAME_OVER, settings, priority = 5)

        val finalScore = (scoreManager.currentScore * currentDifficulty.scoreMultiplier).toInt()
        val finalSurvivalTime = scoreManager.survivalTimeSeconds
        val previousBest = storage.getBestScore()
        var isNewRecord = finalScore > previousBest

        if (currentLevel == null) {
            // Unlimited Mode Run: Persist difficulty-specific unlimited record
            val isUnlimitedRecord = storage.saveUnlimitedRecord(finalScore, finalSurvivalTime, currentDifficulty)
            if (isUnlimitedRecord) isNewRecord = true
        } else {
            if (isNewRecord) {
                storage.saveBestScore(finalScore)
                storage.saveBestSurvivalTime(finalSurvivalTime)
            }
        }

        onGameOver?.invoke(currentLevel, finalScore, finalSurvivalTime, isNewRecord)
    }

    private fun handleLevelSuccess(level: CampaignLevel) {
        if (gameState != GameState.PLAYING || runReported) return

        gameState = GameState.GAME_OVER
        runReported = true
        steeringInput = 0f

        audioManager.stopMusic()
        audioManager.playMusic(MusicTrack.VICTORY, settings)
        audioManager.playSound(SoundEvent.VICTORY, settings, priority = 5)

        val finalScore = (scoreManager.currentScore * currentDifficulty.scoreMultiplier).toInt()
        val finalTime = scoreManager.survivalTimeSeconds
        val wasAlreadyCompleted = storage.isLevelCompleted(level.id, currentDifficulty)

        val rewards = RewardManager.calculateRewards(
            level = level,
            score = finalScore,
            survivalTime = finalTime,
            isFirstTime = !wasAlreadyCompleted
        )

        // Boss bonus credits (first clear gives high bounty; replay gives normal reward)
        val bossBonus = if (level.id % 5 == 0) {
            if (!wasAlreadyCompleted) 150 else 50
        } else 0
        val totalReward = rewards.totalCreditsEarned + bossBonus

        storage.addCredits(totalReward)
        storage.saveLevelCompletion(
            levelId = level.id,
            score = finalScore,
            survivalTime = finalTime,
            stars = rewards.earnedStars,
            difficulty = currentDifficulty
        )

        onLevelSuccess?.invoke(
            level,
            finalScore,
            finalTime,
            totalReward,
            rewards.earnedStars
        )
    }

    private fun updateBackground(dt: Float) {
        val baseSpeed = if (gameState == GameState.PLAYING) 130f else 25f
        backgroundScrollOffset = (backgroundScrollOffset + baseSpeed * 0.45f * dt) % screenHeight

        for (star in stars) {
            var newY = star.yRatio + (baseSpeed * star.speedFactor * dt) / screenHeight
            if (newY > 1.0f) {
                newY = 0f
            }
            star.yRatio = newY
        }

        for (ring in orbitalRings) {
            ring.rotationAngle = (ring.rotationAngle + ring.rotationSpeed * dt) % 360f
        }
    }
}
