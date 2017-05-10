/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.WebRtcSDKHelper;
import com.yongyida.robot.video.comm.BaseActivity;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.ErrorCode;
import com.yongyida.robot.video.sdk.NumberType;
import com.yongyida.robot.video.sdk.YYDVideoServer;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * 添加好友
 */
public class ActivityAddFriend extends BaseActivity implements OnClickListener {
	public static final String TAG = ActivityAddFriend.class.getSimpleName();

	private RadioButton mRbFriendTypeRobot;
	private RadioButton mRbFriendTypePhone;
	private EditText mEdtFriendNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);

		mRbFriendTypeRobot = (RadioButton) findViewById(R.id.rb_friend_type_robot);
		mRbFriendTypePhone = (RadioButton) findViewById(R.id.rb_friend_type_phone);
		mEdtFriendNumber = (EditText) findViewById(R.id.edt_friend_number);

		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_ok:
			findViewById(R.id.btn_ok).setEnabled(false);
			addNewFriend();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		default:
			break;
		}
	}

	private void addNewFriend() {
		log.d(TAG, "addNewFriend()");

		String numberType = NumberType.Robot;
		if (mRbFriendTypeRobot.isSelected()) {
			numberType = NumberType.Robot;
		}
		else if (mRbFriendTypePhone.isSelected()) {
			numberType = NumberType.Phone;
		}

		String number = mEdtFriendNumber.getText().toString().trim();
		if (!YYDVideoServer.getInstance().existFriend(numberType, number)) {
			log.d(TAG, "Not exist frined, numberType: " + numberType + ", number: " + number);
			YYDVideoServer.getInstance().addFriend(numberType, Long.valueOf(number), new CmdCallBacker() {
				public void onSuccess(Object arg) {
					log.d(TAG, "addFriend success");
					WebRtcSDKHelper.getInstance().setHaveNewFriend(true);
					Utils.toast(ActivityAddFriend.this, getResources().getString(R.string.add_friend_success));
					finish();
				}

				public void onFailed(int error) {
					log.d(TAG, "addFriend failed, error: " + error);
					String msg = getResources().getString(R.string.add_friend_failed);
					if (error == ErrorCode.NOTEXEC) {
						msg = getResources().getString(R.string.command_notexec);
					}
					else if (error == ErrorCode.TIMEOUT) {
						msg = getResources().getString(R.string.command_timeout);
					}
					Utils.toast(ActivityAddFriend.this, msg);
					finish();
				}
			});
		}
		else {
			log.d(TAG, "Exist friend, numberType: " + numberType + ", number: " + number);
			Utils.toast(ActivityAddFriend.this, getResources().getString(R.string.friend_exists));
		}
	}
}
