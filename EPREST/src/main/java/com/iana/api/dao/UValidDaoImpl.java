package com.iana.api.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.AdditionalReq;
import com.iana.api.domain.Currency;
import com.iana.api.domain.EPJoinDet;
import com.iana.api.domain.OverrideNeeds;
import com.iana.api.domain.acord.AutoBean;
import com.iana.api.domain.acord.CargoBean;
import com.iana.api.domain.acord.ContCargoBean;
import com.iana.api.domain.acord.ELBean;
import com.iana.api.domain.acord.EmpDishBean;
import com.iana.api.domain.acord.GenBean;
import com.iana.api.domain.acord.RefTrailerBean;
import com.iana.api.domain.acord.TrailerBean;
import com.iana.api.domain.acord.UVldMemBean;
import com.iana.api.domain.acord.UmbBean;
import com.iana.api.domain.acord.WCBean;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Repository
public class UValidDaoImpl extends GenericDAO implements UValidDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Override
	public List<Currency> getAllCurrencyConv() throws Exception {
		List<Currency> arlCurrency = new ArrayList<>(3);
		StringBuffer sbGetQuery = new StringBuffer(
				"SELECT currency_id,from_currency,to_currency,rate FROM currency_conv_master ");

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(),
				new ResultSetExtractor<List<Currency>>() {
					@Override
					public List<Currency> extractData(ResultSet rsCurrency) throws SQLException, DataAccessException {

						while (rsCurrency.next()) {
							Currency currBean = new Currency();
							currBean.setCurrId(rsCurrency.getInt("currency_id"));
							if (rsCurrency.getString("from_currency") != null)
								currBean.setFromCurrency(rsCurrency.getString("from_currency"));
							if (rsCurrency.getString("to_currency") != null)
								currBean.setToCurrency(rsCurrency.getString("to_currency"));
							currBean.setRate(rsCurrency.getDouble("rate"));
							arlCurrency.add(currBean);
						}

						return arlCurrency;
					}
				});
	}

	@Override
	public HashMap getMCInsDetailTableWhatIf(String strEPAccNo, String strMCAccNo, Date paramDate, String strMCSelEPFlg,
			String strMCNewName, String strWhatIf) throws Exception {
		HashMap hshMCAcctPolicyMap = null;
		String strMCEPChange = "";
		if (strEPAccNo.length() > 0) {
			/* Flag for EP Changing requirements */
			strMCEPChange = GlobalVariables.EP_CHANGE;
		} else {
			/* Flag for MC Changing requirements */
			strMCEPChange = GlobalVariables.MC_CHANGE;
		}

		/*----------------Query For current tables--------------------------------*/
		/* StringBuffer for Query from current tables */
		String strPolMStIds = "";
		StringBuffer sbGetQuery = new StringBuffer(" SELECT pm.policy_mst_id, pm.mc_acct_no AS mcacctno,");
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* EP Change */
			sbGetQuery.append(
					" IF(pm.addnl_insrd_flag is null or pm.addnl_insrd_flag <> 'Y','N',IF(EXISTS(SELECT 1 FROM policy_specific_eplist pel where pel.ep_acct_no = '"
							+ strEPAccNo
							+ "' and pel.policy_mst_id = (SELECT policy_mst_id FROM policy_master pmm where pmm.policy_mst_id=pm.policy_mst_id))=1,'Y','N')) AS ep_addtln_insrd, ");
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* MC Change */
			sbGetQuery.append(
					" IF(pm.addnl_insrd_flag is null or pm.addnl_insrd_flag <> 'Y','N','Y') AS ep_addtln_insrd, ");
		}
		sbGetQuery.append(" pm.policy_code,pm.policy_type,pm.policy_eff_dt, pm.policy_exp_dt,pm.policy_ovrwrtn_dt,");
		sbGetQuery.append(
				" pm.policy_trmintn_eff_dt,pm.policy_reinstd_entr_dt, pm.policy_reinstd_eff_dt,pm.policy_deductible,pm.policy_limit,");
		sbGetQuery.append(
				" pm.self_insured,pm.currency, pm.rrg_flag,pm.addnl_insrd_flag,pm.policy_status,pm.blnkt_reqd,");
		sbGetQuery.append(
				" pd.dmg_to_rntd_premises,pd.medi_expense,pd.prsnl_adv_inj, pd.gen_agg,pd.products,pd.bdly_inj_perprsn,");
		sbGetQuery.append(
				" pd.bdly_inj_peraccdnt,pd.prop_dmg_peraccdnt, pd.stnd_endo,pd.hauls_own_only,pd.acv,pd.wc_statuatory_lmts,");
		sbGetQuery.append(
				" pd.el_each_occur, pd.ulmtd_el_lmts,pd.exempt,pd.any,pd.scheduled,pd.hired,pd.all_owned,pd.non_owned,");
		sbGetQuery.append(
				" pd.no_of_claims,pd.no_of_occur,pd.dmg_to_rntd_premises,u.auto_req,u.general_req,u.cargo_req,");
		sbGetQuery
				.append(" pd.el_each_occur AS mcela,pd.el_disease_policy_lmt AS mcelp,pd.el_disease_ea_emp AS mcele,");
		sbGetQuery.append(
				" u.cont_cargo_req,u.wc_req,u.el_req,u.trailer_req,u.ref_trailer_req,u.emp_disbond_req, u.limit_agg as umblimitagg,");
		sbGetQuery.append(" u.occur as umboccur,u.retention as umbretention");

		/*--------From & Where Clause -------------*/
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* EP Change */
			// log.info("From Clause for EP change");
			sbGetQuery.append(
					" FROM policy_master pm LEFT OUTER JOIN policy_details pd ON (pm.policy_mst_id = pd.policy_mst_id)");
			sbGetQuery.append(
					" LEFT OUTER JOIN umbrella_policy_mpg u ON (pm.policy_mst_id=u.policy_mst_id AND pm.policy_code='UMB')");
			sbGetQuery.append(
					" LEFT OUTER JOIN policy_specific_eplist pad ON (pm.policy_mst_id=pad.policy_mst_id AND pad.ep_acct_no=?)");
			sbGetQuery.append(
					" WHERE (IF (pm.policy_trmintn_eff_dt IS NOT NULL, If(pm.policy_trmintn_eff_dt IS NOT NULL,");
			sbGetQuery.append(" pm.policy_trmintn_eff_dt>?, pm.policy_trmintn_eff_dt>?), '') OR ");
			sbGetQuery.append(
					" IF (pm.policy_reinstd_eff_dt IS  NOT NULL, (pm.policy_reinstd_eff_dt<=? AND pm.policy_exp_dt>?),");
			sbGetQuery.append(" '') OR  pm.policy_exp_dt>?) AND IF(pm.policy_ovrwrtn_dt IS NULL, pm.policy_exp_dt>?,");
			sbGetQuery.append(" pm.policy_ovrwrtn_dt>?) AND pm.policy_eff_dt <= ? ");
			sbGetQuery.append(" AND pm.policy_status NOT IN ('SUBMITTED','SAVED')");
			// sbGetQuery.append(" AND pm.policy_mst_id NOT IN("+strPolMStIds +") ");
			sbGetQuery.append(
					" AND pm.mc_acct_no in (SELECT acct.account_no FROM account_info acct,mc_ep_join_status s ");
			sbGetQuery.append(" WHERE s.ep_acct_no=? ");
			sbGetQuery.append(" AND s.mc_acct_no = acct.account_no ");
			/* to check if what if flag is passed then, to get only active members */
			if (GlobalVariables.YES.equals(strWhatIf)) {
				// log.debug("Adding active condition");
				sbGetQuery.append(
						" AND acct.uiia_status='ACTIVE' AND s.mc_ep_status='Y' AND pm.mc_name=acct.company_name ");
			}
			/* If mc acct no */
			if (strMCAccNo.length() > 0) {
				sbGetQuery.append(" AND acct.account_no =?"); // MC AccountNumber
			}
			/* If mc sel flag is passed */
			if (!strMCSelEPFlg.equals("")) {
				sbGetQuery.append(" AND s.mc_selected_ep=?");
			}

			sbGetQuery.append(" ) ORDER BY mcacctno,policy_type DESC,policy_code ");
		} else {
			/* MC Change */
			// log.info("From Clause for MC change");
			sbGetQuery.append(
					" FROM policy_master pm LEFT OUTER JOIN policy_details pd ON (pm.policy_mst_id = pd.policy_mst_id)");
			sbGetQuery.append(
					" LEFT OUTER JOIN umbrella_policy_mpg u ON (pm.policy_mst_id=u.policy_mst_id AND pm.policy_code='UMB')");
			sbGetQuery.append(
					" WHERE (IF (pm.policy_trmintn_eff_dt IS NOT NULL, If(pm.policy_trmintn_eff_dt IS NOT NULL,");
			sbGetQuery.append(" pm.policy_trmintn_eff_dt>?, pm.policy_trmintn_eff_dt>?), '') OR ");
			sbGetQuery.append(
					" IF (pm.policy_reinstd_eff_dt IS  NOT NULL, (pm.policy_reinstd_eff_dt<=? AND pm.policy_exp_dt>?),");
			sbGetQuery.append(" '') OR  pm.policy_exp_dt>?) AND IF(pm.policy_ovrwrtn_dt IS NULL, pm.policy_exp_dt>?,");
			sbGetQuery.append(" pm.policy_ovrwrtn_dt>?) AND pm.policy_eff_dt <= ? ");
			sbGetQuery.append(" AND pm.mc_acct_no = ?  AND pm.policy_status NOT IN ('SUBMITTED','SAVED')");
			// sbGetQuery.append(" AND pm.policy_mst_id NOT IN("+strPolMStIds +") ");
			sbGetQuery.append(" AND EXISTS (SELECT 1 FROM account_info acct ");
			sbGetQuery.append(" WHERE acct.account_no = ? ");
			if (GlobalVariables.YES.equals(strWhatIf)) {
				// log.debug("Adding active condition");
				sbGetQuery.append(" AND acct.uiia_status='ACTIVE'");
			}
			/* Chec If New Name is passed or not */
			if (strMCNewName != null && strMCNewName.length() > 0) {
				// log.debug("New MC Name passed so join with New MC Name");
				sbGetQuery.append(" AND pm.mc_name=?");
			} else {
				// log.debug("New MC Name not passed so join with account Info Company Name");
				sbGetQuery.append(" AND pm.mc_name=acct.company_name");
			}
			sbGetQuery.append(" AND pm.mc_acct_no = acct.account_no)");
			sbGetQuery.append(" ORDER BY mcacctno,policy_type DESC,policy_code ");
		}

		List<Object> params = new ArrayList<>();

		if (strEPAccNo != null && strMCAccNo != null) {
			if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
				// log.debug("Setting parameters for EP change");
				/* For EP Change */
				params.add(strEPAccNo);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(strEPAccNo);
				if (strMCAccNo.length() > 0) {
					params.add(strMCAccNo);
					if (!strMCSelEPFlg.equals("")) {
						params.add(strMCSelEPFlg);
					}
				} else {
					if (!strMCSelEPFlg.equals("")) {
						params.add(strMCSelEPFlg);
					}
				}
			} else {
				/* For MC Change */
				// log.debug("Setting parameters for MC change");
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(paramDate);
				params.add(strMCAccNo);
				params.add(strMCAccNo);
				if (strMCNewName != null && strMCNewName.length() > 0) {
					params.add(strMCNewName);
				}
			}
		}

		hshMCAcctPolicyMap = new HashMap(1000);
		hshMCAcctPolicyMap = getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(),
				new ResultSetExtractor<HashMap>() {
					HashMap hashMap = new HashMap(1000);

					@Override
					public HashMap extractData(ResultSet rsMCList) throws SQLException, DataAccessException {
						return populatePolicyBeans(hashMap, rsMCList, GlobalVariables.NO, "");
					}
				}, params.toArray());

		if (hshMCAcctPolicyMap.containsKey("polmstid")) {
			hshMCAcctPolicyMap.remove("polmstid");
		}

		return hshMCAcctPolicyMap;
	}

	/**
	 * This method will create a populate Policy beans and set the same in the
	 * HashMap with key(PolCode+PolType ..ie. ALPRIMARY). The final HashMap will
	 * have key as MC account Number and value as the HashMap of Policy beans.
	 * 
	 * strArchFlg is used to check if this method is called for Archival ResultSet
	 * or for currrent tables. and based on this flag, to be decided whether to
	 * separate policymst ids (as policy mst ids retrieved from archival tables are
	 * to be used in NOT IN clause for quering current tables)
	 * 
	 * @param HashMap   hshMCAcctPolicyMap
	 * @param ResultSet rsMCList
	 * @param String    flgArchCurr
	 * @return HashMap
	 * @throws SQLException
	 * @throws Exception
	 */

	private HashMap populatePolicyBeans(HashMap hshMCAcctPolicyMap, ResultSet rsMCList, String strArchFlg,
			String strPolMStIds) throws SQLException {
		StringBuffer sbPolMstId = new StringBuffer("");
		ArrayList arlAL = new ArrayList();
		ArrayList arlGL = new ArrayList();
		ArrayList arlTI = new ArrayList();
		ArrayList arlCL = new ArrayList();
		ArrayList arlCC = new ArrayList();
		ArrayList arlWC = new ArrayList();
		ArrayList arlEL = new ArrayList();
		ArrayList arlRTI = new ArrayList();
		ArrayList arlEDH = new ArrayList();
		ArrayList arlUMB = new ArrayList();
		HashMap hshMCPolicyMap = null;
		while (rsMCList != null && rsMCList.next()) {

			if (strArchFlg.equals(GlobalVariables.YES)) {
				//// log.debug("Entering Archival resultSet");
				if (!rsMCList.isLast()) {
					/* Checking if it is the last record in the resultset */
					//// log.debug("Creating comma separated string of Policy Mst Ids");
					sbPolMstId.append(rsMCList.getInt("policy_mst_id")).append(",");
				}
				if (rsMCList.isLast()) {
					sbPolMstId.append(rsMCList.getInt("policy_mst_id"));
				}
				//// log.debug("Exiting Archival resultSet");
			}

			/* Check if Key exists in the main hashtable */
			// log.debug(rsMCList.getString("mcacctno")+"-"+rsMCList.getString("policy_code")+"-"+rsMCList.getString("policy_type")+"-"+rsMCList.getInt("policy_mst_id"));
			if (!hshMCAcctPolicyMap.containsKey(rsMCList.getString("mcacctno"))) {
				// log.debug("Set HASHMAP for "+rsMCList.getString("mcacctno"));
				hshMCPolicyMap = new HashMap(10);
				arlAL = new ArrayList();
				arlGL = new ArrayList();
				arlTI = new ArrayList();
				arlCL = new ArrayList();
				arlCC = new ArrayList();
				arlWC = new ArrayList();
				arlEL = new ArrayList();
				arlRTI = new ArrayList();
				arlEDH = new ArrayList();
				arlUMB = new ArrayList();
				hshMCAcctPolicyMap.put(rsMCList.getString("mcacctno"), hshMCPolicyMap);
				//// log.debug("Exiting Not
				//// if(mcAcctPolicyTable.containsKey(rsMCList.getString(mcacctno)))");
			} else {
				hshMCPolicyMap = new HashMap(10);
				hshMCPolicyMap = (HashMap) hshMCAcctPolicyMap.get(rsMCList.getString("mcacctno"));
			}
			/* check for individual policies */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.AUTOPOLICY)) {
				//// log.debug("Entering Policy check for AutoPolicy");
				AutoBean policy = new AutoBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("mcacctno") != null)
					policy.setMcAcctNo(rsMCList.getString("mcacctno"));
				if (rsMCList.getString("all_owned") != null)
					policy.setAllOwned(rsMCList.getString("all_owned"));
				if (rsMCList.getString("any") != null)
					policy.setAny(rsMCList.getString("any"));
				if (rsMCList.getString("scheduled") != null)
					policy.setScheduled(rsMCList.getString("scheduled"));
				if (rsMCList.getString("hired") != null)
					policy.setHired(rsMCList.getString("hired"));
				if (rsMCList.getString("non_owned") != null)
					policy.setNonOwned(rsMCList.getString("non_owned"));
				if (rsMCList.getString("bdly_inj_peraccdnt") != null)
					policy.setBdlyInjrdPerAccdnt(rsMCList.getString("bdly_inj_peraccdnt"));
				if (rsMCList.getString("bdly_inj_perprsn") != null)
					policy.setBdlyInjrdPerPerson(rsMCList.getString("bdly_inj_perprsn"));
				if (rsMCList.getString("prop_dmg_peraccdnt") != null)
					policy.setPropDmgPerAccdnt(rsMCList.getString("prop_dmg_peraccdnt"));
				if (rsMCList.getString("stnd_endo") != null)
					policy.setStdEndo(rsMCList.getString("stnd_endo"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getString("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.AUTOPOLICY + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlAL.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlAL);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}

				//// log.debug("Exiting Policy check for AutoPolicy");
			}
			/* For GL */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.GENPOLICY)) {
				//// log.debug("Entering Policy check for Gen Liability");
				GenBean policy = new GenBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("dmg_to_rntd_premises") != null)
					policy.setDmgRntdPremises(rsMCList.getString("dmg_to_rntd_premises"));
				if (rsMCList.getString("prsnl_adv_inj") != null)
					policy.setPrsnlAdvInj(rsMCList.getString("prsnl_adv_inj"));
				if (rsMCList.getString("medi_expense") != null)
					policy.setMedExpenses(rsMCList.getString("medi_expense"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getString("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.GENPOLICY + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlGL.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlGL);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				//// log.debug("Exiting Policy check for Gen Liability");
			}
			/* For Cargo */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.CARGOPOLICY)) {
				//// log.debug("Entering Policy check for Cargo");
				CargoBean policy = new CargoBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getString("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.CARGOPOLICY + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlCL.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlCL);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				// log.debug("Exiting Policy check for Cargo");
			}
			/* For Cont Cargo */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.CONTCARGO)) {
				//// log.debug("Entering Policy check for Cont Cargo");
				ContCargoBean policy = new ContCargoBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.CONTCARGO + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlCC.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlCC);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}

				// hshMCPolicyMap.put(rsMCList.getString("policy_code")+rsMCList.getString("policy_type"),policy);
				//// log.debug("Exiting Policy check for Cont Cargo");
			}
			/* For Trailer */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.TRAILERPOLICY)) {
				//// log.debug("Entering Policy check for Trailer");
				TrailerBean policy = new TrailerBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));
				// ====Added by piyush=======
				if (rsMCList.getString("acv") != null)
					policy.setAcv(rsMCList.getString("acv"));
				// ====End added by piyush=======
				if ((GlobalVariables.TRAILERPOLICY + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlTI.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlTI);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				// log.debug("Exiting Policy check for Trailer");
			}
			/* For Ref Trailer */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.REFTRAILER)) {
				//// log.debug("Entering Policy check for Ref Trailer");
				RefTrailerBean policy = new RefTrailerBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.REFTRAILER + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlRTI.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlRTI);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				// hshMCPolicyMap.put(rsMCList.getString("policy_code")+rsMCList.getString("policy_type"),policy);
				//// log.debug("Exiting Policy check for Ref Trailer");
			}
			/* For WC */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.WORKCOMP)) {
				//// log.debug("Entering Policy check for Workers Compensation");
				WCBean policy = new WCBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));
				hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				/* Added for ELA,ELP,ELE.....piyush */
				if (rsMCList.getString("mcela") != null)
					policy.setElEachOccur(rsMCList.getString("mcela"));
				if (rsMCList.getString("mcelp") != null)
					policy.setElDisPlcyLmt(rsMCList.getString("mcelp"));
				if (rsMCList.getString("mcele") != null)
					policy.setElDisEAEmp(rsMCList.getString("mcele"));
				if (rsMCList.getString("exempt") != null)
					policy.setExempt(rsMCList.getString("exempt"));

				if ((GlobalVariables.WORKCOMP + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlWC.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlWC);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				// hshMCPolicyMap.put(rsMCList.getString("policy_code")+rsMCList.getString("policy_type"),policy);
				//// log.debug("Exiting Policy check for Workers Compensation");
			}
			/* For EL */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.EMPLIABILITY)) {
				//// log.debug("Entering Policy check for Emp Liability");
				ELBean policy = new ELBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));
				/* Added for ELA,ELP,ELE..... */
				if (rsMCList.getString("mcela") != null)
					policy.setElEachOccur(rsMCList.getString("mcela"));
				if (rsMCList.getString("mcelp") != null)
					policy.setElDisPlcyLmt(rsMCList.getString("mcelp"));
				if (rsMCList.getString("mcele") != null)
					policy.setElDisEAEmp(rsMCList.getString("mcele"));

				if ((GlobalVariables.EMPLIABILITY + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlEL.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlEL);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}

				// hshMCPolicyMap.put(rsMCList.getString("policy_code")+rsMCList.getString("policy_type"),policy);
				//// log.debug("Exiting Policy check for Emp Liability");
			}
			/* For EDH */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.EMPDISHBOND)) {
				//// log.debug("Entering Policy check for Employee dishonesty Bond");
				EmpDishBean policy = new EmpDishBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.EMPDISHBOND + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlEDH.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlEDH);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				// hshMCPolicyMap.put(rsMCList.getString("policy_code")+rsMCList.getString("policy_type"),policy);
				//// log.debug("Exiting Policy check for Employee dishonesty Bond");
			}
			/* For Umbrella */
			if (rsMCList.getString("policy_code").equals(GlobalVariables.UMBRELLA)) {
				//// log.debug("Entering Policy check for Umbrella");
				UmbBean policy = new UmbBean();
				policy.setPolicyMstId(rsMCList.getInt("policy_mst_id"));
				policy.setALReqd(rsMCList.getString("auto_req"));
				policy.setGLReqd(rsMCList.getString("general_req"));
				policy.setCargoReqd(rsMCList.getString("cargo_req"));
				policy.setContCargoReqd(rsMCList.getString("cont_cargo_req"));
				policy.setELReqd(rsMCList.getString("el_req"));
				policy.setEmpDishReqd(rsMCList.getString("emp_disbond_req"));
				policy.setRefTrailerReqd(rsMCList.getString("ref_trailer_req"));
				policy.setTrailerReqd(rsMCList.getString("trailer_req"));
				policy.setWCReqd(rsMCList.getString("wc_req"));
				policy.setLimitAgg(rsMCList.getString("umblimitagg"));
				policy.setOccur(rsMCList.getString("umboccur"));
				policy.setRetention(rsMCList.getString("umbretention"));
				if (rsMCList.getString("self_insured") != null)
					policy.setSelfInsured(rsMCList.getString("self_insured"));
				if (rsMCList.getString("blnkt_reqd") != null)
					policy.setBlanketReqd(rsMCList.getString("blnkt_reqd"));
				/* Addln Insrd */
				if (rsMCList.getString("ep_addtln_insrd") != null)
					policy.setAddlnInsured(rsMCList.getString("ep_addtln_insrd"));
				if (rsMCList.getString("rrg_flag") != null)
					policy.setRrgFlg(rsMCList.getString("rrg_flag"));
				if (rsMCList.getString("currency") != null)
					policy.setCurrency(rsMCList.getString("currency"));
				if (rsMCList.getString("policy_deductible") != null)
					policy.setDeductible(rsMCList.getString("policy_deductible"));
				if (rsMCList.getString("policy_limit") != null)
					policy.setLimit(rsMCList.getString("policy_limit"));
				if (rsMCList.getString("policy_code") != null)
					policy.setPolicyCode(rsMCList.getString("policy_code"));
				if (rsMCList.getString("policy_type") != null)
					policy.setPolicyType(rsMCList.getString("policy_type"));
				if (rsMCList.getDate("policy_eff_dt") != null)
					policy.setPolicyEffDate(Utility.formatSqlDate(rsMCList.getDate("policy_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_exp_dt") != null)
					policy.setPolicyExpiryDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_exp_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_ovrwrtn_dt") != null)
					policy.setPolicyOvrWrttnDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_ovrwrtn_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_trmintn_eff_dt") != null)
					policy.setPolicyTerminatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_trmintn_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getDate("policy_reinstd_eff_dt") != null)
					policy.setPolicyReinstatedDate(
							Utility.formatSqlDate(rsMCList.getDate("policy_reinstd_eff_dt"), Utility.FORMAT4));
				if (rsMCList.getString("policy_status") != null)
					policy.setPolicyStatus(rsMCList.getString("policy_status"));

				if ((GlobalVariables.UMBRELLA + GlobalVariables.EPSPECIFICPOLICY)
						.equals(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"))) {
					arlUMB.add(policy);
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), arlUMB);
				} else {
					hshMCPolicyMap.put(rsMCList.getString("policy_code") + rsMCList.getString("policy_type"), policy);
				}
				// hshMCPolicyMap.put(rsMCList.getString("policy_code")+rsMCList.getString("policy_type"),policy);
				//// log.debug("Exiting Policy check for Umbrella");
			}

			/*
			 * Creating Policy master bean which will be type casted in Individual policies
			 * for common properties
			 */
		} // end of while resultset
		strPolMStIds = sbPolMstId.toString();
		if (strPolMStIds.length() == 0) {
			strPolMStIds = "0";
		}
		hshMCAcctPolicyMap.put("polmstid", strPolMStIds);
		return hshMCAcctPolicyMap;
	}

	/**
	 * This method gets Policy override details given by EP to all MC's or for all
	 * EP's against a given MC and will be called in UValid. Key for HashMap will
	 * either be EP Account Number or MC Account Number based on whether called from
	 * EP Change or MC Change
	 * 
	 * @param Connection con
	 * @param String     epAccNo
	 * @param String     strMCAccNo
	 * @return HashMap
	 * @throws Exception
	 */
	@Override
	public HashMap getPolicyOvrDtlsMCEP(String strEPAccNo, String strMCAccNo, String strEPChange, Date paramDate)
			throws Exception {

		HashMap hshMCOvrDetailsMap = new HashMap();
		StringBuffer sbGetQuery = new StringBuffer("");
		StringBuffer sbArchQuery = new StringBuffer();
		String strMCEPChange = "";

		if (strEPAccNo.length() > 0 && strMCAccNo.length() == 0) {
			/* Flag for EP Changing requirements */
			strMCEPChange = GlobalVariables.EP_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() == 0) {
			/* Flag for MC Changing requirements */
			strMCEPChange = GlobalVariables.MC_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() > 0) {
			/* For Both MC & EP */
			strMCEPChange = GlobalVariables.MC_EP_CHANGE;
		}
		/* Query for Archival */
		/*
		 * p==========Start commenting Piyush 17Oct'07============
		 * 
		 * if(GlobalVariables.EP_CHANGE.equals(strMCEPChange)) { sbArchQuery.
		 * append("  SELECT  o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt, o.max_ded,"
		 * ); sbArchQuery.
		 * append("  o.addtln_insrd_reqd, o.slf_insrd_reqd, o.min_bst_rtng, o.rrg_allwd, o.spl_insrn_allwd,"
		 * ); sbArchQuery.
		 * append("  o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks"
		 * ); sbArchQuery.
		 * append("  FROM arch_mc_specific_overrides o WHERE arch_epmc_spc_ovr_id IN (SELECT MAX(o1.arch_epmc_spc_ovr_id)"
		 * ); sbArchQuery.append("  FROM arch_mc_specific_overrides o1");
		 * sbArchQuery.append("  WHERE ? BETWEEN o1.eff_start_dt AND o1.eff_end_dt");
		 * sbArchQuery.append("  AND o1.ep_acct_no = ?");
		 * sbArchQuery.append("  GROUP BY epmc_spc_ovr_id)");
		 * sbArchQuery.append("  ORDER BY o.mc_acct_no, o.policy_type");
		 * 
		 * 
		 * } else if(GlobalVariables.MC_CHANGE.equals(strMCEPChange)) { sbArchQuery.
		 * append("  SELECT  o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt, o.max_ded,"
		 * ); sbArchQuery.
		 * append("  o.addtln_insrd_reqd, o.slf_insrd_reqd, o.min_bst_rtng, o.rrg_allwd, o.spl_insrn_allwd,"
		 * ); sbArchQuery.
		 * append("  o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks"
		 * ); sbArchQuery.
		 * append("  FROM arch_mc_specific_overrides o WHERE arch_epmc_spc_ovr_id IN (SELECT MAX(o1.arch_epmc_spc_ovr_id)"
		 * ); sbArchQuery.append("  FROM arch_mc_specific_overrides o1");
		 * sbArchQuery.append("  WHERE ? BETWEEN o1.eff_start_dt AND o1.eff_end_dt");
		 * sbArchQuery.append("  AND o1.mc_acct_no = ?");
		 * sbArchQuery.append("  GROUP BY epmc_spc_ovr_id)");
		 * sbArchQuery.append("  ORDER BY o.ep_acct_no, o.policy_type"); //AND
		 * o1.ep_acct_no = 'EP000001' } else
		 * if(GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) { sbArchQuery.
		 * append("  SELECT  o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt, o.max_ded,"
		 * ); sbArchQuery.
		 * append("  o.addtln_insrd_reqd, o.slf_insrd_reqd, o.min_bst_rtng, o.rrg_allwd, o.spl_insrn_allwd,"
		 * ); sbArchQuery.
		 * append("  o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks"
		 * ); sbArchQuery.
		 * append("  FROM arch_mc_specific_overrides o WHERE arch_epmc_spc_ovr_id IN (SELECT MAX(o1.arch_epmc_spc_ovr_id)"
		 * ); sbArchQuery.append("  FROM arch_mc_specific_overrides o1");
		 * sbArchQuery.append("  WHERE ? BETWEEN o1.eff_start_dt AND o1.eff_end_dt");
		 * sbArchQuery.append("  AND o1.mc_acct_no = ? AND o1.ep_acct_no = ?");
		 * sbArchQuery.append("  GROUP BY epmc_spc_ovr_id)");
		 * sbArchQuery.append("  ORDER BY o.mc_acct_no, o.policy_type"); }p
		 */
		/*
		 * ppstmtArch=con.prepareStatement(sbArchQuery.toString()); if(strEPAccNo!=null
		 * && GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
		 * log.debug("Setting parameter (ARchival) for EP Changing Requirement");
		 * pstmtArch.setDate(1,paramDate); pstmtArch.setString(2,strEPAccNo); } else
		 * if(strMCAccNo!=null && GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
		 * log.debug("Setting parameter (ARchival) for MC Changing Requirement");
		 * pstmtArch.setDate(1,paramDate); pstmtArch.setString(2,strMCAccNo); } else
		 * if(GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
		 * log.debug("Setting parameter(Archival) for MC-EP");
		 * pstmtArch.setDate(1,paramDate); pstmtArch.setString(2,strMCAccNo);
		 * pstmtArch.setString(3,strEPAccNo); }
		 * log.info("Pstmt for Archival:- "+pstmtArch.toString());
		 * rsPolArchDetails=pstmtArch.executeQuery();
		 * log.info("PstmtArchiavl Executed:- "); //String
		 * strPolIds=populatePolOvrDtlsMap(rsPolArchDetails,strMCEPChange,
		 * hshMCOvrDetailsMap,GlobalVariables.YES);
		 * //log.info("Policy ID for Overrides :"+strPolIds);
		 * //if(strPolIds.length()==0) { // strPolIds="0"; } p
		 */
		/*-------For Current Tables-------------*/
		/* For Current Tables */
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* For EP Change (EP Changing Requirements) */
			sbGetQuery.append(" SELECT o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt,");
			sbGetQuery.append(
					" o.max_ded,o.addtln_insrd_reqd,o.slf_insrd_reqd,o.min_bst_rtng,o.rrg_allwd,o.spl_insrn_allwd,");
			sbGetQuery.append(" o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks");
			sbGetQuery.append(" FROM mc_specific_overrides o");
			// sbGetQuery.append(" WHERE o.epmc_spc_ovr_id NOT IN("+ strPolIds+")"); //
			// commented by piyush 28March07
			sbGetQuery.append(" WHERE o.ep_acct_no = ?");
			sbGetQuery.append(" ORDER BY o.mc_acct_no,o.policy_type");
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* For MC Change (MC Changing Requirements) */
			/*
			 * sbGetQuery.
			 * append(" SELECT o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt,"
			 * ); sbGetQuery.
			 * append(" o.max_ded,o.addtln_insrd_reqd,o.slf_insrd_reqd,o.min_bst_rtng,o.rrg_allwd,o.spl_insrn_allwd,"
			 * ); sbGetQuery.
			 * append(" o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks"
			 * ); sbGetQuery.append(" FROM mc_specific_overrides o");
			 * sbGetQuery.append(" WHERE o.epmc_spc_ovr_id NOT IN("+ strPolIds+")");
			 * sbGetQuery.append(" AND o.mc_acct_no=?");
			 * sbGetQuery.append(" ORDER BY o.ep_acct_no,o.policy_type");
			 */
			sbGetQuery.append(" SELECT o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt,");
			sbGetQuery.append(
					" o.max_ded,o.addtln_insrd_reqd,o.slf_insrd_reqd,o.min_bst_rtng,o.rrg_allwd,o.spl_insrn_allwd,");
			sbGetQuery.append(" o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks");
			sbGetQuery.append(" FROM mc_specific_overrides o");
			// sbGetQuery.append(" WHERE o.epmc_spc_ovr_id NOT IN("+ strPolIds+")");
			// //commented by piyush 28March07
			sbGetQuery.append(" WHERE o.mc_acct_no=?");
			sbGetQuery.append(" ORDER BY o.ep_acct_no,o.policy_type");

		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			/*
			 * sbGetQuery.
			 * append(" SELECT o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt,"
			 * ); sbGetQuery.
			 * append(" o.max_ded,o.addtln_insrd_reqd,o.slf_insrd_reqd,o.min_bst_rtng,o.rrg_allwd,o.spl_insrn_allwd,"
			 * ); sbGetQuery.
			 * append(" o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks"
			 * ); sbGetQuery.append(" FROM mc_specific_overrides o");
			 * sbGetQuery.append(" WHERE o.epmc_spc_ovr_id NOT IN("+ strPolIds+")");
			 * sbGetQuery.append(" AND o.ep_acct_no = ?");
			 * sbGetQuery.append(" AND o.mc_acct_no=? ");
			 * sbGetQuery.append(" ORDER BY o.mc_acct_no,o.policy_type");
			 */
			sbGetQuery.append(" SELECT o.mc_acct_no,o.epmc_spc_ovr_id, o.ep_acct_no, o.policy_type, o.min_lmt,");
			sbGetQuery.append(
					" o.max_ded,o.addtln_insrd_reqd,o.slf_insrd_reqd,o.min_bst_rtng,o.rrg_allwd,o.spl_insrn_allwd,");
			sbGetQuery.append(" o.attr1, o.attr2,o.attr3,o.ela,o.elp,o.ele,o.effective_date,o.multi_prsnt,o.remarks");
			sbGetQuery.append(" FROM mc_specific_overrides o");
			// sbGetQuery.append(" WHERE o.epmc_spc_ovr_id NOT IN("+ strPolIds+")");
			sbGetQuery.append(" WHERE o.ep_acct_no = ?");
			sbGetQuery.append(" AND o.mc_acct_no=? ");
			sbGetQuery.append(" ORDER BY o.mc_acct_no,o.policy_type");
		}

		List<Object> params = new ArrayList<>();
		/* setting EP account number or MC Account Number based on EP Change flag */
		if (strEPAccNo != null && GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			// log.debug("Setting parameter for EP Changing Requirement");
			params.add(strEPAccNo);
		} else if (strMCAccNo != null && GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			// log.debug("Setting parameter for MC Changing Requirement");
			params.add(strMCAccNo);
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			// log.debug("Setting parameter for MC-EP");
			params.add(strEPAccNo);
			params.add(strMCAccNo);
		}
		// log.debug("Query to be executed :- "+sbGetQuery.toString());
		final String mcEPChange = strMCEPChange;
		// log.debug("prepared Statment executed");
		/* Calling method to populate the HashMap */
		getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), new ResultSetExtractor<String>() {
			@Override
			public String extractData(ResultSet rsPolicyDetails) throws SQLException, DataAccessException {
				return populatePolOvrDtlsMap(rsPolicyDetails, mcEPChange, hshMCOvrDetailsMap, GlobalVariables.NO);
			}
		}, params.toArray());
		return hshMCOvrDetailsMap;
	}

	/**
	 * Private method which will iterate over ResultSet (Archival and for current
	 * tables) used in getPolOvrDtlsMCEP
	 * 
	 * @param rsPolicyDetails
	 * @param strMCEPChange
	 * @param hshMCOvrDetailsMap
	 * @param strArchFlg
	 * @return
	 * @throws SQLException
	 */
	private String populatePolOvrDtlsMap(ResultSet rsPolicyDetails, String strMCEPChange, HashMap hshMCOvrDetailsMap,
			String strArchFlg) throws SQLException {
		// log.info("Entering method populatePolOvrDtlsMap");
		// log.info("hshPolDtls:- "+hshMCOvrDetailsMap);
		// log.info("strArchFlg:- "+strArchFlg);
		// log.info("strMCEPChange:- "+strMCEPChange);
		StringBuffer sbPolOvrIds = new StringBuffer();
		HashMap hshMCOvrMap = null;
		while (rsPolicyDetails != null && rsPolicyDetails.next()) {
			// log.debug("Iterating over ResultSet rsPolicyDetails");
			/*
			 * If called for Archival then to create CommaSeparted string of MC_SpecOvr Ids,
			 * and return to the calling function
			 */

			if (GlobalVariables.YES.equals(strArchFlg)) {
				if (!rsPolicyDetails.isLast()) {
					// log.debug("Not the last Record");
					sbPolOvrIds.append(rsPolicyDetails.getInt("epmc_spc_ovr_id")).append(",");
				}
				if (rsPolicyDetails.isLast()) {
					// log.debug("Last Record");
					sbPolOvrIds.append(rsPolicyDetails.getInt("epmc_spc_ovr_id"));
				}
			}

			/*
			 * To set key in the hashtable as EP Account Number or MC Account Number based
			 * on the parameter strEPChange passed
			 */
			if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
				/* Called when MC Requierment change so setting EP Account Number as key */
				/* Check if Key exists in the main hashtable */
				if (!hshMCOvrDetailsMap.containsKey(rsPolicyDetails.getString("ep_acct_no"))) {
					// log.debug("Entering Not
					// if(mcOvrTable.containsKey(rsMCList.getString(mcacctno)))");
					hshMCOvrMap = new HashMap();
					hshMCOvrDetailsMap.put(rsPolicyDetails.getString("ep_acct_no"), hshMCOvrMap);
				} else {
					hshMCOvrMap = (HashMap) hshMCOvrDetailsMap.get(rsPolicyDetails.getString("ep_acct_no"));
				}
			} else {
				/* Called when EP Requierment change so setting MC Account Number as key */
				/* Check if Key exists in the main hashtable */
				if (!hshMCOvrDetailsMap.containsKey(rsPolicyDetails.getString("mc_acct_no"))) {
					// log.debug(" FIRST TIME : "+rsPolicyDetails.getString("mc_acct_no"));
					hshMCOvrMap = new HashMap();
					hshMCOvrDetailsMap.put(rsPolicyDetails.getString("mc_acct_no"), hshMCOvrMap);
				} else {
					// log.debug(" SECOND TIME : "+rsPolicyDetails.getString("mc_acct_no"));
					hshMCOvrMap = (HashMap) hshMCOvrDetailsMap.get(rsPolicyDetails.getString("mc_acct_no"));
					// log.debug(" SECOND MAP : "+hshMCOvrMap);
				}
			}
			// getting override details from resultset
			OverrideNeeds polOvrBean = new OverrideNeeds();

			polOvrBean.setOvrNeedId(rsPolicyDetails.getInt("epmc_spc_ovr_id"));
			if (rsPolicyDetails.getString("policy_type") != null)
				polOvrBean.setPolicyType(rsPolicyDetails.getString("policy_type"));
			// if(rsPolicyDetails.getInt("min_lmt")!=null)
			polOvrBean.setLimitBooster(Utility.intToString(rsPolicyDetails.getInt("min_lmt")));
			// if(rsPolicyDetails.getInt("max_ded")!=null)
			polOvrBean.setDedBooster(Utility.intToString(rsPolicyDetails.getInt("max_ded")));
			if (rsPolicyDetails.getString("addtln_insrd_reqd") != null)
				polOvrBean.setAddInsReq(rsPolicyDetails.getString("addtln_insrd_reqd"));
			if (rsPolicyDetails.getString("slf_insrd_reqd") != null)
				polOvrBean.setSelfInsReq(rsPolicyDetails.getString("slf_insrd_reqd"));
			if (rsPolicyDetails.getString("min_bst_rtng") != null)
				polOvrBean.setMinBestRat(rsPolicyDetails.getString("min_bst_rtng"));
			if (rsPolicyDetails.getString("rrg_allwd") != null)
				polOvrBean.setRrgAllwd(rsPolicyDetails.getString("rrg_allwd"));
			if (rsPolicyDetails.getString("spl_insrn_allwd") != null)
				polOvrBean.setSpcInsAllwd(rsPolicyDetails.getString("spl_insrn_allwd"));

			polOvrBean.setELABooster(Utility.intToString(rsPolicyDetails.getInt("ela")));
			polOvrBean.setELPBooster(Utility.intToString(rsPolicyDetails.getInt("elp")));
			polOvrBean.setELEBooster(Utility.intToString(rsPolicyDetails.getInt("ele")));

			polOvrBean.setAttr1(rsPolicyDetails.getInt("attr1"));
			if (rsPolicyDetails.getString("attr2") != null)
				polOvrBean.setAttr2(rsPolicyDetails.getString("attr2"));
			if (rsPolicyDetails.getString("attr3") != null)
				polOvrBean.setAttr3(rsPolicyDetails.getString("attr3"));

			if (rsPolicyDetails.getDate("effective_date") != null)
				polOvrBean
						.setEffDate(Utility.formatSqlDate(rsPolicyDetails.getDate("effective_date"), Utility.FORMAT4));
			if (rsPolicyDetails.getString("multi_prsnt") != null)
				polOvrBean.setMulPresent(rsPolicyDetails.getString("multi_prsnt"));
			if (rsPolicyDetails.getString("remarks") != null)
				polOvrBean.setRemarks(rsPolicyDetails.getString("remarks"));
			// putting override details bean into Hashtable
			hshMCOvrMap.put(rsPolicyDetails.getString("policy_type"), polOvrBean);

		}
		return sbPolOvrIds.toString();
	}

	/**
	 * This method will get all MC Details(Additional Requirement Details) against a
	 * given EP
	 * 
	 * @param strEPAccNo
	 * @param strMCAccNo
	 * @param paramDate
	 * @param strMCSelEPFlg
	 * @return HashMap
	 * @throws Exception
	 */
	@Override
	public HashMap getMCAddReqDetailsMap(String strEPAccNo, String strMCAccNo, Date paramDate, String strMCSelEPFlg)
			throws Exception {
		// log.info("Business: Entering method getMCAddReqDetailsMap()");
		// log.info("strEPAccNo "+strEPAccNo);
		// log.info("strMCAccNo "+strMCAccNo);
		// log.info("paramDate "+paramDate);
		// log.info("strMCSelEPFlg "+strMCSelEPFlg);
		HashMap hshMCAddReqMap = new HashMap(1000);
		String strMCEPChange = "";
		boolean bEPChange = false;
		if (strEPAccNo.length() > 0 && strMCAccNo.length() == 0) {
			/* Flag for EP Changing requirements */
			bEPChange = true;
			strMCEPChange = GlobalVariables.EP_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() == 0) {
			/* Flag for MC Changing requirements */
			strMCEPChange = GlobalVariables.MC_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() > 0) {
			/* For Both MC & EP */
			strMCEPChange = GlobalVariables.MC_EP_CHANGE;
		}

		/*
		 * To write the Query for Archival also... and then to use the mcareq_ids from
		 * archival as NOT IN the query for current table
		 */
		/* Archival Query */
		StringBuffer sbArchGetQuery = new StringBuffer("");
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* Archival Query For EP Change */
			// log.debug("Creating Archival Query for EP change");
			sbArchGetQuery
					.append(" SELECT m.arch_mc_areq_id,m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
			sbArchGetQuery.append(
					" IF(m.areq_rcvd_dt IS NULL,'N','Y' ) as mc_areq_req,IF(areq_ori_rcvd_dt IS NULL,'N','Y' ) as mc_orignl_req,");
			sbArchGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,m.eff_start_dt,m.eff_end_dt");
			sbArchGetQuery.append(" FROM arch_ep_mc_areq_details m");
			sbArchGetQuery.append(" WHERE m.arch_mc_areq_id IN (SELECT MAX(arch_mc_areq_id)");
			sbArchGetQuery.append(" FROM arch_ep_mc_areq_details m1");
			sbArchGetQuery.append(" WHERE ? BETWEEN m1.eff_start_dt AND m1.eff_end_dt");
			sbArchGetQuery.append(" AND m1.ep_acct_no = ? AND (EXISTS (SELECT 1  FROM mc_ep_join_status s");
			sbArchGetQuery.append(" WHERE s.ep_acct_no=?  AND s.mc_acct_no=m1.mc_acct_no");
			if (!strMCSelEPFlg.equals("")) {
				sbArchGetQuery.append(" AND s.mc_selected_ep=?");
			}
			sbArchGetQuery.append(" )) AND ((EXISTS (SELECT 1 FROM account_info acct");
			sbArchGetQuery.append(" WHERE  m1.mc_acct_no = acct.account_no))");
			sbArchGetQuery.append(" OR (EXISTS (SELECT 1 FROM arch_account_info archacct");
			sbArchGetQuery.append(" WHERE  ");
			sbArchGetQuery.append(" ? BETWEEN archacct.eff_start_dt AND archacct.eff_end_dt");
			sbArchGetQuery.append(" AND m1.mc_acct_no = archacct.account_no");
			sbArchGetQuery.append(" )))  GROUP BY mc_areq_id) ORDER BY mc_acct_no");

		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* Archival Query For MC Change */
			// log.debug("Creating Archival Query for MC change");
			sbArchGetQuery
					.append(" SELECT m.arch_mc_areq_id,m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
			sbArchGetQuery.append(
					" IF(m.areq_rcvd_dt IS NULL,'N','Y' ) as mc_areq_req,IF(areq_ori_rcvd_dt IS NULL,'N','Y' ) as mc_orignl_req,");
			sbArchGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,m.eff_start_dt,m.eff_end_dt");
			sbArchGetQuery.append(" FROM arch_ep_mc_areq_details m");
			sbArchGetQuery.append(" WHERE m.arch_mc_areq_id IN (SELECT MAX(arch_mc_areq_id)");
			sbArchGetQuery.append(" FROM arch_ep_mc_areq_details m1");
			sbArchGetQuery.append(" WHERE ? BETWEEN m1.eff_start_dt AND m1.eff_end_dt");
			sbArchGetQuery.append(" AND m1.mc_acct_no = ? AND ("); // MC account number
			sbArchGetQuery.append(" (EXISTS (SELECT 1 FROM account_info acct");
			sbArchGetQuery.append(" WHERE acct.account_no = ?"); // MC account number
			// sbArchGetQuery.append(" AND acct.uiia_status <> 'DELETED'");
			sbArchGetQuery.append(" AND m1.mc_acct_no = acct.account_no))");
			sbArchGetQuery.append(" OR (EXISTS (SELECT 1 FROM arch_account_info archacct");
			sbArchGetQuery.append(" WHERE ");
			sbArchGetQuery.append(" ? BETWEEN archacct.eff_start_dt AND archacct.eff_end_dt ");
			sbArchGetQuery.append(" AND m1.mc_acct_no = archacct.account_no AND archacct.account_no = ?)"); // MC
																											// account
																											// number
			sbArchGetQuery.append(" ))  GROUP BY mc_areq_id) ORDER BY ep_acct_no");
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			// log.debug("Creating Archival Query for MC-EP change");

			sbArchGetQuery
					.append(" SELECT m.arch_mc_areq_id,m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
			sbArchGetQuery.append(
					" IF(m.areq_rcvd_dt IS NULL,'N','Y' ) as mc_areq_req,IF(areq_ori_rcvd_dt IS NULL,'N','Y' ) as mc_orignl_req,");
			sbArchGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,m.eff_start_dt,m.eff_end_dt");
			sbArchGetQuery.append(" FROM arch_ep_mc_areq_details m");
			sbArchGetQuery.append(" WHERE m.arch_mc_areq_id IN (SELECT MAX(arch_mc_areq_id)");
			sbArchGetQuery.append(" FROM arch_ep_mc_areq_details m1");
			sbArchGetQuery.append(" WHERE ? BETWEEN m1.eff_start_dt AND m1.eff_end_dt");
			sbArchGetQuery.append(" AND m1.ep_acct_no = ? AND m1.mc_acct_no=?");
			sbArchGetQuery.append(" AND (EXISTS (SELECT 1  FROM mc_ep_join_status s");
			sbArchGetQuery.append(" WHERE s.ep_acct_no=?  AND s.mc_acct_no=?");
			if (!strMCSelEPFlg.equals("")) {
				sbArchGetQuery.append(" AND s.mc_selected_ep=?");
			}
			sbArchGetQuery.append(" ))AND((EXISTS (SELECT 1 FROM account_info acct WHERE");
			sbArchGetQuery.append(" acct.account_no=? ");
			sbArchGetQuery.append(" AND m1.mc_acct_no = acct.account_no))");
			sbArchGetQuery.append(" OR (EXISTS (SELECT 1 FROM arch_account_info archacct");
			sbArchGetQuery.append(" WHERE archacct.account_no=?");
			// sbArchGetQuery.append(" AND archacct.uiia_status <> 'DELETED'");
			sbArchGetQuery.append(" AND ? BETWEEN archacct.eff_start_dt AND archacct.eff_end_dt");
			sbArchGetQuery.append(" AND m1.mc_acct_no = archacct.account_no");
			sbArchGetQuery.append(" )))  GROUP BY mc_areq_id)	ORDER BY mc_acct_no ");
		}

		String strAreqIds = "";
		List<Object> params = new ArrayList<>();
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			params.add(paramDate);
			params.add(strEPAccNo);
			params.add(strEPAccNo);
			if (!strMCSelEPFlg.equals("")) {
				params.add(strMCSelEPFlg);
				params.add(paramDate);
			} else {
				params.add(paramDate);
			}
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			params.add(paramDate);
			params.add(strMCAccNo);
			params.add(strMCAccNo);
			params.add(paramDate);
			params.add(strMCAccNo);
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			params.add(paramDate);
			params.add(strEPAccNo);
			params.add(strMCAccNo);
			params.add(strEPAccNo);
			params.add(strMCAccNo);
			if (!strMCSelEPFlg.equals("")) {
				params.add(strMCSelEPFlg);
				params.add(strMCAccNo);
				params.add(strMCAccNo);
				params.add(paramDate);
			} else {
				params.add(strMCAccNo);
				params.add(strMCAccNo);
				params.add(paramDate);
			}
		}
		// log.info("pstmt for Archival :- "+pstmtArch.toString());
		// rsArchAddReqDtls=pstmtArch.executeQuery();
		// log.info("Archival Prepared Statement executed");

		StringBuffer sbAddReqIds = new StringBuffer("");
		final String mcepChange = strMCEPChange;
		List arlAddReqList = getSpringJdbcTemplate(this.uiiaDataSource).query(sbArchGetQuery.toString(),
				new ResultSetExtractor<List<Currency>>() {

					@Override
					public List extractData(ResultSet rsArchAddReqDtls) throws SQLException, DataAccessException {
						/* Looping over Archival ResultSet */
						List arlAddReq = null;

						while (rsArchAddReqDtls != null && rsArchAddReqDtls.next()) {
							//// log.debug("Iterating over Archival Additonal Requirement ResultSet");
							if (!rsArchAddReqDtls.isLast() && rsArchAddReqDtls.getInt("mc_areq_id") != 0) {
								/* Checking if it is not the last record in the resultset */
								//// log.debug("Creating comma separated string of Policy Mst Ids");
								sbAddReqIds.append(rsArchAddReqDtls.getInt("mc_areq_id")).append(",");
							}
							if (rsArchAddReqDtls.isLast()) {
								/* If it is not the last record */
								sbAddReqIds.append(rsArchAddReqDtls.getInt("mc_areq_id"));
							}
							/*
							 * IF MC Change, then to set ep acctno as the key, and for other Account Nos
							 * then to set mcAccout number as key
							 */
							if (GlobalVariables.MC_CHANGE.equals(mcepChange)) {
								if (!hshMCAddReqMap.containsKey(rsArchAddReqDtls.getString("ep_acct_no"))) {
									//// log.debug("Creating New ArrayList for MC Change as the Key doesn't
									//// exists");
									arlAddReq = new ArrayList(2);
									hshMCAddReqMap.put(rsArchAddReqDtls.getString("ep_acct_no"), arlAddReq);
								} else {
									arlAddReq = (ArrayList) hshMCAddReqMap
											.get(rsArchAddReqDtls.getString("ep_acct_no"));
								}
							} else {
								if (!hshMCAddReqMap.containsKey(rsArchAddReqDtls.getString("mc_acct_no"))) {
									//// log.debug("Creating New ArrayList for EP Change as the Key doesn't
									//// exists");
									arlAddReq = new ArrayList(2);
									hshMCAddReqMap.put(rsArchAddReqDtls.getString("mc_acct_no"), arlAddReq);
								} else {
									arlAddReq = (ArrayList) hshMCAddReqMap
											.get(rsArchAddReqDtls.getString("mc_acct_no"));
								}
							}
							AdditionalReq aReqBean = new AdditionalReq();
							aReqBean.setMcAReqId(rsArchAddReqDtls.getInt("mc_areq_id"));
							if (rsArchAddReqDtls.getString("ep_areq_code") != null)
								aReqBean.setEndrsCode(rsArchAddReqDtls.getString("ep_areq_code"));
							if (rsArchAddReqDtls.getString("mc_areq_req") != null)
								aReqBean.setRequired(rsArchAddReqDtls.getString("mc_areq_req"));
							if (rsArchAddReqDtls.getString("mc_orignl_req") != null)
								aReqBean.setOriginalReq(rsArchAddReqDtls.getString("mc_orignl_req"));
							if (rsArchAddReqDtls.getDate("areq_rcvd_dt") != null)
								aReqBean.setAreqRcvDate(Utility.formatSqlDate(rsArchAddReqDtls.getDate("areq_rcvd_dt"),
										Utility.FORMAT4));
							if (rsArchAddReqDtls.getDate("areq_ori_rcvd_dt") != null)
								aReqBean.setAreqOriRcvDate(Utility
										.formatSqlDate(rsArchAddReqDtls.getDate("areq_ori_rcvd_dt"), Utility.FORMAT4));
							arlAddReq.add(aReqBean);
						} // end of Loop Iteration of Archival ResultSet
						return arlAddReq;
					}
				});

		strAreqIds = sbAddReqIds.toString();
		if (strAreqIds.length() == 0) {
			strAreqIds = "0";
		}
		// log.info("comma Separated AddtlnReqIds:- "+strAreqIds);
		StringBuffer sbGetQuery = new StringBuffer("");
		/* For Current tables */
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* Query For EP Change */
			// log.debug("Creating Query for EP Change");
			long id = checkNameChangeAddnlReq(strMCAccNo);
			if (id == 0) {
				sbGetQuery.append(" SELECT m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
				sbGetQuery.append(
						" IF(m.areq_rcvd_dt IS NULL,'N',IF(m.areq_rcvd_dt > curdate(),'N','Y')) as mc_areq_req,");
				sbGetQuery.append(
						" IF(areq_ori_rcvd_dt IS NULL,'N',IF(areq_ori_rcvd_dt > curdate(),'N','Y')) as mc_orignl_req,");
				sbGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,rq.ep_orgnl_days");
				sbGetQuery.append(
						" FROM ep_mc_areq_details m,ep_template e,ep_addtln_reqmnt rq   WHERE m.ep_acct_no = ?");
				// sbGetQuery.append(" AND m.mc_areq_id NOT IN("+strAreqIds +")");
				sbGetQuery.append(" AND (EXISTS(SELECT 1 FROM mc_ep_join_status s");
				sbGetQuery.append(" WHERE s.ep_acct_no=?  AND s.mc_acct_no=m.mc_acct_no");
				if (!strMCSelEPFlg.equals("")) {
					sbGetQuery.append(" AND s.mc_selected_ep=?");
				}
				sbGetQuery.append(" ))AND  ((EXISTS (SELECT 1 FROM arch_account_info archacct");
				sbGetQuery.append(" WHERE ");
				sbGetQuery.append(" archacct.eff_start_dt<=? AND archacct.eff_end_dt>?");
				sbGetQuery.append(" AND m.mc_acct_no = archacct.account_no)");
				sbGetQuery.append(" ) OR (EXISTS (SELECT 1 FROM account_info acct");
				sbGetQuery.append(" WHERE m.mc_acct_no = acct.account_no)))");
				sbGetQuery.append(
						" AND e.ep_acct_no=m.ep_acct_no AND rq.ep_template_id=e.ep_template_id AND e.active='Y' AND m.ep_areq_code=rq.ep_areq_code AND for_name_chg IS NULL");
			} else {
				sbGetQuery.append(" SELECT m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
				sbGetQuery.append(
						" IF(m.areq_rcvd_dt IS NULL,'N',IF(m.areq_rcvd_dt > curdate(),'N','Y')) as mc_areq_req,");
				sbGetQuery.append(
						" IF(areq_ori_rcvd_dt IS NULL,'N',IF(areq_ori_rcvd_dt > curdate(),'N','Y')) as mc_orignl_req,");
				sbGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,rq.ep_orgnl_days");
				sbGetQuery.append(
						" FROM ep_mc_areq_details m,ep_template e,ep_addtln_reqmnt rq   WHERE m.ep_acct_no = ?");
				// sbGetQuery.append(" AND m.mc_areq_id NOT IN("+strAreqIds +")");
				sbGetQuery.append(" AND (EXISTS(SELECT 1 FROM mc_ep_join_status s");
				sbGetQuery.append(" WHERE s.ep_acct_no=?  AND s.mc_acct_no=m.mc_acct_no");
				if (!strMCSelEPFlg.equals("")) {
					sbGetQuery.append(" AND s.mc_selected_ep=?");
				}
				sbGetQuery.append(" ))AND  ((EXISTS (SELECT 1 FROM arch_account_info archacct");
				sbGetQuery.append(" WHERE ");
				sbGetQuery.append(" archacct.eff_start_dt<=? AND archacct.eff_end_dt>?");
				sbGetQuery.append(" AND m.mc_acct_no = archacct.account_no)");
				sbGetQuery.append(" ) OR (EXISTS (SELECT 1 FROM account_info acct");
				sbGetQuery.append(" WHERE m.mc_acct_no = acct.account_no)))");
				sbGetQuery.append(
						" AND e.ep_acct_no=m.ep_acct_no AND rq.ep_template_id=e.ep_template_id AND e.active='Y' AND m.ep_areq_code=rq.ep_areq_code AND for_name_chg ="
								+ id);
			}
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* Query For MC Change */
			// log.debug("Creating Query for MC Change");
			long id = checkNameChangeAddnlReq(strMCAccNo);
			if (id == 0) {
				sbGetQuery.append(" SELECT m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
				sbGetQuery.append(
						" IF(m.areq_rcvd_dt IS NULL,'N',IF(m.areq_rcvd_dt > curdate(),'N','Y')) as mc_areq_req,");
				sbGetQuery.append(
						" IF(areq_ori_rcvd_dt IS NULL,'N',IF(areq_ori_rcvd_dt > curdate(),'N','Y')) as mc_orignl_req,");
				sbGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,rq.ep_orgnl_days");
				sbGetQuery.append(" FROM ep_mc_areq_details m,ep_template e,ep_addtln_reqmnt rq");
				// sbGetQuery.append(" WHERE m.mc_acct_no = ? AND m.mc_areq_id NOT
				// IN("+strAreqIds +")");
				sbGetQuery.append(" WHERE m.mc_acct_no = ? ");
				sbGetQuery.append(" AND ((EXISTS (SELECT 1 FROM arch_account_info archacct");
				sbGetQuery.append(" WHERE archacct.account_no = ?");
				// sbGetQuery.append(" AND archacct.uiia_status <> 'DELETED'");
				sbGetQuery.append(" AND archacct.eff_start_dt<=? AND archacct.eff_end_dt>?");
				sbGetQuery.append(" AND m.mc_acct_no = archacct.account_no ))  OR");
				sbGetQuery.append(" (EXISTS (SELECT 1 FROM account_info acct");
				sbGetQuery.append(" WHERE acct.account_no = ?");
				sbGetQuery.append(" AND m.mc_acct_no = acct.account_no)))");
				sbGetQuery.append(
						" AND e.ep_acct_no=m.ep_acct_no AND rq.ep_template_id=e.ep_template_id AND e.active='Y' AND m.ep_areq_code=rq.ep_areq_code AND for_name_chg IS NULL");
			} else {
				sbGetQuery.append(" SELECT m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
				sbGetQuery.append(
						" IF(m.areq_rcvd_dt IS NULL,'N',IF(m.areq_rcvd_dt > curdate(),'N','Y')) as mc_areq_req,");
				sbGetQuery.append(
						" IF(areq_ori_rcvd_dt IS NULL,'N',IF(areq_ori_rcvd_dt > curdate(),'N','Y')) as mc_orignl_req,");
				sbGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,rq.ep_orgnl_days");
				sbGetQuery.append(" FROM ep_mc_areq_details m,ep_template e,ep_addtln_reqmnt rq");
				// sbGetQuery.append(" WHERE m.mc_acct_no = ? AND m.mc_areq_id NOT
				// IN("+strAreqIds +")");
				sbGetQuery.append(" WHERE m.mc_acct_no = ? ");
				sbGetQuery.append(" AND ((EXISTS (SELECT 1 FROM arch_account_info archacct");
				sbGetQuery.append(" WHERE archacct.account_no = ?");
				// sbGetQuery.append(" AND archacct.uiia_status <> 'DELETED'");
				sbGetQuery.append(" AND archacct.eff_start_dt<=? AND archacct.eff_end_dt>?");
				sbGetQuery.append(" AND m.mc_acct_no = archacct.account_no ))  OR");
				sbGetQuery.append(" (EXISTS (SELECT 1 FROM account_info acct");
				sbGetQuery.append(" WHERE acct.account_no = ?");
				sbGetQuery.append(" AND m.mc_acct_no = acct.account_no)))");
				sbGetQuery.append(
						" AND e.ep_acct_no=m.ep_acct_no AND rq.ep_template_id=e.ep_template_id AND e.active='Y' AND m.ep_areq_code=rq.ep_areq_code AND for_name_chg ="
								+ id);
			}
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			// log.debug("Creating Query for MC-EP Change");
			/*
			 * sbGetQuery.
			 * append(" SELECT s.mc_acct_no AS mcacctno,s.ep_acct_no as epacctno,a.account_no AS mcacctno,"
			 * ); sbGetQuery.
			 * append(" m.ep_areq_code, m.mc_areq_id,IF(m.areq_rcvd_dt IS NULL,'N','Y' ) as mc_areq_req,"
			 * ); sbGetQuery.
			 * append(" IF(areq_ori_rcvd_dt IS NULL,'N','Y' ) as mc_orignl_req, m.areq_rcvd_dt,areq_ori_rcvd_dt"
			 * ); sbGetQuery.
			 * append(" FROM account_info a, mc_ep_join_status s LEFT JOIN ep_mc_areq_details m"
			 * );
			 * sbGetQuery.append(" ON (m.mc_acct_no = s.mc_acct_no AND mc_areq_id NOT IN("
			 * +strAreqIds +") ) ");
			 * sbGetQuery.append(" WHERE s.mc_acct_no = a.account_no");
			 * sbGetQuery.append(" AND a.uiia_status <> 'DELETED'");
			 * sbGetQuery.append(" AND s.mc_acct_no=? AND s.ep_acct_no=?");
			 */
			long id = checkNameChangeAddnlReq(strMCAccNo);
			if (id == 0) {
				sbGetQuery.append(" SELECT m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
				sbGetQuery.append(
						" IF(m.areq_rcvd_dt IS NULL,'N',IF(m.areq_rcvd_dt > curdate(),'N','Y')) as mc_areq_req,");
				sbGetQuery.append(
						" IF(areq_ori_rcvd_dt IS NULL,'N',IF(areq_ori_rcvd_dt > curdate(),'N','Y')) as mc_orignl_req,");
				sbGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,rq.ep_orgnl_days");
				sbGetQuery.append(" FROM ep_mc_areq_details m,ep_template e,ep_addtln_reqmnt rq");
				sbGetQuery.append(" WHERE m.ep_acct_no = ? AND m.mc_acct_no=?");
				// sbGetQuery.append(" AND m.mc_areq_id NOT IN("+strAreqIds +") ");
				sbGetQuery.append(" AND (EXISTS(SELECT 1 FROM mc_ep_join_status s");
				sbGetQuery.append(" WHERE s.ep_acct_no=? AND s.mc_acct_no=? ");
				if (!strMCSelEPFlg.equals("")) {
					sbGetQuery.append(" AND s.mc_selected_ep=?");
				}
				sbGetQuery.append(" )) AND ((EXISTS (SELECT 1 FROM arch_account_info archacct");
				sbGetQuery.append(" WHERE archacct.uiia_status <> 'DELETED'");
				sbGetQuery.append(" AND archacct.account_no= ? AND archacct.eff_start_dt<=?");
				sbGetQuery.append(" AND archacct.eff_end_dt>?");
				sbGetQuery.append(" AND m.mc_acct_no = archacct.account_no)");
				sbGetQuery.append(" )OR (EXISTS (SELECT 1 FROM account_info acct");
				sbGetQuery.append(" WHERE acct.account_no= ?");
				sbGetQuery.append(" AND m.mc_acct_no = acct.account_no)))");
				sbGetQuery.append(
						" AND e.ep_acct_no=m.ep_acct_no AND rq.ep_template_id=e.ep_template_id AND e.active='Y' AND m.ep_areq_code=rq.ep_areq_code AND for_name_chg IS NULL");
			} else {
				sbGetQuery.append(" SELECT m.mc_acct_no, m.ep_acct_no, m.ep_areq_code, m.mc_areq_id,");
				sbGetQuery.append(
						" IF(m.areq_rcvd_dt IS NULL,'N',IF(m.areq_rcvd_dt > curdate(),'N','Y')) as mc_areq_req,");
				sbGetQuery.append(
						" IF(areq_ori_rcvd_dt IS NULL,'N',IF(areq_ori_rcvd_dt > curdate(),'N','Y')) as mc_orignl_req,");
				sbGetQuery.append(" m.areq_rcvd_dt, m.areq_ori_rcvd_dt,rq.ep_orgnl_days");
				sbGetQuery.append(" FROM ep_mc_areq_details m,ep_template e,ep_addtln_reqmnt rq");
				sbGetQuery.append(" WHERE m.ep_acct_no = ? AND m.mc_acct_no=?");
				// sbGetQuery.append(" AND m.mc_areq_id NOT IN("+strAreqIds +") ");
				sbGetQuery.append(" AND (EXISTS(SELECT 1 FROM mc_ep_join_status s");
				sbGetQuery.append(" WHERE s.ep_acct_no=? AND s.mc_acct_no=? ");
				if (!strMCSelEPFlg.equals("")) {
					sbGetQuery.append(" AND s.mc_selected_ep=?");
				}
				sbGetQuery.append(" )) AND ((EXISTS (SELECT 1 FROM arch_account_info archacct");
				sbGetQuery.append(" WHERE ");
				sbGetQuery.append(" archacct.account_no= ? AND archacct.eff_start_dt<=?");
				sbGetQuery.append(" AND archacct.eff_end_dt>?");
				sbGetQuery.append(" AND m.mc_acct_no = archacct.account_no)");
				sbGetQuery.append(" )OR (EXISTS (SELECT 1 FROM account_info acct");
				sbGetQuery.append(" WHERE acct.account_no= ?");
				sbGetQuery.append(" AND m.mc_acct_no = acct.account_no)))");
				sbGetQuery.append(
						" AND e.ep_acct_no=m.ep_acct_no AND rq.ep_template_id=e.ep_template_id AND e.active='Y' AND m.ep_areq_code=rq.ep_areq_code AND for_name_chg ="
								+ id);
			}
		}

		List<Object> sbGetQueryParams = new ArrayList<>();
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			// log.debug("Setting parameters for EP change in pstmt:- "+strEPAccNo);
			sbGetQueryParams.add(strEPAccNo);
			sbGetQueryParams.add(strEPAccNo);
			if (!strMCSelEPFlg.equals("")) {
				sbGetQueryParams.add(strMCSelEPFlg);
				sbGetQueryParams.add(paramDate);
				sbGetQueryParams.add(paramDate);
			} else {
				sbGetQueryParams.add(paramDate);
				sbGetQueryParams.add(paramDate);
			}
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			// log.debug("Setting parameters for MC change in pstmt:- "+strMCAccNo);
			sbGetQueryParams.add(strMCAccNo);
			sbGetQueryParams.add(strMCAccNo);
			sbGetQueryParams.add(paramDate);
			sbGetQueryParams.add(paramDate);
			sbGetQueryParams.add(strMCAccNo);
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			// log.debug("For MC EP Change");
			sbGetQueryParams.add(strEPAccNo);
			sbGetQueryParams.add(strMCAccNo);
			sbGetQueryParams.add(strEPAccNo);
			sbGetQueryParams.add(strMCAccNo);
			if (!strMCSelEPFlg.equals("")) {
				sbGetQueryParams.add(strMCSelEPFlg);
				sbGetQueryParams.add(strMCAccNo);
				sbGetQueryParams.add(paramDate);
				sbGetQueryParams.add(paramDate);
				sbGetQueryParams.add(strMCAccNo);
			} else {
				sbGetQueryParams.add(strMCAccNo);
				sbGetQueryParams.add(paramDate);
				sbGetQueryParams.add(paramDate);
				sbGetQueryParams.add(strMCAccNo);
			}
		}
		List arlAddReqCurrList = getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(),
				new ResultSetExtractor<List>() {
					List arlAddReq = arlAddReqList;

					@Override
					public List extractData(ResultSet rsAddReqDetails) throws SQLException, DataAccessException {
						/* Looping over Current ResultSet */
						while (rsAddReqDetails != null && rsAddReqDetails.next()) {
							//// log.debug("Iterating over Additonal Requirement ResultSet");

							if (GlobalVariables.MC_CHANGE.equals(mcepChange)) {
								/* For MC changing requirements */
								if (!hshMCAddReqMap.containsKey(rsAddReqDetails.getString("ep_acct_no"))) {
									//// log.debug("Creating New ArrayList for MC Change as the Key doesn't
									//// exists");
									arlAddReq = new ArrayList(2);
									hshMCAddReqMap.put(rsAddReqDetails.getString("ep_acct_no"), arlAddReq);
								} else {
									arlAddReq = (ArrayList) hshMCAddReqMap.get(rsAddReqDetails.getString("ep_acct_no"));
								}
							} else {
								/* For EP changing requirements */
								if (!hshMCAddReqMap.containsKey(rsAddReqDetails.getString("mc_acct_no"))) {
									//// log.debug("Creating New ArrayList for EP Change as the Key doesn't
									//// exists");
									arlAddReq = new ArrayList(2);
									hshMCAddReqMap.put(rsAddReqDetails.getString("mc_acct_no"), arlAddReq);
								} else {
									arlAddReq = (ArrayList) hshMCAddReqMap.get(rsAddReqDetails.getString("mc_acct_no"));
								}
							}
							AdditionalReq aReqBean = new AdditionalReq();
							aReqBean.setMcAReqId(rsAddReqDetails.getInt("mc_areq_id"));
							if (rsAddReqDetails.getString("ep_areq_code") != null)
								aReqBean.setEndrsCode(rsAddReqDetails.getString("ep_areq_code"));
							if (rsAddReqDetails.getString("mc_areq_req") != null)
								aReqBean.setRequired(rsAddReqDetails.getString("mc_areq_req"));
							if (rsAddReqDetails.getString("mc_orignl_req") != null)
								aReqBean.setOriginalReq(rsAddReqDetails.getString("mc_orignl_req"));
							if (rsAddReqDetails.getDate("areq_rcvd_dt") != null)
								aReqBean.setAreqRcvDate(Utility.formatSqlDate(rsAddReqDetails.getDate("areq_rcvd_dt"),
										Utility.FORMAT4));
							if (rsAddReqDetails.getDate("areq_ori_rcvd_dt") != null)
								aReqBean.setAreqOriRcvDate(Utility
										.formatSqlDate(rsAddReqDetails.getDate("areq_ori_rcvd_dt"), Utility.FORMAT4));
							if (rsAddReqDetails.getString("ep_orgnl_days") != null)
								aReqBean.setReqInDays(rsAddReqDetails.getString("ep_orgnl_days"));
							arlAddReq.add(aReqBean);
						}
						return arlAddReq;
					}

				}, sbGetQueryParams.toArray());

		return hshMCAddReqMap;
	}

	// Added by piyush on 8Oct'2008 in regards to Name change issue.
	public long checkNameChangeAddnlReq(String strMCAccNo) throws Exception {
		StringBuffer sbGetQuery = new StringBuffer(
				"SELECT MAX(mcscac_chg_id) FROM mc_scac_name_history WHERE mc_acct_no = '" + strMCAccNo
						+ "' AND (mc_chg_applied_for ='N' OR mc_chg_applied_for ='NS') AND mc_verified_by IS NOT NULL AND mc_verified_dt IS NOT NULL ");
		return findTotalRecordCount(uiiaDataSource, sbGetQuery.toString());
	}

	/**
	 * This method gets Additional Requirment override details given by EP to all
	 * MC's or for all EP's against a given MC and will be called in UValid. Key for
	 * HashMap will either be EP Account Number or MC Account Number based on
	 * whether called from EP Change or MC Change
	 * 
	 * @param Connection con
	 * @param String     epAccNo
	 * @param String     strMCAccNo
	 * @return HashMap
	 * @throws Exception
	 */
	@Override
	public HashMap getAddReqOvrDtlsMCEP(String strEPAccNo, String strMCAccNo, String strEPChange, Date paramDate)
			throws Exception {
		// log.info("Business: Entering method getAddReqOvrDtlsMCEP()");
		// log.info("EP Account Number: "+strEPAccNo);
		// log.info("MC Account Number: "+strMCAccNo);
		// log.info("strEPChange: "+strEPChange);
		// log.info("paramDate: "+paramDate);
		HashMap hshMCOvrDetailsMap = new HashMap(100);
		StringBuffer sbGetQuery = new StringBuffer("");
		StringBuffer sbArchQuery = new StringBuffer("");
		String strMCEPChange = "";

		if (strEPAccNo.length() > 0 && strMCAccNo.length() == 0) {
			/* Flag for EP Changing requirements */
			strMCEPChange = GlobalVariables.EP_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() == 0) {
			/* Flag for MC Changing requirements */
			strMCEPChange = GlobalVariables.MC_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() > 0) {
			/* For Both MC & EP */
			strMCEPChange = GlobalVariables.MC_EP_CHANGE;
		}

		/* Query for Archival */
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* Archival Query for EP change */

			sbArchQuery.append(" SELECT o.ep_areq_code, o.ep_acct_no, o.mc_areq_id,");
			sbArchQuery.append(" o.mc_areq_req,o.mc_orignl_req, o.mc_orignl_days,  o.areq_rcvd_dt,");
			sbArchQuery.append(" o.areq_ori_rcvd_dt FROM arch_mc_areq_overrides o");
			sbArchQuery.append(" WHERE o.mc_acct_no= ? ");
			sbArchQuery.append(" AND o.arch_mc_areq_id IN (SELECT MAX(o1.arch_mc_areq_id)");
			sbArchQuery.append(" FROM arch_mc_areq_overrides o1");
			sbArchQuery.append(" WHERE ? BETWEEN o1.eff_start_dt AND o1.eff_end_dt");
			sbArchQuery.append(" AND o1.ep_acct_no = ?");
			sbArchQuery.append(" GROUP BY mc_areq_id) ORDER BY o.mc_acct_no,o.ep_areq_code");
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* Archival Query for MC change */
			sbArchQuery.append(" SELECT o.ep_areq_code, o.ep_acct_no, o.mc_areq_id,");
			sbArchQuery.append(" o.mc_areq_req,o.mc_orignl_req, o.mc_orignl_days,  o.areq_rcvd_dt,");
			sbArchQuery.append(" o.areq_ori_rcvd_dt FROM arch_mc_areq_overrides o");
			sbArchQuery.append(" WHERE o.mc_acct_no= ? ");
			sbArchQuery.append(" AND o.arch_mc_areq_id IN (SELECT MAX(o1.arch_mc_areq_id)");
			sbArchQuery.append(" FROM arch_mc_areq_overrides o1");
			sbArchQuery.append(" WHERE ? BETWEEN o1.eff_start_dt AND o1.eff_end_dt");
			sbArchQuery.append(" AND o1.mc_acct_no = ? ");
			sbArchQuery.append(" GROUP BY mc_areq_id) ORDER BY o.ep_acct_no,o.ep_areq_code");

		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			/* Archival Query for MC-EP */
			sbArchQuery.append(" SELECT o.ep_areq_code, o.ep_acct_no, o.mc_areq_id,");
			sbArchQuery.append(" o.mc_areq_req,o.mc_orignl_req, o.mc_orignl_days,  o.areq_rcvd_dt,");
			sbArchQuery.append(" o.areq_ori_rcvd_dt FROM arch_mc_areq_overrides o");
			sbArchQuery.append(" WHERE o.mc_acct_no= ? ");
			sbArchQuery.append(" AND o.arch_mc_areq_id IN (SELECT MAX(o1.arch_mc_areq_id)");
			sbArchQuery.append(" FROM arch_mc_areq_overrides o1");
			sbArchQuery.append(" WHERE ? BETWEEN o1.eff_start_dt AND o1.eff_end_dt");
			sbArchQuery.append(" AND o1.mc_acct_no = ? ");
			sbArchQuery.append(" AND o1.ep_acct_no = ?");
			sbArchQuery.append(" GROUP BY mc_areq_id) ORDER BY o.mc_acct_no,o.ep_areq_code");
		}

		/*
		 * pstmtArch=con.prepareStatement(sbArchQuery.toString());
		 * if(GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
		 * pstmtArch.setString(1,strMCAccNo); pstmtArch.setDate(2,paramDate);
		 * pstmtArch.setString(3,strEPAccNo); } else
		 * if(GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
		 * pstmtArch.setDate(1,paramDate); pstmtArch.setString(2,strMCAccNo); } else
		 * if(GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
		 * pstmtArch.setDate(1,paramDate); pstmtArch.setString(2,strMCAccNo);
		 * pstmtArch.setString(3,strEPAccNo); }
		 * log.info("Pstmt ARchival:- "+pstmtArch.toString() );
		 * rsArchAddReqDetails=pstmtArch.executeQuery();
		 * log.info("psttmt Archival exceuted"); /*String
		 * strAReqIds=populateAReqOvrDtlsMap(rsArchAddReqDetails,strMCEPChange,
		 * hshMCOvrDetailsMap,GlobalVariables.YES); if(strAReqIds.length()==0) {
		 * strAReqIds="0"; }
		 */
		/*---------for current tables-----------------------*/
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* For EP Change (EP Changing Requirements) */
			// log.debug("Inside EP_CHANGE");
			sbGetQuery.append(" SELECT o.mc_acct_no,o.ep_areq_code, o.ep_acct_no,o.mc_areq_id,");
			sbGetQuery.append(" o.mc_areq_req,o.mc_orignl_req,o.mc_orignl_days,  o.areq_rcvd_dt,o.areq_ori_rcvd_dt");
			sbGetQuery.append(" FROM mc_areq_overrides o");
			sbGetQuery.append(" WHERE o.ep_acct_no=? ");
			// sbGetQuery.append(" AND o.mc_areq_id NOT IN("+strAReqIds +")");
			sbGetQuery.append(" ORDER BY o.mc_acct_no,o.ep_areq_code");
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* For MC Change (MC Changing Requirements) */
			// log.debug("Inside MC_CHANGE");
			sbGetQuery.append(" SELECT o.ep_areq_code, o.ep_acct_no,o.mc_areq_id,");
			sbGetQuery.append(" o.mc_areq_req,o.mc_orignl_req,o.mc_orignl_days,  o.areq_rcvd_dt,o.areq_ori_rcvd_dt");
			sbGetQuery.append(" FROM mc_areq_overrides o");
			sbGetQuery.append(" WHERE o.mc_acct_no=?");
			// sbGetQuery.append(" AND o.mc_areq_id NOT IN("+strAReqIds +")");
			sbGetQuery.append(" ORDER BY o.ep_acct_no,o.ep_areq_code");

		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			/* For MC-EP Change */
			// log.debug("Inside MC_EP_CHANGE");
			sbGetQuery.append(" SELECT o.mc_acct_no,o.ep_areq_code, o.ep_acct_no,o.mc_areq_id,");
			sbGetQuery.append(" o.mc_areq_req,o.mc_orignl_req,o.mc_orignl_days,  o.areq_rcvd_dt,o.areq_ori_rcvd_dt");
			sbGetQuery.append(" FROM mc_areq_overrides o");
			sbGetQuery.append(" WHERE o.mc_acct_no=? AND o.ep_acct_no=?");
			// sbGetQuery.append(" AND o.mc_areq_id NOT IN("+strAReqIds +")");
			sbGetQuery.append(" ORDER BY o.mc_acct_no,o.ep_areq_code");
		}

		List<Object> params = new ArrayList<>();
		/* setting EP account number or MC Account Number based on EP Change flag */
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			params.add(strEPAccNo);
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			params.add(strMCAccNo);
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			params.add(strMCAccNo);
			params.add(strEPAccNo);
		}
		final String mcepChange = strMCEPChange;
		// log.debug("prepared Statment executed");
		/* Calling method to populate the HashMap */
		getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), new ResultSetExtractor<String>() {
			@Override
			public String extractData(ResultSet rsAddReqDetails) throws SQLException, DataAccessException {
				return populateAReqOvrDtlsMap(rsAddReqDetails, mcepChange, hshMCOvrDetailsMap, GlobalVariables.NO);
			}
		}, params.toArray());
		return hshMCOvrDetailsMap;
	}

	/**
	 * Private method which will iterate over ResultSet (Archival and for current
	 * tables) used in getPolOvrDtlsMCEP
	 * 
	 * @param rsPolicyDetails
	 * @param strMCEPChange
	 * @param hshMCOvrDetailsMap
	 * @param strArchFlg
	 * @return
	 * @throws SQLException
	 */
	private String populateAReqOvrDtlsMap(ResultSet rsAddReqDetails, String strMCEPChange, HashMap hshMCOvrDetailsMap,
			String strArchFlg) throws SQLException {
		// log.info("Entering method populateAReqOvrDtlsMap");
		// log.info("hshAReqDtls:- "+hshMCOvrDetailsMap);
		// log.info("strArchFlg:- "+strArchFlg);
		// log.info("strMCEPChange:- "+strMCEPChange);
		StringBuffer sbAReqOvrIds = new StringBuffer();
		ArrayList arlAddReq = null;
		while (rsAddReqDetails.next()) {
			arlAddReq = new ArrayList();
			AdditionalReq aReqBean = new AdditionalReq();
			aReqBean.setMcAReqId(rsAddReqDetails.getInt("mc_areq_id"));
			// aReqBean.setEpAReqId(rsAddReqDetails.getInt("ep_areq_id"));
			// if(rsAddReqDetails.getString("ep_areq_desc")!=null)
			// aReqBean.setEndrsDesc(rsAddReqDetails.getString("ep_areq_desc"));
			if (rsAddReqDetails.getString("ep_areq_code") != null)
				aReqBean.setEndrsCode(rsAddReqDetails.getString("ep_areq_code"));
			if (rsAddReqDetails.getString("mc_areq_req") != null)
				aReqBean.setRequired(rsAddReqDetails.getString("mc_areq_req"));
			if (rsAddReqDetails.getString("mc_orignl_req") != null)
				aReqBean.setOriginalReq(rsAddReqDetails.getString("mc_orignl_req"));
			if (rsAddReqDetails.getString("mc_orignl_days") != null)
				aReqBean.setReqInDays(rsAddReqDetails.getString("mc_orignl_days"));
			if (rsAddReqDetails.getDate("areq_rcvd_dt") != null)
				aReqBean.setAreqRcvDate(
						Utility.formatSqlDate(rsAddReqDetails.getDate("areq_rcvd_dt"), Utility.FORMAT4));
			if (rsAddReqDetails.getDate("areq_ori_rcvd_dt") != null)
				aReqBean.setAreqOriRcvDate(
						Utility.formatSqlDate(rsAddReqDetails.getDate("areq_ori_rcvd_dt"), Utility.FORMAT4));
			arlAddReq.add(aReqBean);

			if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
				/* Called when MC Requierment change so setting EP Account Number as key */
				/* Check if Key exists in the main hashtable */
				if (!hshMCOvrDetailsMap.containsKey(rsAddReqDetails.getString("ep_acct_no"))) {
					// log.debug("hashmap do not have EP account no as key");
					hshMCOvrDetailsMap.put(rsAddReqDetails.getString("ep_acct_no"), arlAddReq);
				} else {
					ArrayList arlAddReqTmp = (ArrayList) hshMCOvrDetailsMap
							.get(rsAddReqDetails.getString("ep_acct_no"));
					arlAddReqTmp.add(arlAddReq.get(0));
				}
			} else {
				/* Called when EP Requierment change so setting MC Account Number as key */
				/* Check if Key exists in the main hashtable */
				if (!hshMCOvrDetailsMap.containsKey(rsAddReqDetails.getString("mc_acct_no"))) {
					hshMCOvrDetailsMap.put(rsAddReqDetails.getString("mc_acct_no"), arlAddReq);
				} else {
					ArrayList arlAddReqTmp = (ArrayList) hshMCOvrDetailsMap
							.get(rsAddReqDetails.getString("mc_acct_no"));
					arlAddReqTmp.add(arlAddReq.get(0));
				}
			}
		}
		// log.debug("HashMap after resultSet Iteration:- "+hshMCOvrDetailsMap);
		return sbAReqOvrIds.toString();
	}

	/**
	 * This method will get the Member Specific Details and will be used to
	 * calculate the MC EP Status based on Member Specific Carrier Flag
	 * 
	 * @param con
	 * @param strEPAccNo
	 * @param strMCAccNo
	 * @param paramDate
	 * @param tmpltId    Template Id.. which will be used for EP Change only
	 * @return
	 * @throws UiiaException
	 */
	@Override
	public HashMap getMemberSpecificDetails(String strEPAccNo, String strMCAccNo, Date paramDate, int tmpltId)
			throws Exception {
		// log.info("Entering method getMemberSpecificDetails()");
		// log.info("EP Account Number: "+strEPAccNo);
		// log.info("MC Account Number: "+strMCAccNo);
		// log.info("paramDate: "+paramDate);
		// log.info("tmpltId: "+tmpltId);
		HashMap hshMembDetails = new HashMap();
		String strMCEPChange = "";
		if (strEPAccNo.length() > 0 && strMCAccNo.length() == 0) {
			/* Flag for EP Changing requirements */
			strMCEPChange = GlobalVariables.EP_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() == 0) {
			/* Flag for MC Changing requirements */
			strMCEPChange = GlobalVariables.MC_CHANGE;
		} else if (strMCAccNo.length() > 0 && strEPAccNo.length() > 0) {
			/* For Both MC & EP */
			strMCEPChange = GlobalVariables.MC_EP_CHANGE;
		}

		StringBuffer sbArchQuery = new StringBuffer("");

		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			sbArchQuery.append(" SELECT s.mc_acct_no,ed.ep_mem_det_id,s.ep_acct_no,");
			sbArchQuery.append(
					" IFNULL(ed.ep_cancel,'N') AS mccancel,ed.ep_cncl_eff_dt,IFNULL(ed.ep_mem,'N') AS mcismember,");
			sbArchQuery.append(" a.ep_mem_spcfc_carrier AS epreqmem");
			sbArchQuery.append(
					" FROM arch_ep_specific_addendum a,mc_ep_join_status s LEFT JOIN arch_ep_mc_join_details ed");
			sbArchQuery.append(" ON (ed.ep_acct_no=? ");
			sbArchQuery.append(" AND s.mc_acct_no=ed.mc_acct_no");
			sbArchQuery.append(" AND ? BETWEEN ed.eff_start_dt AND ed.eff_end_dt");
			sbArchQuery
					.append(" AND arch_ep_mem_det_id IN(SELECT MAX(arch_ep_mem_det_id) FROM arch_ep_mc_join_details))");
			sbArchQuery.append(" WHERE a.ep_template_id = ?");
			sbArchQuery.append(" AND s.ep_acct_no=?");
			sbArchQuery.append(" AND ? BETWEEN a.eff_start_dt AND a.eff_end_dt");
			sbArchQuery
					.append(" AND arch_ep_addndm_id IN(SELECT MAX(arch_ep_addndm_id) FROM arch_ep_specific_addendum)");
			sbArchQuery.append(" ORDER BY s.mc_acct_no");
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			sbArchQuery.append(" SELECT t.ep_template_id,ed.ep_mem_det_id,ed.mc_acct_no,t.ep_acct_no,");
			sbArchQuery.append(
					" IFNULL(ed.ep_cancel,'N') AS mccancel,ed.ep_cncl_eff_dt,IFNULL(ed.ep_mem,'N') AS mcismember,");
			sbArchQuery.append(" a.ep_mem_spcfc_carrier AS epreqmem");
			sbArchQuery.append(" FROM ep_template t LEFT JOIN arch_ep_mc_join_details ed");
			sbArchQuery.append(" ON(t.ep_acct_no=ed.ep_acct_no  AND ed.mc_acct_no=?");
			sbArchQuery.append(" AND ? BETWEEN ed.eff_start_dt AND ed.eff_end_dt AND");
			sbArchQuery.append(" arch_ep_mem_det_id IN (SELECT MAX(arch_ep_mem_det_id) FROM arch_ep_mc_join_details))");
			sbArchQuery.append(" ,arch_ep_specific_addendum a");
			sbArchQuery.append(" WHERE t.ep_template_id=a.ep_template_id AND t.active='Y'");
			sbArchQuery
					.append(" AND arch_ep_addndm_id IN (SELECT MAX(arch_ep_addndm_id) FROM arch_ep_specific_addendum");
			sbArchQuery.append(" WHERE ? BETWEEN a.eff_start_dt AND a.eff_end_dt)");
			sbArchQuery.append(" ORDER BY ep_acct_no");
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			sbArchQuery.append(" SELECT s.mc_acct_no,ed.ep_mem_det_id,s.ep_acct_no,");
			sbArchQuery.append(
					" IFNULL(ed.ep_cancel,'N') AS mccancel,ed.ep_cncl_eff_dt,IFNULL(ed.ep_mem,'N') AS mcismember,");
			sbArchQuery.append(" a.ep_mem_spcfc_carrier AS epreqmem");
			sbArchQuery.append(
					" FROM arch_ep_specific_addendum a,mc_ep_join_status s LEFT JOIN arch_ep_mc_join_details ed");
			sbArchQuery.append(" ON (ed.ep_acct_no=? AND ed.mc_acct_no=?");
			sbArchQuery.append(" AND s.mc_acct_no=ed.mc_acct_no");
			sbArchQuery.append(" AND ? BETWEEN ed.eff_start_dt AND ed.eff_end_dt");
			sbArchQuery
					.append(" AND arch_ep_mem_det_id IN(SELECT MAX(arch_ep_mem_det_id) FROM arch_ep_mc_join_details))");
			sbArchQuery.append(" WHERE a.ep_template_id = ?");
			sbArchQuery.append(" AND s.ep_acct_no=?");
			sbArchQuery.append(" AND s.mc_acct_no=?");
			sbArchQuery.append(" AND ? BETWEEN a.eff_start_dt AND a.eff_end_dt");
			sbArchQuery
					.append(" AND arch_ep_addndm_id IN(SELECT MAX(arch_ep_addndm_id) FROM arch_ep_specific_addendum)");
			sbArchQuery.append(" ORDER BY s.mc_acct_no");
		}

		/* for Archival */
		List<Object> params = new ArrayList<>();
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			params.add(strEPAccNo);
			params.add(paramDate);
			params.add(tmpltId);
			params.add(strEPAccNo);
			params.add(paramDate);
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			params.add(strMCAccNo);
			params.add(paramDate);
			params.add(paramDate);
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			params.add(strEPAccNo);
			params.add(strMCAccNo);
			params.add(paramDate);
			params.add(tmpltId);
			params.add(strEPAccNo);
			params.add(strMCAccNo);
			params.add(paramDate);
		}

		final String mcepChange = strMCEPChange;
		String strMemIds = getSpringJdbcTemplate(this.uiiaDataSource).query(sbArchQuery.toString(),
				new ResultSetExtractor<String>() {
					@Override
					public String extractData(ResultSet rsArch) throws SQLException, DataAccessException {
						return populateMemDtlsMap(rsArch, mcepChange, hshMembDetails, GlobalVariables.YES);
					}
				}, params.toArray());

		if (strMemIds.length() == 0) {
			strMemIds = "0";
		}

		/*---------for current tables-----------------------*/
		StringBuffer sbGetQuery = new StringBuffer("");
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			/* Query For EP Change (EP Changing Requirements) */
			sbGetQuery.append(" SELECT s.mc_acct_no,ed.ep_mem_det_id,s.ep_acct_no,");
			sbGetQuery.append(
					" IFNULL(ed.ep_cancel,'N') AS mccancel,ed.ep_cncl_eff_dt,IFNULL(ed.ep_mem,'N') AS mcismember,");
			sbGetQuery.append(" a.ep_mem_spcfc_carrier AS epreqmem");
			sbGetQuery.append(" FROM ep_specific_addendum a,mc_ep_join_status s LEFT JOIN ep_mc_join_details ed");
			sbGetQuery.append(" ON (ed.ep_acct_no=?");
			sbGetQuery.append(" AND ed.ep_mem_det_id NOT IN(" + strMemIds + ")");
			sbGetQuery.append(" AND s.mc_acct_no=ed.mc_acct_no)");
			sbGetQuery.append(" WHERE a.ep_template_id = ?");
			sbGetQuery.append(" AND s.ep_acct_no=?");
			sbGetQuery.append(" ORDER BY s.mc_acct_no");

		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			/* Query For MC Change (MC Changing Requirements) */
			sbGetQuery.append(" SELECT t.ep_template_id,ed.ep_mem_det_id,ed.mc_acct_no,t.ep_acct_no,");
			sbGetQuery.append(
					" IFNULL(ed.ep_cancel,'N') AS mccancel,ed.ep_cncl_eff_dt,IFNULL(ed.ep_mem,'N') AS mcismember,");
			sbGetQuery.append(" a.ep_mem_spcfc_carrier AS epreqmem");
			sbGetQuery.append(" FROM ep_template t LEFT JOIN ep_mc_join_details ed");
			sbGetQuery.append(" ON(t.ep_acct_no=ed.ep_acct_no  AND ed.mc_acct_no=? AND ed.ep_mem_det_id NOT IN("
					+ strMemIds + "))");
			sbGetQuery.append(" ,ep_specific_addendum a");
			sbGetQuery.append(" WHERE t.ep_template_id=a.ep_template_id AND t.active='Y'");
			sbGetQuery.append(" ORDER BY ep_acct_no");
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			/* Query For MC-EP Change */
			sbGetQuery.append(" SELECT s.mc_acct_no,ed.ep_mem_det_id,s.ep_acct_no,");
			sbGetQuery.append(
					" IFNULL(ed.ep_cancel,'N') AS mccancel,ed.ep_cncl_eff_dt,IFNULL(ed.ep_mem,'N') AS mcismember,");
			sbGetQuery.append(" a.ep_mem_spcfc_carrier AS epreqmem");
			sbGetQuery.append(" FROM ep_specific_addendum a,mc_ep_join_status s LEFT JOIN ep_mc_join_details ed");
			sbGetQuery.append(" ON (ed.ep_acct_no=? AND ed.mc_acct_no=?");
			sbGetQuery.append(" AND ed.ep_mem_det_id NOT IN(" + strMemIds + ")");
			sbGetQuery.append(" AND s.mc_acct_no=ed.mc_acct_no)");
			sbGetQuery.append(" WHERE a.ep_template_id = ?");
			sbGetQuery.append(" AND s.ep_acct_no=?");
			sbGetQuery.append(" AND s.mc_acct_no=?");
			sbGetQuery.append(" ORDER BY s.mc_acct_no");
		}
		List<Object> sbGetQueryParams = new ArrayList<>();
		/* Setting parameters for current tables */
		if (GlobalVariables.EP_CHANGE.equals(strMCEPChange)) {
			sbGetQueryParams.add(strEPAccNo);
			sbGetQueryParams.add(tmpltId);
			sbGetQueryParams.add(strEPAccNo);
		} else if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
			sbGetQueryParams.add(strMCAccNo);
		} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
			sbGetQueryParams.add(strEPAccNo);
			sbGetQueryParams.add(strMCAccNo);
			sbGetQueryParams.add(tmpltId);
			sbGetQueryParams.add(strEPAccNo);
			sbGetQueryParams.add(strMCAccNo);
		}
		getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), new ResultSetExtractor<String>() {
			@Override
			public String extractData(ResultSet rsMemList) throws SQLException, DataAccessException {
				return populateMemDtlsMap(rsMemList, mcepChange, hshMembDetails, GlobalVariables.NO);
			}
		}, params.toArray());
		return hshMembDetails;
	}

	/**
	 * This private method will be used to populate Member details and will return
	 * the String of PK's (ep_mem_det_id) used to be used as NOT IN for current
	 * tables
	 * 
	 * @param rsMemDetails       Resultset of member detail on which iteration will
	 *                           be done
	 * @param strMCEPChange      Flag to decide,whether it is called for MC Change
	 *                           or EP change
	 * @param hshMCMemDetailsMap HashMap for which values will be added/updated
	 * @param strArchFlg         To decide whether the query resultset is for
	 *                           archival or current,so that return string can be
	 *                           formed
	 * @return Comma separated string of primary keys which will be used in current
	 *         query
	 * @throws SQLException
	 */
	private String populateMemDtlsMap(ResultSet rsMemDetails, String strMCEPChange, HashMap hshMCMemDetailsMap,
			String strArchFlg) throws SQLException {
		StringBuffer sbMemIds = new StringBuffer();

		UVldMemBean memBean = null;
		while (rsMemDetails != null && rsMemDetails.next()) {
			/*
			 * If called for Archival then to create CommaSeparted string of MC_SpecOvr Ids,
			 * and return to the calling function
			 */
			if (GlobalVariables.YES.equals(strArchFlg)) {
				if (!rsMemDetails.isLast()) {
					//// log.debug("Not the last Record");
					sbMemIds.append(rsMemDetails.getInt("ep_mem_det_id")).append(",");
				}
				if (rsMemDetails.isLast()) {
					//// log.debug("Last Record");
					sbMemIds.append(rsMemDetails.getInt("ep_mem_det_id"));
				}
			}

			/*
			 * To set key in the hashtable as EP Account Number or MC Account Number based
			 * on the parameter strEPChange passed
			 */
			if (GlobalVariables.MC_CHANGE.equals(strMCEPChange)) {
				/* Called when MC Requierment change so setting EP Account Number as key */
				/* Check if Key exists in the main hashtable */
				memBean = new UVldMemBean();
				hshMCMemDetailsMap.put(rsMemDetails.getString("ep_acct_no"), memBean);
			} else if (GlobalVariables.MC_EP_CHANGE.equals(strMCEPChange)) {
				memBean = new UVldMemBean();
				hshMCMemDetailsMap.put(rsMemDetails.getString("ep_acct_no"), memBean);
			} else {
				/* Called when EP Requierment change so setting MC Account Number as key */
				memBean = new UVldMemBean();
				hshMCMemDetailsMap.put(rsMemDetails.getString("mc_acct_no"), memBean);
			}
			memBean.setMemDetId(rsMemDetails.getInt("ep_mem_det_id"));
			memBean.setMcIsMem(rsMemDetails.getString("mcismember"));
			memBean.setEpReqMem(rsMemDetails.getString("epreqmem"));
			memBean.setCncl(rsMemDetails.getString("mccancel"));
			memBean.setCnclDt(Utility.formatSqlDate(rsMemDetails.getDate("ep_cncl_eff_dt"), Utility.FORMAT4));
			memBean.setAttr1("");
			memBean.setAttr2("");
		} // end of while

		return sbMemIds.toString();
	}

	/**
	 * gets MC Member details with EP i.e Member Type, Private, Cancelled etc.
	 * 
	 * @param Connection con
	 * @param String     mcAccNo
	 * @param String     epAccNo
	 * @return ArrayList
	 * @throws Exception
	 */
	@Override
	public EPJoinDet getEPMCMemberDetails(String mcAccNo, String epAccNo) throws Exception {
		EPJoinDet memDetails = new EPJoinDet();
		int intRecCounter = 0;
		StringBuffer sbGetQuery = new StringBuffer(
				"SELECT d.ep_mem_det_id,d.ep_cancel,d.ep_cncl_eff_dt,d.rsn_cancel,d.ep_known_as,a.non_uiia_ep, ");
		sbGetQuery.append(
				" d.ep_mem,d.ep_private,d.ep_house,a.company_name,a.scac_code, d.ep_cncl_email, d.uiia_override_suspension");
		sbGetQuery.append(
				" FROM account_info a LEFT JOIN ep_mc_join_details d ON a.account_no = d.ep_acct_no AND d.mc_acct_no = ? AND  d.ep_acct_no = ? ");
		sbGetQuery.append(" WHERE  a.account_no= ?");
		List<Object> sbGetQueryParams = new ArrayList<>();
		sbGetQueryParams.add(mcAccNo);
		sbGetQueryParams.add(epAccNo);
		sbGetQueryParams.add(epAccNo);
		if (mcAccNo != null && epAccNo != null) {
			getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), new ResultSetExtractor<Object>() {
				@Override
				public Object extractData(ResultSet rsMemDetails) throws SQLException, DataAccessException {
					while (rsMemDetails != null && rsMemDetails.next()) {
						memDetails.setEpMemDtlId(rsMemDetails.getInt("ep_mem_det_id"));
						if (rsMemDetails.getString("ep_cancel") != null) {
							memDetails.setCancelValue(rsMemDetails.getString("ep_cancel"));
							memDetails.setTempCancelValue(rsMemDetails.getString("ep_cancel"));
						} else
							memDetails.setCancelValue("");
						if (rsMemDetails.getDate("ep_cncl_eff_dt") != null) {
							memDetails.setCanEffDate(
									Utility.formatSqlDate(rsMemDetails.getDate("ep_cncl_eff_dt"), Utility.FORMAT4));
							memDetails.setExtraVar(GlobalVariables.YES);
						} else
							memDetails.setCanEffDate("");
						if (rsMemDetails.getString("rsn_cancel") != null)
							memDetails.setRsnCancel(rsMemDetails.getString("rsn_cancel"));
						else
							memDetails.setRsnCancel("");
						if (rsMemDetails.getString("ep_known_as") != null)
							memDetails.setKnownAs(rsMemDetails.getString("ep_known_as"));
						else
							memDetails.setKnownAs("");
						if (rsMemDetails.getString("ep_mem") != null)
							memDetails.setEpMember(rsMemDetails.getString("ep_mem"));
						else
							memDetails.setEpMember("");
						if (rsMemDetails.getString("ep_private") != null)
							memDetails.setEpPrivate(rsMemDetails.getString("ep_private"));
						else
							memDetails.setEpPrivate("");
						if (rsMemDetails.getString("ep_house") != null)
							memDetails.setEpHouse(rsMemDetails.getString("ep_house"));
						else
							memDetails.setEpHouse("");
						if (rsMemDetails.getString("company_name") != null)
							memDetails.setCompanyName(rsMemDetails.getString("company_name"));
						if (rsMemDetails.getString("scac_code") != null)
							memDetails.setScacCode(rsMemDetails.getString("scac_code"));

						if (rsMemDetails.getString("non_uiia_ep") != null)
							memDetails.setNonUIIAEpFlag(rsMemDetails.getString("non_uiia_ep"));

						if (rsMemDetails.getString("ep_cncl_email") != null) {
							memDetails.setEpCanEmail(rsMemDetails.getString("ep_cncl_email"));
						}
						String uiiaOverrideSuspension = rsMemDetails.getString("uiia_override_suspension");
						if (uiiaOverrideSuspension != null
								&& GlobalVariables.YES.equalsIgnoreCase(uiiaOverrideSuspension)) {
							memDetails.setUiiaOverrideEPCan(true);
						} else {
							memDetails.setUiiaOverrideEPCan(false);
						}
						memDetails.setAccountNo(epAccNo);
					}
					return "";
				}
			}, sbGetQueryParams.toArray());
		}
		return memDetails;
	}

	@Override
	public boolean chkEPSpc(int mstId, String epAcctNo) throws Exception {
		boolean flg = false;
		StringBuffer sbGetQuery = new StringBuffer("SELECT * FROM policy_specific_eplist where ep_acct_no='" + epAcctNo
				+ "' AND policy_mst_id = " + mstId);
		int count = findTotalRecordCount(this.uiiaDataSource, sbGetQuery.toString()).intValue();
		if (count > 0) {
			flg = true;
		}
		return flg;
	}

	/*
	 * This method is written to avoid a certificate going bad in case of a PENDING
	 * primary policy so that is checks the EPSPECIFIC policy in the PENDING state
	 * too and not INPLACE before reporting a problem
	 * 
	 */
	@Override
	public ArrayList getEpSpcPending(String epAcctNo, String mcAcctNo, String polCode) throws Exception {
		//// log.info("Business: Entering method
		//// getEpSpcPending("+epAcctNo+","+mcAcctNo+","+polCode+") of
		//// UValidDPolicyCheck class");
		double _lim = 0.0;
		double _ded = 0.0;
		String strEff_date = null;
		ArrayList pendingEpSpcPolDetails = new ArrayList();
		StringBuffer sbGetQuery = new StringBuffer(
				" SELECT p.policy_limit,p.policy_deductible, p.policy_eff_dt FROM policy_master p, policy_specific_eplist e,account_info a ");
		sbGetQuery.append(" WHERE a.account_no=p.mc_acct_no AND p.policy_code=? AND p.policy_type='EPSPECIFIC' ");
		sbGetQuery.append(
				" AND p.policy_status='PENDING' AND a.company_name=p.mc_name AND policy_trmintn_eff_dt IS NULL AND p.mc_acct_no=? ");
		sbGetQuery.append(" AND e.ep_acct_no=? AND p.policy_mst_id=e.policy_mst_id ");

		List<Object> sbGetQueryParams = new ArrayList<>();
		sbGetQueryParams.add(polCode);
		sbGetQueryParams.add(mcAcctNo);
		sbGetQueryParams.add(epAcctNo);

		getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), new ResultSetExtractor<Object>() {
			@Override
			public Object extractData(ResultSet rsJoinSt) throws SQLException, DataAccessException {
				if (rsJoinSt.next()) {
					pendingEpSpcPolDetails.add(rsJoinSt.getDouble("policy_limit"));
					pendingEpSpcPolDetails.add(rsJoinSt.getDouble("policy_deductible"));
					pendingEpSpcPolDetails.add(GlobalVariables.YES);
					pendingEpSpcPolDetails
							.add(Utility.formatSqlDate(rsJoinSt.getDate("POLICY_EFF_DT"), Utility.FORMAT4));
				} else {
					pendingEpSpcPolDetails.add(_lim);
					pendingEpSpcPolDetails.add(_ded);
					pendingEpSpcPolDetails.add(GlobalVariables.NO);
					pendingEpSpcPolDetails.add(strEff_date);
				}
				return "";
			}
		}, sbGetQueryParams.toArray());
		return pendingEpSpcPolDetails;
	}

	@Override
	public ArrayList getDecEpSpcActive(String epAcctNo, String mcAcctNo, String polCode) throws Exception {
		//// log.info("Business: Entering method
		//// getDecEpSpcActive("+epAcctNo+","+mcAcctNo+","+polCode+") of
		//// UValidDPolicyCheck class");
		double _lim = 0.0;
		double _ded = 0.0;
		ArrayList epSpcPolDetails = new ArrayList();
		StringBuffer sbGetQuery = new StringBuffer(
				" SELECT p.policy_limit,p.policy_deductible FROM policy_master p, policy_specific_eplist e,account_info a ");
		sbGetQuery.append(
				" WHERE a.account_no=p.mc_acct_no AND p.policy_code=? AND p.policy_type='EPSPECIFIC' AND p.policy_inplace='Y' ");
		sbGetQuery.append(
				" AND p.policy_status='ACTIVE' AND a.company_name=p.mc_name AND policy_trmintn_eff_dt IS NULL AND p.mc_acct_no=? ");
		sbGetQuery.append(" AND e.ep_acct_no=? AND p.policy_mst_id=e.policy_mst_id ");

		List<Object> sbGetQueryParams = new ArrayList<>();
		sbGetQueryParams.add(polCode);
		sbGetQueryParams.add(mcAcctNo);
		sbGetQueryParams.add(epAcctNo);

		getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), new ResultSetExtractor<Object>() {
			@Override
			public Object extractData(ResultSet rsJoinSt) throws SQLException, DataAccessException {
				if (rsJoinSt.next()) {
					epSpcPolDetails.add(rsJoinSt.getDouble("policy_limit"));
					epSpcPolDetails.add(rsJoinSt.getDouble("policy_deductible"));
					epSpcPolDetails.add(GlobalVariables.YES);
				} else {
					epSpcPolDetails.add(_lim);
					epSpcPolDetails.add(_ded);
					epSpcPolDetails.add(GlobalVariables.NO);
				}
				return "";
			}
		}, sbGetQueryParams.toArray());
		return epSpcPolDetails;
	}
}
