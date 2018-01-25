//
//  NTESQuiz.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NTESQuizOption : NSObject

/* 属于的问题 id */
@property (nonatomic, copy) NSString *fromQuizId;

/* 回答选项号，必须从0累加，如果只有三个选项必须分别是0，1，2，作答的时候以0，1，2作对	应。*/
@property (nonatomic, assign) NSInteger optionId;

/* 选项内容 */
@property (nonatomic, copy) NSString *optionText;

/* 作答人数，在通知结果时有效 */
@property (nonatomic, assign) NSInteger selectNumber;

- (instancetype)initWithJson:(NSDictionary *)json;

@end

@interface NTESQuiz : NSObject

/* 问题 Id */
@property (nonatomic, copy) NSString *quizId;

/* 问题题号 */
@property (nonatomic, assign) NSInteger order;

/* 问题内容 */
@property (nonatomic, copy) NSString *content;

/* 问题选项集合 */
@property (nonatomic, copy) NSArray<NTESQuizOption *> *options;

/* 正确答案，只在通知结果时有效，否则为 NSNotFound */
@property (nonatomic, assign) NSInteger rightOptionId;

/* 我选择的答案，存储位， 默认为 NSNotFound */
@property (nonatomic, assign) NSInteger myOptionId;

- (instancetype)initWithJson:(NSDictionary *)json;

//收到正确的包含答案的 quiz 之后的更新方法
- (void)update:(NTESQuiz *)answer;

@end
