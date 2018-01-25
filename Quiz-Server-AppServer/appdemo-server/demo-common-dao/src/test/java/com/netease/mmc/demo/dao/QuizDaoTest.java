package com.netease.mmc.demo.dao;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.netease.mmc.demo.dao.domain.QuizOptionDO;
import com.netease.mmc.demo.dao.domain.QuizQuestionDO;

/**
 * 直播竞答dao单元测试.
 *
 * @author hzwanglin1
 * @date 2018/1/13
 * @since 1.0
 */
public class QuizDaoTest extends BaseDAOTest {
    @Resource
    private QuizQuestionDao quizQuestionDao;

    private Long existsId;

    @Before
    public void setUp() throws Exception {
        QuizQuestionDO questionDO = new QuizQuestionDO();
        QuizOptionDO optionDO = new QuizOptionDO();
        optionDO.setOptionId(1);
        optionDO.setContent("balabaha");
        questionDO.setOptions(Lists.newArrayList(optionDO));
        questionDO.setQuestion("hahahah");
        questionDO.setRightAnswer(1);
        quizQuestionDao.insertSelective(questionDO);
        existsId = questionDO.getId();
    }

    @Test
    public void quizQuestionSelectTest() {
        QuizQuestionDO questionDO = quizQuestionDao.findByPrimaryKey(existsId);
        System.out.println(questionDO);
    }
}