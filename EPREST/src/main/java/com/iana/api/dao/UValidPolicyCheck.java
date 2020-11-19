/*
 *  File		: UValidPolicyCheck.java
 *  Author		: Ashok Soni
 *  Created		: June 24,2006
 *  Description	: Business class  which will have check for Individual Policies
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */
 

/**
 * @author 146877
 * Modification details:
 * 16July07::By Piyush :: As per new specs of Waiver.Don't add limit booster with the Limit that MC has.
 * Compare MC limit with the new Limit provided while overriding for a specific EP.
 * Changes reflected for all policies.
 */
package com.iana.api.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.AdditionalReq;
import com.iana.api.domain.Currency;
import com.iana.api.domain.EPInsOvrWrapper;
import com.iana.api.domain.MultipleLimit;
import com.iana.api.domain.acord.AutoBean;
import com.iana.api.domain.acord.CargoBean;
import com.iana.api.domain.acord.ContCargoBean;
import com.iana.api.domain.acord.ELBean;
import com.iana.api.domain.acord.EmpDishBean;
import com.iana.api.domain.acord.GenBean;
import com.iana.api.domain.acord.RefTrailerBean;
import com.iana.api.domain.acord.TrailerBean;
import com.iana.api.domain.acord.UVldMemBean;
import com.iana.api.domain.acord.UmbBean;
import com.iana.api.domain.acord.WCBean;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Service
public class UValidPolicyCheck 
{
	Logger log = LogManager.getLogger(this.getClass().getName());
	Date paramDate=null;
	double cndUSD=0.0;
	double mexUSD=0.0;
	@Autowired
	UValidDao uValidDao;
	
	@Autowired
	EpDao epDao;
	
	

	//constructor which will set the date;
	public UValidPolicyCheck(Date pDate,List<Currency> arlCurr)
	{
		 this.paramDate=pDate;
		 if(arlCurr!=null)
		 {
			 for(int i=0;i<arlCurr.size();i++)
			 {
				 Currency currBean=(Currency)arlCurr.get(i);
				 if(currBean!=null)
				 {
					 if(GlobalVariables.CURRCANADIAN.equals(currBean.getFromCurrency()))
					 {
						 cndUSD=currBean.getRate();
					 }
					 else if(GlobalVariables.CURRMEXICAN.equals(currBean.getFromCurrency()))
					 {
						 mexUSD=currBean.getRate();
					 }
				 }
			 }
		 }
	}
	
	public void setUValidPolicyCheck(Date pDate,List<Currency> arlCurr)
	{
		 this.paramDate=pDate;
		 if(arlCurr!=null)
		 {
			 for(int i=0;i<arlCurr.size();i++)
			 {
				 Currency currBean=(Currency)arlCurr.get(i);
				 if(currBean!=null)
				 {
					 if(GlobalVariables.CURRCANADIAN.equals(currBean.getFromCurrency()))
					 {
						 cndUSD=currBean.getRate();
					 }
					 else if(GlobalVariables.CURRMEXICAN.equals(currBean.getFromCurrency()))
					 {
						 mexUSD=currBean.getRate();
					 }
				 }
			 }
		 }
	}
	
	public UValidPolicyCheck()
	{}
	
	
	/**
	 * This method will check Auto Liability Policy requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkAutoLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		log.info("Business: Entering method checkAutoLiability()of UValidPolicyCheck class");
		log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info(" Mexican conversion "+mexUSD);
		ArrayList arlALStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  StringBuffer sbALPrimary= new StringBuffer(GlobalVariables.AUTOPOLICY);
			  sbALPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbALExcess= new StringBuffer(GlobalVariables.AUTOPOLICY);
			  sbALExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.AUTOPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  AutoBean primAutoMC=(AutoBean)mcInsDtls.get(sbALPrimary.toString());
			  AutoBean excessAutoMC=(AutoBean)mcInsDtls.get(sbALExcess.toString());
			  AutoBean epSpecAutoMC=null;
				  //(AutoBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arAuto = new ArrayList();
				  arAuto = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arAuto.size();x++)
				  {
					  epSpecAutoMC=(AutoBean)arAuto.get(x);
					  if(epSpecAutoMC.getPolicyMstId()==0)
					  {
						  //epSpecAutoMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecAutoMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecAutoMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecAutoMC=null;
					  }
				  }
			  }
			  
			  UmbBean umbAutoMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbAutoMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbAutoMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }
			  double dMCHasAutoLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dExAutoLimit=0.0;
			  double dEXAutoDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double autoPrmMCLimits =0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /*Uday added end*/
			  /* to decide whether to check EP Specific or not..
	 		   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
	 		   *  in Primary and then it will be set to true 
	 		   */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.after checking override also
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.AUTOPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have AL");
				  return arlALStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <=0 ");
				  //arlALStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  log.debug("Exiting AutoLiability Check as EP Limits <=0");
				  return arlALStatus;
			  }

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires AL.. checking if MC has AL either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbALPrimary.toString()) || mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlALStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlALStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires AL.. so skipping Policy check ");
				  return arlALStatus;
			  }
			  
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }			  
			  
			  if(primAutoMC!=null)
			  {
					  //log.debug("Entering Primary Policy Check for Auto Liability");
					  //===============Added for All Owned 29March'10===================
					  autoPrmMCLimits=Utility.commaStringtoDouble(primAutoMC.getLimit())+Utility.commaStringtoDouble(primAutoMC.getBdlyInjrdPerAccdnt())+Utility.commaStringtoDouble(primAutoMC.getPropDmgPerAccdnt());
					  if(primAutoMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  autoPrmMCLimits=autoPrmMCLimits*cndUSD;
					  }
					  else if(primAutoMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  autoPrmMCLimits=autoPrmMCLimits*mexUSD;
					  }
					  //===========End added all owned==================================
					  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
					  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						//log.debug("if policy expiration date is today's date");
						arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlALStatus;
					  }
					  if(Utility.stringToSqlDate(primAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						  	//log.debug("if policy expiration date is today's date");
							arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlALStatus;
					  }
					  //end----02/03
					  
					  /*Start Std Endo Check*/
					 /*
					 * TO CHECK FOR STD ENDORSEMENT UIIE/CA-23/TE23-17 */ 
					  //log.debug("Checking attributes for Primary Policy:- "+primAutoMC.getStdEndo());
					  	log.info("epDtls.getEpAcctNo() :"+epDtls.getEpAcctNo());
						 if(!((primAutoMC.getStdEndo().equals(GlobalVariables.ENDOUIIE1))||(primAutoMC.getStdEndo().equals(GlobalVariables.ENDOCA2317))||(primAutoMC.getStdEndo().equals(GlobalVariables.ENDOTE2317B))))
						 {
							if(!GlobalVariables.TRAC_ACCOUNT_NO.equalsIgnoreCase(epDtls.getEpAcctNo())){
								arlALStatus.add(GlobalVariables.UVLD_AL_STDENDO_PRBLM);
							}else{
								log.info("It's TRAC baby so just chill!");
							}
						 }
						 /*End Std Endo Check*/
						 /*Start Check Scheduled Hired and Auto*/
						 if((primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getScheduled().equals(GlobalVariables.YES))) || (primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getAllOwned().equals(GlobalVariables.YES))) || primAutoMC.getAny().equals(GlobalVariables.YES))
						 {
							 	// Scheduled Hired All owned and Auto Ok for Primary (NO ERROR)
						 }
						 else
						 {
							  //log.debug("Scheduled Hired Any Problem: AL Primary: Check Any,Scheduled,Hired,All owned");
							  arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
						 } 
						 						 
					 /*if(autoPrmMCLimits<dEPLimit && (primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getAllOwned().equals(GlobalVariables.YES))))
					 {
						 arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
					 }*/

					 /*Start Check Policy Status (Termination|Cancellation) */
					  ArrayList arr = new ArrayList();
					  arr = getTmpTermDt(primAutoMC.getPolicyMstId());
					  Date tmpTerm = null;
					  Date tmpRein = null;
					  Date curTerm = null;
					  Date curReins = null;
					  if(!arr.isEmpty())
					  {
						  tmpTerm = (Date)arr.get(0);
						  tmpRein = (Date)arr.get(1);
					  }
					  //=========By Piyush 14Mar'09===============================================================
					  if(primAutoMC.getPolicyTerminatedDate().length()>0)
					  {
						  curTerm = Utility.stringToSqlDate(primAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4);
						  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
						  {
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
			  		  else if(primAutoMC.getPolicyReinstatedDate().length()>0)
					  {
			  			  curReins = Utility.stringToSqlDate(primAutoMC.getPolicyReinstatedDate(),Utility.FORMAT4);
						  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
						  else if(curReins.after(paramDate))
						  {
							  if(tmpTerm!=null && tmpTerm.equals(paramDate))
							  {
								  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
					  }						  
					  //=========End By Piyush 14Mar===============================================================					  
					/* End Termination */
					  
					 /*Start Self Insured check*/
					 if(GlobalVariables.YES.equals(primAutoMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlALStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(primAutoMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting AutoLiability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 //=============Added by piyush as per talk with debbie on 11Aug2008=======================						 
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
						  //=============End Added by piyush =================================================================
						  return arlALStatus;//Commented by piyush = As smart checklist doesnt show all possible problems when mc has no policy
					 }
					 /*End Self Insured check*/
					 
										 
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primAutoMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlALStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					  
					 /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {							  
							  /*check if MC has Blanket or Additional Insured Flag is Yes
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()) || GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }*/
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }						  
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  /**----------------------------------------------------------**/
					  /*End Additional Insured check*/
					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasAutoLimit=Utility.commaStringtoDouble(primAutoMC.getLimit())+Utility.commaStringtoDouble(primAutoMC.getBdlyInjrdPerAccdnt())+Utility.commaStringtoDouble(primAutoMC.getPropDmgPerAccdnt());
					  dMCHasDeduct=Utility.commaStringtoDouble(primAutoMC.getDeductible());
					  if(excessAutoMC!=null)
					  {
						  dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit());
						  dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible());
					  }
					  
					  if(umbAutoMC!=null && umbAutoMC.getALReqd().equals(GlobalVariables.YES))
						  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit());
					  
					  if(primAutoMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  dMCHasAutoLimit=dMCHasAutoLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessAutoMC!=null)
							  {
							  	dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*cndUSD ;
							  	dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*cndUSD;
							  }
						  if(umbAutoMC!=null && umbAutoMC.getALReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*cndUSD ;
						  //log.info("Currency conversion dMCHasAutoLimit (CND $) "+dMCHasAutoLimit);
						  //log.info("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.info("Currency conversion dExAutoLimit 	(CND $) "+dExAutoLimit);
						  //log.info("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  else if(primAutoMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  dMCHasAutoLimit=dMCHasAutoLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessAutoMC!=null)
							  {
							  	dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*mexUSD ;
							  	dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*mexUSD ;
							  }
						  if(umbAutoMC!=null && umbAutoMC.getALReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*mexUSD ;
						  //log.info("Currency conversion dMCHasAutoLimit (MEX $) "+dMCHasAutoLimit);
					  //log.info("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
					  //log.info("Currency conversion dExAutoLimit    (MEX $) "+dExAutoLimit);
					  //log.info("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessAutoMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessAutoMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessAutoMC.getPolicyTerminatedDate().length()==0) || paramDate.after(Utility.stringToSqlDate(excessAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasAutoLimit=dMCHasAutoLimit+dExAutoLimit;
							  //dMCHasDeduct=dMCHasDeduct+dEXAutoDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exist  add to MC Limit*/
					  if(umbAutoMC!=null && GlobalVariables.YES.equals(umbAutoMC.getALReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbAutoMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							   //log.debug("Umbrella policy termination date less than equal to  parameter date :"+dMCUmbrella);
							   dMCHasAutoLimit=dMCHasAutoLimit+dMCUmbrella;
						  }
					  }
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasAutoLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits and deductibles without booster. If it fails then to set Override used flag
					      * as true*/
					     //log.debug("Limits|Deductibles override used");
						 bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note 16July07
					  //dMCHasAutoLimit=dMCHasAutoLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasAutoLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //Commented by piyush as per note 16July07
					  //if(dMCHasAutoLimit<dEPLimit)
					  if((dMCHasAutoLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasAutoLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg || bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasAutoLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  
					  /* Check Umbrella limits,EP Booster/Waiver and EP multiple limits*/
					  	 if(bLimitNotOkFlg && (primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getAllOwned().equals(GlobalVariables.YES))))
						 {
							 //arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
						 }
					  /* Above condition added by Piyush on 19Oct'10 - End of AllOwned & Hired with Umbrella Policy*/
					 /*********************************************************************************/
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  	ArrayList activeEpspcDtls = new ArrayList();
					  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primAutoMC.getMcAcctNo(),GlobalVariables.AUTOPOLICY);
					  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
					  	{					  		
					  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
					  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
					  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
					  	}
					  	
					  	/*Uday modification 24 May 12 start*/
					  	/* The pending EP SPEC policy also needs to be checked. 
					  	 * This will only be checked when the policy effective date is greater then current date*/
					  	
					  	if (!primAutoMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate())))
					  	{
					  		ArrayList pendingEpspcDtls = new ArrayList();
					  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
					  				.getEpAcctNo(),
					  				primAutoMC.getMcAcctNo(),
					  				GlobalVariables.AUTOPOLICY);
					  		if (pendingEpspcDtls != null
					  				&& pendingEpspcDtls.size() > 3) {
					  			dEpSpcPenLim = (Double) pendingEpspcDtls
					  			.get(0);
					  			dEpSpcPenDed = (Double) pendingEpspcDtls
					  			.get(1);
					  			IsPenEpSpcExist = (String) pendingEpspcDtls
					  			.get(2);
					  			strPenEpSpcEffDate = (String) pendingEpspcDtls
					  			.get(3);
					  		}
					  	}
					  	if(bLimitNotOkFlg)
					  	{
							  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
							  {}
							  else /* Uday modification 24 May 12 */
							  {
								  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
										  && (!primAutoMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4))))
										  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
								  {
									  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
									  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
									  //log.debug("dEPLimit - "+dEPLimit);
								  }
								  else
								  {
									  arlALStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
								  }
							  }
						 }
					  	 else
					  	 {
					  		 if(excessAutoMC!=null)
					  		 {
						  		 if(excessAutoMC.getHired().equals(GlobalVariables.YES) && (excessAutoMC.getAllOwned().equals(GlobalVariables.YES)))
								 {		
						  			 
						  			arlALStatus.remove(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
								 }
					  		 }
					  	 }
						  if(bDedNotOkFlg)
						  {
							  //log.debug("Deductible Problem.. so to check with Overriden value");
							  //arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);							  
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27Mar07
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
													  && (!primAutoMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4))))
													  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
										  		
										  	}
										  	else{
										  		arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  	}
									  }
								  }
							  }
							  else
							  {
								  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
								  {}
								  else /* Uday modification 24 May 12 */
								  {
									  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
												  && (!primAutoMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4))))
												  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
									  		
									  	}
									  	else{
									  		arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  	}
								  }
							  }
						  }
					  if(arlALStatus.size()==0)
					  {
						  //log.debug("No Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
					//log.debug("Exiting Primary Policy Check for Auto Liability");
			  }//end of primAutoMC !=null
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlALStatus is not empty  as it has problems*/
			  if(!bEPPolCheck && epSpecAutoMC!=null)
			  {
				  //log.info("Entering EP Specific Policy Check for Auto Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlALStatus.removeAll(arlALStatus);
				  
				  /*Repeating EP Specific same as that in Primary*/
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Auto Liability */
				    //strEPSpcUsed=GlobalVariables.AUTOPOLICY; 	
				    
				    //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
					  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						//log.debug("if policy expiration date is today's date");
						arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlALStatus;
					  }
					  if(Utility.stringToSqlDate(epSpecAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						  //log.debug("if policy expiration date is today's date");
							arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlALStatus;
					  }
					  //end----02/03
				    
				  	/*Start Standard Endo Check*/
					/* TO CHECK FOR STD ENDORSEMENT UIIE/CA-23/TE23-17 */
				     //if(epDtls.getEpAcctNo().equals(GlobalVariables.UIIA_EP))
				 	 //log.debug("EP is UIIA EP so checking Truckers Endorsment and Scheuded Hired");
			    	 if(!((epSpecAutoMC.getStdEndo().equals(GlobalVariables.ENDOUIIE1))||(epSpecAutoMC.getStdEndo().equals(GlobalVariables.ENDOCA2317))||(epSpecAutoMC.getStdEndo().equals(GlobalVariables.ENDOTE2317B))))
					 {
			    		 if(!GlobalVariables.TRAC_ACCOUNT_NO.equalsIgnoreCase(epDtls.getEpAcctNo()))
			    			 arlALStatus.add(GlobalVariables.UVLD_AL_STDENDO_PRBLM);
					 }
					 /*End Std Endo Check*/
					 /*Start Check Scheduled Hired and Auto*/
					 if(epSpecAutoMC.getAny().equals(GlobalVariables.YES) || (epSpecAutoMC.getHired().equals(GlobalVariables.YES) && (epSpecAutoMC.getScheduled().equals(GlobalVariables.YES))) || (epSpecAutoMC.getHired().equals(GlobalVariables.YES) && (epSpecAutoMC.getAllOwned().equals(GlobalVariables.YES))))
					 {
							  	//Scheduled Hired and Auto Ok for Primary (NO ERROR)
					 }
				     else
					 {
						  //log.info("Entering error for EP Specific: Check Any,Scheduled,Hired,All Owned");
						  arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
					 } 
				     /*End Standard Endo Check*/

					 /*Start Check Policy Status (Termination|Cancellation) */
					  ArrayList arr = new ArrayList();
					  arr = getTmpTermDt(epSpecAutoMC.getPolicyMstId());
					  Date tmpTerm = null;
					  Date tmpRein = null;
					  Date curTerm = null;
					  Date curReins = null;
					  if(!arr.isEmpty())
					  {
						  tmpTerm = (Date)arr.get(0);
						  tmpRein = (Date)arr.get(1);
					  }
					  //=========By Piyush 14Mar'09===============================================================
					  if(epSpecAutoMC.getPolicyTerminatedDate().length()>0)
					  {
						  curTerm = Utility.stringToSqlDate(epSpecAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4);
						  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
						  {
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
			  		  else if(epSpecAutoMC.getPolicyReinstatedDate().length()>0)
					  {
			  			  curReins = Utility.stringToSqlDate(epSpecAutoMC.getPolicyReinstatedDate(),Utility.FORMAT4);
						  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
						  else if(curReins.after(paramDate))
						  {
							  if(tmpTerm!=null && tmpTerm.equals(paramDate))
							  {
								  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
					  }						  
					  //=========End By Piyush 14Mar===============================================================					  


					/*Start Check Policy Status (Termination|Cancellation) */
					/* if(epSpecAutoMC.getPolicyTerminatedDate().length()>0)
					 {
						  //log.debug("EP Specific Policy Terminated Date:- "+epSpecAutoMC.getPolicyTerminatedDate());
						  if(Utility.stringToSqlDate(epSpecAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) ||Utility.stringToSqlDate(epSpecAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
						  {
							  //log.debug("EP Specific Policy Termination terminated on the param date ");
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  } else if(epSpecAutoMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecAutoMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecAutoMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
					 
				     /*Start Self Insured check*/
				     if(GlobalVariables.YES.equals(epSpecAutoMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							/*Check if EP doesn't allow SI*/
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlALStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecAutoMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting AutoLiability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlALStatus;
					 }
					 /*End Self Insured check*/
				     
				     
				     

					 /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					 
					  if(GlobalVariables.YES.equals(epSpecAutoMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlALStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					  /*END RRG and EP doesn't allow RRG*/
					
					 /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  /*check if MC has Blanket or Additional Insured Flag is Yes*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  /*if(GlobalVariables.YES.equals(epSpecAutoMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecAutoMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }*/
							  if(GlobalVariables.YES.equals(epSpecAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(epSpecAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.info("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(epSpecAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(epSpecAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");
									 
								  }
								  else
								  {
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  /**----------------------------------------------------------**/
					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasAutoLimit=Utility.commaStringtoDouble(epSpecAutoMC.getLimit())+Utility.commaStringtoDouble(epSpecAutoMC.getBdlyInjrdPerAccdnt())+Utility.commaStringtoDouble(epSpecAutoMC.getPropDmgPerAccdnt());
					  dMCHasDeduct=Utility.commaStringtoDouble(epSpecAutoMC.getDeductible());
					  
					  if(excessAutoMC!=null)
						  {
						  	dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit());
						  	dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible());
						  }
					  if(umbAutoMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit());
					  if(epSpecAutoMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasAutoLimit=dMCHasAutoLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessAutoMC!=null)
						  {
							  dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*cndUSD ;
							  dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*cndUSD;
						  }
						  if(umbAutoMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*cndUSD ;
						  //log.debug("Currency conversion dMCHasAutoLimit (CND $) "+dMCHasAutoLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExAutoLimit 	(CND $) "+dExAutoLimit);
						  //log.debug("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  if(epSpecAutoMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasAutoLimit=dMCHasAutoLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessAutoMC!=null)
						  {
							  dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*mexUSD ;
							  dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*mexUSD;
						  }
						  if(umbAutoMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*mexUSD ;
						  //log.info("Currency conversion dMCHasAutoLimit (MEX $) "+dMCHasAutoLimit);
						  //log.info("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
						  //log.info("Currency conversion dExAutoLimit    (MEX $) "+dExAutoLimit);
						  //log.info("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessAutoMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessAutoMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessAutoMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasAutoLimit=dMCHasAutoLimit+dExAutoLimit;
							  //dMCHasDeduct=dMCHasDeduct+dEXAutoDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbAutoMC!=null && GlobalVariables.YES.equals(umbAutoMC.getALReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbAutoMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  //log.debug("Umbrella policy termination date less than equal to  parameter date ");
							   dMCHasAutoLimit=dMCHasAutoLimit+dMCUmbrella;
						  }
					  }//end of if umbAutoMC
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if(dMCHasAutoLimit<dEPLimit)
					  {
						  /* checking limits without booster. If it fails then to set Override used flag
						   * as true*/
						  //log.debug("Override used for Limits");
						  bOvrUsed=true;
					  }
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  /*  checking deductibles without booster. If it fails then to set Override used flag
						   * as true*/
						  //log.debug("Override used for Deductibles");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note on 16July07
					  //dMCHasAutoLimit=dMCHasAutoLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasAutoLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //Commented by piyush as per note on 16July07
					  //if(dMCHasAutoLimit<dEPLimit)
					  if((dMCHasAutoLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasAutoLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit not Ok before Pol Multiple Limits Check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasAutoLimit +"and EP Ded:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Dedcutibles not Ok before Pol Multiple Dedcutibles Check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  	
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasAutoLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  if(bLimitNotOkFlg)
					  {
						  
						  arlALStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  //arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  if(dMCDedBooster>dEPDeduct)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					 /*********************************************************************************/
				  //log.debug("Exiting EP Specific Policy Check for Auto Liability");
			  }//end of else if
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}//end of Try
		catch(Exception exp)
		{
			log.error("Caught Exception in Auto Liability Check for UValid:- ", exp);
		}//end of Catch
		
		log.info("Business: Exiting method checkAutoLiability() with Problems:- "+arlALStatus);
		//log.info("Returning :== Override used flag:- "+strOvrUsed + ":== and EP Specific Flag :-"+ strEPSpcUsed);
		return arlALStatus;
	}
	/**
	 * This method will check General Liability Policy requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkGeneralLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkGeneralLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlGLStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  StringBuffer sbGLPrimary= new StringBuffer(GlobalVariables.GENPOLICY);
			  sbGLPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbGLExcess= new StringBuffer(GlobalVariables.GENPOLICY);
			  sbGLExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.GENPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  GenBean primGenMC=(GenBean)mcInsDtls.get(sbGLPrimary.toString());
			  GenBean excessGenMC=(GenBean)mcInsDtls.get(sbGLExcess.toString());
			  GenBean epSpecGenMC=null;
				  //(GenBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arGen = new ArrayList();
				  arGen = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arGen.size();x++)
				  {
					  epSpecGenMC=(GenBean)arGen.get(x);
					  if(epSpecGenMC.getPolicyMstId()==0)
					  {
						  //epSpecGenMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }else if(uValidDao.chkEPSpc(epSpecGenMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecGenMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecGenMC=null;
					  }
				  }
			  }
			  UmbBean umbGenMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }
			  /*
			  String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasGenLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExGenLimit=0.0;
			  double dExGenDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /*Uday added end*/
			  /* to decide whether to check EP Specific or not..
	 		   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
	 		   *  in Primary and then it will be set to true 
	 		   */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.GENPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have policy ");
				  return arlGLStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlGLStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlGLStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlGLStatus;
			  }
			  //======================End added by Piyush ==========================

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires GL.. checking if MC has either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbGLPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  //arlGLStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlGLStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires GL.. so skipping Policy check ");
				  return arlGLStatus;
			  }
			  
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
				  //log.debug("dEPLimit :"+dEPLimit+" dEPDeduct:"+dEPDeduct);
			  }			  
			  if(primGenMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for General Liability");
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlGLStatus;
						  }
						  if(Utility.stringToSqlDate(primGenMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  //log.debug("if policy expiration date is today's date");
							  arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							  return arlGLStatus;
						  }
						  //end----02/03
						
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primGenMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primGenMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primGenMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primGenMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primGenMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						/* End Termination */
				       
					   /*Start Self Insured check*/
				       if(GlobalVariables.YES.equals(primGenMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 	{
					  		 /*MC has SI and EP doesn't allow Self Insured*/
					  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
					  		 /*Check if EP has given overrides*/
					  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
					  		 {
					  			//log.debug("EP has Override for Self Insured");
					  			strOvrUsed=GlobalVariables.YES;
					  		 }
					  		 else
					  		 {
					  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
					  			arlGLStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
					  		 }
				  	 	}
				       else if(GlobalVariables.YES.equals(primGenMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				       {
				  		 //log.debug("Exiting Gen Liability Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				    	   if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
							  {
								  if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()))
								  {
									  //log.debug("MC has Blanket");
									  /*check if EP allows Blanket*/
									  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
									  {
										  //log.debug("EP allows Blanket..Addtional Insured OK");
									  }
									  else
									  {
										  //log.debug("EP doesn't allow Blanket");
										  //log.debug("Addtional Insured problem");
										  /*Check if EP has given overrides*/
								 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
								 		  {
								 			 //log.debug("EP has given overrides for Additional Insured");
								 		  }
								 		  /*End overrides code*/ 
								 		  else
								 		  {
								 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
								 		  }									  
									  }
								  }
								  else
								  {
									  //log.debug("MC doesn't have Blanket");
									  /*Check if EP has been selected as Addtioanl Insured*/
									  if(GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
									  {
										  //log.debug("EP selected as Additinal Insured(so OK) ");									 
									  }
									  else
									  {
										  //log.debug("Addtional Insured problem");
										  /*Check if EP has given overrides*/
								 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
								 		  {
								 			 //log.debug("EP has given overrides for Additional Insured");
								 		  }
								 		  /*End overrides code*/ 
								 		  else
								 		  {
								 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
								 		  }									  
									  }
								  }
							  }  
				  		 return arlGLStatus;
				       }
					   /*End Self Insured check*/
				       
					  
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primGenMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlGLStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					  
					 /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  /*check if MC has Blanket or Additional Insured Flag is Yes*/
							  /*if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()) || GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }*/
							  if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
						
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK)");
									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 //log.debug("Report additional Insured problem to UValid");
							 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  									  
								  }
							  }
						  }
					  }
					  /*End Additional Insured check*/
					  /**----------------------------------------------------------**/

					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasGenLimit=Utility.commaStringtoDouble(primGenMC.getLimit());
					  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
					  dMCHasDeduct=Utility.commaStringtoDouble(primGenMC.getDeductible());
					  if(excessGenMC!=null)
					  {
						  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit());
						  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible());
					  }
					  if(umbGenMC!=null && umbGenMC.getGLReqd().equals(GlobalVariables.YES))
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  
					  if(primGenMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*cndUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*cndUSD;
						  }
						  if(umbGenMC!=null && umbGenMC.getGLReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  //log.debug("Currency conversion dMCHasGenLimit  (CND $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExGenLimit 	(CND $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  if(primGenMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*mexUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*mexUSD;
						  }
						  if(umbGenMC!=null && umbGenMC.getGLReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  //log.debug("Currency conversion dMCHasGenLimit (MEX $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExAutoLimit    (MEX $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessGenMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessGenMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessGenMC.getPolicyTerminatedDate().length()==0) || paramDate.after(Utility.stringToSqlDate(excessGenMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasGenLimit=dMCHasGenLimit+dExGenLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getGLReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
								  dMCHasGenLimit=dMCHasGenLimit+dMCUmbrella;
						  }
						  
					  }
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasGenLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  /*****
					   * Added by piyush on 16 July2007************************************
					   * According to new waiver specs don't add Limit boosters to MC limit.
					   * Consider limit booster is the new limit of MC that is required by EP.
					   */
					  //Commented by Piyush as per above note on 16 July2007
					  //dMCHasGenLimit=dMCHasGenLimit+dMCLimBooster; //adding booster to Limits					  
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasGenLimit +"and EP Limit:- "+ dEPLimit+" for Comparision");
					  //Commented by Piyush as per above note on 16 July2007
					  //if(dMCHasGenLimit<dEPLimit)
					  if((dMCHasGenLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasGenLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  /*Deductible check*/
					  
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasGenLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
						
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  	ArrayList activeEpspcDtls = new ArrayList();
					  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primGenMC.getMcAcctNo(),GlobalVariables.GENPOLICY);
					  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
					  	{
					  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
					  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
					  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
					  	}
					  	/*Uday modification 24 May 12 start*/
					  	/* The pending EP SPEC policy also needs to be checked. 
					  	 * This will only be checked when the policy effective date is greater then current date*/
					  	if (!primGenMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate()))){
					  		ArrayList pendingEpspcDtls = new ArrayList();
					  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
					  				.getEpAcctNo(),
					  				primGenMC.getMcAcctNo(),
					  				GlobalVariables.GENPOLICY);
					  		if (pendingEpspcDtls != null
					  				&& pendingEpspcDtls.size() > 3) {
					  			dEpSpcPenLim = (Double) pendingEpspcDtls
					  			.get(0);
					  			dEpSpcPenDed = (Double) pendingEpspcDtls
					  			.get(1);
					  			IsPenEpSpcExist = (String) pendingEpspcDtls
					  			.get(2);
					  			strPenEpSpcEffDate = (String) pendingEpspcDtls
					  			.get(3);
					  		}
					  	}

					  if(bLimitNotOkFlg)
					  {
						  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
						  {}
						  else /* Uday modification 24 May 12 */
						  {
							  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
									  && (!primGenMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4))))
									  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
							  {
								  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
								  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
								  //log.debug("dEPLimit - "+dEPLimit);
							  }
							  else
							  {
								  arlGLStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
						  }
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
								  {}
								  else /* Uday modification 24 May 12 */
								  {
									  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
												  && (!primGenMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4))))
												  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
									  		
									  	}
									  	else{
									  		arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  	}
								  }
							  }
						  }
						  else
						  {
							  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
							  {}
							  else /* Uday modification 24 May 12 */
							  {
								  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
											  && (!primGenMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4))))
											  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
								  		
								  	}
								  	else{
								  		arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  	}
							  }
						  }
					  }
					  if(arlGLStatus.size()==0)
					  {
						  //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
				  //log.debug("Exiting Primary Policy Check for General Liability");
			  }//end of primGenMC!=null
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlGLStatus is not empty  as it has problems*/
			  
			  if(!bEPPolCheck && epSpecGenMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for General Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlGLStatus.removeAll(arlGLStatus);
				  
				  /*Repeating EP Specific same as that in Primary*/
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * General Liability */
				  strEPSpcUsed=GlobalVariables.GENPOLICY;
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlGLStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecGenMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  //log.debug("if policy expiration date is today's date");
					  arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					  return arlGLStatus;
				  }
				  //end----02/03

				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecGenMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecGenMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecGenMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecGenMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecGenMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				   /*if(epSpecGenMC.getPolicyTerminatedDate().length()>0)
				   {
					  //log.debug("EP Specific Policy Terminated Date= "+epSpecGenMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) ||Utility.stringToSqlDate(epSpecGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Primary Policy Termination date greater than parameter date ");
						  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecGenMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecGenMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecGenMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(epSpecGenMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
			  	 	{
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			/*Check if EP doesn't allow SI*/
				  			strOvrUsed=GlobalVariables.YES;
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlGLStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
			  	 	}
			       else if(GlobalVariables.YES.equals(epSpecGenMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
			       {
			  		 //log.debug("Exiting GenLiability Check (after Self Insured Check)");
			  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
			  		 return arlGLStatus;
			       }
				   /*End Self Insured check*/
				  
				  
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   
				   if(GlobalVariables.YES.equals(epSpecGenMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							 strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlGLStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				   
				   /*End Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   
				   /*Start Additional Insured check
				    *Ep requires Additional Insured and policy doesn't have Additional Insured 
				    *(check blanket and ep blanket ok or Policy endorsed for this EP
				    **/
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  /*if(GlobalVariables.YES.equals(epSpecGenMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecGenMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  }*/
				 		if(GlobalVariables.YES.equals(epSpecGenMC.getBlanketReqd()))
						  {
							  //log.debug("MC has Blanket");
							  /*check if EP allows Blanket*/
							  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
							  {
								  //log.debug("EP allows Blanket..Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("EP doesn't allow Blanket");
								  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
						 		  }									  
							  }
						  }
						  else
						  {
							  //log.debug("MC doesn't have Blanket");
							  /*Check if EP has been selected as Addtioanl Insured*/
							  if(GlobalVariables.YES.equals(epSpecGenMC.getAddlnInsured()))
							  {
								  //log.debug("EP selected as Additinal Insured(so OK) ");									 
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  
							  }
						  }
				 	  }
				 	
				   }
				   else
				   {
				 	  //log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecGenMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
				 				 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecGenMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK)");
				 				 
				 			  }
				 			  else
				 			  {
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 //log.debug("Report additional Insured problem to UValid");
						 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  									  
				 			  }
				 		  }
				 	  }
				   }
				   /*End Additional Insured check*/
				   /**----------------------------------------------------------**/
				   
				   
				   /*Calculating Limits and Deductibles along with currency conversion*/
				   	  dMCHasGenLimit=Utility.commaStringtoDouble(epSpecGenMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(epSpecGenMC.getDeductible());
					  if(excessGenMC!=null)
						  {
						  	dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit());
						  	dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible());
						  }
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  if(epSpecGenMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*cndUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*cndUSD;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  //log.debug("Currency conversion dMCHasGenLimit  (CND $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExGenLimit 	(CND $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  if(epSpecGenMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*mexUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*mexUSD;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  //log.debug("Currency conversion dMCHasAutoLimit (MEX $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExAutoLimit    (MEX $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessGenMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessGenMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessGenMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessGenMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasGenLimit=dMCHasGenLimit+dExGenLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getGLReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
								  dMCHasGenLimit=dMCHasGenLimit+dMCUmbrella;
						  }
						  
					  }
					 
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasGenLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note on 16July07
					  //dMCHasGenLimit=dMCHasGenLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasGenLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //Commented by piyush as per note on 16July07
					  //if(dMCHasGenLimit<dEPLimit)
					  if((dMCHasGenLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasGenLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit

					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasGenLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
						  if(bLimitNotOkFlg)
						  {
							  arlGLStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
						  }
						  if(bDedNotOkFlg)
						  {
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCDedBooster>dEPDeduct)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
				  //log.debug("Exiting EP Specific Policy Check for General Liability");
			  }// end of EP Specific Policy
			  else
			  {
				  //arlGLStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlGLStatus;
			  }
			  
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
			  
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in General Liability Check for UValid:- ", exp );
			
		}
		//log.info("Business: Exiting method checkGeneralLiability() with Problems:- "+arlGLStatus);
		//log.info("Returning :== Override used flag:- "+strOvrUsed + " :== and EP Specific Flag:- "+ strEPSpcUsed);
		return arlGLStatus;
	}
	/**
	 * This method will check  Cargo Policy requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkCargoLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkCargoLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlCargoStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbCargoPrimary= new StringBuffer(GlobalVariables.CARGOPOLICY);
			  sbCargoPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbCargoExcess= new StringBuffer(GlobalVariables.CARGOPOLICY);
			  sbCargoExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.CARGOPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  //log.info("EPSPC0 :");
			  CargoBean primCrgMC=(CargoBean)mcInsDtls.get(sbCargoPrimary.toString());
			  CargoBean excessCrgMC=(CargoBean)mcInsDtls.get(sbCargoExcess.toString());
			  CargoBean epSpecCrgMC=null;
				  //(CargoBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  epSpecCrgMC=(CargoBean)arCg.get(x);
					  if(epSpecCrgMC.getPolicyMstId()==0)
					  {
						  //epSpecCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecCrgMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecCrgMC=null;
					  }
				  }
			  }
			  UmbBean umbGenMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }			  
			  //log.info("Done Bean Creation ");			  
			  /*
			  String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasCrgLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExGenLimit=0.0;
			  double dExGenDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /* to decide whether to check EP Specific or not..
	 		   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
	 		   *  in Primary and then it will be set to true 
	 		   */
			  boolean bEPPolCheck=false;
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.info("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.CARGOPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlCargoStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlCargoStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlCargoStatus;
			  }
			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  //log.debug("Cargo Ded Booster :"+epDtls.getEpOvrMCBean().getDedBooster());
			  //if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1") || epDtls.getEpOvrMCBean().getDedBooster().equals("-1"))
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1"))
			  {
				  //log.info("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlCargoStatus;
			  }
			  //======================End added by Piyush ==========================
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.info("EP requires Cargo.. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbCargoPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  //log.info("++MCNOPOLICY");
					  arlCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlCargoStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires cargo.. so skipping Policy check ");
				  return arlCargoStatus;
			  }

			  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
			  if(primCrgMC!=null)
			  {
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlCargoStatus;
				  }
				  if(Utility.stringToSqlDate(primCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  //log.debug("if policy expiration date is today's date");
					  arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					  return arlCargoStatus;
				  }
			  }
			  //end----02/03
			  
			  /*Check if MC_Hauls_Only =Y, then to skip the test for Cargo...*/
			  //Modified by swati----22/02----for Cargo Hauls Only msg in smartchecklist and to send notification
			  if(primCrgMC!=null && primCrgMC.getHaulsOwnOnly().equals(GlobalVariables.YES)
					   && epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("Hauls Only=Y, so skipping Cargo Check");
				  arlCargoStatus.add(GlobalVariables.UVLD_CARGO_HAULSOWN);
				  //return arlCargoStatus;
			  }
			 
			  
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  if(primCrgMC!=null)
			  {
				  //log.debug("Entering Primary Policy Check for Cargo Liability");
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(primCrgMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(primCrgMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(primCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(primCrgMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(primCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				/* End Termination */
				       
				     /*Start Self Insured check*/
				  	 if(GlobalVariables.YES.equals(primCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
							 /*MC has SI and EP doesn't allow Self Insured*/
							 //log.debug("MC has SI and EP doesn't allow Self Insured");
							 /*Check if EP has given overrides*/
							 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
							 {
									//log.debug("EP has Override for Self Insured");
									/*Check if EP doesn't allow SI*/
									strOvrUsed=GlobalVariables.YES;
									
							 }
							 else
							 {
								 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
								 arlCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
							 }
					 }
					 else if(GlobalVariables.YES.equals(primCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						// log.debug("Exiting Cargo Liability Check (after Self Insured Check)");
						// log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlCargoStatus;
					 }
					 /*End Self Insured check*/
					   
					 
					   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   if(GlobalVariables.YES.equals(primCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
						  {
							  /*MC has RRG and EP doesn't allow RRG*/
							  //log.debug("MC has RRG and EP doesn't allow RRG");
							  /*Check if EP has given overrides*/
							  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
							  {
								  //log.debug("EP has Override for RRG");
								  strOvrUsed=GlobalVariables.YES;
							  }
							  else
							  {
								  //log.debug("EP doesn't allow RRG So RRG Problem");
								  arlCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
							  }
						  }
					  /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					   /**----------------------------------------------------------**/
					   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					   {
					 	  //log.debug("Start Additional Insured check for UIIEP");
					 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
					 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
					 		if(GlobalVariables.YES.equals(primCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(primCrgMC.getAddlnInsured()))
						 	  {
						 		  //log.debug("Addtional Insured OK");
						 	  }
						 	  else
						 	  {
						 		  //log.debug("Addtional Insured problem for UIIAEP");
						 		 arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 	  }
 
						  }
					 	  					 
					   }
					   else
					   {
					 	  //log.debug("Start Additional Insured check for other EPs");
					 	  /*Check if ep requires additional Insured*/
					 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		  //log.debug("EP requires Addtional Insured");
					 		  /*Check if MC has blanket*/
					 		  if(GlobalVariables.YES.equals(primCrgMC.getBlanketReqd()))
					 		  {
					 			  //log.debug("MC has Blanket");
					 			  /*check if EP allows Blanket*/
					 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
					 			  {
					 				  //log.debug("EP allows Blanket..Addtional Insured OK");
					 			  }
					 			  else
					 			  {
					 				  //log.debug("EP doesn't allow Blanket");
					 				  //log.debug("Addtional Insured problem");
					 				 /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {	  							 		  
							 			  arlCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }
					 			  }
					 		  }					 		 
					 		  else
					 		  {
					 			  //log.debug("MC doesn't have Blanket");
					 			  /*Check if EP has been selected as Addtioanl Insured*/
					 			  if(GlobalVariables.YES.equals(primCrgMC.getAddlnInsured()))
					 			  {
					 				  //log.debug("EP selected as Additinal Insured(so OK)");					 				 
					 			  }
					 			  else
					 			  {
					 				  log.debug("Addtional Insured problem");
					 				 /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			  arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }
					 			  }
					 		  }
					 	  }					 	 
					   }
					   /*End Additional Insured check*/
					   /**----------------------------------------------------------**/
					   

					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasCrgLimit=Utility.commaStringtoDouble(primCrgMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(primCrgMC.getDeductible());
					  if(excessCrgMC!=null)
					  {
						  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit());
						  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible());
					  }
					  if(umbGenMC!=null && umbGenMC.getCargoReqd().equals(GlobalVariables.YES))
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  if(primCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasCrgLimit=dMCHasCrgLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessCrgMC!=null)
							  {
							  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*cndUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*cndUSD;
							  }
						  if(umbGenMC!=null && umbGenMC.getCargoReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(primCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasCrgLimit=dMCHasCrgLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessCrgMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*mexUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*mexUSD;
						  }
						  if(umbGenMC!=null && umbGenMC.getCargoReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }
					  //log.debug("After Currency conversion dMCHasCrgLimit "+dMCHasCrgLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  //log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
					 
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessCrgMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessCrgMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasCrgLimit=dMCHasCrgLimit+dExGenLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
						  }
					  }// end of if excessAutoMC
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getCargoReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasCrgLimit=dMCHasCrgLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasCrgLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note on 16July07
					  //dMCHasCrgLimit=dMCHasCrgLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasCrgLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //log.info("Excess Limit:- "+dExGenLimit +"and Umbrella Limit:- "+ dMCUmbrella+" for Comparision");
//					Commented by piyush as per note on 16July07
					  //if(dMCHasCrgLimit<dEPLimit)
					  if((dMCHasCrgLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasCrgLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					 
					  /*Deductible check*/
					 // log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March 07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						 //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasCrgLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  	ArrayList activeEpspcDtls = new ArrayList();
					  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primCrgMC.getMcAcctNo(),GlobalVariables.CARGOPOLICY);
					  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
					  	{
					  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
					  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
					  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
					  	}
					  	/*Checking for Multiple limits against EP Specific In-place coverage*/
					  	if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  if(bLimitNotOkFlg||bDedNotOkFlg)
							  {
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dEpSpcActLim>=dTempEPLimit)&&(dEpSpcActDed<=dTempEPDeduct))
									  {
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
									  }
								  }
							  }
						  }

					  	
					  	/*Uday modification 24 May 12 start*/
				  		/* The pending EP SPEC policy also needs to be checked. 
				  		 * This will only be checked when the policy effective date is greater then current date*/
					  	if (!primCrgMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate())))
					  	{
					  		ArrayList pendingEpspcDtls = new ArrayList();
					  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
					  				.getEpAcctNo(),
					  				primCrgMC.getMcAcctNo(),
					  				GlobalVariables.CARGOPOLICY);
					  		if (pendingEpspcDtls != null
					  				&& pendingEpspcDtls.size() > 3) {
					  			dEpSpcPenLim = (Double) pendingEpspcDtls
					  			.get(0);
					  			dEpSpcPenDed = (Double) pendingEpspcDtls
					  			.get(1);
					  			IsPenEpSpcExist = (String) pendingEpspcDtls
					  			.get(2);
					  			strPenEpSpcEffDate = (String) pendingEpspcDtls
					  			.get(3);
					  		}

					  		if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  		{
					  			if (IsPenEpSpcExist.equalsIgnoreCase("Y") 
					  					&& (!(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
					  					&& dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit) {
					  				if (bLimitNotOkFlg || bDedNotOkFlg) {
					  					for (int i = 0; i < epDtls
					  					.getPolicyMulLimits().size(); i++) {
					  						double dTempEPLimit = Utility
					  						.commaStringtoDouble(((MultipleLimit) epDtls
					  								.getPolicyMulLimits()
					  								.get(i))
					  								.getMinLimit());
					  						double dTempEPDeduct = Utility
					  						.commaStringtoDouble(((MultipleLimit) epDtls
					  								.getPolicyMulLimits()
					  								.get(i))
					  								.getMaxDed());
					  						if ((dEpSpcPenLim >= dTempEPLimit)
					  								&& (dEpSpcPenDed <= dTempEPDeduct)) {
					  							bLimitNotOkFlg = false;
					  							bDedNotOkFlg = false;
					  						}
					  					}
					  				}
					  			}
					  		}
					  	}
					  	/*Uday modification 24 May 12 end*/
					  	
					  if(bLimitNotOkFlg)
					  {
						  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
						  {}
						  else /* Uday modification 24 May 12 */
						  {
							  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
									  && (!primCrgMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
									  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
							  {
								  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
								  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
								  //log.debug("dEPLimit - "+dEPLimit);
							  }
							  else
							  {
								  arlCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
						  }
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //log.debug(">>>>dMCDedBooster ="+dMCDedBooster +" >>>>dEPDeduct :"+dEPDeduct);
							  //if(dMCDedBooster>dEPDeduct)//Changed by Piyush 27March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else
								  {
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
												  && (!primCrgMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
												  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist

										  }
										  else{
											  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  }
									  }
								  }
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else
							  {
								  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
								  {}
								  else /* Uday modification 24 May 12 */
								  {
									  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
											  && (!primCrgMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
											  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist

									  }
									  else{
										  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  }
								  }
							  }
						  }
					  }
					  if(arlCargoStatus.size()==0)
					  {
						  //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
					  
				  //log.debug("Exiting Primary Policy Check for Cargo Liability");
			  }//end of Primary Policy check
			  if(!bEPPolCheck && epSpecCrgMC!=null )
			  {
				  /*Repeating EP Specific same as that in Primary*/
				  //log.debug("Entering EP Specific Policy Check for Cargo Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlCargoStatus.removeAll(arlCargoStatus);
				  
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Cargo Liability */
				  strEPSpcUsed=GlobalVariables.CARGOPOLICY;
				  
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlCargoStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  //log.debug("if policy expiration date is today's date");
					  arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					  return arlCargoStatus;
				  }
				  //end----02/03
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecCrgMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecCrgMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecCrgMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /* if(epSpecCrgMC.getPolicyTerminatedDate().length()>0)
				   {
					  //log.debug("EP Specific Policy Terminated Date= "+epSpecCrgMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Policy Terminated on parameter date ");
					      arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecCrgMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecCrgMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
					 if(GlobalVariables.YES.equals(epSpecCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting EPSpec CargoLiability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlCargoStatus;
					 }
				   /*End Self Insured check*/
				
				   
				   
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   if(GlobalVariables.YES.equals(epSpecCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				   
				  /*Start Additional Insured check
				  * Ep requires Additional Insured and policy doesn't have Additional Insured 
				  * (check blanket and ep blanket ok or Policy endorsed for this EP
				  * */
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		if(GlobalVariables.YES.equals(epSpecCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecCrgMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  }
				 	  }
				   }
				   else
				   {
				 	  //log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecCrgMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
				 				 arlCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecCrgMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK) ");
				 				 
				 			  }
				 			  else
				 			  {
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			  arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }
				 			  }
				 		  }
				 	  }
				   }
				   /**----------------------------------------------------------**/
				   
				   				   
				   /*Calculating Limits and Deductibles along with currency conversion*/
				  dMCHasCrgLimit=Utility.commaStringtoDouble(epSpecCrgMC.getLimit());
				  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
				  dMCHasDeduct=Utility.commaStringtoDouble(epSpecCrgMC.getDeductible());
				  if(excessCrgMC!=null)
				  {
					  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit());
					  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible());
				  }
				  if(umbGenMC!=null)
					  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
				  if(epSpecCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  //log.debug("Canadian Currency Conversion");
					  dMCHasCrgLimit=dMCHasCrgLimit*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
					  if(excessCrgMC!=null)
						  {
						  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*cndUSD ;
						  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*cndUSD;
						  }
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
				  }
				  else if(epSpecCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  //log.debug("Mexican Currency Conversion");
					  dMCHasCrgLimit=dMCHasCrgLimit*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
					  if(excessCrgMC!=null)
					  {
						  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*mexUSD ;
						  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*mexUSD;
					  }
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
				  }  
				  //log.debug("After Currency conversion dMCHasCrgLimit "+dMCHasCrgLimit);
				  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
				  //log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for excess policy and if it exists to add to MCLimit*/
				  if(excessCrgMC!=null )
				  {
					  //log.debug("Excess Policy Terminated Date:- "+excessCrgMC.getPolicyTerminatedDate());
					  /*check if policy terminated date is empty or less than param date*/
					  if((excessCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4) )) 
					  {
						  //log.debug("Excess policy termination date less than parameter date ");
						  dMCHasCrgLimit=dMCHasCrgLimit+dExGenLimit;
						  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
					  }
				  }// end of if excessAutoMC
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				  
				  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getCargoReqd()))
				  {
					  /*check if umbrella policy less than or equal to param date*/
					  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
					  {
						 	  //log.debug("Umbrella policy termination date after parameter date ");
						 	 dMCHasCrgLimit=dMCHasCrgLimit+dMCUmbrella;
					  }
					  
				  }
				  
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if((dMCHasCrgLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Limits |Deductibles override used");
					  bOvrUsed=true;
				  }
				  /******************************************************************************/
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  //dMCHasCrgLimit=dMCHasCrgLimit+dMCLimBooster; //adding booster to Limits
				  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
				  /*******************************************************************************/
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  //log.info("MC Limit:- "+dMCHasCrgLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
				  //log.info("MC Limit:- "+dMCHasCrgLimit +"EP Limit:- "+ dEPLimit+" for Comparision");
				  //if(dMCHasCrgLimit<dEPLimit)
				  if((dMCHasCrgLimit<dEPLimit) && (dMCLimBooster==0))
				  {//Added as per new waiver specs
					  bLimitNotOkFlg=true;
				  }
				  else if(dMCHasCrgLimit<dMCLimBooster)
				  {	
					  //log.info("Limit Not Ok before multiple Pol Limits check");
					  bLimitNotOkFlg=true;
				  }// end if limit
				  
				  /*Deductible check*/
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }
				  
				  /*Check if multiple limits/deductible exists for this policy */
				  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
				  {
					  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
					  /*will loop through Multiple limits and deductibles bean to find appropriate
					   * limits and deductible to be used for the MC.
					   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
					   * */
					  if(bLimitNotOkFlg||bDedNotOkFlg)
					  {
						  //log.debug("Checking Policy Multiple Limits and Deductibles ");
						  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
						  {
							  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
							  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
							  if((dMCHasCrgLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
							  {
								  /*found Multiple Limits and deductibles so using that limit and deductibles
								   * to check MC Limits and Deductibles; 
								   * */
								  bLimitNotOkFlg=false;
								  bDedNotOkFlg=false;
								  //break;
							  }
						  }// end of for loop
					  }//end if bLimitOkFlg
				  }
				  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
				   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
				   * not found for Multiple Limits and Deductibles, so setting appropriate message
				   * */
				  if(bLimitNotOkFlg)
				  {
					  arlCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(dMCDedBooster!=0)
					  {
						  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
						  if(dMCDedBooster>dEPDeduct)
						  {
							 // log.debug("Ded problem after replacing Booster");
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
								  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  else
					  {
						  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
						  else	
							  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
					  }
				  }
				 // log.debug("Exiting EP Specific Policy Check for Cargo Liability");
			  }//end of EP Specific Policy Check

			  if(primCrgMC==null && epSpecCrgMC==null)
			  {
				  //log.info("MCNOPOLICY++");
				  arlCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Cargo Check for UValid:- ", exp );
		}
		//log.info("Business: Exiting method checkCargoLiability()of UValidPolicyCheck class:- "+arlCargoStatus);
		return arlCargoStatus;
	}
	/**
	 * This method will check  Contingent Cargo Policy requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkContCargoLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkContCargoLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlContCargoStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbContCargoPrimary= new StringBuffer(GlobalVariables.CONTCARGO);
			  sbContCargoPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbContCargoExcess= new StringBuffer(GlobalVariables.CONTCARGO);
			  sbContCargoExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.CONTCARGO);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			 
			  ContCargoBean primCntCrgMC=(ContCargoBean)mcInsDtls.get(sbContCargoPrimary.toString());
			 
			  ContCargoBean excessCntCrgMC=(ContCargoBean)mcInsDtls.get(sbContCargoExcess.toString());
			 
			  ContCargoBean epSpecCntCrgMC=null;			  
				  //(ContCargoBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			 
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arCc = new ArrayList();
				  arCc = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCc.size();x++)
				  {
					  epSpecCntCrgMC=(ContCargoBean)arCc.get(x);
					  if(epSpecCntCrgMC.getPolicyMstId()==0)
					  {
						  //epSpecCntCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecCntCrgMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecCntCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecCntCrgMC=null;
					  }
				  }
			  }
			  
			  UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			 
			  /*String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			 */
			  double dMCHasCCLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExCCLimit=0.0;
			  double dExCCDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			
			   /* to decide whether to check EP Specific or not..
			   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			   *  in Primary and then it will be set to true 
			   */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.CONTCARGO.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlContCargoStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlContCargoStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlContCargoStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlContCargoStatus;
			  }
			  //======================End added by Piyush ==========================

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbContCargoPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlContCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlContCargoStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlContCargoStatus;
			  }
			  
			  /*Check if MC_Hauls_Only =Y, then to skip the test for Cargo...*/
			  /* Not Required for Contingent Cargo
			   * if(primCntCrgMC!=null && primCntCrgMC.getHaulsOwnOnly().equals(GlobalVariables.YES))
			  {
				  //log.debug("Hauls Only=Y, so skipping Cargo Check");
				  return arlContCargoStatus;
			  }*/
			  
			
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  
			  if(primCntCrgMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for Contingent Cargo Liability");
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlContCargoStatus;
						  }
						  if(Utility.stringToSqlDate(primCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlContCargoStatus;
						  }
						  //end----02/03
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primCntCrgMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primCntCrgMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primCntCrgMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primCntCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						/* End Termination */
				       
				       /*Start Self Insured check*/
				       if(GlobalVariables.YES.equals(primCntCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				       {
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			/*Check if EP doesn't allow SI*/
				  			strOvrUsed=GlobalVariables.YES;
				  			
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlContCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
				  	 }
				  	 else if(GlobalVariables.YES.equals(primCntCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 //log.debug("Exiting Cont Cargo Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				  		 return arlContCargoStatus;
				  	 }
   				    /*End Self Insured check*/
					   
				      
					   
					   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   if(GlobalVariables.YES.equals(primCntCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
						  {
							  /*MC has RRG and EP doesn't allow RRG*/
							  //log.debug("MC has RRG and EP doesn't allow RRG");
							  /*Check if EP has given overrides*/
							  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
							  {
								  //log.debug("EP has Override for RRG");
								  strOvrUsed=GlobalVariables.YES;
							  }
							  else
							  {
								  //log.debug("EP doesn't allow RRG So RRG Problem");
								  arlContCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
							  }
						  }
					   /*End Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   
					   /*Start Additional Insured check
					   * Ep requires Additional Insured and policy doesn't have Additional Insured 
					   * (check blanket and ep blanket ok or Policy endorsed for this EP
					   * */
					   /**----------------------------------------------------------**/
					   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					   {
					 	  //log.debug("Start Additional Insured check for UIIEP");
					 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
					 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		if(GlobalVariables.YES.equals(primCntCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(primCntCrgMC.getAddlnInsured()))
						 	  {
						 		  //log.debug("Addtional Insured OK");
						 	  }
						 	  else
						 	  {
						 		  //log.debug("Addtional Insured problem for UIIAEP");
						 		 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 	  } 
					 	  }
					   }
					   else
					   {
					 	  //log.debug("Start Additional Insured check for other EPs");
					 	  /*Check if ep requires additional Insured*/
					 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		  //log.debug("EP requires Addtional Insured");
					 		  /*Check if MC has blanket*/
					 		  if(GlobalVariables.YES.equals(primCntCrgMC.getBlanketReqd()))
					 		  {
					 			  //log.debug("MC has Blanket");
					 			  /*check if EP allows Blanket*/
					 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
					 			  {
					 				  //log.debug("EP allows Blanket..Addtional Insured OK");
					 			  }
					 			  else
					 			  {
					 				  //log.debug("EP doesn't allow Blanket");
					 				  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlContCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  									  					 				 
					 			  }
					 		  }
					 		  else
					 		  {
					 			  //log.debug("MC doesn't have Blanket");
					 			  /*Check if EP has been selected as Addtioanl Insured*/
					 			  if(GlobalVariables.YES.equals(primCntCrgMC.getAddlnInsured()))
					 			  {
					 				  //log.debug("EP selected as Additinal Insured(so OK) ");
					 				 
					 			  }
					 			  else
					 			  {
					 				  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  									  					 				 
					 			  }
					 		  }
					 	  }
					   }
					   /*End Additional Insured check*/
					   /**----------------------------------------------------------**/
					   

					  /*Calculating Limits and Deductibles along with currency conversion*/
					 
					  dMCHasCCLimit=Utility.commaStringtoDouble(primCntCrgMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(primCntCrgMC.getDeductible());
					 
					  if(excessCntCrgMC!=null)
					  {
						  dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit());
						  dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible());
					  }
					 
					  if(primCntCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessCntCrgMC!=null)
							  {
							  	dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*cndUSD ;
							  	dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*cndUSD ;
							  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(primCntCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessCntCrgMC!=null)
						  {
						  	dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*mexUSD ;
						  	dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*mexUSD ;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }
					  //log.debug("After Currency conversion dMCHasCrgLimit "+dMCHasCCLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  //log.debug("After Currency conversion dExGenLimit "+dExCCLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
					  
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessCntCrgMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessCntCrgMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessCntCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasCCLimit=dMCHasCCLimit+dExCCLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExCCDed;
						  }
					  }// end of if excessAutoMC
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getContCargoReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasCCLimit=dMCHasCCLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasCCLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //dMCHasCCLimit=dMCHasCCLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasCCLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //if(dMCHasCCLimit<dEPLimit)
					  if((dMCHasCCLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasCCLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					 
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasCCLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  if(bLimitNotOkFlg)
					  {
						  arlContCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //if(dMCDedBooster>dEPDeduct)Changed by piyush 27 March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
									  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
								  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  if(arlContCargoStatus.size()==0)
					  {
						  //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
					 //log.debug("Exiting Primary Policy Check for Contingent Cargo Liability");
			  }
			  else if(!bEPPolCheck && epSpecCntCrgMC!=null)
			  {
					  /*Repeating EP Specific same as that in Primary*/
					  //log.debug("Entering EP Specific Policy Check for Contingent-Cargo Liability");
					  
					  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
					  arlContCargoStatus.removeAll(arlContCargoStatus);
					  
					  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
					   * Cargo Liability */
					  strEPSpcUsed=GlobalVariables.CONTCARGO;
//					swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
					  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						//log.debug("if policy expiration date is today's date");
						arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlContCargoStatus;
					  }
					  if(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						  	//log.debug("if policy expiration date is today's date");
						  	arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlContCargoStatus;
					  }

					  //end----02/03
					  
					  /*Start Check Policy Status (Termination|Cancellation) */
					  ArrayList arr = new ArrayList();
					  arr = getTmpTermDt(epSpecCntCrgMC.getPolicyMstId());
					  Date tmpTerm = null;
					  Date tmpRein = null;
					  Date curTerm = null;
					  Date curReins = null;
					  if(!arr.isEmpty())
					  {
						  tmpTerm = (Date)arr.get(0);
						  tmpRein = (Date)arr.get(1);
					  }							  
					  //=========By Piyush 14Mar'09===============================================================
					  if(epSpecCntCrgMC.getPolicyTerminatedDate().length()>0)
					  {
						  curTerm = Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
						  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
						  {
							  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
			  		  else if(epSpecCntCrgMC.getPolicyReinstatedDate().length()>0)
					  {
			  			  curReins = Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
						  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
						  else if(curReins.after(paramDate))
						  {
							  if(tmpTerm!=null && tmpTerm.equals(paramDate))
							  {
								  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
					  }						  
					  //=========End By Piyush 14Mar===============================================================					  

					   /*Start Check Policy Status (Termination|Cancellation) */
					   /*if(epSpecCntCrgMC.getPolicyTerminatedDate().length()>0)
					   {
						  //log.debug("EP Specific Policy Terminated Date= "+epSpecCntCrgMC.getPolicyTerminatedDate());
						  if(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
						  {
						      //log.debug("Policy Terminated on parameter date ");
						      arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					   } else if(epSpecCntCrgMC.getPolicyReinstatedDate().length()>0)
						  {
							  log.debug("Primary Policy Reinstated Date:- "+epSpecCntCrgMC.getPolicyReinstatedDate());
							  if(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								  log.debug("Policy Reinstated but have future reinstatement date");
								  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }*/
					  
					  /*Start Self Insured check*/
					  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
						 {
							 /*MC has SI and EP doesn't allow Self Insured*/
							 //log.debug("MC has SI and EP doesn't allow Self Insured");
							 /*Check if EP has given overrides*/
							 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
							 {
								//log.debug("EP has Override for Self Insured");
								strOvrUsed=GlobalVariables.YES;
							 }
							 else
							 {
								 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
								 arlContCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
							 }
						 }
						 else if(GlobalVariables.YES.equals(epSpecCntCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
						 {
							 //log.debug("Exiting Cont Cargo Check (after Self Insured Check)");
							 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
							 return arlContCargoStatus;
						 }
					   /*End Self Insured check*/
						
					
					   
					   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   
					   if(GlobalVariables.YES.equals(epSpecCntCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
						  {
							  /*MC has RRG and EP doesn't allow RRG*/
							  //log.debug("MC has RRG and EP doesn't allow RRG");
							  /*Check if EP has given overrides*/
							  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
							  {
								  //log.debug("EP has Override for RRG");
								  strOvrUsed=GlobalVariables.YES;
							  }
							  else
							  {
								  //log.debug("EP doesn't allow RRG So RRG Problem");
								  arlContCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
							  }
						  }
					   
					   /*Start Additional Insured check
					   * Ep requires Additional Insured and policy doesn't have Additional Insured 
					   * (check blanket and ep blanket ok or Policy endorsed for this EP
					   * */
					   /**----------------------------------------------------------**/
					   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					   {
					 	  //log.debug("Start Additional Insured check for UIIEP");
					 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		/*check if MC has Blanket or Additional Insured Flag is Yes*/
						 	  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecCntCrgMC.getAddlnInsured()))
						 	  {
						 		  //log.debug("Addtional Insured OK");
						 	  }
						 	  else
						 	  {
						 		  //log.debug("Addtional Insured problem for UIIAEP");
						 		 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 	  } 
					 	  }
					   }
					   else
					   {
					 	  //log.debug("Start Additional Insured check for other EPs");
					 	  /*Check if ep requires additional Insured*/
					 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		  //log.debug("EP requires Addtional Insured");
					 		  /*Check if MC has blanket*/
					 		  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getBlanketReqd()))
					 		  {
					 			  //log.debug("MC has Blanket");
					 			  /*check if EP allows Blanket*/
					 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
					 			  {
					 				  //log.debug("EP allows Blanket..Addtional Insured OK");
					 			  }
					 			  else
					 			  {
					 				  //log.debug("EP doesn't allow Blanket");
					 				  //log.debug("Addtional Insured problem");
					 				 arlContCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
					 			  }
					 		  }
					 		  else
					 		  {
					 			  //log.debug("MC doesn't have Blanket");
					 			  /*Check if EP has been selected as Addtioanl Insured*/
					 			  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getAddlnInsured()))
					 			  {
					 				  //log.debug("EP selected as Additinal Insured(so OK) ");
					 				 
					 			  }
					 			  else
					 			  {
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  									  					 				 
					 			  }
					 		  }
					 	  }
					   }
					   /**----------------------------------------------------------**/
					   
	
				   
					   /*Calculating Limits and Deductibles along with currency conversion*/
					   dMCHasCCLimit=Utility.commaStringtoDouble(epSpecCntCrgMC.getLimit());
					  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
					  dMCHasDeduct=Utility.commaStringtoDouble(epSpecCntCrgMC.getDeductible());
					  if(excessCntCrgMC!=null)
					  {
						  dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit());
						  dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible());
					  }
					  	
					  if(epSpecCntCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessCntCrgMC!=null)
							{
							  	dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*cndUSD ;
							  	dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*cndUSD ;
							}
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(epSpecCntCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessCntCrgMC!=null)
						  {
							  dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*mexUSD ;
							  dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*mexUSD ;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }  
					  //log.debug("After Currency conversion dMCHasGenLimit "+dMCHasCCLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  //log.debug("After Currency conversion dExGenLimit "+dExCCLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				   
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessCntCrgMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessCntCrgMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessCntCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasCCLimit=dMCHasCCLimit+dExCCLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExCCDed;
						  }
					  }// end of if excessAutoMC
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getContCargoReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasCCLimit=dMCHasCCLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasCCLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //dMCHasCCLimit=dMCHasCCLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasCCLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //if(dMCHasCCLimit<dEPLimit)
					  if((dMCHasCCLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasCCLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					   
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasCCLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
						  if(bLimitNotOkFlg)
						  {
							  arlContCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
						  }
						  if(bDedNotOkFlg)
						  {
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCDedBooster>dEPDeduct)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
										  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
									  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
				  //log.debug("Exiting EP Specific Policy Check for Contingent-Cargo Liability");
			  } //end of EP Specific Policy Check
			  else
			  {
				  arlContCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlContCargoStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Contingent Cargo Check for UValid:- ", exp );
		}
		log.info("Business: Exiting method checkContCargoLiability()of UValidPolicyCheck class:- "+arlContCargoStatus);
		return arlContCargoStatus;
	}
	/**
	 * This method will check  Trailer Policy requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkTrailerLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkTrailerLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlTrailerStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbTrlPrimary= new StringBuffer(GlobalVariables.TRAILERPOLICY);
			  sbTrlPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbTrlExcess= new StringBuffer(GlobalVariables.TRAILERPOLICY);
			  sbTrlExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.TRAILERPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  TrailerBean primTrlMC=(TrailerBean)mcInsDtls.get(sbTrlPrimary.toString());
			  TrailerBean excessTrlMC=(TrailerBean)mcInsDtls.get(sbTrlExcess.toString());
			  TrailerBean epSpecTrlMC=null;
				  //(TrailerBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {		
			  	  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  epSpecTrlMC=(TrailerBean)arCg.get(x);
					  if(epSpecTrlMC.getPolicyMstId()==0)
					  {
						  //epSpecTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecTrlMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecTrlMC=null;
					  }
				  }
			  }		
				  
			  UmbBean umbGenMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }	
			  
			  /*String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasTILimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExTILimit=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /*Uday added end*/
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.TRAILERPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlTrailerStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlTrailerStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlTrailerStatus;
			  }

			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster= Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  
			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  //log.debug("Trailer OVERRIDDEN :"+epDtls.getEpOvrMCBean());
			  //log.debug("Trailer Booster :"+epDtls.getEpOvrMCBean().getLimitBooster());
			  //if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1") || epDtls.getEpOvrMCBean().getDedBooster().equals("-1"))
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1"))
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlTrailerStatus;
			  }
			  //======================End added by Piyush ==========================
			  
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires  Trailer.. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbTrlPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlTrailerStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlTrailerStatus;
			  }
			  
			  if(primTrlMC!=null)
			  {
				       //log.debug("Entering Trailer Primary Policy Check :"+primTrlMC.getPolicyMstId());
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlTrailerStatus;
						  }
						  if(Utility.stringToSqlDate(primTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlTrailerStatus;
						  }
						  //end----02/03
				       
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primTrlMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primTrlMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm != null && (curTerm.equals(paramDate) || curTerm.before(paramDate)))
							  {
								  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primTrlMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
				  			  if(curReins != null && (curReins.equals(paramDate) || curReins.before(paramDate))) {}
				  			  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						/* End Termination */
				       
					   /*Start Self Insured check*/
				     if(GlobalVariables.YES.equals(primTrlMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				       {
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			strOvrUsed=GlobalVariables.YES;
				  			
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
				  	 }
				  	 else if(GlobalVariables.YES.equals(primTrlMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 //log.debug("Exiting Trailer Liability Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				  		if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primTrlMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primTrlMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  	}	
							}
				  		 return arlTrailerStatus;
				  	 }
				   /*End Self Insured check*/
					  
					   
					 
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					 
					  /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  /*check if MC has Blanket or Additional Insured Flag is Yes*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(primTrlMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }  
						  }
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primTrlMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primTrlMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  /*End Additional Insured check*/
					  /**----------------------------------------------------------**/
					  

					  /*Checking Limits and Deductibles if ACV Not present*/
					  if(primTrlMC.getAcv().equals(GlobalVariables.NO)||primTrlMC.getAcv().length()==0)
					  {
						  //log.debug("ACV Not Present hence checking  Limits and Deductibles");
						  /*Calculating Limits and Deductibles along with currency conversion*/
						  dMCHasTILimit=Utility.commaStringtoDouble(primTrlMC.getLimit());
						  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
						  dMCHasDeduct=Utility.commaStringtoDouble(primTrlMC.getDeductible());
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(primTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(primTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  //log.debug("After Currency conversion dMCHasTILimit "+dMCHasTILimit);
						  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
						  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
						  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						  if(excessTrlMC!=null )
						  {
							  //log.debug("Excess Policy Terminated Date:- "+excessTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							 /* if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						 
						  
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  //Commented by piyush as per note on 16 July2007
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/
						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision -"+dEPLimit);
						  //Commented by piyush as per note on 16 July2007				  
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  //log.info("LIMIT TO BE CHECKED");
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
								  //log.debug("Limit Not Ok before multiple Pol Limits check");
								  bLimitNotOkFlg=true;
						  }// end if limit
						  
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)//Commented By Piyush 28March07
						  //if(dEPDeduct>=0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible  
						  }else if(epDtls.getEpAcctNo().equalsIgnoreCase(GlobalVariables.TRAC_ACCOUNT_NO)){
							  if(dMCHasDeduct>0)
								  bDedNotOkFlg=true;
							  
							  log.info("TRAC - dMCHasDeduct ="+dMCHasDeduct+" bDedNotOkFlg ="+bDedNotOkFlg);
						  }
						  
						  //log.debug(">>>>dMCDedBooster ="+dMCDedBooster +" >>>>dMCHasDeduct :"+dMCHasDeduct);
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg||bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }//end check multiple limits and deductibles
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
						  	ArrayList activeEpspcDtls = new ArrayList();
						  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primTrlMC.getMcAcctNo(),GlobalVariables.TRAILERPOLICY);
						  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
						  	{
						  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
						  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
						  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
						  	}
						  	
						  		/*Uday modification 24 May 12 start*/
						  		/* The pending EP SPEC policy also needs to be checked. 
						  		 * This will only be checked when the policy effective date is greater then current date*/
						  	if (!primTrlMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate()))){
						  		ArrayList pendingEpspcDtls = new ArrayList();
						  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
						  				.getEpAcctNo(),
						  				primTrlMC.getMcAcctNo(),
						  				GlobalVariables.TRAILERPOLICY);
						  		if (pendingEpspcDtls != null
						  				&& pendingEpspcDtls.size() > 3) {
						  			dEpSpcPenLim = (Double) pendingEpspcDtls
						  			.get(0);
						  			dEpSpcPenDed = (Double) pendingEpspcDtls
						  			.get(1);
						  			IsPenEpSpcExist = (String) pendingEpspcDtls
						  			.get(2);
						  			strPenEpSpcEffDate = (String) pendingEpspcDtls
						  			.get(3);
						  		}
						  	}
						//log.debug("dEpSpcActLim - "+dEpSpcActLim);
						  //log.debug("dEPLimit - "+dEPLimit);
						  if(bLimitNotOkFlg)
						  {
							  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
							  {
								  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
								  //log.debug("dEpSpcActLim - "+dEpSpcActLim);
								  //log.debug("dEPLimit - "+dEPLimit);
							  }
							  else /* Uday modification 24 May 12 */
								  {
								  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
										  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
										  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
								  {
									  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
									  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
									  //log.debug("dEPLimit - "+dEPLimit);
								  }
								  else
									  {
									  	arlTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
									  }
								  }
						  }						  
						  if(bDedNotOkFlg)
						  {
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  //log.debug(">>>>dMCDedBooster ="+dMCDedBooster +" >>>>dMCHasDeduct :"+dMCHasDeduct);
								  //if(dMCDedBooster>dEPDeduct) //Changed by piyush 27March07
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  {
										  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
										  {}
										  else /* Uday modification 24 May 12 */
										  {
											  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
														  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
														  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
											  		
											  	}
											  	else{
											  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
											  	}
										  }
									  }
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  {
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
													  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
													  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
										  		
										  	}
										  	else{
										  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  	}
									  }
								  }
							  }
						  }
					  }
					  else
					  {
						  log.debug("ACV Present hence skipping Limits but do check Deductibles ");
						  //=============Added By Piyush on 6June'08 According to Debbie's e-mail stated Deductible should be check if ACV is present========= 
						  dMCHasDeduct=Utility.commaStringtoDouble(primTrlMC.getDeductible());
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(primTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(primTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  if(dEPDeduct>0)//Commented By Piyush 28March07
						  {
								  if(dMCHasDeduct>dEPDeduct)
								  {
									  //log.debug("Deductible Not Ok before multiple Pol Limits check");
									  bDedNotOkFlg=true;
								  }// end if deductible  
						  }
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  if(bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if(dMCHasDeduct<=dTempEPDeduct)
									  {
										  bDedNotOkFlg=false;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }
						  if(bDedNotOkFlg)
						  {
							  	ArrayList activeEpspcDtls = new ArrayList();
							  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primTrlMC.getMcAcctNo(),GlobalVariables.TRAILERPOLICY);
							  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
							  	{
							  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
							  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
							  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
							  	}
							  	/*Uday modification 24 May 12 start*/
							  	/* The pending EP SPEC policy also needs to be checked. 
							  	 * This will only be checked when the policy effective date is greater then current date*/
						  		if (!primTrlMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate()))){
								ArrayList pendingEpspcDtls = new ArrayList();
								pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
										.getEpAcctNo(),
										primTrlMC.getMcAcctNo(),
										GlobalVariables.TRAILERPOLICY);
								if (pendingEpspcDtls != null
										&& pendingEpspcDtls.size() > 3) {
									dEpSpcPenLim = (Double) pendingEpspcDtls
											.get(0);
									dEpSpcPenDed = (Double) pendingEpspcDtls
											.get(1);
									IsPenEpSpcExist = (String) pendingEpspcDtls
											.get(2);
									strPenEpSpcEffDate = (String) pendingEpspcDtls
											.get(3);
								}
							}
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else
									  {
										  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
										  {}
										  else /* Uday modification 24 May 12 */
										  {
											  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
														  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
														  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
											  		
											  	}
											  	else{
											  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
											  	}
										  }
									  }
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  {
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
													  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
													  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
										  		
										  	}
										  	else{
										  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  	}
									  }
								  }
							  }
						  }
						  //=============End added By Piyush=====================================
					  }					  
					  if(arlTrailerStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }

					  
				  //log.debug("Exiting Primary Policy Check for Trailer Liability");
			  }//end of primary  Policy check
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlGLStatus is not empty  as it has problems*/
			  if(!bEPPolCheck && epSpecTrlMC!=null )
			  {
				  log.debug("Entering EP Specific Policy Check for Trailer Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  //log.debug("TIPRIMARY PROBLEMS :"+arlTrailerStatus);
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlTrailerStatus.removeAll(arlTrailerStatus);
				  /*Repeating EP Specific same as that in Primary*/
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Trailer Liability */
				  strEPSpcUsed=GlobalVariables.TRAILERPOLICY;
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlTrailerStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlTrailerStatus;
				  }
				  //end----02/03
				 
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecTrlMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecTrlMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecTrlMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /* if(epSpecTrlMC.getPolicyTerminatedDate().length()>0)
				   {
					  log.debug("EP Specific Policy Terminated Date= "+epSpecTrlMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      log.debug(" Policy Termination on parameter date ");
					      arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecTrlMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecTrlMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
				   if((epSpecTrlMC.getSelfInsured().equals(GlobalVariables.YES))&&(epDtls.getEpNeeds().getSelfInsReq().equals(GlobalVariables.NO)))
				   {
					   //log.debug("Self Insured Problem before override check");
					   /*check if override given for Self Insured*/
					   if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
					   {
						   //log.debug("EP has Override for Self Insured");
				  		   strOvrUsed=GlobalVariables.YES;
					   }
					   else if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
					   {
						   //log.debug("EP doesn't allow Self Insured So self Insured Problem");
						   arlTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
					   }
				   }
				   else if((epSpecTrlMC.getSelfInsured().equals(GlobalVariables.YES))&&(epDtls.getEpNeeds().getSelfInsReq().equals(GlobalVariables.YES)))
				   {
					   //log.debug("Exiting GeneralLiability Check (For Self Insured)");
				       //log.debug("To skip All the Policy Test Status Ok as no policy check required");
					   return arlTrailerStatus;
				   }
				   /*End Self Insured check*/
				  
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   if(GlobalVariables.YES.equals(epSpecTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				   {
					   /*MC has RRG and EP doesn't allow RRG*/
					   //log.debug("MC has RRG and EP doesn't allow RRG");
					   /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
				   }
				   
				   /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
				   
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		/*check if MC has Blanket or Additional Insured Flag is Yes*/
					 	  if(GlobalVariables.YES.equals(epSpecTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecTrlMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  } 
				 	  }
				   }
				   else
				   {
				 	  log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecTrlMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
				 				 arlTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecTrlMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK) ");				 				 
				 			  }
				 			  else
				 			  {
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  
				 			  }
				 		  }
				 	  }
				   }
				   /**----------------------------------------------------------**/
				   
				   
				   if(epSpecTrlMC.getAcv().equals(GlobalVariables.NO) || epSpecTrlMC.getAcv().length()==0)
				   {
					   //log.debug("ACV Not Present hence checking Limits and Deductibles");
					   /*Calculating Limits and Deductibles along with currency conversion*/					   	  
					   	  dMCHasTILimit=Utility.commaStringtoDouble(epSpecTrlMC.getLimit());
						  dMCHasDeduct=Utility.commaStringtoDouble(epSpecTrlMC.getDeductible());
						  //log.debug("EPSPCPOL DETAILS LM0:"+dMCHasTILimit+"="+epSpecTrlMC.getPolicyMstId());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						 // if(excessTrlMC!=null )
						  {
							  ////log.debug("Excess Policy Terminated Date:- "+excessTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							  /*if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  //log.debug("EPSPCPOL DETAILS LM1:"+dMCHasTILimit+"="+epSpecTrlMC.getPolicyMstId());
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  /*****
						   * Added by piyush on 16 July2007************************************
						   * According to new waiver specs don't add Limit boosters to MC limit.
						   * Consider limit booster is the new limit of MC that is required by EP.
						   */
						  //Commented by piyush as per note on 16 July2007
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/
						
						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision :"+dEPLimit);
						  //Commented by piyush as per note on 16 July2007
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
								  log.debug("Limit Not Ok before multiple Pol Limits check");
								  bLimitNotOkFlg=true;
						  }// end if limit
						  else
						  {
							  //log.debug("Limit ELSE");
							  bLimitNotOkFlg=false;
						  }
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible 
							  else
							  {
								  bDedNotOkFlg=false;
							  }
						  }
						  						  
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg||bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
							  if(bLimitNotOkFlg)
							  {
								  arlTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
							  //Added by piyush if EPSPC provided along with PRIMARY
							  arlTrailerStatus.remove(GlobalVariables.UVLD_DED_PRBLM);
							  if(bDedNotOkFlg)
							  {
								  
								  if(dMCDedBooster!=0)
								  {
									  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
									  if(dMCDedBooster>dEPDeduct)
									  {
										  //log.debug("Ded problem after replacing Booster");
										  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
										  else	
										  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  }
								  }
								  else
								  {
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  
				   }//end of ACV check
				   else
				   {
					   log.debug("ACV Present hence skipping Limits but do check Deductibles");
					   //=================Added by Piyush 06Jun'08===================================
						  dMCHasDeduct=Utility.commaStringtoDouble(epSpecTrlMC.getDeductible());
						  //log.debug("EPSPCPOL DETAILS LM0:"+dMCHasTILimit+"="+epSpecTrlMC.getPolicyMstId());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  if(dEPDeduct>0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible 
							  else
							  {
								  bDedNotOkFlg=false;
							  }
						  }
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {									  
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if(dMCHasDeduct<=dTempEPDeduct)
									  {
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg							  
						  }
						  arlTrailerStatus.remove(GlobalVariables.UVLD_DED_PRBLM);
						  if(bDedNotOkFlg)
						  {
							  
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCDedBooster>dEPDeduct)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
					   //=================End added by Piyush=========================================
				   }
				      
				  //log.debug("Exiting EP Specific Policy Check for Trailer Liability");
			  }
			  
			  if(primTrlMC==null && epSpecTrlMC==null)
			  {
				  arlTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlTrailerStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Trailer Liability Check for UValid:- ", exp );
		}
		//log.info("Business: Exiting method checkTrailerLiability()of UValidPolicyCheck class:- "+arlTrailerStatus);
		return arlTrailerStatus;
	}
	/**
	 * This method will check Refrigerated Trailer Policy requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkRefTrailerLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		log.info("Business: Entering method checkRefTrailerLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlRefTrailerStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbRefTrlPrimary= new StringBuffer(GlobalVariables.REFTRAILER);
			  sbRefTrlPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbRefTrlExcess= new StringBuffer(GlobalVariables.REFTRAILER);
			  sbRefTrlExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.REFTRAILER);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  RefTrailerBean primRefTrlMC=(RefTrailerBean)mcInsDtls.get(sbRefTrlPrimary.toString());
			  RefTrailerBean excessRefTrlMC=(RefTrailerBean)mcInsDtls.get(sbRefTrlExcess.toString());
			  RefTrailerBean epSpecRefTrlMC=null;
				  //(RefTrailerBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  epSpecRefTrlMC=(RefTrailerBean)arCg.get(x);
					  if(epSpecRefTrlMC.getPolicyMstId()==0)
					  {
						  //epSpecRefTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecRefTrlMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecRefTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecRefTrlMC=null;
					  }
				  }
			  }
			  UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  /*
			  String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasTILimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExTILimit=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.REFTRAILER.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlRefTrailerStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				 // arlRefTrailerStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlRefTrailerStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlRefTrailerStatus;
			  }
			  //======================End added by Piyush ==========================
			  
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires Ref Trailer.. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbRefTrlPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlRefTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlRefTrailerStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlRefTrailerStatus;
			  }

			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			
			  if(primRefTrlMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for General Liability");
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlRefTrailerStatus;
						  }
						  if(Utility.stringToSqlDate(primRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlRefTrailerStatus;
						  }
						  //end----02/03
				       
				       /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primRefTrlMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primRefTrlMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primRefTrlMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primRefTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
					   /*Start Self Insured check*/
					  if(GlobalVariables.YES.equals(primRefTrlMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			strOvrUsed=GlobalVariables.YES;
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlRefTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
				  	 }
				  	 else if(GlobalVariables.YES.equals(primRefTrlMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 //log.debug("Exiting Ref Trailer Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				  		 return arlRefTrailerStatus;
				  	 }
					 /*End Self Insured check*/
					   
					 
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  
					  if(GlobalVariables.YES.equals(primRefTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					   
					  /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  /*check if MC has Blanket or Additional Insured Flag is Yes*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primRefTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(primRefTrlMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }  
						  }
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primRefTrlMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primRefTrlMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");
									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
								  }
							  }
						  }
					  }
					  /*End Additional Insured check*/
					  /**----------------------------------------------------------**/
					  
					  
					  /*Checking Limits and Deductibles if ACV Not present*/
					  if(primRefTrlMC.getAcv().equals(GlobalVariables.NO) || primRefTrlMC.getAcv().length()==0)
					  {
						  //log.debug("ACV Not Present hence checking  Limits and Deductibles");
						  /*Calculating Limits and Deductibles along with currency conversion*/
						  dMCHasTILimit=Utility.commaStringtoDouble(primRefTrlMC.getLimit());
						  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
						  dMCHasDeduct=Utility.commaStringtoDouble(primRefTrlMC.getDeductible());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(primRefTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(primRefTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  //log.debug("After Currency conversion dMCHasTILimit "+dMCHasTILimit);
						  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
						  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
						  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						  //if(excessRefTrlMC!=null )
						  {
							 // //log.debug("Excess Policy Terminated Date:- "+excessRefTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							 /* if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getRefTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						  
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/
						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
						  }// end if limit
						  
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)//Commented by piyush 28March07
						  //if(dEPDeduct>=0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible  
						  }
						  
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }//end check multiple limits and deductibles
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
						  if(bLimitNotOkFlg)
						  {
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
						  }
						  if(bDedNotOkFlg)
						  {
							  
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27March07
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
					  }
					  else
					  {
						  //log.debug("ACV Present hence skipping Limits and Deductibles Check");
					  }
					  
					  if(arlRefTrailerStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }
				  //log.debug("Exiting Primary Policy Check for Trailer Liability");  
			  }// end of primary check
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlGLStatus is not empty  as it has problems*/
			  else if(!bEPPolCheck && epSpecRefTrlMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for Trailer Liability");
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  arlRefTrailerStatus.removeAll(arlRefTrailerStatus);
				  
				  /*Repeating EP Specific same as that in Primary*/
				
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Trailer Liability */
				  strEPSpcUsed=GlobalVariables.REFTRAILER;
				 
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlRefTrailerStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlRefTrailerStatus;
				  }
				  //end----02/03
				  
			       /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecRefTrlMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecRefTrlMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecRefTrlMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /* if(epSpecRefTrlMC.getPolicyTerminatedDate().length()>0)
				   {
					  //log.debug("EP Specific Policy Terminated Date= "+epSpecRefTrlMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug(" Policy Terminated on parameter date ");
					      arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecRefTrlMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecRefTrlMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(epSpecRefTrlMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlRefTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecRefTrlMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting Ref Trailer Liability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlRefTrailerStatus;
					 }
				   /*End Self Insured check*/
				   
				  
				   
				   
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   if(GlobalVariables.YES.equals(epSpecRefTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				   /*Start Additional Insured check
				   * Ep requires Additional Insured and policy doesn't have Additional Insured 
				   * (check blanket and ep blanket ok or Policy endorsed for this EP
				   * */
				   
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		if(GlobalVariables.YES.equals(epSpecRefTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecRefTrlMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  } 
				 	  }
				   }
				   else
				   {
				 	  //log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecRefTrlMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlRefTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
						 		  }									  									  					 				 				 				 
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecRefTrlMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK) ");
				 				 
				 			  }
				 			  else
				 			  {
				 				  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  									  					 				 				 				 
				 			  }
				 		  }
				 	  }
				   }
				   /**----------------------------------------------------------**/
				   
				   
				   if(epSpecRefTrlMC.getAcv().equals(GlobalVariables.NO)|| epSpecRefTrlMC.getAcv().length()==0)
				   {
					   //log.debug("ACV Not Present hence checking Limits and Deductibles");
					   /*Calculating Limits and Deductibles along with currency conversion*/
					      dMCHasTILimit=Utility.commaStringtoDouble(epSpecRefTrlMC.getLimit());
						  dMCHasDeduct=Utility.commaStringtoDouble(epSpecRefTrlMC.getDeductible());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  
						  if(epSpecRefTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessRefTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessRefTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(epSpecRefTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							 /* if(excessRefTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessRefTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						 // if(excessTrlMC!=null )
						  {
							  ////log.debug("Excess Policy Terminated Date:- "+excessTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							  /*if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getRefTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/

						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
						  }// end if limit
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible  
						  }
						  
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
							  if(bLimitNotOkFlg)
							  {
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
							  if(bDedNotOkFlg)
							  {
								  if(dMCDedBooster!=0)
								  {
									  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
									  if(dMCDedBooster>dEPDeduct)
									  {
										  //log.debug("Ded problem after replacing Booster");
										  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
										  else	
										  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  }
								  }
								  else
								  {
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
				   }//end of ACV check
				   else
				   {
					   //log.debug("ACV Present hence skipping Limits and Deductibles Check");
				   }
				  //log.debug("Exiting EP Specific Policy Check for Ref Trailer Liability");
			  }
			  else
			  {
				  arlRefTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlRefTrailerStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Refrigerated Trailer Liability Check for UValid:- ", exp );
		}
		log.info("Business: Exiting method checkRefTrailerLiability()of UValidPolicyCheck class:- "+arlRefTrailerStatus);
		return arlRefTrailerStatus;
	}
	/**
	 * This method will check Workers Compensation requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @param  String strELCallFlag  //used whether to call EL check method or not in the calling function
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkWCLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,String strELCallFlag) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * 
		 * strELCallFlag will be No by Default and will be used to check in the callling method to decide
		 * whether to call EL soon after this method..  
		 * */
		//log.info("Business: Entering method checkWCLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("strELCallFlag "+strELCallFlag);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlWCStatus= new ArrayList(5);
  	    double dWC_ELA=0.0; 
	    double dWC_ELP=0.0;
		double dWC_ELE=0.0;

		try
		{
			  //boolean bLimitNotOkFlg=false;
			 // boolean bDedNotOkFlg=false;
			  //boolean bOvrUsed=false;
			  
			  StringBuffer sbWCPrimary= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbWCPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  WCBean primWCMC=(WCBean)mcInsDtls.get(sbWCPrimary.toString());
			  WCBean epSpecWCMC=null;
				  //(WCBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  //log.debug("EPSC");
				  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  //log.debug("EPSC"+x);
					  epSpecWCMC=(WCBean)arCg.get(x);
					  //log.debug("EPSCA"+x);
					  if(epSpecWCMC.getPolicyMstId()==0)
					  {
						  //epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecWCMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //log.debug("EPSCB"+x);
						  //epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecWCMC=null;
					  }
				  }
			  }
			  //log.debug("EPSCC");
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  //UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  
			  //String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  //String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  
			   /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.WORKCOMP.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");				  
				  return arlWCStatus;				
			  }
			  
			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  //log.debug("WC BOOSTER :"+epDtls.getEpOvrMCBean().getLimitBooster());
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlWCStatus;
			  }
			  //======================End added by Piyush ==========================
			  //log.debug("EPSCCD");
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbWCPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlWCStatus.add(GlobalVariables.WCUNEEDUHAVE);
					  arlWCStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlWCStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");				  
				  return arlWCStatus;
			  }
			  			  
			  if(primWCMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for Workers Compensation");
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				          if(primWCMC.getStrUnUh().equals(GlobalVariables.WCUNEEDUHAVE))
						  {
							//log.debug("If dummy policy created for the sack of displaying the problems with WC");
							arlWCStatus.add(GlobalVariables.WCUNEEDUHAVE);
							arlWCStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
							return arlWCStatus;
						  }
				       
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlWCStatus;
						  }
						  if(Utility.stringToSqlDate(primWCMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlWCStatus;
						  }
						  //end----02/03
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primWCMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primWCMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primWCMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primWCMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primWCMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  
					  if(GlobalVariables.YES.equals(primWCMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlWCStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				      
					  /*Check MC Exempt and to update whether to call EL Check Required method or not*/
					  if(primWCMC.getExempt().equals(GlobalVariables.YES)  )
					  {
						  //log.debug("MC has selected Emempt =Yes");
						  if(epDtls!=null && epDtls.getEpNeeds()!=null && epDtls.getEpNeeds().getPolicyReq().equals(GlobalVariables.YES))
						  {
							  /*If MC Selects Exempt in WC and EP has selected WC Required means it doesn't allow exemption*/
							  //log.debug("EP doesn't allow Exemption before Overrides");
							  if(epDtls!=null && epDtls.getEpOvrMCBean()!=null &&  epDtls.getEpOvrMCBean().getPolicyReq().equals(GlobalVariables.YES))
							  {
								  //log.debug("After checking overrides WC Exemption problem");
								  arlWCStatus.add(GlobalVariables.UVLD_WC_EXEMPTION);  
							  }
							  else
							  {
								  //log.debug("EL Check not required so setting strELCallFlag =No after checking override");
								  strELCallFlag=GlobalVariables.NO;
							  }
						  }
						  else 
						  {
							  //log.debug("EL Check not required so setting strELCallFlag =No");
							  strELCallFlag=GlobalVariables.NO;
						  }
					  }
					  else
					  {
						 //log.debug("MC has selected Emempt =No");
						  dWC_ELA=Utility.commaStringtoDouble(primWCMC.getElEachOccur()); //EL Each Occur is EL Accident
						  dWC_ELE=Utility.commaStringtoDouble(primWCMC.getElDisEAEmp());
						  dWC_ELP=Utility.commaStringtoDouble(primWCMC.getElDisPlcyLmt());
						  /*Start Self Insured check*/
						  	if(GlobalVariables.YES.equals(primWCMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
							 {
								 /*MC has SI and EP doesn't allow Self Insured*/
								 //log.debug("MC has SI and EP doesn't allow Self Insured");
								 /*Check if EP has given overrides*/
								 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
								 {
									//log.debug("EP has Override for Self Insured");
									strELCallFlag=GlobalVariables.NO;
								 }
								 else
								 {
									 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
									 arlWCStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
								 }
							 }
							 else if(GlobalVariables.YES.equals(primWCMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
							 {
								 //log.debug("Exiting WC Check (after Self Insured Check)");
								 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
								 return arlWCStatus;
							 }
							 else if(GlobalVariables.NO.equals(primWCMC.getSelfInsured()) && ((dWC_ELA+dWC_ELE+dWC_ELP)>0))
							 {
								   //log.debug("MC is not Self Insured so EL Check Required");
								   strELCallFlag=GlobalVariables.YES;
							 }
						   /*End Self Insured check*/
					  }
					  if(arlWCStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }
					  
				      //log.debug("Exiting Primary Policy Check for Workers Compensation");
				      
			  }
			  else if(!bEPPolCheck && epSpecWCMC!=null )
			  {
				  		//log.debug("Entering EP Specific Policy Check for Workers Compensation");
				  		
				  		/*Making the arrayList of Problems empty before starting EP Specific Check...*/
						arlWCStatus.removeAll(arlWCStatus);
				  		strEPSpcUsed=GlobalVariables.WORKCOMP;
				  		
				  		 //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlWCStatus;
						  }	
						  if(Utility.stringToSqlDate(epSpecWCMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlWCStatus;
						  }

						  //end----02/03
						  
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(epSpecWCMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(epSpecWCMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(epSpecWCMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(epSpecWCMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(epSpecWCMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						  
				  		/*if(epSpecWCMC.getPolicyTerminatedDate().length()>0)
						  {
							  //log.debug("Primary Policy Terminated Date= "+epSpecWCMC.getPolicyTerminatedDate());
							  if(Utility.stringToSqlDate(epSpecWCMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
							      //log.debug("Primary Policy Termination date greater than parameter date ");
							      arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  } else if(epSpecWCMC.getPolicyReinstatedDate().length()>0)
						  {
							  log.debug("Primary Policy Reinstated Date:- "+epSpecWCMC.getPolicyReinstatedDate());
							  if(Utility.stringToSqlDate(epSpecWCMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								  log.debug("Policy Reinstated but have future reinstatement date");
								  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }*/
						  
						  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  		if(GlobalVariables.YES.equals(epSpecWCMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  		{
				  		  /*MC has RRG and EP doesn't allow RRG*/
				  		  //log.debug("MC has RRG and EP doesn't allow RRG");
				  		  /*Check if EP has given overrides*/
				  		  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
				  		  {
				  			  //log.debug("EP has Override for RRG");
				  			  strOvrUsed=GlobalVariables.YES;
				  		  }
				  		  else
				  		  {
				  			  //log.debug("EP doesn't allow RRG So RRG Problem");
				  			  arlWCStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
				  		  }
				  		}
				  		
						  /*Check MC Exempt and to update whether to call EL Check Required method or not*/
						  if(epSpecWCMC.getExempt().equals(GlobalVariables.YES)  )
						  {
							  //log.debug("MC has selected Emempt =Yes");
							  if(epDtls!=null && epDtls.getEpNeeds()!=null && epDtls.getEpNeeds().getPolicyReq().equals(GlobalVariables.YES))
							  {
								  /*If MC Selects Exempt in WC and EP has selected WC Required means it doesn't allow exemption*/
								  //log.debug("EP doesn't allow Exemption before Overrides");
								  if(epDtls!=null && epDtls.getEpOvrMCBean()!=null &&  epDtls.getEpOvrMCBean().getPolicyReq().equals(GlobalVariables.YES))
								  {
									  //log.debug("After checking overrides WC Exemption problem");
									  arlWCStatus.add(GlobalVariables.UVLD_WC_EXEMPTION);  
								  }
								  else
								  {
									  //log.debug("EL Check not required so setting strELCallFlag =No after checking override");
									  strELCallFlag=GlobalVariables.NO;
								  }
							  }
							  else 
							  {
								  //log.debug("EL Check not required so setting strELCallFlag =No");
								  strELCallFlag=GlobalVariables.NO;
							  }
						  }
						  else
						  {
							  //log.debug("MC has selected Emempt =No");
							  /*Start Self Insured check*/
							  if(GlobalVariables.YES.equals(epSpecWCMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
								 {
									 /*MC has SI and EP doesn't allow Self Insured*/
									 //log.debug("MC has SI and EP doesn't allow Self Insured");
									 /*Check if EP has given overrides*/
									 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
									 {
										//log.debug("EP has Override for Self Insured");
										strELCallFlag=GlobalVariables.NO;
										strOvrUsed=GlobalVariables.YES;
									 }
									 else
									 {
										 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
										 arlWCStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
									 }
								 }
								 else if(GlobalVariables.YES.equals(epSpecWCMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
								 {
									 //log.debug("Exiting WC Check (after Self Insured Check)");
									 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
									 return arlWCStatus;
								 }
								 else if(GlobalVariables.NO.equals(epSpecWCMC.getSelfInsured()))
								 {
									   //log.debug("MC is not Self Insured so EL Check Required");
									   strELCallFlag=GlobalVariables.YES;
								 }
							   /*End Self Insured check*/
						  }
				  		//log.debug("Exiting EP Specific Policy Check for Workers Compensation");
			  }
			  else
			  {
				  arlWCStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlWCStatus;
			  }
			  //log.debug("epDtls before strELCallFlag:- "+epDtls.getElFlag());
			  if(GlobalVariables.YES.equals(strELCallFlag))
			  {
				  //log.debug("Setting YES");
				  epDtls.setElFlag(GlobalVariables.YES);
			  }
			  else if(GlobalVariables.NO.equals(strELCallFlag))
			  {
				  //log.debug("Setting NO");
				  epDtls.setElFlag(GlobalVariables.NO);
			  }
			  //log.debug("epDtls after strELCallFlag:- "+epDtls.getElFlag());
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in WC Check for UValid:- ", exp );
		}
		//log.info("Business: Exiting method checkWCLiability()of UValidPolicyCheck class:- "+arlWCStatus);
		//log.info("ELCallCheckRequired Flag:- "+strELCallFlag);
		return arlWCStatus;
	}
	
	/**
	 * This method will check Employers Liability requirement for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkEmpLiability(HashMap mcInsDtlsEL,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		/* Two MC Insurance details HashMap are sent.. One for this function (EL and other for WC)*/
	
		//log.info("Business: Entering method checkEmpLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtlsEL "+mcInsDtlsEL);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlELStatus= new ArrayList(5);
		
		try
		{
			
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			 
			  StringBuffer sbWCPrimary= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbWCPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbELPrimary= new StringBuffer(GlobalVariables.EMPLIABILITY);
			  sbELPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbELEPSpecPolicy= new StringBuffer(GlobalVariables.EMPLIABILITY);
			  sbELEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  StringBuffer sbWCEPSpecPolicy= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbWCEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  
			  ELBean primELMC=(ELBean)mcInsDtlsEL.get(sbELPrimary.toString());
			  ELBean epSpecELMC= null;
			  if(mcInsDtlsEL.containsKey(sbELEPSpecPolicy.toString()))
			  {
				  ArrayList arAuto = new ArrayList();
				  arAuto = (ArrayList)mcInsDtlsEL.get(sbELEPSpecPolicy.toString());
				  for(int x=0;x<arAuto.size();x++)
				  {
					  epSpecELMC=(ELBean)arAuto.get(x);
					  if(epSpecELMC.getPolicyMstId()==0)
					  {
						  //epSpecELMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecELMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecELMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecELMC=null;
					  }
				  }
			  }
			  WCBean primWCMC=(WCBean)mcInsDtlsEL.get(sbWCPrimary.toString());
			  WCBean epSpecWCMC= null;
		  	  if(mcInsDtlsEL.containsKey(sbWCEPSpecPolicy.toString()))
		  		  epSpecWCMC= (WCBean)((ArrayList)mcInsDtlsEL.get(sbWCEPSpecPolicy.toString())).get(0);
		  	  
			  if(mcInsDtlsEL.containsKey(sbWCEPSpecPolicy.toString()))
			  {
				  ArrayList arAuto = new ArrayList();
				  arAuto = (ArrayList)mcInsDtlsEL.get(sbWCEPSpecPolicy.toString());
				  for(int x=0;x<arAuto.size();x++)
				  {
					  epSpecWCMC=(WCBean)arAuto.get(x);
					  if(epSpecWCMC.getPolicyMstId()==0)
					  {
						  epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecWCMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecWCMC=null;
					  }
				  }
			  }	  
			  
			  UmbBean umbWCELMC=(UmbBean)mcInsDtlsEL.get(GlobalVariables.UMBRELLA);
			  
			  //log.debug("primELMC:- "+primELMC);
			  //log.debug("epSpecELMC:- "+epSpecELMC);
			  /*String  strAICheck=(String)mcInsDtlsEL.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtlsEL.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific*/
			  
			  double dMC_Limit_ELA=0.0;
			  double dMC_Limit_ELE=0.0;
			  double dMC_Limit_ELP=0.0;
			  double dEPDeduct=0.0;
			  double dMCHasDeduct=0.0;
			  double dWC_ELA=0.0; 
			  double dWC_ELP=0.0;
			  double dWC_ELE=0.0;
			  double dEL_ELA=0.0;
			  double dEL_ELP=0.0;
			  double dEL_ELE=0.0;
			  double dMCUmbrella=0.0;
			  double dMCDedBooster=0.0;
			  double dEP_Limit_ELA=0.0;
			  double dEP_Limit_ELE=0.0;
			  double dEP_Limit_ELP=0.0;
			  double dMC_ELA_Booster=0.0;
			  double dMC_ELE_Booster=0.0;
			  double dMC_ELP_Booster=0.0;
			  
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.EMPLIABILITY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlELStatus;
			  }

			  //Added by piyush
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlELStatus;
			  }
			  //======================End added by Piyush ==========================
			  
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if((primWCMC!=null && primWCMC.getStrWCELflg().equals(GlobalVariables.YES)) || (epSpecWCMC!=null && epSpecWCMC.getStrWCELflg().equals(GlobalVariables.YES)))
				  {
					  log.debug("As EL calling from WC so skipping NO POLICY Check");	
				  }
				  else
				  {
					  if(!(mcInsDtlsEL.containsKey(sbELPrimary.toString()) || mcInsDtlsEL.containsKey(sbELEPSpecPolicy.toString())))
					  {
						  log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
						  arlELStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
						  return arlELStatus;
					  }
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlELStatus;
			  }
			  
			  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
			  if(primELMC!=null)
			  {
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");					  
					arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlELStatus;
				  }
				  if(Utility.stringToSqlDate(primELMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlELStatus;
				  }
	
			  }
			  //end----02/03
			  
			  if(epDtls!=null)
			  {
				  dEP_Limit_ELA=Utility.commaStringtoDouble(epDtls.getEpNeeds().getELA());
				  dEP_Limit_ELE=Utility.commaStringtoDouble(epDtls.getEpNeeds().getELE());
				  dEP_Limit_ELP=Utility.commaStringtoDouble(epDtls.getEpNeeds().getELP());
				  dMC_ELA_Booster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getELABooster());
				  dMC_ELE_Booster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getELEBooster());
				  dMC_ELP_Booster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getELPBooster());
				  /*Added to get deductibles*/
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  //log.info("dEP_Limit_ELA :- "+dEP_Limit_ELA+ " dEP_Limit_ELE:- "+dEP_Limit_ELE +" dEP_Limit_ELP:- "+dEP_Limit_ELP );
			  
			  /*Primary & EP Policy Check if only WC is to be checked.....  i.e WC Check Flag will be set....*/
			  if(GlobalVariables.YES.equals(epDtls.getElFlag()))
			  {
				  //log.debug("Entering check for only WC after checking EL Flag in EPDetails Bean........");
				  /*For WC*/
				  if(primWCMC!=null)
				  {
					  log.debug("WC Primary check ");
					  
					  
					  dWC_ELA=Utility.commaStringtoDouble(primWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(primWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(primWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(primWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(primWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						 
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;

						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
					  
					  if(umbWCELMC!=null && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES)) || ((dWC_ELA+dWC_ELE+dWC_ELP>0) &&  umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
						  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit());
					  
					  if(primWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  if(umbWCELMC!=null && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES)) || ((dWC_ELA+dWC_ELE+dWC_ELP>0) &&  umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
							  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*cndUSD ;
					  }
					  else if(primWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  if(umbWCELMC!=null && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES)) || ((dWC_ELA+dWC_ELE+dWC_ELP>0) &&  umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
							  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*mexUSD ;
					  }

					  if(primELMC!=null)
					  {
						  //log.debug("After checking null condn for primWCMC for Getting ELA ELE ELP for WC ");
						  dEL_ELA=Utility.commaStringtoDouble(primELMC.getElEachOccur()); //EL Each Occur is EL Accident
						  dEL_ELE=Utility.commaStringtoDouble(primELMC.getElDisEAEmp());
						  dEL_ELP=Utility.commaStringtoDouble(primELMC.getElDisPlcyLmt());
						  /*Currency conversion*/
						  if(primELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  dEL_ELA=dEL_ELA*cndUSD;
							  dEL_ELE=dEL_ELE*cndUSD;
							  dEL_ELP=dEL_ELP*cndUSD;
						  }
						  else if(primELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  dEL_ELA=dEL_ELA*mexUSD;
							  dEL_ELE=dEL_ELE*mexUSD;
							  dEL_ELP=dEL_ELP*mexUSD;
						  }
					  }
					  dMC_Limit_ELA=dWC_ELA +dEL_ELA+dMCUmbrella;
					  dMC_Limit_ELE=dWC_ELE +dEL_ELE+dMCUmbrella;
					  dMC_Limit_ELP=dWC_ELP +dEL_ELP+dMCUmbrella;
					  					  
					  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
					  
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  log.debug("Setting Override used flag");
						  bOvrUsed=true;
					  }
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
					  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
					  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/					  
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))					  
					  //if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  if(bOvrUsed && (dMC_ELA_Booster==0 || dMC_ELE_Booster==0 || dMC_ELP_Booster==0))						  
					  {
						  bLimitNotOkFlg=true;
					  }
					  else if(dMC_Limit_ELA<dMC_ELA_Booster || dMC_Limit_ELE <dMC_ELE_Booster || dMC_Limit_ELP <dMC_ELP_Booster)
					  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
					  }// end if limit

					  if(bLimitNotOkFlg)
					  {
						  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  if(dMCDedBooster>dEPDeduct)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  /*if(arlELStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }*/
					  log.debug("Returning from only WC... primary");
					  //return arlELStatus;
				  }
				  else if(epSpecWCMC!=null)
				  {
					  //log.debug("WC EP Specific check ");
					  dWC_ELA=Utility.commaStringtoDouble(epSpecWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(epSpecWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(epSpecWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						 
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
					  
					  if(umbWCELMC!=null && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES)) || ((dWC_ELA+dWC_ELE+dWC_ELP>0) &&  umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
						  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit());
					  
					  if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  if(umbWCELMC!=null && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES)) || ((dWC_ELA+dWC_ELE+dWC_ELP>0) &&  umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
							  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*cndUSD ;
					  }
					  else if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  if(umbWCELMC!=null && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES)) || ((dWC_ELA+dWC_ELE+dWC_ELP>0) &&  umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
							  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*mexUSD ;
					  }

					  if(epSpecELMC!=null)
					  {
						  //log.debug("After checking null condn for primWCMC for Getting ELA ELE ELP for WC ");
						  dEL_ELA=Utility.commaStringtoDouble(epSpecELMC.getElEachOccur()); //EL Each Occur is EL Accident
						  dEL_ELE=Utility.commaStringtoDouble(epSpecELMC.getElDisEAEmp());
						  dEL_ELP=Utility.commaStringtoDouble(epSpecELMC.getElDisPlcyLmt());
						  /*Currency conversion*/
						  if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dEL_ELA=dEL_ELA*cndUSD;
							  dEL_ELE=dEL_ELE*cndUSD;
							  dEL_ELP=dEL_ELP*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
						  }
						  else if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							 
							  dEL_ELA=dEL_ELA*mexUSD;
							  dEL_ELE=dEL_ELE*mexUSD;
							  dEL_ELP=dEL_ELP*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
						  }
					  }
					  
					  dMC_Limit_ELA=dWC_ELA +dEL_ELA+dMCUmbrella;
					  dMC_Limit_ELE=dWC_ELE +dEL_ELE+dMCUmbrella;
					  dMC_Limit_ELP=dWC_ELP +dEL_ELP+dMCUmbrella;
					  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
					  
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  //if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Setting Override used flag");
						  bOvrUsed=true;
					  }
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
					  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
					  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
					  if(bOvrUsed && (dMC_ELA_Booster==0 || dMC_ELE_Booster==0 || dMC_ELP_Booster==0))						  
					  {
						  bLimitNotOkFlg=true;
					  }
					  else if(dMC_Limit_ELA<dMC_ELA_Booster || dMC_Limit_ELE <dMC_ELE_Booster || dMC_Limit_ELP <dMC_ELP_Booster)
					  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
					  }// end if limit
					  if(bLimitNotOkFlg)
					  {
						  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					 /* if(arlELStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }*/
					  //log.debug("Returning from only WC... EP Specific");
					  return arlELStatus;
				  }
				  //log.debug("bEPPolCheck:- "+ bEPPolCheck +"epSpecELMC:- "+epSpecELMC);
				  //log.debug("Exiting check for only WC........");
			  }
			  /*For Primary Policy check... For EL..*/
			  if(primELMC!=null)
			  {
				  log.debug("Entering Primary Policy Check for Employers Liability");
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(primELMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(primELMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(primELMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(primELMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(primELMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Exempt Check*/
				  if(primWCMC!=null)
				  {
					  if(GlobalVariables.YES.equals(primWCMC.getExempt()) || GlobalVariables.YES.equals(primWCMC.getUnlmtdElLimits()))
					  {
						  //log.debug("WC Exempt is Yes,EL Ok so skipping EL Check");
						  return arlELStatus;
					  }  
				  }
				  
				  
				  
				   /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(primELMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							/*Check if EP doesn't allow SI*/
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlELStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(primELMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting WC Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlELStatus;
					 }
				   /*End Self Insured check*/
				  
				  
				  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  if(GlobalVariables.YES.equals(primELMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  {
					  /*MC has RRG and EP doesn't allow RRG*/
					  //log.debug("MC has RRG and EP doesn't allow RRG");
					  /*Check if EP has given overrides*/
					  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
					  {
						  //log.debug("EP has Override for RRG");
						  strOvrUsed=GlobalVariables.YES;
					  }
					  else
					  {
						  //log.debug("EP doesn't allow RRG So RRG Problem");
						  arlELStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
					  }
				  }

				  
				  /*Getting Limits and Deductibles for WC & EL*/
				  /*For EL*/
				  dEL_ELA=Utility.commaStringtoDouble(primELMC.getElEachOccur()); //EL Each Occur is EL Accident
				  dEL_ELE=Utility.commaStringtoDouble(primELMC.getElDisEAEmp());
				  dEL_ELP=Utility.commaStringtoDouble(primELMC.getElDisPlcyLmt());
				  
				  dMCHasDeduct=Utility.commaStringtoDouble(primELMC.getDeductible());
				  /*Currency conversion for EL..*/
				  if(primELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  dEL_ELA=dEL_ELA*cndUSD;
					  dEL_ELE=dEL_ELE*cndUSD;
					  dEL_ELP=dEL_ELP*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
				  }
				  else if(primELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  dEL_ELA=dEL_ELA*mexUSD;
					  dEL_ELE=dEL_ELE*mexUSD;
					  dEL_ELP=dEL_ELP*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
				  }				  
				  
				  /*For WC*/
				  if(primWCMC!=null)
				  {
					  //log.debug("After checking null condn for primWCMC for Getting ELA ELE ELP for WC ");
					  dWC_ELA=Utility.commaStringtoDouble(primWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(primWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(primWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(primWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(primWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						 
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
				  }
				  
				  if((umbWCELMC!=null && (primELMC.getWcELFlag().equals(GlobalVariables.YES) || (dWC_ELA+dWC_ELE+dWC_ELP)>0) && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES))) || (umbWCELMC!=null && umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
					  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit());
				  
				  if(primELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  if((umbWCELMC!=null && (primELMC.getWcELFlag().equals(GlobalVariables.YES) || (dWC_ELA+dWC_ELE+dWC_ELP)>0) && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES))) || (umbWCELMC!=null && umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
						  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*cndUSD ;
				  }
				  else if(primELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  if((umbWCELMC!=null && (primELMC.getWcELFlag().equals(GlobalVariables.YES) || (dWC_ELA+dWC_ELE+dWC_ELP)>0) && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES))) || (umbWCELMC!=null && umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
						  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*mexUSD ;
				  }

				  
				  log.debug("After Currency conversion dEL_ELA "+ dEL_ELA+" dEL_ELE "+ dEL_ELE + "dEL_ELP "+dEL_ELP);
				  log.debug("After Currency conversion dWC_ELA "+ dWC_ELA+" dWC_ELE "+ dWC_ELE + "dWC_ELP "+dWC_ELP);
				  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				 /* if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getELReqd()))
				  {
					  if(umbGenMC.getPolicyTerminatedDate().length()>0)
					  {
						  if(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate)))
						  {
							  //log.debug("Umbrella policy termination date less than equal to  parameter date ");
							  
						  }
					  }
				  }*/
				  dMC_Limit_ELA=dWC_ELA+dEL_ELA+dMCUmbrella;
				  dMC_Limit_ELE=dWC_ELE+dEL_ELE+dMCUmbrella;
				  dMC_Limit_ELP=dWC_ELP+dEL_ELP+dMCUmbrella;
				  
				  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
				  
				  
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
				  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Setting Override used flag");
					  bOvrUsed=true;
				  }
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)//Commented by piyush 28March07
				  //if(dEPDeduct>=0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }				  
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
				  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
				  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
				  
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
				  //log.info("dMC_ELA_Booster:- "+dMC_ELA_Booster+"  dMC_ELE_Booster:- "+dMC_ELE_Booster +" dMC_ELP_Booster:- "+dMC_ELP_Booster);
				  if(bOvrUsed && (dMC_ELA_Booster==0 || dMC_ELE_Booster==0 || dMC_ELP_Booster==0))						  
				  {
					  bLimitNotOkFlg=true;
				  }
				  else if(dMC_Limit_ELA<dMC_ELA_Booster || dMC_Limit_ELE <dMC_ELE_Booster || dMC_Limit_ELP <dMC_ELP_Booster)
						  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
				  }// end if limit
				  
				  if(bLimitNotOkFlg)
				  {
					  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(dMCDedBooster!=0)
					  {
						  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
						  if(dMCDedBooster>dEPDeduct)
						  {
							  //log.debug("Ded problem after replacing Booster");
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  else
					  {
						  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
						  else	
						  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
					  }
				  }
				  if(arlELStatus.size()==0)
				  {
				    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
				    bEPPolCheck=true;
				  }
				  log.debug("Exiting Primary Policy Check for Employers Liability");
			  }
			  if(!bEPPolCheck && epSpecELMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for Employers Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  arlELStatus.removeAll(arlELStatus); 
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlELStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecELMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlELStatus;
				  }
				  //end----02/03
				
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecELMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecELMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecELMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecELMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecELMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  

				  /*Start Check Policy Status (Termination|Cancellation) */
				  /*if(epSpecELMC.getPolicyTerminatedDate().length()>0)
				  {
					  //log.debug("Primary Policy Terminated Date= "+epSpecELMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecELMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecELMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Primary Policy Terminated on parameter date ");
					      arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  } else if(epSpecELMC.getPolicyReinstatedDate().length()>0)
				  {
					  log.debug("Primary Policy Reinstated Date:- "+epSpecELMC.getPolicyReinstatedDate());
					  if(Utility.stringToSqlDate(epSpecELMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
					  {
						  log.debug("Policy Reinstated but have future reinstatement date");
						  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }*/
			
				  if(epSpecWCMC!=null)
				  {
					  /*Exempt Check*/
					  if(GlobalVariables.YES.equals(epSpecWCMC.getExempt()) || GlobalVariables.YES.equals(epSpecWCMC.getUnlmtdElLimits()))
					  {
						  //log.debug("WC Exempt is Yes,EL Ok so skipping EL Check");
						  return arlELStatus;
					  }
				  }
				
				   /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(epSpecELMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlELStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecELMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting Emp Liability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlELStatus;
					 }
				   /*End Self Insured check*/
				  
				
				  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  if(GlobalVariables.YES.equals(epSpecELMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  {
					  /*MC has RRG and EP doesn't allow RRG*/
					  //log.debug("MC has RRG and EP doesn't allow RRG");
					  /*Check if EP has given overrides*/
					  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
					  {
						  //log.debug("EP has Override for RRG");
						  strOvrUsed=GlobalVariables.YES;
					  }
					  else
					  {
						  //log.debug("EP doesn't allow RRG So RRG Problem");
						  arlELStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
					  }
				  }
				 
				
				
				  /*Getting Limits and Deductibles for WC & EL*/
				  
				  /*For EL*/
				  dEL_ELA=Utility.commaStringtoDouble(epSpecELMC.getElEachOccur()); //EL Each Occur is EL Accident
				  dEL_ELE=Utility.commaStringtoDouble(epSpecELMC.getElDisEAEmp());
				  dEL_ELP=Utility.commaStringtoDouble(epSpecELMC.getElDisPlcyLmt());

				  dMCHasDeduct=Utility.commaStringtoDouble(epSpecELMC.getDeductible());
				  /*Currency conversion for EL..*/
				  if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  dEL_ELA=dEL_ELA*cndUSD;
					  dEL_ELE=dEL_ELE*cndUSD;
					  dEL_ELP=dEL_ELE*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
				  }
				  else if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  dEL_ELA=dEL_ELA*mexUSD;
					  dEL_ELE=dEL_ELE*mexUSD;
					  dEL_ELP=dEL_ELE*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
				  }
				  
				  if((umbWCELMC!=null && epSpecELMC.getWcELFlag().equals(GlobalVariables.YES) && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES))) || (umbWCELMC!=null && umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
					  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit());
				  
				  if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  if((umbWCELMC!=null && epSpecELMC.getWcELFlag().equals(GlobalVariables.YES) && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES))) || (umbWCELMC!=null && umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
						  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*cndUSD ;
				  }
				  else if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  if((umbWCELMC!=null && epSpecELMC.getWcELFlag().equals(GlobalVariables.YES) && (umbWCELMC.getWCReqd().equalsIgnoreCase((GlobalVariables.YES))) || (umbWCELMC!=null && umbWCELMC.getELReqd().equalsIgnoreCase((GlobalVariables.YES)))))
						  dMCUmbrella=Utility.commaStringtoDouble(umbWCELMC.getLimit())*mexUSD ;
				  }
				  
				  /*For WC*/
				  			
				  if(epSpecWCMC!=null)
				  {
					  //log.debug("After checking null condn for epSpecWCMC for Getting ELA ELE ELP for WC ");
					  dWC_ELA=Utility.commaStringtoDouble(epSpecWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(epSpecWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(epSpecWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
				  }
				  
				  //log.debug("After Currency conversion dEL_ELA "+ dEL_ELA+" dEL_ELE "+ dEL_ELE + "dEL_ELP "+dEL_ELP);
				  //log.debug("After Currency conversion dWC_ELA "+ dWC_ELA+" dWC_ELE "+ dWC_ELE + "dWC_ELP "+dWC_ELP);
				  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				 /* if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getELReqd()))
				  {
					  if(umbGenMC.getPolicyTerminatedDate().length()>0)
					  {
						  if(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate)))
						  {
							  //log.debug("Umbrella policy termination date less than equal to  parameter date ");
							  
						  }
					  }
				  }*/
				  dMC_Limit_ELA=dWC_ELA +dEL_ELA+dMCUmbrella;
				  dMC_Limit_ELE=dWC_ELE +dEL_ELE+dMCUmbrella;
				  dMC_Limit_ELP=dWC_ELP +dEL_ELP+dMCUmbrella;
				  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
				  
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
				  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Setting Override used flag");
					  bOvrUsed=true;
				  }
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }
				  
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
				  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
				  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
				  
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
				  }// end if limit
				  if(bLimitNotOkFlg)
				  {
					  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(dMCDedBooster!=0)
					  {
						  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
						  if(dMCDedBooster>dEPDeduct)
						  {
							  //log.debug("Ded problem after replacing Booster");
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  else
					  {
						  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
						  else	
						  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
					  }
				  }
				  //log.debug("Exiting EP Specific Policy Check for Employers Liability");
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Emp Liability Check for UValid:- ",exp);
			
		}
		//log.info("Business: Exiting method checkEmpLiability()of UValidPolicyCheck class:- "+arlELStatus);
		return arlELStatus;
	}
	/**
	 * This method will check Employee Dishonesty Bond  for a given MC and EP (For UValid)
	 * @param  HashMap mcInsDtls
	 * @param  EPInsOvrWrapper epDtls
	 * @param  String ovrUsed
	 * @param  String strEPSpcUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkEDHLiability(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed) throws Exception
	{
		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkEDHLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlEDHStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbEDHPrimary= new StringBuffer(GlobalVariables.EMPDISHBOND);
			  sbEDHPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.EMPDISHBOND);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  EmpDishBean primEDHMC=(EmpDishBean)mcInsDtls.get(sbEDHPrimary.toString());
			  EmpDishBean epSpecEDHMC=null;
				  //(EmpDishBean)mcInsDtls.get(sbEPSpecPolicy.toString());
				
				  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
				  {
					  ArrayList arCg = new ArrayList();
					  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
					  for(int x=0;x<arCg.size();x++)
					  {
						  epSpecEDHMC=(EmpDishBean)arCg.get(x);
						  if(epSpecEDHMC.getPolicyMstId()==0)
						  {
							  //epSpecEDHMC.setAddlnInsured(GlobalVariables.YES);
							  break;
						  }
						  else if(uValidDao.chkEPSpc(epSpecEDHMC.getPolicyMstId(),epDtls.getEpAcctNo()))
						  {
							  //epSpecEDHMC.setAddlnInsured(GlobalVariables.YES);
							  break;
						  }
						  else
						  {
							  epSpecEDHMC=null;
						  }
					  }
				  }
				  
			  UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			
			  double dMCHasEDHLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			

			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			 
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			 
			  if(!GlobalVariables.EMPDISHBOND.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlEDHStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlEDHStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlEDHStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlEDHStatus;
			  }
			  //=========End added=============================

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbEDHPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlEDHStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlEDHStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlEDHStatus;
			  }
			  
				  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  
			  if(primEDHMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for EmpDishBond Liability");
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(primEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlEDHStatus;
						  }
						  if(Utility.stringToSqlDate(primEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(primEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlEDHStatus;
						  }
						  //end----02/03

				       
				       /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primEDHMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primEDHMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primEDHMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primEDHMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primEDHMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlEDHStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }


					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasEDHLimit=Utility.commaStringtoDouble(primEDHMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(primEDHMC.getDeductible());
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  if(primEDHMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasEDHLimit=dMCHasEDHLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(primEDHMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasEDHLimit=dMCHasEDHLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }
					  //log.debug("After Currency conversion dMCHasEDHLimit "+dMCHasEDHLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getEmpDishReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasEDHLimit=dMCHasEDHLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasEDHLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //dMCHasEDHLimit=dMCHasEDHLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasEDHLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //if(dMCHasEDHLimit<dEPLimit)
					  if((dMCHasEDHLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasEDHLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  else
					  {
						  bDedNotOkFlg=true;
					  }
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasEDHLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  if(bLimitNotOkFlg)
					  {
						  arlEDHStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						 
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  if(dMCDedBooster>dEPDeduct)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlEDHStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlEDHStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  if(arlEDHStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }
				      //log.debug("Exiting Primary Policy Check for EmpDishBond Liability");
			  }
			  else if(!bEPPolCheck && epSpecEDHMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for EmpDishBond Liability");
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  arlEDHStatus.removeAll(arlEDHStatus);
				  
//				swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(Utility.getSqlSysdate().equals(Utility.stringToSqlDate(epSpecEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlEDHStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && Utility.getSqlSysdate().after(Utility.stringToSqlDate(epSpecEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlEDHStatus;
				  }
				  //end----02/03
				  
			       /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecEDHMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecEDHMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecEDHMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecEDHMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  

				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /*if(epSpecEDHMC.getPolicyTerminatedDate().length()>0)
				  {
					  //log.debug("Primary Policy Terminated Date= "+epSpecEDHMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Primary Policy Terminated on parameter date ");
					      arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }*/
				  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  if(GlobalVariables.YES.equals(epSpecEDHMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  {
					  /*MC has RRG and EP doesn't allow RRG*/
					  //log.debug("MC has RRG and EP doesn't allow RRG");
					  /*Check if EP has given overrides*/
					  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
					  {
						  //log.debug("EP has Override for RRG");
						  strOvrUsed=GlobalVariables.YES;
					  }
					  else
					  {
						  //log.debug("EP doesn't allow RRG So RRG Problem");
						  arlEDHStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
					  }
				  }

				  /*Calculating Limits and Deductibles along with currency conversion*/
				  dMCHasEDHLimit=Utility.commaStringtoDouble(epSpecEDHMC.getLimit());
				  dMCHasDeduct=Utility.commaStringtoDouble(epSpecEDHMC.getDeductible());
				  if(umbGenMC!=null)
					  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
				  if(epSpecEDHMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  //log.debug("Canadian Currency Conversion");
					  dMCHasEDHLimit=dMCHasEDHLimit*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
				  }
				  else if(epSpecEDHMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  //log.debug("Mexican Currency Conversion");
					  dMCHasEDHLimit=dMCHasEDHLimit*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
				  }
				  //log.debug("After Currency conversion dMCHasEDHLimit "+dMCHasEDHLimit);
				  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
				  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getEmpDishReqd()))
				  {
					  /*check if umbrella policy less than or equal to param date*/
					  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
					  {
						 	  //log.debug("Umbrella policy termination date after parameter date ");
						 	 dMCHasEDHLimit=dMCHasEDHLimit+dMCUmbrella;
					  }
					  
				  }
				 
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if((dMCHasEDHLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Limits |Deductibles override used");
					  bOvrUsed=true;
				  }
				  /******************************************************************************/
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  //dMCHasEDHLimit=dMCHasEDHLimit+dMCLimBooster; //adding booster to Limits
				  dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
				  /*******************************************************************************/
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  //log.info("MC Limit:- "+dMCHasEDHLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
				  //if(dMCHasEDHLimit<dEPLimit)
				  if((dMCHasEDHLimit<dEPLimit) && (dMCLimBooster==0))
				  {//Added as per new waiver specs
					  bLimitNotOkFlg=true;
				  }
				  else if(dMCHasEDHLimit<dMCLimBooster)
				  {	
					  //log.debug("Limit Not Ok before multiple Pol Limits check");
					  bLimitNotOkFlg=true;
				  }// end if limit
				  
				  /*Deductible check*/
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)//Commented by piyush 28March07
				  //if(dEPDeduct>=0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }
				  else
				  {
					  bDedNotOkFlg=true;
				  }
				  /*Check if multiple limits/deductible exists for this policy */
				  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
				  {
					  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
					  /*will loop through Multiple limits and deductibles bean to find appropriate
					   * limits and deductible to be used for the MC.
					   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
					   * */
					  if(bLimitNotOkFlg)
					  {
						  //log.debug("Checking Policy Multiple Limits and Deductibles ");
						  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
						  {
							  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
							  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
							  if((dMCHasEDHLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
							  {
								  /*found Multiple Limits and deductibles so using that limit and deductibles
								   * to check MC Limits and Deductibles; 
								   * */
								  bLimitNotOkFlg=false;
								  bDedNotOkFlg=false;
								  //break;
							  }
						  }// end of for loop
					  }//end if bLimitOkFlg
				  }//end check multiple limits and deductibles
				  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
				   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
				   * not found for Multiple Limits and Deductibles, so setting appropriate message
				   * */
				  if(bLimitNotOkFlg)
				  {
					  arlEDHStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
					  else	
					  arlEDHStatus.add(GlobalVariables.UVLD_DED_PRBLM);
				  }
				  //log.debug("Exiting EP Specific Policy Check for EmpDishBond Liability");
			  }
			  else
			  {
				  arlEDHStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlEDHStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Emp Dishonesty Bond Check for UValid:- ", exp);
			
		}
		log.info("Business: Exiting method checkEDHLiability()of UValidPolicyCheck class:- "+arlEDHStatus);
		return arlEDHStatus;
	}
	
	
	/**
	 * This method will check Additional Requirement for a given MC and EP (For UValid)
	 * @param ArrayList mcAddReqDtls
	 * @param EPInsOvrWrapper epDtls
	 * @param String strMCPrivate
	 * @param String strOvrUsed
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList checkAddtlnReqmnt(ArrayList arlMCAddReqDtls,EPInsOvrWrapper epDtls,String strMCPrvtForEP,String strOvrUsed) throws Exception
	{
		//log.info("Business: Entering method checkAddtlnReqmnt()");
		//log.info("arlMCAddReqDtls :-"+arlMCAddReqDtls);
		//log.info("EPInsOvrWrapper :-"+epDtls);
		//log.info("strMCPrvtForEP :-"+strMCPrvtForEP);
		//log.info("paramDate :-"+paramDate);
		//log.info("strOvrUsed :-"+strOvrUsed);
		ArrayList arlAddlnReqStatus=new ArrayList(2);
		List<AdditionalReq> arlEPAddlnReq=new ArrayList<AdditionalReq>(2);
		ArrayList arlEPOvrMCAddlnReq=new ArrayList(2);
		try
		{
			if(epDtls!=null)
			{
				//log.debug("Entering Check for epDtls!=null");
				/*EP Additional Requirement*/
				 arlEPAddlnReq=epDtls.getAddReq(); 
				/*Override given by EP to MC for Additional Requirement*/
				 arlEPOvrMCAddlnReq=epDtls.getAddReqOvrMC(); 
			}
			//log.info("ADREQ :"+arlEPAddlnReq);
			if(arlEPAddlnReq!=null)
			{
				//log.debug("Entering Check for arlMCAddReqDtls!=null");
				boolean bAddReqFlg;
				boolean bOvrChkFlg;
				boolean bskipCheck;
				String strEPAddReq="";
				/*Converting ArrayList to HashMap for EP Additional Req*/
				Map<String, AdditionalReq> hshEPAddlnReq=getHashMapForAddtlnReq(arlEPAddlnReq);
				/*Converting ArrayList to HashMap for Override additional Req */
				Map<String, AdditionalReq> hshEPOvrMCAddlnReq=getHashMapForAddtlnReq(arlEPOvrMCAddlnReq);
				/*Converting ArrayList to HashMap for MC Additional Req*/
				Map<String, AdditionalReq> hshMCAddReqDtls=getHashMapForAddtlnReq(arlMCAddReqDtls);
				//log.debug("KEYSET EP:"+hshEPAddlnReq.keySet().toString());
				//log.info("KEYSET MC  :"+hshMCAddReqDtls.keySet());
				Iterator<String> iterEP=hshEPAddlnReq.keySet().iterator();				
				while(iterEP.hasNext())
				{
					String keyAddReqEP = iterEP.next();
					bAddReqFlg=true;
					bOvrChkFlg=false;
					bskipCheck=false;
					AdditionalReq addReqEPBean=hshEPAddlnReq.get(keyAddReqEP);
					//log.info("Key for Addtional Requirement:- "+keyAddReqEP.toString());
					//log.info("Key for Addtional Requirement:- "+addReqEPBean.toString());
					if(GlobalVariables.NO.equals(addReqEPBean.getRequired()))
					{
						//log.info("EP doesn't require Additional Requirement continue");
						continue;
					}
					else
					{
						//log.info("EP requires Additional Requirement");
						if(!hshMCAddReqDtls.containsKey(keyAddReqEP))
						{
							//log.info("MC doesn't have addln req");
							if(!hshEPOvrMCAddlnReq.containsKey(keyAddReqEP) && !strMCPrvtForEP.equals(GlobalVariables.YES))
							{
								//log.info("No Overrides");
								/*To decide whether to call Override check or not..... 
								 * If there is no override, then no need to even check*/
								if(addReqEPBean.getEndrsCode()!=null)
								{
									arlAddlnReqStatus.add(addReqEPBean.getEndrsCode());
									bskipCheck=true;
								}
							}
							else
							{
								//log.info("Override exists");
							}
						}
					}
					if(addReqEPBean!=null && addReqEPBean.getEndrsCode()!=null)
					{
						//log.info("Add Req Code !=null ");
						strEPAddReq=addReqEPBean.getEndrsCode() ;
					}
					//log.info("-bskipCheck--"+bskipCheck);
					if(!bskipCheck)
					{
						String strMCAddReq="";
						/*Check  Whether MC has the Additional Requirement*/
						if(hshMCAddReqDtls.containsKey(keyAddReqEP))
						{
							AdditionalReq addReqMCBean=(AdditionalReq)hshMCAddReqDtls.get(keyAddReqEP);
							strMCAddReq=addReqMCBean.getEndrsCode();
							
							if(addReqEPBean!=null && addReqMCBean!=null &&  strEPAddReq.equals(strMCAddReq))
							{
								//log.info("EP Addtln Req code equals MC Addtln Req code");
								//if(addReqEPBean.getRequired().equals(GlobalVariables.YES) && addReqEPBean.getOriginalReq().equals(GlobalVariables.YES))
								if(addReqEPBean.getRequired().equals(GlobalVariables.YES))
								{
									if(addReqMCBean.getRequired().equals(GlobalVariables.NO)) 
									{
										//log.info("EP Require =Y and EP Orig Require=Y and MC Provided =N");
										bAddReqFlg=false;
										bOvrChkFlg=true;
									}
									/*Check if MC Provided=Y and MC Original Received= N*/
									if(addReqMCBean.getRequired().equals(GlobalVariables.YES) && addReqMCBean.getAreqOriRcvDate().length()==0)
									{
										//log.info("MC Provided=Y and MC Original Received= N & Days");
										java.util.Date dt=Utility.stringToSqlDate(addReqMCBean.getAreqRcvDate(),Utility.FORMAT4);
										int days=Integer.parseInt(addReqMCBean.getReqInDays().equals("")?"0":addReqMCBean.getReqInDays());
										//log.debug("Days :"+days);
										//log.debug("Add Days :"+Utility.addDays(dt,days));										
										/*If RcvdDate + Required in days > parameter date*/
										if(days!=0)
										{
											if(Utility.addDays(dt,days).before(paramDate))
											{
												//log.info("EP Require =Y and EP Orig Require=Y and MC Provided =Y and MC Orig recvd date =N and MCOrigRcvdDate+ <<x>> days >Parameter date");
												bAddReqFlg=false;
												bOvrChkFlg=true;
											}
										}
									}
								}
								if(addReqEPBean.getRequired().equals(GlobalVariables.YES) && !addReqEPBean.getOriginalReq().equals(GlobalVariables.YES))
								{
									if(addReqMCBean.getRequired().equals(GlobalVariables.NO)) 
									{
										//log.info("EP Require=Y and EP Origreq=Y and MC req=N");
										bAddReqFlg=false;
										bOvrChkFlg=true;
									}
								}
							}
							/*If Additional Req is Letter Of Credit and if MC is Private to 
							 * EP,then skip the Credit Application Check*/
							if((strEPAddReq.equals(GlobalVariables.ADDL_REQ_LOC) && strEPAddReq.equals(strMCAddReq)) && strMCPrvtForEP.equals(GlobalVariables.YES))
							{
								/*Additional Req met for LOC*/
								//log.info("MC has Letter of Credit and MC is Private to EP");
								bAddReqFlg=true;
								bOvrChkFlg=false;
							}
						} // end of if(hshMCAddReqDtls.containsKey(keyAddReqEP))
						else if(arlEPOvrMCAddlnReq.size()>0)
						{
							bOvrChkFlg = true;
						}
						else if(hshMCAddReqDtls.isEmpty())
						{
							//Special Patch for Data-migration realted records.
							if(strEPAddReq.equals(GlobalVariables.ADDL_REQ_LOC) && strMCPrvtForEP.equals(GlobalVariables.YES))
							{
								//log.debug("Private flag is enabled for EP");
							}
							else
							{
								arlAddlnReqStatus.add(addReqEPBean.getEndrsCode());
							}
						}
						/*If bAddReqFlg is false, implies that the Additional Requirment did not match that of EP, 
						 * so to check again with Overridden Additional Requirement*/
						if(bOvrChkFlg)
						{
							//log.info("Entering !bAddReqFlg");
							boolean bOvrAddReqFlg=true;
							strOvrUsed=GlobalVariables.YES;
							/* As Additional Requirement check failed. We need to check if override exists 
							 * and if it does, then to check with the Overrided Additional Requirement and 
							 * if it fails then to set the problems in the ArrayList */
							if(hshEPOvrMCAddlnReq.containsKey(keyAddReqEP))
							{
								//log.info("Checking Overridden Additional Requirements");
								AdditionalReq addReqMCBean=(AdditionalReq)hshEPOvrMCAddlnReq.get(keyAddReqEP);
								strMCAddReq = addReqMCBean.getEndrsCode();
								if(addReqEPBean!=null && addReqMCBean!=null &&  strEPAddReq.equals(strMCAddReq))
								{
									//log.info("EP Addtln Req code equals MC Addtln Req code");
									if(addReqMCBean.getRequired().equals(GlobalVariables.NO) && addReqEPBean.getRequired().equals(GlobalVariables.YES))
									{
										//log.info("YY");
										bOvrAddReqFlg=false;
									}
									if(addReqMCBean.getRequired().equals(GlobalVariables.NO) && !addReqEPBean.getRequired().equals(GlobalVariables.YES))
									{
										//log.info("YN");
										bOvrAddReqFlg=false;
									}
									/*If Additional Req is Letter Of Credit and if MC is Private to 
									 * EP,then skip the Credit Application Check*/
									//if(strEPAddReq.equals(GlobalVariables.ADDL_REQ_LOC) && strEPAddReq.equals(strMCAddReq) && strMCPrvtForEP.equals(GlobalVariables.YES))
									if((strEPAddReq.equals(GlobalVariables.ADDL_REQ_LOC) && strEPAddReq.equals(strMCAddReq)) && strMCPrvtForEP.equals(GlobalVariables.YES))
									{
										/*Additional Req met for LOC*/
										//log.info("MC has Letter of Credit and MC is Private to EP");
										bOvrAddReqFlg=true;
									}
								}								
							}
							else if(!bAddReqFlg)
							{
								
								bOvrAddReqFlg=false;
							}
							
							if(!bOvrAddReqFlg)
							{	
								//log.info("Entering !bOvrAddReqFlg");
								if(addReqEPBean!=null && addReqEPBean.getEndrsCode()!=null)
								{
									//log.info("Setting Additional Req Code in the ArrayList of Problems ");
									arlAddlnReqStatus.add(addReqEPBean.getEndrsCode());
								}
							}
						}
					}
					
				}//end of EP Iteration
			}
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Additional Requirement Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkAddtlnReqmnt() with Problems:- "+arlAddlnReqStatus);
		//log.info("Business: Exiting method checkAddtlnReqmnt() with Problems:- ");
		return arlAddlnReqStatus;
	}
	
	
	
	private Map<String, AdditionalReq> getHashMapForAddtlnReq(List<AdditionalReq> arlEPAddlnReq) throws Exception
	{
		//log.info("Business: Entering method getHashMapForAddtlnReq()");
		//log.info("arlAddReq "+arlAddReq);
		Map<String,AdditionalReq> hshAddtlnReq=new HashMap<String, AdditionalReq>(2);
		try
		{
			if(arlEPAddlnReq!=null)
			{
				//log.debug("arraylist arlAddReq!=null");
				for(int i=0;i<arlEPAddlnReq.size();i++)
				{
					//log.debug("Iterating over arlAddReq");
					AdditionalReq addReqBean= arlEPAddlnReq.get(i);
					if(addReqBean!=null && addReqBean.getEndrsCode()!=null)
						hshAddtlnReq.put(addReqBean.getEndrsCode(),addReqBean);
				}
			}
			
		}
		catch(Exception exp)
		{
			log.error("Caught Exception While Converting HashMap of Additional Req- ", exp);
			
		}
		//log.info("Business: Exiting method getHashMapForAddtlnReq():- "+hshAddtlnReq);
		return hshAddtlnReq;
	}
	
	
	public ArrayList checkMemSpecCarrier(UVldMemBean memDtls,java.sql.Date paramDate) throws Exception
	{
		//log.info("Entering method checkMemSpecCarrier()");
		//log.info("memDtls:- "+memDtls);
		//log.info("paramDate:- "+paramDate);
		ArrayList arlMemDtls=new ArrayList(1);
		try
		{
			if(GlobalVariables.YES.equals(memDtls.getEpReqMem()))
			{
				log.debug("EP Requires MC Specific Carrier");
				if(GlobalVariables.YES.equals(memDtls.getMcIsMem()))
				{
					log.debug("MC is Member Specific Carrier, so to check if Member is cancelled");					
				}
				else
				{
					log.debug("MC is not a memer Specific Carrier so setting problem");
					arlMemDtls.add(GlobalVariables.MEM_SPECIFIC_CARRIER);
				}				
			}
			if(GlobalVariables.YES.equals(memDtls.getCncl()))
			{
				//log.debug("Member is cancelled.. Now checking cancelled date");
				if(Utility.stringToSqlDate(memDtls.getCnclDt(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(memDtls.getCnclDt(),Utility.FORMAT4).equals(paramDate))
				{
					//log.debug("Cancelled Date < Param Date..  so MC is not Mem Specific Carrier");
					arlMemDtls.add(GlobalVariables.CANCELLED_BY_EP);
				}
			}
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Member Specific Carrier ");
	
		}
		//log.info("Exiting method checkMemSpecCarrier:- "+arlMemDtls);
		//log.info("Exiting method checkMemSpecCarrier:- ");
		return arlMemDtls;
	}
	
	public boolean chkEPSPC(Connection con,String epAcctNo,int policy_mst_id) throws Exception
	{
		log.debug("Business: Entering method chkEPSPC("+epAcctNo+",'"+policy_mst_id+"') of UValidDPolicyCheck class");		
		PreparedStatement pstmt = null ;
		boolean flg = false;
		ResultSet rsJoinSt=null;
		StringBuffer sbGetQuery = new StringBuffer("SELECT ep_acct_no FROM policy_specific_eplist WHERE policy_mst_id = "+policy_mst_id+" AND ep_acct_no = '"+epAcctNo+"' ");
		//log.debug("CHKEPSPC QRY :"+sbGetQuery.toString());
		try
		{
		 	pstmt = con.prepareStatement(sbGetQuery.toString());
		 	rsJoinSt=pstmt.executeQuery();
	 		while(rsJoinSt.next())
	 		{
	 			flg =  true;
	 		}
		}
		catch(SQLException sqlEx)
		{
			return flg;
		}
		finally
		{
			try
			{
				if(rsJoinSt!=null)
					rsJoinSt.close();
				if(pstmt!=null)
					pstmt.close();
			}
			catch(SQLException sqlEx)
			{
				log.error("Caught SQL Exception while closing prepared statement:" , sqlEx);
				 
			}
		}
		//log.info("Business: Exiting method chkEPSPC() of UValidPolicyCheck class:- "+flg);
		return flg;
	}

	public String getProblemStr(Connection con,String epAcctNo,String mcAcctNo) throws Exception
	{
		//log.info("Business: Entering method getProblemStr() of UValidDPolicyCheck class");		
		PreparedStatement pstmt = null ;
		String flg = "";
		ResultSet rsJoinSt=null;		
		StringBuffer sbGetQuery = new StringBuffer("SELECT INSRN_PRBLM FROM mc_ep_join_status where mc_acct_no='"+mcAcctNo+"' AND ep_acct_no = '"+epAcctNo+"'");
		//log.debug("INSRN_PRBLM QUERY :"+sbGetQuery.toString());
		try
		{
		 	pstmt = con.prepareStatement(sbGetQuery.toString());
		 	rsJoinSt=pstmt.executeQuery();
	 		while(rsJoinSt.next())
	 		{
	 			flg =  rsJoinSt.getString("INSRN_PRBLM");
	 		}
		}
		catch(SQLException sqlEx)
		{
			return flg;
		}
		finally
		{
			try
			{
				if(rsJoinSt!=null)
					rsJoinSt.close();
				if(pstmt!=null)
					pstmt.close();
			}
			catch(SQLException sqlEx)
			{
				log.error("Caught SQL Exception while closing prepared statement:" , sqlEx);
				 
			}
		}
		//log.info("Business: Exiting method getProblemStr() of UValidPolicyCheck class:- "+flg);
		return flg;
	}
	
	
	public ArrayList getTmpTermDt(int mstId) throws Exception
	{
		//log.info("Business: Entering method getTmpTermDt("+mstId+") of UValidDPolicyCheck class");
		ArrayList arr = new ArrayList();				
		PreparedStatement pstmt = null ;
		ResultSet rsJoinSt=null;
		Connection con =null;
		StringBuffer sbGetQuery = new StringBuffer("SELECT tmp_term_date,tmp_reins_date FROM policy_master where policy_mst_id = "+mstId);
		//log.debug("policy_specific_eplist QUERY :"+sbGetQuery.toString());
		try
		{
//			con = ConnectionFactory.getDBConnection();
		 	pstmt = con.prepareStatement(sbGetQuery.toString());
		 	rsJoinSt=pstmt.executeQuery();
	 		if(rsJoinSt.next())
	 		{
	 			arr.add(rsJoinSt.getDate("tmp_term_date"));
	 			arr.add(rsJoinSt.getDate("tmp_reins_date"));
	 			//return arr;
	 		}
		}
		catch(SQLException sqlEx)
		{
			return arr;
		}
		finally
		{
			try
			{
				if(rsJoinSt!=null)
					rsJoinSt.close();
				if(pstmt!=null)
					pstmt.close();
				if(con!=null)
					con.close();
			}
			catch(SQLException sqlEx)
			{
				log.error("Caught SQL Exception while closing prepared statement:" , sqlEx);
				
			}
		}
		//log.info("Business: Exiting method getTmpTermDt() of UValidPolicyCheck class:- ");
		return arr;
	}
	
	public ArrayList checkAutoLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{
		ArrayList arlALStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  StringBuffer sbALPrimary= new StringBuffer(GlobalVariables.AUTOPOLICY);
			  sbALPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbALExcess= new StringBuffer(GlobalVariables.AUTOPOLICY);
			  sbALExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.AUTOPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  AutoBean primAutoMC=(AutoBean)mcInsDtls.get(sbALPrimary.toString());
			  AutoBean excessAutoMC=(AutoBean)mcInsDtls.get(sbALExcess.toString());
			  AutoBean epSpecAutoMC=null;
				  //(AutoBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arAuto = new ArrayList();
				  arAuto = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arAuto.size();x++)
				  {
					  epSpecAutoMC=(AutoBean)arAuto.get(x);
					  if(epSpecAutoMC.getPolicyMstId()==0)
					  {
						  //epSpecAutoMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecAutoMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecAutoMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecAutoMC=null;
					  }
				  }
			  }	
			  String accountNo = "";
			  boolean otherThenNonUIIAMC = true;
			  if(primAutoMC != null){
				  accountNo = primAutoMC.getMcAcctNo();
			  }else if(epSpecAutoMC != null){
				  accountNo = epSpecAutoMC.getMcAcctNo();
			  }
			  if(StringUtils.isNotBlank(accountNo)){
				  //fetch mc account details by account no.
//				  MotorCarrier mcDao = new MotorCarrier();
				  AccountInfo mcAccountInfo = epDao.getBasicAcctDtls(accountNo);
				  otherThenNonUIIAMC = (mcAccountInfo != null && !(GlobalVariables.ROLE_NON_UIIA_MC.equalsIgnoreCase(mcAccountInfo.getMemType())));
			  }
			  UmbBean umbAutoMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbAutoMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbAutoMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }
			  double dMCHasAutoLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dExAutoLimit=0.0;
			  double dEXAutoDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double autoPrmMCLimits =0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /*Uday added end*/
			  /* to decide whether to check EP Specific or not..
	 		   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
	 		   *  in Primary and then it will be set to true 
	 		   */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.after checking override also
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.AUTOPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have AL");
				  return arlALStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <=0 ");
				  //arlALStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  log.debug("Exiting AutoLiability Check as EP Limits <=0");
				  return arlALStatus;
			  }

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires AL.. checking if MC has AL either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbALPrimary.toString()) || mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlALStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlALStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires AL.. so skipping Policy check ");
				  return arlALStatus;
			  }
			  
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }			  
			  
			  if(primAutoMC!=null)
			  {
					  //log.debug("Entering Primary Policy Check for Auto Liability");
					  //===============Added for All Owned 29March'10===================
					  autoPrmMCLimits=Utility.commaStringtoDouble(primAutoMC.getLimit())+Utility.commaStringtoDouble(primAutoMC.getBdlyInjrdPerAccdnt())+Utility.commaStringtoDouble(primAutoMC.getPropDmgPerAccdnt());
					  if(primAutoMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  autoPrmMCLimits=autoPrmMCLimits*cndUSD;
					  }
					  else if(primAutoMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  autoPrmMCLimits=autoPrmMCLimits*mexUSD;
					  }
					  //===========End added all owned==================================
					  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
					  if(paramDate.equals(Utility.stringToSqlDate(primAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						//log.debug("if policy expiration date is today's date");
						arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlALStatus;
					  }
					  if(Utility.stringToSqlDate(primAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						  	//log.debug("if policy expiration date is today's date");
							arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlALStatus;
					  }
					  //end----02/03
					  
					  /*Start Std Endo Check*/
					 /*
					 * TO CHECK FOR STD ENDORSEMENT UIIE/CA-23/TE23-17 */ 
					  //log.debug("Checking attributes for Primary Policy:- "+primAutoMC.getStdEndo());
						 if(otherThenNonUIIAMC && !((primAutoMC.getStdEndo().equals(GlobalVariables.ENDOUIIE1))||(primAutoMC.getStdEndo().equals(GlobalVariables.ENDOCA2317))||(primAutoMC.getStdEndo().equals(GlobalVariables.ENDOTE2317B))))
						 {
							//log.debug("Standard Endorsment Check failed");
							arlALStatus.add(GlobalVariables.UVLD_AL_STDENDO_PRBLM);
						 }
						 /*End Std Endo Check*/
						 /*Start Check Scheduled Hired and Auto*/
						 if((primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getScheduled().equals(GlobalVariables.YES))) || (primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getAllOwned().equals(GlobalVariables.YES))) || primAutoMC.getAny().equals(GlobalVariables.YES))
						 {
							 	// Scheduled Hired All owned and Auto Ok for Primary (NO ERROR)
						 }
						 else
						 {
							  //log.debug("Scheduled Hired Any Problem: AL Primary: Check Any,Scheduled,Hired,All owned");
							  arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
						 } 
						 						 
					 /*if(autoPrmMCLimits<dEPLimit && (primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getAllOwned().equals(GlobalVariables.YES))))
					 {
						 arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
					 }*/

					 /*Start Check Policy Status (Termination|Cancellation) */
					  ArrayList arr = new ArrayList();
					  arr = getTmpTermDt(primAutoMC.getPolicyMstId());
					  Date tmpTerm = null;
					  Date tmpRein = null;
					  Date curTerm = null;
					  Date curReins = null;
					  if(!arr.isEmpty())
					  {
						  tmpTerm = (Date)arr.get(0);
						  tmpRein = (Date)arr.get(1);
					  }
					  //=========By Piyush 14Mar'09===============================================================
					  if(primAutoMC.getPolicyTerminatedDate().length()>0)
					  {
						  curTerm = Utility.stringToSqlDate(primAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4);
						  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
						  {
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
			  		  else if(primAutoMC.getPolicyReinstatedDate().length()>0)
					  {
			  			  curReins = Utility.stringToSqlDate(primAutoMC.getPolicyReinstatedDate(),Utility.FORMAT4);
						  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
						  else if(curReins.after(paramDate))
						  {
							  if(tmpTerm!=null && tmpTerm.equals(paramDate))
							  {
								  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
					  }						  
					  //=========End By Piyush 14Mar===============================================================					  
					/* End Termination */
					  
					 /*Start Self Insured check*/
					 if(GlobalVariables.YES.equals(primAutoMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlALStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(primAutoMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting AutoLiability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 //=============Added by piyush as per talk with debbie on 11Aug2008=======================						 
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
						  //=============End Added by piyush =================================================================
						  return arlALStatus;//Commented by piyush = As smart checklist doesnt show all possible problems when mc has no policy
					 }
					 /*End Self Insured check*/
					 
										 
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primAutoMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlALStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					  
					 /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {							  
							  /*check if MC has Blanket or Additional Insured Flag is Yes
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()) || GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }*/
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }						  
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  /**----------------------------------------------------------**/
					  /*End Additional Insured check*/
					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasAutoLimit=Utility.commaStringtoDouble(primAutoMC.getLimit())+Utility.commaStringtoDouble(primAutoMC.getBdlyInjrdPerAccdnt())+Utility.commaStringtoDouble(primAutoMC.getPropDmgPerAccdnt());
					  dMCHasDeduct=Utility.commaStringtoDouble(primAutoMC.getDeductible());
					  if(excessAutoMC!=null)
					  {
						  dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit());
						  dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible());
					  }
					  
					  if(umbAutoMC!=null && umbAutoMC.getALReqd().equals(GlobalVariables.YES))
						  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit());
					  
					  if(primAutoMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  dMCHasAutoLimit=dMCHasAutoLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessAutoMC!=null)
							  {
							  	dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*cndUSD ;
							  	dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*cndUSD;
							  }
						  if(umbAutoMC!=null && umbAutoMC.getALReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*cndUSD ;
						  //log.info("Currency conversion dMCHasAutoLimit (CND $) "+dMCHasAutoLimit);
						  //log.info("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.info("Currency conversion dExAutoLimit 	(CND $) "+dExAutoLimit);
						  //log.info("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  else if(primAutoMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  dMCHasAutoLimit=dMCHasAutoLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessAutoMC!=null)
							  {
							  	dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*mexUSD ;
							  	dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*mexUSD ;
							  }
						  if(umbAutoMC!=null && umbAutoMC.getALReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*mexUSD ;
						  //log.info("Currency conversion dMCHasAutoLimit (MEX $) "+dMCHasAutoLimit);
					  //log.info("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
					  //log.info("Currency conversion dExAutoLimit    (MEX $) "+dExAutoLimit);
					  //log.info("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessAutoMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessAutoMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessAutoMC.getPolicyTerminatedDate().length()==0) || paramDate.after(Utility.stringToSqlDate(excessAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasAutoLimit=dMCHasAutoLimit+dExAutoLimit;
							  //dMCHasDeduct=dMCHasDeduct+dEXAutoDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exist  add to MC Limit*/
					  if(umbAutoMC!=null && GlobalVariables.YES.equals(umbAutoMC.getALReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbAutoMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							   //log.debug("Umbrella policy termination date less than equal to  parameter date :"+dMCUmbrella);
							   dMCHasAutoLimit=dMCHasAutoLimit+dMCUmbrella;
						  }
					  }
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasAutoLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits and deductibles without booster. If it fails then to set Override used flag
					      * as true*/
					     //log.debug("Limits|Deductibles override used");
						 bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note 16July07
					  //dMCHasAutoLimit=dMCHasAutoLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasAutoLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //Commented by piyush as per note 16July07
					  //if(dMCHasAutoLimit<dEPLimit)
					  if((dMCHasAutoLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasAutoLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg || bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasAutoLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  
					  /* Check Umbrella limits,EP Booster/Waiver and EP multiple limits*/
					  	 if(bLimitNotOkFlg && (primAutoMC.getHired().equals(GlobalVariables.YES) && (primAutoMC.getAllOwned().equals(GlobalVariables.YES))))
						 {
							 //arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
						 }
					  /* Above condition added by Piyush on 19Oct'10 - End of AllOwned & Hired with Umbrella Policy*/
					 /*********************************************************************************/
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  	ArrayList activeEpspcDtls = new ArrayList();
					  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primAutoMC.getMcAcctNo(),GlobalVariables.AUTOPOLICY);
					  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
					  	{					  		
					  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
					  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
					  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
					  	}
					  	
					  	/*Uday modification 24 May 12 start*/
					  	/* The pending EP SPEC policy also needs to be checked. 
					  	 * This will only be checked when the policy effective date is greater then current date*/
					  	
					  	if (!primAutoMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate())))
					  	{
					  		ArrayList pendingEpspcDtls = new ArrayList();
					  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
					  				.getEpAcctNo(),
					  				primAutoMC.getMcAcctNo(),
					  				GlobalVariables.AUTOPOLICY);
					  		if (pendingEpspcDtls != null
					  				&& pendingEpspcDtls.size() > 3) {
					  			dEpSpcPenLim = (Double) pendingEpspcDtls
					  			.get(0);
					  			dEpSpcPenDed = (Double) pendingEpspcDtls
					  			.get(1);
					  			IsPenEpSpcExist = (String) pendingEpspcDtls
					  			.get(2);
					  			strPenEpSpcEffDate = (String) pendingEpspcDtls
					  			.get(3);
					  		}
					  	}
					  	if(bLimitNotOkFlg)
					  	{
							  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
							  {}
							  else /* Uday modification 24 May 12 */
							  {
								  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
										  && (!primAutoMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4))))
										  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
								  {
									  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
									  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
									  //log.debug("dEPLimit - "+dEPLimit);
								  }
								  else
								  {
									  arlALStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
								  }
							  }
						 }
					  	 else
					  	 {
					  		 if(excessAutoMC!=null)
					  		 {
						  		 if(excessAutoMC.getHired().equals(GlobalVariables.YES) && (excessAutoMC.getAllOwned().equals(GlobalVariables.YES)))
								 {		
						  			 
						  			arlALStatus.remove(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
								 }
					  		 }
					  	 }
						  if(bDedNotOkFlg)
						  {
							  //log.debug("Deductible Problem.. so to check with Overriden value");
							  //arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);							  
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27Mar07
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
													  && (!primAutoMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4))))
													  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
										  		
										  	}
										  	else{
										  		arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  	}
									  }
								  }
							  }
							  else
							  {
								  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
								  {}
								  else /* Uday modification 24 May 12 */
								  {
									  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
												  && (!primAutoMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primAutoMC.getPolicyEffDate(), Utility.FORMAT4))))
												  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
									  		
									  	}
									  	else{
									  		arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  	}
								  }
							  }
						  }
					  if(arlALStatus.size()==0)
					  {
						  //log.debug("No Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
					//log.debug("Exiting Primary Policy Check for Auto Liability");
			  }//end of primAutoMC !=null
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlALStatus is not empty  as it has problems*/
			  if(!bEPPolCheck && epSpecAutoMC!=null)
			  {
				  //log.info("Entering EP Specific Policy Check for Auto Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlALStatus.removeAll(arlALStatus);
				  
				  /*Repeating EP Specific same as that in Primary*/
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Auto Liability */
				    //strEPSpcUsed=GlobalVariables.AUTOPOLICY; 	
				    
				    //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
					  if(paramDate.equals(Utility.stringToSqlDate(epSpecAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						//log.debug("if policy expiration date is today's date");
						arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlALStatus;
					  }
					  if(Utility.stringToSqlDate(epSpecAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecAutoMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						  //log.debug("if policy expiration date is today's date");
							arlALStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlALStatus;
					  }
					  //end----02/03
				    
				  	/*Start Standard Endo Check*/
					/* TO CHECK FOR STD ENDORSEMENT UIIE/CA-23/TE23-17 */
				     //if(epDtls.getEpAcctNo().equals(GlobalVariables.UIIA_EP))
				 	 //log.debug("EP is UIIA EP so checking Truckers Endorsment and Scheuded Hired");
			    	 if(otherThenNonUIIAMC && !((epSpecAutoMC.getStdEndo().equals(GlobalVariables.ENDOUIIE1))||(epSpecAutoMC.getStdEndo().equals(GlobalVariables.ENDOCA2317))||(epSpecAutoMC.getStdEndo().equals(GlobalVariables.ENDOTE2317B))))
					 {
						//log.info("Standard Endorsment Check failed for EP Specific");
						arlALStatus.add(GlobalVariables.UVLD_AL_STDENDO_PRBLM);
					 }
					 /*End Std Endo Check*/
					 /*Start Check Scheduled Hired and Auto*/
					 if(epSpecAutoMC.getAny().equals(GlobalVariables.YES) || (epSpecAutoMC.getHired().equals(GlobalVariables.YES) && (epSpecAutoMC.getScheduled().equals(GlobalVariables.YES))) || (epSpecAutoMC.getHired().equals(GlobalVariables.YES) && (epSpecAutoMC.getAllOwned().equals(GlobalVariables.YES))))
					 {
							  	//Scheduled Hired and Auto Ok for Primary (NO ERROR)
					 }
				     else
					 {
						  //log.info("Entering error for EP Specific: Check Any,Scheduled,Hired,All Owned");
						  arlALStatus.add(GlobalVariables.UVLD_AL_SCDHRD_PRBLM);
					 } 
				     /*End Standard Endo Check*/

					 /*Start Check Policy Status (Termination|Cancellation) */
					  ArrayList arr = new ArrayList();
					  arr = getTmpTermDt(epSpecAutoMC.getPolicyMstId());
					  Date tmpTerm = null;
					  Date tmpRein = null;
					  Date curTerm = null;
					  Date curReins = null;
					  if(!arr.isEmpty())
					  {
						  tmpTerm = (Date)arr.get(0);
						  tmpRein = (Date)arr.get(1);
					  }
					  //=========By Piyush 14Mar'09===============================================================
					  if(epSpecAutoMC.getPolicyTerminatedDate().length()>0)
					  {
						  curTerm = Utility.stringToSqlDate(epSpecAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4);
						  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
						  {
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
			  		  else if(epSpecAutoMC.getPolicyReinstatedDate().length()>0)
					  {
			  			  curReins = Utility.stringToSqlDate(epSpecAutoMC.getPolicyReinstatedDate(),Utility.FORMAT4);
						  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
						  else if(curReins.after(paramDate))
						  {
							  if(tmpTerm!=null && tmpTerm.equals(paramDate))
							  {
								  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
					  }						  
					  //=========End By Piyush 14Mar===============================================================					  


					/*Start Check Policy Status (Termination|Cancellation) */
					/* if(epSpecAutoMC.getPolicyTerminatedDate().length()>0)
					 {
						  //log.debug("EP Specific Policy Terminated Date:- "+epSpecAutoMC.getPolicyTerminatedDate());
						  if(Utility.stringToSqlDate(epSpecAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) ||Utility.stringToSqlDate(epSpecAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
						  {
							  //log.debug("EP Specific Policy Termination terminated on the param date ");
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  } else if(epSpecAutoMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecAutoMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecAutoMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlALStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
					 
				     /*Start Self Insured check*/
				     if(GlobalVariables.YES.equals(epSpecAutoMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							/*Check if EP doesn't allow SI*/
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlALStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecAutoMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting AutoLiability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlALStatus;
					 }
					 /*End Self Insured check*/
				     
				     
				     

					 /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					 
					  if(GlobalVariables.YES.equals(epSpecAutoMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlALStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					  /*END RRG and EP doesn't allow RRG*/
					
					 /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  /*check if MC has Blanket or Additional Insured Flag is Yes*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  /*if(GlobalVariables.YES.equals(epSpecAutoMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecAutoMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }*/
							  if(GlobalVariables.YES.equals(epSpecAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(epSpecAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.info("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(epSpecAutoMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(epSpecAutoMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");
									 
								  }
								  else
								  {
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlALStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  /**----------------------------------------------------------**/
					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasAutoLimit=Utility.commaStringtoDouble(epSpecAutoMC.getLimit())+Utility.commaStringtoDouble(epSpecAutoMC.getBdlyInjrdPerAccdnt())+Utility.commaStringtoDouble(epSpecAutoMC.getPropDmgPerAccdnt());
					  dMCHasDeduct=Utility.commaStringtoDouble(epSpecAutoMC.getDeductible());
					  
					  if(excessAutoMC!=null)
						  {
						  	dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit());
						  	dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible());
						  }
					  if(umbAutoMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit());
					  if(epSpecAutoMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasAutoLimit=dMCHasAutoLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessAutoMC!=null)
						  {
							  dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*cndUSD ;
							  dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*cndUSD;
						  }
						  if(umbAutoMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*cndUSD ;
						  //log.debug("Currency conversion dMCHasAutoLimit (CND $) "+dMCHasAutoLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExAutoLimit 	(CND $) "+dExAutoLimit);
						  //log.debug("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  if(epSpecAutoMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasAutoLimit=dMCHasAutoLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessAutoMC!=null)
						  {
							  dExAutoLimit=Utility.commaStringtoDouble(excessAutoMC.getLimit())*mexUSD ;
							  dEXAutoDed=Utility.commaStringtoDouble(excessAutoMC.getDeductible())*mexUSD;
						  }
						  if(umbAutoMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbAutoMC.getLimit())*mexUSD ;
						  //log.info("Currency conversion dMCHasAutoLimit (MEX $) "+dMCHasAutoLimit);
						  //log.info("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
						  //log.info("Currency conversion dExAutoLimit    (MEX $) "+dExAutoLimit);
						  //log.info("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessAutoMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessAutoMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessAutoMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasAutoLimit=dMCHasAutoLimit+dExAutoLimit;
							  //dMCHasDeduct=dMCHasDeduct+dEXAutoDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbAutoMC!=null && GlobalVariables.YES.equals(umbAutoMC.getALReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbAutoMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbAutoMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  //log.debug("Umbrella policy termination date less than equal to  parameter date ");
							   dMCHasAutoLimit=dMCHasAutoLimit+dMCUmbrella;
						  }
					  }//end of if umbAutoMC
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if(dMCHasAutoLimit<dEPLimit)
					  {
						  /* checking limits without booster. If it fails then to set Override used flag
						   * as true*/
						  //log.debug("Override used for Limits");
						  bOvrUsed=true;
					  }
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  /*  checking deductibles without booster. If it fails then to set Override used flag
						   * as true*/
						  //log.debug("Override used for Deductibles");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note on 16July07
					  //dMCHasAutoLimit=dMCHasAutoLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasAutoLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //Commented by piyush as per note on 16July07
					  //if(dMCHasAutoLimit<dEPLimit)
					  if((dMCHasAutoLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasAutoLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit not Ok before Pol Multiple Limits Check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasAutoLimit +"and EP Ded:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Dedcutibles not Ok before Pol Multiple Dedcutibles Check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  	
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasAutoLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  if(bLimitNotOkFlg)
					  {
						  
						  arlALStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  //arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  if(dMCDedBooster>dEPDeduct)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  arlALStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					 /*********************************************************************************/
				  //log.debug("Exiting EP Specific Policy Check for Auto Liability");
			  }//end of else if
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}//end of Try
		catch(Exception exp)
		{
			log.error("Caught Exception in Auto Liability Check for UValid:- ", exp);
			
		}//end of Catch
		
		//log.info("Business: Exiting method checkAutoLiabilityArch() with Problems:- "+arlALStatus);
		return arlALStatus;
	
	}
	
	public ArrayList checkGeneralLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{
		ArrayList arlGLStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  StringBuffer sbGLPrimary= new StringBuffer(GlobalVariables.GENPOLICY);
			  sbGLPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbGLExcess= new StringBuffer(GlobalVariables.GENPOLICY);
			  sbGLExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.GENPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  GenBean primGenMC=(GenBean)mcInsDtls.get(sbGLPrimary.toString());
			  GenBean excessGenMC=(GenBean)mcInsDtls.get(sbGLExcess.toString());
			  GenBean epSpecGenMC=null;
				  //(GenBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arGen = new ArrayList();
				  arGen = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arGen.size();x++)
				  {
					  epSpecGenMC=(GenBean)arGen.get(x);
					  if(epSpecGenMC.getPolicyMstId()==0)
					  {
						  //epSpecGenMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }else if(uValidDao.chkEPSpc(epSpecGenMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecGenMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecGenMC=null;
					  }
				  }
			  }
			  UmbBean umbGenMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }
			  /*
			  String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasGenLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExGenLimit=0.0;
			  double dExGenDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /*Uday added end*/
			  /* to decide whether to check EP Specific or not..
	 		   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
	 		   *  in Primary and then it will be set to true 
	 		   */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.GENPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have policy ");
				  return arlGLStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlGLStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlGLStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlGLStatus;
			  }
			  //======================End added by Piyush ==========================

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires GL.. checking if MC has either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbGLPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  //arlGLStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlGLStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires GL.. so skipping Policy check ");
				  return arlGLStatus;
			  }
			  
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
				  //log.debug("dEPLimit :"+dEPLimit+" dEPDeduct:"+dEPDeduct);
			  }			  
			  if(primGenMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for General Liability");
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(paramDate.equals(Utility.stringToSqlDate(primGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlGLStatus;
						  }
						  if(Utility.stringToSqlDate(primGenMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  //log.debug("if policy expiration date is today's date");
							  arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							  return arlGLStatus;
						  }
						  //end----02/03
						
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primGenMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primGenMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primGenMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primGenMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primGenMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						/* End Termination */
				       
					   /*Start Self Insured check*/
				       if(GlobalVariables.YES.equals(primGenMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 	{
					  		 /*MC has SI and EP doesn't allow Self Insured*/
					  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
					  		 /*Check if EP has given overrides*/
					  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
					  		 {
					  			//log.debug("EP has Override for Self Insured");
					  			strOvrUsed=GlobalVariables.YES;
					  		 }
					  		 else
					  		 {
					  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
					  			arlGLStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
					  		 }
				  	 	}
				       else if(GlobalVariables.YES.equals(primGenMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				       {
				  		 //log.debug("Exiting Gen Liability Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				    	   if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
							  {
								  if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()))
								  {
									  //log.debug("MC has Blanket");
									  /*check if EP allows Blanket*/
									  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
									  {
										  //log.debug("EP allows Blanket..Addtional Insured OK");
									  }
									  else
									  {
										  //log.debug("EP doesn't allow Blanket");
										  //log.debug("Addtional Insured problem");
										  /*Check if EP has given overrides*/
								 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
								 		  {
								 			 //log.debug("EP has given overrides for Additional Insured");
								 		  }
								 		  /*End overrides code*/ 
								 		  else
								 		  {
								 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
								 		  }									  
									  }
								  }
								  else
								  {
									  //log.debug("MC doesn't have Blanket");
									  /*Check if EP has been selected as Addtioanl Insured*/
									  if(GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
									  {
										  //log.debug("EP selected as Additinal Insured(so OK) ");									 
									  }
									  else
									  {
										  //log.debug("Addtional Insured problem");
										  /*Check if EP has given overrides*/
								 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
								 		  {
								 			 //log.debug("EP has given overrides for Additional Insured");
								 		  }
								 		  /*End overrides code*/ 
								 		  else
								 		  {
								 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
								 		  }									  
									  }
								  }
							  }  
				  		 return arlGLStatus;
				       }
					   /*End Self Insured check*/
				       
					  
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primGenMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlGLStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					  
					 /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  /*check if MC has Blanket or Additional Insured Flag is Yes*/
							  /*if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()) || GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }*/
							  if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
						
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primGenMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primGenMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK)");
									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 //log.debug("Report additional Insured problem to UValid");
							 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  									  
								  }
							  }
						  }
					  }
					  /*End Additional Insured check*/
					  /**----------------------------------------------------------**/

					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasGenLimit=Utility.commaStringtoDouble(primGenMC.getLimit());
					  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
					  dMCHasDeduct=Utility.commaStringtoDouble(primGenMC.getDeductible());
					  if(excessGenMC!=null)
					  {
						  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit());
						  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible());
					  }
					  if(umbGenMC!=null && umbGenMC.getGLReqd().equals(GlobalVariables.YES))
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  
					  if(primGenMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*cndUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*cndUSD;
						  }
						  if(umbGenMC!=null && umbGenMC.getGLReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  //log.debug("Currency conversion dMCHasGenLimit  (CND $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExGenLimit 	(CND $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  if(primGenMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*mexUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*mexUSD;
						  }
						  if(umbGenMC!=null && umbGenMC.getGLReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  //log.debug("Currency conversion dMCHasGenLimit (MEX $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExAutoLimit    (MEX $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessGenMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessGenMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessGenMC.getPolicyTerminatedDate().length()==0) || paramDate.after(Utility.stringToSqlDate(excessGenMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasGenLimit=dMCHasGenLimit+dExGenLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getGLReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
								  dMCHasGenLimit=dMCHasGenLimit+dMCUmbrella;
						  }
						  
					  }
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasGenLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  /*****
					   * Added by piyush on 16 July2007************************************
					   * According to new waiver specs don't add Limit boosters to MC limit.
					   * Consider limit booster is the new limit of MC that is required by EP.
					   */
					  //Commented by Piyush as per above note on 16 July2007
					  //dMCHasGenLimit=dMCHasGenLimit+dMCLimBooster; //adding booster to Limits					  
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasGenLimit +"and EP Limit:- "+ dEPLimit+" for Comparision");
					  //Commented by Piyush as per above note on 16 July2007
					  //if(dMCHasGenLimit<dEPLimit)
					  if((dMCHasGenLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasGenLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  /*Deductible check*/
					  
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasGenLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
						
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  	ArrayList activeEpspcDtls = new ArrayList();
					  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primGenMC.getMcAcctNo(),GlobalVariables.GENPOLICY);
					  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
					  	{
					  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
					  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
					  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
					  	}
					  	/*Uday modification 24 May 12 start*/
					  	/* The pending EP SPEC policy also needs to be checked. 
					  	 * This will only be checked when the policy effective date is greater then current date*/
					  	if (!primGenMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate()))){
					  		ArrayList pendingEpspcDtls = new ArrayList();
					  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
					  				.getEpAcctNo(),
					  				primGenMC.getMcAcctNo(),
					  				GlobalVariables.GENPOLICY);
					  		if (pendingEpspcDtls != null
					  				&& pendingEpspcDtls.size() > 3) {
					  			dEpSpcPenLim = (Double) pendingEpspcDtls
					  			.get(0);
					  			dEpSpcPenDed = (Double) pendingEpspcDtls
					  			.get(1);
					  			IsPenEpSpcExist = (String) pendingEpspcDtls
					  			.get(2);
					  			strPenEpSpcEffDate = (String) pendingEpspcDtls
					  			.get(3);
					  		}
					  	}

					  if(bLimitNotOkFlg)
					  {
						  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
						  {}
						  else /* Uday modification 24 May 12 */
						  {
							  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
									  && (!primGenMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4))))
									  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
							  {
								  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
								  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
								  //log.debug("dEPLimit - "+dEPLimit);
							  }
							  else
							  {
								  arlGLStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
						  }
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
								  {}
								  else /* Uday modification 24 May 12 */
								  {
									  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
												  && (!primGenMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4))))
												  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
									  		
									  	}
									  	else{
									  		arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  	}
								  }
							  }
						  }
						  else
						  {
							  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
							  {}
							  else /* Uday modification 24 May 12 */
							  {
								  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
											  && (!primGenMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primGenMC.getPolicyEffDate(), Utility.FORMAT4))))
											  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
								  		
								  	}
								  	else{
								  		arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  	}
							  }
						  }
					  }
					  if(arlGLStatus.size()==0)
					  {
						  //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
				  //log.debug("Exiting Primary Policy Check for General Liability");
			  }//end of primGenMC!=null
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlGLStatus is not empty  as it has problems*/
			  
			  if(!bEPPolCheck && epSpecGenMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for General Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlGLStatus.removeAll(arlGLStatus);
				  
				  /*Repeating EP Specific same as that in Primary*/
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * General Liability */
				  strEPSpcUsed=GlobalVariables.GENPOLICY;
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(paramDate.equals(Utility.stringToSqlDate(epSpecGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlGLStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecGenMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecGenMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  //log.debug("if policy expiration date is today's date");
					  arlGLStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					  return arlGLStatus;
				  }
				  //end----02/03

				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecGenMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecGenMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecGenMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecGenMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecGenMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlGLStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  				  
				  /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(epSpecGenMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
			  	 	{
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			/*Check if EP doesn't allow SI*/
				  			strOvrUsed=GlobalVariables.YES;
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlGLStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
			  	 	}
			       else if(GlobalVariables.YES.equals(epSpecGenMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
			       {
			  		 //log.debug("Exiting GenLiability Check (after Self Insured Check)");
			  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
			  		 return arlGLStatus;
			       }
				   /*End Self Insured check*/
				  
				  
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   
				   if(GlobalVariables.YES.equals(epSpecGenMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							 strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlGLStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				   
				   /*End Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   
				   /*Start Additional Insured check
				    *Ep requires Additional Insured and policy doesn't have Additional Insured 
				    *(check blanket and ep blanket ok or Policy endorsed for this EP
				    **/
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  /*if(GlobalVariables.YES.equals(epSpecGenMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecGenMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  }*/
				 		if(GlobalVariables.YES.equals(epSpecGenMC.getBlanketReqd()))
						  {
							  //log.debug("MC has Blanket");
							  /*check if EP allows Blanket*/
							  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
							  {
								  //log.debug("EP allows Blanket..Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("EP doesn't allow Blanket");
								  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
						 		  }									  
							  }
						  }
						  else
						  {
							  //log.debug("MC doesn't have Blanket");
							  /*Check if EP has been selected as Addtioanl Insured*/
							  if(GlobalVariables.YES.equals(epSpecGenMC.getAddlnInsured()))
							  {
								  //log.debug("EP selected as Additinal Insured(so OK) ");									 
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  
							  }
						  }
				 	  }
				 	
				   }
				   else
				   {
				 	  //log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecGenMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
				 				 arlGLStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecGenMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK)");
				 				 
				 			  }
				 			  else
				 			  {
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 //log.debug("Report additional Insured problem to UValid");
						 			 arlGLStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  									  
				 			  }
				 		  }
				 	  }
				   }
				   /*End Additional Insured check*/
				   /**----------------------------------------------------------**/
				   
				   
				   /*Calculating Limits and Deductibles along with currency conversion*/
				   	  dMCHasGenLimit=Utility.commaStringtoDouble(epSpecGenMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(epSpecGenMC.getDeductible());
					  if(excessGenMC!=null)
						  {
						  	dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit());
						  	dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible());
						  }
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  if(epSpecGenMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*cndUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*cndUSD;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  //log.debug("Currency conversion dMCHasGenLimit  (CND $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(CND $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExGenLimit 	(CND $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella 	(CND $) "+dMCUmbrella);
					  }
					  if(epSpecGenMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasGenLimit=dMCHasGenLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessGenMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessGenMC.getLimit())*mexUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessGenMC.getDeductible())*mexUSD;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  //log.debug("Currency conversion dMCHasAutoLimit (MEX $) "+dMCHasGenLimit);
						  //log.debug("Currency conversion dMCHasDeduct 	(MEX $) "+dMCHasDeduct);
						  //log.debug("Currency conversion dExAutoLimit    (MEX $) "+dExGenLimit);
						  //log.debug("Currency conversion dMCUmbrella     (MEX $) "+dMCUmbrella);
					  }
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessGenMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessGenMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessGenMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessGenMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasGenLimit=dMCHasGenLimit+dExGenLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
						  }
					  }// end of if excessAutoMC
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getGLReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
								  dMCHasGenLimit=dMCHasGenLimit+dMCUmbrella;
						  }
						  
					  }
					 
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasGenLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note on 16July07
					  //dMCHasGenLimit=dMCHasGenLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasGenLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //Commented by piyush as per note on 16July07
					  //if(dMCHasGenLimit<dEPLimit)
					  if((dMCHasGenLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasGenLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit

					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasGenLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
						  if(bLimitNotOkFlg)
						  {
							  arlGLStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
						  }
						  if(bDedNotOkFlg)
						  {
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCDedBooster>dEPDeduct)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  arlGLStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
				  //log.debug("Exiting EP Specific Policy Check for General Liability");
			  }// end of EP Specific Policy
			  else
			  {
				  //arlGLStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlGLStatus;
			  }
			  
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
			  
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in General Liability Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkGeneralLiability() with Problems:- "+arlGLStatus);
		//log.info("Returning :== Override used flag:- "+strOvrUsed + " :== and EP Specific Flag:- "+ strEPSpcUsed);
		return arlGLStatus;
	}
	
	public ArrayList checkCargoLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{
		ArrayList arlCargoStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbCargoPrimary= new StringBuffer(GlobalVariables.CARGOPOLICY);
			  sbCargoPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbCargoExcess= new StringBuffer(GlobalVariables.CARGOPOLICY);
			  sbCargoExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.CARGOPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  //log.info("EPSPC0 :");
			  CargoBean primCrgMC=(CargoBean)mcInsDtls.get(sbCargoPrimary.toString());
			  CargoBean excessCrgMC=(CargoBean)mcInsDtls.get(sbCargoExcess.toString());
			  CargoBean epSpecCrgMC=null;
				  //(CargoBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  epSpecCrgMC=(CargoBean)arCg.get(x);
					  if(epSpecCrgMC.getPolicyMstId()==0)
					  {
						  //epSpecCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecCrgMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecCrgMC=null;
					  }
				  }
			  }
			  UmbBean umbGenMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }			  
			  //log.info("Done Bean Creation ");			  
			  /*
			  String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasCrgLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExGenLimit=0.0;
			  double dExGenDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /* to decide whether to check EP Specific or not..
	 		   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
	 		   *  in Primary and then it will be set to true 
	 		   */
			  boolean bEPPolCheck=false;
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.info("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.CARGOPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlCargoStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlCargoStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlCargoStatus;
			  }
			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  //log.debug("Cargo Ded Booster :"+epDtls.getEpOvrMCBean().getDedBooster());
			  //if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1") || epDtls.getEpOvrMCBean().getDedBooster().equals("-1"))
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1"))
			  {
				  //log.info("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlCargoStatus;
			  }
			  //======================End added by Piyush ==========================
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.info("EP requires Cargo.. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbCargoPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  //log.info("++MCNOPOLICY");
					  arlCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlCargoStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires cargo.. so skipping Policy check ");
				  return arlCargoStatus;
			  }

			  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
			  if(primCrgMC!=null)
			  {
				  if(paramDate.equals(Utility.stringToSqlDate(primCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlCargoStatus;
				  }
				  if(Utility.stringToSqlDate(primCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  //log.debug("if policy expiration date is today's date");
					  arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					  return arlCargoStatus;
				  }
			  }
			  //end----02/03
			  
			  /*Check if MC_Hauls_Only =Y, then to skip the test for Cargo...*/
			  //Modified by swati----22/02----for Cargo Hauls Only msg in smartchecklist and to send notification
			  if(primCrgMC!=null && primCrgMC.getHaulsOwnOnly().equals(GlobalVariables.YES)
					   && epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("Hauls Only=Y, so skipping Cargo Check");
				  arlCargoStatus.add(GlobalVariables.UVLD_CARGO_HAULSOWN);
				  //return arlCargoStatus;
			  }
			 
			  
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  if(primCrgMC!=null)
			  {
				  //log.debug("Entering Primary Policy Check for Cargo Liability");
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(primCrgMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(primCrgMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(primCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(primCrgMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(primCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				/* End Termination */
				       
				     /*Start Self Insured check*/
				  	 if(GlobalVariables.YES.equals(primCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
							 /*MC has SI and EP doesn't allow Self Insured*/
							 //log.debug("MC has SI and EP doesn't allow Self Insured");
							 /*Check if EP has given overrides*/
							 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
							 {
									//log.debug("EP has Override for Self Insured");
									/*Check if EP doesn't allow SI*/
									strOvrUsed=GlobalVariables.YES;
									
							 }
							 else
							 {
								 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
								 arlCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
							 }
					 }
					 else if(GlobalVariables.YES.equals(primCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						// log.debug("Exiting Cargo Liability Check (after Self Insured Check)");
						// log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlCargoStatus;
					 }
					 /*End Self Insured check*/
					   
					 
					   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   if(GlobalVariables.YES.equals(primCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
						  {
							  /*MC has RRG and EP doesn't allow RRG*/
							  //log.debug("MC has RRG and EP doesn't allow RRG");
							  /*Check if EP has given overrides*/
							  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
							  {
								  //log.debug("EP has Override for RRG");
								  strOvrUsed=GlobalVariables.YES;
							  }
							  else
							  {
								  //log.debug("EP doesn't allow RRG So RRG Problem");
								  arlCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
							  }
						  }
					  /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					   /**----------------------------------------------------------**/
					   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					   {
					 	  //log.debug("Start Additional Insured check for UIIEP");
					 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
					 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
					 		if(GlobalVariables.YES.equals(primCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(primCrgMC.getAddlnInsured()))
						 	  {
						 		  //log.debug("Addtional Insured OK");
						 	  }
						 	  else
						 	  {
						 		  //log.debug("Addtional Insured problem for UIIAEP");
						 		 arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 	  }
 
						  }
					 	  					 
					   }
					   else
					   {
					 	  //log.debug("Start Additional Insured check for other EPs");
					 	  /*Check if ep requires additional Insured*/
					 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		  //log.debug("EP requires Addtional Insured");
					 		  /*Check if MC has blanket*/
					 		  if(GlobalVariables.YES.equals(primCrgMC.getBlanketReqd()))
					 		  {
					 			  //log.debug("MC has Blanket");
					 			  /*check if EP allows Blanket*/
					 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
					 			  {
					 				  //log.debug("EP allows Blanket..Addtional Insured OK");
					 			  }
					 			  else
					 			  {
					 				  //log.debug("EP doesn't allow Blanket");
					 				  //log.debug("Addtional Insured problem");
					 				 /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {	  							 		  
							 			  arlCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }
					 			  }
					 		  }					 		 
					 		  else
					 		  {
					 			  //log.debug("MC doesn't have Blanket");
					 			  /*Check if EP has been selected as Addtioanl Insured*/
					 			  if(GlobalVariables.YES.equals(primCrgMC.getAddlnInsured()))
					 			  {
					 				  //log.debug("EP selected as Additinal Insured(so OK)");					 				 
					 			  }
					 			  else
					 			  {
					 				  log.debug("Addtional Insured problem");
					 				 /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			  arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }
					 			  }
					 		  }
					 	  }					 	 
					   }
					   /*End Additional Insured check*/
					   /**----------------------------------------------------------**/
					   

					  
					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasCrgLimit=Utility.commaStringtoDouble(primCrgMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(primCrgMC.getDeductible());
					  if(excessCrgMC!=null)
					  {
						  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit());
						  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible());
					  }
					  if(umbGenMC!=null && umbGenMC.getCargoReqd().equals(GlobalVariables.YES))
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  if(primCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasCrgLimit=dMCHasCrgLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessCrgMC!=null)
							  {
							  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*cndUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*cndUSD;
							  }
						  if(umbGenMC!=null && umbGenMC.getCargoReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(primCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasCrgLimit=dMCHasCrgLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessCrgMC!=null)
						  {
							  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*mexUSD ;
							  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*mexUSD;
						  }
						  if(umbGenMC!=null && umbGenMC.getCargoReqd().equals(GlobalVariables.YES))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }
					  //log.debug("After Currency conversion dMCHasCrgLimit "+dMCHasCrgLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  //log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
					 
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessCrgMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessCrgMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasCrgLimit=dMCHasCrgLimit+dExGenLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
						  }
					  }// end of if excessAutoMC
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getCargoReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasCrgLimit=dMCHasCrgLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasCrgLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //Commented by piyush as per note on 16July07
					  //dMCHasCrgLimit=dMCHasCrgLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasCrgLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //log.info("Excess Limit:- "+dExGenLimit +"and Umbrella Limit:- "+ dMCUmbrella+" for Comparision");
//					Commented by piyush as per note on 16July07
					  //if(dMCHasCrgLimit<dEPLimit)
					  if((dMCHasCrgLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasCrgLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					 
					  /*Deductible check*/
					 // log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March 07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						 //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasCrgLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  	ArrayList activeEpspcDtls = new ArrayList();
					  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primCrgMC.getMcAcctNo(),GlobalVariables.CARGOPOLICY);
					  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
					  	{
					  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
					  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
					  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
					  	}
					  	/*Checking for Multiple limits against EP Specific In-place coverage*/
					  	if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  if(bLimitNotOkFlg||bDedNotOkFlg)
							  {
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dEpSpcActLim>=dTempEPLimit)&&(dEpSpcActDed<=dTempEPDeduct))
									  {
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
									  }
								  }
							  }
						  }

					  	
					  	/*Uday modification 24 May 12 start*/
				  		/* The pending EP SPEC policy also needs to be checked. 
				  		 * This will only be checked when the policy effective date is greater then current date*/
					  	if (!primCrgMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate())))
					  	{
					  		ArrayList pendingEpspcDtls = new ArrayList();
					  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
					  				.getEpAcctNo(),
					  				primCrgMC.getMcAcctNo(),
					  				GlobalVariables.CARGOPOLICY);
					  		if (pendingEpspcDtls != null
					  				&& pendingEpspcDtls.size() > 3) {
					  			dEpSpcPenLim = (Double) pendingEpspcDtls
					  			.get(0);
					  			dEpSpcPenDed = (Double) pendingEpspcDtls
					  			.get(1);
					  			IsPenEpSpcExist = (String) pendingEpspcDtls
					  			.get(2);
					  			strPenEpSpcEffDate = (String) pendingEpspcDtls
					  			.get(3);
					  		}

					  		if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  		{
					  			if (IsPenEpSpcExist.equalsIgnoreCase("Y") 
					  					&& (!(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
					  					&& dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit) {
					  				if (bLimitNotOkFlg || bDedNotOkFlg) {
					  					for (int i = 0; i < epDtls
					  					.getPolicyMulLimits().size(); i++) {
					  						double dTempEPLimit = Utility
					  						.commaStringtoDouble(((MultipleLimit) epDtls
					  								.getPolicyMulLimits()
					  								.get(i))
					  								.getMinLimit());
					  						double dTempEPDeduct = Utility
					  						.commaStringtoDouble(((MultipleLimit) epDtls
					  								.getPolicyMulLimits()
					  								.get(i))
					  								.getMaxDed());
					  						if ((dEpSpcPenLim >= dTempEPLimit)
					  								&& (dEpSpcPenDed <= dTempEPDeduct)) {
					  							bLimitNotOkFlg = false;
					  							bDedNotOkFlg = false;
					  						}
					  					}
					  				}
					  			}
					  		}
					  	}
					  	/*Uday modification 24 May 12 end*/
					  	
					  if(bLimitNotOkFlg)
					  {
						  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
						  {}
						  else /* Uday modification 24 May 12 */
						  {
							  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
									  && (!primCrgMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
									  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
							  {
								  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
								  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
								  //log.debug("dEPLimit - "+dEPLimit);
							  }
							  else
							  {
								  arlCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
						  }
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //log.debug(">>>>dMCDedBooster ="+dMCDedBooster +" >>>>dEPDeduct :"+dEPDeduct);
							  //if(dMCDedBooster>dEPDeduct)//Changed by Piyush 27March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else
								  {
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
												  && (!primCrgMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
												  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist

										  }
										  else{
											  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  }
									  }
								  }
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else
							  {
								  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
								  {}
								  else /* Uday modification 24 May 12 */
								  {
									  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
											  && (!primCrgMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primCrgMC.getPolicyEffDate(), Utility.FORMAT4))))
											  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist

									  }
									  else{
										  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  }
								  }
							  }
						  }
					  }
					  if(arlCargoStatus.size()==0)
					  {
						  //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
					  
				  //log.debug("Exiting Primary Policy Check for Cargo Liability");
			  }//end of Primary Policy check
			  if(!bEPPolCheck && epSpecCrgMC!=null )
			  {
				  /*Repeating EP Specific same as that in Primary*/
				  //log.debug("Entering EP Specific Policy Check for Cargo Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlCargoStatus.removeAll(arlCargoStatus);
				  
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Cargo Liability */
				  strEPSpcUsed=GlobalVariables.CARGOPOLICY;
				  
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(paramDate.equals(Utility.stringToSqlDate(epSpecCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlCargoStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  //log.debug("if policy expiration date is today's date");
					  arlCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					  return arlCargoStatus;
				  }
				  //end----02/03
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecCrgMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecCrgMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecCrgMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /* if(epSpecCrgMC.getPolicyTerminatedDate().length()>0)
				   {
					  //log.debug("EP Specific Policy Terminated Date= "+epSpecCrgMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Policy Terminated on parameter date ");
					      arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecCrgMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecCrgMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
					 if(GlobalVariables.YES.equals(epSpecCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting EPSpec CargoLiability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlCargoStatus;
					 }
				   /*End Self Insured check*/
				
				   
				   
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   if(GlobalVariables.YES.equals(epSpecCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				   
				  /*Start Additional Insured check
				  * Ep requires Additional Insured and policy doesn't have Additional Insured 
				  * (check blanket and ep blanket ok or Policy endorsed for this EP
				  * */
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		if(GlobalVariables.YES.equals(epSpecCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecCrgMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  }
				 	  }
				   }
				   else
				   {
				 	  //log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecCrgMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
				 				 arlCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecCrgMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK) ");
				 				 
				 			  }
				 			  else
				 			  {
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			  arlCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }
				 			  }
				 		  }
				 	  }
				   }
				   /**----------------------------------------------------------**/
				   
				   				   
				   /*Calculating Limits and Deductibles along with currency conversion*/
				  dMCHasCrgLimit=Utility.commaStringtoDouble(epSpecCrgMC.getLimit());
				  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
				  dMCHasDeduct=Utility.commaStringtoDouble(epSpecCrgMC.getDeductible());
				  if(excessCrgMC!=null)
				  {
					  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit());
					  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible());
				  }
				  if(umbGenMC!=null)
					  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
				  if(epSpecCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  //log.debug("Canadian Currency Conversion");
					  dMCHasCrgLimit=dMCHasCrgLimit*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
					  if(excessCrgMC!=null)
						  {
						  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*cndUSD ;
						  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*cndUSD;
						  }
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
				  }
				  else if(epSpecCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  //log.debug("Mexican Currency Conversion");
					  dMCHasCrgLimit=dMCHasCrgLimit*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
					  if(excessCrgMC!=null)
					  {
						  dExGenLimit=Utility.commaStringtoDouble(excessCrgMC.getLimit())*mexUSD ;
						  dExGenDed=Utility.commaStringtoDouble(excessCrgMC.getDeductible())*mexUSD;
					  }
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
				  }  
				  //log.debug("After Currency conversion dMCHasCrgLimit "+dMCHasCrgLimit);
				  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
				  //log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for excess policy and if it exists to add to MCLimit*/
				  if(excessCrgMC!=null )
				  {
					  //log.debug("Excess Policy Terminated Date:- "+excessCrgMC.getPolicyTerminatedDate());
					  /*check if policy terminated date is empty or less than param date*/
					  if((excessCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4) )) 
					  {
						  //log.debug("Excess policy termination date less than parameter date ");
						  dMCHasCrgLimit=dMCHasCrgLimit+dExGenLimit;
						  //dMCHasDeduct=dMCHasDeduct+dExGenDed;
					  }
				  }// end of if excessAutoMC
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				  
				  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getCargoReqd()))
				  {
					  /*check if umbrella policy less than or equal to param date*/
					  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
					  {
						 	  //log.debug("Umbrella policy termination date after parameter date ");
						 	 dMCHasCrgLimit=dMCHasCrgLimit+dMCUmbrella;
					  }
					  
				  }
				  
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if((dMCHasCrgLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Limits |Deductibles override used");
					  bOvrUsed=true;
				  }
				  /******************************************************************************/
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  //dMCHasCrgLimit=dMCHasCrgLimit+dMCLimBooster; //adding booster to Limits
				  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
				  /*******************************************************************************/
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  //log.info("MC Limit:- "+dMCHasCrgLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
				  //log.info("MC Limit:- "+dMCHasCrgLimit +"EP Limit:- "+ dEPLimit+" for Comparision");
				  //if(dMCHasCrgLimit<dEPLimit)
				  if((dMCHasCrgLimit<dEPLimit) && (dMCLimBooster==0))
				  {//Added as per new waiver specs
					  bLimitNotOkFlg=true;
				  }
				  else if(dMCHasCrgLimit<dMCLimBooster)
				  {	
					  //log.info("Limit Not Ok before multiple Pol Limits check");
					  bLimitNotOkFlg=true;
				  }// end if limit
				  
				  /*Deductible check*/
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }
				  
				  /*Check if multiple limits/deductible exists for this policy */
				  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
				  {
					  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
					  /*will loop through Multiple limits and deductibles bean to find appropriate
					   * limits and deductible to be used for the MC.
					   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
					   * */
					  if(bLimitNotOkFlg||bDedNotOkFlg)
					  {
						  //log.debug("Checking Policy Multiple Limits and Deductibles ");
						  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
						  {
							  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
							  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
							  if((dMCHasCrgLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
							  {
								  /*found Multiple Limits and deductibles so using that limit and deductibles
								   * to check MC Limits and Deductibles; 
								   * */
								  bLimitNotOkFlg=false;
								  bDedNotOkFlg=false;
								  //break;
							  }
						  }// end of for loop
					  }//end if bLimitOkFlg
				  }
				  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
				   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
				   * not found for Multiple Limits and Deductibles, so setting appropriate message
				   * */
				  if(bLimitNotOkFlg)
				  {
					  arlCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(dMCDedBooster!=0)
					  {
						  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
						  if(dMCDedBooster>dEPDeduct)
						  {
							 // log.debug("Ded problem after replacing Booster");
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
								  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  else
					  {
						  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
						  else	
							  arlCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
					  }
				  }
				 // log.debug("Exiting EP Specific Policy Check for Cargo Liability");
			  }//end of EP Specific Policy Check

			  if(primCrgMC==null && epSpecCrgMC==null)
			  {
				  //log.info("MCNOPOLICY++");
				  arlCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Cargo Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkCargoLiability()of UValidPolicyCheck class:- "+arlCargoStatus);
		return arlCargoStatus;
	}
	
	public ArrayList checkTrailerLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{
		ArrayList arlTrailerStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbTrlPrimary= new StringBuffer(GlobalVariables.TRAILERPOLICY);
			  sbTrlPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbTrlExcess= new StringBuffer(GlobalVariables.TRAILERPOLICY);
			  sbTrlExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.TRAILERPOLICY);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  TrailerBean primTrlMC=(TrailerBean)mcInsDtls.get(sbTrlPrimary.toString());
			  TrailerBean excessTrlMC=(TrailerBean)mcInsDtls.get(sbTrlExcess.toString());
			  TrailerBean epSpecTrlMC=null;
				  //(TrailerBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {		
			  	  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  epSpecTrlMC=(TrailerBean)arCg.get(x);
					  if(epSpecTrlMC.getPolicyMstId()==0)
					  {
						  //epSpecTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecTrlMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecTrlMC=null;
					  }
				  }
			  }		
				  
			  UmbBean umbGenMC=null;
			  if(mcInsDtls.containsKey(GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  }else if (mcInsDtls.containsKey(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA))
			  {
				  umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.MOTORCARRIER+GlobalVariables.UMBRELLA);
			  }	
			  
			  /*String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasTILimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExTILimit=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  double dEpSpcActDed = 0.0;
			  double dEpSpcActLim = 0.0;
			  String IsEpSpcExist = "N";
			  /*Uday added start*/
			  double dEpSpcPenDed = 0.0;
			  double dEpSpcPenLim = 0.0;
			  String IsPenEpSpcExist = "N";
			  String strPenEpSpcEffDate = "";
			  /*Uday added end*/
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.TRAILERPOLICY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlTrailerStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlTrailerStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlTrailerStatus;
			  }

			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster= Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  
			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  //log.debug("Trailer OVERRIDDEN :"+epDtls.getEpOvrMCBean());
			  //log.debug("Trailer Booster :"+epDtls.getEpOvrMCBean().getLimitBooster());
			  //if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1") || epDtls.getEpOvrMCBean().getDedBooster().equals("-1"))
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0 || epDtls.getEpOvrMCBean().getLimitBooster().equals("-1"))
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlTrailerStatus;
			  }
			  //======================End added by Piyush ==========================
			  
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires  Trailer.. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbTrlPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlTrailerStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlTrailerStatus;
			  }
			  
			  if(primTrlMC!=null)
			  {
				       //log.debug("Entering Trailer Primary Policy Check :"+primTrlMC.getPolicyMstId());
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(paramDate.equals(Utility.stringToSqlDate(primTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlTrailerStatus;
						  }
						  if(Utility.stringToSqlDate(primTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlTrailerStatus;
						  }
						  //end----02/03
				       
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primTrlMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primTrlMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primTrlMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						/* End Termination */
				       
					   /*Start Self Insured check*/
				     if(GlobalVariables.YES.equals(primTrlMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				       {
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			strOvrUsed=GlobalVariables.YES;
				  			
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
				  	 }
				  	 else if(GlobalVariables.YES.equals(primTrlMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 //log.debug("Exiting Trailer Liability Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				  		if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primTrlMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primTrlMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  	}	
							}
				  		 return arlTrailerStatus;
				  	 }
				   /*End Self Insured check*/
					  
					   
					 
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					 
					  /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  /*check if MC has Blanket or Additional Insured Flag is Yes*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(primTrlMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }  
						  }
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primTrlMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primTrlMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
						 			  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  
								  }
							  }
						  }
					  }
					  /*End Additional Insured check*/
					  /**----------------------------------------------------------**/
					  

					  /*Checking Limits and Deductibles if ACV Not present*/
					  if(primTrlMC.getAcv().equals(GlobalVariables.NO)||primTrlMC.getAcv().length()==0)
					  {
						  //log.debug("ACV Not Present hence checking  Limits and Deductibles");
						  /*Calculating Limits and Deductibles along with currency conversion*/
						  dMCHasTILimit=Utility.commaStringtoDouble(primTrlMC.getLimit());
						  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
						  dMCHasDeduct=Utility.commaStringtoDouble(primTrlMC.getDeductible());
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(primTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(primTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  //log.debug("After Currency conversion dMCHasTILimit "+dMCHasTILimit);
						  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
						  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
						  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						  if(excessTrlMC!=null )
						  {
							  //log.debug("Excess Policy Terminated Date:- "+excessTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							 /* if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						 
						  
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  //Commented by piyush as per note on 16 July2007
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/
						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision -"+dEPLimit);
						  //Commented by piyush as per note on 16 July2007				  
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  //log.info("LIMIT TO BE CHECKED");
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
								  //log.debug("Limit Not Ok before multiple Pol Limits check");
								  bLimitNotOkFlg=true;
						  }// end if limit
						  
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)//Commented By Piyush 28March07
						  //if(dEPDeduct>=0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible  
						  }
						  
						  //log.debug(">>>>dMCDedBooster ="+dMCDedBooster +" >>>>dMCHasDeduct :"+dMCHasDeduct);
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg||bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }//end check multiple limits and deductibles
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
						  	ArrayList activeEpspcDtls = new ArrayList();
						  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primTrlMC.getMcAcctNo(),GlobalVariables.TRAILERPOLICY);
						  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
						  	{
						  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
						  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
						  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
						  	}
						  	
						  		/*Uday modification 24 May 12 start*/
						  		/* The pending EP SPEC policy also needs to be checked. 
						  		 * This will only be checked when the policy effective date is greater then current date*/
						  	if (!primTrlMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate()))){
						  		ArrayList pendingEpspcDtls = new ArrayList();
						  		pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
						  				.getEpAcctNo(),
						  				primTrlMC.getMcAcctNo(),
						  				GlobalVariables.TRAILERPOLICY);
						  		if (pendingEpspcDtls != null
						  				&& pendingEpspcDtls.size() > 3) {
						  			dEpSpcPenLim = (Double) pendingEpspcDtls
						  			.get(0);
						  			dEpSpcPenDed = (Double) pendingEpspcDtls
						  			.get(1);
						  			IsPenEpSpcExist = (String) pendingEpspcDtls
						  			.get(2);
						  			strPenEpSpcEffDate = (String) pendingEpspcDtls
						  			.get(3);
						  		}
						  	}
						//log.debug("dEpSpcActLim - "+dEpSpcActLim);
						  //log.debug("dEPLimit - "+dEPLimit);
						  if(bLimitNotOkFlg)
						  {
							  if(dEpSpcActLim>0 && dEpSpcActLim>=dEPLimit)
							  {
								  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
								  //log.debug("dEpSpcActLim - "+dEpSpcActLim);
								  //log.debug("dEPLimit - "+dEPLimit);
							  }
							  else /* Uday modification 24 May 12 */
								  {
								  if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
										  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
										  && dEpSpcPenLim>0 && dEpSpcPenLim>=dEPLimit)
								  {
									  //log.debug("EP Acct No - "+epDtls.getEpAcctNo());
									  //log.debug("dEpSpcPenLim - "+dEpSpcPenLim);
									  //log.debug("dEPLimit - "+dEPLimit);
								  }
								  else
									  {
									  	arlTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
									  }
								  }
						  }						  
						  if(bDedNotOkFlg)
						  {
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  //log.debug(">>>>dMCDedBooster ="+dMCDedBooster +" >>>>dMCHasDeduct :"+dMCHasDeduct);
								  //if(dMCDedBooster>dEPDeduct) //Changed by piyush 27March07
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  {
										  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
										  {}
										  else /* Uday modification 24 May 12 */
										  {
											  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
														  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
														  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
											  		
											  	}
											  	else{
											  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
											  	}
										  }
									  }
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  {
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
													  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
													  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
										  		
										  	}
										  	else{
										  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  	}
									  }
								  }
							  }
						  }
					  }
					  else
					  {
						  log.debug("ACV Present hence skipping Limits but do check Deductibles ");
						  //=============Added By Piyush on 6June'08 According to Debbie's e-mail stated Deductible should be check if ACV is present========= 
						  dMCHasDeduct=Utility.commaStringtoDouble(primTrlMC.getDeductible());
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(primTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(primTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  if(dEPDeduct>0)//Commented By Piyush 28March07
						  {
								  if(dMCHasDeduct>dEPDeduct)
								  {
									  //log.debug("Deductible Not Ok before multiple Pol Limits check");
									  bDedNotOkFlg=true;
								  }// end if deductible  
						  }
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  if(bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if(dMCHasDeduct<=dTempEPDeduct)
									  {
										  bDedNotOkFlg=false;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }
						  if(bDedNotOkFlg)
						  {
							  	ArrayList activeEpspcDtls = new ArrayList();
							  	activeEpspcDtls = uValidDao.getDecEpSpcActive(epDtls.getEpAcctNo(),primTrlMC.getMcAcctNo(),GlobalVariables.TRAILERPOLICY);
							  	if(activeEpspcDtls!=null && activeEpspcDtls.size()>2)
							  	{
							  		dEpSpcActLim = (Double)activeEpspcDtls.get(0);
							  		dEpSpcActDed = (Double)activeEpspcDtls.get(1);
							  		IsEpSpcExist = (String)activeEpspcDtls.get(2);
							  	}
							  	/*Uday modification 24 May 12 start*/
							  	/* The pending EP SPEC policy also needs to be checked. 
							  	 * This will only be checked when the policy effective date is greater then current date*/
						  		if (!primTrlMC.getPolicyEffDate().equals("") && (Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4).after(Utility.getSqlSysdate()))){
								ArrayList pendingEpspcDtls = new ArrayList();
								pendingEpspcDtls = uValidDao.getEpSpcPending(epDtls
										.getEpAcctNo(),
										primTrlMC.getMcAcctNo(),
										GlobalVariables.TRAILERPOLICY);
								if (pendingEpspcDtls != null
										&& pendingEpspcDtls.size() > 3) {
									dEpSpcPenLim = (Double) pendingEpspcDtls
											.get(0);
									dEpSpcPenDed = (Double) pendingEpspcDtls
											.get(1);
									IsPenEpSpcExist = (String) pendingEpspcDtls
											.get(2);
									strPenEpSpcEffDate = (String) pendingEpspcDtls
											.get(3);
								}
							}
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else
									  {
										  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
										  {}
										  else /* Uday modification 24 May 12 */
										  {
											  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
														  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
														  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
											  		
											  	}
											  	else{
											  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
											  	}
										  }
									  }
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  {
									  if((dEpSpcActDed>0 && dEpSpcActDed<=dEPDeduct) || (dEpSpcActDed==0 && IsEpSpcExist.equalsIgnoreCase(GlobalVariables.YES)))
									  {}
									  else /* Uday modification 24 May 12 */
									  {
										  	if(IsPenEpSpcExist.equalsIgnoreCase("Y") 
													  && (!primTrlMC.getPolicyEffDate().equals("") && !(Utility.stringtoUtilDate(strPenEpSpcEffDate, Utility.FORMAT4).after(Utility.stringtoUtilDate(primTrlMC.getPolicyEffDate(), Utility.FORMAT4))))
													  && ( dEpSpcPenDed>0 && dEpSpcPenDed<=dEPDeduct || (dEpSpcPenDed==0 && IsPenEpSpcExist.equalsIgnoreCase(GlobalVariables.YES) ))){ // Uday to get clarity on last clause IsPenEpSpcExist
										  		
										  	}
										  	else{
										  		arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
										  	}
									  }
								  }
							  }
						  }
						  //=============End added By Piyush=====================================
					  }					  
					  if(arlTrailerStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }

					  
				  //log.debug("Exiting Primary Policy Check for Trailer Liability");
			  }//end of primary  Policy check
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlGLStatus is not empty  as it has problems*/
			  if(!bEPPolCheck && epSpecTrlMC!=null )
			  {
				  log.debug("Entering EP Specific Policy Check for Trailer Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  //log.debug("TIPRIMARY PROBLEMS :"+arlTrailerStatus);
				  bLimitNotOkFlg=false;
				  bDedNotOkFlg=false;
				  bOvrUsed=false;
				  arlTrailerStatus.removeAll(arlTrailerStatus);
				  /*Repeating EP Specific same as that in Primary*/
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Trailer Liability */
				  strEPSpcUsed=GlobalVariables.TRAILERPOLICY;
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(paramDate.equals(Utility.stringToSqlDate(epSpecTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlTrailerStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlTrailerStatus;
				  }
				  //end----02/03
				 
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecTrlMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecTrlMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecTrlMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /* if(epSpecTrlMC.getPolicyTerminatedDate().length()>0)
				   {
					  log.debug("EP Specific Policy Terminated Date= "+epSpecTrlMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      log.debug(" Policy Termination on parameter date ");
					      arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecTrlMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecTrlMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
				   if((epSpecTrlMC.getSelfInsured().equals(GlobalVariables.YES))&&(epDtls.getEpNeeds().getSelfInsReq().equals(GlobalVariables.NO)))
				   {
					   //log.debug("Self Insured Problem before override check");
					   /*check if override given for Self Insured*/
					   if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
					   {
						   //log.debug("EP has Override for Self Insured");
				  		   strOvrUsed=GlobalVariables.YES;
					   }
					   else if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
					   {
						   //log.debug("EP doesn't allow Self Insured So self Insured Problem");
						   arlTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
					   }
				   }
				   else if((epSpecTrlMC.getSelfInsured().equals(GlobalVariables.YES))&&(epDtls.getEpNeeds().getSelfInsReq().equals(GlobalVariables.YES)))
				   {
					   //log.debug("Exiting GeneralLiability Check (For Self Insured)");
				       //log.debug("To skip All the Policy Test Status Ok as no policy check required");
					   return arlTrailerStatus;
				   }
				   /*End Self Insured check*/
				  
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   if(GlobalVariables.YES.equals(epSpecTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				   {
					   /*MC has RRG and EP doesn't allow RRG*/
					   //log.debug("MC has RRG and EP doesn't allow RRG");
					   /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
				   }
				   
				   /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
				   
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		/*check if MC has Blanket or Additional Insured Flag is Yes*/
					 	  if(GlobalVariables.YES.equals(epSpecTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecTrlMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  } 
				 	  }
				   }
				   else
				   {
				 	  log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecTrlMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
				 				 arlTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecTrlMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK) ");				 				 
				 			  }
				 			  else
				 			  {
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  
				 			  }
				 		  }
				 	  }
				   }
				   /**----------------------------------------------------------**/
				   
				   
				   if(epSpecTrlMC.getAcv().equals(GlobalVariables.NO) || epSpecTrlMC.getAcv().length()==0)
				   {
					   //log.debug("ACV Not Present hence checking Limits and Deductibles");
					   /*Calculating Limits and Deductibles along with currency conversion*/					   	  
					   	  dMCHasTILimit=Utility.commaStringtoDouble(epSpecTrlMC.getLimit());
						  dMCHasDeduct=Utility.commaStringtoDouble(epSpecTrlMC.getDeductible());
						  //log.debug("EPSPCPOL DETAILS LM0:"+dMCHasTILimit+"="+epSpecTrlMC.getPolicyMstId());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						 // if(excessTrlMC!=null )
						  {
							  ////log.debug("Excess Policy Terminated Date:- "+excessTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							  /*if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  //log.debug("EPSPCPOL DETAILS LM1:"+dMCHasTILimit+"="+epSpecTrlMC.getPolicyMstId());
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  /*****
						   * Added by piyush on 16 July2007************************************
						   * According to new waiver specs don't add Limit boosters to MC limit.
						   * Consider limit booster is the new limit of MC that is required by EP.
						   */
						  //Commented by piyush as per note on 16 July2007
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/
						
						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision :"+dEPLimit);
						  //Commented by piyush as per note on 16 July2007
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
								  log.debug("Limit Not Ok before multiple Pol Limits check");
								  bLimitNotOkFlg=true;
						  }// end if limit
						  else
						  {
							  //log.debug("Limit ELSE");
							  bLimitNotOkFlg=false;
						  }
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible 
							  else
							  {
								  bDedNotOkFlg=false;
							  }
						  }
						  						  
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg||bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
							  if(bLimitNotOkFlg)
							  {
								  arlTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
							  //Added by piyush if EPSPC provided along with PRIMARY
							  arlTrailerStatus.remove(GlobalVariables.UVLD_DED_PRBLM);
							  if(bDedNotOkFlg)
							  {
								  
								  if(dMCDedBooster!=0)
								  {
									  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
									  if(dMCDedBooster>dEPDeduct)
									  {
										  //log.debug("Ded problem after replacing Booster");
										  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
										  else	
										  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  }
								  }
								  else
								  {
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  
				   }//end of ACV check
				   else
				   {
					   log.debug("ACV Present hence skipping Limits but do check Deductibles");
					   //=================Added by Piyush 06Jun'08===================================
						  dMCHasDeduct=Utility.commaStringtoDouble(epSpecTrlMC.getDeductible());
						  //log.debug("EPSPCPOL DETAILS LM0:"+dMCHasTILimit+"="+epSpecTrlMC.getPolicyMstId());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(epSpecTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  if(dEPDeduct>0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible 
							  else
							  {
								  bDedNotOkFlg=false;
							  }
						  }
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bDedNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {									  
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if(dMCHasDeduct<=dTempEPDeduct)
									  {
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg							  
						  }
						  arlTrailerStatus.remove(GlobalVariables.UVLD_DED_PRBLM);
						  if(bDedNotOkFlg)
						  {
							  
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCDedBooster>dEPDeduct)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
					   //=================End added by Piyush=========================================
				   }
				      
				  //log.debug("Exiting EP Specific Policy Check for Trailer Liability");
			  }
			  
			  if(primTrlMC==null && epSpecTrlMC==null)
			  {
				  arlTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlTrailerStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Trailer Liability Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkTrailerLiability()of UValidPolicyCheck class:- "+arlTrailerStatus);
		return arlTrailerStatus;
	}
	
	public ArrayList checkContCargoLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{

		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkContCargoLiabilityArch()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlContCargoStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbContCargoPrimary= new StringBuffer(GlobalVariables.CONTCARGO);
			  sbContCargoPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbContCargoExcess= new StringBuffer(GlobalVariables.CONTCARGO);
			  sbContCargoExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.CONTCARGO);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			 
			  ContCargoBean primCntCrgMC=(ContCargoBean)mcInsDtls.get(sbContCargoPrimary.toString());
			 
			  ContCargoBean excessCntCrgMC=(ContCargoBean)mcInsDtls.get(sbContCargoExcess.toString());
			 
			  ContCargoBean epSpecCntCrgMC=null;			  
				  //(ContCargoBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			 
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arCc = new ArrayList();
				  arCc = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCc.size();x++)
				  {
					  epSpecCntCrgMC=(ContCargoBean)arCc.get(x);
					  if(epSpecCntCrgMC.getPolicyMstId()==0)
					  {
						  //epSpecCntCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecCntCrgMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecCntCrgMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecCntCrgMC=null;
					  }
				  }
			  }
			  
			  UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			 
			  /*String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			 */
			  double dMCHasCCLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExCCLimit=0.0;
			  double dExCCDed=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			
			   /* to decide whether to check EP Specific or not..
			   * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			   *  in Primary and then it will be set to true 
			   */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.CONTCARGO.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlContCargoStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlContCargoStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlContCargoStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlContCargoStatus;
			  }
			  //======================End added by Piyush ==========================

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbContCargoPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlContCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlContCargoStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlContCargoStatus;
			  }
			  
			  /*Check if MC_Hauls_Only =Y, then to skip the test for Cargo...*/
			  /* Not Required for Contingent Cargo
			   * if(primCntCrgMC!=null && primCntCrgMC.getHaulsOwnOnly().equals(GlobalVariables.YES))
			  {
				  //log.debug("Hauls Only=Y, so skipping Cargo Check");
				  return arlContCargoStatus;
			  }*/
			  
			
			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  
			  if(primCntCrgMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for Contingent Cargo Liability");
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(paramDate.equals(Utility.stringToSqlDate(primCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlContCargoStatus;
						  }
						  if(Utility.stringToSqlDate(primCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlContCargoStatus;
						  }
						  //end----02/03
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primCntCrgMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primCntCrgMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primCntCrgMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primCntCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						/* End Termination */
				       
				       /*Start Self Insured check*/
				       if(GlobalVariables.YES.equals(primCntCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				       {
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			/*Check if EP doesn't allow SI*/
				  			strOvrUsed=GlobalVariables.YES;
				  			
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlContCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
				  	 }
				  	 else if(GlobalVariables.YES.equals(primCntCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 //log.debug("Exiting Cont Cargo Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				  		 return arlContCargoStatus;
				  	 }
   				    /*End Self Insured check*/
					   
				      
					   
					   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   if(GlobalVariables.YES.equals(primCntCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
						  {
							  /*MC has RRG and EP doesn't allow RRG*/
							  //log.debug("MC has RRG and EP doesn't allow RRG");
							  /*Check if EP has given overrides*/
							  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
							  {
								  //log.debug("EP has Override for RRG");
								  strOvrUsed=GlobalVariables.YES;
							  }
							  else
							  {
								  //log.debug("EP doesn't allow RRG So RRG Problem");
								  arlContCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
							  }
						  }
					   /*End Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   
					   /*Start Additional Insured check
					   * Ep requires Additional Insured and policy doesn't have Additional Insured 
					   * (check blanket and ep blanket ok or Policy endorsed for this EP
					   * */
					   /**----------------------------------------------------------**/
					   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					   {
					 	  //log.debug("Start Additional Insured check for UIIEP");
					 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
					 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		if(GlobalVariables.YES.equals(primCntCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(primCntCrgMC.getAddlnInsured()))
						 	  {
						 		  //log.debug("Addtional Insured OK");
						 	  }
						 	  else
						 	  {
						 		  //log.debug("Addtional Insured problem for UIIAEP");
						 		 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 	  } 
					 	  }
					   }
					   else
					   {
					 	  //log.debug("Start Additional Insured check for other EPs");
					 	  /*Check if ep requires additional Insured*/
					 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		  //log.debug("EP requires Addtional Insured");
					 		  /*Check if MC has blanket*/
					 		  if(GlobalVariables.YES.equals(primCntCrgMC.getBlanketReqd()))
					 		  {
					 			  //log.debug("MC has Blanket");
					 			  /*check if EP allows Blanket*/
					 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
					 			  {
					 				  //log.debug("EP allows Blanket..Addtional Insured OK");
					 			  }
					 			  else
					 			  {
					 				  //log.debug("EP doesn't allow Blanket");
					 				  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlContCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
							 		  }									  									  					 				 
					 			  }
					 		  }
					 		  else
					 		  {
					 			  //log.debug("MC doesn't have Blanket");
					 			  /*Check if EP has been selected as Addtioanl Insured*/
					 			  if(GlobalVariables.YES.equals(primCntCrgMC.getAddlnInsured()))
					 			  {
					 				  //log.debug("EP selected as Additinal Insured(so OK) ");
					 				 
					 			  }
					 			  else
					 			  {
					 				  //log.debug("Addtional Insured problem");
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  									  					 				 
					 			  }
					 		  }
					 	  }
					   }
					   /*End Additional Insured check*/
					   /**----------------------------------------------------------**/
					   

					  /*Calculating Limits and Deductibles along with currency conversion*/
					 
					  dMCHasCCLimit=Utility.commaStringtoDouble(primCntCrgMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(primCntCrgMC.getDeductible());
					 
					  if(excessCntCrgMC!=null)
					  {
						  dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit());
						  dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible());
					  }
					 
					  if(primCntCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessCntCrgMC!=null)
							  {
							  	dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*cndUSD ;
							  	dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*cndUSD ;
							  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(primCntCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessCntCrgMC!=null)
						  {
						  	dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*mexUSD ;
						  	dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*mexUSD ;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }
					  //log.debug("After Currency conversion dMCHasCrgLimit "+dMCHasCCLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  //log.debug("After Currency conversion dExGenLimit "+dExCCLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
					  
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessCntCrgMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessCntCrgMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessCntCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasCCLimit=dMCHasCCLimit+dExCCLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExCCDed;
						  }
					  }// end of if excessAutoMC
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getContCargoReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasCCLimit=dMCHasCCLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasCCLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //dMCHasCCLimit=dMCHasCCLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasCCLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //if(dMCHasCCLimit<dEPLimit)
					  if((dMCHasCCLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasCCLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					 
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasCCLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  if(bLimitNotOkFlg)
					  {
						  arlContCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //if(dMCDedBooster>dEPDeduct)Changed by piyush 27 March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
									  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
								  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  if(arlContCargoStatus.size()==0)
					  {
						  //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
						  bEPPolCheck=true;
					  }
					 //log.debug("Exiting Primary Policy Check for Contingent Cargo Liability");
			  }
			  else if(!bEPPolCheck && epSpecCntCrgMC!=null)
			  {
					  /*Repeating EP Specific same as that in Primary*/
					  //log.debug("Entering EP Specific Policy Check for Contingent-Cargo Liability");
					  
					  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
					  arlContCargoStatus.removeAll(arlContCargoStatus);
					  
					  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
					   * Cargo Liability */
					  strEPSpcUsed=GlobalVariables.CONTCARGO;
//					swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
					  if(paramDate.equals(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						//log.debug("if policy expiration date is today's date");
						arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlContCargoStatus;
					  }
					  if(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyExpiryDate(),Utility.FORMAT4)))
					  {
						  	//log.debug("if policy expiration date is today's date");
						  	arlContCargoStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlContCargoStatus;
					  }

					  //end----02/03
					  
					  /*Start Check Policy Status (Termination|Cancellation) */
					  ArrayList arr = new ArrayList();
					  arr = getTmpTermDt(epSpecCntCrgMC.getPolicyMstId());
					  Date tmpTerm = null;
					  Date tmpRein = null;
					  Date curTerm = null;
					  Date curReins = null;
					  if(!arr.isEmpty())
					  {
						  tmpTerm = (Date)arr.get(0);
						  tmpRein = (Date)arr.get(1);
					  }							  
					  //=========By Piyush 14Mar'09===============================================================
					  if(epSpecCntCrgMC.getPolicyTerminatedDate().length()>0)
					  {
						  curTerm = Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4);
						  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
						  {
							  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
			  		  else if(epSpecCntCrgMC.getPolicyReinstatedDate().length()>0)
					  {
			  			  curReins = Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4);
						  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
						  else if(curReins.after(paramDate))
						  {
							  if(tmpTerm!=null && tmpTerm.equals(paramDate))
							  {
								  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
					  }						  
					  //=========End By Piyush 14Mar===============================================================					  

					   /*Start Check Policy Status (Termination|Cancellation) */
					   /*if(epSpecCntCrgMC.getPolicyTerminatedDate().length()>0)
					   {
						  //log.debug("EP Specific Policy Terminated Date= "+epSpecCntCrgMC.getPolicyTerminatedDate());
						  if(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
						  {
						      //log.debug("Policy Terminated on parameter date ");
						      arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					   } else if(epSpecCntCrgMC.getPolicyReinstatedDate().length()>0)
						  {
							  log.debug("Primary Policy Reinstated Date:- "+epSpecCntCrgMC.getPolicyReinstatedDate());
							  if(Utility.stringToSqlDate(epSpecCntCrgMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								  log.debug("Policy Reinstated but have future reinstatement date");
								  arlContCargoStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }*/
					  
					  /*Start Self Insured check*/
					  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
						 {
							 /*MC has SI and EP doesn't allow Self Insured*/
							 //log.debug("MC has SI and EP doesn't allow Self Insured");
							 /*Check if EP has given overrides*/
							 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
							 {
								//log.debug("EP has Override for Self Insured");
								strOvrUsed=GlobalVariables.YES;
							 }
							 else
							 {
								 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
								 arlContCargoStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
							 }
						 }
						 else if(GlobalVariables.YES.equals(epSpecCntCrgMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
						 {
							 //log.debug("Exiting Cont Cargo Check (after Self Insured Check)");
							 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
							 return arlContCargoStatus;
						 }
					   /*End Self Insured check*/
						
					
					   
					   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					   
					   if(GlobalVariables.YES.equals(epSpecCntCrgMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
						  {
							  /*MC has RRG and EP doesn't allow RRG*/
							  //log.debug("MC has RRG and EP doesn't allow RRG");
							  /*Check if EP has given overrides*/
							  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
							  {
								  //log.debug("EP has Override for RRG");
								  strOvrUsed=GlobalVariables.YES;
							  }
							  else
							  {
								  //log.debug("EP doesn't allow RRG So RRG Problem");
								  arlContCargoStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
							  }
						  }
					   
					   /*Start Additional Insured check
					   * Ep requires Additional Insured and policy doesn't have Additional Insured 
					   * (check blanket and ep blanket ok or Policy endorsed for this EP
					   * */
					   /**----------------------------------------------------------**/
					   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					   {
					 	  //log.debug("Start Additional Insured check for UIIEP");
					 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		/*check if MC has Blanket or Additional Insured Flag is Yes*/
						 	  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecCntCrgMC.getAddlnInsured()))
						 	  {
						 		  //log.debug("Addtional Insured OK");
						 	  }
						 	  else
						 	  {
						 		  //log.debug("Addtional Insured problem for UIIAEP");
						 		 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 	  } 
					 	  }
					   }
					   else
					   {
					 	  //log.debug("Start Additional Insured check for other EPs");
					 	  /*Check if ep requires additional Insured*/
					 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
					 	  {
					 		  //log.debug("EP requires Addtional Insured");
					 		  /*Check if MC has blanket*/
					 		  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getBlanketReqd()))
					 		  {
					 			  //log.debug("MC has Blanket");
					 			  /*check if EP allows Blanket*/
					 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
					 			  {
					 				  //log.debug("EP allows Blanket..Addtional Insured OK");
					 			  }
					 			  else
					 			  {
					 				  //log.debug("EP doesn't allow Blanket");
					 				  //log.debug("Addtional Insured problem");
					 				 arlContCargoStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
					 			  }
					 		  }
					 		  else
					 		  {
					 			  //log.debug("MC doesn't have Blanket");
					 			  /*Check if EP has been selected as Addtioanl Insured*/
					 			  if(GlobalVariables.YES.equals(epSpecCntCrgMC.getAddlnInsured()))
					 			  {
					 				  //log.debug("EP selected as Additinal Insured(so OK) ");
					 				 
					 			  }
					 			  else
					 			  {
									  /*Check if EP has given overrides*/
							 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
							 		  {
							 			 //log.debug("EP has given overrides for Additional Insured");
							 		  }
							 		  /*End overrides code*/ 
							 		  else
							 		  {
							 			 arlContCargoStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							 		  }									  									  					 				 
					 			  }
					 		  }
					 	  }
					   }
					   /**----------------------------------------------------------**/
					   
	
				   
					   /*Calculating Limits and Deductibles along with currency conversion*/
					   dMCHasCCLimit=Utility.commaStringtoDouble(epSpecCntCrgMC.getLimit());
					  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
					  dMCHasDeduct=Utility.commaStringtoDouble(epSpecCntCrgMC.getDeductible());
					  if(excessCntCrgMC!=null)
					  {
						  dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit());
						  dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible());
					  }
					  	
					  if(epSpecCntCrgMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(excessCntCrgMC!=null)
							{
							  	dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*cndUSD ;
							  	dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*cndUSD ;
							}
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(epSpecCntCrgMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasCCLimit=dMCHasCCLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(excessCntCrgMC!=null)
						  {
							  dExCCLimit=Utility.commaStringtoDouble(excessCntCrgMC.getLimit())*mexUSD ;
							  dExCCDed=Utility.commaStringtoDouble(excessCntCrgMC.getDeductible())*mexUSD ;
						  }
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }  
					  //log.debug("After Currency conversion dMCHasGenLimit "+dMCHasCCLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  //log.debug("After Currency conversion dExGenLimit "+dExCCLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				   
					  /*check for excess policy and if it exists to add to MCLimit*/
					  if(excessCntCrgMC!=null )
					  {
						  //log.debug("Excess Policy Terminated Date:- "+excessCntCrgMC.getPolicyTerminatedDate());
						  /*check if policy terminated date is empty or less than param date*/
						  if((excessCntCrgMC.getPolicyTerminatedDate().length()==0) ||paramDate.after(Utility.stringToSqlDate(excessCntCrgMC.getPolicyTerminatedDate(),Utility.FORMAT4))) 
						  {
							  //log.debug("Excess policy termination date less than parameter date ");
							  dMCHasCCLimit=dMCHasCCLimit+dExCCLimit;
							  //dMCHasDeduct=dMCHasDeduct+dExCCDed;
						  }
					  }// end of if excessAutoMC
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getContCargoReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasCCLimit=dMCHasCCLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasCCLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //dMCHasCCLimit=dMCHasCCLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasCCLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //if(dMCHasCCLimit<dEPLimit)
					  if((dMCHasCCLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasCCLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)//Commented by piyush 28March07
					  //if(dEPDeduct>=0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					   
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg||bDedNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasCCLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
						  if(bLimitNotOkFlg)
						  {
							  arlContCargoStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
						  }
						  if(bDedNotOkFlg)
						  {
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  if(dMCDedBooster>dEPDeduct)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
										  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
									  arlContCargoStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
				  //log.debug("Exiting EP Specific Policy Check for Contingent-Cargo Liability");
			  } //end of EP Specific Policy Check
			  else
			  {
				  arlContCargoStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlContCargoStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Contingent Cargo Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkContCargoLiability()of UValidPolicyCheck class:- "+arlContCargoStatus);
		return arlContCargoStatus;
	}
	
	public ArrayList checkRefTrailerLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{

		/* Here ovrUsedflag passed in the function will be N by default..
		 * If for any of the check override is used , then this flag will be set yes 
		 * */
		//log.info("Business: Entering method checkRefTrailerLiability()of UValidPolicyCheck class");
		//log.info("mcInsDtls "+mcInsDtls);
		//log.info("EPInsOvrWrapper "+epDtls);
		//log.info("strOvrUsed "+strOvrUsed);
		//log.info("strEPSpcUsed "+strEPSpcUsed);
		//log.info("paramDate "+paramDate);
		//log.info("Canadian conversion "+cndUSD);
		//log.info("Mexican conversion "+mexUSD);
		ArrayList arlRefTrailerStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbRefTrlPrimary= new StringBuffer(GlobalVariables.REFTRAILER);
			  sbRefTrlPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbRefTrlExcess= new StringBuffer(GlobalVariables.REFTRAILER);
			  sbRefTrlExcess.append(GlobalVariables.EXCESSPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.REFTRAILER);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  RefTrailerBean primRefTrlMC=(RefTrailerBean)mcInsDtls.get(sbRefTrlPrimary.toString());
			  RefTrailerBean excessRefTrlMC=(RefTrailerBean)mcInsDtls.get(sbRefTrlExcess.toString());
			  RefTrailerBean epSpecRefTrlMC=null;
				  //(RefTrailerBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  epSpecRefTrlMC=(RefTrailerBean)arCg.get(x);
					  if(epSpecRefTrlMC.getPolicyMstId()==0)
					  {
						  //epSpecRefTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecRefTrlMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecRefTrlMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecRefTrlMC=null;
					  }
				  }
			  }
			  UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  /*
			  String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No
			  String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  */
			  double dMCHasTILimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dTempMCLimit=0.0;
			  double dExTILimit=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			  
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.REFTRAILER.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlRefTrailerStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				 // arlRefTrailerStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlRefTrailerStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlRefTrailerStatus;
			  }
			  //======================End added by Piyush ==========================
			  
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires Ref Trailer.. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbRefTrlPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlRefTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlRefTrailerStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlRefTrailerStatus;
			  }

			  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			
			  if(primRefTrlMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for General Liability");
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(paramDate.equals(Utility.stringToSqlDate(primRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlRefTrailerStatus;
						  }
						  if(Utility.stringToSqlDate(primRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlRefTrailerStatus;
						  }
						  //end----02/03
				       
				       /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primRefTrlMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primRefTrlMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primRefTrlMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primRefTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
					   /*Start Self Insured check*/
					  if(GlobalVariables.YES.equals(primRefTrlMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 /*MC has SI and EP doesn't allow Self Insured*/
				  		 //log.debug("MC has SI and EP doesn't allow Self Insured");
				  		 /*Check if EP has given overrides*/
				  		 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
				  		 {
				  			//log.debug("EP has Override for Self Insured");
				  			strOvrUsed=GlobalVariables.YES;
				  		 }
				  		 else
				  		 {
				  			 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
				  			arlRefTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
				  		 }
				  	 }
				  	 else if(GlobalVariables.YES.equals(primRefTrlMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
				  	 {
				  		 //log.debug("Exiting Ref Trailer Check (after Self Insured Check)");
				  		 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
				  		 return arlRefTrailerStatus;
				  	 }
					 /*End Self Insured check*/
					   
					 
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  
					  if(GlobalVariables.YES.equals(primRefTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
					   
					  /*Start Additional Insured check
					  * Ep requires Additional Insured and policy doesn't have Additional Insured 
					  * (check blanket and ep blanket ok or Policy endorsed for this EP
					  * */
					  /**----------------------------------------------------------**/
					  if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
					  {
						  //log.debug("Start Additional Insured check for UIIEP");
						  /*check if MC has Blanket or Additional Insured Flag is Yes*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  if(GlobalVariables.YES.equals(primRefTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(primRefTrlMC.getAddlnInsured()))
							  {
								  //log.debug("Addtional Insured OK");
							  }
							  else
							  {
								  //log.debug("Addtional Insured problem for UIIAEP");
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
							  }  
						  }
					  }
					  else
					  {
						  //log.debug("Start Additional Insured check for other EPs");
						  /*Check if ep requires additional Insured*/
						  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
						  {
							  //log.debug("EP requires Addtional Insured");
							  /*Check if MC has blanket*/
							  if(GlobalVariables.YES.equals(primRefTrlMC.getBlanketReqd()))
							  {
								  //log.debug("MC has Blanket");
								  /*check if EP allows Blanket*/
								  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
								  {
									  //log.debug("EP allows Blanket..Addtional Insured OK");
								  }
								  else
								  {
									  //log.debug("EP doesn't allow Blanket");
									  //log.debug("Addtional Insured problem");
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
								  }
							  }
							  else
							  {
								  //log.debug("MC doesn't have Blanket");
								  /*Check if EP has been selected as Addtioanl Insured*/
								  if(GlobalVariables.YES.equals(primRefTrlMC.getAddlnInsured()))
								  {
									  //log.debug("EP selected as Additinal Insured(so OK) ");
									 
								  }
								  else
								  {
									  //log.debug("Addtional Insured problem");
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
								  }
							  }
						  }
					  }
					  /*End Additional Insured check*/
					  /**----------------------------------------------------------**/
					  
					  
					  /*Checking Limits and Deductibles if ACV Not present*/
					  if(primRefTrlMC.getAcv().equals(GlobalVariables.NO) || primRefTrlMC.getAcv().length()==0)
					  {
						  //log.debug("ACV Not Present hence checking  Limits and Deductibles");
						  /*Calculating Limits and Deductibles along with currency conversion*/
						  dMCHasTILimit=Utility.commaStringtoDouble(primRefTrlMC.getLimit());
						  //+Utility.commaStringtoDouble(primGenMC)+Utility.commaStringtoDouble(primGenMC);
						  dMCHasDeduct=Utility.commaStringtoDouble(primRefTrlMC.getDeductible());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
						  if(primRefTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(primRefTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							  /*if(excessTrlMC!=null)
								  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  //log.debug("After Currency conversion dMCHasTILimit "+dMCHasTILimit);
						  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
						  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
						  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						  //if(excessRefTrlMC!=null )
						  {
							 // //log.debug("Excess Policy Terminated Date:- "+excessRefTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							 /* if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getRefTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						  
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/
						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
						  }// end if limit
						  
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)//Commented by piyush 28March07
						  //if(dEPDeduct>=0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible  
						  }
						  
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }//end check multiple limits and deductibles
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
						  if(bLimitNotOkFlg)
						  {
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
						  }
						  if(bDedNotOkFlg)
						  {
							  
							  if(dMCDedBooster!=0)
							  {
								  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
								  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27March07
								  if(dMCHasDeduct>dMCDedBooster)
								  {
									  //log.debug("Ded problem after replacing Booster");
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
							  else
							  {
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
					  }
					  else
					  {
						  //log.debug("ACV Present hence skipping Limits and Deductibles Check");
					  }
					  
					  if(arlRefTrailerStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }
				  //log.debug("Exiting Primary Policy Check for Trailer Liability");  
			  }// end of primary check
			  /*check if primary check failed,then to proceed with EP Specific
			   * arlGLStatus is not empty  as it has problems*/
			  else if(!bEPPolCheck && epSpecRefTrlMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for Trailer Liability");
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  arlRefTrailerStatus.removeAll(arlRefTrailerStatus);
				  
				  /*Repeating EP Specific same as that in Primary*/
				
				  /*Setting EP Specific flag to state that the check was done using EP Specific Policy
				   * Trailer Liability */
				  strEPSpcUsed=GlobalVariables.REFTRAILER;
				 
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(paramDate.equals(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlRefTrailerStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlRefTrailerStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlRefTrailerStatus;
				  }
				  //end----02/03
				  
			       /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecRefTrlMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecRefTrlMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecRefTrlMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /* if(epSpecRefTrlMC.getPolicyTerminatedDate().length()>0)
				   {
					  //log.debug("EP Specific Policy Terminated Date= "+epSpecRefTrlMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug(" Policy Terminated on parameter date ");
					      arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				   } else if(epSpecRefTrlMC.getPolicyReinstatedDate().length()>0)
					  {
						  log.debug("Primary Policy Reinstated Date:- "+epSpecRefTrlMC.getPolicyReinstatedDate());
						  if(Utility.stringToSqlDate(epSpecRefTrlMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							  log.debug("Policy Reinstated but have future reinstatement date");
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }*/
				  
				  /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(epSpecRefTrlMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlRefTrailerStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecRefTrlMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting Ref Trailer Liability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlRefTrailerStatus;
					 }
				   /*End Self Insured check*/
				   
				  
				   
				   
				   /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				   if(GlobalVariables.YES.equals(epSpecRefTrlMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlRefTrailerStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				   /*Start Additional Insured check
				   * Ep requires Additional Insured and policy doesn't have Additional Insured 
				   * (check blanket and ep blanket ok or Policy endorsed for this EP
				   * */
				   
				   /**----------------------------------------------------------**/
				   if(GlobalVariables.UIIA_EP.equals(epDtls.getEpAcctNo()))
				   {
				 	  //log.debug("Start Additional Insured check for UIIEP");
				 	  /*check if MC has Blanket or Additional Insured Flag is Yes*/
				 	 if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		if(GlobalVariables.YES.equals(epSpecRefTrlMC.getBlanketReqd()) || GlobalVariables.YES.equals(epSpecRefTrlMC.getAddlnInsured()))
					 	  {
					 		  //log.debug("Addtional Insured OK");
					 	  }
					 	  else
					 	  {
					 		  //log.debug("Addtional Insured problem for UIIAEP");
					 		 arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
					 	  } 
				 	  }
				   }
				   else
				   {
				 	  //log.debug("Start Additional Insured check for other EPs");
				 	  /*Check if ep requires additional Insured*/
				 	  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getAddInsReq()))
				 	  {
				 		  //log.debug("EP requires Addtional Insured");
				 		  /*Check if MC has blanket*/
				 		  if(GlobalVariables.YES.equals(epSpecRefTrlMC.getBlanketReqd()))
				 		  {
				 			  //log.debug("MC has Blanket");
				 			  /*check if EP allows Blanket*/
				 			  if(GlobalVariables.YES.equals(epDtls.getEpSwitches().getBlanketAllwd()))
				 			  {
				 				  //log.debug("EP allows Blanket..Addtional Insured OK");
				 			  }
				 			  else
				 			  {
				 				  //log.debug("EP doesn't allow Blanket");
				 				  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlRefTrailerStatus.add(GlobalVariables.UVLD_BLNKT_PRBLM);
						 		  }									  									  					 				 				 				 
				 			  }
				 		  }
				 		  else
				 		  {
				 			  //log.debug("MC doesn't have Blanket");
				 			  /*Check if EP has been selected as Addtioanl Insured*/
				 			  if(GlobalVariables.YES.equals(epSpecRefTrlMC.getAddlnInsured()))
				 			  {
				 				  //log.debug("EP selected as Additinal Insured(so OK) ");
				 				 
				 			  }
				 			  else
				 			  {
				 				  //log.debug("Addtional Insured problem");
								  /*Check if EP has given overrides*/
						 		  if(GlobalVariables.NO.equals(epDtls.getEpOvrMCBean().getAddInsReq()))
						 		  {
						 			 //log.debug("EP has given overrides for Additional Insured");
						 		  }
						 		  /*End overrides code*/ 
						 		  else
						 		  {
						 			 arlRefTrailerStatus.add(GlobalVariables.UVLD_ADDLN_INSRD_PRBLM);
						 		  }									  									  					 				 				 				 
				 			  }
				 		  }
				 	  }
				   }
				   /**----------------------------------------------------------**/
				   
				   
				   if(epSpecRefTrlMC.getAcv().equals(GlobalVariables.NO)|| epSpecRefTrlMC.getAcv().length()==0)
				   {
					   //log.debug("ACV Not Present hence checking Limits and Deductibles");
					   /*Calculating Limits and Deductibles along with currency conversion*/
					      dMCHasTILimit=Utility.commaStringtoDouble(epSpecRefTrlMC.getLimit());
						  dMCHasDeduct=Utility.commaStringtoDouble(epSpecRefTrlMC.getDeductible());
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  
						  if(epSpecRefTrlMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
						  {
							  //log.debug("Canadian Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*cndUSD;
							  dMCHasDeduct=dMCHasDeduct*cndUSD;
							  /*if(excessRefTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessRefTrlMC.getLimit())*cndUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
						  }
						  else if(epSpecRefTrlMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
						  {
							  //log.debug("Mexican Currency Conversion");
							  dMCHasTILimit=dMCHasTILimit*mexUSD;
							  dMCHasDeduct=dMCHasDeduct*mexUSD;
							 /* if(excessRefTrlMC!=null)
								  dExTILimit=Utility.commaStringtoDouble(excessRefTrlMC.getLimit())*mexUSD ;*/
							  if(umbGenMC!=null)
								  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
						  }
						  
						  /*check for excess policy and if it exists to add to MCLimit*/
						 // if(excessTrlMC!=null )
						  {
							  ////log.debug("Excess Policy Terminated Date:- "+excessTrlMC.getPolicyTerminatedDate());
							  /*check if policy terminated date is empty or less than param date*/
							  /*if((excessTrlMC.getPolicyTerminatedDate().length()==0) ||(excessTrlMC.getPolicyTerminatedDate().length()>0 && Utility.stringToSqlDate(excessTrlMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate) )) 
							  {
								  //log.debug("Excess policy termination date less than parameter date ");
								  dMCHasTILimit=dMCHasTILimit+dExTILimit;
							  }*/
						  }// end of if excessAutoMC
						  
						  /*check for umbrella poilcy exist and if exista add to MC Limit*/
						  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getRefTrailerReqd()))
						  {
							  /*check if umbrella policy less than or equal to param date*/
							  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								 	  //log.debug("Umbrella policy termination date after parameter date ");
								 	 dMCHasTILimit=dMCHasTILimit+dMCUmbrella;
							  }
							  
						  }
						
						  /*Before adding Booster check for limits so that override flag can be set */
						  /*Checking for Override Used*/
						  if((dMCHasTILimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
						  {
							  /* checking limits or deductibles without booster. If it fails then to set Override 
							   * used flag as true*/
							  //log.debug("Limits |Deductibles override used");
							  bOvrUsed=true;
						  }
						  /******************************************************************************/
						  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
						   * */
						  //dMCHasTILimit=dMCHasTILimit+dMCLimBooster; //adding booster to Limits
						  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
						  /*******************************************************************************/

						  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
						   * Deductibles*/
						  //log.info("MC Limit:- "+dMCHasTILimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
						  //if(dMCHasTILimit<dEPLimit)
						  if((dMCHasTILimit<dEPLimit) && (dMCLimBooster==0))
						  {//Added as per new waiver specs
							  bLimitNotOkFlg=true;
						  }
						  else if(dMCHasTILimit<dMCLimBooster)
						  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
						  }// end if limit
						  /*Deductible check*/
						  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
						  if(dEPDeduct>0)
						  {
							  if(dMCHasDeduct>dEPDeduct)
							  {
								  //log.debug("Deductible Not Ok before multiple Pol Limits check");
								  bDedNotOkFlg=true;
							  }// end if deductible  
						  }
						  
						  /*Check if multiple limits/deductible exists for this policy */
						  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
						  {
							  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
							  /*will loop through Multiple limits and deductibles bean to find appropriate
							   * limits and deductible to be used for the MC.
							   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
							   * */
							  if(bLimitNotOkFlg)
							  {
								  //log.debug("Checking Policy Multiple Limits and Deductibles ");
								  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
								  {
									  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
									  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
									  if((dMCHasTILimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
									  {
										  /*found Multiple Limits and deductibles so using that limit and deductibles
										   * to check MC Limits and Deductibles; 
										   * */
										  bLimitNotOkFlg=false;
										  bDedNotOkFlg=false;
										  //break;
									  }
								  }// end of for loop
							  }//end if bLimitOkFlg
						  }
						  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
						   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
						   * not found for Multiple Limits and Deductibles, so setting appropriate message
						   * */
							  if(bLimitNotOkFlg)
							  {
								  arlRefTrailerStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
							  }
							  if(bDedNotOkFlg)
							  {
								  if(dMCDedBooster!=0)
								  {
									  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
									  if(dMCDedBooster>dEPDeduct)
									  {
										  //log.debug("Ded problem after replacing Booster");
										  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
										  else	
										  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
									  }
								  }
								  else
								  {
									  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
									  else	
									  arlRefTrailerStatus.add(GlobalVariables.UVLD_DED_PRBLM);
								  }
							  }
				   }//end of ACV check
				   else
				   {
					   //log.debug("ACV Present hence skipping Limits and Deductibles Check");
				   }
				  //log.debug("Exiting EP Specific Policy Check for Ref Trailer Liability");
			  }
			  else
			  {
				  arlRefTrailerStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlRefTrailerStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Refrigerated Trailer Liability Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkRefTrailerLiability()of UValidPolicyCheck class:- "+arlRefTrailerStatus);
		return arlRefTrailerStatus;
	
	}
	
	public ArrayList checkWCLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,String strELCallFlag,Date paramDate) throws Exception
	{
		ArrayList arlWCStatus= new ArrayList(5);
  	    double dWC_ELA=0.0; 
	    double dWC_ELP=0.0;
		double dWC_ELE=0.0;

		try
		{
			  //boolean bLimitNotOkFlg=false;
			 // boolean bDedNotOkFlg=false;
			  //boolean bOvrUsed=false;
			  
			  StringBuffer sbWCPrimary= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbWCPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  WCBean primWCMC=(WCBean)mcInsDtls.get(sbWCPrimary.toString());
			  WCBean epSpecWCMC=null;
				  //(WCBean)mcInsDtls.get(sbEPSpecPolicy.toString());
			  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
			  {
				  //log.debug("EPSC");
				  ArrayList arCg = new ArrayList();
				  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
				  for(int x=0;x<arCg.size();x++)
				  {
					  //log.debug("EPSC"+x);
					  epSpecWCMC=(WCBean)arCg.get(x);
					  //log.debug("EPSCA"+x);
					  if(epSpecWCMC.getPolicyMstId()==0)
					  {
						  //epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecWCMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //log.debug("EPSCB"+x);
						  //epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecWCMC=null;
					  }
				  }
			  }
			  //log.debug("EPSCC");
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  //UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			  
			  //String  strAICheck=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  //String strAICheckEPSpec=(String)mcInsDtls.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific
			  
			   /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.WORKCOMP.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");				  
				  return arlWCStatus;				
			  }
			  
			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  //log.debug("WC BOOSTER :"+epDtls.getEpOvrMCBean().getLimitBooster());
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlWCStatus;
			  }
			  //======================End added by Piyush ==========================
			  //log.debug("EPSCCD");
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbWCPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlWCStatus.add(GlobalVariables.WCUNEEDUHAVE);
					  arlWCStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlWCStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");				  
				  return arlWCStatus;
			  }
			  			  
			  if(primWCMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for Workers Compensation");
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				          if(primWCMC.getStrUnUh().equals(GlobalVariables.WCUNEEDUHAVE))
						  {
							//log.debug("If dummy policy created for the sack of displaying the problems with WC");
							arlWCStatus.add(GlobalVariables.WCUNEEDUHAVE);
							arlWCStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
							return arlWCStatus;
						  }
				       
						  if(paramDate.equals(Utility.stringToSqlDate(primWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlWCStatus;
						  }
						  if(Utility.stringToSqlDate(primWCMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlWCStatus;
						  }
						  //end----02/03
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primWCMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primWCMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primWCMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primWCMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primWCMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
					  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  
					  if(GlobalVariables.YES.equals(primWCMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlWCStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }
				      
					  /*Check MC Exempt and to update whether to call EL Check Required method or not*/
					  if(primWCMC.getExempt().equals(GlobalVariables.YES)  )
					  {
						  //log.debug("MC has selected Emempt =Yes");
						  if(epDtls!=null && epDtls.getEpNeeds()!=null && epDtls.getEpNeeds().getPolicyReq().equals(GlobalVariables.YES))
						  {
							  /*If MC Selects Exempt in WC and EP has selected WC Required means it doesn't allow exemption*/
							  //log.debug("EP doesn't allow Exemption before Overrides");
							  if(epDtls!=null && epDtls.getEpOvrMCBean()!=null &&  epDtls.getEpOvrMCBean().getPolicyReq().equals(GlobalVariables.YES))
							  {
								  //log.debug("After checking overrides WC Exemption problem");
								  arlWCStatus.add(GlobalVariables.UVLD_WC_EXEMPTION);  
							  }
							  else
							  {
								  //log.debug("EL Check not required so setting strELCallFlag =No after checking override");
								  strELCallFlag=GlobalVariables.NO;
							  }
						  }
						  else 
						  {
							  //log.debug("EL Check not required so setting strELCallFlag =No");
							  strELCallFlag=GlobalVariables.NO;
						  }
					  }
					  else
					  {
						 //log.debug("MC has selected Emempt =No");
						  dWC_ELA=Utility.commaStringtoDouble(primWCMC.getElEachOccur()); //EL Each Occur is EL Accident
						  dWC_ELE=Utility.commaStringtoDouble(primWCMC.getElDisEAEmp());
						  dWC_ELP=Utility.commaStringtoDouble(primWCMC.getElDisPlcyLmt());
						  /*Start Self Insured check*/
						  	if(GlobalVariables.YES.equals(primWCMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
							 {
								 /*MC has SI and EP doesn't allow Self Insured*/
								 //log.debug("MC has SI and EP doesn't allow Self Insured");
								 /*Check if EP has given overrides*/
								 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
								 {
									//log.debug("EP has Override for Self Insured");
									strELCallFlag=GlobalVariables.NO;
								 }
								 else
								 {
									 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
									 arlWCStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
								 }
							 }
							 else if(GlobalVariables.YES.equals(primWCMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
							 {
								 //log.debug("Exiting WC Check (after Self Insured Check)");
								 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
								 return arlWCStatus;
							 }
							 else if(GlobalVariables.NO.equals(primWCMC.getSelfInsured()) && ((dWC_ELA+dWC_ELE+dWC_ELP)>0))
							 {
								   //log.debug("MC is not Self Insured so EL Check Required");
								   strELCallFlag=GlobalVariables.YES;
							 }
						   /*End Self Insured check*/
					  }
					  if(arlWCStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }
					  
				      //log.debug("Exiting Primary Policy Check for Workers Compensation");
				      
			  }
			  else if(!bEPPolCheck && epSpecWCMC!=null )
			  {
				  		//log.debug("Entering EP Specific Policy Check for Workers Compensation");
				  		
				  		/*Making the arrayList of Problems empty before starting EP Specific Check...*/
						arlWCStatus.removeAll(arlWCStatus);
				  		strEPSpcUsed=GlobalVariables.WORKCOMP;
				  		
				  		 //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(paramDate.equals(Utility.stringToSqlDate(epSpecWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlWCStatus;
						  }	
						  if(Utility.stringToSqlDate(epSpecWCMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecWCMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlWCStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlWCStatus;
						  }

						  //end----02/03
						  
						  /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(epSpecWCMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(epSpecWCMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(epSpecWCMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(epSpecWCMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(epSpecWCMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						  
				  		/*if(epSpecWCMC.getPolicyTerminatedDate().length()>0)
						  {
							  //log.debug("Primary Policy Terminated Date= "+epSpecWCMC.getPolicyTerminatedDate());
							  if(Utility.stringToSqlDate(epSpecWCMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
							  {
							      //log.debug("Primary Policy Termination date greater than parameter date ");
							      arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  } else if(epSpecWCMC.getPolicyReinstatedDate().length()>0)
						  {
							  log.debug("Primary Policy Reinstated Date:- "+epSpecWCMC.getPolicyReinstatedDate());
							  if(Utility.stringToSqlDate(epSpecWCMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
							  {
								  log.debug("Policy Reinstated but have future reinstatement date");
								  arlWCStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }*/
						  
						  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  		if(GlobalVariables.YES.equals(epSpecWCMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  		{
				  		  /*MC has RRG and EP doesn't allow RRG*/
				  		  //log.debug("MC has RRG and EP doesn't allow RRG");
				  		  /*Check if EP has given overrides*/
				  		  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
				  		  {
				  			  //log.debug("EP has Override for RRG");
				  			  strOvrUsed=GlobalVariables.YES;
				  		  }
				  		  else
				  		  {
				  			  //log.debug("EP doesn't allow RRG So RRG Problem");
				  			  arlWCStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
				  		  }
				  		}
				  		
						  /*Check MC Exempt and to update whether to call EL Check Required method or not*/
						  if(epSpecWCMC.getExempt().equals(GlobalVariables.YES)  )
						  {
							  //log.debug("MC has selected Emempt =Yes");
							  if(epDtls!=null && epDtls.getEpNeeds()!=null && epDtls.getEpNeeds().getPolicyReq().equals(GlobalVariables.YES))
							  {
								  /*If MC Selects Exempt in WC and EP has selected WC Required means it doesn't allow exemption*/
								  //log.debug("EP doesn't allow Exemption before Overrides");
								  if(epDtls!=null && epDtls.getEpOvrMCBean()!=null &&  epDtls.getEpOvrMCBean().getPolicyReq().equals(GlobalVariables.YES))
								  {
									  //log.debug("After checking overrides WC Exemption problem");
									  arlWCStatus.add(GlobalVariables.UVLD_WC_EXEMPTION);  
								  }
								  else
								  {
									  //log.debug("EL Check not required so setting strELCallFlag =No after checking override");
									  strELCallFlag=GlobalVariables.NO;
								  }
							  }
							  else 
							  {
								  //log.debug("EL Check not required so setting strELCallFlag =No");
								  strELCallFlag=GlobalVariables.NO;
							  }
						  }
						  else
						  {
							  //log.debug("MC has selected Emempt =No");
							  /*Start Self Insured check*/
							  if(GlobalVariables.YES.equals(epSpecWCMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
								 {
									 /*MC has SI and EP doesn't allow Self Insured*/
									 //log.debug("MC has SI and EP doesn't allow Self Insured");
									 /*Check if EP has given overrides*/
									 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
									 {
										//log.debug("EP has Override for Self Insured");
										strELCallFlag=GlobalVariables.NO;
										strOvrUsed=GlobalVariables.YES;
									 }
									 else
									 {
										 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
										 arlWCStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
									 }
								 }
								 else if(GlobalVariables.YES.equals(epSpecWCMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
								 {
									 //log.debug("Exiting WC Check (after Self Insured Check)");
									 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
									 return arlWCStatus;
								 }
								 else if(GlobalVariables.NO.equals(epSpecWCMC.getSelfInsured()))
								 {
									   //log.debug("MC is not Self Insured so EL Check Required");
									   strELCallFlag=GlobalVariables.YES;
								 }
							   /*End Self Insured check*/
						  }
				  		//log.debug("Exiting EP Specific Policy Check for Workers Compensation");
			  }
			  else
			  {
				  arlWCStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlWCStatus;
			  }
			  //log.debug("epDtls before strELCallFlag:- "+epDtls.getElFlag());
			  if(GlobalVariables.YES.equals(strELCallFlag))
			  {
				  //log.debug("Setting YES");
				  epDtls.setElFlag(GlobalVariables.YES);
			  }
			  else if(GlobalVariables.NO.equals(strELCallFlag))
			  {
				  //log.debug("Setting NO");
				  epDtls.setElFlag(GlobalVariables.NO);
			  }
			  //log.debug("epDtls after strELCallFlag:- "+epDtls.getElFlag());
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in WC Check for UValid:- ", exp);
			
		}
		log.info("Business: Exiting method checkWCLiability()of UValidPolicyCheck class:- "+arlWCStatus);
		//log.info("ELCallCheckRequired Flag:- "+strELCallFlag);
		return arlWCStatus;
	
	}
	
	public ArrayList checkEmpLiabilityArch(HashMap mcInsDtlsEL,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{
		ArrayList arlELStatus= new ArrayList(5);
		
		try
		{
			
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			 
			  StringBuffer sbWCPrimary= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbWCPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbELPrimary= new StringBuffer(GlobalVariables.EMPLIABILITY);
			  sbELPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbELEPSpecPolicy= new StringBuffer(GlobalVariables.EMPLIABILITY);
			  sbELEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  StringBuffer sbWCEPSpecPolicy= new StringBuffer(GlobalVariables.WORKCOMP);
			  sbWCEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  
			  ELBean primELMC=(ELBean)mcInsDtlsEL.get(sbELPrimary.toString());
			  ELBean epSpecELMC= null;
			  if(mcInsDtlsEL.containsKey(sbELEPSpecPolicy.toString()))
			  {
				  ArrayList arAuto = new ArrayList();
				  arAuto = (ArrayList)mcInsDtlsEL.get(sbELEPSpecPolicy.toString());
				  for(int x=0;x<arAuto.size();x++)
				  {
					  epSpecELMC=(ELBean)arAuto.get(x);
					  if(epSpecELMC.getPolicyMstId()==0)
					  {
						  //epSpecELMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecELMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  //epSpecELMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecELMC=null;
					  }
				  }
			  }
			  WCBean primWCMC=(WCBean)mcInsDtlsEL.get(sbWCPrimary.toString());
			  WCBean epSpecWCMC= null;
		  	  if(mcInsDtlsEL.containsKey(sbWCEPSpecPolicy.toString()))
		  		  epSpecWCMC= (WCBean)((ArrayList)mcInsDtlsEL.get(sbWCEPSpecPolicy.toString())).get(0);
		  	  
			  if(mcInsDtlsEL.containsKey(sbWCEPSpecPolicy.toString()))
			  {
				  ArrayList arAuto = new ArrayList();
				  arAuto = (ArrayList)mcInsDtlsEL.get(sbWCEPSpecPolicy.toString());
				  for(int x=0;x<arAuto.size();x++)
				  {
					  epSpecWCMC=(WCBean)arAuto.get(x);
					  if(epSpecWCMC.getPolicyMstId()==0)
					  {
						  epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else if(uValidDao.chkEPSpc(epSpecWCMC.getPolicyMstId(),epDtls.getEpAcctNo()))
					  {
						  epSpecWCMC.setAddlnInsured(GlobalVariables.YES);
						  break;
					  }
					  else
					  {
						  epSpecWCMC=null;
					  }
				  }
			  }	  
			  UmbBean umbGenMC=(UmbBean)mcInsDtlsEL.get(GlobalVariables.UMBRELLA);
			  //log.debug("primELMC:- "+primELMC);
			  //log.debug("epSpecELMC:- "+epSpecELMC);
			  /*String  strAICheck=(String)mcInsDtlsEL.get(GlobalVariables.ADDLNINSRD); //Additional Insured Yes or No for Primary
			  String strAICheckEPSpec=(String)mcInsDtlsEL.get(GlobalVariables.ADDLNINSRD_EPSPEC); //Additional Insured Yes or No for EP Specific*/
			  
			  double dMC_Limit_ELA=0.0;
			  double dMC_Limit_ELE=0.0;
			  double dMC_Limit_ELP=0.0;
			  double dEPDeduct=0.0;
			  double dMCHasDeduct=0.0;
			  double dWC_ELA=0.0; 
			  double dWC_ELP=0.0;
			  double dWC_ELE=0.0;
			  double dEL_ELA=0.0;
			  double dEL_ELP=0.0;
			  double dEL_ELE=0.0;
			  double dMCUmbrella=0.0;
			  double dMCDedBooster=0.0;
			  double dEP_Limit_ELA=0.0;
			  double dEP_Limit_ELE=0.0;
			  double dEP_Limit_ELP=0.0;
			  double dMC_ELA_Booster=0.0;
			  double dMC_ELE_Booster=0.0;
			  double dMC_ELP_Booster=0.0;
			  
			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			  if(!GlobalVariables.EMPLIABILITY.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlELStatus;
			  }

			  //Added by piyush
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlELStatus;
			  }
			  //======================End added by Piyush ==========================
			  
			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if((primWCMC!=null && primWCMC.getStrWCELflg().equals(GlobalVariables.YES)) || (epSpecWCMC!=null && epSpecWCMC.getStrWCELflg().equals(GlobalVariables.YES)))
				  {
					  //log.debug("As EL calling from WC so skipping NO POLICY Check");	
				  }
				  else
				  {
					  if(!(mcInsDtlsEL.containsKey(sbELPrimary.toString()) ||mcInsDtlsEL.containsKey(sbELEPSpecPolicy.toString())))
					  {
						  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
						  arlELStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
						  return arlELStatus;
					  }
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlELStatus;
			  }
			  
			  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
			  if(primELMC!=null)
			  {
				  if(paramDate.equals(Utility.stringToSqlDate(primELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");					  
					arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlELStatus;
				  }
				  if(Utility.stringToSqlDate(primELMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlELStatus;
				  }
	
			  }
			  //end----02/03
			  
			  if(epDtls!=null)
			  {
				  dEP_Limit_ELA=Utility.commaStringtoDouble(epDtls.getEpNeeds().getELA());
				  dEP_Limit_ELE=Utility.commaStringtoDouble(epDtls.getEpNeeds().getELE());
				  dEP_Limit_ELP=Utility.commaStringtoDouble(epDtls.getEpNeeds().getELP());
				  dMC_ELA_Booster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getELABooster());
				  dMC_ELE_Booster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getELEBooster());
				  dMC_ELP_Booster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getELPBooster());
				  /*Added to get deductibles*/
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  //log.info("dEP_Limit_ELA :- "+dEP_Limit_ELA+ " dEP_Limit_ELE:- "+dEP_Limit_ELE +" dEP_Limit_ELP:- "+dEP_Limit_ELP );
			  
			  /*Primary & EP Policy Check if only WC is to be checked.....  i.e WC Check Flag will be set....*/
			  if(GlobalVariables.YES.equals(epDtls.getElFlag()))
			  {
				  //log.debug("Entering check for only WC after checking EL Flag in EPDetails Bean........");
				  /*For WC*/
				  if(primWCMC!=null)
				  {
					  //log.debug("WC Primary check ");
					  
					  
					  dWC_ELA=Utility.commaStringtoDouble(primWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(primWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(primWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(primWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(primWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						 
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;

						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
					  dMC_Limit_ELA=dWC_ELA +dEL_ELA;
					  dMC_Limit_ELE=dWC_ELE +dEL_ELE;
					  dMC_Limit_ELP=dWC_ELP +dEL_ELP;
					  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
					  
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  log.debug("Setting Override used flag");
						  bOvrUsed=true;
					  }
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
					  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
					  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/					  
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))					  
					  //if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  if(bOvrUsed && (dMC_ELA_Booster==0 || dMC_ELE_Booster==0 || dMC_ELP_Booster==0))						  
					  {
						  bLimitNotOkFlg=true;
					  }
					  else if(dMC_Limit_ELA<dMC_ELA_Booster || dMC_Limit_ELE <dMC_ELE_Booster || dMC_Limit_ELP <dMC_ELP_Booster)
					  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
					  }// end if limit

					  if(bLimitNotOkFlg)
					  {
						  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  if(dMCDedBooster>dEPDeduct)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  /*if(arlELStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }*/
					  //log.debug("Returning from only WC... primary");
					  //return arlELStatus;
				  }
				  else if(epSpecWCMC!=null)
				  {
					  //log.debug("WC EP Specific check ");
					  dWC_ELA=Utility.commaStringtoDouble(epSpecWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(epSpecWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(epSpecWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						 
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
					  dMC_Limit_ELA=dWC_ELA +dEL_ELA;
					  dMC_Limit_ELE=dWC_ELE +dEL_ELE;
					  dMC_Limit_ELP=dWC_ELP +dEL_ELP;
					  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
					  
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  //if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Setting Override used flag");
						  bOvrUsed=true;
					  }
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
					  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
					  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
					  
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
					  if(bOvrUsed && (dMC_ELA_Booster==0 || dMC_ELE_Booster==0 || dMC_ELP_Booster==0))						  
					  {
						  bLimitNotOkFlg=true;
					  }
					  else if(dMC_Limit_ELA<dMC_ELA_Booster || dMC_Limit_ELE <dMC_ELE_Booster || dMC_Limit_ELP <dMC_ELP_Booster)
					  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
					  }// end if limit
					  if(bLimitNotOkFlg)
					  {
						  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  //if(dMCDedBooster>dEPDeduct)//Changed by piyush 27March07
							  if(dMCHasDeduct>dMCDedBooster)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					 /* if(arlELStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }*/
					  //log.debug("Returning from only WC... EP Specific");
					  return arlELStatus;
				  }
				  //log.debug("bEPPolCheck:- "+ bEPPolCheck +"epSpecELMC:- "+epSpecELMC);
				  //log.debug("Exiting check for only WC........");
			  }
			  /*For Primary Policy check... For EL..*/
			  if(primELMC!=null)
			  {
				  //log.debug("Entering Primary Policy Check for Employers Liability");
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(primELMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(primELMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(primELMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(primELMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(primELMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  
				  
				  /*Exempt Check*/
				  if(primWCMC!=null)
				  {
					  if(GlobalVariables.YES.equals(primWCMC.getExempt()) || GlobalVariables.YES.equals(primWCMC.getUnlmtdElLimits()))
					  {
						  //log.debug("WC Exempt is Yes,EL Ok so skipping EL Check");
						  return arlELStatus;
					  }  
				  }
				  
				  
				  
				   /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(primELMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							/*Check if EP doesn't allow SI*/
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlELStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(primELMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting WC Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlELStatus;
					 }
				   /*End Self Insured check*/
				  
				  
				  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  if(GlobalVariables.YES.equals(primELMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  {
					  /*MC has RRG and EP doesn't allow RRG*/
					  //log.debug("MC has RRG and EP doesn't allow RRG");
					  /*Check if EP has given overrides*/
					  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
					  {
						  //log.debug("EP has Override for RRG");
						  strOvrUsed=GlobalVariables.YES;
					  }
					  else
					  {
						  //log.debug("EP doesn't allow RRG So RRG Problem");
						  arlELStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
					  }
				  }

				  
				  /*Getting Limits and Deductibles for WC & EL*/
				  /*For EL*/
				  dEL_ELA=Utility.commaStringtoDouble(primELMC.getElEachOccur()); //EL Each Occur is EL Accident
				  dEL_ELE=Utility.commaStringtoDouble(primELMC.getElDisEAEmp());
				  dEL_ELP=Utility.commaStringtoDouble(primELMC.getElDisPlcyLmt());
				  if(umbGenMC!=null)
					  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
				  dMCHasDeduct=Utility.commaStringtoDouble(primELMC.getDeductible());
				  /*Currency conversion for EL..*/
				  if(primELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  dEL_ELA=dEL_ELA*cndUSD;
					  dEL_ELE=dEL_ELE*cndUSD;
					  dEL_ELP=dEL_ELP*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
				  }
				  else if(primELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  dEL_ELA=dEL_ELA*mexUSD;
					  dEL_ELE=dEL_ELE*mexUSD;
					  dEL_ELP=dEL_ELP*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
				  }
				  /*For WC*/
				  if(primWCMC!=null)
				  {
					  //log.debug("After checking null condn for primWCMC for Getting ELA ELE ELP for WC ");
					  dWC_ELA=Utility.commaStringtoDouble(primWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(primWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(primWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(primWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(primWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						 
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
				  }
				  //log.debug("After Currency conversion dEL_ELA "+ dEL_ELA+" dEL_ELE "+ dEL_ELE + "dEL_ELP "+dEL_ELP);
				  //log.debug("After Currency conversion dWC_ELA "+ dWC_ELA+" dWC_ELE "+ dWC_ELE + "dWC_ELP "+dWC_ELP);
				  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				 /* if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getELReqd()))
				  {
					  if(umbGenMC.getPolicyTerminatedDate().length()>0)
					  {
						  if(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate)))
						  {
							  //log.debug("Umbrella policy termination date less than equal to  parameter date ");
							  
						  }
					  }
				  }*/
				  dMC_Limit_ELA=dWC_ELA+dEL_ELA;
				  dMC_Limit_ELE=dWC_ELE+dEL_ELE;
				  dMC_Limit_ELP=dWC_ELP+dEL_ELP;
				  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
				  
				  
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
				  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Setting Override used flag");
					  bOvrUsed=true;
				  }
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)//Commented by piyush 28March07
				  //if(dEPDeduct>=0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }				  
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
				  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
				  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
				  
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
				  //log.info("dMC_ELA_Booster:- "+dMC_ELA_Booster+"  dMC_ELE_Booster:- "+dMC_ELE_Booster +" dMC_ELP_Booster:- "+dMC_ELP_Booster);
				  if(bOvrUsed && (dMC_ELA_Booster==0 || dMC_ELE_Booster==0 || dMC_ELP_Booster==0))						  
				  {
					  bLimitNotOkFlg=true;
				  }
				  else if(dMC_Limit_ELA<dMC_ELA_Booster || dMC_Limit_ELE <dMC_ELE_Booster || dMC_Limit_ELP <dMC_ELP_Booster)
						  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {	
							  //log.debug("Limit Not Ok before multiple Pol Limits check");
							  bLimitNotOkFlg=true;
				  }// end if limit
				  
				  if(bLimitNotOkFlg)
				  {
					  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(dMCDedBooster!=0)
					  {
						  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
						  if(dMCDedBooster>dEPDeduct)
						  {
							  //log.debug("Ded problem after replacing Booster");
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  else
					  {
						  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
						  else	
						  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
					  }
				  }
				  if(arlELStatus.size()==0)
				  {
				    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
				    bEPPolCheck=true;
				  }
				  //log.debug("Exiting Primary Policy Check for Employers Liability");
			  }
			  if(!bEPPolCheck && epSpecELMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for Employers Liability");
				  
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  arlELStatus.removeAll(arlELStatus); 
				  //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(paramDate.equals(Utility.stringToSqlDate(epSpecELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlELStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecELMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecELMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlELStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlELStatus;
				  }
				  //end----02/03
				
				  /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecELMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecELMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecELMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecELMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecELMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  

				  /*Start Check Policy Status (Termination|Cancellation) */
				  /*if(epSpecELMC.getPolicyTerminatedDate().length()>0)
				  {
					  //log.debug("Primary Policy Terminated Date= "+epSpecELMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecELMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecELMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Primary Policy Terminated on parameter date ");
					      arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  } else if(epSpecELMC.getPolicyReinstatedDate().length()>0)
				  {
					  log.debug("Primary Policy Reinstated Date:- "+epSpecELMC.getPolicyReinstatedDate());
					  if(Utility.stringToSqlDate(epSpecELMC.getPolicyReinstatedDate(),Utility.FORMAT4).after(paramDate))
					  {
						  log.debug("Policy Reinstated but have future reinstatement date");
						  arlELStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }*/
			
				  if(epSpecWCMC!=null)
				  {
					  /*Exempt Check*/
					  if(GlobalVariables.YES.equals(epSpecWCMC.getExempt()) || GlobalVariables.YES.equals(epSpecWCMC.getUnlmtdElLimits()))
					  {
						  //log.debug("WC Exempt is Yes,EL Ok so skipping EL Check");
						  return arlELStatus;
					  }
				  }
				
				   /*Start Self Insured check*/
				  if(GlobalVariables.YES.equals(epSpecELMC.getSelfInsured()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 /*MC has SI and EP doesn't allow Self Insured*/
						 //log.debug("MC has SI and EP doesn't allow Self Insured");
						 /*Check if EP has given overrides*/
						 if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getSelfInsReq()))
						 {
							//log.debug("EP has Override for Self Insured");
							strOvrUsed=GlobalVariables.YES;
						 }
						 else
						 {
							 //log.debug("EP doesn't allow Self Insured So self Insured Problem");
							 arlELStatus.add(GlobalVariables.UVLD_SELF_INSRD_PRBLM);
						 }
					 }
					 else if(GlobalVariables.YES.equals(epSpecELMC.getSelfInsured())&& GlobalVariables.YES.equals(epDtls.getEpNeeds().getSelfInsReq()))
					 {
						 //log.debug("Exiting Emp Liability Check (after Self Insured Check)");
						 //log.debug("To skip All the Policy Test Status Ok as no policy check required");
						 return arlELStatus;
					 }
				   /*End Self Insured check*/
				  
				
				  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  if(GlobalVariables.YES.equals(epSpecELMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  {
					  /*MC has RRG and EP doesn't allow RRG*/
					  //log.debug("MC has RRG and EP doesn't allow RRG");
					  /*Check if EP has given overrides*/
					  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
					  {
						  //log.debug("EP has Override for RRG");
						  strOvrUsed=GlobalVariables.YES;
					  }
					  else
					  {
						  //log.debug("EP doesn't allow RRG So RRG Problem");
						  arlELStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
					  }
				  }
				 
				
				
				  /*Getting Limits and Deductibles for WC & EL*/
				  
				  /*For EL*/
				  dEL_ELA=Utility.commaStringtoDouble(epSpecELMC.getElEachOccur()); //EL Each Occur is EL Accident
				  dEL_ELE=Utility.commaStringtoDouble(epSpecELMC.getElDisEAEmp());
				  dEL_ELP=Utility.commaStringtoDouble(epSpecELMC.getElDisPlcyLmt());
				  if(umbGenMC!=null)
					  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
				  dMCHasDeduct=Utility.commaStringtoDouble(epSpecELMC.getDeductible());
				  /*Currency conversion for EL..*/
				  if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  dEL_ELA=dEL_ELA*cndUSD;
					  dEL_ELE=dEL_ELE*cndUSD;
					  dEL_ELP=dEL_ELE*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
				  }
				  else if(epSpecELMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  dEL_ELA=dEL_ELA*mexUSD;
					  dEL_ELE=dEL_ELE*mexUSD;
					  dEL_ELP=dEL_ELE*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
				  }
				  /*For WC*/
				  			
				  if(epSpecWCMC!=null)
				  {
					  //log.debug("After checking null condn for epSpecWCMC for Getting ELA ELE ELP for WC ");
					  dWC_ELA=Utility.commaStringtoDouble(epSpecWCMC.getElEachOccur()); //EL Each Occur is EL Accident
					  dWC_ELE=Utility.commaStringtoDouble(epSpecWCMC.getElDisEAEmp());
					  dWC_ELP=Utility.commaStringtoDouble(epSpecWCMC.getElDisPlcyLmt());
					  /*Currency conversion*/
					  if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dWC_ELA=dWC_ELA*cndUSD;
						  dWC_ELE=dWC_ELE*cndUSD;
						  dWC_ELP=dWC_ELP*cndUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*cndUSD ;*/
					  }
					  else if(epSpecWCMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dWC_ELA=dWC_ELA*mexUSD;
						  dWC_ELE=dWC_ELE*mexUSD;
						  dWC_ELP=dWC_ELP*mexUSD;
						  /*if(excessTrlMC!=null)
							  dExGenLimit=Utility.commaStringtoDouble(excessTrlMC.getLimit())*mexUSD ;*/
					  }
				  }
				  
				  //log.debug("After Currency conversion dEL_ELA "+ dEL_ELA+" dEL_ELE "+ dEL_ELE + "dEL_ELP "+dEL_ELP);
				  //log.debug("After Currency conversion dWC_ELA "+ dWC_ELA+" dWC_ELE "+ dWC_ELE + "dWC_ELP "+dWC_ELP);
				  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				 /* if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getELReqd()))
				  {
					  if(umbGenMC.getPolicyTerminatedDate().length()>0)
					  {
						  if(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||(Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate)))
						  {
							  //log.debug("Umbrella policy termination date less than equal to  parameter date ");
							  
						  }
					  }
				  }*/
				  dMC_Limit_ELA=dWC_ELA +dEL_ELA;
				  dMC_Limit_ELE=dWC_ELE +dEL_ELE;
				  dMC_Limit_ELP=dWC_ELP +dEL_ELP;
				  //log.info("dMC_Limit_ELA:- "+dMC_Limit_ELA+"  dMC_Limit_ELE:- "+dMC_Limit_ELE +" dMC_Limit_ELP:- "+dMC_Limit_ELP);
				  
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
				  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Setting Override used flag");
					  bOvrUsed=true;
				  }
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }
				  
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  /*dMC_Limit_ELA=dMC_Limit_ELA+dMC_ELA_Booster;
				  dMC_Limit_ELE=dMC_Limit_ELE+dMC_ELE_Booster;
				  dMC_Limit_ELP=dMC_Limit_ELP+dMC_ELP_Booster;*/
				  
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  if(dMC_Limit_ELA<dEP_Limit_ELA || dMC_Limit_ELE <dEP_Limit_ELE || dMC_Limit_ELP <dEP_Limit_ELP)
					  //if((dMC_Limit_ELA+ dMC_Limit_ELE+dMC_Limit_ELP)<(dEP_Limit_ELA+dEP_Limit_ELE+dEP_Limit_ELP))
				  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
				  }// end if limit
				  if(bLimitNotOkFlg)
				  {
					  arlELStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(dMCDedBooster!=0)
					  {
						  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
						  if(dMCDedBooster>dEPDeduct)
						  {
							  //log.debug("Ded problem after replacing Booster");
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  else
					  {
						  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
						  else	
						  arlELStatus.add(GlobalVariables.UVLD_DED_PRBLM);
					  }
				  }
				  //log.debug("Exiting EP Specific Policy Check for Employers Liability");
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Emp Liability Check for UValid:- ", exp);
			
		}
		//log.info("Business: Exiting method checkEmpLiability()of UValidPolicyCheck class:- "+arlELStatus);
		return arlELStatus;
	}
	
	public ArrayList checkEDHLiabilityArch(HashMap mcInsDtls,EPInsOvrWrapper epDtls,String strOvrUsed,String strEPSpcUsed,Date paramDate) throws Exception
	{
		ArrayList arlEDHStatus= new ArrayList(5);
		try
		{
			  boolean bLimitNotOkFlg=false;
			  boolean bDedNotOkFlg=false;
			  boolean bOvrUsed=false;
			  
			  StringBuffer sbEDHPrimary= new StringBuffer(GlobalVariables.EMPDISHBOND);
			  sbEDHPrimary.append(GlobalVariables.PRIMARYPOLICY);
			  StringBuffer sbEPSpecPolicy= new StringBuffer(GlobalVariables.EMPDISHBOND);
			  sbEPSpecPolicy.append(GlobalVariables.EPSPECIFICPOLICY);
			  EmpDishBean primEDHMC=(EmpDishBean)mcInsDtls.get(sbEDHPrimary.toString());
			  EmpDishBean epSpecEDHMC=null;
				  //(EmpDishBean)mcInsDtls.get(sbEPSpecPolicy.toString());
				
				  if(mcInsDtls.containsKey(sbEPSpecPolicy.toString()))
				  {
					  ArrayList arCg = new ArrayList();
					  arCg = (ArrayList)mcInsDtls.get(sbEPSpecPolicy.toString());
					  for(int x=0;x<arCg.size();x++)
					  {
						  epSpecEDHMC=(EmpDishBean)arCg.get(x);
						  if(epSpecEDHMC.getPolicyMstId()==0)
						  {
							  //epSpecEDHMC.setAddlnInsured(GlobalVariables.YES);
							  break;
						  }
						  else if(uValidDao.chkEPSpc(epSpecEDHMC.getPolicyMstId(),epDtls.getEpAcctNo()))
						  {
							  //epSpecEDHMC.setAddlnInsured(GlobalVariables.YES);
							  break;
						  }
						  else
						  {
							  epSpecEDHMC=null;
						  }
					  }
				  }
				  
			  UmbBean umbGenMC=(UmbBean)mcInsDtls.get(GlobalVariables.UMBRELLA);
			
			  double dMCHasEDHLimit=0.0;
			  double dMCHasDeduct=0.0;
			  double dMCUmbrella=0.0;
			  double dEPLimit=0.0;
			  double dEPDeduct=0.0;
			  double dMCLimBooster=0.0;
			  double dMCDedBooster=0.0;
			

			  /* to decide whether to check EP Specific or not..
			  * By default bEPPolCheck=false..  In Primary Policy it will be checked to see if there are problems
			  *  in Primary and then it will be set to true 
			  */
			  boolean bEPPolCheck=false; 
			  
			  /*To check If the EP requires this policy and MC has the same 
			   * If EP requires and MC do not have... then to set Error NO Policy.
			   * If EP doesn't require then no need to continue check..skip the policy check*/
			 
			  //log.debug("Policy Type for EP:- "+epDtls.getEpNeeds().getPolicyType());
			 
			  if(!GlobalVariables.EMPDISHBOND.equals(epDtls.getEpNeeds().getPolicyType()))
			  {
				  //log.debug("EP doesn't have Policy");
				  return arlEDHStatus;
			  }
			  /*Check EP Limits if Limit < 0, then no need to continue */
			  if(epDtls!=null && Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit())<=0)
			  {
				  //log.debug("Checking EP Limits <= 0 ");
				  //arlEDHStatus.add(GlobalVariables.UVLD_EP_LIMITS_PRBLM);
				  //log.debug("Exiting GenLiability Check as EP Limits <=0");
				  return arlEDHStatus;
			  }

			  //=====Added by Piyush on 3 July ====================================
			  //=====For waivers if provided <=0 as limit booster than skipping policy check===	
			  if(epDtls.getEpOvrMCBean().getLimitBooster().indexOf("-")>=0)
			  {
				  //log.debug("If boosters is NEGATIVE then skipping policy check :"+epDtls.getEpOvrMCBean().getLimitBooster());
				  return arlEDHStatus;
			  }
			  //=========End added=============================

			  if(epDtls!=null && GlobalVariables.YES.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP requires .. checking if MC has  either Primary or EP Specific ");
				  if(!(mcInsDtls.containsKey(sbEDHPrimary.toString()) ||mcInsDtls.containsKey(sbEPSpecPolicy.toString())))
				  {
					  //log.debug("MC doesn't have either primary or EP Specific ..so NO Policy error");
					  arlEDHStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
					  return arlEDHStatus;
				  }
			  }
			  else if(epDtls!=null && GlobalVariables.NO.equals(epDtls.getEpNeeds().getPolicyReq()))
			  {
				  //log.debug("EP doesn't requires .. so skipping Policy check ");
				  return arlEDHStatus;
			  }
			  
				  /* Getting EPLimits and Deductibles and Booster for Limits and Deductibles*/
			  if(epDtls!=null)
			  {
				  //log.debug("Getting EP Limits Deductibles && Booster for Limits and Deductibles");
				  dEPLimit=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMinLimit());
				  dEPDeduct=Utility.commaStringtoDouble(epDtls.getEpNeeds().getMaxDed());
				  dMCLimBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getLimitBooster());
				  dMCDedBooster=Utility.commaStringtoDouble(epDtls.getEpOvrMCBean().getDedBooster());
			  }
			  
			  if(primEDHMC!=null)
			  {
				       //log.debug("Entering Primary Policy Check for EmpDishBond Liability");
				       
				       //swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
						  if(paramDate.equals(Utility.stringToSqlDate(primEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							//log.debug("if policy expiration date is today's date");
							arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
							return arlEDHStatus;
						  }
						  if(Utility.stringToSqlDate(primEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(primEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
						  {
							  	//log.debug("if policy expiration date is today's date");
							  	arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
								return arlEDHStatus;
						  }
						  //end----02/03

				       
				       /*Start Check Policy Status (Termination|Cancellation) */
						  ArrayList arr = new ArrayList();
						  arr = getTmpTermDt(primEDHMC.getPolicyMstId());
						  Date tmpTerm = null;
						  Date tmpRein = null;
						  Date curTerm = null;
						  Date curReins = null;
						  if(!arr.isEmpty())
						  {
							  tmpTerm = (Date)arr.get(0);
							  tmpRein = (Date)arr.get(1);
						  }							  
						  //=========By Piyush 14Mar'09===============================================================
						  if(primEDHMC.getPolicyTerminatedDate().length()>0)
						  {
							  curTerm = Utility.stringToSqlDate(primEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4);
							  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
							  {
								  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
							  }
						  }
				  		  else if(primEDHMC.getPolicyReinstatedDate().length()>0)
						  {
				  			  curReins = Utility.stringToSqlDate(primEDHMC.getPolicyReinstatedDate(),Utility.FORMAT4);
							  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
							  else if(curReins.after(paramDate))
							  {
								  if(tmpTerm!=null && tmpTerm.equals(paramDate))
								  {
									  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
								  }
							  }
						  }						  
						  //=========End By Piyush 14Mar===============================================================					  
						  
					  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
					  if(GlobalVariables.YES.equals(primEDHMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
					  {
						  /*MC has RRG and EP doesn't allow RRG*/
						  //log.debug("MC has RRG and EP doesn't allow RRG");
						  /*Check if EP has given overrides*/
						  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
						  {
							  //log.debug("EP has Override for RRG");
							  strOvrUsed=GlobalVariables.YES;
						  }
						  else
						  {
							  //log.debug("EP doesn't allow RRG So RRG Problem");
							  arlEDHStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
						  }
					  }


					  /*Calculating Limits and Deductibles along with currency conversion*/
					  dMCHasEDHLimit=Utility.commaStringtoDouble(primEDHMC.getLimit());
					  dMCHasDeduct=Utility.commaStringtoDouble(primEDHMC.getDeductible());
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
					  if(primEDHMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
					  {
						  //log.debug("Canadian Currency Conversion");
						  dMCHasEDHLimit=dMCHasEDHLimit*cndUSD;
						  dMCHasDeduct=dMCHasDeduct*cndUSD;
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
					  }
					  else if(primEDHMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
					  {
						  //log.debug("Mexican Currency Conversion");
						  dMCHasEDHLimit=dMCHasEDHLimit*mexUSD;
						  dMCHasDeduct=dMCHasDeduct*mexUSD;
						  if(umbGenMC!=null)
							  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
					  }
					  //log.debug("After Currency conversion dMCHasEDHLimit "+dMCHasEDHLimit);
					  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
					  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
					  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
					  
					  /*check for umbrella poilcy exist and if exista add to MC Limit*/
					  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getEmpDishReqd()))
					  {
						  /*check if umbrella policy less than or equal to param date*/
						  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
						  {
							 	  //log.debug("Umbrella policy termination date after parameter date ");
							 	 dMCHasEDHLimit=dMCHasEDHLimit+dMCUmbrella;
						  }
						  
					  }
					  
					  /*Before adding Booster check for limits so that override flag can be set */
					  /*Checking for Override Used*/
					  if((dMCHasEDHLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
					  {
						  /* checking limits or deductibles without booster. If it fails then to set Override 
						   * used flag as true*/
						  //log.debug("Limits |Deductibles override used");
						  bOvrUsed=true;
					  }
					  /******************************************************************************/
					  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
					   * */
					  //dMCHasEDHLimit=dMCHasEDHLimit+dMCLimBooster; //adding booster to Limits
					  //dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
					  /*******************************************************************************/
					  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
					   * Deductibles*/
					  //log.info("MC Limit:- "+dMCHasEDHLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
					  //if(dMCHasEDHLimit<dEPLimit)
					  if((dMCHasEDHLimit<dEPLimit) && (dMCLimBooster==0))
					  {//Added as per new waiver specs
						  bLimitNotOkFlg=true;
					  }
					  else if(dMCHasEDHLimit<dMCLimBooster)
					  {	
						  //log.debug("Limit Not Ok before multiple Pol Limits check");
						  bLimitNotOkFlg=true;
					  }// end if limit
					  
					  /*Deductible check*/
					  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
					  if(dEPDeduct>0)
					  {
						  if(dMCHasDeduct>dEPDeduct)
						  {
							  //log.debug("Deductible Not Ok before multiple Pol Limits check");
							  bDedNotOkFlg=true;
						  }// end if deductible  
					  }
					  else
					  {
						  bDedNotOkFlg=true;
					  }
					  /*Check if multiple limits/deductible exists for this policy */
					  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
					  {
						  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
						  /*will loop through Multiple limits and deductibles bean to find appropriate
						   * limits and deductible to be used for the MC.
						   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
						   * */
						  if(bLimitNotOkFlg)
						  {
							  //log.debug("Checking Policy Multiple Limits and Deductibles ");
							  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
							  {
								  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
								  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
								  if((dMCHasEDHLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
								  {
									  /*found Multiple Limits and deductibles so using that limit and deductibles
									   * to check MC Limits and Deductibles; 
									   * */
									  bLimitNotOkFlg=false;
									  bDedNotOkFlg=false;
									  //break;
								  }
							  }// end of for loop
						  }//end if bLimitOkFlg
					  }//end check multiple limits and deductibles
					  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
					   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
					   * not found for Multiple Limits and Deductibles, so setting appropriate message
					   * */
					  if(bLimitNotOkFlg)
					  {
						  arlEDHStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
					  }
					  if(bDedNotOkFlg)
					  {
						 
						  if(dMCDedBooster!=0)
						  {
							  //log.debug("Override value exists so replacing MC Ded with Booster and then checking it again");
							  if(dMCDedBooster>dEPDeduct)
							  {
								  //log.debug("Ded problem after replacing Booster");
								  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
								  else	
								  arlEDHStatus.add(GlobalVariables.UVLD_DED_PRBLM);
							  }
						  }
						  else
						  {
							  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
							  else	
							  arlEDHStatus.add(GlobalVariables.UVLD_DED_PRBLM);
						  }
					  }
					  if(arlEDHStatus.size()==0)
					  {
					    //log.debug("NO Problems in Primary policy check:- so setting EPSpecific Check Flag ");
					    bEPPolCheck=true;
					  }
				      //log.debug("Exiting Primary Policy Check for EmpDishBond Liability");
			  }
			  else if(!bEPPolCheck && epSpecEDHMC!=null )
			  {
				  //log.debug("Entering EP Specific Policy Check for EmpDishBond Liability");
				  /*Making the arrayList of Problems empty before starting EP Specific Check...*/
				  arlEDHStatus.removeAll(arlEDHStatus);
				  
//				swati----02/03----if policy expiration date is today's date then showing only one problem in smartchecklist
				  if(paramDate.equals(Utility.stringToSqlDate(epSpecEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					//log.debug("if policy expiration date is today's date");
					arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
					return arlEDHStatus;
				  }
				  if(Utility.stringToSqlDate(epSpecEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)!=null && paramDate.after(Utility.stringToSqlDate(epSpecEDHMC.getPolicyExpiryDate(),Utility.FORMAT4)))
				  {
					  	//log.debug("if policy expiration date is today's date");
					  	arlEDHStatus.add(GlobalVariables.UVLD_POLICY_EXPIRED);
						return arlEDHStatus;
				  }
				  //end----02/03
				  
			       /*Start Check Policy Status (Termination|Cancellation) */
				  ArrayList arr = new ArrayList();
				  arr = getTmpTermDt(epSpecEDHMC.getPolicyMstId());
				  Date tmpTerm = null;
				  Date tmpRein = null;
				  Date curTerm = null;
				  Date curReins = null;
				  if(!arr.isEmpty())
				  {
					  tmpTerm = (Date)arr.get(0);
					  tmpRein = (Date)arr.get(1);
				  }							  
				  //=========By Piyush 14Mar'09===============================================================
				  if(epSpecEDHMC.getPolicyTerminatedDate().length()>0)
				  {
					  curTerm = Utility.stringToSqlDate(epSpecEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4);
					  if(curTerm.equals(paramDate) || curTerm.before(paramDate))
					  {
						  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }
		  		  else if(epSpecEDHMC.getPolicyReinstatedDate().length()>0)
				  {
		  			  curReins = Utility.stringToSqlDate(epSpecEDHMC.getPolicyReinstatedDate(),Utility.FORMAT4);
					  if(curReins.equals(paramDate) || curReins.before(paramDate)){}
					  else if(curReins.after(paramDate))
					  {
						  if(tmpTerm!=null && tmpTerm.equals(paramDate))
						  {
							  arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
						  }
					  }
				  }						  
				  //=========End By Piyush 14Mar===============================================================					  

				  
				  /*Start Check Policy Status (Termination|Cancellation) */
				  /*if(epSpecEDHMC.getPolicyTerminatedDate().length()>0)
				  {
					  //log.debug("Primary Policy Terminated Date= "+epSpecEDHMC.getPolicyTerminatedDate());
					  if(Utility.stringToSqlDate(epSpecEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4).before(paramDate)||Utility.stringToSqlDate(epSpecEDHMC.getPolicyTerminatedDate(),Utility.FORMAT4).equals(paramDate))
					  {
					      //log.debug("Primary Policy Terminated on parameter date ");
					      arlEDHStatus.add(GlobalVariables.UVLD_POL_TRMNTD);
					  }
				  }*/
				  /*Check if MC has Policy with RRG and EP doesn't allow RRG*/
				  if(GlobalVariables.YES.equals(epSpecEDHMC.getRrgFlg()) && GlobalVariables.NO.equals(epDtls.getEpNeeds().getRrgAllwd()))
				  {
					  /*MC has RRG and EP doesn't allow RRG*/
					  //log.debug("MC has RRG and EP doesn't allow RRG");
					  /*Check if EP has given overrides*/
					  if(GlobalVariables.YES.equals(epDtls.getEpOvrMCBean().getRrgAllwd()))
					  {
						  //log.debug("EP has Override for RRG");
						  strOvrUsed=GlobalVariables.YES;
					  }
					  else
					  {
						  //log.debug("EP doesn't allow RRG So RRG Problem");
						  arlEDHStatus.add(GlobalVariables.UVLD_RRG_PRBLM);
					  }
				  }

				  /*Calculating Limits and Deductibles along with currency conversion*/
				  dMCHasEDHLimit=Utility.commaStringtoDouble(epSpecEDHMC.getLimit());
				  dMCHasDeduct=Utility.commaStringtoDouble(epSpecEDHMC.getDeductible());
				  if(umbGenMC!=null)
					  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit());
				  if(epSpecEDHMC.getCurrency().equals(GlobalVariables.CURRCANADIAN))
				  {
					  //log.debug("Canadian Currency Conversion");
					  dMCHasEDHLimit=dMCHasEDHLimit*cndUSD;
					  dMCHasDeduct=dMCHasDeduct*cndUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*cndUSD ;
				  }
				  else if(epSpecEDHMC.getCurrency().equals(GlobalVariables.CURRMEXICAN))
				  {
					  //log.debug("Mexican Currency Conversion");
					  dMCHasEDHLimit=dMCHasEDHLimit*mexUSD;
					  dMCHasDeduct=dMCHasDeduct*mexUSD;
					  if(umbGenMC!=null)
						  dMCUmbrella=Utility.commaStringtoDouble(umbGenMC.getLimit())*mexUSD ;
				  }
				  //log.debug("After Currency conversion dMCHasEDHLimit "+dMCHasEDHLimit);
				  //log.debug("After Currency conversion dMCHasDeduct "+dMCHasDeduct);
				  ////log.debug("After Currency conversion dExGenLimit "+dExGenLimit);
				  //log.debug("After Currency conversion dMCUmbrella "+dMCUmbrella);
				  
				  /*check for umbrella poilcy exist and if exista add to MC Limit*/
				  if(umbGenMC!=null && GlobalVariables.YES.equals(umbGenMC.getEmpDishReqd()))
				  {
					  /*check if umbrella policy less than or equal to param date*/
					  if(umbGenMC.getPolicyTerminatedDate().length()==0 || Utility.stringToSqlDate(umbGenMC.getPolicyTerminatedDate(),Utility.FORMAT4).after(paramDate))
					  {
						 	  //log.debug("Umbrella policy termination date after parameter date ");
						 	 dMCHasEDHLimit=dMCHasEDHLimit+dMCUmbrella;
					  }
					  
				  }
				 
				  /*Before adding Booster check for limits so that override flag can be set */
				  /*Checking for Override Used*/
				  if((dMCHasEDHLimit<dEPLimit) || (dMCHasDeduct>dEPDeduct))
				  {
					  /* checking limits or deductibles without booster. If it fails then to set Override 
					   * used flag as true*/
					  //log.debug("Limits |Deductibles override used");
					  bOvrUsed=true;
				  }
				  /******************************************************************************/
				  /*Adding Booster to the MC Limit before comparing it with EP Limits and Deductibles
				   * */
				  //dMCHasEDHLimit=dMCHasEDHLimit+dMCLimBooster; //adding booster to Limits
				  dMCHasDeduct=dMCHasDeduct+dMCDedBooster; //adding booster to deductibles
				  /*******************************************************************************/
				  /*Limits Check and setting temp flag which will be use also for checking Multiple Limits and 
				   * Deductibles*/
				  //log.info("MC Limit:- "+dMCHasEDHLimit +"and New EP Limit:- "+ dMCLimBooster+" for Comparision");
				  //if(dMCHasEDHLimit<dEPLimit)
				  if((dMCHasEDHLimit<dEPLimit) && (dMCLimBooster==0))
				  {//Added as per new waiver specs
					  bLimitNotOkFlg=true;
				  }
				  else if(dMCHasEDHLimit<dMCLimBooster)
				  {	
					  //log.debug("Limit Not Ok before multiple Pol Limits check");
					  bLimitNotOkFlg=true;
				  }// end if limit
				  
				  /*Deductible check*/
				  //log.info("MC Deductible:- "+dMCHasDeduct +"and EP Deductible:- "+ dEPDeduct+" for Comparision");
				  if(dEPDeduct>0)//Commented by piyush 28March07
				  //if(dEPDeduct>=0)
				  {
					  if(dMCHasDeduct>dEPDeduct)
					  {
						  //log.debug("Deductible Not Ok before multiple Pol Limits check");
						  bDedNotOkFlg=true;
					  }// end if deductible  
				  }
				  else
				  {
					  bDedNotOkFlg=true;
				  }
				  /*Check if multiple limits/deductible exists for this policy */
				  if(epDtls!=null && epDtls.getPolicyMulLimits()!=null && !epDtls.getPolicyMulLimits().isEmpty())
				  {
					  //log.debug(" Entering if(!epDtls.getPolicyMulLimits().isEmpty())");
					  /*will loop through Multiple limits and deductibles bean to find appropriate
					   * limits and deductible to be used for the MC.
					   * If there is some problem with the primary Limit, then bLimitNotOkflg will be true
					   * */
					  if(bLimitNotOkFlg)
					  {
						  //log.debug("Checking Policy Multiple Limits and Deductibles ");
						  for(int i=0;i<epDtls.getPolicyMulLimits().size();i++)
						  {
							  double dTempEPLimit=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMinLimit());
							  double dTempEPDeduct=Utility.commaStringtoDouble(((MultipleLimit)epDtls.getPolicyMulLimits().get(i)).getMaxDed());
							  if((dMCHasEDHLimit>=dTempEPLimit)&&(dMCHasDeduct<=dTempEPDeduct))
							  {
								  /*found Multiple Limits and deductibles so using that limit and deductibles
								   * to check MC Limits and Deductibles; 
								   * */
								  bLimitNotOkFlg=false;
								  bDedNotOkFlg=false;
								  //break;
							  }
						  }// end of for loop
					  }//end if bLimitOkFlg
				  }//end check multiple limits and deductibles
				  /*Checking the bLimitNotOkFlg and bDedNotOkFlg...
				   * If bLimitNotOkFlg and bDedNotOkflg remains true, then appropriate limits and deductibles 
				   * not found for Multiple Limits and Deductibles, so setting appropriate message
				   * */
				  if(bLimitNotOkFlg)
				  {
					  arlEDHStatus.add(GlobalVariables.UVLD_LIMITS_PRBLM);
				  }
				  if(bDedNotOkFlg)
				  {
					  if(epDtls.getEpOvrMCBean().getDedBooster().equals("-1")){}
					  else	
					  arlEDHStatus.add(GlobalVariables.UVLD_DED_PRBLM);
				  }
				  //log.debug("Exiting EP Specific Policy Check for EmpDishBond Liability");
			  }
			  else
			  {
				  arlEDHStatus.add(GlobalVariables.MC_HAS_NOPOLICY);
				  return arlEDHStatus;
			  }
			  if(bOvrUsed)
			  {
				  /*as override has been used so will set the same in the parameter passed in this function*/
				  //log.debug("Setting strOverUsed=Yes");
				  strOvrUsed=GlobalVariables.YES;
			  }
		}
		catch(Exception exp)
		{
			log.error("Caught Exception in Emp Dishonesty Bond Check for UValid:- ", exp);
			
		}
		log.info("Business: Exiting method checkEDHLiability()of UValidPolicyCheck class:- "+arlEDHStatus);
		return arlEDHStatus;
	}
}
