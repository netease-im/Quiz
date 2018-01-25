package com.netease.mmc.demo.web.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.redisson.core.Predicate;
import org.redisson.core.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import com.netease.mmc.demo.common.constant.CommonConst;
import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.enums.GameStatusEnum;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.util.DataPack;
import com.netease.mmc.demo.common.util.RedissonUtil;
import com.netease.mmc.demo.common.util.UUIDUtil;
import com.netease.mmc.demo.dao.domain.QuizGameDO;
import com.netease.mmc.demo.dao.domain.QuizRoomDO;
import com.netease.mmc.demo.service.QuizGameService;
import com.netease.mmc.demo.service.QuizQuestionService;
import com.netease.mmc.demo.service.QuizRoomService;
import com.netease.mmc.demo.service.TouristService;
import com.netease.mmc.demo.service.model.HostRoomModel;
import com.netease.mmc.demo.service.model.QuestionModel;
import com.netease.mmc.demo.web.util.VOUtil;
import com.netease.mmc.demo.web.vo.BonusInfoVO;
import com.netease.mmc.demo.web.vo.HostRoomVO;
import com.netease.mmc.demo.web.vo.QuestionVO;

/**
 * 竞答主播相关Controller.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
@Controller
@RequestMapping("quiz/host")
public class HostController {
    private static final Logger quizLog = LoggerFactory.getLogger("quizLog");

    /**
     * 创建游戏频控时间
     */
    private static final int CREATE_GAME_FREQ_LIMIT_SECONDS = 3;

    @Resource
    private QuizRoomService quizRoomService;
    @Resource
    private QuizGameService quizGameService;
    @Resource
    private QuizQuestionService quizQuestionService;
    @Resource
    private TouristService touristService;

    /**
     * 根据设备号获取直播竞答房间
     *
     * @param deviceId 设备id
     * @param bonus 奖金金额
     * @param ext 聊天室扩展字段
     * @return
     */
    @RequestMapping(value = "create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap createHost(@RequestParam(value = "deviceId") String deviceId,
            @RequestParam(value = "bonus") BigDecimal bonus,
            @RequestParam(value = "ext", required = false) String ext) {
        String limitKey = String.format(RedisKeys.QUIZ_CREATE_GAME_FREQ_LIMIT, deviceId);
        if (RedissonUtil.existsOrExpire(limitKey, CREATE_GAME_FREQ_LIMIT_SECONDS)) {
            return DataPack.packFailure(HttpCodeEnum.REQUEST_FREQ_CTRL);
        }

        if (StringUtils.isBlank(deviceId)) {
            return DataPack.packBadRequest("请指定deviceId");
        }

        // 奖金金额必须为正数
        if (bonus == null || bonus.compareTo(BigDecimal.ZERO) <= 0) {
            return DataPack.packBadRequest("奖金金额必须大于0");
        }

        HostRoomModel room;
        QuizRoomDO roomDO = quizRoomService.queryRoomByDeviceId(deviceId);
        // 已有房间
        if (roomDO != null) {
            room = quizRoomService.enterHostRoom(roomDO, bonus, ext);
        } else {
            room = quizRoomService.createHostRoom(deviceId, bonus, ext);
        }

        HostRoomVO roomVO = VOUtil.INSTANCE.hostRoomModel2VO(room);
        // 如果当前房间没有密码，需要创建密码
        String passwordKey = String.format(RedisKeys.QUIZ_GAME_PASSWORD, room.getRoomId(), room.getGameId());
        String password = RedissonUtil.get(passwordKey);
        if (StringUtils.isBlank(password)) {
            password = UUIDUtil.getUUID();
            RedissonUtil.set(passwordKey, password);
        }
        roomVO.setPassword(password);

        return DataPack.packOk(roomVO);
    }

    /**
     * 变更游戏状态
     *
     * @param roomDO 房间信息
     * @param status 游戏状态
     * @return
     */
    @RequestMapping(value = "switch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap switchHostStatus(
            @RequestAttribute(value = CommonConst.QUIZ_ROOM_DO_REQUEST_ATTRIBUTE) QuizRoomDO roomDO,
            @RequestParam(value = "status") Integer status) {
        GameStatusEnum gameStatus = GameStatusEnum.getEnum(status);
        if (gameStatus == null) {
            return DataPack.packBadRequest("状态参数错误");
        }
        if (Objects.equals(roomDO.getGameStatus(), gameStatus.getValue()) || quizRoomService
                .changeGameStatus(roomDO.getRoomId(), roomDO.getCreator(), gameStatus)) {
            quizLog.info("host switchHostStatus roomId {} gameId {} gameStatus {}", roomDO.getRoomId(),
                    roomDO.getGameId(), gameStatus);
            if (Objects.equals(gameStatus, GameStatusEnum.ON_LIVE)) {
                String gameWinnerMapKey = String.format(RedisKeys.QUIZ_GAME_WINNER_MAP, roomDO.getGameId());
                // 赢家map，key->accid，value->答对次数
                RMap<String, Integer> winnerMap = RedissonUtil.getMap(gameWinnerMapKey);
                winnerMap.delete();
            }
            return DataPack.packOk();
        } else {
            return DataPack.packInternalError();
        }
    }

    /**
     * 通过聊天室消息公布游戏结果
     *
     * @param roomDO 房间信息
     * @param questionId 问题id
     * @param content 消息内容
     * @return
     */
    @RequestMapping(value = "question/publish", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap publishQuestion(
            @RequestAttribute(value = CommonConst.QUIZ_ROOM_DO_REQUEST_ATTRIBUTE) QuizRoomDO roomDO,
            @RequestParam(value = "questionId") Long questionId, @RequestBody String content) {

        if (questionId == null || StringUtils.isBlank(content)) {
            return DataPack.packBadRequest("请求参数错误");
        }

        try {
            JSON.parse(content);
        } catch (JSONException e) {
            quizLog.warn("publishQuestion but content is not json formatted");
            return DataPack.packBadRequest("请求参数错误");
        }

        QuizGameDO gameDO = quizGameService.queryGameById(roomDO.getGameId());
        List<Long> questions = gameDO.getQuestions();
        int index = questions.indexOf(questionId);
        if (index < 0) {
            return DataPack.packBadRequest("题目无效");
        }

        if (!Objects.equals(roomDO.getGameStatus(), GameStatusEnum.ON_LIVE.getValue())) {
            return DataPack.packBadRequest("竞答未开始，不能出题");
        }

        // 先清除当前题目统计数据
        quizQuestionService.clearQuestionAnswerStats(roomDO.getGameId(), questionId);

        if (quizRoomService.publishResult(roomDO, content)) {
            quizLog.info("host publishQuestion roomId {} gameId {} questionId {}", roomDO.getRoomId(),
                    roomDO.getGameId(), questionId);

            // 公布题目后，开始计时，设置有效回答时间
            String gettingAnswerKey =
                    String.format(RedisKeys.QUIZ_GETTING_ANSWER, roomDO.getRoomId(), roomDO.getGameId());
            RedissonUtil.setex(gettingAnswerKey, questionId, CommonConst.VALID_ANSWER_PERIOD);

            // 设置答题参与人集合过期时间，控制每人只能答题一次
            // 设置过期时间是未了防止无效key堆积
            String questionAccidSetKey =
                    String.format(RedisKeys.QUIZ_QUESTION_SUBMIT_ACCID_SET, roomDO.getGameId(), questionId);
            // 空字符串不可能是有效accid，用于在失效之前保留集合
            RedissonUtil.sadd(questionAccidSetKey, StringUtils.EMPTY);
            RedissonUtil.sexpire(questionAccidSetKey, CommonConst.VALID_ANSWER_PERIOD);

            return DataPack.packOk();
        } else {
            return DataPack.packBadRequest("出题失败");
        }
    }

    /**
     * 查询答题结果
     *
     * @param roomDO 房间信息
     * @param questionId 问题id
     * @return
     */
    @RequestMapping(value = "result/query", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap queryQuestionResult(
            @RequestAttribute(value = CommonConst.QUIZ_ROOM_DO_REQUEST_ATTRIBUTE) QuizRoomDO roomDO,
            @RequestParam(value = "questionId") Long questionId) {
        QuizGameDO gameDO = quizGameService.queryGameById(roomDO.getGameId());
        List<Long> questions = gameDO.getQuestions();
        final int index = questions.indexOf(questionId);
        if (index < 0) {
            return DataPack.packBadRequest("题目无效");
        }

        // 如果当前正在答题中，不允许查询
        String gettingAnswerKey = String.format(RedisKeys.QUIZ_GETTING_ANSWER, roomDO.getRoomId(), roomDO.getGameId());
        Long curQuestionId = RedissonUtil.getAtomicLong(gettingAnswerKey);
        if (Objects.equals(curQuestionId, questionId)) {
            return DataPack.packFailure(HttpCodeEnum.ANSWER_IN_PROGRESS);
        }

        QuestionModel questionModel = quizQuestionService.queryQuestionWithStats(roomDO.getGameId(), questionId);
        QuestionVO questionVO = VOUtil.INSTANCE.questionModel2VO(questionModel);
        questionVO.setOrder(index);

        // 如果是查询第一道题，更新游戏参与人数
        if (index == 0) {
            quizGameService.updatePlayerCount(roomDO.getGameId(), questionModel.getSelectCount());
        }
        // 如果是最后一道题，需要更新并返回游戏结果
        if (index == questions.size() - 1) {
            String gameWinnerMapKey = String.format(RedisKeys.QUIZ_GAME_WINNER_MAP, roomDO.getGameId());
            // 赢家map，key->accid，value->答对次数
            RMap<String, Integer> winnerMap = RedissonUtil.getMap(gameWinnerMapKey);
            Map<String, Integer> finalWinnerMap = winnerMap.filterValues(new Predicate<Integer>() {
                @Override
                public boolean apply(Integer curRightCount) {
                    return curRightCount + CommonConst.BRING_BACK_LIMIT >= index + 1;
                }
            });
            Set<String> winnerSet = finalWinnerMap.keySet();
            int winnerCount = winnerSet.size();
            List<String> winnerList = Lists.newArrayListWithCapacity(winnerCount);
            winnerList.addAll(winnerSet);

            List<String> winnerSample;
            List<String> nicknameList;
            if (winnerCount > 0) {
                if (winnerCount > CommonConst.WINNER_SAMPLE_COUNT) {
                    winnerSample = winnerList.subList(0, CommonConst.WINNER_SAMPLE_COUNT);
                } else {
                    winnerSample = winnerList;
                }
                Map<String, String> nicknameMap = touristService.queryNickname(winnerSample);
                nicknameList = Lists.newArrayListWithCapacity(winnerSample.size());
                nicknameList.addAll(nicknameMap.values());
            } else {
                winnerSample = Collections.emptyList();
                nicknameList = Collections.emptyList();
            }
            quizGameService.updateWinnerInfo(roomDO.getGameId(), winnerList, winnerSample);

            BonusInfoVO bonusInfoVO = new BonusInfoVO();
            bonusInfoVO.setPlayerCount(gameDO.getPlayerCount());
            bonusInfoVO.setWinnerCount((long) winnerCount);
            bonusInfoVO.setWinnerSample(nicknameList);

            BigDecimal avgBonus = BigDecimal.ZERO;
            if (winnerCount > 0) {
                 avgBonus = gameDO.getBonus().divide(BigDecimal.valueOf(winnerCount), 2, RoundingMode.FLOOR);
            }
            bonusInfoVO.setBonus(avgBonus);
            questionVO.setBonusInfo(bonusInfoVO);
        }

        quizLog.info("host queryQuestionResult roomId {} gameId {} questionId {}", roomDO.getRoomId(),
                roomDO.getGameId(), questionId);
        return DataPack.packOk(questionVO);
    }

    /**
     * 通过聊天室消息公布游戏结果
     *
     * @param roomDO 房间信息
     * @param content 消息内容
     * @return
     */
    @RequestMapping(value = "result/publish", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap publishResult(
            @RequestAttribute(value = CommonConst.QUIZ_ROOM_DO_REQUEST_ATTRIBUTE) QuizRoomDO roomDO,
            @RequestBody String content) {

        if (StringUtils.isBlank(content)) {
            return DataPack.packBadRequest("请求参数错误");
        }

        try {
            JSON.parse(content);
        } catch (JSONException e) {
            quizLog.warn("publishQuestion content is not json formatted");
            return DataPack.packBadRequest("请求参数错误");
        }

        if (quizRoomService.publishResult(roomDO, content)) {
            quizLog.info("host publishResult roomId {} gameId {} content {}", roomDO.getRoomId(),
                    roomDO.getGameId(), content);
            return DataPack.packOk();
        } else {
            return DataPack.packBadRequest("操作失败");
        }
    }
}