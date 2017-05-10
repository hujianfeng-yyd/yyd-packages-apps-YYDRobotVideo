/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

import com.yongyida.robot.video.activity.InviteReplyDialog;
import com.yongyida.robot.video.apprtc.CallActivity;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.LoginResponse;
import com.yongyida.robot.video.command.Response;
import com.yongyida.robot.video.command.WVMInviteCancelRequest;
import com.yongyida.robot.video.command.WVMInviteRequest;
import com.yongyida.robot.video.command.WVMReplyRequest;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.NumberType;
import com.yongyida.robot.video.sdk.YYDVideoServer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.webkit.URLUtil;

/**
 * 视频监听
 * 
 */
public class WebRtcVideoListener implements IVideoListener {
	private static final String TAG = "AgoraVideoListener";

	private Context mContext;
	private SharedPreferences mSharedPref;
	private InviteReplyDialog mInviteReplyDialog;

	public WebRtcVideoListener(Context context) {
		mContext = context;
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	/**
	 * 打开
	 * 
	 */
	public boolean open() {
		log.d(TAG, "open()");

		// YYDVideoServer初始化
		YYDVideoServer.getInstance().registerEventListener(mEventListener);
		YYDVideoServer.getInstance().connect();
		
		return true;
	}

	/**
	 * 关闭
	 *
	 */
	public void close() {
		log.d(TAG, "close()");
		YYDVideoServer.getInstance().unRegisterEventListener(mEventListener);
	}

	private EventListener mEventListener = new EventListener() {
		public void onEvent(Event event, final Object data) {
			log.d(TAG, "onEvent(), envet: " + event);

			switch (event) {
			case LoginResponse:
				//收到登录响应
				processLoginResponse((LoginResponse) data);
				break;
			case WVMInviteRequest:
				//收到会议邀请
				porcessInviteRequest((WVMInviteRequest) data);
				break;
			case WVMInviteCancelRequest:
				//收到会议邀请取消
				processInviteCancelRequest((WVMInviteCancelRequest) data);
				break;
			case WVMReplyRequest:
				// 收到答复
				processReplyRequest((WVMReplyRequest) data);
				break;
			case CommandTimeout:
				log.e(TAG, "CommandTimeout, cmdId: " + Utils.getHexString((Integer) data));
				break;
			case CommandNotExecute:
				log.e(TAG, "CommandNotExecute, cmdId: " + Utils.getHexString((Integer) data));
				break;
			default:
				break;
			}
		}
	};

	private void processLoginResponse(LoginResponse resp) {
		if (resp.getRet() == Response.RET_OK) {
			// 从登录响应中获取机器人的rid
			Robot.getInstance().setRid(resp.getId());
		}
	}

	private void porcessInviteRequest(final WVMInviteRequest requ) {
		final VideoMode mode = VideoMode.getVideoMode(requ.getMode());
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			public void run() {
				// 会议邀请
				if (mode == VideoMode.MEETING) {
					openInviteReplyDialog(requ.getRoomId(), requ.getUserName(), requ.getRole(), requ.getId(), requ.getNumber());
				}
				// 监控请求
				else if (mode == VideoMode.MONITOR) {
					openVideoMonitor(requ.getRoomId());
				}
				// 未知请求
				else {
					log.e(TAG, "VideoMode error: " + mode);
				}
			}
		});
	}

	private void processInviteCancelRequest(WVMInviteCancelRequest requ) {
		//收到会议邀请取消，关闭答复对话框。
		closeInviteReplyDialog();
	}

	private void processReplyRequest(WVMReplyRequest requ) {
		// 收到答复为“接受”，打开视频会议。
		if (requ.getAnswer() == 1) {
			openVideoMeeting(WebRtcSDKHelper.getInstance().getmRoomId());
			
			// 加为好友
			addFriend(WebRtcSDKHelper.getInstance().getCallNumber());
		}
	}

	/**
	 * 显示邀请回复对话框
	 * 
	 * @param
	 * @return
	 */
	private void openInviteReplyDialog(final String roomId, final String dispName, final String role, final long id, final long callNumber) {
		log.d(TAG, "openInviteReplyDialog()");

		mInviteReplyDialog = new InviteReplyDialog(mContext, dispName);
		mInviteReplyDialog.setListener(new InviteReplyDialog.Listener() {
			@Override
			public void onOk() {
				//答复：接受
				log.d(TAG, "Meeting invite reply: accept");

				YYDVideoServer.getInstance().WVMReply(true, role, id, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "inviteReply success");
						// 保存收到的RoomId
						WebRtcSDKHelper.getInstance().setRoomId(roomId);
						openVideoMeeting(roomId);
						
						// 保存拨号号码
						WebRtcSDKHelper.getInstance().setCallNumber(Long.toString(callNumber));
					}

					public void onFailed(int error) {
						log.d(TAG, "inviteReply failed, error: " + error);
					}
				});
			}

			@Override
			public void onCancel() {
				//答复：拒绝
				log.d(TAG, "Meeting invite reply: reject");

				YYDVideoServer.getInstance().WVMReply(false, role, id, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "inviteReply success");
					}

					public void onFailed(int error) {
						log.d(TAG, "inviteReply failed, error: " + error);
					}
				});
			}

			@Override
			public void onTimeout() {
				//答复超时
				log.d(TAG, "inviteReply onTimeout()");
			}
		});
		mInviteReplyDialog.setTimeout(30000);
		mInviteReplyDialog.show();
	}

	/**
	 * 关闭回复对话框
	 * 
	 * @param
	 * @return
	 */
	private void closeInviteReplyDialog() {
		if (mInviteReplyDialog != null) {
			mInviteReplyDialog.close();
			mInviteReplyDialog = null;
		}
	}

	/**
	 * 打开视频会议
	 * 
	 * @param
	 * @return
	 */
	private void openVideoMeeting(String roomId) {
		
		String roomUrl = mSharedPref.getString(mContext.getString(R.string.pref_room_server_url_key), mContext.getString(R.string.pref_room_server_url_default));

		// Video call enabled flag.
		boolean videoCallEnabled = mSharedPref.getBoolean(mContext.getString(R.string.pref_videocall_key), Boolean.valueOf(mContext.getString(R.string.pref_videocall_default)));

		// Use screencapture option.
		boolean useScreencapture = mSharedPref.getBoolean(mContext.getString(R.string.pref_screencapture_key), Boolean.valueOf(mContext.getString(R.string.pref_screencapture_default)));

		// Use Camera2 option.
		boolean useCamera2 = mSharedPref.getBoolean(mContext.getString(R.string.pref_camera2_key), Boolean.valueOf(mContext.getString(R.string.pref_camera2_default)));
		
		// Get default codecs.
		String videoCodec = mSharedPref.getString(mContext.getString(R.string.pref_videocodec_key), mContext.getString(R.string.pref_videocodec_default));
		String audioCodec = mSharedPref.getString(mContext.getString(R.string.pref_audiocodec_key), mContext.getString(R.string.pref_audiocodec_default));

		// Check HW codec flag.
		boolean hwCodec = mSharedPref.getBoolean(mContext.getString(R.string.pref_hwcodec_key), Boolean.valueOf(mContext.getString(R.string.pref_hwcodec_default)));
		
		// Check Capture to texture.
		boolean captureToTexture = mSharedPref.getBoolean(mContext.getString(R.string.pref_capturetotexture_key), Boolean.valueOf(mContext.getString(R.string.pref_capturetotexture_default)));
		
		// Check FlexFEC.
		boolean flexfecEnabled = mSharedPref.getBoolean(mContext.getString(R.string.pref_flexfec_key), Boolean.valueOf(mContext.getString(R.string.pref_flexfec_default)));
		
		// Check Disable Audio Processing flag.
		boolean noAudioProcessing = mSharedPref.getBoolean(mContext.getString(R.string.pref_noaudioprocessing_key), Boolean.valueOf(mContext.getString(R.string.pref_noaudioprocessing_default)));

		// Check Disable Audio Processing flag.
		boolean aecDump = mSharedPref.getBoolean(mContext.getString(R.string.pref_aecdump_key), Boolean.valueOf(mContext.getString(R.string.pref_aecdump_default)));
		
		// Check OpenSL ES enabled flag.
		boolean useOpenSLES = mSharedPref.getBoolean(mContext.getString(R.string.pref_opensles_key), Boolean.valueOf(mContext.getString(R.string.pref_opensles_default)));
		
		// Check Disable built-in AEC flag.
		boolean disableBuiltInAEC = mSharedPref.getBoolean(mContext.getString(R.string.pref_disable_built_in_aec_key), Boolean.valueOf(mContext.getString(R.string.pref_disable_built_in_aec_default)));

		// Check Disable built-in AGC flag.
		boolean disableBuiltInAGC = mSharedPref.getBoolean(mContext.getString(R.string.pref_disable_built_in_agc_key), Boolean.valueOf(mContext.getString(R.string.pref_disable_built_in_agc_default)));

		// Check Disable built-in NS flag.
		boolean disableBuiltInNS = mSharedPref.getBoolean(mContext.getString(R.string.pref_disable_built_in_ns_key), Boolean.valueOf(mContext.getString(R.string.pref_disable_built_in_ns_default)));
		
		// Check Enable level control.
		boolean enableLevelControl = mSharedPref.getBoolean(mContext.getString(R.string.pref_enable_level_control_key), Boolean.valueOf(mContext.getString(R.string.pref_enable_level_control_default)));

		// Get video resolution from settings.
		int videoWidth = 0;
		int videoHeight = 0;
		String resolution = mSharedPref.getString(mContext.getString(R.string.pref_resolution_key), mContext.getString(R.string.pref_resolution_default));
		String[] dimensions = resolution.split("[ x]+");
		if (dimensions.length == 2) {
			try {
				videoWidth = Integer.parseInt(dimensions[0]);
				videoHeight = Integer.parseInt(dimensions[1]);
			}
			catch (NumberFormatException e) {
				videoWidth = 0;
				videoHeight = 0;
				log.e(TAG, "Wrong video resolution setting: " + resolution);
			}
		}

		// Get camera fps from settings.
		int cameraFps = 0;
		String fps = mSharedPref.getString(mContext.getString(R.string.pref_fps_key), mContext.getString(R.string.pref_fps_default));
		String[] fpsValues = fps.split("[ x]+");
		if (fpsValues.length == 2) {
			try {
				cameraFps = Integer.parseInt(fpsValues[0]);
			}
			catch (NumberFormatException e) {
				cameraFps = 0;
				log.e(TAG, "Wrong camera fps setting: " + fps);
			}
		}

		// Check capture quality slider flag.
		boolean captureQualitySlider = mSharedPref.getBoolean(mContext.getString(R.string.pref_capturequalityslider_key), Boolean.valueOf(mContext.getString(R.string.pref_capturequalityslider_default)));
		
		// Get video and audio start bitrate.
		int videoStartBitrate = 0;
		String bitrateTypeDefault = mContext.getString(R.string.pref_maxvideobitrate_default);
		String bitrateType = mSharedPref.getString(mContext.getString(R.string.pref_maxvideobitrate_key), bitrateTypeDefault);
		//if (!bitrateType.equals(bitrateTypeDefault)) {
		if (bitrateType.equalsIgnoreCase("Manual")) {
			String bitrateValue = mSharedPref.getString(mContext.getString(R.string.pref_maxvideobitratevalue_key), mContext.getString(R.string.pref_maxvideobitratevalue_default));
			videoStartBitrate = Integer.parseInt(bitrateValue);
		}
		log.d(TAG, "bitrateTypeDefault:%s, bitrateType:%s, videoStartBitrate:%d", bitrateTypeDefault, bitrateType, videoStartBitrate);

		int audioStartBitrate = 0;
		bitrateTypeDefault = mContext.getString(R.string.pref_startaudiobitrate_default);
		bitrateType = mSharedPref.getString(mContext.getString(R.string.pref_startaudiobitrate_key), bitrateTypeDefault);
		if (!bitrateType.equals(bitrateTypeDefault)) {
			String bitrateValue = mSharedPref.getString(mContext.getString(R.string.pref_startaudiobitratevalue_key), mContext.getString(R.string.pref_startaudiobitratevalue_default));
			audioStartBitrate = Integer.parseInt(bitrateValue);
		}

		// Check statistics display option.
		boolean displayHud = mSharedPref.getBoolean(mContext.getString(R.string.pref_displayhud_key), Boolean.valueOf(mContext.getString(R.string.pref_displayhud_default)));

		boolean tracing = mSharedPref.getBoolean(mContext.getString(R.string.pref_tracing_key), Boolean.valueOf(mContext.getString(R.string.pref_tracing_default)));

		// Get datachannel options
		boolean dataChannelEnabled = mSharedPref.getBoolean(mContext.getString(R.string.pref_enable_datachannel_key), Boolean.valueOf(mContext.getString(R.string.pref_enable_datachannel_default)));
		boolean ordered = mSharedPref.getBoolean(mContext.getString(R.string.pref_ordered_key), Boolean.valueOf(mContext.getString(R.string.pref_ordered_default)));
		boolean negotiated = mSharedPref.getBoolean(mContext.getString(R.string.pref_negotiated_key), Boolean.valueOf(mContext.getString(R.string.pref_negotiated_default)));
		int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, R.string.pref_max_retransmit_time_ms_default);
	    int maxRetr = sharedPrefGetInteger(R.string.pref_max_retransmits_key, R.string.pref_max_retransmits_default);
		int id = sharedPrefGetInteger(R.string.pref_data_id_key, R.string.pref_data_id_default);
		String protocol = mSharedPref.getString(mContext.getString(R.string.pref_data_protocol_key), mContext.getString(R.string.pref_data_protocol_default));

		// Start AppRTCMobile activity.
		log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
		if (validateUrl(roomUrl)) {
			Uri uri = Uri.parse(roomUrl);
			Intent intent = new Intent(mContext, CallActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(uri);
			intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
			intent.putExtra(CallActivity.EXTRA_LOOPBACK, false);
			intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
			intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
			intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
			intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
			intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
			intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
			intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
			intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
			intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
			intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
			intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
			intent.putExtra(CallActivity.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
			intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
			intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
			intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
			intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
			intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
			intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
			intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
			intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
			intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
			intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
			intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
			intent.putExtra(CallActivity.EXTRA_CMDLINE, false);
			intent.putExtra(CallActivity.EXTRA_RUNTIME, 0);
			intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);
			if (dataChannelEnabled) {
				intent.putExtra(CallActivity.EXTRA_ORDERED, ordered);
				intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
				intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
				intent.putExtra(CallActivity.EXTRA_PROTOCOL, protocol);
				intent.putExtra(CallActivity.EXTRA_NEGOTIATED, negotiated);
				intent.putExtra(CallActivity.EXTRA_ID, id);
			}
			mContext.startActivity(intent);
		}
	}
	
	private boolean validateUrl(String url) {
		if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
			return true;
		}
		
		new AlertDialog.Builder(mContext).setTitle(mContext.getText(R.string.invalid_url_title))
				.setMessage(mContext.getString(R.string.invalid_url_text, url)).setCancelable(false)
				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).create().show();
		
		return false;
	}
	
	/**
	 * Get a value from the shared preference or from the intent, if it does not
	 * exist the default is used.
	 */
	@SuppressWarnings("unused")
	private String sharedPrefGetString(int attributeId, int defaultId) {
		return mSharedPref.getString(mContext.getString(attributeId), mContext.getString(defaultId));
	}

	/**
	 * Get a value from the shared preference or from the intent, if it does not
	 * exist the default is used.
	 */
	@SuppressWarnings("unused")
	private boolean sharedPrefGetBoolean(int attributeId, int defaultId) {
		return mSharedPref.getBoolean(mContext.getString(attributeId), Boolean.valueOf(mContext.getString(defaultId)));
	}
	
	/**
	 * Get a value from the shared preference or from the intent, if it does not
	 * exist the default is used.
	 */
	private int sharedPrefGetInteger(int attributeId, int defaultId) {
		String attributeName = mContext.getString(attributeId);
		String defaultString = mContext.getString(defaultId);
		int defaultValue = -1;
		try {
			defaultValue = Integer.parseInt(defaultString);
		}
		catch (NumberFormatException e) {
			log.e(TAG, "Wrong default for: " + attributeName + ":" + defaultString);
			return defaultValue;
		}
		
		String value = mSharedPref.getString(attributeName, defaultString);
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
			return defaultValue;
		}
	}
	
	/**
	 * 打开视频监控
	 * 
	 * @param
	 * @return
	 */
	private void openVideoMonitor(String roomId) {

	}
	
	/**
	 * 增加好友
	 * 
	 * @param number
	 * @return
	 */
	private void addFriend(String number) {
		String numberType = (number.length() >= 11) ? NumberType.Phone : NumberType.Robot;
		if (!YYDVideoServer.getInstance().existFriend(numberType, number)) {
			log.d(TAG, "Not exist frined, will addFriend, numberType:%s, number:%s", numberType, number);
			YYDVideoServer.getInstance().addFriend(numberType, Long.valueOf(number), null);
			WebRtcSDKHelper.getInstance().setHaveNewFriend(true);
		}
		else {
			log.d(TAG, "Exist friend, numberType:%s, number:%s", numberType, number);
		}
	}
}
