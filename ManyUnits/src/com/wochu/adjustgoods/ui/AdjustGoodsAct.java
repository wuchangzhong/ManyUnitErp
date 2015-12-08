package com.wochu.adjustgoods.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squareup.okhttp.Request;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.AdjustGoodBean;
import com.wochu.adjustgoods.bean.AdjustGoodBean.Goods;
import com.wochu.adjustgoods.net.OkHttpClientManager;
import com.wochu.adjustgoods.net.OkHttpClientManager.ResultCallback;
import com.wochu.adjustgoods.url.AppClient;
import com.wochu.adjustgoods.utils.LogUtil;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class AdjustGoodsAct extends Activity implements OnClickListener{

	private Button btn_trance_orderNumber;
	private EditText et_code;
	private Button btn_confirm;
	private TextView tv_huoweihao;
	private ExpandableListView listview_showinformation;
	private String locationCode = "";
	private ArrayList<String> goodsCodelist = new ArrayList<>();//商品编码的集合
	private ArrayList<ArrayList<Goods>> list = new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjustgoods);
		initView();
	}
	private void initView() {
		// TODO Auto-generated method stub
		btn_trance_orderNumber = (Button) findViewById(R.id.btn_trance_orderNumber);
		et_code = (EditText) findViewById(R.id.et_code);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		tv_huoweihao = (TextView) findViewById(R.id.tv_huoweihao);
		listview_showinformation = (ExpandableListView) findViewById(R.id.listview_showinformation);
		btn_confirm.setOnClickListener(this);
	};
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		btn_confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				locationCode = et_code.getText().toString();
				if(!TextUtils.isEmpty(locationCode)&&(!"".equals(locationCode))){
					initData();
					return;
				}
				Toast.makeText(getBaseContext(),"输入货位号不能为空",0).show();
			}
		});
	}
	@SuppressWarnings("unused")
	private void initData() {
		OkHttpClientManager.getAsyn(AppClient.GetGoodInformations+locationCode,new ResultCallback<AdjustGoodBean>() {
			@Override
			public void onError(Request request, Exception e) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),"获取商品信息失败",0).show();
				
			}
			@Override
			public void onResponse(AdjustGoodBean response) {
				// TODO Auto-generated method stub
				//LogUtil.e("AdjustGoodsAct",response);
//				try {
//					File fp = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "wo.txt");
//					FileWriter pw = new FileWriter(fp);
//					pw.write(response);
//					pw.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				String result = response.RESULT;
				if("success".equals(result)){
					//开始按商品编码进行分类
					goodClassificationByCode(response.DATA);
				}else{
					Toast.makeText(getBaseContext(),"获取商品信息失败",0).show();
				}
			}
		});
	}
	/**
	 * 开始按商品编码进行分类
	 */
	protected void goodClassificationByCode(List<Goods> data) {
		// TODO Auto-generated method stub
		for (int i = 0; i < data.size(); i++) {
			if(!goodsCodelist.contains(data.get(i).GOODSCODE)){
				goodsCodelist.add(data.get(i).GOODSCODE);
			}
		}
		for (int i = 0; i < goodsCodelist.size(); i++) {
			
		}
	}
}
