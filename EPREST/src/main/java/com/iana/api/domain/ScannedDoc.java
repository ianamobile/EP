/*
 *  File		: SearchAccountBean.java
 *  Author		: Ashok Soni
 *  Created		: June 09,2006
 *  Description	: This bean will be used to search BasicAccount details like
 *  			  Company Name,SCAC Code, Account No,  and will  be used
 *  			  to display search results for Account Details 
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain;

public class ScannedDoc
{
	private String scanId="";
	private String scanDate="";
	private String docType="";
	private String fileName="";
	
	private String scanTime="";
	private String directory="";
	
	private String unitId="";
	private String accountNo="";
	private String Bound="";
	
	private String driver="";
	
	/**
	 * @return Returns the docType.
	 */
	public String getDocType() {
		return docType;
	}

	/**
	 * @param docType The docType to set.
	 */
	public void setDocType(String docType) {
		this.docType = docType;
	}

	/**
	 * @return Returns the scanDate.
	 */
	public String getScanDate() {
		return scanDate;
	}

	/**
	 * @param scanDate The scanDate to set.
	 */
	public void setScanDate(String scanDate) {
		this.scanDate = scanDate;
	}

	/**
	 * @return Returns the scanId.
	 */
	public String getScanId() {
		return scanId;
	}

	/**
	 * @param scanId The scanId to set.
	 */
	public void setScanId(String scanId) {
		this.scanId = scanId;
	}

	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getScanTime() {
		return scanTime;
	}

	public void setScanTime(String scanTime) {
		this.scanTime = scanTime;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getBound() {
		return Bound;
	}

	public void setBound(String bound) {
		Bound = bound;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer(this.getClass().getName());
		sbTemp.append("scanId[").append(this.scanId).append("]") ;
		sbTemp.append("scanDate[").append(this.scanDate).append("]") ;
		sbTemp.append("docType[").append(this.docType).append("]") ;		
		return sbTemp.toString();
	}

}
