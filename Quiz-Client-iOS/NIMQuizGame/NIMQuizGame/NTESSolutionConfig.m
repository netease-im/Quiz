//
//  NTESSolutionConfig.m
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/10.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESSolutionConfig.h"

@implementation NTESSolutionConfig

+ (instancetype)config
{
    static NTESSolutionConfig *config;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        config = [[NTESSolutionConfig alloc] init];
    });
    return config;
}

- (instancetype)init
{
    self = [super init];
    if(self)
    {
        _appKey  = @"682a4df6d71da43ce09787dceb502987";
        _appHost = @"https://app.netease.im/appdemo/";
        _resurrectionTimes = 1;
        _timeout = 30.f;
        _quizCardQuizCountdown = 10.f;
        _quizCardAnswerCountdown = 6.f;
    }
    return self;
}

@end
