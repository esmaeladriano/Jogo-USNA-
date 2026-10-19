package com.example.trilho.engine

import com.example.trilho.model.*
import kotlin.math.*

class TrilhoEngine(
  private val onEvent: (TrilhoEvent) -> Unit = {}
) {
  sealed class TrilhoEvent {
    object LaneSwitch : TrilhoEvent()
    object Jump : TrilhoEvent()
    object Slide : TrilhoEvent()
    object CoinCollected : TrilhoEvent()
    object CrystalCollected : TrilhoEvent()
    data class PowerUpCollected(val type: PowerUpType) : TrilhoEvent()
    object CrystalAbilityUsed : TrilhoEvent()
    object ShieldAbsorbed : TrilhoEvent()
    object Crash : TrilhoEvent()
    data class GameOver(val distanceMeters: Int, val coinsEarned: Int) : TrilhoEvent()
  }

  companion object {
    const val TICK_RATE = 60
    const val DELTA_TIME = 1f / TICK_RATE
    const val SCREEN_WIDTH = 720f
    const val SCREEN_HEIGHT = 1280f
    const val PLAYER_BASE_Y = 1020f
    const val BASE_SPEED = 840f
    const val SPEED_STEP_PER_500M = 50f
    const val MAX_CRYSTALS = 3
    const val JUMP_DURATION_TICKS = 34 // ~0.56s snappier jump
    const val SLIDE_DURATION_TICKS = 32 // ~0.53s snappier slide
    const val LANE_CHANGE_DURATION_TICKS = 7 // ~0.11s super fast responsive lane switch
    const val CRYSTAL_INVULN_DURATION_TICKS = 90 // 1.5s
  }

  // Player State
  var currentLane: Lane = Lane.CENTER
  var targetLane: Lane = Lane.CENTER
  var playerX: Float = Lane.CENTER.targetX
  var startLaneX: Float = Lane.CENTER.targetX
  var laneChangeTicksRemaining: Int = 0

  var isJumping: Boolean = false
  var jumpTicks: Int = 0
  var jumpHeightOffset: Float = 0f // Visual Z offset (up to 140px)

  var isSliding: Boolean = false
  var slideTicks: Int = 0

  var crystalsStock: Int = 0
  var crystalInvulnTicks: Int = 0

  // Power-up States
  var hasShield: Boolean = false
  var magnetTicksRemaining: Int = 0
  var boostTicksRemaining: Int = 0
  var graceInvulnTicks: Int = 0

  // Metrics
  var distancePx: Float = 0f
  val distanceMeters: Int get() = (distancePx / 12f).toInt()
  var runCoins: Int = 0
  var isGameOver: Boolean = false
  var currentSpeed: Float = BASE_SPEED

  // Synchronization Lock for thread-safe rendering and physics updates
  val stateLock = Any()

  // Object Pools
  val activeObstacles = mutableListOf<TrackObstacle>()
  val activePickups = mutableListOf<TrackPickup>()
  val particles = mutableListOf<RunnerParticle>()

  private var nextEntityId = 1L
  private var nextSpawnY: Float = -200f
  var trackSleeperOffset: Float = 0f

  init {
    reset()
  }

  fun reset() {
    synchronized(stateLock) {
      currentLane = Lane.CENTER
      targetLane = Lane.CENTER
      playerX = Lane.CENTER.targetX
      startLaneX = Lane.CENTER.targetX
      laneChangeTicksRemaining = 0

      isJumping = false
      jumpTicks = 0
      jumpHeightOffset = 0f

      isSliding = false
      slideTicks = 0

      crystalsStock = 0
      crystalInvulnTicks = 0

      hasShield = false
      magnetTicksRemaining = 0
      boostTicksRemaining = 0
      graceInvulnTicks = 0

      distancePx = 0f
      runCoins = 0
      isGameOver = false
      currentSpeed = BASE_SPEED

      activeObstacles.clear()
      activePickups.clear()
      particles.clear()
      nextSpawnY = -200f
      trackSleeperOffset = 0f

      // Pre-populate track ahead safely
      generateInitialTrack()
    }
  }

  // Input Actions
  fun moveLeft(): Boolean {
    if (isGameOver) return false
    val newIndex = (targetLane.index - 1).coerceAtLeast(0)
    if (newIndex != targetLane.index) {
      changeLane(Lane.fromIndex(newIndex))
      return true
    }
    return false
  }

  fun moveRight(): Boolean {
    if (isGameOver) return false
    val newIndex = (targetLane.index + 1).coerceAtMost(2)
    if (newIndex != targetLane.index) {
      changeLane(Lane.fromIndex(newIndex))
      return true
    }
    return false
  }

  private fun changeLane(newLane: Lane) {
    targetLane = newLane
    startLaneX = playerX
    laneChangeTicksRemaining = LANE_CHANGE_DURATION_TICKS
    onEvent(TrilhoEvent.LaneSwitch)
  }

  fun jump(): Boolean {
    if (isGameOver || isJumping) return false
    // Cancel slide if jumping
    isSliding = false
    slideTicks = 0

    isJumping = true
    jumpTicks = JUMP_DURATION_TICKS
    spawnParticleBurst(playerX, PLAYER_BASE_Y + 10f, 10, 0xFF38E1FF)
    onEvent(TrilhoEvent.Jump)
    return true
  }

  fun slide(): Boolean {
    if (isGameOver) return false
    // If in air, fast drop to ground
    if (isJumping) {
      isJumping = false
      jumpTicks = 0
      jumpHeightOffset = 0f
    }
    isSliding = true
    slideTicks = SLIDE_DURATION_TICKS
    spawnParticleBurst(playerX, PLAYER_BASE_Y + 20f, 8, 0xFFFF9E40)
    onEvent(TrilhoEvent.Slide)
    return true
  }

  fun useCrystalAbility(): Boolean {
    if (isGameOver || crystalsStock <= 0 || crystalInvulnTicks > 0) return false
    crystalsStock--
    crystalInvulnTicks = CRYSTAL_INVULN_DURATION_TICKS
    spawnParticleBurst(playerX, PLAYER_BASE_Y - 20f, 25, 0xFF38E1FF, 1.8f)
    onEvent(TrilhoEvent.CrystalAbilityUsed)
    return true
  }

  // 60 FPS Fixed Physics Tick
  fun tick() {
    synchronized(stateLock) {
      if (isGameOver) return

      // 1. Calculate progressive speed
      val speedMultiplier = if (boostTicksRemaining > 0) 1.85f else 1.0f
      val difficultyStage = distanceMeters / 500
      currentSpeed = (BASE_SPEED + difficultyStage * SPEED_STEP_PER_500M) * speedMultiplier

      val moveDelta = currentSpeed * DELTA_TIME
      distancePx += moveDelta
      trackSleeperOffset = (trackSleeperOffset + moveDelta) % 90f

      // 2. Update Lane Change Interpolation (Tween)
      if (laneChangeTicksRemaining > 0) {
        laneChangeTicksRemaining--
        val progress = 1f - (laneChangeTicksRemaining.toFloat() / LANE_CHANGE_DURATION_TICKS)
        // Smooth sinusoidal easeInOut
        val eased = 0.5f - 0.5f * cos(progress * PI.toFloat())
        playerX = startLaneX + (targetLane.targetX - startLaneX) * eased
        if (laneChangeTicksRemaining == 0) {
          currentLane = targetLane
          playerX = targetLane.targetX
        }
      }

      // 3. Update Jump Curve
      if (isJumping) {
        jumpTicks--
        val progress = 1f - (jumpTicks.toFloat() / JUMP_DURATION_TICKS)
        // Parabolic jump arc: 4 * h * p * (1 - p)
        jumpHeightOffset = 130f * 4f * progress * (1f - progress)
        if (jumpTicks <= 0) {
          isJumping = false
          jumpHeightOffset = 0f
          spawnParticleBurst(playerX, PLAYER_BASE_Y + 10f, 10, 0xFF38E1FF)
        }
      }

      // 4. Update Slide
      if (isSliding) {
        slideTicks--
        if (slideTicks <= 0) {
          isSliding = false
        }
      }

      // 5. Update Timers (Crystal Invulnerability, Power-ups)
      if (crystalInvulnTicks > 0) crystalInvulnTicks--
      if (graceInvulnTicks > 0) graceInvulnTicks--
      if (magnetTicksRemaining > 0) magnetTicksRemaining--
      if (boostTicksRemaining > 0) boostTicksRemaining--

      // 6. Move Obstacles & Pickups downwards
      updateObstacles(moveDelta)
      updatePickups(moveDelta)

      // 7. Check Collisions
      checkCollisions()

      // 8. Procedural Track Generation
      generateProceduralTrack(moveDelta)

      // 9. Update Particles
      val pIter = particles.iterator()
      while (pIter.hasNext()) {
        val p = pIter.next()
        p.life -= DELTA_TIME
        if (p.life <= 0f) {
          pIter.remove()
        } else {
          p.x += p.vx * DELTA_TIME
          p.y += p.vy * DELTA_TIME
        }
      }
    }
  }

  private fun updateObstacles(moveDelta: Float) {
    val iter = activeObstacles.iterator()
    while (iter.hasNext()) {
      val obs = iter.next()
      obs.y += moveDelta
      if (obs.y > SCREEN_HEIGHT + 200f) {
        iter.remove()
      }
    }
  }

  private fun updatePickups(moveDelta: Float) {
    val isMagnetActive = magnetTicksRemaining > 0 || boostTicksRemaining > 0
    val iter = activePickups.iterator()
    while (iter.hasNext()) {
      val item = iter.next()
      item.y += moveDelta

      // Magnet pull towards player
      if (isMagnetActive && item.type == PickupType.COIN && item.y in 300f..PLAYER_BASE_Y + 100f) {
        val dx = playerX - item.x
        val dy = (PLAYER_BASE_Y - 20f) - item.y
        item.x += dx * 0.22f
        item.y += dy * 0.18f
      }

      if (item.y > SCREEN_HEIGHT + 100f) {
        iter.remove()
      }
    }
  }

  private fun checkCollisions() {
    val isInvulnerable = crystalInvulnTicks > 0 || boostTicksRemaining > 0 || graceInvulnTicks > 0

    // Pickup Collection
    val pickupIter = activePickups.iterator()
    while (pickupIter.hasNext()) {
      val item = pickupIter.next()
      if (!item.active) continue

      // Proximity check to player
      val dx = abs(playerX - item.x)
      val dy = abs((PLAYER_BASE_Y - jumpHeightOffset) - item.y)

      if (dx < 60f && dy < 65f) {
        item.active = false
        pickupIter.remove()

        when (item.type) {
          PickupType.COIN -> {
            runCoins++
            spawnParticleBurst(item.x, item.y, 6, 0xFFFFD700)
            onEvent(TrilhoEvent.CoinCollected)
          }
          PickupType.CRYSTAL -> {
            if (crystalsStock < MAX_CRYSTALS) {
              crystalsStock++
            }
            spawnParticleBurst(item.x, item.y, 14, 0xFF38E1FF, 1.4f)
            onEvent(TrilhoEvent.CrystalCollected)
          }
          PickupType.POWERUP_MAGNET -> {
            magnetTicksRemaining = (PowerUpType.MAGNET.durationSec * TICK_RATE).toInt()
            spawnParticleBurst(item.x, item.y, 16, 0xFF00E5FF)
            onEvent(TrilhoEvent.PowerUpCollected(PowerUpType.MAGNET))
          }
          PickupType.POWERUP_SHIELD -> {
            hasShield = true
            spawnParticleBurst(item.x, item.y, 16, 0xFF00E676)
            onEvent(TrilhoEvent.PowerUpCollected(PowerUpType.SHIELD))
          }
          PickupType.POWERUP_BOOST -> {
            boostTicksRemaining = (PowerUpType.BOOST.durationSec * TICK_RATE).toInt()
            spawnParticleBurst(item.x, item.y, 22, 0xFFFF3D57, 1.6f)
            onEvent(TrilhoEvent.PowerUpCollected(PowerUpType.BOOST))
          }
        }
      }
    }

    // Obstacle Collision
    for (obs in activeObstacles) {
      if (!obs.active) continue

      // Lane match: Obstacle is either in player's lane or occupies double lanes
      val inLane = when (obs.type) {
        ObstacleType.DOUBLE_BARRIER -> {
          // Double barrier occupies obs.lane and adjacent lane
          val l1 = obs.lane.index
          val l2 = if (l1 < 2) l1 + 1 else l1 - 1
          currentLane.index == l1 || currentLane.index == l2
        }
        else -> obs.lane == currentLane
      }

      if (!inLane) continue

      // Longitudinal Y overlap with player
      val playerTop = PLAYER_BASE_Y - (if (isSliding) 28f else 64f)
      val playerBottom = PLAYER_BASE_Y
      val obsTop = obs.y - obs.height
      val obsBottom = obs.y

      val yOverlap = playerTop < obsBottom && playerBottom > obsTop

      if (yOverlap) {
        // Can obstacle be cleared by current maneuver?
        val bypassed = when (obs.type.heightType) {
          ObstacleHeight.LOW -> jumpHeightOffset > 45f // Cleared by jump!
          ObstacleHeight.HIGH -> isSliding             // Cleared by slide!
          ObstacleHeight.FULL -> false                 // Cannot clear vertically!
        }

        if (!bypassed) {
          if (isInvulnerable) {
            // Smash obstacle with burst
            obs.active = false
            spawnParticleBurst(obs.lane.targetX, obs.y, 18, 0xFFFF3D57, 1.5f)
          } else if (hasShield) {
            // Absorb hit with shield
            hasShield = false
            obs.active = false
            graceInvulnTicks = 60 // 1s grace period
            spawnParticleBurst(playerX, PLAYER_BASE_Y - 30f, 20, 0xFF00E676)
            onEvent(TrilhoEvent.ShieldAbsorbed)
          } else {
            // Lethal crash!
            isGameOver = true
            spawnParticleBurst(playerX, PLAYER_BASE_Y - 20f, 30, 0xFFFF3D57, 2.0f)
            onEvent(TrilhoEvent.Crash)
            onEvent(TrilhoEvent.GameOver(distanceMeters, runCoins))
            break
          }
        }
      }
    }
  }

  private fun generateInitialTrack() {
    // Generate safe clear track for first 800px, then start procedural segments
    nextSpawnY = -300f
    for (i in 0 until 5) {
      spawnTrackSegment(nextSpawnY)
      nextSpawnY -= 480f
    }
  }

  private fun generateProceduralTrack(moveDelta: Float) {
    nextSpawnY += moveDelta
    if (nextSpawnY > -300f) {
      spawnTrackSegment(-800f)
      nextSpawnY = -800f
    }
  }

  // 10 Distinct Solvable Track Patterns (Guaranteed to always leave at least one passable path)
  private fun spawnTrackSegment(spawnY: Float) {
    val pattern = (0..9).random()

    when (pattern) {
      // 0: Clean coin sprint with single low barrier
      0 -> {
        spawnCoinRun(Lane.CENTER, spawnY - 60f, 4)
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.CENTER, spawnY))
      }

      // 1: Train on left, high barrier on center, clear right lane with coins
      1 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.TRAIN, Lane.LEFT, spawnY, height = 180f))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_HIGH, Lane.CENTER, spawnY))
        spawnCoinRun(Lane.RIGHT, spawnY, 4)
      }

      // 2: Double barrier on Left & Center, clear Right lane with Crystal!
      2 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.DOUBLE_BARRIER, Lane.LEFT, spawnY))
        activePickups.add(TrackPickup(nextEntityId++, PickupType.CRYSTAL, Lane.RIGHT, spawnY))
      }

      // 3: Double barrier on Center & Right, clear Left lane with Magnet power-up!
      3 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.DOUBLE_BARRIER, Lane.RIGHT, spawnY))
        activePickups.add(TrackPickup(nextEntityId++, PickupType.POWERUP_MAGNET, Lane.LEFT, spawnY))
      }

      // 4: Low barriers on Left and Right, high barrier on Center (Jump or Slide challenge)
      4 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.LEFT, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_HIGH, Lane.CENTER, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.RIGHT, spawnY))
        spawnCoinRun(Lane.CENTER, spawnY - 100f, 3)
      }

      // 5: Train on Center, Shield powerup on Left
      5 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.TRAIN, Lane.CENTER, spawnY, height = 180f))
        activePickups.add(TrackPickup(nextEntityId++, PickupType.POWERUP_SHIELD, Lane.LEFT, spawnY))
        spawnCoinRun(Lane.RIGHT, spawnY, 4)
      }

      // 6: Trains on Left and Right, Center has low barrier (Jump through the middle!)
      6 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.TRAIN, Lane.LEFT, spawnY, height = 180f))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.TRAIN, Lane.RIGHT, spawnY, height = 180f))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.CENTER, spawnY))
        spawnCoinRun(Lane.CENTER, spawnY - 80f, 3)
      }

      // 7: High barriers on all lanes with Coin trail underneath (Slide across all lanes)
      7 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_HIGH, Lane.LEFT, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_HIGH, Lane.CENTER, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_HIGH, Lane.RIGHT, spawnY))
        spawnCoinRun(Lane.CENTER, spawnY, 4)
      }

      // 8: Rare Boost Power-up on Center, Low barriers on sides
      8 -> {
        activePickups.add(TrackPickup(nextEntityId++, PickupType.POWERUP_BOOST, Lane.CENTER, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.LEFT, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.RIGHT, spawnY))
      }

      // 9: Zig-zag run: Train on Right, Crystal on Center, Low barrier on Left
      9 -> {
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.TRAIN, Lane.RIGHT, spawnY, height = 180f))
        activePickups.add(TrackPickup(nextEntityId++, PickupType.CRYSTAL, Lane.CENTER, spawnY))
        activeObstacles.add(TrackObstacle(nextEntityId++, ObstacleType.BARRIER_LOW, Lane.LEFT, spawnY + 120f))
      }
    }
  }

  private fun spawnCoinRun(lane: Lane, startY: Float, count: Int) {
    for (i in 0 until count) {
      activePickups.add(
        TrackPickup(
          id = nextEntityId++,
          type = PickupType.COIN,
          lane = lane,
          y = startY + (i * 65f)
        )
      )
    }
  }

  fun spawnParticleBurst(x: Float, y: Float, count: Int, colorHex: Long, speedMult: Float = 1f) {
    for (i in 0 until count) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val speed = (40f + Math.random().toFloat() * 160f) * speedMult
      val maxLife = 0.3f + Math.random().toFloat() * 0.4f
      particles.add(
        RunnerParticle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          life = maxLife,
          maxLife = maxLife,
          color = colorHex,
          size = 3f + Math.random().toFloat() * 4f
        )
      )
    }
  }
}
