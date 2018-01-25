//
//  NTESWinnerInfoCellTableViewCell.m
//  NIMQuizGame
//
//  Created by emily on 16/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESWinnerInfoCellTableViewCell.h"

@interface NTESWinnerInfoCellTableViewCell ()

@property(nonatomic, strong) UIImageView *avatar;
@property(nonatomic, strong) UILabel *idLabel;
@property(nonatomic, strong) UILabel *bonusAmount;
@property(nonatomic, strong) UIView *line;

@end

@implementation NTESWinnerInfoCellTableViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        [self setSeparatorInset:UIEdgeInsetsZero];
        [self setLayoutMargins:UIEdgeInsetsZero];
        [@[self.avatar,
           self.idLabel,
           self.bonusAmount,
           self.line] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
               [self addSubview:view];
           }];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.backgroundColor = [UIColor clearColor];
    self.avatar.left = 20;
    self.avatar.centerY = self.contentView.centerY;
    self.idLabel.centerY = self.contentView.centerY;
    self.bonusAmount.centerY = self.contentView.centerY;
    self.idLabel.left = self.avatar.right + 10;
    self.bonusAmount.right = self.contentView.width - 30;
    self.line.bottom = self.contentView.bottom;
}

- (void)configCellwithUserID:(NSString *)userID andBonus:(NSString *)bonusNumer {
    self.idLabel.text = [NSString stringWithFormat:@"%@", userID];
    self.bonusAmount.text = [NSString stringWithFormat:@"%@元", bonusNumer];
    [self setNeedsLayout];
}

#pragma mark - Private

- (UIImageView *)avatar {
    if (!_avatar) {
        _avatar = [UIImageView new];
        _avatar.size = CGSizeMake(36, 36);
        _avatar.image = [UIImage imageNamed:@"default_avatar"];
    }
    return _avatar;
}

- (UILabel *)idLabel {
    if (!_idLabel) {
        _idLabel = [UILabel new];
        _idLabel.size = CGSizeMake(80, 21);
        _idLabel.font = [UIFont systemFontOfSize:15];
        _idLabel.textColor = [UIColor whiteColor];
    }
    return _idLabel;
}

- (UILabel *)bonusAmount {
    if (!_bonusAmount) {
        _bonusAmount = [UILabel new];
        _bonusAmount.size = CGSizeMake(100, 21);
        _bonusAmount.adjustsFontSizeToFitWidth = YES;
        _bonusAmount.font = [UIFont systemFontOfSize:15];
        _bonusAmount.textColor = [UIColor whiteColor];
        _bonusAmount.textAlignment = NSTextAlignmentRight;
    }
    return _bonusAmount;
}

- (UIView *)line {
    if (!_line) {
        _line = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.width, 1)];
        _line.backgroundColor = [UIColor colorWithWhite:0.5 alpha:0.5];
    }
    return _line;
}

@end
