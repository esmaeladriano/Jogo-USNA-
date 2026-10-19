import React, { createContext, useContext, useEffect, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Language } from '../i18n';

export interface ProgressState {
  totalCoins: number;
  bestDistance: number;
  totalRuns: number;
  selectedSkinId: string;
  unlockedSkinIds: string[];
}

export interface SettingsState {
  soundVolume: number;
  musicVolume: number;
  vibration: boolean;
  language: Language;
}

interface AppContextValue {
  loaded: boolean;
  progress: ProgressState;
  settings: SettingsState;
  recordRun: (distance: number, coins: number) => { isNewRecord: boolean; newTotalCoins: number };
  buySkin: (skinId: string, cost: number) => boolean;
  selectSkin: (skinId: string) => void;
  updateSettings: (partial: Partial<SettingsState>) => void;
}

const DEFAULT_PROGRESS: ProgressState = {
  totalCoins: 0,
  bestDistance: 0,
  totalRuns: 0,
  selectedSkinId: 'solaris',
  unlockedSkinIds: ['solaris'],
};

const DEFAULT_SETTINGS: SettingsState = {
  soundVolume: 0.8,
  musicVolume: 0.5,
  vibration: true,
  language: 'pt',
};

const STORAGE_KEY_PROGRESS = '@trilho_progress_v2';
const STORAGE_KEY_SETTINGS = '@trilho_settings_v2';

const AppContext = createContext<AppContextValue | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [loaded, setLoaded] = useState(false);
  const [progress, setProgress] = useState<ProgressState>(DEFAULT_PROGRESS);
  const [settings, setSettings] = useState<SettingsState>(DEFAULT_SETTINGS);

  useEffect(() => {
    (async () => {
      try {
        const [savedProgress, savedSettings] = await Promise.all([
          AsyncStorage.getItem(STORAGE_KEY_PROGRESS),
          AsyncStorage.getItem(STORAGE_KEY_SETTINGS),
        ]);
        if (savedProgress) {
          setProgress({ ...DEFAULT_PROGRESS, ...JSON.parse(savedProgress) });
        }
        if (savedSettings) {
          setSettings({ ...DEFAULT_SETTINGS, ...JSON.parse(savedSettings) });
        }
      } catch {
        // Fallback to defaults
      } finally {
        setLoaded(true);
      }
    })();
  }, []);

  const saveProgress = async (next: ProgressState) => {
    setProgress(next);
    try {
      await AsyncStorage.setItem(STORAGE_KEY_PROGRESS, JSON.stringify(next));
    } catch {
      // Ignored
    }
  };

  const saveSettings = async (next: SettingsState) => {
    setSettings(next);
    try {
      await AsyncStorage.setItem(STORAGE_KEY_SETTINGS, JSON.stringify(next));
    } catch {
      // Ignored
    }
  };

  const recordRun = (distance: number, coins: number) => {
    const isNewRecord = distance > progress.bestDistance;
    const nextBest = Math.max(progress.bestDistance, distance);
    const nextTotalCoins = progress.totalCoins + coins;
    const nextRuns = progress.totalRuns + 1;

    const next: ProgressState = {
      ...progress,
      bestDistance: nextBest,
      totalCoins: nextTotalCoins,
      totalRuns: nextRuns,
    };
    saveProgress(next);

    return { isNewRecord, newTotalCoins: nextTotalCoins };
  };

  const buySkin = (skinId: string, cost: number): boolean => {
    if (progress.totalCoins < cost || progress.unlockedSkinIds.includes(skinId)) {
      return false;
    }
    const next: ProgressState = {
      ...progress,
      totalCoins: progress.totalCoins - cost,
      unlockedSkinIds: [...progress.unlockedSkinIds, skinId],
      selectedSkinId: skinId,
    };
    saveProgress(next);
    return true;
  };

  const selectSkin = (skinId: string) => {
    if (progress.unlockedSkinIds.includes(skinId)) {
      saveProgress({ ...progress, selectedSkinId: skinId });
    }
  };

  const updateSettings = (partial: Partial<SettingsState>) => {
    const next: SettingsState = { ...settings, ...partial };
    saveSettings(next);
  };

  return (
    <AppContext.Provider
      value={{
        loaded,
        progress,
        settings,
        recordRun,
        buySkin,
        selectSkin,
        updateSettings,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export function useApp(): AppContextValue {
  const ctx = useContext(AppContext);
  if (!ctx) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return ctx;
}
