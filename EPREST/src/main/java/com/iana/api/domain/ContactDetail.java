package com.iana.api.domain;

import lombok.Data;

@Data
public class ContactDetail {

	private String accountNumber;
	private String firstName;
	private String middleName;
	private String lastName;
	private String title;
	private String salutation;
	private String mrms;
	private String suffix;
	private String priPhone;
	private String secPhone;
	private String priFax;
	private String secFax;
	private String priEmail;
	private String secEmail;
	private String contactType;
	private String sameBillCntct;
	private String sameDisputeCntct; //added by Anirban
	private int contactId = 0;
	
}
