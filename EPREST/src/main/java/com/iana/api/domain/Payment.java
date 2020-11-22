package com.iana.api.domain;

import lombok.Data;

@Data
public class Payment {
	
	private String pymtDate;
	private String pymtAmnt;
	private String pymtMode;
	private String pymtDtlId;
	private int invHdrId = 0;
	private String chqRcvdDt;
	private String pymtType;
	private String authCd;
	private String tranId;
	private String chqNo = "0";
	private String creditNo;
	private int crdtExpMonth = 0;
	private int crdtExpYear = 0;
	private int pymtBatchId = 0;
	//added for verisign payment related parameters
	private String companyName;
	private String firstName;
	private String lastName;
	private String invoiceNum;
	private String comment1;
	private String comment2;
	private String address1;
	private String zipCode;
	private String autoPay;
	private String accNo;
	private String scac;
	private String email;
	private String invAmt;
	private double paidAmt;

}
