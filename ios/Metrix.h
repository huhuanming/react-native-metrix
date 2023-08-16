#import <React/RCTEventEmitter.h>

#import "FPS.h"

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNMetrixSpec.h"

@interface Metrix : RCTEventEmitter <NativeMetrixSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Metrix : RCTEventEmitter <RCTBridgeModule>
#endif

@end
