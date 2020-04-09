package com.iana.api.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.iana.api.domain.Login;
import com.iana.api.domain.SecurityObject;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
 
	Logger log = LogManager.getLogger(this.getClass().getName());
	 
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
    private JwtUserDetailsService userDetailsService;
	
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	log.info("-------------CustomAuthenticationProvider------------");
    	Login login = (Login)request.getAttribute(JwtAuthorizationTokenFilter.REQ_BODY);
        SecurityObject jwtUser = null;
        UsernamePasswordAuthenticationToken tokenAuth = null;
        try {
			jwtUser = userDetailsService.loadUserByLoginInfo(login);
			tokenAuth= new UsernamePasswordAuthenticationToken(login.getAccountNumber(), login.getPassword(), jwtUser.getAuthorities());
			
			tokenAuth.setDetails(jwtUser);
			
		} catch (Exception e) {
			//send mail or add mail entry into database 
			throw new AuthenticationException(e.getMessage(), e);
		}
        return tokenAuth;
    }
    
    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

   /* @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
          UsernamePasswordAuthenticationToken.class);
    }*/
}