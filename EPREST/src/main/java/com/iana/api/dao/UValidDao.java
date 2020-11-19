package com.iana.api.dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.iana.api.domain.Currency;
import com.iana.api.domain.EPJoinDet;

public interface UValidDao {

	/**
	 * will get all the currecny conversions
	 * 
	 * @return ArrayList
	 * @throws Exception
	 */
	List<Currency> getAllCurrencyConv() throws Exception;

	HashMap getMCInsDetailTableWhatIf(String strEPAccNo, String strMCAccNo, Date paramDate, String strMCSelEPFlg,
			String strMCNewName, String strWhatIf) throws Exception;

	/**
	 * This method gets Policy override details given by EP to all MC's or for all
	 * EP's against a given MC and will be called in UValid. Key for HashMap will
	 * either be EP Account Number or MC Account Number based on whether called from
	 * EP Change or MC Change
	 * 
	 * @param Connection con
	 * @param String     epAccNo
	 * @param String     strMCAccNo
	 * @return HashMap
	 * @throws Exception
	 */
	HashMap getPolicyOvrDtlsMCEP(String strEPAccNo, String strMCAccNo, String strEPChange, Date paramDate)
			throws Exception;

	/**
	 * This method will get all MC Details(Additional Requirement Details) against a
	 * given EP
	 * 
	 * @param strEPAccNo
	 * @param strMCAccNo
	 * @param paramDate
	 * @param strMCSelEPFlg
	 * @return HashMap
	 * @throws Exception
	 */
	HashMap getMCAddReqDetailsMap(String strEPAccNo, String strMCAccNo, Date paramDate, String strMCSelEPFlg)
			throws Exception;

	/**
	 * This method gets Additional Requirment  override details given by EP to all MC's or for all EP's 
	 * against a given MC and will be called in UValid. Key for HashMap will either be EP Account Number or MC Account Number based on 
	 * whether called from EP Change or MC Change
	 * @param  Connection con
	 * @param  String epAccNo
	 * @param  String strMCAccNo
	 * @return HashMap 
	 * @throws UiiaException
	 */
	HashMap getAddReqOvrDtlsMCEP(String strEPAccNo, String strMCAccNo, String strEPChange, Date paramDate)
			throws Exception;

	/**
	 * This method will get the Member Specific Details and will be used to
	 * calculate the MC EP Status based on Member Specific Carrier Flag
	 * 
	 * @param con
	 * @param strEPAccNo
	 * @param strMCAccNo
	 * @param paramDate
	 * @param tmpltId    Template Id.. which will be used for EP Change only
	 * @return
	 * @throws UiiaException
	 */
	HashMap getMemberSpecificDetails(String strEPAccNo, String strMCAccNo, Date paramDate, int tmpltId)
			throws Exception;

	/**
	 * gets MC Member details with EP i.e Member Type, Private, Cancelled etc.
	 * 
	 * @param Connection con
	 * @param String     mcAccNo
	 * @param String     epAccNo
	 * @return ArrayList
	 * @throws Exception
	 */
	EPJoinDet getEPMCMemberDetails(String mcAccNo, String epAccNo) throws Exception;

	boolean chkEPSpc(int mstId, String epAcctNo) throws Exception;

	ArrayList getEpSpcPending(String epAcctNo, String mcAcctNo, String polCode) throws Exception;

	ArrayList getDecEpSpcActive(String epAcctNo, String mcAcctNo, String polCode) throws Exception;


}
