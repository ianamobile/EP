package com.iana.api.token;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.iana.api.domain.FpToken;
import com.iana.api.domain.SecurityObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

/**
 * Class validates a given token by using the secret configured in the application
 * 
 * @author pascal alma
 */
@Component
public class JwtTokenValidator {

	Logger log = LogManager.getLogger(JwtTokenValidator.class);

	@Value("${jwt.secret:iana}")
	private String secret;

	/**
	 * Tries to parse specified String as a JWT token. If successful, returns User object with
	 * userName, accountNumber and roleName (extracted from token). If unsuccessful (token is invalid or
	 * not containing all required user properties), simply returns null.
	 * 
	 * @param token
	 *            the JWT token to parse
	 * @return the User object extracted from specified token or null if a token is invalid.
	 */
	public SecurityObject parseToken(String token) {

		SecurityObject userObject = null;

		try {
			Claims body = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();

			userObject = new SecurityObject();
			userObject.setUsername((String)body.get("userName"));
			userObject.setAccountNumber((String)body.get("accountNumber"));
			userObject.setRoleName((String)body.get("roleName"));
			userObject.setCompanyName((String)body.get("companyName"));
			userObject.setScac((String)body.get("scac"));
			userObject.setIpAddress((String)body.get("ipAddress"));
			userObject.setInnerAccountNumber((String)body.get("innerAccountNumber"));
			userObject.setInnerCompanyName((String)body.get("innerCompanyName"));
			userObject.setInnerScac((String)body.get("innerScac"));
			userObject.setFirstName((String)body.get("firstName"));
			userObject.setLastName((String)body.get("lastName"));
			userObject.setEmail((String)body.get("email"));

		} catch (JwtException e) {
			// Simply print the exception and null will be returned for the userDto
			log.error("JwtException::", e);
		}
		return userObject;
	}

	public FpToken parseForgotPwdToken(String token) throws ExpiredJwtException, JwtException {
        Claims body = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        FpToken fpToken = new FpToken();
        fpToken.setEmail((String)body.get("email"));
        fpToken.setFirstName((String)body.get("firstName"));
        fpToken.setLastName((String)body.get("lastName"));
        fpToken.setId(body.get("id", Long.class));

        return fpToken;
	}

	
}
