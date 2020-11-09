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
import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.AddressDet;
import com.iana.api.domain.ContactDet;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.EPJoinDet;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MCDataJsonDTO;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SecurityObject;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Repository
public class EpDaoImpl extends GenericDAO implements EpDao {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Autowired
	public Environment env;

	@Override
	public Long countEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount) throws Exception {

		List<Object> params = new ArrayList<>();
		StringBuffer sbQuery = new StringBuffer();

		sbQuery.append(" SELECT COUNT(*) ");
		sbQuery.append(" FROM account_info a,");

		if (StringUtils.isNotBlank(searchAccount.getKnownAs())) {
			sbQuery.append(" (ep_mc_join_details d, mc_ep_join_status j ");

		} else {
			sbQuery.append(
					" (mc_ep_join_status j  LEFT JOIN  ep_mc_join_details d  ON(d.mc_acct_no = j.mc_acct_no AND d.ep_acct_no = ?) ");
			params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		}
		sbQuery.append(" LEFT JOIN (mc_specific_overrides op) ON(op.mc_acct_no = j.mc_acct_no AND op.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_areq_overrides oa) ON(oa.mc_acct_no = j.mc_acct_no AND oa.ep_acct_no = ? )) ");
		sbQuery.append(" WHERE j.ep_acct_no = ? ");

		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));

		filterEPMotorCarriers(searchAccount, params, sbQuery);

		return findTotalRecordCount(this.uiiaDataSource, params.toArray(), sbQuery.toString());
	}

	@Override
	public List<JoinRecord> getEPMotorCarriers(SecurityObject securityObject, SearchAccount searchAccount)
			throws Exception {

		List<Object> params = new ArrayList<>();
		StringBuffer sbQuery = new StringBuffer();

		sbQuery.append(" SELECT a.account_no,d.ep_mem_det_id,j.mc_acct_no, j.ep_acct_no, j.mc_ep_status, ");
		sbQuery.append(" j.override_used,ep_cancel,ep_cncl_eff_dt,rsn_cancel,ep_private,ep_house, ");
		sbQuery.append(
				" d.ep_known_as, d.ep_mem, a.scac_code,a.company_name,MAX(IF(op.epmc_spc_ovr_id IS NOT NULL,'Y',IF(oa.mc_areq_id IS NOT NULL,'Y','N'))) AS ovrused ");
		sbQuery.append(" FROM account_info a, ");

		if (StringUtils.isNotBlank(searchAccount.getKnownAs())) {
			sbQuery.append(" (ep_mc_join_details d, mc_ep_join_status j ");

		} else {
			sbQuery.append(
					" (mc_ep_join_status j  LEFT JOIN  ep_mc_join_details d  ON(d.mc_acct_no = j.mc_acct_no AND d.ep_acct_no = ?) ");
			params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		}

		sbQuery.append(" LEFT JOIN (mc_specific_overrides op) ON(op.mc_acct_no = j.mc_acct_no AND op.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_areq_overrides oa) ON(oa.mc_acct_no = j.mc_acct_no AND oa.ep_acct_no = ? )) ");
		sbQuery.append(" WHERE j.ep_acct_no = ? ");

		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));

		filterEPMotorCarriers(searchAccount, params, sbQuery);

		sbQuery.append(" GROUP BY mc_acct_no ORDER BY company_name ");

		sbQuery.append(" LIMIT ?, ?");

		params.add(searchAccount.getRecordFrom());
		params.add(searchAccount.getPageSize());

//		return findAll(this.uiiaDataSource, sbQuery.toString(), params.toArray(), JoinRecord.class);

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQuery.toString(), params.toArray(),
				new ResultSetExtractor<List<JoinRecord>>() {
					@Override
					public List<JoinRecord> extractData(ResultSet rsOvrJoin) throws SQLException, DataAccessException {
						List<JoinRecord> arlJoinRecord = new ArrayList<>();

						while (rsOvrJoin.next()) {
							JoinRecord joinBean = new JoinRecord();
							EPJoinDet memDetails = new EPJoinDet();
							// Setting MC Basic details in MC override Bean
							log.debug("Account Noo:" + rsOvrJoin.getString("account_no"));
							if (rsOvrJoin.getString("account_no") != null)
								joinBean.setMcAcctNo(rsOvrJoin.getString("account_no"));
							if (rsOvrJoin.getString("company_name") != null)
								joinBean.setMcName(rsOvrJoin.getString("company_name"));
							if (rsOvrJoin.getString("scac_code") != null)
								joinBean.setMcScac(rsOvrJoin.getString("scac_code"));
							if (rsOvrJoin.getString("mc_ep_status") != null) {
								if (GlobalVariables.YES.equals(rsOvrJoin.getString("mc_ep_status"))) {
									joinBean.setMcEPStatus(GlobalVariables.APPRVD_STATUS);
								} else {
									joinBean.setMcEPStatus(GlobalVariables.NOT_APPRVD_STATUS);
								}
							} else {
								joinBean.setMcEPStatus(GlobalVariables.NOT_APPRVD_STATUS);
							}

							if (rsOvrJoin.getString("ovrused") != null) {
								joinBean.setOvrUsed(rsOvrJoin.getString("ovrused"));
							} else {
								joinBean.setOvrUsed(GlobalVariables.NO);
							}

							// Setting Member details in Member Bean
							memDetails.setEpMemDtlId(rsOvrJoin.getInt("ep_mem_det_id"));
							if (rsOvrJoin.getString("ep_cancel") != null)
								memDetails.setCancelValue(rsOvrJoin.getString("ep_cancel"));

							if (rsOvrJoin.getDate("ep_cncl_eff_dt") != null)
								memDetails.setCanEffDate(DateTimeFormater
										.formatSqlDate(rsOvrJoin.getDate("ep_cncl_eff_dt"), DateTimeFormater.FORMAT4));

							if (rsOvrJoin.getString("rsn_cancel") != null)
								memDetails.setRsnCancel(rsOvrJoin.getString("rsn_cancel"));

							if (rsOvrJoin.getString("ep_known_as") != null)
								memDetails.setKnownAs(rsOvrJoin.getString("ep_known_as"));

							if (rsOvrJoin.getString("ep_mem") != null)
								memDetails.setEpMember(rsOvrJoin.getString("ep_mem"));

							if (rsOvrJoin.getString("ep_private") != null)
								memDetails.setEpPrivate(rsOvrJoin.getString("ep_private"));

							if (rsOvrJoin.getString("ep_house") != null)
								memDetails.setEpHouse(rsOvrJoin.getString("ep_house"));

							// Setting the member bean in MCOverride Bean
							joinBean.setJoinBean(memDetails);
							joinBean.setEpAcctNo(securityObject.getAccountNumber());// added by piyush to display you
																					// need you have on EP MC join
																					// status
							arlJoinRecord.add(joinBean);
							log.debug("Exiting while(rsOvrJoin.next())");

						} // while end

						return arlJoinRecord;
					}
				});

	}

	private void filterEPMotorCarriers(SearchAccount searchAccount, List<Object> params, StringBuffer sbQuery) {

		StringBuffer tempCompName = new StringBuffer(CommonUtils.validateObject(searchAccount.getCompanyName()));
		tempCompName.append(GlobalVariables.PERCENTAGE);
		// log.debug("tempCompName:" + tempCompName);
		StringBuffer tempSCAC = new StringBuffer(CommonUtils.validateObject(searchAccount.getScac()));
		tempSCAC.append(GlobalVariables.PERCENTAGE);
		// log.debug("tempSCAC:" + tempSCAC);
		StringBuffer tempKnownAs = new StringBuffer(CommonUtils.validateObject(searchAccount.getKnownAs()));
		tempKnownAs.append(GlobalVariables.PERCENTAGE);

		sbQuery.append(" AND a.company_name LIKE ? ");
		params.add(tempCompName.toString());

		sbQuery.append(" AND j.mc_acct_no = a.account_no ");

		if ("EP200035".equalsIgnoreCase(searchAccount.getAccountNumber())) {
			sbQuery.append(" AND a.uiia_status <>'PENDING' ");

		} else if (GlobalVariables.TRAC_ACCOUNT_NO.equalsIgnoreCase(searchAccount.getAccountNumber())) {
			sbQuery.append("  AND a.mem_type IN ('MC','NON_UIIA_MC') ");

		} else {
			sbQuery.append(" AND (a.uiia_status <>'DELETED' AND a.uiia_status <>'PENDING') ");
		}

		sbQuery.append(" AND ((a.scac_code LIKE ? ) ");
		params.add(tempSCAC);

		if (StringUtils.isNotBlank(searchAccount.getScac())) {
			sbQuery.append(" OR (a.scac_code IN ("
					+ preparedMultipleSQLParamInput(searchAccount.getScac().split(GlobalVariables.COMMA).length)
					+ "))) ");
			preparedMultipleSQLValueInput(params, searchAccount.getScac().split(GlobalVariables.COMMA));
		} else {
			sbQuery.append(" ) ");
		}

		if (StringUtils.isNotEmpty(searchAccount.getKnownAs())) {

			sbQuery.append(" AND d.mc_acct_no = j.mc_acct_no ");

			sbQuery.append(" AND ( (d.ep_known_as LIKE ? ) ");
			params.add(tempKnownAs);

			sbQuery.append(" OR ( d.ep_known_as IN ("
					+ preparedMultipleSQLParamInput(searchAccount.getKnownAs().split(GlobalVariables.COMMA).length)
					+ ")) ) ");
			preparedMultipleSQLValueInput(params, searchAccount.getKnownAs().split(GlobalVariables.COMMA));

			sbQuery.append(" AND d.ep_acct_no = ? ");
			params.add(searchAccount.getAccountNumber());
		}
	}

	@Override
	public List<MCDataJsonDTO> getMCLookUpForEP(SecurityObject securityObject, SearchAccount searchAccount)
			throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuffer sbQuery = new StringBuffer();

		sbQuery.append(" SELECT a.company_name,a.scac_code,j.mc_acct_no, j.mc_ep_status, d.ep_mem  ");
		sbQuery.append(" FROM account_info a,");
		sbQuery.append(" (mc_ep_join_status j  ");
		sbQuery.append(" LEFT JOIN  ep_mc_join_details d  ON (d.mc_acct_no = j.mc_acct_no AND d.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_specific_overrides op) ON(op.mc_acct_no = j.mc_acct_no AND op.ep_acct_no = ?) ");
		sbQuery.append(" LEFT JOIN (mc_areq_overrides oa) ON(oa.mc_acct_no = j.mc_acct_no AND oa.ep_acct_no = ? )) ");
		sbQuery.append(" WHERE j.ep_acct_no = ? ");

		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));
		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));

		filterGetMCLookUpForEP(searchAccount, params, sbQuery);

		sbQuery.append(" GROUP BY j.mc_acct_no ORDER BY a.company_name ");

//		return findAll(this.uiiaDataSource, sbQuery.toString(), params.toArray(), MCDataJsonDTO.class);

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQuery.toString(), params.toArray(),
				new ResultSetExtractor<List<MCDataJsonDTO>>() {
					@Override
					public List<MCDataJsonDTO> extractData(ResultSet rsCompList)
							throws SQLException, DataAccessException {
						List<MCDataJsonDTO> mcDataDtos = new ArrayList<>();

						while (rsCompList.next()) {
							MCDataJsonDTO mcDataJsonDTO = new MCDataJsonDTO();
							mcDataJsonDTO
									.setAccountNumber(rsCompList.getString("mc_acct_no") == null ? StringUtils.EMPTY
											: rsCompList.getString("mc_acct_no"));
							mcDataJsonDTO
									.setCompanyName(rsCompList.getString("company_name") == null ? StringUtils.EMPTY
											: rsCompList.getString("company_name"));
							mcDataJsonDTO.setMcScac(rsCompList.getString("scac_code") == null ? StringUtils.EMPTY
									: rsCompList.getString("scac_code"));
							String mcEPStatus;
							if (rsCompList.getString("mc_ep_status") == null
									|| rsCompList.getString("mc_ep_status").trim().equalsIgnoreCase("")
									|| rsCompList.getString("mc_ep_status").trim().equalsIgnoreCase("null")
									|| rsCompList.getString("mc_ep_status").trim().equalsIgnoreCase("N")) {
								mcEPStatus = "Not approved";
							} else {
								mcEPStatus = "Approved";
							}
							mcDataJsonDTO.setMcEPStatus(mcEPStatus);
							String epMem = "";
							if (rsCompList.getString("ep_mem") == null
									|| rsCompList.getString("ep_mem").trim().equalsIgnoreCase("")
									|| rsCompList.getString("ep_mem").trim().equalsIgnoreCase("null")
									|| rsCompList.getString("ep_mem").trim().equalsIgnoreCase("N")) {
								epMem = "Non member";
							} else {
								epMem = "Member";
							}
							mcDataJsonDTO.setEpMemberFlag(epMem);

							mcDataDtos.add(mcDataJsonDTO);

						} // while end

						return mcDataDtos;
					}
				});
	}

	private void filterGetMCLookUpForEP(SearchAccount searchAccount, List<Object> params, StringBuffer sbQuery) {

		sbQuery.append(" AND a.company_name LIKE ? ");
		params.add(CommonUtils.validateObject(searchAccount.getCompanyName()) + GlobalVariables.PERCENTAGE);

		sbQuery.append(" AND j.mc_acct_no = a.account_no ");
		sbQuery.append(" AND (a.uiia_status <>'DELETED' AND a.uiia_status <>'PENDING') ");

		sbQuery.append(" AND a.scac_code LIKE ? ");
		params.add(CommonUtils.validateObject(searchAccount.getScac()) + GlobalVariables.PERCENTAGE);
	}

	public AccountInfo getBasicAcctDtls(String acctNo) throws Exception {
		StringBuffer qry1 = new StringBuffer(
				"SELECT a.account_no,a.company_name,a.scac_code,a.iana_mem,a.non_uiia_ep, ");
		qry1.append(
				"a.uiia_status,a.uiia_status_cd,a.mem_eff_dt,a.cancelled_dt,a.deleted_date, a.attr1, a.attr2, a.attr3, ");
		qry1.append(
				"a.re_instated_dt,a.mem_type,a.comp_url,a.modified_date,a.uiia_member,a.idd_member,e.ep_entities  FROM account_info a LEFT JOIN ep_basic_details e ON (e.ep_acct_no = a.account_no) WHERE a.account_no = ?"); // prarit
		AccountInfo acctInfo = findBean(this.uiiaDataSource, qry1.toString(), AccountInfo.class, acctNo);

		return acctInfo;

	}
	
	public AddressDet getAddress(String acctNo, String addressType) throws Exception {
		StringBuffer sbQry = new StringBuffer("SELECT addr_id,addr_street1,addr_street2,");
		sbQry.append(
				"addr_city,addr_zip,addr_state,addr_country,SAME_BILL_ADDR,ATTR1 as same_dispute_addr FROM address_master WHERE account_no = ?");
		sbQry.append(" AND addr_type = ?");
		AddressDet address = findBean(this.uiiaDataSource, sbQry.toString(), AddressDet.class, acctNo, addressType);
		return address;

	}

	public ContactDet getContact(String acctNo, String contactType) throws Exception {
		StringBuffer sbQry = new StringBuffer(
				"SELECT contct_id,contct_fname,contct_mname,contct_lname,contct_title,contct_salutation,");
		sbQry.append("contct_mr_ms,contct_suffix,contct_prm_phone,contct_sec_phone,contct_prm_fax,contct_sec_fax,");
		sbQry.append(
				"contct_prm_email,contct_sec_email,SAME_BILL_CONTCT,ATTR1 as same_dispute_cntct FROM contacts_master WHERE account_no = ?");
		sbQry.append(" AND contct_type = ?");
		ContactDet contact = findBean(this.uiiaDataSource, sbQry.toString(), ContactDet.class, acctNo, contactType);
		return contact;

	}

	public EPAcctInfo getEpAcctDtls(String acctNo) throws Exception {
		StringBuffer qry1 = new StringBuffer("SELECT e.ep_basic_info_id,e.ep_type,e.ep_notes,");
		qry1.append(
				"e.ep_rmrks,e.ep_idd_flg,e.ep_inv_req_flg,e.ep_rportng_req,e.ep_lvl_service,e.ep_entities,p.lst_bill_dt,e.admin_fee_flag "); // prarit
		qry1.append("FROM ep_basic_details e LEFT JOIN gnrl_pymnt_dtls p ON(p.account_no = e.ep_acct_no) ");
		qry1.append("WHERE e.ep_acct_no = ?");
		EPAcctInfo epAcctInfo = findBean(this.uiiaDataSource, qry1.toString(), EPAcctInfo.class, acctNo);

		return epAcctInfo;

	}

	@Override
	public boolean ifExistsSameScac(String scac, String accountNumber) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT COUNT(account_no) CNT ");
		sbQuery.append(" FROM account_info ");
		sbQuery.append(
				" WHERE scac_code = ? AND uiia_status <> ? AND account_no <> ? AND mem_type IN ('MC', 'NON_UIIA_MC', 'IDD_MC')");
		return findTotalRecordCount(this.uiiaDataSource, sbQuery.toString(), scac, GlobalVariables.DELETEDMEMBER,
				accountNumber) > 0 ? true : false;
	}

	@Override
	public int countEPBasicAcctDtls(int id, String accountNumber) throws Exception {
		String query = " SELECT COUNT(*) FROM ep_basic_details WHERE ep_basic_info_id = ? AND ep_acct_no = ? ";
		return findTotalRecordCount(this.uiiaDataSource, query, id, accountNumber).intValue();
	}

	@Override
	public int countContactDtls(int contactId, String accountNumber, String contactType) throws Exception {
		String query = " SELECT COUNT(*) FROM contacts_master WHERE contct_id = ? AND account_no = ? AND contct_type = ?";
		return findTotalRecordCount(this.uiiaDataSource, query, contactId, accountNumber, contactType).intValue();
	}

	@Override
	public int countAddressDtls(int addrId, String accountNumber, String contactType) throws Exception {
		String query = " SELECT COUNT(*) FROM address_master WHERE addr_id = ? AND account_no = ? AND addr_type = ?";
		return findTotalRecordCount(this.uiiaDataSource, query, addrId, accountNumber, contactType).intValue();
	}

	@Override
	public int updateAcctDtls(DataSource lUIIADataSource, SecurityObject securityObject, AccountMaster acctbean,
			boolean enableTransMgmt) throws Exception {

		// log.info("Entering method
		// updateAcctDtls("+conn.toString()+","+acctbean.toString()+","+securityObject.toString()+")
		// of AccountDetails class");
		int dbStatus = 0;
		StringBuffer updateQry = new StringBuffer("UPDATE account_info set ");
		updateQry.append("company_name = ?,scac_code =?,iana_mem = ?,uiia_status = ?,uiia_status_cd = ?,");
		updateQry.append("mem_eff_dt = ?,cancelled_dt = ?,deleted_date = ?,re_instated_dt = ?,comp_url = ?,");
		updateQry.append(
				"AUDIT_TRAIL_EXTRA = ?,attr1 = ?,attr2 = ?,attr3 = ?,modified_by = ?,modified_date = ?, non_uiia_ep = ?  ");

		// swati----14/9----UIIA/IDD membership related changes
		if (GlobalVariables.DELETEDMEMBER.equals(acctbean.getAcctInfo().getUiiaStatus())) {
			log.debug("If Uiia status = deleted");
			updateQry.append(" ,mem_type = IF(idd_member = ?,?,mem_type),");
			updateQry.append(" uiia_member = ? ");
		}
		if (acctbean.getAcctInfo().getIddStatus() != null
				&& GlobalVariables.DELETEDMEMBER.equals(acctbean.getAcctInfo().getIddStatus())) {
			log.debug("If IDD status = deleted");
			updateQry.append("  ,idd_member = ? ");
		}
		// Added By Tushar on 07/02/2008
		if (acctbean.getAcctInfo().getIddStatus() != null
				&& GlobalVariables.ACTIVEMEMBER.equalsIgnoreCase(acctbean.getAcctInfo().getIddStatus())) {
			log.debug("If IDD status = Active");
			updateQry.append("  ,idd_member = ? ");
		}

		if (GlobalVariables.YES.equals(acctbean.getAcctInfo().getApplyUiiaMem())
				|| (GlobalVariables.ROLE_IDD_MC.equalsIgnoreCase(securityObject.getRoleName())
						&& GlobalVariables.ROLE_NON_UIIA_MC.equals(acctbean.getAcctInfo().getMemType()))) {
			log.debug("If IDD member has applied for UIIA membership");
			updateQry.append(" ,mem_type = ?,uiia_member = ? ");
		}
		updateQry.append(" WHERE account_no = ?");

		java.sql.Date effDate = Utility.stringToSqlDate(acctbean.getAcctInfo().getMemEffDt(), Utility.FORMAT4);
		java.sql.Date canDate = Utility.stringToSqlDate(acctbean.getAcctInfo().getCancelledDt(), Utility.FORMAT4);
		java.sql.Date reinstDate = Utility.stringToSqlDate(acctbean.getAcctInfo().getReInstatedDt(), Utility.FORMAT4);
		java.sql.Date delDate = Utility.stringToSqlDate(acctbean.getAcctInfo().getDeletedDate(), Utility.FORMAT4);

		List<Object> params = new ArrayList<>();

		params.add(acctbean.getAcctInfo().getCompanyName());
		params.add(StringUtils.isBlank(acctbean.getAcctInfo().getScacCode()) ? null
				: acctbean.getAcctInfo().getScacCode());

		params.add(acctbean.getAcctInfo().getIanaMem());
		params.add(acctbean.getAcctInfo().getUiiaStatus());

		if (acctbean.getAcctInfo().getUiiaStatus().equalsIgnoreCase(GlobalVariables.ACTIVEMEMBER)) {
			params.add("");
		} else {
			params.add(acctbean.getAcctInfo().getUiiaStatus());
		}
		params.add(effDate);
		params.add(canDate);
		params.add(delDate);
		params.add(reinstDate);
		params.add(acctbean.getAcctInfo().getCompUrl());
		params.add(securityObject.getIpAddress());
		params.add(acctbean.getAcctInfo().getAttr1());
		params.add(acctbean.getAcctInfo().getAttr2());
		params.add(acctbean.getAcctInfo().getAttr3());
		params.add(securityObject.getUsername());
		params.add(DateTimeFormater.getSqlSysTimestamp());
		params.add(acctbean.getAcctInfo().getNonUiiaEp());

		// swati----14/9----UIIA/IDD membership related changes
		if (GlobalVariables.DELETEDMEMBER.equals(acctbean.getAcctInfo().getUiiaStatus())) {
			log.debug("If Uiia status = deleted");
			params.add(GlobalVariables.YES);
			params.add(GlobalVariables.IDDUSER + "_" + acctbean.getAcctInfo().getMemType());
			params.add(GlobalVariables.NO);
			params.add(acctbean.getAcctInfo().getAccountNo());
		} else if (GlobalVariables.DELETEDMEMBER.equals(acctbean.getAcctInfo().getIddStatus())) {
			log.debug("If IDD status = deleted");
			params.add(GlobalVariables.NO);
			params.add(acctbean.getAcctInfo().getAccountNo());
		}
		// Added By Tushar On 07/02/2008------------
		else if (acctbean.getAcctInfo().getIddStatus() != null
				&& GlobalVariables.ACTIVEMEMBER.equals(acctbean.getAcctInfo().getIddStatus())) {
			log.debug("If IDD status = active");
			params.add(GlobalVariables.YES);
			params.add(acctbean.getAcctInfo().getAccountNo());
		} else if (GlobalVariables.YES.equals(acctbean.getAcctInfo().getApplyUiiaMem())
				|| (GlobalVariables.ROLE_IDD_MC.equalsIgnoreCase(securityObject.getRoleName())
						&& GlobalVariables.ROLE_NON_UIIA_MC.equals(acctbean.getAcctInfo().getMemType()))) {
			log.debug("If IDD member has applied for UIIA membership");
			params.add(acctbean.getAcctInfo().getMemType());
			params.add(GlobalVariables.YES);
			params.add(acctbean.getAcctInfo().getAccountNo());
		} // end----14/9
		else // swati---18/9
		{
			params.add(acctbean.getAcctInfo().getAccountNo());
		}

		dbStatus = saveOrUpdate((enableTransMgmt ? lUIIADataSource : this.uiiaDataSource), updateQry.toString(),
				params.toArray(), enableTransMgmt);

		return dbStatus;

	}

	@Override
	public int updateAddress(DataSource lUIIADataSource, AddressDet addr, SecurityObject securityObject,
			boolean enableTransMgmt) throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" UPDATE address_master ");
		sbQuery.append(" SET driver_id = ?, addr_street1 = ?, addr_street2 = ?,");
		sbQuery.append(
				" addr_city = ?, addr_zip = ?, addr_state =?, addr_country = ?, addr_type = ?, SAME_BILL_ADDR = ?, ATTR1 = ?,");
		// added by Anirban -- ATTR1 = ? added
		sbQuery.append(" AUDIT_TRAIL_EXTRA = ?, modified_by = ?, modified_date = ? ");
		sbQuery.append(" WHERE addr_id = ? ");

		if (addr.getDriverId() == 0) {
			params.add(null);
		} else {
			params.add(addr.getDriverId());
		}

		params.add(addr.getAddrStreet1());
		params.add(addr.getAddrStreet2());
		params.add(addr.getAddrCity());
		params.add(addr.getAddrZip());
		params.add(addr.getAddrState());
		params.add(addr.getAddrCountry());
		params.add(addr.getAddrType());
		params.add(CommonUtils.validateObject(addr.getSameBillAddr()));
		params.add(CommonUtils.validateObject(addr.getSameDisputeAddr()));
		params.add(securityObject.getIpAddress());
		params.add(securityObject.getUsername());
		params.add(DateTimeFormater.getSqlSysTimestamp());

		params.add(addr.getAddrId());

		return saveOrUpdate((enableTransMgmt ? lUIIADataSource : this.uiiaDataSource), sbQuery.toString(),
				params.toArray(), enableTransMgmt);

	}

	@Override
	public int updateContact(DataSource lUIIADataSource, ContactDet contact, SecurityObject securityObject,
			boolean enableTransMgmt) throws Exception {
		List<Object> params = new ArrayList<>();

		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" UPDATE contacts_master ");
		sbQuery.append(
				" SET contct_fname = ?,contct_mname = ?,contct_lname = ?,contct_title = ?,contct_salutation = ?, ");
		sbQuery.append(
				" contct_mr_ms = ?,contct_suffix = ?,contct_prm_phone = ?,contct_sec_phone = ?,contct_prm_fax = ?,contct_sec_fax = ?, ");
		sbQuery.append(" contct_prm_email = ?,contct_sec_email = ?,contct_type = ?,SAME_BILL_CONTCT = ?,ATTR1 = ?, ");
		sbQuery.append(" AUDIT_TRAIL_EXTRA = ?,modified_by = ?, modified_date = ? ");
		sbQuery.append(" WHERE contct_id = ? ");

		params.add(contact.getContctFname());
		params.add(contact.getContctMname());
		params.add(contact.getContctLname());
		params.add(contact.getContctTitle());
		params.add(contact.getContctSalutation());
		params.add(contact.getContctMrMs());
		params.add(contact.getContctSuffix());
		params.add(contact.getContctPrmPhone());
		params.add(contact.getContctSecPhone());
		params.add(contact.getContctPrmFax());
		params.add(contact.getContctSecFax());
		params.add(contact.getContctPrmEmail());
		params.add(contact.getContctSecEmail());
		params.add(contact.getContctType());
		params.add(contact.getSameBillContct());
		params.add(contact.getSameDisputeCntct());
		params.add(securityObject.getIpAddress());
		params.add(securityObject.getUsername());
		params.add(DateTimeFormater.getSqlSysTimestamp());

		params.add(contact.getContctId());

		return saveOrUpdate((enableTransMgmt ? lUIIADataSource : this.uiiaDataSource), sbQuery.toString(),
				params.toArray(), enableTransMgmt);

	}

	@Override
	public int insertAddress(DataSource lUIIADataSource, AddressDet addr, String accountNumber,
			SecurityObject securityObject, boolean enableTransMgmt) throws Exception {

		Map<String, Object> paramMap = new HashMap<>();

		paramMap.put("ACCOUNT_NO", accountNumber);
		paramMap.put("ADDR_STREET1", addr.getAddrStreet1());
		paramMap.put("ADDR_STREET2", CommonUtils.validateObject(addr.getAddrStreet2()));
		paramMap.put("ADDR_CITY", addr.getAddrCity());
		paramMap.put("ADDR_ZIP", addr.getAddrZip());
		paramMap.put("ADDR_STATE", addr.getAddrState());
		paramMap.put("ADDR_COUNTRY", addr.getAddrCountry());
		paramMap.put("ADDR_TYPE", addr.getAddrType());
		paramMap.put("SAME_BILL_ADDR", addr.getSameBillAddr());
		paramMap.put("ATTR1", StringUtils.EMPTY); // added by Anirban
		paramMap.put("AUDIT_TRAIL_EXTRA", securityObject.getIpAddress());
		paramMap.put("CREATED_BY", CommonUtils.validateObject(securityObject.getUsername()));
		paramMap.put("CREATED_DATE", DateTimeFormater.getSqlSysTimestamp());

		return insertAndReturnGeneratedKey((enableTransMgmt ? lUIIADataSource : this.uiiaDataSource), "address_master",
				paramMap, "ADDR_ID").intValue();

	}

	@Override
	public int insertContact(DataSource lUIIADataSource, ContactDet contact, String accountNumber,
			SecurityObject securityObject, boolean enableTransMgmt) throws Exception {
		Map<String, Object> paramMap = new HashMap<>();

		paramMap.put("ACCOUNT_NO", accountNumber);
		paramMap.put("CONTCT_FNAME", contact.getContctFname());
		paramMap.put("CONTCT_MNAME", CommonUtils.validateObject(contact.getContctMname()));
		paramMap.put("CONTCT_LNAME", contact.getContctLname());
		paramMap.put("CONTCT_TITLE", contact.getContctTitle());
		paramMap.put("CONTCT_SALUTATION", CommonUtils.validateObject(contact.getContctSalutation()));
		paramMap.put("CONTCT_MR_MS", CommonUtils.validateObject(contact.getContctMrMs()));
		paramMap.put("CONTCT_SUFFIX", CommonUtils.validateObject(contact.getContctSuffix()));
		paramMap.put("CONTCT_PRM_PHONE", contact.getContctPrmPhone());
		paramMap.put("CONTCT_SEC_PHONE", CommonUtils.validateObject(contact.getContctSecPhone()));
		paramMap.put("CONTCT_PRM_FAX", contact.getContctPrmFax());
		paramMap.put("CONTCT_SEC_FAX", CommonUtils.validateObject(contact.getContctSecFax()));
		paramMap.put("CONTCT_PRM_EMAIL", contact.getContctPrmEmail());
		paramMap.put("CONTCT_SEC_EMAIL", CommonUtils.validateObject(contact.getContctSecEmail()));
		paramMap.put("CONTCT_TYPE", contact.getContctType());
		paramMap.put("SAME_BILL_CONTCT", contact.getSameBillContct());
		paramMap.put("ATTR1", StringUtils.EMPTY); // added by Anirban
		paramMap.put("AUDIT_TRAIL_EXTRA", securityObject.getIpAddress());
		paramMap.put("CREATED_BY", CommonUtils.validateObject(securityObject.getUsername()));
		paramMap.put("CREATED_DATE", DateTimeFormater.getSqlSysTimestamp());

		return insertAndReturnGeneratedKey((enableTransMgmt ? lUIIADataSource : this.uiiaDataSource), "contacts_master",
				paramMap, "CONTCT_ID").intValue();

	}

	/**
	 * inserts the basic details for EP
	 * 
	 * @param Connection     conn
	 * @param EPAcctInfoBean epacctbean
	 * @param String         accountNo
	 * @param UserBean       userInfo
	 * @return int
	 * @throws UiiaException
	 */
	@Override
	public int updateRegDetailsEP(DataSource lUIIADataSource, EPAcctInfo epAcctInfo, SecurityObject securityObject,
			boolean enableTransMgmt) throws Exception {

		List<Object> params = new ArrayList<>();
		String sAllNotes = "";

		StringBuffer updateqry = new StringBuffer("UPDATE ep_basic_details set ");
		updateqry.append("ep_type = ?,ep_notes = CONCAT(?,ep_notes),ep_rmrks = ?,ep_idd_flg = ?,ep_inv_req_flg = ?,");
		updateqry.append("ep_rportng_req = ?,ep_lvl_service = ?,AUDIT_TRAIL_EXTRA = ?,ATTR1 = ?,ATTR2 = ?,ATTR3 = ?,");
		updateqry
				.append("modified_by = ?,modified_date = ?,ep_entities =?,admin_fee_flag=? WHERE ep_basic_info_id = ?"); // prarit

		params.add(epAcctInfo.getEpType());
		if (!epAcctInfo.getNotes().equals("")) {
			sAllNotes = Utility.addPaddingToNotes(epAcctInfo.getNotes(), securityObject.getUsername());
		}
		params.add(sAllNotes);
		params.add(epAcctInfo.getEpRmrks());
		params.add(epAcctInfo.getEpIddFlg());
		params.add(epAcctInfo.getEpInvReqFlg());
		params.add(epAcctInfo.getEpRportngReq());
		params.add(epAcctInfo.getEpLvlService());
		params.add(securityObject.getIpAddress());
		params.add(epAcctInfo.getAttr1());
		params.add(epAcctInfo.getAttr2());
		params.add(epAcctInfo.getAttr3());
		params.add(CommonUtils.validateObject(securityObject.getUsername()));
		params.add(DateTimeFormater.getSqlSysTimestamp());
		params.add(epAcctInfo.getEpEntities().trim());
		params.add(epAcctInfo.getAdminFeeFlag());// EP Annual Invoice Breakdown
		params.add(epAcctInfo.getEpBasicInfoId());

		return saveOrUpdate((enableTransMgmt ? lUIIADataSource : this.uiiaDataSource), updateqry.toString(),
				params.toArray(), enableTransMgmt);

	}

}