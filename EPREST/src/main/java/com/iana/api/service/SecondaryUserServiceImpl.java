package com.iana.api.service;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.iana.api.dao.EpDao;
import com.iana.api.dao.UserDao;
import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.Role;
import com.iana.api.domain.SecUserDetails;
import com.iana.api.domain.SecurityObject;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

@Service
public class SecondaryUserServiceImpl extends CommonUtils implements SecondaryUserService {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private EpDao epDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private DataSource uiiaDataSource;

	@Override
	public void validateSecondaryUsers(SecurityObject securityObject, List<String> errorList) throws Exception {

		if (!GlobalVariables.ROLE_EP.equalsIgnoreCase(securityObject.getRoleName())) {
			errorList.add(env.getProperty("msg_error_unauthorized_access"));
			return;
		}
	}

	@Override
	public void secondaryUsersBusinessValidation(SecurityObject securityObject, SecUserDetails secUserDetails,
			Pagination pagination, List<String> errorList) throws Exception {

		epDao.countSecondaryUsers(securityObject, secUserDetails);
	}

	@Override
	public List<SecUserDetails> getSecondaryUsers(SecurityObject securityObject, SecUserDetails secUserDetails)
			throws Exception {
		return epDao.getSecondaryUsers(securityObject, secUserDetails);
	}

	@Override
	public void validateAddSecondaryUser(SecUserDetails secUserList, List<String> errorList) throws Exception {

		if (secUserList == null) {
			errorList.add(env.getProperty("msg_error_invalid_request"));
			return;
		}

		if (StringUtils.isBlank(secUserList.getUserName())) {
			errorList.add(env.getProperty("msg_error_empty_userName"));
		}

		if (StringUtils.isBlank(secUserList.getPassword())) {
			errorList.add(env.getProperty("msg_error_empty_password"));
		}
		if (StringUtils.isNotBlank(secUserList.getEmail())) {
			/*
			 * if(StringUtils.isBlank(secUserList.getEmail())) {
			 * errorList.add(env.getProperty("msg_error_empty_email")); } else
			 */if (!isValidEmail(secUserList.getEmail())) {
				errorList.add(env.getProperty("msg_error_invalid_email"));
			}
		}
	}

	@Override
	public void addSecondaryUserBusinessValidation(SecurityObject securityObject, SecUserDetails secUserList,
			List<String> errorList) throws Exception {

		SecUserDetails secondaryUser = ifExistsSecondaryUserName(securityObject.getAccountNumber(),
				secUserList.getUserName());

		if (secondaryUser.getSecUserId() != 0) {
			errorList.add(env.getProperty("msg_error_duplicete_sec_user"));
		}
	}

	private SecUserDetails ifExistsSecondaryUserName(String accountNumber, String userName) throws Exception {
		return epDao.ifExistsSecondaryUserName(accountNumber, userName);
	}

	@Override
	public void addSecondaryUser(SecurityObject securityObject, SecUserDetails secUserDetails) throws Exception {

		Role role = userDao.getRole(GlobalVariables.ROLE_IDD_SEC);

		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;

		try {

			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			AccountInfo accountInfo = new AccountInfo();

			accountInfo.setScacCode(securityObject.getScac());
			accountInfo.setPassword(secUserDetails.getPassword());
			accountInfo.setAccountNo(securityObject.getAccountNumber());
			accountInfo.setMemType((GlobalVariables.ROLE_IDD_SEC));
			accountInfo.setSecUserName(secUserDetails.getUserName());
			accountInfo.setOldSecUserName(secUserDetails.getOldUserName());
			accountInfo.setLoginAllwd(GlobalVariables.Y);

			userDao.insertPassword(this.uiiaDataSource, securityObject, accountInfo, role, true);
			epDao.addSecondaryUser(this.uiiaDataSource, securityObject, secUserDetails, true);

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			log.error("Exception:", e);
			transactionManager.rollback(status);
			throw e;
		}
	}

	@Override
	public void updateSecondaryUserBusinessValidation(SecurityObject securityObject, SecUserDetails secUserDetails,
			List<String> errorList) throws Exception {

		SecUserDetails secondaryUser = ifExistsSecondaryUserName(securityObject.getAccountNumber(),
				secUserDetails.getUserName());

		if (secUserDetails.getSecUserId() == 0 && (secUserDetails.getSecUserId() != secondaryUser.getSecUserId()
				|| secondaryUser.getSecUserId() == 0)) {
			errorList.add(env.getProperty("msg_error_invalid_request"));
		} else {
			if (StringUtils.isBlank(secUserDetails.getPassword())) {
				secUserDetails.setPassword(secondaryUser.getPassword());
			}
		}

	}

	@Override
	public void updateSecondaryUser(SecurityObject securityObject, SecUserDetails secUserList) throws Exception {
		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;

		try {
			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			AccountInfo accountInfo = new AccountInfo();

			accountInfo.setScacCode(securityObject.getScac());
			accountInfo.setPassword(secUserList.getPassword());
			accountInfo.setAccountNo(securityObject.getAccountNumber());
			accountInfo.setMemType(GlobalVariables.ROLE_IDD_SEC);
			accountInfo.setSecUserName(secUserList.getUserName());
			accountInfo.setOldSecUserName(secUserList.getOldUserName());
			accountInfo.setLoginAllwd(GlobalVariables.Y);

			userDao.updateLoginTbl(this.uiiaDataSource, securityObject, accountInfo, true);
			epDao.updateSecondaryUser(this.uiiaDataSource, securityObject, secUserList, true);

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			log.error("Exception:", e);
			transactionManager.rollback(status);
			throw e;
		}
	}

	@Override
	public void validateDeleteSecondaryUser(SecUserDetails secUserList, List<String> errorList) throws Exception {
		if (secUserList.getSecUserId() <= 0) {
			errorList.add(env.getProperty("msg_error_invalid_secondary_users"));
		}

	}

	@Override
	public void deleteSecondaryUserBusinessValidation(SecurityObject securityObject, SecUserDetails secUserList,
			List<String> errorList) throws Exception {

		int idCnt = epDao.countSecondaryUsersId(securityObject, secUserList.getSecUserId());
		if (idCnt == 0) {
			errorList.add(env.getProperty("msg_error_invalid_secondary_users_ids"));
		}
	}

	@Override
	public void deleteSecondaryUser(SecurityObject securityObject, SecUserDetails secUserList) throws Exception {
		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;

		try {

			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			secUserList = epDao.getSecondaryUserDetails(secUserList.getSecUserId());

			secUserList.setAuditTrailExtra(securityObject.getIpAddress());

			if (StringUtils.isNotBlank(securityObject.getAccountNumber()) && secUserList.getSecUserId() > 0) {
				AccountInfo accountInfo = new AccountInfo();
				accountInfo.setAccountNo(securityObject.getAccountNumber());
				accountInfo.setOldSecUserName(secUserList.getUserName());
				accountInfo.setUiiaStatus(GlobalVariables.DELETEDMEMBER);
				accountInfo.setMemType(GlobalVariables.ROLE_IDD_SEC);

				userDao.updateLoginTbl(this.uiiaDataSource, securityObject, accountInfo, true);
				epDao.deleteSecondaryUser(this.uiiaDataSource, secUserList, true);
			}

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			log.error("Exception:", e);
			transactionManager.rollback(status);
			throw e;
		}

	}

}
