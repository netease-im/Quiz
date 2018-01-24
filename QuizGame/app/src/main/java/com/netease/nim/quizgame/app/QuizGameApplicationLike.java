package com.netease.nim.quizgame.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.netease.nim.hotfix.sdk.AppInfo;
import com.netease.nim.hotfix.sdk.PatchCallback;
import com.netease.nim.hotfix.sdk.ResponseCode;
import com.netease.nim.hotfix.sdk.SDKOptions;
import com.netease.nim.hotfix.sdk.TinkerManager;
import com.netease.nim.quizgame.BuildConfig;
import com.netease.nim.quizgame.app.crash.AppCrashHandler;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.common.thread.Handlers;
import com.netease.nim.quizgame.common.utils.StorageUtil;
import com.netease.nim.quizgame.protocol.Servers;
import com.netease.nim.quizgame.protocol.extension.CustomAttachParser;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

@DefaultLifeCycle(application = "com.netease.nim.quizgame.app.QuizGameApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL)
public class QuizGameApplicationLike extends ApplicationLike {

    public QuizGameApplicationLike(Application application, int tinkerFlags,
                                   boolean tinkerLoadVerifyFlag,
                                   long applicationStartElapsedTime,
                                   long applicationStartMillisTime,
                                   Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag,
                applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);

        MultiDex.install(base); // 使应用支持分包
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TinkerManager.getInstance().installTinker(this, SDKOptions.DEFAULT); // 支持Tinker热修复

        onApplicationOnCreate(getApplication());
    }

    private void onApplicationOnCreate(Context context) {
        NIMClient.init(context, null, getSDKOptions(context));

        if (NIMUtil.isMainProcess(context)) {
            initMainProcess(context);
        }
    }

    private com.netease.nimlib.sdk.SDKOptions getSDKOptions(Context context) {
        com.netease.nimlib.sdk.SDKOptions options = new com.netease.nimlib.sdk.SDKOptions();
        options.appKey = Servers.getAppKey();
        options.reducedIM = true;
        options.asyncInitSDK = true;
        options.improveSDKProcessPriority = false;
        options.sdkStorageRootPath = StorageUtil.getAppCacheDir(context) + "/nim"; // 可以不设置，那么将采用默认路径

        return options;
    }

    private void initMainProcess(Context context) {
        AppCache.setContext(context);

        final String logDir = StorageUtil.getAppCacheDir(context) + "/app/log";
        AppCrashHandler.init(context, logDir);
        LogUtil.init(logDir, Log.DEBUG);

        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

        // 热更新流程
        Handlers.sharedHandler(context).postDelayed(this::fetchPatch, 3000);
    }

    /**
     * ************************* 检测更新 *************************
     */
    private void fetchPatch() {
        LogUtil.hotfix("check update...");

        final AppInfo appInfo = new AppInfo("1a98366e43511364", BuildConfig.VERSION_NAME);
        TinkerManager.getInstance().checkUpdate(appInfo, new PatchCallback() {
            @Override
            public void onSuccess() {
                LogUtil.hotfix("patch has installed success!!!");
            }

            @Override
            public void onFailed(int code, String msg) {
                if (ResponseCode.WITHOUT_ANY_PATCH == code) {
                    LogUtil.hotfix("no need to update! without any patch!");
                } else {
                    LogUtil.hotfix("apply patch failed, code=" + code + ", msg=" + msg);
                }
            }
        });
    }
}
