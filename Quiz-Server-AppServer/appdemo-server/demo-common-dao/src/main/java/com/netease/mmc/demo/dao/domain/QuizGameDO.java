package com.netease.mmc.demo.dao.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * This class corresponds to the database table demo_quiz_game
 */
public class QuizGameDO implements Serializable {
    /**
     * Database Table : demo_quiz_game; 
     * Database Column : id; 
     * Database Column Remarks : 
     *   主键id
     */
    private Long id;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : room_id; 
     * Database Column Remarks : 
     *   房间id
     */
    private Long roomId;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : bonus; 
     * Database Column Remarks : 
     *   奖金金额
     */
    private BigDecimal bonus;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : questions; 
     * Database Column Remarks : 
     *   竞答题目，逗号分隔
     */
    private List<Long> questions;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : player_count; 
     * Database Column Remarks : 
     *   参与人数
     */
    private Long playerCount;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : winner_count; 
     * Database Column Remarks : 
     *   获奖人数
     */
    private Long winnerCount;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : winner_sample; 
     * Database Column Remarks : 
     *   部分获奖名单
     */
    private List<String> winnerSample;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : created_at; 
     * Database Column Remarks : 
     *   记录创建时间
     */
    private Date createdAt;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : updated_at; 
     * Database Column Remarks : 
     *   记录更新时间
     */
    private Date updatedAt;

    /**
     * Database Table : demo_quiz_game; 
     * Database Column : winner; 
     * Database Column Remarks : 
     *   完整获奖名单
     */
    private List<String> winner;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public List<Long> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Long> questions) {
        this.questions = questions;
    }

    public Long getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Long playerCount) {
        this.playerCount = playerCount;
    }

    public Long getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(Long winnerCount) {
        this.winnerCount = winnerCount;
    }

    public List<String> getWinnerSample() {
        return winnerSample;
    }

    public void setWinnerSample(List<String> winnerSample) {
        this.winnerSample = winnerSample;
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

    public List<String> getWinner() {
        return winner;
    }

    public void setWinner(List<String> winner) {
        this.winner = winner;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", roomId=").append(roomId);
        sb.append(", bonus=").append(bonus);
        sb.append(", questions=").append(questions);
        sb.append(", playerCount=").append(playerCount);
        sb.append(", winnerCount=").append(winnerCount);
        sb.append(", winnerSample=").append(winnerSample);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", winner=").append(winner);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}