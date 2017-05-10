/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co.,Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.hxvideo;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;

import com.easemob.chat.EMVideoCallHelper;
import com.easemob.chat.EMVideoCallHelper.EMVideoOrientation;
import com.yongyida.robot.video.utils.log;

@SuppressWarnings("deprecation")
public class CameraHelper implements PreviewCallback {
	private static final String TAG = CameraHelper.class.getSimpleName();

	private int mWidth = 320;
	private int mHeight = 240;

	private EMVideoCallHelper mCallHelper;

	private Camera mCamera = null;
	private int mCameraId = CameraInfo.CAMERA_FACING_BACK;
	private Parameters mParameters;
	private CameraInfo mCameraInfo;
	private SurfaceHolder mLocalSurfaceHolder;
	private boolean mStartFlag;
	private byte[] mYuvFame;
	private byte[] mYuvRotate90;
	
	private int mRotateAngle = 0;

	public CameraHelper(EMVideoCallHelper callHelper, SurfaceHolder localSurfaceHolder) {
		mCallHelper = callHelper;
		mLocalSurfaceHolder = localSurfaceHolder;
	}
	
	public void setVideoSize(int width, int height) {
		mWidth = width;
		mHeight = height;
	}
	
	public void setRotateAngle(int rotateAngle) {
		mRotateAngle = rotateAngle;
		log.d(TAG, "mRotateAngle: " + mRotateAngle);
	}
	
	/**
	 * 开启相机拍摄
	 */
	public void startCapture() {
		try {
			mCameraInfo = new CameraInfo();
			if (mCamera == null) {
				int count = Camera.getNumberOfCameras();
				log.d(TAG, "Camera count " + count);
				mCamera = Camera.open(mCameraId);
				if (mCamera == null) {
					for (int i = 0; i < count - 1; i++) {
						mCamera = Camera.open(i);
						if (mCamera != null) {
							mCameraId = i;
							break;
						}
					}
				}
				
				if (mCamera != null) {
					Camera.getCameraInfo(mCameraId, mCameraInfo);
					log.d(TAG, "Open camera " + mCameraId + ", Use " + ((mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) ? "CAMERA_FACING_FRONT":"CAMERA_FACING_BACK"));
				}
				else {
					log.e(TAG, "Open camera failed, CameraId " + mCameraId);
					return;
				}
			}
			
			mCamera.stopPreview();
			mParameters = mCamera.getParameters();
			mCamera.setDisplayOrientation(0);
			if (isScreenOriatationPortrait()) {
				// 设置视频方向为竖屏
				mCallHelper.setVideoOrientation(EMVideoOrientation.EMPortrait);
				log.d(TAG, "设置视频方向为竖屏");
			}
			else {
				// 设置视频方向为横屏
				if (mRotateAngle == 90) {
					log.d(TAG, "设置视频方向为竖屏");
					mCallHelper.setVideoOrientation(EMVideoOrientation.EMPortrait);
				}
				else {
					mCallHelper.setVideoOrientation(EMVideoOrientation.EMLandscape);
					log.d(TAG, "设置视频方向为横屏");
				}
			}
			
			mParameters.setPreviewSize(mWidth, mHeight);
			mParameters.setPreviewFrameRate(15);
			mParameters.setZoom(0);
			mCamera.setParameters(mParameters);
			int mformat = mParameters.getPreviewFormat();
			int bitsperpixel = ImageFormat.getBitsPerPixel(mformat);
			mYuvFame = new byte[mWidth * mHeight * bitsperpixel / 8];
			mYuvRotate90 = new byte[mWidth * mHeight * bitsperpixel / 8];
			mCamera.addCallbackBuffer(mYuvFame);
			mCamera.setPreviewDisplay(mLocalSurfaceHolder);
			mCamera.setPreviewCallbackWithBuffer(this);
			
			//设置要传给对方的图像分辨率
			EMVideoCallHelper.getInstance().setResolution(mWidth, mHeight);
			log.d(TAG, "setResolution: " + mWidth + "x" + mHeight);
			
			mCamera.startPreview();
			log.d(TAG, "camera start preview");
		}
		catch (Exception e) {
			e.printStackTrace();
			if (mCamera != null) {
				mCamera.release();
			    mCamera = null;
			}
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mStartFlag == true) {
			// 根据屏幕方向写入及传输数据
			if (isScreenOriatationPortrait()) {
				if (mCameraInfo.orientation == 90 || mCameraInfo.orientation == 0) {
					YUV420spRotate90(mYuvRotate90, mYuvFame, mWidth, mHeight);
				}
				else if (mCameraInfo.orientation == 270) {
					YUV420spRotate270(mYuvRotate90, mYuvFame, mWidth, mHeight);
				}
				mCallHelper.processPreviewData(mHeight, mWidth, mYuvRotate90);
			}
			else {
				if (mCameraInfo.orientation == 90 || mCameraInfo.orientation == 0) {
					if (mRotateAngle == 90) {
						YUV420spRotate90(mYuvRotate90, mYuvFame, mWidth, mHeight);
						mCallHelper.processPreviewData(mWidth, mHeight, mYuvRotate90);  //改变宽高参数
					}
					else {
						YUV42left2right(mYuvRotate90, mYuvFame, mWidth, mHeight);
						mCallHelper.processPreviewData(mHeight, mWidth, mYuvRotate90);
					}
				}
				else {
					YUV42left2right(mYuvRotate90, mYuvFame, mWidth, mHeight);
					mCallHelper.processPreviewData(mHeight, mWidth, mYuvRotate90);
				}
			}
		}
		camera.addCallbackBuffer(mYuvFame);
	}
	
	/**
	 * 停止拍摄
	 */
	public void stopCapture() {
		mStartFlag = false;
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * 获取是否已开启视频数据传输
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return mStartFlag;
	}

	/**
	 * 设置是否传输视频数据
	 * 
	 * @param start
	 */
	public void setStartFlag(boolean start) {
		this.mStartFlag = start;
	}

	void YUV420spRotate90(byte[] dst, byte[] src, int srcWidth, int srcHeight) {
		int nWidth = 0, nHeight = 0;
		int wh = 0;
		int uvHeight = 0;
		if (srcWidth != nWidth || srcHeight != nHeight) {
			nWidth = srcWidth;
			nHeight = srcHeight;
			wh = srcWidth * srcHeight;
			uvHeight = srcHeight >> 1;//uvHeight = height / 2  
		}
		//旋转Y  
		int k = 0;
		for (int i = 0; i < srcWidth; i++) {
			int nPos = 0;
			for (int j = 0; j < srcHeight; j++) {
				dst[k] = src[nPos + i];
				k++;
				nPos += srcWidth;
			}
		}

		for (int i = 0; i < srcWidth; i += 2) {
			int nPos = wh;
			for (int j = 0; j < uvHeight; j++) {
				dst[k] = src[nPos + i];
				dst[k + 1] = src[nPos + i + 1];
				k += 2;
				nPos += srcWidth;
			}
		}
		return;
	}

	void YUV420spRotate180(byte[] dst, byte[] src, int srcWidth, int srcHeight) {
		int nWidth = 0, nHeight = 0;
		int wh = 0;
		int uvsize = 0;
		//int uvHeight = 0;
		if (srcWidth != nWidth || srcHeight != nHeight) {
			nWidth = srcWidth;
			nHeight = srcHeight;
			wh = srcWidth * srcHeight;
			//uvHeight = srcHeight >> 1;//uvHeight = height / 2
		}
		uvsize = wh >> 1;
		for (int i = 0; i < wh; i++) {
			dst[wh - 1 - i] = src[i];
		}
		for (int i = 0; i < uvsize; i += 2) {
			dst[wh + uvsize - 2 - i] = src[wh + i];
			dst[wh + uvsize - 1 - i] = src[wh + i + 1];
		}
		return;
	}

	void YUV420spRotate270(byte[] dst, byte[] src, int srcWidth, int srcHeight) {
		int nWidth = 0, nHeight = 0;
		int wh = 0;
		int uvHeight = 0;
		if (srcWidth != nWidth || srcHeight != nHeight) {
			nWidth = srcWidth;
			nHeight = srcHeight;
			wh = srcWidth * srcHeight;
			uvHeight = srcHeight >> 1;//uvHeight = height / 2
		}

		int k = 0;
		for (int i = 0; i < srcWidth; i++) {
			int nPos = srcWidth - 1;
			for (int j = 0; j < srcHeight; j++) {
				dst[k] = src[nPos - i];
				k++;
				nPos += srcWidth;
			}
		}

		for (int i = 0; i < srcWidth; i += 2) {
			int nPos = wh + srcWidth - 1;
			for (int j = 0; j < uvHeight; j++) {
				dst[k] = src[nPos - i - 1];
				dst[k + 1] = src[nPos - i];
				k += 2;
				nPos += srcWidth;
			}
		}
		return;
	}

	void YUV42left2right(byte[] dst, byte[] src, int srcWidth, int srcHeight) {
		// int nWidth = 0, nHeight = 0;
		int wh = 0;
		int uvHeight = 0;
		// if(srcWidth != nWidth || srcHeight != nHeight)
		{
			// nWidth = srcWidth;
			// nHeight = srcHeight;
			wh = srcWidth * srcHeight;
			uvHeight = srcHeight >> 1;// uvHeight = height / 2
		}

		// 转换Y
		int k = 0;
		int nPos = 0;
		for (int i = 0; i < srcHeight; i++) {
			nPos += srcWidth;
			for (int j = 0; j < srcWidth; j++) {
				dst[k] = src[nPos - j - 1];
				k++;
			}
		}
		nPos = wh + srcWidth - 1;
		for (int i = 0; i < uvHeight; i++) {
			for (int j = 0; j < srcWidth; j += 2) {
				dst[k] = src[nPos - j - 1];
				dst[k + 1] = src[nPos - j];
				k += 2;
			}
			nPos += srcWidth;
		}
		return;
	}

	boolean isScreenOriatationPortrait() {
		return false;
	}
}
