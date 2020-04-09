package com.iana.api.service;

import java.util.List;

import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.SecurityObject;

public interface LoginService {

	void validateLoginFields(Login login, List<String> errorList) throws Exception;
	
	String prepareAccessToken(SecurityObject userObject) throws Exception;

	void forgotPasswordValidation(FpToken fpToken, List<String> errorList) throws Exception;

	FpToken forgotPasswordBusinessValidation(FpToken fpToken, List<String> errorList) throws Exception;
	
	void forgotPassword(FpToken fpToken) throws Exception;

	void validateTokenForgotPwd(String q, List<String> errorList);

	void validateResetPassword(ResetPassword resetPassword, List<String> errorList) throws Exception;

	void resetPassword(ResetPassword resetPassword, List<String> errorList) throws Exception;
	
}
