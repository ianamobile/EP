package com.iana.api.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iana.api.domain.EPAddendum;
import com.iana.api.domain.EPInsNeeds;
import com.iana.api.domain.EPInsOvrWrapper;
import com.iana.api.domain.EPSwitches;
import com.iana.api.domain.EPWhatIfForm;
import com.iana.api.domain.SecurityObject;
import com.iana.api.service.EPWhatIfService;
import com.iana.api.service.UValidMainService;
import com.iana.api.utils.ApiException;
import com.iana.api.utils.ApiResponseMessage;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "EP WhatIF" })
@RequestMapping(path = GlobalVariables.REST_URI_UIIA)
public class EPWhatIfRest extends CommonUtils {
	Logger log = LogManager.getLogger(this.getClass().getName());

	private static final String CLASS_NAME = "EPWhatIfRest";

	public static final String URI_EP_WHAT_IF = "epWhatIf";
	String validMC;

	@Autowired
	private EPWhatIfService epWhatIfService;

	@Autowired
	private UValidMainService uValidMainService;

	@GetMapping(path = URI_EP_WHAT_IF, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "EP WHAT IF IN " + CLASS_NAME, response = EPWhatIfForm.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> getEPWhatIf(HttpServletRequest request) {

		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			EPWhatIfForm epWhatIfForm = epWhatIfService.getWhatIfTemplate(securityObject.getAccountNumber());
			return new ResponseEntity<EPWhatIfForm>(epWhatIfForm, HttpStatus.OK);
		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

	@PostMapping(path = URI_EP_WHAT_IF, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "EP WHAT IF IN " + CLASS_NAME, response = EPWhatIfForm.class)
	@ApiResponses({ @ApiResponse(code = 200, message = GlobalVariables.RESPONSE_MSG_200),
			@ApiResponse(code = 422, message = GlobalVariables.RESPONSE_MSG_422, response = ApiResponseMessage.class),
			@ApiResponse(code = 500, message = GlobalVariables.RESPONSE_MSG_500, response = ApiResponseMessage.class) })
	public ResponseEntity<?> calculateEPWhatIf(HttpEntity<EPWhatIfForm> requestEntity, HttpServletRequest request) {

		try {
			SecurityObject securityObject = (SecurityObject) request.getAttribute(GlobalVariables.SECURITY_OBJECT);
			EPWhatIfForm epWhatIfForm = requestEntity.getBody();
			EPInsOvrWrapper epDtls = new EPInsOvrWrapper();
//			EPAddendum addendumBean = (EPAddendum) request
//					.getAttribute("addendumBean");
//			EPSwitches epSwitchBean = (EPSwitches) session
//					.getAttribute("epSwitches");
			// epSwitchBean.setBlanketAllwd(request.getParameter("blanketAllwd"));
			EPAddendum addendumBean = epWhatIfForm.getEpAddendum();
			EPSwitches epSwitchBean = epWhatIfForm.getEpAddendum().getEpSwitches();
			epSwitchBean.setBlanketAllwd(epWhatIfForm.getBlanketAllwd());
			epDtls.setEpSwitches(epSwitchBean);
			// addReqList
			epDtls.setAddReq(epWhatIfForm.getEpAddendum().getAddReq());
			log.debug("addreq arraylist" + epDtls.getAddReq().toString());
			log.debug("Template Id:- " + request.getParameter("tempID"));
			if (request.getParameter("tempID") != null) {
				epDtls.setTemplateId(Integer.parseInt(request.getParameter("tempID")));
			}

			List result1 = new ArrayList();
			for (int i = 0; i < epWhatIfForm.getEpNeeds().size(); i++) {
				EPInsNeeds needsBean = (EPInsNeeds) epWhatIfForm.getEpNeeds().get(i);
				boolean emptyVal = needsBean.isEmpty();
				if (!emptyVal) {
					needsBean.setEpNeedsId(0);
					result1.add(needsBean);
				}
			}
//			UserBean uBean = (UserBean) session.getAttribute("userBean");
			log.debug("form:- " + result1.toString());
			int mcCntr = uValidMainService.getValidMCCountForWhatIf(securityObject.getAccountNumber(), epDtls, result1);
			epWhatIfForm.setValidMCCalculated(String.valueOf(mcCntr));
//			request.setAttribute("validMCCalculated", String.valueOf(mcCntr));
			addendumBean.setEpSwitches(epSwitchBean);
			addendumBean.setAddReq(epWhatIfForm.getEpAddendum().getAddReq());
//			addendumBean.setAddReq((ArrayList) session.getAttribute("addReqList"));
			if (request.getParameter("tempID") != null) {
				addendumBean.setTemplateID(Integer.parseInt(request.getParameter("tempID")));
			}
//			request.setAttribute("addendumBean", addendumBean);
			epWhatIfForm.setEpAddendum(addendumBean);
			epWhatIfForm.setBlanketAllwd(addendumBean.getEpSwitches().getBlanketAllwd());
			epWhatIfForm.setEpNeeds(addendumBean.getEpNeeds());
			epWhatIfForm.setValidMC("");
			return new ResponseEntity<EPWhatIfForm>(epWhatIfForm, HttpStatus.OK);
		} catch (ApiException e) {
			return sendValidationError(e);

		} catch (Exception e) {
			return sendServerError(e, GlobalVariables.FAIL);
		}

	}

}
