package com.wochu.adjustgoods.net;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class MyHttpUtils {
	private HttpUtils http;
	public MyHttpUtils(){
		http = new HttpUtils(3000);
	}

	public void Get(String url){
		if(callBack!=null){
			
		http.send(HttpMethod.GET, url,callBack);
		}else{
			http.send(HttpMethod.GET, url,null);
		}
	}
	
	/**
	 * 提交请求体的POST
	 * @param url
	 * @param params
	 */
	public void POST(String url,RequestParams params){
		if(callBack!=null){
			http.send(HttpMethod.POST, url, params,callBack);
		}else{
			http.send(HttpMethod.POST, url, params,null);
		}
		
	}
	/**无请求体的POST请求
	 * 
	 * @param url
	 */
	public void POST(String url){
		if(callBack!=null){
			http.send(HttpMethod.POST, url, callBack);
		}else{
			http.send(HttpMethod.POST, url, null);
		}
	}
	public RequestCallBack<String> callBack ;
}
