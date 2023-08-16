
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNMetrixSpec.h"

@interface Metrix : NSObject <NativeMetrixSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Metrix : NSObject <RCTBridgeModule>
#endif

@end
