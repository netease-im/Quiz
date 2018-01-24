package com.netease.nim.quizgame.protocol;

/**
 * Created by huangjun on 2017/11/19.
 */

public final class Servers {

    private final static boolean SERVER_ONLINE = true;

    private static final String APP_KEY_ONLINE = "682a4df6d71da43ce09787dceb502987";
    private static final String APP_KEY_TEST = "f2c46c369f0b0ae5db7956b1a304c985";

    private final static String SERVER_ADDRESS_ONLINE = "https://app.netease.im/appdemo";
    private final static String SERVER_ADDRESS_TEST = "https://apptest.netease.im/appdemo";

    private static boolean isOnlineEnvironment() {
        return SERVER_ONLINE;
    }

    static String getServerAddress() {
        return isOnlineEnvironment() ? SERVER_ADDRESS_ONLINE : SERVER_ADDRESS_TEST;
    }

    public static String getAppKey() {
        return isOnlineEnvironment() ? APP_KEY_ONLINE : APP_KEY_TEST;
    }
}
