import React, { useState, useRef, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, PanResponder, Modal } from 'react-native';
import { TrilhoEngine, EngineEvent } from '../game/engine';
import { useGameLoop } from '../game/useGameLoop';
import { GameWorld } from '../render/GameWorld';
import { Skin } from '../game/skins';
import { useApp } from '../state/AppContext';
import { t } from '../i18n';
import { colors, radius } from '../theme';

interface GameScreenProps {
  skin: Skin;
  bestDistance: number;
  onRunFinished: (distance: number, coins: number) => { isNewRecord: boolean; newTotalCoins: number };
  onExit: () => void;
}

export const GameScreen: React.FC<GameScreenProps> = ({ skin, onRunFinished, onExit }) => {
  const { settings } = useApp();
  const lang = settings.language;

  const [paused, setPaused] = useState(false);
  const [gameOverInfo, setGameOverInfo] = useState<{
    distance: number;
    coins: number;
    totalCoins: number;
    isNewRecord: boolean;
  } | null>(null);

  const [autoRestartSec, setAutoRestartSec] = useState(10);

  const handleEngineEvent = useCallback(
    (event: EngineEvent) => {
      if (event.type === 'game_over') {
        const result = onRunFinished(event.distance, event.coins);
        setGameOverInfo({
          distance: event.distance,
          coins: event.coins,
          totalCoins: result.newTotalCoins,
          isNewRecord: result.isNewRecord,
        });
        setAutoRestartSec(10);
      }
    },
    [onRunFinished]
  );

  const engineRef = useRef<TrilhoEngine | null>(null);
  if (!engineRef.current) {
    engineRef.current = new TrilhoEngine(handleEngineEvent);
  }
  const engine = engineRef.current;

  // 60 Hz Game Loop
  useGameLoop(engine, paused || !!gameOverInfo);

  const restartGame = useCallback(() => {
    setGameOverInfo(null);
    setPaused(false);
    setAutoRestartSec(10);
    engine.reset();
  }, [engine]);

  // 10s auto-restart timer
  useEffect(() => {
    if (!gameOverInfo) return;

    const timer = setInterval(() => {
      setAutoRestartSec((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          restartGame();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [gameOverInfo, restartGame]);

  // Gesture Controls (Swipe Responder)
  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onPanResponderRelease: (_, gestureState) => {
        const { dx, dy } = gestureState;
        const absX = Math.abs(dx);
        const absY = Math.abs(dy);

        if (absX < 20 && absY < 20) {
          // Tap: jump
          engine.jump();
          return;
        }

        if (absX > absY) {
          if (dx > 30) {
            engine.moveRight();
          } else if (dx < -30) {
            engine.moveLeft();
          }
        } else {
          if (dy < -30) {
            engine.jump();
          } else if (dy > 30) {
            engine.slide();
          }
        }
      },
    })
  ).current;

  return (
    <View style={styles.container} {...panResponder.panHandlers}>
      {/* 2D Canvas / World */}
      <GameWorld engine={engine} skin={skin} />

      {/* Top HUD */}
      <View style={styles.hudTop}>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>🏃 {engine.distanceMeters} m</Text>
        </View>

        <View style={styles.hudRight}>
          <View style={styles.badge}>
            <Text style={[styles.badgeText, { color: colors.gold }]}>🪙 {engine.runCoins}</Text>
          </View>
          <TouchableOpacity style={styles.pauseBtn} onPress={() => setPaused(true)} activeOpacity={0.8}>
            <Text style={styles.pauseBtnText}>⏸</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Active Powerups Badges */}
      <View style={styles.powerupContainer}>
        {engine.hasShield && (
          <View style={[styles.powerupBadge, { backgroundColor: 'rgba(34, 227, 139, 0.25)', borderColor: colors.green }]}>
            <Text style={styles.powerupText}>🛡️ {t(lang, 'shield')}</Text>
          </View>
        )}
        {engine.magnetTicksRemaining > 0 && (
          <View style={[styles.powerupBadge, { backgroundColor: 'rgba(56, 225, 255, 0.25)', borderColor: colors.cyan }]}>
            <Text style={styles.powerupText}>🧲 {Math.ceil(engine.magnetTicksRemaining / 60)}s</Text>
          </View>
        )}
        {engine.boostTicksRemaining > 0 && (
          <View style={[styles.powerupBadge, { backgroundColor: 'rgba(255, 77, 94, 0.25)', borderColor: colors.red }]}>
            <Text style={styles.powerupText}>⚡ {Math.ceil(engine.boostTicksRemaining / 60)}s</Text>
          </View>
        )}
      </View>

      {/* Crystal Invulnerability Ability Button */}
      <View style={styles.crystalContainer}>
        <TouchableOpacity
          style={[
            styles.crystalBtn,
            engine.crystalsStock > 0 && styles.crystalBtnReady,
            engine.crystalInvulnTicks > 0 && styles.crystalBtnActive,
          ]}
          onPress={() => engine.useCrystalAbility()}
          activeOpacity={0.7}
        >
          <Text style={styles.crystalEmoji}>💎</Text>
          <Text style={styles.crystalCount}>
            {engine.crystalInvulnTicks > 0
              ? `${(engine.crystalInvulnTicks / 60).toFixed(1)}s`
              : `${engine.crystalsStock}/${TrilhoEngine.MAX_CRYSTALS}`}
          </Text>
        </TouchableOpacity>
      </View>

      {/* Pause Modal */}
      <Modal visible={paused} transparent animationType="fade">
        <View style={styles.overlayBackdrop}>
          <View style={styles.overlayCard}>
            <Text style={styles.overlayTitle}>{t(lang, 'paused')}</Text>
            <TouchableOpacity style={styles.menuActionBtn} onPress={() => setPaused(false)}>
              <Text style={styles.menuActionBtnText}>▶ {t(lang, 'resume')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.menuActionBtnSecondary} onPress={restartGame}>
              <Text style={styles.menuActionBtnSecondaryText}>🔄 {t(lang, 'restart')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.menuActionBtnSecondary} onPress={onExit}>
              <Text style={styles.menuActionBtnSecondaryText}>🏠 {t(lang, 'menu')}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Game Over Modal with 10s auto-restart */}
      <Modal visible={!!gameOverInfo} transparent animationType="fade">
        <View style={styles.overlayBackdrop}>
          <View style={styles.overlayCard}>
            <Text style={styles.gameOverEmoji}>💥</Text>
            <Text style={styles.overlayTitle}>{t(lang, 'gameOver')}</Text>

            {gameOverInfo?.isNewRecord && (
              <View style={styles.recordBadge}>
                <Text style={styles.recordText}>🏆 {t(lang, 'newRecord')}</Text>
              </View>
            )}

            {/* Run Stats */}
            <View style={styles.statsRow}>
              <View style={styles.statBox}>
                <Text style={styles.statBoxLabel}>Distância</Text>
                <Text style={styles.statBoxValue}>{gameOverInfo?.distance} m</Text>
              </View>
              <View style={styles.statBox}>
                <Text style={styles.statBoxLabel}>Ganhos</Text>
                <Text style={[styles.statBoxValue, { color: colors.gold }]}>+{gameOverInfo?.coins} 🪙</Text>
              </View>
              <View style={styles.statBox}>
                <Text style={styles.statBoxLabel}>Total</Text>
                <Text style={[styles.statBoxValue, { color: colors.cyan }]}>💰 {gameOverInfo?.totalCoins}</Text>
              </View>
            </View>

            {/* Auto restart timer progress */}
            <View style={styles.timerBarContainer}>
              <View style={[styles.timerBarFill, { width: `${(autoRestartSec / 10) * 100}%` }]} />
            </View>
            <Text style={styles.autoRestartText}>{t(lang, 'autoRestart', { s: autoRestartSec })}</Text>

            {/* Action buttons */}
            <TouchableOpacity style={styles.menuActionBtn} onPress={restartGame} activeOpacity={0.85}>
              <Text style={styles.menuActionBtnText}>🔄 {t(lang, 'playAgain')}</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.menuActionBtnSecondary} onPress={onExit} activeOpacity={0.8}>
              <Text style={styles.menuActionBtnSecondaryText}>🏠 {t(lang, 'menu')}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.bg0,
  },
  hudTop: {
    position: 'absolute',
    top: 50,
    left: 20,
    right: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    zIndex: 10,
  },
  hudRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  badge: {
    backgroundColor: 'rgba(15, 23, 41, 0.85)',
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  badgeText: {
    color: colors.text,
    fontSize: 15,
    fontWeight: 'bold',
  },
  pauseBtn: {
    backgroundColor: 'rgba(15, 23, 41, 0.85)',
    width: 40,
    height: 40,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    justifyContent: 'center',
    alignItems: 'center',
  },
  pauseBtnText: {
    color: colors.text,
    fontSize: 16,
  },
  powerupContainer: {
    position: 'absolute',
    top: 110,
    left: 20,
    gap: 6,
    zIndex: 10,
  },
  powerupBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: radius.sm,
    borderWidth: 1,
  },
  powerupText: {
    color: colors.text,
    fontSize: 12,
    fontWeight: '600',
  },
  crystalContainer: {
    position: 'absolute',
    bottom: 40,
    right: 24,
    zIndex: 10,
  },
  crystalBtn: {
    backgroundColor: 'rgba(15, 23, 41, 0.75)',
    width: 68,
    height: 68,
    borderRadius: radius.pill,
    borderWidth: 2,
    borderColor: 'rgba(255,255,255,0.2)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  crystalBtnReady: {
    borderColor: colors.cyan,
    backgroundColor: 'rgba(56, 225, 255, 0.2)',
  },
  crystalBtnActive: {
    borderColor: colors.accent,
    backgroundColor: 'rgba(255, 122, 24, 0.35)',
  },
  crystalEmoji: {
    fontSize: 24,
  },
  crystalCount: {
    color: colors.text,
    fontSize: 11,
    fontWeight: 'bold',
    marginTop: 2,
  },
  overlayBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(6, 10, 20, 0.85)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  overlayCard: {
    backgroundColor: colors.bg1,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    padding: 24,
    width: '100%',
    alignItems: 'center',
  },
  gameOverEmoji: {
    fontSize: 48,
    marginBottom: 8,
  },
  overlayTitle: {
    fontSize: 24,
    fontWeight: '900',
    color: colors.text,
    marginBottom: 16,
  },
  recordBadge: {
    backgroundColor: 'rgba(255, 213, 74, 0.2)',
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.gold,
    marginBottom: 16,
  },
  recordText: {
    color: colors.gold,
    fontWeight: 'bold',
    fontSize: 13,
  },
  statsRow: {
    flexDirection: 'row',
    width: '100%',
    justifyContent: 'space-between',
    backgroundColor: colors.card,
    borderRadius: radius.md,
    padding: 12,
    marginBottom: 18,
  },
  statBox: {
    alignItems: 'center',
    flex: 1,
  },
  statBoxLabel: {
    fontSize: 11,
    color: colors.muted,
    textTransform: 'uppercase',
  },
  statBoxValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: colors.text,
    marginTop: 4,
  },
  timerBarContainer: {
    width: '100%',
    height: 6,
    backgroundColor: 'rgba(255,255,255,0.1)',
    borderRadius: radius.pill,
    overflow: 'hidden',
    marginBottom: 8,
  },
  timerBarFill: {
    height: '100%',
    backgroundColor: colors.green,
  },
  autoRestartText: {
    fontSize: 12,
    color: colors.muted,
    marginBottom: 20,
  },
  menuActionBtn: {
    backgroundColor: colors.accent,
    borderRadius: radius.pill,
    paddingVertical: 14,
    width: '100%',
    alignItems: 'center',
    marginBottom: 10,
  },
  menuActionBtnText: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  menuActionBtnSecondary: {
    backgroundColor: colors.card,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingVertical: 12,
    width: '100%',
    alignItems: 'center',
    marginBottom: 8,
  },
  menuActionBtnSecondaryText: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
  },
});
