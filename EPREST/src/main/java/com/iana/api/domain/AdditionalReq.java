/*
 *  File		: AdditionalReqBean.java
 *  Author		: Ashok Soni
 *  Created		: June 09,2006
 *  Description	: This bean will handle Additional Requriements
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain;

import java.io.File;

import com.iana.api.utils.GlobalVariables;

public class AdditionalReq {

	private int epAReqId = 0;
	private int mcAReqId = 0;
	private String endrsDesc = "";
	private String endrsCode = "";
	private String required = "";
	private String originalReq = "";
	private String reqInDays = "";
	private String epName = "";
	private String attr1 = "";
	private String attr2 = "";
	private String areqRcvDate = "";
	private String areqOriRcvDate = "";
	private String addReqPath = "";
	private int addendaPathId = 0;
	private File myRequestFile = null;
	private String myRequestFileContentType = "";
	private String myRequestFileFileName = "";
	private int nsChgId = 0;
	private String nonUIIAEpFlag = GlobalVariables.NO;

	public int getNsChgId() {
		return nsChgId;
	}

	public void setNsChgId(int nsChgId) {
		this.nsChgId = nsChgId;
	}

	public File getMyRequestFile() {
		return myRequestFile;
	}

	public void setMyRequestFile(File myRequestFile) {
		this.myRequestFile = myRequestFile;
	}

	public int getAddendaPathId() {
		return addendaPathId;
	}

	public void setAddendaPathId(int addendaPathId) {
		this.addendaPathId = addendaPathId;
	}

	public String getAddReqPath() {
		return addReqPath;
	}

	public void setAddReqPath(String addReqPath) {
		this.addReqPath = addReqPath;
	}

	public String getAreqOriRcvDate() {
		return areqOriRcvDate;
	}

	public void setAreqOriRcvDate(String areqOriRcvDate) {
		this.areqOriRcvDate = areqOriRcvDate;
	}

	public String getAreqRcvDate() {
		return areqRcvDate;
	}

	public void setAreqRcvDate(String areqRcvDate) {
		this.areqRcvDate = areqRcvDate;
	}

	public String getEpName() {
		return epName;
	}

	public void setEpName(String epName) {
		this.epName = epName;
	}

	public String getEndrsDesc() {
		return endrsDesc;
	}

	public void setEndrsDesc(String endrsDesc) {
		this.endrsDesc = endrsDesc;
	}

	public String getOriginalReq() {
		return originalReq;
	}

	public void setOriginalReq(String originalReq) {
		this.originalReq = originalReq;
	}

	public String getReqInDays() {
		return reqInDays;
	}

	public void setReqInDays(String reqInDays) {
		this.reqInDays = reqInDays;
	}

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
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

	public int getMcAReqId() {
		return mcAReqId;
	}

	public void setMcAReqId(int mcAReqId) {
		this.mcAReqId = mcAReqId;
	}

	public int getEpAReqId() {
		return epAReqId;
	}

	public void setEpAReqId(int epAReqId) {
		this.epAReqId = epAReqId;
	}

	public String getEndrsCode() {
		return endrsCode;
	}

	public void setEndrsCode(String endrsCode) {
		this.endrsCode = endrsCode;
	}

	public String getMyRequestFileContentType() {
		return myRequestFileContentType;
	}

	public void setMyRequestFileContentType(String myRequestFileContentType) {
		this.myRequestFileContentType = myRequestFileContentType;
	}

	public String getMyRequestFileFileName() {
		return myRequestFileFileName;
	}

	public void setMyRequestFileFileName(String myRequestFileFileName) {
		this.myRequestFileFileName = myRequestFileFileName;
	}

	public String getNonUIIAEpFlag() {

		return nonUIIAEpFlag;
	}

	public void setNonUIIAEpFlag(String nonUIIAEpFlag) {

		this.nonUIIAEpFlag = nonUIIAEpFlag;
	}

	public String toString() {
		StringBuffer sbTemp = new StringBuffer(this.getClass().getName());
		sbTemp.append("epAReqId[").append(this.epAReqId).append("]");
		sbTemp.append("mcAReqId[").append(this.mcAReqId).append("]");
		sbTemp.append("endrsDesc[").append(this.endrsDesc).append("]");
		sbTemp.append("endrsCode[").append(this.endrsCode).append("]");
		sbTemp.append("required[").append(this.required).append("]");
		sbTemp.append("originalReq[").append(this.originalReq).append("]");
		sbTemp.append("reqInDays[").append(this.reqInDays).append("]");
		sbTemp.append("epName[").append(this.epName).append("]");
		sbTemp.append("attr1[").append(this.attr1).append("]");
		sbTemp.append("attr2[").append(this.attr2).append("]");
		sbTemp.append("areqRcvDate[").append(this.areqRcvDate).append("]");
		sbTemp.append("areqOriRcvDate[").append(this.areqOriRcvDate).append("]");
		sbTemp.append(" addReqPath[").append(this.addReqPath).append("]");
		sbTemp.append(" addendaPathId[").append(this.addendaPathId).append("]");
		sbTemp.append(" nsChgId[").append(this.nsChgId).append("]");
		sbTemp.append(" nonUIIAEpFlag[").append(this.nonUIIAEpFlag).append("]");

		return sbTemp.toString();
	}

	public boolean isEmpty() {
		if (this.endrsDesc.equals("")) {
			return true;
		} else {
			return false;
		}
	}

}
