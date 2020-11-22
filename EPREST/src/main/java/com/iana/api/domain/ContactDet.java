package com.iana.api.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ContactDet {

	private String accountNo = "";
	private String contctFname = "";
	private String contctMname = "";
	private String contctLname = "";
	private String contctTitle = "";
	private String contctSalutation = "";
	private String contctMrMs = "";
	private String contctSuffix = "";
	private String contctPrmPhone = "";
	private String contctSecPhone = "";
	private String contctPrmFax = "";
	private String contctSecFax = "";
	private String contctPrmEmail = "";
	private String contctSecEmail = "";
	private String contctType = "";
	private String attr1 = "";
	private String sameBillContct = "";
	private String sameDisputeCntct = "";
	private int contctId = 0;

}
