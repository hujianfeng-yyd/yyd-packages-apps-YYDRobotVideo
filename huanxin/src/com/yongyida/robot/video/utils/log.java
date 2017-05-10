package com.yongyida.robot.video.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

/**
 * log日志类 同时写日志到Android logcat和logcat.log文件
 * 
 */
public class log {
	private static final String APPTAG = "Y20A";
	private static final String DEFAULT_LOG_FILENAME = "robotvideo.log";
	public static final int LOG_NONE = 0;
	public static final int LOG_ASSERT = 1;
	public static final int LOG_ERROR = 2;
	public static final int LOG_WARN = 3;
	public static final int LOG_INFO = 4;
	public static final int LOG_DEBUG = 5;
	public static final int LOG_VERBOSE = 6;
	private static log sInstance = null;
	private static int sLogLevel = LOG_DEBUG;
	private static SimpleDateFormat mFormatter
			= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.getDefault());
	
	private FileOutputStream mOutStream = null;
	private String mLogFilename = DEFAULT_LOG_FILENAME;
	private boolean mAppendMode = true;
	
	private log() {
		if (sLogLevel > LOG_NONE ) {
			openFile();
		}
	}
	
	private void openFile() {
		if (mOutStream == null) {
			try {
				mOutStream = new FileOutputStream(getLogPath() + "/" + mLogFilename, mAppendMode);
			}
			catch (FileNotFoundException e) {
				Log.e("log", "FileNotFoundException: " + e);
			}
		}
	}

	private void closeFile() {
		if (mOutStream != null) {
			try {
				mOutStream.flush();
				mOutStream.close();
				mOutStream = null;
			}
			catch (IOException e) {
				Log.e("log", "OutStream close exception: " + e);
			}
		}
	}
	
	/**
	 * 返回单例
	 *
	 */
	public static synchronized log getInstance() {
		if (sInstance == null) {
			sInstance = new log();
		}
		return sInstance;
	}
	
	/**
	 * 返回日志文件路径
	 * @return String
	 */
	private static String getLogPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 返回当前日期字符串
	 * @return String
	 */
	private static String getTimeString() {
		return mFormatter.format(new Date());
	}
	
	/**
	 * 写日志
	 * @param log
	 */
	private synchronized void write(String log) {
		if (sLogLevel != LOG_NONE && mOutStream != null && log != null) {
			try {
				byte[] bytes = log.getBytes("utf-8");
				mOutStream.write(bytes, 0, bytes.length);
			}
			catch (UnsupportedEncodingException e) {
				Log.e("log", "OutStream write exception: " + e);
			}
			catch (IOException e) {
				Log.e("log", "OutStream write exception: " + e);
			}
		}
	}
	
	// 如果用户忘记close()，则在系统回收的时候会自动close该对象。
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	/**
	 * 设置调试模式
	 *
	 */
	public static void setLogLevel(int logLevel) {
		if (sLogLevel != logLevel) {
			sLogLevel = logLevel;
		}
	}
	
	/**
	 * 写Info日志
	 *
	 */
	public static void i(String tag, String msg) {
		if (msg == null) return;
		
		if (sLogLevel >= LOG_INFO) {
			if (APPTAG != null) {
				tag = APPTAG + ", " + tag;
			}
			Log.i(tag, msg);
			getInstance().write(getTimeString() + "|Info|" + tag + "|" + msg + "\r\n");
		}
	}

	/**
	 * 写Debug日志
	 *
	 */
	public static void d(String tag, String msg) {
		if (msg == null) return;
		
		if (sLogLevel >= LOG_DEBUG) {
			if (APPTAG != null) {
				tag = APPTAG + ", " + tag;
			}
			Log.d(tag, msg);
			getInstance().write(getTimeString() + "|Debug|" + tag + "|" + msg + "\r\n");
		}
	}

	/**
	 * 写Warning日志
	 *
	 */
	public static void w(String tag, String msg) {
		if (msg == null) return;
		
		if (sLogLevel >= LOG_WARN) {
			if (APPTAG != null) {
				tag = APPTAG + ", " + tag;
			}
			Log.w(tag, msg);
			getInstance().write(getTimeString() + "|Warning|" + tag + "|" + msg + "\r\n");
		}
	}

	/**
	 * 写Error日志
	 *
	 */
	public static void e(String tag, String msg) {
		if (msg == null) return;
		
		if (sLogLevel >= LOG_ERROR) {
			if (APPTAG != null) {
				tag = APPTAG + ", " + tag;
			}
			Log.e(tag, msg);
			getInstance().write(getTimeString() + "|Error|" + tag + "|" + msg + "\r\n");
		}
	}

	/**
	 * 写Verbose日志
	 *
	 */
	public static void v(String tag, String msg) {
		if (msg == null) return;
		
		if (sLogLevel >= LOG_VERBOSE) {
			if (APPTAG != null) {
				tag = APPTAG + " " + tag;
			}
			Log.v(tag, msg);
			getInstance().write(getTimeString() + "|Verbose|" + tag + "|" + msg + "\r\n");
		}
	}
	
	/**
	 * 
	 * 关闭日志
	 *
	 */
	public static void close() {
		getInstance().closeFile();
	}
}
