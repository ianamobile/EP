package com.iana.api.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.AccountMaster;
import com.iana.api.domain.EPAcctInfo;
import com.iana.api.domain.EPAddendumDetForm;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SearchResult;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupAddendumDetails;
import com.iana.api.domain.SetupEpTemplates;
import com.iana.api.domain.SetupMCDataJsonDTO;
import com.iana.api.domain.SetupManageAccountInfo;
import com.iana.api.service.EPService;
import com.iana.api.service.LoginService;
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
@Api(tags = { "Equipment Provider - EP" })
@RequestMapping(path = GlobalVariables.REST_URI_UIIA)
public class EPRest extends CommonUtils {

	private static final String CLASS_NAME = "EPRest";

	Logger log = LogManager.getLogger(this.getClass().getName());

	public static final String URI_EP_MOTOR_CARRIERS = "epMotorCarriers";
	public static final String URI_GET_MCLOOKUP_FOR_EP = "getMCLookUpForEP";

	public static final String URI_SETUP = "/setup";
	public static final String URI_EP_TEMPLATES = "epTemplates";
	public static final String URI_MANAGE_ACCOUNT_INFO = "loadEPSelfReg";
	public static final String URI_CURRENT_ADDENDUM_DETAILS = "loadAddendumDetails";

	@Autowired
	private EPService epService;

	@Autowired
	private LoginService loginService;

	@GetMapping(path = URI_EP_MOTOR_CARRIERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET LIST OF EP MOTOR CARRIERS REQUEST "
			+ CLASS_NAME, responseContainer = "List", response = JoinRecord.class, tags = {
					GlobalVariables.CATEGORY_MC_LOOKUP })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> epMotorCarriers(@RequestParam(value = "mcName", defaultValue = "") String mcName,
			@RequestParam(value = "mcScac", defaultValue = "") String mcScac,
			@RequestParam(value = "knownAs", defaultValue = "") String knownAs,
			@RequestParam(value = "pageIndex", defaultValue = "1") int pageIndex,
			@RequestParam(value = "pageSize", defaultValue = GlobalVariables.DEFAULT_TEN) int pageSize,
			HttpServletRequest request) {

		List<Errors> errors = null;
		List<JoinRecord> joinRecords = null;

		List<String> errorList = getListInstance();
		Pagination pagination = new Pagination();

		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			SearchAccount searchAccount = new SearchAccount();
			searchAccount.setCompanyName(decode(mcName));
			searchAccount.setScac(decode(mcScac));
			searchAccount.setKnownAs(decode(knownAs));
			searchAccount.setAccountNumber(securityObject.getAccountNumber());
			searchAccount.setPageIndex(pageIndex);
			searchAccount.setPageSize(pageSize);

			epService.validateEPMotorCarriers(securityObject, searchAccount, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);

			} else {
				epService.epMotorCarriersBusinessValidation(securityObject, searchAccount, pagination, errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);

				} else {
					joinRecords = epService.getEPMotorCarriers(securityObject, searchAccount);
				}
			}
			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				return new ResponseEntity<SearchResult<JoinRecord>>(new SearchResult<>(joinRecords, pagination),
						HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@GetMapping(path = URI_GET_MCLOOKUP_FOR_EP, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET MC LOOKUP FOR EP REQUEST "
			+ CLASS_NAME, responseContainer = "List", response = SetupMCDataJsonDTO.class, tags = {
					GlobalVariables.CATEGORY_MC_LOOKUP })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> getMCLookUpForEP(@RequestParam(value = "mcName", defaultValue = "") String mcName,
			@RequestParam(value = "mcScac", defaultValue = "") String mcScac,
			@RequestParam(value = "epAccNo", defaultValue = "") String epAccNo, HttpServletRequest request) {

		List<Errors> errors = null;
		SetupMCDataJsonDTO setupMCDataJsonDTO = null;

		List<String> errorList = getListInstance();
		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			SearchAccount searchAccount = new SearchAccount();
			searchAccount.setCompanyName(decode(mcName));
			searchAccount.setScac(decode(mcScac));
			searchAccount.setAccountNumber(decode(epAccNo));
			searchAccount.setUserType(GlobalVariables.EQUIPMENTPROVIDER);

			epService.validateMCLookUpForEP(securityObject, searchAccount, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				setupMCDataJsonDTO = epService.getMCLookUpForEP(securityObject, searchAccount);
			}
			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				return new ResponseEntity<SetupMCDataJsonDTO>(setupMCDataJsonDTO, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@GetMapping(path = URI_EP_TEMPLATES + URI_SETUP, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "SETUP EP TEMPLATES IN " + CLASS_NAME, response = SetupEpTemplates.class, tags = {
			GlobalVariables.CATEGORY_EP_TEMPLATE, GlobalVariables.CATEGORY_SETUP })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> epTemplatesSetup(HttpServletRequest request) {

		try {

			SetupEpTemplates setupEpTemplates = epService.setupEpTemplates();
			return new ResponseEntity<SetupEpTemplates>(setupEpTemplates, HttpStatus.OK);

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@GetMapping(path = URI_MANAGE_ACCOUNT_INFO, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET MANAGE ACCOUNT INFORMATION IN " + CLASS_NAME, response = AccountMaster.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> getManageAccountInfo(HttpServletRequest request) {

		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			AccountMaster accountMaster = epService.getEPAccountInfo(securityObject.getAccountNumber());
			return new ResponseEntity<AccountMaster>(accountMaster, HttpStatus.OK);

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@GetMapping(path = URI_MANAGE_ACCOUNT_INFO + URI_SETUP, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "SETUP MANAGE ACCOUNT INFORMATION IN " + CLASS_NAME, response = SetupManageAccountInfo.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> setupManageAccountInfo(HttpServletRequest request) {

		try {

			SetupManageAccountInfo setupManageAccountInfo = epService.setupManageAccountInfo();
			return new ResponseEntity<SetupManageAccountInfo>(setupManageAccountInfo, HttpStatus.OK);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@PutMapping(path = URI_MANAGE_ACCOUNT_INFO, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "UPDATE MANAGE ACCOUNT INFORMATION REQUEST ", response = ApiResponseMessage.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> updateMangeAccountInfo(HttpEntity<EPAcctInfo> requestEntity, HttpServletRequest request) {

		List<Errors> errors = null;
		List<String> errorList = getListInstance();
		EPAcctInfo epAcctInfo = requestEntity.getBody();

		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			epService.validateManageAccountInfo(epAcctInfo, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				epService.updateManageAccountInfoBusinessValidation(epAcctInfo, errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);

				} else {
					epService.updateManageAccountInfo(securityObject, epAcctInfo);
				}
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {

				// Search Success Case
				ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK,
						env.getProperty("msg_success_account_info"), null);
				if (StringUtils.isBlank(securityObject.getScac())
						&& StringUtils.isNotBlank(epAcctInfo.getAcctInfo().getScacCode())) {
					securityObject.setScac(epAcctInfo.getAcctInfo().getScacCode());
					securityObject.setAccessToken(loginService.prepareAccessToken(securityObject));
					response.setDetails(securityObject.getAccessToken());
					log.info("updated scac::" + securityObject);
				}

				return new ResponseEntity<ApiResponseMessage>(response, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}
	
	@GetMapping(path = URI_CURRENT_ADDENDUM_DETAILS + URI_SETUP, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "SETUP MANAGE ACCOUNT INFORMATION IN " + CLASS_NAME, response = SetupManageAccountInfo.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> setupCurrentAddendumDetails(HttpServletRequest request) {

		try {

			SetupAddendumDetails setupAddendumDetails = epService.setupCurrentAddendumDetails();
			return new ResponseEntity<SetupAddendumDetails>(setupAddendumDetails, HttpStatus.OK);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}
	
	@GetMapping(path = URI_CURRENT_ADDENDUM_DETAILS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "SETUP MANAGE ACCOUNT INFORMATION IN " + CLASS_NAME, response = EPAddendumDetForm.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> getCurrentAddendumDetails(HttpServletRequest request) {

		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			EPAddendumDetForm epAddendumDetForm = epService.getCurrentAddendumDetails(securityObject);
			return new ResponseEntity<EPAddendumDetForm>(epAddendumDetForm, HttpStatus.OK);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}


}
