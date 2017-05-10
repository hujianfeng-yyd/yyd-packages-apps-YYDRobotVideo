/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-25
 * 
 */
package com.yongyida.robot.video;

/**
 * 视频监听器接口
 *
 */
public interface IVideoListener {
	public boolean open();

	public void close();
}
