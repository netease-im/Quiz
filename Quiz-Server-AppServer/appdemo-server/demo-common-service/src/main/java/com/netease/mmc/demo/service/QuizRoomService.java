package com.netease.mmc.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netease.mmc.demo.common.enums.GameStatusEnum;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.enums.LiveStatusEnum;
import com.netease.mmc.demo.common.enums.RoomAddressTypeEnum;
import com.netease.mmc.demo.common.exception.ChatroomException;
import com.netease.mmc.demo.common.exception.LiveException;
import com.netease.mmc.demo.common.exception.QuizException;
import com.netease.mmc.demo.common.exception.UserException;
import com.netease.mmc.demo.dao.QuizRoomDao;
import com.netease.mmc.demo.dao.domain.QuizGameDO;
import com.netease.mmc.demo.dao.domain.QuizRoomDO;
import com.netease.mmc.demo.httpdao.nim.NIMServerApiHttpDao;
import com.netease.mmc.demo.httpdao.nim.dto.AddrResponseDTO;
import com.netease.mmc.demo.httpdao.nim.dto.ChatroomDTO;
import com.netease.mmc.demo.httpdao.nim.dto.ChatroomResponseDTO;
import com.netease.mmc.demo.httpdao.nim.dto.NIMUserDTO;
import com.netease.mmc.demo.httpdao.nim.dto.NIMUserResponseDTO;
import com.netease.mmc.demo.httpdao.nim.dto.NimResponseDTO;
import com.netease.mmc.demo.httpdao.vcloud.VcloudServerApiHttpDao;
import com.netease.mmc.demo.httpdao.vcloud.dto.ChannelInfoDTO;
import com.netease.mmc.demo.httpdao.vcloud.dto.ChannelStatusDTO;
import com.netease.mmc.demo.httpdao.vcloud.dto.VCloudResponseDTO;
import com.netease.mmc.demo.service.model.HostRoomModel;
import com.netease.mmc.demo.service.model.PlayerRoomModel;
import com.netease.mmc.demo.service.model.QuestionModel;

/**
 * 竞答房间Service.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
@Service
public class QuizRoomService {
    private static final Logger logger = LoggerFactory.getLogger(QuizRoomService.class);

    private static final String HOST_ACCID_PREFIX = "host";

    private static final String LIVE_CHANNEL_PREFIX = "quizChannel";

    private static final String ROOM_NAME_PREFIX = "quizRoom";

    @Resource
    private QuizRoomDao quizRoomDao;
    @Resource
    private NIMServerApiHttpDao nimServerApiHttpDao;
    @Resource
    private VcloudServerApiHttpDao vcloudServerApiHttpDao;
    @Resource
    private SeqService seqService;
    @Resource
    private QuizGameService quizGameService;
    @Resource
    private QuizQuestionService quizQuestionService;

    /**
     * 根据设备号创建竞答房间.
     *
     * 会针对当前设备创建一个云信账号，用于创建聊天室；
     * 正式场景中，可以使用与主播绑定的云信账号创建。
     *
     * @param deviceId 主播设备号
     * @param bonus 奖金金额
     * @param ext 聊天室扩展字段
     */
    public HostRoomModel createHostRoom(String deviceId, BigDecimal bonus, String ext) {
        Long seqId = seqService.getSeqId();
        // 创建云信账号
        String accid = HOST_ACCID_PREFIX + seqId;
        NIMUserResponseDTO userResponse = nimServerApiHttpDao.createUser(accid, null, null);
        if (!userResponse.isSuccess()) {
            logger.error("quiz room: create host accid failed, cause {}", userResponse);
            throw new UserException(userResponse.getCode(), userResponse.getDesc());
        }
        NIMUserDTO nimUserDTO = userResponse.getInfo();

        // 创建云信聊天室
        String roomName = ROOM_NAME_PREFIX + seqId;
        ChatroomResponseDTO roomResponse =
                nimServerApiHttpDao.createRoom(nimUserDTO.getAccid(), roomName, null, null, ext);
        if (!roomResponse.isSuccess()) {
            logger.error("quiz room: create room failed, cause {}", roomResponse);
            throw new ChatroomException(roomResponse.getCode(), roomResponse.getDesc());
        }
        ChatroomDTO chatroomDTO = roomResponse.getChatroom();

        // 创建直播频道
        String channelName = LIVE_CHANNEL_PREFIX + seqId;
        VCloudResponseDTO<ChannelInfoDTO> channelResponse = vcloudServerApiHttpDao.createChannel(channelName);
        if (!channelResponse.isSuccess()) {
            logger.error("quiz room: create live channel failed, cause {}", channelResponse);
            throw new LiveException(channelResponse.getCode(), channelResponse.getMsg());
        }
        ChannelInfoDTO channelInfoDTO = channelResponse.getRet();

        // 创建一局游戏
        QuizGameDO gameDO =
                quizGameService.createGame(chatroomDTO.getRoomid(), bonus, quizGameService.getDefaultQuestions());

        // 设置房间信息
        QuizRoomDO roomDO = new QuizRoomDO();
        roomDO.setDeviceId(deviceId);
        roomDO.setGameId(gameDO.getId());
        roomDO.setGameStatus(GameStatusEnum.INIT.getValue());

        roomDO.setCreator(nimUserDTO.getAccid());
        roomDO.setCreatorToken(nimUserDTO.getToken());

        roomDO.setRoomId(chatroomDTO.getRoomid());
        roomDO.setName(chatroomDTO.getName());

        roomDO.setCid(channelInfoDTO.getCid());
        roomDO.setPushUrl(channelInfoDTO.getPushUrl());
        roomDO.setRtmpPullUrl(channelInfoDTO.getRtmpPullUrl());
        roomDO.setHttpPullUrl(channelInfoDTO.getHttpPullUrl());
        roomDO.setHlsPullUrl(channelInfoDTO.getHlsPullUrl());

        quizRoomDao.insertSelective(roomDO);

        return genHostRoomModel(roomDO, gameDO);
    }

    /**
     * 主播重新进入房间.
     *
     * 如果游戏状态为初始，则更新游戏金额，并返回房间信息
     * 如果游戏状态为游戏中，则只返回当前房间信息
     * 如果游戏状态为已结束，则创建新的游戏，并返回房间信息
     *
     * @param roomDO 当前房间信息
     * @param bonus 奖金金额
     * @param ext 扩展字段
     * @return
     */
    public HostRoomModel enterHostRoom(@Nonnull QuizRoomDO roomDO, BigDecimal bonus, String ext) {
        // 更新聊天室扩展字段信息
        if (StringUtils.isNotBlank(ext)) {
            nimServerApiHttpDao.updateRoom(roomDO.getRoomId(), null, null, null, ext);
        }
        QuizGameDO gameDO;
        // 游戏已结束，创建新游戏
        if (Objects.equals(roomDO.getGameStatus(), GameStatusEnum.OVER.getValue())) {
            gameDO = quizGameService.createGame(roomDO.getRoomId(), bonus, quizGameService.getDefaultQuestions());
            // 创建游戏后，需要重置房间状态
            roomDO.setGameId(gameDO.getId());
            roomDO.setGameStatus(GameStatusEnum.INIT.getValue());

            // 开启聊天室
            nimServerApiHttpDao.changeRoomStatus(roomDO.getRoomId(), roomDO.getCreator(), true);

            QuizRoomDO updateDO = new QuizRoomDO();
            updateDO.setId(roomDO.getId());
            updateDO.setGameId(roomDO.getGameId());
            updateDO.setGameStatus(roomDO.getGameStatus());
            quizRoomDao.updateByPrimaryKeySelective(updateDO);
        } else {
            gameDO = quizGameService.queryGameById(roomDO.getGameId());
        }

        // 如果游戏还未开始，更新奖金金额
        if (Objects.equals(roomDO.getGameStatus(), GameStatusEnum.INIT.getValue())) {
            gameDO.setBonus(bonus);
            quizGameService.updateBonus(gameDO.getId(), bonus);
        }

        return genHostRoomModel(roomDO, gameDO);
    }

    /**
     * 根据deviceId查询房间
     *
     * @param deviceId 主播端设备号
     * @return
     */
    public QuizRoomDO queryRoomByDeviceId(String deviceId) {
        return quizRoomDao.findByDeviceId(deviceId);
    }

    /**
     * 根据roomId查询房间
     *
     * @param roomId 房间id
     * @return
     */
    public QuizRoomDO queryRoomByRoomId(long roomId) {
        return quizRoomDao.findByRoomId(roomId);
    }

    /**
     * 变更游戏状态
     *
     * @param roomId 房间号
     * @param operator 操作人
     * @param gameStatus 游戏状态
     * @return
     */
    public boolean changeGameStatus(long roomId, String operator, @Nonnull GameStatusEnum gameStatus) {
        boolean result = quizRoomDao.updateGameStatus(roomId, gameStatus.getValue()) > 0;
        if (result && gameStatus == GameStatusEnum.OVER) {
            nimServerApiHttpDao.changeRoomStatus(roomId, operator, false);
        }
        return result;
    }

    /**
     * 通过聊天室消息公布结果
     *
     * @param roomDO 房间信息
     * @param content 消息内容
     * @return
     */
    public boolean publishResult(QuizRoomDO roomDO, String content) {
        NimResponseDTO responseDTO =
                nimServerApiHttpDao.sendChatroomMsg(roomDO.getRoomId(), content, roomDO.getCreator(), true);
        if (responseDTO.isSuccess()) {
            return true;
        } else {
            logger.error("quiz publish result failed, cause {}", responseDTO);
            throw new QuizException(responseDTO.getCode(), responseDTO.getDesc());
        }
    }

    /**
     * 观众查询游戏房间信息
     *
     * @param roomId 房间id
     * @param accid 观众accid
     * @param addressType link地址类型
     * @return
     */
    public PlayerRoomModel queryPlayerRoom(long roomId, String accid, RoomAddressTypeEnum addressType) {
        QuizRoomDO roomDO = quizRoomDao.findByRoomId(roomId);
        if (roomDO == null) {
            return null;
        }

        if (Objects.equals(roomDO.getGameStatus(), GameStatusEnum.OVER.getValue())) {
            throw new QuizException(HttpCodeEnum.GAME_OVER);
        }

        QuizGameDO gameDO = quizGameService.queryGameById(roomDO.getGameId());

        return genPlayerRoomModel(roomDO, gameDO, accid, addressType);
    }

    /**
     * 组合聊天室和游戏信息，返回主播端房间信息
     *
     * @param roomDO 聊天室信息
     * @param gameDO 游戏信息
     * @return
     */
    private HostRoomModel genHostRoomModel(QuizRoomDO roomDO, QuizGameDO gameDO) {
        HostRoomModel hostRoomModel = new HostRoomModel();
        hostRoomModel.setRoomId(roomDO.getRoomId());
        hostRoomModel.setName(roomDO.getName());
        hostRoomModel.setCreator(roomDO.getCreator());
        hostRoomModel.setGameStatus(roomDO.getGameStatus());
        hostRoomModel.setPushUrl(roomDO.getPushUrl());
        hostRoomModel.setRtmpPullUrl(roomDO.getRtmpPullUrl());
        hostRoomModel.setHttpPullUrl(roomDO.getHttpPullUrl());
        hostRoomModel.setHlsPullUrl(roomDO.getHlsPullUrl());

        hostRoomModel.setGameId(gameDO.getId());
        hostRoomModel.setBonus(gameDO.getBonus());

        List<QuestionModel> questionModels = quizQuestionService.queryQuestionsByOrder(gameDO.getQuestions());
        hostRoomModel.setQuestionCount(questionModels.size());
        hostRoomModel.setQuestionInfo(questionModels);

        return hostRoomModel;
    }

    /**
     * 组合聊天室信息和游戏信息，返回玩家端房间信息，包括查询房间在线人数和直播状态
     *
     * @param roomDO 聊天室信息
     * @param gameDO 游戏信息
     * @param accid 玩家账号
     * @param addressType 聊天室link地址类型
     * @return
     */
    private PlayerRoomModel genPlayerRoomModel(QuizRoomDO roomDO, QuizGameDO gameDO, String accid,
            RoomAddressTypeEnum addressType) {
        PlayerRoomModel playerRoomModel = new PlayerRoomModel();
        playerRoomModel.setRoomId(roomDO.getRoomId());
        playerRoomModel.setName(roomDO.getName());
        playerRoomModel.setCreator(roomDO.getCreator());
        playerRoomModel.setGameStatus(roomDO.getGameStatus());
        playerRoomModel.setRtmpPullUrl(roomDO.getRtmpPullUrl());
        playerRoomModel.setHttpPullUrl(roomDO.getHttpPullUrl());
        playerRoomModel.setHlsPullUrl(roomDO.getHlsPullUrl());

        playerRoomModel.setGameId(gameDO.getId());
        playerRoomModel.setBonus(gameDO.getBonus());
        playerRoomModel.setQuestionCount(CollectionUtils.size(gameDO.getQuestions()));

        // 查询在线人数
        ChatroomResponseDTO chatroomResponseDTO = nimServerApiHttpDao.queryRoomInfo(roomDO.getRoomId(), true);
        if (!chatroomResponseDTO.isSuccess()) {
            logger.error("query chatroom onlineUserCount failed, roomId {} cause {}", roomDO.getRoomId(),
                    chatroomResponseDTO);
            playerRoomModel.setOnlineUserCount(0L);
        } else {
            ChatroomDTO chatroomDTO = chatroomResponseDTO.getChatroom();
            playerRoomModel.setOnlineUserCount(chatroomDTO.getOnlineusercount());
            playerRoomModel.setRoomStatus(chatroomDTO.getValid());
        }

        // 查询直播状态
        VCloudResponseDTO<ChannelStatusDTO> liveResponseDTO = vcloudServerApiHttpDao.channelStats(roomDO.getCid());
        if (!liveResponseDTO.isSuccess()) {
            logger.error("query live status failed, roomId {} cid {} cause {}", roomDO.getRoomId(), roomDO.getCid(),
                    liveResponseDTO);
            playerRoomModel.setLiveStatus(LiveStatusEnum.IDLE.getValue());
        } else {
            ChannelStatusDTO statusDTO = liveResponseDTO.getRet();
            playerRoomModel.setLiveStatus(statusDTO.getStatus());
        }

        // 查询聊天室link地址
        AddrResponseDTO addrResponseDTO =
                nimServerApiHttpDao.requestRoomAddress(roomDO.getRoomId(), accid, addressType);
        if (!addrResponseDTO.isSuccess()) {
            logger.error("query chatroom link address failed, roomId {} cause {}", roomDO.getRoomId(), addrResponseDTO);
            throw new QuizException("获取聊天室地址失败");
        } else {
            playerRoomModel.setAddr(addrResponseDTO.getAddr());
        }

        return playerRoomModel;
    }
}