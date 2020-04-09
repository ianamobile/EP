package com.iana.api.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.dao.UserDao;
import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.SecurityObject;
import com.iana.api.token.JwtTokenGenerator;
import com.iana.api.token.JwtTokenValidator;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@Service
public class LoginServiceImpl extends CommonUtils implements LoginService {
	Logger log = LogManager.getLogger(this.getClass().getName());	

	@Autowired
	private JwtTokenGenerator jwtTokenGenerator;

	@Autowired
	private JwtTokenValidator jwtTokenValidator;

	@Autowired
	private UserDao userDao;

	
	@Override
	public String prepareAccessToken(SecurityObject userObject) {
		return jwtTokenGenerator.generateToken(userObject);
	}
	
	@Override
	public void validateLoginFields(Login login, List<String> errorList) throws Exception {

		if(null == login){
			errorList.add(env.getProperty("msg_error_invalid_request"));
			return;
		}
		
		if(StringUtils.isBlank(login.getUsername())) {
			errorList.add(env.getProperty("msg_error_empty_userName"));
		} 
		
		if(StringUtils.isBlank(login.getPassword()))
			errorList.add(env.getProperty("msg_error_empty_password"));
			
		if(StringUtils.isBlank(login.getRoleName())) {
			errorList.add(env.getProperty("msg_error_empty_role"));
		
		} else if(!GlobalVariables.ALLOWED_ROLES.contains(login.getRoleName().toUpperCase())) {
			errorList.add(env.getProperty("msg_error_invalid_role"));
		}
	}

	@Override
	public void forgotPasswordValidation(FpToken fpToken, List<String> errorList) throws Exception {
		if(null == fpToken){
			errorList.add(env.getProperty("msg_error_invalid_request"));
			return;
		}
		
		if(StringUtils.isBlank(fpToken.getScac())) {
			errorList.add(env.getProperty("msg_error_empty_scac"));
			
		} else if(fpToken.getScac().trim().length() < 2 || fpToken.getScac().trim().length() > 4) {
			errorList.add(env.getProperty("msg_error_length_scac"));
			
		} else if(!isAlpha(fpToken.getScac())) {
			errorList.add(env.getProperty("msg_error_char_scac"));
		}
		
	}
	
	@Override
	public FpToken forgotPasswordBusinessValidation(FpToken fpToken, List<String> errorList) throws Exception {
		fpToken = userDao.getForgotPasswordTokenInfoByScac(fpToken);

		if (null == fpToken.getId() || fpToken.getId() <= 0) {
			errorList.add(env.getProperty("msg_error_invalid_request"));
			return null;
			
		} else if (StringUtils.isBlank(fpToken.getEmail())) {
			errorList.add(env.getProperty("msg_error_email_not_exist"));
			return null;
		}

		String key = jwtTokenGenerator.generateForgotPwdToken(fpToken);

		if (StringUtils.isBlank(key)) {
			errorList.add(env.getProperty("msg_error_generate_token"));
		} else {
			fpToken.setKey(key);
		}
		
		return fpToken;
	}

	@Override
	public void forgotPassword(FpToken fpToken) throws Exception {
			String body = restService.prepareForgotPwdEmailBody(fpToken);
			notificationSender.sendEmailWithImage(new String[]{fpToken.getEmail()}, env.getProperty("emailProp.smtp.subject_reset_pwd"), body, 
						new String[] {env.getProperty("root_dir_path")+ env.getProperty("img_path_app_logo") });
	}

	@Override
	public void validateTokenForgotPwd(String q, List<String> errorList) {
		
		if(StringUtils.isBlank(q)) {
			errorList.add(env.getProperty("msg_error_empty_fp_token"));
			
		} else {
			
			try {
				
				jwtTokenValidator.parseForgotPwdToken(q);
	
			} catch(ExpiredJwtException ejt){
	        	log.error("ExpiredJwtException::", ejt);
	        	errorList.add(env.getProperty("msg_error_expired_fp_token"));
	        	
	        } catch (JwtException e) {
	            log.error("JwtException::", e);
	            errorList.add(env.getProperty("msg_error_invalid_fp_token"));
	        }
		}
	}

	@Override
	public void validateResetPassword(ResetPassword resetPassword, List<String> errorList) throws Exception {
		if(null == resetPassword) {
			errorList.add(env.getProperty("msg_error_invalid_request"));
			return;
		}
		
		if(StringUtils.isBlank(resetPassword.getNewPassword()) || StringUtils.isBlank(resetPassword.getConfirmPassword())){
			errorList.add(env.getProperty("msg_error_empty_password"));
			
		} else if(!resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
			errorList.add(env.getProperty("msg_error_match_password"));
		} else if(resetPassword.getNewPassword().length() > 35) {
			errorList.add(env.getProperty("msg_error_length_password"));
		
		}
		
		validateTokenForgotPwd(resetPassword.getQ(),errorList);

	}

	@Override
	public void resetPassword(ResetPassword resetPassword, List<String> errorList) throws Exception {
		FpToken fpToken = this.jwtTokenValidator.parseForgotPwdToken(resetPassword.getQ());
		int updateRecordCount = userDao.updatePassword(resetPassword, fpToken);
		
		if(updateRecordCount > 0){
			String body = restService.prepareResetPwdEmailBody(fpToken.getFirstName(), fpToken.getLastName());
			notificationSender.sendEmailWithImage(new String[] { fpToken.getEmail() }, env.getProperty("emailProp.smtp.subject_reset_pwd_success"), body,
													new String[] { env.getProperty("root_dir_path")+env.getProperty("img_path_app_logo") });
		}else{
			errorList.add(env.getProperty("msg_error_change_password"));
		}
	
	}

	
}
