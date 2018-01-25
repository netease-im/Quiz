package com.netease.mmc.demo.httpdao.nim.dto;

/**
 * 云信API接口返回值DTO.
 *
 * @author hzwanglin1
 * @date 2017/6/24
 * @since 1.0
 */
public class ChatroomResponseDTO extends NimResponseDTO{
    /**
     * 聊天室信息
     */
    private ChatroomDTO chatroom;

    public ChatroomDTO getChatroom() {
        return chatroom;
    }

    public void setChatroom(ChatroomDTO chatroom) {
        this.chatroom = chatroom;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatroomResponseDTO{");
        sb.append("chatroom=").append(chatroom);
        sb.append(", code=").append(getCode());
        sb.append(", desc='").append(getDesc()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
