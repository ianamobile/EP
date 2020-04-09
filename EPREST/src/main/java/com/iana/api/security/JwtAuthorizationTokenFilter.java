package com.iana.api.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.Gson;
import com.iana.api.controller.LoginRest;
import com.iana.api.domain.Login;
import com.iana.api.domain.SecurityObject;
import com.iana.api.token.JwtTokenUtils;
import com.iana.api.token.JwtTokenValidator;
import com.iana.api.utils.GlobalVariables;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    public static final String REQ_BODY = "body";
	
    Logger log = LogManager.getLogger(this.getClass().getName());

    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    
    @Autowired
    private JwtTokenValidator jwtTokenValidator;
    
    @Value("${jwt.header}")
    private String tokenHeader;
    
    @Autowired
    private Gson gsonObj;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.info("processing authentication for '{}'", request.getRequestURL());
        
        if(request.getRequestURL().toString().contains(LoginRest.URI_AUTH_PATH)){
        	
        	String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        	Login login = this.gsonObj.fromJson(requestBody, Login.class);
    		request.setAttribute(REQ_BODY, login);
        	
        }else{
        	//For any other request except login..
        	final String requestHeader = request.getHeader(this.tokenHeader);
            String username = null;
            String authToken = null;
            if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
                authToken = requestHeader.substring(7);
                try {
                    username = jwtTokenUtils.getUsernameFromToken(authToken);
                    
                } catch (IllegalArgumentException | ExpiredJwtException | SignatureException  e) {
					log.error("an error occurred during getting username from token", e);
					 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                	 return;
//	            	PrintWriter writer = response.getWriter();
//	                writer.write(new Gson().toJson(new ApiResponseMessage(1, "JWT signature does not match", null)));
//	                return;
                }
            } else {
                log.warn("couldn't find bearer string, will ignore the header");
            }

            log.info("checking authentication for user '{}'", username);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("security context was null, so authorizing user");

                // It is not compelling necessary to load the use details from the database. You could also store the information
                // in the token and read it from it. It's up to you ;)            
                SecurityObject securityObject = this.jwtTokenValidator.parseToken(authToken);
                request.setAttribute(GlobalVariables.SECURITY_OBJECT, securityObject);
                request.setAttribute("accessToken", authToken);
                if(securityObject == null || StringUtils.isBlank(securityObject.getAccountNumber())){
                	 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                	 return;
                }
                // For simple validation it is completely sufficient to just check the token integrity. You don't have to call
                // the database compellingly. Again it's up to you ;)
                if (jwtTokenUtils.validateCustomToken(authToken, securityObject)) {
                	UserDetails userDetails = securityObject;
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    log.info("authorized user '{}', setting security context", username);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
