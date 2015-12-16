package com.wochu.adjustgoods.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.squareup.okhttp.Request;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.base.BaseActivity;
import com.wochu.adjustgoods.bean.AdjustGoodBean;
import com.wochu.adjustgoods.bean.AdjustGoodBean.Gooods;
import com.wochu.adjustgoods.bean.GoodsQuery.GoodsBean;
import com.wochu.adjustgoods.net.OkHttpClientManager;
import com.wochu.adjustgoods.net.OkHttpClientManager.ResultCallback;
import com.wochu.adjustgoods.url.AppClient;
import com.wochu.adjustgoods.utils.JsonUtil;
import com.wochu.adjustgoods.utils.LogUtil;
import com.wochu.adjustgoods.utils.SharePreUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdjustGoodsAct extends BaseActivity implements OnClickListener{
	private Button btn_trance_orderNumber;
	private EditText et_code;
	private Button btn_confirm;
	private TextView tv_huoweihao;
	private ExpandableListView listview_showinformation;
	private String locationCode = "";
	private Context mcontext;
	private MyAdapter myAdapter;
	private Button summit;
	private int userID;//工人工号
	private MyAdapter myAdapterInvalid ;//作废的适配器
	private Handler handler = new Handler();
	private LinearLayout lv_TakingInventoryArea;
	private LinearLayout lv_MistakenDeleteArea;
	private TextView tv_TakingInventoryArea;
	private ImageView iv_TakingInventoryArea;
	private TextView tv_MistakenDeleteArea;
	private ImageView iv_MistakenDeletAarea;
	private ExpandableListView listview_MistakenDeleteInformation;
	private Button btn_invalid;
	private BroadcastReceiver receiver;
	/**
	 *盘货区集合
	 */
	private ArrayList<Long> longList = new ArrayList<Long>();
	private ArrayList<String> goodsCodelist = new ArrayList<>();//商品编码的集合
	private ArrayList<ArrayList<Gooods>> list = new ArrayList<>();//存放每种商品的集合
	private ArrayList<AdjustGoodBean.Gooods> gooodsList = new ArrayList<>();//所有批次号商品的集合
	/**
	 * 作废区集合
	 */
	private ArrayList<Gooods> invalidGoods = new ArrayList<AdjustGoodBean.Gooods>();//作废批次商品集合
	private ArrayList<String> invalidGoodsCodeList = new ArrayList<String>();//作废商品总类的集合
	private ArrayList<ArrayList<Gooods>> invalidList = new ArrayList<>();//存放每种作废商品的集合
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjustgoods);
		initView();
		mcontext = this;
		userID = SharePreUtil.getInteger(mcontext,"userID",-1);
		LogUtil.e("AdjustGoodsAct",userID+"");
		setListener();
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(action.equals("com.android.scanservice.scancontext")){
					locationCode = intent.getStringExtra("Scan_context");//获得扫码值
					if(!TextUtils.isEmpty(locationCode)&&(!"".equals(locationCode))){
						getData();
						return;
					}
					Toast.makeText(getBaseContext(),"请先扫描货位号",0).show();
				}
			}
		};
	}
	/**
	 * 初始化
	 */
	private void initView() {
		lv_TakingInventoryArea = (LinearLayout) findViewById(R.id.lv_TakingInventoryArea);
		tv_TakingInventoryArea = (TextView) findViewById(R.id.tv_TakingInventoryArea);
		iv_TakingInventoryArea = (ImageView) findViewById(R.id.iv_TakingInventoryArea);
		lv_MistakenDeleteArea = (LinearLayout) findViewById(R.id.lv_MistakenDeleteArea);
		tv_MistakenDeleteArea = (TextView) findViewById(R.id.tv_MistakenDeleteArea);
		iv_MistakenDeletAarea = (ImageView) findViewById(R.id.iv_MistakenDeletAarea);
		summit = (Button) findViewById(R.id.summit);
		btn_trance_orderNumber = (Button) findViewById(R.id.btn_trance_orderNumber);
		et_code = (EditText) findViewById(R.id.et_code);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		tv_huoweihao = (TextView) findViewById(R.id.tv_huoweihao);
		listview_showinformation = (ExpandableListView) findViewById(R.id.listview_showinformation);
		listview_MistakenDeleteInformation = (ExpandableListView) findViewById(R.id.listview_MistakenDeleteInformation);
		myAdapter = new MyAdapter(list,0);
		listview_showinformation.setAdapter(myAdapter);
		myAdapterInvalid = new MyAdapter(invalidList,1);
		listview_MistakenDeleteInformation.setAdapter(myAdapterInvalid);
		listview_showinformation.setGroupIndicator(null);//将控件默认的左边箭头去掉
		listview_MistakenDeleteInformation.setGroupIndicator(null);
	}
	/**
	 * 设置监听器
	 */
	public void setListener() {
		lv_TakingInventoryArea.setOnClickListener(this);
		lv_MistakenDeleteArea.setOnClickListener(this);
		btn_confirm.setOnClickListener(this);
		summit.setOnClickListener(this);
		listview_showinformation.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent,final View v, int groupPosition, int childPosition,
							long id) {
						// TODO Auto-generated method stub
						showPickdialog(v, groupPosition, childPosition, id);
						v.setBackgroundColor(Color.GREEN);
						if(!longList.contains(id)){
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								v.setBackgroundColor(Color.WHITE);
							}
						}, 500);
					}
						return true;
					}
				});
		listview_MistakenDeleteInformation.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent,final View v, final int groupPosition, final int childPosition,
							long id) {
						// TODO Auto-generated method stub
						final Dialog dialog = showPickdialog(v, groupPosition,
								childPosition, id,
								R.layout.adjustgoodact_invalid_dialog);
						v.setBackgroundColor(Color.GREEN);
						handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								v.setBackgroundColor(Color.WHITE);
							}
						}, 500);

						Button btn_ok = (Button) dialog
								.findViewById(R.id.btn_ok);
						Button btn_cancel = (Button) dialog
								.findViewById(R.id.btn_cancel);
						btn_ok.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
							Gooods goods = (Gooods) myAdapterInvalid.getChild(groupPosition, childPosition);//获得选的批次商品
							goods.STATUS = 1;
							gooodsList= new ArrayList<AdjustGoodBean.Gooods>();
							gooodsList.add(goods);
							goodsCodelist.clear();
							longList.clear();
							list.clear();
							//分类操作
							goodClassificationByCode(gooodsList, goodsCodelist, list);
							
							myAdapter.listAdapter = list.subList(0, 1);
							LogUtil.e("123",myAdapter.listAdapter.size()+"");
							myAdapter.notifyDataSetChanged();
							int count = listview_showinformation.getCount();
							for (int i=0; i<count; i++)  
							 {   
								listview_showinformation.collapseGroup(i);  
							 };   
							tv_TakingInventoryArea.setTextColor(Color.GREEN);
							iv_TakingInventoryArea.setVisibility(View.VISIBLE);
							listview_MistakenDeleteInformation.setVisibility(View.INVISIBLE);
							tv_MistakenDeleteArea.setTextColor(Color.BLACK);
							iv_MistakenDeletAarea.setVisibility(View.INVISIBLE);
							listview_showinformation.setVisibility(View.VISIBLE);
							dialog.dismiss();
							tv_huoweihao.setText("货位号:"+myAdapter.listAdapter.get(0).get(0).LOCATIONCODE);
							}
						});
						btn_cancel.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
						return true;
					}
				});
		
	}
	
	/**
	 * 弹出对话框2
	 */
	private Dialog showPickdialog(View v, int groupPosition, int childPosition,
			long id, int layoutId) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(AdjustGoodsAct.this,R.style.pick_dialog);
		dialog.setContentView(layoutId);
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
	    dialog.show();
	    dialog.setCanceledOnTouchOutside(false);
	    return dialog;
	}

	/**
	 * 弹出对话框1
	 */
	private void showPickdialog(final View view,final int groupPosition,final int childPosition,final Long id) {
		String purunitname = list.get(groupPosition).get(childPosition).PURUNITNAME;//大单位名字
		final String unitname = list.get(groupPosition).get(childPosition).UNITNAME;//小单位名字——
		final Dialog dialog = new Dialog(AdjustGoodsAct.this,R.style.pick_dialog);
		View adjustgoodact_dialog_view = View.inflate(mcontext, R.layout.adjustgoodact_dialog_view,null);
		TextView tv_big = (TextView) adjustgoodact_dialog_view.findViewById(R.id.tv_big);
		TextView tv_small = (TextView) adjustgoodact_dialog_view.findViewById(R.id.tv_small);
		tv_big.setText(purunitname+":");
		tv_small.setText(unitname+":");
		LinearLayout small = (LinearLayout) adjustgoodact_dialog_view.findViewById(R.id.small);
		if("——".equals(unitname)){
			small.setVisibility(View.GONE);
		}else{
			small.setVisibility(View.VISIBLE);
		}
		dialog.setContentView(adjustgoodact_dialog_view);
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
	    final EditText ed_dadanwei = (EditText) dialog.findViewById(R.id.ed_dadanwei);//大单位的值
	    btn_invalid = (Button) dialog.findViewById(R.id.btn_invalid);
	    btn_ok.setOnClickListener(new OnClickListener() {//确定按钮
	    	String smallUnits;
	    	EditText xiaodanwei;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub A2015103072004
				String bigUnits = ed_dadanwei.getText().toString();//大单位
				 if(!"——".equals(unitname)){
					 xiaodanwei = (EditText) dialog.findViewById(R.id.xiaodanwei);
					 smallUnits = xiaodanwei.getText().toString();//小单位
				 }
				if(TextUtils.isEmpty(ed_dadanwei.getText().toString())){
					Toast.makeText(getBaseContext(),"大单位不能为空",0).show();
					return;
				}
				 if(!"——".equals(unitname)){
					 if(TextUtils.isEmpty(xiaodanwei.getText().toString())){
							Toast.makeText(getBaseContext(),"小单位不能为空",0).show();
							return;
					 }
				 }
				list.get(groupPosition).get(childPosition).BuyQty=Double.valueOf(bigUnits);
				if(!"——".equals(unitname)){
					list.get(groupPosition).get(childPosition).StoreQty = Double.valueOf(smallUnits);
				}
				Gooods gooods = list.get(groupPosition).get(childPosition);
				list.get(groupPosition).remove(gooods);
				list.get(groupPosition).add(gooods);
				myAdapter.listAdapter = list;
				myAdapter.notifyDataSetChanged();
				view.setBackgroundColor(Color.GREEN);
				if(!longList.contains(id)){
					longList.add(id);
				}
				dialog.dismiss();
			}
		});
	    btn_invalid.setOnClickListener(new OnClickListener() {//批次商品作废功能
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				list.get(groupPosition).get(childPosition).STATUS = 0;
				String BatchCode = list.get(groupPosition).get(childPosition).GOODSBATCHCODE;
				OkHttpClientManager.postAsyn(AppClient.getPostInvalidGood(getLocationCode(tv_huoweihao.getText().toString()), BatchCode,0),new ResultCallback<String>(){

					@Override
					public void onError(Request request, Exception e) {
						// TODO Auto-generated method stub
						Toast.makeText(getBaseContext(),"作废失败",0).show();
					}
					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
						LogUtil.e("AdjustGoodsAct",response);
						Toast.makeText(getBaseContext(),"作废成功",0).show();
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
	    dialog.setCanceledOnTouchOutside(false);
	}
	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_confirm:
			locationCode = et_code.getText().toString();
			if(!TextUtils.isEmpty(locationCode)&&(!"".equals(locationCode))){
				getData();
				longList.clear();
				return;
			}
			Toast.makeText(getBaseContext(),"请先扫描货位号",0).show();
			break;
		case R.id.btn_trance_orderNumber:
			Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
			sendBroadcast(intent);//发送广播
			break;
		case R.id.lv_TakingInventoryArea://盘货区
			tv_TakingInventoryArea.setTextColor(Color.GREEN);
			iv_TakingInventoryArea.setVisibility(View.VISIBLE);
			listview_MistakenDeleteInformation.setVisibility(View.INVISIBLE);
			tv_MistakenDeleteArea.setTextColor(Color.BLACK);
			iv_MistakenDeletAarea.setVisibility(View.INVISIBLE);
			listview_showinformation.setVisibility(View.VISIBLE);
			break;
		case R.id.lv_MistakenDeleteArea://作废区
			tv_TakingInventoryArea.setTextColor(Color.BLACK);
			iv_TakingInventoryArea.setVisibility(View.INVISIBLE);
			listview_MistakenDeleteInformation.setVisibility(View.VISIBLE);
			tv_MistakenDeleteArea.setTextColor(Color.GREEN);
			iv_MistakenDeletAarea.setVisibility(View.VISIBLE);
			listview_showinformation.setVisibility(View.INVISIBLE);
			//联网操作获取作废区批次商品
			if(!(getLocationCode(tv_huoweihao.getText().toString()).length()>0)){
				Toast.makeText(getBaseContext(),"请先扫描正确的货位号",0).show();
				return;
			}
			OkHttpClientManager.getAsyn(AppClient.getInvalidGoodInformations(getLocationCode(tv_huoweihao.getText().toString()),0),new ResultCallback<AdjustGoodBean>() {
				
				@Override
				public void onBefore(Request request) {//开始访问
					// TODO Auto-generated method stub
					super.onBefore(request);
					showLoadingDialog("数据加载中");
				}
				@Override
				public void onError(Request request, Exception e) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(),"获取作废区商品参数失败",0).show();
					invalidGoods.clear();
					//对所有作废商品进行分类
					goodClassificationByCode(invalidGoods,invalidGoodsCodeList, invalidList);
					myAdapterInvalid.listAdapter = invalidList;
					myAdapterInvalid.notifyDataSetChanged();
					hideLoadingDialog();
				}
				@Override
				public void onResponse(AdjustGoodBean response) {
					// TODO Auto-generated method stub
					invalidGoods = (ArrayList<Gooods>) response.DATA;
					Toast.makeText(getBaseContext(),"获取作废区商品参数成功",0).show();
					//对所有作废商品进行分类
					goodClassificationByCode(invalidGoods,invalidGoodsCodeList, invalidList);
					myAdapterInvalid.listAdapter = invalidList;
					myAdapterInvalid.notifyDataSetChanged();
				}
				@Override
				public void onAfter() {//访问结束
					// TODO Auto-generated method stub
					super.onAfter();
					hideLoadingDialog();
				}
			});
			
			break;
		case R.id.summit://提交操作
			postData();
			break;
		default:
			break;
		}
	}
	/**
	 * 提交数据
	 */
	private void postData(){
		Gson gson = new Gson();
		String postJson = gson.toJson(gooodsList);
		LogUtil.e("postJson", postJson);
		try {
			File fp = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "wochu.txt");
			FileWriter pw = new FileWriter(fp);
			pw.write(postJson);
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OkHttpClientManager.postAsyn(AppClient.PostGoodInformations+userID, new ResultCallback<String>() {
			
			@Override
			public void onBefore(Request request) {
				// TODO Auto-generated method stub
				super.onBefore(request);
				showLoadingDialog("数据提交中");
			}
			@Override
			public void onError(Request request, Exception e) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),"提交失败",0).show();
				hideLoadingDialog();
			}
			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				LogUtil.e("AdjustGoodsAct", response);
				Toast.makeText(getBaseContext(),"提交成功",0).show();
			}
			@Override
			public void onAfter() {
				// TODO Auto-generated method stub
				super.onAfter();
				hideLoadingDialog();
			}
		}, postJson.toString());
	}
	@SuppressWarnings("unused")
	private void getData() {
		OkHttpClientManager.getAsyn(AppClient.GetGoodInformations+locationCode,new ResultCallback<AdjustGoodBean>() {
			
			@Override
			public void onBefore(Request request) {
				// TODO Auto-generated method stub
				super.onBefore(request);
				showLoadingDialog("数据加载中");
			}
			@Override
			public void onError(Request request, Exception e) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),"获取商品信息失败",0).show();
			}
			@Override
			public void onResponse(AdjustGoodBean response) {
				// TODO Auto-generated method stub
				LogUtil.e("AdjustGoodsAct",response.MESSAGE);
				try {
					File fp = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "wo.txt");
					FileWriter pw = new FileWriter(fp);
					pw.write(response.toString());
					pw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String result = response.RESULT;
				if("success".equals(result)){
					//开始按商品编码进行分类
					list.clear();
					goodsCodelist.clear();
					gooodsList=(ArrayList<Gooods>) response.DATA;
					goodClassificationByCode(response.DATA,goodsCodelist,list);
					myAdapter.listAdapter = list;
					myAdapter.notifyDataSetChanged();
					tv_huoweihao.setText("货位号:"+locationCode);
				}else{
					Toast.makeText(getBaseContext(),"获取商品信息失败",0).show();
				}
			}
			@Override
			public void onAfter() {
				// TODO Auto-generated method stub
				super.onAfter();
				hideLoadingDialog();
			}
		});
	}
	/**
	 * 获取当前显示的货位号
	 * @param locationCode
	 * @return
	 */
	public String getLocationCode(String locationCode){
		locationCode = locationCode.substring(4);
		LogUtil.e("AdjustGoodsAct",locationCode);
		return locationCode;
	}
	/**
	 * 开始按商品编码进行分类
	 * @param list 
	 */
	protected void goodClassificationByCode(List<Gooods> data,List<String> goodsCodelist, ArrayList<ArrayList<Gooods>> list) {
		// TODO Auto-generated method stub
		goodsCodelist.clear();
		list.clear();
		for (int i = 0; i < data.size(); i++) {
			if(!goodsCodelist.contains(data.get(i).GOODSCODE)){
				goodsCodelist.add(data.get(i).GOODSCODE);
			}
		}
		for (int j = 0; j < goodsCodelist.size(); j++) {
			ArrayList<Gooods> goodsList = new ArrayList<Gooods>();
			list.add(goodsList);
		}
		for (int k = 0; k < goodsCodelist.size(); k++) {
			for (int m = 0; m < data.size(); m++) {
				if(data.get(m).GOODSCODE.equals(goodsCodelist.get(k))){
					list.get(k).add(data.get(m));
				}
			}
		}
	}
	class MyAdapter extends BaseExpandableListAdapter{
		
		public int flag =0;
		List<ArrayList<Gooods>> listAdapter = new ArrayList<ArrayList<Gooods>>();
		
		public MyAdapter(List<ArrayList<Gooods>> list,int flag) {
			// TODO Auto-generated method stub
			this.listAdapter = list;
			this.flag = flag;
		}
		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return listAdapter.size();
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return listAdapter.get(groupPosition).size();
		}
		
		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return listAdapter.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return listAdapter.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return Long.parseLong(listAdapter.get(groupPosition).get(childPosition).GOODSBATCHCODE);
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View goodclassbycode_item = View.inflate(mcontext,R.layout.goodclassbycode_item,null);
			TextView goodsCode = (TextView) goodclassbycode_item.findViewById(R.id.goodsCode);
			TextView goodsName = (TextView) goodclassbycode_item.findViewById(R.id.goodsName);
			if(listAdapter.get(groupPosition).size()>0){
			goodsCode.setText("商品编码:"+listAdapter.get(groupPosition).get(0).GOODSCODE);
			goodsName.setText("商品名称:"+listAdapter.get(groupPosition).get(0).GOODSNAME);
		}
			return goodclassbycode_item;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			 ViewHolder holder = null;
			 if (convertView == null) {
				 //convertView= LayoutInflater.from(AdjustGoodsAct.this).inflate(R.layout.goodadjust_item, parent, true);
	             convertView = View.inflate(mcontext, R.layout.goodadjust_item, null);
	             holder = new ViewHolder(convertView);
	             holder.BatchNo = (TextView)convertView.findViewById(R.id.picihao);
	             holder.big_unit = (TextView) convertView.findViewById(R.id.dadanwei);
	             holder.small_unit = (TextView) convertView.findViewById(R.id.xiaodanwei);
	             holder.tv_showBigUnits = (TextView) convertView.findViewById(R.id.tv_showBigUnits);
	             holder.tv_showSmallUnits = (TextView) convertView.findViewById(R.id.tv_showSmallUnits);
	             convertView.setTag(holder);
	             }else{
	            	 holder = (ViewHolder)convertView.getTag();
	             }
			 holder.rootView.setBackgroundResource(R.drawable.adjustact_expandlistview);
			 holder.BatchNo.setText(listAdapter.get(groupPosition).get(childPosition).GOODSBATCHCODE);
			 holder.big_unit.setText(listAdapter.get(groupPosition).get(childPosition).BuyQty+"");
			 holder.small_unit.setText(listAdapter.get(groupPosition).get(childPosition).StoreQty+"");
			 if(listAdapter.get(groupPosition).get(childPosition).PURUNITNAME.equals(listAdapter.get(groupPosition).get(childPosition).UNITNAME)){
				 listAdapter.get(groupPosition).get(childPosition).UNITNAME = "——";
			 }
			 holder.tv_showBigUnits.setText("数量/"+listAdapter.get(groupPosition).get(childPosition).PURUNITNAME);
			 holder.tv_showSmallUnits.setText("数量/"+listAdapter.get(groupPosition).get(childPosition).UNITNAME);
			 if(this.flag==0){
			 if(longList.contains(getChildId(groupPosition, childPosition))){
				 convertView.setBackgroundColor(Color.GREEN);
			 }else{
				 convertView.setBackgroundColor(Color.WHITE);
			 }
			}else{
				convertView.setBackgroundColor(Color.WHITE);
			}
			 return convertView;
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
	}
	static class  ViewHolder {
		
		public TextView BatchNo;//批次号
		public TextView big_unit;//大单位
		public TextView small_unit;//小单位
		public TextView tv_showBigUnits;//展示大单位
		public TextView tv_showSmallUnits;//展示小单位
		public View rootView;
		public ViewHolder(View rootView){
			this.rootView = rootView;
		}
		
	}
}
