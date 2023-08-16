package com.metrix;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Promise;

abstract class MetrixSpec extends ReactContextBaseJavaModule {
  MetrixSpec(ReactApplicationContext context) {
    super(context);
  }

  public abstract void start();
  public abstract void stop();
}
