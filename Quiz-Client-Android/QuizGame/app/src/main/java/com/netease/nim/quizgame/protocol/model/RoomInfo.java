package com.netease.nim.quizgame.protocol.model;


import com.netease.nim.quizgame.common.annotation.KeepMemberNames;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huangjun on 2017/11/20.
 */
@KeepMemberNames
public class RoomInfo implements Serializable {
    private String roomId;
    private String name;
    private String creator;
    private String rtmpPullUrl;
    private String httpPullUrl;
    private String hlsPullUrl;
    private boolean roomStatus;
    private int liveStatus;
    private int onlineUserCount;
    private float bonus;
    private List<String> addr;
    private int questionCount;

    public String getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getRtmpPullUrl() {
        return rtmpPullUrl;
    }

    public String getHttpPullUrl() {
        return httpPullUrl;
    }

    public String getHlsPullUrl() {
        return hlsPullUrl;
    }

    public boolean isRoomStatus() {
        return roomStatus;
    }

    public int getLiveStatus() {
        return liveStatus;
    }

    public int getOnlineUserCount() {
        return onlineUserCount;
    }

    public float getBonus() {
        return bonus;
    }

    public List<String> getAddr() {
        return addr;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public static RoomInfo createFakeRoomInfo(String roomId, String name) {
        RoomInfo r = new RoomInfo();
        r.roomId = roomId;
        r.name = name;
        return r;
    }
}
