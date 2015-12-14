package com.wochu.adjustgoods.bean;
import java.util.ArrayList;
//订单详情bean类
public class OrderDetail_picking {
	public String MESSAGE;//返回的消息
	public String RESULT;//返回的结果
	public int EntityState;
	public Boolean Selected;
	public ArrayList<GoodsBean> DATA;//返回的订单中所包含商品的集合
}


