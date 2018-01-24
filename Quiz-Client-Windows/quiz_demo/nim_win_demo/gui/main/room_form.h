#pragma once

#include "gui/helper/ui_bitmap_control.h"
#include "api/nim_livestreaming_cpp.h"
#include "shared/ui/msgbox.h"

//选项
#define SELECT_NUM_MAX	3
struct SelectInfo
{
	int32_t id_;
	std::string text_;//显示文本
	int32_t sel_num_;//公布答案是需要的显示选择人数
	SelectInfo()
	{
		sel_num_ = -1;
	}
	//{"sel_id":int, "sel_txt":"string","sel_num":int}
	Json::Value GetJsonValue(bool answer = false)
	{
		Json::Value value;
		value["optionId"] = id_;
		value["content"] = text_;
		if (sel_num_ >= 0 && answer)
		{
			value["selectCount"] = sel_num_;
		}
		return value;
	}
	bool ParseJson(Json::Value value)
	{
		if (value.isObject() && value["content"].isString() && value["optionId"].isInt())
		{
			id_ = value["optionId"].asInt();
			text_ = value["content"].asString();
			sel_num_ = 0;
			if (value["selectCount"].isInt())
			{
				sel_num_ = value["selectCount"].asInt();
			}
			return true;
		}
		return false;
	}
};

//题目
struct QuestionInfo 
{
	int64_t id_;
	std::string text_;
	std::vector<SelectInfo> select_infos_;
	int32_t key_;
	int32_t order_;
	QuestionInfo()
	{
		id_ = -1;
		key_ = -1;
		order_ = 0;
	}
	//{"id":long, "qustion":"string", "select_info":[select,select,select] }
	Json::Value GetJsonValue(bool answer = false)
	{
		Json::Value value;
		value["questionId"] = id_;
		value["question"] = text_;
		value["order"] = order_;
		if (key_ >= 0 && answer)
		{
			value["rightAnswer"] = key_;
		}
		for (auto it : select_infos_)
		{
			value["options"].append(it.GetJsonValue(answer));
		}
		return value;
	}
	bool ParseJson(Json::Value value)
	{
		if (value.isObject() && value["questionId"].isInt64() && value["question"].isString() && value["options"].isArray() && value["options"].size() > 0)
		{
			id_ = value["questionId"].asInt64();
			text_ = value["question"].asString();
			order_ = value["order"].asInt();
			if (value["rightAnswer"].isInt())
			{
				key_ = value["rightAnswer"].asInt();
			}
			select_infos_.clear();
			int32_t sel_num = value["options"].size();
			for (int32_t i = 0; i < sel_num; i++)
			{
				Json::Value item;
				item = value["options"].get(i, item);
				SelectInfo sel_info;
				if (!sel_info.ParseJson(item))
				{
					return false;
				}
				select_infos_.push_back(sel_info);
			}
			return true;
		}
		return false;
	}
};

typedef std::vector<QuestionInfo> QuestionInfos;

struct RoomInfo
{
	int64_t room_id_;
	int32_t money_;
	std::string token_;
	std::string push_url_;
	QuestionInfos questions_;
	int32_t winner_count_;
	std::vector<std::string> winners_;
	FLOAT bonus_;
	RoomInfo()
	{
		room_id_ = 0;
		money_ = 0;
		winner_count_ = 0;
		bonus_ = 0;
	}
};

typedef enum LiveStepType
{
	kLiveStepInit = 0,
	kLiveStepLive,
	kLiveStepQustion,
	kLiveStepEnd,
};
typedef enum QustionStepType
{
	kQustionStepAnswer,
	kQustionStepAnswerShow,
};
typedef enum DeviceType{
	kDeviceTypeMic = 0,
	kDeviceTypeCamera,
	kDeviceTypeDecklink,
};

std::string GetServerUrl();
class RoomForm : 
	public nim_comp::WindowEx
{
public:
	RoomForm();
	~RoomForm();

	/**
	* 虚函数，指定本界面的xml布局文件和图片素材所在的目录的相对路径
	* @return std::wstring 返回该目录相对于[安装目录]/bin/themes/default的路径
	*/
	virtual std::wstring GetSkinFolder() override;

	//覆盖虚函数
	virtual std::wstring GetSkinFile() override;
	virtual std::wstring GetWindowClassName() const override;
	virtual std::wstring GetWindowId() const override;
	virtual UINT GetClassStyle() const override;
	
	/**
	* 处理窗口销毁消息
	* @return void	无返回值
	*/
	virtual void OnFinalMessage(HWND hWnd);

	/**
	* 根据控件类名创建自定义控件
	* @param[in] pstrClass 控件类名
	* @return Control* 创建的控件的指针
	*/
	virtual ui::Control* CreateControl(const std::wstring& pstrClass) override;

	/**
	* 拦截并处理WM_CLOSE消息
	* @param[in] wParam 附加参数
	* @param[in] lParam 附加参数
	* @param[in] lParam 附加参数
	* @param[in out] bHandled 是否处理了消息，如果处理了不继续传递消息
	* @return void	无返回值
	*/
	LRESULT OnClose(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);

	/**
	* 拦截ESC键的消息处理
	* @param[in out] bHandled 是否处理了消息，如果处理了不继续传递消息
	* @return void	无返回值
	*/
	virtual void OnEsc(BOOL &bHandled);

	/**
	* 窗口初始化函数
	* @return void	无返回值
	*/
	virtual void InitWindow() override;

	/**
	* 处理所有控件单击消息
	* @param[in] msg 消息的相关信息
	* @return bool true 继续传递控件消息，false 停止传递控件消息
	*/
	bool OnClicked(ui::EventArgs* msg);
	bool MenuItemClick(ui::EventArgs* msg);

	bool InitRoomInfo(RoomInfo room_info);

private:
	void DoClose(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	void LsErrTipCb(MsgBoxRet);
private:

	static void VideoFrameCb(_HNLSSERVICE hNLSService, ST_NLSS_VIDEO_SAMPLER *pstSampler);
	static void VideoFrameCb2(_HNLSSCHILDSERVICE hNLSSChild, ST_NLSS_VIDEO_SAMPLER *pstSampler);
	void InitLs();
	void LsErrCb(EN_NLSS_ERRCODE errCode);
	void ShowDevice(bool mic, ui::CPoint point);
	void OnPaintFrame();
	void StartDevice(DeviceType type, const std::string &path);

	void SetTitleInfo();
	//显示tip提示语
	void ShowTip(const std::wstring &tip = L"", const std::wstring &tipex1 = L"", int32_t time = 0, const std::wstring &tipex2 = L"");
	void DoTipTime();
	void RefreshTime(int32_t time);
	//直播前页面 kLiveStepInit
	void LiveStartShow();
	//直播开启中
	void LiveStarting();
	//准备出题页 kLiveStepLive kQustionStepStart
	void QustionStart();
	//题目作答 kLiveStepQustion kQustionStepAnswer
	void QustionAndAnswer();
	//作答结束，并得到结果可以显示
	void ShowAnswerRetGet();
	//显示答案 kLiveStepQustion kQustionStepAnswerShow
	void QustionShowAnswer();
	void ShowQustion();
	//中奖结果
	void ShowResult();

	//直播状态
	void PostLiveStatus(bool end);
	void PostLiveStatusCb(bool ret, int response_code, const std::string& reply);
	//出题
	void PushQustion();
	void PushAnswer();
	void PushResult();
	//push消息
	void PushMsg(const std::string& msg);
	void PushMsgCb(bool ret, int response_code, const std::string& reply);
	//直播打开
	void DoLiveStart();
	void LiveStartCb(bool ret);
	//请求结果
	void QuestAnswerRet();
	void QuestAnswerRetCb(bool ret, int response_code, const std::string& reply);

public:
	static const LPCTSTR kClassName;

private:
	RoomInfo room_info_;
	uint32_t cur_qustion_num_;
	nim_livestream::LsSession ls_session_;
	std::string audio_path_;
	std::string video_path_;
	std::string decklink_path_;
	LiveStepType live_step_;
	QustionStepType qustion_step_;

	nbase::WeakCallbackFlag paint_video_timer_;
	ui::BitmapControl* pre_viewer_;
	ui::Label* tip_;
	ui::Box* tip_box_;
	ui::Label* tip1_;
	ui::Label* tip2_;
	ui::Label* time_tip_;
	int32_t tip_time_count_;
	ui::Box* qustion_box_;
	ui::Label* qustion_text_;
	ui::Box* sel_box_[SELECT_NUM_MAX];
	ui::Label* sel_info_[SELECT_NUM_MAX];
	ui::Label* sel_num_[SELECT_NUM_MAX];
	ui::Label* answer_num_;
	ui::Button* btn_step_;

	ui::Box* result_box_;
	ui::Label* ret_num_;
	ui::ListBox* ret_member_;
	ui::Label* member_more_;
	ui::Control* empty_tip_;
	ui::Button* btn_end_;

	ui::Box* device_box_;
	ui::ButtonBox* mic_;
	ui::Control* mic_warning_;
	ui::ButtonBox* camera_;
	ui::Control* camera_warning_;
};
using namespace nbase;
