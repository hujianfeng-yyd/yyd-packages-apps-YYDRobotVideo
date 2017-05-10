package com.yongyida.robot.video.activity;

import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.WVMReplyRequest;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.video.widget.CircleImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.yongyida.robot.video.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InviteDialog extends Dialog implements OnClickListener {
	public static final String TAG = "InviteDialog";

	private Context mContext;
	private CircleImageView mImageView;
	private TextView mTvMessage;
	private Button mBtnCancel;
	private String mCallName;
	private String mPicture;
	private Listener mListener;
	private int mTimeout;
	private Handler mHandler;
	private TimeoutCheckRunnable mTimeoutCheckRunnable;

	public InviteDialog(Context context, String callName) {
		super(context, R.style.Dialog);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		setCanceledOnTouchOutside(false);
		mContext = context;
		mCallName = callName;
		YYDVideoServer.getInstance().registerEventListener(mEventListener);
	}

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View layout = inflater.inflate(R.layout.invite_dialog, null);
		setContentView(layout);

		mImageView = (CircleImageView) layout.findViewById(R.id.img_userphoto);
		mTvMessage = (TextView) layout.findViewById(R.id.tv_message);
		mBtnCancel = (Button) layout.findViewById(R.id.btn_cancel);
		mImageView.setBorderWidth(0);
		mImageView.setImageBitmap(loadPicture(mPicture));
		mTvMessage.setText(mContext.getString(R.string.invite_calling) + mCallName);
		mBtnCancel.setOnClickListener(this);

		if (mListener != null) {
			mListener.onOpen();
		}

		if (mTimeout > 0) {
			openTimeoutCheck(mTimeout);
		}
	}

	public void setTimeout(int timeout) {
		mTimeout = timeout;
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	/**
	 * 打开超时检测
	 * 
	 * @param timeout
	 * @return
	 */
	private void openTimeoutCheck(int timeout) {
		log.d(TAG, "openTimeoutCheck(), timeout: " + timeout);

		mHandler = new Handler();
		mTimeoutCheckRunnable = new TimeoutCheckRunnable();
		mHandler.postDelayed(mTimeoutCheckRunnable, timeout);
	}

	/**
	 * 关闭超时检测
	 * 
	 * @param
	 * @return
	 */
	private void closeTimeoutCheck() {
		log.d(TAG, "closeTimeoutCheck()");

		if (mHandler != null && mTimeoutCheckRunnable != null) {
			mHandler.removeCallbacks(mTimeoutCheckRunnable);
		}
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
		log.d(TAG, "cancel()");

		closeTimeoutCheck();
		if (mListener != null) {
			mListener.onCancel();
		}
		close();
	}

	public void close() {
		log.d(TAG, "close()");

		closeTimeoutCheck();
		YYDVideoServer.getInstance().unRegisterEventListener(mEventListener);
		dismiss();
	}

	public interface Listener {
		public void onOpen();

		public void onCancel();

		public void onTimeout();
	}

	private class TimeoutCheckRunnable implements Runnable {
		public void run() {
			close();

			if (mListener != null) {
				mListener.onTimeout();
			}
		}
	}

	public EventListener mEventListener = new EventListener() {
		public void onEvent(Event event, final Object data) {
			log.d(TAG, "onEvent(), envet: " + event);

			switch (event) {
			//1：收到邀请响应,成功跳过，失败提示。
			case WVMInviteResponse:
				//房间内添加通话，收到邀请响应后关闭对话框。
				//if (YYDVideoServer.getInstance().isVideoing()) {
				//	close();
				//}
				break;
			//2：收到邀请取消响应
			case WVMInviteCancelResponse:
				break;
			//3：收到邀请答复，如果为“接受”不处理，如果为“拒绝”则提示后关闭。
			case WVMReplyRequest:
				WVMReplyRequest req = (WVMReplyRequest) data;
				if (req.getAnswer() == 0) {
					Utils.toast(InviteDialog.this.getContext(),
							InviteDialog.this.getContext().getString(R.string.The_other_party_has_declined));
				}
				close();
				break;
			case CommandTimeout:
				log.e(TAG, "CommandTimeout, cmdId: " + data);
				break;
			case CommandNotExecute:
				log.e(TAG, "CommandNotExecute, cmdId: " + data);
				break;
			default:
				break;
			}
		}
	};

}
