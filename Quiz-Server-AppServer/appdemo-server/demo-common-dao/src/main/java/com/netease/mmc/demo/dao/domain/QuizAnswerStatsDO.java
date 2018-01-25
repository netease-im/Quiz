package com.netease.mmc.demo.dao.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * This class corresponds to the database table demo_quiz_answer_stats
 */
public class QuizAnswerStatsDO implements Serializable {
    /**
     * Database Table : demo_quiz_answer_stats; 
     * Database Column : id; 
     * Database Column Remarks : 
     *   主键id
     */
    private Long id;

    /**
     * Database Table : demo_quiz_answer_stats; 
     * Database Column : game_id; 
     * Database Column Remarks : 
     *   游戏id
     */
    private Long gameId;

    /**
     * Database Table : demo_quiz_answer_stats; 
     * Database Column : question_id; 
     * Database Column Remarks : 
     *   题目id
     */
    private Long questionId;

    /**
     * Database Table : demo_quiz_answer_stats; 
     * Database Column : answer_id; 
     * Database Column Remarks : 
     *   选项id
     */
    private Integer answerId;

    /**
     * Database Table : demo_quiz_answer_stats; 
     * Database Column : select_count; 
     * Database Column Remarks : 
     *   选择人数
     */
    private Long selectCount;

    /**
     * Database Table : demo_quiz_answer_stats; 
     * Database Column : created_at; 
     * Database Column Remarks : 
     *   记录创建时间
     */
    private Date createdAt;

    /**
     * Database Table : demo_quiz_answer_stats; 
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

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Long getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(Long selectCount) {
        this.selectCount = selectCount;
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
        sb.append(", gameId=").append(gameId);
        sb.append(", questionId=").append(questionId);
        sb.append(", answerId=").append(answerId);
        sb.append(", selectCount=").append(selectCount);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}