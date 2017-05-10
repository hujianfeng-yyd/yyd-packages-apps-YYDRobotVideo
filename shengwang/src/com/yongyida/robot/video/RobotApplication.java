/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

import java.util.Locale;

import com.yongyida.robot.video.activity.ActivitySplash;
import com.yongyida.robot.video.agora.AgoraSDKHelper;
import com.yongyida.robot.video.comm.ActivityCollector;
import com.yongyida.robot.video.comm.NetType;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.service.RobotVideoService;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;

/**
 * 应用程序
 * 
 */
public class RobotApplication extends Application {
	private static final String TAG = RobotApplication.class.getSimpleName();
	private static RobotApplication sInstance;
	
	private Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		sInstance = this;
		
		// 日志初始化
		log.setLogLevel(log.LOG_DEBUG);
		log.i(TAG, "RobotApplication onCreate()");
		log.i(TAG, "RobotApplication " + Utils.getVersionInfo(mContext));
		
		// 挂载全局异常处理
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());
		
		// Robot初始化
	    Robot.getInstance().init(mContext);
	    
		// 配置初始化
		Config.getInstance().init(mContext);
		
		// 网络初始化
	  	NetWork.getInstance().init(mContext);
	  	
	    // 视频SDK初始化
	  	YYDSDKHelper.getInstance().init(mContext);
	  	AgoraSDKHelper.getInstance().init(mContext);
	  	
		// 视频状态初始化
		ConfigProvider.save(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "false");
	    log.d(TAG, "video status: " + isVideoing());
	    
	    // 启动服务
		startService();
	}
	
	/**
	 * 返回RobotApplication实例。
	 * 
	 */
	public static RobotApplication getInstance() {
		return sInstance;
	}
	
	/**
	 * 返回应用程序 的上下文
	 *
	 */
	public Context getContext() {
		return mContext;
	}
	
	/**
	 * 返回应用程序使用的语言配置
	 *
	 */
	public Locale getLocale() {
		if (mContext != null) {
			return mContext.getResources().getConfiguration().locale;
		}
		else {
			return Locale.getDefault();
		}
	}
	
	/**
	 * 检测环境错误
	 * @param
	 * @return
	 *
	 */
	public String checkEnvError() {
		String strError = null;
		
		// 检测机器人Id
		try {
			String robotId = Robot.readRobotId(mContext);
			if (TextUtils.isEmpty(robotId)) {
				strError = mContext.getString(R.string.robot_id_error);
			}
		}
		catch (Exception e) {
			log.e(TAG, "Read robot Id exception: " + e);
			strError = mContext.getString(R.string.read_robot_id_error);
		}
		
		// 检测网络
		if (NetWork.getInstance().getCurrentNetWork().value() < NetType.NETTYPE_4G.value()) {
			strError = mContext.getString(R.string.not_network_error);
		}
		
		if (strError != null) {
			Utils.toast(mContext, strError);
		}
		
		return strError;
	}
	
	/**
	 * 启动服务
	 *
	 */
	public void startService() {
		log.i(TAG, "startService()");
		
		if (NetWork.getInstance().getCurrentNetWork().value() >= NetType.NETTYPE_4G.value()) {
			// 启动视频服务
			mContext.startService(new Intent(mContext, RobotVideoService.class));
		}
		else {
			log.e(TAG, "RobotVideoService is not start, because NetType=" + NetWork.getInstance().getNetType());
		}
	}

	/**
	 * 停止服务
	 *
	 */
	public void stopService() {
		log.i(TAG, "stopService");
		
	    // 停止视频服务
		mContext.stopService(new Intent(mContext, RobotVideoService.class));
	}
	
	/**
	 * 重启服务
	 *
	 */
	public void reStartService() {
		log.i(TAG, "reStartService");
		
		// 停止服务
		stopService();
		
	    // 3秒后重启服务
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// 启动服务
				startService();
			}
		}, 3000);
	}

	/**
	 * 重启应用
	 *
	 */
	public void restart(int mils) {
		log.i(TAG, "restart()");

		// 建立定时启动任务到Android系统
		Intent intent = new Intent(mContext, ActivitySplash.class);
		PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + mils, restartIntent);

		// 退出
		exit();
	}

	/**
	 * 退出应用
	 *
	 */
	public void exit() {
		log.i(TAG, "exit()");

		ActivityCollector.finishAll();
		stopService();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
	
	/**
	 * 查询机器人是否处于视频状态
	 * @return true, 正在视频中
	 *         false, 非 视频中
	 *         
	 */
	public boolean isVideoing() {
		String value = null;
		Cursor cursor = mContext.getContentResolver().query(ConfigProvider.CONTENT_URI, 
				null,
                "name = ?",
                new String[] { "videoing" },
                null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				int colIndex = cursor.getColumnIndex("value");
				if (colIndex >= 0) {
					value = cursor.getString(colIndex);
					if (value == null) {
						log.e(TAG, "Value null.");
					}
				}
				else {
					log.e(TAG, "Not found column");
				}
			}
			else {
				log.e(TAG, "Not found record");
			}
		}
		catch (Exception e) {
			log.e(TAG, "Read videoing exception: " + e);
		}
		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		
		return (value != null && value.equals("true"));
	}
	
}
