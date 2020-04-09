package com.iana.api.utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
public class Errors {

	@XmlElement(name = "TRANSNUM")
	private Integer transNum;

	@XmlElement(name = "ERRORCATEGORY")
	private String errorCategory = StringUtils.EMPTY;

	@XmlElement(name = "ERRORMESSAGE")
	private String errorMessage = StringUtils.EMPTY;

	public Errors(Integer transNum, String errorCategory, String errorMessage) {

		this.transNum = transNum;
		this.errorCategory = errorCategory;
		this.errorMessage = errorMessage;
	}

	public Errors(String errorCategory, String errorMessage) {

		this.errorCategory = errorCategory;
		this.errorMessage = errorMessage;
	}

	public Integer getTransNum() {

		return transNum;
	}

	public void setTransNum(Integer transNum) {

		this.transNum = transNum;
	}

	public String getErrorCategory() {

		return errorCategory;
	}

	public void setErrorCategory(String errorCategory) {

		this.errorCategory = errorCategory;
	}

	public String getErrorMessage() {

		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {

		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {

		return "Errors [transNum=" + transNum + ", errorCategory=" + errorCategory + ", errorMessage=" + errorMessage + "]";
	}

}
