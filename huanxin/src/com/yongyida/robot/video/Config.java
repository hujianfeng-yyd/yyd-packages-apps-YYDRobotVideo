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

import com.yongyida.robot.video.utils.log;

public class Config {

	private static final String TAG = Config.class.getSimpleName();
	
	private static final String PREFERENCE_NAME = "setting";
	private final static String SHARED_KEY_ROBOT_ISREGISTED = "shared_key_setting_robot_isregisted";
	private final static String SHARED_KEY_ROBOT_ID = "shared_key_setting_robot_id";
	private final static String SHARED_KEY_ROBOT_SERIALNUMBER = "shared_key_setting_robot_serialnumber";
	private final static String SHARED_KEY_NETCONNECT_TIMEOUT = "shared_key_setting_netconnect_timeout";
	private final static String SHARED_KEY_ROBOTLOGIN_TIMEOUT = "shared_key_setting_robotlogin_timeout";
	private final static String SHARED_KEY_HXREGISTER_TIMEOUT = "shared_key_setting_hxregister_timeout";
	private final static String SHARED_KEY_HXLOGIN_TIMEOUT = "shared_key_setting_hxlogin_timeout";
	private final static String SHARED_KEY_LASTCHAT_USERANME = "shared_key_setting_lastchat_username";
	private final static String SHARED_KEY_PREVIEWSIZE_WIDTH = "shared_key_setting_previewsize_width";
	private final static String SHARED_KEY_PREVIEWSIZE_HEIGHT = "shared_key_setting_previewsize_height";
	private final static String SHARED_KEY_PICTURESIZE_WIDTH = "shared_key_setting_picturesize_width";
	private final static String SHARED_KEY_PICTURESIZE_HEIGHT = "shared_key_setting_picturesize_height";
	private final static String SHARED_KEY_VIDEOMODE = "shared_key_setting_videomode";
	
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
		log.d(TAG, "Preference init()");

		mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		return true;
	}

	public boolean getRobotIsRegisted() {
		return mSharedPreferences.getBoolean(SHARED_KEY_ROBOT_ISREGISTED, false);
	}

	public void setRobotIsRegisted(boolean registed) {
		mEditor.putBoolean(SHARED_KEY_ROBOT_ISREGISTED, registed);
		mEditor.commit();
	}
	
	public String getRobotId() {
		return mSharedPreferences.getString(SHARED_KEY_ROBOT_ID, "");
	}

	public void setRobotId(String id) {
		mEditor.putString(SHARED_KEY_ROBOT_ID, id);
		mEditor.commit();
	}

	public String getRobotSerialNumber() {
		return mSharedPreferences.getString(SHARED_KEY_ROBOT_SERIALNUMBER, "");
	}

	public void setRobotSerialNumber(String username) {
		mEditor.putString(SHARED_KEY_ROBOT_SERIALNUMBER, username);
		mEditor.commit();
	}

	public int getNetConnectTimeout() {
		return mSharedPreferences.getInt(SHARED_KEY_NETCONNECT_TIMEOUT, 5000);
	}

	public void setNetConnectTimeout(int timeout) {
		mEditor.putInt(SHARED_KEY_NETCONNECT_TIMEOUT, timeout);
		mEditor.commit();
	}

	public int getRobotLoginTimeout() {
		return mSharedPreferences.getInt(SHARED_KEY_ROBOTLOGIN_TIMEOUT, 5000);
	}

	public void setRobotLoginTimeout(int timeout) {
		mEditor.putInt(SHARED_KEY_ROBOTLOGIN_TIMEOUT, timeout);
		mEditor.commit();
	}

	public int getHxRegisterTimeout() {
		return mSharedPreferences.getInt(SHARED_KEY_HXREGISTER_TIMEOUT, 5000);
	}

	public void setHxRegisterTimeout(int timeout) {
		mEditor.putInt(SHARED_KEY_HXREGISTER_TIMEOUT, timeout);
		mEditor.commit();
	}

	public int getHxLoginTimeout() {
		return mSharedPreferences.getInt(SHARED_KEY_HXLOGIN_TIMEOUT, 5000);
	}

	public void setHxLoginTimeout(int timeout) {
		mEditor.putInt(SHARED_KEY_HXLOGIN_TIMEOUT, timeout);
		mEditor.commit();
	}

	public String getLastChatUserName() {
		return mSharedPreferences.getString(SHARED_KEY_LASTCHAT_USERANME, "");
	}

	public void setLastChatUserName(String userName) {
		mEditor.putString(SHARED_KEY_LASTCHAT_USERANME, userName);
		mEditor.commit();
	}

	public int getPreviewSizeWidth() {
		return mSharedPreferences.getInt(SHARED_KEY_PREVIEWSIZE_WIDTH, 864);
	}

	public void setPreviewSizeWidth(int size) {
		mEditor.putInt(SHARED_KEY_PREVIEWSIZE_WIDTH, size);
		mEditor.commit();
	}

	public int getPreviewSizeHeight() {
		return mSharedPreferences.getInt(SHARED_KEY_PREVIEWSIZE_HEIGHT, 480);
	}

	public void setPreviewSizeHeight(int size) {
		mEditor.putInt(SHARED_KEY_PREVIEWSIZE_HEIGHT, size);
		mEditor.commit();
	}

	public int getPictureSizeWidth() {
		return mSharedPreferences.getInt(SHARED_KEY_PICTURESIZE_WIDTH, 2560);
	}

	public void setPictureSizeWidth(int size) {
		mEditor.putInt(SHARED_KEY_PICTURESIZE_WIDTH, size);
		mEditor.commit();
	}

	public int getPictureSizeHeight() {
		return mSharedPreferences.getInt(SHARED_KEY_PICTURESIZE_HEIGHT, 1440);
	}

	public void setPictureSizeHeight(int size) {
		mEditor.putInt(SHARED_KEY_PICTURESIZE_HEIGHT, size);
		mEditor.commit();
	}
	
	public String getVideoMode() {
		return mSharedPreferences.getString(SHARED_KEY_VIDEOMODE, "none");
	}
	
	public void setVideoMode(String videoMode) {
		mEditor.putString(SHARED_KEY_VIDEOMODE, videoMode);
		mEditor.commit();
	}

	public void removeLoginedInfo() {
		mEditor.remove(SHARED_KEY_ROBOT_ISREGISTED);
		mEditor.remove(SHARED_KEY_ROBOT_SERIALNUMBER);
		mEditor.remove(SHARED_KEY_PICTURESIZE_WIDTH);
		mEditor.remove(SHARED_KEY_PICTURESIZE_HEIGHT);
		mEditor.commit();
	}

}
