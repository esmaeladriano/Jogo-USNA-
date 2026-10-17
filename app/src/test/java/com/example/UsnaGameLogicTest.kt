package com.example

import com.example.game.engine.UsnaLevels
import com.example.game.engine.UsnaPhysicsEngine
import com.example.game.model.*
import org.junit.Assert.*
import org.junit.Test

class UsnaGameLogicTest {

  @Test
  fun testAllTenLevelsAreDefinedAndValid() {
    val levels = UsnaLevels.getAllLevels()
    assertEquals(10, levels.size)
    for (level in levels) {
      assertNotNull(level.namePt)
      assertNotNull(level.nameEn)
      assertTrue(level.platforms.isNotEmpty())
      assertNotNull(level.exitPortal)
      assertTrue(level.playerSpawn.x > 0f)
      assertTrue(level.playerSpawn.y > 0f)
    }
  }

  @Test
  fun testRecordingInputsLimitsToThreeSeconds() {
    val level = UsnaLevels.getLevel(3)
    val engine = UsnaPhysicsEngine(level)
    engine.startRecording()
    assertTrue(engine.isRecording)

    // Send 200 ticks of right movement (exceeding 180 max ticks)
    for (i in 0 until 200) {
      engine.tick(GameInput(moveRight = true))
    }

    // Should stop recording automatically at 180 ticks (3.0 seconds at 60Hz)
    assertFalse(engine.isRecording)
    assertNotNull(engine.pendingRecording)
    assertEquals(180, engine.pendingRecording!!.inputs.size)
  }

  @Test
  fun testEchoReleaseAndMaxLimit() {
    val level = UsnaLevels.getLevel(8) // Max 3 echoes
    val engine = UsnaPhysicsEngine(level)

    // Record movement
    engine.startRecording()
    for (i in 0 until 30) {
      engine.tick(GameInput(moveRight = true))
    }
    engine.stopRecording()

    assertTrue(engine.canReleaseEcho())
    assertTrue(engine.releaseEcho())
    assertEquals(1, engine.activeEchoes.size)

    // Can release up to maxEchoes
    assertTrue(engine.releaseEcho())
    assertTrue(engine.releaseEcho())
    assertEquals(3, engine.activeEchoes.size)

    // Cannot release fourth echo
    assertFalse(engine.releaseEcho())
  }

  @Test
  fun testTimedButtonTwoSecondCountdown() {
    val level = UsnaLevels.getLevel(7) // Has a timed button (type = TIMED)
    val engine = UsnaPhysicsEngine(level)
    val timedButton = engine.buttons.first { it.type == ButtonType.TIMED }

    // Place player directly on button
    engine.playerPosition = Vector2(timedButton.rect.centerX, timedButton.rect.top)
    engine.tick(GameInput())

    assertTrue(timedButton.isActive)
    assertEquals(120, timedButton.timerRemainingTicks) // 2.0s * 60Hz = 120 ticks

    // Move player away
    engine.playerPosition = Vector2(0f, 0f)
    engine.tick(GameInput())

    // Button should still be active due to timed countdown
    assertTrue(timedButton.isActive)
    assertEquals(119, timedButton.timerRemainingTicks)

    // Fast-forward 120 ticks
    for (i in 0 until 120) {
      engine.tick(GameInput())
    }

    // Button should now be inactive
    assertFalse(timedButton.isActive)
    assertEquals(0, timedButton.timerRemainingTicks)
  }

  @Test
  fun testSpikeCollisionKillsPlayer() {
    val level = UsnaLevels.getLevel(4) // Has spikes
    val engine = UsnaPhysicsEngine(level)
    val spike = level.spikes.first()

    assertFalse(engine.isPlayerDead)

    // Place player directly on spike
    engine.playerPosition = Vector2(spike.rect.centerX, spike.rect.top + 10f)
    engine.tick(GameInput())
    assertTrue(engine.isPlayerDead)
  }

  @Test
  fun testUndoLastEcho() {
    val level = UsnaLevels.getLevel(5)
    val engine = UsnaPhysicsEngine(level)

    engine.startRecording()
    for (i in 0 until 20) {
      engine.tick(GameInput(moveRight = true))
    }
    engine.stopRecording()

    assertFalse(engine.canUndoLastEcho())
    engine.releaseEcho()
    assertEquals(1, engine.activeEchoes.size)
    assertTrue(engine.canUndoLastEcho())

    val undone = engine.undoLastEcho()
    assertTrue(undone)
    assertEquals(0, engine.activeEchoes.size)
    assertFalse(engine.canUndoLastEcho())
  }

  @Test
  fun testSkinsRepositoryIntegrity() {
    val skins = SkinRepository.skins
    assertTrue(skins.isNotEmpty())
    val defaultSkin = SkinRepository.getSkin("solar")
    assertEquals("solar", defaultSkin.id)
    assertEquals(0, defaultSkin.requiredStars)
  }
}

