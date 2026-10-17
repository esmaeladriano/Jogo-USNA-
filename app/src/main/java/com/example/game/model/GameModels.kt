package com.example.game.model

import kotlin.math.sqrt

data class Vector2(val x: Float = 0f, val y: Float = 0f) {
  operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
  operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
  operator fun times(scale: Float) = Vector2(x * scale, y * scale)
  fun length(): Float = sqrt(x * x + y * y)
}

data class Rect2(val x: Float, val y: Float, val width: Float, val height: Float) {
  val left: Float get() = x
  val top: Float get() = y
  val right: Float get() = x + width
  val bottom: Float get() = y + height
  val centerX: Float get() = x + width / 2f
  val centerY: Float get() = y + height / 2f

  fun intersects(other: Rect2): Boolean {
    return left < other.right && right > other.left && top < other.bottom && bottom > other.top
  }

  fun contains(point: Vector2): Boolean {
    return point.x in left..right && point.y in top..bottom
  }
}

data class GameInput(
  val moveLeft: Boolean = false,
  val moveRight: Boolean = false,
  val jump: Boolean = false,
  val action: Boolean = false
) {
  val horizontalAxis: Float
    get() = when {
      moveLeft && !moveRight -> -1f
      moveRight && !moveLeft -> 1f
      else -> 0f
    }
}

data class EchoRecording(
  val spawnPosition: Vector2,
  val inputs: List<GameInput>,
  val durationTicks: Int = inputs.size
)

data class EchoInstance(
  val id: Int,
  val recording: EchoRecording,
  var position: Vector2 = recording.spawnPosition,
  var velocity: Vector2 = Vector2(),
  var playbackTick: Int = 0,
  var isGrounded: Boolean = false,
  var isFinished: Boolean = false,
  var isDestroyed: Boolean = false,
  var facingRight: Boolean = true,
  var width: Float = 28f,
  var height: Float = 36f
) {
  fun boundingBox(): Rect2 = Rect2(position.x - width / 2f, position.y - height, width, height)
}

enum class ButtonType {
  STANDARD,
  TIMED
}

data class ButtonState(
  val id: Int,
  val rect: Rect2,
  val type: ButtonType = ButtonType.STANDARD,
  var isPressed: Boolean = false,
  var wasPressed: Boolean = false,
  var timerRemainingTicks: Int = 0, // for timed variant: 120 ticks = 2s at 60Hz
  val totalTimerTicks: Int = 120
) {
  val isActive: Boolean
    get() = isPressed || timerRemainingTicks > 0
}

data class DoorState(
  val id: Int,
  val buttonId: Int,
  val rect: Rect2,
  var isOpen: Boolean = false,
  var openProgress: Float = 0f // 0f (closed) to 1f (fully open)
) {
  fun collisionBox(): Rect2? {
    if (openProgress >= 0.95f) return null
    val currentHeight = rect.height * (1f - openProgress)
    return Rect2(rect.x, rect.y, rect.width, currentHeight)
  }
}

data class SpikeHazard(
  val rect: Rect2,
  val orientation: SpikeOrientation = SpikeOrientation.UP
)

enum class SpikeOrientation {
  UP, DOWN, LEFT, RIGHT
}

data class Platform(
  val rect: Rect2,
  val isOneWay: Boolean = false
)

data class ExitPortal(
  val rect: Rect2
)

data class LevelData(
  val id: Int,
  val namePt: String,
  val nameEn: String,
  val descriptionPt: String,
  val descriptionEn: String,
  val playerSpawn: Vector2,
  val platforms: List<Platform>,
  val buttons: List<ButtonState>,
  val doors: List<DoorState>,
  val spikes: List<SpikeHazard>,
  val exitPortal: ExitPortal,
  val maxEchoes: Int = 3,
  val echoStaysAtEnd: Boolean = true,
  val targetTimeSec: Float = 20f,
  val themeColorHex: Long = 0xFF1B2233
)

data class SkinTheme(
  val id: String,
  val namePt: String,
  val nameEn: String,
  val primaryColor: Long,
  val secondaryColor: Long,
  val glowColor: Long,
  val eyesColor: Long,
  val requiredStars: Int = 0
)

object SkinRepository {
  val skins = listOf(
    SkinTheme("solar", "Solar Clássico", "Classic Solar", 0xFFFF7A18, 0xFFFF9E40, 0xFFFFD54F, 0xFF0F1A2C, 0),
    SkinTheme("neon_cyber", "Cyberpunk", "Neon Cyber", 0xFFFF007F, 0xFFFF4081, 0xFF38E1FF, 0xFF00E5FF, 3),
    SkinTheme("emerald", "Espírito Esmeralda", "Emerald Spirit", 0xFF00E676, 0xFF69F0AE, 0xFFB9F6CA, 0xFF003314, 8),
    SkinTheme("void", "Fantasma do Vazio", "Void Phantom", 0xFF7C4DFF, 0xFFB388FF, 0xFFEA80FC, 0xFF12005E, 14),
    SkinTheme("cosmic_gold", "Ouro Celestial", "Celestial Gold", 0xFFFFD700, 0xFFFFE57F, 0xFFFFF9C4, 0xFF3E2723, 20)
  )

  fun getSkin(id: String): SkinTheme {
    return skins.find { it.id == id } ?: skins.first()
  }
}

data class VisualParticle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var life: Float,
  val maxLife: Float,
  val color: Long,
  val size: Float
)

data class GameSettings(
  val soundVolume: Float = 0.8f,
  val musicVolume: Float = 0.5f,
  val vibrationEnabled: Boolean = true,
  val language: String = "pt", // "pt" or "en"
  val selectedSkinId: String = "solar",
  val showConduitCables: Boolean = true
)

data class LevelStats(
  val unlocked: Boolean = false,
  val stars: Int = 0,
  val bestTimeSec: Float = 0f,
  val echoesUsed: Int = 0
)

data class GameProgress(
  val levelStats: Map<Int, LevelStats> = mapOf(1 to LevelStats(unlocked = true))
) {
  val totalStars: Int get() = levelStats.values.sumOf { it.stars }
}
