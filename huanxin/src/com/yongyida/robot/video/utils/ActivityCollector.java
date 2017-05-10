/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * Activity收集
 *
 */
public class ActivityCollector {
	private static List<Activity> sActivities = new ArrayList<Activity>();

	/**
	 * 加入Activity
	 * @param activity
	 * @return void
	 *
	 */
	public static void push(Activity activity) {
		sActivities.add(activity);
	}

	/**
	 * 移出Activity
	 * @param activity
	 * @return void
	 *
	 */
	public static void pop(Activity activity) {
		sActivities.remove(activity);
	}

	/**
	 * 销毁所有Activity
	 * @param none
	 * @return void
	 *
	 */
	public static void finishAll() {
		for (Activity activity : sActivities) {
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}
	}
}
