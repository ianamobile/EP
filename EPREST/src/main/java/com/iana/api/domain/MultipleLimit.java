package com.iana.api.domain;

public class MultipleLimit {
	
	private int multiLimId=0; 
	private String policyType = "";
	private String minLimit = "";
	private String maxDed = "";
	private String recType = "";
	private int attr1 = 0;
	private String attr2 = "";
	private String attr3 = "";
	boolean selectedBox;
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
	
	public String getMaxDed() {
		return maxDed;
	}
	public void setMaxDed(String maxDed) {
		this.maxDed = maxDed;
	}
	public String getMinLimit() {
		return minLimit;
	}
	public void setMinLimit(String minLimit) {
		this.minLimit = minLimit;
	}
	public String getPolicyType() {
		return policyType;
	}
	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}
	public int getMultiLimId() {
		return multiLimId;
	}
	public void setMultiLimId(int multiLimId) {
		this.multiLimId = multiLimId;
	}
	public String getRecType() {
		return recType;
	}
	public void setRecType(String recType) {
		this.recType = recType;
	}
	
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer();
		
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" multiLimId[");
		sbTemp.append(this.multiLimId);
		sbTemp.append("]");
		sbTemp.append(" policyType[");
		sbTemp.append(this.policyType);
		sbTemp.append("] minLimit[");
		sbTemp.append(this.minLimit);
		sbTemp.append("] maxDed[");
		sbTemp.append(this.maxDed);
		sbTemp.append("] recType[");
		sbTemp.append(this.recType);
		sbTemp.append("]");
		
		return sbTemp.toString();
	}
	public boolean isEmpty()
	{
		if(this.policyType.equals("") )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean isSelectedBox() {
		return selectedBox;
	}
	public void setSelectedBox(boolean selectedBox) {
		this.selectedBox = selectedBox;
	}
}
