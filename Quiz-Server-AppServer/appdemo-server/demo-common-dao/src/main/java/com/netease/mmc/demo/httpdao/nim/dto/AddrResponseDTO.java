package com.netease.mmc.demo.httpdao.nim.dto;

import java.util.List;

/**
 * 聊天室地址DTO.
 *
 * @author hzwanglin1
 * @date 2017/6/24
 * @since 1.0
 */
public class AddrResponseDTO extends NimResponseDTO{
    /**
     * 聊天室地址
     */
    private List<String> addr;

    public List<String> getAddr() {
        return addr;
    }

    public void setAddr(List<String> addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddrResponseDTO{");
        sb.append("addr=").append(addr);
        sb.append(", code=").append(getCode());
        sb.append(", desc='").append(getDesc()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
