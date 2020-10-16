package com.iana.api.token;



import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.iana.api.domain.FpToken;
import com.iana.api.domain.SecurityObject;
import com.iana.api.utils.CommonUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * convenience class to generate a token for testing your requests. Make sure the used secret here
 * matches the on in your application.properties
 * 
 * @author pascal alma
 */
@Component
public class JwtTokenGenerator {

	Logger log = LogManager.getLogger(JwtTokenGenerator.class);

	@Value("${jwt.secret}")
	private String secret;

	@Autowired
	private JwtTokenUtils tokenUtils;

	
	/**
	 * Generates a JWT token containing username as subject, and userId and role as additional
	 * claims. These properties are taken from the specified User object. Tokens validity is
	 * infinite.
	 * 
	 * @param u
	 *            the user for which the token will be generated
	 * @return the JWT token 
	 */
	public String generateToken(SecurityObject securityObject) {

		Claims claims = Jwts.claims().setSubject(securityObject.getUsername());
		claims.put("userName", CommonUtils.validateObject(securityObject.getUsername()));
		claims.put("accountNumber", CommonUtils.validateObject(securityObject.getAccountNumber()));
		claims.put("roleName",CommonUtils.validateObject(securityObject.getRoleName()));
		claims.put("companyName", CommonUtils.validateObject(securityObject.getCompanyName()));
		claims.put("scac",CommonUtils.validateObject(securityObject.getScac()));
		claims.put("ipAddress",CommonUtils.validateObject(securityObject.getIpAddress()));
		claims.put("innerAccountNumber", CommonUtils.validateObject(securityObject.getInnerAccountNumber()));
		claims.put("innerCompanyName",CommonUtils.validateObject(securityObject.getInnerCompanyName()));
		claims.put("innerScac",CommonUtils.validateObject(securityObject.getInnerScac()));
		claims.put("firstName", CommonUtils.validateObject(securityObject.getFirstName()));
		claims.put("lastName", CommonUtils.validateObject(securityObject.getLastName()));
		claims.put("email", CommonUtils.validateObject(securityObject.getEmail()));
		claims.put("uiiaStaff", CommonUtils.validateObject(securityObject.isUiiaStaff()));
		claims.put("groupId", CommonUtils.validateObject(securityObject.getGroupId()));
		claims.put("extraParam", CommonUtils.validateObject(securityObject.getExtraParam()));
		
		return Jwts.builder()
					.setClaims(claims)
					.signWith(SignatureAlgorithm.HS512, this.secret)
					.compact();
	}

	public String generateForgotPwdToken(FpToken fp) {

		try {

			Claims claims = Jwts.claims().setSubject(fp.getUserType());
			claims.put("firstName",fp.getFirstName());
			claims.put("lastName",fp.getLastName());
			claims.put("email", fp.getEmail());
			claims.put("id", (Long)fp.getId());

			return Jwts.builder().setClaims(claims).setExpiration(tokenUtils.generateExpirationDate()).signWith(SignatureAlgorithm.HS512, secret).compact();

		} catch (Exception e) {
			log.error("Exception::", e);
		}

		return StringUtils.EMPTY;
	}

	
}
