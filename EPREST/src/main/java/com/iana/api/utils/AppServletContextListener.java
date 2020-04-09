package com.iana.api.utils;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.iana.api.dao.GenericDAO;

@Component
public class AppServletContextListener extends GenericDAO {

	Logger log = LogManager.getLogger(this.getClass().getName());

	@PostConstruct
	public void init() throws Exception {
		 
	}
}