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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchResult;
import com.iana.api.domain.SecUserDetails;
import com.iana.api.domain.SecurityObject;
import com.iana.api.service.SecondaryUserService;
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
@Api(tags = { "Manage Secondary User" })
@RequestMapping(path = GlobalVariables.REST_URI_UIIA)
public class SecondaryUserRest extends CommonUtils {

	Logger log = LogManager.getLogger(this.getClass().getName());

	private static final String CLASS_NAME = "SecondaryUserRest";
	public static final String URI_SECONDARY_USERS = "secondaryUsers";

	@Autowired
	private SecondaryUserService secondaryUserService;

	@GetMapping(path = URI_SECONDARY_USERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "GET LIST OF SECONDARY USERS REQUEST "
			+ CLASS_NAME, responseContainer = "List", response = SecUserDetails.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> secondaryUsers(@RequestParam(value = "userName", defaultValue = "") String userName,
			HttpServletRequest request) {

		List<Errors> errors = null;
		List<SecUserDetails> secUserLists = null;

		List<String> errorList = getListInstance();
		Pagination pagination = new Pagination();

		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			SecUserDetails secUserDetails = new SecUserDetails();
			secUserDetails.setUserName(decode(userName));

			secondaryUserService.validateSecondaryUsers(securityObject, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);

			} else {
				secondaryUserService.secondaryUsersBusinessValidation(securityObject, secUserDetails, pagination,
						errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);

				} else {
					secUserLists = secondaryUserService.getSecondaryUsers(securityObject, secUserDetails);
				}
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);
			} else {
				return new ResponseEntity<SearchResult<SecUserDetails>>(new SearchResult<>(secUserLists, pagination),
						HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@PostMapping(path = URI_SECONDARY_USERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "CREATE SECONDARY USERS REQUEST " + CLASS_NAME, response = SecUserDetails.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> addSecondaryUser(HttpEntity<SecUserDetails> requestEntity, HttpServletRequest request) {

		List<Errors> errors = null;
		List<String> errorList = getListInstance();

		try {

			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			SecUserDetails secUserDetails = requestEntity.getBody();

			secondaryUserService.validateAddSecondaryUser(secUserDetails, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);

			} else {
				secondaryUserService.addSecondaryUserBusinessValidation(securityObject, secUserDetails, errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);

				} else {
					secondaryUserService.addSecondaryUser(securityObject, secUserDetails);
					secUserDetails.setOldUserName(secUserDetails.getUserName());
					secUserDetails.setAuditTrailExtra(securityObject.getIpAddress());
				}
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				return new ResponseEntity<SecUserDetails>(secUserDetails, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@PutMapping(path = URI_SECONDARY_USERS, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "UPDATE SECONDARY USERS REQUEST " + CLASS_NAME, response = SecUserDetails.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> updateSecondaryUser(HttpEntity<SecUserDetails> requestEntity, HttpServletRequest request) {

		List<Errors> errors = null;
		List<String> errorList = getListInstance();

		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);

			SecUserDetails secUserList = requestEntity.getBody();

			secondaryUserService.validateAddSecondaryUser(secUserList, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);

			} else {
				secondaryUserService.updateSecondaryUserBusinessValidation(securityObject, secUserList, errorList);
				if (isNotNullOrEmpty(errorList)) {
					errors = setBusinessError(errorList);

				} else {
					secondaryUserService.updateSecondaryUser(securityObject, secUserList);
				}
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				return new ResponseEntity<SecUserDetails>(secUserList, HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@DeleteMapping(path = URI_SECONDARY_USERS + "/{secUserId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "DELETE SECONDARY USERS REQUEST " + CLASS_NAME, response = SecUserDetails.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<ApiResponseMessage> deleteSecondaryUser(@PathVariable(name = "secUserId") int secUserId,
			HttpServletRequest request) {

		List<Errors> errors = null;
		List<String> errorList = getListInstance();

		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			SecUserDetails secUserList = new SecUserDetails();
			secUserList.setSecUserId(secUserId);

			secondaryUserService.validateDeleteSecondaryUser(secUserList, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setValidationErrors(errorList);
			}

			secondaryUserService.deleteSecondaryUserBusinessValidation(securityObject, secUserList, errorList);
			if (isNotNullOrEmpty(errorList)) {
				errors = setBusinessError(errorList);

			} else {
				secondaryUserService.deleteSecondaryUser(securityObject, secUserList);
			}

			if (isNotNullOrEmpty(errors)) {
				return sendUnprocessableEntity(errors);

			} else {
				// for Success Case
				ApiResponseMessage response = new ApiResponseMessage(ApiResponseMessage.OK,
						env.getProperty("msg_success_delete_secondaryUser"), null);
				return new ResponseEntity<ApiResponseMessage>(response, HttpStatus.OK);

			}
		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

}
