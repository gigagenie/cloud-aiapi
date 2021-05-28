#import "AudioSessionManager.h"
#import <AVFoundation/AVFoundation.h>

@implementation AudioSessionManager

+ (void)setAudioSessionCategory: (NSString *)categoryConstant
{
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    
    NSError *setCategoryError = nil;
    BOOL success = [audioSession setCategory:categoryConstant error:&setCategoryError];
    
    if (!success) { /* handle the error condition */
        NSLog(@"setCategoryError = %@", setCategoryError);
    }
    
    NSError *activationError = nil;
    success = [audioSession setActive:YES error:&activationError];
    
    if (!success) { /* handle the error condition */
        NSLog(@"activationError = %@", activationError);
    }
}

@end
