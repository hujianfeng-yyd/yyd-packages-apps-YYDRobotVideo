/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-20
 * 
 */
package com.yongyida.robot.video;

import android.content.Context;

import com.yongyida.robot.video.comm.NetType;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;

/**
 * 网络
 *
 */
public class NetWork {
	private static final String TAG = NetWork.class.getSimpleName();
	private static NetWork sInstance;
	
	private Context mContext;
	private NetType mNetType;
	
	private NetWork() {
	}
	
	public static synchronized NetWork getInstance() {
		if (sInstance == null) {
			sInstance = new NetWork();
		}
		return sInstance;
	}
	
	public void init(Context ctx) {
		log.d(TAG, "init()");
		
		mContext = ctx;
		mNetType = Utils.getNetWorkType(mContext);
		
		log.d(TAG, "NetType: " + mNetType);
	}
	
	public NetType getNetType() {
		return mNetType;
	}
	
	public NetType getCurrentNetWork() {
		mNetType = Utils.getNetWorkType(mContext);
		return mNetType;
	}
}
