package com.iana.api.controller;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.SecurityObject;
import com.iana.api.security.AuthenticationException;
import com.iana.api.security.JwtAuthorizationTokenFilter;
import com.iana.api.service.LoginService;
import com.iana.api.utils.ApiException;
import com.iana.api.utils.ApiResponseMessage;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.Errors;
import com.iana.api.utils.GlobalVariables;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags={"Login"})
@RequestMapping(path=GlobalVariables.REST_URI_UIIA)
public class LoginRest extends CommonUtils { 
	
	Logger log = LogManager.getLogger(this.getClass().getName());
	
	public static final String URI_AUTH_PATH = "auth";
	public static final String URI_FORGOT_PASSWORD = "forgotPassword";
	public static final String URI_VALID_FORGOT_PASSWORD = "validateForgotPwdLink";	
	public static final String URI_RESET_PASSWORD = "resetPassword";
	
	
	@Autowired
	private LoginService loginService;

	@Autowired
	private AuthenticationManager authenticationManager;


	@RequestMapping(value = URI_AUTH_PATH , method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(HttpServletRequest request) throws AuthenticationException {
    	List<Errors> errors = null;
		List<String> errorList = getListInstance();
		SecurityObject securityObject = new SecurityObject();
		HttpEntity<Login> requestEntity = new HttpEntity<Login>((Login)request.getAttribute(JwtAuthorizationTokenFilter.REQ_BODY));
		Login login = requestEntity.getBody();
		
    	try{
    		loginService.validateLoginFields(login, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				
				log.info("login:"+login);
				Authentication auth = authenticate(login.getUsername(), login.getPassword());
				securityObject = (SecurityObject) auth.getDetails();
				securityObject.setIpAddress(getClientIPAddresss(request));
				securityObject.setRequestFrom(CommonUtils.validateObject(securityObject.getRequestFrom()));
				securityObject.setAccessToken(loginService.prepareAccessToken(securityObject));
				log.info("login securityObject ::"+securityObject);
			}
			
			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				return new ResponseEntity<SecurityObject>(securityObject, HttpStatus.OK);
			}
	
			} catch (ApiException e) {
				return sendValidationError(e);
		
			} catch (AuthenticationException e) {
				errorList.add(e.getMessage());
				errors = setBusinessError(errorList);
				return sendUnprocessableEntity(errors);
				
			} catch (Exception e) {
				return sendServerError(e,GlobalVariables.FAIL);
			}
    }

    /**
     * Authenticates the user. If something is wrong, an {@link AuthenticationException} will be thrown
     */
    private Authentication authenticate(String userName, String password) {
        Objects.requireNonNull(userName);
        Objects.requireNonNull(password);
        try {
        	Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        	SecurityContextHolder.getContext().setAuthentication(authentication);
        	
        	return authentication;
        } catch (DisabledException e) {
            throw new AuthenticationException("User is disabled!", e);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Bad credentials!", e);
        }
    }
    
    @PostMapping(path = URI_FORGOT_PASSWORD, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "FORGOT PASSWORD REQUEST ", response = ApiResponseMessage.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
		@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
		@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class)
    })	
	public ResponseEntity<?> forgotPassword(HttpEntity<FpToken> requestEntity,HttpServletRequest request) {
		
		List<Errors> errors    = null;
		List<String> errorList = getListInstance();
		FpToken fp = requestEntity.getBody();
		
		try {
				loginService.forgotPasswordValidation(fp, errorList);
			 	if (isNotNullOrEmpty(errorList)) {
			 		errors = setValidationErrors(errorList);
				} else { 
					fp.setUserType(GlobalVariables.ROLE_EP);
					fp = loginService.forgotPasswordBusinessValidation(fp, errorList);
					if(isNotNullOrEmpty(errorList)){
						errors = setBusinessError(errorList);

					} else {
						// need to set twice because it is removed when fetched from businessValidation.
						fp.setUserType(GlobalVariables.ROLE_EP); 
						loginService.forgotPassword(fp);
					}
				}
			 	
			
			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				 //Search Success Case
				ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK, env.getProperty("msg_success_reset_pwd_link_sent"), null);
				return new ResponseEntity<ApiResponseMessage>(response, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);
	
		} catch (Exception e) {
			return sendServerError(e,GlobalVariables.FAIL);
		}
	
	}

    @GetMapping(path = URI_VALID_FORGOT_PASSWORD, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "VALIDATE FORGOT PASSWORD REQUEST ", response = ApiResponseMessage.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
		@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
		@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class)
    })
	public ResponseEntity<?> validateForgotPwdLink(@RequestParam(value = "q", defaultValue = StringUtils.EMPTY) String q,
			HttpServletRequest request) {
		List<Errors> errors    = null;
		List<String> errorList = getListInstance();
		
		try
		{
			loginService.validateTokenForgotPwd(decode(q),errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			}
		 
			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				// Search Success Case
				ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK,GlobalVariables.SUCCESS, null);
				return new ResponseEntity<ApiResponseMessage>(response, HttpStatus.OK);
			}

		} catch (Exception e) {
			return sendServerError(e,GlobalVariables.FAIL);
		}

	}

    @PostMapping( path = URI_RESET_PASSWORD, produces = { MediaType.APPLICATION_JSON_VALUE }, headers = { "Accept=application/json" })
    @ApiOperation(value = "RESET PASSWORD REQUEST ", response = ApiResponseMessage.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
		@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
		@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class)
    })	
	public ResponseEntity<?> resetPassword(HttpEntity<ResetPassword> requestEntity, HttpServletRequest request) {
		List<Errors> errors = null;
		List<String> errorList = getListInstance();
		
		try {

			ResetPassword resetPassword = requestEntity.getBody();
			loginService.validateResetPassword(resetPassword, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				loginService.resetPassword(resetPassword, errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);
				}
			}
		
			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK,env.getProperty("msg_success_reset_pwd"), null);
				return new ResponseEntity<ApiResponseMessage>(response, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);
	
		} catch (Exception e) {
			return sendServerError(e,GlobalVariables.FAIL);
		}
		
	}
    
}
