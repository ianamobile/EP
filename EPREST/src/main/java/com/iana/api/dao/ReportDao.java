package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.Report;

public interface ReportDao {

	List<Report> getReportsList(int pageIndex, int pageSize, String flag) throws Exception;

}
