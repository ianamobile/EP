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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.FinanceSearch;
import com.iana.api.domain.InternalFinanceSearch;
import com.iana.api.domain.InvoiceHeader;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchResult;
import com.iana.api.domain.SecurityObject;
import com.iana.api.service.billing.payment.BillingInvoiceService;
import com.iana.api.utils.ApiException;
import com.iana.api.utils.ApiResponseMessage;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.Errors;
import com.iana.api.utils.GlobalVariables;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "Billing Invoices - EP" })
@RequestMapping(path = GlobalVariables.REST_URI_UIIA)
public class BillingInvoiceRest extends CommonUtils {
	Logger log = LogManager.getLogger(this.getClass().getName());

	private static final String CLASS_NAME = "BillingInvoiceRest";

	public static final String URI_INVOICES = "invoices";
	public static final String URI_DOWNLOAD_INVOICE = "/downloadInvoice";
	public static final String URI_DOWNLOAD_RECEIPT = "/downloadReceipt";
	public static final String URI_NOTIFY = "/notify";

	@Autowired
	private BillingInvoiceService billingInvoiceService;

	@GetMapping(path = URI_INVOICES, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET LIST OF INVOICES FOR MC", responseContainer = "List", response = FinanceSearch.class, tags = {
			GlobalVariables.CATEGORY_UIIA_INVOICES })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> invoices(@RequestParam(value = "pageIndex", defaultValue = "1") int pageIndex,
			@RequestParam(value = "pageSize", defaultValue = GlobalVariables.DEFAULT_TEN) int pageSize,
			HttpServletRequest request) {
		List<Errors> errors = null;
		List<FinanceSearch> invoices = null;

//		List<String> errorList = getListInstance();
		Pagination pagination = new Pagination();

		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			InternalFinanceSearch internalFinanceSearch = new InternalFinanceSearch();
			internalFinanceSearch.setAccNo(securityObject.getAccountNumber());
			internalFinanceSearch.setStatus("%");
			internalFinanceSearch.setPageIndex(pageIndex);
			internalFinanceSearch.setPageSize(pageSize);
			invoices = billingInvoiceService.getInvoices(internalFinanceSearch);

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				return new ResponseEntity<SearchResult<FinanceSearch>>(new SearchResult<>(invoices, pagination),
						HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}
	}

	@GetMapping(path = URI_INVOICES + "/{invId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET DETAIL OF SELECTED INVOICE DETAILS", response = InvoiceHeader.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> invoiceDetails(@PathVariable(name = "invId") String invNo, HttpServletRequest request) {
		InvoiceHeader headerBean = new InvoiceHeader();
		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			FinanceSearch searchparams = new FinanceSearch();
			searchparams.setInvNo(invNo);
			headerBean = billingInvoiceService.getTempInvData(securityObject, searchparams);

			return new ResponseEntity<InvoiceHeader>(headerBean, HttpStatus.OK);
		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}
	}

}
