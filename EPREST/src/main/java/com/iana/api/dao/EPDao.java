package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.AddressDet;
import com.iana.api.domain.ContactDet;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;

public interface EPDao {

	Long countEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;
	
	List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	List<MCDataJsonDTO> getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount) throws Exception;

	AccountInfo getBasicAcctDtls(String acctNo) throws Exception;
	
	AddressDet getAddress(String acctNo,String addressType) throws Exception;
	
	ContactDet getContact(String acctNo,String contactType) throws Exception;
	
	EPAcctInfo getEpAcctDtls(String acctNo) throws Exception;
}
