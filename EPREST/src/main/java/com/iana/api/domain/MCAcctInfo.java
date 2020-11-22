/*
 *  File		: MCAcctInfoBean.java
 *  Author		: Ronak Bhavsar
 *  Created		: June 27,2006
 *  Description	: This bean will be used to get/insert/update MC account details like
 *  			  Company Name,SCAC Code, Account No 
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 142544
 *
 */
package com.iana.api.domain;

import java.util.ArrayList;

public class MCAcctInfo extends AccountMaster {
	private String stateAuthNo="";
	private String fedRegNo="";
	private String taxId="";
	private String vipFlag="";
	private String vip30Flag="";
	private String pswdIA="";
	private String rStatus="";
	private String attr1="";
	private String attr2="";
	private String attr3="";
	private String natureOfBusiness="";
	private String prmblRcvdFlag="";
	private String prmblRcvdDate="";
	private String mcUiiaRequestCancellationFlag ="";
	private String mcUiiaRequestCancellationDate ="";
	private int mcId=0;
	private ArrayList uiiaChkLst = new ArrayList();
	private ArrayList cancCodeLst = new ArrayList();
	private String isPending = "";
	
	//Added by Ankur Aug 2010
	private String hireFlag="N";
	private String privateFlag="N";
	private String interstateFlag="N";
	private String intrastateFlag="N";
	private String czTaoFlag="N";
	//Added by Ranajit Mar 2011 (ITR Enhancement Phase-II)
	private String smartwayCarrierFlag="N";
	private String waiverFlag="";

	public String getHireFlag() {
		return hireFlag;
	}
	
	public void setHireFlag(String hireFlag) {
		this.hireFlag = hireFlag;
	}
	public String getPrivateFlag() {
		return privateFlag;
	}
	public void setPrivateFlag(String privateFlag) {
		this.privateFlag = privateFlag;
	}
	public String getInterstateFlag() {
		return interstateFlag;
	}
	public void setInterstateFlag(String interstateFlag) {
		this.interstateFlag = interstateFlag;
	}
	public String getIntrastateFlag() {
		return intrastateFlag;
	}
	public void setIntrastateFlag(String intrastateFlag) {
		this.intrastateFlag = intrastateFlag;
	}
	public String getCzTaoFlag() {
		return czTaoFlag;
	}
	public void setCzTaoFlag(String czTaoFlag) {
		this.czTaoFlag = czTaoFlag;
	}
	
	//Added by Ankur Aug 2010
	
	public String getIsPending() {
		return isPending;
	}
	public void setIsPending(String isPending) {
		this.isPending = isPending;
	}
	public ArrayList getCancCodeLst() {
		return cancCodeLst;
	}
	public void setCancCodeLst(ArrayList cancCodeLst) {
		this.cancCodeLst = cancCodeLst;
	}
	public ArrayList getUiiaChkLst() {
		return uiiaChkLst;
	}
	public void setUiiaChkLst(ArrayList uiiaChkLst) {
		this.uiiaChkLst = uiiaChkLst;
	}
	public String getFedRegNo() {
		return fedRegNo;
	}
	public void setFedRegNo(String fedRegNo) {
		this.fedRegNo = fedRegNo;
	}
	public String getPswdIA() {
		return pswdIA;
	}
	public void setPswdIA(String pswdIA) {
		this.pswdIA = pswdIA;
	}
	public String getRStatus() {
		return rStatus;
	}
	public void setRStatus(String status) {
		rStatus = status;
	}
	public String getStateAuthNo() {
		return stateAuthNo;
	}
	public void setStateAuthNo(String stateAuthNo) {
		this.stateAuthNo = stateAuthNo;
	}
	public String getTaxId() {
		return taxId;
	}
	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}
	public String getVip30Flag() {
		return vip30Flag;
	}
	public void setVip30Flag(String vip30Flag) {
		this.vip30Flag = vip30Flag;
	}
	public String getVipFlag() {
		return vipFlag;
	}
	public void setVipFlag(String vipFlag) {
		this.vipFlag = vipFlag;
	}
	public String toString()
	{
		StringBuffer  sbTemp = new StringBuffer();
		
		sbTemp.append(super.toString());
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" stateAuthNo[");
		sbTemp.append(this.stateAuthNo);
		sbTemp.append("] fedRegNo[");
		sbTemp.append(this.fedRegNo);
		sbTemp.append("] taxId[");
		sbTemp.append(this.taxId);
		sbTemp.append("] vipFlag[");
		sbTemp.append(this.vipFlag);
		sbTemp.append("] vip30Flag[");
		sbTemp.append(this.vip30Flag);
		sbTemp.append("] pswdIA[");
		sbTemp.append(this.pswdIA);
		sbTemp.append("]rStatus[");
		sbTemp.append(this.rStatus);
		sbTemp.append("]mcUiiaRequestCancellationDate[");
		sbTemp.append(this.mcUiiaRequestCancellationDate);
		sbTemp.append("]mcUiiaRequestCancellationFlag[");
		sbTemp.append(this.mcUiiaRequestCancellationFlag);
		sbTemp.append("]prmblRcvdFlag[");
		sbTemp.append(this.prmblRcvdFlag);
		sbTemp.append("]prmblRcvdDate[");
		sbTemp.append(this.prmblRcvdDate);
		sbTemp.append("]");
		sbTemp.append(" uiiaChkLst[").append(this.uiiaChkLst).append("]");
		sbTemp.append(" cancCodeLst[").append(this.cancCodeLst).append("]");
		sbTemp.append(" isPending[").append(this.isPending).append("]");
		sbTemp.append(" hireFlag[").append(this.hireFlag).append("]");
		sbTemp.append(" privateFlag[").append(this.privateFlag).append("]");
		sbTemp.append(" interstateFlag[").append(this.interstateFlag).append("]");
		sbTemp.append(" intrastateFlag[").append(this.intrastateFlag).append("]");
		sbTemp.append(" czTaoFlag[").append(this.czTaoFlag).append("]");
		sbTemp.append(" smartwayCarrierFlag[").append(this.smartwayCarrierFlag).append("]");
		return sbTemp.toString();
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
	public String getAttr3() {
		return attr3;
	}
	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}
	public int getMcId() {
		return mcId;
	}
	public void setMcId(int mcId) {
		this.mcId = mcId;
	}
	public String getNatureOfBusiness() {
		return natureOfBusiness;
	}
	public void setNatureOfBusiness(String natureOfBusiness) {
		this.natureOfBusiness = natureOfBusiness;
	}
	public String getPrmblRcvdFlag() {
		return prmblRcvdFlag;
	}
	public void setPrmblRcvdFlag(String prmblRcvdFlag) {
		this.prmblRcvdFlag = prmblRcvdFlag;
	}
	public String getPrmblRcvdDate() {
		return prmblRcvdDate;
	}
	public void setPrmblRcvdDate(String prmblRcvdDate) {
		this.prmblRcvdDate = prmblRcvdDate;
	}
	//Added by Ranajit Mar 2011 (ITR Enhancement Phase-II)
	public String getSmartwayCarrierFlag() {
		return smartwayCarrierFlag;
	}
	public void setSmartwayCarrierFlag(String smartwayCarrierFlag) {
		this.smartwayCarrierFlag = smartwayCarrierFlag;
	}

	public String getWaiverFlag() {
		return waiverFlag;
	}

	public void setWaiverFlag(String waiverFlag) {
		this.waiverFlag = waiverFlag;
	}

	public String getMcUiiaRequestCancellationFlag() {
		return mcUiiaRequestCancellationFlag;
	}

	public void setMcUiiaRequestCancellationFlag(
			String mcUiiaRequestCancellationFlag) {
		this.mcUiiaRequestCancellationFlag = mcUiiaRequestCancellationFlag;
	}

	public String getMcUiiaRequestCancellationDate() {
		return mcUiiaRequestCancellationDate;
	}

	public void setMcUiiaRequestCancellationDate(
			String mcUiiaRequestCancellationDate) {
		this.mcUiiaRequestCancellationDate = mcUiiaRequestCancellationDate;
	}	
	
	

}
