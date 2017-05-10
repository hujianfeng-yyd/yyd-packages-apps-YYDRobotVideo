/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.service;

import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.IVideoListener;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.hxvideo.HxVideoListener;
import com.yongyida.robot.video.hxvideo.VideoCallActivity;
import com.yongyida.robot.video.utils.log;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

/**
 * 机器人视频聊天服务
 * 
 */
public class RobotVideoService extends Service {
	private static final String TAG = RobotVideoService.class.getSimpleName();
	private IVideoListener mVideoListener;
	private IVideoListener mWifiDirectListener;

	@Override
	public void onCreate() {
		super.onCreate();
		log.d(TAG, "onCreate");
		
		mVideoListener = new HxVideoListener(getApplicationContext());
		mVideoListener.open();
		
		// 提高服务优先级
	    improvePriority();
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	private void improvePriority() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// 我们并不需要为 notification.flags 设置 FLAG_ONGOING_EVENT，因为
		// 前台服务的 notification.flags 总是默认包含了那个标志位
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Foreground Service Started.", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, RobotVideoService.class), 0);
		notification.setLatestEventInfo(this, "Foreground Service",
				"Foreground Service Started.", contentIntent);
		// 注意使用 startForeground, id 为 0 将不会显示 notification
		startForeground(0, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log.d(TAG, "onStartCommand");
		int ret = START_STICKY;
		if (intent == null) {
			return ret;
		}

		String user = intent.getStringExtra(Constant.GLOBAL_BROADCAST_ROBOT_USER_EXTRA);
		int angle = intent.getIntExtra(Constant.GLOBAL_BROADCAST_ROBOT_ANGLE_EXTRA, 0);
		if (!TextUtils.isEmpty(user)) {
			startActivity(new Intent(this, VideoCallActivity.class)
					.putExtra("username", user)
					.putExtra("rotateangle", angle)
					.putExtra("isComingCall", false)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		
		return ret;
	}

	@Override
	public IBinder onBind(Intent intent) {
		log.d(TAG, "onBind");
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		log.d(TAG, "onDestroy");
		
		if (mVideoListener != null) {
			mVideoListener.close();
		}
		
		if (mWifiDirectListener != null) {
			mWifiDirectListener.close();
		}
		
		stopForeground(true);
	}

}
