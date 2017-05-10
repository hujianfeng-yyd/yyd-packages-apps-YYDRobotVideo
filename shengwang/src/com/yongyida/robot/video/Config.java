/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co.,Ltd. All rights reserved.
 * 
 * @author: hujianfeng@gmail.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

import android.content.Context;
import android.content.SharedPreferences;

import com.yongyida.robot.video.comm.log;

public class Config {
	public static final String TAG = Config.class.getSimpleName();
	
	public static final String PREFERENCE_NAME = "com.yongyida.robot.video.setting";
	public final static String SHARED_KEY_VIDEOSIZE_WIDTH = "setting_key_videosize_width";
	public final static String SHARED_KEY_VIDEOSIZE_HEIGHT = "setting_key_videosize_height";
	public final static String SHARED_KEY_VIDEO_FPS = "setting_key_video_fps";
	
	private static Config sInstance;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	
	/**
	 *  私有构造
	 */
	private Config() {
		
	}

	/**
	 * 返回PreferenceUtils的单例
	 */
	public static synchronized Config getInstance() {
		if (sInstance == null) {
			sInstance = new Config();
		}
		return sInstance;
	}

	/**
	 * 初始化函数
	 * 
	 */
	public synchronized boolean init(Context context) {
		log.d(TAG, "init()");

		mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		return true;
	}

	public int getVideoSizeWidth() {
		return mSharedPreferences.getInt(SHARED_KEY_VIDEOSIZE_WIDTH, 320);
	}

	public void setVideoSizeWidth(int size) {
		mEditor.putInt(SHARED_KEY_VIDEOSIZE_WIDTH, size);
		mEditor.commit();
	}

	public int getVideoSizeHeight() {
		return mSharedPreferences.getInt(SHARED_KEY_VIDEOSIZE_HEIGHT, 240);
	}

	public void setVideoSizeHeight(int size) {
		mEditor.putInt(SHARED_KEY_VIDEOSIZE_HEIGHT, size);
		mEditor.commit();
	}

	public int getVideoFps() {
		return mSharedPreferences.getInt(SHARED_KEY_VIDEO_FPS, 15);
	}

	public void setVideoFps(int fps) {
		mEditor.putInt(SHARED_KEY_VIDEO_FPS, fps);
		mEditor.commit();
	}
}
