//
//  NTESQuizCard.m
//  NIMQuizGame
//
//  Created by emily on 15/01/2018.
//  Copyright © 2018 chris. All rights reserved.
//

#import "NTESQuizCard.h"
#import "NTESProcessBtn.h"
#import "NTESQuiz.h"

@interface NTESQuizCard() <NTESProcessBtnProtocol>

@property(nonatomic, assign) NTESQuizCardState cardState;

@property(nonatomic, strong) NTESProcessBtn *processBtn;

@property(nonatomic, strong) UILabel *questionLabel;

@property(nonatomic, strong) NSMutableArray *answers;

@property(nonatomic, strong) NSMutableArray *selectNumbers;

@property(nonatomic, strong) NTESQuiz *quiz;

@end

@implementation NTESQuizCard

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self doInit];
    }
    return self;
}

- (void)doInit {
    self.layer.cornerRadius = 10;
    self.backgroundColor = [UIColor whiteColor];
    _answers = [[NSMutableArray alloc] init];
    _selectNumbers = [[NSMutableArray alloc] init];
    [self addSubview:self.processBtn];
    [self addSubview:self.questionLabel];
}

- (CGSize)sizeThatFits:(CGSize)size
{
    [self layoutIfNeeded];
    UIButton *button = self.answers.lastObject;
    CGFloat height = button.bottom + 40;
    return CGSizeMake(self.width, height);
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.processBtn.top = 30;
    self.processBtn.left = self.width/2 - 30;
    self.questionLabel.top = self.processBtn.bottom + 20;
    self.questionLabel.left = 38;
    CGFloat top = self.questionLabel.bottom + 30;
    for (NSInteger i = 0; i < self.answers.count; i++)
    {
        UIButton *button = self.answers[i];
        UILabel *label   = self.selectNumbers[i];
        button.left = 38;
        button.top  = top;
        top = button.bottom + 10;
        label.centerY = button.height * .5f;
        label.right   = button.width - 18;
    }
}

- (void)startProgressAnimationwithDuration:(CGFloat)duration {
    [self.processBtn startProgressAnimationwithDuration:duration];
}


- (void)resetOptions:(NSArray *)options
{
    NSArray *answers = [NSArray arrayWithArray:self.answers];
    if (options.count > answers.count)
    {
        //按钮不够，添加
        for (int i = 0; i < (options.count - answers.count); i++)
        {
            UIButton *button = [self genButton];
            UILabel *label   = [self genLabel];
            [self.answers addObject:button];
            [self.selectNumbers addObject:label];
            [self addSubview:button];
            [button addSubview:label];
        }
    }
    if (options.count < answers.count)
    {
        //按钮太少，移除
        for (int i = 0; i < (answers.count - options.count); i++)
        {
            UIView  *button  = self.answers.lastObject;
            UILabel *label  = self.selectNumbers.lastObject;
            [button removeFromSuperview];
            [label removeFromSuperview];
            [self.answers removeLastObject];
            [self.selectNumbers removeLastObject];
        }
    }
}

- (void)configCardwithQuiz:(NTESQuiz *)quiz CardState:(NTESQuizCardState)cardState canAnswer:(BOOL)canAnswer andCount:(NSInteger)quizCount {
    self.quiz = quiz;
    self.cardState = cardState;
    self.questionLabel.text = [NSString stringWithFormat:@"%lu/%lu\n\n%@", self.quiz.order + 1, quizCount, self.quiz.content];
    
    [self resetOptions:quiz.options];
    
    for (int i = 0; i < self.quiz.options.count; ++i) {
        NTESQuizOption *option = self.quiz.options[i];
        NSString *answerStr = [NSString stringWithFormat:@"%@", option.optionText];
        UIButton *btn = self.answers[i];
        [btn setTitle:answerStr forState:UIControlStateNormal];
    }
    switch (self.cardState) {
        case NTESQuizCardStateInGame:
        {
            [self clearBtnState];
            self.processBtn.hidden = NO;
        }
            break;
        case NTESQuizCardStateResult:
        {
            for (UIButton *btn in self.answers) {
                btn.userInteractionEnabled = NO;
            }
            self.processBtn.hidden = YES;
            [self.selectNumbers enumerateObjectsUsingBlock:^(UILabel *label, NSUInteger idx, BOOL * _Nonnull stop) {
                   NTESQuizOption *option = self.quiz.options[idx];
                   label.hidden = NO;
                   label.text = [NSString stringWithFormat:@"%lu人", option.selectNumber];
               }];
            UIButton *rightBtn = self.answers[quiz.rightOptionId];
            rightBtn.layer.borderWidth = 5;
            rightBtn.layer.borderColor = UIColorFromRGB(0xA3D239).CGColor;
            if (quiz.rightOptionId == quiz.myOptionId) {
                [self makeToast:@"恭喜你，回答正确～" duration:2. position:CSToastPositionCenter];
            }
            else {
                if (quiz.myOptionId >= 0 && quiz.myOptionId < self.answers.count)
                {
                    UIButton *myBtn = self.answers[quiz.myOptionId];
                    myBtn.layer.borderColor = UIColorFromRGB(0xFA6C49).CGColor;
                    myBtn.layer.borderWidth = 5;
                }
            }
        }
            break;
        default:
            break;
    }
    if (!canAnswer) {
        [self disableBtnInteraction];
    }
}

- (void)disableBtnInteraction {
    [self.answers enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
           btn.userInteractionEnabled = NO;
       }];
}

- (void)clearBtnState {
    [self.answers enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
           btn.selected = NO;
           btn.layer.borderColor = UIColorFromRGB(0xA2A6AE).CGColor;
           btn.layer.borderWidth = 2;
           btn.userInteractionEnabled = YES;
           btn.selected = NO;
       }];
    [self.selectNumbers enumerateObjectsUsingBlock:^(UILabel *label, NSUInteger idx, BOOL * _Nonnull stop) {
           label.hidden = YES;
       }];
}

#pragma mark - ProcessBtn delegate

- (void)NTESProcessBtnDidStart:(NTESProcessBtn *)processBtn {
    [self.answers enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
            btn.selected = NO;
            btn.userInteractionEnabled = YES;
       }];
}

- (void)NTESProcessBtnDidStop:(NTESProcessBtn *)processBtn {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:NTESQuizCardDismissNotification object:nil];
    });
    [self.answers enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
           btn.userInteractionEnabled = NO;
       }];
}

#pragma mark - actions

- (void)answerSelect:(UIButton *)sender {
    NSInteger index = [self.answers indexOfObject:sender];
    [self disableMultiSelect:index];
    if ([self.quizCardDelegate respondsToSelector:@selector(answerSelected:withOption:)]) {
        [self.quizCardDelegate answerSelected:self withOption:self.quiz.options[index]];
    }
}

- (void)disableMultiSelect:(NSInteger)index {
    [self.answers enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
        btn.userInteractionEnabled = NO;
        if (idx == index)
        {
            btn.selected = YES;
        }
        else
        {
            btn.selected = NO;
        }
    }];
}

#pragma mark - Private

- (NTESProcessBtn *)processBtn {
    if (!_processBtn) {
        _processBtn = [[NTESProcessBtn alloc] init];
        _processBtn.size = CGSizeMake(60, 60);
        _processBtn.delegate = self;
    }
    return _processBtn;
}

- (UILabel *)questionLabel {
    if (!_questionLabel) {
        _questionLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.width - 76 * UISreenWidthScale, 100)];
        _questionLabel.numberOfLines = 0;
        _questionLabel.font = [UIFont systemFontOfSize:19];
        _questionLabel.textColor = [UIColor blackColor];
        _questionLabel.textAlignment = NSTextAlignmentCenter;
        _questionLabel.adjustsFontSizeToFitWidth = YES;
    }
    return _questionLabel;
}


- (UIButton *)genButton
{
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    button.size = CGSizeMake(self.width - 76, 56);
    button.titleLabel.font = [UIFont systemFontOfSize:15];
    button.titleLabel.textColor = [UIColor blackColor];
    [button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateSelected];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateSelected | UIControlStateHighlighted];
    button.layer.borderColor = UIColorFromRGB(0xA2A6AE).CGColor;
    button.layer.borderWidth = 2;
    button.layer.cornerRadius = 28;
    button.clipsToBounds = YES;
    [button setBackgroundImage:[UIImage imageWithColor:[UIColor colorWithWhite:1. alpha:1.] size:button.size] forState:UIControlStateNormal];
    [button setBackgroundImage:[UIImage imageWithColor:[UIColor colorWithWhite:0.5 alpha:1.] size:button.size] forState:UIControlStateSelected];
    [button addTarget:self action:@selector(answerSelect:) forControlEvents:UIControlEventTouchUpInside];
    return button;
}

- (UILabel *)genLabel
{
    UILabel *label = [UILabel new];
    label.size = CGSizeMake(60, 21);
    label.textColor = [UIColor lightGrayColor];
    label.font = [UIFont systemFontOfSize:12.f];
    label.textAlignment = NSTextAlignmentRight;
    label.adjustsFontSizeToFitWidth = YES;
    label.hidden = YES;
    return label;
}

@end
