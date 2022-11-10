#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import "AudioSessionManager.h"


/**
 오디오 델리게이트
 */
@protocol AudioDelegate <NSObject>
@optional

/**
 에러 이벤트

 @param error 에러 내용
 */
-(void)onError:(NSError *)error;

/**
 진행 상태 값 이벤트

 @param progress 시간값
 */
-(void)onProgress:(float)progress;
@end

/**
 오디오 메니저
 */
@interface AudioMgr : NSObject

#define RECORD_DURATION 30

@property AVAudioPlayer *player;
@property AVPlayer *player2;
@property AVAudioRecorder *recorder;
@property CADisplayLink *link;

@property(nonatomic, weak) id<AudioDelegate> delegate;

/**
 오디오메니저(싱글턴)

 @return 오디오메니저 인스턴스
 */
+ (AudioMgr *)sharedObject;

//player
/**
 오디오 시작

 @param path 플레이할 파일경로
 @param delegate 이벤트 델리게이트
 */
-(void)onPlay:(NSURL *)path delegate:(id)delegate;

/**
 오디오 정지
 */
-(void)onStop;

/**
 오디오플레이 동작중인지 확인

 @return 플레이중일 경우 yes, 정지상태 no.
 */
-(BOOL)isPlaying;

/**
 일시정지 또는 재실행 토글.
 */
-(void)onPauseOrReplay;

//recorder

/**
 음성레코딩 시작

 @param delegate 이벤트 델리게이트
 */
-(void)onRecordStart:(id)delegate path:(NSString *)path channel:(int)channel sampleRate:(int)sampleRate float32F:(BOOL)float32F;

/**
 레코딩 중인지 확인

 @return 레코딩중일 경우 yes, 정지상태 no.
 */
-(BOOL)isRecording;

/**
 레코딩 정지
 */
-(void)onRecordStop;

/**
 레코딩 시간값. 초단위

 @return 레코딩 시간값
 */
-(NSInteger)getRecordCurrentTime;

@end
