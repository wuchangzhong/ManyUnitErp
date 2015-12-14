package com.wochu.adjustgoods.receiver;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wochu.adjustgoods.utils.LogUtil;

public abstract class PDAReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String str = null;
		if (action.equals("com.android.scancontext")){
		// “前台输出”不打勾时，不会发送此Intent
		 str = intent.getStringExtra("Scan_context");
		} else if (action.equals("com.android.scanservice.scancontext")) {
 str = intent.getStringExtra("Scan_context");
		}

		//分发条码处理
		LogUtil.i("扫描结果", str);
		dispathCode(str);

	}
	/**
	 * 处理返回的广播
	 * @param code
	 */
	public abstract void dispathCode(String code);

}
