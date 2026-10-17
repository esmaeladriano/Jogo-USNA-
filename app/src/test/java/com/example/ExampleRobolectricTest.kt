package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.trilho.engine.TrilhoEngine
import com.example.trilho.model.Lane
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TRILHO", appName)
  }

  @Test
  fun `test trilho engine lane switching`() {
    val engine = TrilhoEngine()
    assertEquals(Lane.CENTER, engine.currentLane)

    // Move left to Lane.LEFT
    assertTrue(engine.moveLeft())
    assertEquals(Lane.LEFT, engine.targetLane)

    // Move right back to Lane.CENTER
    assertTrue(engine.moveRight())
    assertEquals(Lane.CENTER, engine.targetLane)
  }

  @Test
  fun `test trilho engine jump and slide`() {
    val engine = TrilhoEngine()
    assertFalse(engine.isJumping)
    assertFalse(engine.isSliding)

    // Initiate jump
    assertTrue(engine.jump())
    assertTrue(engine.isJumping)

    // Cannot jump while already jumping
    assertFalse(engine.jump())

    // Slide cancels jump descent or activates slide
    assertTrue(engine.slide())
    assertTrue(engine.isSliding)
  }

  @Test
  fun `test crystal ability activation`() {
    val engine = TrilhoEngine()
    assertEquals(0, engine.crystalsStock)

    // Cannot use crystal ability with 0 stock
    assertFalse(engine.useCrystalAbility())

    // Add crystal
    engine.crystalsStock = 2
    assertTrue(engine.useCrystalAbility())
    assertEquals(1, engine.crystalsStock)
    assertTrue(engine.crystalInvulnTicks > 0)
  }
}
