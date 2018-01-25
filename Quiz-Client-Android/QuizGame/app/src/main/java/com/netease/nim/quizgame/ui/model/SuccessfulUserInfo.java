package com.netease.nim.quizgame.ui.model;

/**
 * Created by winnie on 13/01/2018.
 */

public class SuccessfulUserInfo {
    private String account;
    private float money;

    public SuccessfulUserInfo(String account, float money) {
        this.account = account;
        this.money = money;
    }

    public String getAccount() {
        return account;
    }

    public float getMoney() {
        return money;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setMoney(long money) {
        this.money = money;
    }
}
