package com.iana.api.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiException extends Exception {

	private static final long serialVersionUID = 1L;

	Logger log = LogManager.getLogger(this.getClass().getName());

	@SuppressWarnings("unused")
	private int code;

	public ApiException(int code, String msg) {

		super(msg);
		this.code = code;
		log.error("Excpetion catched : in ApiException(int code, String msg) code= ::" + code + " Msg::" + msg);
	}
}
