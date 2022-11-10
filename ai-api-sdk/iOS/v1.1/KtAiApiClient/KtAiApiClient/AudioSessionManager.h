#import <Foundation/Foundation.h>


/**
 오디오 세션 메니저
 */
@interface AudioSessionManager : NSObject

/**
 오디오 세션 카테고리 설정

 @param categoryConstant 카테고리 값
 */
+ (void)setAudioSessionCategory: (NSString *)categoryConstant;

@end
