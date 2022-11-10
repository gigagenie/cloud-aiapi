#import <Foundation/Foundation.h>
#import "SttDemo3ViewController.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"
@interface SttDemo3ViewController ()<UIDocumentPickerDelegate, UINavigationControllerDelegate>
@property (weak, nonatomic) IBOutlet UIView *encodingPv;
@property (weak, nonatomic) IBOutlet UIView *tranPv;
@property (weak, nonatomic) IBOutlet UITextField *tranTf;
@property (weak, nonatomic) IBOutlet UIButton *queryBtn;
@property (weak, nonatomic) IBOutlet UITextView *textView;

@property NSString *encoding;
@property NSString *path;
@property NSString *tranId;
@property NSInteger sttmodelcode;
@property AIktManager *manager;

@end

@implementation SttDemo3ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _manager = [AIktManager sharedInstance];
    _sttmodelcode = 3;
    _encoding = @"raw";
}
- (AppDelegate *)appDelegate{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
- (IBAction)onRequest:(id)sender {
    [[self appDelegate] showProgress];
    NSData *data = [NSData dataWithContentsOfFile:_path];
    NSLog(@"requestSTT3");
    [_manager requestSTT2:data sttmodelcode:@(_sttmodelcode) encoding:_encoding Block:^(ApiResult *result, SttResultInfo *resultInfo) {
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
                        }else if (self->_sttmodelcode > 3 && [resultType isEqualToString:@"start"]) {
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
    NSLog(@"requestSTT3QUERY");
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
    UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[@"public.data"] inMode:UIDocumentPickerModeImport]; //@"public.mp3", @"public.audio"
    documentPicker.delegate = self;
    documentPicker.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:documentPicker animated:YES completion:nil];
}
-(void) documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    NSLog(@"--- didPickDocumentController.. -----");
    _path = [urls[0] path];
    [[self appDelegate] makeToast:[NSString stringWithFormat:@"선택한 파일은 %@", [_path lastPathComponent]]];
}
- (IBAction)onSelectSttmodelcode:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"3" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"3" forState:UIControlStateNormal];
        self->_sttmodelcode = 3;
        self->_tranTf.text = @"";
        self->_queryBtn.enabled = NO;
        self->_tranPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"4" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"4" forState:UIControlStateNormal];
        self->_sttmodelcode = 4;
        self->_tranPv.hidden = NO;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"5" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"5" forState:UIControlStateNormal];
        self->_sttmodelcode = 5;
        self->_tranPv.hidden = NO;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectEncoding:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"raw" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"raw" forState:UIControlStateNormal];
        self->_encoding = @"raw";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"mp3" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"mp3" forState:UIControlStateNormal];
        self->_encoding = @"mp3";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"vor" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"vor" forState:UIControlStateNormal];
        self->_encoding = @"vor";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"aac" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"aac" forState:UIControlStateNormal];
        self->_encoding = @"aac";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"fla" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"fla" forState:UIControlStateNormal];
        self->_encoding = @"fla";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"wav" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"wav" forState:UIControlStateNormal];
        self->_encoding = @"wav";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}


@end
