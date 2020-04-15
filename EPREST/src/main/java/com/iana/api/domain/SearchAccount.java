package com.iana.api.domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SearchAccount extends CommonFieldsSearch {

	private String companyName;
	private String scac;
	private String accountNumber;
	private String knownAs;
	private String licenseNumber;
	private String userType;
	private String newCompanyName;
	private String lastName;
	private String iddMemberType;
	private String epName;
	private String epScac;
	private String date;
	
}
