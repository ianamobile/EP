package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

public class EPWhatIfForm  {


	private String blanketAllwd = "";
	private String effDate = "";
	private List<EPInsNeeds> epNeeds = new ArrayList<>();
	private String validMC = "";
	private String validMCCalculated = "";
	private AccountInfo acctInfo;
	private EPAddendum epAddendum;
	
	public void setEpNeedsBean(int index,EPInsNeeds needsBean)
	{
		epNeeds.set(index,needsBean);

	}
	public EPInsNeeds getEpNeedsBean(int index)
	{
		int listSize = epNeeds.size();
		if ((index + 1) > listSize)
		{
			//add objects
			for (int j = listSize; j < index + 1; j++)
			{
				EPInsNeeds beanObj =
					new EPInsNeeds();
				epNeeds.add(j, beanObj);
			}
		}

		return epNeeds.get(index);
	}
	
	public String getBlanketAllwd() {
		return blanketAllwd;
	}
	public void setBlanketAllwd(String blanketAllwd) {
		this.blanketAllwd = blanketAllwd;
	}
		public String getEffDate() {
		return effDate;
	}
	public void setEffDate(String effDate) {
		this.effDate = effDate;
	}
	
	public List<EPInsNeeds> getEpNeeds() {
		return epNeeds;
	}
	public void setEpNeeds(List<EPInsNeeds> epNeeds) {
		this.epNeeds = epNeeds;
	}
	public String getValidMC() {
		return validMC;
	}
	public void setValidMC(String validMC) {
		this.validMC = validMC;
	}
	public AccountInfo getAcctInfo() {
		return acctInfo;
	}
	public void setAcctInfo(AccountInfo acctInfo) {
		this.acctInfo = acctInfo;
	}
	public EPAddendum getEpAddendum() {
		return epAddendum;
	}
	public void setEpAddendum(EPAddendum epAddendum) {
		this.epAddendum = epAddendum;
	}
	public String getValidMCCalculated() {
		return validMCCalculated;
	}
	public void setValidMCCalculated(String validMCCalculated) {
		this.validMCCalculated = validMCCalculated;
	}
	

}
