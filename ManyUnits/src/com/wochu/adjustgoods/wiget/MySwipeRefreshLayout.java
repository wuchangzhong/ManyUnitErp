package com.wochu.adjustgoods.wiget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MySwipeRefreshLayout extends SwipeRefreshLayout {
	
	public boolean comiting =false;

	public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MySwipeRefreshLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
@Override
public boolean onTouchEvent(MotionEvent ev) {
	// TODO Auto-generated method stub
	if(this.comiting){
	return false;	
	}else{
	return super.onTouchEvent(ev);
	}
	
}

}
