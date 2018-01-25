#include "stdafx.h"
#include "util.h"
#include "shared/log.h"
#include "shellapi.h"
#include <shlobj.h> // SHCreateDirectory
//
////
//std::string QString::GetGUID()
//{
//	return nim::Tool::GetUuid();
//}
//
//std::string QString::GetMd5(const std::string& input)
//{
//	return nim::Tool::GetMd5(input);
//}
//
//void QString::NIMFreeBuf(void *data)
//{
//	return nim::Global::FreeBuf(data);
//}

//
std::wstring QPath::GetAppPath()
{
	return nbase::win32::GetCurrentModuleDirectory();
}

//std::wstring QPath::GetUserAppDataDir(const std::string& app_account)
//{
//	return nbase::UTF8ToUTF16(nim::Tool::GetUserAppdataDir(app_account));
//}

bool IsRunningOnVistaOrHigher()
{
	OSVERSIONINFO os_version = { 0 };
	os_version.dwOSVersionInfoSize = sizeof(os_version);
	GetVersionEx(&os_version);
	return os_version.dwMajorVersion >= 6;
}

std::wstring GetLocalAppDataDirForDesktop(HANDLE token /* = NULL */)
{
#if (NTDDI_VERSION < NTDDI_VISTA)
#ifndef KF_FLAG_CREATE
#define KF_FLAG_CREATE 0x00008000
#endif
#endif

	std::wstring temp_path;
	if (IsRunningOnVistaOrHigher())
	{
		typedef HRESULT(WINAPI *__SHGetKnownFolderPath)(REFKNOWNFOLDERID, DWORD, HANDLE, PWSTR*);
		HMODULE moudle_handle = ::LoadLibraryW(L"shell32.dll");
		if (moudle_handle != NULL)
		{
			__SHGetKnownFolderPath _SHGetKnownFolderPath =
				reinterpret_cast<__SHGetKnownFolderPath>(::GetProcAddress(moudle_handle, "SHGetKnownFolderPath"));
			if (_SHGetKnownFolderPath != NULL)
			{
				PWSTR result = NULL;
				if (S_OK == _SHGetKnownFolderPath(FOLDERID_LocalAppData, KF_FLAG_CREATE, token, &result))
				{
					temp_path = result;
					::CoTaskMemFree(result);
				}
			}
			::FreeLibrary(moudle_handle);
		}
	}
	else
	{
		// On Windows XP, CSIDL_LOCAL_APPDATA represents "{user}\Local Settings\Application Data"
		// while CSIDL_APPDATA represents "{user}\Application Data"
		wchar_t buffer[MAX_PATH];
		if (S_OK == ::SHGetFolderPath(NULL, CSIDL_LOCAL_APPDATA | CSIDL_FLAG_CREATE, token, SHGFP_TYPE_CURRENT, buffer))
			temp_path = buffer;
	}
	if (!temp_path.empty())
		if (temp_path.back() != L'\\')
			temp_path.push_back(L'\\');
	return temp_path;
}

std::wstring QPath::GetLocalAppDataDir()
{
	std::wstring default_local_app_dir_path = GetLocalAppDataDirForDesktop(0);
	//return nbase::UTF8ToUTF16(nim::Tool::GetLocalAppdataDir());
	return default_local_app_dir_path;
}

std::wstring QPath::GetNimAppDataDir()
{
	std::wstring dir = QPath::GetLocalAppDataDir();
	dir.append(L"NIM_QUIZ\\");

	return dir;
}

//
std::map<std::wstring,std::wstring> QCommand::key_value_;

void QCommand::ParseCommand( const std::wstring &cmd )
{
	std::list<std::wstring> arrays = ui::StringHelper::Split(cmd, L"/");
	for(std::list<std::wstring>::const_iterator i = arrays.begin(); i != arrays.end(); i++)
	{
		std::list<std::wstring> object = ui::StringHelper::Split(*i, L" ");
		assert(object.size() == 2);
		key_value_[ *object.begin() ] = *object.rbegin();
	}
}

std::wstring QCommand::Get( const std::wstring &key )
{
	std::map<std::wstring,std::wstring>::const_iterator i = key_value_.find(key);
	if(i == key_value_.end())
		return L"";
	else
		return i->second;
}

void QCommand::Set( const std::wstring &key, const std::wstring &value )
{
	key_value_[key] = value;
}

void QCommand::Erase(const std::wstring &key)
{
	key_value_.erase(key);
}

bool QCommand::AppStartWidthCommand( const std::wstring &app, const std::wstring &cmd )
{
	HINSTANCE hInst = ::ShellExecuteW(NULL, L"open", app.c_str(), cmd.c_str(), NULL, SW_SHOWNORMAL);
	return (int)hInst > 32;
}

bool QCommand::RestartApp(const std::wstring &cmd)
{
	wchar_t app[1024] = { 0 };
	GetModuleFileName(NULL, app, 1024);
	HINSTANCE hInst = ::ShellExecuteW(NULL, L"open", app, cmd.c_str(), NULL, SW_SHOWNORMAL);
	return (int)hInst > 32;
}