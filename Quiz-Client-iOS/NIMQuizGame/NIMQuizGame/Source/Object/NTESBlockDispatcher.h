//
//  NTESBlockDispatcher.h
//  NIMQuizGame
//
//  Created by chrisRay on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void(^NTESDispatchBlock)(void);

@interface NTESBlockDispatcher : NSObject

- (void)addDispatchBlock:(NTESDispatchBlock)block
                    time:(NSTimeInterval)timestamp;

@end
