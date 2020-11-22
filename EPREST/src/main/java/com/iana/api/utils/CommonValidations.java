package com.iana.api.utils;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.springframework.util.StringUtils;

public class CommonValidations {

	public static boolean isValidEmail(String emailId) {
		if (emailId != null && !emailId.equalsIgnoreCase(GlobalVariables.EMPTY)) {
			return GenericValidator.isEmail(emailId.trim());
		}
		return Boolean.TRUE;
	}

	public static boolean isValidMaxLength(String value, int maxLenght) {
		if (value != null) {
			return GenericValidator.maxLength(value.trim(), maxLenght);
		}
		return false;
	}

	public static boolean isValidMinLength(String value, int minLenght) {
		if (value != null) {
			return GenericValidator.minLength(value.trim(), minLenght);
		}
		return false;
	}
	
	public static boolean isMatchingWithRegex(String regexp, String value) {
		if (value != null && !value.equalsIgnoreCase(GlobalVariables.EMPTY)) {
			return GenericValidator.matchRegexp(value, regexp);
		}
		return Boolean.TRUE;
	}
	
	/**
	 * Checks if a String is not empty (""), not null and not whitespace only.
 	 * StringUtils.isNotBlank(null)      = false
 	 * StringUtils.isNotBlank("")        = false
 	 * StringUtils.isNotBlank(" ")       = false
 	 * StringUtils.isNotBlank("bob")     = true
 	 * StringUtils.isNotBlank("  bob  ") = true
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isStringEmpty(String value) {
		return StringUtils.isEmpty(value);
	}
	
	public static boolean isInteger(String value) {
		if (value != null) {
			return GenericValidator.isInt(value.trim());
		}
		return false;
	}

	
	public static boolean isNull( final Collection< ? > c ) {
	    return c == null;
	}

	public static boolean isNull( final Map< ?, ? > m ) {
	    return m == null;
	}
	
	public static boolean isNullOrEmpty( final Collection< ? > c ) {
	    return c == null || c.isEmpty();
	}

	public static boolean isNullOrEmpty( final Map< ?, ? > m ) {
	    return m == null || m.isEmpty();
	}
	
	public static boolean isNotNull( final Collection< ? > c ) {
	    return !(c == null);
	}

	public static boolean isNotNull( final Map< ?, ? > m ) {
	    return !(m == null);
	}
	
	public static boolean isNotNullOrEmpty( final Collection< ? > c ) {
	    return !(c == null || c.isEmpty());
	}
	
	public static boolean isNotNullOrEmpty( final Map< ?, ? > m ) {
	    return !(m == null || m.isEmpty());
	}
}
