package com.yongyida.robot.video.agora;

import android.content.Context;

import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.YYDSDKHelper;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class AgoraSDKHelper {
	private static final String TAG = "AgoraSDKHelper";
	private static AgoraSDKHelper sInstance;
	
	private Context mContext;
	private RtcEngine mRtcEngine;
    private VideoMode mVideoMode = VideoMode.NONE;
    private String mCallNumber;
    private String mChannelId;
    private String mMeetingId;
	
	private AgoraSDKHelper() {
	}

	public static synchronized AgoraSDKHelper getInstance() {
		if (sInstance == null) {
			sInstance = new AgoraSDKHelper();
		}
		return sInstance;
	}

	public synchronized void init(Context ctx) {
		log.d(TAG, "init()");
		
		mContext = ctx;
	}
	
	public void setRtcEngine(String vendorKey, IRtcEngineEventHandler rtcEngineEventHandler){
        mRtcEngine = RtcEngine.create(mContext, vendorKey, rtcEngineEventHandler);
    }
	
    public RtcEngine getRtcEngine(){
        return mRtcEngine;
    }
    
	public VideoMode getVideoMode() {
		return mVideoMode;
	}

	public void setVideoMode(VideoMode mode) {
		log.d(TAG, "setVideoMode: " + mode);
		mVideoMode = mode;
	}
	
	public String getCallNumber() {
		return mCallNumber;
	}
	
	public static String getCallNumber(String channelId) {
		if (channelId == null) {
			return null;
		}
		
		String[] strs = channelId.split("_");
		if (strs != null && strs.length == 2) {
			return strs[0];
		}
		
		return null;
	}
	
	public static String getMeetingId(String channelId) {
		if (channelId == null) {
			return null;
		}
		
		String[] strs = channelId.split("_");
		if (strs != null && strs.length == 2) {
			return strs[1];
		}
		
		return null;
	}
	
	public void setCallNumber(String callNumber) {
		mCallNumber = callNumber;
	}

	public String getChannelId() {
		return mChannelId;
	}
	
	public void setChannelId(String channelId) {
		log.d(TAG, "setChannelId(), channelId: " + channelId);
		
		mChannelId = channelId;
		mCallNumber = getCallNumber(mChannelId);
		mMeetingId = getMeetingId(mChannelId);
		log.d(TAG, "CallNumber: " + mCallNumber + ", MeetingId: " + mMeetingId);
	}
	
	public String getMeetingId() {
		return mMeetingId;
	}
	
	public void exitMeeting() {
		log.d(TAG, "exitMeeting()");
		setChannelId(null);
	}
	
	/**
	 * Agora的Uid使用rid;
	 * @return
	 *
	 * @param
	 *
	 * @return
	 *
	 */
	public int getUid() {
		if (YYDSDKHelper.getInstance().getLoginUser() != null) 
			return YYDSDKHelper.getInstance().getLoginUser().getUniqueId();
		else
			return 0;
	}
}
