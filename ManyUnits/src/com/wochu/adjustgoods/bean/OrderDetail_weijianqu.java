package com.wochu.adjustgoods.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderDetail_weijianqu {
	
	public String MESSAGE ;//返回的信息
	public String RESULT  ;//判断是否成功
	public int EntityState  ;
	public Boolean Selected  ;
	public List<DATA> DATA;//订单集合
	public class DATA implements Serializable  {
	public String OrderNO ;//订单号
	public ArrayList<GoodsBean> OrderList;//一个订单所包含的商品的集合
	}
}
