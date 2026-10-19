import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal } from 'react-native';

interface GameOverModalProps {
  visible: boolean;
  distanceMeters: number;
  coinsEarned: number;
  totalCoins: number;
  onPlayAgain: () => void;
  onMainMenu: () => void;
}

export const GameOverModal: React.FC<GameOverModalProps> = ({
  visible,
  distanceMeters,
  coinsEarned,
  totalCoins,
  onPlayAgain,
  onMainMenu
}) => {
  const [countdown, setCountdown] = useState(10);

  useEffect(() => {
    if (!visible) return;
    setCountdown(10);
    const interval = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(interval);
          onPlayAgain();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [visible]);

  if (!visible) return null;

  return (
    <Modal transparent animationType="fade" visible={visible}>
      <View style={styles.overlay}>
        <View style={styles.dialog}>
          <Text style={styles.title}>💥 FIM DE LINHA</Text>

          {/* Stats Box */}
          <View style={styles.statsCard}>
            <View style={styles.row}>
              <Text style={styles.label}>🏃 Distância</Text>
              <Text style={styles.val}>{distanceMeters} m</Text>
            </View>
            <View style={styles.row}>
              <Text style={styles.label}>🪙 Nesta corrida</Text>
              <Text style={[styles.val, { color: '#FFD700' }]}>+{coinsEarned}</Text>
            </View>
            <View style={styles.divider} />
            <View style={styles.row}>
              <Text style={[styles.label, { color: '#38E1FF', fontWeight: 'bold' }]}>💰 Total moedas</Text>
              <Text style={[styles.val, { color: '#FFD700', fontSize: 18 }]}>{totalCoins} 🪙</Text>
            </View>
          </View>

          {/* Restart prompt */}
          <Text style={styles.question}>Recomeçar?</Text>

          {/* 10s Timer */}
          <View style={styles.timerBarContainer}>
            <View style={[styles.timerBarFill, { width: `${(countdown / 10) * 100}%` }]} />
          </View>
          <Text style={styles.timerText}>⏱️ Reinicia automaticamente em {countdown}s</Text>

          {/* Action buttons */}
          <TouchableOpacity style={styles.btnPlayAgain} onPress={onPlayAgain}>
            <Text style={styles.btnPlayText}>▶ Recomeçar ({countdown}s)</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.btnMenu} onPress={onMainMenu}>
            <Text style={styles.btnMenuText}>🏠 Menu</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(5, 10, 20, 0.85)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20
  },
  dialog: {
    width: '100%',
    maxWidth: 380,
    backgroundColor: '#141D2B',
    borderRadius: 24,
    borderWidth: 2,
    borderColor: '#FF5252',
    padding: 22,
    alignItems: 'center'
  },
  title: {
    fontSize: 22,
    fontWeight: '900',
    color: '#FF5252',
    marginBottom: 12
  },
  statsCard: {
    width: '100%',
    backgroundColor: '#0F1622',
    borderRadius: 16,
    padding: 14,
    marginBottom: 14
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 4
  },
  label: {
    color: '#90A4AE',
    fontSize: 14
  },
  val: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 15
  },
  divider: {
    height: 1,
    backgroundColor: 'rgba(255,255,255,0.1)',
    marginVertical: 6
  },
  question: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 6
  },
  timerBarContainer: {
    width: '100%',
    height: 6,
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 3,
    overflow: 'hidden',
    marginBottom: 4
  },
  timerBarFill: {
    height: '100%',
    backgroundColor: '#00E676'
  },
  timerText: {
    color: '#B0BEC5',
    fontSize: 12,
    marginBottom: 16
  },
  btnPlayAgain: {
    width: '100%',
    height: 50,
    backgroundColor: '#00E676',
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 10
  },
  btnPlayText: {
    color: '#092410',
    fontWeight: '900',
    fontSize: 16
  },
  btnMenu: {
    width: '100%',
    height: 44,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.3)',
    justifyContent: 'center',
    alignItems: 'center'
  },
  btnMenuText: {
    color: '#FFFFFF',
    fontSize: 14
  }
});
