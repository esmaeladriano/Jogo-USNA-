import React, { useState, useEffect, useRef } from 'react';
import { StyleSheet, View, Text, TouchableOpacity, SafeAreaView, StatusBar } from 'react-native';
import { TrilhoEngine } from './src/engine/TrilhoEngine';
import { TrilhoCanvas } from './src/components/TrilhoCanvas';
import { TouchHud } from './src/components/TouchHud';
import { GameOverModal } from './src/components/GameOverModal';
import { GameProgress } from './src/types';

export default function App() {
  const [inGame, setInGame] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [gameOver, setGameOver] = useState(false);
  const [, setTick] = useState(0);

  const [progress, setProgress] = useState<GameProgress>({
    totalCoins: 120,
    highScoreMeters: 450,
    selectedSkinId: 'default',
    unlockedSkins: ['default']
  });

  const engineRef = useRef<TrilhoEngine>(
    new TrilhoEngine((event, payload) => {
      if (event === 'GAME_OVER') {
        setGameOver(true);
        setProgress(prev => ({
          ...prev,
          totalCoins: prev.totalCoins + payload.coins,
          highScoreMeters: Math.max(prev.highScoreMeters, payload.distance)
        }));
      }
    })
  );

  const animFrameRef = useRef<number | null>(null);

  // 60 FPS Game Loop
  useEffect(() => {
    if (!inGame || isPaused || gameOver) {
      if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
      return;
    }

    const loop = () => {
      engineRef.current.tick();
      setTick(t => t + 1);
      animFrameRef.current = requestAnimationFrame(loop);
    };

    animFrameRef.current = requestAnimationFrame(loop);
    return () => {
      if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    };
  }, [inGame, isPaused, gameOver]);

  const startGame = () => {
    engineRef.current.reset();
    setGameOver(false);
    setIsPaused(false);
    setInGame(true);
  };

  const handleMainMenu = () => {
    setGameOver(false);
    setIsPaused(false);
    setInGame(false);
  };

  const engine = engineRef.current;

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1726" />

      {inGame ? (
        <View style={StyleSheet.absoluteFill}>
          {/* Game World Rendering */}
          <TrilhoCanvas engine={engine} />

          {/* Touch Gestures & HUD */}
          <TouchHud
            distanceMeters={engine.distanceMeters}
            coinsEarned={engine.coinsEarned}
            crystalsStock={engine.crystalsStock}
            hasShield={engine.hasShield}
            boostRemaining={engine.boostRemaining}
            magnetRemaining={engine.magnetRemaining}
            onSwipeLeft={() => engine.moveLeft()}
            onSwipeRight={() => engine.moveRight()}
            onSwipeUp={() => engine.jump()}
            onSwipeDown={() => engine.slide()}
            onUseCrystal={() => engine.useCrystal()}
            onPause={() => setIsPaused(p => !p)}
          />

          {/* Game Over Dialog (10s auto-restart timer) */}
          <GameOverModal
            visible={gameOver}
            distanceMeters={engine.distanceMeters}
            coinsEarned={engine.coinsEarned}
            totalCoins={progress.totalCoins}
            onPlayAgain={startGame}
            onMainMenu={handleMainMenu}
          />
        </View>
      ) : (
        /* Main Menu */
        <View style={styles.menuContainer}>
          <Text style={styles.gameTitle}>TRILHO</Text>
          <Text style={styles.subtitle}>ENDLESS RUNNER</Text>

          {/* Records Card */}
          <View style={styles.recordCard}>
            <View style={styles.recordItem}>
              <Text style={styles.recordLabel}>🏆 Recorde</Text>
              <Text style={styles.recordVal}>{progress.highScoreMeters} m</Text>
            </View>
            <View style={styles.recordItem}>
              <Text style={styles.recordLabel}>💰 Moedas</Text>
              <Text style={[styles.recordVal, { color: '#FFD700' }]}>{progress.totalCoins}</Text>
            </View>
          </View>

          {/* Play Button */}
          <TouchableOpacity style={styles.btnPlay} onPress={startGame}>
            <Text style={styles.btnPlayText}>JOGAR</Text>
          </TouchableOpacity>

          <View style={styles.controlsHint}>
            <Text style={styles.hintText}>↔ Deslize para mudar de faixa</Text>
            <Text style={styles.hintText}>⬆ Deslize para saltar  |  ⬇ Deslize para abaixar</Text>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F1726'
  },
  menuContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24
  },
  gameTitle: {
    fontSize: 54,
    fontWeight: '900',
    color: '#F97316',
    letterSpacing: 4
  },
  subtitle: {
    fontSize: 14,
    color: '#94A3B8',
    letterSpacing: 6,
    marginBottom: 40
  },
  recordCard: {
    width: '100%',
    maxWidth: 320,
    backgroundColor: 'rgba(30, 41, 59, 0.7)',
    borderRadius: 20,
    padding: 20,
    marginBottom: 32,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)'
  },
  recordItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 6
  },
  recordLabel: {
    color: '#94A3B8',
    fontSize: 16
  },
  recordVal: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold'
  },
  btnPlay: {
    width: '100%',
    maxWidth: 320,
    height: 60,
    backgroundColor: '#00E676',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 8,
    shadowColor: '#00E676',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.4,
    shadowRadius: 10,
    marginBottom: 24
  },
  btnPlayText: {
    color: '#092410',
    fontSize: 22,
    fontWeight: '900',
    letterSpacing: 2
  },
  controlsHint: {
    alignItems: 'center',
    opacity: 0.6
  },
  hintText: {
    color: '#94A3B8',
    fontSize: 13,
    marginVertical: 2
  }
});
