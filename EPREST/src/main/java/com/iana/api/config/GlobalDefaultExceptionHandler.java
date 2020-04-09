package com.iana.api.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.iana.api.service.BaseRestService;
import com.iana.api.utils.ApiResponseMessage;
import com.iana.api.utils.Errors;

@ControllerAdvice
class GlobalDefaultExceptionHandler {

	Logger log = LogManager.getLogger(GlobalDefaultExceptionHandler.class);

	@Autowired
	private BaseRestService restService;

	@Autowired
	private Environment env;

	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponseMessage> defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {

		log.error("GlobalDefaultExceptionHandler Exception::", e);

		List<Errors> errors = new ArrayList<Errors>();
		errors.add(new Errors(1, env.getProperty("server_error"), "Please ensure that all parameters having same datatype as mention in documentation."));

		return new ResponseEntity<ApiResponseMessage>(restService.prepareAPIErrors(errors), HttpStatus.BAD_REQUEST);

	}

}