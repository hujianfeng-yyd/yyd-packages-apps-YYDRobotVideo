package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.widget.CircleImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.yongyida.robot.video.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InviteDialogFragment extends DialogFragment implements OnClickListener {
	public static final String TAG = "InviteDialogFragment";

	private CircleImageView mImageView;
	private TextView mTvMessage;
	private Button mBtnCancel;
	private String mCallName;
	private String mPicture;
	private Handler mHandler;
	private TimeoutEventRunnable mTimeoutEventRunnable;
	private CancelListener mCancelListener;
	private int mTimeout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final Window window = getDialog().getWindow();
		View layout = inflater.inflate(R.layout.invite_dialog, ((ViewGroup) window.findViewById(android.R.id.content)),
				false);
		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

		Bundle bundle = getArguments();
		mCallName = bundle.getString("callname");
		mPicture = bundle.getString("picture");

		mImageView = (CircleImageView) layout.findViewById(R.id.img_userphoto);
		mTvMessage = (TextView) layout.findViewById(R.id.tv_message);
		mBtnCancel = (Button) layout.findViewById(R.id.btn_cancel);
		mImageView.setBorderWidth(0);
		mImageView.setImageBitmap(loadPicture(mPicture));
		mTvMessage.setText("正在呼叫：" + mCallName);
		mBtnCancel.setOnClickListener(this);

		if (mTimeout > 0) {
			openTimeoutEvent(mTimeout);
		}

		return layout;
	}

	public void setCancelListener(CancelListener cancelListener) {
		mCancelListener = cancelListener;
	}

	public void setTimeout(int timeout) {
		mTimeout = timeout;
	}

	private void openTimeoutEvent(int timeout) {
		log.d(TAG, "openTimeoutEvent(), timeout: " + timeout);

		mHandler = new Handler();
		mTimeoutEventRunnable = new TimeoutEventRunnable();
		mHandler.postDelayed(mTimeoutEventRunnable, timeout);
	}

	private void closeTimeoutEvent() {
		log.d(TAG, "closeTimeoutEvent()");
		mHandler.removeCallbacks(mTimeoutEventRunnable);
	}

	private Bitmap loadPicture(String path) {
		Bitmap bitmap = null;

		if (path != null && Utils.fileExists(path)) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(path);
				bitmap = BitmapFactory.decodeStream(fis);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_default2);
		}
		return bitmap;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_cancel: {
			handCancel();
			break;
		}
		default:
			break;
		}
	}

	private void handCancel() {
		log.d(TAG, "handCancel()");
		cancel(true);
	}

	private void timeoutCancel() {
		log.d(TAG, "timeoutCancel()");
		cancel(false);
	}

	private void cancel(boolean hand) {
		log.d(TAG, "cancel()");

		closeTimeoutEvent();
		if (mCancelListener != null) {
			mCancelListener.onCancel(hand);
		}
		close();
	}

	public void close() {
		log.d(TAG, "close()");
		closeTimeoutEvent();
		dismiss();
	}

	interface CancelListener {
		public void onCancel(boolean hand);
	}

	private class TimeoutEventRunnable implements Runnable {
		public void run() {
			log.d(TAG, "Timeout event for None Answer.");
			timeoutCancel();
		}
	}
}
