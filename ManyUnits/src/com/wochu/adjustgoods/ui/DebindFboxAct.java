package com.wochu.adjustgoods.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.DeBindInfo;
import com.wochu.adjustgoods.myinterface.AutoEquit;
import com.wochu.adjustgoods.net.MyHttpUtils;
import com.wochu.adjustgoods.receiver.PDAReceiver;
import com.wochu.adjustgoods.utils.LogUtil;


public class DebindFboxAct extends Activity implements OnClickListener ,AutoEquit{
private Button ib_scan;
private Button btn_confirm;
private Button btn_cancel;
private PDAReceiver receiver;
private SharedPreferences sp;
private int userID;
private String userName;
private EditText et_enterCode;
private boolean start;
private int time = 0;
private Handler handler = new Handler(){
	public void handleMessage(android.os.Message msg) {
		switch (msg.what) {
		case 0:
			time++;
			//锁屏十分钟activity关闭
			if(time<600){
				if(start){
					handler.sendEmptyMessageDelayed(0, 1000);
				}else{
					time = 0;

					
				}
			}else{
//				start =true;
				finish();
				time = 0;
			}
			break;

		default:
			break;
		}
	};
};
@Override
protected void onCreate(Bundle savedInstanceState) {
	
	super.onCreate(savedInstanceState);
	setContentView(R.layout.debind_layout);
	initView();
	sp = getSharedPreferences("config", MODE_PRIVATE);
	userID = sp.getInt("userID", 0);
	userName = sp.getString("user", null);
	receiver = new PDAReceiver() {
		
		@Override
		public void dispathCode(String code) {
			LogUtil.i("test", "接收广播");
			handleCode(code);
		}
	};
	IntentFilter scanDataIntentFilter = new IntentFilter();
	//scanDataIntentFilter.addAction("com.android.scancontext"); //前台输出
	scanDataIntentFilter.addAction("com.android.scanservice.scancontext"); //后台输出
	registerReceiver(receiver, scanDataIntentFilter);
}

private void initView(){
	
	ib_scan = (Button) findViewById(R.id.ib_scan);
	btn_confirm = (Button) findViewById(R.id.btn_confirm);
	btn_cancel = (Button) findViewById(R.id.btn_cancel);
	et_enterCode = (EditText) findViewById(R.id.et_enterCode);
	ib_scan.setOnClickListener(this);
	btn_confirm.setOnClickListener(this);
	btn_cancel.setOnClickListener(this);
}

private void deBindFbox(String code){
	MyHttpUtils http = new MyHttpUtils();
	String url = "http://192.168.34.220:8088/api/WCS_TO_WMS/DebindFBOX?"+"BoxNO="+code+"&UserID="+userID
			+"&UserName="+userName;
//	url += "BoxNO="+code;
//	url += "&UserID="+userID;
//	url += "&UserName="+userName;
	http.callBack = new RequestCallBack<String>() {
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			// TODO Auto-generated method stub
			
			processData(responseInfo.result);
		}
		
		@Override
		public void onFailure(HttpException error, String msg) {
		error.printStackTrace();
			LogUtil.i("message", "服务器无响应");
		}
	};
	http.POST(url);
}

@Override
public void onClick(View v) {
	switch (v.getId()) {
	case R.id.ib_scan:
		Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
		sendBroadcast(intent);
		break;
	case R.id.btn_confirm:
		String code = et_enterCode.getText().toString();
		handleCode(code);
		break;
	case R.id.btn_cancel:
		et_enterCode.setText("");
		break;
	default:
		break;
	}
	
}
/**
 * 处理广播接收的条码
 * @param code
 */
private void handleCode(String code){
	et_enterCode.setText(code);
	deBindFbox(code);
}
/**
 * 解析网络响应
 * @param result
 */
private void processData(String result){
	Gson gson = new Gson();
	DeBindInfo deBindInfo = gson.fromJson(result, DeBindInfo.class);
	Toast.makeText(this, deBindInfo.MESSAGE, Toast.LENGTH_SHORT).show();
}


@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		equit();
	}

@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		cancleEquit();
	}
@Override
public void equit() {
	start = true;
	handler.sendEmptyMessage(0);
	
}

@Override
public void cancleEquit() {
	start = false;
}
}
