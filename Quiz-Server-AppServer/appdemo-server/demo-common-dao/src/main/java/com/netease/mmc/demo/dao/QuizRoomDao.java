package com.netease.mmc.demo.dao;

import org.apache.ibatis.annotations.Param;

import com.netease.mmc.demo.dao.domain.QuizRoomDO;

/**
 * QuizRoomDao table demo_quiz_room's dao.
 *
 * @author hzwanglin1
 * @date 2018-01-13
 * @since 1.0
 */
public interface QuizRoomDao {
    int insertSelective(QuizRoomDO record);

    QuizRoomDO findByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(QuizRoomDO record);

    QuizRoomDO findByDeviceId(@Param("deviceId") String deviceId);

    QuizRoomDO findByRoomId(@Param("roomId") long roomId);

    int updateGameStatus(@Param("roomId") long roomId, @Param("gameStatus") int gameStatus);
}