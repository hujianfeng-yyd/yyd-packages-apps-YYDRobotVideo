/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.agora;

import com.yongyida.robot.video.IVideoListener;
import com.yongyida.robot.video.Robot;
import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.activity.ActivityMeeting;
import com.yongyida.robot.video.activity.ActivityMonitor;
import com.yongyida.robot.video.activity.InviteReplyDialog;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.AVMInviteCancelRequest;
import com.yongyida.robot.video.command.AVMInviteRequest;
import com.yongyida.robot.video.command.AVMReplyRequest;
import com.yongyida.robot.video.command.LoginResponse;
import com.yongyida.robot.video.command.Response;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.Role;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDSDKHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * 视频监听
 * 
 */
public class AgoraVideoListener implements IVideoListener {
	private static final String TAG = "AgoraVideoListener";
	
	private Context mContext;
	private InviteReplyDialog mInviteReplyDialog;

	public AgoraVideoListener(Context context) {
		mContext = context;
	}

	/**
	 * 打开
	 * 
	 */
	public boolean open() {
		log.d(TAG, "open()");
		
		// YYDSDKHelper初始化
		YYDSDKHelper.getInstance().registerEventListener(mEventListener);
		YYDSDKHelper.getInstance().setRole(Role.Robot);
		YYDSDKHelper.getInstance().getOption().setServerAddress("localserver");
		YYDLogicServer.getInstance().autoConnect();
		
		return true;
	}
	
	/**
	 * 关闭
	 *
	 */
	public void close() {
		log.d(TAG, "close()");
		YYDSDKHelper.getInstance().unRegisterEventListener(mEventListener);
	}
	
	private EventListener mEventListener = new EventListener() {
		public void onEvent(Event event, final Object data) {
			log.d(TAG, "onEvent(), envet: " + event);
			
			switch (event) {
			case LoginResponse:
				//收到登录响应
				processLoginResponse((LoginResponse) data);
				break;
			case AVMInviteRequest:
				//收到会议邀请
				porcessInviteRequest((AVMInviteRequest)data);
				break;
			case AVMInviteCancelRequest:
				//收到会议邀请取消
				processInviteCancelRequest((AVMInviteCancelRequest)data);
				break;
			case AVMReplyRequest:
				// 收到答复
				processReplyRequest((AVMReplyRequest)data);
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
	
    private void porcessInviteRequest(final AVMInviteRequest requ) {
    	final VideoMode mode = VideoMode.getVideoMode(requ.getMode());
    	new Handler(Looper.getMainLooper()).post(new Runnable() {
			public void run() {
				// 会议邀请
		    	if (mode == VideoMode.MEETING) {
		    		openInviteReplyDialog(
							requ.getChannelId(),
							requ.getUserName(),
							requ.getRole(),
							requ.getId());
		    	}
		    	// 监控请求
		    	else if (mode == VideoMode.MONITOR) {
		    		AgoraSDKHelper.getInstance().setChannelId(requ.getChannelId());
		    		openVideoMonitor();
		    	}
		    	// 未知请求
		    	else {
		    		log.e(TAG, "VideoMode error: " + mode);
		    	}
			}
		});
    }
    
    private void processInviteCancelRequest(AVMInviteCancelRequest requ) {
    	//收到会议邀请取消，关闭答复对话框。
		closeInviteReplyDialog();
    }
    
    private void processReplyRequest(AVMReplyRequest requ) {
    	// 收到答复为“接受”，打开视频会议。
		if (requ.getAnswer() == 1) {
			openVideoMeeting(
					AgoraSDKHelper.getInstance().getChannelId(),
					AgoraSDKHelper.getInstance().getUid());
		}
    }
    
	/**
	 * 显示邀请回复对话框
	 * @param
	 * @return
	 */
	private void openInviteReplyDialog(final String channelId, 
			final String dispName, 
			final String role, 
			final long id) {
		log.d(TAG, "openInviteReplyDialog()");
		
		mInviteReplyDialog = new InviteReplyDialog(mContext, dispName);
		mInviteReplyDialog.setListener(new InviteReplyDialog.Listener() {
			@Override
			public void onOk() {
				//答复：接受
				log.d(TAG, "Meeting invite reply: accept");
				YYDLogicServer.getInstance().AVMReply(true, role, id, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "inviteReply success");
						// 保存收到的channelId
						AgoraSDKHelper.getInstance().setChannelId(channelId);
						openVideoMeeting(
								channelId,                               // 收到的channelId
								AgoraSDKHelper.getInstance().getUid()    // 自己的uid
								);
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
				YYDLogicServer.getInstance().AVMReply(false, role, id, new CmdCallBacker() {
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
	 * @param
	 * @return
	 */
	private void openVideoMeeting(String channelId, int uid) {
		Intent intent = new Intent(mContext, ActivityMeeting.class);
		intent.putExtra(ActivityMeeting.CHANNEL_ID, channelId);
		intent.putExtra(ActivityMeeting.UID, uid);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}
	
	/**
	 * 打开视频监控
	 * @param
	 * @return
	 */
	private void openVideoMonitor() {
		ActivityMonitor monitor = new ActivityMonitor(RobotApplication.getInstance().getContext());
		monitor.open();
	}
}
