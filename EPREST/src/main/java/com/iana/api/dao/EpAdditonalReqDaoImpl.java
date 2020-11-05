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

import com.iana.api.domain.AdditionalReq;
import com.iana.api.domain.EPTemplate;
import com.iana.api.utils.GlobalVariables;

@Repository
public class EpAdditonalReqDaoImpl extends GenericDAO implements EpAdditonalReqDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	/*
	 * this method gets the EP additonal requirements
	 * 
	 * @param int templateId
	 * 
	 * @param String uvalidFlg
	 * 
	 * @return ArrayList
	 * 
	 * @throws Exception
	 */
	public List<AdditionalReq> getEPAddlReq(EPTemplate epTemplate, String uvalidFlg) throws Exception {
		List<AdditionalReq> addlReqLst = new ArrayList<>();
		StringBuffer sbQry = new StringBuffer();

		if (GlobalVariables.NO.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append("SELECT a.ep_areq_id,a.ep_areq_desc,a.ep_areq_code,a.ep_areq_req,a.ep_orgnl_req,");
			sbQry.append(
					"a.ep_orgnl_days,a.attr1,a.attr2,p.file_path,p.ep_addnm_fileid FROM arch_ep_addtln_reqmnt a LEFT JOIN ");
			sbQry.append("ep_addendum_files p ON (p.ep_areq_id = a.ep_areq_id ) WHERE a.ep_template_id = ?");

			if (GlobalVariables.YES.equals(uvalidFlg)) {
				sbQry.append(" AND a.ep_areq_req = ");
				sbQry.append("'" + uvalidFlg + "'");

			}
		} else if (GlobalVariables.YES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())
				|| GlobalVariables.PENDINGTEMPLATES.equalsIgnoreCase(epTemplate.getDbTemplateStatus())) {
			sbQry.append("SELECT e.ep_areq_id,e.ep_areq_desc,e.ep_areq_code,e.ep_areq_req,e.ep_orgnl_req,");
			sbQry.append(
					"e.ep_orgnl_days,e.attr1,e.attr2,p.file_path,p.ep_addnm_fileid FROM ep_addtln_reqmnt e LEFT JOIN ");
			sbQry.append("ep_addendum_files p  ON (p.ep_areq_id = e.ep_areq_id) WHERE e.ep_template_id = ?  ");

			if (GlobalVariables.YES.equals(uvalidFlg)) {
				sbQry.append(" AND e.ep_areq_req = ");
				sbQry.append("'" + uvalidFlg + "'");
			}
		}
		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<List<AdditionalReq>>() {

					@Override
					public List<AdditionalReq> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {

							AdditionalReq epAddlReq = new AdditionalReq();
							epAddlReq.setEpAReqId(rs.getInt("EP_AREQ_ID"));
							if (rs.getString("EP_AREQ_DESC") != null) {
								epAddlReq.setEndrsDesc(rs.getString("EP_AREQ_DESC"));
							}
							if (rs.getString("ep_areq_code") != null) {
								epAddlReq.setEndrsCode(rs.getString("ep_areq_code"));
							}
							if (rs.getString("EP_AREQ_REQ") != null) {
								epAddlReq.setRequired(rs.getString("EP_AREQ_REQ"));
							}
							if (rs.getString("EP_ORGNL_REQ") != null) {
								epAddlReq.setOriginalReq(rs.getString("EP_ORGNL_REQ"));
							}
							if (rs.getString("EP_ORGNL_DAYS") != null) {
								epAddlReq.setReqInDays(rs.getString("EP_ORGNL_DAYS"));
							}
							if (rs.getString("ATTR1") != null) {
								epAddlReq.setAttr1(rs.getString("ATTR1"));
							}
							if (rs.getString("ATTR2") != null) {
								epAddlReq.setAttr2(rs.getString("ATTR2"));
							}
							if (rs.getString("file_path") != null) {
								epAddlReq.setAddReqPath(rs.getString("file_path"));
							}

							epAddlReq.setAddendaPathId(rs.getInt("ep_addnm_fileid"));

							addlReqLst.add(epAddlReq);

						}

						return addlReqLst;
					}
				}, epTemplate.getTemplateID());

	}

}
