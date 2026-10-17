package com.example.trilho.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trilho.engine.TrilhoEngine
import com.example.trilho.model.TrilhoStrings
import kotlin.math.abs

@Composable
fun TrilhoTouchHud(
  engine: TrilhoEngine,
  language: String,
  onPauseClicked: () -> Unit,
  onSwipeLeft: () -> Unit,
  onSwipeRight: () -> Unit,
  onSwipeUp: () -> Unit,
  onSwipeDown: () -> Unit,
  onCrystalAbilityClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Swipe Gesture Detection with >= 50px threshold
  var dragAccumulatedX by remember { mutableFloatStateOf(0f) }
  var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
  var gestureConsumed by remember { mutableStateOf(false) }

  val swipeModifier = Modifier.pointerInput(Unit) {
    detectDragGestures(
      onDragStart = {
        dragAccumulatedX = 0f
        dragAccumulatedY = 0f
        gestureConsumed = false
      },
      onDragEnd = {
        dragAccumulatedX = 0f
        dragAccumulatedY = 0f
        gestureConsumed = false
      },
      onDragCancel = {
        dragAccumulatedX = 0f
        dragAccumulatedY = 0f
        gestureConsumed = false
      },
      onDrag = { change, dragAmount ->
        change.consume()
        if (!gestureConsumed) {
          dragAccumulatedX += dragAmount.x
          dragAccumulatedY += dragAmount.y

          val threshold = 50f
          val absX = abs(dragAccumulatedX)
          val absY = abs(dragAccumulatedY)

          if (absX > threshold || absY > threshold) {
            gestureConsumed = true
            if (absX > absY) {
              if (dragAccumulatedX > 0) onSwipeRight() else onSwipeLeft()
            } else {
              if (dragAccumulatedY > 0) onSwipeDown() else onSwipeUp()
            }
          }
        }
      }
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .then(swipeModifier)
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    // 1. TOP STATUS BAR (Distance, Coins, Pause Button)
    TopStatusBar(
      engine = engine,
      onPauseClicked = onPauseClicked,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    )

    // 2. ACTIVE POWER-UP INDICATOR BAR
    ActivePowerUpsBar(
      engine = engine,
      language = language,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 64.dp)
    )

    // 3. CRYSTAL ABILITY BUTTON (Bottom Right - Big Action Button)
    CrystalAbilityButton(
      crystalsStock = engine.crystalsStock,
      isInvulnerable = engine.crystalInvulnTicks > 0,
      language = language,
      onClick = onCrystalAbilityClicked,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 28.dp)
    )
  }
}

@Composable
private fun TopStatusBar(
  engine: TrilhoEngine,
  onPauseClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Distance Covered Badge
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xDD141C2B),
      border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x4438E1FF))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF38E1FF), modifier = Modifier.size(18.dp))
        Text(
          text = "${engine.distanceMeters} m",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 16.sp
        )
      }
    }

    // Coins Collected Badge
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xDD141C2B),
      border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x44FFD700))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
        Text(
          text = "${engine.runCoins}",
          color = Color(0xFFFFF9C4),
          fontWeight = FontWeight.Black,
          fontSize = 16.sp
        )
      }
    }

    // Pause Button
    IconButton(
      onClick = onPauseClicked,
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(Color(0xDD141C2B))
        .border(1.2.dp, Color(0x44FFFFFF), CircleShape)
        .testTag("btn_pause")
    ) {
      Icon(Icons.Default.Pause, contentDescription = "Pausa", tint = Color.White)
    }
  }
}

@Composable
private fun ActivePowerUpsBar(
  engine: TrilhoEngine,
  language: String,
  modifier: Modifier = Modifier
) {
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    if (engine.magnetTicksRemaining > 0) {
      val progress = engine.magnetTicksRemaining / (8f * 60f)
      PowerUpBadge("🧲 ${TrilhoStrings.get("powerup_magnet", language)}", Color(0xFF00E5FF), progress)
    }
    if (engine.hasShield) {
      PowerUpBadge("🛡 ${TrilhoStrings.get("powerup_shield", language)}", Color(0xFF00E676), 1.0f)
    }
    if (engine.boostTicksRemaining > 0) {
      val progress = engine.boostTicksRemaining / (4f * 60f)
      PowerUpBadge("⚡ ${TrilhoStrings.get("powerup_boost", language)}", Color(0xFFFF3D57), progress)
    }
  }
}

@Composable
private fun PowerUpBadge(label: String, color: Color, progress: Float) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xEE162234),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, color.copy(alpha = 0.7f))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier.size(14.dp),
        color = color,
        strokeWidth = 2.dp
      )
    }
  }
}

@Composable
private fun CrystalAbilityButton(
  crystalsStock: Int,
  isInvulnerable: Boolean,
  language: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasCrystals = crystalsStock > 0

  val infiniteTransition = rememberInfiniteTransition(label = "crystal_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = if (hasCrystals) 1.08f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(550, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "crystal_scale"
  )

  Surface(
    onClick = onClick,
    enabled = hasCrystals,
    shape = CircleShape,
    color = if (isInvulnerable) {
      Color(0xFF00E5FF)
    } else if (hasCrystals) {
      Color(0xEE1A2A40)
    } else {
      Color(0x55152030)
    },
    border = androidx.compose.foundation.BorderStroke(
      width = if (hasCrystals) 2.5.dp else 1.2.dp,
      color = if (hasCrystals) Color(0xFF38E1FF) else Color(0x33FFFFFF)
    ),
    modifier = modifier
      .scale(pulseScale)
      .size(92.dp)
      .testTag("btn_crystal_ability")
  ) {
    Column(
      modifier = Modifier.padding(6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.Diamond,
        contentDescription = TrilhoStrings.get("crystal_ability", language),
        tint = if (hasCrystals) Color(0xFF38E1FF) else Color(0x66FFFFFF),
        modifier = Modifier.size(34.dp)
      )

      // Stock Gem Dots (0 to 3)
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..3) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(
                if (i <= crystalsStock) Color(0xFF38E1FF) else Color(0x44FFFFFF)
              )
          )
        }
      }
    }
  }
}
