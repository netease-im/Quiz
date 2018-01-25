package com.netease.mmc.demo.common.exception;

import com.netease.mmc.demo.common.enums.HttpCodeEnum;

/**
 * 直播竞答相关异常
 *
 * @author hzwanglin1
 * @date 2018/1/14
 * @since 1.0
 */
public class QuizException extends AbstractCustomException{

	private static final long serialVersionUID = -89131975089238959L;

	private int res = HttpCodeEnum.QUIZ_ERROR.value();

    public QuizException() {
    	super(HttpCodeEnum.QUIZ_ERROR.getReasonPhrase());
	}

	public QuizException(String msg){
		super(msg);
	}

	public QuizException(HttpCodeEnum code){
		this(code.value(), code.getReasonPhrase());
	}

	public QuizException(int res, String msg){
		super(msg);
		this.res = res;
	}
	
	public int getRes(){
		return res;
	}
}
