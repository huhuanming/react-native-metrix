package com.metrix;

import android.os.Debug;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.debug.FpsDebugFrameCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Most important impl details from: https://github.com/facebook/react-native/blob/main/ReactAndroid/src/main/java/com/facebook/react/devsupport/FpsView.java
public class PerformanceStatsImpl {

    private static final int UPDATE_INTERVAL_MS = 500;

    private final FpsDebugFrameCallback mFrameCallback;
    private final StatsMonitorRunnable mStatsMonitorRunnable;
    private final ReactContext reactContext;
    private Handler handler;
    private final String packageName;

    public PerformanceStatsImpl(ReactContext context) {
        mFrameCallback = new FpsDebugFrameCallback(context);
        mStatsMonitorRunnable = new StatsMonitorRunnable();
        reactContext = context;
        packageName = context.getPackageName();
    }

    public void start() {
        handler = new Handler();
        mFrameCallback.reset();
        mFrameCallback.start();
        mStatsMonitorRunnable.start();
    }

    public void stop() {
        handler = null;
        mFrameCallback.stop();
        mStatsMonitorRunnable.stop();
    }

    private void setCurrentStats(double uiFPS, double jsFPS, double usedRam, double usedCpu) {
        WritableMap state = Arguments.createMap();
        state.putDouble("uiFps", uiFPS);
        state.putDouble("jsFps", jsFPS);
        state.putDouble("usedRam", usedRam);
        state.putDouble("usedCpu", usedCpu);

        sendEvent(state);
    }

    private void sendEvent(@Nullable Object data) {
        if (reactContext == null) {
            return;
        }

        if (!reactContext.hasActiveReactInstance()) {
            return;
        }
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("metrixUpdate", data);
    }


    /** Timer that runs every UPDATE_INTERVAL_MS ms and updates the currently displayed FPS and resource usages. */
    private class StatsMonitorRunnable implements Runnable {

        private boolean mShouldStop = false;

        @Override
        public void run() {
            if (mShouldStop) {
                return;
            }
            // Collect FPS info
            double fps = mFrameCallback.getFPS();
            double jsFps = mFrameCallback.getJSFPS();

            // Collect system resource usage
            double cpuUsage = 0;
            try {
                cpuUsage = getUsedCPU();
            } catch (Exception e) {
            }
            double usedRam = getUsedRam();

            setCurrentStats(
                    fps,
                    jsFps,
                    usedRam,
                    cpuUsage
            );
            mFrameCallback.reset();

            // TODO: not sure if we need to run that on a view
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }

        public void start() {
            mShouldStop = false;
            handler.post(this);
        }

        public void stop() {
            mShouldStop = true;
        }

        // NOTE: may not be exactly the same as seen in Profiler, as graphics can't be retrieved.
        // Read here: https://developer.android.com/reference/android/os/Debug#getMemoryInfo(android.os.Debug.MemoryInfo)
        private double getUsedRam() {
            Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
            Debug.getMemoryInfo(memoryInfo);

            return memoryInfo.getTotalPss();
        }

        private double getUsedCPU() throws IOException {
            String[] commands = { "top", "-n", "1", "-q", "-oCMDLINE,%CPU", "-s2", "-b" };
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Runtime.getRuntime().exec(commands).getInputStream())
            );
            String line;
            double cpuUsage = 0;
            while ((line = reader.readLine()) != null) {
                if (!line.contains(packageName)) continue;
                line = line.replace(packageName, "").replaceAll(" ", "");
                cpuUsage = Double.parseDouble(line);
                break;
            }
            reader.close();
            return cpuUsage;
        }
    }
}
