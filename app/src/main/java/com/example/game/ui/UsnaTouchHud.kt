package com.example.game.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.UsnaPhysicsEngine
import com.example.game.model.GameInput
import com.example.game.model.LevelData
import com.example.game.model.UsnaStrings

@Composable
fun UsnaTouchHud(
  level: LevelData,
  engine: UsnaPhysicsEngine,
  language: String,
  onPauseClicked: () -> Unit,
  onRestartClicked: () -> Unit,
  onInputChange: (GameInput) -> Unit,
  onRecordToggle: () -> Unit,
  onReleaseEcho: () -> Unit,
  onUndoEcho: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isLeftPressed by remember { mutableStateOf(false) }
  var isRightPressed by remember { mutableStateOf(false) }
  var isJumpPressed by remember { mutableStateOf(false) }

  // Propagate updated input immediately to ViewModel/Engine
  LaunchedEffect(isLeftPressed, isRightPressed, isJumpPressed) {
    onInputChange(
      GameInput(
        moveLeft = isLeftPressed,
        moveRight = isRightPressed,
        jump = isJumpPressed
      )
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    // 1. TOP STATUS BAR (Level name, Stopwatch, Echoes counter, Restart, Pause)
    TopControlBar(
      level = level,
      engine = engine,
      language = language,
      onPauseClicked = onPauseClicked,
      onRestartClicked = onRestartClicked,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)
    )

    // 2. BOTTOM CONTROLS (Thumbs layout: Movement on Left, Actions on Right)
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      // D-Pad / Horizontal buttons (Left & Right)
      MovementControls(
        onLeftChange = { isLeftPressed = it },
        onRightChange = { isRightPressed = it }
      )

      // Action Buttons (Undo, Record, Release, Jump)
      ActionControls(
        engine = engine,
        language = language,
        onJumpChange = { isJumpPressed = it },
        onRecordToggle = onRecordToggle,
        onReleaseEcho = onReleaseEcho,
        onUndoEcho = onUndoEcho
      )
    }
  }
}

@Composable
private fun TopControlBar(
  level: LevelData,
  engine: UsnaPhysicsEngine,
  language: String,
  onPauseClicked: () -> Unit,
  onRestartClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Level Badge
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xDD182233),
      border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x4438E1FF))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(Color(0xFF38E1FF))
        )
        Text(
          text = if (language.startsWith("pt")) level.namePt else level.nameEn,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }

    // Center Group: Live Stopwatch + Echoes Counter
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      // Stopwatch
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xDD182233),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD600))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(15.dp))
          Text(
            text = String.format("%.1fs", engine.levelTimeSec),
            color = Color(0xFFFFF176),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }

      // Echoes Tracker Badge
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xDD182233),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438E1FF))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AllInclusive,
            contentDescription = null,
            tint = Color(0xFF38E1FF),
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "${UsnaStrings.get("echoes", language)}: ${engine.activeEchoes.size} / ${level.maxEchoes}",
            color = Color(0xFFD4E6FA),
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
          )
        }
      }
    }

    // Right: Restart and Pause Buttons
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      IconButton(
        onClick = onRestartClicked,
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(Color(0xDD182233))
          .border(1.2.dp, Color(0x44FFFFFF), CircleShape)
          .testTag("btn_restart")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = UsnaStrings.get("restart", language),
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      IconButton(
        onClick = onPauseClicked,
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(Color(0xDD182233))
          .border(1.2.dp, Color(0x44FFFFFF), CircleShape)
          .testTag("btn_pause")
      ) {
        Icon(
          imageVector = Icons.Default.Pause,
          contentDescription = UsnaStrings.get("paused", language),
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun MovementControls(
  onLeftChange: (Boolean) -> Unit,
  onRightChange: (Boolean) -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    VirtualTouchButton(
      icon = Icons.AutoMirrored.Filled.ArrowBack,
      label = "←",
      activeColor = Color(0xFF38E1FF),
      testTag = "btn_move_left",
      onPressState = onLeftChange
    )

    VirtualTouchButton(
      icon = Icons.AutoMirrored.Filled.ArrowForward,
      label = "→",
      activeColor = Color(0xFF38E1FF),
      testTag = "btn_move_right",
      onPressState = onRightChange
    )
  }
}

@Composable
private fun ActionControls(
  engine: UsnaPhysicsEngine,
  language: String,
  onJumpChange: (Boolean) -> Unit,
  onRecordToggle: () -> Unit,
  onReleaseEcho: () -> Unit,
  onUndoEcho: () -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Undo Echo Button (Quality of Life)
    if (engine.canUndoLastEcho()) {
      VirtualTapActionButton(
        label = UsnaStrings.get("undo", language),
        icon = Icons.AutoMirrored.Filled.Undo,
        baseColor = Color(0xFFFFB74D),
        enabled = true,
        testTag = "btn_undo_echo",
        onClick = onUndoEcho
      )
    }

    // Record Button (Gravar / Parar)
    val isRecording = engine.isRecording
    val canRecord = engine.canStartRecording() || isRecording

    VirtualTapActionButton(
      label = if (isRecording) UsnaStrings.get("stop_record", language) else UsnaStrings.get("record", language),
      icon = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
      baseColor = if (isRecording) Color(0xFFFF1744) else Color(0xFFE53935),
      pulseGlow = isRecording,
      enabled = canRecord,
      testTag = "btn_record",
      onClick = onRecordToggle
    )

    // Release Echo Button (Libertar)
    val canRelease = engine.canReleaseEcho()
    VirtualTapActionButton(
      label = UsnaStrings.get("release", language),
      icon = Icons.Default.CloudSync,
      baseColor = Color(0xFF00B0FF),
      enabled = canRelease,
      testTag = "btn_release",
      onClick = onReleaseEcho
    )

    // Jump Button (Saltar)
    VirtualTouchButton(
      icon = Icons.Default.KeyboardArrowUp,
      label = UsnaStrings.get("jump", language),
      activeColor = Color(0xFFFF9100),
      sizeDp = 86,
      testTag = "btn_jump",
      onPressState = onJumpChange
    )
  }
}

@Composable
private fun VirtualTouchButton(
  icon: ImageVector,
  label: String,
  activeColor: Color,
  sizeDp: Int = 80,
  testTag: String,
  onPressState: (Boolean) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .size(sizeDp.dp)
      .clip(CircleShape)
      .background(
        Brush.radialGradient(
          colors = if (isPressed) {
            listOf(activeColor.copy(alpha = 0.65f), activeColor.copy(alpha = 0.25f))
          } else {
            listOf(Color(0xCC1A263B), Color(0xEE121A28))
          }
        )
      )
      .border(
        width = if (isPressed) 2.5.dp else 1.5.dp,
        color = if (isPressed) activeColor else Color(0x55FFFFFF),
        shape = CircleShape
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isPressed = true
            onPressState(true)
            tryAwaitRelease()
            isPressed = false
            onPressState(false)
          }
        )
      }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isPressed) Color.White else activeColor,
      modifier = Modifier.size((sizeDp * 0.44).dp)
    )
  }
}

@Composable
private fun VirtualTapActionButton(
  label: String,
  icon: ImageVector,
  baseColor: Color,
  enabled: Boolean = true,
  pulseGlow: Boolean = false,
  testTag: String,
  onClick: () -> Unit
) {
  val scale by animateFloatAsState(
    targetValue = if (pulseGlow) 1.08f else 1.0f,
    animationSpec = if (pulseGlow) {
      infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    } else {
      spring()
    },
    label = "btn_scale"
  )

  Surface(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(20.dp),
    color = if (enabled) {
      if (pulseGlow) baseColor.copy(alpha = 0.88f) else Color(0xDD182233)
    } else {
      Color(0x33182233)
    },
    border = androidx.compose.foundation.BorderStroke(
      width = if (pulseGlow) 2.dp else 1.2.dp,
      color = if (enabled) baseColor.copy(alpha = if (pulseGlow) 1f else 0.7f) else Color(0x22FFFFFF)
    ),
    modifier = Modifier
      .scale(scale)
      .height(68.dp)
      .widthIn(min = 88.dp)
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (enabled) (if (pulseGlow) Color.White else baseColor) else Color(0x55FFFFFF),
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        color = if (enabled) Color.White else Color(0x55FFFFFF),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
