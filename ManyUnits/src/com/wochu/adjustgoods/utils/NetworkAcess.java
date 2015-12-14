package com.wochu.adjustgoods.utils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public abstract class NetworkAcess {
	
	public NetworkAcess() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * 创建请求对象
	 */
	public static HttpUtils http;
	/**
	 * 访问网络
	 * @param http
	 */
	public void accessHttp(String url){
		if(http==null){
			http = new HttpUtils(2000);
		}
		http.send(HttpMethod.GET, url,
				new RequestCallBack<String>() {

					@Override
				public void onFailure(HttpException e, String arg1) {
					// TODO Auto-generated method stub
					e.printStackTrace();
					accessFailure(arg1);
				}
					
				@Override
				public void onSuccess(ResponseInfo<String> response) {
					accessSuccess(response);
				}

			});
	}
	public abstract void accessFailure(String response);
	public abstract void accessSuccess(ResponseInfo<String> response);
}
