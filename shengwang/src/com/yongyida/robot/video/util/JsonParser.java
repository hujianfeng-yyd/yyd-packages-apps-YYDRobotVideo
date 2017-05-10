package com.yongyida.robot.video.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.yongyida.robot.video.bean.PayResultBean;



public class JsonParser {
	public static PayResultBean parserQueryJson(String result){
		PayResultBean resultBean = new PayResultBean();
	    if(result == null){
	    	resultBean.setRet(-1);
	    	return resultBean;
	    }
	    try {
			JSONObject jsonobject = new JSONObject(result);
			 int ret = jsonobject.getInt("ret");
			 String qrcode = jsonobject.optString("qrcode");
			 String remain_time = jsonobject.optString("remain_time");
			 resultBean.setRet(ret);
			 resultBean.setQrcode(qrcode);
			 resultBean.setRemain_time(remain_time);
		} catch (JSONException e) {
			resultBean.setRet(-1);
			e.printStackTrace();
		}
		return resultBean;
	}
	


}