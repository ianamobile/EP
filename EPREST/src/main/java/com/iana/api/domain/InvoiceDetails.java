package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InvoiceDetails {

	private String invoiceNo="";
	private String invoiceDate="";
	private String paidAmt="";
	private String remarks="";
	private String[] detId;
	private String[] billCode;
	private String[] desc;
	private String[] glAcct;
	private String billTo="";
	private String acctNo="";
	private String member="";
	private String invTemplate="";
	private String invoiceAmt="";
	private String workSetNm="";
	private String status="";
	private String[] amnt;
	private String adjtype="";
	private String reasons="";
	private String amtForAdj="";
	private String adjBillTo="";
	private String adjGLAcct="";
	private String glDate="";
	private String addToBatch="";
	private List<InvoiceDet> billReq= new ArrayList<InvoiceDet>();
	
}