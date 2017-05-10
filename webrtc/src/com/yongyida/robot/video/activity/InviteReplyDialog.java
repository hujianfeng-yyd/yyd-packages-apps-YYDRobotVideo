package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.widget.CircleImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InviteReplyDialog extends Dialog implements OnClickListener {
	public static final String TAG = "InviteReplyDialog";

	private Context mContext;
	private CircleImageView mImageView;
	private TextView mTvUserName;
	private TextView mTvMessage;
	private Button mBtnOk;
	private Button mBtnCancel;
	private Handler mHandler;
	private TimerCheckRunnable mTimerCheckRunnable;
	private String mCallName;
	private String mPicture;

	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;
	protected AudioManager mAudioManager;
	protected SoundPool mSoundPool;
	protected int mSoundId;
	protected int mStreamId;
	private Listener mListener;
	private int mTimeout;

	public InviteReplyDialog(Context context, String callName) {
		super(context, R.style.Dialog);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		setCanceledOnTouchOutside(false);

		mContext = context;
		mCallName = callName;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View layout = inflater.inflate(R.layout.invite_reply_dialog, null);
		setContentView(layout);

		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK,
				"bright");
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();

		mImageView = (CircleImageView) layout.findViewById(R.id.img_userphoto);
		mTvUserName = (TextView) layout.findViewById(R.id.tv_username);
		mTvMessage = (TextView) layout.findViewById(R.id.tv_message);
		mBtnOk = (Button) layout.findViewById(R.id.btn_ok);
		mBtnCancel = (Button) layout.findViewById(R.id.btn_cancel);

		mImageView.setBorderWidth(0);
		mImageView.setImageBitmap(loadPicture(mPicture));
		mTvUserName.setText(mCallName);
		mTvMessage.setText(R.string.invite_replying);
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

		playCallSound();
		if (mTimeout > 0) {
			openTimeoutCheck(mTimeout);
		}
	}

	private void openTimeoutCheck(int timeout) {
		log.d(TAG, "openTimeoutCheck(), timeout time: " + timeout);

		mHandler = new Handler();
		mTimerCheckRunnable = new TimerCheckRunnable();
		mHandler.postDelayed(mTimerCheckRunnable, timeout);
	}

	private void closeTimeoutCheck() {
		log.d(TAG, "closeTimeoutCheck()");

		if (mHandler != null && mTimerCheckRunnable != null) {
			mHandler.removeCallbacks(mTimerCheckRunnable);
		}
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	public void setTimeout(int timeout) {
		mTimeout = timeout;
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
			bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_photo_default2);
		}
		return bitmap;
	}

	/**
	 * 播放拨号响铃
	 * 
	 */
	@SuppressWarnings("deprecation")
	public void playCallSound() {
		log.d(TAG, "playCallSound()");

		mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_RINGTONE);
		mAudioManager.setSpeakerphoneOn(false);
		mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
		mSoundId = mSoundPool.load(getContext(), R.raw.incoming, 1);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				try {
					mStreamId = mSoundPool.play(mSoundId, // 声音资源
							0.1f, // 左声道
							0.1f, // 右声道
							1, // 优先级，0最低
							-1, // 循环次数，0是不循环，-1是永远循环
							1); // 回放速度，0.5-2.0之间。1为正常速度
				}
				catch (Exception e) {
					log.e(TAG, "SoundPool play exception: " + e);
				}
			}
		});
	}

	/**
	 * 播放拨号响铃
	 * 
	 */
	public void stopCallSound() {
		if (mSoundPool != null) {
			mSoundPool.stop(mStreamId);
			mSoundPool.release();
			mSoundPool = null;
		}

		if (mAudioManager != null) {
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
		}
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btn_ok:
			replyAccept();
			break;
		case R.id.btn_cancel:
			replyCancel();
			break;
		default:
			break;
		}
	}

	private void replyAccept() {
		log.d(TAG, "replyAccept()");

		closeTimeoutCheck();
		if (mListener != null) {
			mListener.onOk();
		}
		close();
	}

	private void replyCancel() {
		log.d(TAG, "replyCancel()");

		closeTimeoutCheck();
		if (mListener != null) {
			mListener.onCancel();
		}
		close();
	}

	private void closeWakeLock() {
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	public void close() {
		log.d(TAG, "close()");

		closeTimeoutCheck();
		stopCallSound();
		closeWakeLock();
		dismiss();
	}

	public interface Listener {
		public void onOk();

		public void onCancel();

		public void onTimeout();
	}

	private class TimerCheckRunnable implements Runnable {
		public void run() {
			close();
			if (mListener != null) {
				mListener.onTimeout();
			}
		}
	}

}
