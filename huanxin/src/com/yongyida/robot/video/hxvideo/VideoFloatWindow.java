/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.hxvideo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.RelativeLayout;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMCallStateChangeListener.CallError;
import com.easemob.chat.EMCallStateChangeListener.CallState;
import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.ConfigProvider;
import com.yongyida.robot.video.utils.log;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMVideoCallHelper;

/**
 * 视图浮动窗口
 *
 */
public class VideoFloatWindow {
	private static final String TAG = VideoFloatWindow.class.getSimpleName();

	private Context mContext;
	private AudioManager mAudioManager;
	private Ringtone mRingtone;
	private EMCallStateChangeListener mCallStateListener;

	private WindowManager mWindowManager = null;
	private WindowManager.LayoutParams mWmParams = null;
	private RelativeLayout mFrameLayout = null;

	private SurfaceView mLocalSurface;
	private SurfaceHolder mLocalSurfaceHolder;
	private SurfaceView mOppositeSurface;
	private SurfaceHolder mOppositeSurfaceHolder;
	private boolean mIsAnswered;

	private EMVideoCallHelper mCallHelper;
	private Chronometer mChronometer;
	private CameraHelper mCameraHelper;

	private BroadcastReceiver mBroadcastReceiver;

	private int mVideoWidth = 320;
	private int mVideoHeight = 240;
	private int mRotateAngle = 0;
	
	private IntentFilter mIntentFilter;
	private LocalBroadcastManager mLocalBroadcastManager;
	private LocalReceiver mLocalReceiver;
	private boolean mIsClosing = false;

	public VideoFloatWindow(Context context) {
		mContext = context;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		//要求音频焦点
		int result = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			log.e(TAG, "No get AUDIOFOCUS_REQUEST_GRANTED");
		}
		
		//进入监控前发送广播
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "true");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		mContext.sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_ENTERMONITOR));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_ENTERMONITOR);
	}

	/**
	 * 建立浮动窗口
	 * 
	 */
	@SuppressLint("InflateParams")
	private void createFloatView() {
		log.d(TAG, "createFloatView");
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFrameLayout = (RelativeLayout) inflater.inflate(R.layout.activity_video_float, null);

		mChronometer = (Chronometer) mFrameLayout.findViewById(R.id.chronometer);

		// 显示本地图像的surfaceview
		mLocalSurface = (SurfaceView) mFrameLayout.findViewById(R.id.local_surface);
		mLocalSurface.setZOrderMediaOverlay(true);
		mLocalSurface.setZOrderOnTop(true);
		mLocalSurface.setVisibility(View.VISIBLE);
		mLocalSurfaceHolder = mLocalSurface.getHolder();

		// 获取callHelper,cameraHelper
		mCallHelper = EMVideoCallHelper.getInstance();
		mCameraHelper = new CameraHelper(mCallHelper, mLocalSurfaceHolder);
		mCameraHelper.setVideoSize(mVideoWidth, mVideoHeight);
		mCameraHelper.setRotateAngle(mRotateAngle);

		// 显示对方图像的surfaceview
		mOppositeSurface = (SurfaceView) mFrameLayout.findViewById(R.id.opposite_surface);
		mOppositeSurfaceHolder = mOppositeSurface.getHolder();
		mCallHelper.setSurfaceView(mOppositeSurface);

		// 设置Holder回调
		mLocalSurfaceHolder.addCallback(new LocalCallback());
		mOppositeSurfaceHolder.addCallback(new OppositeCallback());

		mWindowManager = (WindowManager) mContext.getSystemService("window");
		mWmParams = new WindowManager.LayoutParams();
		mWmParams.type = LayoutParams.TYPE_PHONE;
		mWmParams.format = PixelFormat.RGBA_8888;
		mWmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		mWmParams.x = 0;
		mWmParams.y = 0;
		mWmParams.width = 1;
		mWmParams.height = 1;
		mWindowManager.addView(mFrameLayout, mWmParams);
	}

	/**
	 * 打开浮动窗口
	 * 
	 */
	public void open() {
		log.d(TAG, "open");

		//建立浮动窗口
		createFloatView();

		// 设置通话监听
		initBroadCastReceiver();
		initLocalReceiver();
		addCallStateListener();

		// 有电话进来
		Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		mAudioManager.setMode(AudioManager.MODE_RINGTONE);
		mAudioManager.setSpeakerphoneOn(true);
		mRingtone = RingtoneManager.getRingtone(mContext, ringUri);
		mRingtone.play();

		autoAnswer();
		setMuteState(false);
		setHandsFree(false);
	}
	
	/**
	 * 关闭浮动窗口
	 * 
	 */
	public void close() {
		if (mIsClosing)
			return;
		
		mIsClosing = true;
		log.d(TAG, "close");
		
		try {
			EMChatManager.getInstance().endCall();
		}
		catch (Exception e) {
			e.printStackTrace();
			log.e(TAG, "Hx endCall exception: " + e.getMessage());
		}

		if (mRingtone != null && mRingtone.isPlaying()) {
			mRingtone.stop();
		}

		if (mAudioManager != null) {
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			mAudioManager.setMicrophoneMute(false);
			//取消音频焦点
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
		}
		
		if (mCallStateListener != null) {
			EMChatManager.getInstance().removeCallStateChangeListener(mCallStateListener);
		}

		mContext.unregisterReceiver(mBroadcastReceiver);
		mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
		
		try {
			if (mCallHelper != null) {
				mCallHelper.setSurfaceView(null);
			}
			if (mCameraHelper != null) {
				mCameraHelper.stopCapture();
				mCameraHelper = null;
			}
		}
		catch (Exception e) {
			log.e(TAG, e.getMessage());
		}

		if (mFrameLayout != null) {
			mWindowManager.removeView(mFrameLayout);
		}

		//退出视频前发送广播
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "false");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		mContext.sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_EXITMONITOR));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_EXITMONITOR);
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

	public void setVideoSize(int width, int height) {
		mVideoWidth = width;
		mVideoHeight = height;
	}

	public void setRotateAngle(int rotateAngle) {
		mRotateAngle = rotateAngle;
	}
	
	/**
	 * 初始化广播接收
	 */
	protected void initBroadCastReceiver() {
		log.d(TAG, "initBroadCastReceiver()");
		
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				log.d(TAG, "onReceive(), recv action: " + action);
				if (action != null && action.equals(Constant.GLOBAL_BROADCAST_ROBOT_STOP)) {
					String extra = intent.getStringExtra(Constant.GLOBAL_BROADCAST_ROBOT_STOP_EXTRA);
					log.d(TAG, "Action extra: " + extra);
					if (extra != null && (extra.equals(Constant.BROADCAST_EXTRA_SHUT_DOWN_VIDEO)
							|| extra.equals(Constant.BROADCAST_EXTRA_RINGUP))) {
						close();
					}
				}
				else {
					log.e(TAG, "error action: " + action);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.GLOBAL_BROADCAST_ROBOT_STOP);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}
	
	/**
	 * 初始化本地广播接收
	 */
	protected void initLocalReceiver() {
		log.d(TAG, "initLocalReceiver()");
		
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Constant.EXTCMD_VIDEO_CLOSE);
		mIntentFilter.addAction(Constant.EXTCMD_VIDEO_MUTE);
		mLocalReceiver = new LocalReceiver();
		mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
	}
	
	class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			log.d(TAG, "onReceive(), recv action: " + action);
			
			if (action.equals(Constant.EXTCMD_VIDEO_CLOSE)) {
				close();
			}
			else if (action.equals(Constant.EXTCMD_VIDEO_MUTE)) {
				final boolean mute = intent.getBooleanExtra("mute", true);
				setMuteState(mute);
			}
		}
	}
	
	/**
	 * 本地SurfaceHolder callback
	 * 
	 */
	class LocalCallback implements SurfaceHolder.Callback {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			log.d(TAG, "surfaceCreated()");
			mCameraHelper.startCapture();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			log.d(TAG, "surfaceChanged()");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			log.d(TAG, "surfaceDestroyed()");
		}
	}

	/**
	 * 对方SurfaceHolder callback
	 */
	@SuppressLint("ShowToast")
	class OppositeCallback implements SurfaceHolder.Callback {
		@SuppressWarnings("deprecation")
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			mCallHelper.onWindowResize(width, height, format);
			if (!mCameraHelper.isStarted()) {
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			log.d(TAG, "handleMessage()");

			Param param = (Param) msg.obj;
			log.d(TAG, "callState: " + param.callState.toString());
			
			switch (param.callState) {
			case CONNECTING: // 正在连接对方
				log.d(TAG, mContext.getResources().getString(R.string.Are_connected_to_each_other));
				break;
			case CONNECTED: // 双方已经建立连接
				log.d(TAG, mContext.getResources().getString(R.string.have_connected_with));
				break;
			case ACCEPTED: // 电话接通成功
				setMuteState(true);
				openSpeakerOn();
				mChronometer.setVisibility(View.VISIBLE);
				mChronometer.setBase(SystemClock.elapsedRealtime());
				mChronometer.start(); // 开始记时
				log.d(TAG, mContext.getResources().getString(R.string.In_the_call));
				break;
			case DISCONNNECTED: // 电话断了
				mChronometer.stop();
				String strMsg = "";
				switch (param.error) {
				case REJECTED:
				    strMsg = mContext.getResources().getString(R.string.The_other_party_refused_to_accept);
					break;
				case ERROR_TRANSPORT:
				    strMsg = mContext.getResources().getString(R.string.Connection_failure);
					break;
				case ERROR_INAVAILABLE:
				    strMsg = mContext.getResources().getString(R.string.The_other_party_is_not_online);
					break;
				case ERROR_BUSY:
				    strMsg = mContext.getResources().getString(R.string.The_other_is_on_the_phone_please);
					break;
				case ERROR_NORESPONSE:
				    strMsg = mContext.getResources().getString(R.string.The_other_party_did_not_answer);
					break;
				default:
					if (mIsAnswered) {
					    strMsg = mContext.getResources().getString(R.string.The_other_is_hang_up);
					}
					else {
					    strMsg = mContext.getResources().getString(R.string.did_not_answer);
					}
					break;
				}
				
				//关闭
                close();
                
				log.d(TAG, strMsg);
			default:
				break;
			}
		}
	};

	private class Param {
		CallState callState;
		CallError error;

		Param(CallState c, CallError e) {
			callState = c;
			error = e;
		}
	}

	/**
	 * 设置通话状态监听
	 */
	void addCallStateListener() {
		log.d(TAG, "addCallStateListener");
		mCallStateListener = new EMCallStateChangeListener() {
			@Override
			public void onCallStateChanged(CallState callState, CallError error) {
				log.d(TAG, "EMCallStateChangeListener onCallStateChanged: " + callState);

				Message msg = mHandler.obtainMessage();
				msg.obj = new Param(callState, error);
				mHandler.sendMessage(msg);
			}
		};
		EMChatManager.getInstance().addVoiceCallStateChangeListener(mCallStateListener);
	}

	/**
	 * 自动接听
	 * 
	 */
	private void autoAnswer() {
		log.d(TAG, "autoAnswer");

		if (mRingtone != null)
			mRingtone.stop();

		try {
			log.d(TAG, "正在接听...");
			EMChatManager.getInstance().answerCall();
			mCameraHelper.setStartFlag(true);
			mIsAnswered = true;
		}
		catch (Exception e) {
			log.e(TAG, "HX answercall error:" + e.getMessage());
		}
	}

	/**
	 * 设置静音
	 *
	 */
	private void setMuteState(boolean mute) {
		mAudioManager.setMicrophoneMute(mute);
	}

	/**
	 * 设置免提
	 *
	 */
	private void setHandsFree(boolean handsFree) {
		if (handsFree)
			openSpeakerOn();
		else
			closeSpeakerOn();
	}

	/**
	 *  打开扬声器
	 *  
	 */
	protected void openSpeakerOn() {
		try {
			if (!mAudioManager.isSpeakerphoneOn())
				mAudioManager.setSpeakerphoneOn(true);

			mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  关闭扬声器
	 *
	 */
	protected void closeSpeakerOn() {
		try {
			if (mAudioManager != null) {
				if (mAudioManager.isSpeakerphoneOn())
					mAudioManager.setSpeakerphoneOn(false);

				mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
