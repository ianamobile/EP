/*
 *  File		: AutoBean.java
 *  Author		: Ashok Soni
 *  Created		: June 16,2006
 *  Description	: Bean for Auto Liability Policy
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class AutoBean extends PolicyMasterBean implements IAPolicyBeanInterface
{ 
private String bdlyInjrdPerPerson="";
private String bdlyInjrdPerAccdnt="";
private String propDmgPerAccdnt="";
private String stdEndo="";  
private String frmMCS90="";
private String any = "";
private String allOwned = "";
private String nonOwned = "";
private String hired = "";
private String scheduled = "";
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

public String getAllOwned() {
	return allOwned;
}
public void setAllOwned(String allOwned) {
	this.allOwned = allOwned;
}
public String getAny() {
	return any;
}
public void setAny(String any) {
	this.any = any;
}
public String getBdlyInjrdPerAccdnt() {
	return bdlyInjrdPerAccdnt;
}
public void setBdlyInjrdPerAccdnt(String bdlyInjrdPerAccdnt) {
	this.bdlyInjrdPerAccdnt = bdlyInjrdPerAccdnt;
}
public String getBdlyInjrdPerPerson() {
	return bdlyInjrdPerPerson;
}
public void setBdlyInjrdPerPerson(String bdlyInjrdPerPerson) {
	this.bdlyInjrdPerPerson = bdlyInjrdPerPerson;
}
public String getFrmMCS90() {
	return frmMCS90;
}
public void setFrmMCS90(String frmMCS90) {
	this.frmMCS90 = frmMCS90;
}
public String getHired() {
	return hired;
}
public void setHired(String hired) {
	this.hired = hired;
}
public String getNonOwned() {
	return nonOwned;
}
public void setNonOwned(String nonOwned) {
	this.nonOwned = nonOwned;
}
public String getPropDmgPerAccdnt() {
	return propDmgPerAccdnt;
}
public void setPropDmgPerAccdnt(String propDmgPerAccdnt) {
	this.propDmgPerAccdnt = propDmgPerAccdnt;
}
public String getScheduled() {
	return scheduled;
}
public void setScheduled(String scheduled) {
	this.scheduled = scheduled;
}
public String getStdEndo() {
	return stdEndo;
}
public void setStdEndo(String stdEndo) {
	this.stdEndo = stdEndo;
}

public String toString()
{
	StringBuffer sbTemp = new StringBuffer();
	
	sbTemp.append(super.toString());
	sbTemp.append(this.getClass().getName());
	sbTemp.append(" bdlyInjrdPerPerson[").append(this.bdlyInjrdPerPerson).append("]");
	sbTemp.append(" bdlyInjrdPerAccdnt[").append(this.bdlyInjrdPerAccdnt).append("]");
	sbTemp.append(" propDmgPerAccdnt[").append(this.propDmgPerAccdnt).append("]");
	sbTemp.append(" stdEndo[").append(this.stdEndo).append("]");
	sbTemp.append(" frmMCS90[").append(this.frmMCS90).append("]");
	sbTemp.append(" any[").append(this.any).append("]");
	sbTemp.append(" allOwned[").append(this.allOwned).append("]");
	sbTemp.append(" nonOwned[").append(this.nonOwned).append("]");
	sbTemp.append(" hired[").append(this.hired).append("]");
	sbTemp.append(" scheduled[").append(this.scheduled).append("]");
	sbTemp.append(" pDetAttr1[").append(this.pDetAttr1).append("]");
	sbTemp.append(" pDetAttr2[").append(this.pDetAttr2).append("]");
	sbTemp.append(" pDetAttr3[").append(this.pDetAttr3).append("]");
	
	return sbTemp.toString();
}


}
