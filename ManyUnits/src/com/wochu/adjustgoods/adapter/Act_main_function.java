package com.wochu.adjustgoods.adapter;

import com.wochu.adjustgoods.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Act_main_function extends BaseAdapter {
	
	private Context mcontext;
	private View erp_functions;
	private ImageView img;
	private TextView text_desc;
	private int[] imageResources;
	private String[] functions;
	public Act_main_function(Context context, String[] functions, int[] imageResources) {
		// TODO Auto-generated constructor stub
		mcontext = context;
		this.functions = functions;
		this.imageResources = imageResources;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return functions.length;
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

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		erp_functions = View.inflate(mcontext,R.layout.erp_functions,null);
		img = (ImageView) erp_functions.findViewById(R.id.img);
		text_desc = (TextView) erp_functions.findViewById(R.id.text_desc);
		img.setBackgroundResource(imageResources[position]);
		text_desc.setText(functions[position]);
		return erp_functions;
	}

}
