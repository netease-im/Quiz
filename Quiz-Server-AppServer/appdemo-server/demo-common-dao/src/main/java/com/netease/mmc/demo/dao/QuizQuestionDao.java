package com.netease.mmc.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.QuizQuestionDO;

/**
 * QuizQuestionDao table demo_quiz_question's dao.
 *
 * @author hzwanglin1
 * @date 2018-01-13
 * @since 1.0
 */
public interface QuizQuestionDao {
    int insertSelective(QuizQuestionDO record);

    QuizQuestionDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(QuizQuestionDO record);

    List<QuizQuestionDO> listByIds(@Param("ids") List<Long> ids);

}