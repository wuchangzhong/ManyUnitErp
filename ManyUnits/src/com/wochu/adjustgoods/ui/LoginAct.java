package com.wochu.adjustgoods.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.squareup.okhttp.Request;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.UserInfo;
import com.wochu.adjustgoods.net.OkHttpClientManager;
import com.wochu.adjustgoods.net.OkHttpClientManager.ResultCallback;
import com.wochu.adjustgoods.url.AppClient;
import com.wochu.adjustgoods.utils.LogUtil;
import com.wochu.adjustgoods.utils.SharePreUtil;
public class LoginAct extends Activity implements OnClickListener{
	private Context mcontext;
	private EditText et_username;
	private EditText et_password;
	private Button btn_login;
	private Button btn_equit;
	private String userName;
	private String password;
	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_login);
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_equit = (Button) findViewById(R.id.btn_equit);
		btn_login.setOnClickListener(this);
		btn_equit.setOnClickListener(this);
		mcontext =this;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			userName = et_username.getText().toString();
			password = et_password.getText().toString();
			boolean loginChecked = LoginChecked(userName,password); 
			if(loginChecked){
				login(userName,password);//开始登录
			}
			break;
		case R.id.btn_equit:
			finish();
			break;
		default:
			break;
		}
	}
	/**
	 * 检验登陆信息
	 * @param userName2
	 * @param password2
	 */
	private boolean LoginChecked(String userName1, String password1) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(userName1)){
			Toast.makeText(getBaseContext(),"工号不能为空",0).show();
			return false;
		}
		if(TextUtils.isEmpty(password1)){
			Toast.makeText(getBaseContext(),"密码不能为空",0).show();
			return false;
		}
		return true;
	}
	/**
	 * 登录操作
	 * @param username
	 * @param password
	 */
	private void login(final String username, final String password){
		
		OkHttpClientManager.getAsyn(AppClient.loginUrl(username, password), new ResultCallback<UserInfo>() {
			@Override
			public void onError(Request request, Exception e) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),"登录失败",0).show();
					}
					@Override
			 public void onResponse(UserInfo response) {
						// TODO Auto-generated method stub
				if ("success".equals(response.RESULT)) {
							Toast.makeText(getBaseContext(), response.MESSAGE,0).show();
							LogUtil.e("LoginAct", response.MESSAGE);
							SharePreUtil.putInteger(mcontext, "userID",response.DATA.USERID);// 保存工人的账号
							toMainAct();
						} else {
							Toast.makeText(getBaseContext(), "登录失败", 0).show();
						}
					}
				});
	}
	@SuppressWarnings("unused")
	private void toMainAct() {
		if (intent == null) {
			intent = new Intent(this, MainAct.class);
		}
		startActivity(intent);
		finish();
	}
}
