//
//  NTESTopBar.m
//  NIMQuizGame
//
//  Created by emily on 12/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESTopBar.h"
#import "NTESChatroom.h"

#define backViewBackGroundColor [UIColor lightGrayColor]

@interface NTESTopBar()

@property(nonatomic, strong) UIImageView *avatarImgview;
@property(nonatomic, strong) UIView *backView;
@property(nonatomic, strong) UILabel *roomIDLabel;
@property(nonatomic, strong) UILabel *onlineCountLabel;
@property(nonatomic, strong) UIButton *exitBtn;
@property(nonatomic, strong) UILabel *extraInfoLabel;


@end

@implementation NTESTopBar

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self doInit];
    }
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self doInit];
    }
    return self;
}

- (void)doInit {
    [@[self.backView,
      self.exitBtn,
      self.extraInfoLabel] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
          [self addSubview:view];
      }];
    
    [@[self.avatarImgview,
       self.roomIDLabel,
       self.onlineCountLabel] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
           [self.backView addSubview:view];
       }];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.backView.left = self.left + 10 * UISreenWidthScale;
    self.backView.top = 0;
    self.exitBtn.centerY = self.backView.centerY;
    self.exitBtn.right = self.right - 10 * UISreenWidthScale;
    self.extraInfoLabel.left = self.left + 10 * UISreenWidthScale;
    self.extraInfoLabel.top = self.backView.bottom + 10;
    
    self.avatarImgview.top = 4;
    self.avatarImgview.left = 4;
    self.roomIDLabel.left = self.avatarImgview.right + 5;
    self.roomIDLabel.top = 5;
    self.onlineCountLabel.left = self.avatarImgview.right + 5;
    self.onlineCountLabel.top = self.roomIDLabel.bottom + 5;
}

- (void)cofigTopBarWithRoomId:(NSString *)roomId andBonus:(NSInteger)bonus {
    self.roomId = roomId;
    self.roomIDLabel.text = [NSString stringWithFormat:@"房间 ID: %@", self.roomId];
    self.bonus = bonus;
}

- (void)refreshTopBarWithOnlineUser:(NSInteger)count {
    self.onlineCountLabel.text = [NSString stringWithFormat:@"人数：%lu", count];

}

- (void)refreshTopBarWithRenewCount:(NSInteger)renewCount {
    self.extraInfoLabel.text = [NSString stringWithFormat:@"奖金总额 ： %lu元\n复活剩余 ： %lu次", self.bonus, renewCount];
}


- (void)exitBtnAction:(UIButton *)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(topBarExitAction:)]) {
        [self.delegate topBarExitAction:self];
    }
}

#pragma mark - Private

- (UIView *)backView {
    if (!_backView) {
        _backView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 140 * UISreenWidthScale, 40)];
        _backView.layer.cornerRadius = 20;
        _backView.backgroundColor = UIColorFromRGBA(0x00000000, 0.3);
    }
    return _backView;
}

- (UIButton *)exitBtn {
    if (!_exitBtn) {
        _exitBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _exitBtn.frame = CGRectMake(0, 0, 20, 20);
        [_exitBtn setImage:[UIImage imageNamed:@"topBar_exit"] forState:UIControlStateNormal];
        [_exitBtn setImage:[UIImage imageNamed:@"topBar_exit"] forState:UIControlStateSelected];
        [_exitBtn addTarget:self action:@selector(exitBtnAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _exitBtn;
}

- (UILabel *)extraInfoLabel {
    if (!_extraInfoLabel) {
        _extraInfoLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 100 * UISreenWidthScale, 40)];
        _extraInfoLabel.numberOfLines = 2;
        _extraInfoLabel.textColor = [UIColor whiteColor];
        _extraInfoLabel.font = [UIFont systemFontOfSize:14.f];
        _extraInfoLabel.adjustsFontSizeToFitWidth = YES;
    }
    return _extraInfoLabel;
}

- (UIImageView *)avatarImgview {
    if (!_avatarImgview) {
        _avatarImgview = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 32, 32)];
        _avatarImgview.image = [UIImage imageNamed:@"default_avatar"];
    }
    return _avatarImgview;
}

- (UILabel *)roomIDLabel {
    if (!_roomIDLabel) {
        _roomIDLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 85 * UISreenWidthScale, 12)];
        _roomIDLabel.font = [UIFont systemFontOfSize:11];
        _roomIDLabel.adjustsFontSizeToFitWidth = YES;
        _roomIDLabel.textColor = [UIColor whiteColor];
        _roomIDLabel.text = [NSString stringWithFormat:@"房间 ID: 0"];
    }
    return _roomIDLabel;
}

- (UILabel *)onlineCountLabel {
    if (!_onlineCountLabel) {
        _onlineCountLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 85 * UISreenWidthScale, 12)];
        _onlineCountLabel.font = [UIFont systemFontOfSize:11.];
        _onlineCountLabel.adjustsFontSizeToFitWidth = YES;
        _onlineCountLabel.textColor = [UIColor whiteColor];
        _onlineCountLabel.text = [NSString stringWithFormat:@"人数：0"];
    }
    return _onlineCountLabel;
}

@end
