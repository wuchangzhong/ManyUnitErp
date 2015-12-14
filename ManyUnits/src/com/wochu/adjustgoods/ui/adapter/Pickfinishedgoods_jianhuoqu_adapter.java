package com.wochu.adjustgoods.ui.adapter;

import java.util.ArrayList;

import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.GoodsBean;
import com.wochu.adjustgoods.utils.LogUtil;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Pickfinishedgoods_jianhuoqu_adapter extends BaseAdapter {
	private Context mcontext;
	public ArrayList<GoodsBean> list ;
	private View pickingfinishedgoods_items;
	public Pickfinishedgoods_jianhuoqu_adapter(Context context, ArrayList<GoodsBean> list) {
		super();
		this.list = list;
		this.mcontext = context;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub I2015102913744  I2015110388571
		LogUtil.e("pickfinishedgoods_jianhuoqu_adapter",list.size()+"");
		return list.size();
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
//		if(position==0){
//			pickingfinishedgoods_items = View.inflate(mcontext,R.layout.pickingfinishedgoods_items,null);
//			return pickingfinishedgoods_items;W2015022800004
//		}
		pickingfinishedgoods_items = View.inflate(mcontext,R.layout.pickingfinishedgoods_items,null);
		TextView huoweihao = (TextView) pickingfinishedgoods_items.findViewById(R.id.huoweihao);
		TextView shangpingbianma = (TextView) pickingfinishedgoods_items.findViewById(R.id.shangpingbianma);
		TextView shangpingmingcheng = (TextView) pickingfinishedgoods_items.findViewById(R.id.shangpingmingcheng);
		TextView dingdanshu = (TextView) pickingfinishedgoods_items.findViewById(R.id.dingdanshu);
		TextView shijianshu = (TextView) pickingfinishedgoods_items.findViewById(R.id.shijianshu);
		shijianshu.setText(""+list.get(position).PQTY);
		huoweihao.setText(list.get(position).LOCATION);
		shangpingbianma.setText(list.get(position).ITEMCODE);
		shangpingmingcheng.setText(list.get(position).ITEMNAME);
		dingdanshu.setText(""+list.get(position).QTY);
		if(list.get(position).QTY!=list.get(position).PQTY){
		  shijianshu.setTextColor(Color.RED);
		}
		return pickingfinishedgoods_items;
	}
}
