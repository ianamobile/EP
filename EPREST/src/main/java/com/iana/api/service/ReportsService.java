package com.iana.api.service;

import java.util.List;

import com.iana.api.domain.Report;

public interface ReportsService {

	List<Report> getReports(int pageIndex) throws Exception;
}
