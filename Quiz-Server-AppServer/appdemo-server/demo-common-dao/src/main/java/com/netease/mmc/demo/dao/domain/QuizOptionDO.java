package com.netease.mmc.demo.dao.domain;

/**
 * 回答选项DO.
 *
 * @author hzwanglin1
 * @date 2018/1/13
 * @since 1.0
 */
public class QuizOptionDO {
    /**
     * 选项id
     */
    private Integer optionId;

    /**
     * 选项内容
     */
    private String content;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuizOptionDO{");
        sb.append("optionId=").append(optionId);
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}