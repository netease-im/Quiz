//
//  NTESQuizService.h
//  NIMQuizGame
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import <NIMSDK/NIMSDK.h>
#import "NTESChatroom.h"
#import "NTESUser.h"
#import "NTESQuiz.h"
#import "NTESChatroom.h"
#import "NTESQuizDefine.h"


@interface NTESQuizService : NSObject

+ (instancetype)sharedService;

- (void)login:(void(^)(NSError *error))completion;

- (void)queryRoomInfo:(NSString *)roomId
           completion:(void(^)(NSError *error, NTESChatroom *chatroom))completion;

- (void)submitMyQuizOption:(NTESQuizOption *)select
                    roomId:(NSString *)roomId
                completion:(void(^)(NSError *error, NIMAnswerResult result))completion;

@end
