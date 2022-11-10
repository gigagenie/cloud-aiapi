
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface TtsPlayManager : NSObject
+(instancetype) sharedInstance;
-(void) addWavData:(NSData*)wavData;
-(void) removeAll;
@end

NS_ASSUME_NONNULL_END
