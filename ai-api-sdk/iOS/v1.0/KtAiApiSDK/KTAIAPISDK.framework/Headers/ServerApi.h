

#import <Foundation/Foundation.h>
#import <KTAIAPISDK/ApiResult.h>
#import <KTAIAPISDK/ApiDefines.h>
#import <KTAIAPISDK/SttResultInfo.h>

@class ApiRequestConstructor;

extern NSString *const kxClientKey;
extern NSString *const kxAuthTimestamp;
extern NSString *const kxClientSignature;
extern NSString *const kContentType;
///  정상
static NSInteger ResultCode0    = 0;
///  해당 이메일이 존재합니다.
static NSInteger ResultCode1    = 1;
///  전문 규칙 오류
static NSInteger ResultCode1000 = 1000;

@interface ServerApi : NSObject
@property NSString *serverUrl;

@property (assign, nonatomic) BOOL isAleadyNew;

+ (instancetype)sharedInstance;
+ (NSURLSessionDataTask *)uploadFileWithData:(NSData *)binary Params:(NSDictionary *)params key:(NSString *)key tStamp:(NSString*)tStamp signature:(NSString *)signature block:(sttResBlock)block;
+ (NSURLSessionDataTask *)getTransactionId:(NSString *)tId key:(NSString *)key tStamp:(NSString*)tStamp signature:(NSString *)signature Block:(sttResBlock)block;
+ (NSURLSessionDataTask *)ttsParams:(NSDictionary *)params key:(NSString *)key tStamp:(NSString*)tStamp signature:(NSString *)signature Block:(ttsResultBlock)block;
@end

