package com.iana.api.service.billing.payment;

import java.util.List;

import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.billing.payment.BillingUser;

public interface BillingUserService {

	List<BillingUser> billingUsers(SecurityObject securityObject) throws Exception;

	void validateDeleteBillingUser(SecurityObject securityObject, Long buId, BillingUser billingUser,
			List<String> errorList);

	void deleteBillingUserBusinessValidation(SecurityObject securityObject, BillingUser billingUser,
			List<String> errorList) throws Exception;

	void deleteBillingUser(SecurityObject securityObject, BillingUser billingUser) throws Exception;

	void validateBillingUser(BillingUser billingUser, List<String> errorList, boolean isUpdate);

	int saveBillingUser(BillingUser billingUser, SecurityObject securityObject) throws Exception;

	int updateBillingUser(BillingUser billingUser, SecurityObject securityObject) throws Exception;

}
