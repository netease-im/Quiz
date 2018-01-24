//
//  NTESFinalResultView.m
//  NIMQuizGame
//
//  Created by emily on 16/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESFinalResultView.h"
#import "NTESWinnerInfoCellTableViewCell.h"

@interface NTESFinalResultView () <UITableViewDelegate, UITableViewDataSource>

@property(nonatomic, strong) UITableView *tableView;

@property(nonatomic, strong) UIView *backView;

@property(nonatomic, strong) NSDictionary *winnerInfo;

@end

@implementation NTESFinalResultView

- (instancetype)initWithFrame:(CGRect)frame winnerInfo:(NSDictionary *)winnerInfo {
    if (self = [super initWithFrame:frame]) {
        self.winnerInfo = winnerInfo;
        [self doInit];
    }
    return self;
}

- (void)doInit {
    if (self.winnerInfo.count > 0) {
        [self addSubview:self.tableView];
        self.tableView.frame = self.bounds;
    }
    else {
        [self addSubview:self.backView];
    }
}

#pragma mark - tableViewDelegate & dataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.winnerInfo.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NTESWinnerInfoCellTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell"];
    NSArray *tmp = self.winnerInfo.allKeys;
    NSString *userID = tmp[indexPath.row];
    NSString *bonus = [self.winnerInfo valueForKey:userID];
    [cell configCellwithUserID:userID andBonus:bonus];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 50;
}

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:self.bounds style:UITableViewStylePlain];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.backgroundColor = [UIColor clearColor];
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.estimatedSectionHeaderHeight = 0;
        _tableView.estimatedSectionFooterHeight = 0;
        [_tableView setSeparatorInset:UIEdgeInsetsZero];
        [_tableView setLayoutMargins:UIEdgeInsetsZero];
        [_tableView registerClass:[NTESWinnerInfoCellTableViewCell class] forCellReuseIdentifier:@"cell"];
    }
    return _tableView;
}

- (UIView *)backView {
    if (!_backView) {
        _backView = [[UIView alloc] initWithFrame:CGRectMake(40, 20, self.width - 80, self.height - 40)];
        UIImage *bgImg = [UIImage imageNamed:@"final_result_none"];
        _backView.layer.contents = (id)bgImg.CGImage;
    }
    return _backView;
}

@end
