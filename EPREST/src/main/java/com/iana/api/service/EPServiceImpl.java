package com.iana.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.iana.api.dao.EpAddendumDao;
import com.iana.api.dao.EpAdditonalReqDao;
import com.iana.api.dao.EpDao;
import com.iana.api.dao.EpInsuranceDao;
import com.iana.api.dao.EpSwitchesDao;
import com.iana.api.dao.UserDao;
import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.AdditionalReq;
import com.iana.api.domain.AddressDet;
import com.iana.api.domain.ContactDet;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.EPAddendum;
import com.iana.api.domain.EPAddendumDetForm;
import com.iana.api.domain.EPInsNeeds;
import com.iana.api.domain.EPTemplate;
import com.iana.api.domain.EPTerminalFeed;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.LabelValueForm;
import com.iana.api.domain.MCCancel;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.MultipleLimit;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupAddendumDetails;
import com.iana.api.domain.SetupEpTemplates;
import com.iana.api.domain.SetupMCDataJsonDTO;
import com.iana.api.domain.SetupManageAccountInfo;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.CommonValidations;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Service
public class EPServiceImpl extends CommonUtils implements EPService {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private EpDao epDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EpAddendumDao epAddendumDao;

	@Autowired
	private EpAdditonalReqDao epAdditonalReqDao;

	@Autowired
	private EpInsuranceDao epInsuranceDao;

	@Autowired
	private EpSwitchesDao epSwitchesDao;

	@Autowired
	private DataSource uiiaDataSource;

	EPAddendumDetForm epAddendumDetForm = new EPAddendumDetForm();

	@Override
	public void validateEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount,
			List<String> errorList) throws Exception {

		if (!GlobalVariables.ROLE_EP.equalsIgnoreCase(securityObject.getRoleName())) {
			errorList.add(env.getProperty("msg_error_unauthorized_access"));
			return;
		}

		// check accountNumber start with EP
		if (StringUtils.isNotBlank(securityObject.getAccountNumber())
				&& securityObject.getAccountNumber().length() > 2) {

			searchAccount.setUserType(securityObject.getAccountNumber().substring(0, 2));

			if (!(GlobalVariables.EQUIPMENTPROVIDER.equalsIgnoreCase(searchAccount.getUserType()))) {
				errorList.add(env.getProperty("msg_error_invalid_account_number"));
				return;
			}
		}
	}

	@Override
	public void epMotorCarriersBusinessValidation(SecurityObject securityObject, SearchAccount searchAccount,
			Pagination pagination, List<String> errorList) throws Exception {

		Long recordCount = epDao.countEPMotorCarriers(securityObject, searchAccount);
		restService.pageSetup(searchAccount, errorList, pagination, recordCount);

	}

	@Override
	public List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount)
			throws Exception {

		return epDao.getEPMotorCarriers(securityObject, searchAccount);
	}

	@Override
	public void validateMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount,
			List<String> errorList) throws Exception {
		if (!GlobalVariables.ROLE_EP.equalsIgnoreCase(securityObject.getRoleName())) {
			errorList.add(env.getProperty("msg_error_unauthorized_access"));
			return;
		}
	}

	@Override
	public SetupMCDataJsonDTO getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount)
			throws Exception {

		List<MCDataJsonDTO> mcData = epDao.getMCLookUpForEP(securityObject, searchAccount);
		List<LabelValueForm> result = new ArrayList<>();
		for (MCDataJsonDTO mcDataJsonDTO : mcData) {
			result.add(new LabelValueForm(
					(mcDataJsonDTO.getCompanyName() + ":" + mcDataJsonDTO.getMcScac() + ":"
							+ mcDataJsonDTO.getMcEPStatus() + ":" + mcDataJsonDTO.getEpMemberFlag()),
					(mcDataJsonDTO.getCompanyName() + ":" + mcDataJsonDTO.getMcScac() + ":"
							+ mcDataJsonDTO.getAccountNumber())));
		}

		SetupMCDataJsonDTO setupMCDataJsonDTO = new SetupMCDataJsonDTO();
		setupMCDataJsonDTO.setResults(result);

		return setupMCDataJsonDTO;
	}

	@Override
	public SetupEpTemplates setupEpTemplates() throws Exception {
		SetupEpTemplates setupEpTemplates = new SetupEpTemplates();
		setupEpTemplates.setEpTemplates(restService.epTemplates());
		return setupEpTemplates;
	}

	@Override
	public AccountMaster getEPAccountInfo(String acctNo) throws Exception {
//		AccountMaster accountMaster = new AccountMaster();
		System.out.println("---acctNo=" + acctNo);
		EPAcctInfo epAcctInfo = epDao.getEpAcctDtls(acctNo);
		epAcctInfo.setPrevNotes(Utility.removePaddingFrmNotes(epAcctInfo.getEpNotes()));

		AccountInfo accountInfo = epDao.getBasicAcctDtls(acctNo);
		accountInfo.setOldScac(accountInfo.getScacCode());
		accountInfo.setOldUiiaStatus(accountInfo.getUiiaStatus());
		if (accountInfo.getIddMember().equals(GlobalVariables.YES)) {
			accountInfo.setIddStatus(GlobalVariables.ACTIVEMEMBER);
		} else if (accountInfo.getIddMember().equals(GlobalVariables.NO)) {
			accountInfo.setIddStatus(GlobalVariables.DELETEDMEMBER);
		}
		epAcctInfo.setAcctInfo(accountInfo);

		AddressDet cntctAdd = epDao.getAddress(acctNo, GlobalVariables.CONTACTADDTYPE);
		epAcctInfo.setCntctAdd(cntctAdd);
		ContactDet cntctInfo = epDao.getContact(acctNo, GlobalVariables.CONTACTADDTYPE);
		epAcctInfo.setCntctInfo(cntctInfo);
		AddressDet billAdd = epDao.getAddress(acctNo, GlobalVariables.BILLADDRESSTYPE);
		epAcctInfo.setBillAdd(billAdd);
		ContactDet billInfo = epDao.getContact(acctNo, GlobalVariables.BILLADDRESSTYPE);
		epAcctInfo.setBillInfo(billInfo);
		AddressDet disputeAdd = epDao.getAddress(acctNo, GlobalVariables.DISPUTEADDRESSTYPE);
		epAcctInfo.setDisputeAdd(disputeAdd);
		ContactDet disputeInfo = epDao.getContact(acctNo, GlobalVariables.DISPUTEADDRESSTYPE);
		epAcctInfo.setDisputeInfo(disputeInfo);
		return epAcctInfo;

	}

	@Override
	public SetupManageAccountInfo setupManageAccountInfo() {
		SetupManageAccountInfo setupManageAccountInfo = new SetupManageAccountInfo();

		setupManageAccountInfo.setEquipProviderType(restService.populateEquipProviderTypeList());
		setupManageAccountInfo.setUiiaStatus(restService.populateUiiaStatusList());
		setupManageAccountInfo.setNamePrefixList(restService.populateNamePrefixList());
		setupManageAccountInfo.setServiceLevels(restService.populateServiceLevelList());

		return setupManageAccountInfo;
	}

	@Override
	public void validateManageAccountInfo(EPAcctInfo epAcctInfo, List<String> errorList) throws Exception {

		validateAccountInfo(epAcctInfo, errorList);

	}

	private void validateAccountInfo(EPAcctInfo epAcctInfo, List<String> errorList) {
		if (StringUtils.isNotBlank(epAcctInfo.getAcctInfo().getScacCode())) {
			epAcctInfo.getAcctInfo().setScacCode((epAcctInfo.getAcctInfo().getScacCode().toUpperCase()));
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getAcctInfo().getScacCode(), 4)) {
				errorList.add(env.getProperty("msg_error_length_scac"));
			} else if (!CommonUtils.isMatchingWithRegex("^[A-Z]*$", epAcctInfo.getAcctInfo().getScacCode())) {
				errorList.add(env.getProperty("msg_error_char_scac"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctInfo().getContctFname())) {
			errorList.add(env.getProperty("msg_error_empty_contact_fname"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctFname(), 60)) {
			errorList.add(env.getProperty("msg_error_length_contact_fname_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
				epAcctInfo.getCntctInfo().getContctFname())) {
			errorList.add(env.getProperty("msg_error_pattern_contact_fname"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctInfo().getContctMname())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctMname(), 60)) {
				errorList.add(env.getProperty("msg_error_length_contact_mname_60"));
			} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
					epAcctInfo.getCntctInfo().getContctMname())) {
				errorList.add(env.getProperty("msg_error_pattern_contact_mname"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctInfo().getContctLname())) {
			errorList.add(env.getProperty("msg_error_empty_contact_lname"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctLname(), 60)) {
			errorList.add(env.getProperty("msg_error_length_contact_lname_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
				epAcctInfo.getCntctInfo().getContctLname())) {
			errorList.add(env.getProperty("msg_error_pattern_contact_lname"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctInfo().getContctSalutation())
				&& !CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctSalutation(), 50)) {
			errorList.add(env.getProperty("msg_error_length_contact_salutation_50"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctInfo().getContctSuffix())
				&& epAcctInfo.getCntctInfo().getContctSuffix().length() > 10) {
			errorList.add(env.getProperty("msg_error_length_contact_suffix_10"));
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctInfo().getContctTitle())) {
			errorList.add(env.getProperty("msg_error_empty_contact_title"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctTitle(), 60)) {
			errorList.add(env.getProperty("msg_error_length_contact_title_60"));
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctInfo().getContctPrmEmail())) {
			errorList.add(env.getProperty("msg_error_empty_contact_email"));
		} else if (!CommonUtils.isValidEmail(epAcctInfo.getCntctInfo().getContctPrmEmail())) {
			errorList.add(env.getProperty("msg_error_invalid_contact_email"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctInfo().getContctSecEmail())) {
			if (!CommonUtils.isValidEmail(epAcctInfo.getCntctInfo().getContctSecEmail())) {
				errorList.add(env.getProperty("msg_error_invalid_contact_sec_email"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctAdd().getAddrStreet1())) {
			errorList.add(env.getProperty("msg_error_empty_contact_addr1"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctAdd().getAddrStreet1(), 200)) {
			errorList.add(env.getProperty("msg_error_length_contact_addr1_200"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctAdd().getAddrStreet2())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctAdd().getAddrStreet2(), 200)) {
				errorList.add(env.getProperty("msg_error_length_contact_addr2_200"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctAdd().getAddrZip())) {
			errorList.add(env.getProperty("msg_error_empty_contact_zipcode"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ZIP_CODE_PATTERN,
				epAcctInfo.getCntctAdd().getAddrZip())) {
			errorList.add(env.getProperty("msg_error_pattern_length_contact_zipcode"));
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctAdd().getAddrCity())) {
			errorList.add(env.getProperty("msg_error_empty_contact_city"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctAdd().getAddrCity(), 60)) {
			errorList.add(env.getProperty("msg_error_length_contact_city_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.CITY_PATTERN,
				epAcctInfo.getCntctAdd().getAddrCity())) {
			errorList.add(env.getProperty("msg_error_pattern_contact_city"));
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctAdd().getAddrState())) {
			errorList.add(env.getProperty("msg_error_empty_contact_state"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctAdd().getAddrState(), 60)) {
			errorList.add(env.getProperty("msg_error_length_contact_state_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ALPHABET_WITH_SPACE_PATTERN,
				epAcctInfo.getCntctAdd().getAddrState())) {
			errorList.add(env.getProperty("msg_error_pattern_contact_state"));
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctAdd().getAddrCountry())) {
			errorList.add(env.getProperty("msg_error_empty_contact_country"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctAdd().getAddrCountry(), 60)) {
			errorList.add(env.getProperty("msg_error_length_contact_country_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ALPHABET_WITH_SPACE_PATTERN,
				epAcctInfo.getCntctAdd().getAddrCountry())) {
			errorList.add(env.getProperty("msg_error_pattern_contact_country"));
		}

		if (StringUtils.isBlank(epAcctInfo.getCntctInfo().getContctPrmPhone())) {
			errorList.add(env.getProperty("msg_error_empty_contact_phone"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctPrmPhone(), 24)) {
			errorList.add(env.getProperty("msg_error_length_contact_phone_24"));
		} /*
			 * else if(!CommonUtils.isMatchingWithRegex(GlobalVariables.PHONE_PATTERN,
			 * epAcctInfo.getCntctInfo().getContctPrmPhone())) {
			 * errorList.add(env.getProperty("msg_error_pattern_contact_phone")); }
			 */

		if (StringUtils.isBlank(epAcctInfo.getCntctInfo().getContctPrmFax())) {
			errorList.add(env.getProperty("msg_error_empty_contact_fax"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctPrmFax(), 13)) {
			errorList.add(env.getProperty("msg_error_length_contact_fax_13"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.FAX_PATTERN,
				epAcctInfo.getCntctInfo().getContctPrmFax())) {
			errorList.add(env.getProperty("msg_error_pattern_contact_fax"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctInfo().getContctSecPhone())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctSecPhone(), 24)) {
				errorList.add(env.getProperty("msg_error_length_sec_contact_phone_24"));
			}
		}

		if (StringUtils.isNotBlank(epAcctInfo.getCntctInfo().getContctSecFax())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getCntctInfo().getContctSecFax(), 13)) {
				errorList.add(env.getProperty("msg_error_length_sec_contact_fax_13"));
			} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.FAX_PATTERN,
					epAcctInfo.getCntctInfo().getContctSecFax())) {
				errorList.add(env.getProperty("msg_error_pattern_sec_contact_fax"));
			}
		}

		if (GlobalVariables.NO.equalsIgnoreCase(epAcctInfo.getCntctAdd().getSameBillAddr())) {
			validateBillingContact(epAcctInfo, errorList);
		}

		if (GlobalVariables.NO.equalsIgnoreCase(epAcctInfo.getCntctAdd().getSameDisputeAddr())) {
			validateDisputeContact(epAcctInfo, errorList);
		}

	}

	private void validateBillingContact(EPAcctInfo epAcctInfo, List<String> errorList) {
		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctFname())) {
			errorList.add(env.getProperty("msg_error_empty_bill_fname"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctFname(), 60)) {
			errorList.add(env.getProperty("msg_error_length_bill_fname_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
				epAcctInfo.getBillInfo().getContctFname())) {
			errorList.add(env.getProperty("msg_error_pattern_bill_fname"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctMname())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctMname(), 60)) {
				errorList.add(env.getProperty("msg_error_length_bill_mname_60"));
			} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.MIDDLE_NAME_PATTERN,
					epAcctInfo.getBillInfo().getContctMname())) {
				errorList.add(env.getProperty("msg_error_pattern_bill_mname"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctLname())) {
			errorList.add(env.getProperty("msg_error_empty_bill_lname"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctLname(), 60)) {
			errorList.add(env.getProperty("msg_error_length_bill_lname_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
				epAcctInfo.getBillInfo().getContctLname())) {
			errorList.add(env.getProperty("msg_error_pattern_bill_lname"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSalutation())
				&& !CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctSalutation(), 50)) {
			errorList.add(env.getProperty("msg_error_length_bill_salutation_50"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSuffix())
				&& epAcctInfo.getBillInfo().getContctSuffix().length() > 10) {
			errorList.add(env.getProperty("msg_error_length_bill_suffix_10"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctTitle())) {
			errorList.add(env.getProperty("msg_error_empty_bill_title"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctTitle(), 60)) {
			errorList.add(env.getProperty("msg_error_length_bill_title_60"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctPrmEmail())) {
			errorList.add(env.getProperty("msg_error_empty_bill_email"));
		} else if (!CommonUtils.isValidEmail(epAcctInfo.getBillInfo().getContctPrmEmail())) {
			errorList.add(env.getProperty("msg_error_invalid_bill_email"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrStreet1())) {
			errorList.add(env.getProperty("msg_error_empty_bill_addr1"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrStreet1(), 200)) {
			errorList.add(env.getProperty("msg_error_length_bill_addr1_200"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillAdd().getAddrStreet2())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrStreet2(), 200)) {
				errorList.add(env.getProperty("msg_error_length_bill_addr2_200"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrZip())) {
			errorList.add(env.getProperty("msg_error_empty_bill_zipcode"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ZIP_CODE_PATTERN,
				epAcctInfo.getBillAdd().getAddrZip())) {
			errorList.add(env.getProperty("msg_error_pattern_length_bill_zipcode"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrCity())) {
			errorList.add(env.getProperty("msg_error_empty_bill_city"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrCity(), 60)) {
			errorList.add(env.getProperty("msg_error_length_bill_city_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.CITY_PATTERN,
				epAcctInfo.getBillAdd().getAddrCity())) {
			errorList.add(env.getProperty("msg_error_pattern_bill_city"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrState())) {
			errorList.add(env.getProperty("msg_error_empty_bill_state"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrState(), 60)) {
			errorList.add(env.getProperty("msg_error_length_bill_state_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ALPHABET_WITH_SPACE_PATTERN,
				epAcctInfo.getBillAdd().getAddrState())) {
			errorList.add(env.getProperty("msg_error_pattern_bill_state"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrCountry())) {
			errorList.add(env.getProperty("msg_error_empty_bill_country"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrCountry(), 60)) {
			errorList.add(env.getProperty("msg_error_length_bill_country_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ALPHABET_WITH_SPACE_PATTERN,
				epAcctInfo.getBillAdd().getAddrCountry())) {
			errorList.add(env.getProperty("msg_error_pattern_bill_country"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctPrmPhone())) {
			errorList.add(env.getProperty("msg_error_empty_bill_phone"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctPrmFax())) {
			errorList.add(env.getProperty("msg_error_empty_bill_fax"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctPrmFax(), 13)) {
			errorList.add(env.getProperty("msg_error_length_bill_fax_13"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.FAX_PATTERN,
				epAcctInfo.getBillInfo().getContctPrmFax())) {
			errorList.add(env.getProperty("msg_error_pattern_bill_fax"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSecPhone())) {
			if (!CommonUtils.isMatchingWithRegex(GlobalVariables.PHONE_PATTERN,
					epAcctInfo.getBillInfo().getContctSecPhone())) {
				errorList.add(env.getProperty("msg_error_pattern_sec_bill_phone"));
			}
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSecFax())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctSecFax(), 13)) {
				errorList.add(env.getProperty("msg_error_length_sec_bill_fax_13"));
			} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.FAX_PATTERN,
					epAcctInfo.getBillInfo().getContctSecFax())) {
				errorList.add(env.getProperty("msg_error_pattern_sec_bill_fax"));
			}
		}
	}

	private void validateDisputeContact(EPAcctInfo epAcctInfo, List<String> errorList) {
		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctFname())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_fname"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctFname(), 60)) {
			errorList.add(env.getProperty("msg_error_length_dispute_fname_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
				epAcctInfo.getBillInfo().getContctFname())) {
			errorList.add(env.getProperty("msg_error_pattern_dispute_fname"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctMname())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctMname(), 60)) {
				errorList.add(env.getProperty("msg_error_length_dispute_mname_60"));
			} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.MIDDLE_NAME_PATTERN,
					epAcctInfo.getBillInfo().getContctMname())) {
				errorList.add(env.getProperty("msg_error_pattern_dispute_mname"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctLname())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_lname"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctLname(), 60)) {
			errorList.add(env.getProperty("msg_error_length_dispute_lname_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.NAME_PATTERN,
				epAcctInfo.getBillInfo().getContctLname())) {
			errorList.add(env.getProperty("msg_error_pattern_dispute_lname"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSalutation())
				&& !CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctSalutation(), 50)) {
			errorList.add(env.getProperty("msg_error_length_dispute_salutation_50"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSuffix())
				&& epAcctInfo.getBillInfo().getContctSuffix().length() > 10) {
			errorList.add(env.getProperty("msg_error_length_dispute_suffix_10"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctTitle())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_title"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctTitle(), 60)) {
			errorList.add(env.getProperty("msg_error_length_dispute_title_60"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctPrmEmail())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_email"));
		} else if (!CommonUtils.isValidEmail(epAcctInfo.getBillInfo().getContctPrmEmail())) {
			errorList.add(env.getProperty("msg_error_invalid_dispute_email"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrStreet1())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_addr1"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrStreet1(), 200)) {
			errorList.add(env.getProperty("msg_error_length_dispute_addr1_200"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillAdd().getAddrStreet2())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrStreet2(), 200)) {
				errorList.add(env.getProperty("msg_error_length_dispute_addr2_200"));
			}
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrZip())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_zipcode"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ZIP_CODE_PATTERN,
				epAcctInfo.getBillAdd().getAddrZip())) {
			errorList.add(env.getProperty("msg_error_pattern_length_dispute_zipcode"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrCity())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_city"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrCity(), 60)) {
			errorList.add(env.getProperty("msg_error_length_dispute_city_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.CITY_PATTERN,
				epAcctInfo.getBillAdd().getAddrCity())) {
			errorList.add(env.getProperty("msg_error_pattern_dispute_city"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrState())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_state"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrState(), 60)) {
			errorList.add(env.getProperty("msg_error_length_dispute_state_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ALPHABET_WITH_SPACE_PATTERN,
				epAcctInfo.getBillAdd().getAddrState())) {
			errorList.add(env.getProperty("msg_error_pattern_dispute_state"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillAdd().getAddrCountry())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_country"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillAdd().getAddrCountry(), 60)) {
			errorList.add(env.getProperty("msg_error_length_dispute_country_60"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.ALPHABET_WITH_SPACE_PATTERN,
				epAcctInfo.getBillAdd().getAddrCountry())) {
			errorList.add(env.getProperty("msg_error_pattern_dispute_country"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctPrmPhone())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_phone"));
		}

		if (StringUtils.isBlank(epAcctInfo.getBillInfo().getContctPrmFax())) {
			errorList.add(env.getProperty("msg_error_empty_dispute_fax"));
		} else if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctPrmFax(), 13)) {
			errorList.add(env.getProperty("msg_error_length_dispute_fax_13"));
		} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.FAX_PATTERN,
				epAcctInfo.getBillInfo().getContctPrmFax())) {
			errorList.add(env.getProperty("msg_error_pattern_dispute_fax"));
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSecPhone())) {
			if (!CommonUtils.isMatchingWithRegex(GlobalVariables.PHONE_PATTERN,
					epAcctInfo.getBillInfo().getContctSecPhone())) {
				errorList.add(env.getProperty("msg_error_pattern_sec_dispute_phone"));
			}
		}

		if (StringUtils.isNotBlank(epAcctInfo.getBillInfo().getContctSecFax())) {
			if (!CommonUtils.isValidMaxLength(epAcctInfo.getBillInfo().getContctSecFax(), 13)) {
				errorList.add(env.getProperty("msg_error_length_sec_dispute_fax_13"));
			} else if (!CommonUtils.isMatchingWithRegex(GlobalVariables.FAX_PATTERN,
					epAcctInfo.getBillInfo().getContctSecFax())) {
				errorList.add(env.getProperty("msg_error_pattern_sec_dispute_fax"));
			}
		}
	}

	@Override
	public void updateManageAccountInfoBusinessValidation(AccountMaster accountMaster, List<String> errorList)
			throws Exception {

		EPAcctInfo epAcctInfo = (EPAcctInfo) accountMaster;

		if (StringUtils.isNotBlank(epAcctInfo.getAcctInfo().getScacCode())) {
			if (epDao.ifExistsSameScac(epAcctInfo.getAcctInfo().getScacCode(),
					epAcctInfo.getAcctInfo().getAccountNo())) {
				errorList.add(env.getProperty("msg_error_exist_scac"));
			}
		}

		if (epDao.countEPBasicAcctDtls(epAcctInfo.getEpBasicInfoId(), epAcctInfo.getAcctInfo().getAccountNo()) <= 0
				|| epDao.countContactDtls(epAcctInfo.getCntctInfo().getContctId(),
						epAcctInfo.getAcctInfo().getAccountNo(), GlobalVariables.CONTACTADDTYPE) <= 0
				|| epDao.countAddressDtls(epAcctInfo.getCntctAdd().getAddrId(), epAcctInfo.getAcctInfo().getAccountNo(),
						GlobalVariables.CONTACTADDTYPE) <= 0) {

			errorList.add(env.getProperty("msg_error_invalid_request"));
		}

		if (GlobalVariables.NO.equalsIgnoreCase(epAcctInfo.getCntctAdd().getSameBillAddr())
				&& isNullOrEmpty(errorList)) {
			/* if billing address and contact details already exist */
			if (epAcctInfo.getBillAdd().getAddrId() != 0 && epAcctInfo.getBillInfo().getContctId() != 0) {
				if (epDao.countContactDtls(epAcctInfo.getBillInfo().getContctId(),
						epAcctInfo.getAcctInfo().getAccountNo(), GlobalVariables.BILLADDRESSTYPE) <= 0
						|| epDao.countAddressDtls(epAcctInfo.getBillAdd().getAddrId(),
								epAcctInfo.getAcctInfo().getAccountNo(), GlobalVariables.BILLADDRESSTYPE) <= 0) {

					errorList.add(env.getProperty("msg_error_invalid_request"));
				}
			}
		}

		if (GlobalVariables.NO.equalsIgnoreCase(epAcctInfo.getCntctAdd().getSameDisputeAddr())
				&& isNullOrEmpty(errorList)) {
			/* if dispute address and contact details already exist */
			if (epAcctInfo.getDisputeAdd().getAddrId() != 0 && epAcctInfo.getDisputeInfo().getContctId() != 0) {
				if (epDao.countContactDtls(epAcctInfo.getDisputeInfo().getContctId(),
						epAcctInfo.getAcctInfo().getAccountNo(), GlobalVariables.BILLADDRESSTYPE) <= 0
						|| epDao.countAddressDtls(epAcctInfo.getDisputeAdd().getAddrId(),
								epAcctInfo.getAcctInfo().getAccountNo(), GlobalVariables.BILLADDRESSTYPE) <= 0) {

					errorList.add(env.getProperty("msg_error_invalid_request"));
				}
			}
		}

	}

	@Override
	public void updateManageAccountInfo(SecurityObject securityObject, AccountMaster accountMaster) throws Exception {

		String isSameBillInfo = "";
		String isSameDisputeInfo = "";

		EPAcctInfo epAcctInfo = (EPAcctInfo) accountMaster;

		log.debug("Updating existing member registration data");

		PlatformTransactionManager transactionManager = null;
		TransactionStatus status = null;

		try {

			transactionManager = restService.getTransactionManager(this.uiiaDataSource);
			status = restService.beginTransAndGetStatus(transactionManager);

			/* updating account_info table */
			int dbStatus = epDao.updateAcctDtls(this.uiiaDataSource, securityObject, epAcctInfo, true);

			/* checking whether billing information is same */
			if (GlobalVariables.YES.equals(epAcctInfo.getCntctAdd().getSameBillAddr())) {
				log.debug("If billing info is same as contact info");
				isSameBillInfo = GlobalVariables.YES;
			} else if (GlobalVariables.NO.equalsIgnoreCase(epAcctInfo.getCntctAdd().getSameBillAddr())) {
				log.debug("If billing info is not same as contact info");
				isSameBillInfo = GlobalVariables.NO;
			}

			/* checking whether dispute information is same */
			if (GlobalVariables.YES.equals(epAcctInfo.getCntctAdd().getSameDisputeAddr())) {
				log.debug("If dispute info is same as contact info");
				isSameDisputeInfo = GlobalVariables.YES;
			} else if (GlobalVariables.NO.equalsIgnoreCase(epAcctInfo.getCntctAdd().getSameDisputeAddr())) {
				log.debug("If dispute info is not same as contact info");
				isSameDisputeInfo = GlobalVariables.NO;
			}

			/* updating UIIA contact address */
			epAcctInfo.getCntctAdd().setAddrType(GlobalVariables.CONTACTADDTYPE);

			dbStatus = epDao.updateAddress(this.uiiaDataSource, epAcctInfo.getCntctAdd(), securityObject, true);

			/* updating UIIA contact details */
			epAcctInfo.getCntctInfo().setContctType(GlobalVariables.CONTACTADDTYPE);

			dbStatus = epDao.updateContact(this.uiiaDataSource, epAcctInfo.getCntctInfo(), securityObject, true);

			if (GlobalVariables.NO.equalsIgnoreCase(isSameBillInfo)) {
				/* if billing address and contact details already exist */
				if (epAcctInfo.getBillAdd().getAddrId() != 0 && epAcctInfo.getBillInfo().getContctId() != 0) {
					/* updating billing address */
					epAcctInfo.getBillAdd().setAddrType(GlobalVariables.BILLADDRESSTYPE);
					dbStatus = epDao.updateAddress(this.uiiaDataSource, epAcctInfo.getBillAdd(), securityObject, true);

					/* updating billing contact details */
					epAcctInfo.getBillInfo().setContctType(GlobalVariables.BILLADDRESSTYPE);
					dbStatus = epDao.updateContact(this.uiiaDataSource, epAcctInfo.getBillInfo(), securityObject, true);
				} else {
					/* inserting billing address */
					epAcctInfo.getBillAdd().setAddrType(GlobalVariables.BILLADDRESSTYPE);

					dbStatus = epDao.insertAddress(this.uiiaDataSource, epAcctInfo.getBillAdd(),
							epAcctInfo.getAcctInfo().getAccountNo(), securityObject, true);

					epAcctInfo.getBillAdd().setAddrId(dbStatus);

					/* inserting billing contact details */
					epAcctInfo.getBillInfo().setContctType(GlobalVariables.BILLADDRESSTYPE);

					dbStatus = epDao.insertContact(this.uiiaDataSource, epAcctInfo.getBillInfo(),
							epAcctInfo.getAcctInfo().getAccountNo(), securityObject, true);

					epAcctInfo.getBillInfo().setContctId(dbStatus);
				}
			}

			if (GlobalVariables.NO.equalsIgnoreCase(isSameDisputeInfo)) {
				/* if dispute address and contact details already exist */
				if (epAcctInfo.getBillAdd().getAddrId() != 0 && epAcctInfo.getBillInfo().getContctId() != 0) {
					/* updating dispute address */
					epAcctInfo.getBillAdd().setAddrType(GlobalVariables.DISPUTEADDRESSTYPE);
					dbStatus = epDao.updateAddress(this.uiiaDataSource, epAcctInfo.getBillAdd(), securityObject, true);

					/* updating dispute contact details */
					epAcctInfo.getBillInfo().setContctType(GlobalVariables.DISPUTEADDRESSTYPE);
					dbStatus = epDao.updateContact(this.uiiaDataSource, epAcctInfo.getBillInfo(), securityObject, true);
				} else {
					/* inserting dispute address */
					epAcctInfo.getBillAdd().setAddrType(GlobalVariables.DISPUTEADDRESSTYPE);

					dbStatus = epDao.insertAddress(this.uiiaDataSource, epAcctInfo.getBillAdd(),
							epAcctInfo.getAcctInfo().getAccountNo(), securityObject, true);

					epAcctInfo.getBillAdd().setAddrId(dbStatus);

					/* inserting dispute contact details */
					epAcctInfo.getBillInfo().setContctType(GlobalVariables.DISPUTEADDRESSTYPE);

					dbStatus = epDao.insertContact(this.uiiaDataSource, epAcctInfo.getBillInfo(),
							epAcctInfo.getAcctInfo().getAccountNo(), securityObject, true);

					epAcctInfo.getBillInfo().setContctId(dbStatus);
				}
			}

			epDao.updateRegDetailsEP(this.uiiaDataSource, epAcctInfo, securityObject, true);
			boolean bLoginUpdReqd = true;
			if ((!epAcctInfo.getAcctInfo().getPassword().equals("")
					|| !epAcctInfo.getAcctInfo().getOldUiiaStatus().equals(epAcctInfo.getAcctInfo().getUiiaStatus())
					|| GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctInfo().getIddStatus()))
					&& bLoginUpdReqd) {

				log.debug("IF password is changing or status is changing");
				userDao.updateLoginTbl(this.uiiaDataSource, securityObject, epAcctInfo.getAcctInfo(), true);

			}

			// TODO: Need to implement email notification feature in future - Vrajesh
//			if(StringUtils.isNotBlank(epAcctInfo.getAcctBean().getPassword()))
//			{
//				NotificationSender notifInst = new NotificationSender();
//				notifInst.notify(acctInfo,userInfo,GlobalVariables.MC_CHG_PW);
//			}

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			log.error("Exception:", e);
			transactionManager.rollback(status);
			throw e;
		}

	}

	@Override
	public SetupAddendumDetails setupCurrentAddendumDetails() {
		SetupAddendumDetails setupAddendumDetails = new SetupAddendumDetails();
		setupAddendumDetails.setYesNoOptions(restService.populateRequiredAndAllowed());
		return setupAddendumDetails;
	}

	@Override
	public EPAddendumDetForm getCurrentAddendumDetails(SecurityObject securityObject) throws Exception {
		EPAddendum epAddendum = getActiveTemplate(securityObject.getAccountNumber(), "");
		generateEPAddendumDetForm(epAddendum);
		return epAddendumDetForm;
	}

	private void generateEPAddendumDetForm(EPAddendum epAddendum) {
		List<EPInsNeeds> resultList;
		List<MultipleLimit> multiLimList;
		List<AdditionalReq> addReqList;
		List<EPInsNeeds> needsList = epAddendum.getEpNeeds();

		EPInsNeeds addAuto = new EPInsNeeds();
		EPInsNeeds addGeneral = new EPInsNeeds();
		EPInsNeeds addCargo = new EPInsNeeds();
		EPInsNeeds addContCargo = new EPInsNeeds();
		EPInsNeeds addTI = new EPInsNeeds();
		EPInsNeeds addRefTI = new EPInsNeeds();
		EPInsNeeds addWC = new EPInsNeeds();
		EPInsNeeds addEL = new EPInsNeeds();
		EPInsNeeds addEDB = new EPInsNeeds();
		// EPInsNeeds addUL = new EPInsNeeds();
		for (int i = 0; i < needsList.size(); i++) {
			EPInsNeeds insBean = needsList.get(i);
			if (insBean.getEpNeedsId() > 0) {
				log.info("YES insBean.getPolicyType():" + insBean.getPolicyType());
				insBean.setPolicyReq(GlobalVariables.YES);
			} else {
				log.info("NO insBean.getPolicyType():" + insBean.getPolicyType());
				insBean.setPolicyReq(GlobalVariables.NO);
			}
			if (insBean.getPolicyType().equals(GlobalVariables.AUTOPOLICY)) {
				addAuto = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.GENPOLICY)) {
				addGeneral = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.CARGOPOLICY)) {
				addCargo = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.CONTCARGO)) {
				addContCargo = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.TRAILERPOLICY)) {
				addTI = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.REFTRAILER)) {
				addRefTI = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.WORKCOMP)) {
				if (insBean.getPolicyReq().equals(GlobalVariables.YES)) {
					insBean.setMinLimit(GlobalVariables.STATUTORY);
					// insBean.setMaxDed(GlobalVariables.STATUTORY);
				}
				addWC = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.EMPLIABILITY)) {
				addEL = insBean;
			} else if (insBean.getPolicyType().equals(GlobalVariables.EMPDISHBOND)) {
				addEDB = insBean;
			}
		}

		log.info("AL:" + addAuto.toString());
		log.info("GL:" + addGeneral.toString());
		log.info("CR:" + addCargo.toString());
		log.info("TI:" + addTI.toString());

		log.debug("Setting policy type to be displyaed on screen :");
		addAuto.setPolicyType(GlobalVariables.AUTOPOLICY);
		addGeneral.setPolicyType(GlobalVariables.GENPOLICY);
		addCargo.setPolicyType(GlobalVariables.CARGOPOLICY);
		addTI.setPolicyType(GlobalVariables.TRAILERPOLICY);
		addContCargo.setPolicyType(GlobalVariables.CONTCARGO);
		addRefTI.setPolicyType(GlobalVariables.REFTRAILER);
		addWC.setPolicyType(GlobalVariables.WORKCOMP);
		addEL.setPolicyType(GlobalVariables.EMPLIABILITY);
		addEDB.setPolicyType(GlobalVariables.EMPDISHBOND);
		// addUL.setPolicyType(GlobalVariables.UMBRELLA);

		resultList = new ArrayList<EPInsNeeds>();

		resultList.add(addAuto);
		resultList.add(addGeneral);
		resultList.add(addCargo);
		resultList.add(addContCargo);
		resultList.add(addTI);
		resultList.add(addRefTI);
		resultList.add(addWC);
		resultList.add(addEL);
		resultList.add(addEDB);
		// resultList.add(addUL);

		log.debug("Setting epNeedsList in addendumBean :");
		epAddendum.setEpNeeds(resultList);

		multiLimList = epAddendum.getMultiLimits();
		if (multiLimList.size() == 0) {
			log.debug("Setting new MultipleLimitBean in multipleLimitList if no record exist :");
			MultipleLimit mul1 = new MultipleLimit();
			multiLimList.add(mul1);
		}
		log.debug("Setting multipleLimitList in addendumBean :");
		epAddendum.setMultiLimits(multiLimList);

		addReqList = epAddendum.getAddReq();
		if (addReqList.size() == 0) {
			log.debug("Setting new AdditionalReqBean in additionalReqList if no record exist :");
			AdditionalReq endrs1 = new AdditionalReq();
			addReqList.add(endrs1);
		}
		log.debug("Setting additionalReqList in addendumBean :");
		epAddendum.setAddReq(addReqList);
		populateFormBean(epAddendum);
	}

	@Override
	public EPAddendum getActiveTemplate(String epAcctNo, String uvalidFlg) throws Exception {

		EPTemplate epTemplate = new EPTemplate();
		EPTemplate epTemplate1 = new EPTemplate();

		List<EPTemplate> tempLst = null;

		epTemplate.setTempStatus(GlobalVariables.PRESENTTEMPLATE);
		// this is called to get the template ID of the active template
		tempLst = epAddendumDao.getTemplateList(epTemplate, epAcctNo);
		if (CommonValidations.isNotNullOrEmpty(tempLst)) {
			Iterator<EPTemplate> iter = tempLst.iterator();
			while (iter.hasNext()) {
				epTemplate1 = iter.next();
			}

		}
		// getting the EP active addendum based on template ID
		return getTemplateDetails(epTemplate1, uvalidFlg);

	}

	@Override
	public EPAddendum getTemplateDetails(EPTemplate epTemplate, String uvalidFlg) throws Exception {

		EPAddendum templateDetails = new EPAddendum();

		List<EPInsNeeds> insLst = epInsuranceDao.getEPInsuranceDetails(epTemplate);
		List<MultipleLimit> multLimLst = epInsuranceDao.getEPMultipleLim(epTemplate);
		templateDetails.setEpNeeds(insLst);
		templateDetails.setMultiLimits(multLimLst);
		templateDetails.setEpSwitches(epSwitchesDao.getEPSwitches(epTemplate));
		templateDetails.setAddReq(epAdditonalReqDao.getEPAddlReq(epTemplate, uvalidFlg));
		templateDetails.setEffDate(epTemplate.getEffDate());
		templateDetails.setTemplateID(epTemplate.getTemplateID());
		templateDetails.setTemplateStatus(epTemplate.getDbTemplateStatus());

		return templateDetails;

	}

	private void populateFormBean(EPAddendum addendumBean) {
		epAddendumDetForm.setAddendumEffDate(addendumBean.getEffDate());
		epAddendumDetForm.setTemplateId(String.valueOf(addendumBean.getTemplateID()));
		epAddendumDetForm.setAddendumId(String.valueOf(addendumBean.getEpSwitches().getAddendumId()));
		epAddendumDetForm.setMemberSpecific(addendumBean.getEpSwitches().getMemberSpecific());
		epAddendumDetForm.setKnownAs(addendumBean.getEpSwitches().getKnownAs());
		epAddendumDetForm.setRampDetReq(addendumBean.getEpSwitches().getRampDetReq());
		epAddendumDetForm.setBlanketAllwd(addendumBean.getEpSwitches().getBlanketAllwd());
		epAddendumDetForm.setEpNeeds(addendumBean.getEpNeeds());
		epAddendumDetForm.setMultiLimits(addendumBean.getMultiLimits());
		epAddendumDetForm.setAddReq(addendumBean.getAddReq());

	}

	@Override
	public List<MCCancel> getDeletedMC(String cancRefStartDate, String cancRefEndDate, int pageIndex, int pageSize)
			throws Exception {
		return epDao.getDeletedMC(cancRefStartDate, cancRefEndDate, pageIndex, pageSize, "");
	}

	@Override
	public List<EPTerminalFeed> getTerminalFeedLocations(String accountNumber) throws Exception {
		return epDao.getTerminalFeedLocations(accountNumber);
	}

	@Override
	public List<EPTemplate> searchEPTemplate(String searchTmplt, int pageIndex, String accountNumber) throws Exception {
		EPTemplate epTemplate = new EPTemplate();
		epTemplate.setTempStatus(searchTmplt);
		epTemplate.setPageNumber(pageIndex);
		epTemplate.setLimit(GlobalVariables.LIMIT);
		return epDao.getTemplateList(epTemplate, accountNumber);
	}

	@Override
	public EPAddendumDetForm getEPTemplateDetails(SecurityObject securityObject, String templateId, String dbStatus,
			String effDate) throws Exception {

		EPTemplate epTemplate = new EPTemplate();
		epTemplate.setTemplateID(Integer.parseInt(templateId));
		epTemplate.setDbTemplateStatus(dbStatus);
		epTemplate.setEffDate(effDate);

		EPAddendum epAddendum = getTemplateDetails(epTemplate, "");
		generateEPAddendumDetForm(epAddendum);
		if (!dbStatus.equals(GlobalVariables.PENDINGTEMPLATES)) {
			epAddendumDetForm.setCopyTmplt("true");
		}
		return epAddendumDetForm;
	}

}
