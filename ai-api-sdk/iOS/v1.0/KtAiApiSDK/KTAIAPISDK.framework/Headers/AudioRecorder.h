#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#include <AudioToolbox/AudioToolbox.h>

#define kNumberBuffers 3

typedef struct AQRecorderState {
    AudioStreamBasicDescription mDataFormat;
    AudioQueueRef mQueue;
    AudioQueueBufferRef mBuffers[kNumberBuffers];
    AudioFileID mAudioFile;
    UInt32 bufferByteSize;
    SInt64 mCurrentPacket;
    bool mIsRunning;
    void *controller;
} AQRecorderState;

@protocol AudioRecorderDelegate

- (void) handleInputBuffer:(void *)inUserData length:(int)length;
- (void) audioRecorderStarted;
- (void) audioRecorderStopped;

@end

@interface AudioRecorder : NSObject {
    AQRecorderState aqData;
	id<AudioRecorderDelegate> delegate;
}

- (id) initWithDelegate:(id<AudioRecorderDelegate>)delegate;
- (BOOL) isRunning;
- (BOOL) recordStart:(Float64)samplerate channel:(UInt32)channel;
- (void) recordStop;

@end


