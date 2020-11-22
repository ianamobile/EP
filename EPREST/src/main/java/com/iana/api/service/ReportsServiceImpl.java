package com.iana.api.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.dao.ReportDao;
import com.iana.api.domain.Report;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

@Service
public class ReportsServiceImpl extends CommonUtils implements ReportsService {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private ReportDao reportDao;

	@Override
	public List<Report> getReports(int pageIndex) throws Exception {
		return reportDao.getReportsList(pageIndex, GlobalVariables.LIMIT, "");
	}

}
