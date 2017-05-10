package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.comm.BaseActivity;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.sdk.CallHistory;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.NumberType;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDSDKHelper;

import java.util.List;

import com.yongyida.robot.video.activity.CallHistoryAdapter.OnListItemClickListener;
import com.yongyida.robot.video.agora.AgoraSDKHelper;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.RobotApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityDialByNumber extends BaseActivity {
	public static final String TAG = "ActivityDialByNumber";

	private ListView mLvHistory;
	private List<CallHistory> mCallHistoryList;
	private TextView mTvNumber;
	private SoundPool mSoundPool;
    private int mSoundId;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dial_number);
		initHistoryList();
		initDialPlate();
		registerLocalBroadcastReceiver();
		
		mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		mSoundId = mSoundPool.load(this, R.raw.click, 1);
		
		RobotApplication.getInstance().checkEnvError();
	}
	
	private void initHistoryList() {
		mCallHistoryList = YYDSDKHelper.getInstance().getCallHistoryList();
		mLvHistory = (ListView) findViewById(R.id.friend_listview);
		CallHistoryAdapter adapter = new CallHistoryAdapter(this, mCallHistoryList);
		adapter.setListItemClickListener(mOnListItemClickListener);
		mLvHistory.setAdapter(adapter);
	}

	private void initDialPlate() {
		mTvNumber = (TextView) findViewById(R.id.tv_number);
		findViewById(R.id.btn_back).setOnClickListener(backClick);
		findViewById(R.id.btn_backspace).setOnClickListener(backSpaceClick);
		findViewById(R.id.btn_number_one).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_two).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_three).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_four).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_five).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_six).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_seven).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_eight).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_nine).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_asterisk).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_zero).setOnClickListener(numberClick);
		findViewById(R.id.btn_number_pound).setOnClickListener(numberClick);
		findViewById(R.id.btn_dial).setOnClickListener(dialClick);
	}
	
	private OnListItemClickListener mOnListItemClickListener = new OnListItemClickListener() {
		public void onClick(int position, CallHistory history) {
			User user = history.getUser();
			if (user != null) {
				meetingInvite(
						user.getNumberType(),
						user.getCallNumber(),
						!TextUtils.isEmpty(user.getUserName()) ? user.getUserName() : getString(R.string.default_name));
			}
		}
	};
	
	private OnClickListener backClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			finish();
		}
	};

	private OnClickListener backSpaceClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			playPressKeySound();
			
			String numbers = mTvNumber.getText().toString();
			if (numbers.length() > 0) {
				numbers = numbers.substring(0, numbers.length() - 1);
				mTvNumber.setText(numbers);
			}
		}
	};

	private OnClickListener numberClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			playPressKeySound();
			
			if (mTvNumber.getText().toString().length() < 20) {
				mTvNumber.setText(mTvNumber.getText().toString() + view.getTag());
			}
		}
	};
	
	private void playPressKeySound() {
		mSoundPool.play(mSoundId, // 声音资源
				0.3f, // 左声道
				0.3f, // 右声道
				1, // 优先级，0最低
				0, // 循环次数，0是不循环，-1是永远循环
				2); // 回放速度，0.5-2.0之间。1为正常速度
	}
	
	private OnClickListener dialClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			playPressKeySound();
			
			String strNumber = mTvNumber.getText().toString().trim();
			if (strNumber.length() > 0) {
				try {
					Long.valueOf(strNumber);
				}
				catch (Exception e) {
					Utils.toast(ActivityDialByNumber.this, "Error Number!");
					log.e(TAG, "Number error, " + e);
					return;
				}
				
				meetingInvite(strNumber.length() >=11 ? NumberType.Phone : NumberType.Robot, 
						strNumber, 
						strNumber);
			}
		}
	};

	public void meetingInvite(final String numberType, final String callNumber, final String callName ) {
		log.d(TAG, "meetingInvite(), callNumber: " + callNumber);
		
		if (RobotApplication.getInstance().checkEnvError() != null) {
			return;
		}
		
		if (callNumber.equals(YYDSDKHelper.getInstance().getLoginUser().getCallNumber())) {
			Utils.toast(this, this.getResources().getString(R.string.cannot_call_self));
			return;
		}
		
		final long number = Long.valueOf(callNumber);
		
		// 显示邀请对话框
		InviteDialog dialog = new InviteDialog(this, callName);
		dialog.setListener(new InviteDialog.Listener() {
			@Override
			public void onOpen() {
				log.d(TAG, "onOpen()");
				// 发送邀请, 使用rid_GUID作为channelId
				String channelId = AgoraSDKHelper.getInstance().getChannelId();
				if (channelId == null) {
					channelId = YYDSDKHelper.getInstance().getLoginUser().getCallNumber() + "_" + Utils.getGUID();
					AgoraSDKHelper.getInstance().setChannelId(channelId);
				}
				AgoraSDKHelper.getInstance().setCallNumber(callNumber);
				YYDLogicServer.getInstance().AVMInvite(numberType, number, channelId, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "AVMInvite success");
					}
					public void onFailed(int error) {
						log.d(TAG, "AVMInvite failed, error: " + error);
					}
				});
			}
			@Override
			public void onCancel() {
				log.d(TAG, "onCancel()");
				// 取消邀请
				YYDLogicServer.getInstance().AVMInviteCancel(numberType, number, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "AVMInviteCancel success");
					}
					public void onFailed(int error) {
						log.d(TAG, "AVMInviteCancel failed, error: " + error);
					}
				});
			}
			@Override
			public void onTimeout() {
				log.d(TAG, "Invite onTimeout()");
			}
		});
		dialog.setTimeout(ActivityDialByContact.INVITE_TIMEOUT);
		dialog.show();
	}
	
	/**
	 * 初始化本地广播接收
	 */
	private void registerLocalBroadcastReceiver() {
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mLocalBroadcastReceiver, 
				new IntentFilter(ActivityMeeting.LOCALBROADCAST_EXIT_MEETING));
	}
	
	private void unRegisterLocalBroadcastReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
	}
	
	private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			log.d(TAG, "onReceive(), recv action: " + action);
			
			if (ActivityMeeting.LOCALBROADCAST_EXIT_MEETING.equals(action)) {
				if (intent.getBooleanExtra(ActivityMeeting.HAVE_NEW_HISTORY, false)) {
					runOnUiThread(new Runnable() {
						public void run() {
							initHistoryList();
						}
					});
				}
			}
			
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterLocalBroadcastReceiver();
	}
}
