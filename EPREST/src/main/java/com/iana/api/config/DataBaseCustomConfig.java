package com.iana.api.config;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@PropertySource(value = { "classpath:jdbc.properties" }, ignoreResourceNotFound = true)
public class DataBaseCustomConfig {

	/* COMMON DATABASE CONFIGURATION PROPERTIES START */
	@Value("${c3p0.driverClassName}")
	private String driverClassName;

	@Value("${c3p0.minPoolSize}")
	private int minSize;

	@Value("${c3p0.maxPoolSize}")
	private int maxSize;

	@Value("${c3p0.acquireIncrement}")
	private int acquireIncrement;

	@Value("${c3p0.maxStatements}")
	private int maxStatements;

	@Value("${c3p0.testConnectionOnCheckin}")
	boolean testConnectionOnCheckin;

	@Value("${c3p0.testConnectionOnCheckout}")
	boolean testConnectionOnCheckout;

	@Value("${c3p0.maxIdleTime}")
	private int maxIdleTime;

	@Value("${c3p0.idleConnectionTestPeriod}")
	private int idleTestPeriod;

	/* COMMON DATABASE CONFIGURATION PROPERTIES END */

	/* INIDIVIDUAL DATABASE CONNECTION PROPERTIES START */
	@Value("${c3p0.datasource.uiia.url}")
	private String uiiaJdbcUrl;

	@Value("${c3p0.datasource.uiia.username}")
	private String uiiaJdbcUsername;

	@Value("${c3p0.datasource.uiia.password}")
	private String uiiaJdbcPassword;

	@Value("${c3p0.datasource.notifemail.url}")
	private String notifEmailJdbcUrl;

	@Value("${c3p0.datasource.notifemail.username}")
	private String notifEmailJdbcUsername;

	@Value("${c3p0.datasource.notifemail.password}")
	private String notifEmailJdbcPassword;
	
	@Value("${c3p0.datasource.notifications.url}")
	private String notificationsJdbcUrl;

	@Value("${c3p0.datasource.notifications.username}")
	private String notificationsJdbcUsername;

	@Value("${c3p0.datasource.notifications.password}")
	private String notificationsJdbcPassword;

	@Value("${c3p0.datasource.pwss.url}")
	private String pwssJdbcUrl;

	@Value("${c3p0.datasource.pwss.username}")
	private String pwssJdbcUsername;

	@Value("${c3p0.datasource.pwss.password}")
	private String pwssJdbcPassword;

	
	
	/* INIDIVIDUAL DATABASE CONNECTION PROPERTIES END */

	@Bean(name = "uiiaDataSource", destroyMethod = "close")
	@Primary
	public DataSource getUiiaDataSource() throws PropertyVetoException {

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		setDataSourceConfiguration(dataSource, this.uiiaJdbcUrl, this.uiiaJdbcUsername, this.uiiaJdbcPassword);
		return dataSource;
	}

	@Bean(name = "notifEmailDataSource", destroyMethod = "close")
	public DataSource getNotifEmailDataSource() throws PropertyVetoException {

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		setDataSourceConfiguration(dataSource, this.notifEmailJdbcUrl, this.notifEmailJdbcUsername, this.notifEmailJdbcPassword);
		return dataSource;
	}
	
	@Bean(name = "notificationsDataSource", destroyMethod = "close")
	public DataSource getNotificationsDataSource() throws PropertyVetoException {

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		setDataSourceConfiguration(dataSource, this.notificationsJdbcUrl, this.notificationsJdbcUsername, this.notificationsJdbcPassword);
		return dataSource;
	}
	
	@Bean(name = "pwssDataSource", destroyMethod = "close")
	public DataSource getPwssDataSource() throws PropertyVetoException {

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		setDataSourceConfiguration(dataSource, this.pwssJdbcUrl, this.pwssJdbcUsername, this.pwssJdbcPassword);
		return dataSource;
	}
 	
	private void setDataSourceConfiguration(ComboPooledDataSource dataSource, String url, String username, String password) throws PropertyVetoException {

		dataSource.setDriverClass(this.driverClassName);
		dataSource.setJdbcUrl(url);
		dataSource.setUser(username);
		dataSource.setPassword(password);

		dataSource.setMinPoolSize(this.minSize);
		dataSource.setMaxPoolSize(this.maxSize);
		dataSource.setAcquireIncrement(this.acquireIncrement);
		dataSource.setMaxStatements(this.maxStatements);

		dataSource.setTestConnectionOnCheckin(this.testConnectionOnCheckin);
		dataSource.setTestConnectionOnCheckout(this.testConnectionOnCheckout);
		dataSource.setIdleConnectionTestPeriod(this.idleTestPeriod);
		dataSource.setMaxIdleTime(this.maxIdleTime);

	}

}
