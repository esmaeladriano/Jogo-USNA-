import React, { useState } from 'react';
import { View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AppProvider, useApp } from './src/state/AppContext';
import { getSkin } from './src/game/skins';
import { colors } from './src/theme';
import { MenuScreen } from './src/screens/MenuScreen';
import { GameScreen } from './src/screens/GameScreen';
import { ShopScreen } from './src/screens/ShopScreen';
import { SettingsScreen } from './src/screens/SettingsScreen';

type Screen = 'menu' | 'game' | 'shop' | 'settings';

export default function App() {
  return (
    <SafeAreaProvider>
      <AppProvider>
        <Root />
      </AppProvider>
    </SafeAreaProvider>
  );
}

function Root() {
  const { loaded, progress, recordRun } = useApp();
  const [screen, setScreen] = useState<Screen>('menu');

  if (!loaded) {
    return <View style={{ flex: 1, backgroundColor: colors.bg0 }} />;
  }

  return (
    <View style={{ flex: 1, backgroundColor: colors.bg0 }}>
      <StatusBar style="light" />

      {screen === 'menu' && (
        <MenuScreen
          onPlay={() => setScreen('game')}
          onShop={() => setScreen('shop')}
          onSettings={() => setScreen('settings')}
        />
      )}

      {screen === 'game' && (
        <GameScreen
          skin={getSkin(progress.selectedSkinId)}
          bestDistance={progress.bestDistance}
          onRunFinished={recordRun}
          onExit={() => setScreen('menu')}
        />
      )}

      {screen === 'shop' && <ShopScreen onBack={() => setScreen('menu')} />}
      {screen === 'settings' && <SettingsScreen onBack={() => setScreen('menu')} />}
    </View>
  );
}
