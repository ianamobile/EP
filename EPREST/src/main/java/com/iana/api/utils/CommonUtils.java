package com.iana.api.utils;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import com.iana.api.domain.SecurityObject;
import com.iana.api.service.BaseRestService;

@Component
public class CommonUtils extends ValidationUtils {
	
	public static final DecimalFormat formatAmount = new DecimalFormat("#,##0.00");
	
	@Autowired
	public Environment env;
	
	@Autowired
	protected BaseRestService restService;
	
	@Autowired
	protected NotificationSender notificationSender;

	static Logger log = LogManager.getLogger(CommonUtils.class);

	public static String getClientIPAddresss(HttpServletRequest request) {
		return request.getHeader("x-forwarded-for") == null ? request.getRemoteAddr() : request.getHeader("x-forwarded-for");
	}
 
	public <T> List<T> getListInstance() {
		return new ArrayList<T>();
	}
	/* Exception related code */
	protected ResponseEntity<ApiResponseMessage> sendValidationError(ApiException e){
		List<Errors> errors = getListInstance();
		errors.add(new Errors(1, env.getProperty("validation_error"), e.getMessage()));
		return new ResponseEntity<ApiResponseMessage>(restService.prepareAPIErrors(errors), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	protected ResponseEntity<ApiResponseMessage> sendServerError(Exception e,String message){
		List<Errors> errors = getListInstance();
		notificationSender.send(e);
		errors.add(new Errors(1, env.getProperty("server_error"), message ));
		return new ResponseEntity<ApiResponseMessage>(restService.prepareAPIErrors(errors), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/* setting new way to set error messages */
	protected ResponseEntity<ApiResponseMessage> sendUnprocessableEntity(List<Errors> errors) {
		return new ResponseEntity<ApiResponseMessage>(restService.prepareAPIErrors(errors), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	protected List<Errors> setBusinessError(List<String> errorList) {
		List<Errors> errors = getListInstance();
		errors.add(new Errors(1, env.getProperty("business_error"), StringUtils.join(errorList, GlobalVariables.COMMA)));
		return errors;
	}


	protected List<Errors> setValidationErrors(List<String> errorList) {
		List<Errors> errors = getListInstance();
		errors.add(new Errors(1, env.getProperty("validation_error"), StringUtils.join(errorList, GlobalVariables.COMMA)));
		return errors;
	}

	/**
	  * Check if provided Object of String is validate or not
	  * 
	  * @param str
	  * @return valid Object of String (Blank if null or null)
	  */
	public static final String validateObject(Object str){
		if(str == null || StringUtils.isBlank(str.toString())){
			return StringUtils.EMPTY;
		}else{
			return str.toString().trim();
		}
	}

	public SecurityObject initSecurityObject() {
		SecurityObject securityObject = new SecurityObject();
		securityObject.setAccountNumber(StringUtils.EMPTY);
		securityObject.setUsername(StringUtils.EMPTY);
		securityObject.setFirstName(StringUtils.EMPTY);
		securityObject.setLastName(StringUtils.EMPTY);
		securityObject.setEmail(StringUtils.EMPTY);
		securityObject.setEnabled(false);
		securityObject.setAccessToken(StringUtils.EMPTY);
		securityObject.setCompanyName(StringUtils.EMPTY);
		securityObject.setScac(StringUtils.EMPTY);
		securityObject.setIpAddress(StringUtils.EMPTY);
		securityObject.setRoleName(StringUtils.EMPTY);
		securityObject.setLastLoginDateTime(StringUtils.EMPTY);
		securityObject.setUiiaStaff(false);
		securityObject.setLastLoginDateTime(StringUtils.EMPTY);
		securityObject.setStatus(StringUtils.EMPTY);
		securityObject.setEpIddFlag(StringUtils.EMPTY);
		securityObject.setInnerAccountNumber(StringUtils.EMPTY);
		securityObject.setInnerCompanyName(StringUtils.EMPTY);
		securityObject.setInnerScac(StringUtils.EMPTY);
		securityObject.setInnerEmail(StringUtils.EMPTY);

		return securityObject;
	}

	protected String decode(String requestVar) {
		return UriUtils.decode(requestVar, StandardCharsets.UTF_8.name());
	}	

}
