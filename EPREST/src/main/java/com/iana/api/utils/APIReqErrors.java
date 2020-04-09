package com.iana.api.utils;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class APIReqErrors {

	@XmlElement(name = "ERRORS")
	private List<Errors> errors = new ArrayList<>();

	public List<Errors> getErrors() {

		return errors;
	}

	public void setErrors(List<Errors> errors) {

		this.errors = errors;
	}

	@Override
	public String toString() {

		return "APIReqErrors [errors=" + errors + "]";
	}

}
