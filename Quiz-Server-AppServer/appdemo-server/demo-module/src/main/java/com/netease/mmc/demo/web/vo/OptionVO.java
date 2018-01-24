package com.netease.mmc.demo.web.vo;

/**
 * 回答选项VO.
 *
 * @author hzwanglin1
 * @date 2018/1/13
 * @since 1.0
 */
public class OptionVO {
    /**
     * 选项id
     */
    private Integer optionId;

    /**
     * 选项内容
     */
    private String content;

    /**
     * 选择人数
     */
    private Long selectCount;

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(Long selectCount) {
        this.selectCount = selectCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OptionModel{");
        sb.append("optionId=").append(optionId);
        sb.append(", content='").append(content).append('\'');
        sb.append(", selectCount=").append(selectCount);
        sb.append('}');
        return sb.toString();
    }
}