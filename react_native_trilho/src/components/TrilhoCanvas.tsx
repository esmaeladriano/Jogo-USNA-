import React from 'react';
import { View, StyleSheet, Dimensions } from 'react-native';
import { TrilhoEngine } from '../engine/TrilhoEngine';
import { LANE_X, Obstacle, Pickup, Particle } from '../types';

interface TrilhoCanvasProps {
  engine: TrilhoEngine;
}

const { width: SCREEN_W, height: SCREEN_H } = Dimensions.get('window');
const SCALE_X = SCREEN_W / 720;
const SCALE_Y = SCREEN_H / 1280;

export const TrilhoCanvas: React.FC<TrilhoCanvasProps> = ({ engine }) => {
  const playerScreenX = engine.playerX * SCALE_X;
  const playerScreenY = (TrilhoEngine.PLAYER_BASE_Y - engine.jumpHeightOffset) * SCALE_Y;

  return (
    <View style={styles.container}>
      {/* 1. Track Rails */}
      <View style={[styles.rail, { left: LANE_X.LEFT * SCALE_X - 1 }]} />
      <View style={[styles.rail, { left: LANE_X.CENTER * SCALE_X - 1 }]} />
      <View style={[styles.rail, { left: LANE_X.RIGHT * SCALE_X - 1 }]} />

      {/* 2. Obstacles */}
      {engine.obstacles.map(obs => {
        if (!obs.active) return null;
        const obsX = LANE_X[obs.lane] * SCALE_X;
        const obsY = obs.y * SCALE_Y;
        const obsW = obs.width * SCALE_X;
        const obsH = obs.height * SCALE_Y;

        let bgColor = '#EF4444';
        let borderColor = '#FCA5A5';
        if (obs.type === 'TRAIN') {
          bgColor = '#DC2626';
          borderColor = '#991B1B';
        } else if (obs.type === 'HIGH_BARRIER') {
          bgColor = '#F97316';
          borderColor = '#FDBA74';
        }

        return (
          <View
            key={`obs-${obs.id}`}
            style={[
              styles.obstacle,
              {
                left: obsX - obsW / 2,
                top: obsY - obsH / 2,
                width: obsW,
                height: obsH,
                backgroundColor: bgColor,
                borderColor: borderColor
              }
            ]}
          />
        );
      })}

      {/* 3. Pickups */}
      {engine.pickups.map(p => {
        if (!p.active) return null;
        const pickX = LANE_X[p.lane] * SCALE_X;
        const pickY = p.y * SCALE_Y;

        let color = '#FACC15';
        if (p.type === 'CRYSTAL') color = '#38BDF8';
        else if (p.type === 'SHIELD') color = '#3B82F6';
        else if (p.type === 'BOOST') color = '#F97316';
        else if (p.type === 'MAGNET') color = '#A855F7';

        return (
          <View
            key={`pick-${p.id}`}
            style={[
              styles.pickup,
              {
                left: pickX - 14,
                top: pickY - 14,
                backgroundColor: color,
                borderRadius: p.type === 'CRYSTAL' ? 4 : 14
              }
            ]}
          />
        );
      })}

      {/* 4. Particles */}
      {engine.particles.map((pt, idx) => (
        <View
          key={`pt-${idx}`}
          style={[
            styles.particle,
            {
              left: pt.x * SCALE_X - pt.size / 2,
              top: pt.y * SCALE_Y - pt.size / 2,
              width: pt.size,
              height: pt.size,
              backgroundColor: pt.color,
              opacity: pt.life / pt.maxLife
            }
          ]}
        />
      ))}

      {/* 5. Player Runner */}
      <View
        style={[
          styles.player,
          {
            left: playerScreenX - (engine.isSliding ? 28 : 22),
            top: playerScreenY - (engine.isSliding ? 14 : 35),
            width: engine.isSliding ? 56 : 44,
            height: engine.isSliding ? 28 : 70,
            backgroundColor: engine.crystalInvulnRemaining > 0 ? '#38BDF8' : '#F97316'
          }
        ]}
      >
        {engine.hasShield && <View style={styles.shieldRing} />}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F1726'
  },
  rail: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    width: 3,
    backgroundColor: 'rgba(255, 255, 255, 0.15)'
  },
  obstacle: {
    position: 'absolute',
    borderRadius: 8,
    borderWidth: 2
  },
  pickup: {
    position: 'absolute',
    width: 28,
    height: 28,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 4
  },
  player: {
    position: 'absolute',
    borderRadius: 12,
    borderWidth: 2,
    borderColor: '#FFFFFF',
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 6
  },
  shieldRing: {
    ...StyleSheet.absoluteFillObject,
    borderRadius: 16,
    borderWidth: 3,
    borderColor: '#60A5FA',
    margin: -6
  },
  particle: {
    position: 'absolute',
    borderRadius: 4
  }
});
