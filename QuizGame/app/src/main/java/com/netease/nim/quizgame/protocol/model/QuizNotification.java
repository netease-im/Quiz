package com.netease.nim.quizgame.protocol.model;

import com.netease.nim.quizgame.common.annotation.KeepMemberNames;

import java.util.List;

/**
 * Created by winnie on 14/01/2018.
 */

@KeepMemberNames
public class QuizNotification {
    int cmd = -1;
    QuestionInfo questionInfo;
    long time;
    int winnerCount;
    float bonus;
    List<String> winnerSample;

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getCmd() {
        return cmd;
    }

    public QuestionInfo getQuestionInfo() {
        return questionInfo;
    }

    public long getTime() {
        return time;
    }

    public int getWinnerCount() {
        return winnerCount;
    }

    public float getBonus() {
        return bonus;
    }

    public List<String> getWinnerSample() {
        return winnerSample;
    }

    public static QuizNotification createQuizNotification(int cmd) {
        QuizNotification quizNotification = new QuizNotification();
        quizNotification.cmd = cmd;
        return quizNotification;
    }
}
