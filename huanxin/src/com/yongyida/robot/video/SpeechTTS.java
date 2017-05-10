/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.yongyida.robot.video.utils.log;

/**
 * 科大讯飞语音合成
 * 
 */
public class SpeechTTS {
	private static final String TAG = SpeechTTS.class.getSimpleName();
	private static final String APPID = "appid=56065ce8";
	private static SpeechTTS sInstance = null;

	// 科大讯飞配置
	private SpeechSynthesizer mTts;
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	private String mVoicer = "aisxa";
	
	private SpeechTTS() {
	}

	/**
	 * 返回单例
	 * @param
	 * @return SpeechTTS
	 *
	 */
	public static synchronized SpeechTTS getInstance() {
		if (sInstance == null) {
			sInstance = new SpeechTTS();
		}
		return sInstance;
	}

	/**
	 * 初骀化
	 *
	 */
	public synchronized void init(Context context) {
		log.d(TAG, "SpeechTTS init()");
		SpeechUtility.createUtility(context, APPID);
		mTts = SpeechSynthesizer.createSynthesizer(context, ttsInitListener);
	}

	/**
	 * 关闭
	 *
	 */
	public void close() {
		mTts.stopSpeaking();
		mTts.destroy();
	}
	
	private InitListener ttsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				log.e(TAG, "讯飞初始化失败,错误码：" + code);
			}
			else {
				setTtsParam();
			}
		}
	};
	
	private void setTtsParam() {
		log.d(TAG, "setTtsParam");
		
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, mVoicer);
		}
		else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");
		}
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, "50");
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50");
		//设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
	}

	/**
	 * TTS语音合成
	 *
	 */
	public int speak(String text) {
		log.d(TAG, "speak: " + text);
		
		int code = mTts.startSpeaking("  " + text, ttsListener);
		if (code != ErrorCode.SUCCESS) {
			if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED)
				log.e(TAG, "语音组件未安装");
			else
				log.e(TAG, "语音合成失败,错误码: " + code);
		}

		return code;
	}
	
	private SynthesizerListener ttsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
		}

		@Override
		public void onSpeakPaused() {
		}

		@Override
		public void onSpeakResumed() {
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
			// 合成进度
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
		}

		@Override
		public void onCompleted(SpeechError error) {
			// 播放完成
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			/*
			if (SpeechEvent.EVENT_SESSION_ID == eventType) {
				String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
				log.d(TAG, "session id =" + sid);
			}
			*/
		}
	};

}
