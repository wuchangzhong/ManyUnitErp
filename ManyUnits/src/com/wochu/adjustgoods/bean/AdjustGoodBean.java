package com.wochu.adjustgoods.bean;

import java.io.Serializable;
import java.util.List;

public class AdjustGoodBean {
	public String MESSAGE;//返回来的信息
	public String RESULT;//返回来的结果
	public int EntityState;
	public Boolean Selected;
	public List<Goods> DATA;//返回来的商品集合
	public class Goods implements Serializable {
	public String UNITNAME;
	public String GOODSNAME;
	public int LOCATIONID;
	public int WAREHOUSEID;
	public Double INVQTY;
	public String CELLNO;
	public String STORAGEDATE;
	public int STATUS;
	public int EXPIRYDAYS;
	public String GOODSBATCHCODE;
	public String PRODUCTIONDATE;
	public String GOODSCODE;
	public String LOCATIONCODE;
	public int GOODSID;
	public int LOCATIONINVENTORYID;
	public Double QTY;
	public Double QTYchange;
	}
}
