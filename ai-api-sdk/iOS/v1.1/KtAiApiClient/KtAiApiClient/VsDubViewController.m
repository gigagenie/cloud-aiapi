#import "VsDubViewController.h"
#import "AudioMgr.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"

@interface VsDubViewController ()<AudioDelegate, AVAudioPlayerDelegate,UIDocumentPickerDelegate, UINavigationControllerDelegate>
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UISlider *speakerSlider;
@property (weak, nonatomic) IBOutlet UISlider *pitchSlider;
@property (weak, nonatomic) IBOutlet UISlider *speedSlider;
@property (weak, nonatomic) IBOutlet UISlider *volumeSlider;
@property (weak, nonatomic) IBOutlet UILabel *speakerCountLb;
@property (weak, nonatomic) IBOutlet UILabel *pitchCountLb;
@property (weak, nonatomic) IBOutlet UILabel *speedCountLb;
@property (weak, nonatomic) IBOutlet UILabel *volumeCountLb;
@property (weak, nonatomic) IBOutlet UIButton *emotion;
@property (weak, nonatomic) IBOutlet UIButton *encoding;
@property (weak, nonatomic) IBOutlet UIButton *sampleRate;
@property (weak, nonatomic) IBOutlet UIButton *langBtn;
@property (weak, nonatomic) IBOutlet UIButton *playOrStopBtn;

@property int mode;
@property NSString *path;
@property AIktManager *manager;
@end

@implementation VsDubViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _speakerSlider.value = 150;
    _pitchSlider.value = 100;
    _speedSlider.value = 100;
    _volumeSlider.value = 100;
    _manager = [AIktManager sharedInstance];
    [AudioSessionManager setAudioSessionCategory:AVAudioSessionCategoryPlayback];
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(dismissKeyboard)];
    tap.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tap];
}
- (void)dismissKeyboard {
    [self.view endEditing:YES];
}
-(void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)player successfully:(BOOL)flag{
    _playOrStopBtn.selected = NO;
}
- (AppDelegate *)appDelegate{
    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
}
- (IBAction)onRequest:(id)sender {
    [[self appDelegate] showProgress];
    NSData *data = [NSData dataWithContentsOfFile:_path];
    NSLog(@"requestVSDUB");
    [_manager requestVSDUB:data text:_textView.text  speaker:@(_speakerCountLb.text.intValue) pitch:@(_pitchCountLb.text.intValue) speed:@(_speedCountLb.text.intValue) volume:@(_volumeCountLb.text.intValue) emotion:_emotion.titleLabel.text language:_langBtn.titleLabel.text encoding:_encoding.titleLabel.text sampleRate:@(_sampleRate.titleLabel.text.intValue) block:^(ApiResult *result, NSString *path) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
                self->_path = path;
                NSLog(@"path %@", path);
            }else{
                NSLog(@"fail %@",result.errorCode);
                [[self appDelegate] makeToast:result.errorCode];
            }
        });
    }];

}
- (IBAction)onPlayOrStop:(UIButton *)sender {
    if (_path) {
        sender.selected = !sender.selected;
        if(sender.isSelected){
            [[AudioMgr sharedObject] onPlay:[NSURL fileURLWithPath:_path]  delegate:self];
        }else{
            [[AudioMgr sharedObject] onStop];
        }
    }else{
        NSLog(@"재생할 파일이 없습니다");
        [[self appDelegate] makeToast:@"재생할 파일이 없습니다"];
    }
}
- (IBAction)onSpeakerChanged:(UISlider *)sender {
    _speakerCountLb.text = [NSString stringWithFormat:@"%d",(int)sender.value];
}

- (IBAction)onPitchChanged:(UISlider *)sender {
    _pitchCountLb.text = [NSString stringWithFormat:@"%d",(int)sender.value];
}
- (IBAction)onSpeedChanged:(UISlider *)sender {
    _speedCountLb.text =[NSString stringWithFormat:@"%d",(int)sender.value];
}
- (IBAction)onVolumeChanged:(UISlider *)sender {
    _volumeCountLb.text =[NSString stringWithFormat:@"%d",(int)sender.value];
}
- (IBAction)onSelectemotion:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"neutral" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"neutral" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"happy" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"happy" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"angry" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"angry" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"calm" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"calm" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"sleepy" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"sleepy" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"fear" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"fear" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"sad" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"sad" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"excited" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"excited" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"disappointed" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_emotion setTitle:@"disappointed" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectLanguage:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"ko" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_langBtn setTitle:@"ko" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

- (IBAction)onSelectEncoding:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"mp3" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_encoding setTitle:@"mp3" forState:UIControlStateNormal];
        self->_sampleRate.enabled = NO;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"wav" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_encoding setTitle:@"wav" forState:UIControlStateNormal];
        self->_sampleRate.enabled = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}

- (IBAction)onSelectSampleRate:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"16000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_sampleRate setTitle:@"16000" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"8000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_sampleRate setTitle:@"8000" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"44100" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_sampleRate setTitle:@"44100" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"48000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_sampleRate setTitle:@"48000" forState:UIControlStateNormal];
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
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

@end
