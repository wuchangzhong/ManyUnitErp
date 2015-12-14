package com.wochu.adjustgoods.ui;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpException;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.squareup.okhttp.internal.http.HttpMethod;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.DeBindInfo;
import com.wochu.adjustgoods.bean.OrderDetail;
import com.wochu.adjustgoods.bean.OrderDetail.OrderInfo;
import com.wochu.adjustgoods.myinterface.AutoEquit;
import com.wochu.adjustgoods.net.MyHttpUtils;
import com.wochu.adjustgoods.net.ServerUrl;
import com.wochu.adjustgoods.receiver.PDAReceiver;
import com.wochu.adjustgoods.utils.LogUtil;
import com.wochu.adjustgoods.wiget.MySwipeRefreshLayout;


/**
 * 复核区界面
 * 
 * @author Administrator
 * 
 */
public class SortingAct extends Activity implements OnClickListener ,AutoEquit,MySwipeRefreshLayout.OnRefreshListener{

	public static final int UNBINDTRANCEBOX = 0;
	public static final int COMITSENDINGINFO = 1;
	public static final int COMITERRORBOX = 2;

	// 储存发货箱的list
	private ArrayList<String> sendBoxes;
	private String userName;
	private Button btn_scan;
	public String tranceBoxCode;
	public String sendBoxCode;
public boolean canComit = true;//防止多次下拉提交数据出错
	private EditText et_code;
	public int step1 = 0;
	public int step2 = 0;
private int time ;
private boolean start;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {

			case 1:
				// 发货箱可用,提交数据
				moreDialog();

				break;
			case 2:
				// 发货箱不能用
				Toast.makeText(SortingAct.this, "发货箱未解绑或不可用",
						Toast.LENGTH_SHORT).show();
				// 清空输入框，发货箱对象置空

				break;
			case 3:

				showDialog();
				
				
				
				break;
			case 4:
				// 清除数据
//				et_tranceBoxcode.setText("");
				//et_sendBoxcode.setText("");
				et_code.setText("");
				tranceBoxCode = null;
				sendBoxCode = null;
				tv_orderItemNum.setText("");
				tv_SortNum.setText("");
				tv_difference.setText("");
				tv_tranceNum.setText("");
				tv_sendPort.setText("");
//				et_tranceBoxcode.setFocusable(true);
				
//				et_tranceBoxcode.setFocusableInTouchMode(true);
//				et_tranceBoxcode.requestFocus();
//				et_tranceBoxcode.findFocus();
				tv_orderNo.setText("");
				orderDetail=null;
				
				handler.sendEmptyMessageDelayed(5, 1000);
				
			
				
				break;
			case 5:
				//刷新listview
				sendBoxes.removeAll(sendBoxes);
				myAdapter.notifyDataSetChanged();
				break;
			case 6:
				time++;
				//锁屏十分钟activity关闭
				if(time<600){
					if(start){
						handler.sendEmptyMessageDelayed(6, 1000);
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
		}

	};
	private ProgressBar loading;
	private ListView lv_detail;
	private EditText et_sendBoxcode;

	private Button btn_confirmSendBox;
	private Button btn_sendBox;
	private Button btn_cancleSendBox;
	private SensorManager sensorManager;
private int receiverCount= 0;
	private OrderDetail orderDetail;
	private AlertDialog.Builder builder1 =null;
	private ListView lv_sendBoxes;
	private MyAdapter myAdapter;
	private TextView tv_orderNo;
//	private BroadcastReceiver receiver;
	private TextView tv_tranceNum;
	private TextView tv_orderItemNum;
	private TextView tv_SortNum;
	private int userID;
	private TextView tv_sendPort;
	private Button btn_confirm;
	private Button btn_confirm_checklocation;
	private Button btn_cancle_checklocation;
	private EditText et_sortingPlace;
	private AlertDialog checkLoacationDialog;
	private String checkLocation;
	private MySwipeRefreshLayout sr_sendBoxInfo;
	private TextView tv_difference;
	private TextView tv_sortingPlace;
	private PDAReceiver receiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sorting_layout);
		initView();
		

		// 获取操作员工账号
		SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		userName = sp.getString("user", null);
		userID = sp.getInt("userID", 0);
		System.out.println(userID);
		sendBoxes = new ArrayList<String>();
		
		
		
		receiver = new PDAReceiver() {
			
			@Override
			public void dispathCode(String code) {
				// TODO Auto-generated method stub
				handleCode(code);
			}
		};
		IntentFilter scanDataIntentFilter = new IntentFilter();
		//scanDataIntentFilter.addAction("com.android.scancontext"); //前台输出
	scanDataIntentFilter.addAction("com.android.scanservice.scancontext"); //后台输出
		registerReceiver(receiver, scanDataIntentFilter);
		
		showChooseSortingPositionDialog();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {

		btn_scan = (Button) findViewById(R.id.btn_scan);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);

		et_code = (EditText) findViewById(R.id.et_code);
tv_sortingPlace = (TextView) findViewById(R.id.tv_sortingPlace);
		 tv_tranceNum = (TextView) findViewById(R.id.tv_tranceNum);
	
		loading = (ProgressBar) findViewById(R.id.pb_loading);
		
		lv_sendBoxes = (ListView) findViewById(R.id.lv_sendBoxes);
		tv_orderNo = (TextView) findViewById(R.id.tv_orderNo);
		tv_orderItemNum = (TextView) findViewById(R.id.tv_orderItemNum);
		tv_SortNum = (TextView) findViewById(R.id.tv_SortNum);
		 tv_sendPort = (TextView) findViewById(R.id.tv_sendPort);
		 sr_sendBoxInfo = (MySwipeRefreshLayout) findViewById(R.id.sr_sendBoxInfo);
		 tv_difference = (TextView) findViewById(R.id.tv_difference);
		btn_scan.setOnClickListener(this);
		btn_confirm.setOnClickListener(this);
		sr_sendBoxInfo.setOnRefreshListener(this);
		sr_sendBoxInfo.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_scan:
			System.out.println("点击了");
//			Intent intent1 = new Intent(SortingAct.this, CaptureActivity.class);
//			startActivityForResult(intent1, 0);
			Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
			sendBroadcast(intent);
			break;
		case R.id.btn_cancle:
			
			et_code.setText("");
			
			break;
		case R.id.btn_confirm:
			
			String str = et_code.getText().toString();
			setString(str);
			if(isTranceBox(str)){
				if(sendBoxes.size()>0&&tranceBoxCode!=str){
					comitData(COMITSENDINGINFO);
				}
				tranceBoxCode = str;
				checkOrder(str);
			}else {
			if(tranceBoxCode!=null){
				if(isSendBox(str)){
					boolean flag = true;
					for(String sendboxCode:sendBoxes){
						if(str==sendboxCode){
							Toast.makeText(getApplicationContext(), "请勿重复扫描", 0).show();
							flag = false;
						}
					}
				sendBoxCode = str;
				if(flag){
				checkSendBox();
				}
				}else{
					Toast.makeText(getApplicationContext(), "条码有误", Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(getApplicationContext(), "请先扫描周转箱", Toast.LENGTH_SHORT).show();
			}
			}
			break;
		case R.id.btn_confirm_checklocation:
			checkLocation = "A"+et_sortingPlace.getText().toString();
			Pattern pattern = Pattern.compile("^A\\d{1,2}");
			Matcher matcher = pattern.matcher(checkLocation);
			boolean flag = matcher.matches();
			if(!matcher.matches()){
				Toast.makeText(getApplicationContext(), "请输入正确复核位", 0).show();
				
			}else{
				tv_sortingPlace.setText(checkLocation);
				checkLoacationDialog.dismiss();
			}
			break;
		case R.id.btn_cancle_checklocation:
			et_sortingPlace.setText("");
			break;
		default:
			break;
		}

	}

	/**
	 * 设置文本框内条码数据，根据条码种类给不同文本框设置
	 * 
	 * @param categary
	 * @param codeString
	 */
	public void setString( String codeString) {
		et_code.setText(codeString);
	}

	/**
	 * 请求网络，查询订单详情,扫码结束和手动输入确认后调用
	 * 
	 * @param code
	 * @return
	 */
	public void checkOrder(String code) {
		boolean flag = false;
		if (code.length() > 5) {
			loading.setVisibility(View.VISIBLE);
			MyHttpUtils myHttpUtils = new MyHttpUtils();
			myHttpUtils.callBack = new RequestCallBack<String>() {
				
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					// TODO Auto-generated method stub
					// 获取服务器响应的数据
					loading.setVisibility(View.GONE);
					String result = responseInfo.result;
				
					System.out.println("响应码" + result);
					// Toast.makeText(mActivity, "响应"+result,
					// Toast.LENGTH_SHORT).show();
					if(result.contains("success")&&result.contains("ORDERNO")){
						tv_tranceNum.setText(tranceBoxCode);
					processOrderData(result);
					}else{
						Toast.makeText(getApplicationContext(), "此周转箱未绑定订单", Toast.LENGTH_SHORT).show();
						tranceBoxCode = null;
						et_code.setText("");
					}
				}
				
				@Override
				public void onFailure(com.lidroid.xutils.exception.HttpException error,
						String msg) {
					error.printStackTrace();
					loading.setVisibility(View.GONE);
					et_code.setText("");
					tranceBoxCode = null;
					
				}
			};
			myHttpUtils.Get(ServerUrl.GETORDERSERVER+code);
			
			System.out.println("参数" + code);
			
		} else {
			Toast.makeText(SortingAct.this, "请输入正确条形码", Toast.LENGTH_SHORT)
					.show();
			tranceBoxCode = null;
			et_code.setText("");
		}

	}

	/**
	 * 弹出对话框，显示订单详情
	 */
	protected void showDialog() {
		// WindowManager wm = mActivity.getWindowManager();
		// Display display = wm.getDefaultDisplay();
		// LayoutParams params = mActivity.getWindow().getAttributes();
		// params.height = (int) (display.getHeight() * 0.8);
		// params.width = (int) (display.getWidth() * 0.9);
		AlertDialog alertDialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(SortingAct.this);
		builder.setTitle("订单详情");
		View dialogView = View.inflate(SortingAct.this, R.layout.dialog_detal,
				null);
		TextView tv_orderNum = (TextView) dialogView
				.findViewById(R.id.tv_orderNum);
		TextView tv_orderCount = (TextView) dialogView
				.findViewById(R.id.tv_orderCount);
		TextView tv_actuallyCount = (TextView) dialogView
				.findViewById(R.id.tv_actuallyCount);
		//计算总数
		int orderCount = 0;
		int actuallyCount = 0;
		for (OrderInfo info : orderDetail.DATA) {
			orderCount+=info.QTY;
			actuallyCount+=info.PQTY;
		}
		tv_orderNum.setText(orderDetail.DATA.get(0).ORDERNO + "");
		tv_orderCount.setText(orderCount + "");
		tv_actuallyCount.setText(actuallyCount + "");
		builder.setView(dialogView);
		builder.setPositiveButton("正确", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				tranceBoxCode = et_code.getText().toString();
				comitData(UNBINDTRANCEBOX);
				
				
				//锁定中转箱输入框，解锁发货箱输入框
				et_code.setFocusable(false);
				
				tv_orderNo.setText(orderDetail.DATA.get(0).ORDERNO);
				
			}
		});
		builder.setNegativeButton("有误", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 先清空输入框和中转箱信息，再提交问题订单
				et_code.setText("");
//				tranceBoxCode = null;
				comitData(COMITERRORBOX);
				
			}
		});

		builder.setCancelable(false);
		alertDialog = builder.create();

		builder.show();
		System.out.println("对话框成功");
	
		// alertDialog.setContentView(dialogView, params);
		// alertDialog.show();
		getSharedPreferences("config", MODE_PRIVATE);
	}

	/**
	 * 解析订单详情
	 */
	private void processOrderData(String result) {
		// 解析json数据封装对象
		if (result.contains("success")) {
			Gson gson = new Gson();
			orderDetail = gson.fromJson(result, OrderDetail.class);
			int qty = 0;
			int pqty = 0;
			for(int x=0;x<orderDetail.DATA.size();x++){
				qty +=orderDetail.DATA.get(x).QTY;
				pqty += orderDetail.DATA.get(x).PQTY;
			}
			tv_orderItemNum.setText(qty+"");
			tv_SortNum.setText(pqty+"");
			tv_difference.setText((pqty-qty)+"");
			tv_orderNo.setText(orderDetail.DATA.get(0).ORDERNO);
			tv_sendPort.setText(orderDetail.DATA.get(0).EXLOCATION);
			if(compair()){
			System.out.println("json数据" + orderDetail);

			//handler.sendEmptyMessage(3);
			}else{
				showComitDialog();
			}
		} else {
			Toast.makeText(getApplicationContext(), "请检查条形码",
					Toast.LENGTH_SHORT).show();
		}

	}

	// 提交绑定的周转箱以及操作员的编号,以及发货箱
	public void comitData(int categary) {
		// 获取操作员工编号
		SharedPreferences sp = getSharedPreferences("config",
				SortingAct.this.MODE_PRIVATE);
		String userName = sp.getString("user", null);
		switch (categary) {

		case COMITSENDINGINFO:
			// 提交发货信息
			String[] boxes = new String[sendBoxes.size()];
			for (int x = 0; x < boxes.length; x++) {
				boxes[x] = sendBoxes.get(x);
			}
//			String boxStrings = arrayToString(boxes);
//			System.out.println("数组" + boxStrings);
			
			
			

			String url = ServerUrl.BINDSENDBOX;
				;
			
//			try {
//				url += URLEncoder.encode(boxStrings, "UTF-8");
//			} catch (UnsupportedEncodingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			RequestParams params = new RequestParams();
			params.setHeader("Content-Type","application/json");
//			params.setHeader("Content-Length","105");
			String json = sendBoxJson(boxes);
			System.out.println("json数据"+json);
			try {
				if(json!=null){
				params.setBodyEntity(new StringEntity(json, "UTF-8"));
				}
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("链接"+url);
		
			
			
			MyHttpUtils myHttpUtils = new MyHttpUtils();
			myHttpUtils.callBack = new RequestCallBack<String>() {
				
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					// TODO Auto-generated method stub
					sr_sendBoxInfo.comiting = false;
					sr_sendBoxInfo.setRefreshing(false);
					System.out.println("发货信息回馈"+responseInfo.result);
					if(responseInfo.result.contains("success")){
						
						Toast.makeText(getApplicationContext(), "提交成功", Toast.LENGTH_SHORT).show();
						handler.sendEmptyMessage(4);
					}else{
						
						showMyToast("未知异常！", "提交发货失败！");
						//删除发货箱
						if(sendBoxes.size()>0){
						sendBoxes.clear();
						myAdapter.notifyDataSetChanged();
						}
					}

				}
				
			

				@Override
				public void onFailure(
						com.lidroid.xutils.exception.HttpException error,
						String msg) {
					sr_sendBoxInfo.comiting = false;
					sr_sendBoxInfo.setRefreshing(false);
					System.out.println("连接错误");
					error.printStackTrace();
					if(sendBoxes.size()>0){
						sendBoxes.remove(sendBoxes.size()-1);
						myAdapter.notifyDataSetChanged();
						}
					
				}
			};
			myHttpUtils.POST(url, params);
		
			break;
		case COMITERRORBOX:
			// 提交错误订单信息
			RequestParams params2 = new RequestParams();
			params2.setHeader("Content-Type","application/json");
			try {
				params2.setBodyEntity(new StringEntity(RecheckJson(), "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			MyHttpUtils myHttpUtils2 = new MyHttpUtils();
			myHttpUtils2.callBack = new RequestCallBack<String>() {

				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					// TODO Auto-generated method stub
					//打印返回信息
					System.out.println("错误提交"+responseInfo.result);
					tranceBoxCode = null;
					if(responseInfo.result.contains("success")){
						tv_orderNo.setText("");
						tv_orderItemNum.setText("");
						tv_SortNum.setText("");
						tv_tranceNum.setText("");
						tv_sendPort.setText("");
						Toast.makeText(getApplicationContext(), "提交错误订单成功", 0).show();
					}

				
				}

				

				@Override
				public void onFailure(
						com.lidroid.xutils.exception.HttpException error,
						String msg) {
					showMyToast("请检查网络!", "服务器无响应！");
					
				}
			};
			
			
			myHttpUtils2.POST(ServerUrl.COMITERRORORDER, params2);

			break;
		default:
			break;
		}

	}

	/**
	 * 检查发货箱是否可用
	 * 
	 * @throws MalformedURLException
	 */
	public void checkSendBox() {

		HttpUtils http = new HttpUtils();
		loading.setVisibility(View.VISIBLE);
		
		http.send(com.lidroid.xutils.http.client.HttpRequest.HttpMethod.GET,
				
				ServerUrl.SENDBOXSERVER
						+ sendBoxCode ,
				new RequestCallBack<String>() {

				

					@Override
					public void onSuccess(ResponseInfo<String> res) {
						// TODO Auto-generated method stub
						System.out.println("解绑发货箱" + res.result);
					
						processBindInfo(res.result);

						loading.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onFailure(
							com.lidroid.xutils.exception.HttpException error,
							String msg) {
						// TODO Auto-generated method stub
						showMyToast("请检查网络！", "服务器无响应！");
						loading.setVisibility(View.INVISIBLE);
					}
				});

		http = null;

	}

	/**
	 * 解析解绑信息
	 * 
	 * @param json
	 */
	private void processBindInfo(String json) {

		Gson gson = new Gson();
		DeBindInfo deBindInfo = gson.fromJson(json, DeBindInfo.class);
		// 解绑成功
		System.out.println("result" + deBindInfo.MESSAGE+deBindInfo.RESULT);
		if ("success".equals(deBindInfo.RESULT)&&"获取订单用户信息成功".equals(deBindInfo.MESSAGE)) {
//			// list集合添加一个箱号
//			if(sendBoxCode.length()>5){
//			sendBoxes.add(sendBoxCode);
//			}
//			if(sendBoxes.size()==1){
//				myAdapter = new MyAdapter();
//				lv_sendBoxes.setAdapter(myAdapter);
//			}
//			if(myAdapter!=null){
//			myAdapter.notifyDataSetChanged();
//			}
//			moreDialog();
			Toast.makeText(getApplicationContext(), "发货箱未解绑，请更换发货箱", Toast.LENGTH_SHORT)
			.show();

		} else {
			// list集合添加一个箱号
						if(sendBoxCode.length()>5){
						sendBoxes.add(sendBoxCode);
						}
						if(sendBoxes.size()==1){
							myAdapter = new MyAdapter();
							lv_sendBoxes.setAdapter(myAdapter);
						}
						if(myAdapter!=null){
						myAdapter.notifyDataSetChanged();
						}
//						moreDialog();
			// 解绑失败
			
			// 发货箱信息清空
			// et_sendBoxcode.setText("");
			// sendBoxCode = null;
		}
	}

	/**
	 * 弹出对话框，确定要不要增加一个发货箱
	 */
	private void moreDialog() {
		
		
		
			builder1= new AlertDialog.Builder(SortingAct.this);
			builder1.setTitle("添加发货箱");
			builder1.setPositiveButton("继续", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 让当前箱号置空
				sendBoxCode = null;
				et_code.setText("");
				Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
				sendBroadcast(intent);

			}
		});
			builder1.setNegativeButton("完成", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// 提交数据
				comitData(COMITSENDINGINFO);
			
				
			}
		});
			builder1.setCancelable(false);
			builder1.create();
			builder1.show();
		
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if (resultCode == RESULT_OK) {
//			Bundle bundle1 = data.getExtras();
//			String scanResult = bundle1.getString("result");
//			System.out.println(scanResult + "长度" + scanResult.length());
//			if (requestCode == 0) {
//
//				setString(requestCode, scanResult);
//
//				checkOrder(scanResult);
//				
//				step1++;
//
//				System.out.println("新step1=" + step1);
//				System.out.println("中转箱" + tranceBoxCode);
//			} else if (requestCode == 1) {
//
//				// 检查发货箱
//				setString(requestCode, scanResult);
//				checkSendBox();
//				step2++;
//			}
//
//		} else if (resultCode == RESULT_CANCELED) {
//			step1 = 0;
//			step2 = 0;
//		}
//	}

	/**
	 * 注销传感器监听
	 */
	@Override
	protected void onDestroy() {
		
		unregisterReceiver(receiver);
		super.onDestroy();
	}


	
	
	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return sendBoxes.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(getApplicationContext(), R.layout.item_sendbox, null);
			TextView tv_sendBoxNo = (TextView) view.findViewById(R.id.tv_sendBoxNo);
			tv_sendBoxNo.setText(sendBoxes.get(position));
			return view;
		}
		
	}
	
	
	private String sendBoxJson(String[] boxArray){
		JSONObject jsonObject = new JSONObject();
		try {
		jsonObject.put("DETAILNO", orderDetail.DATA.get(0).DETAILNO);
			jsonObject.put("ORDERNO", orderDetail.DATA.get(0).ORDERNO);
//			jsonObject.put("BOXNO", "Box111212");
			jsonObject.put("EXCEPTYPE", 1);
			jsonObject.put("USERID", userID);
			jsonObject.put("USERNAME", userName);
			jsonObject.put("CHECK_LOCATION", checkLocation);
			JSONArray jsonArray = new JSONArray();
			for(int x=0;x<boxArray.length;x++){
			jsonArray.put(x,boxArray[x] );
			}
			jsonObject.put("FBOXS", jsonArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return jsonObject.toString();
	}
	
	/**
	 * 错误信息转json
	 * @return
	 */
	private String RecheckJson(){
		
		JSONObject jsonObject = new JSONObject();
		try {
		jsonObject.put("DETAILNO", orderDetail.DATA.get(0).DETAILNO);
			jsonObject.put("ORDERNO", orderDetail.DATA.get(0).ORDERNO);
			
			LogUtil.i("错误周转箱", tranceBoxCode);
			jsonObject.put("BOXNO", tranceBoxCode);
			jsonObject.put("EXCEPTYPE", 3);
			jsonObject.put("CHECK_LOCATION", checkLocation);
			jsonObject.put("USERNAME", userName);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * 判断扫描条码是否为周转箱
	 * @param code
	 * @return
	 */
	private boolean isTranceBox(String code){
		
		System.out.println(code);
		Pattern pattern = Pattern.compile("Z[0-9]{6}");
		Matcher matcher = pattern.matcher(code);
		
		return matcher.matches();
		
	}	
	
	
	private boolean isSendBox(String code){
		
		Pattern pattern1 = Pattern.compile("^B[0-9]{6}");
		Pattern pattern2 = Pattern.compile("^L[0-9]{6}");
		Pattern pattern3 = Pattern.compile("^J[0-9]{6}");
		Pattern pattern4 = Pattern.compile("^X[0-9]{6}");
		Matcher matcher1 = pattern1.matcher(code);
		Matcher matcher2 = pattern2.matcher(code);
		Matcher matcher3 = pattern3.matcher(code);
		Matcher matcher4 = pattern4.matcher(code);
		return matcher1.matches()||matcher2.matches()||matcher3.matches()||matcher4.matches();
	}
	/**
	 * 比对订单数和实捡数,返回true则订单正确，false则有误
	 */
	private boolean compair(){
		boolean flag = true;
		//遍历订单详情
		for(OrderInfo info:orderDetail.DATA){
			if(info.PQTY!=info.QTY){
				flag = false;
			}
		}
		return flag;
		
	}
	
	/**
	 * 弹出对话框，选择是否提交错误订单
	 */
	private void showComitDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("订单异常");
		builder.setMessage("当前订单实捡数异常，提交错误吗？");
		builder.setPositiveButton("提交", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				comitData(COMITERRORBOX);
			}
		});
		builder.setNegativeButton("取消", null);
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}
	private void handleCode(String code){
		setString(code);
		
		if(isTranceBox(code)){
			//扫描下一个周转箱就提交发货
			if(sendBoxes.size()>0&&tranceBoxCode!=code){
				comitData(COMITSENDINGINFO);
			}
			tranceBoxCode = code;
				
				checkOrder(code);
			
			
		}else {
		if(tranceBoxCode!=null){
			if(isSendBox(code)){
				boolean flag = true;
				for(String boxCode:sendBoxes){
					if(boxCode.equals(code)){
						flag = false;
						Toast.makeText(getApplicationContext(), "请勿重复扫描", 0).show();
					}
				}
			sendBoxCode = code;
			if(flag){
			checkSendBox();
			}
			}else{
				Toast.makeText(getApplicationContext(), "条码有误", Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(getApplicationContext(), "请先扫描周转箱", Toast.LENGTH_SHORT).show();
		}
		}
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
		handler.sendEmptyMessage(6);
		
	}
	@Override
	public void cancleEquit() {
		
		start = false;
	}
	/**
	 * 选择复核位
	 */
	private void showChooseSortingPositionDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = View.inflate(this, R.layout.sortingplace_dialog_layout, null);
		et_sortingPlace = (EditText) view.findViewById(R.id.et_sortingPlace);
		btn_confirm_checklocation = (Button) view.findViewById(R.id.btn_confirm_checklocation);
		btn_cancle_checklocation = (Button) view.findViewById(R.id.btn_cancle_checklocation);
		btn_confirm_checklocation.setOnClickListener(this);
		btn_cancle_checklocation.setOnClickListener(this);
		builder.setCancelable(false);
		checkLoacationDialog = builder.create();
		checkLoacationDialog.setView(view, 0, 0, 0, 0);
		checkLoacationDialog.show();
	}

	/**
	 * 下拉刷新方法087
	 */
	@Override
	public void onRefresh() {
		sr_sendBoxInfo.comiting = true;
		//先判断发货箱集合是否为空
		
		if(sendBoxes!=null){
			if(sendBoxes.size()>0){
				//提交发货
				comitData(COMITSENDINGINFO);
			}else{
				Toast.makeText(getApplicationContext(), "请扫描发货箱", 0).show();
				sr_sendBoxInfo.setRefreshing(false);
				sr_sendBoxInfo.comiting = false;
			}
		}else{
			Toast.makeText(getApplicationContext(), "请扫描发货箱", 0).show();
			sr_sendBoxInfo.setRefreshing(false);
			sr_sendBoxInfo.comiting = false;
			
		}
		
		
	}
	/**
	 * 自定义Toast
	 * @param title
	 * @param content
	 */
	private void showMyToast(String title,String content){
		LayoutInflater inflater = getLayoutInflater();
		   View layout = inflater.inflate(R.layout.mytoast,
		     (ViewGroup) findViewById(R.id.llToast));
		   ImageView image = (ImageView) layout
		     .findViewById(R.id.tvImageToast);
		   image.setImageResource(R.drawable.warn);
		   TextView tv_title = (TextView) layout.findViewById(R.id.tvTitleToast);
		   tv_title.setText(title);
		   TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
		   text.setText(content);
		   Toast toast = new Toast(getApplicationContext());
		   toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 12, 40);
		   toast.setDuration(Toast.LENGTH_SHORT);
		   toast.setView(layout);
		   toast.show();
	}
}
