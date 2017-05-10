/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.receiver;

import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.service.RobotVideoService;
import com.yongyida.robot.video.utils.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 机器人广播接收
 *
 */
public class RobotBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = RobotBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		log.d(TAG, "receive action: " + action);
		
		// 接收视频广播（通话语音拨打视频电话）
		if (action.equals(Constant.GLOBAL_BROADCAST_ROBOT_VIDEOCHAT)
				|| action.equals(Constant.GLOBAL_BROADCAST_ROBOT_NORESPONSE)) {
			String user = intent.getStringExtra(Constant.GLOBAL_BROADCAST_ROBOT_USER_EXTRA);
			int angle = intent.getIntExtra(Constant.GLOBAL_BROADCAST_ROBOT_ANGLE_EXTRA, 0);
			log.d(TAG, "Extera user: " + user + ", angle: " + angle);
			
			Intent i = new Intent(context, RobotVideoService.class);
			i.putExtra(Constant.GLOBAL_BROADCAST_ROBOT_USER_EXTRA, user);
			i.putExtra(Constant.GLOBAL_BROADCAST_ROBOT_ANGLE_EXTRA, angle);
			context.startService(i);
		}
	}
}
