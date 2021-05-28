

#import <Foundation/Foundation.h>


@interface BaseInfo : NSObject
- (instancetype)initWithObject:(id)object;
- (void)makeDescString:(NSMutableString *)desc key:(NSString *)key stringValue:(NSString *)value;
- (void)makeDescString:(NSMutableString *)desc key:(NSString *)key arrayValue:(NSArray *)value;
- (void)makeDescString:(NSMutableString *)desc key:(NSString *)key objectValue:(id)value;
- (BOOL)isNull:(id)object;
@end

