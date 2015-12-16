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
	public static final String GetGoodInformations = "http://116.228.118.218:10098/InventoryAdjustment/GetLocationInventory?LocCode=";
	//库存盘点商品信息提交地址
	public static final String PostGoodInformations = "http://116.228.118.218:10098/InventoryAdjustment/UpdateLocationInventory?UserId=";
	//库存盘点提交作废商品地址
	public static final String PostInvalidGood = "http://116.228.118.218:10098/InventoryAdjustment/UpdateLocationInventoryStatus?";
	//库存盘点作废区地址
	public static final String InvalidGoodInformations="http://116.228.118.218:10098/InventoryAdjustment/GetLocationInventory?";
	/**
	 * 获取作废商品
	 * @param locatonCode
	 * @param status
	 * @return
	 */
	public static String getInvalidGoodInformations(String locatonCode,int status){
		StringBuilder sb = new StringBuilder();
		sb.append(InvalidGoodInformations);
		sb.append("LocCode=");
		sb.append(locatonCode+"&");
		sb.append("status=");
		sb.append(status);
		return sb.toString();
	}
	/**
	 * 提交作废商品
	 * @param LocCode
	 * @param BatchCode
	 * @param status
	 * @return
	 */
	public static String getPostInvalidGood(String LocCode,String BatchCode,int status){
		StringBuilder sb = new StringBuilder();
		sb.append(PostInvalidGood);
		sb.append("LocCode=");
		sb.append(LocCode+"&");
		sb.append("BatchCode=");
		sb.append(BatchCode+"&");
		sb.append("status=");
		sb.append(status);
		return sb.toString();
	}
}
