//
//  NTESQuizManager.m
//  NIMQuizGame
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESQuizManager.h"
#import "NTESQuizService.h"
#import "NSDictionary+NTESJson.h"
#import "NTESDDLogManager.h"
#import "NTESAttachment.h"
#import "NTESBlockDispatcher.h"
#import "NTESQuizService.h"
#import "NTESSolutionConfig.h"
#import "NTESUserManager.h"

@interface NTESQuizManager()<NIMChatManagerDelegate, NIMChatroomManagerDelegate>

@property (nonatomic, strong) NSMutableDictionary<NSString *, NTESQuiz *> *quizzes;

@property (nonatomic, strong) NTESBlockDispatcher *dispatcher;

@property (nonatomic, assign) NSInteger expectQuizOrder;

@property (nonatomic, assign) BOOL inQuizOrder;

@property (nonatomic, strong) NTESChatroom *chatroom;

@end

@implementation NTESQuizManager

- (instancetype)initWithChatroom:(NTESChatroom *)chatroom
{
    self = [super init];
    if (self)
    {
        DDLogInfo(@"set up quiz manager, room id %@",chatroom.roomId);
        _chatroom    = chatroom;
        _quizzes     = [[NSMutableDictionary alloc] init];
        _dispatcher  = [[NTESBlockDispatcher alloc] init];
        _resurrectionTimes = [NTESSolutionConfig config].resurrectionTimes;
        _expectQuizOrder = 0;
        _inQuizOrder = YES;
        [self addListener];
    }
    return self;
}

- (void)dealloc
{
    [self removeListener];
}

- (NTESQuiz *)quizById:(NSString *)quizId
{
    return [self.quizzes objectForKey:quizId];
}

- (void)addListener
{
    [[NIMSDK sharedSDK].chatManager addDelegate:self];
    [[NIMSDK sharedSDK].chatroomManager addDelegate:self];
}

- (void)removeListener
{
    [[NIMSDK sharedSDK].chatManager removeDelegate:self];
    [[NIMSDK sharedSDK].chatroomManager removeDelegate:self];
}

- (BOOL)canSubmitQuizOption
{
    //只有当前问题序号是0的时候，才可以提交问题，否则认为是中途加入的观众
    BOOL canSubmit = self.inQuizOrder && self.resurrectionTimes >= 0;
    if (!canSubmit)
    {
        DDLogInfo(@"can not submit quiz option, in order : %zd, resurrection times %zd ",self.inQuizOrder, self.resurrectionTimes);
    }
    return canSubmit;
}

- (void)submitMyQuizOption:(NTESQuizOption *)option
                completion:(void (^)(NSError *, NIMAnswerResult))completion
{
    __weak typeof(self) weakSelf = self;
    [[NTESQuizService sharedService] submitMyQuizOption:option roomId:self.chatroom.roomId completion:^(NSError *error, NIMAnswerResult result) {
        NTESQuiz *quiz = [weakSelf quizById:option.fromQuizId];
        if (error || (!error && result == NIMAnswerResultInvalid))
        {
            //网络出错或者答案无效说明是在服务端超时了，此时当做没有选
            quiz.myOptionId = NSNotFound;
        }
        else
        {
            quiz.myOptionId = option.optionId;
        }
        if (completion)
        {
            completion(error,result);
        }
    }];
}


#pragma mark - NIMChatManagerDelegate

- (void)onRecvMessages:(NSArray<NIMMessage *> *)messages
{
    for (NIMMessage *message in messages)
    {
        if (message.messageType == NIMMessageTypeCustom && [message.from isEqualToString:self.chatroom.creator])
        {
            [self dealWithMessage:(NIMCustomObject *)message.messageObject];
        }
    }
}

- (void)dealWithMessage:(NIMCustomObject *)object
{
    id<NIMCustomAttachment> attach = object.attachment;
    if ([attach isKindOfClass:[NTESQuizAttachment class]])
    {
        NTESQuizAttachment *attatchment = (NTESQuizAttachment *)attach;
        [self dealWithQuiz:attatchment];
    }
    else if([attach isKindOfClass:[NTESFinalResultrAttachment class]])
    {
        NTESFinalResultrAttachment *attatchment = (NTESFinalResultrAttachment *)attach;
        attatchment.roomId = object.message.session.sessionId;
        [self dealWithFinalResult:attatchment];
    }
}


- (void)dealWithQuiz:(NTESQuizAttachment *)attatchment
{
    __weak typeof(self) weakSelf = self;
    if (attatchment.quiz.rightOptionId == NSNotFound)
    {
        //没答案，抛出收到问题的回调
        if (self.inQuizOrder)
        {
            //看下答题顺序，如果当前题号是期待题号，则说明是从第一道题开始答的并且中间没有漏题
            self.inQuizOrder = (attatchment.quiz.order == self.expectQuizOrder);
        }
        
        DDLogInfo(@"receive quiz : %@",attatchment.quiz);
        if (!self.inQuizOrder)
        {
            DDLogInfo(@"quiz is not in order, current quiz order: %zd, expect quiz order: %zd",attatchment.quiz.order,self.expectQuizOrder);
        }
        
        [weakSelf.dispatcher addDispatchBlock:^{
            [weakSelf.quizzes setObject:attatchment.quiz forKey:attatchment.quiz.quizId];
            if ([weakSelf.delegate respondsToSelector:@selector(onReceiveQuiz:)])
            {
                [weakSelf.delegate onReceiveQuiz:attatchment.quiz];
            }
        } time:attatchment.timestamp];
    }
    else
    {
        //有答案，抛出答案回调
        //回调成之前的问题对象
        NTESQuiz *quiz = [self quizById:attatchment.quiz.quizId];
        if (quiz)
        {
            //有说明之前有答过这道题
            
            quiz.rightOptionId = attatchment.quiz.rightOptionId;
            [quiz update:attatchment.quiz];            
            attatchment.quiz = quiz;
            
            //判断是否正确影响复活次数
            if (quiz.rightOptionId != quiz.myOptionId && self.inQuizOrder)
            {
                _resurrectionTimes--;
                DDLogInfo(@"wrong answer! use resurrection chance, current time %zd",_resurrectionTimes);
            }
            
            //等待下一题的题号
            self.expectQuizOrder++;
        }
        
        DDLogInfo(@"receive answer : %@",attatchment.quiz);
        
        [weakSelf.dispatcher addDispatchBlock:^{
            [weakSelf.quizzes setObject:attatchment.quiz forKey:attatchment.quiz.quizId];
            if ([weakSelf.delegate respondsToSelector:@selector(onReceiveAnswer:)])
            {
                [weakSelf.delegate onReceiveAnswer:attatchment.quiz];
            }
        } time:attatchment.timestamp];
    }
}

- (void)dealWithFinalResult:(NTESFinalResultrAttachment *)attatchment
{
    __weak typeof(self) weakSelf = self;
    NTESChatroom *chatroom = [[NTESUserManager sharedManager] chatroomById:attatchment.roomId];
    BOOL isWin = (chatroom.quizCount == self.expectQuizOrder) && (self.resurrectionTimes >= 0);
    attatchment.result.isWin = isWin;
    [weakSelf.dispatcher addDispatchBlock:^{
        [weakSelf.quizzes removeAllObjects];
        //最后的统计结果
        DDLogInfo(@"receive final result : %@",attatchment.result);
        DDLogInfo(@"is win %zd, expect quiz order %zd, resurrection times %zd",isWin,self.expectQuizOrder,self.resurrectionTimes);
        if ([weakSelf.delegate respondsToSelector:@selector(onReceiveFinalResult:)])
        {
            [weakSelf.delegate onReceiveFinalResult:attatchment.result];
        }
    } time:attatchment.timestamp];
}


#pragma mark - NIMChatroomManagerDelegate

- (void)chatroom:(NSString *)roomId connectionStateChanged:(NIMChatroomConnectionState)state
{
    DDLogInfo(@"room id %@ connect changed to state %zd ",roomId,state);
}


@end
