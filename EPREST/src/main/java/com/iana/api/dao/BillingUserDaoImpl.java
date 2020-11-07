package com.iana.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.billing.payment.BillingUser;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Repository
public class BillingUserDaoImpl extends GenericDAO implements BillingUserDao {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Override
	public List<BillingUser> billingUsers(SecurityObject securityObject) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(
				" SELECT BU_ID, ACCOUNT_NO, USER_NAME, PASSWORD, CONTACT_SUFFIX, FIRST_NAME, LAST_NAME, TITLE, PHONE, FAX, EMAIL ");
		sb.append(" FROM billing_user ");
		sb.append(" WHERE ACCOUNT_NO = ? AND DELETED != 'Y' ");

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sb.toString(),
				new Object[] { securityObject.getAccountNumber() }, new ResultSetExtractor<List<BillingUser>>() {

					@Override
					public List<BillingUser> extractData(ResultSet rs) throws SQLException, DataAccessException {
						List<BillingUser> results = new ArrayList<>();
						while (rs.next()) {
							BillingUser billingUser = new BillingUser();
							billingUser.setBuId(rs.getLong("BU_ID"));
							billingUser.setAccountNumber(rs.getString("ACCOUNT_NO"));
							billingUser.setUserName(rs.getString("USER_NAME"));
							billingUser.setPassword(rs.getString("PASSWORD"));
							billingUser.setContactSuffix(CommonUtils.validateObject(rs.getString("CONTACT_SUFFIX")));
							billingUser.setFirstName(rs.getString("FIRST_NAME"));
							billingUser.setLastName(rs.getString("LAST_NAME"));
							billingUser.setTitle(rs.getString("TITLE"));
							billingUser.setPhone(rs.getString("PHONE"));
							billingUser.setFax(CommonUtils.validateObject(rs.getString("FAX")));
							billingUser.setEmail(rs.getString("EMAIL"));

							results.add(billingUser);
						}

						return results;
					}
				});
	}

	@Override
	public BillingUser billingUser(Long buId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(
				" SELECT BU_ID, ACCOUNT_NO, USER_NAME, PASSWORD, CONTACT_SUFFIX, FIRST_NAME, LAST_NAME, TITLE, PHONE, FAX, EMAIL ");
		sb.append(" FROM billing_user ");
		sb.append(" WHERE BU_ID = ? AND DELETED != 'Y' ");

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sb.toString(), new Object[] { buId },
				new ResultSetExtractor<BillingUser>() {
					@Override
					public BillingUser extractData(ResultSet rs) throws SQLException, DataAccessException {
						BillingUser billingUser = new BillingUser();
						while (rs.next()) {
							billingUser.setBuId(rs.getLong("BU_ID"));
							billingUser.setAccountNumber(rs.getString("ACCOUNT_NO"));
							billingUser.setUserName(rs.getString("USER_NAME"));
							billingUser.setPassword(rs.getString("PASSWORD"));
							billingUser.setContactSuffix(CommonUtils.validateObject(rs.getString("CONTACT_SUFFIX")));
							billingUser.setFirstName(rs.getString("FIRST_NAME"));
							billingUser.setLastName(rs.getString("LAST_NAME"));
							billingUser.setTitle(rs.getString("TITLE"));
							billingUser.setPhone(rs.getString("PHONE"));
							billingUser.setFax(CommonUtils.validateObject(rs.getString("FAX")));
							billingUser.setEmail(rs.getString("EMAIL"));
						}

						return billingUser;
					}
				});
	}

	@Override
	public int deleteBillingUser(DataSource lUIIADataSource, SecurityObject securityObject, BillingUser billingUser)
			throws Exception {
		String query = "UPDATE billing_user SET  DELETED = 'Y', MODIFIED_BY = ?, MODIFIED_DATE = ? WHERE ACCOUNT_NO = ? AND BU_ID = ?";
		return saveOrUpdate(lUIIADataSource, query, true, securityObject.getUsername(),
				DateTimeFormater.getSqlSysTimestamp(), securityObject.getAccountNumber(), billingUser.getBuId());
	}

	@Override
	public BillingUser billingUser(SecurityObject securityObject, Long buId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(
				" SELECT BU_ID, ACCOUNT_NO, USER_NAME, PASSWORD, CONTACT_SUFFIX, FIRST_NAME, LAST_NAME, TITLE, PHONE, FAX, EMAIL, ");
		sb.append(
				" CREATED_BY, DATE_FORMAT(CREATED_DATE,'%m/%d/%Y') AS CREATED_DATE, MODIFIED_BY, IF(MODIFIED_DATE IS NULL, '', DATE_FORMAT(MODIFIED_DATE,'%m/%d/%Y')) AS MODIFIED_DATE ");
		sb.append(" FROM billing_user ");
		sb.append(" WHERE DELETED != 'Y' AND ACCOUNT_NO = ? AND BU_ID = ? ");

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sb.toString(),
				new Object[] { securityObject.getAccountNumber(), buId }, new ResultSetExtractor<BillingUser>() {

					@Override
					public BillingUser extractData(ResultSet rs) throws SQLException, DataAccessException {
						BillingUser billingUser = new BillingUser();
						while (rs.next()) {
							billingUser.setBuId(rs.getLong("BU_ID"));
							billingUser.setAccountNumber(rs.getString("ACCOUNT_NO"));
							billingUser.setUserName(rs.getString("USER_NAME"));
							billingUser.setPassword(rs.getString("PASSWORD"));
							billingUser.setContactSuffix(CommonUtils.validateObject(rs.getString("CONTACT_SUFFIX")));
							billingUser.setFirstName(rs.getString("FIRST_NAME"));
							billingUser.setLastName(rs.getString("LAST_NAME"));
							billingUser.setTitle(rs.getString("TITLE"));
							billingUser.setPhone(rs.getString("PHONE"));
							billingUser.setFax(CommonUtils.validateObject(rs.getString("FAX")));
							billingUser.setEmail(rs.getString("EMAIL"));
							billingUser.setCreatedBy(rs.getString("CREATED_BY"));
							billingUser.setCreatedDate(rs.getString("CREATED_DATE"));
							billingUser.setModifiedBy(CommonUtils.validateObject(rs.getString("MODIFIED_BY")));
							billingUser.setModifiedDate(rs.getString("MODIFIED_DATE"));
						}

						return billingUser;
					}
				});
	}

	@Override
	public int saveBillingUser(DataSource lUIIADataSource, SecurityObject securityObject, BillingUser billingUser)
			throws Exception {
		int affectedRows = 0;
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append(" INSERT INTO billing_user ");
		insertQuery.append("(ACCOUNT_NO, USER_NAME, PASSWORD, CONTACT_SUFFIX, FIRST_NAME, LAST_NAME, TITLE, ");
		insertQuery.append(" PHONE, FAX, EMAIL, DELETED, CREATED_BY, CREATED_DATE)");
		insertQuery.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ");

		Object[] params = new Object[] { securityObject.getAccountNumber(), billingUser.getUserName(),
				billingUser.getPassword().toUpperCase(), billingUser.getContactSuffix(), billingUser.getFirstName(),
				billingUser.getLastName(), billingUser.getTitle(), billingUser.getPhone(), billingUser.getFax(),
				billingUser.getEmail(), GlobalVariables.NO, securityObject.getUsername(), Utility.getSqlSysTimestamp()

		};
		affectedRows = saveOrUpdate(lUIIADataSource, insertQuery.toString(), true, params);
		return affectedRows;

	}

	@Override
	public int updateBillingUser(DataSource lUIIADataSource, SecurityObject securityObject, BillingUser billingUser)
			throws Exception {
		int affectedRows = 0;
		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append(" UPDATE billing_user SET ");
		updateQuery.append(" PASSWORD = ?, CONTACT_SUFFIX =?, FIRST_NAME = ?, LAST_NAME =?, ");
		updateQuery.append(" TITLE = ?, PHONE =?, FAX= ?, EMAIL =?,");
		updateQuery.append(" MODIFIED_BY =?, MODIFIED_DATE = ? WHERE ACCOUNT_NO =? AND BU_ID = ? ");

		Object[] params = new Object[] { billingUser.getPassword().toUpperCase(), billingUser.getContactSuffix(),
				billingUser.getFirstName(), billingUser.getLastName(), billingUser.getTitle(), billingUser.getPhone(),
				billingUser.getFax(), billingUser.getEmail(), securityObject.getAccountNumber(),
				Utility.getSqlSysTimestamp(), securityObject.getAccountNumber(), billingUser.getBuId() };
		affectedRows = saveOrUpdate(lUIIADataSource, updateQuery.toString(), true, params);
		return affectedRows;

	}

}
