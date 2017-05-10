package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.agora.AgoraSDKHelper;
import com.yongyida.robot.video.bean.PayResultBean;
import com.yongyida.robot.video.comm.BaseActivity;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.LoginResponse;
import com.yongyida.robot.video.command.Response;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.ErrorCode;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.LoginStatus;
import com.yongyida.robot.video.sdk.PhoneUser;
import com.yongyida.robot.video.sdk.RobotUser;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.util.HttpUtilEn;
import com.yongyida.robot.video.util.JsonParser;
import com.yongyida.robot.video.widget.MyHorizontalScrollView;

import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityDialByContact extends BaseActivity implements OnClickListener {
	public static final String TAG = "ActivityDialByContact";
	public static final int SETTING_CLICK_TIMES = 5;
	public static final int INVITE_TIMEOUT = 30000;

	private MyHorizontalScrollView mFriendView;
	private List<User> mFriendList;
	private TextView mTvId;
	private TextView mTvName;
	private long prevClickTime = 0;
	private int clickCount = 0;
	private GridView friend_gridview;
	private String time = "0";
	private String residual_time;
	private boolean isneedPayFuction = false;
	private int selectPosition = -1;
	private Handler mhandler = new Handler(){

		public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case 0:
				Toast.makeText(ActivityDialByContact.this, getString(R.string.query_time_error), Toast.LENGTH_SHORT).show();
				break;
			case 1:
				 String residualtime=(String) msg.obj;
				 if(residualtime==null||residualtime.equals("")){
					 residualtime = "0";
				 }
				residual_time_tv.setText(residualtime+getString(R.string.pay_minute));
				if(residualtime!=null&&!residualtime.equals("")){
					int time = Integer.parseInt(residual_time);
					if (time < 10) {
						showTipDialog(time);
					}
				}
				
				break;
			default:
				break;
			}
    	}


    };
	private TextView residual_time_tv;
	private User mCurrentUser;
	private FriendGridViewAdapter friendGridViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		isneedPayFuction = getDeviceRechargeFun();
		if(isneedPayFuction){
			setContentView(R.layout.activity_dial_contacts_second);
			findViewById(R.id.recharge_bt).setOnClickListener(this);
			residual_time_tv = (TextView)findViewById(R.id.residual_time_tv);
		}else{
			setContentView(R.layout.activity_dial_contacts);
		}
         
		YYDSDKHelper.getInstance().registerEventListener(mEventListener);
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvId = (TextView) findViewById(R.id.tv_id);
		mTvId.setOnClickListener(this);
		findViewById(R.id.btn_dial).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);

		if (YYDLogicServer.getInstance().getLoginStatus() == LoginStatus.LoginSuccess) {
			fillFriendList();
		}
		
		registerLocalBroadcastReceiver();
		showEnvError();
	}
    @Override
    protected void onResume() {
    	if(isneedPayFuction){
    		queryTime();    		
    	}
    	super.onResume();
    }
	private void showEnvError() {
		String strError = RobotApplication.getInstance().checkEnvError();
		if (strError != null) {
			mTvName.setText(strError);
		}
	}

	private void fillFriendList() {
		log.d(TAG, "fillFriendList()");

		// 显示用户ID
		mTvId.setText(this.getResources().getString(R.string.myid) + ": "
				+ YYDSDKHelper.getInstance().getLoginUser().getId());

		if (mFriendList != null && !mFriendList.isEmpty()) {
			mFriendList.clear();
		}
		mFriendList = YYDLogicServer.getInstance().getFriendList();
		if(isneedPayFuction){
			friend_gridview = (GridView)findViewById(R.id.friend_gridview);
			friendGridViewAdapter = new  FriendGridViewAdapter(this, mFriendList);
			friend_gridview.setAdapter(friendGridViewAdapter);
			friend_gridview.setOnItemLongClickListener(mPayOnItemLongClickListener);
			friend_gridview.setOnItemClickListener(mPayOnItemClickListener);		
		}else{
		mFriendView = (MyHorizontalScrollView) findViewById(R.id.friend_scrollview);
		mFriendView.clear();
		mFriendView.setAdapter(new FriendAdapter(this, mFriendList));
		mFriendView.setOnItemClickListener(mOnItemClickListener);
		mFriendView.setOnItemLongClickListener(mOnItemLongClickListener);

		}
	}

	OnItemClickListener mPayOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//mCurrentUser = mFriendList.get(position);
			//showMenu(view);
			if(isneedPayFuction){
				friendGridViewAdapter.setSelectItem(position);
				friendGridViewAdapter.notifyDataSetChanged();
			}
			if (view != null && position >= 0) {
				mCurrentUser = mFriendList.get(position);
				if (mCurrentUser instanceof RobotUser) {
					mTvId.setText(Long.toString(mCurrentUser.getId()));
					mTvName.setText(!TextUtils.isEmpty(mCurrentUser.getUserName()) ? mCurrentUser.getUserName()
							: getString(R.string.default_name));
				}
				else if (mCurrentUser instanceof PhoneUser) {
					PhoneUser pu = (PhoneUser) mCurrentUser;
					mTvId.setText(pu.getPhone());
					if (!pu.getUserName().equals(pu.getPhone())) {
						mTvName.setText(!TextUtils.isEmpty(mCurrentUser.getUserName()) ? mCurrentUser.getUserName()
								: getString(R.string.default_name));
					}
					else {
						mTvName.setText(R.string.phone_user);
					}
				}
			}
			else {
				mTvId.setText("");
				mTvName.setText("");
			}
		}
	};
	
	private MyHorizontalScrollView.OnItemClickListener mOnItemClickListener = new MyHorizontalScrollView.OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			if (view != null && position >= 0) {
				mCurrentUser = mFriendList.get(position);
				if (mCurrentUser instanceof RobotUser) {
					mTvId.setText(Long.toString(mCurrentUser.getId()));
					mTvName.setText(!TextUtils.isEmpty(mCurrentUser.getUserName()) ? mCurrentUser.getUserName()
							: getString(R.string.default_name));
				}
				else if (mCurrentUser instanceof PhoneUser) {
					PhoneUser pu = (PhoneUser) mCurrentUser;
					mTvId.setText(pu.getPhone());
					if (!pu.getUserName().equals(pu.getPhone())) {
						mTvName.setText(!TextUtils.isEmpty(mCurrentUser.getUserName()) ? mCurrentUser.getUserName()
								: getString(R.string.default_name));
					}
					else {
						mTvName.setText(R.string.phone_user);
					}
				}
			}
			else {
				mTvId.setText("");
				mTvName.setText("");
			}
		}
	};
	
	
		OnItemLongClickListener mPayOnItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			if(isneedPayFuction){
				friendGridViewAdapter.setSelectItem(position);
				friendGridViewAdapter.notifyDataSetChanged();
			}
			if (view != null && position >= 0) {
				User user = mFriendList.get(position);
				if (user != null) {
					meetingInvite(user);
				}
			}
			return false;
		}
	};
	
	
	private Thread queryTimeThread;

	private void queryTime() {
		if(queryTimeThread == null){
			queryTimeThread = new Thread(queryTimeRunnable);			
			queryTimeThread.start();
		}
	}

	Runnable queryTimeRunnable = new Runnable() {
		
		@Override
		public void run() {
			HttpUtilEn.SetUrlType(1);
			String result = HttpUtilEn.submitPostData(null, null, null, null, YYDSDKHelper.getInstance().getLoginUser().getId()+"", 1);
			PayResultBean timeBean = JsonParser.parserQueryJson(result);
			if(timeBean.getRet()==-1){
	            mhandler.sendEmptyMessage(0);
	            queryTimeThread = null;
				return;
			}
			residual_time = timeBean.getRemain_time();

			Message msg = new Message();
			msg.what = 1;
			msg.obj = residual_time;
			mhandler.sendMessage(msg);
			queryTimeThread = null;
		}
	};

	private MyHorizontalScrollView.OnItemClickListener mOnItemLongClickListener = new MyHorizontalScrollView.OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			if (view != null && position >= 0) {
				User user = mFriendList.get(position);
				if (user != null) {
					meetingInvite(user);
				}
			}
		}
	};

	public EventListener mEventListener = new EventListener() {
		public void onEvent(Event event, final Object data) {
			log.d(TAG, "onEvent(), envet: " + event);
			switch (event) {
			case LoginResponse:
				LoginResponse resp = (LoginResponse) data;
				if (resp.getRet() == Response.RET_OK) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							fillFriendList();
						}
					});
				}
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_dial:
			openDialPlate();
			break;
		case R.id.btn_back:
			finish();
			break;
		case R.id.tv_id:
			showMenu();
			break;
        case R.id.recharge_bt:
			Intent intent = new Intent(ActivityDialByContact.this,PayMainActivity.class);
			intent.putExtra("rid", YYDSDKHelper.getInstance().getLoginUser().getId()+"");
			intent.putExtra("time", residual_time);
			startActivityForResult(intent, 55);
			break;
		case R.id.tip_dialog_confirm:
			dismissDialog();
			Intent i = new Intent(ActivityDialByContact.this,PayMainActivity.class);
			i.putExtra("rid", YYDSDKHelper.getInstance().getLoginUser().getId()+"");
			i.putExtra("time", residual_time);
			startActivityForResult(i, 55);
			break;
		case R.id.tip_dialog_cancel:
			dismissDialog();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 30){
			queryTime();
		}
	}
	/**
	 * 点击5次打开设置对话框
	 * 
	 * @param
	 * @return
	 */
	@SuppressWarnings("unused")
	private void idClick() {
		long currClickTime = System.currentTimeMillis();
		if (prevClickTime == 0 || currClickTime - prevClickTime < 2000) {
			clickCount++;
			prevClickTime = currClickTime;

			if (clickCount == SETTING_CLICK_TIMES) {
				startActivity(new Intent(this, ActivityVideoSizeSetting.class));
				prevClickTime = 0;
				clickCount = 0;
			}
		}
		else {
			prevClickTime = currClickTime;
			clickCount = 0;
		}
	}

	/**
	 * 弹出菜单
	 * 
	 * @param
	 * @return
	 */
	private void showMenu() {
		log.d(TAG, "showMenu()");

		PopupMenu popup = new PopupMenu(this, mTvId);
		popup.getMenuInflater().inflate(R.menu.friend_menu, popup.getMenu());
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.add_friend:
					add_friend();
					break;
				case R.id.remove_friend:
					remove_friend();
					break;
				case R.id.edit_friend_nickname:
					edit_friend_alias();
					break;
				default:
					break;
				}
				return true;
			}
		});
		popup.show();
	}

	private void add_friend() {
		log.d(TAG, "add_friend()");
		startActivity(new Intent(this, ActivityAddFriend.class));
	}

	private void remove_friend() {
		log.d(TAG, "remove_friend()");
		
		if (mCurrentUser == null) {
			Utils.toast(this, getResources().getString(R.string.not_selected_user));
			return;
		}
		
		String numberType = mCurrentUser.getNumberType();
		String number = mCurrentUser.getCallNumber();
		if (YYDLogicServer.getInstance().existFriend(numberType, number)) {
			YYDLogicServer.getInstance().removeFriend(numberType, Long.valueOf(number), new CmdCallBacker() {
				public void onSuccess(Object arg) {
					log.d(TAG, "removeFriend success");
					Utils.toast(ActivityDialByContact.this, getResources().getString(R.string.remove_friend_success));
					if(selectPosition>=0&&isneedPayFuction){
						mFriendList.remove(selectPosition);
						friendGridViewAdapter.notifyDataSetChanged();
					}
				}
				
				public void onFailed(int error) {
					log.d(TAG, "removeFriend failed, error: " + error);
					String msg = getResources().getString(R.string.remove_friend_failed);
					if (error == ErrorCode.NOTEXEC) {
						msg = getResources().getString(R.string.command_notexec);
					}
					else if (error == ErrorCode.TIMEOUT) {
						msg = getResources().getString(R.string.command_timeout);
					}
					Utils.toast(ActivityDialByContact.this, msg);
				}
			});
		}
		else {
			log.d(TAG, "Not exist frined, numberType: " + numberType + ", number: " + number);
			Utils.toast(ActivityDialByContact.this, getResources().getString(R.string.friend_exists));
		}
	}

	private void edit_friend_alias() {
		log.d(TAG, "edit_friend_alias()");
		
		if (mCurrentUser == null) {
			Utils.toast(this, getResources().getString(R.string.not_selected_user));
			return;
		}
		log.d(TAG, "edit_friend_alias(), type:%s, number:%s", mCurrentUser.getNumberType(), mCurrentUser.getCallNumber());
		
		Intent intent = new Intent(this, ActivityModifyFriend.class);
		intent.putExtra("type", mCurrentUser.getNumberType());
		intent.putExtra("number", mCurrentUser.getCallNumber());
		startActivity(intent);
	}

	private void openDialPlate() {
		startActivity(new Intent(this, ActivityDialByNumber.class));
	}

	public void meetingInvite(User user) {
		log.d(TAG, "meetingInvite(), " + user);

		if (RobotApplication.getInstance().checkEnvError() != null) {
			return;
		}

		if (user.equals(YYDSDKHelper.getInstance().getLoginUser())) {
			Utils.toast(this, this.getResources().getString(R.string.cannot_call_self));
			return;
		}

		final String numberType = user.getNumberType();
		final String strCallNumber = user.getCallNumber();
		final long callNumber = Long.valueOf(strCallNumber);
		final String callName = !TextUtils.isEmpty(user.getUserName()) ? user.getUserName()
				: getString(R.string.default_name);
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
				AgoraSDKHelper.getInstance().setCallNumber(strCallNumber);
				YYDLogicServer.getInstance().AVMInvite(numberType, callNumber, channelId, new CmdCallBacker() {
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
				YYDLogicServer.getInstance().AVMInviteCancel(numberType, callNumber, new CmdCallBacker() {
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
		dialog.setTimeout(INVITE_TIMEOUT);
		dialog.show();
	}

	/**
	 * 注册本地广播接收
	 */
	private void registerLocalBroadcastReceiver() {
		LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
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
				if (intent.getBooleanExtra(ActivityMeeting.HAVE_NEW_FRIEND, false)) {
					runOnUiThread(new Runnable() {
						public void run() {
							fillFriendList();
						}
					});
				}
			}

		}
	};
	private AlertDialog dialog;

	@Override
	protected void onDestroy() {
		YYDSDKHelper.getInstance().unRegisterEventListener(mEventListener);
		unRegisterLocalBroadcastReceiver();
		super.onDestroy();
		log.d(TAG, "onDestroy()");
	}
	private void showTipDialog(int time) {
		if(dialog == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDialByContact.this);
			View view = LayoutInflater.from(this).inflate(R.layout.query_dialog_layout, null);
			builder.setView(view);
			TextView text =(TextView) view.findViewById(R.id.tip_tv);	
			if(time<=0){
				text.setText(R.string.query_time_zero_tip);
			}else{
				text.setText(R.string.query_time_tip);
			}
			view.findViewById(R.id.tip_dialog_confirm).setOnClickListener(this);
			view.findViewById(R.id.tip_dialog_cancel).setOnClickListener(this);
			dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();			
		}
	};
	private void dismissDialog(){
		if(dialog!=null){
			dialog.dismiss();
			dialog = null;
		}
	}
	
	private boolean getDeviceRechargeFun() {
		String result = SystemProperties.get("ro.product.payvoice", "yes");
		return result.equals("yes")?true:false;
	}
}
