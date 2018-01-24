//
//  NTESLivePlayerViewController.m
//  NIMQuizGame
//
//  Created by emily on 11/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESLivePlayerViewController.h"
#import "NELivePlayer.h"
#import "NELivePlayerController.h"
#import "NTESDDLogManager.h"

@interface NTESLivePlayerViewController ()

@property(nonatomic, assign) NSTimeInterval duration;
@property(nonatomic, assign) BOOL isPaused;
@property(nonatomic, assign) BOOL needRecover;
@property(nonatomic, strong) NSString *pullUrl;
@property(nonatomic, strong) id<NELivePlayer> livePlayer;
@property(nonatomic, strong) UIView *containerView;


@end

@implementation NTESLivePlayerViewController

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    DDLogVerbose(@"NTESLivePlayer dealloc");
    if (self.livePlayer) {
        [self releasePlayer];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.needRecover = YES;
    [self setupSubviews];
    [self setupNotification];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [SVProgressHUD dismiss];
}

- (void)setupSubviews {
    self.view.backgroundColor = [UIColor yellowColor];
}

- (void)setupNotification {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(NELivePlayerDidPreparedToPlay:)
                                                 name:NELivePlayerDidPreparedToPlayNotification
                                               object:nil];
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(NELivePlayerloadStateChanged:)
                                                 name:NELivePlayerLoadStateChangedNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(NELivePlayerPlayBackFinished:)
                                                 name:NELivePlayerPlaybackFinishedNotification
                                               object:nil];
}

- (void)playWithURL:(NSString *)pullUrl inView:(UIView *)containerView {
    DDLogInfo(@"init player");
    self.pullUrl =  pullUrl;
    self.containerView = containerView;
    
    [self.livePlayer.view removeFromSuperview];
    self.livePlayer = [self setupPlayerWithURL:[NSURL URLWithString:self.pullUrl]];
    
    if (self.livePlayer == nil) {
        DDLogInfo(@"live player init failed");
        [SVProgressHUD showWithStatus:@"缓冲中..."];
    }
    else {
        [containerView addSubview:self.livePlayer.view];
        self.livePlayer.view.frame = containerView.bounds;
        if (![self.livePlayer isPreparedToPlay]) {
            [SVProgressHUD showWithStatus:@"缓冲中..."];
            //准备播放
            [self.livePlayer prepareToPlay];
        }
        [self.livePlayer setSyncTimestampListenerWithIntervalMS:300 callback:^(NSTimeInterval realTime) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[NSNotificationCenter defaultCenter] postNotificationName:NTES_CURRENT_TIMETAG_NOTIFICATION object:nil userInfo:@{@"realTime": @(realTime)}];
            });
        }];
    }
}

#pragma mark - Player Notification
- (void)NELivePlayerDidPreparedToPlay:(NSNotification *)notification {
    if (notification.object != self.livePlayer) return ;
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    [SVProgressHUD dismiss];
    [self.livePlayer play];
}

- (void)NELivePlayerPlayBackFinished:(NSNotification *)notification {
    [SVProgressHUD dismiss];
    NSInteger status = [[[notification userInfo] valueForKey:NELivePlayerPlaybackDidFinishReasonUserInfoKey] integerValue];
    switch (status) {
        case NELPMovieFinishReasonPlaybackEnded:
        {
            DDLogInfo(@"NELPMovieFinishReasonPlaybackEnded");
            [self performSelector:@selector(retry) withObject:nil afterDelay:5];
        }
            break;
        case NELPMovieFinishReasonPlaybackError:
        {
            NSError *error = [NSError errorWithDomain:@"PlayerDomain" code:-1000 userInfo:@{NTES_ERROR_MSG_KEY: @"播放出错"}];
            DDLogInfo(@"NELPMovieFinishReasonPlaybackError:%@", error);
            [self performSelector:@selector(retry) withObject:nil afterDelay:5];
        }
            break;
        case NELPMovieFinishReasonUserExited:
        default:
            DDLogInfo(@"NELPMovieFinishReasonUserExited");
            break;
    }
}

- (void)NELivePlayerloadStateChanged:(NSNotification *)notification {
    DDLogInfo(@"缓冲状态改变...");
    NELPMovieLoadState nelpState = self.livePlayer.loadState;
    switch (nelpState) {
        case NELPMovieLoadStatePlaythroughOK:
        {
            [NSObject cancelPreviousPerformRequestsWithTarget:self];
            [SVProgressHUD dismiss];
        }
            break;
        case NELPMovieLoadStateStalled:
        {
            [SVProgressHUD showWithStatus:@"缓冲中..."];
        }
            break;
        default:
            break;
    }
}

#pragma mark - 网络切换重连

- (void)retry {
    DDLogInfo(@"NTESLivePlayer retry");
    //重连先销毁
    [self releasePlayer];
    //再开始
    [self playWithURL:self.pullUrl inView:self.containerView];
}

#pragma mark - Private

- (NELivePlayerController *)setupPlayerWithURL:(NSURL *)pullURL {
    DDLogInfo(@"NEPlayer version [%@]", [NELivePlayerController getSDKVersion]);
    DDLogInfo(@"UUID : %@", [[[UIDevice currentDevice] identifierForVendor] UUIDString]);
    NSError *error = nil;
    NELivePlayerController *player = [[NELivePlayerController alloc] initWithContentURL:pullURL error:&error];
    [NELivePlayerController setLogLevel:NELP_LOG_SILENT];
    if (player == nil) {
        NSString *toast = [NSString stringWithFormat:@"播放器初始化失败，失败原因：[%zi]", error.code];
        [self.view makeToast:toast duration:2.f position:CSToastPositionCenter];
        return nil;
    }
    [player setBufferStrategy:NELPLowDelay];
    [player setScalingMode:NELPMovieScalingModeAspectFit];
    [player setShouldAutoplay:YES];
    [player setHardwareDecoder:NO];
    [player setPauseInBackground:NO];
    [player setPlaybackTimeout:10 * 1000];
    [player setMute:NO];
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    return player;
}

- (void)releasePlayer {
    DDLogInfo(@"releasePlayer");
    [self.livePlayer.view removeFromSuperview];
    [self.livePlayer shutdown];
    self.livePlayer = nil;
    [UIApplication sharedApplication].idleTimerDisabled  = NO;
}


@end
