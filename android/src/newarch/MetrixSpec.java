package com.metrix;

import com.facebook.react.bridge.ReactApplicationContext;

abstract class MetrixSpec extends NativeMetrixSpec {
  MetrixSpec(ReactApplicationContext context) {
    super(context);
  }
}
