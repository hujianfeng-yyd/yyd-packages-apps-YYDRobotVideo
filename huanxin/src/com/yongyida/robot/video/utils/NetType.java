package com.yongyida.robot.video.utils;

public enum NetType {
	NETTYPE_NONE(0),
	NETTYPE_2G(2),
	NETTYPE_3G(3),
	NETTYPE_4G(4),
	NETTYPE_5G(5),
	NETTYPE_WIFI(98),
	NETTYPE_LAN(99);
	
	private int mValue;
	
	private NetType(int value) {
		mValue = value;
	}
	
	public int value() {
		return mValue;
	}
}
