package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InvoiceHeader {

	private int invPkid = 0;
	private String invNo;
	private String tempInvNo;
	private String perInvNo;
	private String invDate;
	private String paidAmnt;
	private String remarks;
	private String acctNo;
	private String member;
	private String invTemplate;
	private String invoiceAmnt;
	private String workSetNm;
	private String billTo;
	private String status;
	private String addToBatch;
	private String invType;
	private List<Adjustments> adjBean = new ArrayList<Adjustments>();
	private List<InvoiceDet> billReq = new ArrayList<InvoiceDet>();
	private List<Payment> pymtBean = new ArrayList<Payment>();
	private List<InvoiceDet> invDetBean = new ArrayList<InvoiceDet>();
	private String uiiaStatus;

	// In Old it is set to request.setAttribute
	private String mcPay; // YES/NO

	private InvoiceDetails invoiceDetails;
	private List<LabelValueForm> listBatchOptions = new ArrayList<LabelValueForm>();
}
