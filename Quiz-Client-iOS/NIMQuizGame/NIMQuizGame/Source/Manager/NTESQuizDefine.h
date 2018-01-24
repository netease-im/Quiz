//
//  NTESQuizDefine.h
//  NIMQuizGame
//
//  Created by chris on 2018/1/15.
//  Copyright © 2018年 chris. All rights reserved.
//

#ifndef NTESQuizDefine_h
#define NTESQuizDefine_h

typedef NS_ENUM(NSInteger, NIMAnswerResult) {
    NIMAnswerResultWrong, // 回答错误
    NIMAnswerResultRight, // 回答正确
    NIMAnswerResultInvalid, // 回答无效
};


typedef NS_ENUM(NSInteger, NTESChatroomLiveStatus) {
    NTESChatroomLiveStatusFree, //空闲
    NTESChatroomLiveStatusLiving, //直播中
    NTESChatroomLiveStatusForbidden, //禁用
    NTESChatroomLiveStatusLivingAndRecording, //直播录制中
};

#endif /* NTESQuizDefine_h */
