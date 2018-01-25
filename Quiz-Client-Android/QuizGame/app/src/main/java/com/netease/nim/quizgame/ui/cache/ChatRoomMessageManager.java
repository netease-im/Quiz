package com.netease.nim.quizgame.ui.cache;

import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatRoomMessageManager {

    private final String roomId;

    public ChatRoomMessageManager(String roomId) {
        this.roomId = roomId;
    }

    public void clear() {
        clearMessageIds();
    }

    /**
     * ***************************** 去重控制 *****************************
     */

    private static final int CAPACITY = 10;

    private Queue<String> messageIds = new ConcurrentLinkedQueue<>(); // 消息去重用

    // 在dispatcher线程上
    public void saveMessageId(final List<ChatRoomMessage> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return;
        }

        for (ChatRoomMessage msg : messageList) {
            if (messageIds.size() >= CAPACITY) {
                messageIds.poll();
            }

            messageIds.add(msg.getUuid());
        }
    }

    // 在dispatcher线程上
    public boolean existMessageId(String msgId) {
        return messageIds.contains(msgId);
    }

    // 在主线程上
    private void clearMessageIds() {
        messageIds.clear();
    }
}
