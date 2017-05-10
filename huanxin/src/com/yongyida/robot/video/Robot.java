/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-20
 * 
 */
package com.yongyida.robot.video;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.yongyida.robot.video.utils.log;

/**
 * 机器人信息
 *
 */
public class Robot {
	private static final String TAG = Robot.class.getSimpleName();
	
	private static Robot sInstance;
	
	private String mId;
	private String mSid;
	private String mNickName;
	
	private Robot() {
	}
	
	private Robot(String id, String sid) {
		mId = id;
		mSid = sid;
	}
	
	public static synchronized Robot getInstance() {
		if (sInstance == null) {
			sInstance = new Robot();
		}
		return sInstance;
	}
	
	public boolean init(Context ctx) {
		boolean ret = false;
		Uri uri = Uri.parse(Constant.URI_ROBOTID);
		Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);
		
		try {
			if (cursor.moveToFirst()) {
				String id = cursor.getString(cursor.getColumnIndex("id")); //id
				String sid = cursor.getString(cursor.getColumnIndex("sid")); //序列号
				log.i(TAG, "Robot id: " + id + ", sid: " + sid);

				if (!TextUtils.isEmpty(id))
					setId(id.trim());
				
				if (!TextUtils.isEmpty(sid))
					setSid(sid.trim());
				
				ret = true;
			}
		}
		catch (Exception e) {
			log.e(TAG, "Query robot error:" + e.getMessage());
		}
		
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		
		return ret;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setId(String id) {
		mId = id;
	}
	
	public String getSid() {
		return mSid;
	}
	
	public void setSid(String sid) {
		mSid = sid;
	}
	
	public String getNickName() {
		return mNickName;
	}
	
	public void setNickName(String nickName) {
		mNickName = nickName;
	}
}
