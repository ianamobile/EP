/*
 *  File		: GenBean.java
 *  Author		: Ashok Soni
 *  Created		: June 16,2006
 *  Description	: Bean for General Liability Policy
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class GenBean extends PolicyMasterBean implements IAPolicyBeanInterface
{
private String claims="";
private String occurence="";
private String dmgRntdPremises="";
private String medExpenses="";
private String prsnlAdvInj="";
private String genAgg="";
private String products="";
private String pDetAttr1 = "";
private String pDetAttr2 = "";
private String pDetAttr3 = "";
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
public String getClaims() {
	return claims;
}
public void setClaims(String claims) {
	this.claims = claims;
}
public String getDmgRntdPremises() {
	return dmgRntdPremises;
}
public void setDmgRntdPremises(String dmgRntdPremises) {
	this.dmgRntdPremises = dmgRntdPremises;
}

public String getGenAgg() {
	return genAgg;
}
public void setGenAgg(String genAgg) {
	this.genAgg = genAgg;
}
public String getMedExpenses() {
	return medExpenses;
}
public void setMedExpenses(String medExpenses) {
	this.medExpenses = medExpenses;
}
public String getOccurence() {
	return occurence;
}
public void setOccurence(String occurence) {
	this.occurence = occurence;
}
public String getProducts() {
	return products;
}
public void setProducts(String products) {
	this.products = products;
}
public String getPrsnlAdvInj() {
	return prsnlAdvInj;
}
public void setPrsnlAdvInj(String prsnlAdvInj) {
	this.prsnlAdvInj = prsnlAdvInj;
}

public String toString()
{
	StringBuffer sbTemp = new StringBuffer();
	
	
	sbTemp.append(super.toString());
	sbTemp.append(this.getClass().getName());
	sbTemp.append(" claims[").append(this.claims).append("]");
	sbTemp.append(" occurence[").append(this.occurence).append("]");
	sbTemp.append(" dmgRntdPremises[").append(this.dmgRntdPremises).append("]");
	sbTemp.append(" medExpenses[").append(this.medExpenses).append("]");
	sbTemp.append(" prsnlAdvInj[").append(this.prsnlAdvInj).append("]");
	sbTemp.append(" genAgg[").append(this.genAgg).append("]");
	sbTemp.append(" products[").append(this.products).append("]");
	sbTemp.append(" pDetAttr1[").append(this.pDetAttr1).append("]");
	sbTemp.append(" pDetAttr2[").append(this.pDetAttr2).append("]");
	sbTemp.append(" pDetAttr3[").append(this.pDetAttr3).append("]");
	
	return sbTemp.toString();
}

}
