package com.netease.mmc.demo.web.vo;

import java.util.List;

/**
 * 主播端竞答房间VO.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public class HostRoomVO extends RoomVO{
    /**
     * 推流地址
     */
    private String pushUrl;

    /**
     * 问题数量
     */
    private Integer questionCount;

    /**
     * 问题详情
     */
    private List<QuestionVO> questionInfo;

    /**
     * 主播房间密码
     */
    private String password;

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public List<QuestionVO> getQuestionInfo() {
        return questionInfo;
    }

    public void setQuestionInfo(List<QuestionVO> questionInfo) {
        this.questionInfo = questionInfo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HostRoomVO{");
        sb.append("pushUrl='").append(pushUrl).append('\'');
        sb.append(", questionCount=").append(questionCount);
        sb.append(", questionInfo=").append(questionInfo);
        sb.append(", password='").append(password).append('\'');
        sb.append(", roomId=").append(getRoomId());
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