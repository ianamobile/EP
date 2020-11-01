package com.iana.api.dao;

import java.util.List;

import javax.sql.DataSource;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.ContactDetail;
import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.LoginHistory;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.User;

public interface UserDao {
	
	User validate(Login login) throws Exception;
	
	List<AccountInfo> getAccountInfos(String accountNumber) throws Exception;
	
	LoginHistory getLastLogin(String userName, String accountNumber) throws Exception;
	
	Long createLoginHistory(SecurityObject securityObject) throws Exception;

	ContactDetail getContact(DataSource uiiaDataSource, String accountNumber, String contactAddType) throws Exception;

	FpToken getForgotPasswordTokenInfoByScac(FpToken fpToken) throws Exception;

	int updatePassword(ResetPassword resetPassword, FpToken fpToken) throws Exception;

	int changePassword(ResetPassword resetPassword, SecurityObject securityObject) throws Exception;
	
	int updateLoginTbl(DataSource lUIIADataSource, SecurityObject securityObject, AccountInfo accountInfo, boolean enableTransMgmt) throws Exception;
	
}