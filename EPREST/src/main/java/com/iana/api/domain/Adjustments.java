package com.iana.api.domain;

import lombok.Data;

@Data
public class Adjustments {
	
	protected String pkId;
	protected String invoiceNo;
	protected String invoiceAmt;
	protected String adjtype;
	protected String reasons;
	protected String amtForAdj;
	protected String glAcct;
	protected double revAmt;
	
}
