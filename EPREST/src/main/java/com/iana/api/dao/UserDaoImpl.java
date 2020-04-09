package com.iana.api.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.ContactDetail;
import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.LoginHistory;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.User;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.EncryptionUtils;
import com.iana.api.utils.GlobalVariables;

@Repository
public class UserDaoImpl extends GenericDAO implements UserDao {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Autowired
	public Environment env;

	
	@Override
	public User validate(Login login) throws Exception {
		List<Object> params = new ArrayList<>();
		
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append(" SELECT u.user_name AS username, u.account_number, r.role_name ");
		sbQuery.append(" FROM user_login u, role_master r ");
		sbQuery.append(" WHERE u.role_id = r.role_id AND r.role_name = ? AND (u.account_number = ? OR u.scac_code = ?) ");
		sbQuery.append(" AND u.password = ? AND u.status = ? ");
	
		params.add(login.getRoleName());
		params.add(login.getUsername());
		params.add(login.getUsername());
		params.add(EncryptionUtils.encrypt(login.getPassword().toUpperCase()));
		params.add(GlobalVariables.Y);
		
		return findBean(this.uiiaDataSource, sbQuery.toString(), params.toArray(), User.class);
	}

	@Override
	public List<AccountInfo> getAccountInfos(String accountNumber) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append(" SELECT a.account_no AS accountNumber, a.company_name companyName, a.scac_code AS scac, a.iana_mem, a.non_uiia_ep AS nonUIIAEpFlag, ");
		sbQuery.append(" a.uiia_status, a.uiia_status_cd AS uiiaStatusCode, IFNULL(DATE_FORMAT(a.mem_eff_dt,'%m/%d/%Y'), '') AS memEffDate, ");
		sbQuery.append(" IFNULL(DATE_FORMAT(a.cancelled_dt,'%m/%d/%Y'), '') AS cancelledDate, IFNULL(DATE_FORMAT(a.deleted_date,'%m/%d/%Y'), '') AS deletedDate, a.attr1, a.attr2, a.attr3, ");
		sbQuery.append(" IFNULL(DATE_FORMAT(a.re_instated_dt,'%m/%d/%Y'), '') AS reInstatedDate, a.mem_type AS memberType, a.comp_url, ");
		sbQuery.append(" IFNULL(DATE_FORMAT(a.modified_date,'%m/%d/%Y'), '') AS lastUpdatedDate, a.uiia_member, ");
		sbQuery.append(" a.idd_member, IF(a.idd_member = null, '', IF(a.idd_member = 'Y', 'ACTIVE', IF(a.idd_member = 'N', 'DELETED', ''))) AS iddStatus, ");
		sbQuery.append(" IF(e.ep_entities != '', '['+e.ep_entities+']', '') AS entitiesName ");
		sbQuery.append(" FROM account_info a ");
		sbQuery.append(" LEFT JOIN ep_basic_details e ON (e.ep_acct_no = a.account_no) ");
		sbQuery.append(" WHERE a.account_no = ? ");
		
		return findAll(this.uiiaDataSource, sbQuery.toString(), AccountInfo.class, accountNumber);
	}
	
	@Override
	public LoginHistory getLastLogin(String userName, String accountNumber) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append(" SELECT DATE_FORMAT(lh.LOG_DATETIME,'%m/%d/%Y %H:%i:%S') loginTime, lh.IP_ADDRESS "); 
		sbQuery.append(" FROM login_history lh WHERE LOGINHIST_ID = ");
		sbQuery.append(" (SELECT MAX(LOGINHIST_ID) FROM login_history lh WHERE ACCOUNT_NO = ? AND USER_NAME = ?)");
		
		return findBean(this.uiiaDataSource, sbQuery.toString(), LoginHistory.class, accountNumber, userName);

	}

	@Override
	public Long createLoginHistory(SecurityObject securityObject) throws Exception {
		if(StringUtils.isBlank(securityObject.getIpAddress())) {
			securityObject.setIpAddress(StringUtils.EMPTY);
		}
		
		Map<String,Object> paramMap = new HashMap<>();
		
		paramMap.put("USER_NAME", securityObject.getUsername());
		paramMap.put("SCAC_CODE", securityObject.getScac());
		paramMap.put("ACCOUNT_NO", securityObject.getAccountNumber());
		paramMap.put("ROLE", securityObject.getRoleName());
		paramMap.put("IP_ADDRESS", CommonUtils.validateObject(securityObject.getIpAddress()));
		paramMap.put("LOG_DATETIME", DateTimeFormater.getSqlSysTimestamp());
		
		return insertAndReturnGeneratedKey(this.uiiaDataSource, "login_history", paramMap, "LOGINHIST_ID").longValue();
	}
	
	@Override
	public ContactDetail getContact(DataSource transUIIADataSource, String accountNumber, String contactAddType) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT contct_id AS contactId, contct_fname AS firstName, contct_mname AS middleName, contct_lname AS lastName, ");
		sbQuery.append(" contct_title AS title, contct_salutation AS salutation, contct_mr_ms AS mrms, contct_suffix AS suffix, contct_prm_phone AS priPhone");
		sbQuery.append(" ,contct_sec_phone AS secPhone, contct_prm_fax AS priFax, contct_sec_fax AS secFax,");
		sbQuery.append(" contct_prm_email AS priEmail, contct_sec_email AS secEmail, SAME_BILL_CONTCT AS sameBillContact, ATTR1 ");
		sbQuery.append(" FROM contacts_master ");
		sbQuery.append(" WHERE account_no = ? ");
		sbQuery.append(" AND contct_type = ? ");
		
		return findBean((null != transUIIADataSource) ? transUIIADataSource : this.uiiaDataSource, sbQuery.toString(), ContactDetail.class, accountNumber, contactAddType);
	}

	@Override
	public FpToken getForgotPasswordTokenInfoByScac(FpToken fpToken) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append(" SELECT ul.LOGIN_ID AS id, CONTCT_FNAME AS firstName, CONTCT_LNAME AS lastName, ");
		sbQuery.append(" CONTCT_PRM_EMAIL AS email "); 
		sbQuery.append(" FROM user_login ul, contacts_master cm");
		sbQuery.append(" WHERE ul.ACCOUNT_NUMBER = cm.ACCOUNT_NO ");
		sbQuery.append(" AND ul.SCAC_CODE = ? AND CONTCT_TYPE = ? AND ul.ROLE_ID = ? "); 
			
		return findBean(this.uiiaDataSource, sbQuery.toString(), FpToken.class, fpToken.getScac(), GlobalVariables.CONTACTADDTYPE, GlobalVariables.ROLE_EP_ID);
	
	}

	@Override
	public int updatePassword(ResetPassword resetPassword, FpToken fpToken) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		
		sbQuery.append(" UPDATE user_login ");
		sbQuery.append(" SET PASSWORD = ?, MODIFIED_BY = ?, MODIFIED_DATE = ? ");
		sbQuery.append(" WHERE LOGIN_ID = ? ");
		
		return saveOrUpdate(this.uiiaDataSource, sbQuery.toString(), false, EncryptionUtils.encrypt(resetPassword.getNewPassword().toUpperCase()), 
							fpToken.getUserName(), DateTimeFormater.getSqlSysTimestamp(), fpToken.getId());
	}

}
