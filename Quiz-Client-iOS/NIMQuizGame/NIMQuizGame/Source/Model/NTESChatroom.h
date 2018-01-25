//
//  NTESChatroom.h
//  NIMQuizGame
//
//  Created by William on 2017/11/19.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESQuizDefine.h"

@interface NTESChatroom : NSObject

@property (nonatomic, copy) NSString *roomId;

@property (nonatomic, copy) NSString *name;

@property (nonatomic, copy) NSString *creator;

@property (nonatomic, copy) NSString *rtmpPullUrl1;

@property (nonatomic, assign) BOOL roomStatus;

@property (nonatomic, assign) NTESChatroomLiveStatus liveStatus;

@property (nonatomic, assign) NSInteger onlineUserCount;

@property (nonatomic, assign) NSInteger bonus;

@property (nonatomic, assign) NSInteger quizCount;

- (instancetype)initWithInfo:(NSDictionary *)info;

@end
