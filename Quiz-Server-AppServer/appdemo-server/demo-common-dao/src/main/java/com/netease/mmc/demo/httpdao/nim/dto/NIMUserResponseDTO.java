package com.netease.mmc.demo.httpdao.nim.dto;

/**
 * 用户信息接口返回值DTO.
 *
 * @author hzwanglin1
 * @date 17-6-25
 * @since 1.0
 */
public class NIMUserResponseDTO extends NimResponseDTO{
    /**
     * 用户账号相关信息
     */
    private NIMUserDTO info;

    public NIMUserDTO getInfo() {
        return info;
    }

    public void setInfo(NIMUserDTO info) {
        this.info = info;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NIMUserResponseDTO{");
        sb.append("info=").append(info);
        sb.append(", code=").append(getCode());
        sb.append(", desc='").append(getDesc()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
