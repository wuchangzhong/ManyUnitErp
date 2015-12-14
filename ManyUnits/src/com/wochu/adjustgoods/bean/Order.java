package com.wochu.adjustgoods.bean;

public class Order {

	private String orderCode;
	private boolean isTransferBind;
	private boolean isSendBoxBind;
	private String transferCode;
	private String sendBoxCode;
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public boolean isTransferBind() {
		return isTransferBind;
	}
	public void setTransferBind(boolean isTransferBind) {
		this.isTransferBind = isTransferBind;
	}
	public boolean isSendBoxBind() {
		return isSendBoxBind;
	}
	public void setSendBoxBind(boolean isSendBoxBind) {
		this.isSendBoxBind = isSendBoxBind;
	}
	public String getTransferCode() {
		return transferCode;
	}
	public void setTransferCode(String transferCode) {
		this.transferCode = transferCode;
	}
	public String getSendBoxCode() {
		return sendBoxCode;
	}
	public void setSendBoxCode(String sendBoxCode) {
		this.sendBoxCode = sendBoxCode;
	}
	
}
