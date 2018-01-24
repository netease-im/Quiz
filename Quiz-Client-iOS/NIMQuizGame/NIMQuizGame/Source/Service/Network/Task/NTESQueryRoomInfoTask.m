//
//  NTESQueryRoomInfoTask.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/12.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESQueryRoomInfoTask.h"

@implementation NTESQueryRoomInfoTask

- (NSString *)requestMethod
{
    return @"quiz/player/room/query";
}

- (NSDictionary *)param
{
    if ([NIMSDK sharedSDK].loginManager.currentAccount.length)
    {
        return @{@"sid":[NIMSDK sharedSDK].loginManager.currentAccount, @"roomId":@(self.roomId.longLongValue)};
    }
    else
    {
        return @{};	
    }
}




@end
