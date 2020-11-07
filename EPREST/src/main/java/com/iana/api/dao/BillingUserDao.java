package com.iana.api.dao;

import java.util.List;

import javax.sql.DataSource;

import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.billing.payment.BillingUser;

public interface BillingUserDao {

	List<BillingUser> billingUsers(SecurityObject securityObject) throws Exception;

	BillingUser billingUser(Long buId) throws Exception;
	
	int deleteBillingUser(DataSource lUIIADataSource, SecurityObject securityObject, BillingUser billingUser) throws Exception;

	BillingUser billingUser(SecurityObject securityObject, Long buId) throws Exception;
	
	int saveBillingUser(DataSource lUIIADataSource, SecurityObject securityObject, BillingUser billingUser) throws Exception;

	int updateBillingUser(DataSource lUIIADataSource, SecurityObject securityObject, BillingUser billingUser)
			throws Exception;

}