package com.wochu.adjustgoods.ui.viewholder;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

public class MyRecheckViewHolder extends ViewHolder implements OnClickListener,OnLongClickListener{
	public TextView orderNum;
public TextView goodsName;
public TextView goodsNum;
public TextView scanNum;
	private MyOnitemClickListenner onItemClickListenner;
	private MyOnItemLongClickListenner onItemLongClickListenner;
	public MyRecheckViewHolder(View rootView) {
		super(rootView);
		rootView.setOnClickListener(this);
		rootView.setOnLongClickListener(this);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onLongClick(View v) {
		if(onItemLongClickListenner!=null){
			onItemLongClickListenner.onItemLongClick(v, getPosition());
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		if(onItemClickListenner!=null){
			onItemClickListenner.onItemClick(v, getPosition());
		}
		
	}
	/**
	 * 条目点击事件回调接口
	 * @author Administrator
	 *
	 */
	public interface MyOnitemClickListenner{
		public void onItemClick(View v,int position);
	}
	
	public interface MyOnItemLongClickListenner{
		public void onItemLongClick(View v,int position);
	}
	/**
	 * 设置条目点击监听
	 * @param listenner
	 */
public void setOnItemClickListenner(MyOnitemClickListenner listenner){
	onItemClickListenner = listenner;
}
/**
 * 设置条目长按监听
 * @param listenner
 */
public void setOnItemLongClickListenner(MyOnItemLongClickListenner listenner){
	onItemLongClickListenner = listenner;
}

}
