
#import <Foundation/Foundation.h>
#import <KTAIAPISDK/ServerApi.h>

#define TIMEOUTINTERVAL 20.0
@interface ApiRequestConstructor : NSObject
- (NSMutableURLRequest *)request;

- (void)requestMethod:(NSString *)method;
- (void)requestHTTPHeaderField:(NSString *)headerField value:(NSString *)value;
- (void)requestUserServerWithUri:(NSString *)uri option:(NSDictionary *)option;
- (void)requestTimeOut:(NSTimeInterval)timeout;
- (void)requestBody:(id)body;
@end

