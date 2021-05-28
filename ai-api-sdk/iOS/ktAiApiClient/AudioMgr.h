#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import "AudioSessionManager.h"


@protocol AudioDelegate <NSObject>
@optional

-(void)onError:(NSError *)error;

-(void)onProgress:(float)progress;
@end


@interface AudioMgr : NSObject

#define RECORD_DURATION 30

@property AVAudioPlayer *player;
@property AVPlayer *player2;
@property AVAudioRecorder *recorder;
@property CADisplayLink *link;

@property(nonatomic, weak) id<AudioDelegate> delegate;

+ (AudioMgr *)sharedObject;

-(void)onPlay:(NSURL *)path delegate:(id)delegate;

-(void)onStop;

-(BOOL)isPlaying;

-(void)onPauseOrReplay;

-(void)onRecordStart:(id)delegate path:(NSString *)path channel:(int)channel sampleRate:(int)sampleRate float32F:(BOOL)float32F;

-(BOOL)isRecording;

-(void)onRecordStop;

-(NSInteger)getRecordCurrentTime;

@end
