package com.orbitbreaker.survival

import com.orbitbreaker.survival.data.CampaignCatalog
import com.orbitbreaker.survival.data.GameDifficulty
import com.orbitbreaker.survival.data.LevelObjectiveType
import com.orbitbreaker.survival.data.PurchaseResult
import com.orbitbreaker.survival.data.RewardManager
import com.orbitbreaker.survival.data.ShipCatalog
import com.orbitbreaker.survival.data.SteeringSensitivity
import com.orbitbreaker.survival.data.ThreatDirector
import com.orbitbreaker.survival.game.CollisionSystem
import com.orbitbreaker.survival.game.DifficultyController
import com.orbitbreaker.survival.game.Meteor
import com.orbitbreaker.survival.game.MeteorSize
import com.orbitbreaker.survival.game.MeteorSpawner
import com.orbitbreaker.survival.game.Rocket
import com.orbitbreaker.survival.game.ScoreManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbitBreakerLogicTest {

    @Test
    fun testRocketSteeringAccelerationAndBoundaries() {
        val rocket = Rocket(x = 200f, y = 500f, width = 40f, height = 60f)
        val sensitivity = SteeringSensitivity.NORMAL

        rocket.update(
            dt = 0.2f,
            targetDir = 1f,
            acceleration = sensitivity.acceleration,
            maxSpeed = sensitivity.maxSpeed,
            screenWidth = 1000f
        )
        assertTrue("Velocity should be positive", rocket.vx > 0f)
        assertTrue("Velocity should be <= maxSpeed", rocket.vx <= sensitivity.maxSpeed)
        assertTrue("X should have moved right", rocket.x > 200f)

        for (i in 0 until 20) {
            rocket.update(
                dt = 0.1f,
                targetDir = 1f,
                acceleration = sensitivity.acceleration,
                maxSpeed = sensitivity.maxSpeed,
                screenWidth = 400f
            )
        }
        val halfW = rocket.width / 2f
        assertEquals(400f - halfW, rocket.x, 0.01f)
    }

    @Test
    fun testRocketMoveToTargetHoldAndDrag() {
        val rocket = Rocket(x = 200f, y = 500f, width = 40f, height = 60f)

        // Drag rocket up-right towards (350, 250)
        for (i in 0 until 10) {
            rocket.moveToTarget(
                targetX = 350f,
                targetY = 250f,
                dt = 0.016f,
                screenWidth = 1080f,
                screenHeight = 1920f
            )
        }
        assertTrue("Rocket should have moved right towards 350", rocket.x > 200f)
        assertTrue("Rocket should have moved up towards 250", rocket.y < 500f)
        assertTrue("Rocket should have positive vx", rocket.vx > 0f)
        assertTrue("Rocket should have negative vy", rocket.vy < 0f)

        // Clamping to screen boundaries
        rocket.moveToTarget(
            targetX = 5000f,
            targetY = -1000f,
            dt = 1.0f,
            screenWidth = 1000f,
            screenHeight = 1920f
        )
        val halfW = rocket.width / 2f
        val halfH = rocket.height / 2f
        assertTrue("Rocket X clamped to right", rocket.x <= 1000f - halfW)
        assertTrue("Rocket Y clamped to top", rocket.y >= halfH + 20f)
    }

    @Test
    fun testRocketReleaseDamping() {
        val rocket = Rocket(x = 200f, y = 500f, vx = 300f, width = 40f, height = 60f)
        val sensitivity = SteeringSensitivity.NORMAL

        rocket.update(
            dt = 0.2f,
            targetDir = 0f,
            acceleration = sensitivity.acceleration,
            maxSpeed = sensitivity.maxSpeed,
            screenWidth = 400f
        )
        assertTrue(rocket.vx < 300f)
        assertTrue(rocket.vx >= 0f)
    }

    @Test
    fun testScoreManagerAccumulation() {
        val scoreManager = ScoreManager()
        scoreManager.reset()

        scoreManager.update(1.0f)
        assertEquals(10, scoreManager.currentScore)

        scoreManager.update(1.0f)
        assertEquals(20, scoreManager.currentScore)

        scoreManager.update(0.5f)
        assertEquals(20, scoreManager.currentScore)

        scoreManager.update(0.5f)
        assertEquals(30, scoreManager.currentScore)
    }

    @Test
    fun testCollisionDetectionDirectHitAndNearMiss() {
        val rocket = Rocket(x = 200f, y = 500f, width = 40f, height = 60f)

        val directHitMeteor = Meteor(
            id = 1L,
            x = 200f,
            y = 480f,
            vy = 200f,
            size = MeteorSize.MEDIUM
        )
        assertTrue(CollisionSystem.checkCollision(rocket, directHitMeteor))

        val distantMeteor = Meteor(
            id = 2L,
            x = 350f,
            y = 500f,
            vy = 200f,
            size = MeteorSize.SMALL
        )
        assertFalse(CollisionSystem.checkCollision(rocket, distantMeteor))
    }

    @Test
    fun testDifficultyScalingAndCapping() {
        val difficulty = DifficultyController()

        val earlyInterval = difficulty.getSpawnInterval(5f)
        val midInterval = difficulty.getSpawnInterval(30f)
        val lateInterval = difficulty.getSpawnInterval(100f)

        assertTrue(earlyInterval > midInterval)
        assertTrue(midInterval > lateInterval)

        val earlySpeed = difficulty.getSpeedMultiplier(5f)
        val lateSpeed = difficulty.getSpeedMultiplier(100f)
        assertTrue(lateSpeed > earlySpeed)
    }

    @Test
    fun testEasyNormalHardModesModifyGameplay() {
        val difficulty = DifficultyController()
        val easyInterval = difficulty.getSpawnInterval(20f, 1.0f, GameDifficulty.EASY)
        val normalInterval = difficulty.getSpawnInterval(20f, 1.0f, GameDifficulty.NORMAL)
        val hardInterval = difficulty.getSpawnInterval(20f, 1.0f, GameDifficulty.HARD)

        // Easy has longer intervals (fewer obstacles) than Normal, Hard has shorter (more obstacles)
        assertTrue(easyInterval > normalInterval)
        assertTrue(normalInterval > hardInterval)

        val easySpeed = difficulty.getSpeedMultiplier(20f, 1.0f, GameDifficulty.EASY)
        val normalSpeed = difficulty.getSpeedMultiplier(20f, 1.0f, GameDifficulty.NORMAL)
        val hardSpeed = difficulty.getSpeedMultiplier(20f, 1.0f, GameDifficulty.HARD)

        assertTrue(easySpeed < normalSpeed)
        assertTrue(normalSpeed < hardSpeed)
    }

    @Test
    fun testCampaignCatalogContainsExactly20PlayableLevels() {
        assertEquals(20, CampaignCatalog.MAX_PLAYABLE_LEVEL)
        assertEquals(20, CampaignCatalog.levels.size)
        for (i in 1..20) {
            val lvl = CampaignCatalog.getLevel(i)
            assertNotNull("Level $i must exist", lvl)
            assertEquals(i, lvl!!.id)
            assertTrue(lvl.targetValue > 0f)
            assertTrue(lvl.baseRewardCredits > 0)
        }
    }

    @Test
    fun testRewardManagerCalculationsAndFirstTimeBonus() {
        val level1 = CampaignCatalog.getLevel(1)!!
        // Target is 20s
        // Scenario 1: First-time clear with 25s survival (eligible for objective bonus)
        val rewardsFirstTime = RewardManager.calculateRewards(
            level = level1,
            score = 250,
            survivalTime = 25f,
            isFirstTime = true
        )
        assertTrue(rewardsFirstTime.isFirstTime)
        assertEquals(level1.firstTimeBonusCredits, rewardsFirstTime.firstTimeBonus)
        assertEquals(level1.baseRewardCredits, rewardsFirstTime.baseCredits)
        assertTrue(rewardsFirstTime.totalCreditsEarned > level1.baseRewardCredits)
        assertTrue(rewardsFirstTime.earnedStars >= 2)

        // Scenario 2: Replay (isFirstTime = false)
        val rewardsReplay = RewardManager.calculateRewards(
            level = level1,
            score = 250,
            survivalTime = 25f,
            isFirstTime = false
        )
        assertFalse(rewardsReplay.isFirstTime)
        assertEquals(0, rewardsReplay.firstTimeBonus)
        assertEquals(rewardsFirstTime.totalCreditsEarned - level1.firstTimeBonusCredits, rewardsReplay.totalCreditsEarned)
    }

    @Test
    fun testThreatDirectorStagesEscalation() {
        val stage1 = ThreatDirector.getCurrentStage(10f)
        val stage3 = ThreatDirector.getCurrentStage(200f)
        val stage6 = ThreatDirector.getCurrentStage(800f)

        assertEquals(1, stage1.tier)
        assertEquals(3, stage3.tier)
        assertEquals(6, stage6.tier)

        // As stages progress, speed modifier increases and spawn interval modifier tightens
        assertTrue(stage6.speedMod > stage1.speedMod)
        assertTrue(stage6.spawnIntervalMod < stage1.spawnIntervalMod)
    }

    @Test
    fun testShipCatalogIntegrity() {
        assertTrue(ShipCatalog.ships.size >= 5)
        val starter = ShipCatalog.ships.first()
        assertEquals(0, starter.price) // Free starter ship
        for (ship in ShipCatalog.ships) {
            assertTrue(ship.accelerationBonus >= 1.0f)
            assertTrue(ship.maxSpeedBonus >= 1.0f)
        }
    }

    @Test
    fun testShipPowerProgressionSpecs() {
        assertEquals(10, com.orbitbreaker.survival.game.ShipPowerProgression.powerLevels.size)
        val level1 = com.orbitbreaker.survival.game.ShipPowerProgression.getSpec(1)
        val level5 = com.orbitbreaker.survival.game.ShipPowerProgression.getSpec(5)
        val level10 = com.orbitbreaker.survival.game.ShipPowerProgression.getSpec(10)

        // Damage scales up
        assertTrue(level10.damage > level5.damage)
        assertTrue(level5.damage > level1.damage)

        // Fire interval tightens (firing faster)
        assertTrue(level10.fireInterval < level5.fireInterval)
        assertTrue(level5.fireInterval < level1.fireInterval)

        // Projectile counts increase
        assertEquals(1, level1.projectileCount)
        assertEquals(3, level5.projectileCount)
        assertEquals(7, level10.projectileCount)

        // Bounds clamping
        val clampedUnder = com.orbitbreaker.survival.game.ShipPowerProgression.getSpec(-5)
        val clampedOver = com.orbitbreaker.survival.game.ShipPowerProgression.getSpec(99)
        assertEquals(1, clampedUnder.level)
        assertEquals(10, clampedOver.level)
    }

    @Test
    fun testActivePowerupManagerTimersAndShieldAbsorption() {
        val manager = com.orbitbreaker.survival.game.ActivePowerupManager()
        assertFalse(manager.isShieldActive)
        assertFalse(manager.isRapidFireActive)

        // Activate Shield
        manager.activate(com.orbitbreaker.survival.game.PickupType.SHIELD)
        assertTrue(manager.isShieldActive)
        assertEquals(8.0f, manager.shieldTimer, 0.01f)

        // Consume Shield
        val consumed = manager.consumeShield()
        assertTrue(consumed)
        assertFalse(manager.isShieldActive)
        assertEquals(0f, manager.shieldTimer, 0.01f)

        // Consume again should return false
        assertFalse(manager.consumeShield())

        // Test Rapid fire duration expiry
        manager.activate(com.orbitbreaker.survival.game.PickupType.RAPID_FIRE)
        assertTrue(manager.isRapidFireActive)
        manager.update(5.0f)
        assertTrue(manager.isRapidFireActive)
        manager.update(6.0f) // exceeds 10s
        assertFalse(manager.isRapidFireActive)
    }

    @Test
    fun testPickupTypesRarityAndValues() {
        val pickups = com.orbitbreaker.survival.game.PickupType.values()
        assertEquals(9, pickups.size)
        for (pickup in pickups) {
            assertTrue(pickup.title.isNotBlank())
            assertTrue(pickup.notificationText.isNotBlank())
            assertTrue(pickup.weight > 0f)
        }
    }

    @Test
    fun testSoundEventsAndMusicTracksIntegrity() {
        val events = com.orbitbreaker.survival.game.SoundEvent.values()
        assertEquals(20, events.size)

        val tracks = com.orbitbreaker.survival.game.MusicTrack.values()
        assertEquals(5, tracks.size) // NONE, LOBBY, FLIGHT, BOSS, VICTORY
        assertTrue(tracks.contains(com.orbitbreaker.survival.game.MusicTrack.BOSS))
        assertTrue(tracks.contains(com.orbitbreaker.survival.game.MusicTrack.VICTORY))
    }

    @Test
    fun testStrictDifficultyProgressionIsolation() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        // 1. Initial State: All difficulties start at Level 1 unlocked
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.EASY))
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.NORMAL))
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.HARD))

        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.EASY))
        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.NORMAL))
        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.HARD))

        // 2. Complete Level 1 on NORMAL with 3 stars
        val firstTimeNormal = storage.saveLevelCompletion(
            levelId = 1,
            score = 1200,
            survivalTime = 25.0f,
            stars = 3,
            difficulty = GameDifficulty.NORMAL
        )
        assertTrue("First-time victory should report true", firstTimeNormal)

        // NORMAL should advance to Level 2
        assertEquals(2, storage.getHighestUnlockedLevel(GameDifficulty.NORMAL))
        assertTrue(storage.isLevelCompleted(1, GameDifficulty.NORMAL))
        assertEquals(3, storage.getLevelStars(1, GameDifficulty.NORMAL))
        assertEquals(1, storage.getCompletedLevelCount(GameDifficulty.NORMAL))

        // EASY and HARD must be 100% untouched!
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.EASY))
        assertFalse(storage.isLevelCompleted(1, GameDifficulty.EASY))
        assertEquals(0, storage.getLevelStars(1, GameDifficulty.EASY))
        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.EASY))

        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.HARD))
        assertFalse(storage.isLevelCompleted(1, GameDifficulty.HARD))
        assertEquals(0, storage.getLevelStars(1, GameDifficulty.HARD))
        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.HARD))

        // 3. Complete Level 1 on HARD with 2 stars
        val firstTimeHard = storage.saveLevelCompletion(
            levelId = 1,
            score = 1800,
            survivalTime = 30.0f,
            stars = 2,
            difficulty = GameDifficulty.HARD
        )
        assertTrue(firstTimeHard)

        // HARD advances to Level 2
        assertEquals(2, storage.getHighestUnlockedLevel(GameDifficulty.HARD))
        assertTrue(storage.isLevelCompleted(1, GameDifficulty.HARD))
        assertEquals(2, storage.getLevelStars(1, GameDifficulty.HARD))
        assertEquals(1, storage.getCompletedLevelCount(GameDifficulty.HARD))

        // EASY still untouched at Level 1
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.EASY))
        assertFalse(storage.isLevelCompleted(1, GameDifficulty.EASY))
        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.EASY))

        // NORMAL level 2 still unlocked
        assertEquals(2, storage.getHighestUnlockedLevel(GameDifficulty.NORMAL))
    }

    @Test
    fun testVictoryGuardAndRepeatClearRewards() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        // First completion
        val firstClear = storage.saveLevelCompletion(
            levelId = 5,
            score = 5000,
            survivalTime = 40.0f,
            stars = 3,
            difficulty = GameDifficulty.NORMAL
        )
        assertTrue(firstClear)

        // Repeat completion with higher score
        val repeatClear = storage.saveLevelCompletion(
            levelId = 5,
            score = 6500,
            survivalTime = 38.0f,
            stars = 3,
            difficulty = GameDifficulty.NORMAL
        )
        assertFalse("Repeat completion must not report first time", repeatClear)
        assertEquals(6500, storage.getLevelBestScore(5, GameDifficulty.NORMAL))
    }

    @Test
    fun testUnlimitedModeDoesNotUnlockCampaignLevels() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.NORMAL))
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.HARD))

        // Save unlimited run
        storage.saveUnlimitedRecord(
            score = 25000,
            survivalTime = 180.0f,
            difficulty = GameDifficulty.HARD
        )

        assertEquals(25000, storage.getUnlimitedBestScore(GameDifficulty.HARD))
        assertEquals(180.0f, storage.getUnlimitedBestTime(GameDifficulty.HARD), 0.01f)

        // Campaign progression must NOT be affected
        assertEquals(1, storage.getHighestUnlockedLevel(GameDifficulty.HARD))
        assertEquals(0, storage.getCompletedLevelCount(GameDifficulty.HARD))
        assertFalse(storage.isLevelCompleted(1, GameDifficulty.HARD))
    }

    @Test
    fun testStarterShipAlwaysOwnedAndFree() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        val starter = ShipCatalog.ships.first()
        assertTrue("Starter ship must be owned by default", storage.isShipOwned(starter.id))
        assertEquals("Starter ship must be equipped by default", starter.id, storage.getEquippedShipId())
        assertTrue("Owned ships must contain starter ship", storage.getOwnedShipIds().contains(starter.id))

        // Attempting to buy starter ship returns AlreadyOwned with zero deduction
        val result = storage.purchaseShip(starter.id)
        assertTrue(result is PurchaseResult.AlreadyOwned)
        assertEquals(150, storage.getCredits())
    }

    @Test
    fun testShipPurchaseFlowWithSufficientFunds() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        // Give player 500 credits
        storage.addCredits(350) // starts with 150, now 500
        assertEquals(500, storage.getCredits())

        val corvette = ShipCatalog.ships[1] // Vanguard Mint, price = 250
        assertFalse(storage.isShipOwned(corvette.id))

        val result = storage.purchaseShip(corvette.id)
        assertTrue("Purchase must succeed", result is PurchaseResult.Success)
        val success = result as PurchaseResult.Success
        assertEquals(250, success.remainingBalance)
        assertEquals(250, storage.getCredits())
        assertTrue(storage.isShipOwned(corvette.id))

        // Equipped ship should remain starter ship until explicitly equipped
        assertEquals(ShipCatalog.ships.first().id, storage.getEquippedShipId())

        // Equip the new ship
        val equipSuccess = storage.equipShip(corvette.id)
        assertTrue(equipSuccess)
        assertEquals(corvette.id, storage.getEquippedShipId())
    }

    @Test
    fun testShipPurchaseExactFunds() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        // Give player exactly 250 credits
        storage.spendCredits(150)
        storage.addCredits(250)
        assertEquals(250, storage.getCredits())

        val corvette = ShipCatalog.ships[1] // price = 250
        val result = storage.purchaseShip(corvette.id)
        assertTrue(result is PurchaseResult.Success)
        assertEquals(0, storage.getCredits())
        assertTrue(storage.isShipOwned(corvette.id))
    }

    @Test
    fun testShipPurchaseInsufficientFundsShortfall() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        // Balance is 150. Vanguard Mint price is 250. Shortfall = 100.
        val corvette = ShipCatalog.ships[1]
        val result = storage.purchaseShip(corvette.id)

        assertTrue(result is PurchaseResult.InsufficientFunds)
        val failure = result as PurchaseResult.InsufficientFunds
        assertEquals(250, failure.required)
        assertEquals(150, failure.current)
        assertEquals(100, failure.shortfall)

        // Coins must not be deducted
        assertEquals(150, storage.getCredits())
        assertFalse(storage.isShipOwned(corvette.id))
    }

    @Test
    fun testShipPurchaseZeroFunds() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        storage.spendCredits(150) // Balance = 0
        assertEquals(0, storage.getCredits())

        val striker = ShipCatalog.ships[2] // Plasma Striker, price = 600
        val result = storage.purchaseShip(striker.id)

        assertTrue(result is PurchaseResult.InsufficientFunds)
        val failure = result as PurchaseResult.InsufficientFunds
        assertEquals(600, failure.required)
        assertEquals(0, failure.current)
        assertEquals(600, failure.shortfall)

        assertEquals(0, storage.getCredits())
        assertFalse(storage.isShipOwned(striker.id))
    }

    @Test
    fun testShipPurchaseDoubleTapProtectionAndNoSecondCharge() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        storage.addCredits(1000) // Balance = 1150
        val corvette = ShipCatalog.ships[1] // price = 250

        // Tap 1
        val result1 = storage.purchaseShip(corvette.id)
        assertTrue(result1 is PurchaseResult.Success)
        assertEquals(900, storage.getCredits())

        // Tap 2 (rapid second tap)
        val result2 = storage.purchaseShip(corvette.id)
        assertTrue("Second purchase attempt must report AlreadyOwned", result2 is PurchaseResult.AlreadyOwned)
        // Balance must remain unchanged at 900
        assertEquals(900, storage.getCredits())
    }

    @Test
    fun testShipPurchasePersistenceAcrossSessions() {
        val fakePrefs = FakeSharedPreferences()
        val session1Storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        session1Storage.addCredits(500) // Balance = 650
        val corvette = ShipCatalog.ships[1] // price = 250
        session1Storage.purchaseShip(corvette.id)
        session1Storage.equipShip(corvette.id)

        // Simulate app restart by creating a new storage instance with the same preferences
        val session2Storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)
        assertEquals(400, session2Storage.getCredits())
        assertTrue("Purchased ship must remain owned in next session", session2Storage.isShipOwned(corvette.id))
        assertEquals("Equipped ship must remain equipped in next session", corvette.id, session2Storage.getEquippedShipId())
    }

    @Test
    fun testCannotEquipUnownedShip() {
        val fakePrefs = FakeSharedPreferences()
        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)

        val unownedShip = ShipCatalog.ships[3] // Titan Aegis, price = 1100
        assertFalse(storage.isShipOwned(unownedShip.id))

        val equipSuccess = storage.equipShip(unownedShip.id)
        assertFalse("Cannot equip unowned ship", equipSuccess)
        assertEquals("Equipped ship must remain previous valid selection", ShipCatalog.ships.first().id, storage.getEquippedShipId())
    }

    @Test
    fun testCorruptedEquippedShipFallsBackToStarter() {
        val fakePrefs = FakeSharedPreferences()
        // Manually inject invalid unowned ship id into preferences
        fakePrefs.edit().putString("key_equipped_ship_id", "invalid_corrupted_id_999").apply()

        val storage = com.orbitbreaker.survival.data.LocalGameStorage(injectedPrefs = fakePrefs)
        val equipped = storage.getEquippedShipId()
        assertEquals("Corrupted or unowned ship must safely fall back to starter", ShipCatalog.ships.first().id, equipped)
    }
}

// Lightweight fake SharedPreferences for deterministic unit tests
private class FakeSharedPreferences : android.content.SharedPreferences {
    private val data = mutableMapOf<String, Any>()

    override fun getAll(): MutableMap<String, *> = data

    override fun getString(key: String?, defValue: String?): String? =
        (data[key] as? String) ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (data[key] as? MutableSet<String>) ?: defValues

    override fun getInt(key: String?, defValue: Int): Int =
        (data[key] as? Int) ?: defValue

    override fun getLong(key: String?, defValue: Long): Long =
        (data[key] as? Long) ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float =
        (data[key] as? Float) ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        (data[key] as? Boolean) ?: defValue

    override fun contains(key: String?): Boolean = data.containsKey(key)

    override fun edit(): android.content.SharedPreferences.Editor = FakeEditor(data)

    override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private class FakeEditor(private val storage: MutableMap<String, Any>) : android.content.SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()
        private var clearFlag = false

        override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = value }
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = values }
            return this
        }

        override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = value }
            return this
        }

        override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = value }
            return this
        }

        override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = value }
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = value }
            return this
        }

        override fun remove(key: String?): android.content.SharedPreferences.Editor {
            key?.let { temp[it] = null }
            return this
        }

        override fun clear(): android.content.SharedPreferences.Editor {
            clearFlag = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearFlag) {
                storage.clear()
            }
            temp.forEach { (k, v) ->
                if (v == null) {
                    storage.remove(k)
                } else {
                    storage[k] = v
                }
            }
        }
    }
}
