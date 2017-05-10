package com.yongyida.robot.video;

import android.content.Context;

import java.util.Date;

import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;

public class WebRtcSDKHelper {
	private static final String TAG = "WebRtcSDKHelper";
	private static WebRtcSDKHelper sInstance;

	@SuppressWarnings("unused")
	private Context mContext;
	private VideoMode mVideoMode = VideoMode.NONE;
	private String mRoomId;
	private String mClientId;
	private String mCallNumber;
	private boolean mHaveNewFriend;
	private boolean mHaveNewHistory;
	
	private WebRtcSDKHelper() {
	}

	public static synchronized WebRtcSDKHelper getInstance() {
		if (sInstance == null) {
			sInstance = new WebRtcSDKHelper();
		}
		return sInstance;
	}

	public synchronized void init(Context ctx) {
		log.d(TAG, "init()");
		mContext = ctx;
	}

	public VideoMode getVideoMode() {
		return mVideoMode;
	}

	public void setVideoMode(VideoMode videoMode) {
		mVideoMode = videoMode;
	}
	
	public String generateRoomId() {
		mRoomId = Long.toString(Robot.getInstance().getRid()) + "_" + Utils.getTimeString("MMddHHmmss", new Date());
		return mRoomId;
	}

	public String getmRoomId() {
		return mRoomId;
	}

	public void setRoomId(String roomId) {
		mRoomId = roomId;
	}

	public String getClientId() {
		return mClientId;
	}

	public void setmClientId(String clientId) {
		mClientId = clientId;
	}

	public String getCallNumber() {
		return mCallNumber;
	}

	public void setCallNumber(String callNumber) {
		mCallNumber = callNumber;
	}

	public boolean haveNewFriend() {
		return mHaveNewFriend;
	}

	public void setHaveNewFriend(boolean haveNewFriend) {
		mHaveNewFriend = haveNewFriend;
	}

	public boolean haveNewHistory() {
		return mHaveNewHistory;
	}

	public void setHaveNewHistory(boolean haveNewHistory) {
		mHaveNewHistory = haveNewHistory;
	}
	
}
