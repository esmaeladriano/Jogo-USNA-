package com.example.trilho.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import com.example.trilho.model.*

@Composable
fun TrilhoMainMenuScreen(
  progress: TrilhoProgress,
  settings: TrilhoSettings,
  onPlayClicked: () -> Unit,
  onShopClicked: () -> Unit,
  onRecordsClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onHowToPlayClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val lang = settings.language

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF0F1726), Color(0xFF141F33), Color(0xFF0A0E17))
        )
      )
      .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .widthIn(max = 500.dp)
        .padding(24.dp)
    ) {
      // Top Stats Row (Coins and Best Distance)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0x661A263B),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5538E1FF))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
            Text(
              text = "${progress.bestDistanceMeters} m",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0x661A263B),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFD700))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
            Text(
              text = "${progress.totalCoins}",
              color = Color(0xFFFFF9C4),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Game Title
      Text(
        text = "TRILHO",
        fontSize = 62.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 6.sp,
        color = Color(0xFF38E1FF)
      )

      Text(
        text = TrilhoStrings.get("subtitle", lang),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFFF9E40),
        letterSpacing = 2.sp
      )

      Spacer(modifier = Modifier.height(44.dp))

      // Primary Play Button
      Button(
        onClick = onPlayClicked,
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .testTag("btn_menu_play"),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF092410), modifier = Modifier.size(28.dp))
          Text(
            text = TrilhoStrings.get("play", lang),
            color = Color(0xFF092410),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Secondary Buttons Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        SecondaryMenuButton(
          label = TrilhoStrings.get("shop", lang),
          icon = Icons.Default.ShoppingCart,
          color = Color(0xFFFFD700),
          modifier = Modifier.weight(1f),
          testTag = "btn_menu_shop",
          onClick = onShopClicked
        )

        SecondaryMenuButton(
          label = TrilhoStrings.get("records", lang),
          icon = Icons.Default.Leaderboard,
          color = Color(0xFF38E1FF),
          modifier = Modifier.weight(1f),
          testTag = "btn_menu_records",
          onClick = onRecordsClicked
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        SecondaryMenuButton(
          label = TrilhoStrings.get("settings", lang),
          icon = Icons.Default.Settings,
          color = Color(0xFFFF4081),
          modifier = Modifier.weight(1f),
          testTag = "btn_menu_settings",
          onClick = onSettingsClicked
        )

        SecondaryMenuButton(
          label = TrilhoStrings.get("how_to_play", lang),
          icon = Icons.Default.HelpOutline,
          color = Color(0xFFFF9100),
          modifier = Modifier.weight(1f),
          testTag = "btn_menu_help",
          onClick = onHowToPlayClicked
        )
      }
    }
  }
}

@Composable
private fun SecondaryMenuButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = Color(0xDD192538),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, color.copy(alpha = 0.6f)),
    modifier = modifier.height(54.dp).testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
  }
}

@Composable
fun TrilhoShopScreen(
  progress: TrilhoProgress,
  settings: TrilhoSettings,
  onEquipSkin: (String) -> Unit,
  onBuySkin: (String, Int) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val lang = settings.language

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF101726))
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClicked,
          modifier = Modifier.testTag("btn_shop_back")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }

        Text(
          text = TrilhoStrings.get("shop", lang),
          color = Color.White,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )

        // Coins balance badge
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0x66FFD700)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
            Text(text = "${progress.totalCoins}", color = Color(0xFFFFF9C4), fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(TrilhoSkinRepository.skins) { skin ->
          val isUnlocked = progress.unlockedSkinIds.contains(skin.id)
          val isEquipped = settings.selectedSkinId == skin.id
          val canAfford = progress.totalCoins >= skin.costCoins

          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1A263B),
            border = androidx.compose.foundation.BorderStroke(
              width = if (isEquipped) 2.dp else 1.dp,
              color = if (isEquipped) Color(0xFF00E676) else Color(0x33FFFFFF)
            ),
            modifier = Modifier.fillMaxWidth().testTag("skin_item_${skin.id}")
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                // Color preview avatar orb
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(skin.primaryColor))
                    .border(2.5.dp, Color(skin.glowColor), CircleShape)
                )

                Column {
                  Text(
                    text = if (lang.startsWith("pt")) skin.namePt else skin.nameEn,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                  if (!isUnlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                      Text("${skin.costCoins} moedas", color = Color(0xFFFFD700), fontSize = 12.sp)
                    }
                  }
                }
              }

              // Action button
              if (isEquipped) {
                Button(
                  onClick = {},
                  enabled = false,
                  colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0x4400E676))
                ) {
                  Text(TrilhoStrings.get("equipped", lang), color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                }
              } else if (isUnlocked) {
                Button(
                  onClick = { onEquipSkin(skin.id) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38E1FF))
                ) {
                  Text(TrilhoStrings.get("equip", lang), color = Color(0xFF0F1726), fontWeight = FontWeight.Bold)
                }
              } else {
                Button(
                  onClick = { onBuySkin(skin.id, skin.costCoins) },
                  enabled = canAfford,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                  Text(TrilhoStrings.get("buy", lang), color = Color(0xFF211500), fontWeight = FontWeight.Bold)
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
fun TrilhoRecordsScreen(
  progress: TrilhoProgress,
  language: String,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF101726))
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBackClicked, modifier = Modifier.testTag("btn_records_back")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
        Text(TrilhoStrings.get("records", language), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(48.dp))
      }

      Spacer(modifier = Modifier.height(30.dp))

      StatCard(
        title = TrilhoStrings.get("best_distance", language),
        value = "${progress.bestDistanceMeters} m",
        icon = Icons.Default.EmojiEvents,
        accentColor = Color(0xFFFFD700)
      )

      Spacer(modifier = Modifier.height(16.dp))

      StatCard(
        title = TrilhoStrings.get("total_coins", language),
        value = "${progress.totalCoins}",
        icon = Icons.Default.MonetizationOn,
        accentColor = Color(0xFFFFD700)
      )

      Spacer(modifier = Modifier.height(16.dp))

      StatCard(
        title = TrilhoStrings.get("total_runs", language),
        value = "${progress.totalRunsPlayed}",
        icon = Icons.Default.DirectionsRun,
        accentColor = Color(0xFF38E1FF)
      )
    }
  }
}

@Composable
private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color(0xFF192538),
    border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor.copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
      }

      Column {
        Text(title, color = Color(0xFF90A4AE), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
      }
    }
  }
}

@Composable
fun TrilhoSettingsScreen(
  settings: TrilhoSettings,
  onSettingsChanged: (TrilhoSettings) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val lang = settings.language

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF101726))
      .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 500.dp)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBackClicked, modifier = Modifier.testTag("btn_settings_back")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
        Text(TrilhoStrings.get("settings", lang), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(48.dp))
      }

      Spacer(modifier = Modifier.height(24.dp))

      // SFX Volume
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(TrilhoStrings.get("sfx_volume", lang), color = Color.White, fontSize = 14.sp)
        Slider(
          value = settings.soundVolume,
          onValueChange = { onSettingsChanged(settings.copy(soundVolume = it)) },
          modifier = Modifier.width(200.dp).testTag("slider_sfx")
        )
      }

      // Music Volume
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(TrilhoStrings.get("music_volume", lang), color = Color.White, fontSize = 14.sp)
        Slider(
          value = settings.musicVolume,
          onValueChange = { onSettingsChanged(settings.copy(musicVolume = it)) },
          modifier = Modifier.width(200.dp).testTag("slider_music")
        )
      }

      // Vibration Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(TrilhoStrings.get("vibration", lang), color = Color.White, fontSize = 14.sp)
        Switch(
          checked = settings.vibrationEnabled,
          onCheckedChange = { onSettingsChanged(settings.copy(vibrationEnabled = it)) },
          modifier = Modifier.testTag("switch_vibration")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Language Switcher
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(TrilhoStrings.get("language", lang), color = Color.White, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = { onSettingsChanged(settings.copy(language = "pt")) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (settings.language == "pt") Color(0xFF38E1FF) else Color(0x33253245)
            )
          ) {
            Text("Português", color = if (settings.language == "pt") Color(0xFF0F1726) else Color.White)
          }

          Button(
            onClick = { onSettingsChanged(settings.copy(language = "en")) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (settings.language == "en") Color(0xFF38E1FF) else Color(0x33253245)
            )
          ) {
            Text("English", color = if (settings.language == "en") Color(0xFF0F1726) else Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun TrilhoGameOverDialog(
  distanceMeters: Int,
  coinsEarned: Int,
  totalCoins: Int,
  isNewRecord: Boolean,
  language: String,
  onPlayAgain: () -> Unit,
  onMainMenu: () -> Unit
) {
  var countdownSeconds by remember { mutableIntStateOf(10) }

  // 10s Auto-restart countdown
  LaunchedEffect(Unit) {
    while (countdownSeconds > 0) {
      delay(1000L)
      countdownSeconds--
    }
    onPlayAgain()
  }

  Dialog(onDismissRequest = {}) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xF5141D2B),
      border = androidx.compose.foundation.BorderStroke(
        width = 2.dp,
        color = if (isNewRecord) Color(0xFFFFD700) else Color(0xFFFF3D57)
      ),
      modifier = Modifier.widthIn(max = 420.dp)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = if (isNewRecord) "🏆 NOVO RECORDE!" else "💥 FIM DE LINHA",
          color = if (isNewRecord) Color(0xFFFFD700) else Color(0xFFFF3D57),
          fontSize = 22.sp,
          fontWeight = FontWeight.Black
        )

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0x660F1622),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("🏃 Distância", color = Color(0xFF90A4AE), fontSize = 14.sp)
              Text("$distanceMeters m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("🪙 Nesta corrida", color = Color(0xFF90A4AE), fontSize = 14.sp)
              Text("+$coinsEarned", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text("💰 Total moedas", color = Color(0xFF38E1FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
              Text("$totalCoins 🪙", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
          }
        }

        Text(
          text = "Recomeçar?",
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )

        // 10s Countdown progress bar
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          LinearProgressIndicator(
            progress = { (countdownSeconds / 10f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF00E676),
            trackColor = Color(0x33FFFFFF)
          )
          Text(
            text = "⏱️ Reinicia automaticamente em ${countdownSeconds}s",
            color = Color(0xFFB0BEC5),
            fontSize = 12.sp
          )
        }

        // [Ponto de integração: Ver anúncio para reviver - não implementado conforme instrução do utilizador]

        Button(
          onClick = onPlayAgain,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_game_over_play_again"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
        ) {
          Text("▶ Recomeçar (${countdownSeconds}s)", color = Color(0xFF092410), fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        OutlinedButton(
          onClick = onMainMenu,
          modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_game_over_menu")
        ) {
          Text("🏠 Menu", color = Color.White, fontSize = 14.sp)
        }
      }
    }
  }
}

@Composable
fun TrilhoPauseDialog(
  language: String,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onMainMenu: () -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color(0xF2141D2B),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x6638E1FF)),
      modifier = Modifier.widthIn(max = 360.dp)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(TrilhoStrings.get("paused", language), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Button(
          onClick = onResume,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_resume"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
        ) {
          Text(TrilhoStrings.get("resume", language), color = Color(0xFF092410), fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onRestart,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_restart")
        ) {
          Text(TrilhoStrings.get("restart", language), color = Color.White)
        }

        OutlinedButton(
          onClick = onMainMenu,
          modifier = Modifier.fillMaxWidth().testTag("btn_pause_menu")
        ) {
          Text(TrilhoStrings.get("main_menu", language), color = Color.White)
        }
      }
    }
  }
}

@Composable
fun TrilhoHowToPlayDialog(
  language: String,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color(0xF5141D2B),
      border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x6638E1FF)),
      modifier = Modifier.widthIn(max = 440.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(TrilhoStrings.get("how_to_play", language), color = Color(0xFF38E1FF), fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          TutorialRow(Icons.Default.Swipe, TrilhoStrings.get("tutorial_swipe_lr", language))
          TutorialRow(Icons.Default.ArrowUpward, TrilhoStrings.get("tutorial_swipe_up", language))
          TutorialRow(Icons.Default.ArrowDownward, TrilhoStrings.get("tutorial_swipe_down", language))
          TutorialRow(Icons.Default.Diamond, TrilhoStrings.get("tutorial_crystal", language))
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().testTag("btn_tutorial_close"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38E1FF))
        ) {
          Text(TrilhoStrings.get("back", language), color = Color(0xFF0F1726), fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun TutorialRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(icon, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
    Text(text, color = Color.White, fontSize = 13.sp)
  }
}
