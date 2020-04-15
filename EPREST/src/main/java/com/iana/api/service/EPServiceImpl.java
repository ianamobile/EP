package com.iana.api.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.dao.EPDao;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
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


}
