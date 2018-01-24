#include "main_form.h"
#include "module/nim_http_cpp/nim_tools_http_cpp.h"
#include "module/util/windows_manager.h"

using namespace ui;


void MainForm::OnStart(int32_t money)
{
	err_tip_->SetVisible(false);

	std::wstring device_info_path = QPath::GetNimAppDataDir();
	device_info_path.append(L"device_info.txt");
	std::string ret_info;
	nbase::ReadFileToString(device_info_path, ret_info);
	std::string device_id;
	if (!ret_info.empty())
	{
		device_id = ret_info;
	}
	else
	{
		device_id = nbase::StringPrintf("%d", rand());
		nbase::WriteFile(device_info_path, device_id);
	}
	QLOG_APP(L"start money {0} id: {1}") << money << device_id;
	std::string ext = "test";
	auto http_cb = nbase::Bind(&MainForm::QuestMasterInfoCb, this, std::placeholders::_1, std::placeholders::_2, std::placeholders::_3);
	std::string api_addr = GetServerUrl();
	api_addr += "/quiz/host/create";
	api_addr = nbase::StringPrintf("%s?deviceId=%s&bonus=%d&ext=%s", api_addr.c_str(), device_id.c_str(), money, ext.c_str());
	nim_http::HttpRequest request(api_addr, "", 0, ToWeakCallback(http_cb));
	request.AddHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	request.SetMethodAsPost();
	request.SetTimeout(5000);
	nim_http::PostRequest(request);
}
void MainForm::QuestMasterInfoCb(bool ret, int response_code, const std::string& reply)
{
	int32_t code = response_code;
	std::string msg;
	if (ret && response_code == 200)
	{
		Json::Value values;
		Json::Reader reader;
		QLOG_APP(L"get qustions {0}") << reply;
		if (reader.parse(reply, values))
		{
			code = values["code"].asInt();
			msg = values["msg"].asString();
			if (code == 200 && values["data"].isObject())
			{
				RoomInfo info;
				info.room_id_ = values["data"]["roomId"].asInt64();
				info.money_ = values["data"]["bonus"].asInt();
				info.push_url_ = values["data"]["pushUrl"].asString();
				info.token_ = values["data"]["password"].asString();
				info.questions_.clear();
				int32_t qustion_num = values["data"]["questionInfo"].size();
				for (int32_t i = 0; i < qustion_num; i++)
				{
					Json::Value item;
					item = values["data"]["questionInfo"].get(i, item);
					QuestionInfo question_info;
					if (!question_info.ParseJson(item))
					{
					}
					info.questions_.push_back(question_info);
				}
				Post2UI(nbase::Bind(&MainForm::CreateRoom, this, info));
				return;
			}
		}
	}
	std::wstring info = nbase::StringPrintf(L"请求错误 code:%d, %s", code, nbase::UTF8ToUTF16(msg).c_str());
	ShowErrTip(info);
};
void MainForm::CreateRoom(RoomInfo info)
{
	RoomForm* room = (RoomForm*)(nim_comp::WindowsManager::GetInstance()->GetWindow(RoomForm::kClassName, RoomForm::kClassName));
	if (!room)
	{
		QLOG_APP(L"create room new RoomForm");
		room = new RoomForm;
		room->Create(NULL, RoomForm::kClassName, WS_OVERLAPPEDWINDOW & ~WS_MAXIMIZEBOX, 0, false);
		room->InitRoomInfo(info);
		room->CenterWindow();
		room->ShowWindow();
	}
	else
	{
		QLOG_APP(L"RoomForm ActiveWindow");
		room->ActiveWindow();
	}
	//ShowWindow(false, false);
}