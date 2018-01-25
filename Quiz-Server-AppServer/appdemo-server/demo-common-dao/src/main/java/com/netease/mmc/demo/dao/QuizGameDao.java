package com.netease.mmc.demo.dao;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.QuizGameDO;

/**
 * QuizGameDao table demo_quiz_game's dao.
 *
 * @author hzwanglin1
 * @date 2018-01-13
 * @since 1.0
 */
public interface QuizGameDao {
    int insertSelective(QuizGameDO record);

    QuizGameDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(QuizGameDO record);

    int updateBonusById(@Param("gameId") long gameId, @Param("bonus") BigDecimal bonus);
}