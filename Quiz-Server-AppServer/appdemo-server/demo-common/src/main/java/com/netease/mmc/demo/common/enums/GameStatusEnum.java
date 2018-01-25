package com.netease.mmc.demo.common.enums;

/**
 * 竞答游戏状态.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public enum GameStatusEnum {
    /**
     * 空闲
     */
    INIT(0),
    /**
     * 游戏进行中
     */
    ON_LIVE(1),
    /**
     * 游戏已结束
     */
    OVER(2);

    private int value;

    GameStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GameStatusEnum getEnum(Integer value) {
        if (value == null) {
            return null;
        }
        for (GameStatusEnum typeEnum : GameStatusEnum.values()) {
            if (typeEnum.getValue() == value) {
                return typeEnum;
            }
        }
        return null;
    }
}
