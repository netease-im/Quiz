//
//  NTESChatroom.m
//  NIMQuizGame
//
//  Created by William on 2017/11/19.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESChatroom.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESChatroom

- (instancetype)initWithInfo:(NSDictionary *)info
{
    self = [super init];
    if (self)
    {
        _roomId          = [info jsonString:@"roomId"];
        _name            = [info jsonString:@"name"];
        _creator         = [info jsonString:@"creator"];
        _rtmpPullUrl1    = [info jsonString:@"rtmpPullUrl"];
        _roomStatus      = [info jsonBool:@"roomStatus"];
        _liveStatus      = [info jsonInteger:@"liveStatus"];
        _onlineUserCount = [info jsonInteger:@"onlineUserCount"];
        _bonus           = [info jsonInteger:@"bonus"];
        _quizCount       = [info jsonInteger:@"questionCount"];
    }
    return self;
}

@end
