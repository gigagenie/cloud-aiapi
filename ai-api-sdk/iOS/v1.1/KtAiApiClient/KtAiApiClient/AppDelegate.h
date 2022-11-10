#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"
#import "Toast.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>
@property (strong, nonatomic) UIWindow *window;

/**
 메인 프로그레스바 보이기(일반)
 */
- (void) showProgress;

/**
 메인 프로그레스바 보이기(메시지형)

 @param message 메시지. nil일경우 일반.
 */
- (void) showProgress:(NSString *)message;

/**
 메인 프로그레스바 숨기기
 */
- (void) hideProgress;

-(void) makeToast:(NSString *)message;                                                          //화면 전환되도 토스트 보이게 하기 위해 추가
-(void) makeToast:(NSString *)message position:(id)position;                                    //위에꺼 extension
-(void) makeToast:(NSString *)message duration:(NSTimeInterval)duration position:(id)position;  //위에꺼 extension
-(void) makeToast:(NSString *)message duration:(NSTimeInterval)duration position:(id)position style:(CSToastStyle *)style;

@end

