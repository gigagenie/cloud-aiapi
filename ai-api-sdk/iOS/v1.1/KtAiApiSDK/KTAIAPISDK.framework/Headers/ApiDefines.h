

#ifndef ApiDefines_h
#define ApiDefines_h

@class ApiResult;
@class ServerResultInfo;
@class SttResultInfo;

static NSString *const GET = @"GET";
static NSString *const PUT = @"PUT";
static NSString *const POST = @"POST";
static NSString *const DELETE = @"DELETE";

static NSString *const Success = @"200";

typedef ApiResult*(^parseBlock)(NSURLResponse *response, id responseObject, NSError *error);

typedef void(^responseBlock)(ApiResult *result, ServerResultInfo *resultInfo);
typedef void(^sttResBlock)(ApiResult *result, SttResultInfo *resultInfo);
typedef void(^objectResultBlock)(ApiResult *result, id object);
typedef void(^ttsResultBlock)(ApiResult *result, NSString *path);
#endif /* ApiDefines_h */
