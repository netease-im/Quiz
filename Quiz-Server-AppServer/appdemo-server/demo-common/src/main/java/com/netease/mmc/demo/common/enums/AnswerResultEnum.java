package com.netease.mmc.demo.common.enums;

/**
 * 竞答答案结果枚举.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public enum AnswerResultEnum {
    /**
     * 错误
     */
    WRONG(0),
    /**
     * 正确
     */
    RIGHT(1),
    /**
     * 答案无效
     */
    INVALID(2);

    private int value;

    AnswerResultEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AnswerResultEnum getEnum(Integer value) {
        if (value == null) {
            return null;
        }
        for (AnswerResultEnum typeEnum : AnswerResultEnum.values()) {
            if (typeEnum.getValue() == value) {
                return typeEnum;
            }
        }
        return null;
    }
}
