package com.wochu.adjustgoods.bean;

import java.util.List;

public class GoodsQuery {
	public String MESSAGE;//返回信息
	public String RESULT;//返回结果
	public List<GoodsBean> DATA;//返回数据
	public class GoodsBean{
	public int LOCATIONID;
	public String GOODSNAME;//商品名称
	public int WAREHOUSEID;
	public int WOCGOODSID;
	public String CELLNO;
	public String DIRECTSELLINGGOODSCODE;
	public int OTHERGOODSID;
	public String GOODSCODE;//商品编码
	public String CREATEDATE;
	public String CREATER;
	public String EDITDATE;
	public int LOCATIONGOODSID;
	public String DIRECTSELLINGGOODSNAME;
	public String LOCATIONCODE;//商品货位号
	public int PROCESSGOODSID;
	public int GOODSID;
	public int DIRECTSELLINGGOODSID;
	public String WAREHOUSENAME;
	}
}
