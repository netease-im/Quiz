//
//  NTESTopBar.h
//  NIMQuizGame
//
//  Created by emily on 12/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import <UIKit/UIKit.h>

@class NTESChatroom;
@class NTESTopBar;
@protocol NTESTopBarProtocol <NSObject>

- (void)topBarExitAction:(NTESTopBar *)topBar;

@end

@interface NTESTopBar : UIView

@property(nonatomic, weak) id<NTESTopBarProtocol> delegate;

@property(nonatomic, strong) NSString *roomId;

@property(nonatomic, assign) NSInteger bonus;

- (void)cofigTopBarWithRoomId:(NSString *)roomId andBonus:(NSInteger)bonus;

- (void)refreshTopBarWithOnlineUser:(NSInteger)count;

- (void)refreshTopBarWithRenewCount:(NSInteger)renewCount;

@end
