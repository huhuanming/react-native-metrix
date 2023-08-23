import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { start, onUpdate } from 'react-native-metrix';
import type { metrixUpdateInfo } from 'src/NativeMetrix';
import { getTimeSinceStartup } from '../../src';
import { STARTUP } from './startup';

export default function App() {
  const [result, setResult] = React.useState<metrixUpdateInfo>();
  React.useEffect(() => {
    STARTUP.FPTime = getTimeSinceStartup();
    start();
    onUpdate((info: metrixUpdateInfo) => {
      setResult(info);
    });
  }, []);

  return (
    <View style={styles.container}>
      <Text>loadJsBundleTime: {STARTUP.loadJsBundleTime}</Text>
      <Text>FPTime: {STARTUP.FPTime}</Text>
      <Text>jsFps: {result?.jsFps}</Text>
      <Text>uiFps: {result?.uiFps}</Text>
      <Text>usedCpu: {result?.usedCpu.toFixed(2)}%</Text>
      <Text>
        usedRam: {`${((result?.usedRam || 0) / 1024 / 1024).toFixed(2)} Mb`}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
