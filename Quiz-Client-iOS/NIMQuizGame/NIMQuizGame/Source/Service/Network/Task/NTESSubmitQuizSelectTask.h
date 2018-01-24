//
//  NTESSubmitQuizSelectTask.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESNetworkTask.h"

@interface NTESSubmitQuizSelectTask : NSObject<NTESNetworkTask>

@property (nonatomic, copy) NSString *roomId;

@property (nonatomic, copy) NSString *quizId;

@property (nonatomic, assign) NSInteger answerId;

@end
