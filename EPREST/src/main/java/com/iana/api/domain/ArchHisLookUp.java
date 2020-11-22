package com.iana.api.domain;

public class ArchHisLookUp {

	private String acctNo = "";

	private String archSelect = "";

	private String date = "";

	private String mcName = "";

	private String mcSCACCode = "";

	private String epName = "";

	private String epSCACCode = "";

	public void setEpSCACCode(String epSCACCode) {
		this.epSCACCode = epSCACCode;
	}

	public String getEpSCACCode() {
		return this.epSCACCode;
	}

	public void setEpName(String epName) {
		this.epName = epName;
	}

	public String getEpName() {
		return this.epName;
	}

	public void setMcSCACCode(String mcSCACCode) {
		this.mcSCACCode = mcSCACCode;
	}

	public String getMcSCACCode() {
		return this.mcSCACCode;
	}

	public void setMcName(String mcName) {
		this.mcName = mcName;
	}

	public String getMcName() {
		return this.mcName;
	}

	public void setArchSelect(String archSelect) {
		this.archSelect = archSelect;
	}

	public String getArchSelect() {
		return this.archSelect;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return this.date;
	}

	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
	}

	public String getAcctNo() {
		return this.acctNo;
	}
}
