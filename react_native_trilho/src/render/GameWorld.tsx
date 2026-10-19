import React from 'react';
import { View, StyleSheet, Dimensions, Text, StyleProp, ViewStyle } from 'react-native';
import { TrilhoEngine, LANE_X } from '../game/engine';
import { Skin } from '../game/skins';
import { colors } from '../theme';

interface GameWorldProps {
  engine: TrilhoEngine;
  skin: Skin;
}

const { width: SCREEN_W, height: SCREEN_H } = Dimensions.get('window');
const SCALE_X = SCREEN_W / TrilhoEngine.SCREEN_WIDTH;
const SCALE_Y = SCREEN_H / TrilhoEngine.SCREEN_HEIGHT;

export const GameWorld: React.FC<GameWorldProps> = ({ engine, skin }) => {
  const playerScreenX = engine.playerX * SCALE_X;
  const playerBaseY = (TrilhoEngine.PLAYER_BASE_Y - engine.jumpHeightOffset) * SCALE_Y;
  const isSliding = engine.isSliding;

  const playerH = (isSliding ? 30 : 64) * SCALE_Y;
  const playerW = (isSliding ? 60 : 44) * SCALE_X;

  const isInvuln = engine.crystalInvulnTicks > 0 || engine.boostTicksRemaining > 0 || engine.graceInvulnTicks > 0;

  // Track sleepers (wooden crossties)
  const sleepers = [];
  const sleeperSpacing = 80 * SCALE_Y;
  const offset = (engine.trackSleeperOffset * SCALE_Y) % sleeperSpacing;
  for (let y = -sleeperSpacing + offset; y < SCREEN_H + sleeperSpacing; y += sleeperSpacing) {
    sleepers.push(
      <View
        key={`tie-${y.toFixed(1)}`}
        style={[
          styles.sleeper,
          {
            top: y,
            left: 50 * SCALE_X,
            width: SCREEN_W - 100 * SCALE_X,
          },
        ]}
      />
    );
  }

  return (
    <View style={styles.container}>
      {/* 1. Track Sleepers */}
      {sleepers}

      {/* 2. Steel Rails */}
      <View style={[styles.rail, { left: (LANE_X.LEFT - 60) * SCALE_X }]} />
      <View style={[styles.rail, { left: (LANE_X.LEFT + 60) * SCALE_X }]} />
      <View style={[styles.rail, { left: (LANE_X.CENTER - 60) * SCALE_X }]} />
      <View style={[styles.rail, { left: (LANE_X.CENTER + 60) * SCALE_X }]} />
      <View style={[styles.rail, { left: (LANE_X.RIGHT - 60) * SCALE_X }]} />
      <View style={[styles.rail, { left: (LANE_X.RIGHT + 60) * SCALE_X }]} />

      {/* 3. Obstacles */}
      {engine.activeObstacles.map((obs) => {
        if (!obs.active) return null;
        const ox = LANE_X[obs.lane] * SCALE_X;
        const oy = (obs.y - obs.height) * SCALE_Y;
        const ow = (obs.type === 'DOUBLE_BARRIER' ? 240 : 120) * SCALE_X;
        const oh = obs.height * SCALE_Y;

        let bgColor = '#FF4D5E';
        let label = '⚠️';
        let barStyle: StyleProp<ViewStyle> = styles.barrierLow;

        if (obs.type === 'BARRIER_LOW') {
          bgColor = '#FF7A18';
          label = '⬆️ SALTO';
          barStyle = styles.barrierLow;
        } else if (obs.type === 'BARRIER_HIGH') {
          bgColor = '#E11D48';
          label = '⬇️ DESLIZE';
          barStyle = styles.barrierHigh;
        } else if (obs.type === 'TRAIN') {
          bgColor = '#2563EB';
          label = '🚆 TREM';
          barStyle = styles.train;
        } else if (obs.type === 'DOUBLE_BARRIER') {
          bgColor = '#9333EA';
          label = '⛔ BLOQUEIO';
          barStyle = styles.doubleBarrier;
        }

        return (
          <View
            key={`obs-${obs.id}`}
            style={[
              styles.obstacleCommon,
              barStyle,
              {
                left: ox - ow / 2,
                top: oy,
                width: ow,
                height: oh,
                backgroundColor: bgColor,
              },
            ]}
          >
            <Text style={styles.obstacleLabel}>{label}</Text>
          </View>
        );
      })}

      {/* 4. Pickups */}
      {engine.activePickups.map((item) => {
        if (!item.active) return null;
        const px = item.x * SCALE_X;
        const py = item.y * SCALE_Y;
        const size = 32 * SCALE_X;

        let icon = '🪙';
        let borderColor = colors.gold;
        let bg = 'rgba(255, 213, 74, 0.2)';

        if (item.type === 'CRYSTAL') {
          icon = '💎';
          borderColor = colors.cyan;
          bg = 'rgba(56, 225, 255, 0.3)';
        } else if (item.type === 'POWERUP_MAGNET') {
          icon = '🧲';
          borderColor = colors.cyan;
          bg = 'rgba(56, 225, 255, 0.4)';
        } else if (item.type === 'POWERUP_SHIELD') {
          icon = '🛡️';
          borderColor = colors.green;
          bg = 'rgba(34, 227, 139, 0.4)';
        } else if (item.type === 'POWERUP_BOOST') {
          icon = '⚡';
          borderColor = colors.red;
          bg = 'rgba(255, 77, 94, 0.4)';
        }

        return (
          <View
            key={`pickup-${item.id}`}
            style={[
              styles.pickupCircle,
              {
                left: px - size / 2,
                top: py - size / 2,
                width: size,
                height: size,
                borderColor,
                backgroundColor: bg,
              },
            ]}
          >
            <Text style={styles.pickupEmoji}>{icon}</Text>
          </View>
        );
      })}

      {/* 5. Particles */}
      {engine.particles.map((p, idx) => (
        <View
          key={`part-${idx}`}
          style={[
            styles.particle,
            {
              left: p.x * SCALE_X,
              top: p.y * SCALE_Y,
              width: p.size * SCALE_X,
              height: p.size * SCALE_X,
              backgroundColor: p.color,
              opacity: p.life / p.maxLife,
            },
          ]}
        />
      ))}

      {/* 6. Player Runner */}
      <View
        style={[
          styles.player,
          {
            left: playerScreenX - playerW / 2,
            top: playerBaseY - playerH,
            width: playerW,
            height: playerH,
            backgroundColor: skin.color,
            borderColor: isInvuln ? colors.cyan : skin.secondaryColor,
            shadowColor: skin.glowColor,
          },
        ]}
      >
        {/* Runner Head/Glow */}
        <View style={[styles.playerHead, { backgroundColor: skin.secondaryColor }]} />
        {/* Active Power-up Auras */}
        {engine.hasShield && <View style={[styles.shieldAura, { borderColor: colors.green }]} />}
        {isInvuln && <View style={[styles.shieldAura, { borderColor: colors.cyan }]} />}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFillObject,
    overflow: 'hidden',
    backgroundColor: colors.bg0,
  },
  sleeper: {
    position: 'absolute',
    height: 12,
    backgroundColor: '#1E293B',
    borderRadius: 3,
    borderWidth: 1,
    borderColor: '#334155',
  },
  rail: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    width: 5,
    backgroundColor: '#94A3B8',
    opacity: 0.7,
  },
  obstacleCommon: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    borderWidth: 2,
    borderColor: '#FFFFFF',
    elevation: 4,
  },
  barrierLow: {
    borderStyle: 'dashed',
  },
  barrierHigh: {
    borderBottomWidth: 0,
  },
  train: {
    borderRadius: 12,
    borderWidth: 3,
  },
  doubleBarrier: {
    borderWidth: 3,
  },
  obstacleLabel: {
    color: '#FFFFFF',
    fontSize: 10,
    fontWeight: 'bold',
    textShadowColor: 'rgba(0,0,0,0.8)',
    textShadowOffset: { width: 1, height: 1 },
    textShadowRadius: 2,
  },
  pickupCircle: {
    position: 'absolute',
    borderRadius: 999,
    borderWidth: 2,
    justifyContent: 'center',
    alignItems: 'center',
  },
  pickupEmoji: {
    fontSize: 16,
  },
  particle: {
    position: 'absolute',
    borderRadius: 999,
  },
  player: {
    position: 'absolute',
    borderRadius: 10,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    shadowOpacity: 0.8,
    shadowRadius: 10,
    elevation: 6,
  },
  playerHead: {
    width: '60%',
    height: '35%',
    borderRadius: 6,
    position: 'absolute',
    top: 4,
  },
  shieldAura: {
    ...StyleSheet.absoluteFillObject,
    borderRadius: 16,
    borderWidth: 3,
    margin: -6,
  },
});
