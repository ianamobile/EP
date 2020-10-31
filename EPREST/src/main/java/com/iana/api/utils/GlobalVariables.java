package com.iana.api.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedCaseInsensitiveMap;

public class GlobalVariables {
	
	// REST URIs Start
	public static final String REST_URI_UIIA = "/rest/v1/UIIA/";
	
	public static final String JSON_REQ_DETAILS = "jsonReqDetails";

	public static final String YES="Y";
	public static final String NO="N";
	
	public static final String HASH = "#";
	public static final String PERCENTAGE = "%";
	public static final String COLON = ":";
	public static final String HYPHEN = "-";
	public static final String COMMA = ",";
	public static final String UNDERSCORE 	= "_";
	public static final String BLANK 		= "";
	public static final String SPACE        = " ";
	
	public static final String STR_YES = "Yes";
	public static final String STR_NO = "No";
	
	public static final String XLSX_WITH_DOT	 											= ".xlsx";
	public static final String XLSX	 															= "xlsx";
	public static final String DOT				 														= ".";
	public static final String MB                                                                       = "mb.";
	
	// API RESPONSE CODE
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFO = 3;
	public static final int OK = 4;
	public static final int TOO_BUSY = 5;
	public static final int UNAUTHO_ACCESS = 403;
	
	public static final int LICENSE_STATE_LENGTH = 2;
	
	public static final String OPEN = "OPEN";
	public static final String CLOSED = "CLOSED";
	
	// User Roles
	public static final String ROLE_EP= "EP";
	public static final String ROLE_UIIASTAFF = "UIIA";
	
	public static final List<String> ALLOWED_ROLES = Arrays.asList(ROLE_EP);
	
	public static final String Y = "Y";
	public static final String N = "N";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String FAIL = "FAIL";

	public static String EMAIL_TYPE_HTML = "HTML";

	public static final String HEADER_ACCEPT = "accept";

	public static final String DEFAULT_TEN = "10";
	
	public static final String INACTIVE_INIT = "Inactive";
	public static final String ACTIVE_INIT = "Active";
	public static final String NEW_INIT = "New";
	
	public static final String ASC = "ASC";
	public static final String DESC = "DESC";
	
	public static final String RESPONSE_MSG_200 = "Successfully completed API operation";
	public static final String RESPONSE_MSG_422 = "This code is returned by the server when Validation/Business Validation Fails";
	public static final String RESPONSE_MSG_500 = "Internal Server Error";
	public static final String CATEGORY_SETUP = "SETUP";
	
	public static final String CATEGORY_MC_LOOKUP = "MC LOOKUP";
	public static final String CATEGORY_EP_TEMPLATE = "EP TEMPLATE";
	
	public static final String URI_PATH_EP_MC_USDOT_STATUS_REPORTS = "epMcUsdotStatusReports/report";
	
	public static final String SECURITY_OBJECT = "securityObject";
	public static final String REQUEST_BODY_OBJ  = "body";
	
	public static final String SP_RESULT_KEY = "#result-set-1";
	
	public static final Map<String, String> sortDirectionMap;

			static {
			    Map<String, String> aMap = new LinkedCaseInsensitiveMap<>();
			    aMap.put("ASC", "ASC");
			    aMap.put("DESC", "DESC");
			    sortDirectionMap = Collections.unmodifiableMap(aMap);
			}; 

	public static final String FOR_RECORD				= " for record ";

	public static final String MEDIA_TYPE_OCTET_STREAM = "application/octet-stream";
	public static final String MEDIA_TYPE_PDF = "application/pdf";

	public static final String VALIDATE   = "validate";

	public static final String EXCEPTION	= "exception";
	
	public static final String MESSAGE	= "Message";
	
	public static final String ACTIVE = "ACTIVE";
	public static final String DELETED = "DELETED";
	public static final String CANCELLED = "CANCELLED";
	public static final String PENDING = "PENDING";
	//added by swati on 19/07/2006
	public static final String PENDINGTEMPLATES = "W";
	//added for approved name/scac changes
	public static final String APPROVEDSTATUS = "APPROVED";

	public static final String BILLADDRESSTYPE= "BILLING";
	public static final String CONTACTADDTYPE= "CONTACT";
	public static final String DISPUTEADDRESSTYPE= "DISPUTE";

	public static final List<String> YN_LIST = Arrays.asList(Y, N);

	public static final String OWNER = "0";
	public static final String GROUP = "607";

	public static final String  I_INSERT = "I";
	public static final String  R_REMOVE = "R";

	public static final String USERNAME_PATTERN				= "^[0-9a-zA-Z]+[0-9a-zA-Z_]*$";
	public static final String ALPHABET_PATTERN				= "^[A-Za-z]*$";
	public static final String NAME_PATTERN					= "^[0-9A-Za-z&amp;'&quot;(:)\\/,.\\- ]*$";
	public static final String PHONE_PATTERN				= "^\\(?(\\d{3})\\)?(\\d{3})[-]?(\\d{4})|([ ]?Ext[:]?[ ](\\d{5}))$";
	public static final String FAX_PATTERN					= "^\\(?(\\d{3})\\)?(\\d{3})[-]?(\\d{4})$";
	public static final String ALPHABET_NUMERIC_PATTERN		= "^[0-9a-zA-Z]*$";
	public static final String DATE_PATTERN					= "^(1[0-2]|0?[1-9])/(3[01]|[12][0-9]|0?[1-9])/(?:[0-9]{2})?[0-9]{2}$";
	public static final String ZIP_CODE_PATTERN				= "^[0-9a-zA-Z\\- ]*$";
	public static final String CITY_PATTERN					= "^[0-9A-Za-z&amp;\\-.,' ]*$";
	public static final String ALPHABET_WITH_SPACE_PATTERN	= "^[A-Za-z]+[A-Za-z ]*$";
	public static final String MIDDLE_NAME_PATTERN			= "^[0-9A-Za-z&amp;'&quot;(:)\\/,.\\- ]*$";

	public static final String IDDUSER = "IDD";

	public static final List<String> PREFIXS = Arrays.asList(new String[]{"Mr", "Mrs", "Ms"});

	public static final String NOTFNSNTCOUNT="1";	
	
	public static final String NOTFN_CUSTOM_MANUAL = "MANUAL";
	public static final String NOTFN_CUSTOM_STANDARD = "STANDARD";
	public static final String NOTFN_EXCEPTION = "EXCEPTION";
	
	/* Notification Modes --ashok 23/06/2006*/
	public static final String NOTFNMAIL = "MAIL";
	public static final String NOTFNFAX = "FAX";
	public static final String NOTFNPRINT = "PRINT";
	public static final String NOTFNPRINT_NP = "NOT PRINTED";
	public static final String NOTFNPRINT_P = "PRINTED";
	
	/* Status for Notification*/
	public static final String NOTFNPENDING = "PENDING";
	public static final String NOTFNSUCCESS = "SUCCESS";
	public static final String NOTFNFAILURE = "FAILURE";
	
	public static final String NAMECH = "NAMECHANGE";
	public static final String PASTCERTIFICATE = "PAST";
	public static final String OVERWRITTEN = "OVERWRITTEN";
	public static final String LAPSED = "EXPIRED";	

	public static final String CERTI_STATUS_SAVED = "SAVED";
	public static final String CERTI_STATUS_SUBMITTED = "SUBMITTED";
	public static final String CERTI_STATUS_PENDING = PENDING;
	public static final String CERTI_STATUS_ACTIVE = ACTIVE;
	public static final String CERTI_STATUS_NAMECHANGE = NAMECH;
	public static final String CERTI_STATUS_PAST = PASTCERTIFICATE;
	
	public static final List<String> CERTI_STATUS = Arrays.asList(CERTI_STATUS_SAVED, CERTI_STATUS_SUBMITTED, CERTI_STATUS_PENDING, CERTI_STATUS_ACTIVE, CERTI_STATUS_NAMECHANGE, CERTI_STATUS_PAST);

	/*  added for showing various policy types --Saumil 14/06/19 */
	public static final String AUTOPOLICY="AL";
	public static final String GENPOLICY="GL";
	public static final String CARGOPOLICY="CARGO";
	public static final String TRAILERPOLICY="TI";
	public static final String EMPLIABILITY="EL";
	public static final String WORKCOMP="WC";
	public static final String UMBRELLA="UMB";
	public static final String CONTCARGO="CONTCARGO";
	public static final String REFTRAILER="REFTRAILER";
	public static final String EMPDISHBOND="EMPDHBOND";
	
	//swati---20/10
	public static final String AL = "Auto Liability";
	public static final String GL = "General Liability";
	public static final String CL = "Cargo";
	public static final String CC = "Contingent Cargo";
	public static final String TL = "Trailer Interchange";
	public static final String RTL = "Refrigerated Trailer Liability";
	public static final String WC = "Worker's Compensation";
	public static final String EL = "Employer's Liability";
	public static final String EMPDISH = "Employer's Dishonesty Bond";
	public static final String UMB = "Umbrella";
	
	/*added by Ashok -- 02-Aug-06*/
	public static final String UIIA_EP="UIIAEP";
	public static final int ROLE_EP_ID = 2;
	
	//for EP Annual Invoice Breakdown
	public static final String DATECHECK="2012-06-06";
	public static final String UIIA_TEMPLATE="UIIA";
	public static final String IANA_TEMPLATE="IANA";
	
	/*UIIA member status*/
	
	public static final String ACTIVEMEMBER = "ACTIVE";
	public static final String DELETEDMEMBER = "DELETED";
	public static final String CANCELLEDMEMBER = "CANCELLED";
	public static final String PENDINGMEMBER = "PENDING";

	public static final String EQUIPMENTPROVIDER= "EP";
	public static final String MOTORCARRIER = "MC";
	
	public static final String NOTFN_LETTERHEAD_UIIA = "UIIA";
	public static final String NOTFN_LETTERHEAD_IANA = "IANA";
	public static final String NOTFN_LETTERHEAD_IDD = "IDD";

	public static final String NUMERIC_PATTERN = "^[0-9]*$";

	//warning message to be shown to the user in case of saving a new acord certificate or a saved certificate
	public static String REASONS =  "The following items are pending for verification or found to be Not OK:";

	public static final String SINGLE_QUOTE = "'";
	public static final String APOSTROPHE = "%27";

	public static final String ROLE_IDD_SEC = "IDD_SEC";
	public static final String ROLE_SUB_SEC ="SU_SEC";	

	//billing type
	public static final String ANNUAL_BILLING_TYPE = "ANNUALLY";
	
	public static final String STATUTORY="STATUTORY";
	public static final String PRIMARYPOLICY = "PRIMARY";
	public static final String EXCESSPOLICY = "EXCESS";
	public static final String EPSPECIFICPOLICY = "EPSPECIFIC";
	
	/*Differenct Currencies --ashok 26/06/2006*/
	public static final String CURRUSD="USD";
	public static final String CURRCANADIAN="CND";
	public static final String CURRMEXICAN="MEX";
	/*Standard Endorsements for Auto Liability --ashok 26/06/2006*/
	public static final String ENDOUIIE1="UIIE-1";
	public static final String ENDOCA2317="CA23-17";
	public static final String ENDOTE2317B ="TE23-17B ";

	
	/*Account Details --ashok 17/07/2006*/
	public static final String ACCTDTLS="ACCTDTLS"; /* added for U Need U Have*/
	
	/*added by Ashok 11/07/2006 for Uneed Uhave*/
	public static final String MCEPJOINPRBLMS="MCEPPRBLM";
	public static final String MCEP_AREQ_PRBLMS="MCEPAREQPRBLMS";
    public static final String MCEP_OTHR_PRBLSM="OTHRPRBLMS";
	public static final String ADDLNREQ="ADDTNLREQ";
	public static final String RAMP="RAMP";
	public static final String MEM_SPECIFIC_CARRIER="MEM SPC CAREER";
	public static final String CANCELLED_BY_EP="CANCELLEDBYEP";
	public static final String PEND_REQ_EP="PENDINGREQEP";
	/*added by ashok for UValid and UNeed You Have --11/07/2006*/
	public static final String UVLD_SELF_INSRD_PRBLM="SELFINSRD";
	public static final String UVLD_ADDLN_INSRD_PRBLM="ADDLNINSRD";
	public static final String UVLD_LIMITS_PRBLM="LIMITS";
	public static final String UVLD_DED_PRBLM="DEDUCTIBLES";
	public static final String UVLD_RRG_PRBLM="RRG";
	public static final String UVLD_SPL_INS_PRBLM="SPLINSRN";
	public static final String UVLD_EP_LIMITS_PRBLM="EPLIMITS";
	public static final String UVLD_BLNKT_PRBLM="BLANKET";
	public static final String UVLD_AL_STDENDO_PRBLM="ALSTDENDO";
	public static final String UVLD_AL_SCDHRD_PRBLM="ALSCHDHRD";
	public static final String UNUHINSKEY="INSDETAILS"; /* U Need You have Insurance Details*/
	public static final String UVLD_POL_TRMNTD="TERMINATED";
	public static final String UVLD_INS_KEY="INS DTLS";
	public static final String UVLD_NO_ADDLN_REQ_PRBLM="NOADDLNREQMC";
	public static final String UVLD_WC_EXEMPTION="WCEXEMP";
	//Temp Key used in Uvalid to put HashMap of problems
	public static final String TEMP_KEY_UIIEP="TMPUIIAEP";
	public static final String MC_VLDWITH_ONE_EP="MCVLDONEEP";
	public static final String MC_HAS_ALGL="MCHASALGL";
	public static final String MC_HAS_EXPAL="MCHASEXPAL";
	public static final String MC_HAS_EXPGL="MCHASEXPGL";
	public static final String MC_HAS_NOPOLICY="MCNOPOLICY";
	public static final String TEMPLATE_LIST="TEMPLATEIDS";
	public static final String EP_GEN_DTLS="EPBSCDTLS";
	
	/*added for showing the EP template status for fetching----swati 09/06*/
	public static final String PRESENTTEMPLATE = "ACTIVE";
	public static final String PASTTEMPLATE = "PREVIOUS";
	public static final String WHATIFTEMPLATE = "FUTURE";

	public static final String ONLINE_CREDITAPP_EPS ="EP065585,UP,EP093573,NSCU,EP200003,CSXU,EP071443,IC,EP069813,CXXX,EP200013,KCS";
	public static final String ONLINE_ADD_EPS ="EP065585,UP,EP106554,TIPL,EP093573,NSCU,EP158474,EGLV,EP200003,CSXU,EP071443,IC,EP053653,BNAU,EP069813,CXXX,EP200013,KCS";
	
	public static final String NON_UIIA_EP = "NON UIIA EP";
	/* added for Zero Deductible Allowed --353630 07/09/12 */
	public static final String ZERODEDUCTIBLE = "N/A";

	public static final String PAYMODE_CHEQUE = "paymodechq";
	public static final String PAYMODE_ONLINE = "paymodeonline";

	public static final String NSCHGLIST = "NSCHGLIST";
	public static final String UIIAVERFITEMS = "UIIAVERFITEMS";

	public static final String ADDENDAVERFITEMS = "ADDENDAVERFITEMS";
	
	public static final String FLAG_REPORT = "report";
	
	public static final String CATEGORY_LIST_OF_DELETED_MCS = "LIST OF DELETED MCS";

	   public static final String TRAC_ACCOUNT_NO ="EP200025";
	//   public static final String TRAC_ACCOUNT_NO ="EP200036";
	   //public static final String TRAC_ACCOUNT_NO ="EP200031";

	public static final String APPRVD_STATUS="APPROVED";
	public static final String NOT_APPRVD_STATUS="NOT APPROVED";
	   
	 //Equipment provider types
	public static final String IMC="IMC";
	public static final String LEASING_COMPANY="Leasing Company";
	public static final String OCEAN_CARRIER="Ocean Carrier";
	public static final String RAIL_ROAD="Railroad";
	public static final String TERMINAL="Terminal";
	
	public static final String ROLE_IDD_MC= "IDD_MC";
	public static final String ROLE_NON_UIIA_MC = "NON_UIIA_MC";
	
	   
}