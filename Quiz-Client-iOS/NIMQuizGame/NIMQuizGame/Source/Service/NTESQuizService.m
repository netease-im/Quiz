//
//  NTESQuizService.m
//  NIMQuizGame
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESQuizService.h"
#import "NTESNetwork.h"
#import "NSDictionary+NTESJson.h"
#import "NTESDDLogManager.h"
#import "NTESSubmitQuizSelectTask.h"
#import "NTESQueryRoomInfoTask.h"
#import "NTESQuizManager.h"
#import "NTESLoginTask.h"
#import <NIMSDK/NIMSDK.h>
#import "NTESUserManager.h"
#import "NTESSolutionConfig.h"

@implementation NTESQuizService

+ (instancetype)sharedService
{
    static NTESQuizService *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[NTESQuizService alloc] init];
    });
    return instance;
}

- (void)login:(void(^)(NSError *error))completion
{
    NTESLoginTask *task = [[NTESLoginTask alloc] init];
    task.sid = [[NSUserDefaults standardUserDefaults] objectForKey:@"userId"];
    DDLogInfo(@"start login... current appkey : %@",[NTESSolutionConfig config].appKey);
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"app login complete error: %@",error);
        if (!error && [jsonObject isKindOfClass:[NSDictionary class]])
        {
            NSDictionary *data = [jsonObject jsonDict:@"data"];
            NSString *accid = [data jsonString:@"accid"];
            NSString *nick  = [data jsonString:@"nickname"];
            NSString *token = [data jsonString:@"imToken"];
            //开始登录云信
            [self loginNIM:accid token:token nick:nick completion:completion];
        }
        else if (completion)
        {
            completion(error);
        }
    }];
}

- (void)loginNIM:(NSString *)userId
           token:(NSString *)token
            nick:(NSString *)nick
      completion:(void(^)(NSError *error))completion
{
    DDLogInfo(@"start login nim...  user id : %@ , token : %@ , nick : %@", userId, token, nick);
    [[NIMSDK sharedSDK].loginManager login:userId token:token completion:^(NSError * _Nullable error) {
        DDLogInfo(@"nim login complete error: %@",error);
        if (!error)
        {
            [[NSUserDefaults standardUserDefaults] setObject:userId forKey:@"userId"];
            NTESUser *user = [NTESUser userWithInfo:@{@"userId":userId,@"nick":nick}];
            [[NTESUserManager sharedManager] addUserInfo:user];
        }
        if (completion)
        {
            completion(error);
        }
        
    }];
}

- (void)queryRoomInfo:(NSString *)roomId
           completion:(void(^)(NSError *error, NTESChatroom *chatroom))completion
{
    DDLogInfo(@"query chat room %@ info ...",roomId);
    NTESQueryRoomInfoTask *task = [[NTESQueryRoomInfoTask alloc] init];
    task.roomId = roomId;
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"query chat room complete error:%@",error);
        if (!error && [jsonObject isKindOfClass:[NSDictionary class]])
        {
            NSDictionary *data = [jsonObject jsonDict:@"data"];
            if (data && completion)
            {
                NTESChatroom *chatroom = [[NTESChatroom alloc] initWithInfo:data];
                completion(error,chatroom);
            }
        }
        else if (completion)
        {
            completion(error,nil);
        }
    }];
}

- (void)submitMyQuizOption:(NTESQuizOption *)select
                    roomId:(NSString *)roomId
                completion:(void(^)(NSError *error, NIMAnswerResult result))completion
{
    DDLogInfo(@"send quiz select to sever ...  my select option : %@",select);
    NTESSubmitQuizSelectTask *task = [[NTESSubmitQuizSelectTask alloc] init];
    task.roomId = roomId;
    task.quizId = select.fromQuizId;
    task.answerId = select.optionId;
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"submit quiz complete error:%@ , result:%@",error,jsonObject);
        if (!error && [jsonObject isKindOfClass:[NSDictionary class]])
        {
            NSDictionary *data = [jsonObject jsonDict:@"data"];
            if (data && completion)
            {
                NIMAnswerResult result = [data jsonInteger:@"result"];
                completion(error,result);
            }
        }
        else if (completion)
        {
            completion(error,NIMAnswerResultInvalid);
        }
    }];
    
}

@end
