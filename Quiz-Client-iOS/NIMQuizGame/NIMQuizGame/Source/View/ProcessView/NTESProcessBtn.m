//
//  NTESProcessBtn.m
//  ShortVideo_Demo
//
//  Created by Netease on 17/2/17.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESProcessBtn.h"

#define degreesToRadians(x) ((x) * M_PI / 180.0)

@interface NTESProcessBtn ()

@property (nonatomic, strong) UIButton *btn;

@property (nonatomic, strong) CAShapeLayer *progressLayer;

@property (nonatomic, strong) UILabel *titleLab;

@property (nonatomic, assign) CGFloat residueTime;

@property (nonatomic, strong) dispatch_source_t timer;

@end

@implementation NTESProcessBtn

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self doInit];
    }
    return self;
}

- (void)doInit
{
    self.duration = 10.0; //默认值
    [self showBtn:YES];
    [self addSubview:self.btn];
    [self.layer addSublayer:self.progressLayer];
    [self addSubview:self.titleLab];
}

#pragma mark - Public
- (void)startProgressAnimationwithDuration:(CGFloat)duration
{
    //进度动画
    self.duration = duration;
    CABasicAnimation *pathAnimation = [CABasicAnimation animationWithKeyPath:@"strokeEnd"];
    pathAnimation.duration = self.duration + 0.5;
    pathAnimation.fromValue = @(0.0);
    pathAnimation.toValue = @(1.0);
    pathAnimation.removedOnCompletion = YES;
    [self.progressLayer addAnimation:pathAnimation forKey:@"processAnimation"];
    
    //倒计时数字
    _residueTime = _duration + 0.5;
    __weak typeof(self) weakSelf = self;
    _timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0));
    dispatch_source_set_timer(_timer, dispatch_walltime(NULL, 0), 1 * NSEC_PER_SEC, 0 * NSEC_PER_SEC);
    dispatch_source_set_event_handler(_timer, ^{
         __strong typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf.residueTime <= 0.1)
        {
            [strongSelf cancelTimer];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                strongSelf.titleLab.text = @"0";
                
                if (strongSelf.delegate && [strongSelf.delegate respondsToSelector:@selector(NTESProcessBtnDidStop:)]) {
                    [strongSelf.delegate NTESProcessBtnDidStop:strongSelf];
                }
            });
        }
        else
        {
            strongSelf.residueTime -= 1.0;
            
            dispatch_async(dispatch_get_main_queue(), ^{
                strongSelf.titleLab.text = [NSString stringWithFormat:@"%lu", (unsigned long)strongSelf.residueTime];
            });
        }
    });
    dispatch_resume(_timer);
}

- (void)stopProgressAnimation
{
    //停止进度动画
    [self.progressLayer removeAnimationForKey:@"processAnimation"];
    
    //防止太小被计时器中所回收
    self.residueTime = 2.0;
    
    [self cancelTimer];

    if (_delegate && [_delegate respondsToSelector:@selector(NTESProcessBtnDidStop:)]) {
        [_delegate NTESProcessBtnDidStop:self];
    }
}

- (void)cancelTimer
{
    if (_timer)
    {
        dispatch_source_cancel(_timer);
        _timer = nil;
    }
}

#pragma mark - Private
- (void)layoutSubviews
{
    if (!CGRectEqualToRect(self.btn.frame, self.bounds)) {
        self.btn.frame = CGRectMake(-1, -1, self.width + 3, self.height + 3);
        self.btn.layer.cornerRadius = self.btn.width/2;
        
        self.progressLayer.frame = self.layer.bounds;
        
        self.titleLab.frame = CGRectMake(0, 0, self.width, _titleLab.height);
        self.titleLab.centerY = self.height/2;
        
        UIBezierPath *path = [self makePath];
        self.progressLayer.path = path.CGPath;
    }
}

- (void)showBtn:(BOOL)isShown
{
    if (!isShown)
    {
        if (!self.btn.isHidden)
        {
            [UIView animateWithDuration:0.5 animations:^{
                self.btn.alpha = 0.0;
            } completion:^(BOOL finished) {
                self.btn.hidden = YES;
            }];
        }
    }
    else
    {
        if (self.btn.isHidden)
        {
            self.btn.hidden = NO;
            [UIView animateWithDuration:0.25 animations:^{
                self.btn.alpha = 1.0;
            }];
        }
    }
}

- (UIBezierPath *)makePath
{
    CGFloat width = CGRectGetWidth(self.frame)/2.0f;
    CGFloat height = CGRectGetHeight(self.frame)/2.0f;
    CGPoint centerPoint = CGPointMake(width, height);
    float radius = CGRectGetWidth(self.frame)/2;
    
    return [UIBezierPath bezierPathWithArcCenter:centerPoint
                                          radius:radius
                                      startAngle:degreesToRadians(-90)
                                        endAngle:degreesToRadians(270)
                                       clockwise:YES];
}

#pragma mark - Action
- (void)btnAction:(UIButton *)btn
{
    //开始
    if (self.delegate && [self.delegate respondsToSelector:@selector(NTESProcessBtnDidStart:)]) {
        [self.delegate NTESProcessBtnDidStart:self];
    }
}

#pragma mark - Setter
- (void)setDuration:(CGFloat)duration
{
    _duration = duration;
    
    _residueTime = duration + 0.5;
    
    self.titleLab.text = [NSString stringWithFormat:@"%lu", (unsigned long)_duration];
}

- (void)setTitleStr:(NSString *)titleStr
{
    _titleStr = (titleStr ?: @"");
    
    [_btn setTitle:titleStr forState:UIControlStateNormal];
}


#pragma mark - Getter
- (UIButton *)btn
{
    if (!_btn) {
        _btn = [UIButton buttonWithType:UIButtonTypeCustom];
        _btn.backgroundColor = UIColorFromRGB(0xF3F3F3);
        _btn.layer.borderWidth = 5;
        _btn.layer.borderColor = UIColorFromRGB(0xF5A623).CGColor;
        [_btn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _btn.titleLabel.font = [UIFont systemFontOfSize:48.0];
        _btn.titleEdgeInsets = UIEdgeInsetsMake(0, 15, 0, 15);
        [_btn addTarget:self action:@selector(btnAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _btn;
}

- (CAShapeLayer *)progressLayer
{
    if (!_progressLayer) {
        _progressLayer = [CAShapeLayer layer];
        _progressLayer.fillColor = [UIColor clearColor].CGColor;
        _progressLayer.lineWidth = 8.0f;
        _progressLayer.lineCap = kCALineCapRound;
        _progressLayer.strokeColor = [UIColor whiteColor].CGColor;
        _progressLayer.strokeStart = 0.f;
    }
    return _progressLayer;
}

- (UILabel *)titleLab
{
    if (!_titleLab) {
        _titleLab = [[UILabel alloc] init];
        _titleLab.text = [NSString stringWithFormat:@"%lu", (unsigned long)self.duration];
        _titleLab.textColor = [UIColor blackColor];
        _titleLab.textAlignment = NSTextAlignmentCenter;
        _titleLab.adjustsFontSizeToFitWidth = YES;
        _titleLab.font = [UIFont boldSystemFontOfSize:18.0];
        [_titleLab sizeToFit];
    }
    return _titleLab;
}

@end
