package com.iana.api.domain;

import lombok.Data;

@Data
public class MCCancel {

	private String mcCancRefDt = "";
	private int pageNumber;
	private int limit;
	private String compName = "";
	private String acctNo = "";
	private String scac = "";
	private String statusCd = "";
	private String cancDt = "";
	private String acctLstUpdt = "";
	private String selectMC = "";

	private String mcCancRefStartDate = "";
	private String mcCancRefEndDate = "";

}
