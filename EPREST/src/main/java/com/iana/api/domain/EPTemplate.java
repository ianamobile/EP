/**
 * 
 */
package com.iana.api.domain;

import com.iana.api.utils.GlobalVariables;

public class EPTemplate {
	private int templateID = 0;
	private String effDate = "";
	private String tempStatus = "";// values can be past/current/future and is set from combo selection
	private String createdDate = "";
	private String dbTemplateStatus = "";// values stored in DB and can be 'A','W', 'N'
	private int pageNumber = 0;
	private int limit = GlobalVariables.LIMIT;
	
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getDbTemplateStatus() {
		return dbTemplateStatus;
	}

	public void setDbTemplateStatus(String dbTemplateStatus) {
		this.dbTemplateStatus = dbTemplateStatus;
	}

	public String getTempStatus() {
		return tempStatus;
	}

	public void setTempStatus(String tempStatus) {
		this.tempStatus = tempStatus;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getEffDate() {
		return effDate;
	}

	public void setEffDate(String effDate) {
		this.effDate = effDate;
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
		sbTemp.append(" templateID[");
		sbTemp.append(this.templateID);
		sbTemp.append("] effDate[");
		sbTemp.append(this.effDate);
		sbTemp.append("] tempStatus[");
		sbTemp.append(this.tempStatus);
		sbTemp.append("]");

		return sbTemp.toString();
	}

}
