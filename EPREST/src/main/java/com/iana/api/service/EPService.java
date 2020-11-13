package com.iana.api.service;

import java.util.List;

import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.EPAddendum;
import com.iana.api.domain.EPAddendumDetForm;
import com.iana.api.domain.EPTemplate;
import com.iana.api.domain.EPTerminalFeed;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MCCancel;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupAddendumDetails;
import com.iana.api.domain.SetupEpTemplates;
import com.iana.api.domain.SetupMCDataJsonDTO;
import com.iana.api.domain.SetupManageAccountInfo;

public interface EPService {

	void validateEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount, List<String> errorList)
			throws Exception;

	void epMotorCarriersBusinessValidation(SecurityObject securityObject, SearchAccount searchAccount,
			Pagination pagination, List<String> errorList) throws Exception;

	List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	void validateMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount, List<String> errorList)
			throws Exception;

	SetupMCDataJsonDTO getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	SetupEpTemplates setupEpTemplates() throws Exception;

	AccountMaster getEPAccountInfo(String acctNo) throws Exception;

	SetupManageAccountInfo setupManageAccountInfo();

	void validateManageAccountInfo(EPAcctInfo form, List<String> errorList) throws Exception;

	void updateManageAccountInfoBusinessValidation(AccountMaster epAcctInfo,
			List<String> errorList) throws Exception;
	
	void updateManageAccountInfo(SecurityObject securityObject, AccountMaster accountMaster) throws Exception;
	
	SetupAddendumDetails setupCurrentAddendumDetails();
	
	EPAddendumDetForm getCurrentAddendumDetails(SecurityObject securityObject) throws Exception;
	
	EPAddendum getTemplateDetails(EPTemplate epTemplate, String uvalidFlg) throws Exception;

	List<MCCancel> getDeletedMC(String cancRefStartDate, String cancRefEndDate, int pageIndex, int pageSize) throws Exception;

	List<EPTerminalFeed> getTerminalFeedLocations(String accountNumber) throws Exception;

	List<EPTemplate> searchEPTemplate(String searchTmplt, int pageIndex, String accountNumber) throws Exception;

}
