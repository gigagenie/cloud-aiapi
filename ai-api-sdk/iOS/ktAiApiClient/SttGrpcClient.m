#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@interface GrpcDemoViewController ()<UIDocumentPickerDelegate, UINavigationControllerDelegate, STTgRPCCallback>
@property (weak, nonatomic) IBOutlet UIButton *connectBtn;
@property (weak, nonatomic) IBOutlet UIButton *disconnectBtn;
@property (weak, nonatomic) IBOutlet UIButton *recordBtn;
@property (weak, nonatomic) IBOutlet UIButton *sendAudioBtn;
@property (weak, nonatomic) IBOutlet UIButton *fileBtn;
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UILabel *statusLb;
@property NSString *path;
@property NSString *lang;
@property NSString *mode;
@property NSString *sampleFmt;
@property NSInteger channel, sampleRate;
@property AIktManager *manager;

@end

@implementation GrpcDemoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _mode = @"long";
    _sampleFmt = @"S16LE";
    _channel = 1;
    _sampleRate = 16000;
    _manager = [AIktManager sharedInstance];
    [_manager setGRpcServerURL:@"aiapi.gigagenie.ai:443"];
}
- (AppDelegate *)appDelegate{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
- (IBAction)onSelectMode:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"long" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"long" forState:UIControlStateNormal];
        self->_mode = @"long";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"cmd" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"cmd" forState:UIControlStateNormal];
        self->_mode = @"cmd";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectChannel:(UIButton *)sender {
}
- (IBAction)onSelectSampleRate:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"8000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"8000" forState:UIControlStateNormal];
        self->_sampleRate = 8000;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"16000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"16000" forState:UIControlStateNormal];
        self->_sampleRate = 16000;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectSampleFmt:(id)sender {
}
- (IBAction)onConnect:(id)sender {
    [_manager connectGRPC:self];
}
- (IBAction)onDisconnect:(id)sender {
    [_manager releaseConnection];
}
- (IBAction)onStartRecordeOrStop:(UIButton *)sender {
    sender.selected = !sender.selected;
    if (sender.isSelected) {
        [_manager startSTT:_mode sampleFmt:_sampleFmt sampleRate:@(_sampleRate) channel:@(_channel)];
    }else{
        [_manager stopStt];
    }
}
- (IBAction)onSendAudioFile:(id)sender {
    if (_path) {
        [_manager sendAudioFile:_path];
    }else{
        [[self appDelegate] makeToast:@"text로 변환할 파일을 선택해주세요"];
    }
}
- (IBAction)onSelectFile:(id)sender {
    UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[@"public.mp3", @"public.audio"] inMode:UIDocumentPickerModeImport];
    documentPicker.delegate = self;
    documentPicker.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:documentPicker animated:YES completion:nil];
}
-(void) documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    NSLog(@"--- didPickDocumentController.. -----");
    _path = [urls[0] path];
    [[self appDelegate] makeToast:[NSString stringWithFormat:@"선택한 파일은 %@", [_path lastPathComponent]]];
}
-(void)onConnectGRPC{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[self appDelegate] makeToast:@"gRPC connect"];
        self->_statusLb.text = @"Connected";
        self->_connectBtn.enabled = NO;
        self->_disconnectBtn.enabled = YES;
        self->_fileBtn.enabled = YES;
        self->_recordBtn.enabled = YES;
        self->_sendAudioBtn.enabled = YES;
    });
}
-(void)onSTTResult:(NSString *)text type:(NSString *)type startTime:(float)sTime endTime:(float)eTime{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_statusLb.text = @"Connected";
        if ([type isEqualToString:@"full"]) {
            if (self->_textView.text.length == 0) {
                self->_textView.text = text;
            }else{
                if (text) {
                    self->_textView.text = [NSString stringWithFormat:@"%@\n%@", self->_textView.text, text];
                }
            }
        }
    });
}
-(void)onReadySTT:(NSString *)sampleFmt sampleRate:(int)sampleRate channel:(int)channel{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_statusLb.text = @"Connected";
    });
}
-(void)onStopSTT{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_statusLb.text = @"Connected";
        self->_recordBtn.selected = NO;
        self->_connectBtn.enabled = NO;
        self->_disconnectBtn.enabled = YES;
        self->_fileBtn.enabled = YES;
        self->_recordBtn.enabled = YES;
        self->_sendAudioBtn.enabled = YES;
    });
}
-(void)onStartRecord{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_statusLb.text = @"onRecording";
        [[self appDelegate] makeToast:@"StartRecord"];
        self->_connectBtn.enabled = NO;
        self->_disconnectBtn.enabled = NO;
        self->_fileBtn.enabled = NO;
        self->_recordBtn.enabled = YES;
        self->_sendAudioBtn.enabled = NO;
    });
}
-(void)onStopRecord{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_statusLb.text = @"Connected";
        [[self appDelegate] makeToast:@"StopRecord"];
        self->_connectBtn.enabled = NO;
        self->_disconnectBtn.enabled = YES;
        self->_fileBtn.enabled = YES;
        self->_recordBtn.selected = NO;
        self->_recordBtn.enabled = YES;
        self->_sendAudioBtn.enabled = YES;
    });
}
-(void)onRelease{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_statusLb.text = @"Disconnected";
        [[self appDelegate] makeToast:@"gRPC Disconnect"];
        self->_connectBtn.enabled = YES;
        self->_disconnectBtn.enabled = NO;
        self->_fileBtn.enabled = NO;
        self->_recordBtn.enabled = NO;
        self->_sendAudioBtn.enabled = NO;
    });
}
-(void)onError:(int)errCode errMsg:(NSString *)errMsg{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"msg = %@", errMsg);
        if(errCode == 301){
            [self->_manager setGRpcServerURL:errMsg];
            [self->_manager connectGRPC:self];
        }
    });
}

@end
