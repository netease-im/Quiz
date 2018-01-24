package com.netease.mmc.demo.httpdao.nim.dto;

import java.util.Objects;

import com.netease.mmc.demo.common.enums.HttpCodeEnum;

/**
 * 云信Server Api接口返回值DTO.
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public class NimResponseDTO {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 错误描述
     */
    private String desc;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 判断api返回值是否为200
     *
     * @return
     */
    public boolean isSuccess() {
        return Objects.equals(code, HttpCodeEnum.OK.value());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NimResponseDTO{");
        sb.append("code=").append(code);
        sb.append(", desc='").append(desc).append('\'');
        sb.append('}');
        return sb.toString();
    }
}