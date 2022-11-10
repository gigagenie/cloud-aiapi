#import "TtsViewController.h"
#import "AudioMgr.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"

@interface TtsViewController ()<AudioDelegate, AVAudioPlayerDelegate>
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UIButton *speakerBtn;
@property (weak, nonatomic) IBOutlet UISlider *pitchSlider;
@property (weak, nonatomic) IBOutlet UISlider *speedSlider;
@property (weak, nonatomic) IBOutlet UISlider *volumeSlider;
@property (weak, nonatomic) IBOutlet UILabel *pitchCountLb;
@property (weak, nonatomic) IBOutlet UILabel *speedCountLb;
@property (weak, nonatomic) IBOutlet UILabel *volumeCountLb;
@property (weak, nonatomic) IBOutlet UIButton *langBtn;
@property (weak, nonatomic) IBOutlet UIButton *encodingBtn;
@property (weak, nonatomic) IBOutlet UIButton *playOrStopBtn;
@property (weak, nonatomic) IBOutlet UIView *encodingPv;
@property NSString *path, *sampleFmt;
@property NSInteger speaker, channel, sampleRate;
@end

@implementation TtsViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _speaker = 1;
    _pitchSlider.value = 100;
    _speedSlider.value = 100;
    _volumeSlider.value = 100;
    _channel = 1;
    _sampleRate = 16000;
    _sampleFmt = @"S16LE";
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
    AIktManager *manager = [AIktManager sharedInstance];
    NSLog(@"requestTTS");
    [manager requestTTS:_textView.text language:_langBtn.titleLabel.text speaker:@(_speaker) pitch:@(_pitchCountLb.text.intValue) speed:@(_speedCountLb.text.intValue) volume:@(_volumeCountLb.text.intValue) encoding:_encodingBtn.titleLabel.text channel:@(_channel) sampleRate:@(_sampleRate) sampleFmt:_sampleFmt block:^(ApiResult *result, NSString *path) {
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
        NSLog(@"asdfjl");
        [[self appDelegate] makeToast:@"재생할 파일이 없습니다"];
    }
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
- (IBAction)onSelectSpeaker:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];

    [actionSheet addAction:[UIAlertAction actionWithTitle:@"1(안찬이 성우)" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_speakerBtn setTitle:@"1(안찬이 성우)" forState:UIControlStateNormal];
        self->_speaker = 1;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];

    [actionSheet addAction:[UIAlertAction actionWithTitle:@"2(이다슬 아나운서)" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_speakerBtn setTitle:@"2(이다슬 아나운서)" forState:UIControlStateNormal];
        self->_speaker = 2;
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
        [self->_encodingBtn setTitle:@"mp3" forState:UIControlStateNormal];
        self->_encodingPv.hidden = YES;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];

   [actionSheet addAction:[UIAlertAction actionWithTitle:@"wav" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_encodingBtn setTitle:@"wav" forState:UIControlStateNormal];
        self->_encodingPv.hidden = NO;
        [self dismissViewControllerAnimated:YES completion:nil];
    }]];
    
    [self presentViewController:actionSheet animated:YES completion:nil];
}
- (IBAction)onSelectFmt:(UIButton *)sender {
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
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"24000" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [sender setTitle:@"24000" forState:UIControlStateNormal];
        self->_sampleRate = 24000;
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

@end

