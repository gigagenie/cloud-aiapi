#import "SttDemo2ViewController.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"
@interface SttDemo2ViewController ()<UIDocumentPickerDelegate, UINavigationControllerDelegate>
@property (weak, nonatomic) IBOutlet UIView *encodingPv;
@property (weak, nonatomic) IBOutlet UIView *tranPv;
@property (weak, nonatomic) IBOutlet UITextField *tranTf;
@property (weak, nonatomic) IBOutlet UIButton *queryBtn;
@property (weak, nonatomic) IBOutlet UITextView *textView;

@property NSString *encoding;
@property NSString *language;
@property NSString *path;
@property NSString *tranId;
@property NSInteger mode, channel, sampleRate, sampleFmt;
@property AIktManager *manager;

@end

@implementation SttDemo2ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _manager = [AIktManager sharedInstance];
    _mode = 1;
    _channel = 1;
    _sampleRate = 16000;
    _encoding = @"raw";
    _language = @"ko";
}
- (AppDelegate *)appDelegate{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
- (IBAction)onRequest:(id)sender {
    [[self appDelegate] showProgress];
    NSData *data = [NSData dataWithContentsOfFile:_path];
    NSLog(@"requestSTT2");
    [_manager requestSTT:data mode:@(_mode) language:_language encoding:_encoding channel:@(_channel) sampleRate:@(_sampleRate) sampleFmt:_sampleFmt?@"F32LE":@"S16LE" Block:^(ApiResult *result, SttResultInfo *resultInfo) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
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
                NSLog(@"fail %@", result.errorCode);
                [[self appDelegate] makeToast: result.errorCode];
                
            }
        });
    }];
}
- (IBAction)onQuery:(id)sender {
    _textView.text = @"";
    [[self appDelegate] showProgress];
    NSLog(@"requestSTT2QUERY");
    [_manager querySTT:_tranId Block:^(ApiResult *result, SttResultInfo *resultInfo) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
                if ([resultInfo.data isKindOfClass:[NSDictionary class]] && [[(NSDictionary *)resultInfo.data objectForKey:@"sttStatus"] isEqualToString:@"processing"]) {
                    [[self appDelegate] makeToast:@"처리중입니다 다시 시도해주세요."];
                }else{
                    for (NSDictionary *arrDic in [(NSDictionary *)resultInfo.data objectForKey:@"sttResults"]) {
                        
                        NSString *text = [arrDic objectForKey:@"text"];
                        if (self->_textView.text.length == 0) {
                            self->_textView.text = text;
                        }else{
                            self->_textView.text = [NSString stringWithFormat:@"%@\n%@",self->_textView.text, text];
                        }
                    }
                }
                
            }else{
                NSLog(@"fail %@", result.errorCode);
                [[self appDelegate] makeToast: result.errorCode];
                
            }
        });
    }];
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

- (IBAction)onSelectMode:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"1" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"1" forState:UIControlStateNormal];
        self->_mode = 1;
        self->_tranTf.text = @"";
        self->_queryBtn.enabled = NO;
        self->_tranPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"2" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"2" forState:UIControlStateNormal];
        self->_mode = 2;
        self->_tranPv.hidden = NO;
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
- (IBAction)onSelectEncoding:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"raw" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"raw" forState:UIControlStateNormal];
        self->_encoding = @"raw";
        self->_encodingPv.hidden = NO;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"mp3" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"mp3" forState:UIControlStateNormal];
        self->_encoding = @"mp3";
        self->_encodingPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"vor" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"vor" forState:UIControlStateNormal];
        self->_encoding = @"vor";
        self->_encodingPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"aac" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"aac" forState:UIControlStateNormal];
        self->_encoding = @"aac";
        self->_encodingPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"fla" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"fla" forState:UIControlStateNormal];
        self->_encoding = @"fla";
        self->_encodingPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"wav" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"wav" forState:UIControlStateNormal];
        self->_encoding = @"wav";
        self->_encodingPv.hidden = YES;
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
        self->_language = @"ko";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}


@end
