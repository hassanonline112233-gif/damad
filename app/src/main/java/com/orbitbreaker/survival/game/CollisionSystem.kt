package com.orbitbreaker.survival.game

import kotlin.math.hypot

object CollisionSystem {

    /**
     * Rocket hit detection using an elliptical/capsule approximation:
     * - Top nose cone: circle centered near top
     * - Main body: rounded rectangle / center circles
     * This ensures precise, fair collisions where near-misses feel rewarding
     * rather than cheap bounding-box collisions.
     */
    fun checkCollision(rocket: Rocket, meteor: Meteor): Boolean {
        // Broad phase bounding box check
        val halfW = rocket.width / 2f
        val halfH = rocket.height / 2f

        if (meteor.x + meteor.radius < rocket.x - halfW ||
            meteor.x - meteor.radius > rocket.x + halfW ||
            meteor.y + meteor.radius < rocket.y - halfH ||
            meteor.y - meteor.radius > rocket.y + halfH
        ) {
            return false
        }

        // Narrow phase circle checks: 3 collision circles along the rocket spine
        // 1. Nose cone (smaller circle near top)
        val noseX = rocket.x
        val noseY = rocket.y - halfH + 12f
        val noseRadius = 10f

        // 2. Center cabin
        val midX = rocket.x
        val midY = rocket.y
        val midRadius = halfW * 0.72f

        // 3. Engine base
        val engineX = rocket.x
        val engineY = rocket.y + halfH - 12f
        val engineRadius = halfW * 0.85f

        // Meteor effective radius is slightly forgiving (0.88f) for fair arcade feel
        val effectiveMeteorR = meteor.radius * 0.88f

        if (circleIntersects(noseX, noseY, noseRadius, meteor.x, meteor.y, effectiveMeteorR)) {
            return true
        }
        if (circleIntersects(midX, midY, midRadius, meteor.x, meteor.y, effectiveMeteorR)) {
            return true
        }
        if (circleIntersects(engineX, engineY, engineRadius, meteor.x, meteor.y, effectiveMeteorR)) {
            return true
        }

        return false
    }

    private fun circleIntersects(
        c1x: Float, c1y: Float, r1: Float,
        c2x: Float, c2y: Float, r2: Float
    ): Boolean {
        val dist = hypot(c1x - c2x, c1y - c2y)
        return dist < (r1 + r2)
    }
}
