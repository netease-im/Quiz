//
//  NTESQuiz.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/11.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESQuiz.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESQuiz

- (instancetype)initWithJson:(NSDictionary *)json
{
    self = [super init];
    if (self)
    {
        _quizId        = [json jsonString:@"questionId"];
        _content       = [json jsonString:@"question"];
        _order         = [json jsonInteger:@"order"];
        _rightOptionId = [json objectForKey:@"rightAnswer"]? [json jsonInteger:@"rightAnswer"] : NSNotFound;
        _myOptionId    = NSNotFound;
        NSArray *items = [json jsonArray:@"options"];
        NSMutableArray *options = [[NSMutableArray alloc] init];
        for (NSDictionary *item in items)
        {
            NTESQuizOption *option = [[NTESQuizOption alloc] initWithJson:item];
            option.fromQuizId = _quizId;
            [options addObject:option];
        }
        _options = [NSArray arrayWithArray:options];
    }
    return self;
}

- (void)update:(NTESQuiz *)answer
{
    //更新正确答案
    self.rightOptionId = answer.rightOptionId;
    
    //更新每个选项卡里的答题人数
    for (NSInteger i=0; i<self.options.count; i++)
    {
        if (answer.options.count > i)
        {
            NTESQuizOption *myOption    = self.options[i];
            NTESQuizOption *answerOption = answer.options[i];
            myOption.selectNumber = answerOption.selectNumber;
        }
        else
        {
            DDLogInfo(@"warning: not enough answer options! my options :%@ , answer options:%@",self.options,answer.options);
        }
    }
    
}

- (NSString *)description
{
    NSString *rightOption = _rightOptionId == NSNotFound? @"NOT FOUND" : @(_rightOptionId).stringValue;
    NSString *myOption    = _myOptionId    == NSNotFound? @"NOT FOUND" : @(_myOptionId).stringValue;
    
    NSString *options = @"";
    
    for (NTESQuizOption *option in _options)
    {
        options = [options stringByAppendingFormat:@"\n%@",option];
    }
    
    return [NSString stringWithFormat:@"{ id:%@, content:%@, order:%zd, right option id:%@, my option id:%@, options :%@ }",_quizId,_content,_order,rightOption,myOption,options];
}

@end



@implementation NTESQuizOption

- (instancetype)initWithJson:(NSDictionary *)json
{
    self = [super init];
    if (self)
    {
        _optionId = [json jsonInteger:@"optionId"];
        _optionText = [json jsonString:@"content"];
        _selectNumber = [json jsonInteger:@"selectCount"];
    }
    return self;
}

- (NSString *)description
{
    return  [NSString stringWithFormat:@"{ id:%zd, from quiz id:%@, content:%@, select player number:%zd }",_optionId,_fromQuizId,_optionText,_selectNumber];
}

@end
