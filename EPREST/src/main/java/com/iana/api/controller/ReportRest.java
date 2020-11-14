package com.iana.api.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.Report;
import com.iana.api.service.ReportsService;
import com.iana.api.utils.ApiException;
import com.iana.api.utils.ApiResponseMessage;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "Reports - EP" })
@RequestMapping(path = GlobalVariables.REST_URI_UIIA)
public class ReportRest extends CommonUtils {
	Logger log = LogManager.getLogger(this.getClass().getName());

	private static final String CLASS_NAME = "ReportRest";

	public static final String URI_REPORTS = "monthlyWeeklyReports";
	
	@Autowired
	private ReportsService reportsService;

	@GetMapping(path = URI_REPORTS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET MONTHLY AND WEEKLY REPORTS LIST IN " + CLASS_NAME, response = Report.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> getMonthlyAndWeeklyReports(
			@RequestParam(value = "pageIndex", defaultValue = GlobalVariables.DEFAULT_TEN) int pageIndex,
			HttpServletRequest request) {
		try {
			List<Report> reports = reportsService.getReports(pageIndex);
			return new ResponseEntity<List<Report>>(reports, HttpStatus.OK);
		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}
	}

}
