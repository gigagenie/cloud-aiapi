
#import <Foundation/Foundation.h>

@interface ApiResult : NSObject
@property (nonatomic, assign, readonly) NSInteger statusCode;
@property (nonatomic, assign, readonly) BOOL success;
@property (nonatomic, strong) NSDictionary *additionalMessage;
@property (nonatomic, strong) NSString *errorCode;


- (instancetype)initWithStatusCode:(NSInteger)statusCode;
- (instancetype)initWithStatusCode:(NSInteger)statusCode resultMessage:(NSString *)resultMessage parseMessage:(NSString *)parseMessage;
@end

