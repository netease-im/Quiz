package com.netease.mmc.demo.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.mmc.demo.dao.SeqDao;
import com.netease.mmc.demo.dao.domain.SeqDO;

/**
 * 获取递增序列号.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
@Service
public class SeqService {
    @Resource
    private SeqDao seqDao;

    /**
     * 获取递增序列号
     *
     * @return
     */
    public long getSeqId() {
        SeqDO seqDO = new SeqDO();
        seqDao.insert(seqDO);
        return seqDO.getId();
    }
}