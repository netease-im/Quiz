package com.netease.mmc.demo.service.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.netease.mmc.demo.dao.domain.QuizOptionDO;
import com.netease.mmc.demo.dao.domain.QuizQuestionDO;
import com.netease.mmc.demo.dao.domain.TouristDO;
import com.netease.mmc.demo.service.model.OptionModel;
import com.netease.mmc.demo.service.model.QuestionModel;
import com.netease.mmc.demo.service.model.TouristModel;


/**
 * Model转换工具类.
 *
 * @author hzwanglin1
 * @date 17-6-26
 * @since 1.0
 */
@Mapper
public interface ModelUtil {
    ModelUtil INSTANCE = Mappers.getMapper(ModelUtil.class);

    /**
     * 将TouristDO转换为TouristModel
     *
     * @param touristDO
     * @return
     */
    TouristModel touristDO2Model(TouristDO touristDO);

    OptionModel optionDO2Model(QuizOptionDO optionDO);

    @Mapping(source = "id", target = "questionId")
    QuestionModel questionDO2Model(QuizQuestionDO questionDO);

}
