#import "ViewController.h"
#import <KTAIAPISDK/AIktManager.h>

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#define AI_PRODUCT 0

#define client_id @"client_id input"
#define client_key @"client_key input"
#define client_secret @"client_secret input"

@interface ViewController ()
@property KtAiApi *service;
@property AudioRecorder *audioRecorder;
@property GRPCStreamingProtoCall *call;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    AIktManager *manager = [AIktManager sharedInstance];
    [manager setRESTServerURL:@"aiapi.gigagenie.ai:443"];
    [manager setGRpcServerURL:@"aiapi.gigagenie.ai:443"];
    [manager setClientId:client_id clientKey:client_key clientSecret:client_secret];
    
}

@end
