/**
 * 
 */
package com.iana.api.domain.acord;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 140975
 *
 */
public class PolicyMasterBean
{
	private int policyMstId = 0; 
	private int policyDetId = 0;
	private int certiId = 0;
	private String certiNo="";
	private String policyNo = "";
	private String policyCode = "";
	private String mcAcctNo = "";
	private String policyType = "";
	private String policyEffDate = "";
	private String policyExpiryDate = "";
	private String policyOvrWrttnDate = "";
	private String policyTerminatedDate = "";
	private String policyTerminatedEnteredDate = "";
	private String policyTerminationReason = "";
	private String policyReinstatedDate = "";
	private String policyReinstatedEnteredDate = "";
	private String policyReinstatedReason = "";
	private String limit = "";
	private String deductible = "";
	private String selfInsured = "";
	private String currency = "";
	private String insurerName = "";
	private String insuranceAgent = "";
	private String naicNo = "";
	private String rrgFlg = "";
	private String addlnInsured = "";
	private String policyStatus = "";
	private String blanketReqd = "";
	private int blanketWordingId = 0;
	private String bestRating = "";
	private String attr1 = "";
	private String attr2 = "";
	private String attr3 = "";
	private String inPlace = "";
	private String mcName = "";
	private String mcScac = "";
	private String certiDate = "";
	private String iaAcctNo="";
	private String tmpTermDt="";
	private String tmpReinsDt="";
	private String bdlyInjrdPerPerson1="0";
	private String bdlyInjrdPerAccdnt1="0";
	private String propDmgPerAccdnt1="0";
	private String wordingChecker="";
	private String endorsementLL;
	//Added by Ankur March 2010 for new Acord
	
	private String memberCheck = "";
	
	// String-> epname, String-> tablename, Integer->id of newly inserted record
	private Map<String, Map<String, Integer>> policyEpMap = new HashMap<String, Map<String, Integer>>(); // Added by Saumil 10th July, 2015
	
	
	public Map<String, Map<String, Integer>> getPolicyEpMap() {
		return policyEpMap;
	}
	public void setPolicyEpMap(Map<String, Map<String, Integer>> policyEpMap) {
		this.policyEpMap = policyEpMap;
	}
	
	public String getWordingChecker() {
		return wordingChecker;
	}
	public void setWordingChecker(String wordingChecker) {
		this.wordingChecker = wordingChecker;
	}
	//Added by Ankur March 2010 for new Acord
	public String getBdlyInjrdPerAccdnt1() {
		return bdlyInjrdPerAccdnt1;
	}
	public void setBdlyInjrdPerAccdnt1(String bdlyInjrdPerAccdnt1) {
		this.bdlyInjrdPerAccdnt1 = bdlyInjrdPerAccdnt1;
	}
	public String getBdlyInjrdPerPerson1() {
		return bdlyInjrdPerPerson1;
	}
	public void setBdlyInjrdPerPerson1(String bdlyInjrdPerPerson1) {
		this.bdlyInjrdPerPerson1 = bdlyInjrdPerPerson1;
	}
	public String getPropDmgPerAccdnt1() {
		return propDmgPerAccdnt1;
	}
	public void setPropDmgPerAccdnt1(String propDmgPerAccdnt1) {
		this.propDmgPerAccdnt1 = propDmgPerAccdnt1;
	}
	public String getTmpReinsDt() {
		return tmpReinsDt;
	}
	public void setTmpReinsDt(String tmpReinsDt) {
		this.tmpReinsDt = tmpReinsDt;
	}
	public String getTmpTermDt() {
		return tmpTermDt;
	}
	public void setTmpTermDt(String tmpTermDt) {
		this.tmpTermDt = tmpTermDt;
	}
	public String getIaAcctNo() {
		return iaAcctNo;
	}
	public void setIaAcctNo(String iaAcctNo) {
		this.iaAcctNo = iaAcctNo;
	}
	public String getCertiDate() {
		return certiDate;
	}
	public void setCertiDate(String certiDate) {
		this.certiDate = certiDate;
	}
	public String getMcName() {
		return mcName;
	}
	public void setMcName(String mcName) {
		this.mcName = mcName;
	}
	public String getMcScac() {
		return mcScac;
	}
	public void setMcScac(String mcScac) {
		this.mcScac = mcScac;
	}
	public int getCertiId() {
		return certiId;
	}
	public void setCertiId(int certiId) {
		this.certiId = certiId;
	}
	public String getAttr3() {
		return attr3;
	}
	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}
	public String getInPlace() {
		return inPlace;
	}
	public void setInPlace(String inPlace) {
		this.inPlace = inPlace;
	}
	public String getPolicyReinstatedEnteredDate() {
		return policyReinstatedEnteredDate;
	}
	public void setPolicyReinstatedEnteredDate(String policyReinstatedEnteredDate) {
		this.policyReinstatedEnteredDate = policyReinstatedEnteredDate;
	}
	public String getPolicyTerminatedEnteredDate() {
		return policyTerminatedEnteredDate;
	}
	public void setPolicyTerminatedEnteredDate(String policyTerminatedEnteredDate) {
		this.policyTerminatedEnteredDate = policyTerminatedEnteredDate;
	}
	public String getPolicyReinstatedDate() {
		return policyReinstatedDate;
	}
	public void setPolicyReinstatedDate(String policyReinstatedDate) {
		this.policyReinstatedDate = policyReinstatedDate;
	}
	public String getPolicyReinstatedReason() {
		return policyReinstatedReason;
	}
	public void setPolicyReinstatedReason(String policyReinstatedReason) {
		this.policyReinstatedReason = policyReinstatedReason;
	}
	public String getAttr1() {
		return attr1;
	}
	public void setAttr1(String attr1) {
		this.attr1 = attr1;
	}
	public String getAttr2() {
		return attr2;
	}
	public void setAttr2(String attr2) {
		this.attr2 = attr2;
	}
	public String getAddlnInsured() {
		return addlnInsured;
	}
	public void setAddlnInsured(String addlnInsured) {
		this.addlnInsured = addlnInsured;
	}
	public String getBlanketReqd() {
		return blanketReqd;
	}
	public void setBlanketReqd(String blanketReqd) {
		this.blanketReqd = blanketReqd;
	}
	
	public int getBlanketWordingId() {
		return blanketWordingId;
	}
	public void setBlanketWordingId(int blanketWordingId) {
		this.blanketWordingId = blanketWordingId;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getDeductible() {
		return deductible;
	}
	public void setDeductible(String deductible) {
		this.deductible = deductible;
	}
	public String getInsurerName() {
		return insurerName;
	}
	public void setInsurerName(String insurerName) {
		this.insurerName = insurerName;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	public String getMcAcctNo() {
		return mcAcctNo;
	}
	public void setMcAcctNo(String mcAcctNo) {
		this.mcAcctNo = mcAcctNo;
	}
	public String getNaicNo() {
		return naicNo;
	}
	public void setNaicNo(String naicNo) {
		this.naicNo = naicNo;
	}
	public String getPolicyCode() {
		return policyCode;
	}
	public void setPolicyCode(String policyCode) {
		this.policyCode = policyCode;
	}
	public String getPolicyEffDate() {
		return policyEffDate;
	}
	public void setPolicyEffDate(String policyEffDate) {
		this.policyEffDate = policyEffDate;
	}
	public String getPolicyExpiryDate() {
		return policyExpiryDate;
	}
	public void setPolicyExpiryDate(String policyExpiryDate) {
		this.policyExpiryDate = policyExpiryDate;
	}
	
	public String getPolicyNo() {
		return policyNo;
	}
	public void setPolicyNo(String policyNo) {
		this.policyNo = policyNo;
	}
	public String getPolicyOvrWrttnDate() {
		return policyOvrWrttnDate;
	}
	public void setPolicyOvrWrttnDate(String policyOvrWrttnDate) {
		this.policyOvrWrttnDate = policyOvrWrttnDate;
	}
	public String getPolicyStatus() {
		return policyStatus;
	}
	public void setPolicyStatus(String policyStatus) {
		this.policyStatus = policyStatus;
	}
	public String getPolicyTerminatedDate() {
		return policyTerminatedDate;
	}
	public void setPolicyTerminatedDate(String policyTerminatedDate) {
		this.policyTerminatedDate = policyTerminatedDate;
	}
	public String getPolicyTerminationReason() {
		return policyTerminationReason;
	}
	public void setPolicyTerminationReason(String policyTerminationReason) {
		this.policyTerminationReason = policyTerminationReason;
	}
	public String getPolicyType() {
		return policyType;
	}
	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}
	public String getRrgFlg() {
		return rrgFlg;
	}
	public void setRrgFlg(String rrgFlg) {
		this.rrgFlg = rrgFlg;
	}
	public String getSelfInsured() {
		return selfInsured;
	}
	public void setSelfInsured(String selfInsured) {
		this.selfInsured = selfInsured;
	}
	public int getPolicyDetId() {
		return policyDetId;
	}
	public void setPolicyDetId(int policyDetId) {
		this.policyDetId = policyDetId;
	}
	public int getPolicyMstId() {
		return policyMstId;
	}
	public void setPolicyMstId(int policyMstId) {
		this.policyMstId = policyMstId;
	}
	public String getInsuranceAgent() {
		return insuranceAgent;
	}
	public void setInsuranceAgent(String insuranceAgent) {
		this.insuranceAgent = insuranceAgent;
	}
	public String getEndorsementLL() {
	
		return endorsementLL;
	}
	
	public void setEndorsementLL(String endorsementLL) {
	
		this.endorsementLL = endorsementLL;
	}
	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer();
		
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" policyMstId[").append(this.policyMstId).append("]");
		sbTemp.append(" policyDetId[").append(this.policyDetId).append("]");
		sbTemp.append(" certiId[").append(this.certiId).append("]");
		sbTemp.append(" policyNo[").append(this.policyNo).append("]");
		sbTemp.append(" policyCode[").append(this.policyCode).append("]");
		sbTemp.append(" mcAcctNo[").append(this.mcAcctNo).append("]");
		sbTemp.append(" policyType[").append(this.policyType).append("]");
		sbTemp.append(" policyEffDate[").append(this.policyEffDate).append("]");
		sbTemp.append(" policyExpiryDate[").append(this.policyExpiryDate).append("]");
		sbTemp.append(" policyOvrWrttnDate[").append(this.policyOvrWrttnDate).append("]");
		sbTemp.append(" policyTerminatedDate[").append(this.policyTerminatedDate).append("]");
		sbTemp.append(" policyTerminatedEnteredDate[").append(this.policyTerminatedEnteredDate).append("]");
		sbTemp.append(" policyTerminationReason[").append(this.policyTerminationReason).append("]");
		sbTemp.append(" policyReinstatedDate[").append(this.policyReinstatedDate).append("]");
		sbTemp.append(" policyReinstatedEnteredDate[").append(this.policyReinstatedEnteredDate).append("]");
		sbTemp.append(" policyReinstatedReason[").append(this.policyReinstatedReason).append("]");
		sbTemp.append(" limit[").append(this.limit).append("]");
		sbTemp.append(" deductible[").append(this.deductible).append("]");
		sbTemp.append(" selfInsured[").append(this.selfInsured).append("]");
		sbTemp.append(" currency[").append(this.currency).append("]");
		sbTemp.append(" insurerName[").append(this.insurerName).append("]");
		sbTemp.append(" insuranceAgent[").append(this.insuranceAgent).append("]");
		sbTemp.append(" naicNo[").append(this.naicNo).append("]");
		sbTemp.append(" rrgFlg[").append(this.rrgFlg).append("]");
		sbTemp.append(" addlnInsured[").append(this.addlnInsured).append("]");
		sbTemp.append(" policyStatus[").append(this.policyStatus).append("]");
		sbTemp.append(" blanketReqd[").append(this.blanketReqd).append("]");
		sbTemp.append(" blanketWordingId[").append(this.blanketWordingId).append("]");
		sbTemp.append(" attr1[").append(this.attr1).append("]");
		sbTemp.append(" attr2[").append(this.attr2).append("]");
		sbTemp.append(" attr3[").append(this.attr3).append("]");
		sbTemp.append(" endorsementLL[").append(this.endorsementLL).append("]");
		sbTemp.append(" inPlace[").append(this.inPlace).append("]");
		sbTemp.append(" mcName[").append(this.mcName).append("]");
		sbTemp.append(" mcScac[").append(this.mcScac).append("]");
		sbTemp.append(" policyEpMap[").append(this.policyEpMap).append("]");
		
		return sbTemp.toString();
	}
	public String getBestRating() {
		return bestRating;
	}
	public void setBestRating(String bestRating) {
		this.bestRating = bestRating;
	}
	public String getCertiNo() {
		return certiNo;
	}
	public void setCertiNo(String certiNo) {
		this.certiNo = certiNo;
	}
	public void setMemberCheck(String memberCheck) {
		this.memberCheck = memberCheck;
	}
	public String getMemberCheck() {
		return memberCheck;
	}

}
