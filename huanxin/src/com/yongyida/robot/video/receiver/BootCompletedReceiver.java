/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.receiver;

import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.utils.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 启动完成广播接收
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = BootCompletedReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		log.i(TAG, "onReceive() BOOT_COMPLETED");
		
		// 启动服务
		RobotApplication.getInstance().startServices();
	}
}
