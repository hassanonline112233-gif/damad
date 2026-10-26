package com.orbitbreaker.survival.data

data class ThreatStage(
    val tier: Int,
    val name: String,
    val description: String,
    val minTimeSeconds: Float,
    val spawnIntervalMod: Float,
    val speedMod: Float
)

object ThreatDirector {
    val stages = listOf(
        ThreatStage(1, "STAGE 1 — WARM-UP", "Lower orbital perimeter. Calibrating thrusters.", 0f, 1.0f, 1.0f),
        ThreatStage(2, "STAGE 2 — RISING PRESSURE", "Micrometeorites and fast skimmers detected.", 60f, 0.88f, 1.10f),
        ThreatStage(3, "STAGE 3 — COMBAT ENGAGEMENT", "Armored interceptors & tactical strikers active.", 180f, 0.76f, 1.22f),
        ThreatStage(4, "STAGE 4 — ADVANCED THREAT", "Heavy destroyers & multi-angle missile barrages.", 300f, 0.65f, 1.35f),
        ThreatStage(5, "STAGE 5 — ELITE SURVIVAL", "Elite hunters in formation. Dense asteroid fields.", 480f, 0.55f, 1.48f),
        ThreatStage(6, "STAGE 6 — EXTREME SINGULARITY", "Zenith anomaly threshold. Continuous apex escalation.", 720f, 0.45f, 1.60f)
    )

    fun getCurrentStage(survivalTimeSeconds: Float): ThreatStage {
        for (i in stages.indices.reversed()) {
            if (survivalTimeSeconds >= stages[i].minTimeSeconds) {
                return stages[i]
            }
        }
        return stages.first()
    }
}
