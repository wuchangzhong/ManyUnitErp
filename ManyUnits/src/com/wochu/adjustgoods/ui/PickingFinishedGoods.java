package com.wochu.adjustgoods.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.GoodsBean;
import com.wochu.adjustgoods.bean.OrderDetail_picking;
import com.wochu.adjustgoods.bean.OrderDetail_weijianqu;
import com.wochu.adjustgoods.bean.Pickfinished_tijiao_bean;
import com.wochu.adjustgoods.net.ServerUrl;
import com.wochu.adjustgoods.ui.adapter.Pickfinishedgoods_jianhuoqu_adapter;
import com.wochu.adjustgoods.utils.JsonUtil;
import com.wochu.adjustgoods.utils.LogUtil;
import com.wochu.adjustgoods.view.XCFlowLayout;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class PickingFinishedGoods extends Activity implements OnClickListener{
	private String lastCode="";//记录上次订单号
	private Context pickingFinishedGoods;
	private Button btn_trance_orderNumber;//扫描订单号
	private Button btn_confirm;
	private EditText et_code;
	private LinearLayout jianhuoqu_linerlayout;
	private LinearLayout weijianqu_linearlayout;
	private ImageView jianhuoqu;
	private ImageView weijianqu;
	private TextView jianhuoqu_textview;
	private TextView weijianqu_textview;
	private SharedPreferences sp;
	private String userID;
	private String userCode;
	private String jsonResult;
	private ArrayList<GoodsBean> OrderList;//拣货区订单中所有商品的集合
	private ListView pickfinishedgoods_jianhuoqu;
	private ExpandableListView pickfinishedgoods_weijianqu;
	private Pickfinishedgoods_jianhuoqu_adapter adapter;
	private SharedPreferences sp2;
	private TextView show_etcode;
	private String scanResult;
	private HttpUtils http;
	private LinearLayout shangpingmiaoshu;
	private boolean falg=true;//判断此次扫描的是订单号还是商品的条形码
					//未拣区的适配器
	private List<OrderDetail_weijianqu.DATA> weijiaqu_list;//未拣区数据集合
	private pickfinishedgoods_weijianqu adapter2;//未拣区适配器
	private Button baochun;
	private Button tijiao;
	private ArrayList<String>papMoXiang;//泡沫箱号的集合
	private String paoMoXiang_code;
	private boolean flag1=true;//判断商品又没有扫完
	private BroadcastReceiver receiver;//创建广播接收用于接收扫码之后的结果
	private TextView tv_paomocode;
	private XCFlowLayout mFlowLayout;
	private MarginLayoutParams lp;
	private Button bt_jiechubangding2;
	private LinearLayout show;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pickfinishedgoods_layout);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
		pickingFinishedGoods = this;
		OrderList = new ArrayList<GoodsBean>();
		weijiaqu_list = new ArrayList<OrderDetail_weijianqu.DATA>();//未拣区数据集合
		sp = getSharedPreferences("config", MODE_PRIVATE);
		userID = String.valueOf(sp.getInt("userID",-1));//获取用户的userID
		userCode = sp.getString("user","");//获取用户的工号
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals("com.android.scanservice.scancontext")){
					String tranceBoxCode = intent.getStringExtra("Scan_context");//获得扫码值
					if(!TextUtils.isEmpty(tranceBoxCode)){
							scanResult = tranceBoxCode;//得到订单的编码A2015103111508
							System.out.println(scanResult+"长度"+scanResult.length());
							show_etcode.setText("订单号:"+scanResult);
							Log.e("WeightingAct",scanResult);
							//取码值得第一位
							String result1=scanResult.substring(0,1);
							//判断是否为泡沫箱码
							if(scanResult.length()==7&&(result1.equals("B")||result1.equals("X")
									||result1.equals("J")||result1.equals("L"))){
								paoMoXiang_code = scanResult;
								papMoXiang.add(paoMoXiang_code);//扫描的是泡沫箱
								Gson gson = new Gson();
								String json = gson.toJson(papMoXiang);
								sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
								//保存泡沫箱信息
								Editor edit = sp2.edit();
								edit.putString("paomoxiang",json);
								edit.commit();
								Toast.makeText(getBaseContext(),"泡沫箱绑定成功",0).show();
								mFlowLayout.removeAllViews();
								for (int i = 0; i < papMoXiang.size(); i++) {
									 TextView view = new TextView(context);  
							            view.setText(papMoXiang.get(i));  
							            view.setTextColor(Color.BLACK); 
							            view.setTextSize(20);
							            mFlowLayout.addView(view,lp);  
								}
								et_code.setText("");
								return;//方法结束
							}
							//取码值得前三位
							String result = scanResult.substring(0,3);
							LogUtil.e("result",result);
							if(result.equals("WOC")||scanResult.length()==13){//此次扫描的是商品的条形码A2015103057636
								falg = dealWithBarCode(scanResult);
								if(falg){
									return;//方法结束,说明此次扫描的是商品的条形码
								}
							}
							papMoXiang.clear();//清空集合
							getOrderDetails(scanResult);
							show_etcode.setText("");
						}
					}
				}
		};
		IntentFilter scanDataIntentFilter = new IntentFilter();
		//scanDataIntentFilter.addAction("com.android.scancontext"); //前台输出
		scanDataIntentFilter.addAction("com.android.scanservice.scancontext"); //后台输出
		registerReceiver(receiver, scanDataIntentFilter);//注册广播接收者
		initView();
	}
			private void showPickDialog(final List<GoodsBean> jianhuoqu_list1){
				final Dialog dialog = new Dialog(PickingFinishedGoods.this,R.style.pick_dialog);
				dialog.setContentView(R.layout.pickinggood_dialog_tijiao);
				Window dialogWindow  = dialog.getWindow();
				WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			    dialogWindow.setGravity(Gravity.CENTER);
			    WindowManager m = getWindowManager();
			    Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
			    WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
			    p.alpha=1.0f;
			    p.height = (int) (d.getHeight() * 0.25); // 高度设置为屏幕的0.6
			    p.width = (int) (d.getWidth() * 0.5); // 宽度设置为屏幕的0.65
			    dialogWindow.setAttributes(p);
			    Button btn_ok=(Button) dialog.findViewById(R.id.btn_ok);
			    Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
			    final TextView numberEditText =  (TextView) dialog.findViewById(R.id.et_pwd);
			    numberEditText.setText("提示：商品没有拣完,确认提交吗？");
			    numberEditText.setTextColor(Color.RED);
			    btn_ok.setOnClickListener(new OnClickListener() {//确定按钮
					
					@Override
					public void onClick(View v) {
						 	sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
							String response = sp.getString("PickingFinishedGoods","");
							String code = show_etcode.getText().toString();
							String code1 = code.substring(4);
							Pickfinished_tijiao_bean tijiao_bean = new Pickfinished_tijiao_bean();
							tijiao_bean.USERID=Integer.parseInt(userID);
							tijiao_bean.FBOXS=papMoXiang;
							tijiao_bean.WCS_TO_WMSs=jianhuoqu_list1;
							tijiao_bean.ORDERNO=code1;
						    Gson gson = new Gson();
						    String json2 = gson.toJson(tijiao_bean);
						    LogUtil.e("123",json2);
							if(http==null){
								 http = new HttpUtils(2000);
								 http.configCurrentHttpCacheExpiry(0);
								}
							RequestParams params1 = new RequestParams();
							params1.addHeader("Content-Type", "application/json");
							try {
								params1.setBodyEntity(new StringEntity(json2));
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							http.send(HttpMethod.POST,ServerUrl.TIJIAO,params1,new RequestCallBack<String>() {
								@Override
								public void onFailure(HttpException arg0, String arg1) {
									// TODO Auto-generated method stub
									Toast.makeText(getBaseContext(),"网络有问题,提交失败",0).show();
								}
								@Override
								public void onSuccess(ResponseInfo<String> arg0) {
									// TODO Auto-generated method stub
									LogUtil.e("resultl",arg0.result);
									papMoXiang.clear();
									JSONObject json = null;
									try {
										json = new JSONObject(arg0.result);
									} catch (JSONException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										if(json.getString("RESULT").equals("success")){
											Toast.makeText(getBaseContext(),json.getString("MESSAGE"),0).show();
											ArrayList<GoodsBean> qingkong_list =new ArrayList<GoodsBean>();
											adapter.list = qingkong_list;
											adapter.notifyDataSetChanged();
											Gson gson = new Gson();
											jsonResult = gson.toJson(qingkong_list);
											scanResult = "";
											//保存
											sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
											//保存商品信息
											Editor edit = sp2.edit();
											edit.putString("PickingFinishedGoods",jsonResult);
											edit.commit();
											//保存订单号
											Editor edit1 = sp2.edit();
											edit1.putString("etcode",scanResult);
											edit1.commit();
											show_etcode.setText("订单号：");
											flag1 =true;
										}else{
											flag1 =true;
											Toast.makeText(getBaseContext(),json.getString("MESSAGE"),0).show();
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						
						dialog.dismiss();
					}
				});
			    btn_cancel.setOnClickListener(new OnClickListener() {//取消按钮
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
			    dialog.show();
			}
			
			/**
			 * 弹出对话框
			 */
			private void showPickdialog(final View view,final int position) {
				final Dialog dialog = new Dialog(PickingFinishedGoods.this,R.style.pick_dialog);
				dialog.setContentView(R.layout.pickinggood_dialog_view);
				Window dialogWindow  = dialog.getWindow();
				WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			    dialogWindow.setGravity(Gravity.CENTER);
			    WindowManager m = getWindowManager();
			    Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
			    WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
			    p.alpha=1.0f;
			    p.height = (int) (d.getHeight() * 0.25); // 高度设置为屏幕的0.6
			    p.width = (int) (d.getWidth() * 0.5); // 宽度设置为屏幕的0.65
			    dialogWindow.setAttributes(p);
			    Button btn_ok=(Button) dialog.findViewById(R.id.btn_ok);
			    Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
			    final EditText numberEditText = (EditText) dialog.findViewById(R.id.et_pwd);
			    btn_ok.setOnClickListener(new OnClickListener() {//确定按钮
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub A2015103072004
						
						TextView shijianshu = (TextView) view.findViewById(R.id.shijianshu);
						TextView dingdanshu = (TextView) view.findViewById(R.id.dingdanshu);
						
						String number = numberEditText.getText().toString().trim();
						if(TextUtils.isEmpty(number)){
							Toast.makeText(getBaseContext(),"输入内容不能为空",0).show();
							return;
						}
						shijianshu.setText(number);
						int dingDanNumber = Integer.parseInt(dingdanshu.getText().toString());
						if(dingDanNumber!=Integer.parseInt(number)){
							shijianshu.setTextColor(Color.RED);
						}else{
							shijianshu.setTextColor(Color.BLACK);
						}
						List<GoodsBean> orderDetail = (List<GoodsBean>) JsonUtil.parseJsonToList(jsonResult, new TypeToken<List<GoodsBean>>(){}.getType());
						orderDetail.get(position).PQTY=Integer.parseInt(number);
						adapter.list=(ArrayList<GoodsBean>) orderDetail;
						adapter.notifyDataSetChanged();
						Gson gson = new Gson();
						jsonResult=gson.toJson(orderDetail);
						sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
						//保存商品信息
						Editor edit = sp2.edit();
						edit.putString("PickingFinishedGoods",jsonResult);
						edit.commit();
						dialog.dismiss();
					}
				});
			    btn_cancel.setOnClickListener(new OnClickListener() {//取消按钮
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
			    dialog.show();
			}
	/**
	 * 初始化界面
	 */
	private void initView() {
		show = (LinearLayout) findViewById(R.id.show);
		papMoXiang = new ArrayList<String>();//泡沫箱集合
		tijiao = (Button) findViewById(R.id.tijiao);
		baochun = (Button) findViewById(R.id.baochun);//保存
		tijiao.setOnClickListener(this);
		baochun.setOnClickListener(this);
		tv_paomocode = (TextView) findViewById(R.id.tv_paomocode);//显示泡沫箱号
		mFlowLayout = (XCFlowLayout) findViewById(R.id.flowlayout);//流式布局
		 lp = new MarginLayoutParams(  
	                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);  
	        lp.leftMargin = 5;  
	        lp.rightMargin = 5;  
	        lp.topMargin = 5;  
	        lp.bottomMargin = 5;  
		shangpingmiaoshu = (LinearLayout) findViewById(R.id.shangpingmiaoshu);
		show_etcode = (TextView) findViewById(R.id.show_etcode);//显示订单号
		pickfinishedgoods_jianhuoqu = (ListView) findViewById(R.id.pickfinishedgoods_jianhuoqu);//显示拣货区界面
		//拣货区的适配器
		adapter = new Pickfinishedgoods_jianhuoqu_adapter(PickingFinishedGoods.this,OrderList);
		pickfinishedgoods_jianhuoqu.setAdapter(adapter);//设置显示拣货区的adapter
		//给listview设置条目点击事件
		pickfinishedgoods_jianhuoqu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {//自定义弹出对话框输入
				showPickdialog(view,position);
			}
		});
		//未拣区
		pickfinishedgoods_weijianqu = (ExpandableListView) findViewById(R.id.pickfinishedgoods_weijianqu);//显示未拣区界面
		adapter2 = new pickfinishedgoods_weijianqu();
		pickfinishedgoods_weijianqu.setGroupIndicator(this.getResources().getDrawable(R.drawable.expandablelistviewselector));
		
		pickfinishedgoods_weijianqu.setAdapter(adapter2);
		btn_trance_orderNumber = (Button) findViewById(R.id.btn_trance_orderNumber);
		et_code = (EditText) findViewById(R.id.et_code);//手动输入订单码
//		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
//        imm.hideSoftInputFromWindow(et_code.getWindowToken(),0);
		et_code.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(s.length()>6){
					btn_confirm.setEnabled(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		jianhuoqu_linerlayout = (LinearLayout) findViewById(R.id.jianhuoqu_linerlayout);
		weijianqu_linearlayout = (LinearLayout) findViewById(R.id.weijianqu_linearlayout);
		jianhuoqu_textview = (TextView) findViewById(R.id.jianhuoqu_textview);
		weijianqu_textview = (TextView) findViewById(R.id.weijianqu_textview);
		jianhuoqu = (ImageView) findViewById(R.id.jianhuoqu);
		weijianqu = (ImageView) findViewById(R.id.weijianqu);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		bt_jiechubangding2 = (Button) findViewById(R.id.bt_jiechubangding);
		btn_confirm.setEnabled(false);
		btn_trance_orderNumber.setOnClickListener(this);
		btn_confirm.setOnClickListener(this);
		jianhuoqu_linerlayout.setOnClickListener(this);
		weijianqu_linearlayout.setOnClickListener(this);
		bt_jiechubangding2.setOnClickListener(this);
		jianhuoqu_textview.setTextColor(0xff006600);
	}
	
	@Override
	protected void onStart() {//数据回显
		// TODO Auto-generated method stub
		super.onStart();
		 SharedPreferences sp = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
		 String response = sp.getString("PickingFinishedGoods","");
		 String Resultcode = sp.getString("etcode","");
		 LogUtil.e("RESULT", response);
		 if(!response.equals("")){
			 @SuppressWarnings("unchecked")
			List<GoodsBean>  jianhuoqu_list= (List<GoodsBean>)JsonUtil.parseJsonToList(response,new TypeToken<List<GoodsBean>>(){}.getType());
				if(jianhuoqu_list.size()!=0){//userCode I2015110255103
					//A2015022800001       I2015110394123
					OrderList = (ArrayList<GoodsBean>) jianhuoqu_list;
					adapter.list = OrderList;
					jsonResult=response;
					LogUtil.e("hehe",OrderList.toString());
					adapter.notifyDataSetChanged();//刷新适配器
				}
			}
		 if(!Resultcode.equals("")){
			scanResult = Resultcode;
			show_etcode.setText("订单号:"+Resultcode);
		 }
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		 SharedPreferences sp = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
		 String response = sp.getString("PickingFinishedGoods","");
		 @SuppressWarnings("unused")
		String Resultcode = sp.getString("etcode","");
		 if(!response.equals("")){
			 @SuppressWarnings("unchecked")
			List<GoodsBean>  jianhuoqu_list= (List<GoodsBean>) JsonUtil.parseJsonToList(response,new TypeToken<List<GoodsBean>>(){}.getType());
				if(jianhuoqu_list.size()!=0){//userCode I2015110255103
					//A2015022800001       I2015110394123
					OrderList = (ArrayList<GoodsBean>) jianhuoqu_list;
					adapter.list = OrderList;
					jsonResult=response;
					LogUtil.e("hehe",OrderList.toString());
					adapter.notifyDataSetChanged();//刷新适配器
				}
			}
		 }
	@Override
	protected void onDestroy() {//此界面销毁时保存数据 I2015110312340
		// TODO Auto-generated method stub
		super.onDestroy();
		sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
			//保存商品信息
			Editor edit = sp2.edit();
			edit.putString("PickingFinishedGoods",jsonResult);
			edit.commit();
			//保存订单号
			if(!falg){//判断只有扫描的是订单号时才进行保存
			Editor edit1 = sp2.edit();
			edit1.putString("etcode",scanResult);
			edit1.commit();
			}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub  A2015102635596
		switch (v.getId()) {
		case R.id.bt_jiechubangding://解除绑定
			papMoXiang.clear();
			mFlowLayout.removeAllViews();
			break;
		case R.id.jianhuoqu_linerlayout:
			show.setVisibility(View.VISIBLE);
			bt_jiechubangding2.setVisibility(View.VISIBLE);
			tv_paomocode.setVisibility(View.VISIBLE);
			mFlowLayout.setVisibility(View.VISIBLE);
			show_etcode.setVisibility(View.VISIBLE);
			shangpingmiaoshu.setVisibility(View.VISIBLE);
			pickfinishedgoods_jianhuoqu.setVisibility(View.VISIBLE);//显示拣货区
			pickfinishedgoods_weijianqu.setVisibility(View.INVISIBLE);//隐藏未拣区  I2015103078902
			jianhuoqu.setVisibility(View.VISIBLE);
			weijianqu.setVisibility(View.INVISIBLE);
			jianhuoqu_textview.setTextColor(0xff006600);
			weijianqu_textview.setTextColor(0xff000000);
			sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
			String json = sp2.getString("PickingFinishedGoods","");
			LogUtil.e("json",json+1);
			if(json.equals("")){//判断假如没有任何数据的话
				ArrayList<GoodsBean> list = new ArrayList<GoodsBean>();
				json=new Gson().toJson(list);
			}
			@SuppressWarnings("unchecked")
			List<GoodsBean>  jianhuoqu_list= (List<GoodsBean>)JsonUtil.parseJsonToList(json,new TypeToken<List<GoodsBean>>(){}.getType());
			if(jianhuoqu_list.size()!=0){//userCode I2015110255103
				//A2015022800001       I2015110394123
				OrderList = (ArrayList<GoodsBean>) jianhuoqu_list;
				adapter.list = OrderList;
				adapter.notifyDataSetChanged();//刷新适配器
			}
			break;
		case R.id.weijianqu_linearlayout:
			show.setVisibility(View.GONE);
			bt_jiechubangding2.setVisibility(View.GONE);
			show_etcode.setVisibility(View.GONE);
			shangpingmiaoshu.setVisibility(View.GONE);
			tv_paomocode.setVisibility(View.GONE);
			mFlowLayout.setVisibility(View.GONE);
			pickfinishedgoods_jianhuoqu.setVisibility(View.INVISIBLE);//显示未拣区
			pickfinishedgoods_weijianqu.setVisibility(View.VISIBLE);//隐藏拣货区
			//访问网络
			if(http==null){
				http = new HttpUtils(2000);
				http.configCurrentHttpCacheExpiry(0);
			}
			long time1 = System.currentTimeMillis();
			String time = String.valueOf(time1);
			http.send(HttpMethod.GET,ServerUrl.WeiJianQu(userID, time), new RequestCallBack<String>() {
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// TODO Auto-generated method
					Toast.makeText(getBaseContext(),"网络有问题",0).show();
				}
				@Override
				public void onSuccess(ResponseInfo<String> response) {
					// TODO Auto-generated method stub
					String orderWeiJianQu = response.result;
					LogUtil.e("orderWeiJianQu",orderWeiJianQu);
					OrderDetail_weijianqu weiJianQuBean = JsonUtil.parseJsonToBean(orderWeiJianQu,OrderDetail_weijianqu.class);
					if(weiJianQuBean.RESULT.equals("success")){//返回数据成功
						weijiaqu_list = weiJianQuBean.DATA;
						adapter2.notifyDataSetChanged();
					}else{
						weijiaqu_list = new ArrayList<OrderDetail_weijianqu.DATA>();
						adapter2.notifyDataSetChanged();
					}
				}
			});
			jianhuoqu.setVisibility(View.INVISIBLE);
			weijianqu.setVisibility(View.VISIBLE);
			jianhuoqu_textview.setTextColor(0xff000000);
			weijianqu_textview.setTextColor(0xff006600);
			time=null;
			break;
		case R.id.btn_trance_orderNumber://扫码按钮
			
//			Intent intent = new Intent(this,CaptureActivity.class);
//			
//			startActivityForResult(intent,0);
			
			Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
			sendBroadcast(intent);//发送广播
			
			break;
		case R.id.btn_confirm:
			scanResult = et_code.getText().toString().trim();
			Log.e("WeightingAct",scanResult);
			if(TextUtils.isEmpty(scanResult)){
			Toast.makeText(getBaseContext(),"订单码不能为空",0).show();
			return;
		}
			//取码值得第一位
			String result1=scanResult.substring(0,1);
			//判断是否为泡沫箱码
			if(scanResult.length()==7&&(result1.equals("B")||result1.equals("X")
					||result1.equals("J")||result1.equals("L"))){
				paoMoXiang_code = scanResult;
				papMoXiang.add(paoMoXiang_code);//扫描的是泡沫箱
				Gson gson = new Gson();
				String json3 = gson.toJson(papMoXiang);
				sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
				//保存泡沫箱信息
				Editor edit = sp2.edit();
				edit.putString("paomoxiang",json3);
				edit.commit();
				Toast.makeText(getBaseContext(),"泡沫箱绑定成功",0).show();
				mFlowLayout.removeAllViews();
				for (int i = 0; i < papMoXiang.size(); i++) {
					 TextView view = new TextView(this);  
			            view.setText(papMoXiang.get(i));  
			            view.setTextColor(Color.BLACK); 
			            view.setTextSize(20);
			            mFlowLayout.addView(view,lp);  
				}
				et_code.setText("");
				return;//方法结束
			}
			//取码值得前三位
			String result = scanResult.substring(0,3);
			LogUtil.e("result",result);
			if(result.equals("WOC")||scanResult.length()==13){//此次扫描的是商品的条形码A2015103057636
				falg = dealWithBarCode(scanResult);
				if(falg){
					return;//方法结束,说明此次扫描的是商品的条形码
				}
			}
			papMoXiang.clear();//清空集合
			getOrderDetails(scanResult);
			et_code.setText("");
		break;
		case R.id.baochun://商品没有拣完,保存当前数据,保存到服务器
			if(http==null){
			 http = new HttpUtils(2000);
			 http.configCurrentHttpCacheExpiry(0);
			 }
			RequestParams params = new RequestParams();
			params.addHeader("Content-Type", "application/json");
			sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
			String saveData = sp2.getString("PickingFinishedGoods","");//获取需要保存的数据
			LogUtil.e("saveData",saveData+"1");
			try {
				params.setBodyEntity(new StringEntity(saveData));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
			http.send(HttpMethod.POST,ServerUrl.BAOCHUN, params, new RequestCallBack<String>() {
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(),"网络有问题",0).show();
				}
				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					// TODO Auto-generated method stub
					
					try {
						JSONObject json = new JSONObject(arg0.result);
						String  result = (String) json.get("RESULT");
						if(result.equals("success")){
							Toast.makeText(getBaseContext(),json.getString("MESSAGE"),0).show();
						}else{
							Toast.makeText(getBaseContext(),json.getString("MESSAGE"),0).show();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			});
			break;
		case R.id.tijiao://当商品拣完提交完整数据
			//先检查拣货是否完成
			List<GoodsBean> jianhuoqu_list1;
			sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
			String json1 = sp2.getString("PickingFinishedGoods","");
			if(json1.equals("")){
				jianhuoqu_list1 = new ArrayList<GoodsBean>();
			}else{
			jianhuoqu_list1= (List<GoodsBean>) JsonUtil.parseJsonToList(json1,new TypeToken<List<GoodsBean>>(){}.getType());
			}
			for (int i = 0; i < jianhuoqu_list1.size(); i++) {//遍历该集合
				if(jianhuoqu_list1.get(i).QTY!=jianhuoqu_list1.get(i).PQTY){//提示拣货员是否进一步提交拣验结果
					showPickDialog(jianhuoqu_list1);
					flag1 = false;
					break;
				}else{
					flag1 =true;
				}
				
			}
			LogUtil.e("flag1", flag1+"");
			if(flag1==true){
		    sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
			String response = sp.getString("PickingFinishedGoods","");
			String code = show_etcode.getText().toString();
			String code1 = code.substring(4);
			Pickfinished_tijiao_bean tijiao_bean = new Pickfinished_tijiao_bean();
			tijiao_bean.USERID=Integer.parseInt(userID);
			tijiao_bean.FBOXS=papMoXiang;
			tijiao_bean.WCS_TO_WMSs=jianhuoqu_list1;
			tijiao_bean.ORDERNO=code1;
		    Gson gson = new Gson();
		    String json2 = gson.toJson(tijiao_bean);
		    LogUtil.e("123",json2);
			if(http==null){
				 http = new HttpUtils(2000);
				 http.configCurrentHttpCacheExpiry(0);
				}
			RequestParams params1 = new RequestParams();
			params1.addHeader("Content-Type", "application/json");
			try {
				params1.setBodyEntity(new StringEntity(json2));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			http.send(HttpMethod.POST,ServerUrl.TIJIAO,params1,new RequestCallBack<String>() {
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(),"网络有问题,提交失败",0).show();//提交失败
				}
				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					// TODO Auto-generated method stub
					LogUtil.e("resultl",arg0.result);
					papMoXiang.clear();
					JSONObject json = null;
					try {
						json = new JSONObject(arg0.result);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						if(json.getString("RESULT").equals("success")){
							Toast.makeText(getBaseContext(),json.getString("MESSAGE"),0).show();
							ArrayList<GoodsBean> qingkong_list =new ArrayList<GoodsBean>();
							adapter.list = qingkong_list;
							adapter.notifyDataSetChanged();
							Gson gson = new Gson();
							jsonResult = gson.toJson(qingkong_list);
							scanResult = "";
							//保存
							sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
							//保存商品信息
							Editor edit = sp2.edit();
							edit.putString("PickingFinishedGoods",jsonResult);
							edit.commit();
							//保存订单号
							Editor edit1 = sp2.edit();
							edit1.putString("etcode",scanResult);
							edit1.commit();
							show_etcode.setText("订单号：");
							}else{
								Toast.makeText(getBaseContext(),json.getString("MESSAGE"),0).show();
							}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			}
			break;
		default:
			break;
		}
	}
	/**
	 * 处理商品的条形码
	 * @param scanResult
	 */
	@SuppressWarnings({ "unchecked", "unchecked", "unchecked", "unchecked" })
	private boolean dealWithBarCode(String scanResult){
		 List<GoodsBean> data = (List<GoodsBean>) JsonUtil.parseJsonToList(jsonResult, new TypeToken<List<GoodsBean>>(){}.getType());//解析json数据
		 for (int i = 0; i < data.size(); i++) {
			if(data.get(i).ITEMCODE.equals(scanResult)){//此次扫描的商品编码A2015110370248
//				View view= (View) pickfinishedgoods_jianhuoqu.getChildAt(i);
//				TextView shijianshu = (TextView) view.findViewById(R.id.shijianshu);
//				TextView dingdanshu = (TextView) view.findViewById(R.id.dingdanshu);
//				String shijianNumber = shijianshu.getText().toString().trim();//实拣数量
//				String dingdanNumber = dingdanshu.getText().toString().trim();//订单数量
				int shijian = data.get(i).PQTY;
				int dingdan = data.get(i).QTY;
				
				if(shijian==dingdan){
					Toast.makeText(getBaseContext(),"亲,数量已经够了",0).show();
					return true;
				}
				shijian = shijian+1;
				data.get(i).PQTY=shijian;
				Gson gson = new Gson();
				jsonResult=gson.toJson(data);
				sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
				//保存商品信息
				Editor edit = sp2.edit();
				edit.putString("PickingFinishedGoods",jsonResult);
				edit.commit();
				List<GoodsBean> orderDetail2 = (List<GoodsBean>) JsonUtil.parseJsonToList(jsonResult, new TypeToken<List<GoodsBean>>(){}.getType());//解析json数据
				/**
				 * 在这里面做商品扫码之后改变位置的操作
				 */
				GoodsBean goodsBean = orderDetail2.get(i);
				if(orderDetail2.get(i).QTY==orderDetail2.get(i).PQTY){
					//数据移位放入最后的位置
					orderDetail2.remove(i);
					orderDetail2.add(goodsBean);
				}else{
					//数据放在第一位
					orderDetail2.remove(i);
					orderDetail2.add(0,goodsBean);
				}
				adapter.list=(ArrayList<GoodsBean>) orderDetail2;
				adapter.notifyDataSetChanged();
				jsonResult=gson.toJson(orderDetail2);
				//保存商品信息
				edit.putString("PickingFinishedGoods",jsonResult);
				edit.commit();
				return true;//证明此次扫码是商品的条形码
			}
		}
		Toast.makeText(getBaseContext(),"该商品不在订单中",0).show();
		return true;//证明此次扫码是商品的条形码
	}
	/**
	 * 扫描结果
	 */
//	@Override 
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
//		if(resultCode==RESULT_OK){
//			Bundle bundle1 = data.getExtras();
//			scanResult = bundle1.getString("result");//得到订单的编码A2015103111508
//			System.out.println(scanResult+"长度"+scanResult.length());
//			show_etcode.setText("订单号:"+scanResult);
//			Log.e("WeightingAct",scanResult);
//			//取码值得第一位
//			String result1=scanResult.substring(0,1);
//			//判断是否为泡沫箱码
//			if(scanResult.length()==7&&(result1.equals("B")||result1.equals("X")
//					||result1.equals("J")||result1.equals("L"))){
//				paoMoXiang_code = scanResult;
//				papMoXiang.add(paoMoXiang_code);//扫描的是泡沫箱
//				Gson gson = new Gson();
//				String json = gson.toJson(papMoXiang);
//				sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
//				//保存泡沫箱信息
//				Editor edit = sp2.edit();
//				edit.putString("paomoxiang",json);
//				edit.commit();
//				Toast.makeText(getBaseContext(),"泡沫箱绑定成功",0).show();
//				et_code.setText("");
//				return;//方法结束
//			}
//			//取码值得前三位
//			String result = scanResult.substring(0,3);
//			LogUtil.e("result",result);
//			if(result.equals("WOC")){//此次扫描的是商品的条形码A2015103057636
//				falg = dealWithBarCode(scanResult);
//				if(falg){
//					return;//方法结束,说明此次扫描的是商品的条形码
//				}
//			}
//			papMoXiang.clear();//清空集合
//			getOrderDetails(scanResult);
//			show_etcode.setText("");
//		}
//	}
	/**
	 * 访问网络获取订单中的商品信息
	 * @param result
	 */
	private void getOrderDetails(final String result) {
		http = new HttpUtils(2000);
		http.configCurrentHttpCacheExpiry(0);
		http.send(HttpMethod.GET,ServerUrl.pickingfinishedUrl(result, userID, userCode),new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				// TODO Auto-generated method stub A2015103014732
				Toast.makeText(getBaseContext(),"网络有问题,请检查你的网络",0).show();
				adapter.list = new ArrayList<GoodsBean>();
				if(!scanResult.equals(lastCode)){
				adapter.notifyDataSetChanged();
				}
			}
			@Override
			public void onSuccess(ResponseInfo<String> response) {
				LogUtil.e("userID",userID);
				// TODO Auto-generated method stub
				jsonResult = response.result;
				OrderDetail_picking orderDetail = JsonUtil.parseJsonToBean(response.result,OrderDetail_picking.class);//解析json数据
				if(orderDetail.RESULT.equals("success")){
					if(orderDetail.DATA.size()!=0){//userCode I2015102936742
						//A2015022800001       I2015110395584
						Toast.makeText(getBaseContext(),orderDetail.MESSAGE,0);
						OrderList = orderDetail.DATA;
						//取码值得第一位
						String result1=result.substring(0,1);
						Gson gson = new Gson();
						jsonResult = gson.toJson(OrderList);
						adapter.list = OrderList;
						LogUtil.e("PickingFinishedGoods",OrderList.toString());
						adapter.notifyDataSetChanged();//刷新适配器I2015102911697
						LogUtil.e("PickingFinishedGoods",jsonResult);
						//判断是否是周转箱
						if(result.length()==7&&result1.equals("Z")){
							 scanResult =OrderList.get(0).ORDERNO;
						}else{
							scanResult =result;
						}
						lastCode = result;
						sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
						Editor edit = sp2.edit();
						edit.putString("PickingFinishedGoods",jsonResult);
						edit.commit();
						Editor edit1 = sp2.edit();
						edit1.putString("etcode",scanResult);
						edit1.commit();
						show_etcode.setText("订单号:"+scanResult);
					}else{
						adapter.list = new ArrayList<GoodsBean>();
						if(!scanResult.equals(lastCode)){
						adapter.notifyDataSetChanged();
						scanResult="";
						show_etcode.setText("订单号:");
						Gson gson = new Gson();
						jsonResult = gson.toJson(adapter.list);
						sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
						Editor edit = sp2.edit();
						edit.putString("PickingFinishedGoods",jsonResult);
						edit.commit();
						Editor edit1 = sp2.edit();
						edit1.putString("etcode",scanResult);
						edit1.commit();
						}
						Toast.makeText(getBaseContext(),"服务器返回数据异常",0).show();
						return;
					}
				}else{
					adapter.list = new ArrayList<GoodsBean>();
					if(!scanResult.equals(lastCode)){
					adapter.notifyDataSetChanged();
					scanResult="";
					show_etcode.setText("订单号:");
					Gson gson = new Gson();
					jsonResult = gson.toJson(adapter.list);
					sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
					Editor edit = sp2.edit();
					edit.putString("PickingFinishedGoods",jsonResult);
					edit.commit();
					Editor edit1 = sp2.edit();
					edit1.putString("etcode",scanResult);
					edit1.commit();
					}
					Toast.makeText(getBaseContext(),orderDetail.MESSAGE,0).show();
					return;
				}
			}
		});
	}
	class pickfinishedgoods_weijianqu extends BaseExpandableListAdapter{
		
		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return weijiaqu_list.size();
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return weijiaqu_list.get(groupPosition).OrderList.size()+1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return weijiaqu_list.get(groupPosition).OrderList;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return weijiaqu_list.get(groupPosition).OrderList.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(final int groupPosition, final boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View weijianqu_group = View.inflate(pickingFinishedGoods,R.layout.weijianqu_group,null);
			TextView show_etcode1 = (TextView) weijianqu_group.findViewById(R.id.show_etcode);
			LinearLayout layout = (LinearLayout) weijianqu_group.findViewById(R.id.groupExpand);
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (isExpanded)
	                {
						pickfinishedgoods_weijianqu.collapseGroup(groupPosition);
	                } else
	                {
	                	pickfinishedgoods_weijianqu.expandGroup(groupPosition);
	                }
				}
			});
			View dianjixujian = weijianqu_group.findViewById(R.id.dianjixujian);
			dianjixujian.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//获取点击位置的数据
					String orderNO = weijiaqu_list.get(groupPosition).OrderNO;//获取点击续拣的订单
					//获取续拣订单中的所有的商品集合
					OrderList= weijiaqu_list.get(groupPosition).OrderList;//拣货区订单所包含商品的集合
					adapter.list = OrderList;
					adapter.notifyDataSetChanged();
					Gson gson = new Gson();
					jsonResult = gson.toJson(OrderList);//再次保存当前值
					scanResult=orderNO;//再次保存当前值 A2015102931681
					show_etcode.setText("订单号:"+scanResult);
					show_etcode.setTextColor(Color.BLACK);
					pickfinishedgoods_jianhuoqu.setVisibility(View.VISIBLE);
					pickfinishedgoods_weijianqu.setVisibility(View.INVISIBLE);
					bt_jiechubangding2.setVisibility(View.VISIBLE);
					jianhuoqu.setVisibility(View.VISIBLE);
					weijianqu.setVisibility(View.INVISIBLE);
					show_etcode.setVisibility(View.VISIBLE);
					tv_paomocode.setVisibility(View.VISIBLE);
					show.setVisibility(View.VISIBLE);
					papMoXiang.clear();
					mFlowLayout.removeAllViews();
					mFlowLayout.setVisibility(View.VISIBLE);
					shangpingmiaoshu.setVisibility(View.VISIBLE);
					jianhuoqu_textview.setTextColor(0xff006600);
					weijianqu_textview.setTextColor(0xff000000);
					//保存订单号,订单中的所有商品的信息
					sp2 = getSharedPreferences("PickingFinishedGoods",MODE_PRIVATE);
					//保存商品信息
					Editor edit = sp2.edit();
					edit.putString("PickingFinishedGoods",jsonResult);
					edit.commit();
					//保存订单号
					Editor edit1 = sp2.edit();
					edit1.putString("etcode",scanResult);
					edit1.commit();
				}
			});
			show_etcode1.setText("订单号:"+weijiaqu_list.get(groupPosition).OrderNO);
			return weijianqu_group;
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View pickingfinishedgoods_items = View.inflate(pickingFinishedGoods,R.layout.pickingfinishedgoods_items,null);
			TextView huoweihao = (TextView) pickingfinishedgoods_items.findViewById(R.id.huoweihao);
			TextView shangpingbianma = (TextView) pickingfinishedgoods_items.findViewById(R.id.shangpingbianma);
			TextView shangpingmingcheng = (TextView) pickingfinishedgoods_items.findViewById(R.id.shangpingmingcheng);
			TextView dingdanshu = (TextView) pickingfinishedgoods_items.findViewById(R.id.dingdanshu);
			TextView shijianshu = (TextView) pickingfinishedgoods_items.findViewById(R.id.shijianshu);
			if(childPosition==0){
				return pickingfinishedgoods_items;
			}else{
				shijianshu.setText(""+weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).PQTY);
				huoweihao.setText(weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).LOCATION);
				shangpingbianma.setText(weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).ITEMCODE);
				shangpingmingcheng.setText(weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).ITEMNAME);
				dingdanshu.setText(""+weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).QTY);
				if(weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).QTY!=weijiaqu_list.get(groupPosition).OrderList.get(childPosition-1).PQTY){
					  shijianshu.setTextColor(Color.RED);
					}
					return pickingfinishedgoods_items;
			}
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

	}

}
