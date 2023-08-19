package com.metrix;

import android.app.Activity;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;

public class MetrixModule extends MetrixSpec {
  public static final String NAME = "Metrix";
  private final PerformanceStatsImpl performanceStats;
  private final long startupTime;


  MetrixModule(ReactApplicationContext context, long startupTime) {
    super(context);
    this.startupTime = startupTime;
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
   performanceStats.stop();
  }

  @Override
  @ReactMethod(isBlockingSynchronousMethod = true)
  public Double getTimeSinceStartup() {
    double diff = SystemClock.uptimeMillis() - startupTime;
    return diff;
  }
}
