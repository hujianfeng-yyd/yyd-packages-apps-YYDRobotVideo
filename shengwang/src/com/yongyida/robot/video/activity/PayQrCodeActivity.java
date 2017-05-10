package com.yongyida.robot.video.activity;

import java.io.File;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.bean.PayResultBean;
import com.yongyida.robot.video.util.HttpUtilEn;
import com.yongyida.robot.video.util.JsonParser;
import com.yongyida.robot.video.util.ZxingUtils;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PayQrCodeActivity extends Activity{
private ImageView qrcode_iv;
private TextView rechange_type_tip;
private TextView robot_rid_tv;
private TextView pay_time_tv;
private TextView pay_amount_tv;
private String pay_type;
private String time_type;
private String time;
private String money;
private String rid;
private Thread qrcodeThread;
private Bitmap bitmap;
private String real_time = "0";
private boolean isneedRefush = false;
private final static String TAG = "PayQrCodeActivity";
private Handler mhandler = new Handler(){

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			Log.d(TAG, "qrcode create falut");
			Toast.makeText(PayQrCodeActivity.this, getString(R.string.generate_qrcode_error), Toast.LENGTH_SHORT).show();
			isneedRefush = false;
			break;
		case 1:
			Log.d(TAG, "qrcode create success");
			qrcode_iv.setImageBitmap(bitmap);
			//isneedRefush = true;
			queryRecharge();
			break;
		case 2:
			if(queryRechargeThread == null){
				return;
			}
			Log.d(TAG, "queryRecharge fault");
			queryRechargeThread = null;
			Toast.makeText(PayQrCodeActivity.this, getString(R.string.query_recharge_status_false), Toast.LENGTH_SHORT).show();
			isneedRefush = false;
			break;
		case 3:
			if(queryRechargeThread == null){
				return;
			}
			real_time = (String)msg.obj;
			int realTime = Integer.parseInt(real_time);
			int residualTime = Integer.parseInt(residual_time);
			Log.d(TAG, "queryRecharge success");
			if(realTime > residualTime){
				Log.d(TAG, "Recharge success");
				queryRechargeThread = null;
				Toast.makeText(PayQrCodeActivity.this, getString(R.string.recharge_sucess), Toast.LENGTH_SHORT).show();				
				isneedRefush = true;
			}/*else{
				Toast.makeText(PayQrCodeActivity.this, getString(R.string.recharge_false), Toast.LENGTH_SHORT).show();
				isneedRefush = false;
			}*/
			break;
		default:
			break;
		}
	};
};
private Thread queryRechargeThread;
@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.recharge_main_second);
	initView();
	initData();
	generateQrCode();
    
}
private void queryRecharge() {
	if(queryRechargeThread == null){
		Log.d(TAG, "start queryRechargeThread");
		queryRechargeThread = new Thread(queryRechargeRunnable);
		queryRechargeThread.start();
	}
	
}
Runnable queryRechargeRunnable = new Runnable() {
	
	@Override
	public void run() {

		try {
			Thread.sleep(10*1000);
			while(queryRechargeThread!=null){
				Log.d(TAG, "start queryRecharge");
				HttpUtilEn.SetUrlType(1);
				String result = HttpUtilEn.submitPostData(null, null, null, null, rid, 1);
				PayResultBean timeBean = JsonParser.parserQueryJson(result);
				if(timeBean.getRet()==-1){
			        mhandler.sendEmptyMessage(2);
			        
					return;
				}
				String real_time = timeBean.getRemain_time();
				Message msg = new Message();
				msg.what = 3;
				msg.obj = real_time;
				mhandler.sendMessage(msg);
				Thread.sleep(6*1000);
			//	queryRechargeRunnable = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		}
		
};


private void generateQrCode() {
	if(qrcodeThread == null){
		Log.d(TAG, "start qrcodeThread");
		qrcodeThread =  new Thread(qrcodeRunnable);		
		qrcodeThread.start();
	}
	
}



Runnable qrcodeRunnable = new Runnable() {

	@Override
	public void run() {
		HttpUtilEn.SetUrlType(0);		
		String result = HttpUtilEn.submitPostData(pay_type, time_type, money, time,rid, 0);
		PayResultBean resultBean = JsonParser.parserQueryJson(result);
		if(resultBean.getRet()==-1){
            mhandler.sendEmptyMessage(0);
            qrcodeThread = null;
			return;
		}
		String filePath = getFilesDir().getPath()+"payresult.png";
		ZxingUtils.getQRCodeImge(resultBean.getQrcode(), 400, filePath);
		File mFile=new File(filePath);
        //若该文件存在
        if (mFile.exists()) {
            bitmap = BitmapFactory.decodeFile(filePath);
            mhandler.sendEmptyMessage(1);
        }
        qrcodeThread = null;
	}
};
private ImageView pay_back;
private String residual_time;

private void initData() {
	Intent intent = getIntent();
	if(intent == null){
		return;
	}
	pay_type = intent.getStringExtra("pay_type");
	time_type = intent.getStringExtra("time_type");
	time = intent.getStringExtra("time");
	money = intent.getStringExtra("money");
	rid = intent.getStringExtra("rid");
	residual_time = intent.getStringExtra("residual_time");
	robot_rid_tv.setText(rid);
	pay_time_tv.setText(time+getString(R.string.pay_minute));
	pay_amount_tv.setText(money+getString(R.string.pay_yuan));
	if(pay_type.equals("alipay")){
		rechange_type_tip.setText(R.string.rechange_alipay_tip);
	}else{
		rechange_type_tip.setText(R.string.rechange_weixin_tip);
	}
}

private void initView() {
	qrcode_iv = (ImageView)findViewById(R.id.qrcode_iv);
	rechange_type_tip = (TextView)findViewById(R.id.rechange_type_tip);
	robot_rid_tv = (TextView)findViewById(R.id.robot_rid_tv);
	pay_time_tv = (TextView)findViewById(R.id.pay_time_tv);
	pay_amount_tv = (TextView)findViewById(R.id.pay_amount_tv);
	pay_back = (ImageView)findViewById(R.id.pay_back);
	pay_back.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("isneedrefush", isneedRefush);
			setResult(40,intent);
            finish();
		}
	});
}

@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		queryRechargeThread  =  null;
	}
}
