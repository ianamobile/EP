package com.iana.api.service;

import java.util.List;

import com.iana.api.domain.Pagination;
import com.iana.api.domain.SecUserDetails;
import com.iana.api.domain.SecurityObject;

public interface SecondaryUserService {

	void validateSecondaryUsers(SecurityObject securityObject, List<String> errorList) throws Exception;
	
	void secondaryUsersBusinessValidation(SecurityObject securityObject, SecUserDetails secUserList, Pagination pagination, List<String> errorList) throws Exception;

	List<SecUserDetails> getSecondaryUsers(SecurityObject securityObject, SecUserDetails secUserList) throws Exception;

	void validateAddSecondaryUser(SecUserDetails secUserList, List<String> errorList) throws Exception;

	void addSecondaryUserBusinessValidation(SecurityObject securityObject, SecUserDetails secUserList, List<String> errorList) throws Exception;

	void addSecondaryUser(SecurityObject securityObject, SecUserDetails secUserList) throws Exception;

	void updateSecondaryUserBusinessValidation(SecurityObject securityObject, SecUserDetails secUserList, List<String> errorList) throws Exception;

	void updateSecondaryUser(SecurityObject securityObject, SecUserDetails secUserList) throws Exception;

	void validateDeleteSecondaryUser(SecUserDetails secUserList, List<String> errorList) throws Exception;

	void deleteSecondaryUserBusinessValidation(SecurityObject securityObject, SecUserDetails secUserList, List<String> errorList) throws Exception;

	void deleteSecondaryUser(SecurityObject securityObject, SecUserDetails secUserList) throws Exception;
	
}
