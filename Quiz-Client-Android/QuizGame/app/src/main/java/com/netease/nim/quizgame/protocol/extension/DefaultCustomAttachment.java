package com.netease.nim.quizgame.protocol.extension;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhoujianghua on 2015/4/10.
 */
public class DefaultCustomAttachment extends CustomAttachment {

    private String content;

    DefaultCustomAttachment() {
        super(0);
    }

    @Override
    protected void parseData(JSONObject data) {
        content = data.toJSONString();
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = null;
        try {
            data = JSONObject.parseObject(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getContent() {
        return content;
    }
}
