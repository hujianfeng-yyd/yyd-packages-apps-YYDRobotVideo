/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.widget;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.comm.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 摄像头视图
 * 
 */
public class CameraView extends LinearLayout implements ISwitchView {
	private Context mContext;
	private LinearLayout mLinearLayout;
	private FrameLayout mSurfaceContainer;
	private TextView mTvTitle;
	private ViewStyle mViewStyle = ViewStyle.ThumbView;

	public CameraView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public SurfaceView getSurfaceView() {
		if (mSurfaceContainer != null && mSurfaceContainer.getChildCount() > 0) {
			return (SurfaceView) mSurfaceContainer.getChildAt(0);
		}
		return null;
	}

	public void setSurfaceView(SurfaceView view) {
		mSurfaceContainer.addView(view,
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
	}

	public TextView getTitleView() {
		return mTvTitle;
	}

	public ViewStyle getViewStyle() {
		return mViewStyle;
	}

	public void setViewStyle(ViewStyle viewStyle) {
		mViewStyle = viewStyle;
		setBorderVisible(mViewStyle == ViewStyle.ThumbView);
		setTitleVisible(mViewStyle == ViewStyle.ThumbView);
	}

	public String getTitle() {
		return mTvTitle.getText().toString();
	}

	public void setTitle(String title) {
		mTvTitle.setText(title);
	}

	@Override
	public String toString() {
		return "CameraView, left: " + this.getLeft() + ", top: " + this.getTop() + ", width: " + this.getWidth()
				+ ", height: " + this.getHeight() + ", ViewStyle: " + mViewStyle;
	}

	public void setBorderVisible(boolean visible) {
		int padding = visible ? Utils.dp2px(getContext(), 1) : 0;
		mLinearLayout.setPadding(padding, padding, padding, padding);
	}

	public void setTitleVisible(boolean visible) {
		mTvTitle.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void init() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLinearLayout = (LinearLayout) inflater.inflate(R.layout.widget_camera_view, (ViewGroup) getParent());
		mLinearLayout.setLayoutParams(
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
		mSurfaceContainer = (FrameLayout) mLinearLayout.findViewById(R.id.layout_surfaceview_container);
		mTvTitle = (TextView) mLinearLayout.findViewById(R.id.tv_title);
		addView(mLinearLayout);
	}
}
