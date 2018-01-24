//
//  NTESAttachment.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/15.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESQuiz.h"
#import "NTESResult.h"

@protocol NTESAttachment<NSObject>

- (instancetype)initWithJson:(NSDictionary *)json;

@end

@interface NTESQuizAttachment : NSObject<NTESAttachment>

@property (nonatomic, strong) NTESQuiz *quiz;

@property (nonatomic, assign) NSTimeInterval timestamp;

@end

@interface NTESFinalResultrAttachment : NSObject<NTESAttachment>

@property (nonatomic, copy) NSString *roomId;

@property (nonatomic, strong) NTESResult *result;

@property (nonatomic, assign) NSTimeInterval timestamp;

@end
