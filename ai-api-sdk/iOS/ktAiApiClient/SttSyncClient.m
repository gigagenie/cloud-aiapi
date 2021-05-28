#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"
#import "AudioMgr.h"

@interface SttSyncClient ()<AVAudioPlayerDelegate, AVAudioRecorderDelegate>
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UIButton *psBtn;
@property (weak, nonatomic) IBOutlet UIButton *rsBtn;
@property (weak, nonatomic) IBOutlet UIButton *queryBtn;
@property (weak, nonatomic) IBOutlet UIView *transactionPv;
@property (weak, nonatomic) IBOutlet UITextField *tranTf;
@property NSString *path;
@property NSString *lang;
@property NSString *tranId;
@property NSInteger mode, channel, sampleRate, sampleFmt;
@property AIktManager *manager;
@end

@implementation SttSyncClient

- (void)viewDidLoad {
    [super viewDidLoad];
    _manager = [AIktManager sharedInstance];
    _mode = 1;
    _channel = 1;
    _sampleRate = 16000;
}

- (IBAction)onRequest:(id)sender {
    [[self appDelegate] showProgress];
    NSData *data = [NSData dataWithContentsOfFile:_path];
    [_manager restSTT:data mode:@(_mode) language:@"ko" encoding:@"raw" channel:@(_channel) sampleRate:@(_sampleRate) sampleFmt:_sampleFmt?@"F32LE":@"S16LE" Block:^(ApiResult *result, SttResultInfo *resultInfo) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
                NSLog(@"resultInfo %@", resultInfo);
                dispatch_async(dispatch_get_main_queue(), ^{
                    for (NSDictionary *arrDic in resultInfo.data) {
                        NSString *resultType = [arrDic objectForKey:@"resultType"];
                        if ([resultType isEqualToString:@"err"]) {
                            dispatch_async(dispatch_get_main_queue(), ^{
                                NSString *code = [arrDic objectForKey:@"errCode"];
                                if ([code isEqualToString:@"STT000"]) {
                                    [[self appDelegate] makeToast:@"허용 음성데이터 용량 초과"];
                                }else if ([code isEqualToString:@"STT001"]) {
                                    [[self appDelegate] makeToast:@"월 API 사용량 한도 초과"];
                                }else if ([code isEqualToString:@"STT002"]) {
                                    [[self appDelegate] makeToast:@"오디오 포맷 판별 실패"];
                                }else if ([code isEqualToString:@"STT003"]) {
                                    [[self appDelegate] makeToast:@"비동기식 장문 음성 데이터 포멧 에러"];
                                }
                            });
                        }else if (self->_mode == 2 && [resultType isEqualToString:@"start"]) {
                            self->_tranId = [arrDic objectForKey:@"transactionId"];
                            dispatch_async(dispatch_get_main_queue(), ^{
                                self->_tranTf.text = self->_tranId;
                                self->_queryBtn.enabled = YES;
                            });
                        }else
                        if ([resultType isEqualToString:@"text"]) {
                            NSDictionary *sttResult = [arrDic objectForKey:@"sttResult"];
                            NSString *text = [sttResult objectForKey:@"text"];
                            if (self->_textView.text.length == 0) {
                                self->_textView.text = text;
                            }else{
                                self->_textView.text = [NSString stringWithFormat:@"%@\n%@",self->_textView.text, text];
                            }
                        }
                    }
                });
            }else{
                if(result.statusCode == 301){
                    NSLog(@"server = %@", result.additionalMessage);
                    [[self appDelegate] makeToast:@"서버 변경"];
                    AIktManager *manager = [AIktManager sharedInstance];
                    [manager setRESTServerURL:[result.additionalMessage objectForKey:@"entrypoint"]];
                    
                }else{
                    NSLog(@"fail");
                    [[self appDelegate] makeToast:@"통신 실패"];
                }
            }
        });
    }];
}

- (IBAction)onQuery:(id)sender {
    self->_psBtn.enabled = NO;
    _tranTf.text = @"";
    [[self appDelegate] showProgress];
    [_manager restTransaction:_tranId Block:^(ApiResult *result, SttResultInfo *resultInfo) {
        if (result.success) {
            NSLog(@"resultInfo %@", resultInfo);
            dispatch_async(dispatch_get_main_queue(), ^{
                for (NSDictionary *arrDic in resultInfo.data) {
                    NSString *resultType = [arrDic objectForKey:@"resultType"];
                    if ([resultType isEqualToString:@"text"]) {
                        NSDictionary *sttResult = [arrDic objectForKey:@"sttResult"];
                        NSString *text = [sttResult objectForKey:@"text"];
                        if (self->_textView.text.length == 0) {
                            self->_textView.text = text;
                        }else{
                            self->_textView.text = [NSString stringWithFormat:@"%@\n%@",self->_textView.text, text];
                        }
                    }
                }
            });
        }else{
            if(result.statusCode == 301){
                NSLog(@"server = %@", result.additionalMessage);
                [[self appDelegate] makeToast:@"서버 변경"];
                AIktManager *manager = [AIktManager sharedInstance];
                [manager setRESTServerURL:[result.additionalMessage objectForKey:@"entrypoint"]];
                
            }else{
                NSLog(@"fail");
                [[self appDelegate] makeToast:@"통신 실패"];
            }
        }
    }];
}
- (IBAction)onRecordAndStop:(UIButton *)sender {
    sender.selected = !sender.selected;
    if ([[AudioMgr sharedObject] isRecording]) {
        [[AudioMgr sharedObject] onRecordStop];
    }else{
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentDirectory = [paths objectAtIndex:0];
        _path = [NSString stringWithFormat:@"%@/test.caf", documentDirectory];
        [[AudioMgr sharedObject] onRecordStart:self path:_path channel:_channel sampleRate:_sampleRate float32F:_sampleFmt];
    }
}
- (IBAction)onPlayAndStop:(UIButton *)sender {
    sender.selected = !sender.selected;
    if (_path) {
        if(sender.isSelected){
            [[AudioMgr sharedObject] onPlay:[NSURL fileURLWithPath:_path] delegate:self];
        }else{
            [[AudioMgr sharedObject] onStop];
        }
    }else{
        NSLog(@"asdfjl");
        [[self appDelegate] makeToast:@"재생할 파일이 없습니다"];
    }
}
- (IBAction)onSelectMode:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"1" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"1" forState:UIControlStateNormal];
        self->_mode = 1;
        self->_tranTf.text = @"";
        self->_queryBtn.enabled = NO;
        self->_transactionPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"2" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"2" forState:UIControlStateNormal];
        self->_mode = 2;
        self->_transactionPv.hidden = NO;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectFmt:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"S16LE" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"S16LE" forState:UIControlStateNormal];
        self->_sampleFmt = 0;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"S32LE" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"F32LE" forState:UIControlStateNormal];
        self->_sampleFmt = 0;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectSampleRate:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"16000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"16000" forState:UIControlStateNormal];
        self->_sampleRate = 16000;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"44100" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"44100" forState:UIControlStateNormal];
        self->_sampleRate = 44100;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"48000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"48000" forState:UIControlStateNormal];
        self->_sampleRate = 48000;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectChannel:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"Mono (1)" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"Mono (1)" forState:UIControlStateNormal];
        self->_channel = 1;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"Stereo (2)" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"Stereo (2)" forState:UIControlStateNormal];
        self->_channel = 2;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectLanguage:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"ko" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"ko" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

@end
