package com.iana.api.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class InternalFinanceSearch extends CommonFieldsSearch{

//	private String invId;
//	private String invNo;
//	private String invDate;
//	private String toDate;
//	private String fromDate;
//	private String paidAmnt;
//	private String invoiceAmnt;
//	private String billTo;
	private String status;
//	private String chqNo;
//	private String authCd;
//	private String invType;
	private String accNo;
//	private String payDate ;
//	private String batchCd;
//	private String userType;
//	private String uiiaStatCode;
//	private String invTemplate;
//	private String remarks;
}