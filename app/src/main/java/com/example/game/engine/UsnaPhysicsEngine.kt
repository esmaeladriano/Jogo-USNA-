package com.example.game.engine

import com.example.game.model.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class UsnaPhysicsEngine(
  var level: LevelData,
  private val onEvent: (EngineEvent) -> Unit = {}
) {
  sealed class EngineEvent {
    object Jump : EngineEvent()
    object RecordStart : EngineEvent()
    object RecordEnd : EngineEvent()
    object ReleaseEcho : EngineEvent()
    object ButtonPressed : EngineEvent()
    object ButtonReleased : EngineEvent()
    object DoorOpen : EngineEvent()
    object DoorClose : EngineEvent()
    object SpikeDeath : EngineEvent()
    object EchoDestroyed : EngineEvent()
    data class LevelWon(val timeSec: Float, val stars: Int, val echoesUsed: Int) : EngineEvent()
  }

  // Physics Constants (tuned for responsive platforming at 60Hz)
  companion object {
    const val TICK_RATE = 60
    const val DELTA_TIME = 1f / TICK_RATE
    const val MAX_RECORD_TICKS = 180 // 3.0 seconds at 60Hz
    const val COYOTE_TICKS = 6       // 0.1s
    const val JUMP_BUFFER_TICKS = 6  // 0.1s
    const val DEATH_RESPAWN_TICKS = 30 // 0.5s

    const val GRAVITY = 1100f
    const val MOVE_ACCEL = 2200f
    const val MOVE_MAX_SPEED = 280f
    const val FRICTION_GROUND = 1800f
    const val FRICTION_AIR = 600f
    const val JUMP_INITIAL_VELOCITY = -450f
    const val JUMP_CUT_VELOCITY = -140f
  }

  // Player state
  var playerPosition = level.playerSpawn
  var playerVelocity = Vector2()
  var playerWidth = 28f
  var playerHeight = 36f
  var isPlayerGrounded = false
  var playerFacingRight = true
  var coyoteTimer = 0
  var jumpBufferTimer = 0
  var isPlayerDead = false
  var deathTimer = 0
  var isLevelCompleted = false

  // Recording state
  var isRecording = false
  var recordingInputs = mutableListOf<GameInput>()
  var recordingSpawnPosition = level.playerSpawn
  var pendingRecording: EchoRecording? = null

  // Active Echoes (up to 3)
  val activeEchoes = mutableListOf<EchoInstance>()
  private var nextEchoId = 1
  var totalEchoesUsedInLevel = 0

  // Interactive Level Objects state
  val buttons = level.buttons.map { it.copy() }.toMutableList()
  val doors = level.doors.map { it.copy() }.toMutableList()

  // Thread synchronization lock
  val stateLock = Any()

  // Metrics
  var levelTicks = 0
  val levelTimeSec: Float get() = levelTicks / TICK_RATE.toFloat()

  fun resetLevel() {
    synchronized(stateLock) {
      playerPosition = level.playerSpawn
      playerVelocity = Vector2()
      isPlayerGrounded = false
      playerFacingRight = true
      coyoteTimer = 0
      jumpBufferTimer = 0
      isPlayerDead = false
      deathTimer = 0
      isLevelCompleted = false
      isRecording = false
      recordingInputs.clear()
      pendingRecording = null
      activeEchoes.clear()
      totalEchoesUsedInLevel = 0
      levelTicks = 0

      // Reset buttons and doors
      buttons.clear()
      buttons.addAll(level.buttons.map { it.copy() })
      doors.clear()
      doors.addAll(level.doors.map { it.copy() })
    }
  }

  fun changeLevel(newLevel: LevelData) {
    this.level = newLevel
    resetLevel()
  }

  fun getPlayerBounds(): Rect2 {
    return Rect2(playerPosition.x - playerWidth / 2f, playerPosition.y - playerHeight, playerWidth, playerHeight)
  }

  // User Action: Toggle or Start Recording
  fun startRecording() {
    if (isPlayerDead || isLevelCompleted || isRecording) return
    isRecording = true
    recordingInputs.clear()
    recordingSpawnPosition = playerPosition
    onEvent(EngineEvent.RecordStart)
  }

  fun stopRecording() {
    if (!isRecording) return
    isRecording = false
    if (recordingInputs.isNotEmpty()) {
      pendingRecording = EchoRecording(
        spawnPosition = recordingSpawnPosition,
        inputs = recordingInputs.toList()
      )
    }
    onEvent(EngineEvent.RecordEnd)
  }

  // User Action: Release Echo
  fun releaseEcho(): Boolean {
    if (isPlayerDead || isLevelCompleted) return false
    // If currently recording, finalize recording first
    if (isRecording) {
      stopRecording()
    }
    val recording = pendingRecording ?: return false
    if (activeEchoes.size >= level.maxEchoes) {
      return false
    }

    val echo = EchoInstance(
      id = nextEchoId++,
      recording = recording,
      position = recording.spawnPosition,
      velocity = Vector2(),
      playbackTick = 0,
      isGrounded = false,
      isFinished = false,
      isDestroyed = false,
      facingRight = true
    )
    activeEchoes.add(echo)
    totalEchoesUsedInLevel++
    spawnParticleBurst(echo.position.x, echo.position.y - 18f, 20, 0xFF38E1FF)
    onEvent(EngineEvent.ReleaseEcho)
    return true
  }

  fun undoLastEcho(): Boolean {
    if (activeEchoes.isNotEmpty()) {
      val removed = activeEchoes.removeAt(activeEchoes.lastIndex)
      spawnParticleBurst(removed.position.x, removed.position.y - 18f, 18, 0xFF38E1FF)
      onEvent(EngineEvent.RecordEnd)
      return true
    }
    return false
  }

  fun canUndoLastEcho(): Boolean {
    return activeEchoes.isNotEmpty() && !isPlayerDead && !isLevelCompleted
  }

  val particles = mutableListOf<VisualParticle>()

  fun spawnParticleBurst(x: Float, y: Float, count: Int, colorHex: Long, speedScale: Float = 1f) {
    for (i in 0 until count) {
      val angle = (Math.random() * 2 * Math.PI).toFloat()
      val speed = (40f + Math.random().toFloat() * 150f) * speedScale
      val maxLife = 0.35f + Math.random().toFloat() * 0.45f
      particles.add(
        VisualParticle(
          x = x,
          y = y,
          vx = kotlin.math.cos(angle) * speed,
          vy = kotlin.math.sin(angle) * speed,
          life = maxLife,
          maxLife = maxLife,
          color = colorHex,
          size = 2.5f + Math.random().toFloat() * 3.5f
        )
      )
    }
  }

  fun canReleaseEcho(): Boolean {
    return pendingRecording != null && activeEchoes.size < level.maxEchoes
  }

  fun canStartRecording(): Boolean {
    return !isRecording && activeEchoes.size < level.maxEchoes
  }

  // Fixed 60Hz physics tick
  fun tick(input: GameInput) {
    synchronized(stateLock) {
      if (isLevelCompleted) return

      // Handle death respawn timer
      if (isPlayerDead) {
        deathTimer++
        if (deathTimer >= DEATH_RESPAWN_TICKS) {
          resetLevel()
        }
        return
      }

      levelTicks++

      // 1. Process Recording
      if (isRecording) {
        recordingInputs.add(input)
        if (recordingInputs.size >= MAX_RECORD_TICKS) {
          stopRecording()
        }
      }

      // 2. Process Echoes physics & playback
      updateEchoes()

      // 3. Process Player physics
      updatePlayer(input)

      // 4. Update Interactive Entities (Buttons, Doors)
      updateButtonsAndDoors()

      // 5. Hazard Check (Spikes)
      checkHazards()

      // 6. Victory Check (Exit Portal)
      checkVictory()

      // 7. Update Particles
      val pIter = particles.iterator()
      while (pIter.hasNext()) {
        val p = pIter.next()
        p.life -= DELTA_TIME
        if (p.life <= 0f) {
          pIter.remove()
        } else {
          p.x += p.vx * DELTA_TIME
          p.y += p.vy * DELTA_TIME
          p.vy += 220f * DELTA_TIME // slight gravity
        }
      }
    }
  }

  private fun updatePlayer(input: GameInput) {
    // Jump Buffer Handling
    if (input.jump) {
      jumpBufferTimer = JUMP_BUFFER_TICKS
    } else if (jumpBufferTimer > 0) {
      jumpBufferTimer--
    }

    // Coyote Time Handling
    if (isPlayerGrounded) {
      coyoteTimer = COYOTE_TICKS
    } else if (coyoteTimer > 0) {
      coyoteTimer--
    }

    // Horizontal Movement
    val targetSpeed = input.horizontalAxis * MOVE_MAX_SPEED
    val accel = if (isPlayerGrounded) MOVE_ACCEL else MOVE_ACCEL * 0.75f
    val friction = if (isPlayerGrounded) FRICTION_GROUND else FRICTION_AIR

    var vx = playerVelocity.x
    if (input.horizontalAxis != 0f) {
      vx = moveTowards(vx, targetSpeed, accel * DELTA_TIME)
      playerFacingRight = input.horizontalAxis > 0f
    } else {
      vx = moveTowards(vx, 0f, friction * DELTA_TIME)
    }

    // Vertical Movement & Gravity
    var vy = playerVelocity.y + GRAVITY * DELTA_TIME

    // Jump Execution
    if (jumpBufferTimer > 0 && coyoteTimer > 0) {
      vy = JUMP_INITIAL_VELOCITY
      jumpBufferTimer = 0
      coyoteTimer = 0
      isPlayerGrounded = false
      onEvent(EngineEvent.Jump)
    }

    // Variable Jump Height: cut jump if jump button is released early
    if (!input.jump && vy < JUMP_CUT_VELOCITY) {
      vy = JUMP_CUT_VELOCITY
    }

    playerVelocity = Vector2(vx, vy)

    // Move & Collide with environment
    val newPos = resolveMoveAndCollide(
      currentPos = playerPosition,
      velocity = playerVelocity,
      width = playerWidth,
      height = playerHeight,
      onGroundChange = { isPlayerGrounded = it },
      onVelocityAdjust = { playerVelocity = it },
      isEcho = false
    )
    playerPosition = newPos
  }

  private fun updateEchoes() {
    val iterator = activeEchoes.iterator()
    while (iterator.hasNext()) {
      val echo = iterator.next()
      if (echo.isDestroyed) {
        iterator.remove()
        continue
      }

      val echoInput = if (echo.playbackTick < echo.recording.inputs.size) {
        echo.recording.inputs[echo.playbackTick]
      } else {
        echo.isFinished = true
        if (!level.echoStaysAtEnd) {
          iterator.remove()
          continue
        }
        GameInput() // Idle when playback completes
      }

      echo.playbackTick++

      // Echo Physics Simulation (identical deterministic model)
      val targetSpeed = echoInput.horizontalAxis * MOVE_MAX_SPEED
      val accel = if (echo.isGrounded) MOVE_ACCEL else MOVE_ACCEL * 0.75f
      val friction = if (echo.isGrounded) FRICTION_GROUND else FRICTION_AIR

      var vx = echo.velocity.x
      if (echoInput.horizontalAxis != 0f) {
        vx = moveTowards(vx, targetSpeed, accel * DELTA_TIME)
        echo.facingRight = echoInput.horizontalAxis > 0f
      } else {
        vx = moveTowards(vx, 0f, friction * DELTA_TIME)
      }

      var vy = echo.velocity.y + GRAVITY * DELTA_TIME
      if (echoInput.jump && echo.isGrounded) {
        vy = JUMP_INITIAL_VELOCITY
        echo.isGrounded = false
      }
      if (!echoInput.jump && vy < JUMP_CUT_VELOCITY) {
        vy = JUMP_CUT_VELOCITY
      }

      echo.velocity = Vector2(vx, vy)

      echo.position = resolveMoveAndCollide(
        currentPos = echo.position,
        velocity = echo.velocity,
        width = echo.width,
        height = echo.height,
        onGroundChange = { echo.isGrounded = it },
        onVelocityAdjust = { echo.velocity = it },
        isEcho = true
      )

      // Check echo spike collision
      val echoBox = echo.boundingBox()
      for (spike in level.spikes) {
        if (spike.rect.intersects(echoBox)) {
          echo.isDestroyed = true
          onEvent(EngineEvent.EchoDestroyed)
          break
        }
      }
    }
  }

  private fun updateButtonsAndDoors() {
    val playerBox = getPlayerBounds()
    val allBodies = mutableListOf<Rect2>().apply {
      if (!isPlayerDead) add(playerBox)
      for (echo in activeEchoes) {
        if (!echo.isDestroyed) add(echo.boundingBox())
      }
    }

    for (button in buttons) {
      button.wasPressed = button.isPressed
      var currentlySteppedOn = false

      // Check if any body stands on or touches button
      // A body is "on" button if horizontal bounds overlap and body bottom is close to button top
      for (body in allBodies) {
        if (body.intersects(button.rect) ||
          (abs(body.bottom - button.rect.top) < 6f && body.right > button.rect.left && body.left < button.rect.right)
        ) {
          currentlySteppedOn = true
          break
        }
      }

      button.isPressed = currentlySteppedOn

      if (button.type == ButtonType.TIMED) {
        if (currentlySteppedOn) {
          button.timerRemainingTicks = button.totalTimerTicks
        } else if (button.timerRemainingTicks > 0) {
          button.timerRemainingTicks--
        }
      }

      // Audio / Feedback on press/release
      if (button.isPressed && !button.wasPressed) {
        onEvent(EngineEvent.ButtonPressed)
      } else if (!button.isPressed && button.wasPressed && button.type != ButtonType.TIMED) {
        onEvent(EngineEvent.ButtonReleased)
      }
    }

    // Update Door States according to their linked Button
    for (door in doors) {
      val linkedButton = buttons.find { it.id == door.buttonId }
      val shouldBeOpen = linkedButton?.isActive == true

      if (shouldBeOpen != door.isOpen) {
        door.isOpen = shouldBeOpen
        if (door.isOpen) {
          onEvent(EngineEvent.DoorOpen)
        } else {
          onEvent(EngineEvent.DoorClose)
        }
      }

      // Smooth opening/closing interpolation
      val speed = 8f * DELTA_TIME
      if (door.isOpen) {
        door.openProgress = min(1f, door.openProgress + speed)
      } else {
        door.openProgress = max(0f, door.openProgress - speed)
      }
    }
  }

  private fun checkHazards() {
    if (isPlayerDead) return
    val playerBox = getPlayerBounds()

    // Slight inset for fair gameplay hitboxes
    val hitbox = Rect2(
      playerBox.x + 4f,
      playerBox.y + 4f,
      playerBox.width - 8f,
      playerBox.height - 6f
    )

    for (spike in level.spikes) {
      if (spike.rect.intersects(hitbox)) {
        isPlayerDead = true
        deathTimer = 0
        onEvent(EngineEvent.SpikeDeath)
        return
      }
    }
  }

  private fun checkVictory() {
    if (isPlayerDead || isLevelCompleted) return
    val playerBox = getPlayerBounds()
    if (level.exitPortal.rect.intersects(playerBox)) {
      isLevelCompleted = true
      val completionTime = levelTimeSec
      val stars = calculateStars(completionTime, totalEchoesUsedInLevel)
      onEvent(EngineEvent.LevelWon(completionTime, stars, totalEchoesUsedInLevel))
    }
  }

  private fun calculateStars(timeSec: Float, echoesUsed: Int): Int {
    // 3 stars: completed efficiently (<= targetTime + reasonable echo budget)
    // 2 stars: completed within 2x targetTime
    // 1 star: completed
    return when {
      timeSec <= level.targetTimeSec && echoesUsed <= max(1, level.maxEchoes) -> 3
      timeSec <= level.targetTimeSec * 1.5f -> 2
      else -> 1
    }
  }

  // Robust AABB Swept / Step Collision Resolution
  private fun resolveMoveAndCollide(
    currentPos: Vector2,
    velocity: Vector2,
    width: Float,
    height: Float,
    onGroundChange: (Boolean) -> Unit,
    onVelocityAdjust: (Vector2) -> Unit,
    isEcho: Boolean
  ): Vector2 {
    var posX = currentPos.x
    var posY = currentPos.y
    var vx = velocity.x
    var vy = velocity.y
    var grounded = false

    // Collect all solid colliders: Platforms + Active Doors
    val solidRects = mutableListOf<Rect2>()
    for (platform in level.platforms) {
      solidRects.add(platform.rect)
    }
    for (door in doors) {
      door.collisionBox()?.let { solidRects.add(it) }
    }

    // If this is the player, and echoes are solid when resting/playback, add echoes as colliders!
    if (!isEcho && level.echoStaysAtEnd) {
      for (echo in activeEchoes) {
        if (!echo.isDestroyed && echo.isGrounded) {
          // Add echo body top surface as solid stepping platform
          solidRects.add(echo.boundingBox())
        }
      }
    }

    // 1. Move X first
    val dx = vx * DELTA_TIME
    if (dx != 0f) {
      posX += dx
      val testBoxX = Rect2(posX - width / 2f, posY - height, width, height)
      for (rect in solidRects) {
        if (testBoxX.intersects(rect)) {
          if (dx > 0) {
            posX = rect.left - width / 2f
          } else {
            posX = rect.right + width / 2f
          }
          vx = 0f
          break
        }
      }
    }

    // 2. Move Y second
    val dy = vy * DELTA_TIME
    if (dy != 0f) {
      posY += dy
      val testBoxY = Rect2(posX - width / 2f, posY - height, width, height)
      for (rect in solidRects) {
        if (testBoxY.intersects(rect)) {
          if (dy > 0) { // Landing on top of a surface
            posY = rect.top
            grounded = true
            vy = 0f
          } else { // Bonking ceiling
            posY = rect.bottom + height
            vy = 0f
          }
          break
        }
      }
    }

    // Extra ground check if falling or standing still
    if (!grounded) {
      val footBox = Rect2(posX - width / 2f + 2f, posY, width - 4f, 2f)
      for (rect in solidRects) {
        if (footBox.intersects(rect)) {
          grounded = true
          break
        }
      }
    }

    onGroundChange(grounded)
    onVelocityAdjust(Vector2(vx, vy))
    return Vector2(posX, posY)
  }

  private fun moveTowards(current: Float, target: Float, maxDelta: Float): Float {
    return if (abs(target - current) <= maxDelta) {
      target
    } else {
      current + kotlin.math.sign(target - current) * maxDelta
    }
  }
}
