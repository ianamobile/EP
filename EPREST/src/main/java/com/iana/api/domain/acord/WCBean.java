/*
 *  File		: WCBean.java
 *  Author		: Ashok Soni
 *  Created		: June 16,2006
 *  Description	: Bean for Workers Compensation
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class WCBean extends PolicyMasterBean implements IAPolicyBeanInterface
{
private String wcStatLimits="";
private String elEachOccur="";
private String elDisEAEmp="";
private String elDisPlcyLmt="";
private String unlmtdElLimits="";
private String exempt="";
private String pDetAttr1 = "";
private String pDetAttr2 = "";
private String pDetAttr3 = "";
private String strUnUh = "";
private String strWCELflg = "";

public String getPDetAttr1() {
	return pDetAttr1;
}
public void setPDetAttr1(String detAttr1) {
	pDetAttr1 = detAttr1;
}
public String getPDetAttr2() {
	return pDetAttr2;
}
public void setPDetAttr2(String detAttr2) {
	pDetAttr2 = detAttr2;
}
public String getPDetAttr3() {
	return pDetAttr3;
}
public void setPDetAttr3(String detAttr3) {
	pDetAttr3 = detAttr3;
}

public String getElDisEAEmp() {
	return elDisEAEmp;
}
public String getElDisPlcyLmt() {
	return elDisPlcyLmt;
}
public void setElDisPlcyLmt(String elDisPlcyLmt) {
	this.elDisPlcyLmt = elDisPlcyLmt;
}
public void setElDisEAEmp(String elDisEAEmp) {
	this.elDisEAEmp = elDisEAEmp;
}
public String getElEachOccur() {
	return elEachOccur;
}
public void setElEachOccur(String elEachOccur) {
	this.elEachOccur = elEachOccur;
}
public String getExempt() {
	return exempt;
}
public void setExempt(String exempt) {
	this.exempt = exempt;
}

public String getUnlmtdElLimits() {
	return unlmtdElLimits;
}
public void setUnlmtdElLimits(String unlmtdElLimits) {
	this.unlmtdElLimits = unlmtdElLimits;
}
public String getWcStatLimits() {
	return wcStatLimits;
}
public void setWcStatLimits(String wcStatLimits) {
	this.wcStatLimits = wcStatLimits;
}

@Override
public String toString() {
	return "WCBean [wcStatLimits=" + wcStatLimits + ", elEachOccur="
			+ elEachOccur + ", elDisEAEmp=" + elDisEAEmp + ", elDisPlcyLmt="
			+ elDisPlcyLmt + ", unlmtdElLimits=" + unlmtdElLimits + ", exempt="
			+ exempt + ", pDetAttr1=" + pDetAttr1 + ", pDetAttr2=" + pDetAttr2
			+ ", pDetAttr3=" + pDetAttr3 + ", strUnUh=" + strUnUh
			+ ", strWCELflg=" + strWCELflg + ", getPolicyDetId()="
			+ getPolicyDetId() + ", getPolicyMstId()=" + getPolicyMstId() + "]";
}
public String getStrUnUh() {
	return strUnUh;
}
public void setStrUnUh(String strUnUh) {
	this.strUnUh = strUnUh;
}
public String getStrWCELflg() {
	return strWCELflg;
}
public void setStrWCELflg(String strWCELflg) {
	this.strWCELflg = strWCELflg;
}

}
