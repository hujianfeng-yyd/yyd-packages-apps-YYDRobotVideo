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
import com.yongyida.robot.video.comm.BaseActivity;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.ErrorCode;
import com.yongyida.robot.video.sdk.YYDVideoServer;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 修改昵称
 */
public class ActivityModifyFriend extends BaseActivity implements OnClickListener {
	public static final String TAG = ActivityModifyFriend.class.getSimpleName();

	private String mNumberType;
	private String mNumber;

	private TextView mTvNumberType;
	private TextView mTvNumber;
	private EditText mEdtFriendAlias;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_friend);

		mNumberType = getIntent().getStringExtra("type");
		mNumber = getIntent().getStringExtra("number");
		log.d(TAG, "onCreate(), type:%s, number:%s", mNumberType, mNumber);

		mTvNumberType = (TextView) findViewById(R.id.tv_friend_type);
		mTvNumberType.setText(mNumberType);
		mTvNumber = (TextView) findViewById(R.id.tv_friend_number);
		mTvNumber.setText(mNumber);
		mEdtFriendAlias = (EditText) findViewById(R.id.edt_friend_alias);
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_ok:
			findViewById(R.id.btn_ok).setEnabled(false);
			modifyFriendAlias();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		default:
			break;
		}
	}

	private void modifyFriendAlias() {
		log.d(TAG, "modifyFriendAlias()");

		String alias = mEdtFriendAlias.getText().toString().trim();
		YYDVideoServer.getInstance().modifyFriend(mNumberType, Long.valueOf(mNumber), alias, new CmdCallBacker() {
			public void onSuccess(Object arg) {
				log.d(TAG, "modifyFriend success");

				Utils.toast(ActivityModifyFriend.this, getResources().getString(R.string.modify_friend_success));
				finish();
			}

			public void onFailed(int error) {
				log.d(TAG, "modifyFriend failed, error: " + error);

				String msg = getResources().getString(R.string.modify_friend_failed);
				if (error == ErrorCode.NOTEXEC) {
					msg = getResources().getString(R.string.command_notexec);
				}
				else if (error == ErrorCode.TIMEOUT) {
					msg = getResources().getString(R.string.command_timeout);
				}
				Utils.toast(ActivityModifyFriend.this, msg);
				finish();
			}
		});
	}
}
