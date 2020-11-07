package com.iana.api.service.billing.payment;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.iana.api.dao.BillingUserDao;
import com.iana.api.dao.UserDao;
import com.iana.api.domain.Role;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.User;
import com.iana.api.domain.billing.payment.BillingUser;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.ValidationUtils;

@Service
public class BillingUserServiceImpl extends CommonUtils implements BillingUserService {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	BillingUserDao billingUserDao;

	@Autowired
	private DataSource uiiaDataSource;

	@Autowired
	UserDao userDao;

	@Override
	public List<BillingUser> billingUsers(SecurityObject securityObject) throws Exception {
		return billingUserDao.billingUsers(securityObject);
	}

	@Override
	public void validateDeleteBillingUser(SecurityObject securityObject, Long buId, BillingUser billingUser,
			List<String> errorList) {
		if (null == buId || buId <= 0) {
			errorList.add(env.getProperty("msg_error_invalid_request"));

		} else if (null == billingUser || StringUtils.isBlank(billingUser.getUserName())) {
			errorList.add(env.getProperty("msg_error_empty_userName"));
		}
	}

	@Override
	public void deleteBillingUserBusinessValidation(SecurityObject securityObject, BillingUser billingUser,
			List<String> errorList) throws Exception {
		BillingUser bu = billingUserDao.billingUser(billingUser.getBuId());
		if (null == bu || null == bu.getBuId()
				|| !bu.getAccountNumber().equalsIgnoreCase(securityObject.getAccountNumber())) {
			errorList.add(env.getProperty("msg_error_invalid_request"));
		} else {
			User user = userDao.user(securityObject.getAccountNumber(), billingUser.getUserName());
			if (null == user || null == user.getLoginId() || user.getLoginId() <= 0
					|| !bu.getUserName().equalsIgnoreCase(user.getUsername())) {
				errorList.add(env.getProperty("msg_error_invalid_request"));
			}
		}
	}

	@Override
	public void deleteBillingUser(SecurityObject securityObject, BillingUser billingUser) throws Exception {
		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;

		try {

			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			billingUserDao.deleteBillingUser(this.uiiaDataSource, securityObject, billingUser);

			userDao.updateUserForDeleteBillingUser(this.uiiaDataSource, securityObject, billingUser);

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			transactionManager.rollback(status);
			log.info("Exception has occured: " + e.getMessage());
			throw e;
		}

	}

	@Override
	public int saveBillingUser(BillingUser billingUser, SecurityObject securityObject) throws Exception {
		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;
		int affectedRows = 0;
		try {

			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			affectedRows = billingUserDao.saveBillingUser(this.uiiaDataSource, securityObject, billingUser);
			if (affectedRows > 0) {
				long iRoleId = 17;
				Role role = userDao.getRole(securityObject.getRoleName());
				if (role != null && role.getRoleId() != 0) {
					iRoleId = role.getRoleId();
				}

				userDao.insertUserForCreateBillingUser(this.uiiaDataSource, securityObject, billingUser, iRoleId);
			}

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			transactionManager.rollback(status);
			log.info("Exception has occured: " + e.getMessage());
			throw e;
		}
		return affectedRows;
	}

	@Override
	public void validateBillingUser(BillingUser billingUser, List<String> errorList, boolean isUpdate) {

		if (null == billingUser.getBuId() || billingUser.getBuId() == 0) {
			errorList.add(env.getProperty("msg_error_empty_billing_user_id"));
		}
		if (StringUtils.isBlank(billingUser.getUserName())) {
			errorList.add(env.getProperty("msg_error_empty_userName"));
		}
		if (StringUtils.isBlank(billingUser.getPassword())) {
			errorList.add(env.getProperty("msg_error_empty_password"));
		} else if (!ValidationUtils.isAlphaNumeric(billingUser.getPassword())) {
			errorList.add(env.getProperty("msg_error_alphanum_password"));
		}
		if (StringUtils.isBlank(billingUser.getFirstName())) {
			errorList.add(env.getProperty("msg_error_empty_firstName"));
		}
		if (StringUtils.isBlank(billingUser.getLastName())) {
			errorList.add(env.getProperty("msg_error_empty_lastName"));
		}
		if (StringUtils.isBlank(billingUser.getTitle())) {
			errorList.add(env.getProperty("msg_error_empty_title"));
		}
		if (StringUtils.isBlank(billingUser.getPhone())) {
			errorList.add(env.getProperty("msg_error_empty_phone"));
		}
		if (StringUtils.isBlank(billingUser.getEmail())) {
			errorList.add(env.getProperty("msg_error_empty_email"));
		} else if (!emailValidator(billingUser.getEmail())) {
			errorList.add(env.getProperty("msg_error_invalid_email"));
		}
	}

	@Override
	public int updateBillingUser(BillingUser billingUser, SecurityObject securityObject) throws Exception {
		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;
		int affectedRows = 0;
		try {

			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			affectedRows = billingUserDao.updateBillingUser(this.uiiaDataSource, securityObject, billingUser);
			if (affectedRows > 0) {
				userDao.updateUserForCreateBillingUser(this.uiiaDataSource, securityObject, billingUser);
			}

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			transactionManager.rollback(status);
			log.info("Exception has occured: " + e.getMessage());
			throw e;
		}
		return affectedRows;
	}

}
