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

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;

import com.yongyida.robot.video.hxvideo.HxSDKHelper;
import com.yongyida.robot.video.service.RobotVideoService;
import com.yongyida.robot.video.utils.ActivityCollector;
import com.yongyida.robot.video.utils.CrashHandler;
import com.yongyida.robot.video.utils.NetWork;
import com.yongyida.robot.video.utils.Utils;
import com.yongyida.robot.video.utils.log;

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

		// 环信初始化
		HxSDKHelper.getInstance().init(mContext);
		
		// 设置视频中为false
		ConfigProvider.save(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "false");
	    log.d(TAG, "video status: " + isVideoing());
	    
		// 启动服务
		startServices();
	}
	
	/**
	 * 返回RobotApplication实例。
	 *     
	 */
	public static RobotApplication getInstance() {
		return sInstance;
	}

	/**
	 * 返回应用程序 的上下文。
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
	 * 启动服务
	 *
	 */
	public void startServices() {
		log.d(TAG, "startServices");
		
		// 启动视频服务
	    startService(new Intent(this, RobotVideoService.class));
	}
	
	/**
	 * 停止服务
	 *
	 */
	public void stopService() {
		log.d(TAG, "stopService");

		// 停止视频服务
        stopService(new Intent(this, RobotVideoService.class));
	}
	
	/**
	 * 重启服务
	 *
	 */
	public void reStartService() {
		log.d(TAG, "reStartService");
		
		// 停止服务
		stopService();
		
	    // 1秒后重启服务
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// 启动服务
				startServices();
			}
		}, 3000);
	}

	/**
	 * 退出应用
	 *
	 */
	public void exit() {
		log.d(TAG, "exit()");
		
		ActivityCollector.finishAll();
		stopService();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	/**
	 * 重启应用
	 *
	 */
	public void restart(int mils) {
		log.d(TAG, "restart()");
		
		Intent intent = new Intent(mContext, ActivitySplash.class);
		PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + mils, restartIntent);
		
		exit();
	}
	
	/**
	 * 查询机器人是否处于视频状态
	 * @return true, 正在视频中
	 *         false, 非 视频中
	 *         
	 */
	public boolean isVideoing() {
		String value = null;
		Cursor cursor = getContentResolver().query(ConfigProvider.CONTENT_URI, 
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
