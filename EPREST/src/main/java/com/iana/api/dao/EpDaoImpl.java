package com.iana.api.dao;

import java.sql.Date;
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
import com.iana.api.domain.AddendaDownload;
import com.iana.api.domain.AddressDet;
import com.iana.api.domain.ContactDet;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.EPJoinDet;
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
import com.iana.api.domain.acord.AutoBean;
import com.iana.api.domain.acord.CargoBean;
import com.iana.api.domain.acord.ContCargoBean;
import com.iana.api.domain.acord.ELBean;
import com.iana.api.domain.acord.EmpDishBean;
import com.iana.api.domain.acord.GenBean;
import com.iana.api.domain.acord.RefTrailerBean;
import com.iana.api.domain.acord.TrailerBean;
import com.iana.api.domain.acord.UmbBean;
import com.iana.api.domain.acord.WCBean;
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

	@Override
	public List<MCCancel> getDeletedMC(String cancRefStartDate, String cancRefEndDate, int pageIndex, int pageSize,
			String flag) throws Exception {
		log.info("EpDaoImpl :: Entering method getDeletedMC()");
		List<MCCancel> mcList = new ArrayList<>();
		StringBuffer sbMCDeleted = new StringBuffer();

		sbMCDeleted.append("SELECT company_name,account_no,scac_code,cancelled_dt,deleted_date,");
		sbMCDeleted.append(
				" REPLACE(REPLACE(REPLACE(CONCAT(uiia_status,' (',uiia_status_cd,')'),',MCHASEXPGL',''),',MCHASEXPAL',''),',C7_GL','') AS uiia_status");
		sbMCDeleted.append(" FROM account_info WHERE mem_type= 'MC'");
		sbMCDeleted.append(" AND uiia_status = 'DELETED' AND (DATE(deleted_date) between ? AND ? ) ");
		sbMCDeleted.append(" ORDER BY company_name,deleted_date ");
		if (!flag.equalsIgnoreCase("report"))
			sbMCDeleted.append(" LIMIT ?,?");

		if (cancRefStartDate.equalsIgnoreCase(GlobalVariables.EMPTY)) {
			cancRefStartDate = "01/01/1999";
		}
		if (cancRefEndDate.equalsIgnoreCase(GlobalVariables.EMPTY)) {
			cancRefEndDate = "01/01/2999";
		}

		Date cancStDt = DateTimeFormater.stringToSqlDate(cancRefStartDate, DateTimeFormater.FORMAT4);
		Date cancEndDt = DateTimeFormater.stringToSqlDate(cancRefEndDate, DateTimeFormater.FORMAT4);
		List<Object> params = new ArrayList<>();
		params.add(cancStDt);
		params.add(cancEndDt);
		if (!flag.equalsIgnoreCase("report")) {
			params.add((pageIndex * pageSize));
			params.add(pageSize);
		}
		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbMCDeleted.toString(),
				new ResultSetExtractor<List<MCCancel>>() {

					@Override
					public List<MCCancel> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {

							MCCancel mcCancel = new MCCancel();
							if (rs.getString("company_name") != null) {
								mcCancel.setCompName(rs.getString("company_name"));
							}
							if (rs.getString("account_no") != null) {
								mcCancel.setAcctNo(rs.getString("account_no"));
							}
							if (rs.getString("scac_code") != null) {
								mcCancel.setScac(rs.getString("scac_code"));
							}
							if (rs.getString("deleted_date") != null) {
								mcCancel.setCancDt(Utility.formatSqlDate(rs.getDate("deleted_date"), Utility.FORMAT4));
							}
							if (rs.getString("cancelled_dt") != null) {
								mcCancel.setAcctLstUpdt(
										Utility.formatSqlDate(rs.getDate("cancelled_dt"), Utility.FORMAT4));
							}
							if (rs.getString("uiia_status") != null) {
								mcCancel.setStatusCd(rs.getString("uiia_status"));
							}
							mcList.add(mcCancel);
						}

						return mcList;
					}
				}, params.toArray());

	}

	@Override
	public List<EPTerminalFeed> getTerminalFeedLocations(String accountNumber) throws Exception {
		log.info("EpDaoImpl :: Entering method getTerminalFeedLocations()");

		List<EPTerminalFeed> terminalFeedList;
		StringBuffer searchFeedQry = null;

		searchFeedQry = new StringBuffer(" SELECT t.company_name FROM acct_trmnl_feed_mpg a ");
		searchFeedQry.append(" JOIN terminal_details_new t ON (t.terminal_id=a.terminal_id) ");
		searchFeedQry.append(
				" WHERE a.acct_no = ? and (t.active='Y' OR t.webquery='Y') and terminal_code NOT IN ('MARTEST1','MARTEST2','TCSTEST','TESTTFS','TOMTEST') ORDER BY company_name");
		terminalFeedList = new ArrayList<EPTerminalFeed>();

		return getSpringJdbcTemplate(this.uiiaDataSource).query(searchFeedQry.toString(),
				new ResultSetExtractor<List<EPTerminalFeed>>() {

					@Override
					public List<EPTerminalFeed> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {

							EPTerminalFeed epTerminalFeed = new EPTerminalFeed();
							if (rs.getString("company_name") != null && rs.getString("company_name") != "") {
								epTerminalFeed.setTerminalFeedName(rs.getString("company_name"));
							}
							terminalFeedList.add(epTerminalFeed);
						}

						return terminalFeedList;
					}
				}, accountNumber);
	}

	/*
	 * this method gets all the EP template list based on the status
	 * (past,present,future)
	 * 
	 * @param Connection conn
	 * 
	 * @param EPTemplateBean epTemplate
	 * 
	 * @param String accountNo
	 * 
	 * @return ArrayList
	 * 
	 * @throws UiiaException
	 */
	@Override
	public List<EPTemplate> getTemplateList(EPTemplate epTemplate, String accountNo) throws Exception {
		List<EPTemplate> templateLst = new ArrayList<EPTemplate>();
		StringBuffer sbQry = new StringBuffer();

		if (GlobalVariables.PASTTEMPLATE.equalsIgnoreCase(epTemplate.getTempStatus())) {
			sbQry.append("SELECT EP_TEMPLATE_ID,EFF_DATE,ACTIVE,CREATED_DATE FROM ep_template WHERE ");
			sbQry.append("EP_ACCT_NO = '" + accountNo + "' AND EFF_DATE <= '" + Utility.getSqlSysdate());
			sbQry.append("' AND ACTIVE = 'N' LIMIT ?,?");
		} else if (GlobalVariables.PRESENTTEMPLATE.equalsIgnoreCase(epTemplate.getTempStatus())) {
			sbQry.append("SELECT EP_TEMPLATE_ID,EFF_DATE,ACTIVE,CREATED_DATE FROM ep_template WHERE ");
			sbQry.append("EP_ACCT_NO = '" + accountNo + "'");
			sbQry.append(" AND ACTIVE = 'Y' LIMIT ?,?");
		} else if (GlobalVariables.WHATIFTEMPLATE.equalsIgnoreCase(epTemplate.getTempStatus())) {
			sbQry.append("SELECT EP_TEMPLATE_ID,EFF_DATE,ACTIVE,CREATED_DATE FROM ep_template WHERE ");
			sbQry.append("EP_ACCT_NO = '" + accountNo + "' AND EFF_DATE > '" + Utility.getSqlSysdate()
					+ "' AND ACTIVE = 'W' LIMIT ?,?");
		}

		List<Object> params = new ArrayList<>();
		params.add((epTemplate.getPageNumber() * epTemplate.getLimit()));
		params.add(epTemplate.getLimit());

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<List<EPTemplate>>() {

					@Override
					public List<EPTemplate> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {
							EPTemplate epTemp = new EPTemplate();
							epTemp.setTemplateID(rs.getInt("EP_TEMPLATE_ID"));
							if (rs.getDate("EFF_DATE") != null) {
								epTemp.setEffDate(Utility.formatSqlDate(rs.getDate("EFF_DATE"), Utility.FORMAT4));
							}
							if (rs.getString("ACTIVE") != null) {
								epTemp.setDbTemplateStatus(rs.getString("ACTIVE"));
							}

							epTemp.setCreatedDate(Utility.formatSqlDate(rs.getDate("CREATED_DATE"), Utility.FORMAT4));
							epTemp.setTempStatus(epTemplate.getTempStatus());
							templateLst.add(epTemp);
						}

						return templateLst;
					}
				}, params.toArray());
	}

	@Override
	public Long countSecondaryUsers(SecurityObject securityObject, SecUserDetails secUserDetails) throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" SELECT COUNT(*) ");
		sbQuery.append(" FROM idd_secondary_users ");
		sbQuery.append(" WHERE account_no = ? ");

		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));

		filterSecondaryUsers(secUserDetails, params, sbQuery);

		return findTotalRecordCount(this.uiiaDataSource, params.toArray(), sbQuery.toString());
	}

	private void filterSecondaryUsers(SecUserDetails secUserList, List<Object> params, StringBuilder sbQuery) {

		sbQuery.append(" AND user_name LIKE ? ");
		params.add(CommonUtils.validateObject(
				secUserList.getUserName().replaceAll(GlobalVariables.SINGLE_QUOTE, GlobalVariables.APOSTROPHE))
				+ GlobalVariables.PERCENTAGE);

	}

	@Override
	public List<SecUserDetails> getSecondaryUsers(SecurityObject securityObject, SecUserDetails secUserList)
			throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(
				" SELECT idd_sec_users_id AS secUserId, IFNULL(user_name, '') AS userName, IFNULL(user_name, '') AS oldUserName, account_no, idd_fst_nm, idd_lst_nm, ");
		sbQuery.append(
				" IFNULL(password, '') AS password, IFNULL(idd_email, '') AS email, IFNULL(status, '') AS status, idd_rprt_frmt, IFNULL(idd_dload_alwd, '') AS download, IFNULL(attr1, '') AS attr1,  ");
		sbQuery.append(" IFNULL(attr2, '') AS attr2, audit_trail_extra, created_by, created_date, ");
		sbQuery.append(" modified_by, modified_date ");
		sbQuery.append(" FROM idd_secondary_users ");
		sbQuery.append(" WHERE account_no = ? ");

		params.add(CommonUtils.validateObject(securityObject.getAccountNumber()));

		filterSecondaryUsers(secUserList, params, sbQuery);

		return findAll(this.uiiaDataSource, sbQuery.toString(), params.toArray(), SecUserDetails.class);
	}

	@Override
	public SecUserDetails ifExistsSecondaryUserName(String accountNumber, String userName) throws Exception {
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" SELECT idd_sec_users_id AS secUserId, password ");
		sbQuery.append(" FROM idd_secondary_users ");
		sbQuery.append(" WHERE account_no = ? AND user_name = ? AND status = ? ");

		return findBean(this.uiiaDataSource, sbQuery.toString(), SecUserDetails.class, accountNumber, userName,
				GlobalVariables.Y);

	}

	@Override
	public void addSecondaryUser(DataSource lUIIADataSource, SecurityObject securityObject, SecUserDetails secUserList,
			boolean enableTransMgmt) throws Exception {
		secUserList.setStatus(GlobalVariables.Y);

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("USER_NAME", secUserList.getUserName());
		paramMap.put("ACCOUNT_NO", securityObject.getAccountNumber());
		paramMap.put("PASSWORD", secUserList.getPassword());
		paramMap.put("IDD_EMAIL", secUserList.getEmail());
		paramMap.put("IDD_DLOAD_ALWD", CommonUtils.validateObject(secUserList.getDownload()).toUpperCase());
		paramMap.put("ATTR1", CommonUtils.validateObject(secUserList.getAttr1()).toUpperCase());
		paramMap.put("ATTR2", CommonUtils.validateObject(secUserList.getAttr2()).toUpperCase());
		paramMap.put("STATUS", secUserList.getStatus());
		paramMap.put("AUDIT_TRAIL_EXTRA", securityObject.getIpAddress());
		paramMap.put("CREATED_BY", securityObject.getUsername());
		paramMap.put("CREATED_DATE", DateTimeFormater.getSqlSysTimestamp());

		int secUserId = insertAndReturnGeneratedKey(enableTransMgmt ? lUIIADataSource : uiiaDataSource,
				"idd_secondary_users", paramMap, "IDD_SEC_USERS_ID").intValue();

		secUserList.setSecUserId(secUserId);

	}

	@Override
	public void updateSecondaryUser(DataSource lUIIADataSource, SecurityObject securityObject,
			SecUserDetails secUserList, boolean enableTransMgmt) throws Exception {
		List<Object> params = new ArrayList<>();

		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" UPDATE idd_secondary_users ");
		sbQuery.append(" SET user_name = ?, idd_email = ?, idd_dload_alwd = ?, password = ?, attr1 = ?, attr2 = ?, ");
		sbQuery.append(" modified_by = ?, modified_date = ?, audit_trail_extra = ? ");
		sbQuery.append(" WHERE idd_sec_users_id = ? ");

		params.add(secUserList.getUserName());
		params.add(secUserList.getEmail());
		params.add(secUserList.getDownload());
		params.add(secUserList.getPassword());
		params.add(secUserList.getAttr1());
		params.add(secUserList.getAttr2());
		params.add(securityObject.getUsername());
		params.add(DateTimeFormater.getSqlSysTimestamp());
		params.add(securityObject.getIpAddress());
		params.add(secUserList.getSecUserId());

		int updatedCnt = saveOrUpdate(enableTransMgmt ? lUIIADataSource : uiiaDataSource, sbQuery.toString(),
				params.toArray(), enableTransMgmt);
		log.info("su_secondary_user: updatedCnt:" + updatedCnt);

	}

	@Override
	public int countSecondaryUsersId(SecurityObject securityObject, int selectedId) throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" SELECT COUNT(*) ");
		sbQuery.append(" FROM idd_secondary_users ");
		sbQuery.append(" WHERE ACCOUNT_NO = ? AND IDD_SEC_USERS_ID = ? ");

		params.add(securityObject.getAccountNumber());
		params.add(selectedId);

		return findTotalRecordCount(this.uiiaDataSource, params.toArray(), sbQuery.toString()).intValue();
	}

	@Override
	public SecUserDetails getSecondaryUserDetails(int secUserId) throws Exception {
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(
				" SELECT idd_sec_users_id AS secUserId, IFNULL(user_name, '') AS userName, IFNULL(user_name, '') AS oldUserName, account_no, idd_fst_nm, idd_lst_nm, ");
		sbQuery.append(
				" IFNULL(password, '') AS password, IFNULL(idd_email, '') AS email, IFNULL(status, '') AS status, idd_rprt_frmt, IFNULL(idd_dload_alwd, '') AS download, IFNULL(attr1, '') AS attr1,  ");
		sbQuery.append(" IFNULL(attr2, '') AS attr2, audit_trail_extra, created_by, created_date, ");
		sbQuery.append(" modified_by, modified_date ");
		sbQuery.append(" FROM idd_secondary_users ");
		sbQuery.append(" WHERE idd_sec_users_id = ? ");

		return findBean(this.uiiaDataSource, sbQuery.toString(), SecUserDetails.class, secUserId);

	}

	@Override
	public void deleteSecondaryUser(DataSource lUIIADataSource, SecUserDetails secUserList, boolean enableTransMgmt)
			throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuilder sbQuery = new StringBuilder();

		sbQuery.append(" UPDATE idd_secondary_users ");
		sbQuery.append(" SET status = ?, audit_trail_extra = ?, modified_by = ?, modified_date = ? ");
		sbQuery.append(" WHERE idd_sec_users_id = ? ");

		params.add(GlobalVariables.N);
		params.add(secUserList.getAuditTrailExtra());
		params.add(secUserList.getUserName());
		params.add(DateTimeFormater.getSqlSysTimestamp());
		params.add(secUserList.getSecUserId());

		saveOrUpdate(enableTransMgmt ? lUIIADataSource : uiiaDataSource, sbQuery.toString(), params.toArray(),
				enableTransMgmt);

	}

	@Override
	public List<AccountInfo> searchMemberArch(SearchAccount searchAccount, int pageIndex, int pageSize)
			throws Exception {
		List<AccountInfo> arlMemberList = new ArrayList<>();
		StringBuffer tempCompName = new StringBuffer(searchAccount.getCompanyName());
		tempCompName.append("%");
		StringBuffer tempSCAC = new StringBuffer(searchAccount.getEpScac());
		tempSCAC.append("%");
		StringBuffer tempAccNo = new StringBuffer(searchAccount.getAccountNumber());
		tempAccNo.append("%");
		StringBuffer tempMemType = new StringBuffer(searchAccount.getUserType());
		tempMemType.append("%");

		StringBuffer sbGetQuery = null;
		StringBuffer sbGetQuery1 = null;
		if (StringUtils.isNotBlank(searchAccount.getKnownAs())) {
			// we have known as code as search criteria
			sbGetQuery = new StringBuffer("SELECT account_no,company_name,scac_code,uiia_status,mem_type");
			sbGetQuery.append(" FROM account_info a WHERE ");
			sbGetQuery.append(
					" (company_name LIKE  ? AND IF(scac_code IS NOT NULL,scac_code,'%') LIKE ? AND account_no like ? ) ");

			if (tempMemType.toString().equalsIgnoreCase("IDD%")) {
				sbGetQuery.append(" AND (mem_type like ? || ");
				sbGetQuery.append(" account_no in (SELECT DISTINCT mc_acct_no FROM driver_details )) ");
			} else {
				if (GlobalVariables.ROLE_MC.equalsIgnoreCase(searchAccount.getUserType())) {
					sbGetQuery.append(" AND (mem_type LIKE ? OR mem_type LIKE ?) ");
				} else {
					sbGetQuery.append(" AND mem_type like ? ");
				}
			}
			sbGetQuery.append(" AND EXISTS (SELECT 1 FROM ep_mc_join_details e WHERE a.account_no = e.mc_acct_no ");
			sbGetQuery.append(" AND IF(e.ep_known_as is not null, e.ep_known_as,'%') LIKE ?)");
			sbGetQuery.append(" ORDER BY company_name");
		} else {
			sbGetQuery = new StringBuffer("SELECT account_no,company_name,scac_code,uiia_status,mem_type");
			sbGetQuery.append(" FROM account_info WHERE ");
			sbGetQuery.append(
					" (company_name LIKE  ? AND IF(scac_code IS NOT NULL,scac_code,'%') LIKE ? AND account_no like ? ) ");
			if (tempMemType.toString().equalsIgnoreCase("IDD%")) {
				sbGetQuery.append(" AND (mem_type like ? || ");
				sbGetQuery.append(" account_no in (SELECT DISTINCT mc_acct_no FROM driver_details )) ");
			} else {
				if (GlobalVariables.ROLE_MC.equalsIgnoreCase(searchAccount.getUserType())) {
					sbGetQuery.append(" AND (mem_type LIKE ? OR mem_type LIKE ? ) ");

				} else {
					sbGetQuery.append(" AND mem_type like ? ");
				}
			}
			sbGetQuery.append(" ORDER BY company_name");
		}
		if (!GlobalVariables.YES.equals(searchAccount.getSkipPagination())) {
			log.debug("Pagination added in Query");
			sbGetQuery.append(" LIMIT ?,?");
		}

		List<Object> params = new ArrayList<>();
		params.add(tempCompName.toString());
		params.add(tempSCAC.toString());
		params.add(tempAccNo.toString());

		if (GlobalVariables.ROLE_MC.equalsIgnoreCase(searchAccount.getUserType())) {
			params.add(GlobalVariables.ROLE_MC + GlobalVariables.PERCENTAGE);
			params.add(GlobalVariables.ROLE_NON_UIIA_MC + GlobalVariables.PERCENTAGE);

		} else {
			params.add(tempMemType);
		}

		if (StringUtils.isNotBlank(searchAccount.getKnownAs())) {
			StringBuffer tmpKnwAs = new StringBuffer(searchAccount.getKnownAs());
			tmpKnwAs.append("%");
			params.add(tmpKnwAs.toString());
		}

		if (!GlobalVariables.YES.equals(searchAccount.getSkipPagination())) {
			log.debug("Pagination parameters added");
			params.add((pageIndex * pageSize));
			params.add(pageSize);
		}
		if (searchAccount != null) {

			getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(),
					new ResultSetExtractor<List<AccountInfo>>() {

						@Override
						public List<AccountInfo> extractData(ResultSet rsMemList)
								throws SQLException, DataAccessException {

							while (rsMemList.next()) {
								AccountInfo acctDetails = new AccountInfo();
								acctDetails.setAccountNo(rsMemList.getString("account_no"));
								acctDetails.setCompanyName(rsMemList.getString("company_name"));
								acctDetails.setScacCode(rsMemList.getString("scac_code"));
								acctDetails.setUiiaStatus(rsMemList.getString("uiia_status"));
								acctDetails.setMemType(rsMemList.getString("mem_type"));
								arlMemberList.add(acctDetails);
							}

							return arlMemberList;
						}
					}, params.toArray());

			if (arlMemberList == null || arlMemberList.isEmpty()) {
				if (StringUtils.isNotBlank(searchAccount.getKnownAs())) {
					// we have known as code as search criteria
					sbGetQuery1 = new StringBuffer(
							"SELECT distinct account_no,company_name,scac_code,uiia_status,mem_type");
					sbGetQuery1.append(" FROM arch_account_info a WHERE ");
					sbGetQuery1.append(
							" (company_name LIKE  ? AND IF(scac_code IS NOT NULL,scac_code,'%') LIKE ? AND account_no like ? ) ");

					if (tempMemType.toString().equalsIgnoreCase("IDD%")) {
						sbGetQuery1.append(" AND (mem_type like ? || ");
						sbGetQuery1.append(" account_no in (SELECT DISTINCT mc_acct_no FROM driver_details)) ");
					} else {
						sbGetQuery1.append(" AND mem_type like ? ");
					}
					sbGetQuery1.append(
							" AND EXISTS (SELECT 1 FROM ep_mc_join_details e WHERE a.account_no = e.mc_acct_no ");
					sbGetQuery1.append(" AND IF(e.ep_known_as is not null, e.ep_known_as,'%') LIKE ?)");
					sbGetQuery1.append(" ORDER BY company_name");
				} else {
					sbGetQuery1 = new StringBuffer(
							"SELECT distinct account_no,company_name,scac_code,uiia_status,mem_type");
					sbGetQuery1.append(" FROM arch_account_info WHERE ");
					sbGetQuery1.append(
							" (company_name LIKE  ? AND IF(scac_code IS NOT NULL,scac_code,'%') LIKE ? AND account_no like ? ) ");
					if (tempMemType.toString().equalsIgnoreCase("IDD%")) {
						sbGetQuery1.append(" AND (mem_type like ? || ");
						sbGetQuery1.append(" account_no in (SELECT DISTINCT mc_acct_no FROM driver_details )) ");
					} else {
						sbGetQuery1.append(" AND mem_type like ? ");
					}
					sbGetQuery1.append(" ORDER BY company_name");
				}
				if (!GlobalVariables.YES.equals(searchAccount.getSkipPagination())) {
					log.debug("Pagination added in Query");
					sbGetQuery1.append(" LIMIT ?,?");
				}
				params = new ArrayList<>();
				params.add(tempCompName.toString());
				params.add(tempSCAC.toString());
				params.add(tempAccNo.toString());
				params.add(tempMemType.toString());

				// use this variable for adjusting count for limit parameters
				// in case known as code is present.
				int j = 5;

				if (StringUtils.isNotBlank(searchAccount.getKnownAs())) {
					StringBuffer tmpKnwAs = new StringBuffer(searchAccount.getKnownAs());
					tmpKnwAs.append("%");
					params.add(tmpKnwAs.toString());
					j++;// i is now 6
				}

				if (!GlobalVariables.YES.equals(searchAccount.getSkipPagination())) {
					log.debug("Pagination parameters added");
					params.add((pageIndex * pageSize));
					params.add(pageSize);
				}
				getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery1.toString(),
						new ResultSetExtractor<List<AccountInfo>>() {

							@Override
							public List<AccountInfo> extractData(ResultSet rsMemList)
									throws SQLException, DataAccessException {

								while (rsMemList.next()) {
									AccountInfo acctDetails = new AccountInfo();
									acctDetails.setAccountNo(rsMemList.getString("account_no"));
									acctDetails.setCompanyName(rsMemList.getString("company_name"));
									acctDetails.setScacCode(rsMemList.getString("scac_code"));
									acctDetails.setUiiaStatus(rsMemList.getString("uiia_status"));
									acctDetails.setMemType(rsMemList.getString("mem_type"));
									arlMemberList.add(acctDetails);
								}

								return arlMemberList;
							}
						}, params.toArray());
			}

		}
		return arlMemberList;
	}

	@Override
	public String getEPAccountNumber(String epName, String epSCAC) throws Exception {
		String epAccNo = "";
		String epnmQuery = "SELECT account_no from account_info where company_name ='" + epName + "' and mem_type ='"
				+ GlobalVariables.ROLE_EP + "'";
		String epscacQuery = "SELECT account_no from account_info where scac_code ='" + epSCAC + "' and mem_type ='"
				+ GlobalVariables.ROLE_EP + "'";
		String queryForExecution = "";
		if (!epName.equals("")) {
			queryForExecution = epnmQuery;
		} else if (!epSCAC.equals("")) {
			queryForExecution = epscacQuery;
		}
		epAccNo = findObject(this.uiiaDataSource, queryForExecution.toString(), String.class);

		return epAccNo;
	}

	@Override
	public MCAcctInfo getMCBasicInfo(SearchAccount searchparams) throws Exception {

		String sMCAcctNo = "";
		String sMCName = "%";
		String sMCScac = "%";

		if (!searchparams.getAccountNumber().equals("")) {
			sMCAcctNo = searchparams.getAccountNumber();
		} else {
			sMCAcctNo = "%";
		}
		/*
		 * if(searchparams.getCompanyName()!= "") { sMCName =
		 * searchparams.getCompanyName(); } else { sMCName = "%"; }
		 * if(!searchparams.getSCAC().equals("")) { sMCScac = searchparams.getSCAC(); }
		 * else { sMCScac = "%"; }
		 */

		StringBuffer sbQry = new StringBuffer(
				"SELECT a.account_no,a.company_name,a.scac_code,a.iana_mem,a.uiia_status,a.mem_type, ");
		sbQry.append("ad.addr_street1,ad.addr_street2,ad.addr_city,ad.addr_zip,ad.addr_state,ad.addr_country,");
		sbQry.append(
				"c.contct_fname,c.contct_lname,c.contct_mname,c.contct_prm_phone,c.contct_prm_fax,c.contct_prm_email,date_format(a.deleted_date,'%m/%d/%Y') as deleted_date ");
		sbQry.append("FROM account_info a ");
		sbQry.append("LEFT OUTER JOIN address_master ad ON (ad.addr_type = ? AND ad.account_no = a.account_no ) ");
		sbQry.append("LEFT OUTER JOIN contacts_master c ON (c.contct_type = ? AND c.account_no = a.account_no ) ");
		sbQry.append("WHERE a.account_no LIKE ? AND ");
		sbQry.append("a.company_name LIKE ? AND a.scac_code LIKE ? ");
		sbQry.append("AND (a.mem_eff_dt IS NULL OR date_format(a.mem_eff_dt,'%Y-%m-%d') <= ?) ");
		sbQry.append(" UNION SELECT a.account_no,a.company_name,a.scac_code,");
		sbQry.append(
				"a.iana_mem,a.uiia_status,a.mem_type,ad.addr_street1,ad.addr_street2,ad.addr_city,ad.addr_zip,ad.addr_state,ad.addr_country,");
		sbQry.append(
				"c.contct_fname,c.contct_lname,c.contct_mname,c.contct_prm_phone,c.contct_prm_fax,c.contct_prm_email,date_format(a.deleted_date,'%m/%d/%Y') as deleted_date ");
		sbQry.append("FROM arch_account_info a ");
		sbQry.append(
				"LEFT OUTER JOIN arch_address_master ad ON (ad.addr_type = ? AND ad.account_no = a.account_no AND (date_format(ad.eff_start_dt,'%Y-%m-%d') <= ? OR date_format(ad.eff_end_dt,'%Y-%m-%d') > ?)) ");
		sbQry.append(
				"LEFT OUTER JOIN arch_contacts_master c ON (c.contct_type = ? AND c.account_no = a.account_no AND (date_format(c.eff_start_dt,'%Y-%m-%d') <= ? OR date_format(c.eff_end_dt,'%Y-%m-%d') > ?)) ");
		sbQry.append("WHERE a.account_no LIKE ?  ");
		sbQry.append("AND a.company_name LIKE ? AND a.scac_code LIKE ? ");
		sbQry.append("AND (date_format(a.eff_start_dt,'%Y-%m-%d') <=? AND date_format(a.eff_end_dt,'%Y-%m-%d') > ?) ");

		log.debug("sMCAcctNo:::" + sMCAcctNo);
		log.debug("sMCName:::" + sMCName);
		log.debug("sMCScac:::" + sMCScac);
		log.debug("searchparams.getDate():::" + searchparams.getDate());

		List<Object> params = new ArrayList<>();
		params.add(GlobalVariables.CONTACTADDTYPE);
		params.add(GlobalVariables.CONTACTADDTYPE);
		params.add(sMCAcctNo);
		params.add(sMCName);
		params.add(sMCScac);
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(GlobalVariables.CONTACTADDTYPE);
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(GlobalVariables.CONTACTADDTYPE);
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(sMCAcctNo);
		params.add(sMCName);
		params.add(sMCScac);
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));

		log.info("Query fired is " + sbQry.toString());

		MCAcctInfo mcAcctInfo = getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<MCAcctInfo>() {

					@Override
					public MCAcctInfo extractData(ResultSet rs) throws SQLException, DataAccessException {
						MCAcctInfo mcBean = new MCAcctInfo();
						while (rs.next()) {
							log.debug("Inside Record Set");
							if (rs.getString("account_no") != null) {
								mcBean.getAcctInfo().setAccountNo(rs.getString("account_no"));
							}
							if (rs.getString("company_name") != null) {
								mcBean.getAcctInfo().setCompanyName(rs.getString("company_name"));
							}
							if (rs.getString("scac_code") != null) {
								mcBean.getAcctInfo().setScacCode(rs.getString("scac_code"));
							}
							if (rs.getString("iana_mem") != null) {
								mcBean.getAcctInfo().setIanaMem(rs.getString("iana_mem"));
							}
							if (rs.getString("uiia_status") != null) {
								mcBean.getAcctInfo().setUiiaStatus(rs.getString("uiia_status"));
							}
							if (rs.getString("addr_street1") != null) {
								mcBean.getCntctAdd().setAddrStreet1(rs.getString("addr_street1"));
							}
							if (rs.getString("addr_street1") != null) {
								mcBean.getCntctAdd().setAddrStreet1(rs.getString("addr_street1"));
							}
							if (rs.getString("addr_street2") != null) {
								mcBean.getCntctAdd().setAddrStreet2(rs.getString("addr_street2"));
							}
							if (rs.getString("addr_city") != null) {
								mcBean.getCntctAdd().setAddrCity(rs.getString("addr_city"));
							}
							if (rs.getString("addr_zip") != null) {
								mcBean.getCntctAdd().setAddrZip(rs.getString("addr_zip"));
							}
							if (rs.getString("addr_state") != null) {
								mcBean.getCntctAdd().setAddrState(rs.getString("addr_state"));
							}
							if (rs.getString("addr_country") != null) {
								mcBean.getCntctAdd().setAddrCountry(rs.getString("addr_country"));
							}
							if (rs.getString("contct_fname") != null) {
								mcBean.getCntctInfo().setContctFname(rs.getString("contct_fname"));
							}
							if (rs.getString("contct_mname") != null) {
								mcBean.getCntctInfo().setContctMname(rs.getString("contct_mname"));
							}
							if (rs.getString("contct_lname") != null) {
								mcBean.getCntctInfo().setContctLname(rs.getString("contct_lname"));
							}
							if (rs.getString("contct_prm_phone") != null) {
								mcBean.getCntctInfo().setContctPrmPhone(rs.getString("contct_prm_phone"));
							}
							if (rs.getString("contct_prm_fax") != null) {
								mcBean.getCntctInfo().setContctPrmFax(rs.getString("contct_prm_fax"));
							}
							if (rs.getString("contct_prm_email") != null) {
								mcBean.getCntctInfo().setContctPrmEmail(rs.getString("contct_prm_email"));
							}
							if (rs.getString("deleted_date") != null) {
								mcBean.getAcctInfo().setDeletedDate(rs.getString("deleted_date"));
							}
							if (rs.getString("mem_type") != null) {
								mcBean.getAcctInfo().setMemType(rs.getString("mem_type"));
							}
						}
						return mcBean;

					}
				}, params.toArray());

		log.info("Exiting method getMCBasicInfo of class ArchivedData with return value " + mcAcctInfo);
		return mcAcctInfo;
	}

	/*
	 * checks if there are any active policies for a given MC
	 * 
	 * @param Connection conn
	 * 
	 * @param SearchAccountBean searchparams
	 * 
	 * @return HashMap
	 * 
	 * @throws UiiaException
	 */
	@Override
	public Map<String, Object> getInPlacePolicyForMC(SearchAccount searchparams) throws Exception {
		Map<String, Object> activePolicies = new HashMap<>();

		// StringBuffer sbQry = new StringBuffer("SELECT
		// m.certi_id,m.policy_mst_id,m.policy_no,m.mc_acct_no,m.policy_code,");
		StringBuffer sbQry = new StringBuffer(
				"SELECT m.certi_id,m.policy_mst_id,m.policy_no,m.mc_acct_no,m.policy_code,m.mem_chk_flg,m.ti_endorsement_ll,");
		sbQry.append(
				"m.policy_type,m.policy_eff_dt,m.policy_exp_dt,m.policy_ovrwrtn_dt,m.policy_trmintn_entr_dt,m.policy_trmintn_eff_dt,m.policy_trmintn_rsn,m.policy_reinstd_entr_dt,");
		sbQry.append(
				"m.policy_reinstd_eff_dt,m.policy_reinstd_rsn,m.policy_deductible,IF(p.acv='Y','ACV',m.policy_limit) as policy_limit,m.self_insured,m.currency,m.insurer_name,m.naic_no,m.rrg_flag,m.addnl_insrd_flag,");
		sbQry.append(
				"m.policy_status,m.blnkt_reqd,m.blnkt_wording,m.policy_inplace,m.attr1,m.attr2,m.attr3,m.mc_name,m.mc_scac,p.policy_dtl_id,p.no_of_claims,p.no_of_occur,p.dmg_to_rntd_premises,");
		sbQry.append(
				"p.medi_expense,p.prsnl_adv_inj,p.gen_agg,p.products,p.bdly_inj_perprsn,p.bdly_inj_peraccdnt,p.prop_dmg_peraccdnt,p.stnd_endo,");
		sbQry.append(
				"p.form_ncs_90,p.hauls_own_only,p.acv,p.wc_statuatory_lmts,p.el_each_occur,p.el_disease_ea_emp,p.el_disease_policy_lmt,");
		sbQry.append(
				"p.ulmtd_el_lmts,p.exempt,p.any,p.scheduled,p.hired,p.all_owned,p.non_owned,p.attr1 AS dattr1,p.attr2 AS dattr2,p.attr3 AS dattr3,a.company_name,if(ins.best_rtg is null,'SELF INSURED',ins.best_rtg) AS best_rating,c.certi_eff_date as certidate,c.certi_no,c.ia_acct_no,m.tmp_term_date,m.tmp_reins_date ");
		sbQry.append(
				"FROM policy_master m LEFT OUTER JOIN policy_details p ON(p.policy_mst_id = m.policy_mst_id),acord_certificates c,account_info a");
		sbQry.append(
				",policy_master m1 LEFT OUTER JOIN insurance_company_details ins on(m1.insurer_name = ins.co_name) ");
		sbQry.append(
				"WHERE m.mc_acct_no = ? AND m.policy_inplace = ? AND (date_format(m.modified_date,'%Y-%m-%d') <= ? OR m.modified_date IS NULL)  ");
		sbQry.append(
				"AND c.certi_id = m.certi_id AND c.certi_eff_date <= ? AND a.account_no = c.ia_acct_no AND m.policy_mst_id=m1.policy_mst_id AND m.mc_name = '"
						+ Utility.convertString(searchparams.getCompanyName()) + "' ");

		// sbQry.append("UNION SELECT
		// m.certi_id,m.policy_mst_id,m.policy_no,m.mc_acct_no,m.policy_code,");
		sbQry.append(
				"UNION SELECT m.certi_id,m.policy_mst_id,m.policy_no,m.mc_acct_no,m.policy_code,m.mem_chk_flg,m.ti_endorsement_ll,");
		sbQry.append(
				"m.policy_type,m.policy_eff_dt,m.policy_exp_dt,m.policy_ovrwrtn_dt,m.policy_trmintn_entr_dt,m.policy_trmintn_eff_dt,m.policy_trmintn_rsn,");
		sbQry.append(
				"m.policy_reinstd_entr_dt,m.policy_reinstd_eff_dt,m.policy_reinstd_rsn,m.policy_deductible,IF(p.acv='Y','ACV',m.policy_limit) as policy_limit,m.self_insured,m.currency,m.insurer_name,m.naic_no,");
		sbQry.append(
				"m.rrg_flag,m.addnl_insrd_flag,m.policy_status,m.blnkt_reqd,m.blnkt_wording,m.policy_inplace,m.attr1,m.attr2,m.attr3,m.mc_name,m.mc_scac,p.policy_dtl_id,p.no_of_claims,p.no_of_occur,");
		sbQry.append(
				"p.dmg_to_rntd_premises,p.medi_expense,p.prsnl_adv_inj,p.gen_agg,p.products,p.bdly_inj_perprsn,p.bdly_inj_peraccdnt,p.prop_dmg_peraccdnt,p.stnd_endo,");
		sbQry.append(
				"p.form_ncs_90,p.hauls_own_only,p.acv,p.wc_statuatory_lmts,p.el_each_occur,p.el_disease_ea_emp,p.el_disease_policy_lmt,");
		sbQry.append(
				"p.ulmtd_el_lmts,p.exempt,p.any,p.scheduled,p.hired,p.all_owned,p.non_owned,p.attr1 AS dattr1,p.attr2 AS dattr2,p.attr3 AS dattr3,a.company_name,if(ins.best_rtg is null,'SELF INSURED',ins.best_rtg) AS best_rating,c.certi_eff_date as certidate,c.certi_no,c.ia_acct_no,m.tmp_term_date,m.tmp_reins_date ");
		sbQry.append(
				"FROM arch_policy_master m LEFT OUTER JOIN policy_details p ON(p.policy_mst_id = m.policy_mst_id),acord_certificates c,account_info a ");
		sbQry.append(
				",policy_master m1 LEFT OUTER JOIN insurance_company_details ins on(m1.insurer_name = ins.co_name) ");
		sbQry.append("WHERE m.mc_acct_no = ? AND m.policy_inplace = ? "
				+ " AND (IF(m.policy_code='AL',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='AL' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='GL',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='GL' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='CARGO',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='CARGO' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='TI',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='TI' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='WC',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='WC' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='CONTCARGO',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='CONTCARGO' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='REFTRAILER',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='REFTRAILER' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='EMPDHBOND',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='EMPDHBOND' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')"
				+ " OR IF(m.policy_code='EL',(SELECT MAX(eff_end_dt) FROM arch_policy_master apm,acord_certificates c where apm.mc_acct_no ='"
				+ searchparams.getAccountNumber()
				+ "' AND apm.policy_code='EL' AND apm.policy_type='PRIMARY' AND c.certi_id = apm.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' )=m.eff_end_dt,'')) ");
		sbQry.append(
				" AND c.certi_id = m.certi_id AND c.certi_eff_date <= ? AND a.account_no = c.ia_acct_no AND m.policy_mst_id=m1.policy_mst_id AND m.mc_name='"
						+ Utility.convertString(searchparams.getCompanyName()) + "'");
		sbQry.append(" AND m.policy_code not in (SELECT m.policy_code FROM policy_master m");
		sbQry.append(
				" LEFT OUTER JOIN policy_details p ON(p.policy_mst_id = m.policy_mst_id),acord_certificates c,account_info a,policy_master m1");
		sbQry.append(" LEFT OUTER JOIN insurance_company_details ins on(m1.insurer_name = ins.co_name)");
		sbQry.append(" WHERE m.mc_acct_no = '" + searchparams.getAccountNumber()
				+ "' AND m.policy_inplace = 'Y' AND (date_format(m.modified_date,'%Y-%m-%d') <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4)
				+ "' OR m.modified_date IS NULL) AND eff_end_dt <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "'");
		sbQry.append(" AND c.certi_id = m.certi_id AND c.certi_eff_date <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4) + "' AND eff_end_dt <= '"
				+ Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4)
				+ "' AND a.account_no = c.ia_acct_no");
		sbQry.append(" AND m.policy_mst_id=m1.policy_mst_id AND m.mc_name = '"
				+ Utility.convertString(searchparams.getCompanyName()) + "')");

		log.info("Query fired is " + sbQry.toString());

		List<Object> params = new ArrayList<>();
		params.add(searchparams.getAccountNumber());
		params.add(GlobalVariables.YES);
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));
		params.add(searchparams.getAccountNumber());
		params.add(GlobalVariables.YES);
		params.add(Utility.stringToSqlDate(searchparams.getDate(), Utility.FORMAT4));

		activePolicies = getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<Map<String, Object>>() {
					@Override
					public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
						/*
						 * calling the method which will retrieve value from the resultset and populate
						 * the hash table of policybeans according to the policy code and policy type
						 */
						return populatePolicyBean(rs);
					}
				}, params.toArray());

		return activePolicies;
	}

	/*
	 * this method obtains values from the resultset and according to the policy
	 * code creates an instance of the bean of that policy code and puts it in a
	 * hash table
	 * 
	 * @param Connection conn
	 * 
	 * @param Resultset rs
	 * 
	 * @return HashMap
	 * 
	 * @throws UiiaException
	 * 
	 */
	public Map<String, Object> populatePolicyBean(ResultSet rs) throws SQLException {
		// log.info("Enterting method
		// populatePolicyBean("+conn.toString()+","+rs.toString()+") of class
		// PolicyMaster");
		Map<String, Object> policybeans = new HashMap<>();

		ArrayList arlAL = new ArrayList();
		ArrayList arlGL = new ArrayList();
		ArrayList arlCL = new ArrayList();
		ArrayList arlTI = new ArrayList();
		ArrayList arlCC = new ArrayList();
		ArrayList arlWC = new ArrayList();
		ArrayList arlEL = new ArrayList();
		ArrayList arlRTI = new ArrayList();
		ArrayList arlEDH = new ArrayList();
		ArrayList arlUMB = new ArrayList();

		while (rs.next()) {
			// log.debug("Getting values from resultset");
			String sbKey = "";

			// log.debug("Policy code obtained from resultset is
			// "+rs.getString("POLICY_CODE"));

			if (GlobalVariables.AUTOPOLICY.equals(rs.getString("POLICY_CODE"))) {
				// log.debug("if policy code is autopolicy");
				AutoBean albean = new AutoBean();

				albean.setCertiId(rs.getInt("certi_id"));
				albean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					albean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					albean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					albean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					albean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				albean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				albean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				albean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				albean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				albean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					albean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				albean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				albean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					albean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				albean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				if ("ACV".equals(rs.getString("POLICY_LIMIT"))) {
					albean.setLimit("ACV");
				} else {
					albean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				}

				if (rs.getString("SELF_INSURED") != null) {
					albean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					albean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					albean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					albean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					albean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					albean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					albean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					albean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}
				albean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));

				if (rs.getString("policy_inplace") != null) {
					albean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					albean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					albean.setAttr2(rs.getString("ATTR2"));
				}

				albean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					albean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					albean.setMcScac(rs.getString("mc_scac"));
				}

				albean.setPolicyDetId(rs.getInt("POLICY_DTL_ID"));
				albean.setBdlyInjrdPerPerson(
						Utility.createCommaString(Utility.intToString(rs.getInt("BDLY_INJ_PERPRSN"))));
				albean.setBdlyInjrdPerAccdnt(
						Utility.createCommaString(Utility.intToString(rs.getInt("BDLY_INJ_PERACCDNT"))));
				albean.setPropDmgPerAccdnt(
						Utility.createCommaString(Utility.intToString(rs.getInt("PROP_DMG_PERACCDNT"))));
				if (rs.getString("STND_ENDO") != null) {
					albean.setStdEndo(rs.getString("STND_ENDO"));
				}
				if (rs.getString("FORM_NCS_90") != null) {
					albean.setFrmMCS90(rs.getString("FORM_NCS_90"));
				}
				if (rs.getString("ANY") != null) {
					albean.setAny(rs.getString("ANY"));
				}
				if (rs.getString("SCHEDULED") != null) {
					albean.setScheduled(rs.getString("SCHEDULED"));
				}
				if (rs.getString("HIRED") != null) {
					albean.setHired(rs.getString("HIRED"));
				}
				if (rs.getString("ALL_OWNED") != null) {
					albean.setAllOwned(rs.getString("ALL_OWNED"));
				}
				if (rs.getString("NON_OWNED") != null) {
					albean.setNonOwned(rs.getString("NON_OWNED"));
				}
				if (rs.getString("dattr1") != null) {
					albean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr2") != null) {
					albean.setPDetAttr2(rs.getString("dattr2"));
				}

				albean.setPDetAttr3(Utility.doubleToString(rs.getDouble("dattr3")));
				if (rs.getString("company_name") != null) {
					albean.setInsuranceAgent(rs.getString("company_name"));
				}
				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					albean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					albean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					albean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					albean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					albean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					albean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================
				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.AUTOPOLICY + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlAL.add(albean);
					policybeans.put(sbKey, arlAL);
				} else {
					policybeans.put(sbKey, albean);
				}
				// policybeans.put(sbKey,albean);
			} else if (GlobalVariables.GENPOLICY.equals(rs.getString("POLICY_CODE"))) {
				GenBean glbean = new GenBean();

				glbean.setCertiId(rs.getInt("certi_id"));
				glbean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					glbean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					glbean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					glbean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					glbean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				glbean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				glbean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				glbean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				glbean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				glbean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					glbean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				glbean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				glbean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					glbean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				glbean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				glbean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));

				if (rs.getString("SELF_INSURED") != null) {
					glbean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					glbean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					glbean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					glbean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					glbean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					glbean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					glbean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					glbean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				glbean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					glbean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					glbean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					glbean.setAttr2(rs.getString("ATTR2"));
				}

				glbean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					glbean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					glbean.setMcScac(rs.getString("mc_scac"));
				}

				glbean.setPolicyDetId(rs.getInt("POLICY_DTL_ID"));
				glbean.setClaims(Utility.intToString(rs.getInt("NO_OF_CLAIMS")));
				glbean.setOccurence(Utility.intToString(rs.getInt("NO_OF_OCCUR")));
				glbean.setDmgRntdPremises(
						Utility.createCommaString(Utility.intToString(rs.getInt("DMG_TO_RNTD_PREMISES"))));
				glbean.setMedExpenses(Utility.createCommaString(Utility.intToString(rs.getInt("MEDI_EXPENSE"))));
				glbean.setPrsnlAdvInj(Utility.createCommaString(Utility.intToString(rs.getInt("PRSNL_ADV_INJ"))));
				glbean.setGenAgg(Utility.createCommaString(Utility.intToString(rs.getInt("GEN_AGG"))));
				glbean.setProducts(Utility.createCommaString(Utility.intToString(rs.getInt("PRODUCTS"))));

				if (rs.getString("dattr1") != null) {
					glbean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr1") != null) {
					glbean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr2") != null) {
					glbean.setPDetAttr2(rs.getString("dattr2"));
				}

				glbean.setPDetAttr3(Utility.doubleToString(rs.getDouble("dattr3")));
				if (rs.getString("company_name") != null) {
					glbean.setInsuranceAgent(rs.getString("company_name"));
				}

				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					glbean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					glbean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					glbean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					glbean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					glbean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					glbean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================

				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.GENPOLICY + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlGL.add(glbean);
					policybeans.put(sbKey, arlGL);
				} else {
					policybeans.put(sbKey, glbean);
				}
				// policybeans.put(sbKey,glbean);
			} else if (GlobalVariables.CARGOPOLICY.equals(rs.getString("POLICY_CODE"))) {
				CargoBean clbean = new CargoBean();

				clbean.setCertiId(rs.getInt("certi_id"));
				clbean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					clbean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					clbean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					clbean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					clbean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				clbean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				clbean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				clbean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				clbean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				clbean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					clbean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				clbean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				clbean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					clbean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				clbean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				clbean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));

				if (rs.getString("SELF_INSURED") != null) {
					clbean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					clbean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					clbean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					clbean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					clbean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					clbean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					clbean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					clbean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				clbean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					clbean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					clbean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					clbean.setAttr2(rs.getString("ATTR2"));
				}

				clbean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					clbean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					clbean.setMcScac(rs.getString("mc_scac"));
				}

				clbean.setPolicyDetId(rs.getInt("POLICY_DTL_ID"));

				if (rs.getString("HAULS_OWN_ONLY") != null) {
					clbean.setHaulsOwnOnly(rs.getString("HAULS_OWN_ONLY"));
				}
				if (rs.getString("dattr1") != null) {
					clbean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr2") != null) {
					clbean.setPDetAttr2(rs.getString("dattr2"));
				}

				clbean.setPDetAttr3(Utility.doubleToString(rs.getDouble("dattr3")));
				if (rs.getString("company_name") != null) {
					clbean.setInsuranceAgent(rs.getString("company_name"));
				}

//					Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					clbean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					clbean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					clbean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					clbean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					clbean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					clbean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================
				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.CARGOPOLICY + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlCL.add(clbean);
					policybeans.put(sbKey, arlCL);
				} else {
					policybeans.put(sbKey, clbean);
				}
				// policybeans.put(sbKey,clbean);
			} else if (GlobalVariables.TRAILERPOLICY.equals(rs.getString("POLICY_CODE"))) {
				TrailerBean tlbean = new TrailerBean();

				tlbean.setCertiId(rs.getInt("certi_id"));
				tlbean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					tlbean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					tlbean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					tlbean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					tlbean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				tlbean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				tlbean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				tlbean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				tlbean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				tlbean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					tlbean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				tlbean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				tlbean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					tlbean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				tlbean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				if ("ACV".equals(rs.getString("POLICY_LIMIT"))) {
					tlbean.setLimit("ACV");
				} else {
					tlbean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				}
				if (rs.getString("SELF_INSURED") != null) {
					tlbean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					tlbean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					tlbean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					tlbean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					tlbean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					tlbean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					tlbean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					tlbean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}
				if (rs.getString("policy_inplace") != null) {
					tlbean.setInPlace(rs.getString("policy_inplace"));
				}
				tlbean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("ATTR1") != null) {
					tlbean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					tlbean.setAttr2(rs.getString("ATTR2"));
				}

				tlbean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					tlbean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					tlbean.setMcScac(rs.getString("mc_scac"));
				}

				tlbean.setPolicyDetId(rs.getInt("POLICY_DTL_ID"));
				if (rs.getString("ACV") != null) {
					tlbean.setAcv(rs.getString("ACV"));
				}
				if (rs.getString("dattr1") != null) {
					tlbean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr2") != null) {
					tlbean.setPDetAttr2(rs.getString("dattr2"));
				}
				tlbean.setPDetAttr3(Utility.doubleToString(rs.getDouble("dattr3")));
				if (rs.getString("company_name") != null) {
					tlbean.setInsuranceAgent(rs.getString("company_name"));
				}

				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					tlbean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					tlbean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					tlbean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					tlbean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					tlbean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					tlbean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================
				if (rs.getString("ti_endorsement_ll") != null) {
					tlbean.setEndorsementLL(rs.getString("ti_endorsement_ll"));
				}

				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");

				if ((GlobalVariables.TRAILERPOLICY + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlTI.add(tlbean);
					policybeans.put(sbKey, arlTI);
				} else {
					policybeans.put(sbKey, tlbean);
				}

				// policybeans.put(sbKey,tlbean);
			} else if (GlobalVariables.EMPLIABILITY.equals(rs.getString("POLICY_CODE"))) {
				ELBean elbean = new ELBean();

				elbean.setCertiId(rs.getInt("certi_id"));
				elbean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					elbean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					elbean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					elbean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					elbean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				elbean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				elbean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				elbean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				elbean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				elbean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					elbean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				elbean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				elbean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					elbean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				elbean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				elbean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));

				if (rs.getString("SELF_INSURED") != null) {
					elbean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					elbean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					elbean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					elbean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					elbean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					elbean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					elbean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					elbean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}
				elbean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					elbean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					elbean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					elbean.setAttr2(rs.getString("ATTR2"));
				}

				elbean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					elbean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					elbean.setMcScac(rs.getString("mc_scac"));
				}

				elbean.setPolicyDetId(rs.getInt("POLICY_DTL_ID"));
				elbean.setElEachOccur(Utility.createCommaString(Utility.intToString(rs.getInt("EL_EACH_OCCUR"))));
				elbean.setElDisEAEmp(Utility.createCommaString(Utility.intToString(rs.getInt("EL_DISEASE_EA_EMP"))));
				elbean.setElDisPlcyLmt(
						Utility.createCommaString(Utility.intToString(rs.getInt("EL_DISEASE_POLICY_LMT"))));
				if (rs.getString("dattr1") != null) {
					elbean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr2") != null) {
					elbean.setPDetAttr2(rs.getString("dattr2"));
				}
				elbean.setPDetAttr3(Utility.doubleToString(rs.getDouble("dattr3")));
				if (rs.getString("company_name") != null) {
					elbean.setInsuranceAgent(rs.getString("company_name"));
				}

				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					elbean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					elbean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					elbean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					elbean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					elbean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					elbean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================

				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.EMPLIABILITY + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlEL.add(elbean);
					policybeans.put(sbKey, arlEL);
				} else {
					policybeans.put(sbKey, elbean);
				}
				// policybeans.put(sbKey,elbean);
			} else if (GlobalVariables.WORKCOMP.equals(rs.getString("POLICY_CODE"))) {
				WCBean wcbean = new WCBean();

				wcbean.setCertiId(rs.getInt("certi_id"));
				wcbean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					wcbean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					wcbean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					wcbean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					wcbean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				wcbean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				wcbean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				wcbean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				wcbean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				wcbean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					wcbean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				wcbean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				wcbean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					wcbean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				wcbean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				wcbean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				if (rs.getString("SELF_INSURED") != null) {
					wcbean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					wcbean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					wcbean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					wcbean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					wcbean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					wcbean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					wcbean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					wcbean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				wcbean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					wcbean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					wcbean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					wcbean.setAttr2(rs.getString("ATTR2"));
				}

				wcbean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					wcbean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					wcbean.setMcScac(rs.getString("mc_scac"));
				}

				wcbean.setPolicyDetId(rs.getInt("POLICY_DTL_ID"));
				if (rs.getString("WC_STATUATORY_LMTS") != null) {
					wcbean.setWcStatLimits(rs.getString("WC_STATUATORY_LMTS"));
				}

				wcbean.setElEachOccur(Utility.createCommaString(Utility.intToString(rs.getInt("EL_EACH_OCCUR"))));
				wcbean.setElDisEAEmp(Utility.createCommaString(Utility.intToString(rs.getInt("EL_DISEASE_EA_EMP"))));
				wcbean.setElDisPlcyLmt(
						Utility.createCommaString(Utility.intToString(rs.getInt("EL_DISEASE_POLICY_LMT"))));

				if (rs.getString("ULMTD_EL_LMTS") != null) {
					wcbean.setUnlmtdElLimits(rs.getString("ULMTD_EL_LMTS"));
				}
				if (rs.getString("EXEMPT") != null) {
					wcbean.setExempt(rs.getString("EXEMPT"));
				}
				if (rs.getString("dattr1") != null) {
					wcbean.setPDetAttr1(rs.getString("dattr1"));
				}
				if (rs.getString("dattr2") != null) {
					wcbean.setPDetAttr2(rs.getString("dattr2"));
				}

				wcbean.setPDetAttr3(Utility.doubleToString(rs.getDouble("dattr3")));
				if (rs.getString("company_name") != null) {
					wcbean.setInsuranceAgent(rs.getString("company_name"));
				}

				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					wcbean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					wcbean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					wcbean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					wcbean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					wcbean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					wcbean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================
				// Added by Ankur March 2010 for new Accord

				if (rs.getString("mem_chk_flg") != null) {
					wcbean.setMemberCheck(rs.getString("mem_chk_flg"));
				}

				// Added by Ankur March 2010
				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");

				if ((GlobalVariables.WORKCOMP + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlWC.add(wcbean);
					policybeans.put(sbKey, arlWC);
				} else {
					policybeans.put(sbKey, wcbean);
				}
				// policybeans.put(sbKey,wcbean);
			} else if (GlobalVariables.UMBRELLA.equals(rs.getString("POLICY_CODE"))) {
				UmbBean ubean = new UmbBean();
				ubean = getUmbPolicyDetails(rs.getInt("POLICY_MST_ID"));

				ubean.setCertiId(rs.getInt("certi_id"));
				ubean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					ubean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					ubean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					ubean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					ubean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				ubean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				ubean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				ubean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				ubean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				ubean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					ubean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				ubean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				ubean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					ubean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				ubean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				ubean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				if (rs.getString("SELF_INSURED") != null) {
					ubean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					ubean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					ubean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					ubean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					ubean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					ubean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					ubean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					ubean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				ubean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					ubean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					ubean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					ubean.setAttr2(rs.getString("ATTR2"));
				}

				ubean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					ubean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					ubean.setMcScac(rs.getString("mc_scac"));
				}

				if (rs.getString("company_name") != null) {
					ubean.setInsuranceAgent(rs.getString("company_name"));
				}

				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					ubean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					ubean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					ubean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					ubean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					ubean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					ubean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================

				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.UMBRELLA + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlUMB.add(ubean);
					policybeans.put(sbKey, arlUMB);
				} else {
					policybeans.put(sbKey, ubean);
				}
				// policybeans.put(sbKey,ubean);

			} else if (GlobalVariables.CONTCARGO.equals(rs.getString("POLICY_CODE"))) {
				ContCargoBean ccBean = new ContCargoBean();

				ccBean.setCertiId(rs.getInt("certi_id"));
				ccBean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					ccBean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					ccBean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					ccBean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					ccBean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				ccBean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				ccBean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				ccBean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				ccBean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				ccBean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					ccBean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				ccBean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				ccBean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					ccBean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				ccBean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				ccBean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				if (rs.getString("SELF_INSURED") != null) {
					ccBean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					ccBean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					ccBean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					ccBean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					ccBean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					ccBean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					ccBean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					ccBean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				ccBean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					ccBean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					ccBean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					ccBean.setAttr2(rs.getString("ATTR2"));
				}

				ccBean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					ccBean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					ccBean.setMcScac(rs.getString("mc_scac"));
				}
				if (rs.getString("company_name") != null) {
					ccBean.setInsuranceAgent(rs.getString("company_name"));
				}

				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					ccBean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					ccBean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					ccBean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					ccBean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					ccBean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					ccBean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================

				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.CONTCARGO + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlCC.add(ccBean);
					policybeans.put(sbKey, arlCC);
				} else {
					policybeans.put(sbKey, ccBean);
				}
				// policybeans.put(sbKey,ccBean);
			} else if (GlobalVariables.REFTRAILER.equals(rs.getString("POLICY_CODE"))) {
				RefTrailerBean rtlBean = new RefTrailerBean();

				rtlBean.setCertiId(rs.getInt("certi_id"));
				rtlBean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					rtlBean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					rtlBean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					rtlBean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					rtlBean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				rtlBean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				rtlBean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				rtlBean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				rtlBean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				rtlBean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					rtlBean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				rtlBean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				rtlBean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					rtlBean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				rtlBean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				if ("ACV".equals(rs.getString("POLICY_LIMIT"))) {
					rtlBean.setLimit("ACV");
				} else {
					rtlBean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				}
				if (rs.getString("SELF_INSURED") != null) {
					rtlBean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					rtlBean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					rtlBean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					rtlBean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					rtlBean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					rtlBean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					rtlBean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					rtlBean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				rtlBean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					rtlBean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					rtlBean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					rtlBean.setAttr2(rs.getString("ATTR2"));
				}

				rtlBean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					rtlBean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					rtlBean.setMcScac(rs.getString("mc_scac"));
				}
				if (rs.getString("company_name") != null) {
					rtlBean.setInsuranceAgent(rs.getString("company_name"));
				}
				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					rtlBean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					rtlBean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					rtlBean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					rtlBean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					rtlBean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					rtlBean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================

				if (rs.getString("ACV") != null) {
					rtlBean.setAcv(rs.getString("ACV"));
				}
				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.REFTRAILER + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlRTI.add(rtlBean);
					policybeans.put(sbKey, arlRTI);
				} else {
					policybeans.put(sbKey, rtlBean);
				}
				// policybeans.put(sbKey,rtlBean);
			} else if (GlobalVariables.EMPDISHBOND.equals(rs.getString("POLICY_CODE"))) {
				EmpDishBean empdBean = new EmpDishBean();

				empdBean.setCertiId(rs.getInt("certi_id"));
				empdBean.setPolicyMstId(rs.getInt("POLICY_MST_ID"));
				if (rs.getString("POLICY_NO") != null) {
					empdBean.setPolicyNo(rs.getString("POLICY_NO"));
				}
				if (rs.getString("MC_ACCT_NO") != null) {
					empdBean.setMcAcctNo(rs.getString("MC_ACCT_NO"));
				}
				if (rs.getString("POLICY_CODE") != null) {
					empdBean.setPolicyCode(rs.getString("POLICY_CODE"));
				}
				if (rs.getString("POLICY_TYPE") != null) {
					empdBean.setPolicyType(rs.getString("POLICY_TYPE"));
				}

				empdBean.setPolicyEffDate(Utility.formatSqlDate(rs.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				empdBean.setPolicyExpiryDate(Utility.formatSqlDate(rs.getDate("POLICY_EXP_DT"), Utility.FORMAT4));
				empdBean.setPolicyOvrWrttnDate(Utility.formatSqlDate(rs.getDate("POLICY_OVRWRTN_DT"), Utility.FORMAT4));
				empdBean.setPolicyTerminatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_entr_dt"), Utility.FORMAT4));
				empdBean.setPolicyTerminatedDate(
						Utility.formatSqlDate(rs.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));

				if (rs.getString("POLICY_TRMINTN_RSN") != null) {
					empdBean.setPolicyTerminationReason(rs.getString("POLICY_TRMINTN_RSN"));
				}

				empdBean.setPolicyReinstatedEnteredDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_entr_dt"), Utility.FORMAT4));
				empdBean.setPolicyReinstatedDate(
						Utility.formatSqlDate(rs.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));

				if (rs.getString("policy_reinstd_rsn") != null) {
					empdBean.setPolicyReinstatedReason(rs.getString("policy_reinstd_rsn"));
				}

				empdBean.setDeductible(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_DEDUCTIBLE"))));
				empdBean.setLimit(Utility.createCommaString(Utility.intToString(rs.getInt("POLICY_LIMIT"))));
				if (rs.getString("SELF_INSURED") != null) {
					empdBean.setSelfInsured(rs.getString("SELF_INSURED"));
				}
				if (rs.getString("CURRENCY") != null) {
					empdBean.setCurrency(rs.getString("CURRENCY"));
				}
				if (rs.getString("INSURER_NAME") != null) {
					empdBean.setInsurerName(rs.getString("INSURER_NAME"));
				}
				if (rs.getString("NAIC_NO") != null) {
					empdBean.setNaicNo(rs.getString("NAIC_NO"));
				}
				if (rs.getString("RRG_FLAG") != null) {
					empdBean.setRrgFlg(rs.getString("RRG_FLAG"));
				}
				if (rs.getString("ADDNL_INSRD_FLAG") != null) {
					empdBean.setAddlnInsured(rs.getString("ADDNL_INSRD_FLAG"));
				}
				if (rs.getString("POLICY_STATUS") != null) {
					empdBean.setPolicyStatus(rs.getString("POLICY_STATUS"));
				}
				if (rs.getString("BLNKT_REQD") != null) {
					empdBean.setBlanketReqd(rs.getString("BLNKT_REQD"));
				}

				empdBean.setBlanketWordingId(rs.getInt("BLNKT_WORDING"));
				if (rs.getString("policy_inplace") != null) {
					empdBean.setInPlace(rs.getString("policy_inplace"));
				}
				if (rs.getString("ATTR1") != null) {
					empdBean.setAttr1(rs.getString("ATTR1"));
				}
				if (rs.getString("ATTR2") != null) {
					empdBean.setAttr2(rs.getString("ATTR2"));
				}

				empdBean.setAttr3(Utility.doubleToString(rs.getDouble("ATTR3")));
				if (rs.getString("mc_name") != null) {
					empdBean.setMcName(rs.getString("mc_name"));
				}
				if (rs.getString("mc_scac") != null) {
					empdBean.setMcScac(rs.getString("mc_scac"));
				}
				if (rs.getString("company_name") != null) {
					empdBean.setInsuranceAgent(rs.getString("company_name"));
				}
				// Added by piyush to display best rating on mc specific details
				if (rs.getString("best_rating") != null) {
					empdBean.setBestRating(rs.getString("best_rating"));
				}
				if (rs.getString("certidate") != null) {
					empdBean.setCertiDate(rs.getString("certidate"));
				}
				if (rs.getString("certi_no") != null) {
					empdBean.setCertiNo(rs.getString("certi_no"));
				}
				if (rs.getString("ia_acct_no") != null) {
					empdBean.setIaAcctNo(rs.getString("ia_acct_no"));
				}
				// =====Added by Piyush on 10Mar'09================
				if (rs.getString("tmp_term_date") != null) {
					empdBean.setTmpTermDt(Utility.formatSqlDate(rs.getDate("tmp_term_date"), Utility.FORMAT4));
				}
				if (rs.getString("tmp_reins_date") != null) {
					empdBean.setTmpReinsDt(Utility.formatSqlDate(rs.getDate("tmp_reins_date"), Utility.FORMAT4));
				}
				// =====End code by Piyush=========================

				/* the key in the hash table */
				sbKey = rs.getString("POLICY_CODE") + rs.getString("POLICY_TYPE");
				if ((GlobalVariables.EMPDISHBOND + GlobalVariables.EPSPECIFICPOLICY).equals(sbKey)) {
					arlEDH.add(empdBean);
					policybeans.put(sbKey, arlEDH);
				} else {
					policybeans.put(sbKey, empdBean);
				}
				// policybeans.put(sbKey,empdBean);
			}

		} // while getting from resultset

		// log.info("Exiting method populatePolicyBean of class PolicyMaster with return
		// value "+policybeans);
		return policybeans;
	}

	/*
	 * this method gets the specific details of Umbrella policy types in the
	 * policy_details table
	 * 
	 * @param Connection conn
	 * 
	 * @param int polMstId
	 * 
	 * @return UmbBean
	 * 
	 * @throws UiiaException
	 */
	public UmbBean getUmbPolicyDetails(int polMstId) {
		// log.info("Entering method getUmbPolicyDetails(" + conn.toString() + ","+
		// polMstId + ") of class PolicySpecificDetails");
		int iCounter = 0;

		StringBuffer sbQry = new StringBuffer(
				"SELECT umb_policy_mpg_id,auto_req,general_req,cargo_req,cont_cargo_req,");
		sbQry.append("wc_req,el_req,trailer_req,ref_trailer_req,emp_disbond_req,limit_agg,occur,claims_made,");
		sbQry.append("retention FROM umbrella_policy_mpg WHERE policy_mst_id = ?");

		UmbBean umbBean = getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<UmbBean>() {

					@Override
					public UmbBean extractData(ResultSet rs) throws SQLException, DataAccessException {
						UmbBean ubean = new UmbBean();
						while (rs.next()) {
							log.debug("Getting values from resultset , loop counter is " + iCounter);

							ubean.setUmbPolicyId(rs.getInt("UMB_POLICY_MPG_ID"));
							if (rs.getString("AUTO_REQ") != null) {
								ubean.setALReqd(rs.getString("AUTO_REQ"));
							}
							if (rs.getString("GENERAL_REQ") != null) {
								ubean.setGLReqd(rs.getString("GENERAL_REQ"));
							}
							if (rs.getString("CARGO_REQ") != null) {
								ubean.setCargoReqd(rs.getString("CARGO_REQ"));
							}
							if (rs.getString("CONT_CARGO_REQ") != null) {
								ubean.setContCargoReqd(rs.getString("CONT_CARGO_REQ"));
							}
							if (rs.getString("WC_REQ") != null) {
								ubean.setWCReqd(rs.getString("WC_REQ"));
							}
							if (rs.getString("EL_REQ") != null) {
								ubean.setELReqd(rs.getString("EL_REQ"));
							}
							if (rs.getString("TRAILER_REQ") != null) {
								ubean.setTrailerReqd(rs.getString("TRAILER_REQ"));
							}
							if (rs.getString("REF_TRAILER_REQ") != null) {
								ubean.setRefTrailerReqd(rs.getString("REF_TRAILER_REQ"));
							}
							if (rs.getString("EMP_DISBOND_REQ") != null) {
								ubean.setEmpDishReqd(rs.getString("EMP_DISBOND_REQ"));
							}

							ubean.setLimitAgg(Utility.createCommaString(Utility.intToString(rs.getInt("LIMIT_AGG"))));
							ubean.setOccur(Utility.intToString(rs.getInt("OCCUR")));
							ubean.setClaims(Utility.intToString(rs.getInt("CLAIMS_MADE")));
							ubean.setRetention(Utility.createCommaString(Utility.intToString(rs.getInt("RETENTION"))));

						}

						return ubean;
					}
				}, polMstId);
		return umbBean;
	}

	@Override
	public boolean getAreqFlag(String epAccNo) throws Exception {
		boolean flag = false;

		StringBuffer sbGetQuery = new StringBuffer(
				"select ep_areq_req from ep_addtln_reqmnt where ep_template_id in (select ep_template_id from ep_template where ep_acct_no = ? and active='Y')");
		sbGetQuery.append(" AND ep_areq_code in ('ADDM','LOC') AND ep_areq_req='Y' ");

		long count = findTotalRecordCount(this.uiiaDataSource, epAccNo, sbGetQuery.toString());
		if (count > 0) {
			flag = true;
		}
		return flag;
	}

	@Override
	public List<ScannedDoc> getScanDoc(String mcAcctNo) throws Exception {
		StringBuffer strSQL = new StringBuffer(
				"SELECT scan_id,scan_date,doctype FROM scanned_docs WHERE mc_acct_no = ? AND ");
		strSQL.append("doctype IN (?,?,?,?,?,?,?) ORDER BY scan_date DESC");

		List<Object> params = new ArrayList<>();
		params.add(mcAcctNo);
		params.add("Add\'l Insurance Endorsement");
		params.add("Add\'l Insured Listing EP/Chkls");
		params.add("Certificate of Insurance");
		params.add("MCS-90");
		params.add("Reinstatement Notice");
		params.add("Termination Notice");
		params.add("Truckers Endorsement");

		log.debug("Query fired is " + strSQL.toString());

		return getSpringJdbcTemplate(this.uiiaDataSource).query(strSQL.toString(),
				new ResultSetExtractor<List<ScannedDoc>>() {

					@Override
					public List<ScannedDoc> extractData(ResultSet rs) throws SQLException, DataAccessException {
						List<ScannedDoc> arr = new ArrayList<>();
						while (rs.next()) {
							ScannedDoc scandocs = new ScannedDoc();
							scandocs.setScanId(rs.getString("scan_id"));
							scandocs.setScanDate(rs.getString("scan_date"));
							scandocs.setDocType(rs.getString("doctype"));
							arr.add(scandocs);
						}

						return arr;
					}
				}, params.toArray());
	}

	/*
	 * this method gets all the EP template list based on the status
	 * (past,present,future)
	 * 
	 * @param Connection conn
	 * 
	 * @param AddendaDownloadBean epTemplate
	 * 
	 * @return ArrayList
	 * 
	 * @throws Exception
	 */
	@Override
	public List<AddendaDownload> getPreviousTemplatesList(AddendaDownload epTemplate, int pageIndex, int pageSize)
			throws Exception {
		List<AddendaDownload> templateLst = new ArrayList<AddendaDownload>();

		StringBuffer sbQry = new StringBuffer();

		sbQry.append("(SELECT DISTINCT t.ep_template_id,f.eff_date,f.addndm_date,f.file_path ");
		sbQry.append("FROM ep_template t,ep_addtln_reqmnt r,ep_addendum_files f ");
		sbQry.append("WHERE t.ep_acct_no LIKE ? AND t.active = 'Y'");
		sbQry.append("AND r.ep_template_id = t.ep_template_id ");
		sbQry.append("AND f.ep_areq_id = r.ep_areq_id ");
		sbQry.append("AND r.ep_areq_code = 'ADDM' AND f.addndm_date IS NOT NULL)");
		sbQry.append("UNION ");
		sbQry.append("(SELECT DISTINCT t.ep_template_id,f.eff_date,f.addndm_date,f.file_path ");
		sbQry.append("FROM ep_template t,arch_ep_addtln_reqmnt r,arch_ep_addendum_files f ");
		sbQry.append("WHERE t.ep_acct_no LIKE ? ");
		sbQry.append("AND t.active = 'N'AND r.ep_template_id = t.ep_template_id ");
		sbQry.append("AND f.ep_areq_id = r.ep_areq_id AND r.ep_areq_code = 'ADDM' AND f.addndm_date IS NOT NULL ) ");
		sbQry.append("ORDER BY addndm_date DESC LIMIT ?,? ");

		List<Object> params = new ArrayList<>();
		params.add(epTemplate.getEpAcctNo());
		params.add(epTemplate.getEpAcctNo());
		params.add((pageIndex * pageSize));
		params.add(pageSize);

		templateLst = getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<List<AddendaDownload>>() {

					@Override
					public List<AddendaDownload> extractData(ResultSet rs) throws SQLException, DataAccessException {
						List<AddendaDownload> addendaDownloadList = new ArrayList<>();
						while (rs.next()) {
							AddendaDownload epTemp = new AddendaDownload();
							epTemp.setAddendaId(rs.getInt("EP_TEMPLATE_ID"));
							if (rs.getDate("addndm_date") != null) {
								epTemp.setAddendaEffDate(
										Utility.formatSqlDate(rs.getDate("addndm_date"), Utility.FORMAT4));
							}
							if (rs.getString("file_path") != null) {
								epTemp.setAddendaPath(rs.getString("file_path"));
							}

							addendaDownloadList.add(epTemp);
						}
						return addendaDownloadList;
					}
				}, params.toArray());
		return templateLst;

	}

	/**
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param flag
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String> getEpMcUsdotStatusReportsList(int pageIndex, int pageSize, String flag) throws Exception {

		StringBuilder searchQry = new StringBuilder(
				"select DISTINCT DATE(CREATED_DATE) dt from MCSyncFMCSA_ACTION order by dt desc");
		List<Object> params = new ArrayList<>();

		if (!GlobalVariables.FLAG_REPORT.equalsIgnoreCase(flag)) {
			searchQry.append(" LIMIT ?, ?");
			params.add((pageIndex * pageSize));
			params.add(pageSize);
		}

		return getSpringJdbcTemplate(this.uiiaDataSource).query(searchQry.toString(),
				new ResultSetExtractor<List<String>>() {
					List<String> reportList = new ArrayList<>();

					@Override
					public List<String> extractData(ResultSet rs) throws SQLException {
						while (rs.next()) {
							if (rs.getString("dt") != null && rs.getString("dt") != "") {
								reportList.add(rs.getString("dt"));
							}

						}
						return reportList;
					}
				}, params.toArray());

	}

}