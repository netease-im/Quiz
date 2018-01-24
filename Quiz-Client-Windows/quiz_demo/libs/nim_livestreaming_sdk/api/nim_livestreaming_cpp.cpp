#include "nim_livestreaming_cpp.h"
#include "base/file/file_util.h"
#include "base/util/string_util.h"
#include "base/memory/singleton.h"
#include "base/thread/thread_manager.h"
#include "shared/threads.h"
#include "shared/log.h"
#include "shared/util.h"
#include "shared/closure.h"
#include <sys/timeb.h>

#include <SensAPI.h>
#include <objbase.h>

namespace nim_livestream
{
#define pLsClient  ((_HNLSSERVICE*)ls_client_)
#define LsClient  (*(_HNLSSERVICE*)ls_client_)
#define pLsChildClient  ((_HNLSSCHILDSERVICE*)ls_child_client_)
#define LsChildClient  (*(_HNLSSCHILDSERVICE*)ls_child_client_)

	nbase::NLock lock_map_;
	static std::map<void*, LsSession*> InstSessionMap;


	struct AutoNlssDeviceInfo
	{
		ST_NLSS_INDEVICE_INF* devices_;
		int32_t num_;
		AutoNlssDeviceInfo()
		{
			devices_ = nullptr;
			num_ = 0;
		}
		~AutoNlssDeviceInfo()
		{
			Clear();
		}
		void Clear()
		{
			if (num_ > 0)
			{
				for (int32_t i = 0; i < num_; i++)
				{
					delete[] devices_[i].paFriendlyName;
					delete[] devices_[i].paPath;
				}
				delete[] devices_;
				devices_ = nullptr;
				num_ = 0;
			}
		}
		static void CheckDeviceInfo(ST_NLSS_INDEVICE_INF** info, int num)
		{
			if (num > 0)
			{
				*info = new ST_NLSS_INDEVICE_INF[num];
				for (int i = 0; i < num; i++)
				{
					(*info)[i].paPath = new char[1024];
					(*info)[i].paFriendlyName = new char[1024];
				}
			}
		}
		void CheckNum(int32_t num)
		{
			Clear();
			CheckDeviceInfo(&devices_, num);
			num_ = num;
		}
		int32_t GetNum()
		{
			return num_;
		}

	};
	LsSession::LsSession()
	{
		init_session_ = false;
		live_streaming_ = false;
		is_recording_ = false;
		ls_client_ = nullptr;
		ls_child_client_ = nullptr;
		decklink_mode_ = -1;
	}

	LsSession::~LsSession()
	{
		ClearSession();
		int32_t num_time = 0;
		while (!IsClearOk() && num_time < 20)
		{
			num_time++;
			Sleep(100);
		}
		if (!IsClearOk())
		{
			QLOG_ERR(L"LsSession delete err");
		}

		nbase::NAutoLock auto_lock(&lock_);
		printf("clear ls session\n");
	}

	bool LsSession::LoadLivestreamingDll()
	{
		bool ret = NLSSDKInstance::LoadSdkDll();
		if (!ret)
		{
			QLOG_ERR(L"LsManange LoadSdkDll error");
		}
		return ret;
	}

	void  LsSession::UnLoadLivestreamingDll()
	{
		printf("UnLoadLivestreamingDll begin\n");
		int32_t map_num = 0;
		{
			nbase::NAutoLock auto_lock_map(&lock_map_);
			map_num = InstSessionMap.size();
		}
		while (map_num > 0)
		{
			Sleep(100);
			printf("ls session map is not empty\n");
			nbase::NAutoLock auto_lock_map(&lock_map_);
			map_num = InstSessionMap.size();
		}
		NLSSDKInstance::UnLoadSdkDll();
		printf("UnLoadLivestreamingDll end\n");
	}

	bool LsSession::GetAvailableAppWindNum(int *piAppWindNum)
	{
		if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetAvailableAppWindNum)(piAppWindNum))
			return true;
		else  return false;
	}

	bool LsSession::GetAvailableAppWind(NLSS_OUT ST_NLSS_INDEVICE_INF **pLSAppWindTitles, int *iMaxNum)
	{
		GetAvailableAppWindNum(iMaxNum);
		AutoNlssDeviceInfo::CheckDeviceInfo(pLSAppWindTitles, *iMaxNum);

		if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetAvailableAppWind)(*pLSAppWindTitles, *iMaxNum))
		{
			return true;
		}
		else
			return false;
	}

	bool LsSession::GetDevicesNum(int* video_num, int* audio_num)
	{
		if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetFreeDevicesNum)(video_num, audio_num))
			return true;
		else
			return false;
	}

	bool LsSession::GetDeviceInf(std::vector<DeviceInfo> &VideoDevices, std::vector<DeviceInfo> &AudioDevices)
	{
		AutoNlssDeviceInfo auto_video_devices, auto_audio_devices;
		int iMaxVideoDevicesNum = 0;
		int iMaxAudioDevicesNum = 0;
		GetDevicesNum(&iMaxVideoDevicesNum, &iMaxAudioDevicesNum);
		auto_video_devices.CheckNum(iMaxVideoDevicesNum);
		auto_audio_devices.CheckNum(iMaxAudioDevicesNum);
		VideoDevices.clear();
		AudioDevices.clear();
		NLSS_RET ret = NIS_SDK_GET_FUNC(Nlss_GetFreeDeviceInf)(auto_video_devices.devices_, iMaxVideoDevicesNum, auto_audio_devices.devices_, iMaxAudioDevicesNum);
		if (NLSS_OK == ret)
		{
			for (int i = 0; i < iMaxVideoDevicesNum; i++)
			{
				DeviceInfo info;
				info.device_path_ = auto_video_devices.devices_[i].paPath;
				info.friendly_name_ = auto_video_devices.devices_[i].paFriendlyName;
				VideoDevices.push_back(info);
			}
			for (int i = 0; i < iMaxAudioDevicesNum; i++)
			{
				DeviceInfo info;
				info.device_path_ = auto_audio_devices.devices_[i].paPath;
				std::string friend_name = auto_audio_devices.devices_[i].paFriendlyName;
				
				int wideLength = MultiByteToWideChar(CP_ACP, NULL, friend_name.c_str(), -1, NULL, 0);
				std::unique_ptr<wchar_t[]> strText(new wchar_t[wideLength]);
				MultiByteToWideChar(CP_ACP, NULL, friend_name.c_str(), -1, strText.get(), wideLength);
				info.friendly_name_ = nbase::UTF16ToUTF8(strText.get());
				AudioDevices.push_back(info);
			}
		}
		return NLSS_OK == ret;
	}

	bool LsSession::GetPerCameraCaptureinf(ST_NLSS_INDEVICE_INF *pstCamera, NLSS_OUT ST_NLSS_CAMERA_CAPTURE_PARAM **pstCaptureParams, NLSS_OUT int *piNum)
	{
		if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_DeviceGetCamereCaptureInf)(pstCamera, pstCaptureParams, piNum))
			return true;
		else
			return false;
	}
	bool LsSession::GetDeckLinkList(std::vector<DeviceInfo> &Devices)
	{
		int32_t num;
		bool ret = false;
		if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetDeckLinkDeviceList)(nullptr, &num))
		{
			Devices.clear();
			ret = true;
			if (num > 0)
			{
				ret = false;
				AutoNlssDeviceInfo auto_devices;
				auto_devices.CheckNum(num);
				if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetDeckLinkDeviceList)(auto_devices.devices_, &num))
				{
					ret = true;
					for (int i = 0; i < num; i++)
					{
						DeviceInfo info;
						info.device_path_ = auto_devices.devices_[i].paPath;
						info.friendly_name_ = auto_devices.devices_[i].paFriendlyName;
						Devices.push_back(info);
					}
				}
			}
			return ret;
		}
		else
			return ret;
	}
	bool LsSession::GetDeckLinkModeList(const std::string &device_id, std::vector<DeckLinkMode> &modes)
	{
		int32_t num;
		bool ret = false;
		if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetDeckLinkDeviceModeListById)(nullptr, device_id.c_str(), &num))
		{
			modes.clear();
			ret = true;
			if (num > 0)
			{
				ret = false;
				ST_NLSS_INDEVICE_MODE_INF *pstModes = nullptr;
				{
					pstModes = new ST_NLSS_INDEVICE_MODE_INF[num];
					for (int i = 0; i < num; i++)
					{
						pstModes[i].paModeName = new char[1024];
					}
				}
				if (NLSS_OK == NIS_SDK_GET_FUNC(Nlss_GetDeckLinkDeviceModeListById)(pstModes, device_id.c_str(), &num))
				{
					ret = true;
					for (int i = 0; i < num; i++)
					{
						DeckLinkMode info;
						info.name_ = pstModes[i].paModeName;
						info.mode_ = pstModes[i].iMode;
						modes.push_back(info);
					}
				}
				if (pstModes)
				{
					for (int32_t i = 0; i < num; i++)
					{
						delete[] pstModes[i].paModeName;
					}
					delete[] pstModes;
				}
			}
			return ret;
		}
		else
			return ret;
	}

	//直播发生错误回调，当直播过程中发生错误，通知应用层，应用层可以做相应的处理
	void ErrorCallback(_HNLSSERVICE hNLSService, EN_NLSS_STATUS enStatus, EN_NLSS_ERRCODE enErrCode)
	{
		QLOG_ERR(L"livesteaming {0}, {1}") << enStatus << enErrCode;
		switch (enStatus)
		{
		case EN_NLSS_STATUS_ERR: //直播出错
		{
			StdClosure closure = [=]() 
			{
				auto iter = InstSessionMap.find((void*)hNLSService);
				if (iter != InstSessionMap.end())
				{
					LsSession* session = iter->second;
					session->ls_error_cb_(enErrCode);
				}
			};
			Post2UI(closure);
			break;
		}
		default:
			break;
		}
	}

	bool LsSession::InitSession(const std::string& audio, const std::string& url, LsErrorCallback ls_error_cb, PFN_NLSS_MERGED_VIDEO_SAMPLER_CB ls_video_frame_cb)
	{
		nbase::NAutoLock auto_lock(&lock_);
		if (ls_client_ == NULL)
		{
			init_session_ = false;
			ls_client_ = new _HNLSSERVICE;
			std::wstring work_dir = QPath::GetAppPath() + L"live_stream\\";
			std::wstring log_dir = QPath::GetNimAppDataDir() + L"live_stream\\";
			if (!nbase::FilePathIsExist(log_dir, true))
				nbase::CreateDirectory(log_dir);
			NLSS_RET ret = NIS_SDK_GET_FUNC(Nlss_Create)(nbase::UTF16ToUTF8(work_dir).c_str(), nbase::UTF16ToUTF8(log_dir).c_str(), pLsClient);
			QLOG_APP(L"ls sdk dir {0}") << work_dir;
			if (ret != NLSS_OK)
			{
				delete ls_client_;
				ls_client_ = nullptr;
				assert(0);
				return false;
			}

			NIS_SDK_GET_FUNC(Nlss_SetStatusCB)(LsClient, ErrorCallback);
			if (ls_video_frame_cb)
			{
				ls_video_frame_cb_ = ls_video_frame_cb;
				NIS_SDK_GET_FUNC(Nlss_SetVideoSamplerCB)(LsClient, ls_video_frame_cb);
			}
			ls_error_cb_ = ls_error_cb;

			NIS_SDK_GET_FUNC(Nlss_GetDefaultParam)(LsClient, &ls_param_);
			ls_param_.stAudioParam.stIn.iInSamplerate = 44100;
			if (!camera_path_.empty() && decklink_mode_ >= 0)
			{
				ls_param_.stAudioParam.stIn.paaudioDeviceName = (char*)camera_path_.c_str();
				ls_param_.stAudioParam.stIn.enInType = EN_NLSS_AUDIOIN_DECKLINK;
			} 
			else
			{
				ls_param_.stAudioParam.stIn.paaudioDeviceName = (char*)audio.c_str();
				ls_param_.stAudioParam.stIn.enInType = EN_NLSS_AUDIOIN_MIC;
			}
			ls_param_.stVideoParam.enOutCodec = EN_NLSS_VIDEOOUT_CODEC_X264;
			ls_param_.stVideoParam.bHardEncode = false;
			ls_param_.stVideoParam.iOutFps = 20;
			ls_param_.stVideoParam.iOutBitrate = 1200000;
			ls_param_.stVideoParam.iOutHeight = 960;
			ls_param_.stVideoParam.iOutWidth = 540;
			ls_param_.enOutContent = EN_NLSS_OUTCONTENT_AV;
			ls_param_.paOutUrl = (char*)url.c_str();
			ls_param_.bSyncTimestamp = true;
			push_url_ = url;
			audio_path_ = audio;
			if (NIS_SDK_GET_FUNC(Nlss_InitParam)(LsClient, &ls_param_) != NLSS_OK)
			{
				NIS_SDK_GET_FUNC(Nlss_Destroy)(LsClient);
				delete ls_client_;
				ls_client_ = nullptr;
				assert(0);
				return false;
			}
			ret = NIS_SDK_GET_FUNC(Nlss_Start)(LsClient);
			if (ret != NLSS_OK)
			{
				NIS_SDK_GET_FUNC(Nlss_UninitParam)(LsClient);
				NIS_SDK_GET_FUNC(Nlss_Destroy)(LsClient);
				delete ls_client_;
				ls_client_ = nullptr;
				assert(0);
				return false;
			}
			else
			{
				NIS_SDK_GET_FUNC(Nlss_StartVideoPreview)(LsClient);
			}
			nbase::NAutoLock auto_lock_map(&lock_map_);
			InstSessionMap[*ls_client_] = this;
			init_session_ = true;
		}
		return true;
	}

	bool LsSession::OnStartLiveStream(const std::string& audio, OptCallback cb)
	{
		nbase::NAutoLock auto_lock(&lock_);
		if (live_streaming_)
		{
			return true; //当前已经在推流，返回TRUE
		}
		if (ls_client_)
		{
			//if (init_session_)
			//{
			//	NIS_SDK_GET_FUNC(Nlss_UninitParam)(LsClient);
			//}
			//audio_path_ = audio;
			//ls_param_.stAudioParam.stIn.paaudioDeviceName = (char*)audio_path_.c_str();
			//if (NIS_SDK_GET_FUNC(Nlss_InitParam)(LsClient, &ls_param_) != NLSS_OK)
			//{
			//	NIS_SDK_GET_FUNC(Nlss_Destroy)(LsClient);
			//	delete ls_client_;
			//	ls_client_ = nullptr;
			//	assert(0);
			//	return false;
			//}
			nbase::ThreadManager::PostTask(kThreadLiveStreaming, nbase::Bind(&LsSession::DoStartLiveStream, this, audio, cb));
			return true;
		}
		return false;
	}

	bool LsSession::OnStopLiveStream(OptCallback cb)
	{
		nbase::NAutoLock auto_lock(&lock_);
		if (live_streaming_ && ls_client_)
			nbase::ThreadManager::PostTask(kThreadLiveStreaming, nbase::Bind(&LsSession::DoStopLiveStream, this, cb));
		else if (cb)
		{
			Post2UI(nbase::Bind(cb, true));
		}
		return true;
	}

	void LsSession::OnLiveStreamError()
	{
		nbase::ThreadManager::PostTask(kThreadLiveStreaming, ToWeakCallback([this]()
		{
			nbase::NAutoLock auto_lock(&lock_);
			live_streaming_ = false;
		}));
	}
	void LsSession::StartCamera(const std::string& camera, int32_t decklink_mode, OptCallback cb)
	{
		nbase::ThreadManager::PostTask(kThreadLiveStreaming, nbase::Bind(&LsSession::DoStartCamera, this, camera, decklink_mode, cb));
	}
	void LsSession::DoStartCamera(const std::string& camera, int32_t decklink_mode, OptCallback cb)
	{
		nbase::NAutoLock auto_lock(&lock_);
		bool ret = false;
		if (ls_client_)
		{
			if (ls_child_client_)
			{
				NIS_SDK_GET_FUNC(Nlss_ChildVideoStopCapture)(LsChildClient);
				NIS_SDK_GET_FUNC(Nlss_ChildVideoClose)(LsChildClient);
				delete ls_child_client_;
				ls_child_client_ = nullptr;
			}
			if (ls_child_client_ == NULL)
			{
				camera_path_ = camera;
				decklink_mode_ = decklink_mode;
				ST_NLSS_VIDEOIN_PARAM ls_child_video_in_param;
				if (decklink_mode >= 0)
				{
					ls_child_video_in_param.enInType = EN_NLSS_VIDEOIN_DECKLINK;
					ls_child_video_in_param.iCaptureFps = 20;
					ls_child_video_in_param.u.stInDeckLink.paDeviceId = (char*)camera.c_str();
					ls_child_video_in_param.u.stInDeckLink.paDeviceName = nullptr;
					ls_child_video_in_param.u.stInDeckLink.iDeviceMode = decklink_mode;
				} 
				else
				{
					ls_child_video_in_param.enInType = EN_NLSS_VIDEOIN_CAMERA;
					ls_child_video_in_param.iCaptureFps = 20;
					ls_child_video_in_param.u.stInCamera.paDevicePath = (char*)camera.c_str();
					ls_child_video_in_param.u.stInCamera.enLvl = EN_NLSS_VIDEOQUALITY_MIDDLE;
				}
				pLsChildClient = new _HNLSSCHILDSERVICE;
				LsChildClient = NIS_SDK_GET_FUNC(Nlss_ChildVideoOpen)(LsClient, &ls_child_video_in_param);
				if (ls_child_client_)
				{
					NIS_SDK_GET_FUNC(Nlss_ChildVideoSetBackLayer)(LsChildClient);
					::CoInitialize(NULL);
					NLSS_RET r = NIS_SDK_GET_FUNC(Nlss_ChildVideoStartCapture)(LsChildClient);
					//NIS_SDK_GET_FUNC(Nlss_ChildVideoSwitchDisplay)(LsChildClient, false);
					//NIS_SDK_GET_FUNC(Nlss_ChildVideoSetSoloPreviewCB)(LsChildClient, pFunVideoSamplerCB);
					ret = true;
				}
			}
		}
		if (cb)
		{
			Post2UI(nbase::Bind(cb, ret));
		}
	}

	//开始直播
	void LsSession::DoStartLiveStream(const std::string& audio, OptCallback cb)
	{
		nbase::NAutoLock auto_lock(&lock_);
		bool success = false;
		if (live_streaming_)
		{
			success = true;
		}
		else
		{
			if (audio != audio_path_ || decklink_mode_ >= 0)
			{
				QLOG_APP(L"DoStartLiveStream init again.");
				DoClearSession();
				InitSession(audio, push_url_, ls_error_cb_, ls_video_frame_cb_);
				if (!camera_path_.empty())
				{
					DoStartCamera(camera_path_, decklink_mode_, OptCallback());
				}
			}
			if (ls_client_ && ls_child_client_)
			{
				QLOG_APP(L"Do Nlss_StartLiveStream");
				//if (ls_child_client_)
				//{
				//	//NIS_SDK_GET_FUNC(Nlss_ChildVideoSetBackLayer)(LsChildClient);
				//	DoStartCamera(camera_path_, OptCallback());
				//	NIS_SDK_GET_FUNC(Nlss_StartVideoPreview)(LsClient);
				//}
				if (NIS_SDK_GET_FUNC(Nlss_StartLiveStream)(LsClient) == NLSS_OK)
				{
					live_streaming_ = true;
					success = true;
				}
				else //开启直播错误，不管什么原因，只返回NLSS_ERROR
					QLOG_ERR(L"Nlss_StartLiveStream error.");
			}
		}
		if (cb)
		{
			Post2UI(nbase::Bind(cb, success));
		}
	}

	//结束直播
	void LsSession::DoStopLiveStream(OptCallback cb)
	{
		nbase::NAutoLock auto_lock(&lock_);
		bool ret = true;
		if (live_streaming_ && ls_client_)
		{
			ret = false;
			NIS_SDK_GET_FUNC(Nlss_StopLiveStream)(LsClient);
			live_streaming_ = false;
			ret = true;
		}
		if (ls_child_client_)
		{
			NIS_SDK_GET_FUNC(Nlss_ChildVideoStopCapture)(LsChildClient);
			NIS_SDK_GET_FUNC(Nlss_ChildVideoClose)(LsChildClient);
			delete ls_child_client_;
			ls_child_client_ = nullptr;
		}
		QLOG_APP(L"StopLiveStream success.");
		if (cb)
		{
			Post2UI(nbase::Bind(cb, ret));
		}
	}

	void LsSession::ClearSession()
	{
		QLOG_APP(L"LsSession ClearSession");
		nbase::NAutoLock auto_lock(&lock_);
		if (ls_client_ || ls_child_client_)
			nbase::ThreadManager::PostTask(kThreadLiveStreaming, nbase::Bind(&LsSession::DoClearSession, this));
	}
	bool LsSession::IsClearOk()
	{
		if (ls_client_ || ls_child_client_)
			return false;
		return true;
	}

	void LsSession::DoClearSession()
	{
		QLOG_APP(L"LsSession DoClearSession");
		nbase::NAutoLock auto_lock(&lock_);
		if (live_streaming_ && ls_client_)
		{
			NIS_SDK_GET_FUNC(Nlss_StopLiveStream)(LsClient);
			live_streaming_ = false;
		}
		if (ls_child_client_)
		{
			NIS_SDK_GET_FUNC(Nlss_ChildVideoStopCapture)(LsChildClient);
			NIS_SDK_GET_FUNC(Nlss_ChildVideoClose)(LsChildClient);
			delete ls_child_client_;
			ls_child_client_ = nullptr;
		}
		if (ls_client_)
		{
			NIS_SDK_GET_FUNC(Nlss_SetVideoSamplerCB)(LsClient, nullptr);
			NIS_SDK_GET_FUNC(Nlss_StopVideoPreview)(LsClient);
			NIS_SDK_GET_FUNC(Nlss_Stop)(LsClient);
			NIS_SDK_GET_FUNC(Nlss_UninitParam)(LsClient);
			NIS_SDK_GET_FUNC(Nlss_Destroy)(LsClient);
			{
				nbase::NAutoLock auto_lock_map(&lock_map_);
				InstSessionMap.erase(*ls_client_);
			}
			delete ls_client_;
			ls_client_ = nullptr;
			init_session_ = false;
		}
	}

	//直播过程中的统计信息
	bool LsSession::GetStaticInfo(NLSS_OUT ST_NLSS_STATS &pstStats)
	{
		nbase::NAutoLock auto_lock(&lock_);
		bool ret = false;
		if (ls_client_)
		{
			ret = NIS_SDK_GET_FUNC(Nlss_GetStaticInfo)(LsClient, &pstStats) == NLSS_OK;
		}
		return ret;
	}

	bool LsSession::StartRecord(char*path)
	{
		nbase::NAutoLock auto_lock(&lock_);
		bool ret = false;
		if (!is_recording_)
		{
			ret = NIS_SDK_GET_FUNC(Nlss_StartRecord)(LsClient, path) == NLSS_OK;
			is_recording_ = ret;
		}
		return ret;
	}

	void LsSession::StopRecord()
	{
		nbase::NAutoLock auto_lock(&lock_);
		if (is_recording_)
		{
			NIS_SDK_GET_FUNC(Nlss_StopRecord)(LsClient);
			is_recording_ = false;
		}
	}
	uint64_t LsSession::GetSyncTimestamp()
	{
		nbase::NAutoLock auto_lock(&lock_);
		if (ls_client_ && live_streaming_)
		{
			return NIS_SDK_GET_FUNC(Nlss_GetSyncTimestamp)(LsClient);
		}
		return 0;
	}
	uint64_t LsSession::GetSyncPts()
	{
		nbase::NAutoLock auto_lock(&lock_);
		if (ls_client_ && live_streaming_)
		{
			return NIS_SDK_GET_FUNC(Nlss_GetStreamPts)(LsClient);
		}
		return 0;
	}
}