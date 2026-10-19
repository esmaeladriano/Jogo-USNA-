import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Switch } from 'react-native';
import { useApp } from '../state/AppContext';
import { t } from '../i18n';
import { colors, radius } from '../theme';

interface SettingsScreenProps {
  onBack: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ onBack }) => {
  const { settings, updateSettings } = useApp();
  const lang = settings.language;

  return (
    <View style={styles.container}>
      {/* Top Bar */}
      <View style={styles.topBar}>
        <TouchableOpacity style={styles.backBtn} onPress={onBack}>
          <Text style={styles.backBtnText}>← {t(lang, 'back')}</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.screenTitle}>{t(lang, 'settingsTitle')}</Text>

      <View style={styles.section}>
        {/* Language Selection */}
        <View style={styles.row}>
          <Text style={styles.rowLabel}>{t(lang, 'language')}</Text>
          <View style={styles.langSelector}>
            <TouchableOpacity
              style={[styles.langChip, lang === 'pt' && styles.langChipActive]}
              onPress={() => updateSettings({ language: 'pt' })}
            >
              <Text style={[styles.langChipText, lang === 'pt' && styles.langChipTextActive]}>PT</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.langChip, lang === 'en' && styles.langChipActive]}
              onPress={() => updateSettings({ language: 'en' })}
            >
              <Text style={[styles.langChipText, lang === 'en' && styles.langChipTextActive]}>EN</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Vibration Toggle */}
        <View style={styles.row}>
          <Text style={styles.rowLabel}>{t(lang, 'vibration')}</Text>
          <Switch
            value={settings.vibration}
            onValueChange={(val) => updateSettings({ vibration: val })}
            trackColor={{ false: '#334155', true: colors.accent }}
            thumbColor={'#FFFFFF'}
          />
        </View>

        {/* SFX Toggle */}
        <View style={styles.row}>
          <Text style={styles.rowLabel}>Efeitos Sonoros</Text>
          <Switch
            value={settings.soundVolume > 0}
            onValueChange={(val) => updateSettings({ soundVolume: val ? 0.8 : 0 })}
            trackColor={{ false: '#334155', true: colors.accent }}
            thumbColor={'#FFFFFF'}
          />
        </View>

        {/* Music Toggle */}
        <View style={styles.row}>
          <Text style={styles.rowLabel}>Música de Fundo</Text>
          <Switch
            value={settings.musicVolume > 0}
            onValueChange={(val) => updateSettings({ musicVolume: val ? 0.5 : 0 })}
            trackColor={{ false: '#334155', true: colors.accent }}
            thumbColor={'#FFFFFF'}
          />
        </View>
      </View>
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
  screenTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: colors.text,
    paddingHorizontal: 24,
    marginBottom: 24,
  },
  section: {
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    marginHorizontal: 20,
    paddingVertical: 8,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: colors.cardBorder,
  },
  rowLabel: {
    fontSize: 16,
    color: colors.text,
    fontWeight: '500',
  },
  langSelector: {
    flexDirection: 'row',
    gap: 8,
  },
  langChip: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: radius.pill,
    backgroundColor: 'rgba(255,255,255,0.08)',
  },
  langChipActive: {
    backgroundColor: colors.accent,
  },
  langChipText: {
    color: colors.muted,
    fontSize: 14,
    fontWeight: 'bold',
  },
  langChipTextActive: {
    color: '#FFF',
  },
});
