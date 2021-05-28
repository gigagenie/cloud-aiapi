#import "AudioMgr.h"
#import <KTAIAPISDK/AIktManager.h>
#import "AppDelegate.h"

@interface TtsClient ()<AudioDelegate, AVAudioPlayerDelegate>
@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UISlider *pitchSlider;
@property (weak, nonatomic) IBOutlet UISlider *speedSlider;
@property (weak, nonatomic) IBOutlet UISlider *volumeSlider;
@property (weak, nonatomic) IBOutlet UILabel *pitchCountLb;
@property (weak, nonatomic) IBOutlet UILabel *speedCountLb;
@property (weak, nonatomic) IBOutlet UILabel *volumeCountLb;
@property (weak, nonatomic) IBOutlet UIButton *modeBtn;
@property (weak, nonatomic) IBOutlet UIButton *langBtn;
@property (weak, nonatomic) IBOutlet UIButton *playOrStopBtn;
@property int mode;
@property NSString *path;
@end

@implementation TtsClient

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _mode = 1;
    _pitchSlider.value = 100;
    _speedSlider.value = 100;
    _volumeSlider.value = 100;
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
    [manager restTTS:_textView.text language:_langBtn.titleLabel.text speaker:@(_mode) pitch:@(_pitchCountLb.text.intValue) speed:@(_speedCountLb.text.intValue) volume:@(_volumeCountLb.text.intValue) encoding:@"mp3" channel:@(2) sampleRate:@(16000) sampleFmt:@"F32LE" block:^(ApiResult *result, NSString *path) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self appDelegate] hideProgress];
            if (result.success) {
                self->_path = path;
                NSLog(@"path %@", path);
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
- (IBAction)onSelectMode:(id)sender {
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    [actionSheet addAction:[UIAlertAction actionWithTitle:@"1(친절한 목소리)" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self->_modeBtn setTitle:@"1(친절한 목소리)" forState:UIControlStateNormal];
        self->_mode = 1;
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

@end
