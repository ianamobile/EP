package com.iana.api.domain;

import com.iana.api.utils.GlobalVariables;

public class EPInsNeeds {

	private int epNeedsId = 0;
	private String policyType = "";
	private String policyReq = "N";
	private String minLimit = "";
	private String maxDed = "";
	private String addInsReq = "N";
	private String selfInsReq = "";
	private String minBestRat = "";
	private String rrgAllwd = "Y";
	private String spcInsAllwd = "Y";
	private int attr1 = 0;
	private String attr2 = "";
	private String attr3 = "";
	private String effDate = "";
	private String multiLimDedPresent = "";
	/* Added by ashok */
	private String ELA = "";
	private String ELE = "";
	private String ELP = "";

	public String getELA() {
		return ELA;
	}

	public void setELA(String ela) {
		ELA = ela;
	}

	public String getELE() {
		return ELE;
	}

	public void setELE(String ele) {
		ELE = ele;
	}

	public String getELP() {
		return ELP;
	}

	public void setELP(String elp) {
		ELP = elp;
	}

	public String getMultiLimDedPresent() {
		return multiLimDedPresent;
	}

	public void setMultiLimDedPresent(String multiLimDedPresent) {
		this.multiLimDedPresent = multiLimDedPresent;
	}

	public String getAddInsReq() {
		return addInsReq;
	}

	public void setAddInsReq(String addInsReq) {
		this.addInsReq = addInsReq;
	}

	public String getMaxDed() {
		return maxDed;
	}

	public void setMaxDed(String maxDed) {
		this.maxDed = maxDed;
	}

	public String getMinBestRat() {
		return minBestRat;
	}

	public void setMinBestRat(String minBestRat) {
		this.minBestRat = minBestRat;
	}

	public String getMinLimit() {
		return minLimit;
	}

	public void setMinLimit(String minLimit) {
		this.minLimit = minLimit;
	}

	public String getPolicyReq() {
		return policyReq;
	}

	public void setPolicyReq(String policyReq) {
		this.policyReq = policyReq;
	}

	public String getPolicyType() {
		return policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}

	public String getRrgAllwd() {
		return rrgAllwd;
	}

	public void setRrgAllwd(String rrgAllwd) {
		this.rrgAllwd = rrgAllwd;
	}

	public String getSelfInsReq() {
		return selfInsReq;
	}

	public void setSelfInsReq(String selfInsReq) {
		this.selfInsReq = selfInsReq;
	}

	public String getSpcInsAllwd() {
		return spcInsAllwd;
	}

	public void setSpcInsAllwd(String spcInsAllwd) {
		this.spcInsAllwd = spcInsAllwd;
	}

	public int getEpNeedsId() {
		return epNeedsId;
	}

	public void setEpNeedsId(int epNeedsId) {
		this.epNeedsId = epNeedsId;
	}

	public int getAttr1() {
		return attr1;
	}

	public void setAttr1(int attr1) {
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

	public String getEffDate() {
		return effDate;
	}

	public void setEffDate(String effDate) {
		this.effDate = effDate;
	}

	public String toString() {
		StringBuffer sbTemp = new StringBuffer();
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" epNeedsId[");
		sbTemp.append(this.epNeedsId);
		sbTemp.append("]");
		sbTemp.append(" policyType[");
		sbTemp.append(this.policyType);
		sbTemp.append("] policyReq[");
		sbTemp.append(this.policyReq);
		sbTemp.append("] minLimit[");
		sbTemp.append(this.minLimit);
		sbTemp.append("] maxDed[");
		sbTemp.append(this.maxDed);
		sbTemp.append("] addInsReq[");
		sbTemp.append(this.addInsReq);
		sbTemp.append("] selfInsReq[");
		sbTemp.append(this.selfInsReq);
		sbTemp.append("] minBestRat[");
		sbTemp.append(this.minBestRat);
		sbTemp.append("] rrgAllwd[");
		sbTemp.append(this.rrgAllwd);
		sbTemp.append("] spcInstAllwd[");
		sbTemp.append(this.spcInsAllwd);
		sbTemp.append("] attr1[");
		sbTemp.append(this.attr1);
		sbTemp.append("] attr2[");
		sbTemp.append(this.attr2);
		sbTemp.append("] attr3[");
		sbTemp.append(this.attr3);
		sbTemp.append("]");
		sbTemp.append(" multiLimDedPresent[").append(this.multiLimDedPresent).append("]");
		sbTemp.append(" ELA[").append(this.ELA).append("]");
		sbTemp.append(" ELP[").append(this.ELP).append("]");
		sbTemp.append(" ELE[").append(this.ELE).append("]");
		return sbTemp.toString();
	}

	public boolean isEmpty() {

		if ((this.policyReq.equals(GlobalVariables.NO))) {
			return true;
		} else {
			return false;
		}
	}
}
