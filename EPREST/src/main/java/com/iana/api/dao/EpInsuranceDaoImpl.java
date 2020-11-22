package com.iana.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.EPInsNeeds;
import com.iana.api.domain.EPTemplate;
import com.iana.api.domain.MultipleLimit;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Repository
public class EpInsuranceDaoImpl extends GenericDAO implements EpInsuranceDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	/*
	 * this method gets all the EP insurance details
	 * 
	 * @param int templateId
	 * 
	 * @return ArrayList
	 * 
	 * @throws Exception
	 */
	public List<EPInsNeeds> getEPInsuranceDetails(EPTemplate epTemplate) throws Exception {
		List<EPInsNeeds> insLst = new ArrayList<>();
		StringBuffer sbQry = new StringBuffer();

		if (GlobalVariables.NO.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append("SELECT EP_NEEDS_ID,POLICY_TYPE,MIN_LMT,MAX_DED,ADDTLN_INSRD_REQD,");
			sbQry.append("SLF_INSRD_REQD,MIN_BST_RTNG,RRG_ALLWD,SPL_INSRN_ALLWD,ela,elp,ele,");
			sbQry.append("ATTR1,ATTR2,ATTR3,MULTI_PRSNT FROM arch_ep_needs WHERE");
			sbQry.append(" EP_TEMPLATE_ID = '" + epTemplate.getTemplateID() + "'");

		} else if (GlobalVariables.YES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())
				|| GlobalVariables.PENDINGTEMPLATES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append("SELECT EP_NEEDS_ID,POLICY_TYPE,MIN_LMT,MAX_DED,ADDTLN_INSRD_REQD,");
			sbQry.append("SLF_INSRD_REQD,MIN_BST_RTNG,RRG_ALLWD,SPL_INSRN_ALLWD,ela,elp,ele,");
			sbQry.append("ATTR1,ATTR2,ATTR3,MULTI_PRSNT FROM ep_needs WHERE");
			sbQry.append(" EP_TEMPLATE_ID = '" + epTemplate.getTemplateID() + "'");
		}
		if (sbQry.length() == 0) {
			return insLst;
		}

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<List<EPInsNeeds>>() {

					@Override
					public List<EPInsNeeds> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {
							EPInsNeeds epIns = new EPInsNeeds();
							epIns.setEpNeedsId(rs.getInt("EP_NEEDS_ID"));
							if (rs.getString("POLICY_TYPE") != null) {
								epIns.setPolicyType(rs.getString("POLICY_TYPE"));
							}
							epIns.setMinLimit(Utility.createCommaString(Utility.intToString(rs.getInt("MIN_LMT"))));
							epIns.setMaxDed(Utility.createCommaString(Utility.intToString(rs.getInt("MAX_DED"))));
							if (rs.getString("ADDTLN_INSRD_REQD") != null) {
								epIns.setAddInsReq(rs.getString("ADDTLN_INSRD_REQD"));
							}
							if (rs.getString("SLF_INSRD_REQD") != null) {
								epIns.setSelfInsReq(rs.getString("SLF_INSRD_REQD"));
							}
							if (rs.getString("MIN_BST_RTNG") != null)
								epIns.setMinBestRat(rs.getString("MIN_BST_RTNG"));
							if (rs.getString("RRG_ALLWD") != null) {
								epIns.setRrgAllwd(rs.getString("RRG_ALLWD"));
							}
							if (rs.getString("SPL_INSRN_ALLWD") != null) {
								epIns.setSpcInsAllwd(rs.getString("SPL_INSRN_ALLWD"));
							}
							// swati----14/9----client comments
							epIns.setELA(Utility.createCommaString(Utility.intToString(rs.getInt("ela"))));
							epIns.setELP(Utility.createCommaString(Utility.intToString(rs.getInt("elp"))));
							epIns.setELE(Utility.createCommaString(Utility.intToString(rs.getInt("ele"))));
							// end---14/9
							if (rs.getString("ATTR2") != null) {
								epIns.setAttr2(rs.getString("ATTR2"));
							}
							epIns.setAttr1(rs.getInt("ATTR1"));
							if (rs.getString("ATTR3") != null) {
								epIns.setAttr3(rs.getString("ATTR3"));
							}
							if (rs.getString("MULTI_PRSNT") != null) {
								epIns.setMultiLimDedPresent(rs.getString("MULTI_PRSNT"));
							}

							insLst.add(epIns);

						}

						return insLst;
					}
				});
	}

	/*
	 * this method gets the EP multiple limit/deductibles
	 * 
	 * @param int templateId
	 * 
	 * @return ArrayList
	 * 
	 * @throws Exception
	 */
	public List<MultipleLimit> getEPMultipleLim(EPTemplate epTemplate) throws Exception {
		List<MultipleLimit> multLst = new ArrayList<>();
		StringBuffer sbQry = new StringBuffer();

		if (GlobalVariables.NO.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append("SELECT EPMC_SPC_LMTS_ID,POLICY_TYPE,MIN_LMT,MAX_DED,");
			sbQry.append("ATTR1,ATTR2,ATTR3 FROM arch_policy_multiple_limits WHERE");
			sbQry.append(" EP_TEMPLATE_ID = '" + epTemplate.getTemplateID() + "'");

		} else if (GlobalVariables.YES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())
				|| GlobalVariables.PENDINGTEMPLATES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append("SELECT EPMC_SPC_LMTS_ID,POLICY_TYPE,MIN_LMT,MAX_DED,");
			sbQry.append("ATTR1,ATTR2,ATTR3 FROM policy_multiple_limits WHERE");
			sbQry.append(" EP_TEMPLATE_ID = '" + epTemplate.getTemplateID() + "'");
		}
		if (sbQry.length() == 0) {
			return multLst;
		}
		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<List<MultipleLimit>>() {

					@Override
					public List<MultipleLimit> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {

							MultipleLimit epMultiLim = new MultipleLimit();
							epMultiLim.setMultiLimId(rs.getInt("EPMC_SPC_LMTS_ID"));
							if (rs.getString("POLICY_TYPE") != null) {
								epMultiLim.setPolicyType(rs.getString("POLICY_TYPE"));
							}
							epMultiLim
									.setMinLimit(Utility.createCommaString(Utility.intToString(rs.getInt("MIN_LMT"))));
							epMultiLim.setMaxDed(Utility.createCommaString(Utility.intToString(rs.getInt("MAX_DED"))));
							epMultiLim.setAttr1(rs.getInt("ATTR1"));
							if (rs.getString("ATTR2") != null) {
								epMultiLim.setAttr2(rs.getString("ATTR2"));
							}
							if (rs.getString("ATTR3") != null) {
								epMultiLim.setAttr3(rs.getString("ATTR3"));
							}

							multLst.add(epMultiLim);
						}

						return multLst;
					}
				});

	}
}
