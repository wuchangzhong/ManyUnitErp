package com.wochu.adjustgoods.ui;

import java.util.ArrayList;
import com.libs.zxing.CaptureActivity;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;



import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.WeightingBean;
import com.wochu.adjustgoods.myinterface.AutoEquit;
import com.wochu.adjustgoods.ui.adapter.MyAdapter;
import com.wochu.adjustgoods.utils.NetworkAcess;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class WeightingAct extends Activity implements OnClickListener,AutoEquit{
	private Button btn_tranceGoods;
	private EditText et_weighting;
	private HttpUtils http;
	private NetworkAcess accessIntnet;
	private ListView lv_goodsweight;
	private MyAdapter myAdapter;
	private ArrayList<WeightingBean> weightList;
	private Context context;
	private Button btn_confirm;
	private boolean start;
	private int time ;
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
//					start =true;
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.goodsweighting_layout); 
		//打印了一句话
		System.out.println("打印了一句话");
		context = this;
		initView();
	}
	/** 
	 * 初始化界面
	 */
	private void initView() {
		btn_tranceGoods = (Button) findViewById(R.id.btn_tranceGoods);
		lv_goodsweight = (ListView) findViewById(R.id.lv_goodsweight);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		et_weighting = (EditText) findViewById(R.id.et_weighting);
		lv_goodsweight.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(position>=1){
				String weighting = weightList.get(position-1).weighting;
				et_weighting.setText(weighting);
				}
			}
		});
		btn_tranceGoods.setOnClickListener(this);
		btn_confirm.setOnClickListener(this);
		weightList = new ArrayList<WeightingBean>();
		/**
		 * 创建5个对象进行测试
		 */
			WeightingBean weightingBean = new WeightingBean();
			weightingBean.GoodsCode="11111111";
			weightingBean.GoodsName="苹果";
			weightingBean.IDNumber="1234556767";
			weightingBean.weighting="10";
			weightList.add(weightingBean);
			WeightingBean weightingBean1 = new WeightingBean();
			weightingBean1.GoodsCode="222222222";
			weightingBean1.GoodsName="橡胶";
			weightingBean1.IDNumber="2847489577";
			weightingBean1.weighting="15";
			weightList.add(weightingBean1);
			myAdapter = new MyAdapter(context,weightList);
			lv_goodsweight.setAdapter(myAdapter);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_tranceGoods:
			Intent intent = new Intent(this,CaptureActivity.class);
			startActivity(intent);
			break;
		//手动输入条形码扫描数据
		case R.id.btn_confirm:
			Intent intent1 = new Intent(this,CaptureActivity.class);
			startActivity(intent1);
		break;
		case R.id.btn_submit_weighting:
			@SuppressWarnings("unused")
			String weighting = et_weighting.getText().toString();
			//创建访问网络的对象
			accessIntnet = new NetworkAcess() {
			
				@Override
				public void accessFailure(String response) {
					// TODO Auto-generated method stub
					Toast.makeText(context,"网络有问题,请检查网络",1);
				}
				//访问网络成功时此方法被调用
				@Override
				public void accessSuccess(ResponseInfo<String> response) {
					
					/**
					 * 提交完商品的重量后返回商品的流水号ID
					 */
					/**
					 * 构建对象并且增加到集合中,同时把流水号idNumber增加到对象中
					 */
					/**‘；’；
					 * 刷新适配器
					 */
					myAdapter = new MyAdapter(context,weightList);
					lv_goodsweight.setAdapter(myAdapter);
				}
			};
			//开始访问服务器提供的借口     
			/**
			 * 根据weightBean对象的IDNumber值否为-1来决定请求方式
			 */
			accessIntnet.accessHttp("http://www.baidu.com"+"拼接字符串");
			
			accessIntnet=null;
			break;
		case R.id.btn_cancel_weighting:
			
			break;
		default:
			break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			Bundle bundle1 = data.getExtras();
			String scanResult = bundle1.getString("result");
			System.out.println(scanResult+"长度"+scanResult.length());
			checkOrder(scanResult);
		}
	}
	/**
	 * 检验商品是否存在
	 * @param scanResult
	 */
	private void checkOrder(String code) {
		// TODO Auto-generated method stub
		if(code.length()<8){
			Toast.makeText(getBaseContext(),"订单号有误",1).show();
			return;
		}
		accessIntnet = new NetworkAcess() {
			@Override
			public void accessSuccess(ResponseInfo<String> response) {
				// 返回此商品的相关信息
				//构建对象
				String result = response.result;
				// System.out.println("响应码"+result);
				if (result == null) {
					Toast.makeText(getBaseContext(), "该商品不存在", 1).show();
					return;
				}
			}
			@Override
			public void accessFailure(String response) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(), "访问网络失败,服务器无响应", 1).show();
			}
		};
		//开始访问服务器提供的借口
		accessIntnet.accessHttp("http://58.247.11.228:8082/api/WCS_TO_WMS/getWCS_TO_WMSlist?BoxNO=Box111212");
	}
	/**
	 * 解析json
	 * 
	 * @param result
	 */
//	private void analysisJson(String result) {
//		Gson gson = new Gson();
//		//gson.fromJson(result, typeOfT);
//	}
	
	

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
