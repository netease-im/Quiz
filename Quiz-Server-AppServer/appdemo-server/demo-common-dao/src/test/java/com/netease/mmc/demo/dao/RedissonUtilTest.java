package com.netease.mmc.demo.dao;

import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.util.RedissonUtil;

/**
 * Redisson测试类.
 *
 * @author hzwanglin1
 * @date 2017/6/28
 * @since 1.0
 */
public class RedissonUtilTest extends BaseDAOTest{

    @Test
    public void setTest() throws Exception {
        String key = "testKey";
        System.out.println(RedissonUtil.get(key));
        System.out.println(RedissonUtil.incrby(key,6));
        RedissonUtil.set(key, 10);
        System.out.println(RedissonUtil.get(key));
        System.out.println(RedissonUtil.exists(key));
        RedissonUtil.del(key);
        System.out.println(RedissonUtil.exists(key));
        System.out.println(RedissonUtil.decrby(key,3));
        System.out.println(RedissonUtil.exists(key));
        RedissonUtil.del(key);
        System.out.println(RedissonUtil.llen(RedisKeys.QUEUE_TOURIST_KEY));
    }

    @Test
    public void setListTest() {
        List<Long> longs = Lists.newArrayList();
        longs.add(1991L);
        longs.add(8L);
        longs.add(25L);

        RedissonUtil.set("list_test", JSON.toJSONString(longs));

        List<Long> list_test = JSON.parseArray((String)RedissonUtil.get("list_test"), Long.class);
        System.out.println(list_test);
    }
}
