#import <Foundation/Foundation.h>
#import "GeniememoViewController.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"
@interface GeniememoViewController ()<UIDocumentPickerDelegate, UINavigationControllerDelegate>
@property (weak, nonatomic) IBOutlet UITextField *callkeyTf;
@property (weak, nonatomic) IBOutlet UIButton *queryBtn;
@property (weak, nonatomic) IBOutlet UITextView *textView;

@property NSString *selectmemo;
@property NSString *path;
@property NSString *callkeypp;
@property NSString *lastYN;
@property NSInteger callindex;
@property AIktManager *manager;

@end

@implementation GeniememoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _manager = [AIktManager sharedInstance];
    _lastYN = @"Y";
    _selectmemo = @"GENIEMEMO";
    _callindex = 0;
}
- (AppDelegate *)appDelegate{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
- (IBAction)onRequest:(id)sender {
    [[self appDelegate] showProgress];
    NSData *data = [NSData dataWithContentsOfFile:_path];
    NSLog(@"requestGENIEMEMO");
    [_manager requestGENIEMEMO:data lastYn:_lastYN callIndex:@(_callindex) callkey:_callkeyTf.text
        Block:^(ApiResult *result, SttResultInfo *resultInfo) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    for (NSDictionary *arrDic in resultInfo.data) {
                        NSString *codenumber = [arrDic objectForKey:@"code"];
                        if ([codenumber isEqualToString:@"0000"]) {
                            for (NSDictionary *arrDic2 in [arrDic objectForKey:@"dataList"]) {
                                NSString *text = [arrDic2 objectForKey:@"text"];
                                if (self->_textView.text.length == 0) {
                                    self->_textView.text = text;
                                }else{
                                    self->_textView.text = [NSString stringWithFormat:@"%@\n%@",self->_textView.text, text];
                                }
                            }
                        }
                        else{
                            NSString *codenumber2 = [arrDic objectForKey:@"message"];
                            NSLog(@"codenumber2 : %@", codenumber2);
                            [[self appDelegate] makeToast: codenumber2];
                        }
                    }
                });
            }else{
                NSLog(@"fail %@", result);
                [[self appDelegate] makeToast: result.errorCode];
                
            }
        });
    }];
}
- (IBAction)onQuery:(id)sender {
    _textView.text = @"";
    [[self appDelegate] showProgress];
    NSLog(@"requestGENIEMEMOQUERY");
    [_manager queryGENIEMEMO:_callkeyTf.text Block:^(ApiResult *result, SttResultInfo *resultInfo) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
                if ([resultInfo.data isKindOfClass:[NSDictionary class]] && [[(NSDictionary *)resultInfo.data objectForKey:@"sttStatus"] isEqualToString:@"processing"]) {
                    [[self appDelegate] makeToast:@"처리중입니다 다시 시도해주세요."];
                }else{
                    NSString *codenumber = [(NSDictionary *)resultInfo.data objectForKey:@"code"];
                    if([codenumber isEqualToString:@"0000"]){
                    for (NSDictionary *arrDic in [(NSDictionary *)resultInfo.data objectForKey:@"dataList"]) {

                        NSString *text = [arrDic objectForKey:@"text"];
                        if (self->_textView.text.length == 0) {
                            self->_textView.text = text;
                        }else{
                            self->_textView.text = [NSString stringWithFormat:@"%@\n%@",self->_textView.text, text];
                        }
                    }
                    }
                    else{
                        NSString *codenumber2 = [(NSDictionary *)resultInfo.data objectForKey:@"message"];
                        NSLog(@"codenumber2 : %@", codenumber2);
                        [[self appDelegate] makeToast: codenumber2];
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

- (IBAction)onSelectmemo:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"GENIEMEMO" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"GENIEMEMO" forState:UIControlStateNormal];
        self->_selectmemo = @"GENIEMEMO";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"GENIEMEMOASYNC" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"GENIEMEMOASYNC" forState:UIControlStateNormal];
        self->_selectmemo = @"GENIEMEMOASYNC";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectYN:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"Y" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"Y" forState:UIControlStateNormal];
        self->_lastYN = @"Y";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"N" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"N" forState:UIControlStateNormal];
        self->_lastYN = @"N";
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectCallkey:(UIButton *)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"0" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"0" forState:UIControlStateNormal];
        self->_callindex = 0;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

@end
