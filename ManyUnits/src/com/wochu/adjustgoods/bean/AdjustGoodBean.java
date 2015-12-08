package com.wochu.adjustgoods.bean;

import java.io.Serializable;
import java.util.List;

public class AdjustGoodBean {
	public String MESSAGE  ;
	public String RESULT  ;
	public int EntityState  ;
	public Boolean Selected  ;
	public List<Gooods> DATA;
	public class Gooods implements Serializable  {
	public String UNITNAME  ;
	public String GOODSNAME  ;
	public int LOCATIONID  ;
	public String PURUNITNAME  ;
	public Double INVQTY  ;
	public int WAREHOUSEID  ;
	public String CELLNO  ;
	public String STORAGEDATE  ;
	public int STATUS  ;
	public int EXPIRYDAYS  ;
	public Double PURUNITQTY  ;
	public String GOODSBATCHCODE  ;
	public String PRODUCTIONDATE  ;
	public String GOODSCODE  ;
	public String CREATER  ;
	public String LOCATIONCODE  ;
	public int GOODSID  ;
	public Double QTY  ;
	public int LOCATIONINVENTORYID  ;
	public Double QTYchange  ;
	public Double BuyQty  ;
	public Double StoreQty  ;
	}
}
