/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.IRtcEngineEventHandler.RtcStats;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import com.yongyida.robot.video.ConfigProvider;
import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.agora.AgoraSDKHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 视频通话
 * 
 */
public class ActivityMonitor {
	public static final String TAG = "ActivityMonitor";
	
	private static ActivityMonitor sInstance;
	
	private Context mContext;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mWmParams;
	private RelativeLayout mRootLayout;
	private RelativeLayout mLayoutContainer;
	private AudioManager mAudioManager;
	private SurfaceView mCameraView;
	
	private RtcEngine mRtcEngine;
	private String mVendorKey;
    private String mChannelId;
    private int mUid;
    private int mVideoWidth = 320;
	@SuppressWarnings("unused")
	private int mVideoHeight = 240;
	
	public ActivityMonitor(Context context) {
		sInstance = this;
		mContext = context;
	}
	
	public void open() {
		log.d(TAG, "open()");
		
		mVendorKey = Utils.getMetaData(mContext, ActivityMeeting.VENDOR_KEY);
		mChannelId = AgoraSDKHelper.getInstance().getChannelId();
		mUid = AgoraSDKHelper.getInstance().getUid();
		
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		int result = mAudioManager.requestAudioFocus(mAudioFocusListener, 
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			log.e(TAG, "No get AUDIOFOCUS_REQUEST_GRANTED");
		}
		
		createWindow();
		
		//发送广播
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "true");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		mContext.sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_ENTERMONITOR));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_ENTERMONITOR);
	}
	
    public void close() {
    	log.d(TAG, "close()");
    	
    	if (mAudioManager != null) {
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
		}
    	
    	if (mRootLayout != null) {
			mWindowManager.removeView(mRootLayout);
			mRootLayout = null;
		}
    	
    	//退出视频前发送广播
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "false");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		mContext.sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_EXITMONITOR));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_EXITMONITOR);
		
		sInstance = null;
	}
    
	@SuppressLint("InflateParams")
	private void createWindow() {
		log.d(TAG, "createWindow()");
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootLayout = (RelativeLayout) inflater.inflate(R.layout.activity_monitor, null);
		mLayoutContainer = (RelativeLayout) mRootLayout.findViewById(R.id.layout_container);
		mWindowManager = (WindowManager) mContext.getSystemService("window");
		mWmParams = new WindowManager.LayoutParams();
		mWmParams.type = LayoutParams.TYPE_PHONE;
		mWmParams.format = PixelFormat.RGBA_8888;
		mWmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		mWmParams.x = 0;
		mWmParams.y = 0;
		mWmParams.width = 1;
		mWmParams.height = 1;
		mWindowManager.addView(mRootLayout, mWmParams);
		
		setupRtcEngine();
		joinChannel();
		setLocalView();
		setMute(true);
	}
	
	private void setupRtcEngine() {
        AgoraSDKHelper.getInstance().setRtcEngine(mVendorKey, new MessageHandler());
        mRtcEngine = AgoraSDKHelper.getInstance().getRtcEngine();
        mRtcEngine.setLogFile(Utils.getExternalStorageDirectory() + "/agora.log");
        mRtcEngine.enableVideo();
    }
	
	private void joinChannel() {
        mRtcEngine.joinChannel(
			mVendorKey,
			mChannelId,
			"" /*optionalInfo*/,
			mUid/*optionalUid*/);
    }
	
	/**
	 * 加入本地视图
	 * @param
	 * @return
	 */
	private void setLocalView() {
		log.d(TAG, "setLocalView()");
		
        if (mCameraView == null) {
        	SurfaceView surfaceView = RtcEngine.CreateRendererView(mContext);
        	FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
        			LayoutParams.MATCH_PARENT,
        			LayoutParams.MATCH_PARENT);
        	mLayoutContainer.addView(surfaceView, layoutParams);
    		mRtcEngine.setParameters("{\"che.video.local.camera_index\":0}");
    		mRtcEngine.setVideoProfile(getVideoProfileValue(mVideoWidth));
            mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, mUid));
        }
    }
	
	private int getVideoProfileValue(int videoWidth) {
		switch (videoWidth) {
		case 320:
			return 20;
		case 640:
			return 40;
		case 1280:
			return 50;
		case 1920:
			return 60;
		default:
			return 20;
		}
	}
	
	private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			log.d(TAG, "onAudioFocusChange(), focusChange: " + focusChange);

			//短暂获得音频焦点
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
			}
			//完全获得音频焦点
			else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				log.d(TAG, "AUDIOFOCUS_GAIN");
			}
			//失去音频焦点
			else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				log.d(TAG, "AUDIOFOCUS_LOSS");
			}
		}
	};
	
	public void videoReply() {
		log.d(TAG, "videoReply()");
		
		// 设置MIC静音, 扬声器打开
		setMute(true);
		setSpeakerOn(true);
	}
	
	public static ActivityMonitor getInstance() {
		return sInstance;
	}
    
	public boolean isMute() {
		return true;
	}

	public void setMute(boolean mute) {
		log.d(TAG, "setMute: " + mute);
	}
	
	public boolean isSpeakerOn() {
		return true;
	}
	
	public void setSpeakerOn(boolean speakerOn) {
		log.d(TAG, "setSpeakerOn: " + speakerOn);
	}
	
	public void onFirstRemoteVideoDecoded(final int uid, int width, int height, final int elapsed) {
        log.d(TAG, "onFirstRemoteVideoDecoded: uid: " + uid + ", width: " + width + ", height: " + height);
    }
	
	/**
	 * 用户离线
	 * @param uid
	 * @param reason
	 * @return
	 */
	public void onUserOffline(final int uid, int reason) {
		log.d(TAG, "onUserOffline(), uid: " + uid + ", reason: " + reason);
		
		//收到用户离线，则离开房间。
		if (mRtcEngine != null) {
    		mRtcEngine.leaveChannel();
    	}
	}
	
	/**
	 * 离开房间
	 * @param stats
	 * @return
	 */
	public void onLeaveChannel(RtcStats stats) {
		log.d(TAG, "onLeaveChannel(), users: " + stats.users);
		
		// 收到离开房间后，关闭窗体
		close();
	}
	
	public class MessageHandler extends IRtcEngineEventHandler {
		public void onApiCallExecuted(String api, int error) {
			if (error != 0) {
				log.d(TAG, "onApiCallExecuted(), api: " + api + ", error: " + error);
			}
		}
		
		public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
			log.d(TAG, "onAudioVolumeIndication()");
		}

		public void onCameraReady() {
			log.d(TAG, "onCameraReady()");
		}

		public void onConnectionInterrupted() {
			log.d(TAG, "onConnectionInterrupted()");
		}

		public void onConnectionLost() {
			log.d(TAG, "onConnectionLost()");
		}
		
		public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
			log.d(TAG, "onFirstLocalVideoFrame(), width: " + width + ", height: " + height);
		}

		public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
			log.d(TAG, "onFirstRemoteVideoFrame(), uid: " + uid + ", width: " + width + ", height: " + height);
		}
		
		public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
			log.d(TAG, "onFirstRemoteVideoDecoded(), uid: " + uid + ", width: " + width + ", height: " + height);
			
			ActivityMonitor.this.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
		}
		
		/**
		 * 进入房间
		 * @param channel
		 * @param uid
		 * @param elapsed
		 * @return
		 */
		public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
			log.d(TAG, "onJoinChannelSuccess(), channel: " + channel + ", uid: " + uid + ", elapsed: " + elapsed);
		}
		
		public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
			log.d(TAG, "onRejoinChannelSuccess(), channel: " + channel + ", uid: " + uid);
		}
		
		/**
		 * 离开房间
		 * @param stats
		 * @return
		 */
		public void onLeaveChannel(RtcStats stats) {
			log.d(TAG, "onLeaveChannel(), users: " + stats.users);
			ActivityMonitor.this.onLeaveChannel(stats);
		}
		
		public void onRefreshRecordingServiceStatus(int status) {
			log.d(TAG, "onRefreshRecordingServiceStatus(), status: " + status);
		}
		
		public void onRtcStats(RtcStats stats) {
			//log.d(TAG, "onRtcStats()");
		}
		
		public void onNetworkQuality(int quality) {
			//log.d(TAG, "onNetworkQuality(), quality: " + quality);
		}
		
		public void onAudioQuality(int uid, int quality, short delay, short lost) {
			//log.d(TAG, "onAudioQuality(), uid: " + uid + ", quality: " + quality + ", delay: " + delay + ", lost: " + lost);
		}
		
		public void onLocalVideoStats(LocalVideoStats stats) {
			//log.d(TAG, "onLocalVideoStats(), sentFrameRate: " + stats.sentFrameRate + ", sentBitrate: " + stats.sentBitrate);
		}
		
		public void onRemoteVideoStats(RemoteVideoStats stats) {
			//log.d(TAG, "onRemoteVideoStats(), receivedFrameRate: " + stats.receivedFrameRate + ", receivedBitrate: " + stats.receivedBitrate);
		}
		
		public void onLocalVideoStat(int sentBitrate, int sentFrameRate) {
			log.d(TAG, "onLocalVideoStat(), sentBitrate: " + sentBitrate + ", sentFrameRate: " + sentFrameRate);
		}
		
		public void onRemoteVideoStat(int uid, int delay, int receivedBitrate, int receivedFrameRate) {
			log.d(TAG, "onRemoteVideoStat(), uid: " + uid + ", delay: " + delay + ", receivedBitrate: " + receivedBitrate
					+ ", receivedFrameRate: " + receivedFrameRate);
		}
		
		public void onUserEnableVideo(int uid, boolean enabled) {
			log.d(TAG, "onUserEnableVideo(), uid: " + uid + ", enabled: " + enabled);
		}
		
		/**
		 * 有用户加入
		 * @param uid
		 * @param elapsed
		 * @return
		 */
		public void onUserJoined(int uid, int elapsed) {
			log.d(TAG, "onUserJoined(), uid: " + uid + ", elapsed: " + elapsed);
		}
		
		/**
		 * 有用户离开
		 * @param uid
		 * @param reason
		 * @return
		 */
		public void onUserOffline(int uid, int reason) {
			log.d(TAG, "onUserOffline(), uid: " + uid + ", reason: " + reason);
			ActivityMonitor.this.onUserOffline(uid, reason);
		}

		public void onUserMuteAudio(int uid, boolean muted) {
			log.d(TAG, "onUserMuteAudio(), uid: " + uid + ", muted: " + muted);
		}

		public void onUserMuteVideo(int uid, boolean muted) {
			log.d(TAG, "onUserMuteVideo(), uid: " + uid + ", muted: " + muted);
		}
		
		public void onVideoStopped() {
			log.d(TAG, "onVideoStopped()");
		}

		public void onWarning(int warn) {
			log.d(TAG, "onWarning(), warn: " + warn);
		}
		
		public void onError(int err) {
			log.d(TAG, "onError(), err: " + err);
		}
	}
}
