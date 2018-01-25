#pragma once

#include "room_form.h"

class MainForm : 
	public nim_comp::WindowEx
{
public:
	MainForm();
	~MainForm();

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

private:

	void ShowErrTip(const std::wstring &tip);

	void AddMoneyItem(const std::wstring &text);

	void OnStart(int32_t money);

	void QuestMasterInfoCb(bool ret, int response_code, const std::string& reply);

	void CreateRoom(RoomInfo info);

public:
	static const LPCTSTR kClassName;

private:
	ui::Combo* money_cmb_;
	ui::Label* err_tip_;
};

using namespace nbase;
//DISABLE_RUNNABLE_METHOD_REFCOUNT(MainForm);
