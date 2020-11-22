package com.iana.api.domain;

import lombok.Data;

@Data
public class SecUserDetails {

	private String userName;
	private String oldUserName;
	private int secUserId;
	private String status;
	private String password;
	private String email;
	private String download;
	private String attr1;
	private String attr2;
	private String auditTrailExtra;
}
