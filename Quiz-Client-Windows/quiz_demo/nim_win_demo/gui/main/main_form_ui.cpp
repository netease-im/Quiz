
#include "main_form.h"

using namespace ui;

const LPCTSTR MainForm::kClassName	= L"MainForm";
const int32_t combo_money_num_ = 5;
const std::wstring money_text_[combo_money_num_] = { L"1,000 元", L"5,000 元", L"10,000 元", L"50,000 元", L"100,000 元" };
const int32_t money_info_[combo_money_num_] = { 1000, 5000, 10000, 50000, 100000 };

MainForm::MainForm()
{
}

MainForm::~MainForm()
{

}

std::wstring MainForm::GetSkinFolder()
{
	return L"main";
}

std::wstring MainForm::GetSkinFile()
{
	return L"main.xml";
}

std::wstring MainForm::GetWindowClassName() const 
{
	return kClassName;
}

std::wstring MainForm::GetWindowId() const 
{
	return kClassName;
}

UINT MainForm::GetClassStyle() const 
{
	return (UI_CLASSSTYLE_FRAME | CS_DBLCLKS);
}

void MainForm::OnEsc( BOOL &bHandled )
{
	bHandled = TRUE;
}

void MainForm::OnFinalMessage(HWND hWnd)
{
	QLOG_APP(L"main form final message");
	__super::OnFinalMessage(hWnd);
}

LRESULT MainForm::OnClose(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	nim_comp::WindowsManager::GetInstance()->DestroyAllWindows();
	PostQuitMessage(0);
	return 1;
}

void MainForm::InitWindow()
{
	SetTaskbarTitle(L"创建房间");
	//m_pRoot->AttachBubbledEvent(ui::kEventAll, nbase::Bind(&MainForm::Notify, this, std::placeholders::_1));
	m_pRoot->AttachBubbledEvent(ui::kEventClick, nbase::Bind(&MainForm::OnClicked, this, std::placeholders::_1));

	err_tip_ = (ui::Label*)FindControl(L"err_tip");
	err_tip_->SetVisible(false);
	money_cmb_ = (ui::Combo*)FindControl(L"money_cmb");
	for (int32_t i = 0; i < combo_money_num_; i++)
	{
		AddMoneyItem(money_text_[i]);
	}
	money_cmb_->SelectItem(0);

}
bool MainForm::OnClicked(ui::EventArgs* msg)
{
	std::wstring name = msg->pSender->GetName();
	if (name == L"btn_start")
	{
		int32_t sel = money_cmb_->GetCurSel();
		OnStart(money_info_[sel]);
	}
	return true;
}
void MainForm::AddMoneyItem(const std::wstring &text)
{
	ListContainerElement* label = new ListContainerElement;
	label->SetStateColor(kControlStateHot, L"link_blue");
	label->SetFixedHeight(28);
	label->SetTextPadding(UiRect(10, 1, 30, 1));
	label->SetText(text);
	money_cmb_->Add(label);
}
void MainForm::ShowErrTip(const std::wstring &tip)
{
	if (err_tip_)
	{
		err_tip_->SetText(tip);
		err_tip_->SetVisible(true);
	}
}

