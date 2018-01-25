package com.netease.mmc.demo.service;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.util.RedissonUtil;
import com.netease.mmc.demo.dao.QuizGameDao;
import com.netease.mmc.demo.dao.domain.QuizGameDO;

/**
 * 竞答游戏Service.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
@Service
public class QuizGameService {
    private static final Logger logger = LoggerFactory.getLogger(QuizGameService.class);

    /**
     * 创建房间时的默认题目
     */
    private static final List<Long> DEFAULT_QUESTIONS = Lists.newArrayList(103L, 104L, 105L);

    @Resource
    private QuizGameDao quizGameDao;

    /**
     * 创建一局问答游戏
     *
     * @param roomId 房间id
     * @param bonus 奖金金额
     * @param questions 问题列表
     * @return
     */
    public QuizGameDO createGame(long roomId, @Nonnull BigDecimal bonus, @Nonnull List<Long> questions) {
        QuizGameDO gameDO = new QuizGameDO();
        gameDO.setRoomId(roomId);
        gameDO.setBonus(bonus);
        gameDO.setQuestions(questions);

        quizGameDao.insertSelective(gameDO);
        return gameDO;
    }

    /**
     * 获取默认题目列表
     *
     * @return
     */
    @Nonnull
    public List<Long> getDefaultQuestions() {
        String cacheQuestionsStr = RedissonUtil.get(RedisKeys.DEFAULT_QUIZ_QUESTIONS_KEY);
        try {
            List<Long> cacheQuestions = JSON.parseArray(cacheQuestionsStr, Long.class);
            if (CollectionUtils.isEmpty(cacheQuestions)) {
                RedissonUtil.set(RedisKeys.DEFAULT_QUIZ_QUESTIONS_KEY, JSON.toJSONString(DEFAULT_QUESTIONS));
            } else {
                return cacheQuestions;
            }
        } catch (Exception e) {
            logger.error("cacheQuestions format error, {}", cacheQuestionsStr);
        }
        return DEFAULT_QUESTIONS;
    }

    public QuizGameDO queryGameById(long gameId) {
        return quizGameDao.findByPrimaryKey(gameId);
    }

    /**
     * 更新游戏奖金金额
     *
     * @param gameId 游戏id
     * @param bonus 奖金金额
     * @return
     */
    public boolean updateBonus(long gameId, BigDecimal bonus) {
        QuizGameDO gameDO = new QuizGameDO();
        gameDO.setId(gameId);
        gameDO.setBonus(bonus);
        return quizGameDao.updateByPrimaryKeySelective(gameDO) > 0;
    }

    /**
     * 更新玩家人数
     *
     * @param gameId 游戏id
     * @param count 参与人数
     * @return
     */
    public boolean updatePlayerCount(long gameId, long count) {
        QuizGameDO gameDO = new QuizGameDO();
        gameDO.setId(gameId);
        gameDO.setPlayerCount(count);
        return quizGameDao.updateByPrimaryKeySelective(gameDO) > 0;
    }

    /**
     * 更新赢家信息
     *
     * @param gameId
     * @param winner
     * @return
     */
    public boolean updateWinnerInfo(long gameId, @Nullable List<String> winner, @Nullable List<String> winnerSample) {
        QuizGameDO gameDO = new QuizGameDO();
        gameDO.setId(gameId);
        gameDO.setWinnerCount(winner == null ? 0L : winner.size());
        gameDO.setWinner(winner);
        gameDO.setWinnerSample(winnerSample);
        return quizGameDao.updateByPrimaryKeySelective(gameDO) > 0;
    }
}