/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.hxvideo;

import com.yongyida.robot.video.IVideoListener;
import com.yongyida.robot.video.Robot;
import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.utils.NetType;
import com.yongyida.robot.video.utils.NetWork;
import com.yongyida.robot.video.utils.log;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

/**
 * 环信聊天监听
 * 
 */
public class HxVideoListener implements IVideoListener {
	private static final String TAG = HxVideoListener.class.getSimpleName();
	private static int RELOGIN_DELAY = 10 * 1000; // 环信重新登录间隔

	public HxVideoListener(Context context) {
	}

	/**
	 * 打开
	 *
	 */
	public boolean open() {
		log.d(TAG, "open()");

		String robotId = Robot.getInstance().getId();
		if (TextUtils.isEmpty(robotId)) {
			log.e(TAG, "Not found robot id");
			return false;
		}
		
		if (NetWork.getInstance().getNetType() == NetType.NETTYPE_NONE) {
			log.e(TAG, "NETTYPE_NONE !");
			return false;
		}
				
		String userName = robotId.toLowerCase(RobotApplication.getInstance().getLocale());
		String passwrod = userName;
		boolean result = HxSDKHelper.getInstance().connect(userName, passwrod);
		if (result) {
			HxSDKHelper.getInstance().startListener();
		}
		else {
			log.e(TAG, "Connect to hxserver failed, will connect after " + RELOGIN_DELAY + " ms.");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					open();
				}
			}, RELOGIN_DELAY);
		}
		
		return result;
	}

	/**
	 * 关闭
	 *
	 */
	public void close() {
		log.d(TAG, "close()");
		HxSDKHelper.getInstance().stopListener();
		HxSDKHelper.getInstance().logout();
		HxSDKHelper.getInstance().uninit();
	}
}
