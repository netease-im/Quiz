//
//  NTESUserManager.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/17.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESUser.h"
#import "NTESChatroom.h"

@interface NTESUserManager : NSObject

+ (instancetype)sharedManager;

/* 登录 */
- (void)login:(void(^)(NSError *error))completion;

/* 添加用户信息 */
- (void)addUserInfo:(NTESUser *)user;

/* 根据 Id 获取用户 */
- (NTESUser *)userById:(NSString *)userId;

/* 根据 Id 获取聊天室 */
- (NTESChatroom *)chatroomById:(NSString *)roomId;

/* 向应用服务器查询聊天室信息 */
- (void)queryRoomInfo:(NSString *)roomId
           completion:(void(^)(NSError *error, NTESChatroom *chatroom))completion;

@end
