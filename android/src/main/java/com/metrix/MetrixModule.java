package com.metrix;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;

public class MetrixModule extends MetrixSpec {
  public static final String NAME = "Metrix";
  private final PerformanceStatsImpl performanceStats;

  MetrixModule(ReactApplicationContext context) {
    super(context);
    performanceStats = new PerformanceStatsImpl(context);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  @Override
  @ReactMethod
  public void start() {
   performanceStats.start();
  }

  @Override
  @ReactMethod
  public void stop() {
   performanceStats.start();
  }
}
