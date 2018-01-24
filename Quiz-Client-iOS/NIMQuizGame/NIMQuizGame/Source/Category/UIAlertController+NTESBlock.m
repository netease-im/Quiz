//
//  UIAlertController+NTESBlock.m
//
//  Created by chris on 18-1-9.
//  Copyright (c) 2018年 Netease. All rights reserved.
//

#import "UIAlertController+NTESBlock.h"

@implementation UIAlertController (NTESBlock)
- (UIAlertController *)addAction:(NSString *)title
                           style:(UIAlertActionStyle)style
                         handler:(void (^ __nullable)(UIAlertAction *action))handler
{
    UIAlertAction *action = [UIAlertAction actionWithTitle:title style:style handler:handler];
    [self addAction:action];
    return self;
}

- (void)show
{
    UIViewController *vc = [UIApplication sharedApplication].keyWindow.rootViewController;
    [vc presentViewController:self animated:YES completion:nil];
}
@end
