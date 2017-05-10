/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.widget;

import android.view.SurfaceView;

/**
 * 切换视图
 * 
 */
public interface ISwitchView {

	public ViewStyle getViewStyle();

	public void setViewStyle(ViewStyle viewStyle);

	public String getTitle();

	public void setTitle(String title);

	public void setBorderVisible(boolean visible);

	public void setTitleVisible(boolean visible);

	public SurfaceView getSurfaceView();
}
