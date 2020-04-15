package com.iana.api.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class JoinRecord extends MCOverride {

	private String mcName="";
	private String mcScac="";
	private String mcAccountNumber="";
	private String mcEPStatus="";
	private String overUsed="";
	private String epMember="";
	private String knownAs="";

	private EPJoinDet joinBean = new EPJoinDet();
	
}
