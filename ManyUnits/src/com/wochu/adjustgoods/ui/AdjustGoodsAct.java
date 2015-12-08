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
import com.wochu.adjustgoods.bean.AdjustGoodBean.Gooods;
import com.wochu.adjustgoods.net.OkHttpClientManager;
import com.wochu.adjustgoods.net.OkHttpClientManager.ResultCallback;
import com.wochu.adjustgoods.url.AppClient;
import com.wochu.adjustgoods.utils.LogUtil;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
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
	private ArrayList<ArrayList<Gooods>> list = new ArrayList<>();//存放每种商品的集合
	private Context mcontext;
	private MyAdapter myAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjustgoods);
		initView();
		mcontext = this;
	}
	private void initView() {
		// TODO Auto-generated method stub
		btn_trance_orderNumber = (Button) findViewById(R.id.btn_trance_orderNumber);
		et_code = (EditText) findViewById(R.id.et_code);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		tv_huoweihao = (TextView) findViewById(R.id.tv_huoweihao);
		listview_showinformation = (ExpandableListView) findViewById(R.id.listview_showinformation);
		myAdapter = new MyAdapter();
		listview_showinformation.setAdapter(myAdapter);
		btn_confirm.setOnClickListener(this);
		listview_showinformation.setGroupIndicator(null);//将控件默认的左边箭头去掉
		listview_showinformation.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				showPickdialog(v, groupPosition,childPosition);
				return true;
			}
		});
			
	}
	/**
	 * 弹出对话框
	 */
	private void showPickdialog(final View view,final int groupPosition,int childPosition) {
		final Dialog dialog = new Dialog(AdjustGoodsAct.this,R.style.pick_dialog);
		dialog.setContentView(R.layout.adjustgoodact_dialog_view);
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
	    final EditText xiaodanwei = (EditText) dialog.findViewById(R.id.xiaodanwei);//小单位的值
	    btn_ok.setOnClickListener(new OnClickListener() {//确定按钮
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub A2015103072004
				if(TextUtils.isEmpty(ed_dadanwei.getText().toString())){
					Toast.makeText(getBaseContext(),"大单位不能为空",0).show();
					return;
				}
				if(TextUtils.isEmpty(xiaodanwei.getText().toString())){
					Toast.makeText(getBaseContext(),"小单位不能为空",0).show();
					return;
				}
				
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
					goodClassificationByCode(response.DATA);
					myAdapter.notifyDataSetChanged();
					tv_huoweihao.setText("货位号:"+locationCode);
				}else{
					Toast.makeText(getBaseContext(),"获取商品信息失败",0).show();
				}
			}
		});
	}
	/**
	 * 开始按商品编码进行分类
	 */
	protected void goodClassificationByCode(List<Gooods> data) {
		// TODO Auto-generated method stub
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
		
		
		public MyAdapter() {
			super();
		}
		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return list.size();
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return list.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return list.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return list.get(groupPosition).get(childPosition);
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
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View goodclassbycode_item = View.inflate(mcontext,R.layout.goodclassbycode_item,null);
			TextView goodsCode = (TextView) goodclassbycode_item.findViewById(R.id.goodsCode);
			TextView goodsName = (TextView) goodclassbycode_item.findViewById(R.id.goodsName);
			if(list.get(groupPosition).size()>0){
			goodsCode.setText("商品编码:"+list.get(groupPosition).get(0).GOODSCODE);
			goodsName.setText("商品名称:"+list.get(groupPosition).get(0).GOODSNAME);
		}
			return goodclassbycode_item;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			 ViewHolder holder = null;
			 if (convertView == null) {
	             convertView = View.inflate(mcontext, R.layout.goodadjust_item, null);
	             holder = new ViewHolder();
	             holder.BatchNo = (TextView)convertView.findViewById(R.id.picihao);
	             holder.big_unit = (TextView) convertView.findViewById(R.id.dadanwei);
	             holder.small_unit = (TextView) convertView.findViewById(R.id.xiaodanwei);
	             convertView.setTag(holder);
	             }else{
	            	 holder = (ViewHolder)convertView.getTag();
	             }
			 holder.BatchNo.setText(list.get(groupPosition).get(childPosition).GOODSBATCHCODE);
			 holder.big_unit.setText(list.get(groupPosition).get(childPosition).BuyQty+"");
			 holder.small_unit.setText(list.get(groupPosition).get(childPosition).StoreQty+"");
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
	}
}
