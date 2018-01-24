package com.netease.nim.quizgame.common;

import com.netease.nimlib.log.sdk.wrapper.NimLog;

public class LogUtil extends NimLog {

    private static final String LOG_FILE_NAME_PREFIX = "demo";

    public static void init(String logDir, int level) {
        NimLog.initDateNLog(null, logDir, LOG_FILE_NAME_PREFIX, level, 0, 0, true, null);
    }

    public static void app(String msg) {
        getLog().i("app", buildMessage(msg));
    }

    public static void hotfix(String msg) {
        getLog().i("HOTFIX", buildMessage(msg));
    }
}
