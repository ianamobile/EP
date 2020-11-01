package com.iana.api.service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.iana.api.dao.EPDao;
import com.iana.api.dao.UserDao;
import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.AddressDet;
import com.iana.api.domain.ContactDet;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.LabelValueForm;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupEpTemplates;
import com.iana.api.domain.SetupMCDataJsonDTO;
import com.iana.api.domain.SetupManageAccountInfo;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Service
public class EPServiceImpl extends CommonUtils implements EPService {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private EPDao epDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private DataSource uiiaDataSource;

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
		System.out.println("---cntctAdd=" + cntctAdd);
		ContactDet cntctInfo = epDao.getContact(acctNo, GlobalVariables.CONTACTADDTYPE);
		epAcctInfo.setCntctInfo(cntctInfo);
		System.out.println("---cntctInfo=" + cntctInfo);
		AddressDet billAdd = epDao.getAddress(acctNo, GlobalVariables.BILLADDRESSTYPE);
		epAcctInfo.setBillAdd(billAdd);
		System.out.println("---billAdd=" + billAdd);
		ContactDet billInfo = epDao.getContact(acctNo, GlobalVariables.BILLADDRESSTYPE);
		epAcctInfo.setBillInfo(billInfo);
		System.out.println("---cntBill=" + billInfo);
		AddressDet disputeAdd = epDao.getAddress(acctNo, GlobalVariables.DISPUTEADDRESSTYPE);
		epAcctInfo.setDisputeAdd(disputeAdd);
		System.out.println("---disputeAdd=" + disputeAdd);
		ContactDet disputeInfo = epDao.getContact(acctNo, GlobalVariables.DISPUTEADDRESSTYPE);
		epAcctInfo.setDisputeInfo(disputeInfo);
		System.out.println("---disputeInfo=" + disputeInfo);
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
			// TODO: need to discuss with Vipul
			// swati-----------14/9------login permissions related changes
//			if(GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctBean().getUiiaStatus()) && (GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctBean().getIddStatus()) || epAcctInfo.getAcctBean().getIddStatus().equals("")))
//			{
//				log.debug("IF uiia status is deleted and IDD status is deleted or empty string");
//				epAcctInfo.getAcctBean().setLoginAllwd(GlobalVariables.NO);
//			}
//			else if(GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctBean().getIddStatus()) && (GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctBean().getUiiaStatus()) || epAcctInfo.getAcctBean().getUiiaStatus().equals("") ))
//			{
//				log.debug("IF IDD status is deleted and UIIA status is deleted or empty string");
//				epAcctInfo.getAcctBean().setLoginAllwd(GlobalVariables.NO);
//			}
//			else
//			{
//				epAcctInfo.getAcctBean().setLoginAllwd(GlobalVariables.YES);
//			}
//			
//			
//			//added by Swati----11/09---to avoid updating login table if IDD member turned UIIA member is deleted from UIIA
//			//Since the role related changes are done by trigger updating login table in this case will cause role related conflicts
//			boolean bLoginUpdReqd = true;
//			if(GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctBean().getUiiaStatus()) && GlobalVariables.ACTIVEMEMBER.equals(epAcctInfo.getAcctBean().getIddStatus()))
//			{
//				log.debug("If member has been deleted from UIIA but is still an active IDD member");
//				bLoginUpdReqd = false;
//			}
//			
//			if((StringUtils.isNotBlank(epAcctInfo.getAcctBean().getPassword()) || !epAcctInfo.getAcctBean().getOldScac().equals(epAcctInfo.getAcctBean().getScac()) ||!epAcctInfo.getAcctBean().getOldUiiaStatus().equals(epAcctInfo.getAcctBean().getUiiaStatus()) || GlobalVariables.DELETEDMEMBER.equals(epAcctInfo.getAcctBean().getIddStatus())) && bLoginUpdReqd)
//			{
//				log.debug("IF password is changing or status is changing");
//				userDao.updateLoginTbl(securityObject, epAcctInfo.getAcctBean(), true);
//			}
//
//			
//			//start----swati-----6/9
//			if(StringUtils.isNotBlank(epAcctInfo.getAcctBean().getPassword()))
//			{
//				// TODO
////				NotificationSender notifInst = new NotificationSender();
////				notifInst.notify(acctInfo,userInfo,GlobalVariables.MC_CHG_PW);
//			}

			// if all statement execute successfully then commit the transactions.
			transactionManager.commit(status);

		} catch (Exception e) {
			log.error("Exception:", e);
			transactionManager.rollback(status);
			throw e;
		}

	}

}
