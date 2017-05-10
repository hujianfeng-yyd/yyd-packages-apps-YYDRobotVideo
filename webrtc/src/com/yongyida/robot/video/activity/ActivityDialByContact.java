package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.RobotApplication;
import com.yongyida.robot.video.WebRtcSDKHelper;
import com.yongyida.robot.video.apprtc.SettingsActivity;
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
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.video.widget.CarrouselLayout;
import com.yongyida.robot.video.widget.CarrouselLayout.OnCarrouselItemClickListener;
import com.yongyida.robot.video.widget.CarrouselLayout.OnCarrouselItemLongClickListener;
import com.yongyida.robot.video.widget.CarrouselLayout.OnCarrouselItemSelectedListener;
import com.yongyida.robot.video.widget.CircleImageView;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.TextView;

public class ActivityDialByContact extends BaseActivity implements OnClickListener {
	public static final String TAG = ActivityDialByContact.class.getSimpleName();
	public static final int SETTING_CLICK_TIMES = 5;
	public static final int INVITE_TIMEOUT = 30000;

	private CarrouselLayout mCarrouselLayout;
	private List<User> mFriendList;
	private TextView mTvId;
	private TextView mTvName;
	private User mCurrentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dial_contacts);

		YYDVideoServer.getInstance().registerEventListener(mEventListener);
		
		mCarrouselLayout = (CarrouselLayout)findViewById(R.id.carrouselLayout1);
		mCarrouselLayout.setR((int) (Utils.getScreenWidth(this) / 3));
		mCarrouselLayout.setOnCarrouselItemClickListener(mOnItemClickListener);
		mCarrouselLayout.setOnCarrouselItemSelectedListener(mOnItemSelectedListener);
		mCarrouselLayout.setOnCarrouselItemLongClickListener(mOnItemLongClickListener);
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvId = (TextView) findViewById(R.id.tv_id);
		mTvId.setOnClickListener(this);
		findViewById(R.id.btn_dial).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		
		if (YYDVideoServer.getInstance().getLoginStatus() == LoginStatus.LoginSuccess) {
			fillFriendList();
		}
		
		chkAndShowEnvError();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (YYDVideoServer.getInstance().getLoginStatus() == LoginStatus.LoginSuccess
				&& WebRtcSDKHelper.getInstance().haveNewFriend()) {
			fillFriendList();
		}
	}
	
	private void chkAndShowEnvError() {
		String strError = RobotApplication.getInstance().checkEnvError();
		if (strError != null) {
			mTvName.setText(strError);
		}
	}

	private void fillFriendList() {
		log.d(TAG, "fillFriendList()");

		// 显示用户ID
		mTvId.setText(this.getResources().getString(R.string.myid) + ": "
				+ YYDVideoServer.getInstance().getLoginUser().getId());
		
		if (mFriendList != null)
			mFriendList.clear();
		// 读取用户好友列表
		mFriendList = YYDVideoServer.getInstance().getFriendList();
		
		// 加载用户好友列表
		mCarrouselLayout.removeAllViews();
		for (int i = 0; i < mFriendList.size(); ++i) {
			CircleImageView imageView = new CircleImageView(this);
			imageView.setImageResource(R.drawable.ic_photo_default1);
			mCarrouselLayout.addView(imageView);
		}
	}
	
	/**
	 * 用户单击
	 */
	private OnCarrouselItemClickListener mOnItemClickListener = new OnCarrouselItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			if (view != null && position >= 0 && position < mFriendList.size()) {
				selectedUser(position);
			}
			else {
				mTvId.setText("");
				mTvName.setText("");
			}
		}
	};
	
	/**
	 * 用户被选中
	 */
	private OnCarrouselItemSelectedListener mOnItemSelectedListener = new OnCarrouselItemSelectedListener() {
		@Override
		public void selected(View view, int position) {
			if (view != null && position >= 0 && position < mFriendList.size()) {
				selectedUser(position);
			}
			else {
				mTvId.setText("");
				mTvName.setText("");
			}
		}
	};
	
	/**
	 * 选中用户
	 */
	private void selectedUser(int position) {
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
		else {
			log.e(TAG, "user type error");
		}
	}
	
	/**
	 * 用户长点击
	 */
	private OnCarrouselItemLongClickListener mOnItemLongClickListener = new OnCarrouselItemLongClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			if (view != null && position >= 0 && position < mFriendList.size()) {
				User user = mFriendList.get(position);
				if (user != null) {
					meetingInvite(user);
				}
			}
		}
	};

	/**
	 * 登录事件监听
	 */
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
		default:
			break;
		}
	}

	/**
	 * 显示菜单
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
				case R.id.setting:
					setting();
					break;
				default:
					break;
				}
				return true;
			}
		});
		popup.show();
	}

	/**
	 * 添加好友
	 */
	private void add_friend() {
		log.d(TAG, "add_friend()");
		startActivity(new Intent(this, ActivityAddFriend.class));
	}

	/**
	 * 移除好友
	 */
	private void remove_friend() {
		log.d(TAG, "remove_friend()");

		if (mCurrentUser == null) {
			Utils.toast(this, getResources().getString(R.string.not_selected_user));
			return;
		}

		String numberType = mCurrentUser.getNumberType();
		String number = mCurrentUser.getCallNumber();
		if (YYDVideoServer.getInstance().existFriend(numberType, number)) {
			YYDVideoServer.getInstance().removeFriend(numberType, Long.valueOf(number), new CmdCallBacker() {
				public void onSuccess(Object arg) {
					log.d(TAG, "removeFriend success");
					Utils.toast(ActivityDialByContact.this, getResources().getString(R.string.remove_friend_success));
					
					fillFriendList();
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

	/**
	 * 修改别名
	 */
	private void edit_friend_alias() {
		log.d(TAG, "edit_friend_alias()");

		if (mCurrentUser == null) {
			Utils.toast(this, getResources().getString(R.string.not_selected_user));
			return;
		}
		log.d(TAG, "edit_friend_alias(), type:%s, number:%s",
				mCurrentUser.getNumberType(),
				mCurrentUser.getCallNumber());
		
		Intent intent = new Intent(this, ActivityModifyFriend.class);
		intent.putExtra("type", mCurrentUser.getNumberType());
		intent.putExtra("number", mCurrentUser.getCallNumber());
		startActivity(intent);
	}
	
	/**
	 * 设置
	 */
	private void setting() {
		log.d(TAG, "setting()");
		startActivity(new Intent(this, SettingsActivity.class));
	}
	
	/**
	 * 打开拨号页面
	 */
	private void openDialPlate() {
		startActivity(new Intent(this, ActivityDialByNumber.class));
	}

	/**
	 * 会议邀请
	 */
	public void meetingInvite(User user) {
		log.d(TAG, "meetingInvite(), " + user);

		if (RobotApplication.getInstance().checkEnvError() != null) {
			return;
		}

		if (user.equals(YYDVideoServer.getInstance().getLoginUser())) {
			Utils.toast(this, this.getResources().getString(R.string.cannot_call_self));
			return;
		}

		final String numberType = user.getNumberType();
		final String callNumber = user.getCallNumber();
		final long number = Long.valueOf(callNumber);
		final String callName = !TextUtils.isEmpty(user.getUserName()) ? user.getUserName()
				: getString(R.string.default_name);
		// 显示邀请对话框
		InviteDialog dialog = new InviteDialog(this, callName);
		dialog.setListener(new InviteDialog.Listener() {
			@Override
			public void onOpen() {
				log.d(TAG, "onOpen()");
				
				// 发送邀请, 使用生成的ROOMID
				String roomId = WebRtcSDKHelper.getInstance().generateRoomId();
				String clientId = null;
				YYDVideoServer.getInstance().WVMInvite(numberType, number, roomId, clientId, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "Invite success");
					}

					public void onFailed(int error) {
						log.d(TAG, "Invite failed, error: " + error);
					}
				});
				
				// 保存拨号号码
				WebRtcSDKHelper.getInstance().setCallNumber(callNumber);
			}

			@Override
			public void onCancel() {
				log.d(TAG, "onCancel()");

				// 取消邀请
				YYDVideoServer.getInstance().WVMInviteCancel(numberType, number, new CmdCallBacker() {
					public void onSuccess(Object arg) {
						log.d(TAG, "InviteCancel success");
					}

					public void onFailed(int error) {
						log.d(TAG, "InviteCancel failed, error: " + error);
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

	@Override
	protected void onDestroy() {
		YYDVideoServer.getInstance().unRegisterEventListener(mEventListener);
		super.onDestroy();
		log.d(TAG, "onDestroy()");
	}

}
