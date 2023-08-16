#import <Foundation/Foundation.h>

#ifndef FPS_h
#define FPS_h

@interface FPS : NSObject
    @property (nonatomic, assign, readonly) NSUInteger FPS;
    @property (nonatomic, assign, readonly) NSUInteger maxFPS;
    @property (nonatomic, assign, readonly) NSUInteger minFPS;

    - (void)onUpdate:(NSTimeInterval)timestamp;
@end

#endif
