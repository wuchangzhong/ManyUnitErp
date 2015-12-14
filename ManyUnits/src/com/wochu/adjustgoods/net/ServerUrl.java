package com.wochu.adjustgoods.net;

public class ServerUrl {

	//登录地址
	public static final String LOGINSERVER = "http://116.228.118.218:8088/api/Account/LogOn?";
	//订单详情地址
	public static final String GETORDERSERVER = "http://116.228.118.218:8088/api/OrderPick/GetReviewInfo?BoxNO=";
	//发货箱状态地址
	public static final String SENDBOXSERVER = "http://116.228.118.218:8088/api/OrderPick/GetOrderUserInfo?FBoxNO=";
	public static final String GETORDERSERVER_TEST = "http://58.247.11.228:8082/api/OrderPick/GetReviewInfo?BoxNO=";
	//提交发货地址
	public static final String BINDSENDBOX = "http://116.228.118.218:8088/api/WCS_TO_WMS/SendWMStoWCStask/";
	//提交错误订单地址
	public static final String COMITERRORORDER = "http://116.228.118.218:8088/api/WCS_TO_WMS/SendReCheckExcep/";
	//称重数据的地址

	public static final String WINTINGURL="http://58.247.11.228:8099/weigh/getgoods";
	public static final String SEND_RECHECK = "http://58.247.11.228:8088/WCS_TO_WMS/SendReCheckExcep";
	//获取订单中的商品信息的地址
	public static final String ORDERDETAILS="http://58.247.11.228:8088/WCS_TO_WMS/GetAPPPickOrderInfoByOrderNO?";
	//未拣区访问网络
	public static final String WEIJIANQU="http://58.247.11.228:8088/WCS_TO_WMS/GetUserAppPickInfo?";
	//成品拣货保存的地址
	public static final String BAOCHUN="http://58.247.11.228:8088/WCS_TO_WMS/AppSaveWMStoWCStask";
	//成品拣货提交的地址
	public static final String TIJIAO="http://58.247.11.228:8088/WCS_TO_WMS/AppSendWMStoWCStask";
	public static String loginUrl(String userName,String password){
		StringBuilder sb = new StringBuilder();
		sb.append(LOGINSERVER);
		sb.append("userName=");
		sb.append(userName+"&");
		sb.append("password=");
		sb.append(password);
		return sb.toString();
	}
	public static String WeiJianQu(String userID,String time){
		StringBuilder sb = new StringBuilder();
		sb.append(WEIJIANQU);
		sb.append("UserId=");
		sb.append(userID+"&");
		sb.append("Time=");
		sb.append(time);
		return sb.toString();
	}
	public static String pickingfinishedUrl(String orderNumber,String userID,String userCode){
		StringBuilder sb = new StringBuilder();
		sb.append(ORDERDETAILS);
		sb.append("OrderNO=");
		sb.append(orderNumber+"&");
		sb.append("UserID=");
		sb.append(userID+"&");
		sb.append("UserName=");
		sb.append(userCode);
		return sb.toString();
	}
}