//
//  NTESCustomAttachmentDecoder.m
//  NIM
//
//  Created by amao on 7/2/15.
//  Copyright (c) 2015 Netease. All rights reserved.
//

#import "NTESCustomAttachmentDecoder.h"
#import "NSDictionary+NTESJson.h"
#import "NTESAttachment.h"

#define CMType @"cmd"
#define CMData @"data"

typedef NS_ENUM(NSInteger, NTESCMType){
    NTESCMTypeQuiz   = 1,   //问题题目
    NTESCMTypeAnswer = 4,   //问题答案
    NTESCMTypeFinalResult = 5,  //最终的获奖结果
};

@implementation NTESCustomAttachmentDecoder

- (id<NIMCustomAttachment>)decodeAttachment:(NSString *)content
{
    id attachment = nil;
    DDLogInfo(@"receive custom decode content : %@",content);
    NSData *data = [content dataUsingEncoding:NSUTF8StringEncoding];
    if (data) {
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data
                                                             options:0
                                                               error:nil];
        if ([dict isKindOfClass:[NSDictionary class]])
        {
            NSInteger type     = [dict jsonInteger:CMType];
            NSDictionary *data = [dict jsonDict:CMData];
            switch (type)
            {
                case NTESCMTypeQuiz:
                case NTESCMTypeAnswer:
                {
                    attachment = [[NTESQuizAttachment alloc] initWithJson:data];
                }
                    break;
                case NTESCMTypeFinalResult:
                {
                    attachment = [[NTESFinalResultrAttachment alloc] initWithJson:data];
                }
                    break;
                default:
                    break;
            }
        }
    }
    return attachment;
}


@end
