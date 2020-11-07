package com.iana.api.domain.billing.payment;

import lombok.Data;

/**
 * @author ianaoffshore Created At 01-May-2020 4:56:24 am
 * 
 */
@Data
public class BillingUser {

	private Long buId;
	private String accountNumber;
	private String userName;
	private String password;
	private String contactSuffix;
	private String firstName;
	private String lastName;
	private String title;
	private String phone;
	private String fax;
	private String email;
	private String createdBy;
	private String createdDate;
	private String modifiedBy;
	private String modifiedDate;

}
