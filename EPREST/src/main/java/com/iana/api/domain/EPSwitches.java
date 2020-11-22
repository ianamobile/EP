package com.iana.api.domain;

public class EPSwitches {

	private int addendumId=0;
	private String memberSpecific="";
	private String knownAs="";
	private String rampDetReq="";
	private String blanketAllwd="Y";
	private String attr1="";
	private String attr2="";
	
	public int getAddendumId() {
		return addendumId;
	}
	public void setAddendumId(int addendumId) {
		this.addendumId = addendumId;
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
	public String getBlanketAllwd() {
		return blanketAllwd;
	}
	public void setBlanketAllwd(String blanketAllwd) {
		this.blanketAllwd = blanketAllwd;
	}
	public String getKnownAs() {
		return knownAs;
	}
	public void setKnownAs(String knownAs) {
		this.knownAs = knownAs;
	}
	public String getMemberSpecific() {
		return memberSpecific;
	}
	public void setMemberSpecific(String memberSpecific) {
		this.memberSpecific = memberSpecific;
	}
	public String getRampDetReq() {
		return rampDetReq;
	}
	public void setRampDetReq(String rampDetReq) {
		this.rampDetReq = rampDetReq;
	}
	
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer();
		
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" addendumId[");
		sbTemp.append(this.addendumId);
		sbTemp.append("]");
		sbTemp.append(" memberSpecific[");
		sbTemp.append(this.memberSpecific);
		sbTemp.append("],knownAs[");
		sbTemp.append(this.knownAs);
		sbTemp.append("],rampDetReq[");
		sbTemp.append(this.rampDetReq);
		sbTemp.append("] blanketAllwd[");
		sbTemp.append(this.blanketAllwd);
		sbTemp.append("] attr1[");
		sbTemp.append(this.attr1);
		sbTemp.append("] attr2[");
		sbTemp.append(this.attr2);
		sbTemp.append("]");
		
		return sbTemp.toString();
		
	}
}
