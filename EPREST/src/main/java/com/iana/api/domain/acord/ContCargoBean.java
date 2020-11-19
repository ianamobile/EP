/*
 *  File		: ContCargoBean.java
 *  Author		: Ashok Soni
 *  Created		: June 22,2006
 *  Description	: Bean for Contingent Cargo Policy
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class ContCargoBean extends PolicyMasterBean implements IAPolicyBeanInterface
{

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
	
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer();
		
		super.toString();
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" pDetAttr1[").append(this.pDetAttr1).append("]");
		sbTemp.append(" pDetAttr2[").append(this.pDetAttr2).append("]");
		sbTemp.append(" pDetAttr3[").append(this.pDetAttr3).append("]");
		
		return sbTemp.toString();
	}
	
}
