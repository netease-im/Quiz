package com.netease.mmc.demo.dao;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.QuizAnswerStatsDO;

/**
 * QuizAnswerStatsDao table demo_quiz_answer_stats's dao.
 *
 * @author hzwanglin1
 * @date 2018-01-13
 * @since 1.0
 */
public interface QuizAnswerStatsDao {
    int insertSelective(QuizAnswerStatsDO record);

    QuizAnswerStatsDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(QuizAnswerStatsDO record);

    Long findAnswerCount(@Param("gameId") long gameId, @Param("questionId") long questionId,
            @Param("answerId") int answerId);

    int insertOrPlusAnswerCount(@Param("gameId") long gameId, @Param("questionId") long questionId,
            @Param("answerId") int answerId, @Param("count") int count);

    /**
     * 删除现有答题统计数据
     *
     * @param gameId 游戏id
     * @param questionId 题目id
     * @return
     */
    int deleteAnswerStats(@Param("gameId") long gameId, @Param("questionId") long questionId);
}