package com.yongyida.robot.video;

import com.yongyida.robot.video.comm.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 自定义的 异常处理类 , 实现了 UncaughtExceptionHandler接口
 * 
 * @author hujianfeng@yongyida.com
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";
	private static CrashHandler sInstance;

	private CrashHandler() {
	}

	public static synchronized CrashHandler getInstance() {
		if (sInstance == null) {
			sInstance = new CrashHandler();
		}
		return sInstance;
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		log.e(TAG, "uncaughtException()");

		String errorinfo = getErrorInfo(ex);
		log.e(TAG, "ERROR: " + errorinfo);

		// 3秒后重启应用
		RobotApplication.getInstance().restart(3000);
	}

	private String getErrorInfo(Throwable arg) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		arg.printStackTrace(pw);
		pw.close();
		return writer.toString();
	}
}
