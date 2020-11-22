package com.iana.api.domain;

import java.util.ArrayList;
import java.util.List;

public class EPAddendumDetForm {

	private String memberSpecific = "";
	private String knownAs = "";
	private String rampDetReq = "";
	private String blanketAllwd = "";

	private List<EPInsNeeds> epNeeds = new ArrayList<EPInsNeeds>();
	private List<MultipleLimit> multiLimits = new ArrayList<MultipleLimit>();
	private List<AdditionalReq> addReq = new ArrayList<AdditionalReq>();

	private String endrsDesc = "";
	private String required = "";
	private String originalReq = "";
	private String reqInDays = "0";
	private String spcinsallwd = "";
	private String addendumEffDate = "";
	private String templateId = "";
	private String addendumId = "";
	private String copyTmplt = "";

	public String getAddendumId() {
		return addendumId;
	}

	public void setAddendumId(String addendumId) {
		this.addendumId = addendumId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getAddendumEffDate() {
		return addendumEffDate;
	}

	public void setAddendumEffDate(String addendumEffDate) {
		this.addendumEffDate = addendumEffDate;
	}

	public EPInsNeeds getEpNeedsBean(int index) {
		int listSize = epNeeds.size();
		if ((index + 1) > listSize) {
			// add objects
			for (int j = listSize; j < index + 1; j++) {
				EPInsNeeds beanObj = new EPInsNeeds();
				epNeeds.add(j, beanObj);
			}
		}

		return epNeeds.get(index);
	}

	public void setMultiLim(int index, MultipleLimit addBean) {
		multiLimits.set(index, addBean);

	}

	public MultipleLimit getMultiLim(int index) {
		int listSize = multiLimits.size();
		if ((index + 1) > listSize) {
			// add objects
			for (int j = listSize; j < index + 1; j++) {
				MultipleLimit beanObj = new MultipleLimit();
				multiLimits.add(j, beanObj);
			}
		}

		return multiLimits.get(index);
	}

	public void setEpNeedsBean(int index, EPInsNeeds addBean) {
		epNeeds.set(index, addBean);

	}

	public AdditionalReq getEndorsment(int index) {
		int listSize = addReq.size();
		if ((index + 1) > listSize) {
			// add objects
			for (int j = listSize; j < index + 1; j++) {
				AdditionalReq beanObj = new AdditionalReq();
				addReq.add(j, beanObj);
			}
		}

		return addReq.get(index);
	}

	public void setEndorsment(int index, AdditionalReq endorsmentBean) {
		addReq.set(index, endorsmentBean);

	}

	public String getSpcinsallwd() {
		return spcinsallwd;
	}

	public void setSpcinsallwd(String spcinsallwd) {
		this.spcinsallwd = spcinsallwd;
	}

	public String getEndrsDesc() {
		return endrsDesc;
	}

	public void setEndrsDesc(String endrsDesc) {
		this.endrsDesc = endrsDesc;
	}

	public String getOriginalReq() {
		return originalReq;
	}

	public void setOriginalReq(String originalReq) {
		this.originalReq = originalReq;
	}

	public String getReqInDays() {
		return reqInDays;
	}

	public void setReqInDays(String reqInDays) {
		this.reqInDays = reqInDays;
	}

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	public String getBlanketAllwd() {
		return blanketAllwd;
	}

	public void setBlanketAllwd(String blanketAllwd) {
		this.blanketAllwd = blanketAllwd;
	}

	public String getKnownAs() {
		return knownAs;
	}

	public void setKnownAs(String knownAs) {
		this.knownAs = knownAs;
	}

	public String getMemberSpecific() {
		return memberSpecific;
	}

	public void setMemberSpecific(String memberSpecific) {
		this.memberSpecific = memberSpecific;
	}

	public String getRampDetReq() {
		return rampDetReq;
	}

	public void setRampDetReq(String rampDetReq) {
		this.rampDetReq = rampDetReq;
	}

	public List<EPInsNeeds> getEpNeeds() {
		return epNeeds;
	}

	public void setEpNeeds(List<EPInsNeeds> epNeeds) {
		this.epNeeds = epNeeds;
	}

	public List<MultipleLimit> getMultiLimits() {
		return multiLimits;
	}

	public void setMultiLimits(List<MultipleLimit> multiLimits) {
		this.multiLimits = multiLimits;
	}

	public List<AdditionalReq> getAddReq() {
		return addReq;
	}

	public void setAddReq(List<AdditionalReq> addReq) {
		this.addReq = addReq;
	}

	public String getCopyTmplt() {
		return copyTmplt;
	}

	public void setCopyTmplt(String copyTmplt) {
		this.copyTmplt = copyTmplt;
	}

}