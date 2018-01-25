package com.netease.nim.quizgame.ui.cache;

import com.netease.nim.quizgame.common.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by winnie on 23/01/2018.
 */

public class ChatRoomCache {

    public static ChatRoomCache getInstance() {
        return InstanceHolder.instance;
    }

    private Map<String, ChatRoomMessageManager> messageManagers = new ConcurrentHashMap<>();

    public void clear() {
        for (Map.Entry<String, ChatRoomMessageManager> entry : messageManagers.entrySet()) {
            entry.getValue().clear();
        }
        messageManagers.clear();
    }

    /**
     * ***************** exitTimeoutTask *****************
     */
    public ChatRoomMessageManager getMessageManager(String roomId) {
        if (roomId == null) {
            LogUtil.app("getMessageManager, room id is null");
        }

        if (!this.messageManagers.containsKey(roomId)) {
            this.messageManagers.put(roomId, new ChatRoomMessageManager(roomId));
        }

        return this.messageManagers.get(roomId);
    }

    /**
     * ***************** single instance *****************
     */
    public static class InstanceHolder {
        public final static ChatRoomCache instance = new ChatRoomCache();
    }
}
