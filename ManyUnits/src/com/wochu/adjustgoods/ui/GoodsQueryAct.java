package com.wochu.adjustgoods.ui;

import com.squareup.okhttp.Request;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.GoodsQuery;
import com.wochu.adjustgoods.net.OkHttpClientManager;
import com.wochu.adjustgoods.net.OkHttpClientManager.ResultCallback;
import com.wochu.adjustgoods.url.AppClient;
import com.wochu.adjustgoods.utils.JsonUtil;
import com.wochu.adjustgoods.utils.LogUtil;
import com.wochu.adjustgoods.utils.SharePreUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GoodsQueryAct extends Activity implements OnClickListener {
	private Button btn_trance_orderNumber;
	private EditText et_code;
	private Button btn_confirm;
	private TextView tv_huoweihao;
	private TextView tv_goodscode;
	private TextView tv_goodsname;
	private BroadcastReceiver receiver;//创建广播接收用于接收扫码之后的结果
	private String huoweihao="";//货位号
	private String goodscode="";//商品编码
	private String goodsName="";//商品名称
	private Context mcontext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actvity_goodsquery);
		initView();
		mcontext = this;
	}
	
	private void getGoodsInformation() {
		String huoweihao = SharePreUtil.getString(mcontext,"huoweihao","");
		String goodscode = SharePreUtil.getString(mcontext,"goodscode","");
		String goodsName = SharePreUtil.getString(mcontext,"goodsName","");
		this.huoweihao = huoweihao;
		this.goodscode = goodscode;
		this.goodsName = goodsName;
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getGoodsInformation();
		initData();
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		getGoodsInformation();
		initData();
	}
	
	private void initView() {
		btn_trance_orderNumber = (Button) findViewById(R.id.btn_trance_orderNumber);
		et_code = (EditText) findViewById(R.id.et_code);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		tv_huoweihao = (TextView) findViewById(R.id.tv_huoweihao);
		tv_goodscode = (TextView) findViewById(R.id.tv_goodscode);
		tv_goodsname = (TextView) findViewById(R.id.tv_goodsname);
		btn_trance_orderNumber.setOnClickListener(this);
		btn_confirm.setOnClickListener(this);
		receiver = new BroadcastReceiver() {//广播接收者
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals("com.android.scanservice.scancontext")){
					String sweepCode = intent.getStringExtra("Scan_context");//获得扫码值
					if(!TextUtils.isEmpty(sweepCode)){
							//网络访问获取该商品的货位号
						getInformationFromNetWork(sweepCode);//访问网路从网络中获取数据
					}
				}
			}
			
		};
		
		IntentFilter scanDataIntentFilter = new IntentFilter();
		//scanDataIntentFilter.addAction("com.android.scancontext"); //前台输出
		scanDataIntentFilter.addAction("com.android.scanservice.scancontext"); //后台输出
		registerReceiver(receiver, scanDataIntentFilter);//注册广播接收者
	}
	/**
	 * 联网操作
	 * @param tranceBoxCode
	 */
	private void getInformationFromNetWork(String tranceBoxCode) {
		// TODO Auto-generated method stub
		OkHttpClientManager.getAsyn(AppClient.LOCATIONURL+tranceBoxCode,new ResultCallback<GoodsQuery>() {
			
			@Override
			public void onError(Request request, Exception e) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),"网络访问失败",0).show();
			}
			@Override
			public void onResponse(GoodsQuery response) {
				// TODO Auto-generated method stub
				if("success".equals(response.RESULT)){
					LogUtil.e("GoodsQueryAct",response.DATA.get(0).LOCATIONCODE+"123");
					huoweihao = response.DATA.get(0).LOCATIONCODE;
					goodscode = response.DATA.get(0).GOODSCODE;
					goodsName = response.DATA.get(0).GOODSNAME;
					//保存数据
					SharePreUtil.putString(mcontext,"huoweihao", huoweihao);
					SharePreUtil.putString(mcontext,"goodscode", goodscode);
					SharePreUtil.putString(mcontext,"goodsName", goodsName);
					initData();//数据显示
				}else{
					Toast.makeText(mcontext,response.MESSAGE,0).show();
					//保存数据
					SharePreUtil.putString(mcontext,"huoweihao", "");
					SharePreUtil.putString(mcontext,"goodscode", "");
					SharePreUtil.putString(mcontext,"goodsName", "");
					initData();
				}
			}
		});
	}
	/**
	 * 数据显示
	 */
	private void initData() {
		// TODO Auto-generated method stub
		getGoodsInformation();;
		tv_huoweihao.setText("货位号:"+"\t"+huoweihao);
		tv_goodscode.setText("商品编码:"+"\t"+goodscode);
		tv_goodsname.setText("商品名称:"+"\t"+goodsName);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_trance_orderNumber:
			//扫码程序
			Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
			sendBroadcast(intent);//发送广播
			break;
		case R.id.btn_confirm:
			String sweepCode = et_code.getText().toString();
			if(TextUtils.isEmpty(sweepCode)){
				Toast.makeText(getBaseContext(),"商品编码不能为空",0).show();
				return;
			}
			//访问网络程序
			getInformationFromNetWork(sweepCode);
			break;
		default:
			break;
		}
	}
	
}
