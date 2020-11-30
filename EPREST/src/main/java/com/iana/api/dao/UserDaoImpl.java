package com.iana.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.ContactDetail;
import com.iana.api.domain.FpToken;
import com.iana.api.domain.Login;
import com.iana.api.domain.LoginHistory;
import com.iana.api.domain.ResetPassword;
import com.iana.api.domain.Role;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.User;
import com.iana.api.domain.billing.payment.BillingUser;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.EncryptionUtils;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

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
		sbQuery.append(
				" WHERE u.role_id = r.role_id AND r.role_name = ? AND (u.account_number = ? OR u.scac_code = ?) ");
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

		sbQuery.append(
				" SELECT a.account_no, a.company_name companyName, a.scac_code AS scac, a.iana_mem, a.non_uiia_ep AS nonUIIAEpFlag, ");
		sbQuery.append(
				" a.uiia_status, a.uiia_status_cd AS uiiaStatusCode, IFNULL(DATE_FORMAT(a.mem_eff_dt,'%m/%d/%Y'), '') AS memEffDate, ");
		sbQuery.append(
				" IFNULL(DATE_FORMAT(a.cancelled_dt,'%m/%d/%Y'), '') AS cancelledDate, IFNULL(DATE_FORMAT(a.deleted_date,'%m/%d/%Y'), '') AS deletedDate, a.attr1, a.attr2, a.attr3, ");
		sbQuery.append(
				" IFNULL(DATE_FORMAT(a.re_instated_dt,'%m/%d/%Y'), '') AS reInstatedDate, a.mem_type AS memberType, a.comp_url, ");
		sbQuery.append(" IFNULL(DATE_FORMAT(a.modified_date,'%m/%d/%Y'), '') AS lastUpdatedDate, a.uiia_member, ");
		sbQuery.append(
				" a.idd_member, IF(a.idd_member = null, '', IF(a.idd_member = 'Y', 'ACTIVE', IF(a.idd_member = 'N', 'DELETED', ''))) AS iddStatus, ");
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
		if (StringUtils.isBlank(securityObject.getIpAddress())) {
			securityObject.setIpAddress(StringUtils.EMPTY);
		}

		Map<String, Object> paramMap = new HashMap<>();

		paramMap.put("USER_NAME", securityObject.getUsername());
		paramMap.put("SCAC_CODE", securityObject.getScac());
		paramMap.put("ACCOUNT_NO", securityObject.getAccountNumber());
		paramMap.put("ROLE", securityObject.getRoleName());
		paramMap.put("IP_ADDRESS", CommonUtils.validateObject(securityObject.getIpAddress()));
		paramMap.put("LOG_DATETIME", DateTimeFormater.getSqlSysTimestamp());

		return insertAndReturnGeneratedKey(this.uiiaDataSource, "login_history", paramMap, "LOGINHIST_ID").longValue();
	}

	@Override
	public ContactDetail getContact(DataSource transUIIADataSource, String accountNumber, String contactAddType)
			throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(
				" SELECT contct_id AS contactId, contct_fname AS firstName, contct_mname AS middleName, contct_lname AS lastName, ");
		sbQuery.append(
				" contct_title AS title, contct_salutation AS salutation, contct_mr_ms AS mrms, contct_suffix AS suffix, contct_prm_phone AS priPhone");
		sbQuery.append(" ,contct_sec_phone AS secPhone, contct_prm_fax AS priFax, contct_sec_fax AS secFax,");
		sbQuery.append(
				" contct_prm_email AS priEmail, contct_sec_email AS secEmail, SAME_BILL_CONTCT AS sameBillContact, ATTR1 ");
		sbQuery.append(" FROM contacts_master ");
		sbQuery.append(" WHERE account_no = ? ");
		sbQuery.append(" AND contct_type = ? ");

		return findBean((null != transUIIADataSource) ? transUIIADataSource : this.uiiaDataSource, sbQuery.toString(),
				ContactDetail.class, accountNumber, contactAddType);
	}

	@Override
	public FpToken getForgotPasswordTokenInfoByScac(FpToken fpToken) throws Exception {
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" SELECT ul.LOGIN_ID AS id, CONTCT_FNAME AS firstName, CONTCT_LNAME AS lastName, ");
		sbQuery.append(" CONTCT_PRM_EMAIL AS email ");
		sbQuery.append(" FROM user_login ul, contacts_master cm");
		sbQuery.append(" WHERE ul.ACCOUNT_NUMBER = cm.ACCOUNT_NO ");
		sbQuery.append(" AND ul.SCAC_CODE = ? AND CONTCT_TYPE = ? AND ul.ROLE_ID = ? ");

		return findBean(this.uiiaDataSource, sbQuery.toString(), FpToken.class, fpToken.getScac(),
				GlobalVariables.CONTACTADDTYPE, GlobalVariables.ROLE_EP_ID);

	}

	@Override
	public int updatePassword(ResetPassword resetPassword, FpToken fpToken) throws Exception {
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" UPDATE user_login ");
		sbQuery.append(" SET PASSWORD = ?, MODIFIED_BY = ?, MODIFIED_DATE = ? ");
		sbQuery.append(" WHERE LOGIN_ID = ? ");

		return saveOrUpdate(this.uiiaDataSource, sbQuery.toString(), false,
				EncryptionUtils.encrypt(resetPassword.getNewPassword().toUpperCase()), fpToken.getUserName(),
				DateTimeFormater.getSqlSysTimestamp(), fpToken.getId());
	}

	@Override
	public int changePassword(ResetPassword resetPassword, SecurityObject securityObject) throws Exception {
		StringBuffer sbQuery = new StringBuffer();

		sbQuery.append(" UPDATE user_login ");
		sbQuery.append(" SET PASSWORD = ?, MODIFIED_BY = ?, MODIFIED_DATE = ? ");
		sbQuery.append(" WHERE ACCOUNT_NUMBER = ? AND SCAC_CODE = ?");

		return saveOrUpdate(this.uiiaDataSource, sbQuery.toString(), false,
				EncryptionUtils.encrypt(resetPassword.getNewPassword().toUpperCase()), securityObject.getUsername(),
				DateTimeFormater.getSqlSysTimestamp(), securityObject.getAccountNumber(), securityObject.getScac());
	}

	@Override
	public int updateLoginTbl(DataSource lUIIADataSource, SecurityObject securityObject, AccountInfo accountInfo,
			boolean enableTransMgmt) throws Exception {

		List<Object> params = new ArrayList<>();

		StringBuilder sbQuery = new StringBuilder("UPDATE user_login SET ");

		if (StringUtils.isNotBlank(accountInfo.getPassword())) {
			sbQuery.append("PASSWORD = ?,");
		}

		sbQuery.append(" STATUS = ?, AUDIT_TRAIL_EXTRA = ?, MODIFIED_BY = ?, MODIFIED_DATE = ?, scac_code = ? ");

		if (GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())
				|| GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())) {
			log.debug("If IDD secondary user ");
			sbQuery.append(" ,user_name = ? ");
		}

		if (GlobalVariables.Y.equals(accountInfo.getApplyUiiaMem())) {
			log.debug("If IDD user has applied for UIIA membership");
			sbQuery.append(" ,role_id = (SELECT ROLE_ID FROM role_master WHERE ROLE_NAME = ?) ");
		}

		if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
				|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
						&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
								|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
			log.debug("If IDD secondary user is deleted");
			sbQuery.append(" WHERE ACCOUNT_NUMBER = ?");

		} else {
			sbQuery.append(
					" WHERE ACCOUNT_NUMBER = ? AND ROLE_ID = (SELECT ROLE_ID FROM role_master WHERE ROLE_NAME = ?) ");
		}

		if (GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())
				|| GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())) {
			log.debug("If IDD secodary user");
			sbQuery.append(" AND user_name = ?");
		}

		// code as per old UIIA starts
		if (StringUtils.isNotBlank(accountInfo.getPassword())) {
			params.add(EncryptionUtils.encrypt(accountInfo.getPassword().toUpperCase()));
			params.add(CommonUtils.validateObject(accountInfo.getLoginAllwd())); // login allowed
			params.add(securityObject.getIpAddress());
			params.add(securityObject.getUsername());
			params.add(DateTimeFormater.getSqlSysTimestamp());
			params.add(CommonUtils.validateObject(accountInfo.getScacCode()));

			if (GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())
					|| GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())) {
				params.add(CommonUtils.validateObject(accountInfo.getSecUserName()));
				if (GlobalVariables.Y.equals(accountInfo.getApplyUiiaMem())) {
					params.add(accountInfo.getMemType());
					params.add(accountInfo.getAccountNo());

					if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
							|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
									&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
											|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
						params.add(accountInfo.getOldSecUserName());
					} else {
						params.add(GlobalVariables.IDDUSER + "_" + accountInfo.getMemType());
						params.add(accountInfo.getOldSecUserName());
					}
				} else {
					if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
							|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
									&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
											|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
						log.debug("If IDD secondary user has been deleted");

						params.add(accountInfo.getAccountNo());
						params.add(accountInfo.getOldSecUserName());
					} else {
						params.add(accountInfo.getAccountNo());
						params.add(accountInfo.getMemType());
						params.add(accountInfo.getOldSecUserName());
					}
				}
			} else {
				if (GlobalVariables.Y.equals(accountInfo.getApplyUiiaMem())
						|| (GlobalVariables.ROLE_IDD_MC.equalsIgnoreCase(securityObject.getRoleName())
								&& GlobalVariables.ROLE_NON_UIIA_MC.equals(accountInfo.getMemType()))) {
					params.add(accountInfo.getMemType());
					params.add(accountInfo.getAccountNo());
					params.add(GlobalVariables.IDDUSER + "_" + accountInfo.getMemType());
				} else if (accountInfo.getIddStatus().equalsIgnoreCase(GlobalVariables.DELETEDMEMBER)) {
					params.add(accountInfo.getAccountNo());
				} else {
					params.add(accountInfo.getAccountNo());
					params.add(accountInfo.getMemType());
				}
			}
		} // if password has been provided
		else {
			params.add(CommonUtils.validateObject(accountInfo.getLoginAllwd())); // login allowed
			params.add(securityObject.getIpAddress()); // getAuditTrail
			params.add(securityObject.getUsername());
			params.add(DateTimeFormater.getSqlSysTimestamp());
			params.add(CommonUtils.validateObject(accountInfo.getScacCode()));

			// modified----swati----14/9---to avoid user_name being updated in case of
			// non-IDD users
			if (GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())
					|| GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())) {
				log.debug("If IDD secondary user");
				params.add(CommonUtils.validateObject(accountInfo.getSecUserName()));

				// Swati----11/03---Apply for UIIA membership
				if (GlobalVariables.Y.equals(accountInfo.getApplyUiiaMem())) {
					log.debug("If IDD user has applied for UIIA membership");
					params.add(accountInfo.getMemType());

					if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
							|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
									&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
											|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
						log.debug("If IDD secondary user has been deleted");

						params.add(accountInfo.getAccountNo());
						params.add(accountInfo.getOldSecUserName());
					} else {
						params.add(accountInfo.getAccountNo());
						params.add(GlobalVariables.IDDUSER + "_" + accountInfo.getMemType());
						params.add(accountInfo.getOldSecUserName());
					}
				} else {
					log.debug("If not applied for UIIA membership ");
					if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
							|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
									&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
											|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
						log.debug("If IDD secondary user has been deleted");

						params.add(accountInfo.getAccountNo());
						params.add(accountInfo.getOldSecUserName());
					} else {
						params.add(accountInfo.getAccountNo());
						params.add(accountInfo.getMemType());
						params.add(accountInfo.getOldSecUserName());
					}
				}
			} // if IDD member
			else {
//				Swati----11/03---Apply for UIIA membership
				if (GlobalVariables.Y.equals(accountInfo.getApplyUiiaMem())) {
					log.debug("Applied for UIIA membership");
					params.add(accountInfo.getMemType());

					if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
							|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
									&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
											|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
						log.debug("IDD secondary user delete");
						params.add(accountInfo.getAccountNo());
					} else {
						params.add(accountInfo.getAccountNo());
						params.add(GlobalVariables.IDDUSER + "_" + accountInfo.getMemType());
					}
				} else {
					if (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getIddStatus())
							|| (GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(accountInfo.getUiiaStatus())
									&& (GlobalVariables.ROLE_SUB_SEC.equals(accountInfo.getMemType())
											|| GlobalVariables.ROLE_IDD_SEC.equals(accountInfo.getMemType())))) {
						log.debug("IDD secondary user delete");
						params.add(accountInfo.getAccountNo());
					} else {
						params.add(accountInfo.getAccountNo());
						params.add(accountInfo.getMemType());
					}
				}
			}
		}
		// code as per old UIIA end

		int updatedCnt = saveOrUpdate(enableTransMgmt ? lUIIADataSource : uiiaDataSource, sbQuery.toString(),
				params.toArray(), enableTransMgmt);
		log.info("user_login: updatedCnt:" + updatedCnt);
		return updatedCnt;

	}

	@Override
	public User user(String accountNumber, String userName) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT LOGIN_ID, STATUS, PASSWORD, USER_NAME ");
		sb.append(" FROM user_login ");
		sb.append(" WHERE ACCOUNT_NUMBER = ? AND USER_NAME = ? AND STATUS = ? ");

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sb.toString(),
				new Object[] { accountNumber, userName, GlobalVariables.YES }, new ResultSetExtractor<User>() {
					@Override
					public User extractData(ResultSet rs) throws SQLException, DataAccessException {
						User user = new User();
						while (rs.next()) {
							user.setLoginId(rs.getLong("LOGIN_ID"));
							user.setStatus(rs.getString("STATUS"));
							user.setUsername(rs.getString("USER_NAME"));
							try {
								user.setPassword(EncryptionUtils.decrypt(rs.getString("PASSWORD")));
							} catch (Exception e) {
								log.error("user(): extractData:" + e);
							}
						}
						return user;
					}
				});
	}

	@Override
	public void updateUserForDeleteBillingUser(DataSource lUIIADataSource, SecurityObject securityObject,
			BillingUser billingUser) throws Exception {
		String query = "UPDATE user_login SET STATUS = 'N', MODIFIED_BY = ?, MODIFIED_DATE = ? WHERE ACCOUNT_NUMBER = ? AND USER_NAME = ? ";
		saveOrUpdate(lUIIADataSource, query, true, securityObject.getUsername(), DateTimeFormater.getSqlSysTimestamp(),
				securityObject.getAccountNumber(), billingUser.getUserName());
	}

	@Override
	public Role getRole(String roleName) throws Exception {
		String query = " SELECT ROLE_ID, ROLE_NAME FROM role_master WHERE ROLE_NAME = ? ";
		return findBean(this.uiiaDataSource, query, Role.class, roleName);
	}

	@Override
	public int insertUserForCreateBillingUser(DataSource lUIIADataSource, SecurityObject securityObject,
			BillingUser billingUser, long roleId) throws Exception {
		StringBuilder query = new StringBuilder(" INSERT INTO user_login ");
		query.append(
				" (USER_NAME, SCAC_CODE, PASSWORD, ACCOUNT_NUMBER, ROLE_ID, STATUS, AUDIT_TRAIL_EXTRA, CREATED_BY, CREATED_DATE) ");
		query.append(" VALUES (?,?,?,?,?,?,?,?,?) ");

		Object[] params = new Object[] { billingUser.getUserName(), securityObject.getScac(),
				EncryptionUtils.encrypt(billingUser.getPassword().toUpperCase()), securityObject.getAccountNumber(),
				roleId, GlobalVariables.YES, securityObject.getIpAddress(), securityObject.getUsername(),
				Utility.getSqlSysTimestamp(), };

		return saveOrUpdate(lUIIADataSource, query.toString(), true, params);
	}

	@Override
	public int updateUserForCreateBillingUser(DataSource lUIIADataSource, SecurityObject securityObject,
			BillingUser billingUser) throws Exception {

		String query = "UPDATE user_login SET PASSWORD = ?, MODIFIED_BY =?, MODIFIED_DATE = ? WHERE ACCOUNT_NUMBER =? AND USER_NAME = ?";

		Object[] params = new Object[] { EncryptionUtils.encrypt(billingUser.getPassword()),
				securityObject.getUsername(), Utility.getSqlSysTimestamp(), securityObject.getAccountNumber(),
				billingUser.getUserName() };

		return saveOrUpdate(lUIIADataSource, query.toString(), true, params);

	}

	@Override
	public void insertPassword(DataSource lUIIADataSource, SecurityObject securityObject, AccountInfo acctInfo,
			Role role, boolean enableTransMgmt) throws Exception {

		// USER_NAME,SCAC_CODE,PASSWORD,ACCOUNT_NUMBER,
		// ROLE_ID,STATUS,AUDIT_TRAIL_EXTRA,CREATED_BY,CREATED_DATE
		Map<String, Object> paramMap = new HashMap<String, Object>();

		if (acctInfo.getMemType().equals(GlobalVariables.ROLE_IDD_SEC)
				|| acctInfo.getMemType().equals(GlobalVariables.ROLE_SUB_SEC)) {
			paramMap.put("USER_NAME", acctInfo.getSecUserName());
		} else {
			paramMap.put("USER_NAME", securityObject.getAccountNumber());
		}

		paramMap.put("SCAC_CODE", securityObject.getScac());
		paramMap.put("PASSWORD",
				StringUtils.isNotBlank(acctInfo.getPassword())
						? EncryptionUtils.encrypt(acctInfo.getPassword().toUpperCase())
						: acctInfo.getPassword());
		paramMap.put("ACCOUNT_NUMBER", securityObject.getAccountNumber());
		paramMap.put("ROLE_ID", role.getRoleId());

		// For Set Status
		if (!GlobalVariables.DELETEDMEMBER.equalsIgnoreCase(securityObject.getStatus())) {
			paramMap.put("STATUS", GlobalVariables.Y);
		} else {
			paramMap.put("STATUS", GlobalVariables.N);
		}

		// Close For Set Status

		paramMap.put("AUDIT_TRAIL_EXTRA", securityObject.getIpAddress());
		paramMap.put("CREATED_BY", CommonUtils.validateObject(securityObject.getUsername()));
		paramMap.put("CREATED_DATE", DateTimeFormater.getSqlSysTimestamp());

		Long userLoginId = insertAndReturnGeneratedKey(enableTransMgmt ? lUIIADataSource : uiiaDataSource, "user_login",
				paramMap, "LOGIN_ID").longValue();
		log.info("insertPassword: userLoginId:" + userLoginId);
	}

}
