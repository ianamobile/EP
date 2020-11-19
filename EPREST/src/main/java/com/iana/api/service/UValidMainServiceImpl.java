package com.iana.api.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.iana.api.dao.EPWhatIfDao;
import com.iana.api.dao.EpDao;
import com.iana.api.dao.UValidDao;
import com.iana.api.dao.UValidPolicyCheck;
import com.iana.api.domain.AdditionalReq;
import com.iana.api.domain.Currency;
import com.iana.api.domain.EPInsNeeds;
import com.iana.api.domain.EPInsOvrWrapper;
import com.iana.api.domain.EPJoinDet;
import com.iana.api.domain.EPSwitches;
import com.iana.api.domain.JoinRecord;
import com.iana.api.domain.MultipleLimit;
import com.iana.api.domain.OverrideNeeds;
import com.iana.api.domain.acord.ELBean;
import com.iana.api.domain.acord.UVldMemBean;
import com.iana.api.domain.acord.WCBean;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.Utility;

@Service
public class UValidMainServiceImpl extends CommonUtils implements UValidMainService {

	Logger log = LogManager.getLogger(this.getClass().getName());

//	@Autowired
//	private EpDao epDao;

//	@Autowired
//	private EPWhatIfDao epWhatIfDao;

	@Autowired
	private UValidDao uValidDao;

//	@Autowired
//	private EPService epService;
	
	@Autowired
	private UValidPolicyCheck busPolicyCheck;
	
	

	/**
	 * Wrapper class for getting Number of MCs that are Valid for any changes made
	 * by EP in WhatIf
	 * 
	 * @param strEPAcctNo Account Number of the EP
	 * @param epDtls      EP Details (having Switches, Additional Requirments,
	 *                    Policy Multiple Limits) <br>
	 *                    But from screen only switches is passed. other parameters
	 *                    are not set
	 * @param arlEPNeeds  ArrayList of EPInsNeeds (all the policy selected as Yes
	 *                    will be passed in this Arraylist <br>
	 *                    against which comparison will be made in UValidPolicyCheck
	 *                    class
	 * @return
	 * @throws Exception Custom Exception
	 */
	@Override
	public int getValidMCCountForWhatIf(String strEPAcctNo, EPInsOvrWrapper epDtls, List arlEPNeeds)
			throws Exception {
		// log.info("Entering method getValidMCForWhatIf () of UValidWrapper ");
		// log.info("strEPAcctNo :- "+strEPAcctNo);
		// log.info("EPInsOvrWrapper :- "+epDtls);
		// log.info("ArrayList arlEPNeeds :- "+arlEPNeeds);
		int cntrVldMC = 0;
		Connection con = null;
		List<Currency> arlCurr = new ArrayList<>(1);
		Map<String, Object> policyLimitsMap = null;
		Map<String, Object> epNeedsMap = null;
		EPInsOvrWrapper epDetailsBean = new EPInsOvrWrapper();
		HashMap hshMCDetailsMap = new HashMap(1);
		HashMap mcOvrDetailsTable = new HashMap(1);
		HashMap mcInsDetailsTable = null;
		HashMap polOvr = null;
		ArrayList arlProblems = null;
		HashMap mcAddReqOvrDtlsMap = null;
		HashMap hshMCAddlnReqMap = null;
		List<AdditionalReq> arlAddReq = new ArrayList<AdditionalReq>(2);
		HashMap hshMemSpcfcMap = null;
		ArrayList arlALProblems = null;
		ArrayList arlGLProblems = null;
		ArrayList arlCrgProblems = null;
		ArrayList arlContCrgProblems = null;
		ArrayList arlTrlProblems = null;
		ArrayList arlRefTrlProblems = null;
		ArrayList arlWCProblems = null;
		ArrayList arlELProblems = null;
		ArrayList arlMemSpcProblems = null;
		ArrayList arlAddReqProblems = null;
		ArrayList tempArl = new ArrayList();
		try {

			arlALProblems = new ArrayList();
			arlGLProblems = new ArrayList();
			arlCrgProblems = new ArrayList();
			arlContCrgProblems = new ArrayList();
			arlTrlProblems = new ArrayList();
			arlRefTrlProblems = new ArrayList();
			arlWCProblems = new ArrayList();
			arlELProblems = new ArrayList();
			arlMemSpcProblems = new ArrayList();
			arlAddReqProblems = new ArrayList();
			EPSwitches switchBean = epDtls.getEpSwitches();
			ArrayList arlPolMulLimits = epDtls.getPolicyMulLimits();
			arlAddReq = epDtls.getAddReq();
			// log.debug("EP Addtln Req:- "+arlAddReq);
			/* converting ArrayList of MultipleLimit and EPInsNeeds to HashTable */
			if (arlPolMulLimits != null) {
				policyLimitsMap = getHshtableForPolicy(arlPolMulLimits);
			}
			if (arlEPNeeds != null) {
				epNeedsMap = getHshtableForPolicy(arlEPNeeds);
			}
			/* End Get EP Details */

			/* Set EP Details in Ep Wrapper Bean */

			epDetailsBean.setEpAcctNo(strEPAcctNo);
			epDetailsBean.setEpSwitches(switchBean);

			/* Getting all the currencies from the database */

			arlCurr = uValidDao.getAllCurrencyConv();// Commented by piyush on 17Oct'07
			// arlCurr = GlobalVariables.CURR_ARRAY;
			String strMCAccNo = "";
			String strMCSelEPFlg = "";
			Date paramDate = Utility.getSqlSysdate();
			/*******************************************/

			/* 2. Get MC Details */
			/* (all MC Members for this EP i.e Insurance Details for MC) */
			/* setting New MC Name as empty string */
			/* setting whatif flag as yes... */
			strMCSelEPFlg = GlobalVariables.YES;
			hshMCDetailsMap = uValidDao.getMCInsDetailTableWhatIf(strEPAcctNo, strMCAccNo, paramDate, strMCSelEPFlg, "",
					GlobalVariables.YES);

			/* Getting All MC Additional Requirement */
			// HashMap
			// hshMCAddlnReqMap=busUvldDBHelper.getMCAddReqDetailsMap(con,strEPAcctNo,strMCAccNo,paramDate,strMCSelEPFlg);
			JoinRecord busJnRecord = new JoinRecord();
			/* End Get MC Details (all MC Members for this EP) */

			/* 3. Getting overrides given by this EP to all MCs */
			/*
			 * sending empty string as MC Account Number in get Pol Over details method to
			 * get Policy Overrides given by this EP to All MC's and setting EPChange Flag
			 * as Yes in the Method as EP req change is called
			 */
			mcOvrDetailsTable = uValidDao.getPolicyOvrDtlsMCEP(strEPAcctNo, strMCAccNo, GlobalVariables.YES, paramDate);

			hshMCAddlnReqMap = uValidDao.getMCAddReqDetailsMap(strEPAcctNo, strMCAccNo, paramDate, strMCSelEPFlg);

			// log.info("MC Additional Insurance Details:"+hshMCAddlnReqMap);
			/* Getting Overrdies for Additional Requirement */
			mcAddReqOvrDtlsMap = uValidDao.getAddReqOvrDtlsMCEP(strEPAcctNo, strMCAccNo, GlobalVariables.YES,
					paramDate);

			/* Getting Member Specific details */
			int templtId = epDtls.getTemplateId();
			// log.debug("Template Id from action class:- "+templtId);
			hshMemSpcfcMap = uValidDao.getMemberSpecificDetails(strEPAcctNo, strMCAccNo, paramDate, templtId);
			// log.debug("INS KEYSET :"+hshMCDetailsMap.keySet());
			Set mcAcctSet = hshMCDetailsMap.keySet();
			Iterator mcIter = mcAcctSet.iterator();
//			UValidPolicyCheck busPolicyCheck = new UValidPolicyCheck(paramDate, arlCurr);
			busPolicyCheck.setUValidPolicyCheck(paramDate, arlCurr);
			// log.debug("Size of MC HashMap:- "+hshMCDetailsMap.size());
			/* 3. End Getting overrides given by this EP to all MCs */

			/* 4. Looping through each MC */
			// boolean bOvrFlg=false;
			while (mcIter.hasNext()) {
				// log.debug("Iterating over MC hash table");
				Object keyMCObj = mcIter.next();
				// log.debug("Key for outer Hash Table:- "+keyMCObj);
				mcInsDetailsTable = (HashMap) hshMCDetailsMap.get(keyMCObj);

				/* Start Getting Override Bean from the Override Hashtable for this MC */
				polOvr = (HashMap) mcOvrDetailsTable.get(keyMCObj);
				if (polOvr != null)
					log.debug("PolOVER :" + polOvr.toString());
				else
					log.debug("No Override Exists ");
				/* End Getting Override Bean from the Override Hashtable for this MC */
				/*
				 * Start Calling Auto Liability check function Getting the Override,EP
				 * Needs,PolicyMulLimits for AL for this MC and setting in the Wrapper bean
				 * Initializing EPBean before calling Policy check methods
				 */
				epDetailsBean.initForEPChange();
				arlProblems = null;
				arlProblems = new ArrayList(10);
				/*
				 * Calling AutoLiability Check Method after checking whether the check is for
				 * Auto Liabilty
				 */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.AUTOPOLICY)) {
					// log.debug("EPNeeds has AutoLiability");
					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.AUTOPOLICY) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.AUTOPOLICY));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.AUTOPOLICY) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.AUTOPOLICY));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.AUTOPOLICY) != null)
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.AUTOPOLICY));

					if (mcInsDetailsTable != null) {
						arlALProblems = busPolicyCheck.checkAutoLiability(mcInsDetailsTable, epDetailsBean, "", "");
					}
				}

				/* Calling General Liability Check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.GENPOLICY)) {
					// log.debug("EPNeeds has General Liability");
					epDetailsBean.initForEPChange();

					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.GENPOLICY) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.GENPOLICY));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.GENPOLICY) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.GENPOLICY));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.GENPOLICY) != null)
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.GENPOLICY));

					if (mcInsDetailsTable != null) {
						arlGLProblems = busPolicyCheck.checkGeneralLiability(mcInsDetailsTable, epDetailsBean, "", "");
					}
				}

				/* Calling Cargo Liability check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.CARGOPOLICY)) {
					// log.debug("EPNeeds has Cargo Liability");
					epDetailsBean.initForEPChange();

					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.CARGOPOLICY) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.CARGOPOLICY));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.CARGOPOLICY) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.CARGOPOLICY));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.CARGOPOLICY) != null) {
						// log.debug("CARGO OVERRIDE BEAN
						// :"+((OverrideNeeds)polOvr.get(GlobalVariables.CARGOPOLICY)).toString());
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.CARGOPOLICY));
					}

					if (mcInsDetailsTable != null) {
						arlCrgProblems = busPolicyCheck.checkCargoLiability(mcInsDetailsTable, epDetailsBean, "", "");
					}
				}

				/* Calling Contingent Cargo Liability check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.CONTCARGO)) {
					// log.debug("EPNeeds has contingent Cargo Liability");
					epDetailsBean.initForEPChange();

					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.CONTCARGO) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.CONTCARGO));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.CONTCARGO) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.CONTCARGO));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.CONTCARGO) != null)
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.CONTCARGO));

					if (mcInsDetailsTable != null) {
						arlContCrgProblems = busPolicyCheck.checkContCargoLiability(mcInsDetailsTable, epDetailsBean,
								"", "");
					}
				}

				/* Calling Trailer Liability check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.TRAILERPOLICY)) {
					// log.debug("EPNeeds has Trailer Liability");
					epDetailsBean.initForEPChange();

					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.TRAILERPOLICY) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.TRAILERPOLICY));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.TRAILERPOLICY) != null)
						epDetailsBean
								.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.TRAILERPOLICY));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.TRAILERPOLICY) != null) {
						// log.debug("TRAILER OVERRIDE BEAN
						// :"+((OverrideNeeds)polOvr.get(GlobalVariables.TRAILERPOLICY)).toString());
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.TRAILERPOLICY));
					}
					if (mcInsDetailsTable != null) {
						arlTrlProblems = busPolicyCheck.checkTrailerLiability(mcInsDetailsTable, epDetailsBean, "", "");
					}
				}

				/* Calling Refrigerated Trailer Liability check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.REFTRAILER)) {
					// log.debug("EPNeeds has Refrigerated Trailer Liability");
					epDetailsBean.initForEPChange();

					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.REFTRAILER) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.REFTRAILER));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.REFTRAILER) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.REFTRAILER));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.REFTRAILER) != null)
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.REFTRAILER));

					if (mcInsDetailsTable != null) {
						// log.debug("Calling Ref Trailer Liaility check method");
						arlRefTrlProblems = busPolicyCheck.checkRefTrailerLiability(mcInsDetailsTable, epDetailsBean,
								"", "");

					}
				}

				String strELCallFlg = "";
				/* Calling Workers Compensation Liability check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.WORKCOMP)) {
					// log.debug("EPNeeds has Workers Compensation Liability");
					double dWC_ELA = 0.0;
					double dWC_ELP = 0.0;
					double dWC_ELE = 0.0;
					WCBean w = null;

					if (mcInsDetailsTable != null && mcInsDetailsTable
							.containsKey(GlobalVariables.WORKCOMP + GlobalVariables.PRIMARYPOLICY)) {
						w = (WCBean) mcInsDetailsTable.get(GlobalVariables.WORKCOMP + GlobalVariables.PRIMARYPOLICY);
						dWC_ELA = Utility.commaStringtoDouble(w.getElEachOccur());
						dWC_ELP = Utility.commaStringtoDouble(w.getElDisPlcyLmt());
						dWC_ELE = Utility.commaStringtoDouble(w.getElDisEAEmp());
						if (!mcInsDetailsTable.containsKey(GlobalVariables.EMPLIABILITY + GlobalVariables.PRIMARYPOLICY)
								&& ((dWC_ELA + dWC_ELP + dWC_ELE) > 0 || GlobalVariables.YES.equals(w.getExempt()))) {
							// log.debug("Add temp Bean for EL to show on SmartCheck list");
							ELBean elBean = new ELBean();
							elBean.setElDisEAEmp(w.getElDisEAEmp());
							elBean.setElDisPlcyLmt(w.getElDisPlcyLmt());
							elBean.setElEachOccur(w.getElEachOccur());
							elBean.setSelfInsured(w.getSelfInsured());
							elBean.setRrgFlg(w.getRrgFlg());
							mcInsDetailsTable.put(GlobalVariables.EMPLIABILITY + GlobalVariables.PRIMARYPOLICY, elBean);
						} else if (mcInsDetailsTable
								.containsKey(GlobalVariables.EMPLIABILITY + GlobalVariables.PRIMARYPOLICY)) {
							/*
							 * Check if EL is expired or terminated then use EL limits provided on WC Policy
							 * -- Added By Piyush on 19Oct'10
							 */
							ELBean elBean1 = (ELBean) mcInsDetailsTable
									.get(GlobalVariables.EMPLIABILITY + GlobalVariables.PRIMARYPOLICY);
							if (elBean1 != null) {
								if (Utility.stringToSqlDate(elBean1.getPolicyExpiryDate(), Utility.FORMAT4) != null
										&& (Utility.getSqlSysdate().equals(
												Utility.stringToSqlDate(elBean1.getPolicyExpiryDate(), Utility.FORMAT4))
												|| Utility.getSqlSysdate().after(Utility.stringToSqlDate(
														elBean1.getPolicyExpiryDate(), Utility.FORMAT4)))) {// Check if
																											// EL is
																											// expired
									ELBean elBean = new ELBean();
									elBean.setElDisEAEmp(w.getElDisEAEmp());
									elBean.setElDisPlcyLmt(w.getElDisPlcyLmt());
									elBean.setElEachOccur(w.getElEachOccur());
									elBean.setSelfInsured(w.getSelfInsured());
									elBean.setRrgFlg(w.getRrgFlg());
									mcInsDetailsTable.put(GlobalVariables.EMPLIABILITY + GlobalVariables.PRIMARYPOLICY,
											elBean);
								} else {// Check if EL is terminated
									boolean isTerminated = false;
									ArrayList arr = new ArrayList();
									arr = busPolicyCheck.getTmpTermDt(elBean1.getPolicyMstId());
									Date tmpTerm = null;
									Date tmpRein = null;
									Date curTerm = null;
									Date curReins = null;
									if (!arr.isEmpty()) {
										tmpTerm = (Date) arr.get(0);
										tmpRein = (Date) arr.get(1);
									}

									if (elBean1.getPolicyTerminatedDate().length() > 0) {
										curTerm = Utility.stringToSqlDate(elBean1.getPolicyTerminatedDate(),
												Utility.FORMAT4);
										if (curTerm.equals(paramDate) || curTerm.before(paramDate)) {
											isTerminated = true;
										}
									} else if (elBean1.getPolicyReinstatedDate().length() > 0) {
										curReins = Utility.stringToSqlDate(elBean1.getPolicyReinstatedDate(),
												Utility.FORMAT4);
										if (curReins.equals(paramDate) || curReins.before(paramDate)) {
										} else if (curReins.after(paramDate)) {
											if (tmpTerm != null && tmpTerm.equals(paramDate)) {
												isTerminated = true;
											}
										}
									}
									if (isTerminated) {
										ELBean elBean = new ELBean();
										elBean.setElDisEAEmp(w.getElDisEAEmp());
										elBean.setElDisPlcyLmt(w.getElDisPlcyLmt());
										elBean.setElEachOccur(w.getElEachOccur());
										elBean.setSelfInsured(w.getSelfInsured());
										elBean.setRrgFlg(w.getRrgFlg());
										mcInsDetailsTable.put(
												GlobalVariables.EMPLIABILITY + GlobalVariables.PRIMARYPOLICY, elBean);
									}
								}
							}
							/*
							 * End of new condition -- Added by Piyush on 19Oct'10 -- Check if EL is expired
							 * or terminated
							 */
						}
					}
					strELCallFlg = GlobalVariables.NO;

					epDetailsBean.initForEPChange();
					if (epNeedsMap != null && !epNeedsMap.isEmpty() && epNeedsMap.get(GlobalVariables.WORKCOMP) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.WORKCOMP));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.WORKCOMP) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.WORKCOMP));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.WORKCOMP) != null)
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.WORKCOMP));

					if (mcInsDetailsTable != null) {
						// log.debug("Calling Cont Cargo Liaility check method");
						arlWCProblems = busPolicyCheck.checkWCLiability(mcInsDetailsTable, epDetailsBean, "", "",
								strELCallFlg);
					} else {
						// log.debug("MC Insurance details not found.. Trailer Liability check not
						// called");
					}
					// log.info("Problems Size:- "+arlProblems.size()+" for policy on EP Change :-
					// "+arlProblems);

					/*
					 * To call EL along with WC.... after checking flag which is returned from
					 * WCLiability check method
					 */
					/*
					 * Change made for EL Call ..... El Call Flag, as the reference to the String
					 * object doesn't remain same after the Method call, so as a change, the flag is
					 * temporarily set in the EPInsOverridewrapper bean....
					 */
					strELCallFlg = epDetailsBean.getElFlag();
					/* End change EL call..... */
					// log.info("strELCallFlg Flag from WCLiability check method"+strELCallFlg);
					if (GlobalVariables.YES.equals(strELCallFlg)) {
						// log.debug("To call EL Check for Workers Compensation as strELCallFlg=Yes");
						if (epNeedsMap != null && !epNeedsMap.isEmpty()
								&& epNeedsMap.get(GlobalVariables.EMPLIABILITY) != null)
							epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.EMPLIABILITY));
						if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
								&& policyLimitsMap.get(GlobalVariables.EMPLIABILITY) != null)
							epDetailsBean
									.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.EMPLIABILITY));
						if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.EMPLIABILITY) != null)
							epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.EMPLIABILITY));

						if (mcInsDetailsTable != null) {
							// log.debug("Calling Emp Liaility check method");
							arlELProblems = busPolicyCheck.checkEmpLiability(mcInsDetailsTable, epDetailsBean, "", "");
						}
					}

				}
				// log.info("strELCallFlg Flag for EL check method"+strELCallFlg);
				if (GlobalVariables.NO.equals(strELCallFlg)) {
					/* Calling Employers Liability check method */
					if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.EMPLIABILITY)) {
						// log.debug("EPNeeds has Employers Liability");
						epDetailsBean.initForEPChange();

						if (epNeedsMap != null && !epNeedsMap.isEmpty()
								&& epNeedsMap.get(GlobalVariables.EMPLIABILITY) != null)
							epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.EMPLIABILITY));
						if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
								&& policyLimitsMap.get(GlobalVariables.EMPLIABILITY) != null)
							epDetailsBean
									.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.EMPLIABILITY));
						if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.EMPLIABILITY) != null)
							epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.EMPLIABILITY));

						if (mcInsDetailsTable != null) {
							// log.debug("Calling Cont Cargo Liaility check method");
							arlELProblems = busPolicyCheck.checkEmpLiability(mcInsDetailsTable, epDetailsBean, "", "");
						}
					}
				}

				/* Calling Employee Dishonesty Bond Liability check method */
				if (epNeedsMap != null && epNeedsMap.containsKey(GlobalVariables.EMPDISHBOND)) {
					// log.debug("EPNeeds has Employee Dishonesty Bond Liability");
					epDetailsBean.initForEPChange();
					if (epNeedsMap != null && !epNeedsMap.isEmpty()
							&& epNeedsMap.get(GlobalVariables.EMPDISHBOND) != null)
						epDetailsBean.setEpNeeds((EPInsNeeds) epNeedsMap.get(GlobalVariables.EMPDISHBOND));
					if (policyLimitsMap != null && !policyLimitsMap.isEmpty()
							&& policyLimitsMap.get(GlobalVariables.EMPDISHBOND) != null)
						epDetailsBean.setPolicyMulLimits((ArrayList) policyLimitsMap.get(GlobalVariables.EMPDISHBOND));
					if (polOvr != null && !polOvr.isEmpty() && polOvr.get(GlobalVariables.EMPDISHBOND) != null)
						epDetailsBean.setEpOvrMCBean((OverrideNeeds) polOvr.get(GlobalVariables.EMPDISHBOND));

					if (mcInsDetailsTable != null) {
						// log.debug("Calling Cont Cargo Liaility check method");
						arlProblems = busPolicyCheck.checkEDHLiability(mcInsDetailsTable, epDetailsBean, "", "");
					}
				}

				/* Start Additional Req check */
				// log.debug("Calling Additional Requirement Check method for What If");
				if (mcAddReqOvrDtlsMap != null && mcAddReqOvrDtlsMap.get(keyMCObj) != null)
					epDetailsBean.setAddReqOvrMC((ArrayList) mcAddReqOvrDtlsMap.get(keyMCObj));
				// log.info("Override AddReq for EP:- "+epDetailsBean.getAddReqOvrMC());
				ArrayList arlMCAddReq = (ArrayList) hshMCAddlnReqMap.get(keyMCObj);
				/*
				 * Getting Member Details for MC EP to know whether the MC is private to EP or
				 * not which is required for Additional Requirement check
				 */
				String strMCPrvtForEP = "";
				EPJoinDet jnRcrdMCEP = null;
				try {
					jnRcrdMCEP = uValidDao.getEPMCMemberDetails(keyMCObj.toString(), strEPAcctNo);
				} catch (Exception uiiaExp) {
					// log.debug("Exception Caught while getting MC EP Join Record :- "+uiiaExp);
				}
				if (jnRcrdMCEP != null && GlobalVariables.YES.equals(jnRcrdMCEP.getEpPrivate())) {
					/* If for this MC EP combination, MC is private then seting strMCPrivate =Y */
					// log.debug("Setting MC as Private for Additional Requirement check");
					strMCPrvtForEP = GlobalVariables.YES;
				} else {
					/*
					 * If for this MC EP combination, MC is not private then seting strMCPrivate =N
					 */
					// log.debug("Setting MC not Private for Additional Requirement check");
					strMCPrvtForEP = GlobalVariables.NO;
				}
				epDetailsBean.setAddReq(arlAddReq);
				// log.debug("EP Bean:- "+epDetailsBean.getAddReq());
				arlAddReqProblems = busPolicyCheck.checkAddtlnReqmnt(arlMCAddReq, epDetailsBean, strMCPrvtForEP, "");
				/* End Additional Req check */

				/* Start calling Member Specific Carrier Check */
				UVldMemBean memSpcBean = (UVldMemBean) hshMemSpcfcMap.get(keyMCObj.toString());
				if (memSpcBean != null) {
					/*
					 * Setting Member Specific Carrier for EP based on the parameter recived from
					 * the screen
					 */
					// log.debug("Calling member specific Check");
					memSpcBean.setEpReqMem(switchBean.getMemberSpecific());
					arlMemSpcProblems = checkMemSpecCarrier(memSpcBean, paramDate);
				}

				/* Finally incrementing the counter */
				if (arlALProblems.size() > 0)
					log.debug("AL Problems :" + arlALProblems);
				if (arlGLProblems.size() > 0)
					log.debug("GL Problems :" + arlGLProblems);
				if (arlCrgProblems.size() > 0)
					log.debug("CL Problems :" + arlCrgProblems);
				if (arlContCrgProblems.size() > 0)
					log.debug("CCL Problems :" + arlContCrgProblems);
				if (arlTrlProblems.size() > 0)
					log.debug("TL Problems :" + arlTrlProblems);
				if (arlRefTrlProblems.size() > 0)
					log.debug("RTL Problems :" + arlRefTrlProblems);
				if (arlWCProblems.size() > 0)
					log.debug("WC Problems :" + arlWCProblems);
				if (arlELProblems.size() > 0)
					log.debug("EL Problems :" + arlELProblems);
				if (arlProblems.size() > 0)
					log.debug("EDH Problems :" + arlProblems);
				if (arlAddReqProblems.size() > 0)
					log.debug("AREQ Problems :" + arlAddReqProblems);
				if (arlMemSpcProblems.size() > 0)
					log.debug("MEMSPC Problems :" + arlMemSpcProblems);

				if (arlProblems.size() == 0 && arlALProblems.size() == 0 && arlGLProblems.size() == 0
						&& arlCrgProblems.size() == 0 && arlContCrgProblems.size() == 0 && arlTrlProblems.size() == 0
						&& arlRefTrlProblems.size() == 0 && arlWCProblems.size() == 0 && arlELProblems.size() == 0
						&& arlAddReqProblems.size() == 0 && arlMemSpcProblems.size() == 0) {
					cntrVldMC++;
					tempArl.add(keyMCObj.toString());
					log.debug("MC Account no that is Valid:-" + keyMCObj.toString() + "  and Current Count = "
							+ cntrVldMC);
				} else {
					log.debug(
							"NOT VALID MC FOR NEW REQ:-" + keyMCObj.toString() + "  and Current Count = " + cntrVldMC);
				}
			} // end iterating over MCs
				// log.info("Final count of Valid MC:- "+cntrVldMC);
			/* Closing connection */
			// log.info("Total Valid MCs :"+tempArl.toString());
		} catch (Exception uiiaExp) {
			log.error("Caught Exception:- " + uiiaExp.getMessage());
			throw uiiaExp;
		} // end of Exception

		return cntrVldMC;
	}

	/**
	 * This method will return Hashtable from ArrayList of EPNeeds or Policy
	 * Multilple Limits passed
	 * 
	 * @param ArrayList arlPolicy
	 * @return HashMap
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String, Object> getHshtableForPolicy(List<T> arlPolMulLimits) throws Exception {
		// log.info("Session Facade: Entering method getHshtableForPolicy(ArrayList
		// arlPolicy) of UValidMain class");
		// log.info("Session Facade: Value for arlPolicy: "+arlPolicy);
		Map<String, Object> policyMap = new HashMap<String, Object>(10);
		if (!arlPolMulLimits.isEmpty()) {
			// log.debug("Entering if(!arlPolicy.isEmpty())");
			Iterator<T> iterPolicy = arlPolMulLimits.iterator();
			List<T> arlPolMulLmts = null;
			while (iterPolicy.hasNext()) {
				// log.debug("Iteratiog over ArrayList");
				Object insObj = iterPolicy.next();
				if (insObj instanceof MultipleLimit) {
					MultipleLimit epMulPolLimits = new MultipleLimit();
					// log.debug("Entering if(insObj instanceof MultipleLimit)");
					epMulPolLimits = (MultipleLimit) insObj;
					if (!policyMap.containsKey(epMulPolLimits.getPolicyType())) {
						arlPolMulLmts = new ArrayList<T>(20);
						policyMap.put(epMulPolLimits.getPolicyType(), arlPolMulLmts);
					}
					T t = (T) epMulPolLimits;
					((List<T>) policyMap.get(epMulPolLimits.getPolicyType())).add(t);
				}
				if (insObj instanceof EPInsNeeds) {
					EPInsNeeds epNeeds = new EPInsNeeds();
					// log.debug("Entering if(insObj instanceof EPInsNeeds)");
					epNeeds = (EPInsNeeds) insObj;
					policyMap.put(epNeeds.getPolicyType(), epNeeds);
				}
			}
			//// log.info("Cntr in Hashtable conversion:-"+intCntr);
			// log.debug("Exiting if(!arlPolicy.isEmpty())");
		}
		return policyMap;
	}

	public ArrayList checkMemSpecCarrier(UVldMemBean memDtls, java.sql.Date paramDate) {
		// log.info("Entering method checkMemSpecCarrier()");
		// log.info("memDtls:- "+memDtls);
		// log.info("paramDate:- "+paramDate);
		ArrayList arlMemDtls = new ArrayList(1);

		if (GlobalVariables.YES.equals(memDtls.getEpReqMem())) {
			log.debug("EP Requires MC Specific Carrier");
			if (GlobalVariables.YES.equals(memDtls.getMcIsMem())) {
				log.debug("MC is Member Specific Carrier, so to check if Member is cancelled");
			} else {
				log.debug("MC is not a memer Specific Carrier so setting problem");
				arlMemDtls.add(GlobalVariables.MEM_SPECIFIC_CARRIER);
			}
		}
		if (GlobalVariables.YES.equals(memDtls.getCncl())) {
			// log.debug("Member is cancelled.. Now checking cancelled date");
			if (Utility.stringToSqlDate(memDtls.getCnclDt(), Utility.FORMAT4).before(paramDate)
					|| Utility.stringToSqlDate(memDtls.getCnclDt(), Utility.FORMAT4).equals(paramDate)) {
				// log.debug("Cancelled Date < Param Date.. so MC is not Mem Specific Carrier");
				arlMemDtls.add(GlobalVariables.CANCELLED_BY_EP);
			}
		}

		return arlMemDtls;
	}

}
