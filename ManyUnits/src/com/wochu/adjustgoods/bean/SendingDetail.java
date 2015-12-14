package com.wochu.adjustgoods.bean;

import java.util.ArrayList;

public class SendingDetail {
public String RESULT;
public String MESSAGE;
public ArrayList<Info> DATA;

@Override
public String toString() {
	return "SendingDetail [RESULT=" + RESULT + ", MESSAGE=" + MESSAGE
			+ ", DATA=" + DATA + "]";
}

public class Info{
	
	public String SHEETID;
	public String CONSIGNEEADD;//收件地址
	public String CONSIGNEE;
	public String ORDERTIME;
	public String CHECKTIME;
	public String CUSTOMERID;
	public String EDITOR;
	public String MOBILE;//手机号
	public int OUTSHOPID;
	public String SITEID;//地区id
	public String SITENAME;//地区名
	public String CUSTOMERPAYTYPENAME;//支付方式
	public String RATIONDATE;//最佳配送日期
	public String RATIONTIMEPERIOD;//最佳配送时间
	public String TICKETNO;//票号
	public String SENDPORTID;
	
	@Override
	public String toString() {
		return "Info [SHEETID=" + SHEETID + ", CONSIGNEEADD=" + CONSIGNEEADD
				+ ", CONSIGNEE=" + CONSIGNEE + ", ORDERTIME=" + ORDERTIME
				+ ", CHECKTIME=" + CHECKTIME + ", CUSTOMERID=" + CUSTOMERID
				+ ", EDITOR=" + EDITOR + ", MOBILE=" + MOBILE + ", OUTSHOPID="
				+ OUTSHOPID + ", SITEID=" + SITEID + ", SITENAME=" + SITENAME
				+ ", CUSTOMERPAYTYPENAME=" + CUSTOMERPAYTYPENAME + "]";
	}
	
}
}
