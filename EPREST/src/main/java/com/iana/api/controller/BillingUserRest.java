package com.iana.api.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchResult;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.billing.payment.BillingUser;
import com.iana.api.service.billing.payment.BillingUserService;
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
@Api(tags = { "Manage Billing Users" })
@RequestMapping(path = GlobalVariables.REST_URI_UIIA)
public class BillingUserRest extends CommonUtils {
	Logger log = LogManager.getLogger(this.getClass().getName());

	private static final String CLASS_NAME = "BillingUserRest";

	public static final String URI_BILLING_USERS = "billingUsers";

	@Autowired
	BillingUserService billingUserService;

	@GetMapping(path = URI_BILLING_USERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET LIST OF BILLING USERS REQUEST "
			+ CLASS_NAME, responseContainer = "List", response = BillingUser.class, tags = {
					GlobalVariables.CATEGORY_MANAGE_BILLING_USERS })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> billingUsers(HttpServletRequest request) {

		List<Errors> errors = null;
		List<BillingUser> billingUsers = null;

		List<String> errorList = getListInstance();
		Pagination pagination = new Pagination();

		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			billingUsers = billingUserService.billingUsers(securityObject);
			if (isNullOrEmpty(billingUsers)) {
				errorList.add(env.getProperty("msg_error_no_records_found"));
				errors = setBusinessError(errorList);
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				return new ResponseEntity<SearchResult<BillingUser>>(new SearchResult<>(billingUsers, pagination),
						HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@DeleteMapping(path = URI_BILLING_USERS + "/{buId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "DELETE SELECTED BILLING USER REQUEST "
			+ CLASS_NAME, response = ApiResponseMessage.class, tags = { GlobalVariables.CATEGORY_MANAGE_BILLING_USERS })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> billingUsersDelete(@PathVariable(name = "buId") Long buId,
			HttpEntity<BillingUser> requestEntity, HttpServletRequest request) {
		List<Errors> errors = null;
		List<String> errorList = getListInstance();
		BillingUser billingUser = requestEntity.getBody();
		try {
			log.info("billingUsersDelete:buId:" + buId + "=billingUser:" + billingUser);

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			billingUserService.validateDeleteBillingUser(securityObject, buId, billingUser, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				billingUser.setBuId(buId);
				billingUserService.deleteBillingUserBusinessValidation(securityObject, billingUser, errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);
				} else {
					billingUserService.deleteBillingUser(securityObject, billingUser);
				}
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				ApiResponseMessage apiResponseMessage = new ApiResponseMessage(GlobalVariables.OK,
						env.getProperty("msg_success_delete_billing_user"), null);
				return new ResponseEntity<ApiResponseMessage>(apiResponseMessage, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}
	}

	@PostMapping(path = URI_BILLING_USERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "CREATE NEW BILLING USER REQUEST " + CLASS_NAME, response = ApiResponseMessage.class, tags = {
			GlobalVariables.CATEGORY_MANAGE_BILLING_USERS })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> billingUsersCreate(HttpEntity<BillingUser> requestEntity, HttpServletRequest request) {
		List<Errors> errors = null;
		List<String> errorList = getListInstance();
		BillingUser billingUser = requestEntity.getBody();
		try {
//			log.info("billingUsersDelete:buId:" + buId + "=billingUser:" + billingUser);

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			billingUserService.validateBillingUser(billingUser, errorList, false);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				billingUserService.saveBillingUser(billingUser, securityObject);
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				ApiResponseMessage apiResponseMessage = new ApiResponseMessage(GlobalVariables.OK,
						env.getProperty("msg_success_add_billing_user"), null);
				return new ResponseEntity<ApiResponseMessage>(apiResponseMessage, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}
	}
	
	@PutMapping(path = URI_BILLING_USERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "UPDATE BILLING USER REQUEST " + CLASS_NAME, response = ApiResponseMessage.class, tags = {
			GlobalVariables.CATEGORY_MANAGE_BILLING_USERS })
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> billingUsersUpdate(HttpEntity<BillingUser> requestEntity, HttpServletRequest request) {
		List<Errors> errors = null;
		List<String> errorList = getListInstance();
		BillingUser billingUser = requestEntity.getBody();
		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			
			billingUserService.validateBillingUser(billingUser, errorList, true);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			} else {
				billingUserService.updateBillingUser(billingUser, securityObject);
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				ApiResponseMessage apiResponseMessage = new ApiResponseMessage(GlobalVariables.OK,
						env.getProperty("msg_success_add_billing_user"), null);
				return new ResponseEntity<ApiResponseMessage>(apiResponseMessage, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}
	}


}
