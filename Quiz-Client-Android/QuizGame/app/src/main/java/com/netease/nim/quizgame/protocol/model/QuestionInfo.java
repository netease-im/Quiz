package com.netease.nim.quizgame.protocol.model;

import com.netease.nim.quizgame.common.annotation.KeepMemberNames;

import java.util.List;

/**
 * Created by winnie on 15/01/2018.
 */
@KeepMemberNames
public class QuestionInfo {
    long questionId;
    int order;
    String question;
    int rightAnswer;
    List<AnswerOption> options;

    public long getQuestionId() {
        return questionId;
    }

    public int getOrder() {
        return order;
    }

    public String getQuestion() {
        return question;
    }

    public int getRightAnswer() {
        return rightAnswer;
    }

    public List<AnswerOption> getOptions() {
        return options;
    }

    public static QuestionInfo createQuestionInfo(long questionId, int order, String question,
                                                  int rightAnswer, List<AnswerOption> options) {
        QuestionInfo questionInfo = new QuestionInfo();
        questionInfo.questionId = questionId;
        questionInfo.order = order;
        questionInfo.question = question;
        questionInfo.rightAnswer = rightAnswer;
        questionInfo.options = options;
        return questionInfo;
    }
}

