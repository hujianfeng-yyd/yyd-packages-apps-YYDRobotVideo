/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.receiver;

import com.yongyida.robot.video.comm.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 机器人广播接收
 *
 */
public class RobotBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "RobotBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		log.d(TAG, "receive action: " + action);
	}
}
