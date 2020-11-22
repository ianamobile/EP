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

import com.iana.api.domain.EPTemplate;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Repository
public class EpAddendumDaoImpl extends GenericDAO implements EpAddendumDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	/*
	 * this method gets all the EP template list based on the status
	 * (past,present,future)
	 * 
	 * @param EPTemplateBean epTemplate
	 * 
	 * @param String accountNo
	 * 
	 * @return ArrayList
	 * 
	 * @throws Exception
	 */
	public List<EPTemplate> getTemplateList(EPTemplate epTemplate, String accountNo) throws Exception {

		List<EPTemplate> templateLst = new ArrayList<>();
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
				}, epTemplate.getPageNumber() * epTemplate.getLimit(), epTemplate.getLimit());
	}

}
