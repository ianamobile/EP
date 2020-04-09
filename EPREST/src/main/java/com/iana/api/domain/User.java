package com.iana.api.domain;

import lombok.Data;

@Data
public class User {

    private String accountNumber;
	private String roleName;
    private String username;
    private String password;
    private String status;
    private Long loginId;
}