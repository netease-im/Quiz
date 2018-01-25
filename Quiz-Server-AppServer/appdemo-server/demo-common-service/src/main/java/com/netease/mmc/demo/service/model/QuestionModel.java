package com.netease.mmc.demo.service.model;

import java.util.List;

/**
 * 竞答问题Model.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public class QuestionModel {
    /**
     * 问题id
     */
    private Long questionId;

    /**
     * 问题内容
     */
    private String question;

    /**
     * 问题顺序
     */
    private Integer order;

    /**
     * 正确答案id
     */
    private Integer rightAnswer;

    /**
     * 回答人数
     */
    private Long selectCount;

    private List<OptionModel> options;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(Integer rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public Long getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(Long selectCount) {
        this.selectCount = selectCount;
    }

    public List<OptionModel> getOptions() {
        return options;
    }

    public void setOptions(List<OptionModel> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuestionModel{");
        sb.append("questionId=").append(questionId);
        sb.append(", question='").append(question).append('\'');
        sb.append(", order=").append(order);
        sb.append(", rightAnswer=").append(rightAnswer);
        sb.append(", selectCount=").append(selectCount);
        sb.append(", options=").append(options);
        sb.append('}');
        return sb.toString();
    }
}