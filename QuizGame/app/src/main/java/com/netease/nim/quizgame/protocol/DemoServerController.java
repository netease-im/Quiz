package com.netease.nim.quizgame.protocol;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.quizgame.app.Preferences;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.common.http.HttpClientWrapper;
import com.netease.nim.quizgame.common.http.NimHttpClient;
import com.netease.nim.quizgame.protocol.model.JsonObject2Model;
import com.netease.nim.quizgame.protocol.model.RoomInfo;
import com.netease.nim.quizgame.protocol.model.TouristLoginInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 网易云信Demo Http客户端。第三方开发者请连接自己的应用服务器。
 * <p/>
 * Created by huangjun on 2017/11/18.
 */
public class DemoServerController {

    private static final String TAG = "DemoServerController";

    // code
    private static final int RESULT_CODE_SUCCESS = 200;

    // api
    public static final String SERVICE_NAME = "quiz/player";
    private static final String API_NAME_CREATE = "create";
    private static final String API_NAME_ROOM_QUERY = "room/query";

    // header
    private static final String HEADER_KEY_DEMO_ID = "Demo-Id";
    private static final String HEADER_KEY_APP_KEY = "appkey";

    // request
    private static final String REQUEST_KEY_SID = "sid";
    private static final String REQUEST_KEY_ROOM_ID = "roomId";

    // result
    private static final String RESULT_KEY_CODE = "code";
    private static final String RESULT_KEY_DATA = "data";

    private static final String RESULT_KEY_ROOM_LIST = "list";
    private static final String RESULT_KEY_ROOM_TOTAL = "total";

    public interface IHttpCallback<T> {
        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }

    /**
     * case 1: 请求账号信息
     */
    public void fetchLoginInfo(final IHttpCallback<TouristLoginInfo> callback) {
        NimHttpClient.getInstance().execute(getCreateAPIUrl(), getCommonHeaders(), getCommonRequestParams(), (response, code, exception) -> {
            if (code != 200 || exception != null) {
                LogUtil.e(TAG, "http fetch login info failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                if (callback != null) {
                    callback.onFailed(code, exception != null ? exception.getMessage() : null);
                }
                return;
            }

            try {
                // ret 0
                JSONObject res = JSONObject.parseObject(response);
                // res 1
                int resCode = res.getIntValue(RESULT_KEY_CODE);
                if (resCode == RESULT_CODE_SUCCESS) {
                    // data 1
                    JSONObject data = res.getJSONObject(RESULT_KEY_DATA);
                    // login info 2
                    TouristLoginInfo loginInfo = null;
                    if (data != null) {
                        loginInfo = (TouristLoginInfo) JsonObject2Model.parseJsonObjectToModule(data, TouristLoginInfo.class);
                    }
                    // reply
                    callback.onSuccess(loginInfo);
                } else {
                    callback.onFailed(resCode, null);
                }
            } catch (JSONException e) {
                callback.onFailed(-1, e.getMessage());
            } catch (Exception e) {
                callback.onFailed(-2, e.getMessage());
            }
        });
    }

    /**
     * case 2: 获取聊天室信息
     */
    public void fetchRoomInfo(String roomId, final IHttpCallback<RoomInfo> callback) {
        Map<String, Object> params = new HashMap<>(2);
        params.put(REQUEST_KEY_SID, Preferences.getAccount());
        params.put(REQUEST_KEY_ROOM_ID, roomId);
        String body = HttpClientWrapper.buildRequestParams(params);

        NimHttpClient.getInstance().execute(getRoomQueryUrl(), getCommonHeaders(), body, (response, code, exception) -> {
            if (code != 200 || exception != null) {
                LogUtil.e(TAG, "http fetch room list failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
                if (callback != null) {
                    callback.onFailed(code, exception != null ? exception.getMessage() : null);
                }
                return;
            }

            try {
                // ret 0
                JSONObject res = JSONObject.parseObject(response);
                // res 1
                int resCode = res.getIntValue(RESULT_KEY_CODE);
                if (resCode == RESULT_CODE_SUCCESS) {
                    // data 1
                    RoomInfo roomInfo = null;
                    JSONObject data = res.getJSONObject(RESULT_KEY_DATA);
                    if (data != null) {
                        // roomInfo 2
                        roomInfo = (RoomInfo) JsonObject2Model.parseJsonObjectToModule(data, RoomInfo.class);
                    }
                    // reply
                    callback.onSuccess(roomInfo);
                } else {
                    callback.onFailed(resCode, null);
                }
            } catch (JSONException e) {
                callback.onFailed(-1, e.getMessage());
            } catch (Exception e) {
                callback.onFailed(-2, e.getMessage());
            }
        });
    }

    /**
     * ******************************* api/header/params *******************************
     */

    private String getCreateAPIUrl() {
        return Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_CREATE;
    }

    private String getRoomQueryUrl() {
        return Servers.getServerAddress() + "/" + SERVICE_NAME + "/" + API_NAME_ROOM_QUERY;
    }

    public Map<String, String> getCommonHeaders() {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put(HEADER_KEY_DEMO_ID, "dolls-catcher");
        headers.put(HEADER_KEY_APP_KEY, Servers.getAppKey());
        return headers;
    }

    private String getCommonRequestParams() {
        Map<String, Object> params = new HashMap<>(1);
        params.put(REQUEST_KEY_SID, Preferences.getAccount());
        return HttpClientWrapper.buildRequestParams(params);
    }

    /**
     * ******************************* single instance *******************************
     */

    private static DemoServerController instance;

    public static synchronized DemoServerController getInstance() {
        if (instance == null) {
            instance = new DemoServerController();
        }

        return instance;
    }
}
