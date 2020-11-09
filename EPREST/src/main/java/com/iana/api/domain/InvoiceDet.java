package com.iana.api.domain;

import lombok.Data;

@Data
public class InvoiceDet {
	
	protected int pkId=0;
	protected String billId;
	protected String billCode;	
	protected String desc;
	protected String glAcctNo;
	protected String billCodeAmnt="0";
}
