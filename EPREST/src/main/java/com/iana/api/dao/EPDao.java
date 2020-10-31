package com.iana.api.dao;

import java.util.List;

import javax.sql.DataSource;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.AccountMaster;
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
	
	boolean ifExistsSameScac(String scac, String accountNumber) throws Exception;
	
	int countEPBasicAcctDtls(int id, String accountNumber) throws Exception;
	
	int countContactDtls(int contactId, String accountNumber, String contactType) throws Exception;
	
	int countAddressDtls(int addrId, String accountNumber, String contactType) throws Exception;
	
	int updateAcctDtls(DataSource lUIIADataSource, SecurityObject securityObject, AccountMaster acctbean, boolean enableTransMgmt) throws Exception;
	
	int updateAddress(DataSource lUIIADataSource, AddressDet addr, SecurityObject securityObject, boolean enableTransMgmt) throws Exception;

	int updateContact(DataSource lUIIADataSource, ContactDet contact, SecurityObject securityObject, boolean enableTransMgmt) throws Exception;
	
	int insertAddress(DataSource lUIIADataSource, AddressDet addr, String accountNumber, SecurityObject securityObject, boolean enableTransMgmt) throws Exception;
	
	int insertContact(DataSource lUIIADataSource, ContactDet contact, String accountNumber, SecurityObject securityObject, boolean enableTransMgmt) throws Exception;

	int updateRegDetailsEP(DataSource lUIIADataSource, EPAcctInfo epAcctInfo, SecurityObject securityObject, boolean enableTransMgmt) throws Exception;
}
