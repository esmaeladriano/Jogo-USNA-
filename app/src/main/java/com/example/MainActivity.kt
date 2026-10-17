package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trilho.model.TrilhoSkinRepository
import com.example.trilho.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: TrilhoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        TrilhoAppRoot(viewModel = viewModel)
      }
    }
  }

  // Support for desktop and physical keyboards (WASD, Arrows, Space, Shift/E, P/Esc)
  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    var handled = false
    if (viewModel.screen.value == TrilhoScreenState.GAMEPLAY) {
      when (keyCode) {
        KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> {
          viewModel.onSwipeLeft()
          handled = true
        }
        KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> {
          viewModel.onSwipeRight()
          handled = true
        }
        KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_UP -> {
          viewModel.onSwipeUp()
          handled = true
        }
        KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_DPAD_DOWN -> {
          viewModel.onSwipeDown()
          handled = true
        }
        KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_E -> {
          viewModel.onUseCrystalAbility()
          handled = true
        }
        KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_ESCAPE -> {
          viewModel.togglePause()
          handled = true
        }
      }
    }
    return if (handled) true else super.onKeyDown(keyCode, event)
  }
}

@Composable
fun TrilhoAppRoot(viewModel: TrilhoViewModel) {
  val currentScreen by viewModel.screen.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val progress by viewModel.progress.collectAsStateWithLifecycle()
  val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
  val gameOverState by viewModel.gameOverState.collectAsStateWithLifecycle()
  val showHowToPlay by viewModel.showHowToPlay.collectAsStateWithLifecycle()

  // Handle system back navigation
  BackHandler(enabled = currentScreen != TrilhoScreenState.MAIN_MENU) {
    when (currentScreen) {
      TrilhoScreenState.GAMEPLAY -> viewModel.togglePause()
      TrilhoScreenState.SHOP, TrilhoScreenState.RECORDS, TrilhoScreenState.SETTINGS -> viewModel.navigateTo(TrilhoScreenState.MAIN_MENU)
      TrilhoScreenState.MAIN_MENU -> {}
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F1726))
  ) {
    when (currentScreen) {
      TrilhoScreenState.MAIN_MENU -> {
        TrilhoMainMenuScreen(
          progress = progress,
          settings = settings,
          onPlayClicked = { viewModel.startRun() },
          onShopClicked = { viewModel.navigateTo(TrilhoScreenState.SHOP) },
          onRecordsClicked = { viewModel.navigateTo(TrilhoScreenState.RECORDS) },
          onSettingsClicked = { viewModel.navigateTo(TrilhoScreenState.SETTINGS) },
          onHowToPlayClicked = { viewModel.setHowToPlayVisible(true) }
        )
      }

      TrilhoScreenState.SHOP -> {
        TrilhoShopScreen(
          progress = progress,
          settings = settings,
          onEquipSkin = { viewModel.equipSkin(it) },
          onBuySkin = { skinId, cost -> viewModel.buySkin(skinId, cost) },
          onBackClicked = { viewModel.navigateTo(TrilhoScreenState.MAIN_MENU) }
        )
      }

      TrilhoScreenState.RECORDS -> {
        TrilhoRecordsScreen(
          progress = progress,
          language = settings.language,
          onBackClicked = { viewModel.navigateTo(TrilhoScreenState.MAIN_MENU) }
        )
      }

      TrilhoScreenState.SETTINGS -> {
        TrilhoSettingsScreen(
          settings = settings,
          onSettingsChanged = { viewModel.updateSettings(it) },
          onBackClicked = { viewModel.navigateTo(TrilhoScreenState.MAIN_MENU) }
        )
      }

      TrilhoScreenState.GAMEPLAY -> {
        val currentSkin = remember(settings.selectedSkinId) { TrilhoSkinRepository.getSkin(settings.selectedSkinId) }

        Box(modifier = Modifier.fillMaxSize()) {
          // 1. 2D Railway Game World
          TrilhoGameCanvas(
            engine = viewModel.engine,
            skin = currentSkin,
            modifier = Modifier.fillMaxSize()
          )

          // 2. Touch HUD & Swipe Controls (>=50px threshold, crystal button, indicators)
          TrilhoTouchHud(
            engine = viewModel.engine,
            language = settings.language,
            onPauseClicked = { viewModel.togglePause() },
            onSwipeLeft = { viewModel.onSwipeLeft() },
            onSwipeRight = { viewModel.onSwipeRight() },
            onSwipeUp = { viewModel.onSwipeUp() },
            onSwipeDown = { viewModel.onSwipeDown() },
            onCrystalAbilityClicked = { viewModel.onUseCrystalAbility() }
          )

          // 3. Pause Overlay
          if (isPaused) {
            TrilhoPauseDialog(
              language = settings.language,
              onResume = { viewModel.resumeGame() },
              onRestart = { viewModel.startRun() },
              onMainMenu = { viewModel.navigateTo(TrilhoScreenState.MAIN_MENU) }
            )
          }

          // 4. Game Over Overlay
          gameOverState?.let { over ->
            TrilhoGameOverDialog(
              distanceMeters = over.distanceMeters,
              coinsEarned = over.coinsEarned,
              isNewRecord = over.isNewRecord,
              language = settings.language,
              onPlayAgain = { viewModel.startRun() },
              onMainMenu = { viewModel.navigateTo(TrilhoScreenState.MAIN_MENU) }
            )
          }
        }
      }
    }

    // How to Play Tutorial Modal
    if (showHowToPlay) {
      TrilhoHowToPlayDialog(
        language = settings.language,
        onDismiss = { viewModel.setHowToPlayVisible(false) }
      )
    }
  }
}
