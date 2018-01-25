//
//  NTESResult.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/15.
//  Copyright © 2018年 chris. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NTESResult : NSObject

/* 中奖人数 */
@property (nonatomic, assign) NSInteger winnerCount;

/* 优胜者赢得金额 */
@property (nonatomic, assign) float bonus;

/* 示例的用户 */
@property (nonatomic, strong) NSArray *winnerSample;

/* 是否中奖 */
@property (nonatomic, assign) BOOL isWin;


- (instancetype)initWithJson:(NSDictionary *)json;

@end
