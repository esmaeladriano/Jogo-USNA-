package com.example.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.UsnaAudioSynthesizer
import com.example.game.audio.UsnaVibrator
import com.example.game.engine.UsnaLevels
import com.example.game.engine.UsnaPhysicsEngine
import com.example.game.model.*
import com.example.game.storage.GameStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class ScreenState {
  MAIN_MENU,
  LEVEL_SELECT,
  SETTINGS,
  GAMEPLAY
}

data class VictoryState(
  val level: LevelData,
  val timeSec: Float,
  val stars: Int,
  val echoesUsed: Int
)

/**
 * State of the player movement recording system.
 */
data class EchoRecordingState(
  val isRecording: Boolean = false,
  val recordingTicks: Int = 0,
  val maxRecordingTicks: Int = UsnaPhysicsEngine.MAX_RECORD_TICKS,
  val recordingProgress: Float = 0f,
  val recordedInputsCount: Int = 0,
  val hasPendingRecording: Boolean = false,
  val canRecord: Boolean = true
)

/**
 * State of active echoes replaying in the puzzle level.
 */
data class EchoTrackingState(
  val activeEchoes: List<EchoInstance> = emptyList(),
  val activeEchoesCount: Int = 0,
  val maxEchoes: Int = 3,
  val echoesRemaining: Int = 3,
  val totalEchoesUsed: Int = 0,
  val canReleaseEcho: Boolean = false,
  val canUndoEcho: Boolean = false
)

/**
 * Real-time score, elapsed time, stars and high score status.
 */
data class ScoreStatusState(
  val currentScore: Int = 0,
  val levelTimeSec: Float = 0f,
  val targetTimeSec: Float = 20f,
  val currentStars: Int = 3,
  val isTimeExceeded: Boolean = false,
  val totalStarsEarned: Int = 0,
  val totalGameScore: Int = 0,
  val bestTimeForLevel: Float = 0f,
  val bestStarsForLevel: Int = 0,
  val isNewHighScore: Boolean = false,
  val formattedTime: String = "00:00.0"
)

/**
 * High-level phase of the puzzle level.
 */
enum class GameplayPhase {
  READY,
  PLAYING,
  RECORDING,
  PAUSED,
  VICTORY,
  RESPAWNING
}

/**
 * Main ViewModel managing full game state, including player movement recording,
 * echo tracking, dynamic score status, level progression, and physics loops.
 */
open class GameViewModel(application: Application) : AndroidViewModel(application) {
  protected val storage = GameStorage(application)
  val audio = UsnaAudioSynthesizer()
  val vibrator = UsnaVibrator(application)

  // Navigation Screen State
  private val _screen = MutableStateFlow(ScreenState.MAIN_MENU)
  val screen: StateFlow<ScreenState> = _screen.asStateFlow()

  // High-level Gameplay Phase
  private val _gameplayPhase = MutableStateFlow(GameplayPhase.READY)
  val gameplayPhase: StateFlow<GameplayPhase> = _gameplayPhase.asStateFlow()

  // Current Level Data
  private val _currentLevelId = MutableStateFlow(1)
  val currentLevelId: StateFlow<Int> = _currentLevelId.asStateFlow()

  private val _currentLevel = MutableStateFlow(UsnaLevels.getLevel(1))
  val currentLevel: StateFlow<LevelData> = _currentLevel.asStateFlow()

  // Settings & Progress Persistence
  private val _settings = MutableStateFlow(storage.loadSettings())
  val settings: StateFlow<GameSettings> = _settings.asStateFlow()

  private val _progress = MutableStateFlow(storage.loadProgress())
  val progress: StateFlow<GameProgress> = _progress.asStateFlow()

  // Pause & Dialog Overlays
  private val _isPaused = MutableStateFlow(false)
  val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

  private val _victoryState = MutableStateFlow<VictoryState?>(null)
  val victoryState: StateFlow<VictoryState?> = _victoryState.asStateFlow()

  private val _showHowToPlay = MutableStateFlow(false)
  val showHowToPlay: StateFlow<Boolean> = _showHowToPlay.asStateFlow()

  private val _showSkins = MutableStateFlow(false)
  val showSkins: StateFlow<Boolean> = _showSkins.asStateFlow()

  // 1. Player Movement Recording State
  private val _recordingState = MutableStateFlow(EchoRecordingState())
  val recordingState: StateFlow<EchoRecordingState> = _recordingState.asStateFlow()

  val isRecording: StateFlow<Boolean> get() = MutableStateFlow(_recordingState.value.isRecording).asStateFlow()
  val recordingProgress: StateFlow<Float> get() = MutableStateFlow(_recordingState.value.recordingProgress).asStateFlow()

  // 2. Echo Tracking State
  private val _echoTrackingState = MutableStateFlow(EchoTrackingState())
  val echoTrackingState: StateFlow<EchoTrackingState> = _echoTrackingState.asStateFlow()

  val activeEchoesCount: StateFlow<Int> get() = MutableStateFlow(_echoTrackingState.value.activeEchoesCount).asStateFlow()
  val echoesRemaining: StateFlow<Int> get() = MutableStateFlow(_echoTrackingState.value.echoesRemaining).asStateFlow()

  // 3. Current Score & Status
  private val _scoreStatus = MutableStateFlow(ScoreStatusState())
  val scoreStatus: StateFlow<ScoreStatusState> = _scoreStatus.asStateFlow()

  val currentScore: StateFlow<Int> get() = MutableStateFlow(_scoreStatus.value.currentScore).asStateFlow()
  val levelTimeSec: StateFlow<Float> get() = MutableStateFlow(_scoreStatus.value.levelTimeSec).asStateFlow()
  val currentStars: StateFlow<Int> get() = MutableStateFlow(_scoreStatus.value.currentStars).asStateFlow()

  // Physics engine instance
  lateinit var engine: UsnaPhysicsEngine
    private set

  // Continuous input cache
  private var currentInput = GameInput()
  private var gameLoopJob: Job? = null

  init {
    val initialLevel = UsnaLevels.getLevel(_currentLevelId.value)
    initEngine(initialLevel)
    applySettingsToAudioAndHaptics(_settings.value)
    audio.startBackgroundMusic()
    refreshAllStates()
  }

  protected fun initEngine(level: LevelData) {
    _currentLevel.value = level
    engine = UsnaPhysicsEngine(level) { event ->
      handleEngineEvent(event)
    }
    refreshAllStates()
  }

  // --- Engine Event Dispatcher ---
  protected open fun handleEngineEvent(event: UsnaPhysicsEngine.EngineEvent) {
    when (event) {
      is UsnaPhysicsEngine.EngineEvent.Jump -> {
        audio.playJump()
      }
      is UsnaPhysicsEngine.EngineEvent.RecordStart -> {
        audio.playRecordStart()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.RecordEnd -> {
        audio.playRecordEnd()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.ReleaseEcho -> {
        audio.playReleaseEcho()
        vibrator.vibrateAction()
      }
      is UsnaPhysicsEngine.EngineEvent.ButtonPressed -> {
        audio.playButtonPressed()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.ButtonReleased -> {
        audio.playButtonReleased()
      }
      is UsnaPhysicsEngine.EngineEvent.DoorOpen -> {
        audio.playDoorOpen()
      }
      is UsnaPhysicsEngine.EngineEvent.DoorClose -> {
        audio.playDoorClose()
      }
      is UsnaPhysicsEngine.EngineEvent.SpikeDeath -> {
        audio.playSpikeDeath()
        vibrator.vibrateHazard()
        _gameplayPhase.value = GameplayPhase.RESPAWNING
      }
      is UsnaPhysicsEngine.EngineEvent.EchoDestroyed -> {
        audio.playSpikeDeath()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.LevelWon -> {
        audio.playVictory()
        vibrator.vibrateVictory()
        _gameplayPhase.value = GameplayPhase.VICTORY

        val currentLvl = _currentLevel.value
        val updated = storage.updateLevelCompletion(
          currentLvl.id,
          event.stars,
          event.timeSec,
          event.echoesUsed
        )
        _progress.value = updated
        _victoryState.value = VictoryState(
          level = currentLvl,
          timeSec = event.timeSec,
          stars = event.stars,
          echoesUsed = event.echoesUsed
        )
      }
    }
    refreshAllStates()
  }

  // --- Player Movement Recording ---
  fun startRecording() {
    if (engine.canStartRecording() && !_isPaused.value && _victoryState.value == null) {
      engine.startRecording()
      _gameplayPhase.value = GameplayPhase.RECORDING
      refreshAllStates()
    }
  }

  fun stopRecording() {
    if (engine.isRecording) {
      engine.stopRecording()
      _gameplayPhase.value = GameplayPhase.PLAYING
      refreshAllStates()
    }
  }

  fun toggleRecording() {
    if (engine.isRecording) {
      stopRecording()
    } else {
      startRecording()
    }
  }

  fun discardRecording() {
    if (engine.isRecording) {
      engine.stopRecording()
      engine.pendingRecording = null
      _gameplayPhase.value = GameplayPhase.PLAYING
      refreshAllStates()
    }
  }

  // --- Echo Tracking & Management ---
  fun releaseEcho() {
    if (engine.canReleaseEcho() && !_isPaused.value) {
      engine.releaseEcho()
      refreshAllStates()
    }
  }

  fun undoLastEcho() {
    if (engine.canUndoLastEcho()) {
      if (engine.undoLastEcho()) {
        vibrator.vibrateClick()
      }
      refreshAllStates()
    }
  }

  fun clearAllEchoes() {
    synchronized(engine.stateLock) {
      engine.activeEchoes.clear()
    }
    refreshAllStates()
  }

  // --- Input & Controls ---
  fun onInputChanged(input: GameInput) {
    currentInput = input
  }

  fun onMoveLeft(pressed: Boolean) {
    currentInput = currentInput.copy(moveLeft = pressed)
  }

  fun onMoveRight(pressed: Boolean) {
    currentInput = currentInput.copy(moveRight = pressed)
  }

  fun onJump(pressed: Boolean) {
    currentInput = currentInput.copy(jump = pressed)
  }

  fun onAction() {
    if (engine.canReleaseEcho()) {
      releaseEcho()
    } else if (engine.canStartRecording()) {
      startRecording()
    }
  }

  // --- Navigation & Level Flow ---
  fun navigateTo(newScreen: ScreenState) {
    _screen.value = newScreen
    if (newScreen == ScreenState.GAMEPLAY) {
      startGameLoop()
    } else {
      stopGameLoop()
    }
  }

  fun startLevel(levelId: Int) {
    _currentLevelId.value = levelId
    val level = UsnaLevels.getLevel(levelId)
    initEngine(level)
    _isPaused.value = false
    _victoryState.value = null
    _gameplayPhase.value = GameplayPhase.PLAYING
    navigateTo(ScreenState.GAMEPLAY)
  }

  fun restartCurrentLevel() {
    engine.resetLevel()
    _isPaused.value = false
    _victoryState.value = null
    _gameplayPhase.value = GameplayPhase.PLAYING
    refreshAllStates()
  }

  fun nextLevel() {
    val nextId = _currentLevelId.value + 1
    if (nextId <= 10) {
      startLevel(nextId)
    } else {
      navigateTo(ScreenState.LEVEL_SELECT)
    }
  }

  fun togglePause() {
    _isPaused.value = !_isPaused.value
    _gameplayPhase.value = if (_isPaused.value) GameplayPhase.PAUSED else GameplayPhase.PLAYING
  }

  fun resumeGame() {
    _isPaused.value = false
    _gameplayPhase.value = GameplayPhase.PLAYING
  }

  fun setHowToPlayVisible(visible: Boolean) {
    _showHowToPlay.value = visible
  }

  fun setSkinsVisible(visible: Boolean) {
    _showSkins.value = visible
  }

  fun selectSkin(skinId: String) {
    val updated = _settings.value.copy(selectedSkinId = skinId)
    updateSettings(updated)
  }

  fun updateSettings(newSettings: GameSettings) {
    _settings.value = newSettings
    storage.saveSettings(newSettings)
    applySettingsToAudioAndHaptics(newSettings)
  }

  private fun applySettingsToAudioAndHaptics(settings: GameSettings) {
    audio.soundVolume = settings.soundVolume
    audio.musicVolume = settings.musicVolume
    vibrator.isEnabled = settings.vibrationEnabled
  }

  // --- Dynamic State Synchronization ---
  private fun refreshAllStates() {
    val level = _currentLevel.value
    val timeSec = engine.levelTimeSec
    val targetTime = level.targetTimeSec

    // 1. Sync Recording State
    val recInputsCount = engine.recordingInputs.size
    val recTicks = if (engine.isRecording) recInputsCount else 0
    val recProgress = (recTicks.toFloat() / UsnaPhysicsEngine.MAX_RECORD_TICKS).coerceIn(0f, 1f)
    _recordingState.value = EchoRecordingState(
      isRecording = engine.isRecording,
      recordingTicks = recTicks,
      maxRecordingTicks = UsnaPhysicsEngine.MAX_RECORD_TICKS,
      recordingProgress = recProgress,
      recordedInputsCount = recInputsCount,
      hasPendingRecording = engine.pendingRecording != null,
      canRecord = engine.canStartRecording()
    )

    // 2. Sync Echo Tracking State
    val safeEchoes = synchronized(engine.stateLock) { engine.activeEchoes.toList() }
    val maxEchoes = level.maxEchoes
    val activeCount = safeEchoes.count { !it.isDestroyed }
    val remaining = (maxEchoes - activeCount).coerceAtLeast(0)
    _echoTrackingState.value = EchoTrackingState(
      activeEchoes = safeEchoes,
      activeEchoesCount = activeCount,
      maxEchoes = maxEchoes,
      echoesRemaining = remaining,
      totalEchoesUsed = engine.totalEchoesUsedInLevel,
      canReleaseEcho = engine.canReleaseEcho(),
      canUndoEcho = engine.canUndoLastEcho()
    )

    // 3. Sync Score & Stars Status
    val levelStats = _progress.value.levelStats[level.id]
    val bestTime = levelStats?.bestTimeSec ?: 0f
    val bestStars = levelStats?.stars ?: 0

    // Dynamic star calculation based on time and echoes used
    val starsEstimate = when {
      timeSec <= targetTime && engine.totalEchoesUsedInLevel <= maxEchoes -> 3
      timeSec <= targetTime * 1.5f -> 2
      else -> 1
    }

    // Dynamic real-time score calculation
    val baseScore = 1000
    val timeBonus = ((targetTime - timeSec) * 25f).toInt().coerceAtLeast(0)
    val echoBonus = (remaining * 150).coerceAtLeast(0)
    val score = (baseScore + timeBonus + echoBonus).coerceAtLeast(0)

    val minutes = (timeSec / 60f).toInt()
    val seconds = timeSec % 60f
    val formattedTime = String.format(Locale.US, "%02d:%04.1f", minutes, seconds)

    val totalStars = _progress.value.totalStars
    val isNewBest = bestTime > 0f && timeSec < bestTime

    _scoreStatus.value = ScoreStatusState(
      currentScore = score,
      levelTimeSec = timeSec,
      targetTimeSec = targetTime,
      currentStars = starsEstimate,
      isTimeExceeded = timeSec > targetTime,
      totalStarsEarned = totalStars,
      totalGameScore = totalStars * 1000,
      bestTimeForLevel = bestTime,
      bestStarsForLevel = bestStars,
      isNewHighScore = isNewBest,
      formattedTime = formattedTime
    )
  }

  // --- Physics Game Loop ---
  private fun startGameLoop() {
    stopGameLoop()
    gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
      val frameDurationMs = 1000L / UsnaPhysicsEngine.TICK_RATE
      while (isActive) {
        val start = System.currentTimeMillis()
        if (!_isPaused.value && _victoryState.value == null) {
          engine.tick(currentInput)
          refreshAllStates()
        }
        val elapsed = System.currentTimeMillis() - start
        val delayTime = (frameDurationMs - elapsed).coerceAtLeast(1L)
        delay(delayTime)
      }
    }
  }

  private fun stopGameLoop() {
    gameLoopJob?.cancel()
    gameLoopJob = null
  }

  override fun onCleared() {
    super.onCleared()
    stopGameLoop()
    audio.release()
  }
}
