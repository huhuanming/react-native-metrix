import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { start, onUpdate } from 'react-native-metrix';
import type { metrixUpdateInfo } from 'src/NativeMetrix';

export default function App() {
  const [result, setResult] = React.useState<metrixUpdateInfo>();

  React.useEffect(() => {
    start();
    onUpdate((info) => {
      setResult(info);
    });
  }, []);

  return (
    <View style={styles.container}>
      <Text>jsFps: {result?.jsFps}</Text>
      <Text>uiFps: {result?.uiFps}</Text>
      <Text>usedCpu: {result?.usedCpu.toFixed(2)}%</Text>
      <Text>
        usedRam: {`${((result?.usedRam || 0) / 1024 / 1024).toFixed(2)} Mb`}
      </Text>
      <Text>viewCount: {result?.viewCount}</Text>
      <Text>visibleViewCount: {result?.visibleViewCount}</Text>
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
