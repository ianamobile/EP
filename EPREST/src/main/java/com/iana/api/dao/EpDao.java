package com.iana.api.dao;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.AddendaDownload;
import com.iana.api.domain.AddressDet;
import com.iana.api.domain.ContactDet;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.EPMCSuspensionNotifPreference;
import com.iana.api.domain.EPTemplate;
import com.iana.api.domain.EPTerminalFeed;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MCAcctInfo;
import com.iana.api.domain.MCCancel;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.ScannedDoc;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecUserDetails;
import com.iana.api.domain.SecurityObject;

public interface EpDao {

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
	
	List<MCCancel> getDeletedMC(String cancRefStartDate, String cancRefEndDate, int pageIndex, int pageSize, String flag) throws Exception;

	List<EPTerminalFeed> getTerminalFeedLocations(String accountNumber) throws Exception;

	List<EPTemplate> getTemplateList(EPTemplate epTemplate, String accountNo) throws Exception;

	Long countSecondaryUsers(SecurityObject securityObject, SecUserDetails secUserDetails) throws Exception;

	List<SecUserDetails> getSecondaryUsers(SecurityObject securityObject, SecUserDetails secUserList) throws Exception;

	SecUserDetails ifExistsSecondaryUserName(String accountNumber, String userName) throws Exception;

	void addSecondaryUser(DataSource lUIIADataSource, SecurityObject securityObject, SecUserDetails secUserList,
			boolean enableTransMgmt) throws Exception;

	void updateSecondaryUser(DataSource lUIIADataSource, SecurityObject securityObject, SecUserDetails secUserList,
			boolean enableTransMgmt) throws Exception;

	int countSecondaryUsersId(SecurityObject securityObject, int selectedId) throws Exception;

	SecUserDetails getSecondaryUserDetails(int secUserId) throws Exception;

	void deleteSecondaryUser(DataSource lUIIADataSource, SecUserDetails secUserList, boolean enableTransMgmt)
			throws Exception;

	List<AccountInfo> searchMemberArch(SearchAccount searchAccount, int pageIndex, int pageSize) throws Exception;

	String getEPAccountNumber(String epName, String epSCAC) throws Exception;

	MCAcctInfo getMCBasicInfo(SearchAccount searchparams) throws Exception;

	Map<String, Object> getInPlacePolicyForMC(SearchAccount searchparams) throws Exception;

	boolean getAreqFlag(String epAccNo) throws Exception;

	List<ScannedDoc> getScanDoc(String mcAcctNo) throws Exception;

	List<AddendaDownload> getPreviousTemplatesList(AddendaDownload epTemplate, int pageIndex, int pageSize) throws Exception;

	List<String> getEpMcUsdotStatusReportsList(int pageIndex, int pageSize, String flag) throws Exception;

	List<EPMCSuspensionNotifPreference> getEPMCSuspensionNotifPref(Long notifPreferenceSelectedByEP) throws Exception;

	Long getNotifPreferenceSelectedByEP(String accountNumber) throws Exception;

	boolean ifExistsEpMcNotifPreferenceForAccount(String accountNumber) throws Exception;

	void insertEPMCSuspensionNotification(SecurityObject securityObject, Long notifPreferenceSelection)
			throws Exception;

	void updateEPMCSuspensionNotification(SecurityObject securityObject, Long notifPreferenceSelection,
			Long notifPreferenceSelectionDB) throws Exception;

	boolean ifExistsOldEpMcNotifPreference(Long notifPreferenceSelection, String accountNumber) throws Exception;
}
