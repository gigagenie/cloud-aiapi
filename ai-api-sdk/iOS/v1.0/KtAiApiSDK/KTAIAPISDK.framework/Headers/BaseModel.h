

@protocol BaseModel <NSObject>

@required
/**
 json Response Model Object 초기화 시, josnObject(NSDictionary) 를 인자로 받아 초기화 하도록함.
 @param object jsonObject
 @return instance
 */
- (instancetype)initWithObject:(id)object;

/**
 입력받은 dictionary 를 kv 로 값을 추출하여 property 에 mapping 하도록 하는 메소드.
 @param object mapping 하려는 NSDictionary
 */
- (void)parseObject:(id)object;

@end
