package com.iana.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.iana.api.dao.GenericDAO;
import com.iana.api.domain.CommonFieldsSearch;
import com.iana.api.domain.FpToken;
import com.iana.api.domain.LabelValueForm;
import com.iana.api.domain.Pagination;
import com.iana.api.utils.APIReqErrors;
import com.iana.api.utils.ApiResponseMessage;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.Errors;
import com.iana.api.utils.GlobalVariables;

@Service
public class BaseRestService extends GenericDAO {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	public Environment env;

	public <T> ApiResponseMessage prepareAPIErrors(List<Errors> errors) {

		ApiResponseMessage response = null;
		APIReqErrors apiReqErrors = new APIReqErrors();
		apiReqErrors.setErrors(errors);
		response = new ApiResponseMessage(ApiResponseMessage.ERROR, null, apiReqErrors);
		return response;
	}

	protected String prepareForgotPwdEmailBody(FpToken fpToken) {
		
		String urlLink = env.getProperty("resetPwdLink") + "?q=" + fpToken.getKey();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		setButtonStyle(sb);
		sb.append("<head>");
		sb.append("<table border='0'>");
		sb.append("<tr><td><img src=\"cid:1\" height=\"57px;\" width=\"160px;\"/></td></tr>");
		sb.append("<tr><td><hr></td></tr>");
		sb.append("<tr><td>Hi&nbsp;");
		sb.append(CommonUtils.validateObject(fpToken.getFirstName()));
		sb.append("&nbsp;");
		sb.append(CommonUtils.validateObject(fpToken.getLastName()));
		sb.append("<br/>&nbsp;</td></tr>");
		sb.append("<tr><td>Kindly click on \"Reset Password\" button to change your password.<br/><br/>");
		sb.append("<div class=\"btn btn-primary\" >");
		sb.append("<a style=\"color:white;\" href=\"" + urlLink + "\"> Reset Password</a>");
		sb.append("</div>");
		sb.append("<br/>&nbsp;</td></tr>");
		sb.append("<tr><td>Thanks.<br/>IANA Administrator</td></tr>");
		sb.append("<tr><td><hr></td></tr>");
		sb.append("</table>");
		sb.append("</html>");
		return sb.toString();
	}

	private void setButtonStyle(StringBuilder sb) {
		sb.append("<style>");
		sb.append("a {");
		sb.append("text-decoration: none !important;");
		sb.append("}");
		sb.append(".btn-primary {");
		sb.append("color: #fff;");
		sb.append("text-shadow: 0 -1px 0 rgba(0,0,0,0.25);");
		sb.append("background-color: #006dcc;");
		sb.append("background-image: linear-gradient(to bottom,#08c,#04c);");
		sb.append("background-repeat: repeat-x;");
		sb.append("border-color: rgba(0,0,0,0.1) rgba(0,0,0,0.1) rgba(0,0,0,0.25);");
		sb.append("}");
		sb.append(".btn {");
		sb.append("display: inline-block;");
		sb.append("padding: 4px 12px;");
		sb.append("margin-bottom: 0;");
		sb.append("font-size: 14px;");
		sb.append("line-height: 20px;");
		sb.append("text-align: center;");
		sb.append("vertical-align: middle;");
		sb.append("cursor: pointer;");
		sb.append("border: 1px solid #ccc;");
		sb.append("border-radius: 4px;");
		sb.append("box-shadow: inset 0 1px 0 rgba(255,255,255,0.2), 0 1px 2px rgba(0,0,0,0.05);");
		sb.append("}");
		sb.append("</style>");
	}

	protected String prepareResetPwdEmailBody(String firstName, String lastName) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<head>");
		sb.append("<table border='0'>");
		sb.append("<tr><td><img src=\"cid:1\" height=\"57px;\" width=\"160px;\"/></td></tr>");
		sb.append("<tr><td><hr></td></tr>");
		sb.append("<tr><td>Hi&nbsp;");
		sb.append(CommonUtils.validateObject(firstName));
		sb.append("&nbsp;");
		sb.append(CommonUtils.validateObject(lastName));
		sb.append("<br/>&nbsp;<br/>");
		sb.append("Your UIIA password has been changed successfully.");
		sb.append("<br/>&nbsp;</td></tr>");
		sb.append("<tr><td>");
		sb.append("<strong> If you did this, </strong> you can safely disregard this email.");
		sb.append("<br/></td></tr>");
		sb.append("<tr><td>");
		sb.append("<strong> If you didn't this, </strong> please contact at IANA Info Service Desk.");
		sb.append("<br/><br/></td></tr>");
		sb.append("<tr><td><br/>Thanks.<br/>IANA Administrator</td></tr>");
		sb.append("<tr><td><hr></td></tr>");
		sb.append("</table>");
		sb.append("</html>");
		
		return sb.toString();
	}

	public void pageSetup(CommonFieldsSearch search, List<String> errorList, Pagination page, Long recordCount){
		if (search.getPageSize() <= 0) {
			search.setPageSize(Integer.valueOf(GlobalVariables.DEFAULT_TEN));
		}
		
		if (search.getPageIndex() <= 0) {
			search.setPageIndex(1);
		}
		
		if (recordCount != 0) {
			int totalPage = (int) Math.ceil((float) recordCount / Long.valueOf(search.getPageSize()));

			if (totalPage == 0 && Integer.valueOf(search.getPageIndex()).intValue() == 1) {
				search.setRecordFrom(0);

			} else {
				if (totalPage < search.getPageIndex()) {
//					errorList.add(env.getProperty("msg_error_change_search"));
					search.setRecordFrom(0);
					search.setPageIndex(1);
					
				} else if (search.getPageIndex() != 0) {
					search.setRecordFrom((search.getPageIndex() - 1) * search.getPageSize());

				}

				page.setTotalPages(totalPage);
				page.setTotalElements(recordCount.intValue());
				page.setCurrentPage(search.getPageIndex());
				page.setSize(search.getPageSize());
			}

		} else {
			errorList.add(env.getProperty("msg_error_no_records_found"));
		}
		
	}

	public List<LabelValueForm> epTemplates() throws Exception {
		
		List<LabelValueForm> epTemplatesList = new ArrayList<>();
 
		epTemplatesList.add(new LabelValueForm("Present",GlobalVariables.PRESENTTEMPLATE ));
		epTemplatesList.add(new LabelValueForm("Future", GlobalVariables.WHATIFTEMPLATE ));
		epTemplatesList.add(new LabelValueForm("Past", GlobalVariables.PASTTEMPLATE ));
		
		return epTemplatesList;
	}
	
	public List<LabelValueForm> epMcSuspensionNotif() throws Exception {
		
		List<LabelValueForm> epTemplatesList = new ArrayList<>();
 
		epTemplatesList.add(new LabelValueForm("Present",GlobalVariables.PRESENTTEMPLATE ));
		epTemplatesList.add(new LabelValueForm("Future", GlobalVariables.WHATIFTEMPLATE ));
		epTemplatesList.add(new LabelValueForm("Past", GlobalVariables.PASTTEMPLATE ));
		
		return epTemplatesList;
	}
	
	public List<LabelValueForm> populateUiiaStatusList() {
		List<LabelValueForm> uiiaStatus = new ArrayList<>();
			uiiaStatus.add(new LabelValueForm("Active", GlobalVariables.ACTIVE));
			uiiaStatus.add(new LabelValueForm("Cancelled", GlobalVariables.CANCELLEDMEMBER));
			uiiaStatus.add(new LabelValueForm("Deleted", GlobalVariables.DELETEDMEMBER));
			uiiaStatus.add(new LabelValueForm("Pending", GlobalVariables.PENDINGMEMBER));
		return uiiaStatus;
	}
	
	public List<LabelValueForm> populateEquipProviderTypeList() {
		List<LabelValueForm> uiiaStatus = new ArrayList<>();
		uiiaStatus.add(new LabelValueForm("IMC", GlobalVariables.IMC));
		uiiaStatus.add(new LabelValueForm("Leasing Company", GlobalVariables.LEASING_COMPANY));
		uiiaStatus.add(new LabelValueForm("Ocean Carrier", GlobalVariables.OCEAN_CARRIER));
		uiiaStatus.add(new LabelValueForm("Railroad", GlobalVariables.RAIL_ROAD));
		uiiaStatus.add(new LabelValueForm("Terminal", GlobalVariables.TERMINAL));
		return uiiaStatus;
	}
	
	public List<LabelValueForm> populateNamePrefixList() {
		List<LabelValueForm> namePrefixes = new ArrayList<>();
		
		namePrefixes.add(new LabelValueForm("--", StringUtils.EMPTY));
		namePrefixes.add(new LabelValueForm("Mr", "Mr"));
		namePrefixes.add(new LabelValueForm("Mrs", "Mrs"));
		namePrefixes.add(new LabelValueForm("Ms", "Ms"));
		return namePrefixes;
	}
	
	public List<LabelValueForm> populateServiceLevelList() {
		List<LabelValueForm> namePrefixes = new ArrayList<>();
		namePrefixes.add(new LabelValueForm("Level1", "Level1"));
		namePrefixes.add(new LabelValueForm("Level2", "Level2"));
		return namePrefixes;
	}
	
	public List<LabelValueForm> populateRequiredAndAllowed() {
		List<LabelValueForm> optionsRequiredAllowed = new ArrayList<>();
		optionsRequiredAllowed.add(new LabelValueForm("Yes", GlobalVariables.YES));
		optionsRequiredAllowed.add(new LabelValueForm("No", GlobalVariables.NO));
		
		return optionsRequiredAllowed;
	}

}
