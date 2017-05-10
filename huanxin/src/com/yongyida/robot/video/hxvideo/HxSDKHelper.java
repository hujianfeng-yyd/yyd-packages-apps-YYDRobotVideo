/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co.,Ltd. All rights reserved.
 * 
 * @author: hujianfeng@gmail.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.hxvideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.chat.EMConversation.EMConversationType;
import com.easemob.exceptions.EaseMobException;
import com.yongyida.robot.video.Config;
import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.utils.Utils;
import com.yongyida.robot.video.utils.log;

import java.io.File;

/**
 * SDK助手类
 *
 */
public class HxSDKHelper {
	private static final String TAG = HxSDKHelper.class.getSimpleName();
	private static HxSDKHelper sInstance;

	private boolean mSdkInited = false;
	protected Context mContext;
	private EMConnectionListener mHxConnectListener;
	protected EMEventListener mEventListener;
	private BroadcastReceiver mCallReceiver;

	private String mChatUsername;
	private EMConversation mConversation;

	private HxVideoMode mVideoMode = HxVideoMode.MONITOR;
	private VideoFloatWindow mFloatWindow;
	private int mRotateAngle = 0;
	private ConnectListener mConnectListener;
	
	private HxSDKHelper() {
	}

	/**
	 * 返回单例
	 */
	public static synchronized HxSDKHelper getInstance() {
		if (sInstance == null) {
			sInstance = new HxSDKHelper();
		}
		return sInstance;
	}

	/**
	 * SDK初始化函数
	 * 
	 */
	public synchronized boolean init(Context context) {
		log.d(TAG, "HX init()");

		if (mSdkInited)
			return true;

		mContext = context;
		EMChat.getInstance().setAutoLogin(false);
		EMChat.getInstance().init(mContext);
		EMChat.getInstance().setDebugMode(false);

		initHXOptions();
		initVideoMode();

		mSdkInited = true;
		return true;
	}

	public HxVideoMode getVideoMode() {
		return mVideoMode;
	}

	public void setVideoMode(HxVideoMode hxVideoMode) {
		mVideoMode = hxVideoMode;
	}

	private void initVideoMode() {
		setVideoMode(HxVideoMode.getVideoMode(Config.getInstance().getVideoMode()));
	}

	/**
	 * SDK去初始化
	 * 
	 */
	public void uninit() {
		log.d(TAG, "uninit()");
	}

	public boolean isInited() {
		return mSdkInited;
	}

	/**
	 * 选项配置
	 * 
	 */
	private void initHXOptions() {
		// 获取到EMChatOptions对象
		EMChatOptions options = EMChatManager.getInstance().getChatOptions();

		// 添加好友时是否需要验证
		options.setAcceptInvitationAlways(false);

		// 默认是不维护好友关系列表的，如果app依赖的好友关系，把这个属性设置为true
		options.setUseRoster(true);

		// 设置是否需要已读回执
		options.setRequireAck(false);

		// 设置是否需要已送达回执
		options.setRequireDeliveryAck(false);

		// 设置从db初始化加载时, 每个conversation需要加载msg的个数
		options.setNumberOfMessagesLoaded(0);
	}

	/**
	 * 返回应用程序上下文
	 */
	public Context getAppContext() {
		return mContext;
	}

	/**
	 * 登录
	 *
	 */
	public boolean connect(String userName, String password) {
		log.d(TAG, "connect()");

		int ret = -1;

		// 如果没有注册或登录名与注册名称不同，则必须先注册。
		String loginedName = Config.getInstance().getRobotId();
		if (!Config.getInstance().getRobotIsRegisted() || !userName.equals(loginedName)) {
			ret = new HxUserRegister().register(userName, password);
			if (ret == EMError.NO_ERROR || ret == EMError.USER_ALREADY_EXISTS) {
				// 注册成功后保存
				Config.getInstance().setRobotIsRegisted(true);
				Config.getInstance().setRobotId(userName);
			}
			else {
				log.e(TAG, "Register hxuser failed !");
				return false;
			}
		}

		ret = new HxUserLogin().login(userName, password);
		if (ret != 0) {
			log.e(TAG, "Hxuser login failed !");
			return false;
		}

		return true;
	}

	/**
	 * 登出
	 * 
	 */
	public void logout() {
		log.d(TAG, "logout()");

		if (isLogined()) {
			EMChatManager.getInstance().logout(true, new EMCallBack() {
				@Override
				public void onSuccess() {
					log.d(TAG, "logout, onSuccess()");
					if (mConnectListener != null) {
						mConnectListener.onChanged(false);
					}
				}

				@Override
				public void onError(int code, String message) {
					log.d(TAG, "logout, onError()");
				}

				@Override
				public void onProgress(int progress, String status) {
				}
			});
		}
	}

	/**
	 * 检查是否已经登录过
	 * 
	 */
	public boolean isLogined() {
		return EMChat.getInstance().isLoggedIn();
	}

	/**
	 * 打开
	 *
	 */
	public void startListener() {
		log.d(TAG, "startListener()");

		initConnectListener();
		initEventListener();
		initCallReceiver();

		notifyForRecevingEvents();
	}

	/**
	 * 关闭
	 *
	 */
	public void stopListener() {
		log.d(TAG, "stopListener()");

		if (mHxConnectListener != null) {
			EMChatManager.getInstance().removeConnectionListener(mHxConnectListener);
			mHxConnectListener = null;
		}

		if (mEventListener != null) {
			EMChatManager.getInstance().unregisterEventListener(mEventListener);
			mEventListener = null;
		}

		if (mContext != null && mCallReceiver != null) {
			mContext.unregisterReceiver(mCallReceiver);
			mCallReceiver = null;
		}
	}

	/**
	 * 初始化连接监听
	 */
	private void initConnectListener() {
		log.d(TAG, "initConnectListener()");

		mHxConnectListener = new EMConnectionListener() {
			@Override
			public void onConnected() {
				log.d(TAG, "Hx onConnectionConnected()");
				if (mConnectListener != null) {
					mConnectListener.onChanged(true);
				}
			}

			@Override
			public void onDisconnected(int error) {
				if (error == EMError.USER_REMOVED) {
					log.e(TAG, "Hx onCurrentAccountRemoved()");
				}
				else if (error == EMError.CONNECTION_CONFLICT) {
					log.e(TAG, "Hx onConnectionConflict()");
				}
				else {
					log.e(TAG, "Hx onConnectionDisconnected()");
					
					// 此处不用自己重连，有自动重连机制
					if (Utils.isNetWorkConnected(mContext))
						log.d(TAG, "Network is ok !");
					else
						log.e(TAG, "Network disconnect !");
				}
				if (mConnectListener != null) {
					mConnectListener.onChanged(false);
				}
			}
		};

		EMChatManager.getInstance().addConnectionListener(mHxConnectListener);
	}

	/**
	 * 事件监听
	 * 
	 */
	private void initEventListener() {
		log.d(TAG, "initEventListener()");

		mEventListener = new EMEventListener() {
			@Override
			public void onEvent(EMNotifierEvent event) {
				if (event.getData() instanceof EMMessage) {
					EMMessage message = (EMMessage) event.getData();
					String newUser = message.getFrom();
					loadConversation(newUser);
					log.d(TAG, "HX receive a event:" + event.getEvent() + ", from:" + newUser);

					switch (event.getEvent()) {
					case EventNewMessage:
						handleNewMessage(message);
						break;
					case EventNewCMDMessage:
						log.d(TAG, "收到透传消息");
						handleCmdMessage(message);
						break;
					case EventOfflineMessage:
						log.d(TAG, "收到离线消息");
						break;
					default:
						break;
					}
				}
			}
		};
		EMChatManager.getInstance().registerEventListener(mEventListener);
	}

	public void setConnectListener(ConnectListener listener) {
		mConnectListener = listener;
	}

	private void handleNewMessage(EMMessage message) {
		switch (message.getType()) {
		case TXT:
			log.d(TAG, "收到文本消息");
			handleTextMessage(message);
			break;
		case IMAGE:
			log.d(TAG, "收到图片消息");
			break;
		case VOICE:
			log.d(TAG, "收到语音消息");
			handleVoiceMessage(message);
			break;
		case LOCATION:
			log.d(TAG, "收到位置消息");
			break;
		case VIDEO:
			log.d(TAG, "收到视频消息");
			break;
		case FILE:
			log.d(TAG, "收到文件消息");
			break;
		default:
			log.d(TAG, "收到" + message.getType() + "消息");
			break;
		}
	}

	private void handleTextMessage(EMMessage message) {
		TextMessageBody txtBody = (TextMessageBody) message.getBody();
		String msg = txtBody.getMessage();
		log.d(TAG, "message text: " + msg);
	}

	public void handleVoiceMessage(EMMessage message) {
	}

	private void handleCmdMessage(EMMessage message) {
		// 获取消息body
		CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
		String action = cmdMsgBody.action;
		log.d(TAG, "CmdMessage action: " + action);
		
		// 视频模式
		if (action.equals(Constant.EXTCMD_VIDEO_VIDEOMODE)) {
			try {
				String strMode = message.getStringAttribute("mode");
				log.d(TAG, "video mode: " + strMode);
				mVideoMode = HxVideoMode.getVideoMode(strMode);
				Config.getInstance().setVideoMode(mVideoMode.toString());
			}
			catch (EaseMobException e) {
				log.e(TAG, e.getMessage());
			}
		}
		// 视频旋转角度
		else if (action.equals(Constant.EXTCMD_VIDEO_ROTATE)) {
			try {
				int angle = message.getIntAttribute("angle");
				log.d(TAG, "angle: " + angle);

				if (angle == 0 || angle == 90 || angle == 180 || angle == 270) {
					mRotateAngle = angle;
				}
			}
			catch (EaseMobException e) {
				log.e(TAG, e.getMessage());
			}
		}
		// 关闭视频
		else if (action.equals(Constant.EXTCMD_VIDEO_CLOSE)) {
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
			Intent intent = new Intent();
			intent.setAction(Constant.EXTCMD_VIDEO_CLOSE);
			localBroadcastManager.sendBroadcast(intent);
		}
		// 静音
		else if (action.equals(Constant.EXTCMD_VIDEO_MUTE)) {
			boolean mute = true;
			try {
				mute = message.getBooleanAttribute("mute");
			}
			catch (EaseMobException e) {
				log.e(TAG, e.getMessage());
			}
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
			Intent intent = new Intent();
			intent.setAction(Constant.EXTCMD_VIDEO_MUTE);
			intent.putExtra("mute", mute);
			localBroadcastManager.sendBroadcast(intent);
		}
		else {
			log.e(TAG, "Error action: " + action);
		}
	}

	/**
	 * 初始化来电接收广播
	 * 
	 */
	private void initCallReceiver() {
		log.d(TAG, "initCallReceiver()");

		mCallReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				log.d(TAG, "CallReceiver, onReceive()");

				String username = intent.getStringExtra("from");
				String callType = intent.getStringExtra("type");
				if (callType.equals("video")) {
					log.d(TAG, "Recv a video call");

					if (mVideoMode == HxVideoMode.CHAT) {
						// 视频聊天
						context.startActivity(new Intent(context, VideoCallActivity.class)
								.putExtra("username", username)
								.putExtra("isComingCall", true)
								.putExtra("rotateangle", mRotateAngle)
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
					else {
						// 视频控制
						mFloatWindow = new VideoFloatWindow(context);
						mFloatWindow.setRotateAngle(mRotateAngle);
						mFloatWindow.open();
					}
				}
				else {
					log.d(TAG, "Recv a voice call");
				}
			}
		};

		IntentFilter filter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
		mContext.registerReceiver(mCallReceiver, filter);
	}

	/**
	 * 通知sdkUI初始化完毕，已注册了相应的receiver和listener, 可以接受broadcast了。
	 *
	 */
	private synchronized void notifyForRecevingEvents() {
		EMChat.getInstance().setAppInited();
	}

	/**
	 * 设置当前聊天用户
	 *
	 */
	private void loadConversation(String chatUser) {
		if (!TextUtils.isEmpty(chatUser) && !chatUser.equals(mChatUsername)) {
			mChatUsername = chatUser;
			mConversation = EMChatManager.getInstance().getConversationByType(mChatUsername, EMConversationType.Chat);
			mConversation.markAllMessagesAsRead();
		}
	}

	/**
	 * 发送文本消息
	 * 
	 */
	public boolean sendText(String text) {
		log.d(TAG, "sendText: " + text + ", to: " + mChatUsername);

		if (TextUtils.isEmpty(text)) {
			log.e(TAG, "Send text empty");
			return false;
		}

		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
		TextMessageBody txtBody = new TextMessageBody(text);
		message.addBody(txtBody);
		message.setReceipt(mChatUsername);
		mConversation.addMessage(message);
		EMChatManager.getInstance().sendMessage(message, null);

		return true;
	}

	/**
	 * 发送语音消息
	 * 
	 */
	public boolean sendVoice(String path, int len) {
		log.d(TAG, "sendVoice: " + path + ", to: " + mChatUsername);

		if (!(new File(path).exists())) {
			log.e(TAG, "File not exist");
			return false;
		}

		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.VOICE);
		VoiceMessageBody body = new VoiceMessageBody(new File(path), len);
		message.addBody(body);
		message.setReceipt(mChatUsername);
		mConversation.addMessage(message);
		EMChatManager.getInstance().sendMessage(message, null);

		return true;
	}
}
