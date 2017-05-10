/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co.,Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

public class Utils {
	private static final String TAG = Utils.class.getSimpleName();
	
	public static String getAppName(Context ctx, int pid) {
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> i = l.iterator();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			if (info.pid == pid) {
				return info.processName;
			}
		}
		return null;
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 * 
	 */
	public static String getVersionInfo(Context ctx) {
		try {
			PackageManager manager = ctx.getPackageManager();
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			return "versionName: " + info.versionName + ", versionCode: " + info.versionCode;
		}
		catch (Exception e) {
			return "";
		}
	}

	/**
	 * 检测网络是否可用
	 * 
	 */
	public static boolean isNetWorkConnected(Context ctx) {
		if (ctx != null) {
			ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null) {
				return networkInfo.isAvailable();
			}
		}

		return false;
	}

	/** 
	 * 返回网络类型
	 * @param ctx Context 
	 * @return int
	 */
	public static NetType getNetWorkType(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			String type = networkInfo.getTypeName();
			if (type.equalsIgnoreCase("WIFI")) {
				log.i(TAG, "NETTYPE_WIFI");
				return NetType.NETTYPE_WIFI;
			}
			else if (type.equalsIgnoreCase("MOBILE")) {
				NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (mobileInfo != null) {
					switch (mobileInfo.getType()) {
					case ConnectivityManager.TYPE_MOBILE:// 移动网络  
						switch (mobileInfo.getSubtype()) {
						case TelephonyManager.NETWORK_TYPE_UMTS:
						case TelephonyManager.NETWORK_TYPE_EVDO_0:
						case TelephonyManager.NETWORK_TYPE_EVDO_A:
						case TelephonyManager.NETWORK_TYPE_HSDPA:
						case TelephonyManager.NETWORK_TYPE_HSUPA:
						case TelephonyManager.NETWORK_TYPE_HSPA:
						case TelephonyManager.NETWORK_TYPE_EVDO_B:
						case TelephonyManager.NETWORK_TYPE_EHRPD:
						case TelephonyManager.NETWORK_TYPE_HSPAP:
							log.i(TAG, "NETTYPE_3G");
							return NetType.NETTYPE_3G;
						case TelephonyManager.NETWORK_TYPE_CDMA:
						case TelephonyManager.NETWORK_TYPE_GPRS:
						case TelephonyManager.NETWORK_TYPE_EDGE:
						case TelephonyManager.NETWORK_TYPE_1xRTT:
						case TelephonyManager.NETWORK_TYPE_IDEN:
							log.i(TAG, "NETTYPE_2G");
							return NetType.NETTYPE_2G;
						case TelephonyManager.NETWORK_TYPE_LTE:
							log.i(TAG, "NETTYPE_4G");
							return NetType.NETTYPE_4G;
						default:
							log.i(TAG, "NETTYPE_NONE");
							return NetType.NETTYPE_NONE;
						}
					}
				}
			}
		}

		log.i(TAG, "NETTYPE_NONE");
		return NetType.NETTYPE_NONE;
	}

	/**
	 * 检测SDCARD是否存在
	 * @param
	 * @return boolean
	 *
	 */
	public static boolean isExitsSdcard() {
		return (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED));
	}

	/**
	 * 返回外部存储路径
	 * @param
	 * @return String
	 *
	 */
	public static String getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 返回拍照文件路径
	 * @param path
	 * @return String
	 * 
	 */
	public static String getStoragePath(String path) {
		String absPath = getExternalStorageDirectory() + "/" + path;
		File f = new File(absPath);
		if (!f.exists()) {
			f.mkdir();
		}
		return absPath;
	}

	/**
	 * 返回当前 时间字符串（年月日时分秒）
	 * @param
	 * @return String
	 *
	 */
	public static String getTimeString() {
		return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
	}

	/**
	 * 返回当前 时间字符串（年月日时分秒毫秒）
	 * @param
	 * @return String
	 *
	 */
	public static String getTimeMilsString() {
		return new SimpleDateFormat("HH:mm:ss SSS", Locale.getDefault()).format(new Date());
	}

	/**
	 * 保存Bitmap图像到文件
	 * @param filename
	 * @param bitmap
	 * @return boolean
	 *
	 */
	public static boolean saveBitmap(String filename, Bitmap bitmap, Bitmap.CompressFormat format) {
		log.d(TAG, "save bitmap to:" + filename);
		try {
			FileOutputStream fout = new FileOutputStream(filename);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			bitmap.compress(format, 100, bos);
			bos.flush();
			bos.close();
			return true;
		}
		catch (IOException e) {
			log.e(TAG, "save bitmap error:" + e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * 保存字节数组到文件
	 * @param filename
	 * @param bytes
	 * @return boolean
	 *
	 */
	public static boolean saveFile(String filename, byte[] bytes) {
		log.d(TAG, "saveFile: " + filename);

		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(filename);
			fOut.write(bytes);
			fOut.close();
			return true;
		}
		catch (FileNotFoundException e) {
			log.e(TAG, "File not found: " + e.getMessage());
		}
		catch (IOException e) {
			log.e(TAG, "Save file error: " + e.getMessage());
		}
		return false;
	}

	/**
	 * 返回屏幕横竖屏信息
	 * @param ctx
	 * @return int
	 * 
	 */
	public static int getScreenConfigurationOrientatioin(Context ctx) {
		int orientation = ctx.getResources().getConfiguration().orientation;

		log.d(TAG, "Configuration.orientation = " + orientation);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			log.d(TAG, "Configuration.ORIENTATION_PORTRAIT, orientation = " + orientation);
		else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
			log.d(TAG, "Configuration.ORIENTATION_LANDSCAPE, orientation = " + orientation);

		return orientation;
	}

	/**
	 * 设置屏幕方向，设置为横屏或竖屏后，屏幕就不会自动旋转。
	 * setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	 * == android:screenOrientation="landscape"
	 * @param ctx
	 * @return int
	 * 
	 */
	public static void setScreenOrientatioin(Activity activity, int screenOrientatioin) {
		activity.setRequestedOrientation(screenOrientatioin);
	}

	/**
	 * 返回屏幕横竖屏的设置值
	 * @param ctx
	 * @return int
	 * 
	 */
	public static int getScreenOrientatioin(Activity activity) {
		int orientation = activity.getRequestedOrientation();

		log.d(TAG, "Requested.Orientation = " + orientation);
		if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
			log.d(TAG, "ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, orientation=" + orientation);
		else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
			log.d(TAG, "ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, orientation=" + orientation);
		else if (orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
			log.d(TAG, "ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, orientation=" + orientation);
		else
			log.d(TAG, "ActivityInfo, orientation=" + orientation);

		return orientation;
	}

	/**
	 * 返回屏幕宽
	 * @param ctx
	 * @return int
	 *
	 */
	public static int getScreenWidth(Context ctx) {
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	/**
	 * 返回屏幕高
	 * @param ctx
	 * @return int
	 * 
	 */
	public static int getScreenHeight(Context ctx) {
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	/**
	 * 返回屏幕大小
	 * @param ctx
	 * @return
	 * 
	 */
	public static Point getDisplayMetrics(Context ctx) {
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		return new Point(width, height);
	}

	/**
	 * 返回屏幕比例
	 * @param ctx
	 * @return
	 * 
	 */
	public static float getScreenRate(Context ctx) {
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		float width = dm.widthPixels;
		float height = dm.heightPixels;
		float rate = width / height;
		return rate;
	}

	/**
	 * 返回字符串资源
	 * 
	 */
	static String getStringResource(Context ctx, int resId) {
		return ctx.getResources().getString(resId);
	}

	/**
	 * 返回顶层Activity
	 * 
	 */
	@SuppressWarnings("deprecation")
	public static String getTopActivity(Context ctx) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}

	/**
	 * 返回DeviceId
	 * 
	 */
	public static String getDeviceId(Context ctx) {
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * 返回SubscriberId
	 * 
	 */
	public static String getSubscriberId(Context ctx) {
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}

	/**
	 * 返回SimSerialNumber
	 * 
	 */
	public static String getSimSerialNumber(Context ctx) {
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSimSerialNumber();
	}

	/**
	 * 返回UID
	 * 
	 */
	public static String getUID(Context ctx) {
		return getSimSerialNumber(ctx);
	}

	/**
	 * 返回PhoneNumber
	 * 
	 */
	public static String getPhoneNumber(Context ctx) {
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getLine1Number();
	}

	/**
	 * 返回短整型的十六进制字符串
	 * 
	 */
	public static String getHexString(short value) {
		return String.format("0x%02X", value);
	}

	/**
	 * 返回整型的十六进制字符串
	 * 
	 */
	public static String getHexString(int value) {
		return String.format("0x%04X", value);
	}

	/**
	 * 返回长整型的十六进制字符串
	 * 
	 */
	public static String getHexString(long value) {
		return String.format("0x%08X", value);
	}

	/**
	 * 返回字节数组的十六进制字符串
	 * 
	 */
	public static String getHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase(Locale.getDefault()));
		}
		return sb.toString();
	}

	/**
	 * 图像旋转
	 * @param bitmap
	 * @param degrees
	 * @return
	 * 
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate((float) degrees);
		Bitmap rotaBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return rotaBitmap;
	}

	/**
	 * 返回本机IP
	 * @param ctx
	 * @return String
	 *
	 */
	public static String getIp(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return getIpString(ipAddress);
	}

	/**
	 * 整型IP转换成字符串型IP
	 * @param i
	 * @return
	 *
	 */
	public static String getIpString(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
	}

	/**
	 * 判断IP地址是否合法
	 * 
	 * @param ip
	 *            IP地址
	 * @return IP合法返回true，不合法返回false
	 */
	public static boolean isCorrectIp(String ip) {
		Pattern ipPattern = Pattern
				.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
		Matcher ipMatcher = ipPattern.matcher(ip);
		return ipMatcher.find();
	}

	/**
	 * 在Activity的非UI线程中显示提示
	 * @param activity
	 * @param msg
	 * @return
	 */
	public static void noUIThreadToast(final Activity activity, final String msg) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 在非UI线程中显示提示
	 * @param ctx
	 * @param msg
	 * @return
	 */
	public static void handlerToast(final Context ctx, final String msg) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			public void run() {
				Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static void screenOff(Context ctx) {
		log.d(TAG, "screenOff()");
		Intent intent = new Intent(Intent.ACTION_SCREEN_OFF);
		ctx.sendBroadcast(intent);
	}

	public static void screenOn(Context ctx) {
		log.d(TAG, "screenOn()");
		Intent intent = new Intent(Intent.ACTION_SCREEN_ON);
		ctx.sendBroadcast(intent);
	}

	/**
	 * 屏幕唤醒
	 * @param ctx
	 * @return
	 *
	 */
	@SuppressWarnings("deprecation")
	public static void wakeUp(Context ctx) {
		//获取电源管理器对象
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			//获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
			PowerManager.WakeLock wl = pm
					.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
			//点亮屏幕
			wl.acquire();
			//释放
			wl.release();
		}
	}

	/**
	 * 屏幕唤醒
	 * @param ctx
	 * @return
	 *
	 */
	@SuppressWarnings("deprecation")
	public static void wakeUp_screenOff(Context ctx) {
		//获取电源管理器对象
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		//获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");
		//点亮屏幕
		wakeLock.acquire();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				//释放
				wakeLock.release();
			}
		}, 1 * 1000);
	}
	
	/**
	 * 解锁
	 * @param ctx
	 * @return
	 *
	 */
	@SuppressWarnings("deprecation")
	public static void wakeUnlock(Context ctx) {
		KeyguardManager km = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		kl.disableKeyguard();
	}
}
