package com.yongyida.robot.video.hxvideo;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.yongyida.robot.video.Config;
import com.yongyida.robot.video.utils.log;

/**
 * 环信登录类
 * 因为原环信登录方法没有登录超时处理，所以封装成一个类，增加登录超时处理。
 * @author hujianfeng
 *
 */
public class HxUserLogin {
	private static final String TAG = HxUserLogin.class.getSimpleName();
	private int result = -1;
	private boolean recvNotify = false;

	/**
	 * 环信登录
	 * 
	 * @param username
	 * @param passwrod
	 * @return 登录成功返回0, 否则返回错误码
	 *
	 */
	public int login(String username, String passwrod) {
		log.d(TAG, "login()");
		
		recvNotify = false;
		try {
		    EMChatManager.getInstance().login(username, passwrod, new EMCallBack() {
	            @Override
	            public void onSuccess() {
	                result = 0;
	                log.i(TAG, "Hx login success !");
	                synchronized (HxUserLogin.this) {
	                    recvNotify = true;
	                    HxUserLogin.this.notifyAll();
	                }
	            }

	            @Override
	            public void onProgress(int progress, String status) {
	            }

	            @Override
	            public void onError(final int code, final String message) {
	                log.e(TAG, "Hx login failed, ErrorCode: " + code + ", message: " + message);
	                result = code;
	                synchronized (HxUserLogin.this) {
	                    recvNotify = true;
	                    HxUserLogin.this.notifyAll();
	                }
	            }
	        });
		}
		catch (Exception e) {
		    log.e(TAG, "Hx login exception: " + e.getMessage());
		}
		
		try {
			synchronized (this) {
				this.wait(Config.getInstance().getHxLoginTimeout());
				if (!recvNotify) {
					result = -9998;
					log.e(TAG, "Hx login wait timeout: " + Config.getInstance().getHxLoginTimeout() + "ms");
				}
			}
		}
		catch (InterruptedException e) {
			result = -9999;
			log.e(TAG, "Hx login wait InterruptedException!");
		}
		return result;
	}
}
