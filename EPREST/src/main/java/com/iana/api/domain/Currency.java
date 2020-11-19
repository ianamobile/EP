/*
 *  File		: CurrencyBean.java
 *  Author		: Ashok Soni
 *  Created		: June 26,2006
 *  Description	: This bean will be used for Currency coversion Details
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain;

public class Currency 
{
	private int currId=0;
	private String fromCurrency="";
	private String toCurrency="";
	private double rate=0.0;
	
	
	public int getCurrId() {
		return currId;
	}
	public void setCurrId(int currId) {
		this.currId = currId;
	}
	public String getFromCurrency() {
		return fromCurrency;
	}
	public void setFromCurrency(String fromCurrency) {
		this.fromCurrency = fromCurrency;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public String getToCurrency() {
		return toCurrency;
	}
	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer(this.getClass().getName());
		sbTemp.append("currId[").append(this.currId).append("]") ;
		sbTemp.append("fromCurrency[").append(this.fromCurrency).append("]") ;
		sbTemp.append("toCurrency[").append(this.toCurrency).append("]") ;
		sbTemp.append("rate[").append(this.rate).append("]") ;
		return sbTemp.toString();
	}
}
