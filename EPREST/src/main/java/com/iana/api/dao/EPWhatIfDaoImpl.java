package com.iana.api.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EPWhatIfDaoImpl extends GenericDAO implements EPWhatIfDao {

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Override
	public int getValidMCCount(String acctNo) throws Exception {
//		log.debug("Entering method getValidMCCount(" + acctNo + ") of EPWhatIfDaoImpl class");
		StringBuffer sbQry = new StringBuffer();

		sbQry.append("SELECT COUNT(MC_ACCT_NO) FROM mc_ep_join_status WHERE ");
		sbQry.append("EP_ACCT_NO = ? AND MC_EP_STATUS = 'Y' ");
		return findTotalRecordCount(this.uiiaDataSource, sbQry.toString(), acctNo).intValue();

	}

}
