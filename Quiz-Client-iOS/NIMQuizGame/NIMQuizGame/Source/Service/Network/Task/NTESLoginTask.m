//
//  NTESLoginTask.m
//  NIMQuizGame
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESLoginTask.h"

@implementation NTESLoginTask

- (NSString *)requestMethod
{
    return @"quiz/player/create";
}

- (NSDictionary *)param
{
    if (self.sid.length)
    {
        return @{@"sid":self.sid};
    }
    else
    {
        return @{};
    }
}

@end
