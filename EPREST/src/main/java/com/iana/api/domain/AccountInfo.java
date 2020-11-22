package com.iana.api.domain;

import com.iana.api.utils.GlobalVariables;

import lombok.Data;

@Data
public class AccountInfo implements Comparable<AccountInfo> {

	private String accountNo = "";
	private String companyName = "";
	private String epEntities = "";
	private String scacCode = "";
	private String oldScac = "";
	private String ianaMem = GlobalVariables.NO;
	private String nonUiiaEp = GlobalVariables.NO;
	private String statusWTrac = GlobalVariables.NO;
	private String uiiaStatus = "";
	private String uiiaStatusCd = "";
	private String memType = "";
	private String compUrl = "";
	private String attr1 = "";
	private String attr2 = "";
	private String attr3 = "";
	private String memEffDt = "";
	private String cancelledDt = "";
	private String deletedDate = "";
	private String reInstatedDt = "";
	private String modifiedDate = "";
	private String password = "";
	private String oldUiiaStatus = "";
	private String secUserName = "";
	private String oldSecUserName = "";
	private String uiiaMember = "";
	private String iddMember = "";
	private String iddStatus = "";
	private String applyUiiaMem = "";
	private String loginAllwd = "";
	private String dt = "";
	// attritbute added for identifiying in change password if the password is
	// changed for sec idd user
	private String iddSec = "N";
	// attributes added for IA used to display in IA search by Huda
	private String iaFaxRcvd = GlobalVariables.NO;
	private String contctName = "";
	private String iaPassword = "";
	private String createdDate ="";

	// prarit added for mc name change pending indicator
	private String status = "";
	private String verifiedBy = "";
	private String verifiedDate = "";
	private String newName = null;
	private String iaFax = "";
	private String iaEmail = "";
	private String iddMemberType = "";
	
	private String firstName = "";
	private String lastName = "";
	private String phone = "";
	private String fax = "";
	private String email = "";
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getEpEntities() {
		return epEntities;
	}
	public void setEpEntities(String epEntities) {
		this.epEntities = epEntities;
	}
	public String getScacCode() {
		return scacCode;
	}
	public void setScacCode(String scacCode) {
		this.scacCode = scacCode;
	}
	public String getOldScac() {
		return oldScac;
	}
	public void setOldScac(String oldScac) {
		this.oldScac = oldScac;
	}
	public String getIanaMem() {
		return ianaMem;
	}
	public void setIanaMem(String ianaMem) {
		this.ianaMem = ianaMem;
	}
	public String getNonUiiaEp() {
		return nonUiiaEp;
	}
	public void setNonUiiaEp(String nonUiiaEp) {
		this.nonUiiaEp = nonUiiaEp;
	}
	public String getStatusWTrac() {
		return statusWTrac;
	}
	public void setStatusWTrac(String statusWTrac) {
		this.statusWTrac = statusWTrac;
	}
	public String getUiiaStatus() {
		return uiiaStatus;
	}
	public void setUiiaStatus(String uiiaStatus) {
		this.uiiaStatus = uiiaStatus;
	}
	public String getUiiaStatusCd() {
		return uiiaStatusCd;
	}
	public void setUiiaStatusCd(String uiiaStatusCd) {
		this.uiiaStatusCd = uiiaStatusCd;
	}
	public String getMemType() {
		return memType;
	}
	public void setMemType(String memType) {
		this.memType = memType;
	}
	public String getCompUrl() {
		return compUrl;
	}
	public void setCompUrl(String compUrl) {
		this.compUrl = compUrl;
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
	public String getAttr3() {
		return attr3;
	}
	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}
	public String getMemEffDt() {
		return memEffDt;
	}
	public void setMemEffDt(String memEffDt) {
		this.memEffDt = memEffDt;
	}
	public String getCancelledDt() {
		return cancelledDt;
	}
	public void setCancelledDt(String cancelledDt) {
		this.cancelledDt = cancelledDt;
	}
	public String getDeletedDate() {
		return deletedDate;
	}
	public void setDeletedDate(String deletedDate) {
		this.deletedDate = deletedDate;
	}
	public String getReInstatedDt() {
		return reInstatedDt;
	}
	public void setReInstatedDt(String reInstatedDt) {
		this.reInstatedDt = reInstatedDt;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getOldUiiaStatus() {
		return oldUiiaStatus;
	}
	public void setOldUiiaStatus(String oldUiiaStatus) {
		this.oldUiiaStatus = oldUiiaStatus;
	}
	public String getSecUserName() {
		return secUserName;
	}
	public void setSecUserName(String secUserName) {
		this.secUserName = secUserName;
	}
	public String getOldSecUserName() {
		return oldSecUserName;
	}
	public void setOldSecUserName(String oldSecUserName) {
		this.oldSecUserName = oldSecUserName;
	}
	public String getUiiaMember() {
		return uiiaMember;
	}
	public void setUiiaMember(String uiiaMember) {
		this.uiiaMember = uiiaMember;
	}
	public String getIddMember() {
		return iddMember;
	}
	public void setIddMember(String iddMember) {
		this.iddMember = iddMember;
	}
	public String getIddStatus() {
		return iddStatus;
	}
	public void setIddStatus(String iddStatus) {
		this.iddStatus = iddStatus;
	}
	public String getApplyUiiaMem() {
		return applyUiiaMem;
	}
	public void setApplyUiiaMem(String applyUiiaMem) {
		this.applyUiiaMem = applyUiiaMem;
	}
	public String getLoginAllwd() {
		return loginAllwd;
	}
	public void setLoginAllwd(String loginAllwd) {
		this.loginAllwd = loginAllwd;
	}
	public String getDt() {
		return dt;
	}
	public void setDt(String dt) {
		this.dt = dt;
	}
	public String getIddSec() {
		return iddSec;
	}
	public void setIddSec(String iddSec) {
		this.iddSec = iddSec;
	}
	public String getIaFaxRcvd() {
		return iaFaxRcvd;
	}
	public void setIaFaxRcvd(String iaFaxRcvd) {
		this.iaFaxRcvd = iaFaxRcvd;
	}
	public String getContctName() {
		return contctName;
	}
	public void setContctName(String contctName) {
		this.contctName = contctName;
	}
	public String getIaPassword() {
		return iaPassword;
	}
	public void setIaPassword(String iaPassword) {
		this.iaPassword = iaPassword;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVerifiedBy() {
		return verifiedBy;
	}
	public void setVerifiedBy(String verifiedBy) {
		this.verifiedBy = verifiedBy;
	}
	public String getVerifiedDate() {
		return verifiedDate;
	}
	public void setVerifiedDate(String verifiedDate) {
		this.verifiedDate = verifiedDate;
	}
	public String getNewName() {
		return newName;
	}
	public void setNewName(String newName) {
		this.newName = newName;
	}
	public String getIaFax() {
		return iaFax;
	}
	public void setIaFax(String iaFax) {
		this.iaFax = iaFax;
	}
	public String getIaEmail() {
		return iaEmail;
	}
	public void setIaEmail(String iaEmail) {
		this.iaEmail = iaEmail;
	}
	public String getIddMemberType() {
		return iddMemberType;
	}
	public void setIddMemberType(String iddMemberType) {
		this.iddMemberType = iddMemberType;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int compareTo(AccountInfo aib) {
        return this.companyName.compareTo(aib.getCompanyName());
	}
	
}
