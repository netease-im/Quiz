//
//  NTESLiveGameViewController.h
//  NIMQuizGame
//
//  Created by emily on 12/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESLivePlayerViewController.h"
#import "NTESChatroom.h"
@interface NTESLiveGameViewController : NTESLivePlayerViewController

@property(nonatomic, copy) NSString *roomId;

@property(nonatomic, copy) NSString *pullUrl;

- (instancetype)initWithLiveroom:(NTESChatroom *)room;

@end
