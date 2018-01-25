# 网易云信直播竞答解决方案 PC demo 源码导读

## <span id="工程概述">工程概述</span>
直播竞答Demo 是网易云通信的一款针对目前市场比较热门的直播竞答场景推出的解决方案。在方案中结合了网易云通信 IM 能力的聊天室模型和直播推流模型。在使用本解决方案之前请务必了解  [IM即时通讯](/docs/product/IM即时通讯/SDK开发集成/Windows开发集成/概要介绍) 的聊天室能力，和[直播推流](/docs/product/直播/推流端SDK/Win推流SDK/开发指南.md)的直播推流能力。其中PC作为推流出题端，没有整合聊天室功能，所有出题操作通过应用服务器实现。应用服务器相关功能需要用户自己实现。
Demo使用Visual Studio 2013 Update5开发(必须使用Update5版本)。

## <span id="界面开发资料">界面开发资料</span> 

### <span id="网易云信Duilib使用说明">网易云信Duilib使用说明</span> 

[代码托管地址](https://github.com/netease-im/NIM_PC_UIKit)

[网易云信Duilib使用说明](https://github.com/netease-im/NIM_PC_UIKit/blob/master/doc/nim_duilib.md)

### <span id="控件属性">控件属性</span> 

[控件属性](https://github.com/netease-im/NIM_PC_UIKit/blob/master/doc/duilib%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8.xml)

### <span id="界面布局介绍">界面布局介绍</span> 

[网易云信 DuiLib 布局功能指南](/docs/product/通用/Demo源码导读/PC通用/Demo 界面布局介绍 "target=_blank")

### <span id="Duillib高分屏(高DPI)支持">Duillib高分屏(高DPI)支持</span>

从3.5.0版本开始，Duilib增加了对高分屏的支持，方便在用户设置了DPI后保持软件界面的清晰效果。
 
[Duillib高分屏(高DPI)支持](/docs/product/通用/Demo源码导读/PC通用/Demo 高分屏实现 "target=_blank")

## <span id="目录结构">目录结构</span>

* callback：注册到SDK的一些回调的处理

* gui：所有功能的界面相关实现

* module：所有功能的逻辑相关实现

* util：一些公用的工具类

## <span id="打包说明">打包说明</span>

开发者在打包自己的应用时，应确保将以下云信SDK相关文件打包进去。

- nim\_tools\_http.dll：http功能库文件。

- msvcr100.dll：SDK依赖的VS2010动态链接库。

- msvcp100.dll：SDK依赖的VS2010动态链接库。

- live_stream：直播推流Sdk库文件目录。

- lang：Demo界面文案对照表，可支持多国语言。

- themes：Demo皮肤目录，包含XML配置文件和图片文件。

## <span id="功能点指引">功能点指引</span>

### <span id="SDK C++封装层">SDK C++封装层</span>

因为SDK所有接口都是C接口，为了方便使用C++的同学使用，我们提供了直播sdk的c++封装曾，demo直接使用`libs\nim_livestreaming_sdk`的C++封装层代码。开发者可以直接在解决方案中导入`nim_livestreaming_sdk`工程。其中提供的http组件附带的c++封装层，直接包含在demo启动项目中`\nim_win_demo\module\nim_http_cpp`。

### <span id="界面开发">界面开发</span>

云信PC demo以及UI组件的界面开发都依赖`云信DuiLib库`，关于`云信DuiLib库`的使用方法和注意事项，请参考：[云信Duilib](https://github.com/netease-im/NIM_PC_UIKit/blob/master/doc/nim_duilib.md)

## <span id="总体流程">总体流程</span>

* 初始化sdk
* 获取主播信息：直播房间号、直播推流地址、token、题目列表等
* 打开直播出题界面
* 选择音视频设备
* 开启直播
* 出题
* 等待并请求答题结果
* 公布单题答案结果
* 重复出题
* 出题完成后公布中奖信息
* 退出房间，结束推流。
* 关闭程序，释放sdk。

### <span id="能力实现说明">能力实现说明</span>

* 直播：通过直播推流sdk，采集普通的麦克风+摄像头，或者采集视频采集卡中的导播数据，通过rtmp推流。
* 出题等指令操作：通过http模块向应用服务器请求，对于需要同步给观众的信息，由应用服务器发高优先级聊天室消息给所有观众。所有demo中http请求都没有作失败重试，如果用户业务需要可以在失败后作提示逻辑或者重试发送。
* 指令同步：在http请求时，带上本地推流时间戳，由客户端才拉流时解析时间戳后同步显示。

### <span id="初始化">初始化</span>

程序启动在main.cpp中，并直接在main函数中使用`nim_livestream::LsSession`中的静态函数初始化和释放直播sdk，并在启动MainThread主线程后，在`MainThread::Init`中初始化日志文件。之后创建起始窗口MainForm。

### <span id="获取账号信息">获取账号信息</span>

由用户手动触发获取直播信息，直播信息由`deviceId`在应用服务器作映射，`deviceId`由本地生成后保存。通过调用`void MainForm::OnStart(int32_t money)`获取，通过`MainForm::QuestMasterInfoCb`返回结果，成功后创建直播窗口RoomForm。

### <span id="同步状态">同步状态</span>

客户端需要向应用服务器告知当前直播状态，用于关闭房间及重置答题信息。相关逻辑为demo演示逻辑。参考代码`RoomForm::PostLiveStatus`。

### <span id="选择设备">选择设备</span>

在`RoomForm::InitLs`中调用`nim_livestream::LsSession`接口遍历音视频设备或采集卡设备。其中音频设备需要在直播开始时打开，摄像头及采集卡在初始化直播对象后打开。

### <span id="直播参数设置">直播参数设置</span>

在`LsSession::InitSession`中通过设置`ls_param_`相关参数来确定直播形式。其中直播画面大小设定后底层会自动裁剪和缩放采集数据。

### <span id="打开摄像头或采集卡">打开摄像头或采集卡</span>

在`LsSession::StartCamera`打开设备，其中`decklink_mode`大于等于零时代表是采集卡设备。之中设备标识和模式标识由遍历接口得到。其中如果需要打开采集卡的音频数据流则重新初始化并设置音频模式为采集卡类型即可，demo中实现逻辑为：选择采集卡后音视频都由采集卡采集。

### <span id="开始直播">开始直播</span>

参考demo中`RoomForm::DoLiveStart`相关代码，其中`LsSession`中的实现会去判断音频设备是否修改，修改需要重新初始化，相关代码已经封装。

### <span id="预览数据监听">预览数据监听</span>

在初始化函数`LsSession::InitSession`中，通过设置视频预览回调函数可以得到视频数据，并在上层`RoomForm::VideoFrameCb`中监听保存到视频数据缓存对象`nim_comp::VideoFrameMng video_frame_mng_`中。显示时通过启动定时器定时调用`RoomForm::OnPaintFrame`来主动刷新画布控件`ui::BitmapControl* pre_viewer_`。其中画布控件在初始化时绑定了视频缓存对象`video_frame_mng_`。

### <span id="获取直播流时间戳">获取直播流时间戳</span>

直播流时间戳分两类：

* 视频包扩展传递的pts时间戳。通过接口`LsSession::GetSyncTimestamp`获取到。此时间戳不需要CDN定制，但是需要依赖视频数据流。

* 数据包的pts。通过接口`LsSession::GetSyncPts`获取。此时间戳不需要依赖视频画面，可以实现纯音频直播。**注意**，CDN需要定制设置，不能修改媒体流中的时间戳。

### <span id="出题及相关同步操作">出题及相关同步操作</span>

出题及公布答案等，本质是通过应用服务器调用发送云信聊天室中的高优先级消息来实现。高优先级消息来保证聊天室高并发时的消息保障。demo中参考`RoomForm::PushQustion`及`RoomForm::PushMsg`。

### <span id="答题结果及中奖信息查询">答题结果及中奖信息查询</span>

在出题后，间隔固定答题时间，定时向应用服务器获取答题结果，如果答题完成则应用服务器会返回有效结果，其中最后一题会同步返回中奖信息。代码参考`RoomForm::QuestAnswerRet`及对应的结果返回回调`RoomForm::QuestAnswerRetCb`。

### <span id="直播结束">直播结束</span>

直接关闭窗口，向服务器通知直播结束状态，见`RoomForm::PostLiveStatus`。同时释放直播对象`nim_livestream::LsSession ls_session_`。

### <span id="程序结束">程序结束</span>

结束主窗口MainForm，同时推出主进程。回到main函数，释放sdk。

### <span id="异常处理">异常处理</span>

在所有的http请求中可以在结果失败是带上重试逻辑，但是由于直播的实时同步出题的性质，不建议多次重试。在直播中断时可以紧急重启直播，通过应用服务器记录出题流程通知客户端后复原之前的出题状态。或者选择备用推流客户端，利用推送消息给观众，直接将直播切换到备用系统中。
