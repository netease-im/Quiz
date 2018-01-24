//
//  NTESLivePlayerViewController.h
//  NIMQuizGame
//
//  Created by emily on 11/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface NTESLivePlayerViewController : UIViewController

- (void)playWithURL:(NSString *)pullUrl inView:(UIView *)containerView;

- (void)releasePlayer;

@end
