package com.iana.api.config;

import java.beans.PropertyVetoException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@PropertySource(value = { "classpath:jdbc.properties" }, ignoreResourceNotFound = true)
public class DataBaseCustomConfig {

	/* COMMON DATABASE CONFIGURATION PROPERTIES START */
	
	@Value("${jdbc.driverClassName}")
	private String driverClassName;
	
	@Value("${jdbc.maxPoolSize}")
	private int maxPoolSize;
	
	@Value("${jdbc.idealTimeOut}")
	private long idealTimeOut;
	
	@Value("${jdbc.maxLifeTime}")
	private long maxLifeTime;
	
	@Value("${jdbc.cachePrepStmts}")
	private String cachePrepStmts;
	
	@Value("${jdbc.prepStmtCacheSize}")
	private String prepStmtCacheSize;
	
	@Value("${jdbc.prepStmtCacheSqlLimit}")
	private String prepStmtCacheSqlLimit;
	
	@Value("${jdbc.useServerPrepStmts}")
	private String useServerPrepStmts;
	

	/* COMMON DATABASE CONFIGURATION PROPERTIES END */

	/* INIDIVIDUAL DATABASE CONNECTION PROPERTIES START */
	@Value("${jdbc.url.uiia}")
	private String uiiaJdbcUrl;

	@Value("${jdbc.username.uiia}")
	private String uiiaJdbcUsername;

	@Value("${jdbc.password.uiia}")
	private String uiiaJdbcPassword;

	@Value("${jdbc.url.notifemail}")
	private String notifEmailJdbcUrl;

	@Value("${jdbc.username.notifemail}")
	private String notifEmailJdbcUsername;

	@Value("${jdbc.password.notifemail}")
	private String notifEmailJdbcPassword;
	
	@Value("${jdbc.url.pwss}")
	private String pwssJdbcUrl;

	@Value("${jdbc.username.pwss}")
	private String pwssJdbcUsername;

	@Value("${jdbc.password.pwss}")
	private String pwssJdbcPassword;

	
	
	/* INIDIVIDUAL DATABASE CONNECTION PROPERTIES END */

	@Bean(name = "uiiaDataSource", destroyMethod = "close")
	@Primary
	public HikariDataSource getUiiaDataSource() throws PropertyVetoException {

		HikariConfig configuration = setDataSourceConfiguration(this.uiiaJdbcUrl, this.uiiaJdbcUsername, this.uiiaJdbcPassword);
		return new HikariDataSource(configuration);
	}

	@Bean(name = "notifEmailDataSource", destroyMethod = "close")
	public HikariDataSource getNotifEmailDataSource() throws PropertyVetoException {

		HikariConfig configuration = setDataSourceConfiguration(this.notifEmailJdbcUrl, this.notifEmailJdbcUsername, this.notifEmailJdbcPassword);
		return new HikariDataSource(configuration);
	}
	
	@Bean(name = "pwssDataSource", destroyMethod = "close")
	public HikariDataSource getPwssDataSource() throws PropertyVetoException {

		HikariConfig configuration = setDataSourceConfiguration(this.pwssJdbcUrl, this.pwssJdbcUsername, this.pwssJdbcPassword);
		return new HikariDataSource(configuration);
	}
 	
	private HikariConfig setDataSourceConfiguration(String url, String username, String password) throws PropertyVetoException {

		
	    HikariConfig jdbcConfig = new HikariConfig();
        jdbcConfig.setPoolName("springHikariCP");
        jdbcConfig.setConnectionTestQuery("SELECT 1");
        jdbcConfig.setMaximumPoolSize(this.maxPoolSize);
        jdbcConfig.setIdleTimeout(this.idealTimeOut);
        jdbcConfig.setMaxLifetime(this.maxLifeTime);
        
        jdbcConfig.setDriverClassName(this.driverClassName);
        jdbcConfig.setJdbcUrl(url);
        jdbcConfig.setUsername(username);
        jdbcConfig.setPassword(password);
        
        jdbcConfig.addDataSourceProperty("cachePrepStmts", this.cachePrepStmts);
        jdbcConfig.addDataSourceProperty("prepStmtCacheSize", this.prepStmtCacheSize);
        jdbcConfig.addDataSourceProperty("prepStmtCacheSqlLimit", this.prepStmtCacheSqlLimit);
        jdbcConfig.addDataSourceProperty("useServerPrepStmts", this.useServerPrepStmts);
        
        return jdbcConfig;

	}

}
