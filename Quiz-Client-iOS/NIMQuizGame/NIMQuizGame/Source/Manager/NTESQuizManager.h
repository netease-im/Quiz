//
//  NTESQuizManager.h
//  NIMQuizGame
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESUser.h"
#import "NTESQuiz.h"
#import "NTESResult.h"
#import "NTESQuizDefine.h"
#import "NTESChatroom.h"

@protocol NTESInfoManagerDelegate<NSObject>

/* 收到问题 */
- (void)onReceiveQuiz:(NTESQuiz *)quiz;

/* 收到回答结果*/
- (void)onReceiveAnswer:(NTESQuiz *)quiz;

/* 收到最后的答题统计结果*/
- (void)onReceiveFinalResult:(NTESResult *)result;

@optional

@end

@interface NTESQuizManager : NSObject

@property (nonatomic,weak) id<NTESInfoManagerDelegate> delegate;/* 事件回调代理 */

@property(nonatomic,assign,readonly) NSInteger resurrectionTimes;/* 复活次数 */

- (instancetype)initWithChatroom:(NTESChatroom *)chatroom;

/* 根据 ID 查问题 */
- (NTESQuiz *)quizById:(NSString *)quizId;

/* 此时是否可以提交答案 */
- (BOOL)canSubmitQuizOption;

/* 提交自己的答案 */
- (void)submitMyQuizOption:(NTESQuizOption *)option
                completion:(void(^)(NSError *error, NIMAnswerResult result))completion;

@end
