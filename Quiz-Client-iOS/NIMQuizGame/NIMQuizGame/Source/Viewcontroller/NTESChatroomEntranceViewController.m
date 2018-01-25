//
//  NTESChatroomEntranceViewController.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESChatroomEntranceViewController.h"
#import "NTESQuizManager.h"
#import "UIAlertController+NTESBlock.h"
#import "UIView+Toast.h"
#import "NTESLiveGameViewController.h"
#import "NTESUserManager.h"

@interface NTESChatroomEntranceViewController ()

@property(nonatomic, strong) UIImageView *logoView;
@property(nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, strong) UITextField *textField;
@property(nonatomic, strong) UIView *line;
@property(nonatomic, strong) UILabel *hintLabel;
@property(nonatomic, strong) UIButton *enterBtn;

@end

@implementation NTESChatroomEntranceViewController

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [self doLogin];
    [self setupSubviews];
    [self hideKeyboardGesture];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [self.view endEditing:YES];
}

- (void)doLogin {
    __weak typeof(self) weakSelf = self;
    [[NTESUserManager sharedManager] login:^(NSError *error) {
        if (error)
        {
            //登录失败，杀掉进程
            NSString *msg = [NSString stringWithFormat:@"登录失败 ： %zi，关闭应用", error.code];
            
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:msg message:nil preferredStyle:UIAlertControllerStyleAlert];
            [alert addAction:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [weakSelf exitApp];
            }];
            [alert show];
        }
        else
        {
            [weakSelf.view makeToast:@"登录成功" duration:2.0f position:CSToastPositionCenter];
        }
    }];
}

- (void)exitApp {
    id<UIApplicationDelegate> app = [UIApplication sharedApplication].delegate;
    UIWindow *window = app.window;
    [UIView animateWithDuration:0.3 animations:^{
        window.alpha = 0;
        window.frame = CGRectMake(0, window.bounds.size.width, 0, 0);
        exit(0);
    }];
}

- (void)setupSubviews {
    self.view.backgroundColor = UIColorFromRGB(0x252647);
    [@[self.logoView, self.titleLabel,
       self.textField, self.line,
       self.hintLabel, self.enterBtn] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
           [self.view addSubview:view];
       }];
}

- (void)viewDidLayoutSubviews {
    self.logoView.centerX = self.view.centerX;
    self.logoView.top = self.view.top + 100;
    self.logoView.width = 110 * UISreenWidthScale;
    self.logoView.height = 110 * UISreenWidthScale;
    self.titleLabel.top = self.logoView.bottom + 14;
    self.titleLabel.centerX = self.view.centerX;
    self.textField.centerX = self.view.centerX;
    self.textField.centerY = self.view.centerY + 10;
    self.line.top = self.textField.bottom;
    self.line.left = self.textField.left;
    self.hintLabel.top = self.textField.bottom + 10;
    self.enterBtn.top = self.hintLabel.bottom + 30;
}

- (void)hideKeyboard {
    [self.textField resignFirstResponder];
}

- (void)hideKeyboardGesture {
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideKeyboard)];
    [self.view addGestureRecognizer:tap];
}

- (void)textChanged:(UITextField *)sender {
    if (self.textField.text.length != 0) {
        self.line.backgroundColor = UIColorFromRGB(0x4B74FF);
    }else {
        self.line.backgroundColor = UIColorFromRGB(0x9298A3);
    }
    self.enterBtn.enabled = self.textField.text.length != 0;
}

- (void)enterBtnAction:(UIButton *)sender {
    NIMChatroomEnterRequest *request = [[NIMChatroomEnterRequest alloc] init];
    request.roomId = self.textField.text;
    DDLogInfo(@"start enter chatroom...");
    __weak typeof(self) weakSelf = self;
    [[NIMSDK sharedSDK].chatroomManager enterChatroom:request completion:^(NSError * _Nullable error, NIMChatroom * _Nullable chatroom, NIMChatroomMember * _Nullable me) {
        DDLogInfo(@"enter chatroom complete error : %@",error);
        if (!error)
        {
            [[NTESUserManager sharedManager] queryRoomInfo:chatroom.roomId completion:^(NSError *error, NTESChatroom *chatroom) {
                if (!error) {
                    NTESLiveGameViewController *gameVC = [[NTESLiveGameViewController alloc] initWithLiveroom:chatroom];
                    [weakSelf presentViewController:gameVC animated:YES completion:nil];
                }
            }];
        }
        else
        {
            [weakSelf.view makeToast:@"房间号错误，可在主播端DEMO中获取" duration:2.0 position:CSToastPositionCenter];
        }
    }];
}

- (void)longPress:(UITapGestureRecognizer *)gestureRecognizer
{
    if (gestureRecognizer.state == UIGestureRecognizerStateEnded)
    {
        NSArray *paths = [NTESDDLogManager fileLogger].logFileManager.sortedLogFilePaths;
        __weak typeof(self) weakSelf = self;
        [[NIMSDK sharedSDK].resourceManager upload:paths.firstObject progress:nil completion:^(NSString * _Nullable urlString, NSError * _Nullable error) {
            if (!error)
            {
                [UIPasteboard generalPasteboard].string = urlString;
                [weakSelf.view makeToast:@"上传日志成功,URL已复制到剪切板中" duration:3.0 position:CSToastPositionCenter];
            }
            else
            {
                [weakSelf.view makeToast:@"上传失败" duration:2.0 position:CSToastPositionCenter];
            }
        }];
    }
}

#pragma mark - Private

- (UIImageView *)logoView {
    if (!_logoView) {
        _logoView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"enterRoom_logo"]];
        UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPress:)];
        [self.view addGestureRecognizer:longPress];
        

    }
    return _logoView;
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 200, 45)];
        _titleLabel.text = @"云信直播竞答DEMO\n[观众端]";
        _titleLabel.numberOfLines = 2;
        _titleLabel.textColor = [UIColor whiteColor];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.font = [UIFont systemFontOfSize:18.];
    }
    return _titleLabel;
}

- (UITextField *)textField {
    if (!_textField) {
        _textField = [[UITextField alloc] initWithFrame:CGRectMake(24, 0, self.view.width - 48, 32)];
        NSString *str = @" 请输入房间号";
        NSMutableAttributedString *attStr = [[NSMutableAttributedString alloc] initWithString:str];
        [attStr addAttribute:NSForegroundColorAttributeName value:[UIColor lightGrayColor] range:NSMakeRange(0, str.length)];
        [attStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:16.f] range:NSMakeRange(0, str.length)];
        _textField.attributedPlaceholder = attStr;
        _textField.keyboardType = UIKeyboardTypeNumberPad;
        _textField.textColor = [UIColor whiteColor];
        _textField.font = [UIFont systemFontOfSize:16];
        [_textField addTarget:self action:@selector(textChanged:) forControlEvents:UIControlEventEditingChanged];
    }
    return _textField;
}

- (UIView *)line {
    if (!_line) {
        _line = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.textField.width, 1)];
        _line.backgroundColor = UIColorFromRGB(0x9298A3);
    }
    return _line;
}

- (UILabel *)hintLabel {
    if (!_hintLabel) {
        _hintLabel = [[UILabel alloc] initWithFrame:CGRectMake(24, 0, self.view.width - 48, 14)];
        _hintLabel.text = @"可在“主播端DEMO”中获取房间号";
        _hintLabel.textColor = [UIColor lightGrayColor];
        _hintLabel.font = [UIFont systemFontOfSize:14.f];
    }
    return _hintLabel;
}

- (UIButton *)enterBtn {
    if (!_enterBtn) {
        _enterBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _enterBtn.frame = CGRectMake(24, 0, self.view.width - 48, 52);
        _enterBtn.backgroundColor = [UIColor blueColor];
        _enterBtn.layer.cornerRadius = 8.f;
        _enterBtn.titleLabel.font = [UIFont systemFontOfSize:18.f];
        [_enterBtn setTitle:@"进入房间" forState:UIControlStateNormal];
        [_enterBtn setBackgroundImage:[UIImage imageNamed:@"enterRoom_background"] forState:UIControlStateNormal];
        [_enterBtn setBackgroundImage:[UIImage imageNamed:@"enterRoom_background_disable"] forState:UIControlStateDisabled];
        [_enterBtn setBackgroundImage:[UIImage imageNamed:@"enterRoom_background_pressed"] forState:UIControlStateHighlighted];
        [_enterBtn addTarget:self action:@selector(enterBtnAction:) forControlEvents:UIControlEventTouchUpInside];
        _enterBtn.enabled = NO;
    }
    return _enterBtn;
}


@end
