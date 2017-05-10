/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.hxvideo;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.SoundPool;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.utils.BaseActivity;
import com.yongyida.robot.video.utils.log;

/**
 * 通话页面
 *
 */
public class CallActivity extends BaseActivity {
	private static final String TAG = CallActivity.class.getSimpleName();

	protected boolean mIsInComingCall;
	protected String mUsername;
	protected CallingState mCallingState = CallingState.CANCED;
	protected String mCallDruationText;
	protected String mMsgid;
	protected AudioManager mAudioManager;
	protected SoundPool mSoundPool;
	protected Ringtone mRingtone;
	protected int mOutgoing;
	protected EMCallStateChangeListener mCallStateListener;
	protected boolean mInitSpeakerphoneOn = false;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mInitSpeakerphoneOn = mAudioManager.isSpeakerphoneOn();
		
		//要求音频焦点
		int result = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			log.e(TAG, "No get AUDIOFOCUS_REQUEST_GRANTED");
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSoundPool != null) {
			mSoundPool.release();
		}
		
		if (mRingtone != null && mRingtone.isPlaying()) {
			mRingtone.stop();
		}
		
		if (mAudioManager != null) {
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
			mAudioManager.setMicrophoneMute(false);
			mAudioManager.setSpeakerphoneOn(mInitSpeakerphoneOn);
			
			//取消音频焦点
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
		}
		
		if (mCallStateListener != null) {
			EMChatManager.getInstance().removeCallStateChangeListener(mCallStateListener);
		}
	}

	/**
	 * 播放拨号响铃
	 * 
	 */
	protected int playMakeCallSounds() {
		try {
			mAudioManager.setMode(AudioManager.MODE_RINGTONE);
			mAudioManager.setSpeakerphoneOn(false);

			// 播放
			int id = mSoundPool.play(mOutgoing, // 声音资源
					0.3f, // 左声道
					0.3f, // 右声道
					1, // 优先级，0最低
					-1, // 循环次数，0是不循环，-1是永远循环
					1); // 回放速度，0.5-2.0之间。1为正常速度
			return id;
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 *  打开扬声器
	 *  
	 */
	protected void openSpeakerOn() {
		log.d(TAG, "openSpeakerOn()");
		
		try {
			if (mAudioManager != null) {
				if (!mAudioManager.isSpeakerphoneOn())
					mAudioManager.setSpeakerphoneOn(true);
				
			    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
			}
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
		log.d(TAG, "closeSpeakerOn()");
		
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
	
	/**
	 * 保存通话消息记录
	 * @param type 0：音频，1：视频
	 * 
	 */
	protected void saveCallRecord(int type) {
		EMMessage message = null;
		TextMessageBody txtBody = null;

		if (!mIsInComingCall) { // 打出去的通话
			message = EMMessage.createSendMessage(EMMessage.Type.TXT);
			message.setReceipt(mUsername);
		}
		else {
			message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
			message.setFrom(mUsername);
		}

		String st1 = getResources().getString(R.string.call_duration);
		String st2 = getResources().getString(R.string.Refused);
		String st3 = getResources().getString(R.string.The_other_party_has_refused_to);
		String st4 = getResources().getString(R.string.The_other_is_not_online);
		String st5 = getResources().getString(R.string.The_other_is_on_the_phone);
		String st6 = getResources().getString(R.string.The_other_party_did_not_answer);
		String st7 = getResources().getString(R.string.did_not_answer);
		String st8 = getResources().getString(R.string.Has_been_cancelled);

		switch (mCallingState) {
		case NORMAL:
			txtBody = new TextMessageBody(st1 + mCallDruationText);
			break;
		case REFUESD:
			txtBody = new TextMessageBody(st2);
			break;
		case BEREFUESD:
			txtBody = new TextMessageBody(st3);
			break;
		case OFFLINE:
			txtBody = new TextMessageBody(st4);
			break;
		case BUSY:
			txtBody = new TextMessageBody(st5);
			break;
		case NORESPONSE:
			txtBody = new TextMessageBody(st6);
			break;
		case UNANSWERED:
			txtBody = new TextMessageBody(st7);
			break;
		default:
			txtBody = new TextMessageBody(st8);
			break;
		}

		// 设置扩展属性
		if (type == 0)
			message.setAttribute(Constant.HX_MESSAGE_ATTR_IS_VOICE_CALL, true);
		else
			message.setAttribute(Constant.HX_MESSAGE_ATTR_IS_VIDEO_CALL, true);

		// 设置消息body
		message.addBody(txtBody);
		message.setMsgId(mMsgid);

		// 保存
		EMChatManager.getInstance().saveMessage(message, false);
	}

	enum CallingState {
		CANCED, NORMAL, REFUESD, BEREFUESD, UNANSWERED, OFFLINE, NORESPONSE, BUSY
	}

}
