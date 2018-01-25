#include "room_form.h"
#include "shared/log.h"

using namespace ui;

const LPCTSTR RoomForm::kClassName	= L"RoomForm";
nim_comp::VideoFrameMng video_frame_mng_;
//live stream
void RoomForm::VideoFrameCb(_HNLSSERVICE hNLSService, ST_NLSS_VIDEO_SAMPLER *pstSampler)
{
	if (pstSampler)
	{
		video_frame_mng_.AddVideoFrameEx((char*)pstSampler->puaData, pstSampler->iDataSize, pstSampler->iWidth, pstSampler->iHeight, pstSampler->iFormat);
	}
}
void RoomForm::VideoFrameCb2(_HNLSSCHILDSERVICE hNLSSChild, ST_NLSS_VIDEO_SAMPLER *pstSampler)
{
	//if (pstSampler)
	//{
	//	video_frame_mng_.AddVideoFrameEx((char*)pstSampler->puaData, pstSampler->iDataSize, pstSampler->iWidth, pstSampler->iHeight, pstSampler->iFormat);
	//}
}
void RoomForm::OnPaintFrame()
{
	pre_viewer_->Refresh(this);
}
RoomForm::RoomForm()
{
	live_step_ = kLiveStepInit;
	qustion_step_ = kQustionStepAnswer;
	tip_time_count_ = 0;
	video_frame_mng_.Clear();
}

RoomForm::~RoomForm()
{
}

std::wstring RoomForm::GetSkinFolder()
{
	return L"main";
}

std::wstring RoomForm::GetSkinFile()
{
	return L"room.xml";
}

std::wstring RoomForm::GetWindowClassName() const 
{
	return kClassName;
}

std::wstring RoomForm::GetWindowId() const 
{
	return kClassName;
}

UINT RoomForm::GetClassStyle() const 
{
	return (UI_CLASSSTYLE_FRAME | CS_DBLCLKS);
}

void RoomForm::OnEsc( BOOL &bHandled )
{
	bHandled = TRUE;
}

void RoomForm::OnFinalMessage(HWND hWnd)
{
	QLOG_APP(L"room form final message");
	__super::OnFinalMessage(hWnd);
}

ui::Control* RoomForm::CreateControl(const std::wstring& pstrClass)
{
	if (pstrClass == _T("BitmapControl"))
	{
		return new ui::BitmapControl(&video_frame_mng_);
	}
	return NULL;
}

LRESULT RoomForm::OnClose(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	PostLiveStatus(true);
	ls_session_.ClearSession();
	return __super::OnClose(uMsg, wParam, lParam, bHandled);
}

void RoomForm::InitWindow()
{
	SetTaskbarTitle(L"主播端");
	m_pRoot->AttachBubbledEvent(ui::kEventClick, nbase::Bind(&RoomForm::OnClicked, this, std::placeholders::_1));
	ui::Label* label = (ui::Label*)FindControl(L"title");
	if (label)
	{
		label->SetText(L"云信直播竞答DEMO [主播端]");
	}
	pre_viewer_ = (ui::BitmapControl*)FindControl(L"screen");
	tip_ = (ui::Label*)FindControl(L"tip");
	tip1_ = (ui::Label*)FindControl(L"tipex1");
	tip2_ = (ui::Label*)FindControl(L"tipex2");
	time_tip_ = (ui::Label*)FindControl(L"tipex_time");
	tip_box_ = (ui::Box*)FindControl(L"tip_box");
	btn_step_ = (ui::Button*)FindControl(L"btn_step");

	qustion_box_ = (ui::Box*)FindControl(L"qustion_box");
	qustion_text_ = (ui::Label*)FindControl(L"qustion_text");
	for (int32_t i = 0; i < SELECT_NUM_MAX; i++)
	{
		sel_box_[i] = (ui::Box*)FindControl(nbase::StringPrintf(L"sel_%d", i + 1));
		sel_info_[i] = (ui::Label*)FindControl(nbase::StringPrintf(L"sel_text_%d", i + 1));
		sel_num_[i] = (ui::Label*)FindControl(nbase::StringPrintf(L"sel_num_%d", i + 1));
	}
	answer_num_ = (ui::Label*)FindControl(L"answer_num");


	result_box_ = (ui::Box*)FindControl(L"result_box");
	ret_num_ = (ui::Label*)FindControl(L"ret_num");
	ret_member_ = (ui::ListBox*)FindControl(L"ret_member"); 
	member_more_ = (ui::Label*)FindControl(L"member_more");
	empty_tip_ = (ui::Control*)FindControl(L"empty_tip");
	btn_end_ = (ui::Button*)FindControl(L"btn_end");

	device_box_ = (ui::Box*)FindControl(L"device_box");
	mic_ = (ui::ButtonBox*)FindControl(L"mic");
	mic_warning_ = FindControl(L"mic_warning");
	camera_ = (ui::ButtonBox*)FindControl(L"camera");
	camera_warning_ = FindControl(L"camera_warning");
}
bool RoomForm::OnClicked(ui::EventArgs* msg)
{
	std::wstring name = msg->pSender->GetName();
	if (name == L"btn_step")
	{
		switch (live_step_)
		{
		case kLiveStepInit:
			LiveStarting();
			break;
		case kLiveStepLive:
			live_step_ = kLiveStepLive;
			QustionAndAnswer();
			break;
		case kLiveStepQustion:
		{
			switch (qustion_step_)
			{
			case kQustionStepAnswer:
				QustionShowAnswer();
				break;
			case kQustionStepAnswerShow:
				if (cur_qustion_num_ >= room_info_.questions_.size())
				{
					ShowResult();
				}
				else
				{
					QustionAndAnswer();
				}
				break;
			default:
				break;
			}
			break;
		}
		case kLiveStepEnd:
		{
			Close();
			//end
			break;
		}
		default:
			break;
		}
	}
	else if (name == L"btn_end")
	{
		PostLiveStatus(true);
		Close();
	}
	else if (name == L"mic")
	{
		RECT rect = msg->pSender->GetPos();
		CPoint point;
		point.x = rect.right;
		point.y = rect.bottom;
		ClientToScreen(m_hWnd, &point);
		ShowDevice(true, point);
	}
	else if (name == L"camera")
	{
		RECT rect = msg->pSender->GetPos();
		CPoint point;
		point.x = rect.right;
		point.y = rect.bottom;
		ClientToScreen(m_hWnd, &point);
		ShowDevice(false, point);
	}
	return true;
}
bool RoomForm::MenuItemClick(ui::EventArgs* msg)
{
	std::wstring name = msg->pSender->GetName();
	if (name == L"mic_item")
	{
		audio_path_ = msg->pSender->GetUTF8DataID();
	}
	else if (name == L"camera_item")
	{
		std::string path = msg->pSender->GetUTF8DataID();
		StartDevice(kDeviceTypeCamera, path);
	}
	else if (name == L"decklink_item")
	{
		std::string path = msg->pSender->GetUTF8DataID();
		StartDevice(kDeviceTypeDecklink, path);
	}
	return true;
}
void RoomForm::SetTitleInfo()
{
	ui::Label* room_id = (ui::Label*)FindControl(L"room_id");
	if (room_id)
	{
		room_id->SetText(nbase::StringPrintf(L"房间ID：%lld", room_info_.room_id_));
	}
	LiveStartShow();
}
//显示tip提示语
void RoomForm::ShowTip(const std::wstring &tip, const std::wstring &tipex1, int32_t time, const std::wstring &tipex2)
{
	tip_->SetText(tip);
	tip_->SetVisible(!tip.empty());
	tip1_->SetText(tipex1);
	tip1_->SetVisible(!tipex1.empty());
	tip2_->SetText(tipex2);
	tip2_->SetVisible(!tipex2.empty());
	bool show_box = (!tipex1.empty() || !tipex2.empty());
	tip_box_->SetVisible(show_box);
	if (show_box && time > 0)
	{
		tip_time_count_ = time;
		DoTipTime();
	}
	else
	{
		time_tip_->SetVisible(false);
	}
}
void RoomForm::DoTipTime()
{
	RefreshTime(tip_time_count_);
	if (tip_time_count_ > 0)
	{
		StdClosure closure = nbase::Bind(&RoomForm::DoTipTime, this);
		nbase::ThreadManager::PostDelayedTask(kThreadUI, closure, nbase::TimeDelta::FromSeconds(1));
	}
	else
	{
		if (qustion_step_ == kQustionStepAnswer)
		{
			ShowTip(L"答题完毕，正在收集回答", L"收集好后即可公布答案");
			btn_step_->SetText(L"公布答案");
			QuestAnswerRet();
		}
		else if (qustion_step_ == kQustionStepAnswerShow)
		{
			btn_step_->SetEnabled();
			ShowTip();
		}
		else
		{
			btn_step_->SetEnabled();
			ShowTip();
		}
	}
	tip_time_count_--;
}
void RoomForm::RefreshTime(int32_t time)
{
	time_tip_->SetText(nbase::StringPrintf(L"%dS", time));
	time_tip_->SetVisible();
}
//直播前页面 kLiveStepInit
void RoomForm::LiveStartShow()
{
	QLOG_APP(L"live start show, init ok!");
	//初始化直播对象
	btn_step_->SetEnabled();
	btn_step_->SetText(L"开始直播");
	live_step_ = kLiveStepInit;
}
void RoomForm::LiveStarting()
{
	QLOG_APP(L"begin start liveshow");
	ShowTip(L"");
	btn_step_->SetEnabled(false);
	btn_step_->SetText(L"开始直播");
	DoLiveStart();
}
//准备出题页 kLiveStepLive kQustionStepStart
void RoomForm::QustionStart()
{
	QLOG_APP(L"show qustion start");
	btn_step_->SetEnabled();
	btn_step_->SetText(L"开始出题");
	ShowTip(L"直播已开始！");
	live_step_ = kLiveStepLive;
	cur_qustion_num_ = 0;
}
//题目作答 kLiveStepQustion kQustionStepAnswer
void RoomForm::QustionAndAnswer()
{
	QLOG_APP(L"show qustion and answer {0}") << cur_qustion_num_;
	btn_step_->SetEnabled(false);
	btn_step_->SetText(L"答题中...");
	live_step_ = kLiveStepQustion;
	qustion_step_ = kQustionStepAnswer;
	ShowQustion(); 
	PushQustion();
	ShowTip(nbase::StringPrintf(L"第%d/%d题已下发", cur_qustion_num_ + 1, room_info_.questions_.size()), L"答题倒计时：", 10);
}
//作答结束，并得到结果可以显示
void RoomForm::ShowAnswerRetGet()
{
	btn_step_->SetEnabled(true);
}
//显示答案 kLiveStepQustion kQustionStepAnswerShow
void RoomForm::QustionShowAnswer()
{
	QLOG_APP(L"show qustion answer and people num {0}") << cur_qustion_num_;
	qustion_step_ = kQustionStepAnswerShow;
	ShowQustion();
	PushAnswer();
	cur_qustion_num_++;
	if (cur_qustion_num_ >= room_info_.questions_.size())
	{
		btn_step_->SetEnabled(true);
		btn_step_->SetText(L"宣布结果");
		ShowTip(L"出题结束");
	} 
	else
	{
		btn_step_->SetEnabled(false);
		btn_step_->SetText(nbase::StringPrintf(L"出第%d题", cur_qustion_num_ + 1));
		ShowTip(L"", L"", 10, L"后可继续出题");
	}
}
void RoomForm::ShowQustion()
{
	if (cur_qustion_num_ < room_info_.questions_.size())
	{
		QuestionInfo item = room_info_.questions_[cur_qustion_num_];
		std::string text = nbase::StringPrintf("%d/%d. %s", cur_qustion_num_ + 1, room_info_.questions_.size(), item.text_.c_str());
		qustion_text_->SetUTF8Text(text);
		int32_t people_num = 0;
		for (uint32_t i = 0; i < SELECT_NUM_MAX; i++)
		{
			if (i < item.select_infos_.size())
			{
				SelectInfo sel_item = item.select_infos_[i];
				sel_info_[i]->SetUTF8Text(sel_item.text_);
				if (qustion_step_ == kQustionStepAnswer)
				{
					sel_num_[i]->SetVisible(false);
				} 
				else
				{
					sel_num_[i]->SetText(nbase::StringPrintf(L"%d人", sel_item.sel_num_));
					sel_num_[i]->SetVisible();
					people_num += sel_item.sel_num_;
				}

				if (qustion_step_ == kQustionStepAnswerShow && sel_item.id_ == item.key_)
				{
					sel_box_[i]->SetBkImage(L"sel1.png");
				}
				else
				{
					sel_box_[i]->SetBkImage(L"sel0.png");
				}
				sel_box_[i]->SetVisible();
			}
			else
			{
				sel_box_[i]->SetVisible(false);
			}
		}
		if (qustion_step_ == kQustionStepAnswer)
		{
			answer_num_->SetVisible(false);
		}
		else
		{
			answer_num_->SetText(nbase::StringPrintf(L"答题人数：%d人", people_num));
			answer_num_->SetVisible();
		}
		qustion_box_->SetVisible();
	}
}
//中奖结果
void RoomForm::ShowResult()
{
	live_step_ = kLiveStepEnd;
	PushResult();
	uint32_t ret_num = room_info_.winner_count_;
	ret_num_->SetText(nbase::StringPrintf(L"%d人冲关成功", ret_num));
	ret_member_->RemoveAll();
	float money = room_info_.bonus_;
	uint32_t show_count = ret_num - 2;
	for (auto it : room_info_.winners_)
	{
		ui::ListContainerElement* item = new ui::ListContainerElement;
		GlobalManager::FillBoxWithCache(item, L"main/ret_member.xml");
		auto member_id = static_cast<ui::Label*>(item->FindSubControl(L"member_id"));
		member_id->SetText(nbase::StringPrintf(L"ID：%s", nbase::UTF8ToUTF16(it)));
		auto money_count = static_cast<ui::Label*>(item->FindSubControl(L"money_count"));
		money_count->SetText(nbase::StringPrintf(L"%.2f元", money));
		ret_member_->Add(item);
	}
	member_more_->SetVisible(show_count < ret_num);
	empty_tip_->SetVisible(ret_num == 0);

	ShowTip(L"请主持人讲结束语，然后结束直播");

	qustion_box_->SetVisible(false);
	btn_step_->SetVisible(false);
	result_box_->SetVisible();
	btn_end_->SetVisible();
}
