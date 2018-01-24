//
//  NTESLiveGameViewController.m
//  NIMQuizGame
//
//  Created by emily on 12/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESLiveGameViewController.h"
#import "NTESChatView.h"
#import "NTESTextInputView.h"
#import "NTESChatView.h"
#import "NTESTopBar.h"
#import "NTESQuizCard.h"
#import "NTESQuizManager.h"
#import "NTESTimerHolder.h"
#import "NTESFinalResultView.h"
#import "NTESUserManager.h"
#import "NTESSolutionConfig.h"

#define chatViewHeight 178

@interface NTESLiveGameViewController () <NIMLoginManagerDelegate,NIMChatManagerDelegate, NIMChatroomManagerDelegate, NTESTextInputViewDelegate, NTESTextInputViewDelegate, NTESTopBarProtocol, NTESQuizCardDelegate, NTESInfoManagerDelegate, NTESTimerHolderDelegate>

@property(nonatomic, strong) UIView *containerView;

@property(nonatomic, strong) NTESChatView *chatView;

@property(nonatomic, strong) NTESTextInputView *textInputView;

@property(nonatomic, strong) UIButton *chatBtn;

@property(nonatomic, strong) NTESTopBar *topBar;

@property(nonatomic, strong) NTESQuizCard *quizCard;

@property(nonatomic, strong) NTESTimerHolder *timer;

@property(nonatomic, strong) NTESFinalResultView *resultView;

@property(nonatomic, strong) NTESQuizManager *manager;

@property(nonatomic, strong) UILabel *finalResultLabel;

@property(nonatomic, assign) NSInteger quizCount;

@end

@implementation NTESLiveGameViewController

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

- (void)dealloc {
    [[NIMSDK sharedSDK].loginManager removeDelegate:self];
    [[NIMSDK sharedSDK].chatManager removeDelegate:self];
    [[NIMSDK sharedSDK].chatroomManager removeDelegate:self];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (instancetype)initWithLiveroom:(NTESChatroom *)room{
    if (self = [super initWithNibName:nil bundle:nil]) {
        _roomId = room.roomId;
        _pullUrl = room.rtmpPullUrl1;
        _manager = [[NTESQuizManager alloc] initWithChatroom:room];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupSubviews];
    [self addListener];
#if TARGET_IPHONE_SIMULATOR
#else
    [self setupPlayer];
#endif
    [self hideKeyboardGesture];
    [self setupTimer];
}

- (void)hideKeyboard {
    [self.textInputView myResignFirstResponder];
}

- (void)hideKeyboardGesture {
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideKeyboard)];
    [self.view addGestureRecognizer:tap];
}

- (void)setupTimer {
    self.timer = [[NTESTimerHolder alloc] init];
    [self.timer startTimer:30 delegate:self repeats:YES];
}

- (void)setupSubviews {
    UIImage *enterBgImg = [UIImage imageNamed:@"liveGame_background"];
    self.view.layer.contents = (id)enterBgImg.CGImage;
    [self.view addSubview:self.containerView];
    [self.view addSubview:self.topBar];
    [self.view addSubview:self.chatView];
    [self.view addSubview:self.quizCard];
    [self.view addSubview:self.textInputView];
    [self.view addSubview:self.chatBtn];
    [self.view addSubview:self.finalResultLabel];
    NSInteger renewCount = self.manager.resurrectionTimes >= 0 ? self.manager.resurrectionTimes : 0;
    NTESChatroom *chatroom = [[NTESUserManager sharedManager] chatroomById:self.roomId];
    self.quizCount = chatroom.quizCount;
    [self.topBar cofigTopBarWithRoomId:self.roomId andBonus:chatroom.bonus];
    [self.topBar refreshTopBarWithOnlineUser:chatroom.onlineUserCount];
    [self.topBar refreshTopBarWithRenewCount:renewCount];
}

- (void)addListener {
    self.manager.delegate = self;
    [[NIMSDK sharedSDK].loginManager addDelegate:self];
    [[NIMSDK sharedSDK].chatManager addDelegate:self];
    [[NIMSDK sharedSDK].chatroomManager addDelegate:self];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillChange:) name:UIKeyboardWillChangeFrameNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(dismissQuizCard:) name:NTESQuizCardDismissNotification object:nil];
}

- (void)setupPlayer {
    [self playWithURL:self.pullUrl inView:self.containerView];
}

- (void)onNTESTimerFired:(NTESTimerHolder *)holder {
    [[NIMSDK sharedSDK].chatroomManager fetchChatroomInfo:self.roomId completion:^(NSError * _Nullable error, NIMChatroom * _Nullable chatroom) {
        [self.topBar refreshTopBarWithOnlineUser:chatroom.onlineUserCount];
    }];
}

- (void)keyboardWillChange:(NSNotification *)notification {
    NSDictionary *userInfo = notification.userInfo;
    CGFloat textAdjustDistance = 0.f;
    CGFloat chatAdjustDistance = 0.f;
    CGRect endFrame = [userInfo[UIKeyboardFrameEndUserInfoKey] CGRectValue];
    BOOL visible = endFrame.origin.y != [UIApplication sharedApplication].keyWindow.frame.size.height;
    
    if (visible) {
        textAdjustDistance = endFrame.size.height;
        chatAdjustDistance = textAdjustDistance + self.textInputView.height;
        WEAK_SELF(weakSelf);
        [UIView animateWithDuration:0.2 animations:^{
            if (self.resultView) {
                self.resultView.alpha = 0;
            }
            weakSelf.textInputView.hidden = NO;
            weakSelf.textInputView.frame = CGRectMake(0, weakSelf.view.height - textAdjustDistance - 36, weakSelf.view.width, 36);
            weakSelf.chatView.frame = CGRectMake(10, weakSelf.view.bottom - chatViewHeight - 10 - chatAdjustDistance , weakSelf.view.width - 60, chatViewHeight);
            [weakSelf.view layoutIfNeeded];
        }];
    }
    else {
        WEAK_SELF(weakSelf)
        [UIView animateWithDuration:0.2 animations:^{
            if (self.resultView) {
                self.resultView.alpha = 1;
            }
            weakSelf.textInputView.hidden = YES;
            weakSelf.chatView.frame = CGRectMake(10, weakSelf.view.bottom - chatViewHeight - 10, weakSelf.view.width - 60, chatViewHeight);
        }];
    }
}

- (void)btnAction:(UIButton *)sender {
    [self.textInputView myFirstResponder];
}

- (void)dismissQuizCard:(NSNotification *)noti {
    self.quizCard.hidden = YES;
    [UIApplication sharedApplication].statusBarHidden = NO;
}

- (void)chatroom:(NSString *)roomId beKicked:(NIMChatroomKickReason)reason {
    if (roomId != self.roomId) return;
    DDLogInfo(@"on kick by chatroom id :%@, reaseon %zd",roomId,reason);
    [self doExitifKicked:YES];
}

- (void)onKick:(NIMKickReason)code clientType:(NIMLoginClientType)clientType
{
    DDLogInfo(@"on kick by im link, reaseon %zd, client %zd", code, clientType);
    [self doExitifKicked:YES];
}

#pragma mark - quizCard Delegate

- (void)answerSelected:(NTESQuizCard *)card withOption:(NTESQuizOption *)option {
    [self.manager submitMyQuizOption:option completion:^(NSError *error, NIMAnswerResult result) {
        if (error) {
            NSString *toastStr = [NSString stringWithFormat:@"网络原因，该次回答无效 ：("];
            [self.view makeToast:toastStr duration:2. position:CSToastPositionCenter];
        }
    }];
}

#pragma mark - quizManager  && NTESInfoManager Delegate

- (void)onReceiveQuiz:(NTESQuiz *)quiz {
    [self refreshTopBar];
    self.quizCard.hidden = NO;
    [UIApplication sharedApplication].statusBarHidden = YES;
    BOOL canAnswer = [self.manager canSubmitQuizOption];
    [self.quizCard configCardwithQuiz:quiz CardState:NTESQuizCardStateInGame canAnswer:canAnswer andCount:self.quizCount];
    [self.quizCard sizeToFit];
    [self.quizCard startProgressAnimationwithDuration:[NTESSolutionConfig config].quizCardQuizCountdown];
}

- (void)onReceiveAnswer:(NTESQuiz *)quiz {
    [self refreshTopBar];
    self.quizCard.hidden = NO;
    [UIApplication sharedApplication].statusBarHidden = YES;
    [self.quizCard startProgressAnimationwithDuration:[NTESSolutionConfig config].quizCardAnswerCountdown];
    BOOL canAnswer = [self.manager canSubmitQuizOption];
    [self.quizCard configCardwithQuiz:quiz CardState:NTESQuizCardStateResult canAnswer:canAnswer andCount:self.quizCount];
    [self.quizCard sizeToFit];
}

- (void)onReceiveFinalResult:(NTESResult *)result {
    self.containerView.hidden = YES;
    NSDictionary *winnerInfo = @{}.mutableCopy;
    if (result.winnerCount > 0) {
        for (NSString *winnerId in result.winnerSample) {
            NSString *bonus = [NSString stringWithFormat:@"%.1f", result.bonus];
            [winnerInfo setValue:bonus forKey:winnerId];
        }
    }
    if (result.isWin) {
        self.finalResultLabel.hidden = NO;
        self.finalResultLabel.backgroundColor = UIColorFromRGB(0xFA6C49);
        self.finalResultLabel.text = [NSString stringWithFormat:@"恭喜你冲关成功！\n赢取奖金%.1f元", result.bonus];
    } else {
        self.finalResultLabel.hidden = NO;
        self.finalResultLabel.backgroundColor = [UIColor lightGrayColor];
        self.finalResultLabel.text = @"很遗憾~冲关失败!";
    }
    self.resultView = [[NTESFinalResultView alloc] initWithFrame:CGRectMake(35, 200, self.view.width - 70, 250) winnerInfo:winnerInfo];
    [self.view addSubview:self.resultView];
}

- (void)refreshTopBar {
    NSInteger count = self.manager.resurrectionTimes < 0 ? 0 : self.manager.resurrectionTimes;
    [self.topBar refreshTopBarWithRenewCount:count];
}

#pragma mark - override

- (void)doExitifKicked:(BOOL)isKicked {
    [self.view endEditing:YES];
    [self releasePlayer];
    if (isKicked) {
        NSString *toast = [NSString stringWithFormat:@"您已被踢出房间"];
        [self.view makeToast:toast duration:2. position:CSToastPositionCenter];
        [self dismissViewControllerAnimated:YES completion:nil];
    }else {
        [[NIMSDK sharedSDK].chatroomManager exitChatroom:self.roomId completion:^(NSError * _Nullable error) {
            [self dismissViewControllerAnimated:YES completion:nil];
        }];
    }
}

#pragma mark - NTESTextInputDelegate

- (void)inputView:(NTESTextInputView *)inputView didSendText:(NSString *)text {
    if (text.length == 0) {
        [self.view makeToast:@"不能发送空消息哦" duration:2. position:CSToastPositionCenter];
        return ;
    }
    else {
        NIMMessage *message = [[NIMMessage alloc] init];
        message.text        = text;
        NIMSession *session = [NIMSession session:self.roomId type:NIMSessionTypeChatroom];
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
    }
}

- (void)inputView:(NTESTextInputView *)inputView didChangeHeight:(CGFloat)height {
    if (height != self.textInputView.height)
    {
        CGFloat y = self.textInputView.bottom;
        CGFloat width = self.textInputView.width;
        WEAK_SELF(weakSelf);
        [UIView animateWithDuration:0.1 animations:^{
            weakSelf.textInputView.frame = CGRectMake(0, y - height, width, height);
        }];
    }
}

#pragma mark - NIMChatManagerDelegate

- (void)willSendMessage:(NIMMessage *)message {
    switch (message.messageType)
    {
        case NIMMessageTypeText: //普通消息
        {
            NSString *userId = [NIMSDK sharedSDK].loginManager.currentAccount;
            NTESUser *me = [NTESUser new];
            me = [[NTESUserManager sharedManager] userById:userId];
            NTESTextMessage *msg = [NTESTextMessage textMessage:message.text sender:me];
            [self.chatView addNormalMessages:@[msg]];
            break;
        }
        default:
            break;
    }
}

- (void)onRecvMessages:(NSArray *)messages
{
    for (NIMMessage *message in messages)
    {
        if (![message.session.sessionId isEqualToString:self.roomId]
            && message.session.sessionType == NIMSessionTypeChatroom)
        {
            return; //不属于这个聊天室的消息
        }
        switch (message.messageType)
        {
            case NIMMessageTypeText: //普通消息
            {
                [self doReceiveTextMessage:message];
                break;
            }
            default:
                break;
        }
    }
}

- (void)doReceiveTextMessage:(NIMMessage *)message {
    NIMMessageChatroomExtension *ext = message.messageExt;
    NSString *nick = ext.roomNickname;
    NTESUser *user = [NTESUser new];
    user.nick = nick;
    user.userId = message.from;
    NTESTextMessage *msg = [NTESTextMessage textMessage:message.text sender:user];
    [self.chatView addNormalMessages:@[msg]];
}

#pragma mark - TopBar Delegate

- (void)topBarExitAction:(NTESTopBar *)topBar {
    [self doExitifKicked:NO];
}

#pragma mark - Private

- (UIView *)containerView {
    if (!_containerView) {
        CGSize containerSize = [self getFrame];
        CGFloat x = (self.view.width - containerSize.width) / 2;
        CGFloat y = (self.view.height - containerSize.height) / 2;
        _containerView = [[UIView alloc] initWithFrame:CGRectMake(x, y, containerSize.width, containerSize.height)];
    }
    return _containerView;
}

- (NTESTextInputView *)textInputView {
    if (!_textInputView) {
        _textInputView = [[NTESTextInputView alloc] initWithFrame:CGRectMake(0, self.containerView.height - 36, self.containerView.width, 36)];
        _textInputView.hidden = YES;
        _textInputView.delegate = self;
    }
    return _textInputView;
}

- (NTESChatView *)chatView {
    if (!_chatView) {
        _chatView = [[NTESChatView alloc] initWithFrame:CGRectMake(10, self.view.bottom - 188, self.view.width - 60, chatViewHeight)];
    }
    return _chatView;
}

- (UIButton *)chatBtn {
    if (!_chatBtn) {
        _chatBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _chatBtn.frame = CGRectMake(self.view.width - 48, self.view.bottom - 48, 38, 38);
        [_chatBtn setBackgroundImage:[UIImage imageNamed:@"liveGame_chat"] forState:UIControlStateNormal];
        [_chatBtn addTarget:self action:@selector(btnAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _chatBtn;
}

- (NTESTopBar *)topBar {
    if (!_topBar) {
        _topBar = [[NTESTopBar alloc] initWithFrame:CGRectMake(0, statusBarHeight, self.view.width, 90)];
        _topBar.backgroundColor = [UIColor clearColor];
        _topBar.delegate = self;
    }
    return _topBar;
}

- (NTESQuizCard *)quizCard {
    if (!_quizCard) {
        _quizCard = [[NTESQuizCard alloc] initWithFrame:CGRectMake(20, -10, self.view.width - 40, 390 * UISreenWidthScale)];
        _quizCard.hidden = YES;
        _quizCard.quizCardDelegate = self;
    }
    return _quizCard;
}

- (UILabel *)finalResultLabel {
    if (!_finalResultLabel) {
        _finalResultLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width/2 - 100, 100, 200, 70)];
        _finalResultLabel.size = CGSizeMake(205, 70);
        _finalResultLabel.layer.cornerRadius = 5;
        _finalResultLabel.font = [UIFont boldSystemFontOfSize:19];
        _finalResultLabel.textAlignment = NSTextAlignmentCenter;
        _finalResultLabel.adjustsFontSizeToFitWidth = YES;
        _finalResultLabel.textColor = [UIColor whiteColor];
        _finalResultLabel.hidden = YES;
        _finalResultLabel.numberOfLines = 2;
    }
    return _finalResultLabel;
}

- (CGSize)getFrame {
    CGSize size = CGSizeZero;
    CGFloat width = self.view.width;
    CGFloat height = self.view.width * 16 / 9;
    if (height > self.view.height) {
        width = 9 * self.view.height / 16;
        size = CGSizeMake(width, self.view.height);
    }
    else {
        size = CGSizeMake(self.view.width, height);
    }
    return size;
}

@end
