/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.receiver;

import com.yongyida.robot.video.NetWork;
import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.comm.NetType;
import com.yongyida.robot.video.comm.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * 网络改变广播接收
 *
 */
public class NetworkConnectReceiver extends BroadcastReceiver {
	private static final String TAG = "NetworkConnectReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		log.i(TAG, "receive action: " + action);

		// 接收网络连接改变
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			NetType oldNetType = NetWork.getInstance().getNetType();
			NetType newNetType = NetWork.getInstance().getCurrentNetWork();

			if (newNetType != NetType.NETTYPE_NONE) {
				if (newNetType != oldNetType) {
					log.d(TAG,
							"NetType from: " + oldNetType + ", change to: " + newNetType + ", will reStartService()");
					RobotApplication.getInstance().reStartService();
				}
				else {
					log.d(TAG, "NewNetType: " + newNetType + ", OldNetType: " + oldNetType);
				}
			}
			else {
				if (newNetType != oldNetType) {
					log.d(TAG, "NetType from: " + oldNetType + ", change to: " + newNetType + ", will stopService()");
					//RobotApplication.getInstance().stopService();
				}
				else {
					log.d(TAG, "NewNetType: " + newNetType + ", OldNetType: " + oldNetType);
				}
			}
		}

	}
}
