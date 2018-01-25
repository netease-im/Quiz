package com.netease.nim.quizgame.protocol;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.quizgame.app.Preferences;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.common.http.HttpClientWrapper;
import com.netease.nim.quizgame.common.http.NimHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏过程答题控制器
 * 与应用服务器进行互动
 * Created by winnie on 11/01/2018.
 */

public class QuizServerController {

    private static final String TAG = "QuizServerController";

    // api
    private static final String API_NAME_ANSWER = "answer";

    // request
    private static final String REQUEST_KEY_SID = "sid";
    private static final String REQUEST_KEY_ROOM_ID = "roomId";
    private static final String REQUEST_QUESTION_ID = "questionId";
    private static final String REQUEST_ANSWER = "answer";

    // result
    private static final String RESULT_KEY_CODE = "code";
    private static final String RESULT_KEY_DATA = "data";

    private static final String RESULT_KEY_RESULT = "result";

    /**
     * 提交答案
     */
    public void answer(String roomId, long questionId, int answer, final DemoServerController.IHttpCallback<Integer> callback) {
        Map<String, Object> params = new HashMap<>(2);
        params.put(REQUEST_KEY_SID, Preferences.getAccount());
        params.put(REQUEST_KEY_ROOM_ID, roomId);
        params.put(REQUEST_QUESTION_ID, questionId);
        params.put(REQUEST_ANSWER, answer);
        String body = HttpClientWrapper.buildRequestParams(params);

        NimHttpClient.getInstance().execute(getAnswerAPIUrl(),
                DemoServerController.getInstance().getCommonHeaders(), body, (response, code, exception) -> {
            if (code != 200 || exception != null) {
                LogUtil.e(TAG, "http answer failed, code=" + code + ", error=" + (exception != null ? exception.getMessage() : "null"));
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
                if (resCode == 200) {
                    // data 1
                    int result = 2;
                    JSONObject data = res.getJSONObject(RESULT_KEY_DATA);
                    if (data != null) {
                        // result 2
                        result = data.getIntValue(RESULT_KEY_RESULT);
                    }
                    // reply
                    callback.onSuccess(result);
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
     * ******************************* api *******************************
     */

    public String getAnswerAPIUrl() {
        return Servers.getServerAddress() + "/" + DemoServerController.SERVICE_NAME + "/" + API_NAME_ANSWER;
    }

    /**
     * ******************************* single instance *******************************
     */

    private static QuizServerController instance;

    public static synchronized QuizServerController getInstance() {
        if (instance == null) {
            instance = new QuizServerController();
        }

        return instance;
    }
}
