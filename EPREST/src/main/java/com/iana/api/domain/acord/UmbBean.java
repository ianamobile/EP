/*
 *  File		: UmbBean.java
 *  Author		: Ashok Soni
 *  Created		: June 16,2006
 *  Description	: Bean for Umbrella Policy
 * 	Copyright   : Copyright (c) 2006-2007 IANA  
 * 				  All rights reserved 	
 */

/**
 * @author 146877
 *
 */
package com.iana.api.domain.acord;

public class UmbBean extends PolicyMasterBean implements IAPolicyBeanInterface
{
	private int umbPolicyId=0;
	private String ALReqd="";
	private String GLReqd="";
	private String cargoReqd="";
	private String contCargoReqd="";
	private String WCReqd="";
	private String ELReqd="";
	private String trailerReqd="";
	private String refTrailerReqd="";
	private String empDishReqd="";
	private String limitAgg="";
	private String occur="";
	private String claims="";
	private String retention="";
	
	public String getEmpDishReqd() {
		return empDishReqd;
	}
	public void setEmpDishReqd(String empDishReqd) {
		this.empDishReqd = empDishReqd;
	}
	public String getRefTrailerReqd() {
		return refTrailerReqd;
	}
	public void setRefTrailerReqd(String refTrailerReqd) {
		this.refTrailerReqd = refTrailerReqd;
	}
	public String getALReqd() {
		return ALReqd;
	}
	public void setALReqd(String reqd) {
		ALReqd = reqd;
	}
	public String getCargoReqd() {
		return cargoReqd;
	}
	public void setCargoReqd(String cargoReqd) {
		this.cargoReqd = cargoReqd;
	}
	public String getClaims() {
		return claims;
	}
	public void setClaims(String claims) {
		this.claims = claims;
	}
	public String getContCargoReqd() {
		return contCargoReqd;
	}
	public void setContCargoReqd(String contCargoReqd) {
		this.contCargoReqd = contCargoReqd;
	}
	public String getELReqd() {
		return ELReqd;
	}
	public void setELReqd(String reqd) {
		ELReqd = reqd;
	}
	public String getGLReqd() {
		return GLReqd;
	}
	public void setGLReqd(String reqd) {
		GLReqd = reqd;
	}
	public String getLimitAgg() {
		return limitAgg;
	}
	public void setLimitAgg(String limitAgg) {
		this.limitAgg = limitAgg;
	}
	public String getOccur() {
		return occur;
	}
	public void setOccur(String occur) {
		this.occur = occur;
	}
	public String getRetention() {
		return retention;
	}
	public void setRetention(String retention) {
		this.retention = retention;
	}
	public String getTrailerReqd() {
		return trailerReqd;
	}
	public void setTrailerReqd(String trailerReqd) {
		this.trailerReqd = trailerReqd;
	}
	public int getUmbPolicyId() {
		return umbPolicyId;
	}
	public void setUmbPolicyId(int umbPolicyId) {
		this.umbPolicyId = umbPolicyId;
	}
	public String getWCReqd() {
		return WCReqd;
	}
	public void setWCReqd(String reqd) {
		WCReqd = reqd;
	}

	public String toString()
	{
		StringBuffer sbTemp = new StringBuffer();
		
		sbTemp.append(this.getClass().getName());
		sbTemp.append(" umbPolicyId[").append(this.umbPolicyId).append("]");
		sbTemp.append(" ALReqd[").append(this.ALReqd).append("]");
		sbTemp.append(" GLReqd[").append(this.GLReqd).append("]");
		sbTemp.append(" cargoReqd[").append(this.cargoReqd).append("]");
		sbTemp.append(" cargoLim[").append(this.getLimit()).append("]");
		sbTemp.append(" contCargoReqd[").append(this.contCargoReqd).append("]");		
		sbTemp.append(" WCReqd[").append(this.WCReqd).append("]");
		sbTemp.append(" ELReqd[").append(this.ELReqd).append("]");
		sbTemp.append(" trailerReqd[").append(this.trailerReqd).append("]");
		sbTemp.append(" refTrailerReqd[").append(this.refTrailerReqd).append("]");
		sbTemp.append(" empDishReqd[").append(this.empDishReqd).append("]");
		sbTemp.append(" limitAgg[").append(this.limitAgg).append("]");
		sbTemp.append(" occur[").append(this.occur).append("]");
		sbTemp.append(" claims[").append(this.claims).append("]");
		sbTemp.append(" retention[").append(this.retention).append("]");
		
		return sbTemp.toString();
	}

	
	
}
