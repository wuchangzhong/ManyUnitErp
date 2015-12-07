package com.wochu.adjustgoods.url;

public class AppClient {
	//登录地址
	public static final String LOGINSERVER = "http://58.247.11.228:8082/api/Account/LogOn?";
	public static String loginUrl(String userName,String password){
		StringBuilder sb = new StringBuilder();
		sb.append(LOGINSERVER);
		sb.append("userName=");
		sb.append(userName+"&");
		sb.append("password=");
		sb.append(password);
		return sb.toString();
	}
	//获取商品货位号地址
	public static final String LOCATIONURL="http://58.247.11.228:8088/LocationGoods/GetLocationInfo?GoodsCode=";
	//获取货位商品信息地址
	public static final String GetGoodInformations = "";
}
