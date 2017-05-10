/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.utils;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity基类
 *
 */
public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ActivityCollector.push(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.pop(this);
	}

}
