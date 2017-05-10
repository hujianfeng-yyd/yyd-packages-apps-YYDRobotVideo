package com.yongyida.robot.video.service;



import com.yongyida.robot.video.bean.PayResultBean;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.util.HttpUtilEn;
import com.yongyida.robot.video.util.JsonParser;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class TimeCountService extends Service{

	private MyBinder mBinder = new MyBinder();
	private int time;
	private Handler handler;
	private boolean isrunnable;
	private Thread timeTread;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	public class MyBinder extends Binder{
		
		public TimeCountService getService(){
			return TimeCountService.this;
		}
	}
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	public void TimeCountStart(){
		
		if(timeTread==null){
			isrunnable = true;
			timeTread = new Thread(runnable);
			timeTread.start();
		}
	}
    /*public void setTime(int time){
		   this.time = time;
	}*/
	public void stopTimeCountService(){
		isrunnable = false;
		timeTread = null;
		//stopSelf();
	}
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			HttpUtilEn.SetUrlType(1);
			String result = HttpUtilEn.submitPostData(null, null, null, null, YYDSDKHelper.getInstance().getLoginUser().getId()+"", 1);
			PayResultBean timeBean = JsonParser.parserQueryJson(result);
			if(timeBean.getRet()==-1){
				handler.sendEmptyMessage(0);
				timeTread = null;
	         //   queryTimeThread = null;
				return;
			}
			String residual_time = timeBean.getRemain_time();
			 time = Integer.parseInt(residual_time);
			 Log.d("testtimecount", "time :"+time+"分钟");
			// time = 2;
			if(time<=0){
				handler.sendEmptyMessage(0);
			}
			if(time>0&&isrunnable){
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			while(time>0&&isrunnable){
			try {
				Thread.sleep(60*1000);
				time--;
				Log.d("testtimecount", "time :"+time+"分钟");
				if(time<=0){
					handler.sendEmptyMessage(0);
			    Log.d("testtimecount", "time :"+time+"分钟");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			}		
		}
	};
}
