package com.iana.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ValidationUtils {

	static Logger log = LogManager.getLogger(ValidationUtils.class);
 
	private static final Pattern TIME24HOURS_PATTERN = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");

	public static final Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z0-9]*$");
	
	public static final Pattern chassisPattern = Pattern.compile("^([a-zA-Z]+[ ]*)?$");
	
	public static final Pattern ftpPortPattern = Pattern.compile("^\\d{1,5}$");
	
	public static final Pattern COMMA_SEPARATED_NUMBER = Pattern.compile("[0-9]+(,[0-9]+)*");
	
	public static final Pattern EXCEL_FORMAT_PATTERN  = Pattern.compile(".*\\.(xls|xlsx)$");
	
	public static final Pattern ONE_UPPERCASE_PATTERN = Pattern.compile("(.*[A-Z].*)");
	
	public static final Pattern ONE_NUMBER_PATTERN = Pattern.compile("(.*[0-9].*)");

	public static boolean isInteger(String s) {

		return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {

		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	public static boolean isNull(final Collection<?> c) {

		return c == null;
	}

	public static boolean isNull(final Map<?, ?> m) {

		return m == null;
	}

	public static boolean isNullOrEmpty(final Collection<?> c) {

		return c == null || c.isEmpty();
	}

	public static boolean isNullOrEmpty(final Map<?, ?> m) {

		return m == null || m.isEmpty();
	}

	public static boolean isNotNull(final Collection<?> c) {

		return !(c == null);
	}

	public static boolean isNotNull(final Map<?, ?> m) {

		return !(m == null);
	}

	public static boolean isNotNullOrEmpty(final Collection<?> c) {

		return !(c == null || c.isEmpty());
	}

	public static boolean isNotNullOrEmpty(final Map<?, ?> m) {

		return !(m == null || m.isEmpty());
	}

	public static boolean isvalidLengh(Object str, int min, int max, boolean enableTrim) {

		if (str == null) {
			return false;
		}
		if (enableTrim) {
			if (StringUtils.isBlank(str.toString())) {
				return false;
			}
		}
		int strLength = str.toString().length();
		if (strLength < min || strLength > max) {
			return false;
		}
		return true;
	}

	public static boolean isAlpha(String name) {
		char[] chars = name.toCharArray();

		for (char c : chars) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}

		return true;
	}
	public static boolean isCharacterString(String str) {
		if (StringUtils.isBlank(str)) {
			return false;
		}
		boolean isChar = str.matches("[a-zA-z]+");
		return isChar;
	}

	// check if provided string is digit or not
	public static boolean isNumber(String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		return str.matches("[0-9]+");
	}
	
	public static boolean isAlphaNumeric(String str) {

		if (StringUtils.isBlank(str)) {
			return false;
		}
		return alphaNumericPattern.matcher(str).matches();
	}

	public String getAcceptHeaderVal(HttpServletRequest request) {
		
		return request.getHeader(GlobalVariables.HEADER_ACCEPT);
	}
	
	public static final boolean inputDateIsFutureDate(Date inputDate) {

		return !inputDate.before(new Date());
	}

	public static boolean isValidDateFormat(String input, String format) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(input);
			if (!input.equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {
			log.error("ParseException:", ex);
		}
		return date != null;
	}
	
	public static boolean emailValidator(String email){
		String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
		
		Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
		
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	public static boolean isFTPPortValid(String ftpPort)
	{
	    Matcher matcher = ftpPortPattern.matcher(ftpPort);
	    return matcher.matches();
	}
	
    public static boolean chassisValidator(String chassisCode){
    	if (StringUtils.isBlank(chassisCode)) {
			return false;
		}
    	return chassisPattern.matcher(chassisCode).matches();
	
	}
    
    public static boolean isFutureDate(String date){
        Date sysDate = DateTimeFormater.getCurrentDateInstance();
  	  	//passed from user
  	  Date userDate = DateTimeFormater.getStrToDate(date,DateTimeFormater.FORMAT5);
  		
  	  if(userDate.compareTo(sysDate)>0){
  		return true;
  	  }else{
  		return false;
  	  }
      }
	
    public static boolean isCommaSepratedNumbers(String ids){
		Matcher matcher = COMMA_SEPARATED_NUMBER.matcher(ids);
		return matcher.matches();
	}
    
    public static boolean isTimeValid(String time){
    	Matcher matcher = TIME24HOURS_PATTERN.matcher(time);
    	return matcher.matches();
    }

    
    public static boolean isExcelFile(String filename){
		Matcher matcher = EXCEL_FORMAT_PATTERN.matcher(filename);
		return matcher.matches();
	}
    
    public static boolean isContaineUppercaseLetter(String str){
    	Matcher matcher = ONE_UPPERCASE_PATTERN.matcher(str);
    	return matcher.matches();
    }
    
    public static boolean isContaineNumber(String str){
    	Matcher matcher = ONE_NUMBER_PATTERN.matcher(str);
    	return matcher.matches();
    }
}
