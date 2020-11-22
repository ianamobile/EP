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

import com.iana.api.domain.Report;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Repository
public class ReportDaoImpl extends GenericDAO implements ReportDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Override
	public List<Report> getReportsList(int pageIndex, int pageSize, String flag) throws Exception {
		List<Report> arlRampList = new ArrayList<>();
		StringBuffer sbGetQuery = new StringBuffer(
				"SELECT name,type,attr1,attr2,created_date,created_datetime FROM books_suppliments b order by created_datetime desc");
		if (!GlobalVariables.FLAG_REPORT.equalsIgnoreCase(flag))
			sbGetQuery.append(" LIMIT ?,?");

		List<Object> params = new ArrayList<>();
		if (!GlobalVariables.FLAG_REPORT.equalsIgnoreCase(flag)) {
			params.add((pageIndex * pageSize));
			params.add(pageSize);
		}

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(),
				new ResultSetExtractor<List<Report>>() {

					@Override
					public List<Report> extractData(ResultSet rs) throws SQLException, DataAccessException {

						while (rs.next()) {

							Report rBean = new Report();

							if (rs.getString("name") != null) {
								rBean.setFileName(rs.getString("name"));
							}

							if (rs.getString("type") != null) {
								rBean.setType(rs.getString("type"));
							}
							if (rs.getString("attr1") != null) {
								rBean.setAttr1(rs.getString("attr1"));
							}
							if (rs.getString("attr2") != null) {
								rBean.setAttr2(rs.getString("attr2"));
							}
							if (rs.getString("created_date") != null) {
								rBean.setCreatedDate(
										Utility.formatSqlDate(rs.getDate("created_date"), Utility.FORMAT4));
							}
							if (rs.getString("created_datetime") != null) {
								rBean.setCreatedDateTime(
										Utility.formatSqlDate(rs.getTimestamp("created_datetime"), Utility.FORMAT6));
							}
							arlRampList.add(rBean);
						}

						return arlRampList;
					}
				}, params.toArray());

	}

}
