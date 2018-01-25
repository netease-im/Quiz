//
//  NTESSubmitQuizSelectTask.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESSubmitQuizSelectTask.h"

@implementation NTESSubmitQuizSelectTask

- (NSString *)requestMethod
{
    return @"quiz/player/answer";
}

- (NSDictionary *)param
{
    if ([NIMSDK sharedSDK].loginManager.currentAccount.length)
    {
        return @{
                 @"sid"       : [NIMSDK sharedSDK].loginManager.currentAccount,
                 @"roomId"    : @(self.roomId.longLongValue),
                 @"questionId": @(self.quizId.longLongValue),
                 @"answer"    : @(self.answerId),
                };
    }
    else
    {
        return @{};
    }
}


@end
