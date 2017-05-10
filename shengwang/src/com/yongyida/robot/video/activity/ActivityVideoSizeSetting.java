/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.Config;
import com.yongyida.robot.video.R;
import com.yongyida.robot.video.comm.log;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * 视频大小设置
 */
@SuppressWarnings("deprecation")
public class ActivityVideoSizeSetting extends Activity implements OnClickListener {
	public static final String TAG = "ActivityVideoSizeSetting";
	
	private Spinner mSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videosize_setting);
		
		mSpinner = (Spinner) findViewById(R.id.spinner1);
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		
		Config.getInstance().init(this);
		loadVideoSize(Config.getInstance().getVideoSizeWidth(), Config.getInstance().getVideoSizeHeight());
	}
	
	private void loadVideoSize(int width, int height) {
		log.d(TAG, "loadVideoSize(), width: " + width + ", height: " + height);
		
		Camera camera = Camera.open(0);
		Camera.Parameters parameters = camera.getParameters();
		camera.release();
		List<Camera.Size> supportPreviewSizes = parameters.getSupportedPreviewSizes();
		
		List<String> lstSize = new ArrayList<String>();
		for (int i = 0; i < supportPreviewSizes.size(); ++i) {
			Camera.Size s = supportPreviewSizes.get(i);
			lstSize.add(s.width + "x" + s.height);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lstSize);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
		
		for (int i = 0; i < supportPreviewSizes.size(); ++i) {
			Camera.Size s = supportPreviewSizes.get(i);
			if (s.width == width && s.height == height) {
				mSpinner.setSelection(i);
				break;
			}
		}
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_ok:
			saveSetting();
			finish();
			break;
		case R.id.btn_cancel:
			finish();
			break;
		default:
			break;
		}
	}
	
	private void saveSetting() {
		String strSize = mSpinner.getSelectedItem().toString();
		String[] sizes = strSize.split("x");
		if (sizes.length == 2) {
			int w = Integer.valueOf(sizes[0]);
			int h = Integer.valueOf(sizes[1]);
			log.d(TAG, "set video width: " + w + ", height: " + h);
			Config.getInstance().setVideoSizeWidth(w);
			Config.getInstance().setVideoSizeHeight(h);
		}
	}
}
