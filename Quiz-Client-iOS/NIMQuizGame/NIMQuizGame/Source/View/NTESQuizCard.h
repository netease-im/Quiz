//
//  NTESQuizCard.h
//  NIMQuizGame
//
//  Created by emily on 15/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import <UIKit/UIKit.h>

#define NTESQuizCardDismissNotification @"NTESQuizCardDismissNotification"

typedef NS_ENUM(NSInteger, NTESQuizCardState) {
    NTESQuizCardStateInGame,
    NTESQuizCardStateResult,
};

typedef NS_ENUM(NSInteger, NTESQuizBtnStatus) {
    NTESQuizBtnStatusCorrect,
    NTESQuizBtnStatusWrong,
};

@class NTESQuizCard;
@class NTESQuiz;
@class NTESQuizOption;

@protocol NTESQuizCardDelegate <NSObject>

- (void)answerSelected:(NTESQuizCard *)card withOption:(NTESQuizOption *)option;

@end


@interface NTESQuizCard : UIView

@property(nonatomic, assign) BOOL canAnswer;

@property(nonatomic, weak) id<NTESQuizCardDelegate> quizCardDelegate;

- (void)startProgressAnimationwithDuration:(CGFloat)duration;

- (void)configCardwithQuiz:(NTESQuiz *)quiz CardState:(NTESQuizCardState)cardState canAnswer:(BOOL)canAnswer andCount:(NSInteger)quizCount;

@end
