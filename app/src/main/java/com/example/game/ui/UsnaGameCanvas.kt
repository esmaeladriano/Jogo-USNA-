package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.engine.UsnaPhysicsEngine
import com.example.game.model.*
import kotlin.math.*

@Composable
fun UsnaGameCanvas(
  engine: UsnaPhysicsEngine,
  skin: SkinTheme = SkinRepository.skins.first(),
  modifier: Modifier = Modifier
) {
  // Continuous frame animation clock for vibrant visual effects
  val infiniteTransition = rememberInfiniteTransition(label = "game_visuals")
  val pulseAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse"
  )

  val energyFlow by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "energy_flow"
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val targetWidth = 1280f
    val targetHeight = 720f

    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      val scale = min(canvasWidth / targetWidth, canvasHeight / targetHeight)
      val offsetX = (canvasWidth - targetWidth * scale) / 2f
      val offsetY = (canvasHeight - targetHeight * scale) / 2f

      // Deep void letterboxing
      drawRect(Color(0xFF090D14), Offset.Zero, size)

      drawContext.canvas.save()
      drawContext.canvas.translate(offsetX, offsetY)
      drawContext.canvas.scale(scale, scale)

      // 1. Sci-Fi Cyberpunk Background with Star Dust
      drawAtmosphericBackground(pulseAnim, engine.level)

      // 2. Interactive Energy Conduits connecting Buttons to Doors
      drawConduitCables(engine, energyFlow)

      // 3. Static Platforms with Glowing Edge Bevels
      for (platform in engine.level.platforms) {
        drawPlatform(platform, pulseAnim)
      }

      // 4. Pressure Buttons
      for (button in engine.buttons) {
        drawButton(button, pulseAnim)
      }

      // 5. High-Tech Forcefield Doors
      for (door in engine.doors) {
        drawDoor(door, pulseAnim)
      }

      // 6. Hazard Energy Spikes
      for (spike in engine.level.spikes) {
        drawSpikes(spike, pulseAnim)
      }

      // 7. Celestial Exit Portal Vortex
      drawExitPortal(engine.level.exitPortal, pulseAnim)

      // 8. Active Echoes (Holographic scanline phantoms)
      for (echo in engine.activeEchoes) {
        if (!echo.isDestroyed) {
          drawEcho(echo, pulseAnim)
        }
      }

      // 9. Player Character ("Eco-Sprite")
      if (!engine.isPlayerDead) {
        drawPlayer(engine, skin, pulseAnim)
      } else {
        drawPlayerDeathEffect(engine.playerPosition, engine.deathTimer, skin)
      }

      // 10. Dynamic Particles System
      drawParticles(engine.particles)

      // 11. Active Recording Aura & Indicator
      if (engine.isRecording) {
        drawRecordingOverlay(engine, pulseAnim)
      }

      drawContext.canvas.restore()
    }
  }
}

private fun DrawScope.drawAtmosphericBackground(pulse: Float, level: LevelData) {
  // Vibrant multi-stop gradient background
  val topColor = Color(0xFF131A29)
  val midColor = Color(0xFF0E1420)
  val bottomColor = Color(0xFF0A0E17)

  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(topColor, midColor, bottomColor),
      startY = 0f,
      endY = 720f
    ),
    topLeft = Offset(0f, 0f),
    size = Size(1280f, 720f)
  )

  // Cybernetic neon grid lines with soft opacity
  val gridColor = Color(0x1838E1FF)
  var x = 0f
  while (x <= 1280f) {
    drawLine(gridColor, Offset(x, 0f), Offset(x, 720f), strokeWidth = 1f)
    x += 80f
  }
  var y = 0f
  while (y <= 720f) {
    drawLine(gridColor, Offset(0f, y), Offset(1280f, y), strokeWidth = 1f)
    y += 80f
  }

  // Floating background ambient light blooms (Cyan & Violet)
  val glowAlpha1 = 0.08f + 0.04f * sin(pulse * 2 * PI).toFloat()
  val glowAlpha2 = 0.06f + 0.03f * cos(pulse * 2 * PI).toFloat()

  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFF38E1FF).copy(alpha = glowAlpha1), Color.Transparent),
      center = Offset(320f, 240f),
      radius = 400f
    ),
    center = Offset(320f, 240f),
    radius = 400f
  )

  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFFB388FF).copy(alpha = glowAlpha2), Color.Transparent),
      center = Offset(960f, 380f),
      radius = 420f
    ),
    center = Offset(960f, 380f),
    radius = 420f
  )
}

// Draw glowing cyber conduits connecting each button directly to its linked door
private fun DrawScope.drawConduitCables(engine: UsnaPhysicsEngine, flow: Float) {
  for (door in engine.doors) {
    val linkedBtn = engine.buttons.find { it.id == door.buttonId } ?: continue
    val startX = linkedBtn.rect.centerX
    val startY = linkedBtn.rect.centerY
    val endX = door.rect.centerX
    val endY = door.rect.centerY

    val isActive = linkedBtn.isActive
    val cableColor = if (isActive) Color(0xFF00E676) else Color(0xFF23445C)
    val glowColor = if (isActive) Color(0x6600E676) else Color(0x2238E1FF)

    val path = Path().apply {
      moveTo(startX, startY)
      // High-tech stepped ortholine cable
      val midX = (startX + endX) / 2f
      lineTo(midX, startY)
      lineTo(midX, endY)
      lineTo(endX, endY)
    }

    // Outer glow aura
    drawPath(path = path, color = glowColor, style = Stroke(width = if (isActive) 5f else 2.5f))
    // Core wire
    drawPath(path = path, color = cableColor, style = Stroke(width = if (isActive) 2f else 1f))

    // Flowing energy pulse dot traveling along the cable
    if (isActive) {
      val pulseT = flow
      val px = startX + (endX - startX) * pulseT
      val py = startY + (endY - startY) * pulseT
      drawCircle(Color.White, radius = 3.5f, center = Offset(px, py))
      drawCircle(Color(0xFF00E676), radius = 6f, center = Offset(px, py))
    }
  }
}

private fun DrawScope.drawPlatform(platform: Platform, pulse: Float) {
  val r = platform.rect

  // Platform Base Body (Sleek dark tech slate)
  drawRoundRect(
    color = Color(0xFF1E2838),
    topLeft = Offset(r.x, r.y),
    size = Size(r.width, r.height),
    cornerRadius = CornerRadius(5f, 5f)
  )

  // Gradient inner plate
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF29364C), Color(0xFF182230)),
      startY = r.y,
      endY = r.bottom
    ),
    topLeft = Offset(r.x + 1f, r.y + 1f),
    size = Size(r.width - 2f, r.height - 2f),
    cornerRadius = CornerRadius(4f, 4f)
  )

  // Vibrant Glowing Top Surface Bevel (Neon Cyan & Sapphire accent)
  drawRoundRect(
    brush = Brush.horizontalGradient(
      colors = listOf(Color(0xFF38E1FF), Color(0xFF00B0FF), Color(0xFF38E1FF))
    ),
    topLeft = Offset(r.x, r.y),
    size = Size(r.width, min(4f, r.height)),
    cornerRadius = CornerRadius(3f, 3f)
  )

  // Subtle metallic bevel outline
  drawRoundRect(
    color = Color(0xFF101724),
    topLeft = Offset(r.x, r.y),
    size = Size(r.width, r.height),
    cornerRadius = CornerRadius(5f, 5f),
    style = Stroke(width = 1.2f)
  )
}

private fun DrawScope.drawButton(button: ButtonState, pulse: Float) {
  val r = button.rect
  val isPressed = button.isPressed
  val isActive = button.isActive

  // Heavy steel base housing
  drawRoundRect(
    color = Color(0xFF161E2C),
    topLeft = Offset(r.x - 2f, r.y + 3f),
    size = Size(r.width + 4f, r.height - 1f),
    cornerRadius = CornerRadius(4f, 4f)
  )

  // Plate compression animation
  val capHeight = if (isPressed) 4f else 8f
  val capY = if (isPressed) r.y + r.height - capHeight else r.y

  val capColor = when {
    button.type == ButtonType.TIMED -> if (isActive) Color(0xFFFFD600) else Color(0xFF9E8400)
    isActive -> Color(0xFF00E676)
    else -> Color(0xFF00A352)
  }

  // Radiant button top pad
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(capColor, capColor.copy(alpha = 0.7f)),
      startY = capY,
      endY = capY + capHeight
    ),
    topLeft = Offset(r.x + 2f, capY),
    size = Size(r.width - 4f, capHeight),
    cornerRadius = CornerRadius(3f, 3f)
  )

  // Pulsing glow aura when engaged
  if (isActive) {
    val auraAlpha = 0.35f + 0.2f * sin(pulse * 6 * PI).toFloat()
    drawRoundRect(
      color = capColor.copy(alpha = auraAlpha),
      topLeft = Offset(r.x - 4f, r.y - 4f),
      size = Size(r.width + 8f, r.height + 6f),
      cornerRadius = CornerRadius(6f, 6f),
      style = Stroke(width = 2.5f)
    )
  }

  // Timed button countdown ring / bar
  if (button.type == ButtonType.TIMED && button.timerRemainingTicks > 0) {
    val progress = button.timerRemainingTicks / button.totalTimerTicks.toFloat()
    val gaugeY = r.y - 12f

    // Gauge container
    drawRoundRect(
      color = Color(0xAA000000),
      topLeft = Offset(r.x, gaugeY),
      size = Size(r.width, 5f),
      cornerRadius = CornerRadius(2f, 2f)
    )
    // Dynamic countdown fill (Yellow to Red as time ticks down)
    val gaugeColor = if (progress > 0.3f) Color(0xFFFFD600) else Color(0xFFFF3D57)
    drawRoundRect(
      color = gaugeColor,
      topLeft = Offset(r.x, gaugeY),
      size = Size(r.width * progress, 5f),
      cornerRadius = CornerRadius(2f, 2f)
    )
  }
}

private fun DrawScope.drawDoor(door: DoorState, pulse: Float) {
  val r = door.rect
  val openProg = door.openProgress

  // Anchor pylons at top and bottom
  drawRoundRect(Color(0xFF141B26), Offset(r.x - 3f, r.y - 3f), Size(r.width + 6f, 8f), CornerRadius(2f, 2f))
  drawRoundRect(Color(0xFF141B26), Offset(r.x - 3f, r.bottom - 5f), Size(r.width + 6f, 8f), CornerRadius(2f, 2f))

  if (openProg < 0.98f) {
    val visibleHeight = r.height * (1f - openProg)
    val doorColor = if (door.isOpen) Color(0x6600E676) else Color(0xFF263345)

    // Barrier frame
    drawRoundRect(
      color = doorColor,
      topLeft = Offset(r.x, r.y),
      size = Size(r.width, visibleHeight),
      cornerRadius = CornerRadius(3f, 3f)
    )

    // Animated energy laser beams across the barrier
    val beamCount = 4
    for (i in 1..beamCount) {
      val ly = r.y + (visibleHeight / (beamCount + 1)) * i
      val laserColor = if (door.isOpen) Color(0xFF00E676) else Color(0xFFFF3D57)
      val beamAlpha = 0.7f + 0.3f * sin(pulse * 6 * PI + i).toFloat()

      drawLine(
        color = laserColor.copy(alpha = beamAlpha),
        start = Offset(r.x + 2f, ly),
        end = Offset(r.right - 2f, ly),
        strokeWidth = 2.5f
      )
    }

    // Heavy status lock bracket
    drawRoundRect(
      color = Color(0xFF111722),
      topLeft = Offset(r.x + 3f, r.y + visibleHeight - 7f),
      size = Size(r.width - 6f, 7f),
      cornerRadius = CornerRadius(2f, 2f)
    )
  }
}

private fun DrawScope.drawSpikes(spike: SpikeHazard, pulse: Float) {
  val r = spike.rect
  val spikeWidth = 14f
  val count = max(1, (r.width / spikeWidth).toInt())
  val actualWidth = r.width / count

  for (i in 0 until count) {
    val sx = r.x + i * actualWidth
    val path = Path().apply {
      moveTo(sx, r.bottom)
      lineTo(sx + actualWidth / 2f, r.top)
      lineTo(sx + actualWidth, r.bottom)
      close()
    }

    // Glowing Crimson Neon Spikes
    drawPath(
      path = path,
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF3D57), Color(0xFFD50000)),
        startY = r.top,
        endY = r.bottom
      )
    )

    // Danger gleam along the edges
    val gleamAlpha = 0.8f + 0.2f * sin(pulse * 5 * PI + i).toFloat()
    drawPath(
      path = path,
      color = Color(0xFFFF8A9E).copy(alpha = gleamAlpha),
      style = Stroke(width = 1.4f)
    )
  }
}

private fun DrawScope.drawExitPortal(portal: ExitPortal, pulse: Float) {
  val r = portal.rect
  val center = Offset(r.centerX, r.centerY)
  val radius = min(r.width, r.height) / 2f + 6f

  // Dynamic vortex gradient
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color(0xFFE040FB), Color(0xFF38E1FF), Color(0xFF00E5FF).copy(alpha = 0.2f), Color.Transparent),
      center = center,
      radius = radius * 1.5f
    ),
    center = center,
    radius = radius * 1.5f
  )

  // Pulsing celestial rings
  val ringColor1 = Color(0xFF38E1FF).copy(alpha = 0.6f + 0.3f * sin(pulse * 3 * PI).toFloat())
  val ringColor2 = Color(0xFFFF4081).copy(alpha = 0.5f + 0.3f * cos(pulse * 3 * PI).toFloat())

  drawCircle(color = ringColor1, center = center, radius = radius, style = Stroke(width = 3.5f))
  drawCircle(
    color = ringColor2,
    center = center,
    radius = radius * 0.72f + 3f * sin(pulse * 5 * PI).toFloat(),
    style = Stroke(width = 2.2f)
  )

  // Rotating orbital energy sparks
  for (i in 0 until 4) {
    val angle = (pulse * 2 * PI) + (i * PI / 2.0)
    val sparkX = center.x + cos(angle).toFloat() * (radius * 0.85f)
    val sparkY = center.y + sin(angle).toFloat() * (radius * 0.85f)
    drawCircle(Color.White, radius = 3f, center = Offset(sparkX, sparkY))
    drawCircle(Color(0xFF38E1FF), radius = 5f, center = Offset(sparkX, sparkY))
  }

  // Brilliant core
  drawCircle(color = Color.White, center = center, radius = radius * 0.3f)
}

private fun DrawScope.drawPlayer(engine: UsnaPhysicsEngine, skin: SkinTheme, pulse: Float) {
  val pos = engine.playerPosition
  val w = engine.playerWidth
  val h = engine.playerHeight
  val facingRight = engine.playerFacingRight
  val isGrounded = engine.isPlayerGrounded
  val vx = engine.playerVelocity.x

  // Organic bounce and run animations
  val walkBob = if (isGrounded && abs(vx) > 10f) sin(pulse * 10 * PI).toFloat() * 2.5f else 0f
  val stretchY = if (!isGrounded) 3f else 0f

  val bodyRect = Rect2(
    pos.x - w / 2f,
    pos.y - h + walkBob - stretchY,
    w,
    h + stretchY
  )

  // Ground contact shadow
  if (isGrounded) {
    drawOval(
      color = Color(0x66000000),
      topLeft = Offset(pos.x - w / 2f + 3f, pos.y - 3f),
      size = Size(w - 6f, 6f)
    )
  }

  val primaryColor = Color(skin.primaryColor)
  val secondaryColor = Color(skin.secondaryColor)
  val glowColor = Color(skin.glowColor)
  val eyesColor = Color(skin.eyesColor)

  // Outer subtle aura
  drawRoundRect(
    color = glowColor.copy(alpha = 0.25f),
    topLeft = Offset(bodyRect.x - 2f, bodyRect.y - 2f),
    size = Size(bodyRect.width + 4f, bodyRect.height + 4f),
    cornerRadius = CornerRadius(10f, 10f),
    style = Stroke(width = 1.5f)
  )

  // Torso / Creature Body
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(secondaryColor, primaryColor),
      startY = bodyRect.top,
      endY = bodyRect.bottom
    ),
    topLeft = Offset(bodyRect.x, bodyRect.y),
    size = Size(bodyRect.width, bodyRect.height),
    cornerRadius = CornerRadius(8f, 8f)
  )

  // Cute Little Ears / Crest
  val earY = bodyRect.top - 6f
  val earX1 = if (facingRight) bodyRect.x + 4f else bodyRect.right - 10f
  val earX2 = if (facingRight) bodyRect.x + 12f else bodyRect.right - 18f
  drawRoundRect(primaryColor, Offset(earX1, earY), Size(6f, 8f), CornerRadius(3f, 3f))
  drawRoundRect(primaryColor, Offset(earX2, earY + 1f), Size(6f, 7f), CornerRadius(3f, 3f))

  // Flowing energy tail / scarf in movement
  val tailDir = if (facingRight) -1f else 1f
  val tailX = if (facingRight) bodyRect.left else bodyRect.right
  val tailY = bodyRect.centerY + 4f
  drawCircle(
    color = glowColor.copy(alpha = 0.7f),
    radius = 4f,
    center = Offset(tailX + tailDir * 4f, tailY + sin(pulse * 8 * PI).toFloat() * 2f)
  )

  // Expressive Glowing Anime Eyes
  val eyeY = bodyRect.top + 10f
  val eyeOffsetX = if (facingRight) 6f else -2f
  val eye1X = bodyRect.centerX + eyeOffsetX - 4f
  val eye2X = bodyRect.centerX + eyeOffsetX + 3f

  // White eye glow
  drawCircle(Color.White, radius = 3.6f, center = Offset(eye1X, eyeY))
  drawCircle(Color.White, radius = 3.6f, center = Offset(eye2X, eyeY))

  // Pupils tracking movement
  val pupilDir = if (facingRight) 1f else -1f
  drawCircle(eyesColor, radius = 2f, center = Offset(eye1X + pupilDir, eyeY))
  drawCircle(eyesColor, radius = 2f, center = Offset(eye2X + pupilDir, eyeY))

  // Sparkle highlight
  drawCircle(Color.White, radius = 1f, center = Offset(eye1X + pupilDir - 0.5f, eyeY - 0.8f))
  drawCircle(Color.White, radius = 1f, center = Offset(eye2X + pupilDir - 0.5f, eyeY - 0.8f))
}

private fun DrawScope.drawEcho(echo: EchoInstance, pulse: Float) {
  val pos = echo.position
  val w = echo.width
  val h = echo.height
  val facingRight = echo.facingRight

  val alpha = 0.55f + 0.18f * sin(pulse * 5 * PI + echo.id).toFloat()

  // Holographic scanline aura
  drawRoundRect(
    color = Color(0x38E1FF).copy(alpha = alpha * 0.35f),
    topLeft = Offset(pos.x - w / 2f - 4f, pos.y - h - 4f),
    size = Size(w + 8f, h + 8f),
    cornerRadius = CornerRadius(10f, 10f)
  )

  // Ghostly Echo Body
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0x80D8FF).copy(alpha = alpha), Color(0x0091EA).copy(alpha = alpha * 0.75f)),
      startY = pos.y - h,
      endY = pos.y
    ),
    topLeft = Offset(pos.x - w / 2f, pos.y - h),
    size = Size(w, h),
    cornerRadius = CornerRadius(8f, 8f)
  )

  // Ghost Eyes
  val eyeY = pos.y - h + 10f
  val eyeOffsetX = if (facingRight) 5f else -3f
  val eye1X = pos.x + eyeOffsetX - 4f
  val eye2X = pos.x + eyeOffsetX + 3f

  drawCircle(Color.White.copy(alpha = alpha), radius = 3f, center = Offset(eye1X, eyeY))
  drawCircle(Color.White.copy(alpha = alpha), radius = 3f, center = Offset(eye2X, eyeY))

  // Holographic echo badge
  drawCircle(
    color = Color(0xFF00B0FF).copy(alpha = alpha),
    radius = 6f,
    center = Offset(pos.x, pos.y - h - 10f)
  )
}

private fun DrawScope.drawParticles(particles: List<VisualParticle>) {
  for (p in particles) {
    val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
    val pColor = Color(p.color).copy(alpha = alpha)
    drawCircle(
      color = pColor,
      radius = p.size * alpha,
      center = Offset(p.x, p.y)
    )
  }
}

private fun DrawScope.drawPlayerDeathEffect(pos: Vector2, deathTimer: Int, skin: SkinTheme) {
  val progress = deathTimer / 30f
  val radius = progress * 45f
  val alpha = (1f - progress).coerceIn(0f, 1f)

  drawCircle(
    color = Color(0xFFFF3D57).copy(alpha = alpha),
    radius = radius,
    center = Offset(pos.x, pos.y - 18f),
    style = Stroke(width = 3f)
  )

  for (i in 0 until 8) {
    val angle = (i / 8f) * 2 * PI
    val px = pos.x + cos(angle).toFloat() * radius * 1.2f
    val py = (pos.y - 18f) + sin(angle).toFloat() * radius * 1.2f
    drawCircle(
      color = Color(skin.primaryColor).copy(alpha = alpha),
      radius = 3.5f * (1f - progress),
      center = Offset(px, py)
    )
  }
}

private fun DrawScope.drawRecordingOverlay(engine: UsnaPhysicsEngine, pulse: Float) {
  val durationFrac = engine.recordingInputs.size / 180f

  // Dynamic pulsing red border
  val borderAlpha = 0.28f + 0.15f * sin(pulse * 6 * PI).toFloat()
  drawRect(
    color = Color(0xFFFF1744).copy(alpha = borderAlpha),
    topLeft = Offset(0f, 0f),
    size = Size(1280f, 720f),
    style = Stroke(width = 6f)
  )

  val badgeCenter = Offset(engine.playerPosition.x, engine.playerPosition.y - engine.playerHeight - 24f)

  // REC beacon
  drawCircle(
    color = Color(0xFFFF1744),
    radius = 5.5f,
    center = Offset(badgeCenter.x - 20f, badgeCenter.y)
  )

  val barWidth = 50f
  val barHeight = 6f
  val barLeft = badgeCenter.x - barWidth / 2f + 6f
  val barTop = badgeCenter.y - barHeight / 2f

  drawRect(Color(0x88000000), Offset(barLeft, barTop), Size(barWidth, barHeight))
  drawRect(
    color = Color(0xFFFF1744),
    topLeft = Offset(barLeft, barTop),
    size = Size(barWidth * (1f - durationFrac), barHeight)
  )
}
