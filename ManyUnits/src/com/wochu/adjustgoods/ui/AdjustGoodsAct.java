package com.wochu.adjustgoods.ui;

import com.squareup.okhttp.Request;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.net.OkHttpClientManager;
import com.wochu.adjustgoods.net.OkHttpClientManager.ResultCallback;
import com.wochu.adjustgoods.utils.LogUtil;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class AdjustGoodsAct extends Activity implements OnClickListener{

	private Button btn_trance_orderNumber;
	private EditText et_code;
	private Button btn_confirm;
	private TextView tv_huoweihao;
	private ExpandableListView listview_showinformation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjustgoods);
		initView();
		initData();
	}
	private void initView() {
		// TODO Auto-generated method stub
		btn_trance_orderNumber = (Button) findViewById(R.id.btn_trance_orderNumber);
		et_code = (EditText) findViewById(R.id.et_code);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		tv_huoweihao = (TextView) findViewById(R.id.tv_huoweihao);
		listview_showinformation = (ExpandableListView) findViewById(R.id.listview_showinformation);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	@SuppressWarnings("unused")
	private void initData() {
		OkHttpClientManager.getAsyn("123",new ResultCallback<String>() {
			@Override
			public void onError(Request request, Exception e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				LogUtil.e(this.getClass().getSimpleName(),response);
			}
		});
	}
}
