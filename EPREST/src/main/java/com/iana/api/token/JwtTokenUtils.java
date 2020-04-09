package com.iana.api.token;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.iana.api.domain.SecurityObject;

//import com.lynas.security.model.SpringSecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClock;

@Component
public class JwtTokenUtils implements Serializable {

	private static final long serialVersionUID = -4335237427876519896L;

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	private Clock clock = DefaultClock.INSTANCE;

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}
	/*-------------------------------------------------------------------------------------------*/

	public Date getIssuedAtDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getIssuedAt);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(clock.now());
	}

	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

	private Boolean ignoreTokenExpiration(String token) {
		// here you specify tokens, for that the expiration is ignored
		return false;
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userDetails.getUsername());
	}

	private String doGenerateToken(Map<String, Object> claims, String subject) {
		final Date createdDate = clock.now();
		final Date expirationDate = calculateExpirationDate(createdDate);

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(createdDate)
				.setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
		final Date created = getIssuedAtDateFromToken(token);
		return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
				&& (!isTokenExpired(token) || ignoreTokenExpiration(token));
	}

	public String refreshToken(String token) {
		final Date createdDate = clock.now();
		final Date expirationDate = calculateExpirationDate(createdDate);

		final Claims claims = getAllClaimsFromToken(token);
		claims.setIssuedAt(createdDate);
		claims.setExpiration(expirationDate);

		return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		SecurityObject user = (SecurityObject) userDetails;
		final String username = getUsernameFromToken(token);
		final Date created = getIssuedAtDateFromToken(token);
		// final Date expiration = getExpirationDateFromToken(token);
		return (username.equals(user.getUsername()) && !isTokenExpired(token)
				&& !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate()));
	}
	
	public Boolean validateCustomToken(String token, UserDetails userDetails) {
		SecurityObject user = (SecurityObject) userDetails;
		final String username = getUsernameFromToken(token);
		// final Date expiration = getExpirationDateFromToken(token);
		return (username.equals(user.getUsername()));
	}

	private Date calculateExpirationDate(Date createdDate) {
		return new Date(createdDate.getTime() + expiration * 1000);
	}

	public Date getCreatedDateFromToken(String token) {

		Date created;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			created = new Date((Long) claims.get("created"));
		} catch (Exception e) {
			created = null;
		}
		return created;
	}

	public String getAudienceFromToken(String token) {

		String audience;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			audience = (String) claims.get("audience");
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}

	private Claims getClaimsFromToken(String token) {

		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}

	public Date generateCurrentDate() {

		return new Date(System.currentTimeMillis());
	}

	public Date generateExpirationDate() {

		return new Date(System.currentTimeMillis() + this.expiration * 1000);
	}

	public String generateToken(Map<String, Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(this.generateExpirationDate())
				.signWith(SignatureAlgorithm.HS512, this.secret).compact();
	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * try { JwtTokenUtils t = new JwtTokenUtils(); String token =
	 * "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyTmFtZSIsImF1ZGllbmNlIjoid2ViIiwiY3JlYXRlZCI6MTUxMjQ2NDgwODUwNywiZXhwIjoxNTEyNDY0ODM4fQ.2CKJgQNvF79LA3Lnp9E978I-pEgW4prYSN7mkYO9NDQl-LhW4vfcj8C1zp-heXQtv7iCAwHMSY-LECzPXUAUTg"
	 * ;
	 * 
	 * Map<String, Object> claims = new HashMap<String, Object>();
	 * claims.put("sub", "userName"); claims.put("audience", "web"); Date d =
	 * t.generateCurrentDate(); System.out.println("generateCurrentDate:=>" +
	 * d); claims.put("created", d);
	 * 
	 * token = t.generateToken(claims); System.out.println("token:=>" + token);
	 * System.out.println("ExpirationDateFromToken:=>" +
	 * t.getExpirationDateFromToken(token));
	 * 
	 * System.out.println("isTokenExpired:=>"+t.isTokenExpired(token)); }
	 * catch(Exception e) { e.printStackTrace(); } }
	 */

}
