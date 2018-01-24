package com.netease.mmc.demo.common.constant;

/**
 * 声明redis key.
 *
 * @author hzwanglin1
 * @date 2017/6/28
 * @since 1.0
 */
public class RedisKeys {
    private RedisKeys() {
        throw new UnsupportedOperationException("RedisKeys.class can not be construct to a instance");
    }

    /**
     * 记录IP当天获取游客账户总次数 %s->ip
     */
    public static final String TOURIST_GET_IP_COUNT_TODAY = "TOURIST_GET_IP_COUNT_TODAY_%s";
    
    /**
     * 记录游客账户有效期 %s->accid
     */
    public static final String TOURIST_ACCOUNT_USED = "TOURIST_ACCOUNT_USED_%s";
    
    /**
     * 当前游客可用账户队列key值
     */
    public static final String QUEUE_TOURIST_KEY = "QUEUE_TOURIST_KEY";
    
    /**
     * 为游客队列增加的线程锁设置的竞争锁
     */
    public static final String QUEUE_ADD_TOURIST_LOCK = "QUEUE_ADD_TOURIST_LOCK";

    /**
     * 竞答房间默认题目列表，存储的是题目id列表的jsonArray
     */
    public static final String DEFAULT_QUIZ_QUESTIONS_KEY = "default_quiz_questions";

    /**
     * 问答游戏主播端密码key，roomId + gameId
     */
    public static final String QUIZ_GAME_PASSWORD = "quiz_game_password_%s_%s";

    /**
     * 答题key，roomId + gameId，值存储的是当前回答的题目
     */
    public static final String QUIZ_GETTING_ANSWER = "quiz_getting_answer_%s_%s";

    /**
     * 问答答案缓存key，questionId，存储内容为选项id列表，列表第一位存储正确答案
     */
    public static final String QUIZ_QUESTION_ANSWER = "quiz_question_answer_%s";

    /**
     * 某局游戏某一题已答题的accid集合，用于防止重复答题，gameId + questionId
     */
    public static final String QUIZ_QUESTION_SUBMIT_ACCID_SET = "quiz_question_submit_accid_set_%s_%s";

    /**
     * 游戏存活玩家map，map存储accid->答对次数，gameId
     */
    public static final String QUIZ_GAME_WINNER_MAP = "quiz_game_winner_map_%s";

    /**
     * 主播创建游戏频控key，device_id
     */
    public static final String QUIZ_CREATE_GAME_FREQ_LIMIT = "quiz_create_game_freq_limit_%s";
}
