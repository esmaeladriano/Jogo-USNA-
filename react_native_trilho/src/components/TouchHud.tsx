import React, { useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, PanResponder } from 'react-native';

interface TouchHudProps {
  distanceMeters: number;
  coinsEarned: number;
  crystalsStock: number;
  hasShield: boolean;
  boostRemaining: number;
  magnetRemaining: number;
  onSwipeLeft: () => void;
  onSwipeRight: () => void;
  onSwipeUp: () => void;
  onSwipeDown: () => void;
  onUseCrystal: () => void;
  onPause: () => void;
}

export const TouchHud: React.FC<TouchHudProps> = ({
  distanceMeters,
  coinsEarned,
  crystalsStock,
  hasShield,
  boostRemaining,
  magnetRemaining,
  onSwipeLeft,
  onSwipeRight,
  onSwipeUp,
  onSwipeDown,
  onUseCrystal,
  onPause
}) => {
  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: (_, gestureState) => {
        return Math.abs(gestureState.dx) > 15 || Math.abs(gestureState.dy) > 15;
      },
      onPanResponderRelease: (_, gestureState) => {
        const { dx, dy } = gestureState;
        const SWIPE_THRESHOLD = 45;

        if (Math.abs(dx) > Math.abs(dy)) {
          if (dx > SWIPE_THRESHOLD) onSwipeRight();
          else if (dx < -SWIPE_THRESHOLD) onSwipeLeft();
        } else {
          if (dy > SWIPE_THRESHOLD) onSwipeDown();
          else if (dy < -SWIPE_THRESHOLD) onSwipeUp();
        }
      }
    })
  ).current;

  return (
    <View style={StyleSheet.absoluteFill} pointerEvents="box-none">
      {/* Invisible Gesture Catch Area */}
      <View style={StyleSheet.absoluteFill} {...panResponder.panHandlers} />

      {/* Top Header HUD */}
      <View style={styles.topBar}>
        <View style={styles.hudBadge}>
          <Text style={styles.hudText}>🏃 {distanceMeters} m</Text>
        </View>

        <View style={styles.hudBadge}>
          <Text style={[styles.hudText, { color: '#FFD700' }]}>🪙 {coinsEarned}</Text>
        </View>

        {hasShield && (
          <View style={[styles.hudBadge, { backgroundColor: '#1E3A8A' }]}>
            <Text style={styles.hudText}>🛡️</Text>
          </View>
        )}

        {boostRemaining > 0 && (
          <View style={[styles.hudBadge, { backgroundColor: '#B45309' }]}>
            <Text style={styles.hudText}>⚡ {Math.ceil(boostRemaining / 60)}s</Text>
          </View>
        )}

        {magnetRemaining > 0 && (
          <View style={[styles.hudBadge, { backgroundColor: '#7C3AED' }]}>
            <Text style={styles.hudText}>🧲 {Math.ceil(magnetRemaining / 60)}s</Text>
          </View>
        )}

        <TouchableOpacity style={styles.pauseBtn} onPress={onPause}>
          <Text style={styles.pauseText}>⏸</Text>
        </TouchableOpacity>
      </View>

      {/* Crystal Ability Button (Bottom Right) */}
      <View style={styles.bottomRight}>
        <TouchableOpacity
          style={[
            styles.crystalBtn,
            crystalsStock > 0 ? styles.crystalBtnActive : styles.crystalBtnInactive
          ]}
          onPress={onUseCrystal}
          disabled={crystalsStock <= 0}
        >
          <Text style={styles.crystalIcon}>💎</Text>
          <Text style={styles.crystalCount}>{crystalsStock}/3</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  topBar: {
    position: 'absolute',
    top: 50,
    left: 16,
    right: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8
  },
  hudBadge: {
    backgroundColor: 'rgba(15, 23, 38, 0.85)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.15)'
  },
  hudText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 14
  },
  pauseBtn: {
    marginLeft: 'auto',
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: 'rgba(15, 23, 38, 0.85)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)'
  },
  pauseText: {
    color: '#FFFFFF',
    fontSize: 16
  },
  bottomRight: {
    position: 'absolute',
    bottom: 36,
    right: 20
  },
  crystalBtn: {
    width: 64,
    height: 64,
    borderRadius: 32,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 8,
    shadowColor: '#38E1FF',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.5,
    shadowRadius: 8
  },
  crystalBtnActive: {
    backgroundColor: '#0284C7',
    borderWidth: 2,
    borderColor: '#38E1FF'
  },
  crystalBtnInactive: {
    backgroundColor: 'rgba(30, 41, 59, 0.6)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.1)'
  },
  crystalIcon: {
    fontSize: 22
  },
  crystalCount: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: 'bold'
  }
});
