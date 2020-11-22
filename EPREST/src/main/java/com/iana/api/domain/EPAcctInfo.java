package com.iana.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iana.api.utils.GlobalVariables;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class EPAcctInfo extends AccountMaster {

	private String epType = "";
	private String billDate = null;
	private String epIddFlg = GlobalVariables.NO;
	private String epRportngReq = GlobalVariables.NO;
	private String adminFeeFlag = GlobalVariables.NO;
	private String epLvlService = "";
	private String attr1 = "";
	private String epEntities = "";
	private String attr2 = "";
	private String attr3 = "";
	private int epBasicInfoId = 0;
	@JsonIgnore
	private String epNotes = "";
	private String epInvReqFlg = "";
	private String lstBillDt = "";
	private String reasonCancellation = ""; // Reason for Cancelling MC
	private String insProb = ""; // insurance problem for EP as in mc_ep_join_status table
	private String canEffDate = "";
	private String epCnclEmail;

}
