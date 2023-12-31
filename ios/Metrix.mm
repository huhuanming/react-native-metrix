#import "Metrix.h"

#import <sys/sysctl.h>
#import <mach/mach.h>
#import <QuartzCore/QuartzCore.h>
#import <React/RCTBridge+Private.h>
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>

// NOTICE: Mainly copied from here: https://github.com/facebook/react-native/blob/main/React/CoreModules/RCTPerfMonitor.mm
#pragma Resource usage methods
static vm_size_t RCTGetResidentMemorySize(void)
{
  vm_size_t memoryUsageInByte = 0;
  task_vm_info_data_t vmInfo;
  mach_msg_type_number_t count = TASK_VM_INFO_COUNT;
  kern_return_t kernelReturn = task_info(mach_task_self(), TASK_VM_INFO, (task_info_t)&vmInfo, &count);
  if (kernelReturn == KERN_SUCCESS) {
    memoryUsageInByte = (vm_size_t)vmInfo.phys_footprint;
  }
  return memoryUsageInByte;
}

// https://stackoverflow.com/a/8382889/3668241
float cpu_usage()
{
    kern_return_t kr;
    task_info_data_t tinfo;
    mach_msg_type_number_t task_info_count;

    task_info_count = TASK_INFO_MAX;
    kr = task_info(mach_task_self(), TASK_BASIC_INFO, (task_info_t)tinfo, &task_info_count);
    if (kr != KERN_SUCCESS) {
        return -1;
    }

    task_basic_info_t      basic_info;
    thread_array_t         thread_list;
    mach_msg_type_number_t thread_count;

    thread_info_data_t     thinfo;
    mach_msg_type_number_t thread_info_count;

    thread_basic_info_t basic_info_th;
    uint32_t stat_thread = 0; // Mach threads

    basic_info = (task_basic_info_t)tinfo;

    // get threads in the task
    kr = task_threads(mach_task_self(), &thread_list, &thread_count);
    if (kr != KERN_SUCCESS) {
        return -1;
    }
    if (thread_count > 0)
        stat_thread += thread_count;

    long tot_sec = 0;
    long tot_usec = 0;
    float tot_cpu = 0;
    int j;

    for (j = 0; j < (int)thread_count; j++)
    {
        thread_info_count = THREAD_INFO_MAX;
        kr = thread_info(thread_list[j], THREAD_BASIC_INFO,
                         (thread_info_t)thinfo, &thread_info_count);
        if (kr != KERN_SUCCESS) {
            return -1;
        }

        basic_info_th = (thread_basic_info_t)thinfo;

        if (!(basic_info_th->flags & TH_FLAGS_IDLE)) {
            tot_sec = tot_sec + basic_info_th->user_time.seconds + basic_info_th->system_time.seconds;
            tot_usec = tot_usec + basic_info_th->user_time.microseconds + basic_info_th->system_time.microseconds;
            tot_cpu = tot_cpu + basic_info_th->cpu_usage / (float)TH_USAGE_SCALE * 100.0;
        }

    } // for each thread

    kr = vm_deallocate(mach_task_self(), (vm_offset_t)thread_list, thread_count * sizeof(thread_t));
    assert(kr == KERN_SUCCESS);

    return tot_cpu;
}

@implementation Metrix {
    bool _isRunning;
    
    FPS *_uiFPSTracker;
    FPS *_jsFPSTracker;
    
    CADisplayLink *_uiDisplayLink;
    CADisplayLink *_jsDisplayLink;
}

RCT_EXPORT_MODULE()

static CFTimeInterval processStartTime() {
    size_t len = 4;
    int mib[len];
    struct kinfo_proc kp;
    
    sysctlnametomib("kern.proc.pid", mib, &len);
    mib[3] = getpid();
    len = sizeof(kp);
    sysctl(mib, 4, &kp, &len, NULL, 0);
    
    struct timeval startTime = kp.kp_proc.p_un.__p_starttime;
    
    CFTimeInterval absoluteTimeToRelativeTime =  CACurrentMediaTime() - [NSDate date].timeIntervalSince1970;
    return startTime.tv_sec + startTime.tv_usec / 1e6 + absoluteTimeToRelativeTime;
}

static CFTimeInterval startupTime;

+ (void)initialize {
    startupTime = processStartTime();
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[ @"metrixUpdate" ];
}

- (void)updateStats
{
    double mem = (double)RCTGetResidentMemorySize();
    float cpu = 0;
    cpu = cpu_usage();

    [self sendEventWithName:@"metrixUpdate" body:@{
        @"jsFps": [NSNumber numberWithUnsignedInteger:_jsFPSTracker.FPS],
        @"uiFps": [NSNumber numberWithUnsignedInteger:_uiFPSTracker.FPS],
        @"usedCpu": [NSNumber numberWithFloat:cpu],
        @"usedRam": [NSNumber numberWithDouble:mem]
    }];
    
    __weak __typeof__(self) weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
      __strong __typeof__(weakSelf) strongSelf = weakSelf;
      if (strongSelf && strongSelf->_isRunning) {
        [strongSelf updateStats];
      }
    });
}

- (void)threadUpdate:(CADisplayLink *)displayLink
{
  FPS *fps = displayLink == _jsDisplayLink ? _jsFPSTracker : _uiFPSTracker;
  [fps onUpdate:displayLink.timestamp];
}

RCT_EXPORT_METHOD(start)
{
    _isRunning = true;
    _uiFPSTracker= [[FPS alloc] init];
    _jsFPSTracker= [[FPS alloc] init];
    
    [self updateStats];
    
    // Get FPS for UI Thread
    _uiDisplayLink = [CADisplayLink displayLinkWithTarget:self selector:@selector(threadUpdate:)];
    [_uiDisplayLink addToRunLoop:[NSRunLoop mainRunLoop] forMode:NSRunLoopCommonModes];
    
    // Get FPS for JS thread
    [self.bridge
        dispatchBlock:^{
          self->_jsDisplayLink = [CADisplayLink displayLinkWithTarget:self selector:@selector(threadUpdate:)];
          [self->_jsDisplayLink addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
        }
                queue:RCTJSThread];
    
}

RCT_EXPORT_METHOD(stop)
{
    _isRunning = false;
    _jsFPSTracker = nil;
    _uiFPSTracker = nil;
    
    [_uiDisplayLink invalidate];
    [_jsDisplayLink invalidate];
    
    _uiDisplayLink = nil;
    _jsDisplayLink = nil;
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(getTimeSinceStartup)
{
    CFTimeInterval diff = CACurrentMediaTime() - startupTime;
    return [NSNumber numberWithDouble:ceil(diff * 1000)];
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeMetrixSpecJSI>(params);
}
#endif

@end
