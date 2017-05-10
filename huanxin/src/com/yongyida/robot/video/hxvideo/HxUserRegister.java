package com.yongyida.robot.video.hxvideo;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.util.Log;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.yongyida.robot.video.Config;
import com.yongyida.robot.video.utils.log;

public class HxUserRegister {
	private static final String TAG = HxUserRegister.class.getSimpleName();

	/**
	 * 环信注册
	 * 
	 * @param username
	 * @param passwrod
	 * @return 注册成功返回0, 否则返回错误码
	 *
	 */
	public int register(String username, String passwrod) {
		int ret = -1;
		ExecutorService exec = Executors.newCachedThreadPool();
		Future<Integer> future = exec.submit(new RegisterCallable(username, passwrod));
		
		try {
			long beginTime = System.currentTimeMillis();
			int timeOut = Config.getInstance().getHxRegisterTimeout();
			
			while (true) {
				if (future.isDone()) {
					ret = future.get();
					break;
				}
				else if (System.currentTimeMillis() - beginTime > timeOut) {
					Log.e(TAG, "register TimeoutException");
					ret = -1;
					break;
				}
			}
		}
		catch (CancellationException e) {
			Log.e(TAG, "register CancellationException");
		}
		catch (ExecutionException e) {
			Log.e(TAG, "register ExecutionException");
		}
		catch (InterruptedException e) {
			Log.e(TAG, "register InterruptedException");
		}
		finally {
			exec.shutdown();
		}
		
		return ret;
	}

	/**
	 * 环信注册类
	 *
	 */
	class RegisterCallable implements Callable<Integer> {
		private static final String TAG = "RegisterCallable";
		private String mUsername;
		private String mPasswrod;

		public RegisterCallable(String username, String passwrod) {
			super();
			mUsername = username;
			mPasswrod = passwrod;
		}

		@Override
		public Integer call() throws Exception {
			int result = 1;

			try {
				// 调用sdk注册方法， 不能在主线程中运行
				EMChatManager.getInstance().createAccountOnServer(mUsername, mPasswrod);
				result = EMError.NO_ERROR;
				log.i(TAG, "HX register success, username:" + mUsername + ", passwrod:" + mPasswrod);
			}
			catch (EaseMobException e) {
				String error = "";
				result = e.getErrorCode();

				switch (result) {
				case EMError.NONETWORK_ERROR:
					error = "网络错误";
					break;
				case EMError.UNAUTHORIZED:
					error = "没有权限";
					break;
				case EMError.ILLEGAL_USER_NAME:
					error = "用户名不合法";
					break;
				case EMError.USER_ALREADY_EXISTS:
					error = "用户已经存在";
					break;
				default:
					error = e.getMessage();
					break;
				}
				log.e(TAG, "HX register failed, username:" + mUsername + ", passwrod:" + mPasswrod + ", ErrorCode:"
						+ result + ", ErrorMsg:" + error);
			}

			return result;
		}
	}
}
