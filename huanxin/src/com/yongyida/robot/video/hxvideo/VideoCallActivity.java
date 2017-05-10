/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */

package com.yongyida.robot.video.hxvideo;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMVideoCallHelper;
import com.easemob.exceptions.EMServiceNotReadyException;
import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.ConfigProvider;
import com.yongyida.robot.video.utils.log;

public class VideoCallActivity extends CallActivity implements OnClickListener {
	private static final String TAG = VideoCallActivity.class.getSimpleName();

	private SurfaceView mLocalSurface;
	private SurfaceHolder mLocalSurfaceHolder;
	private SurfaceView mOppositeSurface;
	private SurfaceHolder mOppositeSurfaceHolder;

	private boolean mIsMuteState;
	private boolean mIsHandsfreeState;
	private boolean mIsAnswered;
	private int mStreamID;
	private boolean mIsEndCallByMe = false;
	private boolean mIsMonitor = true;

	private EMVideoCallHelper mCallHelper;
	private TextView mTxtCallState;

	private Handler mHandler = new Handler();
	private LinearLayout mComingBtnContainer;
	private Button mBtnRefuse; // 拒绝
	private Button mBtnAnswer; // 接听
	private Button mBtnHangup; // 断开
	private ImageView mMuteImage; // 静音
	private ImageView mHandsFreeImage; // 免提
	private TextView mNickName;
	private Chronometer mChronometer;
	private LinearLayout mLayVoiceContronl;
	private RelativeLayout mLayRootContainer;
	private CameraHelper mCameraHelper;
	private LinearLayout mLayTopContainer;
	private LinearLayout mLayBottomContainer;

	private BroadcastReceiver mBroadcastReceiver;
	private int mRotateAngle = 0;
	
	private IntentFilter mIntentFilter;
	private LocalBroadcastManager mLocalBroadcastManager;
	private LocalReceiver mLocalReceiver;
	private boolean mIsClosing = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_video_call);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		mTxtCallState = (TextView) findViewById(R.id.tv_call_state);
		mComingBtnContainer = (LinearLayout) findViewById(R.id.ll_coming_call);
		mComingBtnContainer.setVisibility(View.INVISIBLE);
		mLayRootContainer = (RelativeLayout) findViewById(R.id.root_layout);
		mBtnRefuse = (Button) findViewById(R.id.btn_refuse_call);
		mBtnAnswer = (Button) findViewById(R.id.btn_answer_call);
		mBtnHangup = (Button) findViewById(R.id.btn_hangup_call);
		mMuteImage = (ImageView) findViewById(R.id.iv_mute);
		mHandsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
		mTxtCallState = (TextView) findViewById(R.id.tv_call_state);
		mNickName = (TextView) findViewById(R.id.tv_nick);
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mLayVoiceContronl = (LinearLayout) findViewById(R.id.ll_voice_control);
		mLayTopContainer = (LinearLayout) findViewById(R.id.ll_top_container);
		mLayBottomContainer = (LinearLayout) findViewById(R.id.ll_bottom_container);

		mBtnRefuse.setOnClickListener(this);
		mBtnAnswer.setOnClickListener(this);
		mBtnHangup.setOnClickListener(this);
		mMuteImage.setOnClickListener(this);
		mHandsFreeImage.setOnClickListener(this);
		mLayRootContainer.setOnClickListener(this);

		mMsgid = UUID.randomUUID().toString();

		// 获取通话是否为接收方向的
		mIsInComingCall = getIntent().getBooleanExtra("isComingCall", false);
		mUsername = getIntent().getStringExtra("username");
		mRotateAngle = getIntent().getIntExtra("rotateangle", 0);

		// 设置通话人
		mNickName.setText(mUsername);

		// 显示本地图像的surfaceview
		mLocalSurface = (SurfaceView) findViewById(R.id.local_surface);
		mLocalSurface.setZOrderMediaOverlay(true);
		mLocalSurface.setZOrderOnTop(true);

		mLocalSurfaceHolder = mLocalSurface.getHolder();

		// 获取callHelper,cameraHelper
		mCallHelper = EMVideoCallHelper.getInstance();
		mCameraHelper = new CameraHelper(mCallHelper, mLocalSurfaceHolder);
		mCameraHelper.setRotateAngle(mRotateAngle);

		// 显示对方图像的surfaceview
		mOppositeSurface = (SurfaceView) findViewById(R.id.opposite_surface);
		mOppositeSurfaceHolder = mOppositeSurface.getHolder();
		mCallHelper.setSurfaceView(mOppositeSurface);

		mLocalSurfaceHolder.addCallback(new LocalCallback());
		mOppositeSurfaceHolder.addCallback(new OppositeCallback());

		// 设置通话监听
		addCallStateListener();
		if (!mIsInComingCall) { // 拨打电话
			mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
			mOutgoing = mSoundPool.load(this, R.raw.outgoing, 1);

			mComingBtnContainer.setVisibility(View.INVISIBLE);
			mBtnHangup.setVisibility(View.VISIBLE);
			String st = getResources().getString(R.string.Are_connected_to_each_other);
			mTxtCallState.setText(st);

			mHandler.postDelayed(new Runnable() {
				public void run() {
					mStreamID = playMakeCallSounds();
				}
			}, 300);
		}
		else { // 有电话进来
			mLayVoiceContronl.setVisibility(View.INVISIBLE);
			mLocalSurface.setVisibility(View.INVISIBLE);
			Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			mAudioManager.setMode(AudioManager.MODE_RINGTONE);
			mAudioManager.setSpeakerphoneOn(true);
			mRingtone = RingtoneManager.getRingtone(this, ringUri);
			mRingtone.play();

			// 如果有电话进来，自动接听。
			mHandler.postDelayed(new Runnable() {
				public void run() {
					onClick(mBtnAnswer);
				}
			}, 2000);
		}

		initBroadCastReceiver();
		initLocalReceiver();
		
		//进入视频前发送广播
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "true");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_ENTERVIDEO));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_ENTERVIDEO);
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
						runOnUiThread(new Runnable() {
							public void run() {
								onClick(mBtnHangup);
							}
						});
					}
				}
				else {
					log.e(TAG, "error action: " + action);
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.GLOBAL_BROADCAST_ROBOT_STOP);
		this.registerReceiver(mBroadcastReceiver, filter);
	}

	/**
	 * 初始化本地广播接收
	 */
	protected void initLocalReceiver() {
		log.d(TAG, "initLocalReceiver()");

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
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
				runOnUiThread(new Runnable() {
					public void run() {
						onClick(mBtnHangup);
					}
				});
			}
			else if (action.equals(Constant.EXTCMD_VIDEO_MUTE)) {
				final boolean mute = intent.getBooleanExtra("mute", true);
				runOnUiThread(new Runnable() {
					public void run() {
						setMute(mute);
					}
				});
			}
		}
	}

	/**
	 * 本地SurfaceHolder callback
	 */
	class LocalCallback implements SurfaceHolder.Callback {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			log.d(TAG, "Local Surface, surfaceCreated()");
			mCameraHelper.startCapture();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			log.d(TAG, "Local Surface, surfaceChanged()");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			log.d(TAG, "Local Surface, surfaceDestroyed()");
			if (mCameraHelper != null) {
				mCameraHelper.stopCapture();
			}
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
			log.d(TAG, "Remote Surface, surfaceCreated()");
			
			holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
			mCallHelper.setRenderFlag(true);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			log.d(TAG, "Remote Surface, surfaceChanged()");
			
			//设置显示对方对象的surfaceview的宽、高及format
			mCallHelper.onWindowResize(width, height, format);

			if (!mCameraHelper.isStarted()) {
				if (!mIsInComingCall) {
					try {
						// 拨打视频通话
						EMChatManager.getInstance().makeVideoCall(mUsername);

						// 通知cameraHelper可以写入数据
						mCameraHelper.setStartFlag(true);
					}
					catch (EMServiceNotReadyException e) {
						Toast.makeText(VideoCallActivity.this, R.string.Is_not_yet_connected_to_the_server,
								android.widget.Toast.LENGTH_SHORT).show();
					}
				}
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			log.d(TAG, "Remote Surface, surfaceDestroyed()");
			
			if (mCallHelper != null) {
				mCallHelper.setRenderFlag(false);
			}
		}
	}

	/**
	 * 设置通话状态监听
	 */
	void addCallStateListener() {
		mCallStateListener = new EMCallStateChangeListener() {
			String strState = "";

			@Override
			public void onCallStateChanged(CallState callState, CallError error) {
				log.d(TAG, "EMCallStateChangeListener onCallStateChanged: " + callState);

				switch (callState) {
				case CONNECTING: // 正在连接对方
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							strState = getResources().getString(R.string.Are_connected_to_each_other);
							mTxtCallState.setText(strState);
							log.d(TAG, strState);
						}
					});
					break;
				case CONNECTED: // 双方已经建立连接
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							strState = getResources().getString(R.string.have_connected_with);
							mTxtCallState.setText(strState);
							log.d(TAG, strState);
						}
					});
					break;
				case ACCEPTED: // 电话接通成功
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								if (mSoundPool != null)
									mSoundPool.stop(mStreamID);
							}
							catch (Exception e) {
								log.e(TAG, "SoundPool stop exception: " + e.getMessage());
							}

							// 设置静音
							setMute(true);
							
							// 打开扬声器
							openSpeakerOn();

							((TextView) findViewById(R.id.tv_is_p2p)).setText(EMChatManager.getInstance().isDirectCall()
									? R.string.direct_call : R.string.relay_call);
							mHandsFreeImage.setImageResource(R.drawable.icon_speaker_on);
							mIsHandsfreeState = true;
							mChronometer.setVisibility(View.VISIBLE);
							mChronometer.setBase(SystemClock.elapsedRealtime());

							// 开始记时
							mChronometer.start();
							mNickName.setVisibility(View.INVISIBLE);
							mCallingState = CallingState.NORMAL;
							strState = getResources().getString(R.string.In_the_call);
							mTxtCallState.setText(strState);
							log.d(TAG, strState);
							
							startMonitor();
						}

					});
					break;
				case DISCONNNECTED: // 电话断了
					final CallError fError = error;
					runOnUiThread(new Runnable() {
						private void postDelayedCloseMsg() {
							mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									Animation animation = new AlphaAnimation(1.0f, 0.0f);
									animation.setDuration(800);
									mLayRootContainer.startAnimation(animation);
									if (!mIsClosing) {
										mIsClosing = true;
										finish();
									}
								}
							}, 200);
						}

						@Override
						public void run() {
							String strMsg = "";
							mChronometer.stop();
							mCallDruationText = mChronometer.getText().toString();

							if (fError == CallError.REJECTED) {
								mCallingState = CallingState.BEREFUESD;
								strMsg = getResources().getString(R.string.The_other_party_refused_to_accept);
								mTxtCallState.setText(strMsg);
							}
							else if (fError == CallError.ERROR_TRANSPORT) {
								strMsg = getResources().getString(R.string.Connection_failure);
								mTxtCallState.setText(strMsg);
							}
							else if (fError == CallError.ERROR_INAVAILABLE) {
								strMsg = getResources().getString(R.string.The_other_party_is_not_online);
								mCallingState = CallingState.OFFLINE;
								mTxtCallState.setText(strMsg);
							}
							else if (fError == CallError.ERROR_BUSY) {
								strMsg = getResources().getString(R.string.The_other_is_on_the_phone_please);
								mCallingState = CallingState.BUSY;
								mTxtCallState.setText(strMsg);
							}
							else if (fError == CallError.ERROR_NORESPONSE) {
								strMsg = getResources().getString(R.string.The_other_party_did_not_answer);
								mCallingState = CallingState.NORESPONSE;
								mTxtCallState.setText(strMsg);
							}
							else {
								if (mIsAnswered) {
									mCallingState = CallingState.NORMAL;
									if (mIsEndCallByMe) {
										strMsg = getResources().getString(R.string.hang_up);
										mTxtCallState.setText(strMsg);
									}
									else {
										strMsg = getResources().getString(R.string.The_other_is_hang_up);
										mTxtCallState.setText(strMsg);
									}
								}
								else {
									if (mIsInComingCall) {
										strMsg = getResources().getString(R.string.did_not_answer);
										mCallingState = CallingState.UNANSWERED;
										mTxtCallState.setText(strMsg);
									}
									else {
										if (mCallingState != CallingState.NORMAL) {
											strMsg = getResources().getString(R.string.Has_been_cancelled);
											mCallingState = CallingState.CANCED;
											mTxtCallState.setText(strMsg);
										}
										else {
											strMsg = getResources().getString(R.string.hang_up);
											mTxtCallState.setText(strMsg);
										}
									}
								}
							}
							postDelayedCloseMsg();
							log.d(TAG, strMsg);
						}

					});
					break;

				default:
					break;
				}
			}
		};
		EMChatManager.getInstance().addVoiceCallStateChangeListener(mCallStateListener);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_refuse_call: // 拒绝接听
			mBtnRefuse.setEnabled(false);
			if (mRingtone != null) {
				mRingtone.stop();
			}
			try {
				EMChatManager.getInstance().rejectCall();
			}
			catch (Exception e) {
				e.printStackTrace();
				log.e(TAG, "Hx rejectCall exception: " + e.getMessage());
				finish();
			}
			mCallingState = CallingState.REFUESD;
			break;
		case R.id.btn_answer_call: // 接听电话
			mBtnAnswer.setEnabled(false);
			if (mRingtone != null) {
				mRingtone.stop();
			}
			if (mIsInComingCall) {
				try {
					mTxtCallState.setText("正在接听...");
					log.d(TAG, "正在接听");
					EMChatManager.getInstance().answerCall();
					mCameraHelper.setStartFlag(true);

					openSpeakerOn();
					mHandsFreeImage.setImageResource(R.drawable.icon_speaker_on);
					mIsAnswered = true;
					mIsHandsfreeState = true;
				}
				catch (Exception e) {
					e.printStackTrace();
					log.e(TAG, "Hx answercall exception: " + e.getMessage());
					finish();
					return;
				}
			}
			mComingBtnContainer.setVisibility(View.INVISIBLE);
			mBtnHangup.setVisibility(View.VISIBLE);
			mLayVoiceContronl.setVisibility(View.VISIBLE);
			mLocalSurface.setVisibility(View.VISIBLE);
			break;
		case R.id.btn_hangup_call: // 挂断电话
			log.d(TAG, "挂断电话");
			mBtnHangup.setEnabled(false);
			if (mSoundPool != null)
				mSoundPool.stop(mStreamID);
			mChronometer.stop();
			mIsEndCallByMe = true;
			mTxtCallState.setText(getResources().getString(R.string.hanging_up));
			try {
				EMChatManager.getInstance().endCall();
			}
			catch (Exception e) {
				e.printStackTrace();
				log.e(TAG, "Hx endCall exception: " + e.getMessage());
				finish();
			}
			break;
		case R.id.iv_mute: // 静音开关
			setMute(!mIsMuteState);
			break;
		case R.id.iv_handsfree: // 免提开关
			if (mIsHandsfreeState) {
				// 关闭免提
				mHandsFreeImage.setImageResource(R.drawable.icon_speaker_normal);
				closeSpeakerOn();
				mIsHandsfreeState = false;
			}
			else {
				// 打开免提
				mHandsFreeImage.setImageResource(R.drawable.icon_speaker_on);
				openSpeakerOn();
				mIsHandsfreeState = true;
			}
			break;
		case R.id.root_layout:
			if (mCallingState == CallingState.NORMAL) {
				if (mLayBottomContainer.getVisibility() == View.VISIBLE) {
					mLayBottomContainer.setVisibility(View.GONE);
					mLayTopContainer.setVisibility(View.GONE);
				}
				else {
					mLayBottomContainer.setVisibility(View.VISIBLE);
					mLayTopContainer.setVisibility(View.VISIBLE);
				}
			}
			break;
		default:
			break;
		}
	}
	
	private void setMute(boolean mute) {
		log.d(TAG, "mute: " + mute);
		
		if (mute) {
			// 打开静音
			mMuteImage.setImageResource(R.drawable.icon_mute_on);
			mAudioManager.setMicrophoneMute(true);
			mIsMuteState = true;
		}
		else {
			// 关闭静音
			mMuteImage.setImageResource(R.drawable.icon_mute_normal);
			mAudioManager.setMicrophoneMute(false);
			mIsMuteState = false;
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);

		stopMonitor();

		try {
			if (mCallHelper != null) {
				mCallHelper.setSurfaceView(null);
				mCallHelper = null;
			}

			if (mCameraHelper != null) {
				mCameraHelper.stopCapture();
				mCameraHelper = null;
			}
		}
		catch (Exception e) {
			log.e(TAG, "Exception: " + e.getMessage());
		}

		// 退出视频前发送广播
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "false");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_EXITVIDEO));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_EXITVIDEO);
		
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		EMChatManager.getInstance().endCall();
		mCallDruationText = mChronometer.getText().toString();
		finish();
	}

	/**
	 * 方便开发测试，实际app中去掉显示即可
	 */
	void startMonitor() {
		mIsMonitor = true;
		new Thread(new Runnable() {
			int width = 0;
			int height = 0;

			public void run() {
				while (mIsMonitor) {
					if (mCallHelper != null) {
						// 如果检测到视频宽高错误，则自动挂断。
						width = mCallHelper.getVideoWidth();
						height = mCallHelper.getVideoHeight();
						if (width == 240 && height == 320) {
							log.e(TAG, "video size: " + width + "x" + height + ", will auto end call.");
							try {
								EMChatManager.getInstance().endCall();
							}
							catch (Exception e) {
								log.e(TAG, "Hx endCall exception: " + e.getMessage());
								finish();
							}
						}
					}
					try {
						Thread.sleep(500);
					}
					catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	void stopMonitor() {
		mIsMonitor = false;
	}

}
