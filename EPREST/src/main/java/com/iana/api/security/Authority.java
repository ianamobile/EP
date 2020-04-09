package com.iana.api.security;

import lombok.Data;

@Data
public class Authority {

    private Long id;
    private AuthorityName name;
    
	public Authority(Long id, AuthorityName name) {
		super();
		this.id = id;
		this.name = name;
	}
    
    
    
    
}