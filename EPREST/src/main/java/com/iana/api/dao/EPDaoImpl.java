package com.iana.api.dao;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

@Repository
public class EPDaoImpl extends GenericDAO implements EPDao {
	
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;
	
	@Autowired
	public Environment env;
	
}