

#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonCrypto.h>
#import <KTAIAPISDK/ServerApi.h>
#import <GRPCClient/GRPCTransport.h>
#import <RxLibrary/GRXWriter+Immediate.h>
#import <KTAIAPISDK/Ktaiapi.pbrpc.h>

#include <KTAIAPISDK/AudioRecorder.h>

@class AIktManager;
NS_ASSUME_NONNULL_BEGIN
@protocol STTgRPCCallback <NSObject>
-(void)onConnectGRPC;
-(void)onSTTResult:(NSString *)text type:(NSString *)type startTime:(float)sTime endTime:(float)eTime;
-(void)onReadySTT:(NSString *)sampleFmt sampleRate:(int)sampleRate channel:(int)channel ;
-(void)onStopSTT;
-(void)onStartRecord;
-(void)onStopRecord;
-(void)onRelease;
-(void)onError:(int)errCode errMsg:(NSString *)errMsg;
@end

@interface AIktManager : NSObject


+(instancetype) sharedInstance;
-(void)setRESTServerURL:(NSString *)url;
-(void)setGRpcServerURL:(NSString *)url;
-(void)setClientId:(NSString *)cId clientKey:(NSString *)cKey clientSecret:(NSString *)cSecret;
-(void)requestTTS:(NSString *)text language:(NSString *)language speaker:(NSNumber *)speaker pitch:(NSNumber *)pitch speed:(NSNumber *)speed volume:(NSNumber *)volume encoding:(NSString *)encoding channel:(nullable NSNumber *)channel sampleRate:(nullable NSNumber *)sampleRate sampleFmt:(nullable NSString *)sampleFmt block:(ttsResultBlock)block;
-(void)requestSTT:(NSData *)binary mode:(NSNumber *)mode language:(NSString *)language encoding:(NSString *)encoding channel:(nullable NSNumber *)channel sampleRate:(nullable NSNumber *)sampleRate sampleFmt:(nullable NSString *)sampleFmt Block:(sttResBlock)block;
-(void)querySTT:(NSString *)tId Block:(sttResBlock)block;

-(void)connectGRPC:(id<STTgRPCCallback>)callBack;
-(void)releaseConnection;
-(void)startSTT:(NSString *)sttMode sampleFmt:(nullable NSString *)sampleFmt sampleRate:(nullable NSNumber *)sampleRate channel:(nullable NSNumber *)channel;
-(void)stopStt;
-(void)sendAudioFile:(NSString *)path;
@end

NS_ASSUME_NONNULL_END
