package com.iana.api.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Login implements Serializable {

	private static final long serialVersionUID = -8445943548965154778L;

	private String accountNumber;
	private String username;
	private String password;
	private String roleName;

	public Login() {
		super();
	}

	public Login(String accountNumber, String username, String password, String roleName) {
		super();
		this.accountNumber = accountNumber;
		this.username = username;
		this.password = password;
		this.roleName = roleName;
	}

}
