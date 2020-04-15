package com.iana.api.domain;

import com.iana.api.utils.GlobalVariables;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EPJoinDet extends AccountInfo {
	private int epMemDtlId=0;
	private String knownAs="";
	private String cancelValue="";
	private String canEffDate="";
	private String epMember="";
	private String epPrivate="";
	private String epHouse="";
	private String rsnCancel="";
	private String tempCancelValue=""; //used for notification
	private String extraVar="";
	private String nonUIIAEpFlag = GlobalVariables.NO;
	private String epCnclNotified=""; //EP Cancelled MC - Notified or not - If yes then 'Y' else 'N'
	private boolean uiiaOverrideEPCan;
	private String epCanEmail="";
	
}
