package com.example.trilho.ui

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
import com.example.trilho.engine.TrilhoEngine
import com.example.trilho.model.*
import kotlin.math.*

@Composable
fun TrilhoGameCanvas(
  engine: TrilhoEngine,
  skin: RunnerSkin = TrilhoSkinRepository.skins.first(),
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "runner_visuals")
  val pulseAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
    label = "pulse"
  )

  val runStepCycle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(260, easing = LinearEasing), RepeatMode.Restart),
    label = "run_cycle"
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val targetWidth = 720f
    val targetHeight = 1280f

    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      // Maintain 720x1280 aspect ratio with centered fit
      val scale = min(canvasWidth / targetWidth, canvasHeight / targetHeight)
      val offsetX = (canvasWidth - targetWidth * scale) / 2f
      val offsetY = (canvasHeight - targetHeight * scale) / 2f

      drawRect(Color(0xFF0D121D), Offset.Zero, size)

      drawContext.canvas.save()
      drawContext.canvas.translate(offsetX, offsetY)
      drawContext.canvas.scale(scale, scale)

      // Take thread-safe snapshots of mutable entity pools
      val (safeObstacles, safePickups, safeParticles) = synchronized(engine.stateLock) {
        Triple(
          engine.activeObstacles.toList(),
          engine.activePickups.toList(),
          engine.particles.toList()
        )
      }

      // 1. Draw 3-Lane Railway Perspective Background
      drawRailwayTrack(engine.trackSleeperOffset, pulseAnim, engine.boostTicksRemaining > 0)

      // 2. Draw Active Obstacles
      for (obs in safeObstacles) {
        if (obs.active && obs.y in -100f..1350f) {
          drawObstacle(obs, pulseAnim)
        }
      }

      // 3. Draw Pickups (Coins, Crystals, Power-ups)
      for (item in safePickups) {
        if (item.active && item.y in -50f..1350f) {
          drawPickup(item, pulseAnim)
        }
      }

      // 4. Draw Runner Character
      if (!engine.isGameOver) {
        drawRunner(engine, skin, runStepCycle, pulseAnim)
      }

      // 5. Dynamic Particle System
      drawParticles(safeParticles)

      // 6. Active Invulnerability / Boost Screen Vignette
      if (engine.crystalInvulnTicks > 0 || engine.boostTicksRemaining > 0) {
        drawInvulnVignette(engine, pulseAnim)
      }

      drawContext.canvas.restore()
    }
  }
}

private fun DrawScope.drawRailwayTrack(sleeperOffset: Float, pulse: Float, isBoost: Boolean) {
  // Deep dark railway void background
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF0F1726), Color(0xFF151E30), Color(0xFF0B101A)),
      startY = 0f,
      endY = 1280f
    ),
    topLeft = Offset(0f, 0f),
    size = Size(720f, 1280f)
  )

  // Track ballast bed (central gravel platform)
  drawRoundRect(
    color = Color(0xFF1B2436),
    topLeft = Offset(90f, 0f),
    size = Size(540f, 1280f),
    cornerRadius = CornerRadius(12f, 12f)
  )

  // Scrolling Railway Sleepers (Travessas da linha)
  var sleeperY = (sleeperOffset - 90f)
  while (sleeperY <= 1350f) {
    if (sleeperY >= -40f) {
      // Wooden/Steel sleeper bar across the 3 lanes
      drawRoundRect(
        color = Color(0xFF26334A),
        topLeft = Offset(110f, sleeperY),
        size = Size(500f, 24f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      // Sleeper top highlight
      drawRect(
        color = Color(0xFF384966),
        topLeft = Offset(110f, sleeperY),
        size = Size(500f, 4f)
      )
    }
    sleeperY += 90f
  }

  // Steel Rails for the 3 lanes (Left, Center, Right)
  // Left Lane: center 190 (rails at 140, 240)
  // Center Lane: center 360 (rails at 310, 410)
  // Right Lane: center 530 (rails at 480, 580)
  val railXCoords = listOf(140f, 240f, 310f, 410f, 480f, 580f)
  val railGlow = if (isBoost) Color(0xFFFF3D57) else Color(0xFF38E1FF)

  for (rx in railXCoords) {
    // Outer rail glow
    drawLine(
      color = railGlow.copy(alpha = 0.25f),
      start = Offset(rx, 0f),
      end = Offset(rx, 1280f),
      strokeWidth = 6f
    )
    // Shiny steel rail
    drawLine(
      color = Color(0xFF90A4AE),
      start = Offset(rx, 0f),
      end = Offset(rx, 1280f),
      strokeWidth = 3f
    )
    // Center bright reflection
    drawLine(
      color = Color.White.copy(alpha = 0.8f),
      start = Offset(rx, 0f),
      end = Offset(rx, 1280f),
      strokeWidth = 1f
    )
  }

  // Track Boundaries / Neon Danger Curb
  drawLine(Color(0x5538E1FF), Offset(90f, 0f), Offset(90f, 1280f), strokeWidth = 3f)
  drawLine(Color(0x5538E1FF), Offset(630f, 0f), Offset(630f, 1280f), strokeWidth = 3f)

  // Speed Motion Lines
  for (i in 0 until 6) {
    val lineX = 60f + i * 105f
    val lineY = ((pulse * 1280f) + i * 210f) % 1280f
    drawLine(
      color = Color(0x3338E1FF),
      start = Offset(lineX, lineY),
      end = Offset(lineX, lineY + 60f),
      strokeWidth = 2f
    )
  }
}

private fun DrawScope.drawObstacle(obs: TrackObstacle, pulse: Float) {
  val cx = obs.lane.targetX
  val y = obs.y

  when (obs.type) {
    // 1. Low Barrier (Must Jump Over): Neon orange hurdle
    ObstacleType.BARRIER_LOW -> {
      val w = 130f
      val h = 48f
      val left = cx - w / 2f
      val top = y - h

      // Support legs
      drawRect(Color(0xFF263238), Offset(left + 8f, top + 15f), Size(12f, h - 15f))
      drawRect(Color(0xFF263238), Offset(left + w - 20f, top + 15f), Size(12f, h - 15f))

      // Hurdle bar with hazard stripes
      drawRoundRect(
        color = Color(0xFFFF9100),
        topLeft = Offset(left, top),
        size = Size(w, 22f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      // Alternating black hazard stripes
      for (i in 0 until 4) {
        drawRect(Color(0xFF212121), Offset(left + 15f + i * 28f, top), Size(14f, 22f))
      }
      // Glowing neon edge
      drawRoundRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(left, top),
        size = Size(w, 4f),
        cornerRadius = CornerRadius(2f, 2f)
      )
    }

    // 2. High Barrier (Must Slide Under): Overhead arch with hanging laser teeth
    ObstacleType.BARRIER_HIGH -> {
      val w = 136f
      val h = 64f
      val left = cx - w / 2f
      val top = y - h

      // Tall overhead support posts
      drawRect(Color(0xFF1E2733), Offset(left, top - 30f), Size(12f, h + 30f))
      drawRect(Color(0xFF1E2733), Offset(left + w - 12f, top - 30f), Size(12f, h + 30f))

      // Overhead beam
      drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(left, top - 30f),
        size = Size(w, 28f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      // Hanging laser barrier
      val beamAlpha = 0.75f + 0.25f * sin(pulse * 8 * PI).toFloat()
      drawRect(
        color = Color(0xFFFF3D57).copy(alpha = beamAlpha),
        topLeft = Offset(left + 12f, top - 2f),
        size = Size(w - 24f, 22f)
      )
      // Downward warning arrow indicator
      drawCircle(Color(0xFFFF3D57), radius = 5f, center = Offset(cx, top - 16f))
    }

    // 3. Train (Full block: Must Change Lane!): High-speed futuristic locomotive
    ObstacleType.TRAIN -> {
      val w = 136f
      val h = 180f
      val left = cx - w / 2f
      val top = y - h

      // Train Chassis (Deep Metallic Red & Dark Slate)
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFD50000), Color(0xFF8E0000)),
          startY = top,
          endY = y
        ),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(14f, 14f)
      )

      // Train Front Windshield / Cockpit
      drawRoundRect(
        color = Color(0xFF0D1B2A),
        topLeft = Offset(left + 14f, top + 20f),
        size = Size(w - 28f, 38f),
        cornerRadius = CornerRadius(6f, 6f)
      )

      // Bright Twin Headlights
      drawCircle(Color.White, radius = 9f, center = Offset(left + 28f, top + 80f))
      drawCircle(Color(0xFF38E1FF), radius = 13f, center = Offset(left + 28f, top + 80f), style = Stroke(2f))

      drawCircle(Color.White, radius = 9f, center = Offset(left + w - 28f, top + 80f))
      drawCircle(Color(0xFF38E1FF), radius = 13f, center = Offset(left + w - 28f, top + 80f), style = Stroke(2f))

      // Locomotive Cowcatcher / Grille at bottom
      drawRoundRect(
        color = Color(0xFF1E2838),
        topLeft = Offset(left + 8f, y - 24f),
        size = Size(w - 16f, 20f),
        cornerRadius = CornerRadius(4f, 4f)
      )
    }

    // 4. Double Barrier (Blocks 2 Lanes, Leaves 1 Open): Massive Security Gate
    ObstacleType.DOUBLE_BARRIER -> {
      val w = 300f
      val h = 60f
      val left = cx - 70f
      val top = y - h

      // Heavy barrier beam across 2 lanes
      drawRoundRect(
        color = Color(0xFFC62828),
        topLeft = Offset(left, top),
        size = Size(w, 32f),
        cornerRadius = CornerRadius(6f, 6f)
      )
      // Yellow warning chevron lines
      for (i in 0 until 8) {
        drawRect(Color(0xFFFFD600), Offset(left + 15f + i * 36f, top), Size(18f, 32f))
      }
      // Glowing warning lamps
      val lampAlpha = 0.6f + 0.4f * sin(pulse * 6 * PI).toFloat()
      drawCircle(Color(0xFFFF1744).copy(alpha = lampAlpha), radius = 7f, center = Offset(left + 14f, top + 16f))
      drawCircle(Color(0xFFFF1744).copy(alpha = lampAlpha), radius = 7f, center = Offset(left + w - 14f, top + 16f))
    }
  }
}

private fun DrawScope.drawPickup(item: TrackPickup, pulse: Float) {
  val x = item.x
  val y = item.y

  when (item.type) {
    // Gold Coin: Spinning coin with star reflection
    PickupType.COIN -> {
      val spinScale = abs(cos(pulse * 4 * PI)).toFloat().coerceAtLeast(0.2f)
      val radius = 16f

      // Outer gold aura
      drawCircle(Color(0xFFFFD700).copy(alpha = 0.35f), radius = radius + 4f, center = Offset(x, y))

      // 3D Coin face
      drawOval(
        brush = Brush.horizontalGradient(
          colors = listOf(Color(0xFFFFD700), Color(0xFFFFEA00), Color(0xFFFFB300))
        ),
        topLeft = Offset(x - radius * spinScale, y - radius),
        size = Size(radius * 2 * spinScale, radius * 2)
      )
      // Inner star / coin rim
      drawOval(
        color = Color(0xFFFFF9C4),
        topLeft = Offset(x - (radius - 5f) * spinScale, y - (radius - 5f)),
        size = Size((radius - 5f) * 2 * spinScale, (radius - 5f) * 2),
        style = Stroke(width = 1.5f)
      )
    }

    // Cyan Crystal: Rotating Diamond
    PickupType.CRYSTAL -> {
      val size = 20f
      val crystalPath = Path().apply {
        moveTo(x, y - size)
        lineTo(x + size * 0.8f, y)
        lineTo(x, y + size)
        lineTo(x - size * 0.8f, y)
        close()
      }

      // Outer crystal glow
      drawCircle(Color(0xFF38E1FF).copy(alpha = 0.4f), radius = size + 6f, center = Offset(x, y))

      // Faceted gem body
      drawPath(
        path = crystalPath,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF0091EA)),
          startY = y - size,
          endY = y + size
        )
      )
      // Prismatic center cut
      drawPath(crystalPath, color = Color.White, style = Stroke(width = 1.5f))
    }

    // Power-ups: Glowing Orbs with Symbols
    PickupType.POWERUP_MAGNET -> {
      drawPowerUpOrb(x, y, Color(0xFF00E5FF), "🧲", pulse)
    }
    PickupType.POWERUP_SHIELD -> {
      drawPowerUpOrb(x, y, Color(0xFF00E676), "🛡", pulse)
    }
    PickupType.POWERUP_BOOST -> {
      drawPowerUpOrb(x, y, Color(0xFFFF3D57), "⚡", pulse)
    }
  }
}

private fun DrawScope.drawPowerUpOrb(x: Float, y: Float, color: Color, symbol: String, pulse: Float) {
  val radius = 22f
  val auraRadius = radius + 6f + 3f * sin(pulse * 6 * PI).toFloat()

  drawCircle(color.copy(alpha = 0.35f), radius = auraRadius, center = Offset(x, y))
  drawCircle(
    brush = Brush.radialGradient(colors = listOf(Color.White, color, Color(0xFF101B2B)), center = Offset(x, y), radius = radius),
    radius = radius,
    center = Offset(x, y)
  )
  drawCircle(color = Color.White, radius = radius, center = Offset(x, y), style = Stroke(width = 2f))
}

private fun DrawScope.drawRunner(
  engine: TrilhoEngine,
  skin: RunnerSkin,
  stepCycle: Float,
  pulse: Float
) {
  val px = engine.playerX
  val py = TrilhoEngine.PLAYER_BASE_Y
  val jumpZ = engine.jumpHeightOffset
  val isSliding = engine.isSliding
  val isJumping = engine.isJumping

  val currentY = py - jumpZ

  // 1. Ground Shadow (stays on the railway tracks)
  val shadowScale = (1f - (jumpZ / 200f)).coerceIn(0.4f, 1f)
  drawOval(
    color = Color(0x66000000),
    topLeft = Offset(px - 26f * shadowScale, py - 4f),
    size = Size(52f * shadowScale, 14f * shadowScale)
  )

  // 2. Active Power-Up Barrier Auras
  if (engine.crystalInvulnTicks > 0) {
    // Crystal Invulnerability: Radiant Hexagonal Shield
    val shieldRadius = 52f
    drawCircle(
      color = Color(0xFF38E1FF).copy(alpha = 0.35f + 0.15f * sin(pulse * 8 * PI).toFloat()),
      radius = shieldRadius + 4f,
      center = Offset(px, currentY - 25f)
    )
    drawCircle(
      color = Color.White,
      radius = shieldRadius,
      center = Offset(px, currentY - 25f),
      style = Stroke(width = 2.5f)
    )
  } else if (engine.hasShield) {
    // Green Shield Sphere
    drawCircle(
      color = Color(0xFF00E676).copy(alpha = 0.35f),
      radius = 48f,
      center = Offset(px, currentY - 25f)
    )
    drawCircle(
      color = Color(0xFF69F0AE),
      radius = 48f,
      center = Offset(px, currentY - 25f),
      style = Stroke(width = 2.5f)
    )
  }

  // Boost Super Flame Trail
  if (engine.boostTicksRemaining > 0) {
    drawCircle(
      color = Color(0xFFFF3D57).copy(alpha = 0.45f),
      radius = 54f,
      center = Offset(px, currentY - 20f)
    )
  }

  val primaryColor = Color(skin.primaryColor)
  val secondaryColor = Color(skin.secondaryColor)
  val glowColor = Color(skin.glowColor)

  if (isSliding) {
    // --- SLIDING POSE: Low compressed aerodynamic posture ---
    val slideW = 44f
    val slideH = 26f
    val slideTop = currentY - slideH

    // Friction spark particles under slide
    drawOval(
      color = Color(0xFFFFD54F),
      topLeft = Offset(px - slideW / 2f - 4f, py - 6f),
      size = Size(slideW + 8f, 10f)
    )

    // Body
    drawRoundRect(
      brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
      topLeft = Offset(px - slideW / 2f, slideTop),
      size = Size(slideW, slideH),
      cornerRadius = CornerRadius(10f, 10f)
    )
    // Helmet
    drawCircle(Color.White, radius = 9f, center = Offset(px + 12f, slideTop + 8f))
    drawCircle(glowColor, radius = 4f, center = Offset(px + 15f, slideTop + 8f))
  } else {
    // --- RUNNING / JUMPING POSE ---
    val bodyW = 34f
    val bodyH = 58f
    val bodyTop = currentY - bodyH

    // Leg step animation
    val legOffset = sin(stepCycle * 2 * PI).toFloat() * 8f

    // Legs
    drawRoundRect(Color(0xFF1E2838), Offset(px - 14f, currentY - 18f + legOffset), Size(10f, 20f), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xFF1E2838), Offset(px + 4f, currentY - 18f - legOffset), Size(10f, 20f), CornerRadius(4f, 4f))

    // Torso (Vibrant Runner Suit)
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(secondaryColor, primaryColor),
        startY = bodyTop,
        endY = bodyTop + bodyH
      ),
      topLeft = Offset(px - bodyW / 2f, bodyTop + 14f),
      size = Size(bodyW, bodyH - 18f),
      cornerRadius = CornerRadius(8f, 8f)
    )

    // Belt / Energy Battery Core
    drawCircle(glowColor, radius = 4.5f, center = Offset(px, bodyTop + 34f))

    // Head / Aerodynamic Helmet
    val headRadius = 14f
    drawCircle(
      brush = Brush.radialGradient(listOf(Color.White, primaryColor), center = Offset(px, bodyTop + 8f), radius = headRadius),
      radius = headRadius,
      center = Offset(px, bodyTop + 8f)
    )

    // Visor with neon cyber glow
    drawRoundRect(
      color = glowColor,
      topLeft = Offset(px - 9f, bodyTop + 6f),
      size = Size(18f, 7f),
      cornerRadius = CornerRadius(3f, 3f)
    )
  }
}

private fun DrawScope.drawParticles(particles: List<RunnerParticle>) {
  for (i in 0 until particles.size) {
    val p = particles.getOrNull(i) ?: continue
    val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
    drawCircle(
      color = Color(p.color).copy(alpha = alpha),
      radius = p.size * alpha,
      center = Offset(p.x, p.y)
    )
  }
}

private fun DrawScope.drawInvulnVignette(engine: TrilhoEngine, pulse: Float) {
  val borderAlpha = 0.28f + 0.14f * sin(pulse * 8 * PI).toFloat()
  val borderColor = if (engine.boostTicksRemaining > 0) Color(0xFFFF3D57) else Color(0xFF38E1FF)

  drawRect(
    color = borderColor.copy(alpha = borderAlpha),
    topLeft = Offset(0f, 0f),
    size = Size(720f, 1280f),
    style = Stroke(width = 8f)
  )
}
