package com.netease.nim.quizgame.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.nim.liveplayer.sdk.INIMLivePlayer;
import com.netease.nim.liveplayer.sdk.NIMLivePlayer;
import com.netease.nim.liveplayer.sdk.NIMLiveSurfaceView;
import com.netease.nim.liveplayer.sdk.extension.SimplePlayerObserver;
import com.netease.nim.liveplayer.sdk.model.StateInfo;
import com.netease.nim.liveplayer.sdk.model.VideoBufferStrategy;
import com.netease.nim.liveplayer.sdk.model.VideoOptions;
import com.netease.nim.liveplayer.sdk.model.VideoScaleMode;
import com.netease.nim.quizgame.R;
import com.netease.nim.quizgame.common.net.NetworkUtil;
import com.netease.nim.quizgame.common.ui.UI;
import com.netease.nim.quizgame.common.utils.ScreenUtil;
import com.netease.nim.quizgame.common.widget.CircleProgressView;
import com.netease.nim.quizgame.protocol.model.RoomInfo;
import com.netease.nim.quizgame.ui.module.ChatRoomInputPanel;
import com.netease.nim.quizgame.ui.module.ChatRoomMsgListPanel;
import com.netease.nim.quizgame.ui.module.Container;
import com.netease.nim.quizgame.ui.module.DefaultModuleProxy;
import com.netease.nimlib.sdk.NIMChatRoomSDK;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 直播答题界面基类，主要实现聊天列表
 * Created by winnie on 11/01/2018.
 */

public abstract class QuizBaseActivity extends UI {
    private static final String EXTRA_ROOM_INFO = "ROOM_INFO";

    /// abstract
    abstract void onPlayerRealTime(long time);

    /// data
    protected RoomInfo roomInfo;
    protected String roomId;
    protected int totalQuestionNumber; // 总题数
    protected boolean isInputShow = false;

    /// view
    protected ChatRoomInputPanel inputPanel;
    protected ChatRoomMsgListPanel msgListPanel;

    @BindView(R.id.live_surface)
    protected NIMLiveSurfaceView surfaceView;
    @BindView(R.id.root_view)
    protected RelativeLayout rootView;
    @BindView(R.id.input_btn)
    protected ImageButton inputBtn;
    @BindView(R.id.messageActivityBottomLayout)
    protected LinearLayout messageActivityBottomLayout;
    @BindView(R.id.quiz_tip)
    protected CircleProgressView quizTip;
    @BindView(R.id.quiz_number)
    protected TextView quizNumber;
    @BindView(R.id.quiz_title)
    protected TextView quizTitle;
    @BindView(R.id.quiz_question_layout)
    protected LinearLayout quizQuestionLayout;
    @BindView(R.id.revive_text)
    protected TextView reviveText;
    @BindView(R.id.result_tip_text)
    protected TextView resultTipText;
    @BindView(R.id.result_list)
    protected RecyclerView resultList;
    @BindView(R.id.result_layout)
    protected RelativeLayout resultLayout;
    @BindView(R.id.result_success_layout)
    protected RelativeLayout resultSuccessLayout;
    @BindView(R.id.result_failed_layout)
    protected LinearLayout resultFailedLayout;
    @BindView(R.id.room_id_text)
    protected TextView roomIdText;
    @BindView(R.id.online_count_text)
    protected TextView onlineCountText;
    @BindView(R.id.bonus_text)
    protected TextView bonusText;
    @BindView(R.id.waiting_for_live_tip)
    protected TextView waitingForLiveTip;
    @BindView(R.id.network_text)
    protected TextView networkText;

    /// 播放器
    private NIMLivePlayer player;

    public static void startActivity(Context context, RoomInfo roomInfo) {
        Intent intent = new Intent();
        intent.setClass(context, QuizActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_layout);
        ButterKnife.bind(this);

        parseIntent();
        findViews();
        registerObserves(true);

        // 启动播放器
        initPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onActivityResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.onActivityStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 注销监听
        registerObserves(false);

        // 停止拉流
        destroyPlayer();
    }

    private void parseIntent() {
        roomInfo = (RoomInfo) getIntent().getSerializableExtra(EXTRA_ROOM_INFO);
        roomId = roomInfo.getRoomId();
        totalQuestionNumber = roomInfo.getQuestionCount();
    }

    private void findViews() {
        Container container = new Container(this, roomId, SessionTypeEnum.ChatRoom, moduleProxy);
        if (inputPanel == null) {
            inputPanel = new ChatRoomInputPanel(container, rootView);
        } else {
            inputPanel.reload(container);
        }

        if (msgListPanel == null) {
            msgListPanel = new ChatRoomMsgListPanel(container, rootView);
        }
    }

    private void registerObserves(boolean register) {
        NIMChatRoomSDK.getChatRoomServiceObserve().observeReceiveMessage(incomingChatRoomMsg, register);
    }

    protected DefaultModuleProxy moduleProxy = new DefaultModuleProxy() {
        @Override
        public boolean sendMessage(IMMessage msg) {
            ChatRoomMessage message = (ChatRoomMessage) msg;

            NIMClient.getService(ChatRoomService.class).sendMessage(message, false)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                        }

                        @Override
                        public void onFailed(int code) {
                            if (code == ResponseCode.RES_CHATROOM_MUTED) {
                                Toast.makeText(QuizBaseActivity.this, "用户被禁言", Toast.LENGTH_SHORT).show();
                            } else if (code == ResponseCode.RES_CHATROOM_ROOM_MUTED) {
                                Toast.makeText(QuizBaseActivity.this, "全体禁言", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(QuizBaseActivity.this, "消息发送失败：code:" + code, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onException(Throwable exception) {
                            Toast.makeText(QuizBaseActivity.this, "消息发送失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
            msgListPanel.onMsgSend(message);
            return true;
        }

        @Override
        public void shouldCollapseInputPanel() {
            switchInputLayout(false);
        }
    };

    private Observer<List<ChatRoomMessage>> incomingChatRoomMsg = (messages) -> {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        for (ChatRoomMessage msg : messages) {
            // 只有text类型可以显示
            if (msg.getMsgType() == MsgTypeEnum.text) {
                msgListPanel.onIncomingMessage(msg);
            }
        }
    };

    // 切换输入法
    protected void switchInputLayout(boolean isShow) {
        isInputShow = isShow;
        inputPanel.messageActivityBottomLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        inputPanel.switchToTextLayout(isShow);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.dip2px(100));
        if (isShow) {
            // 显示输入法
            params.addRule(RelativeLayout.ABOVE, R.id.messageActivityBottomLayout);
            params.setMargins(ScreenUtil.dip2px(15), 0, 0, ScreenUtil.dip2px(10));
            msgListPanel.messageListView.setLayoutParams(params);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.setMargins(ScreenUtil.dip2px(15), 0, 0, ScreenUtil.dip2px(70));
            msgListPanel.messageListView.setLayoutParams(params);
        }
    }


    /**
     * ********************************* 直播拉流 ******************************
     */

    private void initPlayer() {
        VideoOptions options = new VideoOptions();
        options.scaleMode = VideoScaleMode.FULL;
        options.bufferStrategy = VideoBufferStrategy.LOW_LATENCY;
        options.keepScreenWhilePlaying = true;
        options.playbackTimeout = 5;

        player = new NIMLivePlayer(this, roomInfo.getRtmpPullUrl(), options);
        player.registerPlayerCurrentSyncTimestampListener(500, realTimeListener, true);
        player.registerPlayerObserver(playerStateObserver, true);
        player.setupRenderView(surfaceView);
        player.asyncInit();
    }

    private NELivePlayer.OnCurrentSyncTimestampListener realTimeListener = (l) -> {
        Log.i("huangjun", "current time=" + l);
        onPlayerRealTime(l);
    };

    private SimplePlayerObserver playerStateObserver = new SimplePlayerObserver() {
        @Override
        public void onStateChanged(StateInfo stateInfo) {
            Log.i("huangjun", "player state changed to=" + stateInfo.getState() + ", cause=" + stateInfo.getCauseCode());

            if (NetworkUtil.isNetAvailable(QuizBaseActivity.this)
                    && (stateInfo.getState() == INIMLivePlayer.STATE.ERROR
                    || stateInfo.getState() == INIMLivePlayer.STATE.STOPPED)) {
                player.asyncInit(); // 直播过程中出现了任何错误都立即重启
            }

            if (stateInfo.getState() == INIMLivePlayer.STATE.PLAYING) {
                waitingForLiveTip.setVisibility(View.GONE);
            }
        }
    };

    private void destroyPlayer() {
        if (player != null) {
            player.registerPlayerCurrentSyncTimestampListener(0, null, false);
            player.registerPlayerObserver(playerStateObserver, false);
            player.destroy();
        }
    }
}
