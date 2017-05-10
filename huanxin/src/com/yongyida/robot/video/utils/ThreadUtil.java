package com.yongyida.robot.video.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadUtil {
	private static ScheduledThreadPoolExecutor sExecutor;

	public static ScheduledThreadPoolExecutor getExecutor() {
		if (sExecutor == null) {
			sExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
		}
		return sExecutor;
	}
}
