import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal, ScrollView } from 'react-native';
import { useApp } from '../state/AppContext';
import { t } from '../i18n';
import { colors, radius } from '../theme';

interface MenuScreenProps {
  onPlay: () => void;
  onShop: () => void;
  onSettings: () => void;
}

export const MenuScreen: React.FC<MenuScreenProps> = ({ onPlay, onShop, onSettings }) => {
  const { progress, settings } = useApp();
  const lang = settings.language;
  const [showHowTo, setShowHowTo] = useState(false);

  return (
    <View style={styles.container}>
      {/* Header / Title */}
      <View style={styles.header}>
        <Text style={styles.title}>TRILHO</Text>
        <Text style={styles.subtitle}>{t(lang, 'tagline')}</Text>
      </View>

      {/* Stats Card */}
      <View style={styles.statsCard}>
        <View style={styles.statItem}>
          <Text style={styles.statLabel}>{t(lang, 'best')}</Text>
          <Text style={styles.statValue}>{progress.bestDistance} m</Text>
        </View>
        <View style={styles.statDivider} />
        <View style={styles.statItem}>
          <Text style={styles.statLabel}>{t(lang, 'coins')}</Text>
          <Text style={[styles.statValue, { color: colors.gold }]}>💰 {progress.totalCoins}</Text>
        </View>
        <View style={styles.statDivider} />
        <View style={styles.statItem}>
          <Text style={styles.statLabel}>{t(lang, 'runs')}</Text>
          <Text style={styles.statValue}>{progress.totalRuns}</Text>
        </View>
      </View>

      {/* Action Buttons */}
      <View style={styles.actions}>
        <TouchableOpacity style={styles.playButton} onPress={onPlay} activeOpacity={0.85}>
          <Text style={styles.playButtonText}>▶ {t(lang, 'play')}</Text>
        </TouchableOpacity>

        <View style={styles.rowButtons}>
          <TouchableOpacity style={styles.secondaryButton} onPress={onShop} activeOpacity={0.8}>
            <Text style={styles.secondaryButtonText}>🛍️ {t(lang, 'shop')}</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.secondaryButton} onPress={onSettings} activeOpacity={0.8}>
            <Text style={styles.secondaryButtonText}>⚙️ {t(lang, 'settings')}</Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity style={styles.tutorialButton} onPress={() => setShowHowTo(true)} activeOpacity={0.8}>
          <Text style={styles.tutorialButtonText}>❓ {t(lang, 'howToPlay')}</Text>
        </TouchableOpacity>
      </View>

      {/* How to Play Modal */}
      <Modal visible={showHowTo} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>{t(lang, 'howToPlay')}</Text>
            <ScrollView style={styles.modalScroll}>
              <Text style={styles.tipText}>↔️ {t(lang, 'tut1')}</Text>
              <Text style={styles.tipText}>⬆️ {t(lang, 'tut2')}</Text>
              <Text style={styles.tipText}>⬇️ {t(lang, 'tut3')}</Text>
              <Text style={styles.tipText}>💎 {t(lang, 'tut4')}</Text>
              <Text style={styles.tipText}>⚡ {t(lang, 'tut5')}</Text>
            </ScrollView>
            <TouchableOpacity style={styles.closeButton} onPress={() => setShowHowTo(false)}>
              <Text style={styles.closeButtonText}>{t(lang, 'back')}</Text>
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
    justifyContent: 'space-between',
    paddingVertical: 60,
    paddingHorizontal: 24,
  },
  header: {
    alignItems: 'center',
    marginTop: 20,
  },
  title: {
    fontSize: 54,
    fontWeight: '900',
    color: colors.accent,
    letterSpacing: 4,
    textShadowColor: 'rgba(255, 122, 24, 0.5)',
    textShadowOffset: { width: 0, height: 4 },
    textShadowRadius: 16,
  },
  subtitle: {
    fontSize: 16,
    color: colors.muted,
    marginTop: 8,
    letterSpacing: 1,
  },
  statsCard: {
    flexDirection: 'row',
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingVertical: 18,
    paddingHorizontal: 12,
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  statItem: {
    alignItems: 'center',
    flex: 1,
  },
  statLabel: {
    fontSize: 12,
    color: colors.muted,
    textTransform: 'uppercase',
    marginBottom: 4,
  },
  statValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: colors.text,
  },
  statDivider: {
    width: 1,
    height: 30,
    backgroundColor: colors.cardBorder,
  },
  actions: {
    gap: 16,
  },
  playButton: {
    backgroundColor: colors.accent,
    borderRadius: radius.pill,
    paddingVertical: 20,
    alignItems: 'center',
    shadowColor: colors.accent,
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.5,
    shadowRadius: 12,
    elevation: 8,
  },
  playButtonText: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#FFFFFF',
    letterSpacing: 2,
  },
  rowButtons: {
    flexDirection: 'row',
    gap: 12,
  },
  secondaryButton: {
    flex: 1,
    backgroundColor: colors.card,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingVertical: 14,
    alignItems: 'center',
  },
  secondaryButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
  },
  tutorialButton: {
    alignItems: 'center',
    paddingVertical: 10,
  },
  tutorialButtonText: {
    fontSize: 14,
    color: colors.cyan,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.8)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  modalCard: {
    backgroundColor: colors.bg1,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    padding: 24,
    width: '100%',
    maxHeight: '80%',
  },
  modalTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: 16,
    textAlign: 'center',
  },
  modalScroll: {
    marginBottom: 20,
  },
  tipText: {
    fontSize: 15,
    color: colors.text,
    lineHeight: 24,
    marginBottom: 14,
  },
  closeButton: {
    backgroundColor: colors.accent,
    borderRadius: radius.md,
    paddingVertical: 12,
    alignItems: 'center',
  },
  closeButtonText: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
