//
//  NTESResult.m
//  NIMQuizGame
//
//  Created by chris on 2018/1/15.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESResult.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESResult

- (instancetype)initWithJson:(NSDictionary *)json
{
    self = [super init];
    if (self)
    {
        _winnerCount  = [json jsonInteger:@"winnerCount"];
        _bonus        = [json jsonDouble:@"bonus"];
        _winnerSample = [json jsonArray:@"winnerSample"];        
    }
    return self;
}


- (NSString *)description
{
    return [NSString stringWithFormat:@"{ winner count %zd, bonus %zd, _winnerSample %@ }",_winnerCount,_bonus,_winnerSample];
}

@end
