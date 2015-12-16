package com.wochu.adjustgoods.base;
import com.wochu.adjustgoods.utils.ToastUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	/**
	 * 公共toast
	 * 
	 * @param msg
	 */
	public void ToastCheese(String msg) {
		ToastUtil.makeText(this, msg).show();
	}

	/**
	 *  判断网络是否连接
	 * @return
	 */
	protected boolean baseHasNet() {
		if (!com.wochu.adjustgoods.utils.NetUtils.isConnected(this)) {
			ToastCheese("亲，当前网络不给力!");
			return false;
		}
		return true;
	}
	
	/**
	 * 加载圆形进度条
	 */
	private static Dialog	mProgressDialog;
	public void showLoadingDialog(String message) {
		
		if (mProgressDialog == null) {
			mProgressDialog = new Dialog(this, com.wochu.adjustgoods.R.style.dialog_tran);
			View view = LayoutInflater.from(this).inflate(com.wochu.adjustgoods.R.layout.progress_dialog, null);
			WindowManager.LayoutParams params = getWindow().getAttributes();
			params.height = ViewGroup.LayoutParams.MATCH_PARENT;
			params.width = ViewGroup.LayoutParams.MATCH_PARENT;
			//getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
			mProgressDialog.setCanceledOnTouchOutside(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.addContentView(view, params);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
	}
	/**
	 * 判断进度条是否已显示
	 * @return
	 */
	public boolean isDialogShowing() {
		return mProgressDialog != null && mProgressDialog.isShowing();
	}
	/**
	 * 隐藏进度条
	 */
	public void hideLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}
	/****
	 * 公用跳转方法
	 */
	public void intentTo(Context packageContext, Class<?> cls) {
		Intent i = new Intent();
		i.setClass(packageContext, cls);
		startActivity(i);
	}
}
