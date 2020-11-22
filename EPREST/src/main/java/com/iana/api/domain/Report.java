/*
 *  File		: MCEPJoinBean.java
 *  Author		: Ashok Soni
 *  Created		: June 11,2006
 *  Description	: This bean will be used for giving details related to 
 *  			  MC EP Join. 	
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain;

import java.util.Map;

public class Report 
{
	
	private String jrxml="";
	private String fileName="";
	private String type="";
	private String attr1="";
	private String attr2="";
	private String acctNo="";
	private String createdDate;
	private String createdDateTime = "";
	private Map<String, Object> parameterMap= null;
	
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer(this.getClass().getName());
		sbTemp.append("jrxml[").append(this.jrxml).append("]") ;
		sbTemp.append("fileName[").append(this.fileName).append("]") ;
		sbTemp.append("type[").append(this.type).append("]");
		sbTemp.append("attr1[").append(this.attr1).append("]");
		sbTemp.append("attr2[").append(this.attr2).append("]");
		sbTemp.append("acctNo[").append(this.acctNo).append("]");
		
		return sbTemp.toString();
	}


	public String getAcctNo() {
		return acctNo;
	}


	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
	}


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


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getJrxml() {
		return jrxml;
	}


	public void setJrxml(String jrxml) {
		this.jrxml = jrxml;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getCreatedDate() {
		return createdDate;
	}


	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedDateTime() {
		return createdDateTime;
	}
	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}
	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	
}
