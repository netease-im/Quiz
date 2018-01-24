package com.netease.mmc.demo.web.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 奖励信息VO.
 *
 * @author hzwanglin1
 * @date 2018/1/17
 * @since 1.0
 */
public class BonusInfoVO {
    /**
     * 游戏参与人数
     */
    private Long playerCount;

    /**
     * 获胜人数
     */
    private Long winnerCount;

    /**
     * 获胜玩家示例
     */
    private List<String> winnerSample;

    /**
     * 每人奖励金额
     */
    private BigDecimal bonus;

    public Long getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Long playerCount) {
        this.playerCount = playerCount;
    }

    public Long getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(Long winnerCount) {
        this.winnerCount = winnerCount;
    }

    public List<String> getWinnerSample() {
        return winnerSample;
    }

    public void setWinnerSample(List<String> winnerSample) {
        this.winnerSample = winnerSample;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BonusInfoVO{");
        sb.append("playerCount=").append(playerCount);
        sb.append(", winnerCount=").append(winnerCount);
        sb.append(", winnerSample=").append(winnerSample);
        sb.append(", bonus=").append(bonus);
        sb.append('}');
        return sb.toString();
    }
}