
#include "video_frame_mng.h"
#include "libyuv.h"
#include <sys/timeb.h>
#include "include/nlss_type.h"

namespace nim_comp
{
VideoFrameMng::VideoFrameMng()
{
}

VideoFrameMng::~VideoFrameMng()
{
	Clear();
}
void VideoFrameMng::Clear()
{
	nbase::NAutoLock auto_lock(&lock_);
	capture_video_pic_.Clear();
}
void VideoFrameMng::AddVideoFrame(const char* data, int size, int width, int height, FrameType frame_type)
{
	//nim::NIMVideoSubType subtype = nim::kNIMVideoSubTypeI420;
	timeb time_now;
	ftime(&time_now); // 秒数
	int64_t cur_timestamp = time_now.time * 1000 + time_now.millitm; // 毫秒
	const char* src_buffer = data;
	std::string ret_data;
	if (frame_type != Ft_I420)
	{
		int byte_width = width * 4;
		width -= width % 2;
		height -= height % 2;
		int wxh = width * height;
		ret_data.append(wxh * 3 / 2, (char)0);
		uint8_t* des_y = (uint8_t*)ret_data.c_str();
		uint8_t* des_u = des_y + wxh;
		uint8_t* des_v = des_u + wxh / 4;
		const uint8_t* src_argb = (const uint8_t*)data;
		if (frame_type == Ft_ARGB_r)
		{
			src_argb = (const uint8_t*)data + size - byte_width;
			byte_width = -byte_width;
		}
		libyuv::ARGBToI420(src_argb, byte_width,
			des_y, width,
			des_u, width / 2,
			des_v, width / 2,
			width, height);
		src_buffer = ret_data.c_str();
		size = wxh * 3 / 2;
	}
	nbase::NAutoLock auto_lock(&lock_);
	capture_video_pic_.ResetData(cur_timestamp, src_buffer, size, width, height/*, subtype*/);
}
void VideoFrameMng::AddVideoFrameEx(const char* data, int size, int width, int height, int32_t ls_type)
{
	//nim::NIMVideoSubType subtype = nim::kNIMVideoSubTypeI420;
	timeb time_now;
	ftime(&time_now); // 秒数
	int64_t cur_timestamp = time_now.time * 1000 + time_now.millitm; // 毫秒
	const char* src_buffer = data;
	std::string ret_data(data, size);
	int32_t wxh = width * height;
	switch (ls_type)
	{
	case EN_NLSS_VIDEOIN_FMT_NV21:
	{
		ret_data.append(wxh * 3 / 2, (char)0);
		uint8_t* des_y = (uint8_t*)ret_data.c_str();
		uint8_t* des_u = des_y + wxh;
		uint8_t* des_v = des_u + wxh / 4;
		libyuv::NV12ToI420((const uint8_t*)data, width,
						   (const uint8_t*)data + wxh, width,
						   des_y, width,
						   des_u, width / 2,
						   des_v, width / 2,
						   width, height);
		src_buffer = ret_data.c_str();
		size = wxh * 3 / 2;
		break;
	}
	case EN_NLSS_VIDEOIN_FMT_I420:
	{
		break;
	}
	case EN_NLSS_VIDEOIN_FMT_BGRA32:
	{
		ret_data.append(wxh * 3 / 2, (char)0);
		uint8_t* des_y = (uint8_t*)ret_data.c_str();
		uint8_t* des_u = des_y + wxh;
		uint8_t* des_v = des_u + wxh / 4;
		libyuv::ARGBToI420((const uint8_t*)data, width * 4,
							des_y, width,
							des_u, width / 2,
							des_v, width / 2,
							width, height);
		src_buffer = ret_data.c_str();
		size = wxh * 3 / 2;
		break;
	}
	case EN_NLSS_VIDEOIN_FMT_ARGB32:
	{
		ret_data.append(wxh * 3 / 2, (char)0);
		uint8_t* des_y = (uint8_t*)ret_data.c_str();
		uint8_t* des_u = des_y + wxh;
		uint8_t* des_v = des_u + wxh / 4;
		libyuv::BGRAToI420((const uint8_t*)data, width * 4,
						   des_y, width,
						   des_u, width / 2,
						   des_v, width / 2,
						   width, height);
		src_buffer = ret_data.c_str();
		size = wxh * 3 / 2;
		break;
	}
	case EN_NLSS_VIDEOIN_FMT_YUY2:
	{
		ret_data.append(wxh * 3 / 2, (char)0);
		uint8_t* des_y = (uint8_t*)ret_data.data();
		uint8_t* des_u = des_y + width * height;
		uint8_t* des_v = des_u + width * height / 4;
		libyuv::YUY2ToI420((const uint8*)data, width * 2,
						   des_y, width,
						   des_u, width / 2,
						   des_v, width / 2,
						   width, height);
		src_buffer = ret_data.data();
		size = wxh * 3 / 2;
		break;
	}
	case EN_NLSS_VIDEOIN_FMT_BGR24:
	{
		ret_data.append(wxh * 3 / 2, (char)0);
		uint8_t* des_y = (uint8_t*)ret_data.c_str();
		uint8_t* des_u = des_y + wxh;
		uint8_t* des_v = des_u + wxh / 4;
		libyuv::RGB24ToI420((const uint8_t*)data, width * 3,
							des_y, width,
							des_u, width / 2,
							des_v, width / 2,
							width, height);
		src_buffer = ret_data.c_str();
		size = wxh * 3 / 2;
		break;
	}
	default:
		return;
	}
	nbase::NAutoLock auto_lock(&lock_);
	capture_video_pic_.ResetData(cur_timestamp, src_buffer, size, width, height/*, subtype*/);
}
bool VideoFrameMng::GetVideoFrame(uint64_t& time, char* out_data, int& width, int& height, bool mirror, bool argb_or_yuv)
{
	nbase::NAutoLock auto_lock(&lock_);
	timeb time_now;
	ftime(&time_now); // 秒数
	uint64_t cur_timestamp = time_now.time * 1000 + time_now.millitm; // 毫秒
	PicRegion* pic_info = &capture_video_pic_;
	if (pic_info && pic_info->pdata_ && time < pic_info->timestamp_ && cur_timestamp - 1000 < pic_info->timestamp_)
	{
		time = pic_info->timestamp_;
		int src_w = pic_info->width_;
		int src_h = pic_info->height_;
		//等比
		if (width <= 0 || height <= 0)
		{
			width = src_w;
			height = src_h;
		}
		else if (src_h * width > src_w * height)
		{
			width = src_w * height / src_h;
		}
		else
		{
			height = src_h * width / src_w;
		}
		width -= width % 2;
		height -= height % 2;

		std::string ret_data;
		if (width != src_w || height != src_h)
		{
			ret_data.append(width * height * 3 / 2, (char)0);
			uint8_t* src_y = (uint8_t*)pic_info->pdata_;
			uint8_t* src_u = src_y + src_w * src_h;
			uint8_t* src_v = src_u + src_w * src_h / 4;
			uint8_t* des_y = (uint8_t*)ret_data.c_str();
			uint8_t* des_u = des_y + width * height;
			uint8_t* des_v = des_u + width * height / 4;
			libyuv::FilterMode filter_mode = libyuv::kFilterBox;
			libyuv::I420Scale(src_y, src_w,
				src_u, src_w / 2,
				src_v, src_w / 2,
				src_w, src_h,
				des_y, width,
				des_u, width / 2,
				des_v, width / 2,
				width, height,
				filter_mode);
		}
		else
		{
			ret_data.append(pic_info->pdata_, pic_info->size_);
		}
		if (mirror)
		{
			std::string data_src_temp = ret_data;
			uint8_t* src_y = (uint8_t*)data_src_temp.c_str();
			uint8_t* src_u = src_y + width * height;
			uint8_t* src_v = src_u + width * height / 4;
			uint8_t* des_y = (uint8_t*)ret_data.c_str();
			uint8_t* des_u = des_y + width * height;
			uint8_t* des_v = des_u + width * height / 4;
			libyuv::I420Mirror(src_y, width,
				src_u, width / 2,
				src_v, width / 2,
				des_y, width,
				des_u, width / 2,
				des_v, width / 2,
				width, height);
		}
		if (argb_or_yuv)
		{
			uint8_t* des_y = (uint8_t*)ret_data.c_str();
			uint8_t* des_u = des_y + width * height;
			uint8_t* des_v = des_u + width * height / 4;
			libyuv::I420ToARGB(
				des_y, width,
				des_u, width / 2,
				des_v, width / 2,
				(uint8_t*)out_data, width * 4,
				width, height);
		} 
		else
		{
			memcpy(out_data, ret_data.c_str(), ret_data.size());
		}
		return true;
	}
	return false;
}
}