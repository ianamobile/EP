package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

public class EPAddendum {

	private String accountNo = "";
	private EPSwitches epSwitches = new EPSwitches();
	private List<EPInsNeeds> epNeeds = new ArrayList<EPInsNeeds>();
	private List<MultipleLimit> multiLimits = new ArrayList<MultipleLimit>();
	private List<AdditionalReq> addReq = new ArrayList<AdditionalReq>();
	private String effDate = "";
	private String templateStatus = "";// values can be 'A', 'W' or 'N'
	private int templateID = 0;

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public EPSwitches getEpSwitches() {
		return epSwitches;
	}

	public void setEpSwitches(EPSwitches epSwitches) {
		this.epSwitches = epSwitches;
	}

	public List<EPInsNeeds> getEpNeeds() {
		return epNeeds;
	}

	public void setEpNeeds(List<EPInsNeeds> epNeeds) {
		this.epNeeds = epNeeds;
	}

	public List<MultipleLimit> getMultiLimits() {
		return multiLimits;
	}

	public void setMultiLimits(List<MultipleLimit> multiLimits) {
		this.multiLimits = multiLimits;
	}

	public List<AdditionalReq> getAddReq() {
		return addReq;
	}

	public void setAddReq(List<AdditionalReq> addReq) {
		this.addReq = addReq;
	}

	public String getEffDate() {
		return effDate;
	}

	public void setEffDate(String effDate) {
		this.effDate = effDate;
	}

	public String getTemplateStatus() {
		return templateStatus;
	}

	public void setTemplateStatus(String templateStatus) {
		this.templateStatus = templateStatus;
	}

	public int getTemplateID() {
		return templateID;
	}

	public void setTemplateID(int templateID) {
		this.templateID = templateID;
	}

	public String toString() {
		StringBuffer sbTemp = new StringBuffer();

		sbTemp.append(this.getClass().getName());
		sbTemp.append(" epSwitches[");
		sbTemp.append(this.epSwitches);
		sbTemp.append("] epNeeds[");
		sbTemp.append(this.epNeeds);
		sbTemp.append("] mutliLimits[");
		sbTemp.append(this.multiLimits);
		sbTemp.append("] addReq[");
		sbTemp.append(this.addReq);
		sbTemp.append("] effDate[");
		sbTemp.append(this.effDate);
		sbTemp.append("]");
		sbTemp.append(" templateID[");
		sbTemp.append(this.templateID);
		sbTemp.append("]");
		sbTemp.append(" templateStatus[");
		sbTemp.append(this.templateStatus);
		sbTemp.append("]");

		return sbTemp.toString();
	}

}
