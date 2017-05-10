/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

import android.os.Bundle;

import com.yongyida.robot.video.utils.BaseActivity;
import com.yongyida.robot.video.utils.log;

public class ActivitySplash extends BaseActivity {
	private static final String TAG = ActivitySplash.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		finish();
	}
}
