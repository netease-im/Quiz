//
//  NTESQueryRoomInfoTask.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/12.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESNetworkTask.h"

@interface NTESQueryRoomInfoTask : NSObject<NTESNetworkTask>

@property (nonatomic, copy) NSString *roomId;

@end
