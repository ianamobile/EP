package com.iana.api.dao;

import java.util.List;

import javax.sql.DataSource;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.ContactDetail;
import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.LoginHistory;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.Role;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.User;
import com.iana.api.domain.billing.payment.BillingUser;

public interface UserDao {

	User validate(Login login) throws Exception;

	List<AccountInfo> getAccountInfos(String accountNumber) throws Exception;

	LoginHistory getLastLogin(String userName, String accountNumber) throws Exception;

	Long createLoginHistory(SecurityObject securityObject) throws Exception;

	ContactDetail getContact(DataSource uiiaDataSource, String accountNumber, String contactAddType) throws Exception;

	FpToken getForgotPasswordTokenInfoByScac(FpToken fpToken) throws Exception;

	int updatePassword(ResetPassword resetPassword, FpToken fpToken) throws Exception;

	int changePassword(ResetPassword resetPassword, SecurityObject securityObject) throws Exception;

	int updateLoginTbl(DataSource lUIIADataSource, SecurityObject securityObject, AccountInfo accountInfo,
			boolean enableTransMgmt) throws Exception;

	User user(String accountNumber, String userName) throws Exception;

	void updateUserForDeleteBillingUser(DataSource lUIIADataSource, SecurityObject securityObject,
			BillingUser billingUser) throws Exception;

	Role getRole(String roleName) throws Exception;

	int updateUserForCreateBillingUser(DataSource lUIIADataSource, SecurityObject securityObject,
			BillingUser billingUser)  throws Exception;

	int insertUserForCreateBillingUser(DataSource lUIIADataSource, SecurityObject securityObject,
			BillingUser billingUser, long roleId) throws Exception;

	void insertPassword(DataSource lUIIADataSource, SecurityObject securityObject, AccountInfo acctInfo, Role role,
			boolean enableTransMgmt) throws Exception;

}