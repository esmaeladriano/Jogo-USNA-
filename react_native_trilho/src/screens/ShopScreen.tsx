import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { useApp } from '../state/AppContext';
import { SKINS, Skin } from '../game/skins';
import { t } from '../i18n';
import { colors, radius } from '../theme';

interface ShopScreenProps {
  onBack: () => void;
}

export const ShopScreen: React.FC<ShopScreenProps> = ({ onBack }) => {
  const { progress, settings, buySkin, selectSkin } = useApp();
  const lang = settings.language;

  const handleAction = (skin: Skin) => {
    const isUnlocked = progress.unlockedSkinIds.includes(skin.id);
    if (isUnlocked) {
      selectSkin(skin.id);
    } else {
      buySkin(skin.id, skin.cost);
    }
  };

  return (
    <View style={styles.container}>
      {/* Top Bar */}
      <View style={styles.topBar}>
        <TouchableOpacity style={styles.backBtn} onPress={onBack}>
          <Text style={styles.backBtnText}>← {t(lang, 'back')}</Text>
        </TouchableOpacity>
        <View style={styles.balanceBadge}>
          <Text style={styles.balanceText}>💰 {progress.totalCoins}</Text>
        </View>
      </View>

      <Text style={styles.screenTitle}>{t(lang, 'shopTitle')}</Text>

      {/* Skins List */}
      <ScrollView contentContainerStyle={styles.listContent}>
        {SKINS.map((skin) => {
          const isUnlocked = progress.unlockedSkinIds.includes(skin.id);
          const isEquipped = progress.selectedSkinId === skin.id;
          const canAfford = progress.totalCoins >= skin.cost;
          const skinName = lang === 'en' ? skin.nameEn : skin.namePt;

          return (
            <View key={skin.id} style={[styles.skinCard, isEquipped && styles.skinCardEquipped]}>
              {/* Visual Skin Preview */}
              <View style={[styles.skinPreview, { backgroundColor: skin.color, borderColor: skin.glowColor }]}>
                <View style={[styles.skinPreviewHead, { backgroundColor: skin.secondaryColor }]} />
              </View>

              {/* Skin Info */}
              <View style={styles.skinInfo}>
                <Text style={styles.skinName}>{skinName}</Text>
                {!isUnlocked && (
                  <Text style={styles.skinCost}>🪙 {skin.cost}</Text>
                )}
                {isUnlocked && (
                  <Text style={styles.unlockedTag}>✓ Desbloqueado</Text>
                )}
              </View>

              {/* Action Button */}
              <TouchableOpacity
                style={[
                  styles.skinBtn,
                  isEquipped && styles.skinBtnEquipped,
                  !isUnlocked && !canAfford && styles.skinBtnDisabled,
                ]}
                disabled={isEquipped || (!isUnlocked && !canAfford)}
                onPress={() => handleAction(skin)}
                activeOpacity={0.8}
              >
                <Text style={styles.skinBtnText}>
                  {isEquipped
                    ? t(lang, 'equipped')
                    : isUnlocked
                    ? t(lang, 'equip')
                    : canAfford
                    ? `${t(lang, 'buy')} (${skin.cost})`
                    : t(lang, 'notEnough')}
                </Text>
              </TouchableOpacity>
            </View>
          );
        })}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.bg0,
    paddingTop: 50,
  },
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 16,
  },
  backBtn: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  backBtnText: {
    color: colors.cyan,
    fontSize: 16,
    fontWeight: '600',
  },
  balanceBadge: {
    backgroundColor: colors.card,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingHorizontal: 14,
    paddingVertical: 6,
  },
  balanceText: {
    color: colors.gold,
    fontSize: 15,
    fontWeight: 'bold',
  },
  screenTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: colors.text,
    paddingHorizontal: 24,
    marginBottom: 20,
  },
  listContent: {
    paddingHorizontal: 20,
    paddingBottom: 40,
    gap: 16,
  },
  skinCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    padding: 16,
    gap: 16,
  },
  skinCardEquipped: {
    borderColor: colors.accent,
  },
  skinPreview: {
    width: 52,
    height: 70,
    borderRadius: radius.md,
    borderWidth: 2,
    justifyContent: 'center',
    alignItems: 'center',
  },
  skinPreviewHead: {
    width: 26,
    height: 20,
    borderRadius: 4,
    position: 'absolute',
    top: 6,
  },
  skinInfo: {
    flex: 1,
  },
  skinName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: 4,
  },
  skinCost: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.gold,
  },
  unlockedTag: {
    fontSize: 12,
    color: colors.green,
    fontWeight: '500',
  },
  skinBtn: {
    backgroundColor: colors.accent,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: radius.md,
  },
  skinBtnEquipped: {
    backgroundColor: 'rgba(255, 122, 24, 0.2)',
    borderWidth: 1,
    borderColor: colors.accent,
  },
  skinBtnDisabled: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  skinBtnText: {
    color: '#FFF',
    fontSize: 13,
    fontWeight: 'bold',
  },
});
