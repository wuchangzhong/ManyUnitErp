package com.wochu.adjustgoods.bean;

import java.util.ArrayList;

//订单详情bean类
public class OrderDetail {

	public String RESULT;
	
	public ArrayList<OrderInfo> DATA;
	public class OrderInfo {
		public String DETAILNO;
		public String ORDERNO;
		public String BOXNO;
		public int QTY ;
		public int PQTY;
		public String ITEMNAME;
		public String ITEMCODE;
		public String BARCODE;
		public String EXLOCATION;
	}
	

	
	
	
	
	
	
	
	
}
