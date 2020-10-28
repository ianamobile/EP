package com.iana.api.service;

import java.util.List;

import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupEpTemplates;
import com.iana.api.domain.SetupMCDataJsonDTO;

public interface EPService {
	
	void validateEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount, List<String> errorList) throws Exception;
	
	void epMotorCarriersBusinessValidation(SecurityObject securityObject, SearchAccount searchAccount, Pagination pagination, List<String> errorList) throws Exception;

	List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	void validateMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount, List<String> errorList) throws Exception;
	
	SetupMCDataJsonDTO getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	SetupEpTemplates setupEpTemplates() throws Exception;
	
	AccountMaster getEPAccountInfo(String acctNo) throws Exception;
	
	
	
}

