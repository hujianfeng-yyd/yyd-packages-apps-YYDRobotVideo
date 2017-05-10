/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-09-01
 * 
 */
package com.yongyida.robot.video;

/**
 * APP常量
 *
 */
public class Constant {
	// 机器人信息URI
	public static final String URI_ROBOTID = "content://com.yongyida.robot.idprovider//id";
	
	// 服务名称
	public static final String ROBOT_VIDEO_SERVICE = "com.yongyida.robot.video.service.RobotVideoService";
	
	// 机器人全局广播
	public static final String GLOBAL_BROADCAST_ROBOT_STOP = "com.yydrobot.STOP";  //机器人与服务SOCKET断开连接
	
	// 机器人进入/退出视频广播
	public static final String GLOBAL_BROADCAST_ROBOT_ENTERVIDEO = "com.yydrobot.ENTERVIDEO";
	public static final String GLOBAL_BROADCAST_ROBOT_EXITVIDEO = "com.yydrobot.EXITVIDEO";
	public static final String GLOBAL_BROADCAST_ROBOT_ENTERMONITOR = "com.yydrobot.ENTERMONITOR";
	public static final String GLOBAL_BROADCAST_ROBOT_EXITMONITOR = "com.yydrobot.EXITMONITOR";
	public static final String PROVIDER_CONFIG_ITEM_VIDEOING = "videoing";
	
	// 机器人广播扩展字符串
	public static final String GLOBAL_BROADCAST_EXTRA_STRING = "result";
	public static final String BROADCAST_EXTRA_SHUT_DOWN_VIDEO = "shut_down_video";  //机器人与服务SOCKET断开连接
	public static final String BROADCAST_EXTRA_RINGUP = "ringup";  //来电
	
	// 视频模式
	public static final String VIDCEOMODE_CHAT = "chat";
	public static final String VIDCEOMODE_MONITOR = "controll";
}
