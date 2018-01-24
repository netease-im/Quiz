package com.netease.nim.quizgame.ui.model;

import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by winnie on 17/01/2018.
 */

public class AnswerView {
    private ViewGroup answerLayout;
    private TextView answerContentText;
    private TextView answerNumberText;

    public AnswerView(ViewGroup answerLayout, TextView answerContentText, TextView answerNumberText) {
        this.answerLayout = answerLayout;
        this.answerContentText = answerContentText;
        this.answerNumberText = answerNumberText;
    }

    public ViewGroup getAnswerLayout() {
        return answerLayout;
    }

    public TextView getAnswerContentText() {
        return answerContentText;
    }

    public TextView getAnswerNumberText() {
        return answerNumberText;
    }
}
