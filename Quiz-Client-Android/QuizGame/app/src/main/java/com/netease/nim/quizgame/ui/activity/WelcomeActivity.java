package com.netease.nim.quizgame.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.netease.nim.quizgame.R;
import com.netease.nim.quizgame.app.AppCache;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.common.http.NimHttpClient;
import com.netease.nim.quizgame.common.net.NetworkUtil;
import com.netease.nim.quizgame.common.thread.Handlers;
import com.netease.nim.quizgame.common.ui.UI;
import com.netease.nim.quizgame.protocol.DemoServerController;
import com.netease.nim.quizgame.protocol.model.TouristLoginInfo;

/**
 * APP启动页
 * <p>
 * Created by huangjun on 2017/11/17.
 */
public class WelcomeActivity extends UI {
    private static final int HTTP_TIMEOUT = 10 * 1000;
    private static final int MAX_FETCH_LOGIN_INFO_TRY_COUNT = 3;
    private static final int AUTO_QUIT_DELAY_TIME_AFTER_FETCH_LOGIN_INFO_FAILED = 3 * 1000;

    private static boolean firstEnter = true; // 是否首次进入

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.app("WelcomeActivity onCreate");

        setContentView(R.layout.activity_welcome);

        if (savedInstanceState != null) {
            setIntent(new Intent()); // 从堆栈恢复，不再重复解析之前的intent
        }

        init(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.app("WelcomeActivity onNewIntent, intent=" + intent);

        super.onNewIntent(intent);
        init(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firstEnter) {
            firstEnter = false; // 进程活着，已经进来过一次了.
        }
    }

    /**
     * 检查网络状态，无网络则退出APP
     */
    private void init(Intent intent) {
        if (!firstEnter) {
            // 已经进入过了，可能APP退到后台后，从Launcher点开，又走了onCreate
            if (AppCache.getLoginInfo() != null) {
                onEnter(AppCache.getLoginInfo());
                return;
            }
        }

        if (!NetworkUtil.isNetAvailable(this)) {
            LogUtil.app("network is unavailable!");
            Toast.makeText(this.getApplicationContext(), R.string.net_unavailable, Toast.LENGTH_LONG).show();
            autoQuit();
            return;
        }

        LogUtil.app("network ok, start init, intent=" + intent);

        requestLoginInfo(1); // 从云信Demo服务器获取登录信息
    }

    /**
     * 从云信服务器获取登录信息
     */
    private void requestLoginInfo(final int tryCount) {
        LogUtil.app("fetch login info, try count=" + tryCount);
        NimHttpClient.getInstance().init(getApplicationContext()); // 初始化HttpClient
        DemoServerController.getInstance().fetchLoginInfo(new DemoServerController.IHttpCallback<TouristLoginInfo>() {
            @Override
            public void onSuccess(TouristLoginInfo touristLoginInfo) {
                onEnter(touristLoginInfo);
            }

            @Override
            public void onFailed(int code, String error) {
                if (tryCount >= MAX_FETCH_LOGIN_INFO_TRY_COUNT) {
                    onFetchLoginInfoFailed(code, error);
                } else {
                    requestLoginInfo(tryCount + 1);
                }
            }
        });
    }

    /**
     * 失败，退出APP
     */
    private void onFetchLoginInfoFailed(int code, String error) {
        String tip = TextUtils.isEmpty(error) ? "" : ", error=" + error;
        LogUtil.app("on enter failed, as fetch login info from http server failed, code=" + code + tip);
        Toast.makeText(this, "无法获取云信Demo服务器数据, code=" + code + tip, Toast.LENGTH_LONG).show();
        autoQuit();
    }

    private void autoQuit() {
        Handlers.sharedHandler(this.getApplicationContext()).postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.app("auto quit!");

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                finish();

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }, AUTO_QUIT_DELAY_TIME_AFTER_FETCH_LOGIN_INFO_FAILED);
    }

    /**
     * 成功，进入主界面
     */
    private void onEnter(TouristLoginInfo loginInfo) {
        AppCache.setLoginInfo(loginInfo);
        LogUtil.app("on enter success! login info=" + loginInfo);
        MainActivity.start(WelcomeActivity.this);
        finish();
    }
}
