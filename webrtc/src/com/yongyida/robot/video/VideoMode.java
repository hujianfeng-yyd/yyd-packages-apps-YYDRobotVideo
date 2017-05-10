/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

import com.yongyida.robot.video.comm.log;

/**
 * 视频模式枚举
 *
 */
public enum VideoMode {
	NONE("none"), MEETING("meeting"), VIDEO("video"), MONITOR("monitor");

	private String mValue;

	private VideoMode(String value) {
		mValue = value;
	}

	/**
	 * 根据字符串返回视频模式权举值
	 * 
	 * @param strMode
	 *            视频模式字符串
	 * @return VideoMode 视频模式权举
	 */
	public static VideoMode getVideoMode(String strMode) {
		if ("meeting".equals(strMode)) {
			return MEETING;
		}
		else if ("video".equals(strMode)) {
			return VIDEO;
		}
		else if ("monitor".equals(strMode)) {
			return MONITOR;
		}
		else {
			log.e("VideoMode", "Error video mode: " + strMode);
			return NONE;
		}
	}

	@Override
	public String toString() {
		return mValue;
	}
}
