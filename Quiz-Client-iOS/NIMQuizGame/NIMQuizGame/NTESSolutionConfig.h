//
//  NTESSolutionConfig.h
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/10.
//  Copyright © 2018年 chris. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NTESSolutionConfig : NSObject

/* app key */
@property (nonatomic,copy) NSString *appKey;

/* app server host */
@property (nonatomic,copy) NSString *appHost;

/* 复活次数 */
@property (nonatomic,assign) NSInteger resurrectionTimes;

/* 超时时间 */
@property(nonatomic, assign) CGFloat timeout;

/* 答题卡显示题目倒计时 */
@property(nonatomic, assign) CGFloat quizCardQuizCountdown;

/* 答题卡答案答题倒计时 */
@property(nonatomic, assign) CGFloat quizCardAnswerCountdown;


+ (instancetype)config;

@end
