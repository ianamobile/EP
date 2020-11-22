/*
 *  File		: UvldMemBean.java
 *  Author		: Ashok Soni
 *  Created		: Oct 14,2006
 *  Description	: Member Bean which will be used in Uvalid for calcualting 
 *  			  member specific status between MC & EP
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */
/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class UVldMemBean {

	private int memDetId=0;
	private String epReqMem="";
	private String mcIsMem="";
	private String cncl="";
	private String cnclDt="";
	private String attr1="";
	private String attr2="";
	
	
	
	public String getAttr1() {
		return attr1;
	}



	public void setAttr1(String attr1) {
		this.attr1 = attr1;
	}



	public String getAttr2() {
		return attr2;
	}



	public void setAttr2(String attr2) {
		this.attr2 = attr2;
	}



	public String getCncl() {
		return cncl;
	}



	public void setCncl(String cncl) {
		this.cncl = cncl;
	}



	public String getCnclDt() {
		return cnclDt;
	}



	public void setCnclDt(String cnclDt) {
		this.cnclDt = cnclDt;
	}



	public String getEpReqMem() {
		return epReqMem;
	}



	public void setEpReqMem(String epReqMem) {
		this.epReqMem = epReqMem;
	}



	public String getMcIsMem() {
		return mcIsMem;
	}



	public void setMcIsMem(String mcIsMem) {
		this.mcIsMem = mcIsMem;
	}



	public int getMemDetId() {
		return memDetId;
	}



	public void setMemDetId(int memDetId) {
		this.memDetId = memDetId;
	}



	public String toString()
	{
		StringBuffer sbTemp=new StringBuffer();
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" memDetId[").append(this.memDetId).append("]");
		sbTemp.append(" epReqMem[").append(this.epReqMem).append("]");
		sbTemp.append(" mcIsMem[").append(this.mcIsMem).append("]");
		sbTemp.append(" cncl[").append(this.cncl).append("]");
		sbTemp.append(" cnclDt[").append(this.cnclDt).append("]");
		sbTemp.append(" attr1[").append(this.attr1).append("]");
		sbTemp.append(" attr2[").append(this.attr2).append("]");
		return sbTemp.toString();
	}
}
