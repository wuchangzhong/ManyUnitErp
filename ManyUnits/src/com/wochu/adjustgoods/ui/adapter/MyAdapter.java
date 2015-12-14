package com.wochu.adjustgoods.ui.adapter;

import java.util.ArrayList;

import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.WeightingBean;



import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
	public Context mcontext;
	public ArrayList<WeightingBean> weightList;
	private View view;
	private TextView product_code;
	private TextView product_weight;
	private TextView product_name;
	public MyAdapter(Context context, ArrayList<WeightingBean> list) {
		super();
		// TODO Auto-generated constructor stub
		weightList = list;
		mcontext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return weightList.size()+1;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup arg2) {
		// TODO Auto-generated method stubi
		if(position==0){
		view = View.inflate(mcontext,R.layout.list_item_weighting,null);
		}else{
		view = View.inflate(mcontext, R.layout.list_item_weighting, null);
		product_code = (TextView) view.findViewById(R.id.Product_code);
		product_weight = (TextView) view.findViewById(R.id.Product_weight);
		product_name = (TextView) view.findViewById(R.id.Product_name);
		product_code.setText((CharSequence) weightList.get(position-1).GoodsCode);
		product_name.setText((CharSequence) weightList.get(position-1).GoodsName);
		product_weight.setText((CharSequence) weightList.get(position-1).weighting);
		}
		return view;
	}

}
