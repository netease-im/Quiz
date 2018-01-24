package com.netease.nim.quizgame.app;

import android.content.Context;

import com.netease.nim.quizgame.protocol.model.TouristLoginInfo;


/**
 * Created by huangjun on 2017/11/19.
 */

public class AppCache {

    private AppCache() {
    }

    private static Context context;

    private static TouristLoginInfo loginInfo;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AppCache.context = context;
    }

    public static TouristLoginInfo getLoginInfo() {
        return loginInfo;
    }

    public static void setLoginInfo(TouristLoginInfo loginInfo) {
        AppCache.loginInfo = loginInfo;
        Preferences.saveAccount(loginInfo.getAccount());
        Preferences.saveToken(loginInfo.getToken());
    }
}
