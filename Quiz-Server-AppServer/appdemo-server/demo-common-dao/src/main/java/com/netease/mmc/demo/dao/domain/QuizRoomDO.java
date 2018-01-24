package com.netease.mmc.demo.dao.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * This class corresponds to the database table demo_quiz_room
 */
public class QuizRoomDO implements Serializable {
    /**
     * Database Table : demo_quiz_room; 
     * Database Column : id; 
     * Database Column Remarks : 
     *   主键ID
     */
    private Long id;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : room_id; 
     * Database Column Remarks : 
     *   聊天室房间号
     */
    private Long roomId;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : creator; 
     * Database Column Remarks : 
     *   房主账号
     */
    private String creator;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : creator_token; 
     * Database Column Remarks : 
     *   房主IM token
     */
    private String creatorToken;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : name; 
     * Database Column Remarks : 
     *   房间名称
     */
    private String name;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : device_id; 
     * Database Column Remarks : 
     *   主播设备号
     */
    private String deviceId;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : game_id; 
     * Database Column Remarks : 
     *   游戏id
     */
    private Long gameId;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : game_status; 
     * Database Column Remarks : 
     *   游戏状态，0-未开始，1-已开始，2-已结束
     */
    private Integer gameStatus;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : cid; 
     * Database Column Remarks : 
     *   直播频道ID
     */
    private String cid;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : push_url; 
     * Database Column Remarks : 
     *   推流地址
     */
    private String pushUrl;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : rtmp_pull_url; 
     * Database Column Remarks : 
     *   rtmp拉流地址
     */
    private String rtmpPullUrl;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : hls_pull_url; 
     * Database Column Remarks : 
     *   hls拉流地址
     */
    private String hlsPullUrl;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : http_pull_url; 
     * Database Column Remarks : 
     *   http拉流地址
     */
    private String httpPullUrl;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : created_at; 
     * Database Column Remarks : 
     *   创建时间
     */
    private Date createdAt;

    /**
     * Database Table : demo_quiz_room; 
     * Database Column : updated_at; 
     * Database Column Remarks : 
     *   更新时间
     */
    private Date updatedAt;

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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorToken() {
        return creatorToken;
    }

    public void setCreatorToken(String creatorToken) {
        this.creatorToken = creatorToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Integer getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(Integer gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public String getRtmpPullUrl() {
        return rtmpPullUrl;
    }

    public void setRtmpPullUrl(String rtmpPullUrl) {
        this.rtmpPullUrl = rtmpPullUrl;
    }

    public String getHlsPullUrl() {
        return hlsPullUrl;
    }

    public void setHlsPullUrl(String hlsPullUrl) {
        this.hlsPullUrl = hlsPullUrl;
    }

    public String getHttpPullUrl() {
        return httpPullUrl;
    }

    public void setHttpPullUrl(String httpPullUrl) {
        this.httpPullUrl = httpPullUrl;
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
        sb.append(", roomId=").append(roomId);
        sb.append(", creator=").append(creator);
        sb.append(", creatorToken=").append(creatorToken);
        sb.append(", name=").append(name);
        sb.append(", deviceId=").append(deviceId);
        sb.append(", gameId=").append(gameId);
        sb.append(", gameStatus=").append(gameStatus);
        sb.append(", cid=").append(cid);
        sb.append(", pushUrl=").append(pushUrl);
        sb.append(", rtmpPullUrl=").append(rtmpPullUrl);
        sb.append(", hlsPullUrl=").append(hlsPullUrl);
        sb.append(", httpPullUrl=").append(httpPullUrl);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}