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

import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.Role;

/**
 * 机器人信息
 *
 */
public class Robot {
	private static final String TAG = "Robot";

	private static Robot sInstance;

	private String mId;
	private String mSid;
	private String mNickName;
	private String mRole = Role.Robot;
	private long mRid;

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
		log.d(TAG, "init()");

		boolean ret = false;
		Uri uri = Uri.parse(Constant.URI_ROBOTID);
		Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);

		try {
			if (cursor != null && cursor.moveToFirst()) {
				String id = cursor.getString(cursor.getColumnIndex("id")); //id
				String sid = cursor.getString(cursor.getColumnIndex("sid")); //序列号
				log.i(TAG, "Robot id: " + id + ", sid: " + sid);

				if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(sid)) {
					setId(id.trim());
					setSid(sid.trim());
					ret = true;
				}
				else {
					Utils.toast(ctx, ctx.getString(R.string.robot_id_error));
				}
			}
			else {
				log.e(TAG, "Read id error, Not found: " + Constant.URI_ROBOTID);
				Utils.toast(ctx, ctx.getString(R.string.read_robot_id_error));
			}
		}
		catch (Exception e) {
			log.e(TAG, "Read id error:" + e.getMessage());
			Utils.toast(ctx, ctx.getString(R.string.robot_id_error));
		}
		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return ret;
	}

	public static String readRobotId(Context ctx) throws Exception {
		Uri uri = Uri.parse(Constant.URI_ROBOTID);
		Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);

		try {
			if (cursor != null && cursor.moveToFirst()) {
				String id = cursor.getString(cursor.getColumnIndex("id"));
				@SuppressWarnings("unused")
				String sid = cursor.getString(cursor.getColumnIndex("sid"));

				if (!TextUtils.isEmpty(id)) {
					return id.trim();
				}
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return null;
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

	public String getRole() {
		return mRole;
	}

	public void setRole(String role) {
		mRole = role;
	}

	public long getRid() {
		return mRid;
	}

	public void setRid(long rid) {
		mRid = rid;
	}
}
