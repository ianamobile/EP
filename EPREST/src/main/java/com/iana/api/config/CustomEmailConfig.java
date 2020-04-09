package com.iana.api.config;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class CustomEmailConfig {

	private Properties emailProp;

	public Properties getEmailProp() {

		return emailProp;
	}

	public void setEmailProp(Properties emailProp) {

		this.emailProp = emailProp;
	}

}
