//
//  AppDelegate.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "AppDelegate.h"
#import <NIMSDK/NIMSDK.h>
#import "NTESSolutionConfig.h"
#import "NTESChatroomEntranceViewController.h"
#import "NTESCustomAttachmentDecoder.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    [self.window makeKeyAndVisible];
    
    [[NIMSDK sharedSDK] enableConsoleLog];
    [[NIMSDK sharedSDK] registerWithAppID:[NTESSolutionConfig config].appKey cerName:nil];
    
    [NIMCustomObject registerCustomDecoder:[NTESCustomAttachmentDecoder new]];
    
    NTESChatroomEntranceViewController *vc = [[NTESChatroomEntranceViewController alloc] initWithNibName:nil bundle:nil];
    self.window.rootViewController = vc;
    return YES;
}




@end
