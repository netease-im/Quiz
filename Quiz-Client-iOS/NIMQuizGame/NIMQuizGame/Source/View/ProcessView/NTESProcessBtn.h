//
//  NTESProcessBtn.h
//  ShortVideo_Demo
//
//  Created by Netease on 17/2/17.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol NTESProcessBtnProtocol;

@interface NTESProcessBtn : UIView

@property (nonatomic, assign) CGFloat duration;

@property (nonatomic, weak) id <NTESProcessBtnProtocol> delegate;

@property (nonatomic, copy) NSString *titleStr;

- (void)showBtn:(BOOL)isShown;

- (void)stopProgressAnimation;

- (void)startProgressAnimationwithDuration:(CGFloat)duration;

@end


@protocol NTESProcessBtnProtocol <NSObject>

- (void)NTESProcessBtnDidStart:(NTESProcessBtn *)processBtn;

- (void)NTESProcessBtnDidStop:(NTESProcessBtn *)processBtn;

@end
