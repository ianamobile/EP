/*
 *  File		: ELBean.java
 *  Author		: Ashok Soni
 *  Created		: June 16,2006
 *  Description	: Bean for Employee Liability Policy
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class ELBean extends PolicyMasterBean implements IAPolicyBeanInterface
{
	private String elEachOccur="0";
	private String elDisEAEmp="0";
	private String elDisPlcyLmt="0";
	private String unlmtdElLimits="";
	private String elExempt="";
	private String pDetAttr1 = "";
	private String pDetAttr2 = "";
	private String pDetAttr3 = "";
	private String wcELFlag = "";
	
	
	public String getWcELFlag() {
		return wcELFlag;
	}
	public void setWcELFlag(String wcELFlag) {
		this.wcELFlag = wcELFlag;
	}
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
	public void setElDisEAEmp(String elDisEAEmp) {
		this.elDisEAEmp = elDisEAEmp;
	}
	public String getElDisPlcyLmt() {
		return elDisPlcyLmt;
	}
	public void setElDisPlcyLmt(String elDisPlcyLmt) {
		this.elDisPlcyLmt = elDisPlcyLmt;
	}
	public String getElEachOccur() {
		return elEachOccur;
	}
	public void setElEachOccur(String elEachOccur) {
		this.elEachOccur = elEachOccur;
	}
	public String getElExempt() {
		return elExempt;
	}
	public void setElExempt(String elExempt) {
		this.elExempt = elExempt;
	}

	@Override
	public String toString() {
		return "ELBean [elEachOccur=" + elEachOccur + ", elDisEAEmp="
				+ elDisEAEmp + ", elDisPlcyLmt=" + elDisPlcyLmt
				+ ", unlmtdElLimits=" + unlmtdElLimits + ", elExempt="
				+ elExempt + ", pDetAttr1=" + pDetAttr1 + ", pDetAttr2="
				+ pDetAttr2 + ", pDetAttr3=" + pDetAttr3 + ", wcELFlag="
				+ wcELFlag + ", getPolicyDetId()=" + getPolicyDetId()
				+ ", getPolicyMstId()=" + getPolicyMstId() + "]";
	}
	public String getUnlmtdElLimits() {
		return unlmtdElLimits;
	}
	public void setUnlmtdElLimits(String unlmtdElLimits) {
		this.unlmtdElLimits = unlmtdElLimits;
	}


}
