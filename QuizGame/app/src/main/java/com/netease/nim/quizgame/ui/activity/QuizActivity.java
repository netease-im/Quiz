package com.netease.nim.quizgame.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.quizgame.R;
import com.netease.nim.quizgame.app.Preferences;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.common.net.NetworkUtil;
import com.netease.nim.quizgame.common.utils.ScreenUtil;
import com.netease.nim.quizgame.protocol.DemoServerController;
import com.netease.nim.quizgame.protocol.QuizObserver;
import com.netease.nim.quizgame.protocol.QuizServerController;
import com.netease.nim.quizgame.protocol.model.AnswerOption;
import com.netease.nim.quizgame.protocol.model.QuestionInfo;
import com.netease.nim.quizgame.protocol.model.QuizNotification;
import com.netease.nim.quizgame.ui.adapter.ResultAdapter;
import com.netease.nim.quizgame.ui.constant.AnswerState;
import com.netease.nim.quizgame.ui.constant.QuizCardState;
import com.netease.nim.quizgame.ui.constant.QuizProcessState;
import com.netease.nim.quizgame.ui.model.AnswerView;
import com.netease.nim.quizgame.ui.model.SuccessfulUserInfo;
import com.netease.nimlib.sdk.NIMChatRoomSDK;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.OnClick;

/**
 * 竞猜答题界面
 * Created by winnie on 12/01/2018.
 */

public class QuizActivity extends QuizBaseActivity {

    private static final int QUIZ_START_COUNT_DOWN_TIME = 10 * 1000;
    private int quizStartCountDownTime = 10 * 1000; // 游戏开始倒计时时间
    private final static int FETCH_ONLINE_PEOPLE_COUNTS_DELTA = 5 * 1000;
    private final static int ANSWER_NO_CHOSEN = -1; // 默认没选择答案

    /// state
    private int currentAnswerState = AnswerState.NORMAL; // 当前答题状态
    private int currentAnswerChosen = ANSWER_NO_CHOSEN; // 当前用户的答案
    private int currentCmd; // 当前存储待显示的指令
    private long currentCmdTime; // 当前待显示的时间戳

    //data
    private Timer timer;
    private List<AnswerOption> answerOptionList; // 答题选项内容列表
    private List<AnswerView> answerViewList; // 答题选项布局列表

    private QuizObserver quizObserver;

    private int correctAnswer; // 正确答案
    private int reviveCount = 1; // 复活次数
    private int winnerCount = 0; // 冲关成功人数
    private float bonusPerson; // 每个人奖金
    private QuestionInfo questionInfo; // 题目
    private int currentQuestionNumber = 0; // 已答题数目

    private List<SuccessfulUserInfo> userInfoList; // 冲关成功用户信息
    private ResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        enterRoom();

        quizObserver.init(roomId, roomInfo.getCreator());
        registerObservers(true);
    }

    private void initData() {
        answerOptionList = new ArrayList<>();
        quizObserver = new QuizObserver();
        reviveText.setText(String.format(getString(R.string.revive_tip), reviveCount));

        quizTip.setFormat("");
        quizTip.setMaxProgress(10 * 1000);
    }

    // 初始化选项布局
    private void initLayout(int size) {
        if (answerViewList != null) {
            return;
        }

        answerViewList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            // 根布局
            RelativeLayout answerLayout = new RelativeLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ScreenUtil.dip2px(56));
            layoutParams.setMargins(ScreenUtil.dip2px(20), ScreenUtil.dip2px(20), ScreenUtil.dip2px(20), 0);
            answerLayout.setBackgroundResource(R.mipmap.ic_answer_normal);
            answerLayout.setLayoutParams(layoutParams);

            // 内容布局
            TextView textViewContent = new TextView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            textViewContent.setLayoutParams(params);
            textViewContent.setTextColor(getResources().getColor(R.color.color_black_333333));
            textViewContent.setTextSize(19);
            answerLayout.addView(textViewContent);

            // 人数布局
            TextView textViewCount = new TextView(this);
            RelativeLayout.LayoutParams paramsCount = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsCount.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            paramsCount.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            paramsCount.rightMargin = ScreenUtil.dip2px(18);
            textViewCount.setLayoutParams(paramsCount);
            textViewCount.setVisibility(View.GONE);
            textViewCount.setTextColor(getResources().getColor(R.color.color_gray_c6cbd4));
            textViewCount.setTextSize(15);
            answerLayout.addView(textViewCount);
            answerLayout.setId(i);

            quizQuestionLayout.addView(answerLayout);
            answerViewList.add(new AnswerView(answerLayout, textViewContent, textViewCount));

            // 答题点击事件
            answerLayout.setOnClickListener(v -> doAnswer(v.getId()));
        }
    }

    private void enterRoom() {
        EnterChatRoomData enterChatRoomData = new EnterChatRoomData(roomId);
        enterChatRoomData.setIndependentMode((roomId, account) -> roomInfo.getAddr(), Preferences.getAccount(), Preferences.getToken());
        NIMChatRoomSDK.getChatRoomService().enterChatRoomEx(
                enterChatRoomData, 1).setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object param) {
                LogUtil.app("enter chat room success, roomId=" + roomId);
                updateRoomUI();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.app("enter chat room failed, code=" + code);
                Toast.makeText(QuizActivity.this, "进入聊天室失败, code=" + code, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.app("enter chat room error, e=" + exception.getMessage());
                Toast.makeText(QuizActivity.this, "进入聊天室出错, error=" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateRoomUI() {
        roomIdText.setText(String.format(getString(R.string.roomid_content), roomInfo.getRoomId()));
        onlineCountText.setText(String.format(getString(R.string.onlinecount_content), roomInfo.getOnlineUserCount()));
        bonusText.setText(String.format(getString(R.string.bonus_content), roomInfo.getBonus()));
        fetchOnlineCount();
    }

    private void registerObservers(boolean register) {
        NIMChatRoomSDK.getChatRoomServiceObserve().observeOnlineStatus(onlineStatusObserver, register);
        NIMChatRoomSDK.getChatRoomServiceObserve().observeKickOutEvent(kickOutEventObserver, register);
        quizObserver.observeQuiz(quizNotificationObserver, register);
    }

    private Observer<QuizNotification> quizNotificationObserver = (Observer<QuizNotification>) quizNotification -> {
        currentCmd = quizNotification.getCmd();
        switch (currentCmd) {
            case QuizProcessState.SET_QUESTION:
                saveQuestion(quizNotification);
                break;
            case QuizProcessState.ANNOUNCE_ANSWER:
                saveAnswer(quizNotification);
                break;
            case QuizProcessState.RESULT:
                saveResult(quizNotification);
                break;
        }
        LogUtil.app("receive quiz notification, cmd:" + quizNotification.getCmd());
    };

    private Observer<ChatRoomKickOutEvent> kickOutEventObserver = (Observer<ChatRoomKickOutEvent>) chatRoomKickOutEvent -> {
        if (!chatRoomKickOutEvent.getRoomId().equals(roomId)) {
            return;
        }
        Toast.makeText(QuizActivity.this, "直播已结束 ", Toast.LENGTH_SHORT).show();
        finish();
    };

    private Observer<ChatRoomStatusChangeData> onlineStatusObserver = (data) -> {
        LogUtil.app("online status:" + data.status);
        if (data.status == StatusCode.LOGINED) {
            networkText.setVisibility(View.GONE);
        } else {
            networkText.setVisibility(View.VISIBLE);
        }
    };

    /**
     * ********************************* 收到答题过程数据 ******************************
     */

    // 收到出题消息，把题目存下来
    private void saveQuestion(QuizNotification quizNotification) {
        if (quizNotification == null || quizNotification.getQuestionInfo() == null
                || quizNotification.getQuestionInfo().getOptions() == null) {
            return;
        }
        questionInfo = quizNotification.getQuestionInfo();
        currentCmdTime = quizNotification.getTime();

        saveQuestionContent(questionInfo);
    }

    private void saveQuestionContent(QuestionInfo questionInfo) {
        initLayout(questionInfo.getOptions().size());

        answerOptionList.clear();
        answerOptionList.addAll(questionInfo.getOptions());

        quizNumber.setText((questionInfo.getOrder() + 1) + "/" + questionInfo.getOptions().size());
        quizTitle.setText(questionInfo.getQuestion());
        for (int i = 0; i < answerOptionList.size(); i++) {
            AnswerView answerView = answerViewList.get(i);
            answerView.getAnswerContentText().setText(answerOptionList.get(i).getContent());
        }
    }

    // 收到答案，存储在本地，等推流时间戳再显示
    private void saveAnswer(QuizNotification quizNotification) {
        if (quizNotification == null || quizNotification.getQuestionInfo() == null
                || quizNotification.getQuestionInfo().getOptions() == null) {
            return;
        }

        questionInfo = quizNotification.getQuestionInfo();
        currentCmdTime = quizNotification.getTime();

        // 存储题目，以防中间来的
        saveQuestionContent(questionInfo);

        // 正确答案
        correctAnswer = quizNotification.getQuestionInfo().getRightAnswer();

        // 每个选项卡人数
        for (int i = 0; i < answerOptionList.size(); i++) {
            int selectCount = quizNotification.getQuestionInfo().getOptions().get(i).getSelectCount();
            answerViewList.get(i).getAnswerNumberText().setText(selectCount + "人");
        }
    }

    // 收到冲关结果，存储在本地，等待推流时间戳时显示
    private void saveResult(QuizNotification quizNotification) {
        if (quizNotification == null) {
            return;
        }

        currentCmdTime = quizNotification.getTime();
        winnerCount = quizNotification.getWinnerCount();
        bonusPerson = quizNotification.getBonus();

        if (quizNotification.getWinnerSample() != null) {
            // 冲关成功列表展示
            userInfoList = new ArrayList<>();

            float bonus = quizNotification.getBonus();
            for (int i = 0; i < quizNotification.getWinnerSample().size(); i++) {
                userInfoList.add(new SuccessfulUserInfo(quizNotification.getWinnerSample().get(i), bonus));
            }
            adapter = new ResultAdapter(userInfoList);
            resultList.setLayoutManager(new LinearLayoutManager(this));
            resultList.setAdapter(adapter);
        }
    }

    /**
     * ********************************* 答题过程界面展现 ******************************
     */

    private void showQuestion() {
        LogUtil.app("show question, order:" + questionInfo.getOrder());
        // 是否中间加入的，迟到不能作答
        if (currentQuestionNumber == 0
                && questionInfo.getOrder() > currentQuestionNumber
                && currentAnswerState == AnswerState.NORMAL) {
            Toast.makeText(this, "迟到啦~ 不能作答", Toast.LENGTH_LONG).show();
            currentAnswerState = AnswerState.FAILED;
        }

        // 没收到上一题的答案，不会刷新复活的界面，所以收到题目，就更新一下复活状态
        updateReviveCountUI();

        // 没有迟到，第一题答了，然后中间断网，收到第三题，判断是否使用复活
        if (currentQuestionNumber > 0
                && currentQuestionNumber != questionInfo.getOrder()
                &&  (questionInfo.getOrder() - currentQuestionNumber) == reviveCount) {
            setReviveOrFailed();
            updateReviveState();
        }

        // 开始倒计时
        // 显示答题卡
        quizStartTimer.start();
        if (currentAnswerState != AnswerState.FAILED) {
            updateQuizCard(QuizCardState.SHOW_CLICKABLE);
        } else {
            updateQuizCard(QuizCardState.SHOW_NON_CLICKABLE);
        }
    }

    private void updateReviveCountUI() {
        reviveText.setText(String.format(getResources().getString(R.string.revive_tip), reviveCount));
    }

    private void updateReviveState() {
        if (currentAnswerState == AnswerState.REVIVE) {
            // 如果可以复活，就算他漏题已经答过了
            Toast.makeText(this, "漏答题了，帮你使用一张复活卡", Toast.LENGTH_SHORT).show();
            LogUtil.app("auto revive");
            currentQuestionNumber++;
            updateReviveCountUI();
        }
    }

    // 答题卡界面
    private void updateQuizCard(int quizCardState) {
        quizQuestionLayout.setVisibility(quizCardState == QuizCardState.HIDE
                || quizCardState == QuizCardState.WAITING ? View.GONE : View.VISIBLE);

        quizTip.setVisibility(quizCardState == QuizCardState.SHOW_ANSWER ? View.GONE : View.VISIBLE);

        // 不可点击
        for (int i = 0; i < answerViewList.size(); i++) {
            answerViewList.get(i).getAnswerLayout().setEnabled(quizCardState == QuizCardState.SHOW_CLICKABLE);
        }

        if (quizCardState == QuizCardState.SHOW_ANSWER) {
            showAnswer();

            // 公布完答案，选中的可以还原
            currentAnswerChosen = ANSWER_NO_CHOSEN;

        } else if (quizCardState == QuizCardState.HIDE) {
            // 隐藏答题卡，恢复初始状态
            for (int i = 0; i < answerViewList.size(); i++) {
                answerViewList.get(i).getAnswerContentText().setTextColor(Color.BLACK);
                answerViewList.get(i).getAnswerLayout().setBackgroundResource(R.mipmap.ic_answer_normal);
                answerViewList.get(i).getAnswerNumberText().setVisibility(View.GONE);
                answerViewList.get(i).getAnswerNumberText().setTextColor(getResources().getColor(R.color.color_gray_c6cbd4));

            }
        } else if (quizCardState == QuizCardState.SHOW_NON_CLICKABLE) {
            // 不可点击
            for (int i = 0; i < answerViewList.size(); i++) {
                answerViewList.get(i).getAnswerContentText().setTextColor(Color.GRAY);
                answerViewList.get(i).getAnswerLayout().setBackgroundResource(R.mipmap.ic_answer_normal);

            }
        }
    }

    // 显示答案
    private void showAnswer() {
        LogUtil.app("show answer, order:" + questionInfo.getOrder());
        // 显示每个选项人数
        for (int i = 0; i < answerViewList.size(); i++) {
            answerViewList.get(i).getAnswerNumberText().setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < answerViewList.size(); i++) {
            if (correctAnswer == answerOptionList.get(i).getOptionId()) {
                // 正确答案标绿
                answerViewList.get(i).getAnswerLayout().setBackgroundResource(R.mipmap.ic_answer_correct);
                answerViewList.get(i).getAnswerContentText().setTextColor(Color.WHITE);
                answerViewList.get(i).getAnswerNumberText().setTextColor(Color.WHITE);
            }
        }

        if (currentQuestionNumber != (questionInfo.getOrder() + 1)) {
            // 答非所问
            currentAnswerChosen = ANSWER_NO_CHOSEN;
        }

        if (currentAnswerChosen != ANSWER_NO_CHOSEN && answerOptionList.get(currentAnswerChosen).getOptionId() != correctAnswer) {
            // 用户选择了答案，但是答案错误，答案标红
            answerViewList.get(currentAnswerChosen).getAnswerLayout().setBackgroundResource(R.mipmap.ic_answer_wrong);
            answerViewList.get(currentAnswerChosen).getAnswerContentText().setTextColor(Color.WHITE);
            answerViewList.get(currentAnswerChosen).getAnswerNumberText().setTextColor(Color.WHITE);
        }

        // 用户还没死，并且答错了，判断复活状态。只在复活那一题提醒
        if (currentAnswerState == AnswerState.REVIVE
                && questionInfo != null && questionInfo.getOrder() == (currentQuestionNumber - 1)) {
            reviveText.setText(String.format(getString(R.string.revive_tip), reviveCount));
            LogUtil.app("show answer and revive");
            getHandler().postDelayed(() -> Toast.makeText(QuizActivity.this,
                    "已使用1次复活机会", Toast.LENGTH_LONG).show(), 1000);
        }

        // 用户无法复活，冲关失败。只在失败的那一题提醒就好啦
        if (questionInfo != null && questionInfo.getOrder() == (currentQuestionNumber - 1)
                && currentAnswerState == AnswerState.FAILED) {
            Toast.makeText(this, "已出局", Toast.LENGTH_SHORT).show();
        }
    }

    // 答案选中界面
    private void updateAnswerUI(int answerChosen) {
        LogUtil.app("current answer chosen:" + currentAnswerChosen);

        for (int i = 0; i < answerViewList.size(); i++) {
            answerViewList.get(i).getAnswerLayout().setEnabled(false);
            answerViewList.get(i).getAnswerContentText().setTextColor(answerChosen
                    == (i) ? Color.WHITE : Color.BLACK);
            answerViewList.get(i).getAnswerLayout().setBackgroundResource(answerChosen
                    == (i) ? R.mipmap.ic_answer_chosed : R.mipmap.ic_answer_normal);
        }
    }

    // 显示冲关结果
    private void showQuizResult() {
        resultLayout.setVisibility(View.VISIBLE);
        // 没收到上一题的答案，不会刷新复活的界面，所以收到结果，就更新一下复活状态
        updateReviveCountUI();

        if (winnerCount == 0) {
            // 没人答对
            resultFailedLayout.setVisibility(View.VISIBLE);
            resultSuccessLayout.setVisibility(View.GONE);
        } else {
            resultSuccessLayout.setVisibility(View.VISIBLE);
            resultFailedLayout.setVisibility(View.GONE);

            // 只能使用一张复活卡，最后一题漏答了，判断是否使用复活卡
            if (currentQuestionNumber != totalQuestionNumber) {
                setReviveOrFailed();
                updateReviveState();
            }

            if (currentQuestionNumber > 0 // 答过题
                    && (currentQuestionNumber + reviveCount) >= totalQuestionNumber // 答过的题数 + 复活卡张数 >= 总体数
                     && currentAnswerState != AnswerState.FAILED) {
                // 冲关成功
                resultTipText.setText(String.format(getResources().getString(R.string.result_success), bonusPerson));
                resultTipText.setBackgroundResource(R.mipmap.ic_result_success);
            } else {
                // 冲关失败
                resultTipText.setText(R.string.result_failed);
                resultTipText.setBackgroundResource(R.mipmap.ic_result_failed);
            }

            adapter.notifyDataSetChanged();
        }
    }

    private CountDownTimer quizStartTimer = new CountDownTimer(QUIZ_START_COUNT_DOWN_TIME + 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            quizStartCountDownTime -= 1000;
            if (quizStartCountDownTime < 0) {
                // 每个手机表现还不一样
                return;
            }
            quizTip.setProgress(quizStartCountDownTime);
            quizTip.setContent(String.valueOf(quizStartCountDownTime / 1000));

            if (quizStartCountDownTime == 0 && currentAnswerState != AnswerState.FAILED
                    && currentAnswerChosen == ANSWER_NO_CHOSEN) {

                for (int i = 0; i < answerViewList.size(); i++) {
                    answerViewList.get(i).getAnswerLayout().setEnabled(false);
                }

                // 时间到未作答，按照复活到死亡流程
                currentQuestionNumber++;
                setReviveOrFailed();
                Toast.makeText(QuizActivity.this, "时间到，未作答", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFinish() {
            quizStartCountDownTime = QUIZ_START_COUNT_DOWN_TIME;
            updateQuizCard(QuizCardState.HIDE);
        }
    };

    /**
     * ************************** 服务器交互操作 ************************
     */

    private void doAnswer(int answerChosen) {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(this, "检查网络", Toast.LENGTH_SHORT).show();
        }

        currentAnswerChosen = answerChosen;
        currentQuestionNumber++;
        updateAnswerUI(answerChosen);
        QuizServerController.getInstance().answer(roomId, questionInfo.getQuestionId(),
                questionInfo.getOptions().get(answerChosen).getOptionId(), new DemoServerController.IHttpCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        if (integer == 0) {
                            // 答错啦
                            LogUtil.app("you are wrong! answer result code:" + integer);
                            setReviveOrFailed();
                        } else if (integer == 2) {
                            // 无效
                            LogUtil.app("invalid! answer result code:" + integer);
                            Toast.makeText(QuizActivity.this, "无效答案，sorry", Toast.LENGTH_LONG).show();
                            setReviveOrFailed();
                        } else if (integer == 1) {
                            // 答对了
                            LogUtil.app("congratulations! answer result code:" + integer);
                            currentAnswerState = AnswerState.NORMAL;
                        } else {
                            LogUtil.app("answer result code:" + integer);
                        }
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        LogUtil.app("answer failed, code:" + code);
                        Toast.makeText(QuizActivity.this, "answer failed, code:" + code, Toast.LENGTH_SHORT).show();
                        setReviveOrFailed();
                    }
                });
    }

    private void setReviveOrFailed() {
        if (reviveCount > 0) {
            reviveCount--;
            currentAnswerState = AnswerState.REVIVE;
        } else {
            currentAnswerState = AnswerState.FAILED;
        }
    }

    @OnClick({R.id.input_btn, R.id.root_view, R.id.close_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.input_btn:
                switchInputLayout(true);
                break;
            case R.id.root_view:
                switchInputLayout(false);
                break;
            case R.id.close_btn:
                finish();
                break;
        }
    }

    @Override
    void onPlayerRealTime(long time) {

        if (currentCmdTime != 0 && time > currentCmdTime) {
            switch (currentCmd) {
                case QuizProcessState.SET_QUESTION:
                    showQuestion();
                    break;
                case QuizProcessState.ANNOUNCE_ANSWER:
                    updateQuizCard(QuizCardState.SHOW_ANSWER);
                    // 6s 后消失
                    getHandler().postDelayed(() -> updateQuizCard(QuizCardState.HIDE), 6 * 1000);
                    break;
                case QuizProcessState.RESULT:
                    showQuizResult();
                    break;
            }
            LogUtil.app("onPlayerRealTime!cmd:" + currentCmd + ", time:" + time + ", current cmd time:" + currentCmdTime);
            currentCmdTime = 0;
        }
    }

    /**
     * ********************************* 查询聊天室人数 ******************************
     */

    // 30s轮询一次在线人数
    private void fetchOnlineCount() {
        if (timer == null) {
            timer = new Timer();
        }

        //开始一个定时任务
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomId).setCallback(new RequestCallback<ChatRoomInfo>() {
                    @Override
                    public void onSuccess(final ChatRoomInfo param) {
                        onlineCountText.setText(String.format(getResources().getString(R.string.onlinecount_content), param.getOnlineUserCount()));
                    }

                    @Override
                    public void onFailed(int code) {
                        LogUtil.app("fetch room info failed:" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        LogUtil.app("fetch room info exception:" + exception);
                    }
                });
            }
        }, FETCH_ONLINE_PEOPLE_COUNTS_DELTA, FETCH_ONLINE_PEOPLE_COUNTS_DELTA);
    }

    /**
     * ********************************* 退出流程 ******************************
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        registerObservers(false);
        quizObserver.destroy();

        // 离开聊天室
        LogUtil.app("exit chat room");
        NIMChatRoomSDK.getChatRoomService().exitChatRoom(roomId);

        // 取消倒计时
        quizStartTimer.cancel();

        // 清空delay task
        getHandler().removeCallbacksAndMessages(null);
    }
}
