#ifndef NIM_LIVESTREAM_API_CPP_H_
#define NIM_LIVESTREAM_API_CPP_H_
#include "base/synchronization/lock.h"
#include "base/callback/callback.h"
#include "include/nlss_type.h"
#include "util/nls_sdk_util.h"
namespace nim_livestream
{
	typedef NLSS_RET(*Nlss_GetAvailableAppWindNum)(int *iAppWindNum);
	typedef NLSS_RET(*Nlss_GetAvailableAppWind)(NLSS_OUT ST_NLSS_INDEVICE_INF *pLSAppWindTitles, int iMaxNum);
	typedef NLSS_RET(*Nlss_GetFreeDevicesNum)(NLSS_OUT int *iVideoDeviceNum, NLSS_OUT int *iAudioDeviceNum);
	typedef NLSS_RET(*Nlss_GetFreeDeviceInf)(NLSS_OUT ST_NLSS_INDEVICE_INF *pstVideoDevices, int iMaxVideoDevicesNum, NLSS_OUT ST_NLSS_INDEVICE_INF* pstAudioDevices, int iMaxAudioDevicesNum);
	typedef NLSS_RET(*Nlss_DeviceGetCamereCaptureInf)(ST_NLSS_INDEVICE_INF *pstCamera, NLSS_OUT ST_NLSS_CAMERA_CAPTURE_PARAM **pstCaptureParams, NLSS_OUT int *piNum);
	typedef NLSS_RET(*Nlss_Create)(const char *paWorkPath, const char *paCachePath, NLSS_OUT _HNLSSERVICE *phNLSService);
	typedef void(*Nlss_Destroy)(_HNLSSERVICE hNLSService);
	typedef NLSS_RET(*Nlss_GetDefaultParam)(_HNLSSERVICE hNLSService, NLSS_OUT ST_NLSS_PARAM *pstParam);
	typedef NLSS_RET(*Nlss_InitParam)(_HNLSSERVICE hNLSService, ST_NLSS_PARAM *pstParam);
	typedef void(*Nlss_UninitParam)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_SetVideoSamplerCB)(_HNLSSERVICE hNLSService, PFN_NLSS_MERGED_VIDEO_SAMPLER_CB pFunVideoSamplerCB);
	typedef void(*Nlss_ChildVideoSetSoloPreviewCB)(_HNLSSCHILDSERVICE hNLSSChild, PFN_NLSS_CHILD_VIDEO_SAMPLER_CB pFunVideoSamplerCB);
	typedef void(*Nlss_SetStatusCB)(_HNLSSERVICE hNLSService, PFN_NLSS_STATUS_NTY pFunStatusNty);
	typedef NLSS_RET(*Nlss_Start)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_Stop)(_HNLSSERVICE hNLSService);
	typedef  NLSS_RET(*Nlss_StartVideoPreview)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_PauseVideoPreview)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_ResumeVideoPreview)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_StopVideoPreview)(_HNLSSERVICE hNLSService);
	typedef NLSS_RET(*Nlss_StartLiveStream)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_StopLiveStream)(_HNLSSERVICE hNLSService);
	typedef void(*Nlss_ChildVideoSwitchDisplay)(_HNLSSCHILDSERVICE hNLSSChild, bool bHide);
	typedef _HNLSSCHILDSERVICE(*Nlss_ChildVideoOpen)(_HNLSSERVICE hNLSService, ST_NLSS_VIDEOIN_PARAM *pVideoInParam);
	typedef void(*Nlss_ChildVideoClose)(_HNLSSCHILDSERVICE hNLSSChild);
	typedef void(*Nlss_ChildVideoSetBackLayer)(_HNLSSCHILDSERVICE hNLSSChild);
	typedef NLSS_RET(*Nlss_ChildVideoStartCapture)(_HNLSSCHILDSERVICE hNLSSChild);
	typedef void(*Nlss_ChildVideoStopCapture)(_HNLSSCHILDSERVICE hNLSSChild);
	typedef NLSS_RET(*Nlss_VideoChildSendCustomData)(_HNLSSCHILDSERVICE hNLSSChild, char *pcVideoData, int iLen);
	typedef NLSS_RET(*Nlss_SendCustomAudioData)(_HNLSSERVICE hNLSService, char *pcAudioData, int iLen, int iSampleRate);
	typedef NLSS_RET(*Nlss_GetStaticInfo)(_HNLSSERVICE hNLSService, NLSS_OUT ST_NLSS_STATS *pstStats);
	typedef NLSS_RET(*Nlss_StartRecord)(_HNLSSERVICE hNLSService, char *pcRecordPath);
	typedef void(*Nlss_StopRecord)(_HNLSSERVICE hNLSService);
	typedef unsigned long long(*Nlss_GetStreamPts)(_HNLSSERVICE hNLSService); 
	typedef  unsigned long long(*Nlss_GetSyncTimestamp)(_HNLSSERVICE hNLSService);

	typedef   NLSS_RET(*Nlss_GetDeckLinkDeviceList)(NLSS_OUT ST_NLSS_INDEVICE_INF *pstDeckLinkDevices, NLSS_IN_OUT int* iMaxDeckLinkDeviceNum);
	typedef   NLSS_RET(*Nlss_GetDeckLinkDeviceModeListById)(NLSS_OUT ST_NLSS_INDEVICE_MODE_INF *pstDeckLinkDeviceModes, const char* paDeviceId, NLSS_IN_OUT int* iMaxDeckLinkDeviceModeNum);


	//操作结果
	typedef std::function<void(bool ret)> OptCallback;
	typedef std::function<void(EN_NLSS_ERRCODE errCode)> LsErrorCallback;
	typedef std::function<void(ST_NLSS_VIDEO_SAMPLER *pstSampler)> LsVideoFrameCallback;
	typedef void(*ErrorOpt) (bool);

	struct DeviceInfo
	{
		std::string device_path_;
		std::string friendly_name_;
	};
	struct DeckLinkMode
	{
		std::string name_;
		int32_t mode_;
	};
	class LsSession :public virtual nbase::SupportWeakCallback
	{
	public:
		LsSession();
		~LsSession();
	public:
		//初始化和释放dll，必须先调用init才能使用nim_ls中的其他接口
		static bool LoadLivestreamingDll();
		static void UnLoadLivestreamingDll();
		//设备
		static  bool  GetAvailableAppWindNum(int *piAppWindNum);
		static  bool  GetAvailableAppWind(ST_NLSS_INDEVICE_INF **pLSAppWindTitles, int* iMaxNum);
		static  bool  GetDevicesNum(int* video_num, int* audio_num);
		static  bool  GetDeviceInf(std::vector<DeviceInfo> &VideoDevices, std::vector<DeviceInfo> &pstAudioDevices);
		//static void   ClearDeviceInfo(ST_NLSS_INDEVICE_INF** devices, int num);
		static  bool  GetPerCameraCaptureinf(ST_NLSS_INDEVICE_INF *pstCamera, NLSS_OUT ST_NLSS_CAMERA_CAPTURE_PARAM **pstCaptureParams, NLSS_OUT int *piNum);
		static	bool  GetDeckLinkList(std::vector<DeviceInfo> &Devices);
		static  bool  GetDeckLinkModeList(const std::string &device_id, std::vector<DeckLinkMode> &modes);
		//初始化直播模块
		bool InitSession(const std::string& audio, const std::string& url, LsErrorCallback ls_error_cb, PFN_NLSS_MERGED_VIDEO_SAMPLER_CB ls_video_frame_cb = nullptr);
		void ClearSession();
		bool IsClearOk();
		friend void ErrorCallback(_HNLSSERVICE hNLSService, EN_NLSS_STATUS enStatus, EN_NLSS_ERRCODE enErrCode);

		//开始直播推流
		bool OnStartLiveStream(const std::string& audio, OptCallback cb);
		//结束直播推流
		bool OnStopLiveStream(OptCallback cb);
		void OnLiveStreamError();


		bool IsLivingsteam() { return live_streaming_; }
		bool IsLsInit() { return init_session_; }

		//直播过程中的统计信息
		bool GetStaticInfo(NLSS_OUT ST_NLSS_STATS  &pstStats);

		bool StartRecord(char*path);
		void StopRecord();
		void StartCamera(const std::string& camera, int32_t decklink_mode, OptCallback cb);
		uint64_t GetSyncTimestamp();
		uint64_t GetSyncPts();
	private:
		void DoStartCamera(const std::string& camera, int32_t decklink_mode, OptCallback cb);
		//开始直播推流
		void DoStartLiveStream(const std::string& audio, OptCallback cb);
		//结束直播推流
		void DoStopLiveStream(OptCallback cb);
		void DoClearSession();

	private:
		nbase::NLock lock_;
		ST_NLSS_PARAM ls_param_;
		_HNLSSERVICE* ls_client_;
		_HNLSSCHILDSERVICE  *ls_child_client_;
		std::string push_url_;
		std::string  audio_path_;
		std::string  camera_path_;
		int32_t decklink_mode_;
		bool init_session_;
		bool live_streaming_;
		bool is_recording_;
		std::wstring current_work_dir_;
		LsErrorCallback ls_error_cb_;
		PFN_NLSS_MERGED_VIDEO_SAMPLER_CB ls_video_frame_cb_;
	};
}
#endif// NIM_LIVESTREAM_API_CPP_H_



