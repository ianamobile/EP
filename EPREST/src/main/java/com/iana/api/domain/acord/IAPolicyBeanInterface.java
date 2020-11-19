package com.iana.api.domain.acord;

public interface IAPolicyBeanInterface {
	
		public void setPolicyMstId(int policyMstId);
	 	public void setPolicyDetId(int policyDetId);
		public void setCertiId(int certiId);
		public void setCertiNo(String certiNo);
		public void setPolicyNo(String policyNo);
		public void setPolicyCode(String policyCode);
		public void setMcAcctNo(String mcAcctNo);
		public void setPolicyType(String policyType);
		public void setPolicyEffDate(String policyEffDate);
		public void setPolicyExpiryDate(String policyExpiryDate);
		public void setPolicyOvrWrttnDate(String policyOvrWrttnDate);
		public void setPolicyTerminatedDate(String policyTerminatedDate);
		public void setPolicyTerminatedEnteredDate(String policyTerminatedEnteredDate);
		public void setPolicyTerminationReason(String policyTerminationReason);
		public void setPolicyReinstatedDate(String policyReinstatedDate);
		public void setPolicyReinstatedEnteredDate(String policyReinstatedEnteredDate);
		public void setPolicyReinstatedReason(String policyReinstatedReason);
		public void setLimit(String limit);
		public void setDeductible(String deductible);
		public void setSelfInsured(String selfInsured);
		public void setCurrency(String currency);
		public void setInsurerName(String insurerName);
		public void setInsuranceAgent(String insuranceAgent);
		public void setNaicNo(String naicNo);
		public void setRrgFlg(String rrgFlg);
		public void setAddlnInsured(String addlnInsured);
		public void setPolicyStatus(String policyStatus);
		public void setBlanketReqd(String blanketReqd);
		public void setBlanketWordingId(int blanketWordingId);
		public void setBestRating(String bestRating);
		public void setInPlace(String inPlace);
		public void setMcName(String mcName);
		public void setMcScac(String mcScac);
		public void setCertiDate(String certiDate);
		public void setIaAcctNo(String iaAcctNo);
		public void setTmpTermDt(String tmpTermDt);
		public void setTmpReinsDt(String tmpReinsDt);
		public void setBdlyInjrdPerPerson1(String bdlyInjrdPerPerson1);
		public void setBdlyInjrdPerAccdnt1(String bdlyInjrdPerAccdnt1);
		public void setPropDmgPerAccdnt1(String propDmgPerAccdnt1);
		public void setEndorsementLL(String endorsementLL);
		public void setWordingChecker(String wordingChecker);
		
		public String getPolicyNo();
		public int getPolicyMstId();
		public int getPolicyDetId();
		public int getCertiId();
		public String getCertiNo();
		
		public String getPolicyTerminatedDate();
		public String getPolicyTerminationReason();
		public String getPolicyReinstatedDate();
		public String getPolicyReinstatedReason();
		
		public String getPolicyCode();

		public String getMcAcctNo();

		public String getPolicyType();

		public String getPolicyEffDate();

		public String getPolicyExpiryDate();

		public String getPolicyOvrWrttnDate();
		
		public String getPolicyReinstatedEnteredDate();

		public String getLimit();

		public String getDeductible();

		public String getSelfInsured();

		public String getCurrency();

		public String getInsurerName();

		public String getInsuranceAgent();

		public String getNaicNo();

		public String getRrgFlg();

		public String getAddlnInsured();

		public String getPolicyStatus();

		public String getBlanketReqd();

		public int getBlanketWordingId();

		public String getBestRating();

		public String getInPlace();

		public String getMcName();

		public String getMcScac();

		public String getCertiDate();

		public String getIaAcctNo();

		public String getTmpTermDt();

		public String getTmpReinsDt();

		public String getBdlyInjrdPerPerson1();

		public String getBdlyInjrdPerAccdnt1();

		public String getPropDmgPerAccdnt1();

		public String getWordingChecker();
		
		public String getEndorsementLL();
		
		public String getMemberCheck();
		
}
