package com.netease.nim.quizgame.protocol.model;

import com.netease.nim.quizgame.common.annotation.KeepMemberNames;

/**
 * Created by winnie on 15/01/2018.
 */
@KeepMemberNames
public class AnswerOption {
    int optionId;
    String content;
    int selectCount;

    public int getOptionId() {
        return optionId;
    }

    public String getContent() {
        return content;
    }

    public int getSelectCount() {
        return selectCount;
    }

    public static AnswerOption createAnswerOption(int optionId, String content, int selectCount) {
        AnswerOption answerOption = new AnswerOption();
        answerOption.optionId = optionId;
        answerOption.content = content;
        answerOption.selectCount = selectCount;
        return answerOption;
    }
}
