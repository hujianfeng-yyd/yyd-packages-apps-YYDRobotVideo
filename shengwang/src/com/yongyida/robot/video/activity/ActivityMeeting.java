/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.comm.BaseActivity;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.sdk.CallHistory;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.FriendProvider;
import com.yongyida.robot.video.sdk.NumberType;
import com.yongyida.robot.video.sdk.PhoneUser;
import com.yongyida.robot.video.sdk.RobotUser;
import com.yongyida.robot.video.sdk.Role;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.service.TimeCountService;
import com.yongyida.robot.video.service.TimeCountService.MyBinder;
import com.yongyida.robot.video.widget.CameraView;
import com.yongyida.robot.video.widget.FlowLayout;
import com.yongyida.robot.video.widget.ISwitchView;
import com.yongyida.robot.video.widget.VideoView;
import com.yongyida.robot.video.widget.WidgetPhotoView;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.IRtcEngineEventHandler.RtcStats;
import io.agora.rtc.video.VideoCanvas;

import com.yongyida.robot.video.Config;
import com.yongyida.robot.video.ConfigProvider;
import com.yongyida.robot.video.Constant;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.agora.AgoraSDKHelper;
import com.yongyida.robot.video.av.ViewStyle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;

/**
 * 视频会议
 * 
 */
public class ActivityMeeting extends BaseActivity {
	public static final String TAG = "ActivityMeeting";
	public final static String VENDOR_KEY = "VENDOR_KEY";
    public final static String CHANNEL_ID = "channelid";
    public final static String UID = "uid";
	public static final String LOCALBROADCAST_EXIT_MEETING = "com.yongyida.robot.video.exitmeeting";
	public final static String HAVE_NEW_FRIEND = "havenewfriend";
	public final static String HAVE_NEW_HISTORY = "havenewhistory";
	public static final int MEETINGMEMBERS_MAXCOUNT = 3;
	public static final int PHONE_USER_RID_BASE = 1000000000;

	private static ActivityMeeting sInstance;
	
	private RtcEngine mRtcEngine;
	private String mVendorKey;
    private String mChannelId;
    private int mUid;
    
	private FrameLayout mFrameLayout;
	private DrawerLayout mDrawerLayout;
	private CameraView mCameraView;
	private List<VideoView> mVideoViews;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mFullViewWidth;
	private int mFullViewHeight;
	private int mThumbWidth;
	private int mThumbHeight;
	private int mVideoWidth = 320;
	private int mVideoHeight = 240;
	
	private String mMyNumber;
	private boolean mMute;
	private Chronometer mChronometer;
	private boolean mChronometerStarted;
	private long mMeetingBeginTime;
	private boolean mHaveNewFriend;
	private boolean mHaveNewHistory;
	private Handler mHandler;
	private TimeoutCloseRunnable mTimeoutCloseRunnable;
	private boolean mDestoryedWindow;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				 Log.d("testtimecount", "timeCountdown");
				ExitMeeting("Hangup");
				break;
			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sInstance = this;
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meeting);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mVendorKey = Utils.getMetaData(this, VENDOR_KEY);
		mChannelId = getIntent().getStringExtra(CHANNEL_ID);
		mUid = getIntent().getIntExtra(UID, 0);
		log.d(TAG, "ChannelId: " + mChannelId + ", Uid: " + mUid);
		
		findViewById(R.id.btn_addcall).setOnClickListener(mToolbarClickListener);
		findViewById(R.id.btn_mute).setOnClickListener(mToolbarClickListener);
		findViewById(R.id.btn_hangup).setOnClickListener(mToolbarClickListener);
		findViewById(R.id.btn_addcall_dial).setOnClickListener(mAddCallClickListener);
		findViewById(R.id.btn_addcall_cancel).setOnClickListener(mAddCallClickListener);
		
		mMyNumber = YYDSDKHelper.getInstance().getLoginUser().getCallNumber();
		mVideoWidth = Config.getInstance().getVideoSizeWidth();
		mVideoHeight = Config.getInstance().getVideoSizeHeight();
		
		mScreenWidth = Utils.getScreenWidth(this);
		mScreenHeight = Utils.getScreenHeight(this);
		mFullViewHeight = getFullViewHeight();
		mFullViewWidth = getFullViewWidth(mFullViewHeight);
		mThumbWidth = getThumbWidth();
		mThumbHeight = getThumbHeight(mThumbWidth);
		
		mFrameLayout = (FrameLayout) findViewById(R.id.view_container);
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mChronometer.setVisibility(View.INVISIBLE);
		mVideoViews = new ArrayList<VideoView>();
		
		setupRtcEngine();
		joinChannel();
		setLocalView();
		setMute(true);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setScrimColor(Color.TRANSPARENT);
		
		//发送进入视频前广播
		sendEnterVideoBroadcast();
		bindTimeCountService();
	}
	
	private void bindTimeCountService() {
		Log.d("testtimecount", "service start");
		Intent intent = new Intent(ActivityMeeting.this,TimeCountService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		
	}

	private TimeCountService service;
	ServiceConnection conn = new ServiceConnection() {
		

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			TimeCountService.MyBinder mybinder = (TimeCountService.MyBinder)binder;
			 service = mybinder.getService();
			 //service.setTime(time);
			 service.setHandler(handler);
			 service.TimeCountStart();
		}
	};
	
	
	
	
	public static ActivityMeeting getInstance() {
		return sInstance;
	}
	
	private void setupRtcEngine() {
        AgoraSDKHelper.getInstance().setRtcEngine(mVendorKey, new MessageHandler());
        mRtcEngine = AgoraSDKHelper.getInstance().getRtcEngine();
        mRtcEngine.setLogFile(Utils.getExternalStorageDirectory() + "/agora.log");
        mRtcEngine.enableVideo();
    }
	
	private void joinChannel() {
		/*
		 * 机器人使用rid做ChannelId和Uid
		 * 手机用户使用手机号码做ChannelId, 使用1000000000+ rid做Uid
		 */
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
        if (mCameraView == null) {
        	SurfaceView surfaceView = RtcEngine.CreateRendererView(getApplicationContext());
        	
        	mCameraView = new CameraView(this);
        	mCameraView.setSurfaceView(surfaceView);
    		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mThumbWidth, mThumbHeight);
    		layoutParams.setMargins(getThumbLeft(), getThumbTop(0), 0, 0);
    		mCameraView.setLayoutParams(layoutParams);
    		mCameraView.setViewStyle(ViewStyle.ThumbView);
    		mCameraView.getSurfaceView().setZOrderOnTop(true);
    		mCameraView.getSurfaceView().setZOrderMediaOverlay(true);
    		mCameraView.setTitle("Camera");
    		mFrameLayout.addView(mCameraView);
    		
    		mRtcEngine.setParameters("{\"che.video.local.camera_index\":0}");
    		int videoProfile = getVideoProfileValue(mVideoWidth);
    		log.d(TAG, "VideoWidth: " + mVideoWidth + ", VideoHeight: " + mVideoHeight + ", VideoProfile: " + videoProfile);
    		mRtcEngine.setVideoProfile(videoProfile);
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
	
	public void onFirstRemoteVideoDecoded(final int uid, int width, int height, final int elapsed) {
        log.d(TAG, "onFirstRemoteVideoDecoded: uid: " + uid + ", width: " + width + ", height: " + height);
        
        // 过滤重复消息
        final String viewKey = String.valueOf(uid);
        if (atMeeting(viewKey)) {
        	log.e(TAG, viewKey + " is already at meeting.");
			return;
        }
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	final SurfaceView surfaceView = RtcEngine.CreateRendererView(getApplicationContext());
            	VideoView videoView = new VideoView(ActivityMeeting.this, viewKey);
            	videoView.setSurfaceView(surfaceView);
            	
        		int index = mVideoViews.size();
        		int h = getUserViewHeight(index);
        		int w = getUserViewWidth(index, h);
        		int l = getUserViewLeft(index, w);
        		int t = getUserViewTop(index);
        		ViewStyle viewStyle = getInitViewStyle();
        		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(w, h);
        		layoutParams.setMargins(l, t, 0, 0);
        		videoView.setLayoutParams(layoutParams);
        		videoView.setViewStyle(viewStyle);
        		videoView.setOnClickListener(mSwitchClickListener);
        		videoView.setTitle(String.valueOf(uid));
        		// 如果是小视图，SurfaceView置前
        		if (viewStyle == ViewStyle.ThumbView) {
        			videoView.getSurfaceView().setZOrderOnTop(true);
        			videoView.getSurfaceView().setZOrderMediaOverlay(true);
        		}
        		mVideoViews.add(videoView);
        		mFrameLayout.addView(videoView);
        		
                mRtcEngine.enableVideo();
                int successCode = mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
                if (successCode < 0) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        	mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
                        	surfaceView.invalidate();
                        }
                    }, 500);
                }
                
                // 记录会议开始时间
                if (!mChronometerStarted) {
        			mChronometer.setVisibility(View.VISIBLE);
        			mChronometer.start();
        			mChronometerStarted = true;
        			mMeetingBeginTime = System.currentTimeMillis();
        		}
                
                // 刷新视图
                refreshView();
                
                // 会议成员接受了邀请，并加入会议后才会加为好友。
                String callNumber = AgoraSDKHelper.getInstance().getCallNumber();
				if (!callNumber.equals(mMyNumber)) {
					addFriend(callNumber);
					addCallHistory(callNumber);
				}
            }
        });
    }
	
	/**
	 * 用户离线
	 * @param uid
	 * @param reason
	 * @return
	 */
	public void onUserOffline(final int uid, int reason) {
		log.d(TAG, "onUserOffline(), uid: " + uid + ", reason: " + reason);
		
		final String viewKey = String.valueOf(uid);
		runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	// 用户离线，移除视频窗口
	            	removeUserView(viewKey);
	            	
	            	// 如果所有用户都已经离线，则关闭会议。
	        		if (mVideoViews.size() == 0) {
	        			ExitMeeting("VideoViews size == 0");
	        		}
	            }
		 });
	}
	
	/**
	 * 离开房间
	 * @param stats
	 * @return
	 */
	public void onLeaveChannel(RtcStats stats) {
		log.d(TAG, "onLeaveChannel(), users: " + stats.users);
		
		// 收到离开房间后关闭窗体
		Close("onLeaveChannel");
	}
	
	public void onConnectionLost() {
		log.d(TAG, "onConnectionLost()");
		
		// 收到连接断开后关闭窗体
	    Close("onConnectionLost");
	}
	
	/**
	 * 判断用户是否在会议中
	 * 
	 * @param number
	 * @return
	 */
	private boolean atMeeting(String viewKey) {
		for (VideoView v : mVideoViews) {
			if (v.getViewKey().equals(viewKey)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 刷新视图
	 * 
	 * @param
	 * @return
	 */
	private void refreshView() {
		log.d(TAG, "refreshView()");
		
		//使小图在上面
		for (View view : mVideoViews) {
			if (view instanceof ISwitchView 
					&& ((ISwitchView) view).getViewStyle() == ViewStyle.ThumbView) {
				mFrameLayout.bringChildToFront(view);
			}
		}
		mFrameLayout.bringChildToFront(mCameraView);
		mFrameLayout.bringChildToFront(mChronometer);
	}

	/**
	 * 返回大图
	 * 
	 * @param
	 * @return
	 */
	private View getBigView() {
		for (View v : mVideoViews) {
			if (v instanceof ISwitchView && ((ISwitchView) v).getViewStyle() == ViewStyle.FullView)
				return v;
		}
		return null;
	}
	
	/**
	 * 右边栏切换
	 * 
	 * @param
	 * @return
	 */
	private void toggleRightSliding() {
		log.d(TAG, "toggleRightSliding");

		if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
			mDrawerLayout.closeDrawer(Gravity.END);
		}
		else {
			fillCallList();
			mDrawerLayout.openDrawer(Gravity.END);
		}
	}
	
	private String getViewKey(User user) {
		if (user instanceof PhoneUser) {
			return  String.valueOf(PHONE_USER_RID_BASE + user.getId());
		}
		else {
			return String.valueOf(user.getId());
		}
	}

	/**
	 * 填充可添加通话列表
	 * 
	 * @param
	 * @return
	 *
	 */
	private void fillCallList() {
		FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
		layout.clear();

		User myself = YYDSDKHelper.getInstance().getLoginUser();
		String userName = "";
		String dispName = "";
		String defaultName = getString(R.string.default_name);
		List<User> friends = YYDLogicServer.getInstance().getFriendList();
		
		for (User user : friends) {
			// 排除自已和视频中用户
			String viewKey = getViewKey(user);
			if ((myself != null && user.equals(myself))
					|| atMeeting(viewKey))
				continue;
			
			userName = user.getUserName();
			if (!TextUtils.isEmpty(userName)) {
				if (!userName.equals(defaultName))
					dispName = userName;
				else
					dispName = userName + user.getCallNumber();
			}
			else {
				dispName = user.getCallNumber();
			}
			
			WidgetPhotoView view = new WidgetPhotoView(this);
			view.setUser(user);
			view.setImageResource(R.drawable.ic_photo_default3);
			view.setSelectedColor(Color.YELLOW);
			view.setText(dispName);
			view.setOnClickListener(mContactClickListener);
			layout.addView(view);
		}
	}

	/**
	 * 切换视图点击监听
	 */
	private OnClickListener mSwitchClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v instanceof ISwitchView && ((ISwitchView) v).getViewStyle() == ViewStyle.ThumbView) {
				View bigView = getBigView();
				switchView(v, bigView);
			}
		}
	};

	/**
	 * 切换视图
	 * 
	 * @param smallView
	 * @param bigView
	 * @return
	 */
	private void switchView(View smallView, View bigView) {
		log.d(TAG, "switchView(), smallView: " + smallView + ", bigView: " + bigView);

		// 大视图变为小视图
		FrameLayout.LayoutParams oldSp = (FrameLayout.LayoutParams) smallView.getLayoutParams();
		FrameLayout.LayoutParams oldBp = null;
		if (bigView != null) {
			oldBp = (FrameLayout.LayoutParams) bigView.getLayoutParams();

			FrameLayout.LayoutParams newBp = new FrameLayout.LayoutParams(oldSp);
			bigView.setLayoutParams(newBp);
			if (bigView instanceof ISwitchView) {
				ISwitchView bv = (ISwitchView) bigView;
				bv.setViewStyle(ViewStyle.ThumbView);
				bv.getSurfaceView().setZOrderOnTop(true);
				bv.getSurfaceView().setZOrderMediaOverlay(true);
			}
		}
		else {
			oldBp = new FrameLayout.LayoutParams(mFullViewWidth, mFullViewHeight);
			oldBp.setMargins(mScreenWidth - mFullViewWidth, 0, 0, 0);
		}

		// 小视图变为大视图
		FrameLayout.LayoutParams newSp = new FrameLayout.LayoutParams(oldBp);
		smallView.setLayoutParams(newSp);
		if (smallView instanceof ISwitchView) {
			ISwitchView sv = (ISwitchView) smallView;
			sv.setViewStyle(ViewStyle.FullView);
			sv.getSurfaceView().setZOrderOnTop(false);
			sv.getSurfaceView().setZOrderMediaOverlay(false);
		}

		// 刷新，把小视图（原来的大视图）置前
		refreshView();
	}
	
	/**
	 * 移除摄像头视图
	 * 
	 * @param
	 * @return
	 */
	public void removeCameraView() {
		log.d(TAG, "removeCameraView()");
		
		mFrameLayout.removeView(mCameraView);
	}

	/**
	 * 移除用户视图
	 * 
	 * @param viewKey
	 * @return
	 */
	private void removeUserView(String viewKey) {
		log.d(TAG, "removeUserView(), viewKey: " + viewKey);

		VideoView view = getUserView(viewKey);
		if (view != null) {
			mVideoViews.remove(view);
			mFrameLayout.removeView(view);
			
			if (mVideoViews.size() > 0) {
				reLayoutUserViews();
			}
		}
		else {
			log.e(TAG, "Not found user view, viewKey: " + viewKey);
		}
	}
	
	/**
	 * 重新布局用户视图
	 * @param
	 * @return
	 */
	public void reLayoutUserViews() {
		boolean haveFullView = false;
		for (View v : mVideoViews) {
			if (v instanceof ISwitchView 
					&& ((ISwitchView) v).getViewStyle() == ViewStyle.FullView) {
				haveFullView = true;
			}
		}
		
		// 如果没有大图把第一个做大图
		if (!haveFullView) {
			VideoView fullView = mVideoViews.get(0);
			fullView.setViewStyle(ViewStyle.FullView);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mFullViewWidth, mFullViewHeight);
			layoutParams.setMargins(getFullViewLeft(), getFullViewTop(), 0, 0);
			fullView.setLayoutParams(layoutParams);
		}
		
		// 小图
		int index = 1;
		for (View v : mVideoViews) {
			if (v instanceof ISwitchView 
					&& ((ISwitchView) v).getViewStyle() == ViewStyle.ThumbView) {
				VideoView videoView = (VideoView)v;
				int l = getThumbLeft();
				int t = getThumbTop(index);
				int w = getThumbWidth();
				int h = getThumbHeight(w);
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(w, h);
				layoutParams.setMargins(l, t, 0, 0);
				videoView.setLayoutParams(layoutParams);
				++index;
			}
		}
	}

	/**
	 * 返回用户视图
	 * 
	 * @param viewKey
	 * @return
	 */
	private VideoView getUserView(String viewKey) {
		for (View v : mVideoViews) {
			if (v instanceof VideoView) {
				VideoView uv = (VideoView) v;
				if (uv.getViewKey().equals(viewKey)) {
					return uv;
				}
			}
		}
		return null;
	}

	/**
	 * 返回视图样式
	 * 
	 * @param
	 * @return
	 */
	private ViewStyle getInitViewStyle() {
		return (mVideoViews.size() == 0) ? ViewStyle.FullView : ViewStyle.ThumbView;
	}

	/**
	 * 返回大图左边距
	 * 
	 * @param
	 * @return int
	 */
	private int getFullViewLeft() {
		return mScreenWidth - mFullViewWidth;
	}

	/**
	 * 返回大图上边距
	 * 
	 * @param
	 * @return int
	 */
	private int getFullViewTop() {
		return 0;
	}
	
	/**
	 * 返回大图高度
	 * 
	 * @param
	 * @return int
	 */
	private int getFullViewHeight() {
		return mScreenHeight;
	}

	/**
	 * 返回大图宽度
	 * 
	 * @param
	 * @return int
	 */
	private int getFullViewWidth(int height) {
		int width = (int) ((((float) mVideoWidth) / mVideoHeight) * height);
		//避免大图宽等于屏幕宽时小图宽为0
		if (width >= mScreenWidth) {
			width = mScreenWidth - 160;
		}
		return width;
	}
	
	/**
	 * 返回缩略图左边距
	 * 
	 * @param
	 * @return int
	 */
	private int getThumbLeft() {
		return (mScreenWidth - mFullViewWidth - mThumbWidth) / 2;
	}
	
	/**
	 * 返回缩略图上边距
	 * 
	 * @param
	 * @return int
	 */
	private int getThumbTop(int index) {
		return index * (mThumbHeight + 6);
	}
	
	/**
	 * 返回缩略图宽度
	 * 
	 * @param
	 * @return int
	 */
	private int getThumbWidth() {
		return mScreenWidth - mFullViewWidth - 10;
	}

	/**
	 * 返回缩略图高度
	 * 
	 * @param
	 * @return int
	 */
	private int getThumbHeight(int w) {
		return (int) (((float) mVideoHeight / mVideoWidth) * w);
	}

	/**
	 * 返回用户视图左边距
	 * 
	 * @param width
	 * @return int
	 */
	private int getUserViewLeft(int index, int width) {
		return (index == 0) ? getFullViewLeft() : getThumbLeft();
	}

	/**
	 * 返回用户视图上边距
	 * 
	 * @param
	 * @return int
	 */
	private int getUserViewTop(int index) {
		return (index == 0) ? getFullViewTop() : (mVideoViews.size() * (mThumbHeight + 5));
	}

	/**
	 * 返回用户视图高
	 * 
	 * @param
	 * @return
	 */
	private int getUserViewHeight(int index) {
		return (index == 0) ? mFullViewHeight : mThumbHeight;
	}
	
	/**
	 * 返回视图宽
	 * @param index 索引
	 * @param height 高
	 * @return
	 */
	private int getUserViewWidth(int index, int height) {
		return (index == 0) ? mFullViewWidth : mThumbWidth;
	}
	
	/**
	 * 工具栏按扭点击监听
	 * 
	 */
	private OnClickListener mToolbarClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_addcall:
				showAddCall();
				break;
			case R.id.btn_mute:
				switchMute();
				break;
			case R.id.btn_hangup:
				ExitMeeting("Hangup");
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 联系人点击事件
	 * 
	 */
	private OnClickListener mContactClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 取消已选中的项
			unSelectedAll();
			
			WidgetPhotoView pv = (WidgetPhotoView) v;
			pv.setSelected(!pv.getSelected());
		}
	};

	/**
	 * 添加通话点击事件
	 * 
	 */
	private OnClickListener mAddCallClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_addcall_dial:
				addCall_dial();
				break;
			case R.id.btn_addcall_cancel:
				addCall_close();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 添加通话
	 * 
	 * @param
	 * @return
	 */
	private void addCall_dial() {
		FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
		int count = layout.getChildCount();
		for (int i = 0; i < count; ++i) {
			if (mVideoViews.size() >= MEETINGMEMBERS_MAXCOUNT) {
				Utils.toast(this, this.getString(R.string.meetingmember_cannot_morethan_four));
				break;
			}
			
			View view = layout.getChildAt(i);
			if (view instanceof WidgetPhotoView) {
				WidgetPhotoView wpv = (WidgetPhotoView) view;
				if (wpv.getSelected()) {
					User user = wpv.getUser();
					meetingInvite(user);
				}
			}
		}
		
		// 拨号完成后关闭
		addCall_close();
	}
	
	public void meetingInvite(User user) {
		log.d(TAG, "meetingInvite(), " + user);
		
		final String numberType = user.getNumberType();
		final String callNumber = user.getCallNumber();
		final long number = Long.valueOf(callNumber);
		String channelId = AgoraSDKHelper.getInstance().getChannelId();
		YYDLogicServer.getInstance().AVMInvite(numberType, number, channelId, new CmdCallBacker() {
			public void onSuccess(Object arg) {
				log.d(TAG, "AVMInvite success");
			}
			public void onFailed(int error) {
				log.d(TAG, "AVMInvite failed, error: " + error);
			}
		});
	}
	
	/**
	 * 关闭添加通话侧边框
	 * 
	 * @param
	 * @return
	 */
	private void addCall_close() {
		toggleRightSliding();
		
		// 取消已选中的项
		unSelectedAll();
	}
	
	/**
	 * 取消所有选中项
	 * @param
	 * @return
	 */
	private void unSelectedAll() {
		FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
		int count = layout.getChildCount();
		for (int i = 0; i < count; ++i) {
			View view = layout.getChildAt(i);
			if (view instanceof WidgetPhotoView) {
				WidgetPhotoView wpv = (WidgetPhotoView) view;
				wpv.setSelected(false);
			}
		}
	}
	
	/**
	 * 显示添加通话侧边栏
	 * 
	 * @param
	 * @return
	 */
	private void showAddCall() {
		toggleRightSliding();
	}

	/**
	 * 静音切换
	 * 
	 * @param
	 * @return
	 *
	 */
	private void switchMute() {
		log.d(TAG, "switchMute()");
		
		setMute(!isMute());
	}
	
	/**
	 * 退出会议
	 * 
	 * @param
	 * @return
	 *
	 */
	private void ExitMeeting(String reason) {
		log.d(TAG, "ExitMeeting() for " + reason);
		
		// 离开房间
		mRtcEngine.leaveChannel();
		
		// 如果超时未关闭窗口，则强制关闭。
		openTimeoutCheck(1000*5);
	}
	
	/**
	 * 打开超时检测
	 * @param timeout
	 * @return
	 */
	private void openTimeoutCheck(int timeout) {
		log.d(TAG, "openTimeoutCheck()");
		
		mHandler = new Handler();
		mTimeoutCloseRunnable = new TimeoutCloseRunnable();
		mHandler.postDelayed(mTimeoutCloseRunnable, timeout);
	}
	
	/**
	 * 关闭超时检测
	 * @param
	 * @return
	 */
	private void closeTimeoutCheck() {
		log.d(TAG, "closeTimeoutCheck()");
		
		if (mHandler != null && mTimeoutCloseRunnable != null) {
			mHandler.removeCallbacks(mTimeoutCloseRunnable);
		}
	}
	
	private class TimeoutCloseRunnable implements Runnable {
		public void run() {
			// 如果到了时间窗口还没有关闭，强制关闭。
		    if (!mDestoryedWindow) {
		    	log.e(TAG, "Windows isn't destory Timeout not, will force close().");
		    	Close("CloseTimeout");
		    }
		}
	}
	
	/**
	 * 增加好友
	 * 
	 * @param number
	 * @return
	 */
	private void addFriend(String number) {
		log.d(TAG, "addFriend(), number: " + number);

		String numberType = NumberType.Robot;
		if (number.length() >= 11) {
			numberType = NumberType.Phone;
		}

		if (!existFriend(numberType, number)) {
			log.d(TAG, "Not exist frined, numberType: " + numberType + ", number: " + number);
			YYDLogicServer.getInstance().addFriend(numberType, Long.valueOf(number), null);
			mHaveNewFriend = true;
		}
		else {
			log.d(TAG, "Exist friend, numberType: " + numberType + ", number: " + number);
		}
	}

	/**
	 * 增加通话历史
	 * 
	 * @param strNumber
	 * @return
	 */
	private void addCallHistory(String strNumber) {
		log.d(TAG, "addCallHistory(), number: " + strNumber);

		Long number = 0L;
		try {
			number = Long.valueOf(strNumber);
		}
		catch (Exception e) {
			log.e(TAG, "Cast number[" + strNumber + "] to long exceptiong: " + e);
			return;
		}

		User user = null;
		if (strNumber.length() >= 11) {
			// 返回手机用户
			user = FriendProvider.getUser(this, strNumber);
			if (user == null) {
				user = new PhoneUser(-1, strNumber, strNumber);
			}
		}
		else {
			user = FriendProvider.getUser(this, Role.Robot, number);
			if (user == null) {
				user = new RobotUser(number, strNumber);
			}
		}

		YYDSDKHelper.getInstance().addCallHistory(new CallHistory(user, System.currentTimeMillis()));
		mHaveNewHistory = true;
	}
	
	/**
	 * 好友是否存在
	 * 
	 * @param numberType
	 * @param number
	 * @return
	 */
	private boolean existFriend(String numberType, String number) {
		if (number != null) {
			if (numberType.equalsIgnoreCase(NumberType.Phone)) {
				return YYDLogicServer.getInstance().existFriend(number);
			}
			else if (numberType.equalsIgnoreCase(NumberType.Robot)) {
				return YYDLogicServer.getInstance().existFriend(Role.Robot, Long.valueOf(number));
			}
		}
		return false;
	}
	
	/**
	 * 准备关闭
	 * @param
	 * @return
	 */
	private void Close(String reason) {
		log.d(TAG, "Close() for " + reason);
		
		closeTimeoutCheck();
		mChronometer.stop();
		
		// 会议信息上报
		meetingReport();
		AgoraSDKHelper.getInstance().exitMeeting();
		
		// 发送退出视频前广播
		sendExitVideoBroadcast();
		sendExitMeetingBroadcast();
		
		finish();
	}
	
	/**
	 * 返回MIC是否静音
	 * 
	 * @param
	 * @return boolean
	 * 
	 */
	public boolean isMute() {
		return mMute;
	}

	/**
	 * 设置静音
	 * 
	 * @param mute
	 * @return
	 */
	public void setMute(boolean mute) {
		log.d(TAG, "setMute(), mute: " + mute);
		mMute = mute;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mRtcEngine.muteLocalAudioStream(mMute);
				findViewById(R.id.btn_mute).setBackgroundResource(
						mMute ? R.drawable.ic_toolbar_mute_checked : R.drawable.ic_toolbar_mute);
			}
		});
	}
	
	/**
	 * 返回扬声器是否开启
	 * 
	 * @param
	 * @return boolean
	 * 
	 */
	public boolean isSpeakerOn() {
		return mRtcEngine.isSpeakerphoneEnabled();
	}
	
	/**
	 * 设置扬声器打开或关闭
	 * 
	 * @param speakerOn
	 * @return
	 * 
	 */
	public void setSpeakerOn(boolean speakerOn) {
		log.d(TAG, "setSpeakerOn: " + speakerOn);
		
		mRtcEngine.setEnableSpeakerphone(speakerOn);
	}
	
	/**
	 * 发送进入视频广播
	 * @param
	 * @return
	 */
	private void sendEnterVideoBroadcast() {
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "true");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_ENTERVIDEO));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_ENTERVIDEO);
	}
	
	/**
	 * 发送退出视频广播
	 * @param
	 * @return
	 */
	private void sendExitVideoBroadcast() {
		ConfigProvider.update(Constant.PROVIDER_CONFIG_ITEM_VIDEOING, "false");
	    log.d(TAG, "video status: " + ConfigProvider.query(Constant.PROVIDER_CONFIG_ITEM_VIDEOING));
		sendBroadcast(new Intent(Constant.GLOBAL_BROADCAST_ROBOT_EXITVIDEO));
		log.d(TAG, "Send: " + Constant.GLOBAL_BROADCAST_ROBOT_EXITVIDEO);
	}
	
	/**
	 * 发送退出会议广播
	 * @param
	 * @return
	 */
	private void sendExitMeetingBroadcast() {
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(LOCALBROADCAST_EXIT_MEETING)
				.putExtra(HAVE_NEW_FRIEND, mHaveNewFriend)
				.putExtra(HAVE_NEW_HISTORY, mHaveNewHistory));
	}
	
	/**
	 * 会议信息上报
	 * @param
	 * @return
	 */
	private void meetingReport() {
		log.d(TAG, "meetingReport()");
		
		long meetingEndTime = System.currentTimeMillis();
		if (mMeetingBeginTime != 0  && (meetingEndTime - mMeetingBeginTime) > 1000) {
			YYDLogicServer.getInstance().meetingReport(
					Role.Robot,
					Long.valueOf(mMyNumber),
					AgoraSDKHelper.getInstance().getMeetingId(),
					Utils.getLongTimeString("yyyy-MM-dd HH:mm:ss", mMeetingBeginTime),
					Utils.getLongTimeString("yyyy-MM-dd HH:mm:ss", meetingEndTime),
					(int)Math.ceil((meetingEndTime - mMeetingBeginTime) / 1000.0),  //会议时长，单位：秒
					0f,  // 使用流量，单位：MB
					null);
		}
	}
	
	/**
     * 监听Back键按下事件
     * 返回值表示: 是否能完全处理该事件. true, 表示不会继续传播该事件, false, 会继续传播该事件
     * 
     */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {
              log.d(TAG, "keyCode == KEYCODE_BACK");
              return true;
         }
         
         return super.onKeyDown(keyCode, event);
     }
     
	/**
	 * 窗体销毁
	 * 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		log.d(TAG, "onDestroy()");
		mDestoryedWindow = true;
		sInstance = null;
		service.stopTimeCountService();
		service.stopSelf();
		Log.d("testtimecount", "service end");
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
			ActivityMeeting.this.onConnectionLost();
		}
		
		public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
			log.d(TAG, "onFirstLocalVideoFrame(), width: " + width + ", height: " + height);
		}

		public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
			log.d(TAG, "onFirstRemoteVideoFrame(), uid: " + uid + ", width: " + width + ", height: " + height);
		}
		
		public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
			log.d(TAG, "onFirstRemoteVideoDecoded(), uid: " + uid + ", width: " + width + ", height: " + height);
			ActivityMeeting.this.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
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
			ActivityMeeting.this.onLeaveChannel(stats);
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
			ActivityMeeting.this.onUserOffline(uid, reason);
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
