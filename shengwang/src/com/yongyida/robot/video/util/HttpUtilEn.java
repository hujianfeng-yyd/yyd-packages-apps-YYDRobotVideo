package com.yongyida.robot.video.util;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilEn {

	    private static String path;
		private static int current_type;




		 public static void SetUrlType(int type){
			 current_type  = type;
			 switch (type) {
				 case 0:
					 path = "http://120.24.213.239:81/robot/payment";
					 //120.24.242.163
					// path = "http://120.24.242.163:81/robot/payment";
					 break;
				 case 1:
					 path = "http://120.24.213.239:81/media_remain_time/search";
					// path = "http://120.24.242.163:81/media_remain_time/search";
					 break;
			default:
				break;
			}
		 }
			/**
			 *  httpget请求
			 * */

	public static String HttpGet() {
    	try {

					URL url = new URL(path);
					Log.d("jlog", path);
					HttpURLConnection httpconn =(HttpURLConnection) url.openConnection();
					if(httpconn.getResponseCode() == HttpURLConnection.HTTP_OK){
						InputStreamReader isr = new InputStreamReader(httpconn.getInputStream(),"utf-8");
						int len = 0;
						StringBuffer str = new StringBuffer();
						while((len = isr.read())!=-1){
							str.append((char)len);
						}
						Log.d("voiceUnderstand", "json:" + str.toString());
						httpconn.disconnect();
						return str.toString();
					}else{
						Log.d("voiceUnderstand", "server error");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
				
		    }
		 /**
		  * payment_method string（支付方法：alipay or weixinpay）
		  * payment_mode string （50分钟/10元 100分钟/20元 ）
		  * total_amount long （支付金额）
		  * id long (机器人id)
		  * */
		  public static String submitPostData(String payment_method,String payment_mode,String total_amount,String time, String userId,int rc) {
			  HashMap<String,String> params = new HashMap<String, String>();
		   switch (rc) {
			case 0:
				params.put("payment_method", payment_method);//支付方法：alipay or weixinpay
				params.put("payment_mode", payment_mode);//50分钟/10元 100分钟/20元
				params.put("total_amount", total_amount);	//支付金额
				params.put("id", userId);//机器人id
				params.put("time", time);
				break;
			case 1:
				params.put("id", userId);
				break;
			default:
				break;
			}
		        byte[] data = getRequestData(params,"encode").toString().getBytes();//获得请求体
		        try {            
		            
		            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php"; 
		            URL url = new URL(path);  
		            Log.d("jlog", path);
		            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
		            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
		            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
		            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
		            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
		            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
		            //httpURLConnection.
		            //设置请求体的类型是文本类型
		            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		            //设置请求体的长度
		            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
		            //获得输出流，向服务器写入数据
		            OutputStream outputStream = httpURLConnection.getOutputStream();
		            outputStream.write(data);
		            
		            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
		            if(response == HttpURLConnection.HTTP_OK) {
		                InputStream inptStream = httpURLConnection.getInputStream();
		             //   httpURLConnection.disconnect();
		                return dealResponseResult(inptStream);                     //处理服务器的响应结果
		            }
		        } catch (IOException e) {
		            //e.printStackTrace();
		            //return "err: " + e.getMessage().toString();
		        	return "";
		        }
		        return "";
		    }
		    
		    /*
		     * Function  :   封装请求体信息
		     * Param     :   params请求体内容，encode编码格式
		     */
		   public static String getRequestData(Map<String, String> params, String encode) {
		    //  StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
			   String json_str = "";
			   try {
		    	  JSONObject object = new JSONObject();
		    	  for(Map.Entry<String, String> entry : params.entrySet()) {
		    		  object.put(entry.getKey(), entry.getValue());  
		    	  }
		    	json_str = object.toString();
		        } catch (Exception e) {
		           e.printStackTrace();
		       }
		       return json_str;
		    }
		   
		   /*
		    * Function  :   处理服务器的响应结果（将输入流转化成字符串）
		    * Param     :   inputStream服务器的响应输入流
		    */
		   public static String dealResponseResult(InputStream inputStream) {
		      String resultData = null;      //存储处理结果
		       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		      byte[] data = new byte[1024];
		      int len = 0;
		       try {
		          while((len = inputStream.read(data)) != -1) {
		             byteArrayOutputStream.write(data, 0, len);
		          }
		     } catch (IOException e) {
		         e.printStackTrace();
		        }
		       resultData = new String(byteArrayOutputStream.toByteArray());    
		       return resultData;
		   }    
		    
}