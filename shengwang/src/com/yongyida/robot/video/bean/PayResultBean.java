package com.yongyida.robot.video.bean;

public class PayResultBean {

//返回json {"ret":0,"qrcode":xxx} ，错误是{"ret", "-1"，"msg"，"xxxxxxx"}
	
	private int ret ;
	private String qrcode;
	private String remain_time;
	
	public String getRemain_time() {
		return remain_time;
	}
	public void setRemain_time(String remain_time) {
		this.remain_time = remain_time;
	}
	public int getRet() {
		return ret;
	}
	public void setRet(int ret) {
		this.ret = ret;
	}
	public String getQrcode() {
		return qrcode;
	}
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}		
	
}
