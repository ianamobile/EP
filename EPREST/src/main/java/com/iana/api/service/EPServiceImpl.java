package com.iana.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.dao.EPDao;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.LabelValueForm;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupEpTemplates;
import com.iana.api.domain.SetupMCDataJsonDTO;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;


@Service
public class EPServiceImpl extends CommonUtils implements EPService {
	
	Logger log = LogManager.getLogger(this.getClass().getName());
	
	@Autowired
	private EPDao epDao;
	
	@Override
	public void validateEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount,	List<String> errorList) throws Exception {
	
		if(!GlobalVariables.ROLE_EP.equalsIgnoreCase(securityObject.getRoleName())) {
			errorList.add(env.getProperty("msg_error_unauthorized_access"));
			return;
		}
		
		//check accountNumber start with EP
		if(StringUtils.isNotBlank(securityObject.getAccountNumber()) && securityObject.getAccountNumber().length() > 2) {
			
			searchAccount.setUserType(securityObject.getAccountNumber().substring(0,2));
		
			if(!(GlobalVariables.EQUIPMENTPROVIDER.equalsIgnoreCase(searchAccount.getUserType()))) {
				errorList.add(env.getProperty("msg_error_invalid_account_number"));
				return;
			}
		}
	}

	@Override
	public void epMotorCarriersBusinessValidation(SecurityObject securityObject, SearchAccount searchAccount, Pagination pagination, List<String> errorList) throws Exception {
		
		Long recordCount = epDao.countEPMotorCarriers(securityObject, searchAccount);
		restService.pageSetup(searchAccount, errorList, pagination, recordCount);
		
	}

	@Override
	public List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount)throws Exception {
	
		return epDao.getEPMotorCarriers(securityObject, searchAccount);
	}

	@Override
	public void validateMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount, List<String> errorList) throws Exception {
		if(!GlobalVariables.ROLE_EP.equalsIgnoreCase(securityObject.getRoleName())) {
			errorList.add(env.getProperty("msg_error_unauthorized_access"));
			return;
		}
	}

	@Override
	public SetupMCDataJsonDTO getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount) throws Exception {
		
		List<MCDataJsonDTO> mcData = epDao.getMCLookUpForEP(securityObject, searchAccount);
		List<LabelValueForm> result = new ArrayList<>();
		for(MCDataJsonDTO mcDataJsonDTO : mcData ){
			result.add(new LabelValueForm((mcDataJsonDTO.getCompanyName()+":"+mcDataJsonDTO.getMcScac()+":"+mcDataJsonDTO.getMcEPStatus()+":"+mcDataJsonDTO.getEpMemberFlag()), 
					(mcDataJsonDTO.getCompanyName()+":"+mcDataJsonDTO.getMcScac()+":"+mcDataJsonDTO.getAccountNumber())));
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
}
