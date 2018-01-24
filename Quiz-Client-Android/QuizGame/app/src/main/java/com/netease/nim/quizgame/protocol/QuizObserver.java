package com.netease.nim.quizgame.protocol;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.quizgame.common.LogUtil;
import com.netease.nim.quizgame.protocol.extension.DefaultCustomAttachment;
import com.netease.nim.quizgame.protocol.model.JsonObject2Model;
import com.netease.nim.quizgame.protocol.model.QuizNotification;
import com.netease.nim.quizgame.ui.cache.ChatRoomCache;
import com.netease.nimlib.sdk.NIMChatRoomSDK;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏过程监听器
 * 监听聊天室消息中的：
 * 0、直播开始
 * 1、发题目
 * 2、作答结果
 * 3、最终结果
 * 4、直播结束
 * Created by winnie on 11/01/2018.
 */

public class QuizObserver {
    private static final String CMD = "cmd";
    private static final String DATA = "data";

    private String roomId;
    private String creator;
    private Observer<QuizNotification> observer;
    private int currentShowedOrder = -1; // 当前已经存储的order

    public void init(String roomId, String creator) {
        this.roomId = roomId;
        this.creator = creator;

        NIMChatRoomSDK.getChatRoomServiceObserve().observeReceiveMessage(messageObserver, true);
        LogUtil.app("QuizObserver init");
    }

    public void destroy() {
        NIMChatRoomSDK.getChatRoomServiceObserve().observeReceiveMessage(messageObserver, false);
        LogUtil.app("QuizObserver destroy");
    }

    public void observeQuiz(Observer<QuizNotification> observer, boolean register) {
        if (observer == null) {
            return;
        }

        if (register) {
            this.observer = observer;
        } else {
            this.observer = null;
        }
    }

    private Observer<List<ChatRoomMessage>> messageObserver = (messages) -> {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        ArrayList<ChatRoomMessage> chatRoomMessages = new ArrayList<>();
        for (ChatRoomMessage message : messages) {
            // 不是主播发的自定义消息，就不用这个解析器了
            if (!message.getSessionId().equals(roomId)
                    || !message.getFromAccount().equals(creator)
                    || !checkChatRoomMsgId(message)) {
                continue;
            }
            if (message.getMsgType() == MsgTypeEnum.custom) {

                chatRoomMessages.add(message);

                DefaultCustomAttachment attachment = (DefaultCustomAttachment) message.getAttachment();
                String content = attachment.getContent();

                JSONObject contentObject = JSONObject.parseObject(content);
                int cmd = contentObject.getIntValue(CMD);
                JSONObject data = contentObject.getJSONObject(DATA);

                if (data == null) {
                    continue;
                }

                // 去重
                chatRoomMessages.add(message);
                ChatRoomCache.getInstance().getMessageManager(roomId).saveMessageId(chatRoomMessages);

                QuizNotification quizNotification = (QuizNotification) JsonObject2Model.parseJsonToModule(data.toJSONString(), QuizNotification.class);
                quizNotification.setCmd(cmd);

                LogUtil.app("receive cmd:" + cmd);
                if (observer != null && quizNotification.getCmd() != -1) {
                    // order比现在还旧，就不用上报了
                    if (quizNotification.getQuestionInfo() != null
                            && currentShowedOrder <= quizNotification.getQuestionInfo().getOrder()) {
                        LogUtil.app("currentShowedOrder:" + currentShowedOrder + ", order:" + quizNotification.getQuestionInfo().getOrder());
                        currentShowedOrder = quizNotification.getQuestionInfo().getOrder();
                        observer.onEvent(quizNotification);
                    }

                    // result 没有order
                    if (quizNotification.getQuestionInfo() == null) {
                        LogUtil.app("notify result");
                        observer.onEvent(quizNotification);
                    }

                }
            }
        }
    };


    private static boolean checkChatRoomMsgId(ChatRoomMessage message) {
        String roomId = message.getSessionId();
        String msgId = message.getUuid();

        // 先检查MSGID是否已经存在，去重
        if (!TextUtils.isEmpty(msgId) && ChatRoomCache.getInstance().getMessageManager(roomId).existMessageId(msgId)) {
            return false;
        }

        return true;
    }

}
