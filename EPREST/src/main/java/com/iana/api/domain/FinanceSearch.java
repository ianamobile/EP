package com.iana.api.domain;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class FinanceSearch {

	private String invId = StringUtils.EMPTY;
	private String invNo = StringUtils.EMPTY;
	private String invDate = StringUtils.EMPTY;
	private String toDate = StringUtils.EMPTY;
	private String fromDate = StringUtils.EMPTY;
	private String paidAmnt = StringUtils.EMPTY;
	private String invoiceAmnt = StringUtils.EMPTY;
	private String billTo = StringUtils.EMPTY;
	private String status = StringUtils.EMPTY;
	private String chqNo = StringUtils.EMPTY;
	private String authCd = StringUtils.EMPTY;
	private String invType = StringUtils.EMPTY;
	private String accNo = StringUtils.EMPTY;
	private String payDate = StringUtils.EMPTY;
	private String batchCd = StringUtils.EMPTY;
	private String userType = StringUtils.EMPTY;
	private String uiiaStatCode = StringUtils.EMPTY;
	private String invTemplate = StringUtils.EMPTY;
	private String remarks = StringUtils.EMPTY;

	private String uiia_Stat_Code = "";

}