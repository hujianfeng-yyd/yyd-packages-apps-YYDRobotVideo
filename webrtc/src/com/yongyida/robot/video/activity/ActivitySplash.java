/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.activity;

import android.content.Intent;
import android.os.Bundle;

import com.yongyida.robot.video.comm.BaseActivity;
import com.yongyida.robot.video.comm.log;

public class ActivitySplash extends BaseActivity {
	private static final String TAG = "ActivitySplash";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, ActivityDialByContact.class));
		finish();
	}
}
