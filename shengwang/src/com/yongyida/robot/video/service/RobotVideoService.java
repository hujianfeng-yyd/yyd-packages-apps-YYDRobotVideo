/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.service;

import com.yongyida.robot.video.IVideoListener;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.agora.AgoraVideoListener;
import com.yongyida.robot.video.comm.log;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * 机器人视频聊天服务
 * 
 */
public class RobotVideoService extends Service {
	private static final String TAG = "RobotVideoService";
	private IVideoListener mVideoListener;

	@Override
	public void onCreate() {
		super.onCreate();
		log.d(TAG, "onCreate");
		
		mVideoListener = new AgoraVideoListener(getApplicationContext());
		mVideoListener.open();
		
		// 提高服务优先级
	    improvePriority();
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	private void improvePriority() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Foreground Service Started.", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, RobotVideoService.class), 0);
		notification.setLatestEventInfo(this, "Foreground Service",
				"Foreground Service Started.", contentIntent);
		// id为 0 不显示 notification
		startForeground(0, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log.d(TAG, "onStartCommand");
		return START_STICKY;
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
		
		stopForeground(true);
	}

}
