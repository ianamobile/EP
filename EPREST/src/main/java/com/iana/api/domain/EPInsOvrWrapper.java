/*
 *  File		: EPInsOvrWrapperBean.java
 *  Author		: Ashok Soni
 *  Created		: June 24,2006
 *  Description	: Wrapper Bean which will have various other beans and will 
 *  			  be sent to Uvalid. It will have SwitchesBean,EPInsNeeds
 *  			  JoinRecordBean,Overridebean,AdditionalReq,Multiple Limits bean
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */

package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

public class EPInsOvrWrapper {
	private String epAcctNo="";
	private EPSwitches epSwitches = new EPSwitches(); //EP Switches
	private EPInsNeeds epNeeds= new EPInsNeeds() ; // Insurance details for EP
	private ArrayList policyMulLimits = new ArrayList(); //Multiple limits will be an ArrayList for each Type..  
	private List<AdditionalReq> addReq = new ArrayList<AdditionalReq>(); //Additional Req for EP
	private OverrideNeeds epOvrMCBean = new OverrideNeeds(); //Overrides given on Insurance to MC
	private ArrayList addReqOvrMC=new ArrayList(); //Additional Req Override given by EP to MC
	private String elFlag ="";   // This flag will be used whether to call Employee Liability method or not...
	private int templateId=0; //This Template id will be used to get Member Specific Details on EP Change
	
	public ArrayList getAddReqOvrMC() {
		return addReqOvrMC;
	}
	public void setAddReqOvrMC(ArrayList addReqOvrMC) {
		this.addReqOvrMC = addReqOvrMC;
	}
	public EPInsNeeds getEpNeeds() {
		return epNeeds;
	}
	public void setEpNeeds(EPInsNeeds epNeeds) {
		this.epNeeds = epNeeds;
	}
	public EPSwitches getEpSwitches() {
		return epSwitches;
	}
	public void setEpSwitches(EPSwitches epSwitches) {
		this.epSwitches = epSwitches;
	}
	
	public OverrideNeeds getEpOvrMCBean() {
		return epOvrMCBean;
	}
	public void setEpOvrMCBean(OverrideNeeds epOvrMCBean) {
		this.epOvrMCBean = epOvrMCBean;
	}
	public ArrayList getPolicyMulLimits() {
		return policyMulLimits;
	}
	public void setPolicyMulLimits(ArrayList policyMulLimits) {
		this.policyMulLimits = policyMulLimits;
	}
	public String getEpAcctNo() {
		return epAcctNo;
	}
	public void setEpAcctNo(String epAcctNo) {
		this.epAcctNo = epAcctNo;
	}

	
	public List<AdditionalReq> getAddReq() {
		return addReq;
	}
	public void setAddReq(List<AdditionalReq> addReq) {
		this.addReq = addReq;
	}
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer(this.getClass().getName());
		sbTemp.append("templateId[").append(this.templateId).append("]") ;
		sbTemp.append("epAcctNo[").append(this.epAcctNo).append("]") ;
		sbTemp.append("epSwitches[").append(this.epSwitches).append("]") ;
		sbTemp.append("Ins Needs[").append(this.epNeeds).append("]") ;
		sbTemp.append("policyMulLimits[").append(this.policyMulLimits).append("]");
		sbTemp.append("addReq[").append(this.addReq).append("]");
		sbTemp.append("epOvrMCBean[").append(this.epOvrMCBean).append("]");
		sbTemp.append("addReqOvrMC[").append(this.addReqOvrMC).append("]");
		
		return sbTemp.toString();
	}
	public void initForEPChange()
	{
		/*For Initializing some of the properties for UValid*/
		this.epNeeds= new EPInsNeeds();
		this.policyMulLimits=new ArrayList();
		this.epOvrMCBean=new OverrideNeeds();
		this.addReqOvrMC=new ArrayList();
	}
	public void initForMCChange()
	{
		/*For Initializing some of the properties for UValid*/
		/*this.epAcctNo="";
		this.epSwitches=new EPSwitches();
		this.addReq=new ArrayList();*/
		this.epNeeds= new EPInsNeeds();
		this.policyMulLimits=new ArrayList();
		this.epOvrMCBean=new OverrideNeeds();
		this.addReqOvrMC=new ArrayList();
	}
	public String getElFlag() {
		return elFlag;
	}
	public void setElFlag(String elFlag) {
		this.elFlag = elFlag;
	}
	public int getTemplateId() {
		return templateId;
	}
	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}
	

}
