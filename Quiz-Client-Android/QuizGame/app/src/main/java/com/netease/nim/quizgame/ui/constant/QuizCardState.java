package com.netease.nim.quizgame.ui.constant;

/**
 * 答题卡界面状态
 * Created by winnie on 11/01/2018.
 */

public class QuizCardState {
    /**
     * 等待答题
     */
    public static int WAITING = 0;
    /**
     * 显示答题卡且可点击
     */
    public static int SHOW_CLICKABLE = 1;
    /**
     * 显示答题卡且不可点击
     */
    public static int SHOW_NON_CLICKABLE = 2;
    /**
     * 隐藏答题卡
     */
    public static int HIDE = 3;
    /**
     * 显示答题结果
     */
    public static int SHOW_ANSWER = 4;
    /**
     * 显示最终结果
     */
    public static int SHOW_RESULT = 5;
}
