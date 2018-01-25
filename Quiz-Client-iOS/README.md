## 网易云信直播竞答解决方案 iOS 端实现说明
### 一）终端整体业务逻辑介绍
该答题解决方案的流程推进需要 PC 端作为主播端进行推流，客户端作为观众端进行拉流并参与答题，同时根据答题情况，在客户端分为两种角色：`答题者`和`观众`。`答题者`可参与答题；在答错或者未及时进入答题流程的则被称为`观众`，`观众`不可答题，但仍可看题以及发送消息进行互动。
整体答题流程分为以下几个步骤：

1. 输入房间号进入房间，在房间内可一直进行聊天，如果主播已经开始直播即推流，则房间内有主播画面显示，若没有推流，则无画面。
2. 当主播端开始下发题目之后，若此时在房间内，则可以参与答题，若下发题目之后，仍未进入房间，则不能参与作答，解决方案共提供三道题作答。
3. 答题过程有一次自动复活机会，若答错一道题，会自动使用复活机会复活；若答错一道题，且用掉一次复活时，仍然答错，则判定为闯关失败，无法继续答题平分奖金。若答对所有题或者使用一次复活机会后，答对剩余题目，均判定为闯关成功，最终平分奖金。
4. 答题环节结束后，主播端会退出房间，拉流端也会收到回调，离开房间，一次答题流程结束。

### 二）答题场景重难点实现
#### 1. 拉流端画面实现
拉流端画面是一个播放器视图的封装，该播放器接入网易视频云的播放器 `NELivePlayerController` 和 `NELivePlayer`；上层封装在 `NTESLivePlayerViewController` 类中。

首先，通过 `NELivePlayerController` 的初始化接口，进行播放器初始化和播放参数配置，例如延时模式，是否自动播放等等。
其次需要监听播放器状态；通过广播监听播放器的状态并进行相关逻辑处理；
`NELivePlayerDidPreparedToPlayNotification` 这是播放器初始化文件完成后的通知，在该消息通知之后，播放器就可以开始播放；`NELivePlayerLoadStateChangedNotification` 监听视频加载状态，并作相应处理；`NELivePlayerPlaybackFinishedNotification` 是监听播放结束的状态。因为这个解决方案可能会有断网情况，所以，播放器在收到 `NELivePlayerPlaybackFinishedNotification` 状态后，会进行销毁播放器并重新初始化逻辑，保证在网络重新连上后可答题。

#### 2. 答题流程实现
通过监听聊天室消息回调，获得题目的下发，答案公布以及最终结果的公布消息，并附带上该消息的视频流时间戳，然后并将这些消息封装成回调 `NTESDispatchBlock`，并维护在一个按照时间戳由先到后进行排序的数组 `dispatchObjects` 里。同时，实时监听视频流的时间戳，去不断等待 dispatchObjects 数组里的回调。若 dispatchObjects 中存在了回调，则将流的时间戳和 dispatchObjects 里的第一个回调时间戳进行比对。若当前题目消息回调里的时间戳小于流时间戳则将该回调抛给上层，否则说明收到题目时已经超时，就丢弃该回调。从而实现了根据视频流时间戳显示题目或者答案或者不做显示的逻辑。

### 三）源码导读
#### 1. 工程说明
网易云信直播竞答解决方案的工程基于以下第三方库进行开发

* 项目依赖管理 [CocoaPods](https://cocoapods.org/)，版本 1.3.1
* 网易云信精简版本 [NIMSDK_LITE](http://netease.im/im-sdk-demo)，版本4.7.2
* 播放器 [NELivePlayer](http://netease.im/im-sdk-demo)，版本1.4.6
* 日志库 [CocoaLumberjack](https://github.com/CocoaLumberjack/CocoaLumberjack) 版本 3.4.0
* 加载状态 UI [SVProgressHUD](https://github.com/SVProgressHUD/SVProgressHUD) 版本 2.2.2
* 弹出提示 UI [Toast](https://github.com/scalessec/Toast) 版本 4.0.0 

#### 2. 工程结构

工程结构截图如下

**目录图**

<img src="http://yx-web.nos.netease.com/webdoc/default/1516778347434.png"/>

**工程图**

<img src="http://yx-web.nos.netease.com/webdoc/default/1516779732680.png"/>

```
└── NIMQuizGame                             # 答题工程
    └── Source
       ├── Category                             # Category 工具
       ├── Manager                              # 管理层
       ├── Model                                # 数据模型层
       ├── Object                               # 消息解析层
       ├── Service                              # 网络服务层
       ├── Util                                 # 工具类
       ├── Vendors                              # 无法 pods 管理的第三方库，这里是播放器
       ├── View                                 # 界面视图层
       └── Viewcontroller                       # 视图控制器层              
```

**层次结构**

![](http://yx-web.nos.netease.com/webdoc/default/3.jpg)

* **UI** 层具体包括 View 层，即界面层和 Viewcontroller 层，即视图控制器层，控制视图出现的逻辑等。本解决方案里有进入房间页面`NTESChatroomEntranceViewController`，答题页面 `NTESLiveGameViewController`。另外，播放器的 UI 界面单独封装为 `NTESLivePlayerViewController`，之后有章节单独介绍。

* **Manager** 层为业务管理层，其中包括 `NTESQuizManager`、`NTESUserManager`和`NTESDDLogManager`。`NTESQuizManager` 提供答题状态的管理查询接口，`NTESUserManager` 是一个全局单例，提供用户信息以及聊天室信息的查询接口，`NTESDDLogManager` 是日志相关的配置管理，包括更新时间，最大日志文件数等等。

* **Model** 层为各类数据模型的封装，包括消息数据、聊天室、题目内容、作答结果和参与游戏者的数据模型，提供网络数据的封装。同时，Object 层为消息解析层，其中 `NTESAttachment` 和 `NTESCustomAttachmentDecoder` 为自定义消息的封装器。`NTESBlockDispatcher` 通过监听拉流的时间戳回调，去比对当前所维护的 block 数组的第一个元素，这个 block 数组是根据时间由小到大进行排序的，比对之后若当前回调时间戳小于播放器流的实时时间戳，则抛出回调，从而保证题目的下发顺序。

* **Category** 层为方便并行开发时遇到的一些共性问题而准备的通用接口，本解决方案主要提供更加简洁方便的数据解析，图片渲染以及视图尺寸等接口，具体项目开发可参考使用。

* **Service** 层为网络层，提供了与应用服务器的接口。

#### 3. 网络层 Network

网络层单例 NTESNetwork 通过接口

```objc
- (void)postNetworkTask:(id<NTESNetworkTask>)task
             completion:(void(^)(NSError *error, id jsonObject))completion;
```
发起网络请求，网络请求被封装在实现了 `NTESNetworkTask` 协议的 task 对象中。目前主要的网络请求包括以下三个：

* 登录请求 NTESLoginTask 

    * 请求说明

    ```
    POST http://${Host}/quiz/player/create HTTP/1.1
    Content-Type: application/x-www-form-urlencoded;charset=utf-8
    ```
    * 参数列表
    
      |名称|  类型| 说明| 必须|
      |----------------------|--------------------|--------|-----|
      |sid  |String|    如果当前已有正在使用的 accid   |否|

    在参数对应的 sid 未失效时，会返回 sid 对应的账号信息，并更新账号失效时间，其他情况（sid 已失效或者 sid 不存在）会重新返回一个可用账号。
    
    * 返回说明
    
    
      | 参数              | 类型             | 说明|                 
      |----------------------|--------------------|--------|
      | code  | int  | 状态码 |
      | msg | String  | 错误信息|       | data | String  | 返回信息 | 
      | accid | String  | 用户账号 | 
      | nickname | String  | 用户昵称 | 
      | imToken | String  | im token | 
   
   
   说明：为了简化解决方案的次要业务逻辑，这里将登录注册合二为一，每次调用该接口，应用服务器会返回一个可用的账号进行登录。
   
* 获取房间信息请求 NTESQueryRoomInfoTask
    * 请求说明
    
    ```
    POST http://${Host}/quiz/player/room/query HTTP/1.1</br>Content-Type: application/x-www-form-urlencoded;charset=utf-8
    ```
    
    * 参数列表
    
      |名称|  类型| 说明| 必须|
      |-------------------|---------------|--------|-----|
      |sid  |String|    当前 accid    |是|
      |roomId   |Long|  请求进入的房间 id      |是|
      |addrType |String|    聊天室 Link 类型 WEB/COMMON，默认为COMMON    |否|


    通过该接口获取需要进入的房间信息
    
    
    * 返回说明


      | 参数              | 类型             | 说明|            
      |-------------------|--------------------|--------|
      | code  | int  | 状态码 |
      | msg | String  | 错误信息|   
      | data | String  | 返回信息 |
      | roomId | String | 房间号 |
      | name | String |  房间名称 |
      | creator | String | 房间创建者 |
      | rtmpPullUrl | String | 拉流地址 |
      | roomStatus | BOOL | 房间状态 |
      | liveStatus | int | 房间状态 |
      | onlineUserCount | int | 在线人数 |
      | bonus | int | 奖金金额 |
      | quizCount | int | 题目数量 |

    
* 提交答案请求 NTESSubmitQuizSelectTask

    * 请求说明
    
    ```
    POST http://${Host}/quiz/player/answer HTTP/1.1</br>Content-Type:</br>application/x-www-form-urlencoded;charset=utf-8
    ```
    * 参数列表
   
    
      |名称|  类型| 说明| 必须|
      |------------------|------------------|--------|-----|
      |sid  |String|    当前 accid    |是|
      |roomId   |Long|  请求进入的房间 id      |是|
      |questionId   |Long|  题目 id |是|
      | answer | Long | 回答选项 id |是|
  
    * 返回说明
    根据提交返回是否正确，判定提交答案是否正确
    
      | 参数              | 类型             | 说明|      
      |-----------------|------------------|--------|
      | code  | int  | 状态码 |
      | msg | String  | 错误信息|   
      | data | String  | 返回信息 |
      | result | int | 正确与否判定结果 |
      


#### 4. 业务层 Manager
业务层会调用网络层接口，并通过返回的数据完成业务逻辑。

目前共有两个业务：

* **登录相关业务 NTESUserManager**

    登录业务主要完成登录步骤：
    1. 调用网络层向应用层发送登录请求，获取账号 accid 和 密码 imToken
    2. 调用云信 SDK 登录云信服务器
    3. 回调总的请求结果
    4. 提供 根据 roomId 和 用户 accid 获取房间信息和用户信息
    
* **答题相关业务 NTESQuizManager**
    答题相关业务主要完成答题的流程：通过聊天室的接收消息回调，筛选出主播发的消息，并根据消息类型判断是进行发题、发布答案或是公布最后结果的处理。
    这里实现的难点是就是如何根据时间戳去判断是否显示题目，在第二小节已做相关介绍，这里不再赘述。
    
#### 5. 界面层 Viewcontroller
网易云信直播竞答解决方案的界面主要逻辑在 `NTESLiveGameViewController` 类。
主要配置函数以及相关处理如下：
`viewDidLoad` 进行如下配置：
1）**setupSubviews**
在该方法里进行所有子视图的添加
2）**addListener**
在该方法里添加 NIM SDK 的相关回调代理，用于监听聊天室消息的获取，以及答题逻辑的回调
3）**setupPlayer**
在该方法里进行拉流播放器的初始，具体播放器实现见 `NTESLivePlayerViewController`
4）**setupTimer**
该方法里定义定时器进行页面在线人数，复活次数等数据刷新

答题逻辑的实现：

```objc
1） onReceiveQuiz:(NTESQuiz *)quiz
```
在该回调里实现收到问题时的界面显示

```objc
2） answerSelected:(NTESQuizCard *)card withOption:(NTESQuizOption *)option
```
在该回调实现选择答案，并提交答案的逻辑

```objc
3） onReceiveAnswer:(NTESQuiz *)quiz
```
在该回调实现收到答案的时候界面显示，根据是否回答正确，界面显示不同

```objc
4) onReceiveFinalResult:(NTESResult *)result
```
在该回调里实现收到最终结果时的界面显示

#### 6. 参数配置

在 `NTESSolutionConfig` 全局单例的初始化函数中进行 appkey、服务器地址、复活次数、超时时长、答题倒计时时长配置。


```objc
- (instancetype)init
{
    self = [super init];
    if(self)
    {
        _appKey  = @"";
        _appHost = @"https://XXX.XXX";
        _resurrectionTimes = 1;
        _timeout = 30.f;
        _quizCardQuizCountdown = 10.f;
        _quizCardAnswerCountdown = 6.f;
    }
    return self;
}
```



