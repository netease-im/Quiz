//
//  NTESAttachment.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/15.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESAttachment.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESQuizAttachment

- (instancetype)initWithJson:(NSDictionary *)json
{
    self = [super init];
    if (self)
    {
        NSDictionary *quizInfo = [json jsonDict:@"questionInfo"];
        _quiz = [[NTESQuiz alloc] initWithJson:quizInfo];
        _timestamp = [json jsonDouble:@"time"];
    }
    return self;
}

@end


@implementation NTESFinalResultrAttachment

- (instancetype)initWithJson:(NSDictionary *)json
{
    self = [super init];
    if (self)
    {
        _result    = [[NTESResult alloc] initWithJson:json];
        _timestamp = [json jsonDouble:@"time"];
    }
    return self;
}

@end
