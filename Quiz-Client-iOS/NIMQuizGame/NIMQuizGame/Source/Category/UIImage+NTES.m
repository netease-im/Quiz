//
//  UIImage+NTES.m
//  LiveStream_IM_Demo
//
//  Created by Netease on 17/1/9.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "UIImage+NTES.h"
#import <objc/runtime.h>

@implementation UIImage (NTES)

- (void)setHandelEvent:(void (^)(id))handelEvent
{
    objc_setAssociatedObject(self, @selector(handelEvent), handelEvent, OBJC_ASSOCIATION_COPY_NONATOMIC);
}

- (void (^)(id))handelEvent
{
    return objc_getAssociatedObject(self, _cmd);
}

+ (UIImage *)imageWithColor:(UIColor *)color size:(CGSize)size
{
    CGRect rect = CGRectMake(0, 0, size.width, size.height);
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(context,
                                   color.CGColor);
    CGContextFillRect(context, rect);
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

@end
