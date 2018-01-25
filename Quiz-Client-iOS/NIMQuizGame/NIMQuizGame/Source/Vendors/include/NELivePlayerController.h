/*
 * NELivePlayerController.h
 * NELivePlayer
 *
 * Create by biwei on 15-9-21
 * Copyright (c) 2015年 Netease. All rights reserved
 *
 * This file is part of LivePlayer.
 *
 */

#import "NELivePlayer.h"

/************************************初始化方法使用*******************************************************************************
*  initWithContentURL:error: 方法 = （new/init/initWithNeedConfigAudioSession:）方法 + setPlayUrl: 方法                           
*
*  初始化->准备播放的调用流程如下:
*
*  1）使用initWithContentURL:error:初始化                         2）使用new/init/initWithNeedConfigAudioSession:初始化
*             |                                                                  |
*        set 参数 A -> set 参数 B ... -> set 参数 N                           setPlayUrl: (首先设置)
*                                    |                                           |
*                             prepareToPlay方法                              set 参数 A -> set 参数 B ... -> set 参数 N
*                                                                                                             |
*                                                                                                       prepareToPlay方法
*
*******************************************************************************************************************************/

/**
 *	@brief	播放器核心功能类
 */
@interface NELivePlayerController : NSObject <NELivePlayer>

/**
 *    @brief    初始化播放器
 *
 *    @param     isNeed 是否需要内部配置audiosession
 *
 *    @return    返回播放器实例
 */
- (instancetype)initWithNeedConfigAudioSession:(BOOL)isNeed;

/**
 *	@brief	初始化播放器，输入播放文件路径
 *
 *	@param 	aUrl 	播放文件的路径
 *  @param 	error 	初始化错误原因
 *
 *	@return	返回播放器实例
 */
- (id)initWithContentURL:(NSURL *)aUrl
                   error:(NSError **)error;

/**
 *    @brief    初始化播放器，输入播放文件路径
 *
 *    @param     aUrl     播放文件的路径
 *    @param     error     初始化错误原因
 *
 *    @return    返回播放器实例
 */
- (id)initWithContentURL:(NSURL *)aUrl
                   error:(NSError *__autoreleasing *)error
             logCallBack:(void (^)(NSString *logMsg))logCallback;

/**
 *	@brief	初始化播放器，输入播放文件路径
 *
 *	@param 	aUrl 	播放文件的路径
 *	@param 	isNeed 	是否需要内部配置audio session
 *  @param 	error 	初始化错误原因
 *
 *	@return	返回播放器实例
 */
- (id)initWithContentURL:(NSURL *)aUrl
  needConfigAudioSession:(BOOL)isNeed
                   error:(NSError **)error;

/**
 *    @brief    初始化播放器，输入播放文件路径
 *
 *    @param     aUrl           播放文件的路径
 *    @param     isNeed         是否需要内部配置audio session
 *    @param     error          初始化错误原因
 *    @param     logCallBack    日志记录回调
 *
 *    @return    返回播放器实例
 */
- (id)initWithContentURL:(NSURL *)aUrl
  needConfigAudioSession:(BOOL)isNeed
                   error:(NSError *__autoreleasing *)error
             logCallBack:(void (^)(NSString *logMsg))logCallback;

/**
 *	@brief	设置log级别
 *
 *	@param 	logLevel 	log级别
 *
 *	@return	无
 */
+ (void)setLogLevel:(NELPLogLevel)logLevel;

/**
 *	@brief	获取当前SDK版本号
 *
 *	@return	SDK版本号
 */
+ (NSString *)getSDKVersion;

/**
 *	@brief	获取当前日志的路径
 *
 *  @warning 需要对日志操作，请在当前实例析构前使用日志以确保日志存在，不可删除该路径下的日志。
 *
 *	@return	SDK版本号
 */
+ (NSString *)getLogPath;

@end
