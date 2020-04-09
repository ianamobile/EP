package com.iana.api.domain;

import lombok.Data;

@Data
public class FpToken {

	private String scac;
	private String email;
	private String firstName;
	private String lastName;
	private String userType; // roleName
	private Long id; // loginId
	private String userName;
	
	// used to set generated token
	private String key;

}
