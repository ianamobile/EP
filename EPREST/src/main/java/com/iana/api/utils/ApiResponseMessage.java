package com.iana.api.utils;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@XmlRootElement(name = "APIRESPONSE")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiResponseMessage extends GlobalVariables implements Serializable {

	private static final long serialVersionUID = 5271542866557679713L;

	@XmlElement(name = "CODE")
	private int code; // Response Code

	@XmlElement(name = "TYPE")
	private String type; // Response Type

	@XmlElement(name = "MESSAGE")
	private String message = StringUtils.EMPTY; // SUCCESS Response Message

	@XmlElement(name = "DETAILS")
	private Object details; // Additional details about the error

	@XmlElement(name = "APIREQERRORS")
	private APIReqErrors apiReqErrors = new APIReqErrors();

	public ApiResponseMessage() {

	}

	public ApiResponseMessage(int code, String message, APIReqErrors apiReqErrors) {

		this.code = code;
		switch (code) {
			case ERROR:
				setType("error");
				break;
			case WARNING:
				setType("warning");
				break;
			case INFO:
				setType("info");
				break;
			case OK:
				setType("ok");
				break;
			case TOO_BUSY:
				setType("too busy");
				break;
			default:
				setType("unknown");
				break;
		}
		if(StringUtils.isNotBlank(message))
			this.message = message;
		if(apiReqErrors != null)
			this.apiReqErrors = apiReqErrors;
	}
 

	/*
	 * public static void main(String[] args) { ApiResponseMessage a = new ApiResponseMessage();
	 * a.setCode(1); a.setType("error"); a.setMessage("asdfasdf"); APIReqErrors err= new
	 * APIReqErrors(); List<Errors> l = new ArrayList<Errors>(); Errors e = new Errors();
	 * e.setTransNum(10); e.setErrorCategory("Business"); e.setErrorMessage("test"); l.add(e);
	 * err.setErrors(l); a.setApiReqErrors(err);
	 * 
	 * Gson g= new Gson(); System.out.println(g.toJson(a));
	 * 
	 * 
	 * }
	 */

}
