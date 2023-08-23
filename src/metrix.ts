import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type { onMetrixUpdateCallback, metrixUpdateInfo } from './NativeMetrix';

const LINKING_ERROR =
  `The package 'react-native-metrix' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const MetrixModule = isTurboModuleEnabled
  ? require('./NativeMetrix').default
  : NativeModules.Metrix;

const Metrix = MetrixModule
  ? MetrixModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function start() {
  Metrix.start();
}

export function stop() {
  Metrix.stop();
}

export function onUpdate(callback: onMetrixUpdateCallback) {
  const eventEmitter = new NativeEventEmitter(Metrix);
  const subscription = eventEmitter.addListener(
    'metrixUpdate',
    (info: metrixUpdateInfo) => {
      if (Platform.OS === 'android') {
        info.usedRam = (info.usedRam || 0) * 1024;
      }
      callback(info);
    }
  );
  return () => {
    if (subscription) {
      subscription.remove();
    }
  };
}

export function getTimeSinceStartup(): number {
  return Metrix.getTimeSinceStartup();
}

export type { metrixUpdateInfo };
