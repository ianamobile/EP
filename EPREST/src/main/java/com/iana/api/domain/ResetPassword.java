package com.iana.api.domain;

import lombok.Data;

@Data
public class ResetPassword {

	private String q;
	private String newPassword;
	private String confirmPassword;
}
