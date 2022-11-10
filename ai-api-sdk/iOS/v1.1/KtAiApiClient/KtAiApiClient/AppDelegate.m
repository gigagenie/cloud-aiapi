#import "AppDelegate.h"

@interface AppDelegate ()<MBProgressHUDDelegate>
@property MBProgressHUD *HUD;
@end

@implementation AppDelegate
@synthesize HUD;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    return YES;
}
- (void) showProgress {
    [self showProgress:nil];
}
- (void) showProgress:(NSString *)message {
    if(!HUD){
        HUD = [[MBProgressHUD alloc] initWithView:self.window];
        HUD.delegate = self;
        HUD.dimBackground = NO;
        //        [HUD setColor:[UIColor darkGrayColor]];
    }
    
    if(message){
        NSLog(@"message = %@", message);
        [HUD setDetailsLabelText:message];
    }else{
        HUD.detailsLabelText = nil;
    }
    
    if ([HUD superview]==nil) {
        [self.window addSubview:HUD];
    }
    
    [HUD show:YES];
}

- (void) hideProgress {
    [HUD hide:YES];
}

- (void)hudWasHidden:(MBProgressHUD *)hud {
    NSLog(@"hudWasHidden");
    
    // Remove HUD from screen when the HUD was hidded
    [HUD removeFromSuperview];
    //HUD = nil;
}

-(void) makeToast:(NSString *)message{
    [self makeToast:message duration:2 position:CSToastPositionBottom];
}
-(void) makeToast:(NSString *)message position:(id)position{
    [self makeToast:message duration:2 position:position];
}
-(void) makeToast:(NSString *)message duration:(NSTimeInterval)duration position:(id)position{
    [self.window makeToast:message duration:duration position:position];
}
-(void) makeToast:(NSString *)message duration:(NSTimeInterval)duration position:(id)position style:(CSToastStyle *)style{
    [self.window makeToast:message duration:duration position:position style:style];
}
@end
