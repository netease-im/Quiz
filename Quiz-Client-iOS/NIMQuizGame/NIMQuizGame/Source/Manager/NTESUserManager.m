//
//  NTESUserManager.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/17.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESUserManager.h"
#import "NTESQuizService.h"

@interface NTESUserManager()

@property (nonatomic, strong) NSMutableDictionary<NSString *, NTESUser *> *userInfo;

@property (nonatomic, strong) NSMutableDictionary<NSString *, NTESChatroom *> *roomInfo;

@end

@implementation NTESUserManager

+ (instancetype)sharedManager
{
    static NTESUserManager *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[NTESUserManager alloc] init];
    });
    return instance;
}

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        _userInfo = [[NSMutableDictionary alloc] init];
        _roomInfo = [[NSMutableDictionary alloc] init];
    }
    return self;
}


- (void)login:(void(^)(NSError *error))completion
{
    [[NTESQuizService sharedService] login:completion];
}


- (void)addUserInfo:(NTESUser *)user
{
    DDLogInfo(@"add user info  %@",user);
    [self.userInfo setObject:user forKey:user.userId];
}

- (NTESUser *)userById:(NSString *)userId
{
    return [self.userInfo objectForKey:userId];
}

- (NTESChatroom *)chatroomById:(NSString *)roomId
{
    return [self.roomInfo objectForKey:roomId];
}

- (void)queryRoomInfo:(NSString *)roomId
           completion:(void(^)(NSError *error, NTESChatroom *chatroom))completion
{
    __weak typeof(self) weakSelf = self;
    [[NTESQuizService sharedService] queryRoomInfo:roomId completion:^(NSError *error, NTESChatroom *chatroom) {
        if (!error)
        {
            [weakSelf.roomInfo setObject:chatroom forKey:chatroom.roomId];
        }
        if (completion)
        {
            completion(error,chatroom);
        }
    }];
}

@end
