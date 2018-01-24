package com.netease.mmc.demo.web.vo;

import java.math.BigDecimal;

/**
 * 竞答房间VO.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public class RoomVO {
    /**
     * 房间id
     */
    private Long roomId;

    /**
     * 房间名称
     */
    private String name;

    /**
     * 房主账号
     */
    private String creator;

    /**
     * 奖金金额
     */
    private BigDecimal bonus;

    /**
     * 游戏状态
     */
    private Integer gameStatus;

    /**
     * rtmp拉流地址
     */
    private String rtmpPullUrl;

    /**
     * hls拉流地址
     */
    private String hlsPullUrl;

    /**
     * http拉流地址
     */
    private String httpPullUrl;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public Integer getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(Integer gameStatus) {
        this.gameStatus = gameStatus;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomModel{");
        sb.append("roomId=").append(roomId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", creator='").append(creator).append('\'');
        sb.append(", bonus=").append(bonus);
        sb.append(", gameStatus=").append(gameStatus);
        sb.append(", rtmpPullUrl='").append(rtmpPullUrl).append('\'');
        sb.append(", hlsPullUrl='").append(hlsPullUrl).append('\'');
        sb.append(", httpPullUrl='").append(httpPullUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}