#include "main.h"
#include "resource.h"
#include "gui/main/main_form.h"
#include "app_dump.h"
#include "base/util/at_exit.h"
#include "shared/xml_util.h"
#include "base/util/string_number_conversions.h"

#include "gui/main/main_form.h"
#include "module/nim_http_cpp/nim_tools_http_cpp.h"
#include "api/nim_livestreaming_cpp.h"
#include "shared/log.h"

void MainThread::Init()
{
	nbase::ThreadManager::RegisterThread(kThreadUI);
	PreMessageLoop();

	std::wstring theme_dir = QPath::GetAppPath();
	ui::GlobalManager::Startup(theme_dir + L"themes\\default", ui::CreateControlCallback());
	if (!nbase::FilePathIsExist(QPath::GetNimAppDataDir(), true))
		nbase::CreateDirectory(QPath::GetNimAppDataDir());
	QLogImpl::GetInstance()->SetLogFile(QPath::GetNimAppDataDir() + L"demo.log");
	QLogImpl::GetInstance()->SetLogLevel(LV_APP);
	std::wstring app_crash = QCommand::Get(kCmdAppCrash);
	if( app_crash.empty() )
	{
		QLOG_APP(L"start -----------------------");
		nim_comp::WindowsManager::SingletonShow<MainForm>(MainForm::kClassName);
	}
	else
	{
		std::wstring content(L"程序崩溃了，崩溃日志：");
		content.append(app_crash);

		MsgboxCallback cb = nbase::Bind(&MainThread::OnMsgBoxCallback, this, std::placeholders::_1);
		ShowMsgBox(NULL, content, cb, L"提示", L"打开", L"取消");
	}
}

void MainThread::Cleanup()
{
	ui::GlobalManager::Shutdown();

	PostMessageLoop();
	SetThreadWasQuitProperly(true);
	nbase::ThreadManager::UnregisterThread();
}

void MainThread::PreMessageLoop()
{
	misc_thread_.reset( new MiscThread(kThreadGlobalMisc, "Global Misc Thread") );
	misc_thread_->Start();

	ls_thread_.reset(new MiscThread(kThreadLiveStreaming, "LiveStreaming Thread"));
	ls_thread_->Start();

	screen_capture_thread_.reset(new MiscThread(kThreadScreenCapture, "screen capture"));
	screen_capture_thread_->Start();
}

void MainThread::PostMessageLoop()
{
	misc_thread_->Stop();
	misc_thread_.reset(NULL);

	ls_thread_->Stop();
	ls_thread_.reset(NULL);

	screen_capture_thread_->Stop();
	screen_capture_thread_.reset(NULL);
}

void MainThread::OnMsgBoxCallback( MsgBoxRet ret )
{
	if(ret == MB_YES)
	{
		std::wstring dir = QPath::GetNimAppDataDir();
		QCommand::AppStartWidthCommand(dir, L"");
	}
}

int WINAPI wWinMain(HINSTANCE hInst, HINSTANCE hPrevInst, LPWSTR lpszCmdLine, int nCmdShow)
{
	nbase::AtExitManager at_manager;

	CComModule _Module;
	_Module.Init(NULL, hInst);

	_wsetlocale(LC_ALL, L"chs");

//#ifdef _DEBUG
	AllocConsole();
	FILE* fp = NULL;
	freopen_s(&fp, "CONOUT$", "w+t", stdout);
	wprintf_s(L"Command:\n%s\n\n", lpszCmdLine);
//#endif

	srand( (unsigned int) time(NULL) );

	::SetUnhandledExceptionFilter(MyUnhandledExceptionFilter);

	QCommand::ParseCommand(lpszCmdLine);

	HRESULT hr = ::OleInitialize(NULL);
	nim_livestream::LsSession::LoadLivestreamingDll();
	nim_http::Init();
	if( FAILED(hr) )
		return 0;

	{
		MainThread thread; // 创建主线程
		thread.RunOnCurrentThreadWithLoop(nbase::MessageLoop::kUIMessageLoop); // 执行主线程循环
	}
	QLOG_APP(L"exit ui loop");
	nim_http::Uninit();
	nim_livestream::LsSession::UnLoadLivestreamingDll();

	_Module.Term();
	::OleUninitialize();

	return 0;
}

