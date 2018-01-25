package com.netease.mmc.demo.web.interceptor;

import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.netease.mmc.demo.common.constant.CommonConst;
import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.enums.GameStatusEnum;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.util.DataPack;
import com.netease.mmc.demo.common.util.RedissonUtil;
import com.netease.mmc.demo.dao.domain.QuizRoomDO;
import com.netease.mmc.demo.service.QuizRoomService;

/**
 * 主播端密码验证拦截器.
 *
 * @author hzwanglin1
 * @date 2018/1/15
 * @since 1.0
 */
public class ValidateHostInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private QuizRoomService quizRoomService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String roomIdStr = request.getParameter("roomId");
        String passwordInput = request.getParameter("password");

        Long roomId;
        try {
            roomId = Long.valueOf(roomIdStr);
        } catch (NumberFormatException e) {
            roomId = null;
        }

        if (roomId == null || StringUtils.isBlank(passwordInput)) {
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.getWriter().print(DataPack
                    .failResponseByJson(HttpCodeEnum.BAD_REQUEST.value(), HttpCodeEnum.BAD_REQUEST.getReasonPhrase()));
            return false;
        }

        QuizRoomDO roomDO = quizRoomService.queryRoomByRoomId(roomId);
        if (roomDO == null) {
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.getWriter().print(DataPack.failResponseByJson(HttpCodeEnum.CHATROOM_NOT_FOUND.value(),
                    HttpCodeEnum.CHATROOM_NOT_FOUND.getReasonPhrase()));
            return false;
        }
        if (Objects.equals(roomDO.getGameStatus(), GameStatusEnum.OVER.getValue())) {
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.getWriter().print(DataPack.failResponseByJson(HttpCodeEnum.BAD_REQUEST.value(),
                    "游戏已结束，无法操作"));
            return false;
        }

        String passwordKey = String.format(RedisKeys.QUIZ_GAME_PASSWORD, roomDO.getRoomId(), roomDO.getGameId());
        String password = RedissonUtil.get(passwordKey);
        if (!Objects.equals(password, passwordInput)) {
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.getWriter().print(DataPack.failResponseByJson(HttpCodeEnum.UNAUTHORIZED.value(), "密码校验失败"));
            return false;
        }
        request.setAttribute(CommonConst.QUIZ_ROOM_DO_REQUEST_ATTRIBUTE, roomDO);
        return true;
    }
}
