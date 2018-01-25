package com.netease.mmc.demo.dao.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * This class corresponds to the database table demo_quiz_question
 */
public class QuizQuestionDO implements Serializable {
    /**
     * Database Table : demo_quiz_question; 
     * Database Column : id; 
     * Database Column Remarks : 
     *   题目id
     */
    private Long id;

    /**
     * Database Table : demo_quiz_question; 
     * Database Column : question; 
     * Database Column Remarks : 
     *   题目内容
     */
    private String question;

    /**
     * Database Table : demo_quiz_question; 
     * Database Column : options; 
     * Database Column Remarks : 
     *   json格式的选项
     */
    private List<QuizOptionDO> options;

    /**
     * Database Table : demo_quiz_question; 
     * Database Column : right_answer; 
     * Database Column Remarks : 
     *   正确答案id
     */
    private Integer rightAnswer;

    /**
     * Database Table : demo_quiz_question; 
     * Database Column : created_at; 
     * Database Column Remarks : 
     *   记录创建时间
     */
    private Date createdAt;

    /**
     * Database Table : demo_quiz_question; 
     * Database Column : updated_at; 
     * Database Column Remarks : 
     *   记录更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<QuizOptionDO> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOptionDO> options) {
        this.options = options;
    }

    public Integer getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(Integer rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", question=").append(question);
        sb.append(", options=").append(options);
        sb.append(", rightAnswer=").append(rightAnswer);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}