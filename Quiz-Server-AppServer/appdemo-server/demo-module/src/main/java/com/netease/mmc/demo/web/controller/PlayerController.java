package com.netease.mmc.demo.web.controller;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.redisson.core.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netease.mmc.demo.common.constant.CommonConst;
import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.context.WebContextHolder;
import com.netease.mmc.demo.common.enums.AnswerResultEnum;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.enums.RoomAddressTypeEnum;
import com.netease.mmc.demo.common.session.SessionUserModel;
import com.netease.mmc.demo.common.util.DataPack;
import com.netease.mmc.demo.common.util.RedissonUtil;
import com.netease.mmc.demo.dao.domain.QuizGameDO;
import com.netease.mmc.demo.dao.domain.QuizRoomDO;
import com.netease.mmc.demo.service.QuizGameService;
import com.netease.mmc.demo.service.QuizQuestionService;
import com.netease.mmc.demo.service.QuizRoomService;
import com.netease.mmc.demo.service.TouristService;
import com.netease.mmc.demo.service.model.PlayerRoomModel;
import com.netease.mmc.demo.service.model.TouristModel;
import com.netease.mmc.demo.web.util.VOUtil;
import com.netease.mmc.demo.web.vo.AnswerResultVO;

/**
 * 游客账号Controller.
 *
 * @author hzwanglin1
 * @date 2017/11/20
 * @since 1.0
 */
@Controller
@RequestMapping("quiz/player")
public class PlayerController {
    private static final Logger quizLog = LoggerFactory.getLogger("quizLog");

    @Resource
    private TouristService touristService;
    @Resource
    private QuizRoomService quizRoomService;
    @Resource
    private QuizQuestionService quizQuestionService;
    @Resource
    private QuizGameService quizGameService;

    /**
     * 游客登陆.
     *
     * @param accid 当前使用的游客账号
     * @return
     */
    @RequestMapping(value = "create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap get(@RequestParam(value = "sid", required = false) String accid) {
        TouristModel touristModel = touristService.validateAndRefreshUser(accid);
        // 继续使用当前账号并重置账号失效时间
        if (touristModel != null) {
            return DataPack.packOk(VOUtil.INSTANCE.touristModel2VO(touristModel));
        }

        String ip = WebContextHolder.getIp();
        // 针对ip进行频控
        if (touristService.isIpLimited(ip)) {
            return DataPack.packFailure(HttpCodeEnum.TOURIST_GET_LIMIT);
        }
        // 从当前账号池获取账号
        touristModel = touristService.getFromTouristPool();
        if (touristModel == null) {
            // 如果账号池中没有账号，同步创建新的游客账号
            touristModel = touristService.createNewTourist();
        }

        if (touristModel == null) {
            return DataPack.packFailure(HttpCodeEnum.TOURIST_GET_ERROR);
        } else {
            // 成功获取到账号，更新ip频控计数
            touristService.incrTouristGetCount(ip);
            return DataPack.packOk(VOUtil.INSTANCE.touristModel2VO(touristModel));
        }
    }

    /**
     * 玩家查询房间信息
     *
     * @param roomId 房间id
     * @param addrType 聊天室地址类型
     * @return
     */
    @RequestMapping(value = "room/query", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap queryRoomInfo(@RequestParam(value = "roomId") Long roomId,
            @RequestParam(value = "addrType", defaultValue = "COMMON") RoomAddressTypeEnum addrType) {
        SessionUserModel userModel = (SessionUserModel) WebContextHolder.getCurrentUser();
        PlayerRoomModel playerRoomModel = quizRoomService.queryPlayerRoom(roomId, userModel.getAccid(), addrType);
        if (playerRoomModel == null) {
            return DataPack.packFailure(HttpCodeEnum.CHATROOM_NOT_FOUND);
        }
        return DataPack.packOk(VOUtil.INSTANCE.playerRoomModel2VO(playerRoomModel));
    }

    /**
     * 玩家提交问题答案
     *
     * @param roomId 房间id
     * @param questionId 问题id
     * @param answerId 答案id
     * @return
     */
    @RequestMapping(value = "answer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap answer(@RequestParam(value = "roomId") Long roomId,
            @RequestParam(value = "questionId") Long questionId, @RequestParam(value = "answer") Integer answerId) {
        if (roomId == null || questionId == null || answerId == null) {
            return DataPack.packFailure(HttpCodeEnum.BAD_REQUEST);
        }

        QuizRoomDO roomDO = quizRoomService.queryRoomByRoomId(roomId);
        if (roomDO == null) {
            return DataPack.packFailure(HttpCodeEnum.CHATROOM_NOT_FOUND);
        }

        SessionUserModel userModel = (SessionUserModel) WebContextHolder.getCurrentUser();
        String accid = userModel.getAccid();

        AnswerResultEnum answerResult;
        String gettingAnswerKey =
                String.format(RedisKeys.QUIZ_GETTING_ANSWER, roomDO.getRoomId(), roomDO.getGameId());
        Long curQuestionId = RedissonUtil.getAtomicLong(gettingAnswerKey);

        // 不在有效答题时间返回内或者不是回答指定题目，视为无效
        if (!Objects.equals(curQuestionId, questionId)) {
            answerResult = AnswerResultEnum.INVALID;
        } else {
            String submitAccidSetKey =
                    String.format(RedisKeys.QUIZ_QUESTION_SUBMIT_ACCID_SET, roomDO.getGameId(), questionId);
            // 只有之前没有答过题才允许真正答题
            if (RedissonUtil.sadd(submitAccidSetKey, accid)) {
                // 答题之前判断是否有答题资格
                String gameWinnerMapKey = String.format(RedisKeys.QUIZ_GAME_WINNER_MAP, roomDO.getGameId());
                // 赢家map，key->accid，value->答对次数
                RMap<String, Integer> winnerMap = RedissonUtil.getMap(gameWinnerMapKey);
                Integer curRightCount = winnerMap.get(accid);
                if (curRightCount == null) {
                    curRightCount = 0;
                }

                // 查询游戏题目信息
                QuizGameDO gameDO = quizGameService.queryGameById(roomDO.getGameId());
                List<Long> questions = gameDO.getQuestions();
                // 之前已出题数量
                int questionNum = questions.indexOf(questionId);

                // 当前仍然有答题资格
                if (curRightCount + CommonConst.BRING_BACK_LIMIT >= questionNum) {
                    answerResult = quizQuestionService.answerQuestion(roomDO.getGameId(), questionId, answerId);
                    if (answerResult == AnswerResultEnum.RIGHT) {
                        winnerMap.addAndGet(accid, 1);
                    }
                } else {
                    answerResult = AnswerResultEnum.INVALID;
                }
            } else {
                answerResult = AnswerResultEnum.INVALID;
            }
        }

        quizLog.info("player answer roomId {} gameId {} questionId {} answerId {} result {}", roomId,
                roomDO.getGameId(), questionId, answerId, answerResult);
        AnswerResultVO resultVO = new AnswerResultVO();
        resultVO.setResult(answerResult.getValue());
        return DataPack.packOk(resultVO);
    }

}