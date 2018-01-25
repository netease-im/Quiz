package com.netease.mmc.demo.service;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.mmc.demo.common.constant.CommonConst;
import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.enums.AnswerResultEnum;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.exception.QuizException;
import com.netease.mmc.demo.common.util.RedissonUtil;
import com.netease.mmc.demo.dao.QuizAnswerStatsDao;
import com.netease.mmc.demo.dao.QuizQuestionDao;
import com.netease.mmc.demo.dao.domain.QuizOptionDO;
import com.netease.mmc.demo.dao.domain.QuizQuestionDO;
import com.netease.mmc.demo.service.model.OptionModel;
import com.netease.mmc.demo.service.model.QuestionModel;
import com.netease.mmc.demo.service.util.ModelUtil;

/**
 * 竞答题目Service.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
@Service
public class QuizQuestionService {
    private static final Logger logger = LoggerFactory.getLogger(QuizQuestionService.class);

    @Resource
    private QuizQuestionDao quizQuestionDao;
    @Resource
    private QuizAnswerStatsDao quizAnswerStatsDao;

    /**
     * 按照传入的id顺序查询题目的信息.
     *
     * @param questionIds 题目id列表
     * @return
     */
    public List<QuestionModel> queryQuestionsByOrder(@Nonnull List<Long> questionIds) {
        List<QuizQuestionDO> questionDOList = quizQuestionDao.listByIds(questionIds);

        ImmutableMap<Long, QuizQuestionDO> quistionIdMap =
                Maps.uniqueIndex(questionDOList, new Function<QuizQuestionDO, Long>() {
                    @Override
                    public Long apply(QuizQuestionDO quizQuestionDO) {
                        return quizQuestionDO.getId();
                    }
                });

        List<QuestionModel> modelList = Lists.newArrayListWithCapacity(questionIds.size());
        for (int i = 0; i < questionIds.size(); i++) {
            Long questionId = questionIds.get(i);
            QuizQuestionDO questionDO = quistionIdMap.get(questionId);
            if (questionDO == null) {
                logger.error("quiz questions error, question not exists, questionId {}", questionId);
                throw new QuizException(HttpCodeEnum.QUESTION_NOT_FOUND);
            }
            QuestionModel questionModel = ModelUtil.INSTANCE.questionDO2Model(questionDO);
            questionModel.setOrder(i);
            modelList.add(questionModel);
        }
        return modelList;
    }

    /**
     * 查询题目信息以及答题统计数据
     *
     * @param questionId 题目id
     * @return
     */
    public QuestionModel queryQuestionWithStats(long gameId, long questionId) {
        QuizQuestionDO questionDO = quizQuestionDao.findByPrimaryKey(questionId);
        if (questionDO == null) {
            logger.error("quiz question stats error, question not exists, questionId {}", questionId);
            throw new QuizException(HttpCodeEnum.QUESTION_NOT_FOUND);
        }

        QuestionModel questionModel = ModelUtil.INSTANCE.questionDO2Model(questionDO);

        List<OptionModel> options = questionModel.getOptions();
        Long totalCount = 0L;
        for (OptionModel optionModel : options) {
            Long selectCount = quizAnswerStatsDao.findAnswerCount(gameId, questionId, optionModel.getOptionId());
            if (selectCount == null) {
                optionModel.setSelectCount(0L);
            } else {
                optionModel.setSelectCount(selectCount);
            }
            totalCount += optionModel.getSelectCount();
        }
        questionModel.setSelectCount(totalCount);
        return questionModel;
    }

    /**
     * 回答问题，问题的答案会放到缓存里，从缓存获取答案判断对错
     *
     * @param gameId 游戏id
     * @param questionId 问题id
     * @param answerId 答案id
     * @return
     */
    public AnswerResultEnum answerQuestion(long gameId, long questionId, int answerId) {
        String answerKey = String.format(RedisKeys.QUIZ_QUESTION_ANSWER, questionId);
        List<Integer> answerList = RedissonUtil.get(answerKey);
        if (CollectionUtils.isEmpty(answerList)) {
            answerList = Lists.newArrayList();

            QuizQuestionDO questionDO = quizQuestionDao.findByPrimaryKey(questionId);
            Integer rightAnswer = questionDO.getRightAnswer();
            answerList.add(rightAnswer);

            List<QuizOptionDO> options = questionDO.getOptions();
            for (QuizOptionDO option : options) {
                answerList.add(option.getOptionId());
            }

            RedissonUtil.setex(answerKey, answerList, CommonConst.VALID_ANSWER_PERIOD);
        }

        AnswerResultEnum answerResult;

        int index = answerList.indexOf(answerId);
        // 答案缓存列表，第一位为正确答案
        if (index == 0) {
            answerResult = AnswerResultEnum.RIGHT;
        } else if (index < 0) {
            answerResult = AnswerResultEnum.INVALID;
        } else {
            answerResult = AnswerResultEnum.WRONG;
        }

        // 有效回答，更新答题统计
        if (answerResult == AnswerResultEnum.RIGHT || answerResult == AnswerResultEnum.WRONG) {
            quizAnswerStatsDao.insertOrPlusAnswerCount(gameId, questionId, answerId, 1);
        }
        return answerResult;
    }

    /**
     * 清楚现有答题统计数据
     *
     * @param gameId 游戏id
     * @param questionId 问题id
     */
    public void clearQuestionAnswerStats(long gameId, long questionId) {
        quizAnswerStatsDao.deleteAnswerStats(gameId, questionId);
    }
}