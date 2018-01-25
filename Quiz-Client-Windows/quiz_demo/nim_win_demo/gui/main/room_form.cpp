#include "room_form.h"
#include "module/nim_http_cpp/nim_tools_http_cpp.h"
#include "shared/log.h"
#include "shared/ui/ui_menu.h"

using namespace ui;
using namespace nim_livestream;

std::string GetServerUrl()
{
	return "https://app.netease.im/appdemo";
}

bool RoomForm::InitRoomInfo(RoomInfo room_info)
{
	if (room_info_.room_id_ != 0)
	{
		return false;
	}
	room_info_ = room_info;
	QLOG_APP(L"init room info roomid:{0}, qustion num:{1}, push url:{2}") << room_info.room_id_ << room_info.questions_.size() << room_info.push_url_;
	SetTitleInfo();
	InitLs();
	return true;
}

void RoomForm::InitLs()
{
	QLOG_APP(L"init ls");
	std::vector<DeviceInfo> videos, audios, decklinks;
	ls_session_.GetDeviceInf(videos, audios);
	ls_session_.GetDeckLinkList(decklinks);
	mic_warning_->SetVisible(audios.size() == 0);
	camera_warning_->SetVisible(videos.size() == 0 && decklinks.size() == 0);
	if (audios.size() > 0)
	{
		audio_path_ = audios[0].device_path_;
	}
	ls_session_.InitSession(audio_path_, room_info_.push_url_, nbase::Bind(&RoomForm::LsErrCb, this, std::placeholders::_1), &RoomForm::VideoFrameCb);
	if (videos.size() > 0)
	{
		video_path_ = videos[0].device_path_;
		ls_session_.StartCamera(video_path_, -1, OptCallback());
	}
	paint_video_timer_.Cancel();
	StdClosure task = nbase::Bind(&RoomForm::OnPaintFrame, this);
	nbase::ThreadManager::PostRepeatedTask(kThreadUI, paint_video_timer_.ToWeakCallback(task), nbase::TimeDelta::FromMilliseconds(60));
}
void RoomForm::LsErrTipCb(MsgBoxRet)
{
	Close();
}
void RoomForm::LsErrCb(EN_NLSS_ERRCODE errCode)
{
	QLOG_APP(L"live stream err cb {0}") << errCode;
	std::wstring tip = nbase::StringPrintf(L"直播推流失败 code%d", errCode);
	ShowMsgBox(NULL, tip, nbase::Bind(&RoomForm::LsErrTipCb, this, std::placeholders::_1));
}
void RoomForm::ShowDevice(bool mic, ui::CPoint point)
{
	std::vector<DeviceInfo> videos, audios, decklinks; 
	ls_session_.GetDeviceInf(videos, audios);
	ls_session_.GetDeckLinkList(decklinks);
	mic_warning_->SetVisible(audios.size() == 0);
	camera_warning_->SetVisible(videos.size() == 0 && decklinks.size() == 0);
	if (audios.size() > 0 && audio_path_.empty())
	{
		audio_path_ = audios[0].device_path_;
	}
	if (videos.size() > 0 && video_path_.empty() && decklink_path_.empty())
	{
		video_path_ = videos[0].device_path_;
		ls_session_.StartCamera(video_path_, -1, OptCallback());
	}
	if (mic && audios.size() > 0)
	{
		CMenuWnd* pMenu = new CMenuWnd(NULL);
		STRINGorID xml(L"device_menu.xml");
		pMenu->Init(xml, _T("xml"), point);
		auto device_menu = static_cast<ui::ListBox*>(pMenu->FindControl(L"device_menu"));
		for (auto it : audios)
		{
			ui::CMenuElementUI* item = new ui::CMenuElementUI;
			GlobalManager::FillBoxWithCache(item, L"menu/device_item.xml");
			item->AttachSelect(nbase::Bind(&RoomForm::MenuItemClick, this, std::placeholders::_1));
			item->SetName(L"mic_item");
			item->SetUTF8DataID(it.device_path_);
			auto name = static_cast<ui::Label*>(item->FindSubControl(L"item_name"));
			name->SetUTF8Text(it.friendly_name_);
			device_menu->Add(item);
		}
		pMenu->Show();
	}
	if (!mic && (videos.size() > 0 || decklinks.size() > 0))
	{
		CMenuWnd* pMenu = new CMenuWnd(NULL);
		STRINGorID xml(L"device_menu.xml");
		pMenu->Init(xml, _T("xml"), point);
		auto device_menu = static_cast<ui::ListBox*>(pMenu->FindControl(L"device_menu"));
		for (auto it : videos)
		{
			ui::CMenuElementUI* item = new ui::CMenuElementUI;
			GlobalManager::FillBoxWithCache(item, L"menu/device_item.xml");
			item->AttachSelect(nbase::Bind(&RoomForm::MenuItemClick, this, std::placeholders::_1));
			item->SetName(L"camera_item");
			item->SetUTF8DataID(it.device_path_);
			auto name = static_cast<ui::Label*>(item->FindSubControl(L"item_name"));
			name->SetUTF8Text(it.friendly_name_);
			device_menu->Add(item);
		}
		for (auto it : decklinks)
		{
			ui::CMenuElementUI* item = new ui::CMenuElementUI;
			GlobalManager::FillBoxWithCache(item, L"menu/device_item.xml");
			item->AttachSelect(nbase::Bind(&RoomForm::MenuItemClick, this, std::placeholders::_1));
			item->SetName(L"decklink_item");
			item->SetUTF8DataID(it.device_path_);
			auto name = static_cast<ui::Label*>(item->FindSubControl(L"item_name"));
			std::wstring dev_name = nbase::StringPrintf(L"[采集卡]%s", nbase::UTF8ToUTF16(it.friendly_name_).c_str());
			name->SetText(dev_name);
			device_menu->Add(item);
		}
		pMenu->Show();
	}
}
void RoomForm::StartDevice(DeviceType type, const std::string &path)
{
	if (type == kDeviceTypeCamera)
	{
		decklink_path_.clear();
		video_path_ = path;
		ls_session_.StartCamera(video_path_, -1, nim_livestream::OptCallback());
	}
	else if (type == kDeviceTypeDecklink)
	{
		decklink_path_ = path;
		int32_t mode_id = 0;
		{
			std::vector<DeckLinkMode> decklink_modes;
			ls_session_.GetDeckLinkModeList(path, decklink_modes);
		}

		ls_session_.StartCamera(decklink_path_, mode_id, nim_livestream::OptCallback());
	}
}

void RoomForm::DoLiveStart()
{
	QLOG_APP(L"do live start");
	//直播开始
	ls_session_.OnStartLiveStream(audio_path_, nbase::Bind(&RoomForm::LiveStartCb, this, std::placeholders::_1));
	//LiveStartCb(true);
}
void RoomForm::LiveStartCb(bool ret)
{
	if (ret)
	{
		device_box_->SetVisible(false);
		PostLiveStatus(false);
		QustionStart();
	}
	else
	{
		ShowTip(L"发起直播失败");
		btn_step_->SetEnabled(true);
	}
}
//请求结果
void RoomForm::QuestAnswerRet()
{
	QuestionInfo item = room_info_.questions_[cur_qustion_num_];

	auto http_cb = nbase::Bind(&RoomForm::QuestAnswerRetCb, this, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3);
	std::string api_addr = GetServerUrl();
	api_addr += "/quiz/host/result/query";
	api_addr = nbase::StringPrintf("%s?roomId=%lld&password=%s&questionId=%lld", api_addr.c_str(), room_info_.room_id_, room_info_.token_.c_str(), item.id_);
	nim_http::HttpRequest request(api_addr, "", 0, ToWeakCallback(http_cb));
	request.AddHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	request.SetMethodAsPost();
	request.SetTimeout(5000);
	nim_http::PostRequest(request);
}
void RoomForm::QuestAnswerRetCb(bool ret, int response_code, const std::string& reply)
{
	int32_t code = response_code;
	if (ret && response_code == 200)
	{
		Json::Value values;
		Json::Reader reader;
		if (reader.parse(reply, values))
		{
			code = values["code"].asInt();
			if (code == 200)
			{
				QLOG_APP(L"quest ret cb {0}") << reply;
				room_info_.questions_[cur_qustion_num_].ParseJson(values["data"]);
				auto bonus_info = values["data"]["bonusInfo"];
				if (bonus_info.isObject())
				{
					room_info_.winner_count_ = bonus_info["winnerCount"].asInt();
					room_info_.bonus_ = bonus_info["bonus"].asFloat();
					room_info_.winners_.clear();
					int32_t num = bonus_info["winnerSample"].size();
					for (int32_t i = 0; i < num; i++)
					{
						Json::Value item;
						item = bonus_info["winnerSample"].get(i, item);
						room_info_.winners_.push_back(item.asString());
					}
				}
				ShowAnswerRetGet();
				return;
			}
		}
	}
	nbase::ThreadManager::PostDelayedTask(kThreadUI, nbase::Bind(&RoomForm::QuestAnswerRet, this), nbase::TimeDelta::FromSeconds(3));
}
//直播状态
void RoomForm::PostLiveStatus(bool end)
{
	QLOG_APP(L"post live status {0}") << end;
	auto http_cb = nbase::Bind(&RoomForm::PostLiveStatusCb, this, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3);
	std::string api_addr = GetServerUrl();
	api_addr += "/quiz/host/switch";
	api_addr = nbase::StringPrintf("%s?roomId=%lld&password=%s&status=%d", api_addr.c_str(), room_info_.room_id_, room_info_.token_.c_str(), end ? 2 : 1);
	nim_http::HttpRequest request(api_addr, nullptr, 0, ToWeakCallback(http_cb));
	request.AddHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	request.SetMethodAsPost();
	request.SetTimeout(5000);
	nim_http::PostRequest(request);
}
void RoomForm::PostLiveStatusCb(bool ret, int response_code, const std::string& reply)
{
	int32_t code = response_code;
	if (ret && response_code == 200)
	{
		Json::Value values;
		Json::Reader reader;
		if (reader.parse(reply, values))
		{
			code = values["code"].asInt();
			if (code == 200)
			{
				return;
			}
		}
	}
	QLOG_ERR(L"post live status cb {0}, {1}") << response_code << reply;
}
//出题
void RoomForm::PushQustion()
{
	uint64_t pts = ls_session_.GetSyncTimestamp();
	QLOG_APP(L"push qustion pts {0}") << pts;
	auto item = room_info_.questions_[cur_qustion_num_];
	auto values = item.GetJsonValue(false);
	Json::Value value;
	value["cmd"] = 1;
	value["data"]["questionInfo"] = values;
	value["data"]["time"] = pts;
	Json::FastWriter fs;
	std::string json = fs.write(value);

	auto http_cb = nbase::Bind(&RoomForm::PushMsgCb, this, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3);
	std::string api_addr = GetServerUrl();
	api_addr += "/quiz/host/question/publish";
	api_addr = nbase::StringPrintf("%s?roomId=%lld&password=%s&questionId=%lld", api_addr.c_str(), room_info_.room_id_, room_info_.token_.c_str(), item.id_);
	nim_http::HttpRequest request(api_addr, json.c_str(), json.size(), ToWeakCallback(http_cb));
	request.AddHeader("Content-Type", "application/json;charset=utf-8");
	//request.AddHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	request.SetMethodAsPost();
	request.SetTimeout(5000);
	nim_http::PostRequest(request);
}
void RoomForm::PushAnswer()
{
	uint64_t pts = ls_session_.GetSyncTimestamp();
	QLOG_APP(L"push qustion answer pts {0}") << pts;
	auto item = room_info_.questions_[cur_qustion_num_];
	auto values = item.GetJsonValue(true);
	Json::Value value;
	value["cmd"] = 4;
	value["data"]["questionInfo"] = values;
	value["data"]["time"] = pts;
	Json::FastWriter fs;
	std::string json = fs.write(value);
	PushMsg(json);
}
void RoomForm::PushResult()
{
	uint64_t pts = ls_session_.GetSyncTimestamp();
	QLOG_APP(L"push result pts {0}") << pts;
	Json::Value value;
	value["cmd"] = 5;
	value["data"]["winnerCount"] = room_info_.winner_count_;
	value["data"]["bonus"] = room_info_.bonus_;
	for (auto it : room_info_.winners_)
	{
		value["data"]["winnerSample"].append(it);
	}
	value["data"]["time"] = pts;
	Json::FastWriter fs;
	std::string json = fs.write(value);
	PushMsg(json);
}
//push消息
void RoomForm::PushMsg(const std::string& msg)
{
	auto http_cb = nbase::Bind(&RoomForm::PushMsgCb, this, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3);
	std::string api_addr = GetServerUrl();
	api_addr += "/quiz/host/result/publish";
	api_addr = nbase::StringPrintf("%s?roomId=%lld&password=%s", api_addr.c_str(), room_info_.room_id_, room_info_.token_.c_str());
	nim_http::HttpRequest request(api_addr, msg.c_str(), msg.size(), ToWeakCallback(http_cb));
	request.AddHeader("Content-Type", "application/json;charset=utf-8");
	//request.AddHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	request.SetMethodAsPost();
	request.SetTimeout(5000);
	nim_http::PostRequest(request);
}
void RoomForm::PushMsgCb(bool ret, int response_code, const std::string& reply)
{
	int32_t code = response_code;
	if (ret && response_code == 200)
	{
		Json::Value values;
		Json::Reader reader;
		if (reader.parse(reply, values))
		{
			code = values["code"].asInt();
			if (code == 200)
			{
				return;
			}
		}
	}

	QLOG_ERR(L"push msg cb err {0}: step {1}, cur question {2}, ret json {3}") << response_code << qustion_step_ << cur_qustion_num_ << reply;
}
