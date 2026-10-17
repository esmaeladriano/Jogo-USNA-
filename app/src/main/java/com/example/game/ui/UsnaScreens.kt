package com.example.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.UsnaLevels
import com.example.game.model.*

@Composable
fun MainMenuScreen(
  settings: GameSettings,
  progress: GameProgress,
  onPlayClicked: () -> Unit,
  onLevelSelectClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onSkinsClicked: () -> Unit,
  onHowToPlayClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val lang = settings.language
  val currentSkin = remember(settings.selectedSkinId) { SkinRepository.getSkin(settings.selectedSkinId) }

  // Title pulse effect
  val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")
  val titleGlow by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "glow"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF131B2B), Color(0xFF0D121D), Color(0xFF080C14))
        )
      )
      .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(16.dp)
    ) {
      // Top Stars Count Badge
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0x661C273B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFD600))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
          Text(
            text = "${UsnaStrings.get("total_stars", lang)}: ${progress.totalStars} / 30",
            color = Color(0xFFFFF9C4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Game Title
      Text(
        text = "USNA",
        fontSize = 60.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 8.sp,
        color = Color(0xFF38E1FF).copy(alpha = titleGlow),
        modifier = Modifier.scale(titleGlow)
      )

      Text(
        text = UsnaStrings.get("subtitle", lang),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color(currentSkin.primaryColor),
        letterSpacing = 2.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Menu Buttons Row
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        MenuButton(
          label = UsnaStrings.get("play", lang),
          icon = Icons.Default.PlayArrow,
          accentColor = Color(0xFF00E676),
          testTag = "btn_menu_play",
          onClick = onPlayClicked
        )

        MenuButton(
          label = UsnaStrings.get("levels", lang),
          icon = Icons.Default.GridView,
          accentColor = Color(0xFF38E1FF),
          testTag = "btn_menu_levels",
          onClick = onLevelSelectClicked
        )

        MenuButton(
          label = UsnaStrings.get("skins", lang),
          icon = Icons.Default.Palette,
          accentColor = Color(currentSkin.secondaryColor),
          testTag = "btn_menu_skins",
          onClick = onSkinsClicked
        )

        MenuButton(
          label = UsnaStrings.get("settings", lang),
          icon = Icons.Default.Settings,
          accentColor = Color(0xFFFFD600),
          testTag = "btn_menu_settings",
          onClick = onSettingsClicked
        )

        MenuButton(
          label = UsnaStrings.get("how_to_play", lang),
          icon = Icons.Default.HelpOutline,
          accentColor = Color(0xFFFF9100),
          testTag = "btn_menu_help",
          onClick = onHowToPlayClicked
        )
      }
    }
  }
}

@Composable
private fun MenuButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(18.dp),
    color = Color(0xDD1C2638),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f)),
    modifier = Modifier
      .width(128.dp)
      .height(64.dp)
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = accentColor,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
    }
  }
}

@Composable
fun SkinsDialog(
  settings: GameSettings,
  progress: GameProgress,
  onSkinSelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val lang = settings.language

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color(0xF5131C2C),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x6638E1FF)),
      modifier = Modifier.widthIn(max = 580.dp)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = UsnaStrings.get("select_skin", lang),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Skins List
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          for (skin in SkinRepository.skins) {
            val isUnlocked = progress.totalStars >= skin.requiredStars
            val isSelected = settings.selectedSkinId == skin.id

            Surface(
              onClick = { if (isUnlocked) onSkinSelected(skin.id) },
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) Color(0xFF23324A) else Color(0x66182334),
              border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(skin.glowColor) else Color(0x33FFFFFF)
              ),
              modifier = Modifier.weight(1f).height(120.dp).testTag("skin_card_${skin.id}")
            ) {
              Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
              ) {
                // Skin preview color dot / orb
                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(skin.primaryColor))
                    .border(2.dp, Color(skin.glowColor), CircleShape)
                )

                Text(
                  text = if (lang.startsWith("pt")) skin.namePt else skin.nameEn,
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  maxLines = 2
                )

                if (isSelected) {
                  Text(
                    text = UsnaStrings.get("equipped", lang),
                    color = Color(0xFF00E676),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                } else if (!isUnlocked) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(10.dp))
                    Text(
                      text = "${skin.requiredStars}★",
                      color = Color(0xFFFFD600),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                } else {
                  Text(
                    text = UsnaStrings.get("equip", lang),
                    color = Color(0xFF38E1FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun LevelSelectScreen(
  progress: GameProgress,
  language: String,
  onLevelSelected: (Int) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allLevels = remember { UsnaLevels.getAllLevels() }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF131A28))
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
      // Header with Stars Count
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        IconButton(
          onClick = onBackClicked,
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0x551E2838))
            .testTag("btn_level_select_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = UsnaStrings.get("back", language),
            tint = Color.White
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = UsnaStrings.get("select_level", language),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0x55FFD600)
          ) {
            Text(
              text = "${progress.totalStars} / 30 ★",
              color = Color(0xFFFFD600),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.size(42.dp))
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Level Grid (2 rows x 5 columns)
      LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(allLevels) { level ->
          val stat = progress.levelStats[level.id]
          val isUnlocked = stat?.unlocked == true
          val stars = stat?.stars ?: 0
          val bestTime = stat?.bestTimeSec ?: 0f

          LevelCard(
            level = level,
            isUnlocked = isUnlocked,
            stars = stars,
            bestTimeSec = bestTime,
            language = language,
            onClick = { if (isUnlocked) onLevelSelected(level.id) }
          )
        }
      }
    }
  }
}

@Composable
private fun LevelCard(
  level: LevelData,
  isUnlocked: Boolean,
  stars: Int,
  bestTimeSec: Float,
  language: String,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    enabled = isUnlocked,
    shape = RoundedCornerShape(16.dp),
    color = if (isUnlocked) Color(0xFF1E293B) else Color(0x44141C28),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.2.dp,
      color = if (isUnlocked) Color(0x5538E1FF) else Color(0x22FFFFFF)
    ),
    modifier = Modifier
      .height(104.dp)
      .testTag("level_card_${level.id}")
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${level.id}",
          color = if (isUnlocked) Color(0xFF38E1FF) else Color(0x55FFFFFF),
          fontWeight = FontWeight.Black,
          fontSize = 18.sp
        )
        if (bestTimeSec > 0.01f) {
          Text(
            text = String.format("%.1fs", bestTimeSec),
            color = Color(0xFFFFD600),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Text(
        text = if (language.startsWith("pt")) level.namePt.substringAfter(". ") else level.nameEn.substringAfter(". "),
        color = if (isUnlocked) Color.White else Color(0x66FFFFFF),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )

      if (isUnlocked) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          for (i in 1..3) {
            Icon(
              imageVector = if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = null,
              tint = if (i <= stars) Color(0xFFFFD600) else Color(0x44FFFFFF),
              modifier = Modifier.size(14.dp)
            )
          }
        }
      } else {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Locked",
          tint = Color(0x66FFFFFF),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun SettingsScreen(
  settings: GameSettings,
  onSettingsChanged: (GameSettings) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val lang = settings.language

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF131A28))
      .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 560.dp)
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClicked,
          modifier = Modifier.testTag("btn_settings_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = UsnaStrings.get("back", lang),
            tint = Color.White
          )
        }

        Text(
          text = UsnaStrings.get("settings", lang),
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.size(48.dp))
      }

      Spacer(modifier = Modifier.height(14.dp))

      // SFX Volume Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = UsnaStrings.get("sfx_volume", lang), color = Color.White, fontSize = 13.sp)
        Slider(
          value = settings.soundVolume,
          onValueChange = { onSettingsChanged(settings.copy(soundVolume = it)) },
          valueRange = 0f..1f,
          modifier = Modifier.width(220.dp).testTag("slider_sfx")
        )
      }

      // Music Volume Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = UsnaStrings.get("music_volume", lang), color = Color.White, fontSize = 13.sp)
        Slider(
          value = settings.musicVolume,
          onValueChange = { onSettingsChanged(settings.copy(musicVolume = it)) },
          valueRange = 0f..1f,
          modifier = Modifier.width(220.dp).testTag("slider_music")
        )
      }

      // Haptic Vibration Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = UsnaStrings.get("vibration", lang), color = Color.White, fontSize = 13.sp)
        Switch(
          checked = settings.vibrationEnabled,
          onCheckedChange = { onSettingsChanged(settings.copy(vibrationEnabled = it)) },
          modifier = Modifier.testTag("switch_vibration")
        )
      }

      // Language Switcher (PT / EN)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = UsnaStrings.get("language", lang), color = Color.White, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = { onSettingsChanged(settings.copy(language = "pt")) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (settings.language == "pt") Color(0xFF38E1FF) else Color(0x33253245)
            ),
            modifier = Modifier.testTag("btn_lang_pt")
          ) {
            Text("Português", color = if (settings.language == "pt") Color(0xFF0F1A28) else Color.White)
          }

          Button(
            onClick = { onSettingsChanged(settings.copy(language = "en")) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (settings.language == "en") Color(0xFF38E1FF) else Color(0x33253245)
            ),
            modifier = Modifier.testTag("btn_lang_en")
          ) {
            Text("English", color = if (settings.language == "en") Color(0xFF0F1A28) else Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun PauseDialog(
  language: String,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onSelectLevel: () -> Unit,
  onMainMenu: () -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xF2162030),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x6638E1FF)),
      modifier = Modifier.widthIn(max = 360.dp)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = UsnaStrings.get("paused", language),
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )

        Button(
          onClick = onResume,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_resume"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
        ) {
          Text(UsnaStrings.get("resume", language), color = Color(0xFF0C2411), fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onRestart,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_restart")
        ) {
          Text(UsnaStrings.get("restart", language), color = Color.White)
        }

        OutlinedButton(
          onClick = onSelectLevel,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_select_level")
        ) {
          Text(UsnaStrings.get("select_level", language), color = Color.White)
        }

        OutlinedButton(
          onClick = onMainMenu,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_menu")
        ) {
          Text(UsnaStrings.get("main_menu", language), color = Color.White)
        }
      }
    }
  }
}

@Composable
fun LevelCompleteDialog(
  level: LevelData,
  timeSec: Float,
  stars: Int,
  echoesUsed: Int,
  language: String,
  onNextLevel: () -> Unit,
  onReplay: () -> Unit,
  onSelectLevel: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xF5162030),
      border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E676)),
      modifier = Modifier.widthIn(max = 400.dp)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = UsnaStrings.get("level_completed", language),
          color = Color(0xFF00E676),
          fontSize = 22.sp,
          fontWeight = FontWeight.Black
        )

        // 3 Animated Stars
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..3) {
            Icon(
              imageVector = if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = null,
              tint = if (i <= stars) Color(0xFFFFD600) else Color(0x33FFFFFF),
              modifier = Modifier.size(36.dp)
            )
          }
        }

        // Stats summary
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0x660F1622),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "${UsnaStrings.get("time", language)}: ${String.format("%.1fs", timeSec)}",
              color = Color.White,
              fontSize = 14.sp
            )
            Text(
              text = "${UsnaStrings.get("echoes", language)}: $echoesUsed / ${level.maxEchoes}",
              color = Color(0xFF38E1FF),
              fontSize = 14.sp
            )
          }
        }

        if (level.id < 10) {
          Button(
            onClick = onNextLevel,
            modifier = Modifier.fillMaxWidth().testTag("btn_victory_next"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
          ) {
            Text(UsnaStrings.get("next_level", language), color = Color(0xFF0C2411), fontWeight = FontWeight.Bold)
          }
        } else {
          Text(
            text = UsnaStrings.get("congratulations", language),
            color = Color(0xFFFFD600),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onReplay,
            modifier = Modifier.weight(1f).testTag("btn_victory_replay")
          ) {
            Text(UsnaStrings.get("restart", language), color = Color.White)
          }

          OutlinedButton(
            onClick = onSelectLevel,
            modifier = Modifier.weight(1f).testTag("btn_victory_levels")
          ) {
            Text(UsnaStrings.get("levels", language), color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun HowToPlayDialog(
  language: String,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xF2162030),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6638E1FF)),
      modifier = Modifier.widthIn(max = 480.dp)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = UsnaStrings.get("how_to_play", language),
          color = Color(0xFF38E1FF),
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          TutorialPoint(Icons.Default.Adjust, UsnaStrings.get("tutorial_goal", language))
          TutorialPoint(Icons.Default.FiberManualRecord, UsnaStrings.get("tutorial_record", language))
          TutorialPoint(Icons.Default.CloudSync, UsnaStrings.get("tutorial_release", language))
          TutorialPoint(Icons.Default.ToggleOn, UsnaStrings.get("tutorial_mechanics", language))
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().testTag("btn_how_to_play_close"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38E1FF))
        ) {
          Text(UsnaStrings.get("back", language), color = Color(0xFF0F1A28), fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun TutorialPoint(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFFF9E40), modifier = Modifier.size(20.dp))
    Text(text = text, color = Color.White, fontSize = 13.sp)
  }
}
