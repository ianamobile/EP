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

import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.Pagination;
import com.iana.api.domain.SearchAccount;
import com.iana.api.domain.SearchResult;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.SetupMCDataJsonDTO;
import com.iana.api.service.EPService;
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
@Api(tags={"Equipment Provider - EP"})
@RequestMapping(path=GlobalVariables.REST_URI_UIIA)
public class EPRest extends CommonUtils { 
	
	private static final String CLASS_NAME = "EPRest";
	
	Logger log = LogManager.getLogger(this.getClass().getName());
	
	public static final String URI_EP_MOTOR_CARRIERS  			= "epMotorCarriers";
	public static final String URI_GET_MCLOOKUP_FOR_EP			= "getMCLookUpForEP";
	
	@Autowired
	private EPService epService;
	
	@GetMapping(path = URI_EP_MOTOR_CARRIERS, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "GET LIST OF EP MOTOR CARRIERS REQUEST "+ CLASS_NAME, responseContainer="List", response = JoinRecord.class, tags={GlobalVariables.CATEGORY_MC_LOOKUP})
	@ApiResponses({
		@ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
		@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
		@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class)
    })
	public ResponseEntity<?> epMotorCarriers (	@RequestParam(value = "mcName", defaultValue = "") String mcName,
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
				return new ResponseEntity<SearchResult<JoinRecord>>(new SearchResult<>(joinRecords, pagination), HttpStatus.OK);
			}

		} catch (ApiException e) {
			return sendValidationError(e);
	
		} catch (Exception e) {
			return sendServerError(e,GlobalVariables.FAIL);
		}
		
	}
	
	@GetMapping(path = URI_GET_MCLOOKUP_FOR_EP, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "GET MC LOOKUP FOR EP REQUEST "+ CLASS_NAME, responseContainer="List", response = SetupMCDataJsonDTO.class, tags = {GlobalVariables.CATEGORY_MC_LOOKUP})
	@ApiResponses({
		@ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
		@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
		@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class)
    })
	public ResponseEntity<?> getMCLookUpForEP (@RequestParam(value = "mcName", defaultValue = "") String mcName,
											   @RequestParam(value = "mcScac", defaultValue = "") String mcScac,
											   @RequestParam(value = "epAccNo", defaultValue = "") String epAccNo,
											   HttpServletRequest request) {
		
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
			return sendServerError(e,GlobalVariables.FAIL);
		}
		
	}
 	
}
