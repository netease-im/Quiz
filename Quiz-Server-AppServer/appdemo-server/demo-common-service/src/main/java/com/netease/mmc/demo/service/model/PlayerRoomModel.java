package com.netease.mmc.demo.service.model;

import java.util.List;

/**
 * 游客端竞答房间Model.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public class PlayerRoomModel extends RoomModel{

    /**
     * 聊天室状态
     */
    private Boolean roomStatus;

    /**
     * 直播推流状态
     */
    private Integer liveStatus;

    /**
     * 在线人数
     */
    private Long onlineUserCount;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 聊天室link地址
     */
    private List<String> addr;

    public Boolean getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(Boolean roomStatus) {
        this.roomStatus = roomStatus;
    }

    public Integer getLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(Integer liveStatus) {
        this.liveStatus = liveStatus;
    }

    public Long getOnlineUserCount() {
        return onlineUserCount;
    }

    public void setOnlineUserCount(Long onlineUserCount) {
        this.onlineUserCount = onlineUserCount;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public List<String> getAddr() {
        return addr;
    }

    public void setAddr(List<String> addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayerRoomModel{");
        sb.append("roomStatus=").append(roomStatus);
        sb.append(", liveStatus=").append(liveStatus);
        sb.append(", onlineUserCount=").append(onlineUserCount);
        sb.append(", questionCount=").append(questionCount);
        sb.append(", addr=").append(addr);
        sb.append(", roomId=").append(getRoomId());
        sb.append(", gameId=").append(getGameId());
        sb.append(", name='").append(getName()).append('\'');
        sb.append(", creator='").append(getCreator()).append('\'');
        sb.append(", bonus=").append(getBonus());
        sb.append(", gameStatus=").append(getGameStatus());
        sb.append(", rtmpPullUrl='").append(getRtmpPullUrl()).append('\'');
        sb.append(", hlsPullUrl='").append(getHlsPullUrl()).append('\'');
        sb.append(", httpPullUrl='").append(getHttpPullUrl()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}