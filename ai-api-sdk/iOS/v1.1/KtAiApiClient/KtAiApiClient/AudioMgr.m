#import "AudioMgr.h"

@implementation AudioMgr
+ (AudioMgr *)sharedObject
{
    static AudioMgr *gInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        gInstance = [[AudioMgr alloc] init];
    });
    return gInstance;
}
-(void)onPlay:(NSURL *)path delegate:(id)delegate{
    if (_player != nil && _player.playing) {
        return;
    }
    if (_player != nil) {
        [self onPauseOrReplay];
        return;
    }
    
    _link = [CADisplayLink displayLinkWithTarget:self selector:@selector(playProgress)];
    _link.frameInterval = 25;
    [_link addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
    
    [AudioSessionManager setAudioSessionCategory:AVAudioSessionCategoryPlayback];
    NSError *error;
    NSData *data = [NSData dataWithContentsOfURL:path];
    _player = [[AVAudioPlayer alloc] initWithData:data error:&error];
    [_player setDelegate:delegate];
    [_player prepareToPlay];
    [_player play];
    
    if (error) {
        NSLog(@"error, %@", error);
        [_delegate onError:error];
    }else{
        NSLog(@"path, %@", path.absoluteString);
    }
    
}
-(void)onStop{
    if (_player != nil && _player.playing) {
        [_player stop];
    }
    _player = nil;
    [_link invalidate];
    _link = nil;
}

-(void)onPauseOrReplay{
    if (_player != nil && _player.playing) {
        [_player pause];
        [_link setPaused:YES];
        return;
    }else if (_player != nil){
        [_player play];
        [_link setPaused:NO];
    }
}

-(BOOL)isPlaying{
    if (_player) {
        return _player.isPlaying;
    }
    return NO;
}

-(void)onRecordStart:(id)delegate path:(NSString *)path channel:(int)channel sampleRate:(int)sampleRate float32F:(BOOL)float32F{
    _link = [CADisplayLink displayLinkWithTarget:self selector:@selector(updateProgress)];
    _link.frameInterval = 25;
    [_link addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
    
    [AudioSessionManager setAudioSessionCategory:AVAudioSessionCategoryRecord];
    
    NSMutableDictionary *recordSetting = [[NSMutableDictionary alloc] init];
    [recordSetting setValue:[NSNumber numberWithInt:kAudioFormatLinearPCM] forKey:AVFormatIDKey];
    [recordSetting setValue:[NSNumber numberWithFloat:sampleRate] forKey:AVSampleRateKey];
    [recordSetting setValue:[NSNumber numberWithInt:channel] forKey:AVNumberOfChannelsKey];
    [recordSetting setValue:[NSNumber numberWithInt:float32F] forKey:AVLinearPCMIsFloatKey];
    
    NSURL *url = [NSURL fileURLWithPath:path];
    
    NSLog(@"url = %@", url.absoluteString);
    
    NSError *error = nil;
    _recorder = [[ AVAudioRecorder alloc] initWithURL:url settings:recordSetting error:&error];
    
    _recorder.delegate = delegate;
    _recorder.meteringEnabled = YES;
    if ([_recorder prepareToRecord] == YES){
        [_recorder recordForDuration:RECORD_DURATION];
    }else {
        int errorCode = CFSwapInt32HostToBig ([error code]);
        NSLog(@"Error: %@" , error);
        [_delegate onError:error];
    }
}

-(void)updateProgress{
    [_delegate onProgress:_recorder.currentTime/RECORD_DURATION];
}

-(void)playProgress{
    [_delegate onProgress:(_player.currentTime / _player.duration)];
}

-(BOOL)isRecording{
    if (_recorder) {
        return _recorder.recording;
    }
    return NO;
}

-(NSInteger)getRecordCurrentTime{
    if (_recorder) {
        return _recorder.currentTime;
    }
    return 0;
}
-(void)onRecordStop{
    [_recorder stop];
    _recorder = nil;
    
    [_link invalidate];
    _link = nil;
}

@end
