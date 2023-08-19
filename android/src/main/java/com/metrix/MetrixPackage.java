package com.metrix;

import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.TurboReactPackage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetrixPackage extends TurboReactPackage {
  private final long STARTUP_TIME = SystemClock.uptimeMillis();

  @Nullable
  @Override
  public NativeModule getModule(String name, ReactApplicationContext reactContext) {
    if (name.equals(MetrixModule.NAME)) {
      return new MetrixModule(reactContext, STARTUP_TIME);
    } else {
      return null;
    }
  }

  @Override
  public ReactModuleInfoProvider getReactModuleInfoProvider() {
    return () -> {
      final Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();
      boolean isTurboModule = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
      moduleInfos.put(
              MetrixModule.NAME,
              new ReactModuleInfo(
                      MetrixModule.NAME,
                      MetrixModule.NAME,
                      false, // canOverrideExistingModule
                      false, // needsEagerInit
                      true, // hasConstants
                      false, // isCxxModule
                      isTurboModule // isTurboModule
      ));
      return moduleInfos;
    };
  }
}
