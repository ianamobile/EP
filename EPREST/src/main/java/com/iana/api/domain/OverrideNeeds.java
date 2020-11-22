/*
 *  File		: OverrideNeedsBean.java
 *  Author		: Ashok Soni
 *  Created		: June 09,2006
 *  Description	: This bean will handle MC Overrides (Only Insurance related)  
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain;


public class OverrideNeeds {
	private int ovrNeedId=0;
	private String policyType = "";
	private String policyReq = "";
	private String limitBooster = ""; // Min Limit in database
	private String dedBooster = "";   //Max ded in database
	private String addInsReq = "";
	private String selfInsReq = "";
	private String minBestRat = "";
	private String rrgAllwd = "";
	private String spcInsAllwd=""; //special insurance allowed
	private int attr1=0;
	private String attr2="";
	private String attr3="";
	private String effDate="";
	private String mulPresent="";
	private String remarks="";
	
	private String ELABooster="";
	private String ELEBooster="";
	private String ELPBooster="";
	
	public String getELABooster() {
		return ELABooster;
	}
	public void setELABooster(String booster) {
		ELABooster = booster;
	}
	public String getELEBooster() {
		return ELEBooster;
	}
	public void setELEBooster(String booster) {
		ELEBooster = booster;
	}
	public String getELPBooster() {
		return ELPBooster;
	}
	public void setELPBooster(String booster) {
		ELPBooster = booster;
	}
	public String getMulPresent() {
		return mulPresent;
	}
	public void setMulPresent(String mulPresent) {
		this.mulPresent = mulPresent;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getAddInsReq() {
		return addInsReq;
	}
	public void setAddInsReq(String addInsReq) {
		this.addInsReq = addInsReq;
	}
	public int getAttr1() {
		return attr1;
	}
	public void setAttr1(int attr1) {
		this.attr1 = attr1;
	}
	public String getAttr2() {
		return attr2;
	}
	public void setAttr2(String attr2) {
		this.attr2 = attr2;
	}
	public String getAttr3() {
		return attr3;
	}
	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}
	public String getDedBooster() {
		return dedBooster;
	}
	public void setDedBooster(String dedBooster) {
		this.dedBooster = dedBooster;
	}
	public String getEffDate() {
		return effDate;
	}
	public void setEffDate(String effDate) {
		this.effDate = effDate;
	}

	
	public int getOvrNeedId() {
		return ovrNeedId;
	}
	public void setOvrNeedId(int ovrNeedId) {
		this.ovrNeedId = ovrNeedId;
	}
	public String getLimitBooster() {
		return limitBooster;
	}
	public void setLimitBooster(String limitBooster) {
		this.limitBooster = limitBooster;
	}
	public String getMinBestRat() {
		return minBestRat;
	}
	public void setMinBestRat(String minBestRat) {
		this.minBestRat = minBestRat;
	}
	public String getPolicyReq() {
		return policyReq;
	}
	public void setPolicyReq(String policyReq) {
		this.policyReq = policyReq;
	}
	public String getPolicyType() {
		return policyType;
	}
	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}
	public String getRrgAllwd() {
		return rrgAllwd;
	}
	public void setRrgAllwd(String rrgAllwd) {
		this.rrgAllwd = rrgAllwd;
	}
	public String getSelfInsReq() {
		return selfInsReq;
	}
	public void setSelfInsReq(String selfInsAllwd) {
		this.selfInsReq = selfInsAllwd;
	}
	public String getSpcInsAllwd() {
		return spcInsAllwd;
	}
	public void setSpcInsAllwd(String spcInsAllwd) {
		this.spcInsAllwd = spcInsAllwd;
	}
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer(this.getClass().getName());
		sbTemp.append("ovrNeedId[").append(this.ovrNeedId).append("]") ;
		sbTemp.append("policyType[").append(this.policyType).append("]") ;
		sbTemp.append("policyReq[").append(this.policyReq).append("]") ;
		sbTemp.append("limitBooster[").append(this.limitBooster).append("]") ;
		sbTemp.append("dedBooster[").append(this.dedBooster).append("]") ;
		sbTemp.append("addInsReq[").append(this.addInsReq).append("]") ;
		sbTemp.append("selfInsReq[").append(this.selfInsReq).append("]") ;
		sbTemp.append("spcInsAllwd[").append(this.spcInsAllwd).append("]") ;
		sbTemp.append("attr1[").append(this.attr1).append("]") ;
		sbTemp.append("attr2[").append(this.attr2).append("]") ;
		sbTemp.append("attr3[").append(this.attr3).append("]") ;
		sbTemp.append("effDate[").append(this.effDate).append("]") ;
		sbTemp.append("mulPresent[").append(this.mulPresent).append("]") ;
		sbTemp.append("remarks[").append(this.remarks).append("]") ;
		sbTemp.append("ELABooster[").append(this.ELABooster).append("]") ;
		sbTemp.append("ELEBooster[").append(this.ELEBooster).append("]") ;
		sbTemp.append("ELPBooster[").append(this.ELPBooster).append("]") ;
		return sbTemp.toString();
	}
	public boolean isEmpty()
	{
		if(this.limitBooster.length()== 0 && this.dedBooster.length()== 0 && this.selfInsReq.equals("") && this.rrgAllwd.equals("") )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
}
