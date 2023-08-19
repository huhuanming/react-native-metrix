import { AppRegistry } from 'react-native';
import App from './src/App';
import { STARTUP } from './src/startup';
import { name as appName } from './app.json';
import { getTimeSinceStartup } from '../src';

STARTUP.loadJsBundleTime = getTimeSinceStartup();
AppRegistry.registerComponent(appName, () => App);
