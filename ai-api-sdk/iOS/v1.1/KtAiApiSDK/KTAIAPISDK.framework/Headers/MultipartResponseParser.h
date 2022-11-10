

#import <Foundation/Foundation.h>

@interface MultipartResponseParser : NSObject

+ (NSArray *)parseData:(NSData *)data;

@end

extern NSString *const kMultipartHeadersKey;
extern NSString *const kMultipartBodyKey;
