package com.iana.api.domain;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;


@Data
public class SecurityObject implements UserDetails{

	private static final long serialVersionUID = -854578964060948702L;
	
	private String accountNumber;
	private String username;
	private String firstName;
    private String lastName;
    private String email;
	private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;
    
    @JsonIgnore
    private String password;
    
    @JsonIgnore
    private Date lastPasswordResetDate;

	private String accessToken;
	private String companyName;
	private String scac;
	private String ipAddress;
	private String roleName;
	private String lastLoginDateTime;
	private boolean uiiaStaff;
	private String status;
	private String epIddFlag;
	
	private String innerAccountNumber;
	private String innerCompanyName;
	private String innerScac;
	private String innerEmail;
	private String requestFrom; // This field is indicating originSource(web/ mobile)
	
	public SecurityObject(){
		
	}
			
	public SecurityObject(
	          String accountNumber,
	          String username,
	          String firstName,
	          String lastName,
	          String email,
	          String password, Collection<? extends GrantedAuthority> authorities,
	          boolean enabled,
	          Date lastPasswordResetDate,
	          String companyName,
		      String scac,
		      String ipAddress,
		      String roleName,
		      String lastLoginDateTime,
		      boolean uiiaStaff,
		      String status,
		      String epIddFlag,
		      String innerAccountNumber,
		      String innerCompanyName,
		      String innerScac,
		      String innerEmail,
		      String requestFrom
	    ) {
	        this.accountNumber = accountNumber;
	        this.username = username;
	        this.firstName = firstName;
	        this.lastName = lastName;
	        this.email = email;
	        this.password = password;
	        this.authorities = authorities;
	        this.enabled = enabled;
	        this.lastPasswordResetDate = lastPasswordResetDate;
	        this.companyName = companyName;
	        this.scac= scac;
	        this.ipAddress = ipAddress;
	        this.roleName = roleName;
	        this.lastLoginDateTime = lastLoginDateTime;
	        this.uiiaStaff = uiiaStaff;
	        this.status = status;
	        this.epIddFlag = epIddFlag;
	        this.innerAccountNumber = innerAccountNumber;
	        this.innerCompanyName = innerCompanyName;
	        this.innerScac = innerScac;
	        this.innerEmail = innerEmail;
	        this.requestFrom = requestFrom;
	    }
	
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
