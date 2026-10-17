package com.example.trilho.model

import kotlin.math.sqrt

enum class Lane(val index: Int, val targetX: Float) {
  LEFT(0, 190f),
  CENTER(1, 360f),
  RIGHT(2, 530f);

  companion object {
    fun fromIndex(index: Int): Lane = when (index.coerceIn(0, 2)) {
      0 -> LEFT
      1 -> CENTER
      else -> RIGHT
    }
  }
}

enum class ObstacleType(val heightType: ObstacleHeight, val widthLanes: Int) {
  BARRIER_LOW(ObstacleHeight.LOW, 1),       // Must jump over
  BARRIER_HIGH(ObstacleHeight.HIGH, 1),     // Must slide under
  TRAIN(ObstacleHeight.FULL, 1),            // Must change lane
  DOUBLE_BARRIER(ObstacleHeight.FULL, 2)    // Blocks 2 lanes (leaves 1 open)
}

enum class ObstacleHeight {
  LOW,   // Clearable by JUMP
  HIGH,  // Clearable by SLIDE
  FULL   // Must dodge horizontally
}

enum class PowerUpType(val durationSec: Float) {
  MAGNET(8.0f),
  SHIELD(0f), // Permanent until hit
  BOOST(4.0f) // Super speed + invulnerability
}

data class TrackObstacle(
  var id: Long,
  var type: ObstacleType,
  var lane: Lane,
  var y: Float,
  var width: Float = 120f,
  var height: Float = 60f,
  var active: Boolean = true
)

enum class PickupType {
  COIN,
  CRYSTAL,
  POWERUP_MAGNET,
  POWERUP_SHIELD,
  POWERUP_BOOST
}

data class TrackPickup(
  var id: Long,
  var type: PickupType,
  var lane: Lane,
  var y: Float,
  var x: Float = lane.targetX,
  var active: Boolean = true
)

data class RunnerSkin(
  val id: String,
  val namePt: String,
  val nameEn: String,
  val costCoins: Int,
  val primaryColor: Long,
  val secondaryColor: Long,
  val glowColor: Long
)

object TrilhoSkinRepository {
  val skins = listOf(
    RunnerSkin("solaris", "Corredor Solaris", "Solaris Runner", 0, 0xFFFF7A18, 0xFFFFB74D, 0xFF38E1FF),
    RunnerSkin("cyber_neon", "Velocista Neon", "Neon Sprinter", 150, 0xFFFF007F, 0xFFFF4081, 0xFF00E5FF),
    RunnerSkin("emerald_tech", "Expresso Esmeralda", "Emerald Express", 300, 0xFF00E676, 0xFF69F0AE, 0xFF76FF03),
    RunnerSkin("phantom", "Fantasma dos Trilhos", "Track Phantom", 500, 0xFF7C4DFF, 0xFFB388FF, 0xFFEA80FC)
  )

  fun getSkin(id: String): RunnerSkin = skins.find { it.id == id } ?: skins.first()
}

data class RunnerParticle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var life: Float,
  val maxLife: Float,
  val color: Long,
  val size: Float
)

data class TrilhoSettings(
  val soundVolume: Float = 0.8f,
  val musicVolume: Float = 0.5f,
  val vibrationEnabled: Boolean = true,
  val language: String = "pt",
  val selectedSkinId: String = "solaris"
)

data class TrilhoProgress(
  val totalCoins: Int = 0,
  val bestDistanceMeters: Int = 0,
  val totalRunsPlayed: Int = 0,
  val unlockedSkinIds: Set<String> = setOf("solaris")
)
