#import "AudioSessionManager.h"
#import <AVFoundation/AVFoundation.h>

@implementation AudioSessionManager

+ (void)setAudioSessionCategory: (NSString *)categoryConstant
{
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    
    // todo - check categoryConstant type, throw error if necessary
    
    NSError *setCategoryError = nil;
    BOOL success = [audioSession setCategory:categoryConstant error:&setCategoryError];
    
    // todo - handle error
    if (!success) { /* handle the error condition */
        NSLog(@"setCategoryError = %@", setCategoryError);
    }
    
    NSError *activationError = nil;
    success = [audioSession setActive:YES error:&activationError];
    
    // todo - handle error
    if (!success) { /* handle the error condition */
        NSLog(@"activationError = %@", activationError);
    }
}

@end
