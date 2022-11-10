#import "GrpcDemoViewController.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@interface GrpcDemoViewController ()<UIDocumentPickerDelegate, UINavigationControllerDelegate, STTgRPCCallback>
@property (weak, nonatomic) IBOutlet UIButton *connectBtn;
@property (weak, nonatomic) IBOutlet UIButton *disconnectBtn;
@property (weak, nonatomic) IBOutlet UIButton *recordBtn;
@property (weak, nonatomic) IBOutlet UIButton *sendAudioBtn;
@property (weak, nonatomic) IBOutlet UIButton *sendAudio2Btn;
@property (weak, nonatomic) IBOutlet UIButton *fileBtn;
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UILabel *statusLb;
@property NSString *path;
@property NSString *lang;
@property NSString *mode;
@property NSString *sampleFmt;
@property NSInteger sttModelCode, channel, sampleRate;
@property AIktManager *manager;

@end

@implementation GrpcDemoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _sttModelCode = 1;
    _mode = @"long";
    _sampleFmt = @"S16LE";
    _channel = 1;
    _sampleRate = 16000;
    _manager = [AIktManager sharedInstance];
    
}
- (AppDelegate *)appDelegate{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
- (IBAction)onSelectsttModelCode:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"1" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"1" forState:UIControlStateNormal];
        self->_sttModelCode = 1;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"2" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"2" forState:UIControlStateNormal];
        self->_sttModelCode = 2;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"4" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"4" forState:UIControlStateNormal];
        self->_sttModelCode = 4;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
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
    [actionSheet addAction:[UIAlertAction actionWithTitle:@" " style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@" " forState:UIControlStateNormal];
        self->_mode = @" ";
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
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"S16LE" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"S16LE" forState:UIControlStateNormal];
        self->_sampleFmt = @"S16LE";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"F32LE" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"F32LE" forState:UIControlStateNormal];
        self->_sampleFmt = @"F32LE";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
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
    NSLog(@"requestgRPC");
    if (_path) {
        [_manager sendAudioFile:_path sttMode:_mode sampleFmt:_sampleFmt sampleRate:@(_sampleRate) channel:@(_channel)];
    }else{
        [[self appDelegate] makeToast:@"text로 변환할 파일을 선택해주세요"];
    }
}

- (IBAction)onSendAudio2File:(id)sender {
    NSLog(@"requestgRPC");
    if (_path) {
        [_manager sendAudioFile2:_path sttModelCode:@(_sttModelCode) sampleRate:@(_sampleRate)];
    }else{
        [[self appDelegate] makeToast:@"text로 변환할 파일을 선택해주세요"];
    }
}

- (IBAction)onSelectFile:(id)sender {
    UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[@"public.data"] inMode:UIDocumentPickerModeImport];
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
        self->_sendAudio2Btn.enabled = YES;
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
        self->_sendAudio2Btn.enabled = YES;
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
        self->_sendAudio2Btn.enabled = NO;
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
        self->_sendAudio2Btn.enabled = YES;
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
        self->_sendAudio2Btn.enabled = NO;
    });
}
-(void)onError:(int)errCode errMsg:(NSString *)errMsg{
        NSLog(@"msg = %@", errMsg);
}

@end
