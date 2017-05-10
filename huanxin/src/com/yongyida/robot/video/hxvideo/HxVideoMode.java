/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.hxvideo;

import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.utils.log;

/**
 * 视频模式枚举
 *
 */
public enum HxVideoMode {
	NONE("none"),
	CHAT(Constant.VIDCEOMODE_CHAT),
	MONITOR(Constant.VIDCEOMODE_MONITOR);
	
	private String mValue;

	private HxVideoMode(String value) {
		mValue = value;
	}
	
	/**
	 * 根据字符串返回视频模式权举值
	 * @param strMode 视频模式字符串
	 * @return VideoMode 视频模式权举
	 */
	public static HxVideoMode getVideoMode(String strMode) {
		if (Constant.VIDCEOMODE_CHAT.equalsIgnoreCase(strMode)) {
			return CHAT;
		}
		else if (Constant.VIDCEOMODE_MONITOR.equalsIgnoreCase(strMode)) {
			return MONITOR;
		}
		else  {
			log.e("VideoMode", "Error video mode: " + strMode);
			return MONITOR;
		}
	}
	
	@Override
	public String toString() {
		return mValue;
	}
}
