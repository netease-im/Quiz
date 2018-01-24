package com.netease.mmc.demo.web.util;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.netease.mmc.demo.service.model.HostRoomModel;
import com.netease.mmc.demo.service.model.OptionModel;
import com.netease.mmc.demo.service.model.PlayerRoomModel;
import com.netease.mmc.demo.service.model.QuestionModel;
import com.netease.mmc.demo.service.model.TouristModel;
import com.netease.mmc.demo.web.vo.HostRoomVO;
import com.netease.mmc.demo.web.vo.OptionVO;
import com.netease.mmc.demo.web.vo.PlayerRoomVO;
import com.netease.mmc.demo.web.vo.QuestionVO;
import com.netease.mmc.demo.web.vo.TouristVO;


/**
 * Model转换工具类.
 *
 * @author hzwanglin1
 * @date 17-6-26
 * @since 1.0
 */
@Mapper
public interface VOUtil {
    VOUtil INSTANCE = Mappers.getMapper(VOUtil.class);

    TouristVO touristModel2VO(TouristModel touristModel);

    OptionVO optionModel2VO(OptionModel optionModel);

    QuestionVO questionModel2VO(QuestionModel questionModel);

    HostRoomVO hostRoomModel2VO(HostRoomModel hostRoomModel);

    PlayerRoomVO playerRoomModel2VO(PlayerRoomModel playerRoomModel);
}
