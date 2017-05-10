package com.yongyida.robot.video.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.yongyida.robot.video.R;
import com.yongyida.robot.video.bean.PayResultBean;
import com.yongyida.robot.video.util.HttpUtilEn;
import com.yongyida.robot.video.util.JsonParser;

public class PayMainActivity extends Activity implements OnClickListener ,OnCheckedChangeListener{
    private Button pay_help_bt;
	private ToggleButton pay_type_one_tg;
	private ToggleButton pay_type_two_tg;
	private ToggleButton pay_type_three_tg;
	private ToggleButton pay_type_four_tg;
	private ToggleButton pay_type_five_tg;
	private ToggleButton pay_type_six_tg;
	private boolean isclickable = false;
	private String time_type;
	private String time;
	private String money;
	private RadioGroup radioGroup_paytype;
	private Button bt_sumbit;
	private String pay_type;
	private Thread queryTimeThread;
	private TextView residual_time_tv;
	private String rid;
	private static String residual_time;
	private ImageView pay_back;
	
    private Handler mhandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case 0:
				Toast.makeText(PayMainActivity.this, getString(R.string.query_time_error), Toast.LENGTH_SHORT).show();

				break;
			case 1:
				 String residualtime=(String) msg.obj;
				 if(residualtime == null||residualtime.equals("")){
					 residualtime = "0";
				 }
				residual_time_tv.setText(residualtime+getString(R.string.pay_minute));	
				/*if(isneedrefush){
					int residualTime = Integer.parseInt(residualtime);
					int tempTime = Integer.parseInt(temp_time);
					if(residualTime>tempTime){
						Toast.makeText(PayMainActivity.this, getString(R.string.recharge_sucess), Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(PayMainActivity.this, getString(R.string.recharge_false), Toast.LENGTH_SHORT).show();
					}
				}*/
				break;
			case 2:
				
				break;
			default:
				break;
			}
    	};
    };
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.pay_main_layout);
    	initView();
    	initData();
    	
    }

	private void initData() {
		Intent intent = getIntent();
		if(intent == null){
			return;
		}
		rid = intent.getStringExtra("rid");
		residual_time = intent.getStringExtra("time");
		residual_time_tv.setText(residual_time+getString(R.string.pay_minute));
		pay_type_one_tg.setChecked(true);
		isclickable = true;
		time = "50";
		money = "10";
		time_type = getString(R.string.pay_type_one);
		queryTime();
	}

	private void queryTime() {
		if(queryTimeThread == null){
			queryTimeThread = new Thread(queryTimeRunnable);			
		}
		queryTimeThread.start();
	}

	Runnable queryTimeRunnable = new Runnable() {
		
		@Override
		public void run() {
			HttpUtilEn.SetUrlType(1);
			String result = HttpUtilEn.submitPostData(null, null, null, null, rid, 1);
			PayResultBean timeBean = JsonParser.parserQueryJson(result);
			if(timeBean.getRet()==-1){
	            mhandler.sendEmptyMessage(0);
	            queryTimeThread = null;
				return;
			}
			residual_time = timeBean.getRemain_time();
			Message msg = new Message();
			msg.what = 1;
			msg.obj = residual_time;
			mhandler.sendMessage(msg);
			queryTimeThread = null;
		}
	};
	private boolean isneedrefush;
	private String temp_time;

	
	
	private void initView() {
		 residual_time_tv = (TextView)findViewById(R.id.residual_time_tv);
		 pay_help_bt = (Button)findViewById(R.id.pay_help_bt);
    	 bt_sumbit = (Button)findViewById(R.id.bt_sumbit);
    	 pay_back = (ImageView)findViewById(R.id.pay_back); 
    	 pay_back.setOnClickListener(this);		
    	 pay_help_bt.setOnClickListener(this); 
    	 radioGroup_paytype = (RadioGroup)findViewById(R.id.radioGroup_paytype);
    	 pay_type_one_tg = (ToggleButton)findViewById(R.id.pay_type_one_tg);
    	 pay_type_two_tg = (ToggleButton)findViewById(R.id.pay_type_two_tg);
    	 pay_type_three_tg = (ToggleButton)findViewById(R.id.pay_type_three_tg);
    	 pay_type_four_tg = (ToggleButton)findViewById(R.id.pay_type_four_tg);
    	 pay_type_five_tg = (ToggleButton)findViewById(R.id.pay_type_five_tg);
    	 pay_type_six_tg = (ToggleButton)findViewById(R.id.pay_type_six_tg);
    	 pay_type_one_tg.setOnCheckedChangeListener(this);
    	 pay_type_two_tg.setOnCheckedChangeListener(this);
    	 pay_type_three_tg.setOnCheckedChangeListener(this);
    	 pay_type_four_tg.setOnCheckedChangeListener(this);
    	 pay_type_five_tg.setOnCheckedChangeListener(this);
    	 pay_type_six_tg.setOnCheckedChangeListener(this);
    	 bt_sumbit.setOnClickListener(this);
    	 pay_type_one_tg.setOnClickListener(this);
    	 pay_type_two_tg.setOnClickListener(this);
    	 pay_type_three_tg.setOnClickListener(this);
    	 pay_type_four_tg.setOnClickListener(this);
    	 pay_type_five_tg.setOnClickListener(this);
    	 pay_type_six_tg.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		isclickable = true;
		switch (v.getId()) {
		case R.id.pay_help_bt:
			Intent intent = new Intent(PayMainActivity.this,PayHelpActivity.class);
			startActivity(intent);
			break;
		case R.id.bt_sumbit:
			switch (radioGroup_paytype.getCheckedRadioButtonId()) {
			case R.id.pay_type_one:
				pay_type = "alipay";
				break;
			case R.id.pay_type_two:
			    pay_type = "weixinpay";
			    break;
			default:
				break;
			}
			Intent info = new Intent(PayMainActivity.this,PayQrCodeActivity.class);
			info.putExtra("pay_type", pay_type);
			info.putExtra("time_type", time_type);
			info.putExtra("time", time);
			info.putExtra("money", money);
			info.putExtra("rid", rid);
			info.putExtra("residual_time", residual_time);
			startActivityForResult(info, 40);
			break;
		case R.id.pay_back:
			setResult(30);
            finish();
			break;

		default:
			break;
		}
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 40){
			isneedrefush = data.getBooleanExtra("isneedrefush", false);
			temp_time = residual_time;
			if(isneedrefush){
				queryTime();				
			}
		}
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(!isclickable){		
			return;
		}
		isclickable = false;
		switch (buttonView.getId()) {
		case R.id.pay_type_one_tg:
			changeToggleButtonView("one");
			break;
		case R.id.pay_type_two_tg:
			changeToggleButtonView("two");
			break;
		case R.id.pay_type_three_tg:
			changeToggleButtonView("three");
			break;
		case R.id.pay_type_four_tg:
			changeToggleButtonView("four");
			break;
		case R.id.pay_type_five_tg:
			changeToggleButtonView("five");
			break;
		case R.id.pay_type_six_tg:
			changeToggleButtonView("six");
			break;
			
		default:
			break;
		}
		
	}

	private void changeToggleButtonView(String str) {
		pay_type_one_tg.setChecked(false);
		pay_type_two_tg.setChecked(false);
		pay_type_three_tg.setChecked(false);
		pay_type_four_tg.setChecked(false);
		pay_type_five_tg.setChecked(false);
		pay_type_six_tg.setChecked(false);
	
		switch (str) {
		case "one":
			pay_type_one_tg.setChecked(true);
			time_type = getString(R.string.pay_type_one);
			time = "50";
			money = "10";
			break;
		case "two":
			pay_type_two_tg.setChecked(true);
			time_type = getString(R.string.pay_type_two);
			time = "100";
			money = "20";
			break;
		case "three":
			pay_type_three_tg.setChecked(true);
			time_type = getString(R.string.pay_type_three);
			time = "200";
			money = "40";
			break;
		case "four":
			pay_type_four_tg.setChecked(true);
			time_type = getString(R.string.pay_type_four);
			time = "500";
			money = "100";
			break;
		case "five":
			pay_type_five_tg.setChecked(true);
			time_type = getString(R.string.pay_type_five);
			time = "1000";
			money = "200";
			break;
		case "six":
			pay_type_six_tg.setChecked(true);
			time_type = getString(R.string.pay_type_six);
			time = "5000";
			money = "1000";
			break;
		default:
			break;
		}
		
	}
}
